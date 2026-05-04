package org.limewire.ui.compose

import androidx.compose.runtime.snapshots.SnapshotStateList
import ca.odell.glazedlists.EventList
import ca.odell.glazedlists.event.ListEventListener
import java.awt.EventQueue
import java.beans.PropertyChangeListener
import java.util.IdentityHashMap
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

internal fun runOnUi(block: () -> Unit) {
    if (EventQueue.isDispatchThread()) {
        block()
    } else {
        EventQueue.invokeLater(block)
    }
}

internal fun <T> snapshotEventList(list: EventList<T>): List<T> {
    val lock = list.readWriteLock.readLock()
    lock.lock()
    return try {
        list.toList()
    } finally {
        lock.unlock()
    }
}

internal object ComposePerformanceTracker {
    private val worstNanos = ConcurrentHashMap<String, AtomicLong>()
    private val overBudgetSamples = ConcurrentHashMap<String, AtomicLong>()

    fun <T> measure(label: String, block: () -> T): T {
        val start = System.nanoTime()
        return try {
            block()
        } finally {
            val elapsed = System.nanoTime() - start
            worstNanos.computeIfAbsent(label) { AtomicLong() }.updateAndGet { current ->
                maxOf(current, elapsed)
            }
            if (EventQueue.isDispatchThread() && elapsed >= 16_000_000L) {
                overBudgetSamples.computeIfAbsent(label) { AtomicLong() }.incrementAndGet()
            }
        }
    }
}

internal fun <T, K> reconcileKeyedStateList(
    target: SnapshotStateList<T>,
    items: List<T>,
    keySelector: (T) -> K
) {
    var index = 0
    while (index < items.size) {
        val nextItem = items[index]
        val nextKey = keySelector(nextItem)
        if (index >= target.size) {
            target.add(nextItem)
            index += 1
            continue
        }
        val currentItem = target[index]
        if (keySelector(currentItem) == nextKey) {
            if (currentItem !== nextItem) {
                target[index] = nextItem
            }
            index += 1
            continue
        }
        val existingIndex = findKeyIndex(target, nextKey, index + 1, keySelector)
        if (existingIndex >= 0) {
            val moved = target.removeAt(existingIndex)
            target.add(index, moved)
            if (target[index] !== nextItem) {
                target[index] = nextItem
            }
        } else {
            target.add(index, nextItem)
        }
        index += 1
    }
    while (target.size > items.size) {
        target.removeAt(target.lastIndex)
    }
}

private fun <T, K> findKeyIndex(
    target: SnapshotStateList<T>,
    key: K,
    startIndex: Int,
    keySelector: (T) -> K
): Int {
    for (index in startIndex until target.size) {
        if (keySelector(target[index]) == key) {
            return index
        }
    }
    return -1
}

internal class EventListBinding<T>(
    private val list: EventList<T>,
    private val target: SnapshotStateList<T>,
    private val onChanged: () -> Unit = {},
    private val keyOf: ((T) -> Any)? = null,
    private val addPropertyListener: ((T, PropertyChangeListener) -> Unit)? = null,
    private val removePropertyListener: ((T, PropertyChangeListener) -> Unit)? = null,
    private val coalesceOnChanged: Boolean = false
) : AutoCloseable {
    private val listeners = IdentityHashMap<T, PropertyChangeListener>()
    private var onChangedScheduled = false
    private val listListener = ListEventListener<T> {
        syncList()
    }

    init {
        list.addListEventListener(listListener)
        syncList()
    }

    private fun syncList() {
        val items = snapshotEventList(list)
        runOnUi {
            ComposePerformanceTracker.measure("eventListBinding.sync") {
                if (keyOf == null) {
                    target.clear()
                    target.addAll(items)
                } else {
                    reconcileKeyedStateList(target, items, keyOf)
                }
                syncPropertyListeners(items)
                dispatchOnChanged()
            }
        }
    }

    private fun dispatchOnChanged() {
        if (!coalesceOnChanged) {
            onChanged()
            return
        }
        if (onChangedScheduled) {
            return
        }
        onChangedScheduled = true
        EventQueue.invokeLater {
            onChangedScheduled = false
            onChanged()
        }
    }

    private fun syncPropertyListeners(items: List<T>) {
        val addListener = addPropertyListener ?: return
        val removeListener = removePropertyListener ?: return
        val desiredItems = IdentityHashMap<T, Boolean>(items.size)
        items.forEach { item ->
            desiredItems[item] = true
            if (listeners.containsKey(item)) {
                return@forEach
            }
            val listener = PropertyChangeListener {
                runOnUi { dispatchOnChanged() }
            }
            addListener(item, listener)
            listeners[item] = listener
        }
        val iterator = listeners.entries.iterator()
        while (iterator.hasNext()) {
            val (item, listener) = iterator.next()
            if (desiredItems[item] == true) {
                continue
            }
            removeListener(item, listener)
            iterator.remove()
        }
    }

    private fun detachPropertyListeners() {
        val removeListener = removePropertyListener ?: return
        listeners.entries.forEach { (item, listener) ->
            removeListener(item, listener)
        }
        listeners.clear()
    }

    override fun close() {
        list.removeListEventListener(listListener)
        runOnUi {
            detachPropertyListeners()
        }
    }
}
