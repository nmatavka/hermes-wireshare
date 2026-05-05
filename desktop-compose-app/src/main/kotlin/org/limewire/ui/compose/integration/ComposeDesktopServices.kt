package org.limewire.ui.compose.integration

import ca.odell.glazedlists.event.ListEventListener
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
import org.limewire.friend.api.FriendException
import org.limewire.friend.api.Network
import org.limewire.friend.api.FriendPresence
import org.limewire.io.IP
import org.limewire.io.IOUtils
import org.limewire.listener.ListenerSupport
import org.limewire.player.api.AudioPlayer
import org.limewire.player.api.AudioPlayerEvent
import org.limewire.player.api.AudioPlayerListener
import org.limewire.player.api.PlayerState
import org.limewire.ui.compose.BlockingPromptBroker
import org.limewire.ui.compose.CategorySaveDirectoriesDraft
import org.limewire.ui.compose.ConversationMessage
import org.limewire.ui.compose.ConnectionColumn
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
import org.limewire.ui.compose.PendingFriendRequest
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
import org.limewire.ui.compose.FriendsNotificationsPreferencesDraft
import org.limewire.ui.compose.AddToLibraryDefaultsDraft
import org.limewire.ui.compose.AdvancedSearchSuggestionEntry
import org.limewire.ui.compose.ComposePerformanceTracker
import org.limewire.ui.compose.runOnUi
import org.limewire.ui.compose.snapshotEventList
import org.limewire.ui.compose.tr
import org.limewire.util.CommonUtils
import org.limewire.util.FileUtils
import org.limewire.util.OSUtils
import java.awt.EventQueue
import java.awt.Window
import java.beans.PropertyChangeListener
import java.io.File
import java.io.FilenameFilter
import java.io.IOException
import java.util.Collections
import java.util.LinkedHashMap
import java.util.Locale
import java.net.NetworkInterface
import java.net.SocketException
import java.util.concurrent.CopyOnWriteArrayList

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
