package org.limewire.ui.compose.integration

import ca.odell.glazedlists.event.ListEventListener
import com.google.common.base.Predicate
import com.google.inject.Provider
import com.limegroup.gnutella.ActiveLimeWireCheck
import org.limewire.bittorrent.Torrent
import org.limewire.bittorrent.TorrentManager
import org.limewire.bittorrent.TorrentManagerSettings
import org.limewire.bittorrent.TorrentSettingsAnnotation
import org.limewire.collection.AutoCompleteDictionary
import org.limewire.core.api.Category
import org.limewire.core.api.daap.DaapManager
import org.limewire.core.api.FilePropertyKey
import org.limewire.core.api.callback.GuiCallback
import org.limewire.core.api.download.DownloadException
import org.limewire.core.api.file.CategoryManager
import org.limewire.core.api.library.FriendAutoCompleterFactory
import org.limewire.core.api.library.LibraryManager
import org.limewire.core.api.library.LocalFileItem
import org.limewire.core.api.library.SharedFileListManager
import org.limewire.core.api.network.NetworkManager
import org.limewire.core.api.search.Search
import org.limewire.core.api.search.SearchCategory
import org.limewire.core.api.search.SearchDetails
import org.limewire.core.api.search.SearchManager
import org.limewire.core.api.search.SearchResultList
import org.limewire.core.api.search.browse.BrowseSearchFactory
import org.limewire.core.api.spam.SpamManager
import org.limewire.core.api.xmpp.XMPPResourceFactory
import org.limewire.core.settings.ApplicationSettings
import org.limewire.core.settings.BittorrentSettings
import org.limewire.core.settings.ConnectionSettings
import org.limewire.core.settings.DaapSettings
import org.limewire.core.settings.DHTSettings
import org.limewire.core.settings.DownloadSettings
import org.limewire.core.settings.FilterSettings
import org.limewire.core.settings.FriendSettings
import org.limewire.core.settings.InstallSettings
import org.limewire.core.settings.LibrarySettings
import org.limewire.core.settings.NetworkSettings
import org.limewire.core.settings.SearchSettings
import org.limewire.core.settings.SharingSettings
import org.limewire.core.settings.UltrapeerSettings
import org.limewire.core.settings.UploadSettings
import org.limewire.core.settings.iTunesSettings
import org.limewire.friend.api.ChatState
import org.limewire.friend.api.Friend
import org.limewire.friend.api.FriendConnection
import org.limewire.friend.api.FriendConnectionConfiguration
import org.limewire.friend.api.FriendConnectionEvent
import org.limewire.friend.api.FriendConnectionFactory
import org.limewire.friend.api.FriendException
import org.limewire.friend.api.FriendRequestEvent
import org.limewire.friend.api.Network
import org.limewire.friend.api.PasswordManager
import org.limewire.friend.api.FriendPresence
import org.limewire.io.IP
import org.limewire.io.UnresolvedIpPort
import org.limewire.io.UnresolvedIpPortImpl
import org.limewire.io.IOUtils
import org.limewire.listener.ListenerSupport
import org.limewire.player.api.AudioPlayer
import org.limewire.player.api.AudioPlayerEvent
import org.limewire.player.api.AudioPlayerListener
import org.limewire.player.api.PlayerState
import org.limewire.ui.compose.BlockingPromptBroker
import org.limewire.ui.compose.CategorySaveDirectoriesDraft
import org.limewire.ui.compose.ConversationFileOffer
import org.limewire.ui.compose.ConversationMessage
import org.limewire.ui.compose.ConversationMessageKind
import org.limewire.ui.compose.ConnectionColumn
import org.limewire.ui.compose.ComposeAppearance
import org.limewire.ui.compose.ConnectionLayoutPreferences
import org.limewire.ui.compose.DelayedExitState
import org.limewire.ui.compose.DownloadColumn
import org.limewire.ui.compose.DownloadLayoutPreferences
import org.limewire.ui.compose.DownloadSortMode
import org.limewire.ui.compose.FriendsPaneLayoutPreferences
import org.limewire.ui.compose.FriendLoginDraft
import org.limewire.ui.compose.FriendLoginOption
import org.limewire.ui.compose.FriendRosterItem
import org.limewire.ui.compose.HostFilterPreferencesDraft
import org.limewire.ui.compose.ITunesPreferencesDraft
import org.limewire.ui.compose.NetworkAdvancedPreferencesDraft
import org.limewire.ui.compose.NetworkInterfaceOption
import org.limewire.ui.compose.LibraryColumn
import org.limewire.ui.compose.LibraryLayoutPreferences
import org.limewire.ui.compose.LibraryPaneLayoutPreferences
import org.limewire.ui.compose.LibrarySortMode
import org.limewire.ui.compose.FileAssociationPromptState
import org.limewire.ui.compose.PreferencesDraft
import org.limewire.ui.compose.ProxyMode
import org.limewire.ui.compose.PortForwardMode
import org.limewire.ui.compose.SaveDirectoryValidationResult
import org.limewire.ui.compose.SearchPreferencesDraft
import org.limewire.ui.compose.SecurityLevelOption
import org.limewire.ui.compose.SettingsApplyResult
import org.limewire.ui.compose.SystemPreferencesDraft
import org.limewire.ui.compose.SearchColumn
import org.limewire.ui.compose.SearchLayoutPreferences
import org.limewire.ui.compose.SearchPaneLayoutPreferences
import org.limewire.ui.compose.SearchSortMode
import org.limewire.ui.compose.SearchSuggestionEntry
import org.limewire.ui.compose.SearchSuggestionSource
import org.limewire.ui.compose.TransferPreferencesDraft
import org.limewire.ui.compose.TransferPaneLayoutPreferences
import org.limewire.ui.compose.TransferFilterMode
import org.limewire.ui.compose.TorrentEngineHealthState
import org.limewire.ui.compose.UploadColumn
import org.limewire.ui.compose.UploadLayoutPreferences
import org.limewire.ui.compose.UploadSortMode
import org.limewire.ui.compose.WindowPlacementPreferences
import org.limewire.ui.compose.TrayBehaviorPreferences
import org.limewire.ui.compose.LibraryPreferencesDraft
import org.limewire.ui.compose.PendingFriendRequest
import org.limewire.ui.compose.FriendsNotificationsPreferencesDraft
import org.limewire.ui.compose.AddToLibraryDefaultsDraft
import org.limewire.ui.compose.AdvancedSearchSuggestionEntry
import org.limewire.ui.compose.ComposePerformanceTracker
import org.limewire.ui.compose.runOnUi
import org.limewire.ui.compose.snapshotEventList
import org.limewire.ui.compose.tr
import org.limewire.ui.swing.friends.chat.ChatFriend
import org.limewire.ui.swing.friends.chat.ComposeChatBridge
import org.limewire.ui.swing.friends.settings.FriendAccountConfiguration
import org.limewire.ui.swing.friends.settings.FriendAccountConfigurationManager
import org.limewire.ui.swing.player.PlayerMediator
import org.limewire.ui.swing.player.PlayerMediatorListener
import org.limewire.ui.swing.shell.LimeAssociationOption
import org.limewire.ui.swing.shell.LimeAssociations
import org.limewire.ui.swing.settings.SwingUiSettings
import org.limewire.ui.swing.settings.StartupSettings
import org.limewire.ui.swing.tray.Notification
import org.limewire.ui.swing.tray.TrayNotifier
import org.limewire.ui.swing.util.FilePropertyKeyUtils
import org.limewire.ui.swing.util.I18n
import org.limewire.ui.swing.util.LanguageUtils
import org.limewire.ui.swing.util.MacOSXUtils
import org.limewire.ui.swing.util.NativeLaunchUtils
import org.limewire.ui.swing.util.BackgroundExecutorService
import org.limewire.ui.swing.util.WindowsUtils
import org.limewire.ui.swing.settings.QuestionsHandler
import org.limewire.util.CommonUtils
import org.limewire.util.FileUtils
import org.limewire.util.OSUtils
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
import java.awt.event.ActionEvent
import java.beans.PropertyChangeListener
import java.io.File
import java.io.FilenameFilter
import java.io.IOException
import java.io.RandomAccessFile
import java.util.Collections
import java.util.LinkedHashMap
import java.util.IdentityHashMap
import java.util.Locale
import java.util.Random
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.SocketException
import java.util.concurrent.CopyOnWriteArrayList
import javax.swing.Icon
import javax.swing.AbstractAction
import org.jdesktop.swingx.icon.EmptyIcon
import java.awt.image.BufferedImage

interface DesktopFilePicker {
    fun chooseFiles(
        parent: Window?,
        title: String,
        directoriesOnly: Boolean = false,
        multiple: Boolean = false,
        filenameFilter: FilenameFilter? = null,
        initialDirectory: File? = null
    ): List<File>

    fun chooseSaveFile(
        parent: Window?,
        title: String,
        suggestedName: String,
        filenameFilter: FilenameFilter? = null,
        initialDirectory: File? = null
    ): File?
}

sealed interface DesktopLaunchResult {
    data object Success : DesktopLaunchResult

    data class Failure(
        val title: String,
        val message: String
    ) : DesktopLaunchResult
}

interface DesktopLauncher {
    fun open(file: File): DesktopLaunchResult
    fun reveal(file: File): DesktopLaunchResult
    fun openUri(uri: java.net.URI): DesktopLaunchResult
}

interface DesktopNotifications {
    fun supportsNotifications(): Boolean
    fun showNotification(title: String, body: String, onOpen: (() -> Unit)? = null)
    fun showChatNotification(senderName: String, body: String, onReply: () -> Unit)
    fun playAttentionSound()
}

interface ComposeTrayService {
    interface Listener {
        fun restoreRequested()
        fun showTransfersRequested()
        fun toggleExitAfterTransfersRequested()
        fun quitRequested()
    }

    fun supportsTray(): Boolean
    fun activate(listener: Listener)
    fun deactivate(listener: Listener)
    fun updateState(
        appVisible: Boolean,
        minimizeToTray: Boolean,
        notificationsEnabled: Boolean,
        delayedExitPending: Boolean
    )
}

interface ComposeLocalizationService {
    fun translate(text: String, vararg args: Any?): String
    fun availableLocales(): List<Locale>
    fun currentLocale(): Locale
    fun applyLocale(locale: Locale)
}

interface ComposeSettingsService {
    fun loadPreferences(): PreferencesDraft
    fun applyPreferences(draft: PreferencesDraft): SettingsApplyResult
    fun validateSaveDirectory(path: String): SaveDirectoryValidationResult
    fun torrentEngineHealthState(): TorrentEngineHealthState
    fun torrentPromptBeforeDownloadingEnabled(): Boolean
    fun setTorrentPromptBeforeDownloadingEnabled(enabled: Boolean)
    fun clearDownloadsWhenFinished(): Boolean
    fun setClearDownloadsWhenFinished(enabled: Boolean)
    fun clearUploadsWhenFinished(): Boolean
    fun setClearUploadsWhenFinished(enabled: Boolean)
    fun showUploadsInTray(): Boolean
    fun setShowUploadsInTray(enabled: Boolean)
    fun resolveConnectionHostnamesEnabled(): Boolean
    fun setResolveConnectionHostnamesEnabled(enabled: Boolean)
    fun shouldShowSetupAssociationsPage(): Boolean
    fun validateCurrentSaveDirectory(): String?
    fun startupFileAssociationPrompt(): FileAssociationPromptState?
    fun resolveStartupFileAssociationPrompt(reassociate: Boolean, warnOnChange: Boolean)
    fun playerVolume(): Float
    fun setPlayerVolume(value: Float)
    fun playerEnabled(): Boolean
    fun clearSearchHistory()
    fun recordSearchHistoryEntry(query: String)
    fun resetSpamFilter()
    fun resetWarnings()
    fun confirmBlockUsersEnabled(): Boolean
    fun setConfirmBlockUsersEnabled(enabled: Boolean)
    fun confirmRemoveFileInfoSharingEnabled(): Boolean
    fun setConfirmRemoveFileInfoSharingEnabled(enabled: Boolean)
    fun defaultBlockedExtensions(): List<String>
    fun loadWindowPlacementPreferences(): WindowPlacementPreferences
    fun saveWindowPlacementPreferences(preferences: WindowPlacementPreferences)
    fun loadTrayBehaviorPreferences(): TrayBehaviorPreferences
    fun saveTrayBehaviorPreferences(preferences: TrayBehaviorPreferences)
    fun loadSearchLayoutPreferences(): SearchLayoutPreferences
    fun saveSearchLayoutPreferences(preferences: SearchLayoutPreferences)
    fun loadLibraryLayoutPreferences(): LibraryLayoutPreferences
    fun saveLibraryLayoutPreferences(preferences: LibraryLayoutPreferences)
    fun loadDownloadLayoutPreferences(): DownloadLayoutPreferences
    fun saveDownloadLayoutPreferences(preferences: DownloadLayoutPreferences)
    fun loadUploadLayoutPreferences(): UploadLayoutPreferences
    fun saveUploadLayoutPreferences(preferences: UploadLayoutPreferences)
    fun loadSearchPaneLayoutPreferences(): SearchPaneLayoutPreferences
    fun saveSearchPaneLayoutPreferences(preferences: SearchPaneLayoutPreferences)
    fun loadLibraryPaneLayoutPreferences(): LibraryPaneLayoutPreferences
    fun saveLibraryPaneLayoutPreferences(preferences: LibraryPaneLayoutPreferences)
    fun showLibraryOverlayMessageEnabled(): Boolean
    fun setShowLibraryOverlayMessageEnabled(enabled: Boolean)
    fun showSharingOverlayMessageEnabled(): Boolean
    fun setShowSharingOverlayMessageEnabled(enabled: Boolean)
    fun warnSharingDocumentsWithWorldEnabled(): Boolean
    fun setWarnSharingDocumentsWithWorldEnabled(enabled: Boolean)
    fun removeDocumentsFromPublicLists()
    fun loadFriendsPaneLayoutPreferences(): FriendsPaneLayoutPreferences
    fun saveFriendsPaneLayoutPreferences(preferences: FriendsPaneLayoutPreferences)
    fun loadTransferPaneLayoutPreferences(): TransferPaneLayoutPreferences
    fun saveTransferPaneLayoutPreferences(preferences: TransferPaneLayoutPreferences)
    fun loadConnectionLayoutPreferences(): ConnectionLayoutPreferences
    fun saveConnectionLayoutPreferences(preferences: ConnectionLayoutPreferences)
}

internal fun readComposeAppearanceSetting(): ComposeAppearance {
    return runCatching {
        ComposeAppearance.valueOf(SwingUiSettings.COMPOSE_APPEARANCE.get())
    }.getOrElse { ComposeAppearance.SOLARIZED_LIGHT }
}

interface ComposeSearchSuggestionsService {
    fun suggestions(
        input: String,
        category: SearchCategory,
        includeHistory: Boolean,
        includeSmartSuggestions: Boolean
    ): List<SearchSuggestionEntry>
}

interface ComposeAdvancedSearchSuggestionsService {
    fun suggestions(
        category: SearchCategory,
        key: FilePropertyKey,
        input: String
    ): List<AdvancedSearchSuggestionEntry>
}

interface ComposeRecentDownloadsService {
    fun recentDownloads(): List<File>
    fun clearRecentDownloads()
}

interface ComposeTransferRepairService {
    fun stalledDownloadCount(): Int
    fun fixStalledDownloads(): Int
}

interface ComposeDelayedExitService {
    interface Listener {
        fun stateChanged(state: DelayedExitState)
        fun exitReady()
    }

    fun activate(listener: Listener)
    fun deactivate(listener: Listener)
    fun state(): DelayedExitState
    fun start()
    fun cancel()
}

interface ComposePlayerService {
    interface Listener {
        fun progressUpdated(progress: Float)
        fun mediaChanged(name: String)
        fun stateChanged(state: PlayerState)
    }

    fun activate(listener: Listener)
    fun deactivate(listener: Listener)

    fun status(): PlayerState
    fun currentFile(): File?
    fun isVisible(): Boolean
    fun trackName(): String
    fun isShuffle(): Boolean
    fun isSeekable(): Boolean
    fun isPlayable(file: File): Boolean

    fun setPlaylistAndPlay(item: LocalFileItem, playlist: ca.odell.glazedlists.EventList<LocalFileItem>?)
    fun playFile(file: File)
    fun pause()
    fun resume()
    fun seek(progress: Float)
    fun stop()
    fun next()
    fun previous()
    fun setShuffle(shuffle: Boolean)
    fun setVolume(value: Float)
}

interface ComposeFriendService {
    interface Listener {
        fun rosterChanged(friends: List<FriendRosterItem>)
        fun connectionChanged(event: FriendConnectionEvent)
        fun messageReceived(message: ConversationMessage)
        fun chatStateChanged(friendId: String, state: ChatState)
        fun conversationReady(friendId: String)
        fun friendRequestReceived(request: PendingFriendRequest)
    }

    fun activate()
    fun deactivate()
    fun addListener(listener: Listener)
    fun removeListener(listener: Listener)

    fun roster(): List<FriendRosterItem>
    fun rosterItem(friendId: String): FriendRosterItem?
    fun friendById(friendId: String): Friend?
    fun currentConnection(): FriendConnection?
    fun lastConnectionEvent(): FriendConnectionEvent?
    fun supportsAddRemove(): Boolean
    fun supportsPresenceModes(): Boolean
    fun isDoNotDisturbEnabled(): Boolean

    fun loginOptions(): List<FriendLoginOption>
    fun preferredLoginDraft(): FriendLoginDraft?
    fun loginDraftFor(label: String): FriendLoginDraft?
    fun saveLoginConfiguration(draft: FriendLoginDraft)
    fun submitLogin(draft: FriendLoginDraft)
    fun logout()
    fun setDoNotDisturb(enabled: Boolean)
    fun addFriend(username: String, nickname: String)
    fun removeFriend(friendId: String)
    fun acceptFriendRequest(requestId: Long)
    fun declineFriendRequest(requestId: Long)

    fun markConversationViewed(friendId: String)
    fun sendMessage(friendId: String, text: String)
    fun setChatState(friendId: String, state: ChatState)
    fun closeConversation(friendId: String)
    fun canBrowseFriendLibrary(friendId: String): Boolean
    fun supportsOffTheRecord(friendId: String): Boolean
    fun isOffTheRecordEnabled(friendId: String): Boolean
    fun toggleOffTheRecord(friendId: String)
    fun supportsFileOffers(friendId: String): Boolean
    fun offerFile(friendId: String, file: File)
    fun offerFolder(friendId: String, folder: File)
    fun downloadFileOffer(messageId: String)
}

data class BrowseRequest(
    val title: String,
    val query: String,
    val searchType: SearchDetails.SearchType,
    val search: Search,
    val resultList: SearchResultList
)

interface ComposeBrowseService {
    fun browseAllFriends(): BrowseRequest
    fun browseFriend(friendId: String): BrowseRequest?
    fun browsePresence(presence: FriendPresence): BrowseRequest
    fun browsePresences(presences: Collection<FriendPresence>): BrowseRequest?
}

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

    private fun lastChooserDirectory(): File? {
        val saved = SwingUiSettings.LAST_FILECHOOSER_DIRECTORY.get()
        if (saved != null && saved.exists() && saved.isDirectory) {
            return saved
        }
        return CommonUtils.getUserHomeDir()
            ?.takeIf { it.exists() && it.isDirectory }
            ?: CommonUtils.getCurrentDirectory()?.takeIf { it.exists() && it.isDirectory }
    }

    private fun rememberChooserDirectory(file: File) {
        val directory = if (file.isDirectory) file else file.parentFile
        if (directory != null && directory.exists() && directory.isDirectory) {
            SwingUiSettings.LAST_FILECHOOSER_DIRECTORY.set(directory)
        }
    }
}

class SwingDesktopLauncher(
    private val categoryManager: CategoryManager
) : DesktopLauncher {
    override fun open(file: File): DesktopLaunchResult {
        return NativeLaunchUtils.safeLaunchFileSilently(file, categoryManager).toComposeResult()
    }

    override fun reveal(file: File): DesktopLaunchResult {
        return NativeLaunchUtils.launchExplorerSilently(file).toComposeResult()
    }

    override fun openUri(uri: java.net.URI): DesktopLaunchResult {
        return NativeLaunchUtils.openURLSilently(uri.toString()).toComposeResult()
    }
}

private fun NativeLaunchUtils.LaunchResult.toComposeResult(): DesktopLaunchResult {
    return if (successful()) {
        DesktopLaunchResult.Success
    } else {
        DesktopLaunchResult.Failure(
            title = userTitle() ?: tr("Open"),
            message = userMessage() ?: tr("The requested item could not be opened.")
        )
    }
}

class SwingDesktopNotifications(
    private val trayNotifier: TrayNotifier
) : DesktopNotifications {
    override fun supportsNotifications(): Boolean = trayNotifier.supportsSystemTray()

    override fun showNotification(title: String, body: String, onOpen: (() -> Unit)?) {
        val action = onOpen?.let { callback ->
            object : AbstractAction(tr("Open")) {
                override fun actionPerformed(e: ActionEvent?) {
                    runOnUi(callback)
                }
            }
        }
        trayNotifier.showMessage(
            if (action != null) Notification(title, body, action) else Notification(title, body)
        )
    }

    override fun showChatNotification(senderName: String, body: String, onReply: () -> Unit) {
        showNotification(tr("Chat from {0}", senderName), body, onReply)
    }

    override fun playAttentionSound() {
        EventQueue.invokeLater {
            runCatching { Toolkit.getDefaultToolkit().beep() }
        }
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

class SwingComposeLocalizationService : ComposeLocalizationService {
    override fun translate(text: String, vararg args: Any?): String = I18n.tr(text, *args)

    override fun availableLocales(): List<Locale> = LanguageUtils.getLocales(null).toList()

    override fun currentLocale(): Locale = LanguageUtils.getCurrentLocale()

    override fun applyLocale(locale: Locale) {
        LanguageUtils.setLocale(locale)
    }
}

class CoreComposeSearchSuggestionsService(
    private val searchHistory: AutoCompleteDictionary? = null
) : ComposeSearchSuggestionsService {
    override fun suggestions(
        input: String,
        category: SearchCategory,
        includeHistory: Boolean,
        includeSmartSuggestions: Boolean
    ): List<SearchSuggestionEntry> {
        val normalizedInput = input.trim()
        val suggestions = mutableListOf<SearchSuggestionEntry>()
        val seen = linkedSetOf<String>()

        if (includeHistory) {
            val historyEntries = searchHistory?.getPrefixedBy(normalizedInput).orEmpty()
            historyEntries
                .asSequence()
                .map(String::trim)
                .filter(String::isNotEmpty)
                .forEach { entry ->
                    val key = entry.lowercase(Locale.US)
                    if (seen.add(key)) {
                        suggestions += SearchSuggestionEntry(
                            source = SearchSuggestionSource.HISTORY,
                            title = entry,
                            queryText = entry,
                            category = category
                        )
                    }
                }
        }

        if (includeSmartSuggestions) {
            smartSuggestions(normalizedInput, category).forEach { entry ->
                val key = entry.queryText.lowercase(Locale.US)
                if (seen.add(key)) {
                    suggestions += entry
                }
            }
        }

        return suggestions.take(10)
    }

    private fun smartSuggestions(input: String, category: SearchCategory): List<SearchSuggestionEntry> {
        if (input.isBlank() || input.contains(':')) {
            return emptyList()
        }

        return when (category) {
            SearchCategory.AUDIO -> createAudioSmartSuggestions(input)
            SearchCategory.VIDEO -> createVideoSmartSuggestions(input)
            else -> emptyList()
        }
    }

    private fun createAudioSmartSuggestions(input: String): List<SearchSuggestionEntry> {
        val fields = input.split('-')
        return when {
            fields.size == 1 || (fields.size > 1 && fields[1].trim().isEmpty()) -> listOfNotNull(
                smartSuggestion(SearchCategory.AUDIO, FilePropertyKey.AUTHOR to fields[0].trim()),
                smartSuggestion(SearchCategory.AUDIO, FilePropertyKey.TITLE to fields[0].trim()),
                smartSuggestion(SearchCategory.AUDIO, FilePropertyKey.ALBUM to fields[0].trim())
            )

            fields.size == 2 || (fields.size > 2 && fields[2].trim().isEmpty()) -> listOfNotNull(
                smartSuggestion(
                    SearchCategory.AUDIO,
                    FilePropertyKey.AUTHOR to fields[0].trim(),
                    FilePropertyKey.TITLE to fields[1].trim()
                )
            )

            else -> listOfNotNull(
                smartSuggestion(
                    SearchCategory.AUDIO,
                    FilePropertyKey.AUTHOR to fields[0].trim(),
                    FilePropertyKey.TITLE to fields[1].trim(),
                    FilePropertyKey.ALBUM to fields[2].trim()
                ),
                smartSuggestion(
                    SearchCategory.AUDIO,
                    FilePropertyKey.AUTHOR to fields[0].trim(),
                    FilePropertyKey.ALBUM to fields[1].trim(),
                    FilePropertyKey.TITLE to fields[2].trim()
                )
            )
        }
    }

    private fun createVideoSmartSuggestions(input: String): List<SearchSuggestionEntry> {
        val fields = input.split('-')
        return when {
            fields.size == 1 -> {
                val field = fields[0].trim()
                if (field.matches(Regex("\\d+"))) {
                    listOfNotNull(
                        smartSuggestion(SearchCategory.VIDEO, FilePropertyKey.YEAR to field),
                        smartSuggestion(SearchCategory.VIDEO, FilePropertyKey.TITLE to field)
                    )
                } else {
                    emptyList()
                }
            }

            fields.size > 1 -> {
                val first = fields[0].trim()
                val second = fields[1].trim()
                when {
                    second.matches(Regex("\\d+")) -> listOfNotNull(
                        smartSuggestion(
                            SearchCategory.VIDEO,
                            FilePropertyKey.TITLE to first,
                            FilePropertyKey.YEAR to second
                        )
                    )

                    first.matches(Regex("\\d+")) -> listOfNotNull(
                        smartSuggestion(
                            SearchCategory.VIDEO,
                            FilePropertyKey.YEAR to first,
                            FilePropertyKey.TITLE to second
                        )
                    )

                    else -> emptyList()
                }
            }

            else -> emptyList()
        }
    }

    private fun smartSuggestion(
        category: SearchCategory,
        vararg parts: Pair<FilePropertyKey, String>
    ): SearchSuggestionEntry? {
        val filteredParts = parts
            .mapNotNull { (key, value) ->
                value.takeIf(String::isNotBlank)?.let { key to it.trim() }
            }
        if (filteredParts.isEmpty()) {
            return null
        }

        val advancedDetails = LinkedHashMap<FilePropertyKey, String>()
        filteredParts.forEach { (key, value) -> advancedDetails[key] = value }
        val labels = filteredParts.map { (key, value) -> propertyLabel(key, category) to value }
        val title = when (labels.size) {
            1 -> I18n.tr("{0} is \"{1}\"", labels[0].first, labels[0].second)
            2 -> I18n.tr(
                "{0} \"{1}\" - {2} \"{3}\"",
                labels[0].first,
                labels[0].second,
                labels[1].first,
                labels[1].second
            )

            else -> I18n.tr(
                "{0} \"{1}\" - {2} \"{3}\" - {4} \"{5}\"",
                labels[0].first,
                labels[0].second,
                labels[1].first,
                labels[1].second,
                labels[2].first,
                labels[2].second
            )
        }
        val queryText = filteredParts.joinToString(" ") { (key, value) ->
            "${propertyLabel(key, category).lowercase(Locale.getDefault())}:$value"
        }

        return SearchSuggestionEntry(
            source = SearchSuggestionSource.SMART,
            title = title,
            queryText = queryText,
            subtitle = queryText,
            category = category,
            advancedDetails = advancedDetails
        )
    }

    private fun propertyLabel(key: FilePropertyKey, category: SearchCategory): String {
        return I18n.tr(FilePropertyKeyUtils.getUntraslatedDisplayName(key, category))
    }
}

class CoreComposeAdvancedSearchSuggestionsService(
    private val friendAutoCompleterFactory: FriendAutoCompleterFactory
) : ComposeAdvancedSearchSuggestionsService {
    override fun suggestions(
        category: SearchCategory,
        key: FilePropertyKey,
        input: String
    ): List<AdvancedSearchSuggestionEntry> {
        val normalizedInput = input.trim()
        if (normalizedInput.isBlank()) {
            return emptyList()
        }

        val dictionary = friendAutoCompleterFactory.getDictionary(category, key)
        return try {
            dictionary.getPrefixedBy(normalizedInput)
                .orEmpty()
                .asSequence()
                .map(String::trim)
                .filter(String::isNotEmpty)
                .distinctBy { it.lowercase(Locale.US) }
                .take(8)
                .map(::AdvancedSearchSuggestionEntry)
                .toList()
        } catch (_: InterruptedException) {
            Thread.currentThread().interrupt()
            emptyList()
        }
    }
}

class SwingComposeSettingsService(
    private val friendAccountConfigurationManager: FriendAccountConfigurationManager? = null,
    private val searchHistory: AutoCompleteDictionary? = null,
    private val spamManager: SpamManager? = null,
    private val daapManager: DaapManager? = null,
    private val categoryManager: CategoryManager? = null,
    private val libraryManager: LibraryManager? = null,
    private val sharedFileListManager: SharedFileListManager? = null,
    private val networkManager: NetworkManager? = null,
    private val connectionManager: org.limewire.core.api.connection.GnutellaConnectionManager? = null,
    private val torrentManager: Provider<TorrentManager>? = null,
    private val torrentManagerSettings: TorrentManagerSettings? = null
) : ComposeSettingsService {
    override fun loadPreferences(): PreferencesDraft {
        return PreferencesDraft(
            search = SearchPreferencesDraft(
                defaultCategory = defaultSearchCategory(),
                showSmartSuggestions = SwingUiSettings.SHOW_SMART_SUGGESTIONS.getValue(),
                keepSearchHistory = SwingUiSettings.KEEP_SEARCH_HISTORY.getValue(),
                groupSimilarResults = SwingUiSettings.GROUP_SIMILAR_RESULTS_ENABLED.getValue(),
                useTorrentWebSearch = SearchSettings.USE_TORRENT_WEB_SEARCH.getValue(),
                filterAdultContent = FilterSettings.FILTER_ADULT.getValue(),
                hostFilters = HostFilterPreferencesDraft(
                    enabled = FilterSettings.USE_NETWORK_FILTER.getValue(),
                    blockedHosts = FilterSettings.BLACK_LISTED_IP_ADDRESSES.get().toList(),
                    allowedHosts = FilterSettings.WHITE_LISTED_IP_ADDRESSES.get().toList()
                ),
                blockedKeywords = FilterSettings.BANNED_WORDS.get().toList(),
                blockedExtensions = FilterSettings.BANNED_EXTENSIONS.get().toList().sorted()
            ),
            library = LibraryPreferencesDraft(
                playerEnabled = SwingUiSettings.PLAYER_ENABLED.getValue(),
                handleMagnets = SwingUiSettings.HANDLE_MAGNETS.getValue(),
                handleTorrents = SwingUiSettings.HANDLE_TORRENTS.getValue(),
                warnFileAssociationChanges = SwingUiSettings.WARN_FILE_ASSOCIATION_CHANGES.getValue(),
                shareDownloadedFiles = SharingSettings.SHARE_DOWNLOADED_FILES_IN_NON_SHARED_DIRECTORIES.getValue(),
                allowPartialSharing = SharingSettings.ALLOW_PARTIAL_SHARING.getValue(),
                allowProgramSearchAndShare = LibrarySettings.ALLOW_PROGRAMS.getValue(),
                allowDocumentSharing = LibrarySettings.ALLOW_DOCUMENT_GNUTELLA_SHARING.getValue(),
                addToLibraryDefaults = AddToLibraryDefaultsDraft(
                    audio = SwingUiSettings.CATEGORY_AUDIO_DEFAULT.getValue(),
                    video = SwingUiSettings.CATEGORY_VIDEO_DEFAULT.getValue(),
                    images = SwingUiSettings.CATEGORY_IMAGES_DEFAULT.getValue(),
                    documents = SwingUiSettings.CATEGORY_DOCUMENTS_DEFAULT.getValue(),
                    programs = SwingUiSettings.CATEGORY_PROGRAMS_DEFAULT.getValue()
                ),
                iTunes = ITunesPreferencesDraft(
                    supported = daapManager != null,
                    addDownloadedAudioToLibrary = iTunesSettings.ITUNES_SUPPORT_ENABLED.getValue(),
                    shareAudioAcrossLan = DaapSettings.DAAP_ENABLED.getValue(),
                    requirePassword = DaapSettings.DAAP_REQUIRES_PASSWORD.getValue(),
                    password = DaapSettings.DAAP_PASSWORD.get()
                )
            ),
            transfers = TransferPreferencesDraft(
                uploadTorrentsForever = BittorrentSettings.UPLOAD_TORRENTS_FOREVER.getValue(),
                torrentSeedRatio = formatSeedRatio(BittorrentSettings.LIBTORRENT_SEED_RATIO_LIMIT.getValue()),
                torrentSeedDays = wholeDays(BittorrentSettings.LIBTORRENT_SEED_TIME_LIMIT.getValue()).toString(),
                torrentSeedHours = remainingHours(BittorrentSettings.LIBTORRENT_SEED_TIME_LIMIT.getValue()).toString(),
                showTorrentSelectorBeforeDownloading = BittorrentSettings.TORRENT_SHOW_POPUP_BEFORE_DOWNLOADING.getValue(),
                autoRenameDuplicateFiles = SwingUiSettings.AUTO_RENAME_DUPLICATE_FILES.getValue(),
                showTransfersTrayByDefault = SwingUiSettings.SHOW_TRANSFERS_TRAY.getValue(),
                closeTrayWhenNoTransfers = SwingUiSettings.HIDE_BOTTOM_TRAY_WHEN_NO_TRANSFERS.getValue(),
                showTotalBandwidth = SwingUiSettings.SHOW_TOTAL_BANDWIDTH.getValue(),
                clearDownloadsWhenFinished = SharingSettings.CLEAR_DOWNLOAD.getValue(),
                clearUploadsWhenFinished = SharingSettings.CLEAR_UPLOAD.getValue(),
                useCategorySpecificFolders = isCategorySaveDirectoriesCustom(),
                downloadDirectory = SharingSettings.getSaveDirectory().absolutePath,
                categorySaveDirectories = loadCategorySaveDirectories(),
                maxDownloadsAtOnce = DownloadSettings.MAX_SIM_DOWNLOAD.getValue().toString(),
                maxUploadsAtOnce = UploadSettings.HARD_MAX_UPLOADS.getValue().toString(),
                limitDownloadBandwidth = DownloadSettings.LIMIT_MAX_DOWNLOAD_SPEED.getValue(),
                limitUploadBandwidth = UploadSettings.LIMIT_MAX_UPLOAD_SPEED.getValue(),
                maxDownloadKiB = bytesToKiBString(DownloadSettings.MAX_DOWNLOAD_SPEED.getValue()),
                maxUploadKiB = bytesToKiBString(UploadSettings.MAX_UPLOAD_SPEED.getValue())
            ),
            friends = FriendsNotificationsPreferencesDraft(
                showNotifications = SwingUiSettings.SHOW_NOTIFICATIONS.getValue(),
                playNotificationSound = SwingUiSettings.PLAY_NOTIFICATION_SOUND.getValue(),
                autoLoginConfigured = friendAccountConfigurationManager?.autoLoginConfig != null,
                autoLoginEnabled = friendAccountConfigurationManager?.autoLoginConfig != null
            ),
            system = SystemPreferencesDraft(
                connectOnStartup = ConnectionSettings.CONNECT_ON_STARTUP.getValue(),
                runOnStartup = StartupSettings.RUN_ON_STARTUP.getValue(),
                runOnStartupSupported = supportsRunOnStartup(),
                minimizeToTray = SwingUiSettings.MINIMIZE_TO_TRAY.getValue(),
                restoreWindowPlacement = SwingUiSettings.COMPOSE_RESTORE_WINDOW_PLACEMENT.getValue(),
                localRestAccessEnabled = ApplicationSettings.LOCAL_REST_ACCESS_ENABLED.getValue(),
                securityLevel = SecurityLevelOption.fromLevel(InstallSettings.SECURITY_LEVEL.getValue()),
                appearance = readComposeAppearanceSetting()
            ),
            network = NetworkAdvancedPreferencesDraft(
                proxyMode = currentProxyMode(),
                proxyHost = ConnectionSettings.PROXY_HOST.get(),
                proxyPort = ConnectionSettings.PROXY_PORT.getValue().takeIf { it > 0 }?.toString().orEmpty(),
                proxyAuthenticate = ConnectionSettings.PROXY_AUTHENTICATE.getValue(),
                proxyUsername = ConnectionSettings.PROXY_USERNAME.get(),
                proxyPassword = ConnectionSettings.PROXY_PASS.get(),
                torrentUseUpnp = BittorrentSettings.TORRENT_USE_UPNP.getValue(),
                torrentListenStartPort = BittorrentSettings.LIBTORRENT_LISTEN_START_PORT.getValue().toString(),
                torrentListenEndPort = BittorrentSettings.LIBTORRENT_LISTEN_END_PORT.getValue().toString(),
                disableUltrapeer = UltrapeerSettings.DISABLE_ULTRAPEER_MODE.getValue(),
                disableMojito = DHTSettings.DISABLE_DHT_USER.getValue(),
                disableTls = currentTlsDisabled(),
                disableOutOfBandSearch = !SearchSettings.OOB_ENABLED.getValue(),
                gnutellaPort = NetworkSettings.PORT.getValue().toString(),
                portForwardMode = currentPortForwardMode(),
                manualPort = ConnectionSettings.FORCED_PORT.getValue().toString(),
                useCustomNetworkInterface = ConnectionSettings.CUSTOM_NETWORK_INTERFACE.getValue(),
                selectedNetworkInterfaceAddress = ConnectionSettings.CUSTOM_INETADRESS.get()
                    .takeIf { it.isNotBlank() && it != "0.0.0.0" },
                availableNetworkInterfaces = availableNetworkInterfaces()
            )
        )
    }

    override fun applyPreferences(draft: PreferencesDraft): SettingsApplyResult {
        var restartRequired = false
        var restartMessage: String? = null

        val defaultSearchCategory = sanitizeDefaultSearchCategory(
            draft.search.defaultCategory,
            draft.library.allowProgramSearchAndShare
        )
        val spamSettingsChanged =
            FilterSettings.FILTER_ADULT.getValue() != draft.search.filterAdultContent ||
                FilterSettings.BANNED_WORDS.get().toList() != draft.search.blockedKeywords ||
                FilterSettings.BANNED_EXTENSIONS.get().toList().sorted() != draft.search.blockedExtensions.sorted()
        val hostFilterSettingsChanged =
            FilterSettings.USE_NETWORK_FILTER.getValue() != draft.search.hostFilters.enabled ||
                FilterSettings.BLACK_LISTED_IP_ADDRESSES.get().toList() != draft.search.hostFilters.blockedHosts ||
                FilterSettings.WHITE_LISTED_IP_ADDRESSES.get().toList() != draft.search.hostFilters.allowedHosts

        SwingUiSettings.DEFAULT_SEARCH_CATEGORY_ID.setValue(defaultSearchCategory.id)
        SwingUiSettings.SHOW_SMART_SUGGESTIONS.setValue(draft.search.showSmartSuggestions)
        SwingUiSettings.KEEP_SEARCH_HISTORY.setValue(draft.search.keepSearchHistory)
        SwingUiSettings.GROUP_SIMILAR_RESULTS_ENABLED.setValue(draft.search.groupSimilarResults)
        SearchSettings.USE_TORRENT_WEB_SEARCH.setValue(draft.search.useTorrentWebSearch)
        FilterSettings.FILTER_ADULT.setValue(draft.search.filterAdultContent)
        FilterSettings.USE_NETWORK_FILTER.setValue(draft.search.hostFilters.enabled)
        FilterSettings.BLACK_LISTED_IP_ADDRESSES.set(normalizeHostEntries(draft.search.hostFilters.blockedHosts).toTypedArray())
        FilterSettings.WHITE_LISTED_IP_ADDRESSES.set(normalizeHostEntries(draft.search.hostFilters.allowedHosts).toTypedArray())
        FilterSettings.BANNED_WORDS.set(draft.search.blockedKeywords.toTypedArray())
        FilterSettings.BANNED_EXTENSIONS.set(normalizeExtensions(draft.search.blockedExtensions).toTypedArray())
        if (hostFilterSettingsChanged) {
            spamManager?.reloadIPFilter()
        }
        if (spamSettingsChanged) {
            spamManager?.adjustSpamFilters()
        }

        val allowProgramsBefore = LibrarySettings.ALLOW_PROGRAMS.getValue()
        val allowDocumentsBefore = LibrarySettings.ALLOW_DOCUMENT_GNUTELLA_SHARING.getValue()
        SwingUiSettings.HANDLE_MAGNETS.setValue(draft.library.handleMagnets)
        SwingUiSettings.HANDLE_TORRENTS.setValue(draft.library.handleTorrents)
        SwingUiSettings.WARN_FILE_ASSOCIATION_CHANGES.setValue(draft.library.warnFileAssociationChanges)
        applyAssociationSettings(draft.library.handleMagnets, draft.library.handleTorrents)
        SwingUiSettings.PLAYER_ENABLED.setValue(draft.library.playerEnabled)
        SwingUiSettings.CATEGORY_AUDIO_DEFAULT.setValue(draft.library.addToLibraryDefaults.audio)
        SwingUiSettings.CATEGORY_VIDEO_DEFAULT.setValue(draft.library.addToLibraryDefaults.video)
        SwingUiSettings.CATEGORY_IMAGES_DEFAULT.setValue(draft.library.addToLibraryDefaults.images)
        SwingUiSettings.CATEGORY_DOCUMENTS_DEFAULT.setValue(draft.library.addToLibraryDefaults.documents)
        SwingUiSettings.CATEGORY_PROGRAMS_DEFAULT.setValue(draft.library.addToLibraryDefaults.programs)
        SharingSettings.SHARE_DOWNLOADED_FILES_IN_NON_SHARED_DIRECTORIES.setValue(draft.library.shareDownloadedFiles)
        SharingSettings.ALLOW_PARTIAL_SHARING.setValue(draft.library.allowPartialSharing)
        LibrarySettings.ALLOW_PROGRAMS.setValue(draft.library.allowProgramSearchAndShare)
        LibrarySettings.ALLOW_DOCUMENT_GNUTELLA_SHARING.setValue(draft.library.allowDocumentSharing)

        if (allowProgramsBefore && !draft.library.allowProgramSearchAndShare) {
            libraryManager?.libraryManagedList?.removeFiles(Predicate<LocalFileItem> { it.category == Category.PROGRAM })
        }
        if (allowDocumentsBefore && !draft.library.allowDocumentSharing) {
            sharedFileListManager?.removeDocumentsFromPublicLists()
        }
        applyITunesPreferences(draft.library.iTunes)

        val requestedDirectory = draft.transfers.downloadDirectory.trim()
        if (requestedDirectory.isEmpty()) {
            throw IllegalArgumentException(tr("Choose a download folder to continue."))
        }
        val previousDefaultDirectory = canonicalizeFile(SharingSettings.getSaveDirectory())
        val resolvedDirectory = requireValidatedSaveDirectory(requestedDirectory)
        SharingSettings.setSaveDirectory(resolvedDirectory)
        if (draft.transfers.useCategorySpecificFolders) {
            applyCategorySaveDirectories(draft.transfers.categorySaveDirectories, resolvedDirectory, previousDefaultDirectory)
        } else {
            revertCategorySaveDirectoriesToDefault()
        }

        BittorrentSettings.UPLOAD_TORRENTS_FOREVER.setValue(draft.transfers.uploadTorrentsForever)
        if (!draft.transfers.uploadTorrentsForever) {
            BittorrentSettings.LIBTORRENT_SEED_RATIO_LIMIT.setValue(
                parseBoundedFloat(
                    draft.transfers.torrentSeedRatio,
                    BittorrentSettings.LIBTORRENT_SEED_RATIO_LIMIT.getMinValue(),
                    BittorrentSettings.LIBTORRENT_SEED_RATIO_LIMIT.getMaxValue(),
                    BittorrentSettings.LIBTORRENT_SEED_RATIO_LIMIT.getValue()
                )
            )
            BittorrentSettings.LIBTORRENT_SEED_TIME_LIMIT.setValue(
                parseSeedTimeLimit(
                    draft.transfers.torrentSeedDays,
                    draft.transfers.torrentSeedHours,
                    BittorrentSettings.LIBTORRENT_SEED_TIME_LIMIT.getValue()
                )
            )
        }
        BittorrentSettings.TORRENT_SHOW_POPUP_BEFORE_DOWNLOADING.setValue(draft.transfers.showTorrentSelectorBeforeDownloading)
        SwingUiSettings.AUTO_RENAME_DUPLICATE_FILES.setValue(draft.transfers.autoRenameDuplicateFiles)
        SwingUiSettings.SHOW_TRANSFERS_TRAY.setValue(draft.transfers.showTransfersTrayByDefault)
        SwingUiSettings.HIDE_BOTTOM_TRAY_WHEN_NO_TRANSFERS.setValue(draft.transfers.closeTrayWhenNoTransfers)
        SwingUiSettings.SHOW_TOTAL_BANDWIDTH.setValue(draft.transfers.showTotalBandwidth)
        SharingSettings.CLEAR_DOWNLOAD.setValue(draft.transfers.clearDownloadsWhenFinished)
        SharingSettings.CLEAR_UPLOAD.setValue(draft.transfers.clearUploadsWhenFinished)
        DownloadSettings.MAX_SIM_DOWNLOAD.setValue(parseBoundedInt(draft.transfers.maxDownloadsAtOnce, 1, 999, DownloadSettings.MAX_SIM_DOWNLOAD.getValue()))
        UploadSettings.HARD_MAX_UPLOADS.setValue(parseBoundedInt(draft.transfers.maxUploadsAtOnce, 0, 50, UploadSettings.HARD_MAX_UPLOADS.getValue()))
        DownloadSettings.LIMIT_MAX_DOWNLOAD_SPEED.setValue(draft.transfers.limitDownloadBandwidth)
        UploadSettings.LIMIT_MAX_UPLOAD_SPEED.setValue(draft.transfers.limitUploadBandwidth)
        DownloadSettings.MAX_DOWNLOAD_SPEED.setValue(parseKiBSetting(draft.transfers.maxDownloadKiB))
        UploadSettings.MAX_UPLOAD_SPEED.setValue(parseKiBSetting(draft.transfers.maxUploadKiB))

        SwingUiSettings.SHOW_NOTIFICATIONS.setValue(draft.friends.showNotifications)
        SwingUiSettings.PLAY_NOTIFICATION_SOUND.setValue(draft.friends.playNotificationSound)

        ConnectionSettings.CONNECT_ON_STARTUP.setValue(draft.system.connectOnStartup)
        val runOnStartupChanged = StartupSettings.RUN_ON_STARTUP.getValue() != draft.system.runOnStartup
        if (runOnStartupChanged && draft.system.runOnStartupSupported) {
            applyRunOnStartup(draft.system.runOnStartup)
        }
        StartupSettings.RUN_ON_STARTUP.setValue(draft.system.runOnStartupSupported && draft.system.runOnStartup)
        SwingUiSettings.MINIMIZE_TO_TRAY.setValue(draft.system.minimizeToTray)
        SwingUiSettings.COMPOSE_RESTORE_WINDOW_PLACEMENT.setValue(draft.system.restoreWindowPlacement)
        SwingUiSettings.COMPOSE_APPEARANCE.set(draft.system.appearance.name)
        ApplicationSettings.LOCAL_REST_ACCESS_ENABLED.setValue(draft.system.localRestAccessEnabled)

        if (InstallSettings.SECURITY_LEVEL.getValue() != draft.system.securityLevel.level) {
            InstallSettings.SECURITY_LEVEL.setValue(draft.system.securityLevel.level)
            if (draft.system.securityLevel == SecurityLevelOption.NONE) {
                InstallSettings.SECURITY_UPDATE.setValue(false)
                File(CommonUtils.getUserSettingsDir(), "hostiles.txt").takeIf(File::exists)?.delete()
            } else {
                InstallSettings.SECURITY_UPDATE.setValue(true)
            }
            restartRequired = true
        }

        val portRestartRequired = applyNetworkPreferences(draft.network)
        restartRequired = restartRequired || portRestartRequired

        syncTorrentManagerSettings()

        if (restartRequired) {
            restartMessage = tr("One or more settings will take effect after restarting WireShare.")
        }
        return SettingsApplyResult(restartRequired = restartRequired, restartMessage = restartMessage)
    }

    override fun torrentEngineHealthState(): TorrentEngineHealthState {
        val provider = torrentManager ?: return TorrentEngineHealthState.ERROR
        return runCatching {
            val manager = provider.get()
            when {
                !manager.isInitialized -> TorrentEngineHealthState.STARTING
                manager.isValid -> TorrentEngineHealthState.READY
                else -> TorrentEngineHealthState.ERROR
            }
        }.getOrElse { TorrentEngineHealthState.ERROR }
    }

    override fun torrentPromptBeforeDownloadingEnabled(): Boolean {
        return BittorrentSettings.TORRENT_SHOW_POPUP_BEFORE_DOWNLOADING.getValue()
    }

    override fun setTorrentPromptBeforeDownloadingEnabled(enabled: Boolean) {
        BittorrentSettings.TORRENT_SHOW_POPUP_BEFORE_DOWNLOADING.setValue(enabled)
    }

    override fun clearDownloadsWhenFinished(): Boolean = SharingSettings.CLEAR_DOWNLOAD.getValue()

    override fun setClearDownloadsWhenFinished(enabled: Boolean) {
        SharingSettings.CLEAR_DOWNLOAD.setValue(enabled)
    }

    override fun clearUploadsWhenFinished(): Boolean = SharingSettings.CLEAR_UPLOAD.getValue()

    override fun setClearUploadsWhenFinished(enabled: Boolean) {
        SharingSettings.CLEAR_UPLOAD.setValue(enabled)
    }

    override fun showUploadsInTray(): Boolean = UploadSettings.SHOW_UPLOADS_IN_TRAY.getValue()

    override fun setShowUploadsInTray(enabled: Boolean) {
        UploadSettings.SHOW_UPLOADS_IN_TRAY.setValue(enabled)
    }

    override fun resolveConnectionHostnamesEnabled(): Boolean {
        return SwingUiSettings.RESOLVE_CONNECTION_HOSTNAMES.getValue()
    }

    override fun setResolveConnectionHostnamesEnabled(enabled: Boolean) {
        SwingUiSettings.RESOLVE_CONNECTION_HOSTNAMES.setValue(enabled)
    }

    override fun shouldShowSetupAssociationsPage(): Boolean {
        return LimeAssociations.isMagnetAssociationSupported() ||
            LimeAssociations.isTorrentAssociationSupported() ||
            !FilterSettings.FILTER_ADULT.getValue() ||
            (!InstallSettings.START_STARTUP.getValue() && supportsRunOnStartup())
    }

    override fun validateSaveDirectory(path: String): SaveDirectoryValidationResult {
        val trimmed = path.trim()
        if (trimmed.isEmpty()) {
            return SaveDirectoryValidationResult(
                accepted = false,
                errorMessage = tr("Choose a download folder to continue.")
            )
        }
        return validateSaveDirectory(File(trimmed))
    }

    override fun validateCurrentSaveDirectory(): String? {
        return validateSaveDirectory(SharingSettings.getSaveDirectory()).errorMessage
    }

    override fun startupFileAssociationPrompt(): FileAssociationPromptState? {
        val torrentAssociationOption = LimeAssociations.getTorrentAssociation()
        applyAvailableAssociation(torrentAssociationOption, SwingUiSettings.HANDLE_TORRENTS)

        val magnetAssociationOption = LimeAssociations.getMagnetAssociation()
        applyAvailableAssociation(magnetAssociationOption, SwingUiSettings.HANDLE_MAGNETS)

        val torrentsStolen = isSettingStolen(torrentAssociationOption, SwingUiSettings.HANDLE_TORRENTS)
        val magnetsStolen = isSettingStolen(magnetAssociationOption, SwingUiSettings.HANDLE_MAGNETS)

        if (SwingUiSettings.WARN_FILE_ASSOCIATION_CHANGES.getValue() && (torrentsStolen || magnetsStolen)) {
            return FileAssociationPromptState(
                message = startupAssociationMessage(torrentsStolen, magnetsStolen),
                warnOnChange = SwingUiSettings.WARN_FILE_ASSOCIATION_CHANGES.getValue()
            )
        }

        syncAssociationSettings(torrentAssociationOption, magnetAssociationOption)
        return null
    }

    override fun resolveStartupFileAssociationPrompt(reassociate: Boolean, warnOnChange: Boolean) {
        SwingUiSettings.WARN_FILE_ASSOCIATION_CHANGES.setValue(warnOnChange)
        val torrentAssociationOption = LimeAssociations.getTorrentAssociation()
        val magnetAssociationOption = LimeAssociations.getMagnetAssociation()
        if (reassociate) {
            fixAssociation(torrentAssociationOption, SwingUiSettings.HANDLE_TORRENTS)
            fixAssociation(magnetAssociationOption, SwingUiSettings.HANDLE_MAGNETS)
        } else {
            syncAssociationSettings(torrentAssociationOption, magnetAssociationOption)
        }
    }

    override fun playerVolume(): Float = SwingUiSettings.PLAYER_VOLUME.getValue()

    override fun setPlayerVolume(value: Float) {
        SwingUiSettings.PLAYER_VOLUME.setValue(value.coerceIn(0f, 1f))
    }

    override fun playerEnabled(): Boolean = SwingUiSettings.PLAYER_ENABLED.getValue()

    override fun clearSearchHistory() {
        searchHistory?.clear()
    }

    override fun recordSearchHistoryEntry(query: String) {
        val trimmed = query.trim()
        if (trimmed.isNotEmpty()) {
            searchHistory?.addEntry(trimmed)
        }
    }

    override fun resetSpamFilter() {
        spamManager?.clearFilterData()
        spamManager?.adjustSpamFilters()
    }

    override fun resetWarnings() {
        QuestionsHandler.WARN_TORRENT_SEED_MORE.revertToDefault()
        QuestionsHandler.CONFIRM_BLOCK_HOST.revertToDefault()
        QuestionsHandler.CONFIRM_REMOVE_FILE_INFO_SHARING.revertToDefault()
        SwingUiSettings.WARN_DOWNLOAD_DANGEROUS.revertToDefault()
        SwingUiSettings.WARN_DOWNLOAD_SCAN_FAILED.revertToDefault()
        SwingUiSettings.WARN_DOWNLOAD_THREAT_FOUND.revertToDefault()
        SharingSettings.WARN_SHARING_DOCUMENTS_WITH_WORLD.revertToDefault()
    }

    override fun confirmBlockUsersEnabled(): Boolean = QuestionsHandler.CONFIRM_BLOCK_HOST.getValue()

    override fun setConfirmBlockUsersEnabled(enabled: Boolean) {
        QuestionsHandler.CONFIRM_BLOCK_HOST.setValue(enabled)
    }

    override fun confirmRemoveFileInfoSharingEnabled(): Boolean {
        return QuestionsHandler.CONFIRM_REMOVE_FILE_INFO_SHARING.getValue()
    }

    override fun setConfirmRemoveFileInfoSharingEnabled(enabled: Boolean) {
        QuestionsHandler.CONFIRM_REMOVE_FILE_INFO_SHARING.setValue(enabled)
    }

    override fun defaultBlockedExtensions(): List<String> {
        return DEFAULT_BANNED_EXTENSIONS.toList()
    }

    override fun loadWindowPlacementPreferences(): WindowPlacementPreferences {
        return WindowPlacementPreferences(
            restoreWindowPlacement = SwingUiSettings.COMPOSE_RESTORE_WINDOW_PLACEMENT.getValue(),
            positionsSet = SwingUiSettings.POSITIONS_SET.getValue(),
            width = SwingUiSettings.APP_WIDTH.getValue(),
            height = SwingUiSettings.APP_HEIGHT.getValue(),
            x = SwingUiSettings.WINDOW_X.getValue(),
            y = SwingUiSettings.WINDOW_Y.getValue(),
            maximized = SwingUiSettings.MAXIMIZE_WINDOW.getValue()
        )
    }

    override fun saveWindowPlacementPreferences(preferences: WindowPlacementPreferences) {
        SwingUiSettings.COMPOSE_RESTORE_WINDOW_PLACEMENT.setValue(preferences.restoreWindowPlacement)
        SwingUiSettings.POSITIONS_SET.setValue(preferences.positionsSet)
        SwingUiSettings.APP_WIDTH.setValue(preferences.width.coerceAtLeast(640))
        SwingUiSettings.APP_HEIGHT.setValue(preferences.height.coerceAtLeast(480))
        SwingUiSettings.WINDOW_X.setValue(preferences.x)
        SwingUiSettings.WINDOW_Y.setValue(preferences.y)
        SwingUiSettings.MAXIMIZE_WINDOW.setValue(preferences.maximized)
    }

    override fun loadTrayBehaviorPreferences(): TrayBehaviorPreferences {
        return TrayBehaviorPreferences(
            minimizeToTray = SwingUiSettings.MINIMIZE_TO_TRAY.getValue(),
            showTransfersTray = SwingUiSettings.SHOW_TRANSFERS_TRAY.getValue()
        )
    }

    override fun saveTrayBehaviorPreferences(preferences: TrayBehaviorPreferences) {
        SwingUiSettings.MINIMIZE_TO_TRAY.setValue(preferences.minimizeToTray)
        SwingUiSettings.SHOW_TRANSFERS_TRAY.setValue(preferences.showTransfersTray)
    }

    override fun loadSearchLayoutPreferences(): SearchLayoutPreferences {
        return SearchLayoutPreferences(
            visibleColumns = parseEnumSet(
                SwingUiSettings.COMPOSE_SEARCH_VISIBLE_COLUMNS.get(),
                SearchColumn.entries.toList()
            ),
            sortMode = parseEnum(
                SwingUiSettings.COMPOSE_SEARCH_SORT_KEY.get(),
                SearchSortMode.RELEVANCE
            ),
            sortDescending = SwingUiSettings.COMPOSE_SEARCH_SORT_DESCENDING.getValue()
        )
    }

    override fun saveSearchLayoutPreferences(preferences: SearchLayoutPreferences) {
        SwingUiSettings.COMPOSE_SEARCH_VISIBLE_COLUMNS.set(joinEnumNames(SearchColumn.entries.toList(), preferences.visibleColumns))
        SwingUiSettings.COMPOSE_SEARCH_SORT_KEY.set(preferences.sortMode.name)
        SwingUiSettings.COMPOSE_SEARCH_SORT_DESCENDING.setValue(preferences.sortDescending)
    }

    override fun loadLibraryLayoutPreferences(): LibraryLayoutPreferences {
        return LibraryLayoutPreferences(
            visibleColumns = parseEnumSet(
                SwingUiSettings.COMPOSE_LIBRARY_VISIBLE_COLUMNS.get(),
                LibraryColumn.entries.toList()
            ),
            sortMode = parseEnum(
                SwingUiSettings.COMPOSE_LIBRARY_SORT_KEY.get(),
                LibrarySortMode.NAME
            ),
            sortDescending = SwingUiSettings.COMPOSE_LIBRARY_SORT_DESCENDING.getValue()
        )
    }

    override fun saveLibraryLayoutPreferences(preferences: LibraryLayoutPreferences) {
        SwingUiSettings.COMPOSE_LIBRARY_VISIBLE_COLUMNS.set(joinEnumNames(LibraryColumn.entries.toList(), preferences.visibleColumns))
        SwingUiSettings.COMPOSE_LIBRARY_SORT_KEY.set(preferences.sortMode.name)
        SwingUiSettings.COMPOSE_LIBRARY_SORT_DESCENDING.setValue(preferences.sortDescending)
    }

    override fun loadDownloadLayoutPreferences(): DownloadLayoutPreferences {
        return DownloadLayoutPreferences(
            visibleColumns = parseEnumSet(
                SwingUiSettings.COMPOSE_DOWNLOAD_VISIBLE_COLUMNS.get(),
                DownloadColumn.entries.toList()
            ),
            sortMode = parseEnum(
                SwingUiSettings.COMPOSE_DOWNLOAD_SORT_KEY.get(),
                DownloadSortMode.STATUS
            ),
            sortDescending = SwingUiSettings.COMPOSE_DOWNLOAD_SORT_DESCENDING.getValue(),
            filterMode = parseEnum(
                SwingUiSettings.COMPOSE_DOWNLOAD_FILTER_MODE.get(),
                TransferFilterMode.ALL
            )
        )
    }

    override fun saveDownloadLayoutPreferences(preferences: DownloadLayoutPreferences) {
        SwingUiSettings.COMPOSE_DOWNLOAD_VISIBLE_COLUMNS.set(joinEnumNames(DownloadColumn.entries.toList(), preferences.visibleColumns))
        SwingUiSettings.COMPOSE_DOWNLOAD_SORT_KEY.set(preferences.sortMode.name)
        SwingUiSettings.COMPOSE_DOWNLOAD_SORT_DESCENDING.setValue(preferences.sortDescending)
        SwingUiSettings.COMPOSE_DOWNLOAD_FILTER_MODE.set(preferences.filterMode.name)
    }

    override fun loadUploadLayoutPreferences(): UploadLayoutPreferences {
        return UploadLayoutPreferences(
            visibleColumns = parseEnumSet(
                SwingUiSettings.COMPOSE_UPLOAD_VISIBLE_COLUMNS.get(),
                UploadColumn.entries.toList()
            ),
            sortMode = parseEnum(
                SwingUiSettings.COMPOSE_UPLOAD_SORT_KEY.get(),
                UploadSortMode.STATUS
            ),
            sortDescending = SwingUiSettings.COMPOSE_UPLOAD_SORT_DESCENDING.getValue(),
            filterMode = parseEnum(
                SwingUiSettings.COMPOSE_UPLOAD_FILTER_MODE.get(),
                TransferFilterMode.ALL
            )
        )
    }

    override fun saveUploadLayoutPreferences(preferences: UploadLayoutPreferences) {
        SwingUiSettings.COMPOSE_UPLOAD_VISIBLE_COLUMNS.set(joinEnumNames(UploadColumn.entries.toList(), preferences.visibleColumns))
        SwingUiSettings.COMPOSE_UPLOAD_SORT_KEY.set(preferences.sortMode.name)
        SwingUiSettings.COMPOSE_UPLOAD_SORT_DESCENDING.setValue(preferences.sortDescending)
        SwingUiSettings.COMPOSE_UPLOAD_FILTER_MODE.set(preferences.filterMode.name)
    }

    override fun loadSearchPaneLayoutPreferences(): SearchPaneLayoutPreferences {
        return SearchPaneLayoutPreferences(
            refinementRailVisible = SwingUiSettings.COMPOSE_SEARCH_REFINEMENT_VISIBLE.getValue(),
            refinementRailFraction = clampSplitFraction(
                SwingUiSettings.COMPOSE_SEARCH_REFINEMENT_SPLIT.getValue(),
                0.26f
            ),
            resultsFraction = clampSplitFraction(
                SwingUiSettings.COMPOSE_SEARCH_RESULTS_SPLIT.getValue(),
                0.62f
            )
        )
    }

    override fun saveSearchPaneLayoutPreferences(preferences: SearchPaneLayoutPreferences) {
        SwingUiSettings.COMPOSE_SEARCH_REFINEMENT_VISIBLE.setValue(preferences.refinementRailVisible)
        SwingUiSettings.COMPOSE_SEARCH_REFINEMENT_SPLIT.setValue(
            clampSplitFraction(preferences.refinementRailFraction, 0.26f)
        )
        SwingUiSettings.COMPOSE_SEARCH_RESULTS_SPLIT.setValue(
            clampSplitFraction(preferences.resultsFraction, 0.62f)
        )
    }

    override fun loadLibraryPaneLayoutPreferences(): LibraryPaneLayoutPreferences {
        return LibraryPaneLayoutPreferences(
            navigatorFraction = clampSplitFraction(
                SwingUiSettings.COMPOSE_LIBRARY_NAVIGATOR_SPLIT.getValue(),
                0.24f
            ),
            filtersVisible = SwingUiSettings.SHOW_LIBRARY_FILTERS.getValue()
        )
    }

    override fun saveLibraryPaneLayoutPreferences(preferences: LibraryPaneLayoutPreferences) {
        SwingUiSettings.COMPOSE_LIBRARY_NAVIGATOR_SPLIT.setValue(
            clampSplitFraction(preferences.navigatorFraction, 0.24f)
        )
        SwingUiSettings.SHOW_LIBRARY_FILTERS.setValue(preferences.filtersVisible)
    }

    override fun showLibraryOverlayMessageEnabled(): Boolean =
        SwingUiSettings.SHOW_LIBRARY_OVERLAY_MESSAGE.getValue()

    override fun setShowLibraryOverlayMessageEnabled(enabled: Boolean) {
        SwingUiSettings.SHOW_LIBRARY_OVERLAY_MESSAGE.setValue(enabled)
    }

    override fun showSharingOverlayMessageEnabled(): Boolean =
        SwingUiSettings.SHOW_SHARING_OVERLAY_MESSAGE.getValue()

    override fun setShowSharingOverlayMessageEnabled(enabled: Boolean) {
        SwingUiSettings.SHOW_SHARING_OVERLAY_MESSAGE.setValue(enabled)
    }

    override fun warnSharingDocumentsWithWorldEnabled(): Boolean =
        SharingSettings.WARN_SHARING_DOCUMENTS_WITH_WORLD.getValue()

    override fun setWarnSharingDocumentsWithWorldEnabled(enabled: Boolean) {
        SharingSettings.WARN_SHARING_DOCUMENTS_WITH_WORLD.setValue(enabled)
    }

    override fun removeDocumentsFromPublicLists() {
        sharedFileListManager?.removeDocumentsFromPublicLists()
    }

    override fun loadFriendsPaneLayoutPreferences(): FriendsPaneLayoutPreferences {
        return FriendsPaneLayoutPreferences(
            rosterFraction = clampSplitFraction(
                SwingUiSettings.COMPOSE_FRIENDS_ROSTER_SPLIT.getValue(),
                0.28f
            )
        )
    }

    override fun saveFriendsPaneLayoutPreferences(preferences: FriendsPaneLayoutPreferences) {
        SwingUiSettings.COMPOSE_FRIENDS_ROSTER_SPLIT.setValue(
            clampSplitFraction(preferences.rosterFraction, 0.28f)
        )
    }

    override fun loadTransferPaneLayoutPreferences(): TransferPaneLayoutPreferences {
        return TransferPaneLayoutPreferences(
            mainAreaFraction = clampSplitFraction(
                SwingUiSettings.COMPOSE_TRANSFER_TRAY_SPLIT.getValue(),
                0.72f
            )
        )
    }

    override fun saveTransferPaneLayoutPreferences(preferences: TransferPaneLayoutPreferences) {
        SwingUiSettings.COMPOSE_TRANSFER_TRAY_SPLIT.setValue(
            clampSplitFraction(preferences.mainAreaFraction, 0.72f)
        )
    }

    override fun loadConnectionLayoutPreferences(): ConnectionLayoutPreferences {
        return ConnectionLayoutPreferences(
            visibleColumns = parseEnumSet(
                SwingUiSettings.COMPOSE_CONNECTION_VISIBLE_COLUMNS.get(),
                ConnectionColumn.entries.toList()
            ),
            sortColumn = parseEnum(
                SwingUiSettings.COMPOSE_CONNECTION_SORT_KEY.get(),
                ConnectionColumn.HOST
            ),
            sortDescending = SwingUiSettings.COMPOSE_CONNECTION_SORT_DESCENDING.getValue()
        )
    }

    override fun saveConnectionLayoutPreferences(preferences: ConnectionLayoutPreferences) {
        SwingUiSettings.COMPOSE_CONNECTION_VISIBLE_COLUMNS.set(
            joinEnumNames(ConnectionColumn.entries.toList(), preferences.visibleColumns)
        )
        SwingUiSettings.COMPOSE_CONNECTION_SORT_KEY.set(preferences.sortColumn.name)
        SwingUiSettings.COMPOSE_CONNECTION_SORT_DESCENDING.setValue(preferences.sortDescending)
    }

    private fun defaultSearchCategory(): SearchCategory {
        val configuredId = SwingUiSettings.DEFAULT_SEARCH_CATEGORY_ID.getValue()
        return SearchCategory.values().firstOrNull { it.id == configuredId && it != SearchCategory.OTHER }
            ?: SearchCategory.ALL
    }

    private fun sanitizeDefaultSearchCategory(category: SearchCategory, programsAllowed: Boolean): SearchCategory {
        return when {
            category == SearchCategory.OTHER -> SearchCategory.ALL
            category == SearchCategory.PROGRAM && !programsAllowed -> SearchCategory.ALL
            else -> category
        }
    }

    private fun loadCategorySaveDirectories(): CategorySaveDirectoriesDraft {
        fun categoryDir(category: Category): String =
            SharingSettings.getFileSettingForCategory(category).get().absolutePath

        return CategorySaveDirectoriesDraft(
            audio = categoryDir(Category.AUDIO),
            video = categoryDir(Category.VIDEO),
            images = categoryDir(Category.IMAGE),
            documents = categoryDir(Category.DOCUMENT),
            programs = categoryDir(Category.PROGRAM),
            other = categoryDir(Category.OTHER)
        )
    }

    private fun isCategorySaveDirectoriesCustom(): Boolean {
        val defaultLocation = SharingSettings.getSaveDirectory()
        return listOf(
            Category.AUDIO,
            Category.VIDEO,
            Category.IMAGE,
            Category.DOCUMENT,
            Category.PROGRAM,
            Category.OTHER
        ).any { SharingSettings.getFileSettingForCategory(it).get() != defaultLocation }
    }

    private fun applyCategorySaveDirectories(
        draft: CategorySaveDirectoriesDraft,
        defaultDirectory: File,
        previousDefaultDirectory: File
    ) {
        applyCategorySaveDirectory(Category.AUDIO, draft.audio, defaultDirectory, previousDefaultDirectory)
        applyCategorySaveDirectory(Category.VIDEO, draft.video, defaultDirectory, previousDefaultDirectory)
        applyCategorySaveDirectory(Category.IMAGE, draft.images, defaultDirectory, previousDefaultDirectory)
        applyCategorySaveDirectory(Category.DOCUMENT, draft.documents, defaultDirectory, previousDefaultDirectory)
        applyCategorySaveDirectory(Category.PROGRAM, draft.programs, defaultDirectory, previousDefaultDirectory)
        applyCategorySaveDirectory(Category.OTHER, draft.other, defaultDirectory, previousDefaultDirectory)
    }

    private fun applyCategorySaveDirectory(
        category: Category,
        path: String,
        defaultDirectory: File,
        previousDefaultDirectory: File
    ) {
        val trimmed = path.trim().ifEmpty { defaultDirectory.absolutePath }
        val directory = requireValidatedSaveDirectory(trimmed)
        val setting = SharingSettings.getFileSettingForCategory(category)
        if (directory == defaultDirectory || directory == previousDefaultDirectory) {
            setting.revertToDefault()
        } else {
            setting.set(directory)
        }
    }

    private fun revertCategorySaveDirectoriesToDefault() {
        listOf(
            Category.AUDIO,
            Category.VIDEO,
            Category.IMAGE,
            Category.DOCUMENT,
            Category.PROGRAM,
            Category.OTHER
        ).forEach { SharingSettings.getFileSettingForCategory(it).revertToDefault() }
    }

    private fun supportsRunOnStartup(): Boolean {
        return OSUtils.isMacOSX() || WindowsUtils.isLoginStatusAvailable()
    }

    private fun applyRunOnStartup(enabled: Boolean) {
        when {
            OSUtils.isMacOSX() -> MacOSXUtils.setLoginStatus(enabled)
            WindowsUtils.isLoginStatusAvailable() -> WindowsUtils.setLoginStatus(enabled)
        }
    }

    private fun applyAssociationSettings(handleMagnets: Boolean, handleTorrents: Boolean) {
        LimeAssociations.getMagnetAssociation()?.setEnabled(handleMagnets)
        LimeAssociations.getTorrentAssociation()?.setEnabled(handleTorrents)
    }

    private fun applyITunesPreferences(draft: ITunesPreferencesDraft) {
        iTunesSettings.ITUNES_SUPPORT_ENABLED.setValue(draft.addDownloadedAudioToLibrary)
        val manager = daapManager ?: return

        val previousEnabled = DaapSettings.DAAP_ENABLED.getValue()
        val previousRequiresPassword = DaapSettings.DAAP_REQUIRES_PASSWORD.getValue()
        val previousPassword = DaapSettings.DAAP_PASSWORD.get()
        val requiresPassword = draft.requirePassword
        val password = draft.password

        if (requiresPassword && password.isBlank()) {
            throw IllegalArgumentException(tr("Enter a DAAP password before enabling protected iTunes sharing."))
        }

        DaapSettings.DAAP_ENABLED.setValue(draft.shareAudioAcrossLan)
        if (DaapSettings.DAAP_PASSWORD.get() != password) {
            DaapSettings.DAAP_PASSWORD.set(password)
        }

        try {
            if (requiresPassword != previousRequiresPassword || (requiresPassword && password != previousPassword)) {
                DaapSettings.DAAP_REQUIRES_PASSWORD.setValue(requiresPassword)
                if (requiresPassword) {
                    manager.disconnectAll()
                }
                manager.updateService()
            }

            if (draft.shareAudioAcrossLan) {
                if (previousEnabled) {
                    manager.restart()
                } else {
                    manager.start()
                }
            } else if (previousEnabled) {
                manager.stop()
            }
        } catch (failure: IOException) {
            DaapSettings.DAAP_ENABLED.setValue(previousEnabled)
            DaapSettings.DAAP_REQUIRES_PASSWORD.setValue(previousRequiresPassword)
            DaapSettings.DAAP_PASSWORD.set(previousPassword)
            manager.stop()
            throw IllegalArgumentException(tr("WireShare could not restart the iTunes sharing service."))
        }
    }

    private fun currentProxyMode(): ProxyMode {
        return when (ConnectionSettings.CONNECTION_METHOD.getValue()) {
            ConnectionSettings.C_SOCKS4_PROXY -> ProxyMode.SOCKS4
            ConnectionSettings.C_SOCKS5_PROXY -> ProxyMode.SOCKS5
            ConnectionSettings.C_HTTP_PROXY -> ProxyMode.HTTP
            else -> ProxyMode.NONE
        }
    }

    private fun currentTlsDisabled(): Boolean {
        val manager = networkManager ?: return false
        return !manager.isIncomingTLSEnabled || !manager.isOutgoingTLSEnabled
    }

    private fun currentPortForwardMode(): PortForwardMode {
        return when {
            ConnectionSettings.FORCE_IP_ADDRESS.getValue() && !ConnectionSettings.UPNP_IN_USE.getValue() ->
                PortForwardMode.MANUAL
            ConnectionSettings.DISABLE_UPNP.getValue() -> PortForwardMode.NONE
            else -> PortForwardMode.UPNP
        }
    }

    private fun availableNetworkInterfaces(): List<NetworkInterfaceOption> {
        return try {
            val entries = mutableListOf<NetworkInterfaceOption>()
            val interfaces = NetworkInterface.getNetworkInterfaces() ?: return emptyList()
            while (interfaces.hasMoreElements()) {
                val networkInterface = interfaces.nextElement()
                val addresses = networkInterface.inetAddresses
                while (addresses.hasMoreElements()) {
                    val address = addresses.nextElement()
                    if (address.isAnyLocalAddress || address.isLinkLocalAddress || address.isLoopbackAddress) {
                        continue
                    }
                    entries += NetworkInterfaceOption(
                        address = address.hostAddress,
                        displayName = networkInterface.displayName ?: address.hostAddress
                    )
                }
            }
            entries.sortedWith(compareBy({ it.displayName.lowercase(Locale.US) }, { it.address }))
        } catch (_: SocketException) {
            emptyList()
        }
    }

    private fun applyNetworkPreferences(draft: NetworkAdvancedPreferencesDraft): Boolean {
        var restartRequired = false
        val torrentStartPort = parseBoundedInt(
            draft.torrentListenStartPort,
            1,
            0xFFFF,
            BittorrentSettings.LIBTORRENT_LISTEN_START_PORT.getValue()
        )
        val torrentEndPort = parseBoundedInt(
            draft.torrentListenEndPort,
            1,
            0xFFFF,
            BittorrentSettings.LIBTORRENT_LISTEN_END_PORT.getValue()
        )
        if (torrentStartPort > torrentEndPort) {
            throw IllegalArgumentException(tr("BitTorrent start port must be less than or equal to the end port."))
        }
        BittorrentSettings.LIBTORRENT_LISTEN_START_PORT.setValue(torrentStartPort)
        BittorrentSettings.LIBTORRENT_LISTEN_END_PORT.setValue(torrentEndPort)
        BittorrentSettings.TORRENT_USE_UPNP.setValue(draft.torrentUseUpnp)

        val desiredPort = parseBoundedInt(draft.gnutellaPort, 1, 0xFFFF, NetworkSettings.PORT.getValue())
        if (desiredPort != NetworkSettings.PORT.getValue()) {
            try {
                NetworkSettings.PORT.setValue(desiredPort)
                networkManager?.setListeningPort(desiredPort)
                networkManager?.portChanged()
            } catch (failure: IOException) {
                throw IllegalArgumentException(tr("Port {0} is in use.", desiredPort))
            }
        }

        val oldDisableUpnp = ConnectionSettings.DISABLE_UPNP.getValue()
        val oldForcedPort = ConnectionSettings.FORCED_PORT.getValue()
        val oldForce = ConnectionSettings.FORCE_IP_ADDRESS.getValue()

        when (draft.portForwardMode) {
            PortForwardMode.UPNP -> {
                if (!ConnectionSettings.UPNP_IN_USE.getValue()) {
                    ConnectionSettings.FORCE_IP_ADDRESS.setValue(false)
                }
                ConnectionSettings.DISABLE_UPNP.setValue(false)
                if (oldDisableUpnp || oldForce) {
                    restartRequired = true
                }
            }

            PortForwardMode.NONE -> {
                ConnectionSettings.FORCE_IP_ADDRESS.setValue(false)
                ConnectionSettings.DISABLE_UPNP.setValue(true)
            }

            PortForwardMode.MANUAL -> {
                ConnectionSettings.DISABLE_UPNP.setValue(false)
                ConnectionSettings.FORCE_IP_ADDRESS.setValue(true)
                ConnectionSettings.UPNP_IN_USE.setValue(false)
                ConnectionSettings.FORCED_PORT.setValue(
                    parseBoundedInt(draft.manualPort, 1, 0xFFFF, oldForcedPort)
                )
            }
        }

        val newForce = ConnectionSettings.FORCE_IP_ADDRESS.getValue()
        val newForcedPort = ConnectionSettings.FORCED_PORT.getValue()
        if (oldForce != newForce) {
            networkManager?.addressChanged()
        }
        if (newForce && oldForcedPort != newForcedPort) {
            networkManager?.portChanged()
        }

        val oldCustomInterface = ConnectionSettings.CUSTOM_NETWORK_INTERFACE.getValue()
        val oldAddress = ConnectionSettings.CUSTOM_INETADRESS.get()
        ConnectionSettings.CUSTOM_NETWORK_INTERFACE.setValue(draft.useCustomNetworkInterface)
        ConnectionSettings.CUSTOM_INETADRESS.set(draft.selectedNetworkInterfaceAddress ?: "0.0.0.0")
        if (oldCustomInterface != draft.useCustomNetworkInterface ||
            oldAddress != (draft.selectedNetworkInterfaceAddress ?: "0.0.0.0")
        ) {
            restartRequired = true
        }

        val disableTls = draft.disableTls
        networkManager?.setIncomingTLSEnabled(!disableTls)
        networkManager?.setOutgoingTLSEnabled(!disableTls)
        if (!disableTls) {
            networkManager?.validateTLS()
        }
        UltrapeerSettings.DISABLE_ULTRAPEER_MODE.setValue(draft.disableUltrapeer)
        DHTSettings.DISABLE_DHT_USER.setValue(draft.disableMojito)
        SearchSettings.OOB_ENABLED.setValue(!draft.disableOutOfBandSearch)

        ConnectionSettings.CONNECTION_METHOD.setValue(
            when (draft.proxyMode) {
                ProxyMode.NONE -> ConnectionSettings.C_NO_PROXY
                ProxyMode.SOCKS4 -> ConnectionSettings.C_SOCKS4_PROXY
                ProxyMode.SOCKS5 -> ConnectionSettings.C_SOCKS5_PROXY
                ProxyMode.HTTP -> ConnectionSettings.C_HTTP_PROXY
            }
        )
        when (draft.proxyMode) {
            ProxyMode.NONE -> {
                ConnectionSettings.PROXY_HOST.set("")
                ConnectionSettings.PROXY_PORT.setValue(0)
                ConnectionSettings.PROXY_AUTHENTICATE.setValue(false)
                ConnectionSettings.PROXY_USERNAME.set("")
                ConnectionSettings.PROXY_PASS.set("")
            }

            ProxyMode.HTTP -> {
                ConnectionSettings.PROXY_HOST.set(draft.proxyHost.trim())
                ConnectionSettings.PROXY_PORT.setValue(parseBoundedInt(draft.proxyPort, 0, 0xFFFF, ConnectionSettings.PROXY_PORT.getValue()))
                ConnectionSettings.PROXY_AUTHENTICATE.setValue(false)
                ConnectionSettings.PROXY_USERNAME.set("")
                ConnectionSettings.PROXY_PASS.set("")
            }

            ProxyMode.SOCKS4 -> {
                ConnectionSettings.PROXY_HOST.set(draft.proxyHost.trim())
                ConnectionSettings.PROXY_PORT.setValue(parseBoundedInt(draft.proxyPort, 0, 0xFFFF, ConnectionSettings.PROXY_PORT.getValue()))
                ConnectionSettings.PROXY_AUTHENTICATE.setValue(draft.proxyAuthenticate)
                ConnectionSettings.PROXY_USERNAME.set(if (draft.proxyAuthenticate) draft.proxyUsername else "")
                ConnectionSettings.PROXY_PASS.set("")
            }

            ProxyMode.SOCKS5 -> {
                ConnectionSettings.PROXY_HOST.set(draft.proxyHost.trim())
                ConnectionSettings.PROXY_PORT.setValue(parseBoundedInt(draft.proxyPort, 0, 0xFFFF, ConnectionSettings.PROXY_PORT.getValue()))
                ConnectionSettings.PROXY_AUTHENTICATE.setValue(draft.proxyAuthenticate)
                ConnectionSettings.PROXY_USERNAME.set(if (draft.proxyAuthenticate) draft.proxyUsername else "")
                ConnectionSettings.PROXY_PASS.set(if (draft.proxyAuthenticate) draft.proxyPassword else "")
            }
        }

        return restartRequired
    }

    private fun syncTorrentManagerSettings() {
        val provider = torrentManager ?: return
        val settings = torrentManagerSettings ?: return
        Thread {
            runCatching {
                val manager = provider.get()
                if (manager.isInitialized && manager.isValid) {
                    manager.setTorrentManagerSettings(settings)
                    if (BittorrentSettings.TORRENT_USE_UPNP.getValue()) {
                        manager.startUPnP()
                    } else {
                        manager.stopUPnP()
                    }
                }
            }
        }.start()
    }

    private fun bytesToKiBString(value: Int): String {
        return if (value <= 0) "0" else (value / 1024).toString()
    }

    private fun formatSeedRatio(value: Float): String {
        val integerValue = value.toInt().toFloat()
        return if (value == integerValue) integerValue.toInt().toString() else value.toString()
    }

    private fun wholeDays(totalSeconds: Int): Int {
        return (totalSeconds / (60 * 60 * 24)).coerceAtLeast(0)
    }

    private fun remainingHours(totalSeconds: Int): Int {
        val totalHours = (totalSeconds / (60 * 60)).coerceAtLeast(0)
        return totalHours % 24
    }

    private fun parseKiBSetting(value: String): Int {
        val trimmed = value.trim()
        if (trimmed.isEmpty()) {
            return 0
        }
        return trimmed.toIntOrNull()?.coerceAtLeast(0)?.times(1024) ?: 0
    }

    private fun parseBoundedFloat(value: String, min: Float, max: Float, fallback: Float): Float {
        return value.trim().toFloatOrNull()?.coerceIn(min, max) ?: fallback
    }

    private fun parseSeedTimeLimit(daysValue: String, hoursValue: String, fallback: Int): Int {
        val days = daysValue.trim().toIntOrNull()
        val hours = hoursValue.trim().toIntOrNull()
        if (days == null || hours == null) {
            return fallback
        }
        val totalSeconds = days.coerceAtLeast(0).toLong() * 24L * 60L * 60L +
            hours.coerceIn(0, 24).toLong() * 60L * 60L
        return totalSeconds.coerceIn(0L, Int.MAX_VALUE.toLong()).toInt()
    }

    private fun parseBoundedInt(value: String, min: Int, max: Int, fallback: Int): Int {
        return value.trim().toIntOrNull()?.coerceIn(min, max) ?: fallback
    }

    private fun normalizeExtensions(values: List<String>): List<String> {
        return values
            .map { entry -> entry.trim().ifEmpty { "" } }
            .filter { it.isNotEmpty() }
            .map { if (it.startsWith('.')) it.lowercase(Locale.US) else ".${it.lowercase(Locale.US)}" }
            .distinct()
            .sorted()
    }

    private fun normalizeHostEntries(values: List<String>): List<String> {
        return values.mapNotNull { entry ->
            val trimmed = entry.trim()
            if (trimmed.isEmpty()) {
                null
            } else {
                runCatching { IP(trimmed) }.getOrNull()?.toString() ?: trimmed
            }
        }.distinct().sorted()
    }

    private fun startupAssociationMessage(torrentsStolen: Boolean, magnetsStolen: Boolean): String {
        return when {
            torrentsStolen && magnetsStolen ->
                tr("Torrent files and magnet links are no longer associated with WireShare. Re-associate them?")
            torrentsStolen ->
                tr("Torrent files are no longer associated with WireShare. Re-associate them?")
            else ->
                tr("Magnet links are no longer associated with WireShare. Re-associate them?")
        }
    }

    private fun syncAssociationSettings(
        torrentAssociationOption: LimeAssociationOption?,
        magnetAssociationOption: LimeAssociationOption?
    ) {
        updateAssociationSetting(torrentAssociationOption, SwingUiSettings.HANDLE_TORRENTS)
        updateAssociationSetting(magnetAssociationOption, SwingUiSettings.HANDLE_MAGNETS)
    }

    private fun updateAssociationSetting(
        associationOption: LimeAssociationOption?,
        handleType: org.limewire.setting.BooleanSetting
    ) {
        handleType.setValue(associationOption != null && associationOption.isEnabled())
    }

    private fun isSettingStolen(
        associationOption: LimeAssociationOption?,
        handleType: org.limewire.setting.BooleanSetting
    ): Boolean {
        return associationOption != null && handleType.getValue() && !associationOption.isEnabled()
    }

    private fun fixAssociation(
        associationOption: LimeAssociationOption?,
        handleType: org.limewire.setting.BooleanSetting
    ) {
        if (associationOption != null) {
            associationOption.setEnabled(handleType.getValue())
        }
    }

    private fun applyAvailableAssociation(
        associationOption: LimeAssociationOption?,
        handleType: org.limewire.setting.BooleanSetting
    ) {
        if (associationOption != null && !associationOption.isEnabled() && handleType.getValue() && associationOption.isAvailable()) {
            associationOption.setEnabled(true)
        }
    }

    private fun requireValidatedSaveDirectory(path: String): File {
        val validation = validateSaveDirectory(path)
        if (!validation.accepted) {
            throw IllegalArgumentException(validation.errorMessage ?: tr("Choose a different download folder to continue."))
        }
        return File(validation.normalizedPath ?: path)
    }

    private fun validateSaveDirectory(saveDir: File?): SaveDirectoryValidationResult {
        if (saveDir == null) {
            return SaveDirectoryValidationResult(
                accepted = false,
                errorMessage = tr("Choose a download folder to continue.")
            )
        }
        if (saveDir.isFile) {
            return SaveDirectoryValidationResult(
                accepted = false,
                errorMessage = tr("{0} is not a folder.", saveDir.absolutePath)
            )
        }
        if (!saveDir.exists() && !saveDir.mkdirs()) {
            return SaveDirectoryValidationResult(
                accepted = false,
                errorMessage = tr("Cannot find {0}.", saveDir.absolutePath)
            )
        }
        if (!saveDir.isDirectory) {
            return SaveDirectoryValidationResult(
                accepted = false,
                errorMessage = tr("{0} is not a folder.", saveDir.absolutePath)
            )
        }

        FileUtils.setWriteable(saveDir)

        val generator = Random()
        var testFile: File? = null
        repeat(10) {
            if (testFile != null) {
                return@repeat
            }
            val name = buildString {
                repeat(8) {
                    append(('a'.code + generator.nextInt('z'.code - 'a'.code)).toChar())
                }
                append(".tmp")
            }
            val candidate = File(saveDir, name)
            if (!candidate.exists()) {
                testFile = candidate
            }
        }

        val writableTestFile = testFile ?: return SaveDirectoryValidationResult(
            accepted = false,
            errorMessage = tr("Cannot write to {0}.", saveDir.absolutePath)
        )
        var randomAccessFile: RandomAccessFile? = null
        val writable = try {
            randomAccessFile = RandomAccessFile(writableTestFile, "rw")
            randomAccessFile.write(7)
            FileUtils.canWrite(saveDir)
        } catch (_: IOException) {
            false
        } finally {
            writableTestFile.delete()
            IOUtils.close(randomAccessFile)
        }
        if (!writable) {
            return SaveDirectoryValidationResult(
                accepted = false,
                errorMessage = tr("Cannot write to {0}.", saveDir.absolutePath)
            )
        }
        val canonical = canonicalizeFile(saveDir)
        return SaveDirectoryValidationResult(
            accepted = true,
            normalizedPath = canonical.absolutePath
        )
    }

    private fun canonicalizeFile(file: File): File {
        return try {
            file.canonicalFile
        } catch (_: IOException) {
            file.absoluteFile
        }
    }

    private fun <T : Enum<T>> parseEnum(value: String, defaultValue: T): T {
        return defaultValue.declaringJavaClass.enumConstants.firstOrNull { it.name == value } ?: defaultValue
    }

    private inline fun <reified T : Enum<T>> parseEnumSet(value: String, allValues: List<T>): Set<T> {
        val parsed = value
            .split(',')
            .mapNotNull { token ->
                val trimmed = token.trim()
                enumValues<T>().firstOrNull { it.name == trimmed }
            }
            .toSet()
        return if (parsed.isEmpty()) allValues.toSet() else parsed
    }

    private fun <T : Enum<T>> joinEnumNames(allValues: List<T>, selected: Set<T>): String {
        return allValues.filter { it in selected }.joinToString(",") { it.name }
    }

    private fun clampSplitFraction(value: Float, fallback: Float): Float {
        return value.takeIf { it.isFinite() }?.coerceIn(0.1f, 0.9f) ?: fallback
    }

    private companion object {
        private val DEFAULT_BANNED_EXTENSIONS = listOf(
            ".and", ".asf", ".asx", ".au", ".htm", ".html", ".mht", ".vbs",
            ".use", ".wav", ".wax", ".wm", ".wma", ".wmd", ".wmv", ".wmx", ".wmz", ".wvx"
        )
    }
}

class CoreComposeRecentDownloadsService : ComposeRecentDownloadsService {
    override fun recentDownloads(): List<File> {
        val files = synchronized(DownloadSettings.RECENT_DOWNLOADS) {
            DownloadSettings.RECENT_DOWNLOADS.get().toList()
        }
        return files.sortedByDescending(File::lastModified)
    }

    override fun clearRecentDownloads() {
        synchronized(DownloadSettings.RECENT_DOWNLOADS) {
            DownloadSettings.RECENT_DOWNLOADS.clear()
        }
    }
}

class CoreComposeTransferRepairService(
    private val downloadListManager: org.limewire.core.api.download.DownloadListManager
) : ComposeTransferRepairService {
    override fun stalledDownloadCount(): Int {
        return snapshotEventList(downloadListManager.swingThreadSafeDownloads)
            .count { it.state == org.limewire.core.api.download.DownloadState.STALLED }
    }

    override fun fixStalledDownloads(): Int {
        val stalled = snapshotEventList(downloadListManager.swingThreadSafeDownloads)
            .filter { it.state == org.limewire.core.api.download.DownloadState.STALLED }
        stalled.forEach { it.resume() }
        return stalled.size
    }
}

class CoreComposeDelayedExitService(
    private val connectionManager: org.limewire.core.api.connection.GnutellaConnectionManager,
    private val downloadListManager: org.limewire.core.api.download.DownloadListManager,
    private val uploadListManager: org.limewire.core.api.upload.UploadListManager
) : ComposeDelayedExitService {
    private val listeners = CopyOnWriteArrayList<ComposeDelayedExitService.Listener>()

    @Volatile
    private var delayedExitState = DelayedExitState()

    @Volatile
    private var disconnectOnCancel = false

    @Volatile
    private var exitNotified = false

    private var downloadCompletionListener: PropertyChangeListener? = null
    private var uploadCompletionListener: PropertyChangeListener? = null

    override fun activate(listener: ComposeDelayedExitService.Listener) {
        listeners.addIfAbsent(listener)
        runOnUi { listener.stateChanged(delayedExitState) }
    }

    override fun deactivate(listener: ComposeDelayedExitService.Listener) {
        listeners.remove(listener)
    }

    override fun state(): DelayedExitState = delayedExitState

    override fun start() {
        if (delayedExitState.pending) {
            return
        }

        disconnectOnCancel = connectionManager.isConnected
        if (disconnectOnCancel) {
            connectionManager.disconnect()
        }

        installListeners()
        exitNotified = false
        updateState(
            DelayedExitState(
                pending = true,
                downloadsCompleted = false,
                uploadsCompleted = false,
                disconnected = disconnectOnCancel
            )
        )
        downloadListManager.updateDownloadsCompleted()
        uploadListManager.updateUploadsCompleted()
    }

    override fun cancel() {
        if (!delayedExitState.pending) {
            return
        }

        removeListeners()
        val shouldReconnect = disconnectOnCancel
        disconnectOnCancel = false
        exitNotified = false
        updateState(DelayedExitState())
        if (shouldReconnect) {
            connectionManager.connect()
        }
    }

    private fun installListeners() {
        if (downloadCompletionListener == null) {
            downloadCompletionListener = PropertyChangeListener { event ->
                if (event.propertyName == org.limewire.core.api.download.DownloadListManager.DOWNLOADS_COMPLETED) {
                    markDownloadsCompleted()
                }
            }
            downloadListManager.addPropertyChangeListener(downloadCompletionListener)
        }
        if (uploadCompletionListener == null) {
            uploadCompletionListener = PropertyChangeListener { event ->
                if (event.propertyName == org.limewire.core.api.upload.UploadListManager.UPLOADS_COMPLETED) {
                    markUploadsCompleted()
                }
            }
            uploadListManager.addPropertyChangeListener(uploadCompletionListener)
        }
    }

    private fun removeListeners() {
        downloadCompletionListener?.let(downloadListManager::removePropertyChangeListener)
        uploadCompletionListener?.let(uploadListManager::removePropertyChangeListener)
        downloadCompletionListener = null
        uploadCompletionListener = null
    }

    private fun markDownloadsCompleted() {
        updateState(delayedExitState.copy(downloadsCompleted = true))
        maybeNotifyExitReady()
    }

    private fun markUploadsCompleted() {
        updateState(delayedExitState.copy(uploadsCompleted = true))
        maybeNotifyExitReady()
    }

    private fun maybeNotifyExitReady() {
        val state = delayedExitState
        if (!state.pending || !state.downloadsCompleted || !state.uploadsCompleted || exitNotified) {
            return
        }
        exitNotified = true
        runOnUi {
            listeners.forEach { it.exitReady() }
        }
    }

    private fun updateState(state: DelayedExitState) {
        delayedExitState = state
        runOnUi {
            listeners.forEach { it.stateChanged(state) }
        }
    }
}

class CoreComposeFriendAccountConfigurationManager(
    private val passwordManager: PasswordManager,
    xmppResourceFactory: XMPPResourceFactory
) : FriendAccountConfigurationManager {
    private val resource = xmppResourceFactory.resource
    private val configs = LinkedHashMap<String, FriendAccountConfiguration>()

    @Volatile
    private var loaded = false

    @Volatile
    private var autoLoginConfig: FriendAccountConfiguration? = null

    override fun getConfig(label: String): FriendAccountConfiguration? = rawConfigs()[label]

    override fun getLabels(): List<String> = getConfigurations().map { it.label }

    override fun getConfigurations(): List<FriendAccountConfiguration> {
        return rawConfigs().values.sortedBy { it.label.lowercase(Locale.US) }
    }

    override fun getAutoLoginConfig(): FriendAccountConfiguration? {
        ensureLoaded()
        return autoLoginConfig
    }

    override fun setAutoLoginConfig(config: FriendAccountConfiguration?) {
        autoLoginConfig?.let { existing ->
            passwordManager.removePassword(existing.userInputLocalID)
            SwingUiSettings.XMPP_AUTO_LOGIN.set("")
            SwingUiSettings.USER_DEFINED_JABBER_SERVICENAME.set("")
            autoLoginConfig = null
        }

        if (config == null) {
            return
        }

        runCatching {
            if (config.storePassword()) {
                passwordManager.storePassword(config.userInputLocalID, config.password)
            }
            SwingUiSettings.XMPP_AUTO_LOGIN.set("${config.label},${config.userInputLocalID}")
            if (config.label == JABBER_LABEL) {
                SwingUiSettings.USER_DEFINED_JABBER_SERVICENAME.set(config.serviceName)
            }
            autoLoginConfig = config
        }
    }

    private fun rawConfigs(): Map<String, FriendAccountConfiguration> {
        ensureLoaded()
        return configs
    }

    private fun ensureLoaded() {
        if (loaded) {
            return
        }
        synchronized(this) {
            if (loaded) {
                return
            }
            loadWellKnownServers()
            loadCustomServer()
            loadAutoLoginAccount()
            loaded = true
        }
    }

    private fun loadCustomServer() {
        val custom = SwingUiSettings.USER_DEFINED_JABBER_SERVICENAME.get()
            .ifBlank { DEFAULT_JABBER_SERVICE }
        val config = ComposeFriendAccountConfiguration(
            requireDomain = false,
            serviceName = custom,
            label = JABBER_LABEL,
            resource = resource,
            defaultServers = emptyList(),
            modifyUser = false
        )
        configs[config.label] = config
    }

    private fun loadAutoLoginAccount() {
        val autoLogin = SwingUiSettings.XMPP_AUTO_LOGIN.get()
        if (autoLogin.isBlank()) {
            return
        }

        val comma = autoLogin.indexOf(',')
        if (comma <= 0 || comma >= autoLogin.lastIndex) {
            return
        }

        val label = autoLogin.substring(0, comma)
        val username = autoLogin.substring(comma + 1)
        val config = configs[label] ?: return

        try {
            config.setUsername(username)
            if (config.storePassword()) {
                config.setPassword(passwordManager.loadPassword(username))
            }
            autoLoginConfig = config
        } catch (_: IllegalArgumentException) {
        } catch (_: IOException) {
        }
    }

    private fun loadWellKnownServers() {
        listOf(
            ComposeFriendAccountConfiguration(
                requireDomain = true,
                serviceName = "gmail.com",
                label = "Gmail",
                resource = resource,
                defaultServers = listOf(
                    UnresolvedIpPortImpl("talk.1.google.com", 5222),
                    UnresolvedIpPortImpl("talk1.1.google.com", 5222),
                    UnresolvedIpPortImpl("talk2.1.google.com", 5222),
                    UnresolvedIpPortImpl("talk3.1.google.com", 5222),
                    UnresolvedIpPortImpl("talk4.1.google.com", 5222)
                )
            ),
            ComposeFriendAccountConfiguration(
                requireDomain = false,
                serviceName = "livejournal.com",
                label = "LiveJournal",
                resource = resource,
                defaultServers = listOf(
                    UnresolvedIpPortImpl("xmpp.services.livejournal.com", 5222)
                )
            )
        ).forEach { configs[it.label] = it }
    }

    private companion object {
        const val JABBER_LABEL = "Jabber"
        const val DEFAULT_JABBER_SERVICE = "jabber.org"
    }
}

private class ComposeFriendAccountConfiguration(
    private val requireDomain: Boolean,
    private var serviceName: String,
    private var label: String,
    private val resource: String,
    private val defaultServers: List<UnresolvedIpPort>,
    private val modifyUser: Boolean = true,
    private val icon: Icon = EmptyIcon(16, 16),
    private val largeIcon: Icon = EmptyIcon(28, 28),
    private val type: Network.Type = Network.Type.XMPP
) : FriendAccountConfiguration {
    private val attributes = Collections.synchronizedMap(LinkedHashMap<String, Any?>())

    @Volatile
    private var username = ""

    @Volatile
    private var canonicalId = ""

    @Volatile
    private var password = ""

    override fun isDebugEnabled(): Boolean = false

    override fun getServiceName(): String = serviceName

    override fun setServiceName(serviceName: String) {
        this.serviceName = serviceName
    }

    override fun getLabel(): String = label

    override fun setLabel(label: String) {
        this.label = label
    }

    override fun getIcon(): Icon = icon

    override fun getLargeIcon(): Icon = largeIcon

    override fun getUserInputLocalID(): String = username

    override fun setUsername(username: String) {
        setCanonicalIdFromUsername(username)
        var updated = username
        if (modifyUser) {
            val at = updated.indexOf('@')
            updated = when {
                requireDomain && at == -1 -> "$updated@${getServiceName()}"
                !requireDomain && at > -1 -> updated.substring(0, at)
                else -> updated
            }
        }
        this.username = updated
    }

    private fun setCanonicalIdFromUsername(username: String) {
        canonicalId = if ('@' in username) {
            username.lowercase(Locale.US)
        } else {
            "$username@${getServiceName()}".lowercase(Locale.US)
        }
    }

    override fun getPassword(): String = password

    override fun setPassword(password: String) {
        this.password = password
    }

    override fun storePassword(): Boolean = true

    override fun getResource(): String = resource

    override fun getCanonicalizedLocalID(): String = canonicalId

    override fun getNetworkName(): String = serviceName

    override fun getType(): Network.Type = type

    override fun getRosterListener(): org.limewire.listener.EventListener<org.limewire.friend.api.RosterEvent>? = null

    override fun getDefaultServers(): List<UnresolvedIpPort> = defaultServers

    override fun setAttribute(key: String, property: Any?) {
        attributes[key] = property
    }

    override fun getAttribute(key: String): Any? = attributes[key]
}

class CoreComposePlayerService(
    private val audioPlayer: AudioPlayer,
    private val categoryManager: CategoryManager,
    private val launcher: DesktopLauncher,
    initialVolume: Float
) : ComposePlayerService {
    private val listeners = CopyOnWriteArrayList<ComposePlayerService.Listener>()
    private var knownAudioLengthBytes: Int? = null

    @Volatile
    private var currentFile: File? = null

    @Volatile
    private var lastTrackName = tr("Nothing selected")

    @Volatile
    private var shuffle = false

    @Volatile
    private var active = false

    private val audioListener = object : AudioPlayerListener {
        override fun songOpened(properties: Map<String, Any>) {
            knownAudioLengthBytes = properties["audio.length.bytes"] as? Int
            val author = properties["author"]?.toString()?.trim().orEmpty()
            val title = properties["title"]?.toString()?.trim().orEmpty()
            val fallback = currentFile?.nameWithoutExtension?.ifBlank { currentFile?.name.orEmpty() }
                ?.ifBlank { tr("Unknown") }
                ?: tr("Unknown")
            lastTrackName = when {
                author.isNotEmpty() && title.isNotEmpty() -> "$author - $title"
                title.isNotEmpty() -> title
                else -> fallback
            }
            notifyMediaChanged(lastTrackName)
        }

        override fun progressChange(bytesread: Int) {
            val total = knownAudioLengthBytes?.takeIf { it > 0 } ?: return
            notifyProgress((bytesread.toFloat() / total.toFloat()).coerceIn(0f, 1f))
        }

        override fun stateChange(event: AudioPlayerEvent) {
            if (event.state == PlayerState.EOM || event.state == PlayerState.STOPPED) {
                notifyProgress(0f)
            }
            notifyStateChanged(event.state)
        }
    }

    init {
        setVolume(initialVolume)
    }

    override fun activate(listener: ComposePlayerService.Listener) {
        listeners.addIfAbsent(listener)
        if (!active) {
            audioPlayer.addAudioPlayerListener(audioListener)
            active = true
        }
    }

    override fun deactivate(listener: ComposePlayerService.Listener) {
        listeners.remove(listener)
        if (active && listeners.isEmpty()) {
            audioPlayer.removeAudioPlayerListener(audioListener)
            active = false
        }
    }

    override fun status(): PlayerState = audioPlayer.status

    override fun currentFile(): File? = currentFile ?: audioPlayer.currentSong?.file

    override fun isVisible(): Boolean {
        val file = currentFile()
        return file != null && status() !in setOf(PlayerState.EOM, PlayerState.UNKNOWN, PlayerState.NO_SOUND_DEVICE)
    }

    override fun trackName(): String = lastTrackName

    override fun isShuffle(): Boolean = shuffle

    override fun isSeekable(): Boolean = knownAudioLengthBytes?.let { it > 0 } == true

    override fun isPlayable(file: File): Boolean {
        return categoryManager.getCategoryForFile(file) in setOf(Category.AUDIO, Category.VIDEO)
    }

    override fun setPlaylistAndPlay(item: LocalFileItem, playlist: ca.odell.glazedlists.EventList<LocalFileItem>?) {
        playFile(item.file)
    }

    override fun playFile(file: File) {
        when (categoryManager.getCategoryForFile(file)) {
            Category.AUDIO -> playAudio(file)
            Category.VIDEO -> launcher.open(file)
            else -> launcher.open(file)
        }
    }

    override fun pause() {
        audioPlayer.pause()
    }

    override fun resume() {
        when (audioPlayer.status) {
            PlayerState.STOPPED, PlayerState.UNKNOWN -> currentFile?.let { playAudio(it) }
            else -> audioPlayer.unpause()
        }
    }

    override fun seek(progress: Float) {
        val total = knownAudioLengthBytes?.takeIf { it > 0 } ?: return
        audioPlayer.seekLocation((total * progress.coerceIn(0f, 1f)).toLong())
    }

    override fun stop() {
        audioPlayer.stop()
    }

    override fun next() {
        // Queue navigation is Compose-owned; without an active queue there is no meaningful next track.
    }

    override fun previous() {
        // Queue navigation is Compose-owned; without an active queue there is no meaningful previous track.
    }

    override fun setShuffle(shuffle: Boolean) {
        this.shuffle = shuffle
    }

    override fun setVolume(value: Float) {
        audioPlayer.setVolume(value.coerceIn(0f, 1f))
    }

    private fun playAudio(file: File) {
        currentFile = file
        knownAudioLengthBytes = null
        lastTrackName = file.nameWithoutExtension.ifBlank { file.name }
        notifyMediaChanged(lastTrackName)
        audioPlayer.stop()
        audioPlayer.loadSong(file)
        audioPlayer.playSong()
    }

    private fun notifyProgress(progress: Float) {
        runOnUi {
            listeners.forEach { it.progressUpdated(progress) }
        }
    }

    private fun notifyMediaChanged(name: String) {
        runOnUi {
            listeners.forEach { it.mediaChanged(name) }
        }
    }

    private fun notifyStateChanged(state: PlayerState) {
        runOnUi {
            listeners.forEach { it.stateChanged(state) }
        }
    }
}

class SwingComposePlayerService(
    private val playerMediator: PlayerMediator
) : ComposePlayerService {
    private val listeners = CopyOnWriteArrayList<ComposePlayerService.Listener>()
    private val mediatorListener = object : PlayerMediatorListener {
        override fun progressUpdated(progress: Float) {
            listeners.forEach { it.progressUpdated(progress) }
        }

        override fun mediaChanged(name: String) {
            lastTrackName = name
            listeners.forEach { it.mediaChanged(name) }
        }

        override fun stateChanged(state: PlayerState) {
            listeners.forEach { it.stateChanged(state) }
        }
    }

    @Volatile
    private var active = false

    @Volatile
    private var lastTrackName = tr("Nothing selected")

    override fun activate(listener: ComposePlayerService.Listener) {
        listeners.addIfAbsent(listener)
        if (!active) {
            playerMediator.addMediatorListener(mediatorListener)
            active = true
        }
    }

    override fun deactivate(listener: ComposePlayerService.Listener) {
        listeners.remove(listener)
        if (active && listeners.isEmpty()) {
            playerMediator.removeMediatorListener(mediatorListener)
            active = false
        }
    }

    override fun status(): PlayerState = playerMediator.status

    override fun currentFile(): File? = playerMediator.currentMediaFile

    override fun isVisible(): Boolean {
        val current = currentFile()
        return current != null && status() !in setOf(PlayerState.EOM, PlayerState.UNKNOWN, PlayerState.NO_SOUND_DEVICE)
    }

    override fun trackName(): String = lastTrackName

    override fun isShuffle(): Boolean = playerMediator.isShuffle

    override fun isSeekable(): Boolean = playerMediator.isSeekable

    override fun isPlayable(file: File): Boolean = playerMediator.isPlayable(file)

    override fun setPlaylistAndPlay(item: LocalFileItem, playlist: ca.odell.glazedlists.EventList<LocalFileItem>?) {
        playerMediator.stop()
        playerMediator.setActivePlaylist(playlist)
        playerMediator.play(item)
    }

    override fun playFile(file: File) {
        playerMediator.playOrLaunchNatively(file)
    }

    override fun pause() {
        playerMediator.pause()
    }

    override fun resume() {
        playerMediator.resume()
    }

    override fun seek(progress: Float) {
        if (playerMediator.isSeekable) {
            playerMediator.seek(progress.coerceIn(0f, 1f).toDouble())
        }
    }

    override fun stop() {
        playerMediator.stop()
    }

    override fun next() {
        playerMediator.nextSong()
    }

    override fun previous() {
        playerMediator.prevSong()
    }

    override fun setShuffle(shuffle: Boolean) {
        playerMediator.setShuffle(shuffle)
    }

    override fun setVolume(value: Float) {
        playerMediator.setVolume(value.coerceIn(0f, 1f).toDouble())
    }
}

class SwingComposeFriendService(
    private val friendConnectionFactory: FriendConnectionFactory,
    private val friendAccountConfigurationManager: FriendAccountConfigurationManager,
    private val chatBridge: ComposeChatBridge,
    private val friendRequestListeners: ListenerSupport<FriendRequestEvent>
) : ComposeFriendService {
    private val listeners = CopyOnWriteArrayList<ComposeFriendService.Listener>()
    private val friendListeners = IdentityHashMap<ChatFriend, PropertyChangeListener>()
    private val chatFriendById = LinkedHashMap<String, ChatFriend>()
    private val rosterItems = LinkedHashMap<String, FriendRosterItem>()
    private val pendingFriendRequests = LinkedHashMap<Long, PendingFriendRequest>()
    private var nextFriendRequestId = 1L
    @Volatile
    private var rosterSyncScheduled = false

    private val rosterListListener = ListEventListener<ChatFriend> {
        scheduleRosterSync()
    }

    private val friendRequestListener = object : org.limewire.listener.EventListener<FriendRequestEvent> {
        override fun handleEvent(event: FriendRequestEvent) {
            if (event.type != FriendRequestEvent.Type.REQUESTED) {
                return
            }
            val pendingRequest = synchronized(this@SwingComposeFriendService) {
                PendingFriendRequest(
                    id = nextFriendRequestId++,
                    username = event.data.friendUsername,
                    request = event.data
                ).also { pendingFriendRequests[it.id] = it }
            }
            runOnUi {
                listeners.forEach { it.friendRequestReceived(pendingRequest) }
            }
        }
    }

    private val chatListener = object : ComposeChatBridge.Listener {
        override fun connectionChanged(event: FriendConnectionEvent) {
            if (event.type == FriendConnectionEvent.Type.DISCONNECTED) {
                clearState()
            } else if (event.type == FriendConnectionEvent.Type.CONNECTED) {
                scheduleRosterSync()
            }
            listeners.forEach { it.connectionChanged(event) }
        }

        override fun messageReceived(message: ComposeChatBridge.ComposeChatMessage) {
            listeners.forEach {
                it.messageReceived(
                    ConversationMessage(
                        id = message.id,
                        friendId = message.friendId,
                        senderName = message.senderName,
                        body = message.body,
                        timestamp = message.timestamp,
                        incoming = message.isIncoming,
                        outgoing = message.isOutgoing,
                        server = message.isServer,
                        kind = when (message.kind) {
                            ComposeChatBridge.MessageKind.FILE_OFFER -> ConversationMessageKind.FILE_OFFER
                            ComposeChatBridge.MessageKind.STATUS -> ConversationMessageKind.STATUS
                            ComposeChatBridge.MessageKind.TEXT -> ConversationMessageKind.TEXT
                        },
                        fileOffer = message.fileOffer?.let { offer ->
                            ConversationFileOffer(
                                offerId = offer.offerId,
                                fileName = offer.fileName,
                                size = offer.size,
                                description = offer.description,
                                urns = offer.urns,
                                downloadState = offer.downloadState,
                                localPath = offer.localPath
                            )
                        }
                    )
                )
            }
            scheduleRosterSync()
        }

        override fun chatStateChanged(friendId: String, state: ChatState) {
            listeners.forEach { it.chatStateChanged(friendId, state) }
        }

        override fun conversationReady(chatFriend: ChatFriend) {
            scheduleRosterSync()
            listeners.forEach { it.conversationReady(chatFriend.id) }
        }
    }

    @Volatile
    private var active = false

    override fun activate() {
        if (active) {
            return
        }
        active = true
        chatBridge.friends.addListEventListener(rosterListListener)
        chatBridge.addListener(chatListener)
        friendRequestListeners.addListener(friendRequestListener)
        chatBridge.activate()
        syncRoster()
    }

    override fun deactivate() {
        if (!active) {
            return
        }
        active = false
        chatBridge.friends.removeListEventListener(rosterListListener)
        chatBridge.removeListener(chatListener)
        friendRequestListeners.removeListener(friendRequestListener)
        detachFriendListeners()
        chatBridge.deactivate()
        clearState()
    }

    override fun addListener(listener: ComposeFriendService.Listener) {
        listeners.addIfAbsent(listener)
    }

    override fun removeListener(listener: ComposeFriendService.Listener) {
        listeners.remove(listener)
    }

    override fun roster(): List<FriendRosterItem> = synchronized(this) { rosterItems.values.toList() }

    override fun rosterItem(friendId: String): FriendRosterItem? = synchronized(this) { rosterItems[friendId] }

    override fun friendById(friendId: String): Friend? = synchronized(this) { chatFriendById[friendId]?.friend }

    override fun currentConnection(): FriendConnection? = chatBridge.currentConnection

    override fun lastConnectionEvent(): FriendConnectionEvent? = chatBridge.lastConnectionEvent

    override fun supportsAddRemove(): Boolean {
        val connection = currentConnection() ?: return false
        return connection.isLoggedIn && connection.supportsAddRemoveFriend()
    }

    override fun supportsPresenceModes(): Boolean {
        val connection = currentConnection() ?: return false
        return connection.isLoggedIn && connection.supportsMode()
    }

    override fun isDoNotDisturbEnabled(): Boolean = FriendSettings.DO_NOT_DISTURB.getValue()

    override fun loginOptions(): List<FriendLoginOption> {
        return friendAccountConfigurationManager.configurations
            .sortedBy { it.label }
            .map { FriendLoginOption(it.label) }
    }

    override fun preferredLoginDraft(): FriendLoginDraft? {
        val config = friendAccountConfigurationManager.autoLoginConfig
            ?: friendAccountConfigurationManager.configurations.firstOrNull()
            ?: return null
        return loginDraftFor(config.label)
    }

    override fun loginDraftFor(label: String): FriendLoginDraft? {
        val config = configFor(label) ?: return null
        val autoLoginConfig = friendAccountConfigurationManager.autoLoginConfig
        return FriendLoginDraft(
            configLabel = config.label,
            serviceName = config.serviceName ?: "",
            username = config.userInputLocalID ?: "",
            password = config.password ?: "",
            autoLogin = config == autoLoginConfig
        )
    }

    override fun saveLoginConfiguration(draft: FriendLoginDraft) {
        val config = applyLoginDraft(draft) ?: return
        if (draft.autoLogin) {
            friendAccountConfigurationManager.setAutoLoginConfig(config)
        } else {
            friendAccountConfigurationManager.setAutoLoginConfig(null)
        }
    }

    override fun submitLogin(draft: FriendLoginDraft) {
        val config = applyLoginDraft(draft) ?: return
        if (draft.autoLogin) {
            friendAccountConfigurationManager.setAutoLoginConfig(config)
        } else {
            friendAccountConfigurationManager.setAutoLoginConfig(null)
        }
        friendConnectionFactory.login(config)
    }

    private fun applyLoginDraft(draft: FriendLoginDraft): FriendAccountConfiguration? {
        val config = configFor(draft.configLabel) ?: return null
        config.setUsername(draft.username.trim())
        config.setPassword(draft.password)
        if (config.label == "Jabber") {
            config.setServiceName(draft.serviceName.trim())
        }
        return config
    }

    override fun logout() {
        currentConnection()?.logout()
    }

    override fun setDoNotDisturb(enabled: Boolean) {
        val connection = currentConnection() ?: return
        if (!connection.isLoggedIn || !connection.supportsMode()) {
            return
        }
        connection.setMode(if (enabled) FriendPresence.Mode.dnd else FriendPresence.Mode.available)
        FriendSettings.DO_NOT_DISTURB.setValue(enabled)
        scheduleRosterSync()
    }

    override fun addFriend(username: String, nickname: String) {
        val connection = currentConnection() ?: return
        if (!connection.supportsAddRemoveFriend()) {
            return
        }
        val trimmedUser = username.trim()
        if (trimmedUser.isEmpty()) {
            return
        }
        val normalizedId = if (trimmedUser.contains('@')) trimmedUser else "$trimmedUser@${connection.configuration.serviceName}"
        val normalizedNickname = nickname.trim().ifEmpty { trimmedUser }
        connection.addNewFriend(normalizedId, normalizedNickname)
    }

    override fun removeFriend(friendId: String) {
        currentConnection()?.removeFriend(friendId)
    }

    override fun acceptFriendRequest(requestId: Long) {
        resolveFriendRequest(requestId, true)
    }

    override fun declineFriendRequest(requestId: Long) {
        resolveFriendRequest(requestId, false)
    }

    override fun markConversationViewed(friendId: String) {
        synchronized(this) {
            chatFriendById[friendId]?.setHasUnviewedMessages(false)
        }
        scheduleRosterSync()
    }

    override fun sendMessage(friendId: String, text: String) {
        val friend = synchronized(this) { chatFriendById[friendId] } ?: return
        try {
            chatBridge.ensureConversation(friend).writeMessage(text)
        } catch (failure: FriendException) {
            throw failure
        }
    }

    override fun setChatState(friendId: String, state: ChatState) {
        try {
            chatBridge.setChatState(friendId, state)
        } catch (_: FriendException) {
        }
    }

    override fun closeConversation(friendId: String) {
        val friend = synchronized(this) { chatFriendById[friendId] } ?: return
        chatBridge.closeConversation(friend)
        scheduleRosterSync()
    }

    override fun canBrowseFriendLibrary(friendId: String): Boolean = chatBridge.supportsBrowse(friendId)

    override fun supportsOffTheRecord(friendId: String): Boolean = chatBridge.supportsOffTheRecord(friendId)

    override fun isOffTheRecordEnabled(friendId: String): Boolean = chatBridge.isOffTheRecord(friendId)

    override fun toggleOffTheRecord(friendId: String) {
        chatBridge.toggleOffTheRecord(friendId)
        scheduleRosterSync()
    }

    override fun supportsFileOffers(friendId: String): Boolean = chatBridge.supportsFileOffers(friendId)

    override fun offerFile(friendId: String, file: File) {
        chatBridge.offerFile(friendId, file)
    }

    override fun offerFolder(friendId: String, folder: File) {
        chatBridge.offerFolder(friendId, folder)
    }

    override fun downloadFileOffer(messageId: String) {
        chatBridge.downloadFileOffer(messageId)
    }

    private fun configFor(label: String): FriendAccountConfiguration? {
        return friendAccountConfigurationManager.configurations.firstOrNull { it.label == label }
    }

    private fun scheduleRosterSync() {
        if (!active) {
            return
        }
        synchronized(this) {
            if (rosterSyncScheduled) {
                return
            }
            rosterSyncScheduled = true
        }
        EventQueue.invokeLater {
            synchronized(this) {
                rosterSyncScheduled = false
            }
            if (!active) {
                return@invokeLater
            }
            syncRoster()
        }
    }

    private fun syncRoster() {
        val chatFriends = snapshotEventList(chatBridge.friends)
        runOnUi {
            ComposePerformanceTracker.measure("friends.syncRoster") {
                val rosterSnapshot = synchronized(this@SwingComposeFriendService) {
                    val desiredById = LinkedHashMap<String, ChatFriend>(chatFriends.size)
                    chatFriends.forEach { friend ->
                        desiredById[friend.id] = friend
                        attachFriendListener(friend)
                    }

                    chatFriendById.entries.toList().forEach { (friendId, friend) ->
                        val replacement = desiredById[friendId]
                        if (replacement === friend) {
                            return@forEach
                        }
                        detachFriendListener(friend)
                        if (replacement == null) {
                            chatFriendById.remove(friendId)
                            rosterItems.remove(friendId)
                        }
                    }

                    desiredById.forEach { (friendId, friend) ->
                        chatFriendById[friendId] = friend
                    }

                    val nextSnapshot = desiredById.values
                        .sortedWith(compareByDescending<ChatFriend> { it.isSignedIn() }
                            .thenByDescending { it.hasUnviewedMessages() }
                            .thenBy { it.name.lowercase(Locale.US) })
                        .map(::rosterItemFor)

                    if (nextSnapshot != rosterItems.values.toList()) {
                        rosterItems.clear()
                        nextSnapshot.forEach { rosterItems[it.id] = it }
                        nextSnapshot
                    } else {
                        null
                    }
                }
                rosterSnapshot?.let { snapshot ->
                    listeners.forEach { it.rosterChanged(snapshot) }
                }
            }
        }
    }

    private fun clearState() {
        synchronized(this) {
            rosterSyncScheduled = false
            detachFriendListeners()
            chatFriendById.clear()
            rosterItems.clear()
            pendingFriendRequests.clear()
        }
        listeners.forEach { it.rosterChanged(emptyList()) }
    }

    private fun resolveFriendRequest(requestId: Long, accept: Boolean) {
        val pending = synchronized(this) { pendingFriendRequests.remove(requestId) } ?: return
        BackgroundExecutorService.execute {
            pending.request.decisionHandler.handleDecision(pending.request.friendUsername, accept)
        }
    }

    private fun detachFriendListeners() {
        friendListeners.forEach { (friend, listener) ->
            friend.removePropertyChangeListener(listener)
        }
        friendListeners.clear()
    }

    private fun attachFriendListener(friend: ChatFriend) {
        if (friendListeners.containsKey(friend)) {
            return
        }
        val listener = PropertyChangeListener { scheduleRosterSync() }
        friend.addPropertyChangeListener(listener)
        friendListeners[friend] = listener
    }

    private fun detachFriendListener(friend: ChatFriend) {
        friendListeners.remove(friend)?.let(friend::removePropertyChangeListener)
    }

    private fun rosterItemFor(friend: ChatFriend): FriendRosterItem {
        return FriendRosterItem(
            id = friend.id,
            displayName = friend.name,
            status = friend.status ?: "",
            mode = friend.mode,
            signedIn = friend.isSignedIn(),
            unreadMessages = friend.hasUnviewedMessages(),
            browseable = chatBridge.supportsBrowse(friend.id),
            supportsOffTheRecord = chatBridge.supportsOffTheRecord(friend.id),
            supportsFileOffers = chatBridge.supportsFileOffers(friend.id)
        )
    }
}

class CoreComposeBrowseService(
    private val browseSearchFactory: BrowseSearchFactory,
    private val searchManager: SearchManager,
    private val friendService: ComposeFriendService
) : ComposeBrowseService {
    override fun browseAllFriends(): BrowseRequest {
        val search = browseSearchFactory.createAllFriendsBrowseSearch()
        val details = BrowseSearchDetails(tr("Friends' Files"), SearchDetails.SearchType.ALL_FRIENDS_BROWSE)
        val results = searchManager.addSearch(search, details)
        return BrowseRequest(
            title = tr("Friends' Files"),
            query = tr("Friends' Files"),
            searchType = SearchDetails.SearchType.ALL_FRIENDS_BROWSE,
            search = search,
            resultList = results
        )
    }

    override fun browseFriend(friendId: String): BrowseRequest? {
        val friend = friendService.friendById(friendId) ?: return null
        val search = browseSearchFactory.createFriendBrowseSearch(friend)
        val title = tr("{0}'s Files", friend.renderName)
        val details = BrowseSearchDetails(title, SearchDetails.SearchType.SINGLE_BROWSE)
        val results = searchManager.addSearch(search, details)
        return BrowseRequest(title, title, SearchDetails.SearchType.SINGLE_BROWSE, search, results)
    }

    override fun browsePresence(presence: FriendPresence): BrowseRequest {
        val search = browseSearchFactory.createBrowseSearch(presence)
        val title = tr("{0}'s Files", presence.friend.renderName)
        val details = BrowseSearchDetails(title, SearchDetails.SearchType.SINGLE_BROWSE)
        val results = searchManager.addSearch(search, details)
        return BrowseRequest(title, title, SearchDetails.SearchType.SINGLE_BROWSE, search, results)
    }

    override fun browsePresences(presences: Collection<FriendPresence>): BrowseRequest? {
        if (presences.isEmpty()) {
            return null
        }
        if (presences.size == 1) {
            return browsePresence(presences.first())
        }
        val search = browseSearchFactory.createBrowseSearch(presences)
        val title = tr("Browse Files")
        val details = BrowseSearchDetails(title, SearchDetails.SearchType.MULTIPLE_BROWSE)
        val results = searchManager.addSearch(search, details)
        return BrowseRequest(title, title, SearchDetails.SearchType.MULTIPLE_BROWSE, search, results)
    }
}

private data class BrowseSearchDetails(
    private val query: String,
    private val searchType: SearchDetails.SearchType
) : SearchDetails {
    override fun getSearchCategory(): SearchCategory = SearchCategory.ALL

    override fun getSearchQuery(): String = query

    override fun getSearchType(): SearchDetails.SearchType = searchType

    override fun getAdvancedDetails(): Map<FilePropertyKey, String> = emptyMap()
}
