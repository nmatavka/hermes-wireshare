package org.limewire.ui.compose

import java.awt.Desktop
import java.awt.EventQueue
import java.awt.desktop.AboutHandler
import java.awt.desktop.AppReopenedListener
import java.awt.desktop.OpenFilesHandler
import java.awt.desktop.OpenURIHandler
import java.awt.desktop.PreferencesHandler
import java.awt.desktop.QuitHandler
import java.io.File
import java.util.concurrent.ConcurrentLinkedQueue

object DesktopIntegration {
    private sealed interface DesktopLaunchEvent {
        data class OpenFile(val file: File) : DesktopLaunchEvent
        data class OpenUri(val uri: String) : DesktopLaunchEvent
        data object About : DesktopLaunchEvent
        data object Preferences : DesktopLaunchEvent
        data object Reopen : DesktopLaunchEvent
        data object Quit : DesktopLaunchEvent
    }

    private val queuedEvents = ConcurrentLinkedQueue<DesktopLaunchEvent>()

    @Volatile
    private var registered = false

    @Volatile
    private var controller: ComposeAppController? = null

    fun register() {
        if (registered || !Desktop.isDesktopSupported()) {
            return
        }

        val desktop = Desktop.getDesktop()
        if (desktop.isSupported(Desktop.Action.APP_ABOUT)) {
            desktop.setAboutHandler(AboutHandler { deliverAbout() })
        }
        if (desktop.isSupported(Desktop.Action.APP_PREFERENCES)) {
            desktop.setPreferencesHandler(PreferencesHandler { deliverPreferences() })
        }
        if (desktop.isSupported(Desktop.Action.APP_OPEN_FILE)) {
            desktop.setOpenFileHandler(OpenFilesHandler { event ->
                event.files.forEach(::deliverFileOrQueue)
            })
        }
        if (desktop.isSupported(Desktop.Action.APP_OPEN_URI)) {
            desktop.setOpenURIHandler(OpenURIHandler { event ->
                deliverUriOrQueue(event.uri.toString())
            })
        }
        if (desktop.isSupported(Desktop.Action.APP_QUIT_HANDLER)) {
            desktop.setQuitHandler(QuitHandler { _, response ->
                response.cancelQuit()
                deliverQuit()
            })
        }
        desktop.addAppEventListener(AppReopenedListener { deliverReopen() })
        registered = true
    }

    fun attach(controller: ComposeAppController) {
        this.controller = controller
        drainQueues()
    }

    fun detach(controller: ComposeAppController? = null) {
        val activeController = this.controller ?: return
        if (controller == null || activeController === controller) {
            this.controller = null
            queuedEvents.clear()
        }
    }

    fun enqueueLaunchArgs(args: List<String>) {
        args.forEach { arg ->
            val trimmed = arg.trim()
            if (trimmed.isEmpty() || trimmed == "-startup") {
                return@forEach
            }

            val file = File(trimmed)
            when {
                file.exists() -> deliverFileOrQueue(file)
                trimmed.startsWith("magnet:", ignoreCase = true) -> deliverUriOrQueue(trimmed)
                trimmed.contains("://") -> deliverUriOrQueue(trimmed)
                else -> deliverUriOrQueue(trimmed)
            }
        }
    }

    private fun drainQueues() {
        val activeController = controller ?: return
        var event = queuedEvents.poll()
        while (event != null) {
            val currentEvent = event
            when (currentEvent) {
                is DesktopLaunchEvent.OpenFile -> deliver(activeController) { handleOpenFile(currentEvent.file) }
                is DesktopLaunchEvent.OpenUri -> deliver(activeController) { handleOpenUri(currentEvent.uri) }
                DesktopLaunchEvent.About -> deliver(activeController) { showAbout() }
                DesktopLaunchEvent.Preferences -> deliver(activeController) { showPreferences() }
                DesktopLaunchEvent.Reopen -> deliver(activeController) { restoreApplication() }
                DesktopLaunchEvent.Quit -> deliver(activeController) { requestExit() }
            }
            event = queuedEvents.poll()
        }
    }

    private fun deliverAbout() {
        deliverOrQueue(DesktopLaunchEvent.About)
    }

    private fun deliverPreferences() {
        deliverOrQueue(DesktopLaunchEvent.Preferences)
    }

    private fun deliverReopen() {
        deliverOrQueue(DesktopLaunchEvent.Reopen)
    }

    private fun deliverQuit() {
        deliverOrQueue(DesktopLaunchEvent.Quit)
    }

    private fun deliverFileOrQueue(file: File) {
        deliverOrQueue(DesktopLaunchEvent.OpenFile(file))
    }

    private fun deliverUriOrQueue(uri: String) {
        deliverOrQueue(DesktopLaunchEvent.OpenUri(uri))
    }

    private fun deliverOrQueue(event: DesktopLaunchEvent) {
        val activeController = controller
        if (activeController == null) {
            queuedEvents.add(event)
        } else {
            when (event) {
                is DesktopLaunchEvent.OpenFile -> {
                    val openFile = event
                    deliver(activeController) { handleOpenFile(openFile.file) }
                }
                is DesktopLaunchEvent.OpenUri -> {
                    val openUri = event
                    deliver(activeController) { handleOpenUri(openUri.uri) }
                }
                DesktopLaunchEvent.About -> deliver(activeController) { showAbout() }
                DesktopLaunchEvent.Preferences -> deliver(activeController) { showPreferences() }
                DesktopLaunchEvent.Reopen -> deliver(activeController) { restoreApplication() }
                DesktopLaunchEvent.Quit -> deliver(activeController) { requestExit() }
            }
        }
    }

    private fun deliver(controller: ComposeAppController, action: ComposeAppController.() -> Unit) {
        if (EventQueue.isDispatchThread()) {
            controller.action()
        } else {
            EventQueue.invokeLater { controller.action() }
        }
    }
}
