package org.limewire.ui.compose.integration

import org.limewire.core.api.file.CategoryManager
import org.limewire.ui.compose.runOnUi
import org.limewire.ui.compose.tr
import java.awt.EventQueue
import java.awt.FileDialog
import java.awt.Frame
import java.awt.GraphicsEnvironment
import java.awt.MenuItem
import java.awt.PopupMenu
import java.awt.SystemTray
import java.awt.Toolkit
import java.awt.TrayIcon
import java.awt.Window
import java.awt.image.BufferedImage
import java.io.File
import java.io.FilenameFilter
import java.util.concurrent.CopyOnWriteArrayList

class AwtDesktopFilePicker : DesktopFilePicker {
    override fun chooseFiles(
        parent: Window?,
        title: String,
        directoriesOnly: Boolean,
        multiple: Boolean,
        filenameFilter: FilenameFilter?,
        initialDirectory: File?
    ): List<File> {
        val owner = (parent as? Frame) ?: Frame().apply {
            isUndecorated = true
            setLocationRelativeTo(null)
            setSize(0, 0)
        }
        val temporaryOwner = parent !is Frame
        val previous = System.getProperty("apple.awt.fileDialogForDirectories")
        if (directoriesOnly) {
            System.setProperty("apple.awt.fileDialogForDirectories", "true")
        }
        try {
            if (temporaryOwner) {
                owner.isVisible = true
            }
            val dialog = FileDialog(owner, title, FileDialog.LOAD)
            dialog.directory = preferredChooserDirectory(initialDirectory)?.absolutePath
            dialog.isMultipleMode = multiple
            dialog.filenameFilter = filenameFilter
            dialog.isVisible = true
            return dialog.files
                ?.filterNotNull()
                ?.toList()
                .orEmpty()
                .also { files ->
                    if (files.isNotEmpty()) {
                        rememberChooserDirectory(files.last().parentFile ?: files.last())
                    }
                }
        } finally {
            if (directoriesOnly) {
                if (previous == null) {
                    System.clearProperty("apple.awt.fileDialogForDirectories")
                } else {
                    System.setProperty("apple.awt.fileDialogForDirectories", previous)
                }
            }
            if (temporaryOwner) {
                owner.dispose()
            }
        }
    }

    override fun chooseSaveFile(
        parent: Window?,
        title: String,
        suggestedName: String,
        filenameFilter: FilenameFilter?,
        initialDirectory: File?
    ): File? {
        val owner = (parent as? Frame) ?: Frame().apply {
            isUndecorated = true
            setLocationRelativeTo(null)
            setSize(0, 0)
        }
        val temporaryOwner = parent !is Frame
        return try {
            if (temporaryOwner) {
                owner.isVisible = true
            }
            FileDialog(owner, title, FileDialog.SAVE).run {
                directory = preferredChooserDirectory(initialDirectory)?.absolutePath
                file = suggestedName
                this.filenameFilter = filenameFilter
                isVisible = true
                this.file?.takeIf { it.isNotBlank() }?.let { chosen ->
                    val directory = directory?.takeIf { it.isNotBlank() }
                    val selected = if (directory.isNullOrBlank()) {
                        File(chosen)
                    } else {
                        File(directory, chosen)
                    }
                    rememberChooserDirectory(selected.parentFile ?: selected)
                    selected
                }
            }
        } finally {
            if (temporaryOwner) {
                owner.dispose()
            }
        }
    }

    private fun preferredChooserDirectory(initialDirectory: File?): File? {
        return initialDirectory
            ?.takeIf { it.exists() && it.isDirectory }
            ?: lastChooserDirectory()
    }

    private fun lastChooserDirectory(): File? = LegacySwingDesktopPlatformBackends.lastChooserDirectory()

    private fun rememberChooserDirectory(file: File) {
        LegacySwingDesktopPlatformBackends.rememberChooserDirectory(file)
    }
}

class SwingDesktopLauncher(
    private val categoryManager: CategoryManager
) : DesktopLauncher {
    override fun open(file: File): DesktopLaunchResult {
        return LegacySwingDesktopPlatformBackends.openFile(file, categoryManager).toComposeResult()
    }

    override fun reveal(file: File): DesktopLaunchResult {
        return LegacySwingDesktopPlatformBackends.revealFile(file).toComposeResult()
    }

    override fun openUri(uri: java.net.URI): DesktopLaunchResult {
        return LegacySwingDesktopPlatformBackends.openUri(uri).toComposeResult()
    }
}

private fun LegacySwingDesktopPlatformBackends.SilentLaunchResult.toComposeResult(): DesktopLaunchResult {
    return if (isSuccessful) {
        DesktopLaunchResult.Success
    } else {
        DesktopLaunchResult.Failure(
            title = title ?: tr("Open"),
            message = message ?: tr("The requested item could not be opened.")
        )
    }
}

class AwtDesktopShellService : DesktopNotifications, ComposeTrayService {
    private val listeners = CopyOnWriteArrayList<ComposeTrayService.Listener>()
    private val tray = if (GraphicsEnvironment.isHeadless() || !SystemTray.isSupported()) null else SystemTray.getSystemTray()
    private val popupMenu = PopupMenu()
    private val showItem = MenuItem("Show WireShare")
    private val transfersItem = MenuItem("Show Transfers")
    private val delayedExitItem = MenuItem("Exit After Transfers")
    private val quitItem = MenuItem("Quit")

    @Volatile
    private var trayIcon: TrayIcon? = null

    @Volatile
    private var trayVisible = false

    @Volatile
    private var delayedExitPending = false

    @Volatile
    private var notificationsEnabled = true

    @Volatile
    private var minimizeToTray = false

    @Volatile
    private var appVisible = true

    @Volatile
    private var notificationAction: (() -> Unit)? = null

    init {
        showItem.addActionListener { runOnUi { listeners.forEach(ComposeTrayService.Listener::restoreRequested) } }
        transfersItem.addActionListener { runOnUi { listeners.forEach(ComposeTrayService.Listener::showTransfersRequested) } }
        delayedExitItem.addActionListener { runOnUi { listeners.forEach(ComposeTrayService.Listener::toggleExitAfterTransfersRequested) } }
        quitItem.addActionListener { runOnUi { listeners.forEach(ComposeTrayService.Listener::quitRequested) } }
        popupMenu.add(showItem)
        popupMenu.add(transfersItem)
        popupMenu.addSeparator()
        popupMenu.add(delayedExitItem)
        popupMenu.addSeparator()
        popupMenu.add(quitItem)
    }

    override fun supportsNotifications(): Boolean = supportsTray()

    override fun supportsTray(): Boolean = tray != null

    override fun activate(listener: ComposeTrayService.Listener) {
        listeners.addIfAbsent(listener)
        syncIconVisibility()
    }

    override fun deactivate(listener: ComposeTrayService.Listener) {
        listeners.remove(listener)
        if (listeners.isEmpty()) {
            removeTrayIcon()
        }
    }

    override fun updateState(
        appVisible: Boolean,
        minimizeToTray: Boolean,
        notificationsEnabled: Boolean,
        delayedExitPending: Boolean
    ) {
        this.appVisible = appVisible
        this.minimizeToTray = minimizeToTray
        this.notificationsEnabled = notificationsEnabled
        this.delayedExitPending = delayedExitPending
        EventQueue.invokeLater {
            showItem.label = if (appVisible) "Show WireShare" else "Restore WireShare"
            delayedExitItem.label = if (delayedExitPending) "Cancel Exit After Transfers" else "Exit After Transfers"
            trayIcon()?.toolTip = if (delayedExitPending) "WireShare • Exit After Transfers" else "WireShare"
            syncIconVisibility()
        }
    }

    override fun showNotification(title: String, body: String, onOpen: (() -> Unit)?) {
        if (!supportsTray()) {
            return
        }
        notificationAction = onOpen
        EventQueue.invokeLater {
            val icon = trayIcon()
            syncIconVisibility(forceVisible = true)
            icon.displayMessage(title, body, TrayIcon.MessageType.INFO)
        }
    }

    override fun showChatNotification(senderName: String, body: String, onReply: () -> Unit) {
        showNotification(tr("Chat from {0}", senderName), body, onReply)
    }

    override fun playAttentionSound() {
        EventQueue.invokeLater {
            runCatching { Toolkit.getDefaultToolkit().beep() }
        }
    }

    private fun trayIcon(): TrayIcon {
        val existing = trayIcon
        if (existing != null) {
            return existing
        }
        val created = TrayIcon(
            BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB),
            "WireShare",
            popupMenu
        ).apply {
            isImageAutoSize = true
            addActionListener {
                val clickAction = notificationAction
                notificationAction = null
                if (clickAction != null) {
                    runOnUi(clickAction)
                } else {
                    runOnUi { listeners.forEach(ComposeTrayService.Listener::restoreRequested) }
                }
            }
        }
        trayIcon = created
        return created
    }

    private fun syncIconVisibility(forceVisible: Boolean = false) {
        if (!supportsTray()) {
            return
        }
        val shouldShow = forceVisible || minimizeToTray || delayedExitPending || notificationsEnabled || !appVisible
        if (shouldShow) {
            ensureTrayIconVisible()
        } else {
            removeTrayIcon()
        }
    }

    private fun ensureTrayIconVisible() {
        if (trayVisible) {
            return
        }
        val tray = tray ?: return
        runCatching {
            tray.add(trayIcon())
            trayVisible = true
        }
    }

    private fun removeTrayIcon() {
        if (!trayVisible) {
            return
        }
        val tray = tray ?: return
        trayIcon?.let(tray::remove)
        trayVisible = false
    }
}
