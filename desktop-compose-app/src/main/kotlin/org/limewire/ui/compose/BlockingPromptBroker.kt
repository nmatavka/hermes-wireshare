package org.limewire.ui.compose

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.limewire.bittorrent.Torrent
import java.awt.EventQueue
import java.util.ArrayDeque
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicReference

sealed interface BlockingPrompt {
    val title: String
    fun resolve(accepted: Boolean)
}

data class BlockingConfirmationPrompt(
    override val title: String,
    val message: String,
    private val resolver: (Boolean) -> Unit
) : BlockingPrompt {
    override fun resolve(accepted: Boolean) {
        resolver(accepted)
    }
}

data class BlockingTorrentSelectionDecision(
    val accepted: Boolean,
    val entries: List<TorrentFileEntryPresentation>,
    val alwaysAskBeforeStarting: Boolean
)

class BlockingTorrentSelectionPrompt(
    override val title: String,
    val message: String,
    val torrentName: String,
    initialEntries: List<TorrentFileEntryPresentation>,
    initialAlwaysAskBeforeStarting: Boolean,
    private val resolver: (BlockingTorrentSelectionDecision) -> Unit
) : BlockingPrompt {
    val entries = mutableStateListOf<TorrentFileEntryPresentation>().apply {
        addAll(initialEntries)
    }
    var alwaysAskBeforeStarting by mutableStateOf(initialAlwaysAskBeforeStarting)

    val selectedCount: Int
        get() = entries.count { it.priority != TorrentFilePriority.DO_NOT_DOWNLOAD }

    fun setPriority(index: Int, priority: TorrentFilePriority) {
        val currentIndex = entries.indexOfFirst { it.index == index }
        if (currentIndex >= 0) {
            entries[currentIndex] = entries[currentIndex].copy(priority = priority)
        }
    }

    fun selectAll() {
        entries.indices.forEach { entryIndex ->
            val entry = entries[entryIndex]
            if (entry.priority == TorrentFilePriority.DO_NOT_DOWNLOAD) {
                entries[entryIndex] = entry.copy(priority = TorrentFilePriority.LOW)
            }
        }
    }

    fun selectNone() {
        entries.indices.forEach { entryIndex ->
            val entry = entries[entryIndex]
            if (entry.priority != TorrentFilePriority.DO_NOT_DOWNLOAD) {
                entries[entryIndex] = entry.copy(priority = TorrentFilePriority.DO_NOT_DOWNLOAD)
            }
        }
    }

    override fun resolve(accepted: Boolean) {
        resolver(
            BlockingTorrentSelectionDecision(
                accepted = accepted,
                entries = entries.toList(),
                alwaysAskBeforeStarting = alwaysAskBeforeStarting
            )
        )
    }
}

class BlockingPromptBroker {
    private val queuedPrompts = ArrayDeque<BlockingPrompt>()
    private val lock = Any()

    var activePrompt by mutableStateOf<BlockingPrompt?>(null)
        private set

    fun confirm(title: String, message: String): Boolean {
        return awaitPrompt { resolver ->
            BlockingConfirmationPrompt(title, message, resolver)
        }
    }

    fun promptTorrentFilePriorities(
        torrent: Torrent,
        title: String,
        message: String,
        alwaysAskBeforeStarting: Boolean,
        persistAlwaysAskBeforeStarting: (Boolean) -> Unit
    ): Boolean {
        if (!torrent.hasMetaData() || torrent.getTorrentInfo() == null) {
            return true
        }

        val rawEntries = torrent.getTorrentFileEntries()
        if (rawEntries.isEmpty()) {
            return true
        }

        val initialEntries = rawEntries.map { entry ->
            TorrentFileEntryPresentation(
                index = entry.index,
                path = entry.path,
                size = entry.size,
                totalDone = entry.totalDone,
                progress = entry.progress,
                priority = TorrentFilePriority.fromValue(entry.priority),
                localPath = torrent.getTorrentDataFile(entry)?.absolutePath
            )
        }

        return awaitPrompt { resolver ->
            BlockingTorrentSelectionPrompt(
                title = title,
                message = message,
                torrentName = torrent.name,
                initialEntries = initialEntries,
                initialAlwaysAskBeforeStarting = alwaysAskBeforeStarting
            ) { decision ->
                if (decision.accepted && torrent.isEditable() && !torrent.isFinished && torrent.isValid) {
                    torrent.lock.lock()
                    try {
                        rawEntries.forEach { entry ->
                            val selected = decision.entries.firstOrNull { it.index == entry.index } ?: return@forEach
                            torrent.setTorrenFileEntryPriority(entry, selected.priority.value)
                        }
                    } finally {
                        torrent.lock.unlock()
                    }
                    persistAlwaysAskBeforeStarting(decision.alwaysAskBeforeStarting)
                }
                resolver(decision.accepted)
            }
        }
    }

    fun resolveActivePrompt(accepted: Boolean) {
        val prompt = synchronized(lock) {
            val current = activePrompt ?: return
            activePrompt = null
            current
        }
        prompt.resolve(accepted)
    }

    private fun enqueue(prompt: BlockingPrompt) {
        val activateImmediately = synchronized(lock) {
            queuedPrompts.addLast(prompt)
            activePrompt == null
        }
        if (activateImmediately) {
            activateNextPrompt()
        }
    }

    private fun activateNextPrompt() {
        val nextPrompt: BlockingPrompt? = synchronized(lock) {
            if (activePrompt != null) {
                return@synchronized null
            }
            if (queuedPrompts.isEmpty()) {
                null
            } else {
                queuedPrompts.removeFirst().also { activePrompt = it }
            }
        }
        if (nextPrompt == null) {
            return
        }
        if (!EventQueue.isDispatchThread()) {
            EventQueue.invokeLater {
                synchronized(lock) {
                    if (activePrompt == null) {
                        activePrompt = nextPrompt
                    }
                }
            }
        }
    }

    private fun <T> awaitPrompt(createPrompt: (((T) -> Unit) -> BlockingPrompt)): T {
        val answer = AtomicReference<T?>(null)
        val waitLatch = CountDownLatch(1)

        enqueue(
            createPrompt { resolved ->
                answer.set(resolved)
                waitLatch.countDown()
                activateNextPrompt()
            }
        )

        ComposePerformanceTracker.measure(
            if (EventQueue.isDispatchThread()) "prompts.await.edt" else "prompts.await"
        ) {
            waitLatch.await()
        }

        @Suppress("UNCHECKED_CAST")
        return answer.get() as T
    }
}
