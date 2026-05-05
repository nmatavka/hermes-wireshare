package org.limewire.ui.compose.integration

import org.limewire.service.MessageCallback
import org.limewire.service.MessageService
import org.limewire.service.Switch
import org.limewire.ui.compose.tr
import java.util.concurrent.CopyOnWriteArrayList

enum class ComposeSystemMessageSeverity {
    INFO,
    ERROR
}

data class ComposeSystemMessage(
    val title: String,
    val message: String,
    val severity: ComposeSystemMessageSeverity,
    val checkboxLabel: String? = null,
    val checkboxInitialChecked: Boolean = false,
    val onCloseWithCheckbox: ((Boolean) -> Unit)? = null
)

interface ComposeSystemMessageService {
    interface Listener {
        fun onSystemMessage(message: ComposeSystemMessage)
    }

    fun install()
    fun addListener(listener: Listener)
    fun removeListener(listener: Listener)
}

class CoreComposeSystemMessageService : ComposeSystemMessageService {
    private val listeners = CopyOnWriteArrayList<ComposeSystemMessageService.Listener>()
    private val pendingMessages = mutableListOf<ComposeSystemMessage>()
    private val pendingLock = Any()

    @Volatile
    private var installed = false

    override fun install() {
        if (installed) {
            return
        }
        installed = true
        MessageService.setCallback(ComposeMessageCallback(this))
    }

    override fun addListener(listener: ComposeSystemMessageService.Listener) {
        listeners += listener
        val pending = synchronized(pendingLock) {
            val snapshot = pendingMessages.toList()
            pendingMessages.clear()
            snapshot
        }
        pending.forEach { message ->
            runCatching { listener.onSystemMessage(message) }
        }
    }

    override fun removeListener(listener: ComposeSystemMessageService.Listener) {
        listeners -= listener
    }

    private fun publish(message: ComposeSystemMessage) {
        if (listeners.isEmpty()) {
            synchronized(pendingLock) {
                if (listeners.isEmpty()) {
                    pendingMessages += message
                    return
                }
            }
        }
        listeners.forEach { listener ->
            runCatching { listener.onSystemMessage(message) }
        }
    }

    private fun publishLocalized(
        severity: ComposeSystemMessageSeverity,
        titleKey: String,
        messageKey: String,
        ignore: Switch? = null,
        args: Array<out Any?> = emptyArray()
    ) {
        if (ignore?.getValue() == true) {
            return
        }
        publish(
            ComposeSystemMessage(
                title = tr(titleKey),
                message = sanitizeMessage(tr(messageKey, *args)),
                severity = severity,
                checkboxLabel = ignore?.let { tr("Do not display this message again") },
                onCloseWithCheckbox = ignore?.let { setting ->
                    { checked -> setting.setValue(checked) }
                }
            )
        )
    }

    private fun sanitizeMessage(message: String): String {
        if (!message.trimStart().startsWith("<html", ignoreCase = true)) {
            return message
        }
        return message
            .replace(Regex("(?i)<br\\s*/?>"), "\n")
            .replace(Regex("(?i)</p>"), "\n\n")
            .replace(Regex("(?i)<li>"), "• ")
            .replace(Regex("(?i)</li>"), "\n")
            .replace(Regex("<[^>]+>"), "")
            .replace("&nbsp;", " ")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&#39;", "'")
            .trim()
    }

    private class ComposeMessageCallback(
        private val service: CoreComposeSystemMessageService
    ) : MessageCallback {
        override fun showError(messageKey: String) {
            service.publishLocalized(
                severity = ComposeSystemMessageSeverity.ERROR,
                titleKey = "Error",
                messageKey = messageKey
            )
        }

        override fun showError(messageKey: String, ignore: Switch) {
            service.publishLocalized(
                severity = ComposeSystemMessageSeverity.ERROR,
                titleKey = "Error",
                messageKey = messageKey,
                ignore = ignore
            )
        }

        override fun showFormattedError(errorKey: String, vararg args: Any?) {
            service.publishLocalized(
                severity = ComposeSystemMessageSeverity.ERROR,
                titleKey = "Error",
                messageKey = errorKey,
                args = args
            )
        }

        override fun showFormattedError(errorKey: String, ignore: Switch, vararg args: Any?) {
            service.publishLocalized(
                severity = ComposeSystemMessageSeverity.ERROR,
                titleKey = "Error",
                messageKey = errorKey,
                ignore = ignore,
                args = args
            )
        }

        override fun showMessage(messageKey: String) {
            service.publishLocalized(
                severity = ComposeSystemMessageSeverity.INFO,
                titleKey = "Message",
                messageKey = messageKey
            )
        }

        override fun showMessage(messageKey: String, ignore: Switch) {
            service.publishLocalized(
                severity = ComposeSystemMessageSeverity.INFO,
                titleKey = "Message",
                messageKey = messageKey,
                ignore = ignore
            )
        }

        override fun showFormattedMessage(messageKey: String, vararg args: Any?) {
            service.publishLocalized(
                severity = ComposeSystemMessageSeverity.INFO,
                titleKey = "Message",
                messageKey = messageKey,
                args = args
            )
        }

        override fun showFormattedMessage(messageKey: String, ignore: Switch, vararg args: Any?) {
            service.publishLocalized(
                severity = ComposeSystemMessageSeverity.INFO,
                titleKey = "Message",
                messageKey = messageKey,
                ignore = ignore,
                args = args
            )
        }
    }
}
