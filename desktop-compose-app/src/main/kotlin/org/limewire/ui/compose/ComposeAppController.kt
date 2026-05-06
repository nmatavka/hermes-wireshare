package org.limewire.ui.compose

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import ca.odell.glazedlists.event.ListEvent
import ca.odell.glazedlists.event.ListEventListener
import com.limegroup.gnutella.LifecycleManager
import com.limegroup.gnutella.util.LimeWireUtils
import org.limewire.bittorrent.LimeWireTorrentProperties
import org.limewire.bittorrent.Torrent
import org.limewire.core.api.Application
import org.limewire.core.api.Category
import org.limewire.core.api.FilePropertyKey
import org.limewire.core.api.callback.GuiCallback
import org.limewire.core.api.connection.ConnectionItem
import org.limewire.core.api.connection.ConnectionStrength
import org.limewire.core.api.connection.FWTStatusReason
import org.limewire.core.api.connection.FirewallStatus
import org.limewire.core.api.connection.FirewallTransferStatus
import org.limewire.core.api.connection.GnutellaConnectionManager
import org.limewire.core.api.download.DownloadAction
import org.limewire.core.api.download.DownloadPiecesInfo
import org.limewire.core.api.download.DownloadException
import org.limewire.core.api.download.DownloadItem
import org.limewire.core.api.download.DownloadListManager
import org.limewire.core.api.download.DownloadState
import org.limewire.core.api.endpoint.RemoteHost
import org.limewire.core.api.file.CategoryManager
import org.limewire.core.api.library.FileProcessingEvent
import org.limewire.core.api.library.LibraryData
import org.limewire.core.api.library.LibraryManager
import org.limewire.core.api.library.LocalFileItem
import org.limewire.core.api.library.LocalFileList
import org.limewire.core.api.library.MagnetLinkFactory
import org.limewire.core.api.library.MetaDataException
import org.limewire.core.api.library.MetaDataManager
import org.limewire.core.api.library.SharedFileList
import org.limewire.core.api.library.SharedFileListManager
import org.limewire.core.api.magnet.MagnetFactory
import org.limewire.core.api.magnet.MagnetLink
import org.limewire.core.api.properties.PropertyDictionary
import org.limewire.core.api.search.GroupedSearchResult
import org.limewire.core.api.search.Search
import org.limewire.core.api.search.SearchCategory
import org.limewire.core.api.search.SearchDetails
import org.limewire.core.api.search.SearchFactory
import org.limewire.core.api.search.SearchListener
import org.limewire.core.api.search.SearchManager
import org.limewire.core.api.search.SearchResult
import org.limewire.core.api.search.SearchResultList
import org.limewire.core.api.search.browse.BrowseSearch
import org.limewire.core.api.search.browse.BrowseStatus
import org.limewire.core.api.search.browse.BrowseStatus.BrowseState
import org.limewire.core.api.search.browse.BrowseStatusListener
import org.limewire.core.api.spam.SpamManager
import org.limewire.core.api.upload.UploadItem
import org.limewire.core.api.upload.UploadState
import org.limewire.core.settings.BittorrentSettings
import org.limewire.core.settings.ConnectionSettings
import org.limewire.core.settings.DownloadSettings
import org.limewire.core.settings.InstallSettings
import org.limewire.core.settings.LibrarySettings
import org.limewire.core.settings.UploadSettings
import org.limewire.ed2k.api.Ed2kDownloadItem
import org.limewire.ed2k.api.Ed2kGroupedSearchResultView
import org.limewire.ed2k.api.Ed2kListener
import org.limewire.ed2k.api.Ed2kServerRecord
import org.limewire.ed2k.api.Ed2kService
import org.limewire.ed2k.api.Ed2kStatus
import org.limewire.ed2k.api.Ed2kUploadItem
import org.limewire.friend.api.ChatState
import org.limewire.friend.api.FriendConnection
import org.limewire.friend.api.FriendConnectionEvent
import org.limewire.friend.api.FriendException
import org.limewire.friend.api.FriendPresence
import org.limewire.player.api.PlayerState
import org.limewire.ui.compose.integration.BrowseRequest
import org.limewire.ui.compose.integration.ComposeBrowseService
import org.limewire.ui.compose.integration.ComposeDelayedExitService
import org.limewire.ui.compose.integration.ComposeFriendService
import org.limewire.ui.compose.integration.ComposeFileAppearanceService
import org.limewire.ui.compose.integration.ComposeAdvancedSearchSuggestionsService
import org.limewire.ui.compose.integration.ComposeAdvancedToolsService
import org.limewire.ui.compose.integration.ComposeLocalizationService
import org.limewire.ui.compose.integration.ComposeMojitoVisualizerSession
import org.limewire.ui.compose.integration.ComposePlayerService
import org.limewire.ui.compose.integration.ComposeRecentDownloadsService
import org.limewire.ui.compose.integration.ComposeSearchSuggestionsService
import org.limewire.ui.compose.integration.ComposeSystemMessage
import org.limewire.ui.compose.integration.ComposeSystemMessageService
import org.limewire.ui.compose.integration.ComposeSystemMessageSeverity
import org.limewire.ui.compose.integration.ComposeTrayService
import org.limewire.ui.compose.integration.ComposeTransferRepairService
import org.limewire.ui.compose.integration.ComposeRuntimeErrorReport
import org.limewire.ui.compose.integration.ComposeRuntimeErrorService
import org.limewire.ui.compose.integration.DesktopLaunchResult
import org.limewire.ui.compose.integration.DesktopFilePicker
import org.limewire.ui.compose.settings.ComposeSettingsService
import org.limewire.ui.compose.integration.DesktopLauncher
import org.limewire.ui.compose.integration.DesktopNotifications
import org.limewire.ui.compose.integration.FileIdentityPresentation
import org.limewire.ui.compose.integration.FileIconPresentation
import org.limewire.util.OSUtils
import org.limewire.listener.EventListener
import com.google.common.base.Predicate
import java.awt.EventQueue
import java.awt.Frame
import java.awt.Component
import java.awt.Window
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.awt.event.WindowEvent
import java.awt.event.WindowStateListener
import java.beans.PropertyChangeListener
import java.io.File
import java.io.FileFilter
import java.io.FilenameFilter
import java.io.IOException
import java.util.ArrayDeque
import java.net.MalformedURLException
import java.net.URI
import java.net.URL
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean
import org.limewire.util.CommonUtils
import org.limewire.util.FileUtils

sealed interface ComposeScreen {
    data object Library : ComposeScreen
    data object Transfers : ComposeScreen
    data object Friends : ComposeScreen
    data object Player : ComposeScreen
    data class Search(val tabId: Long) : ComposeScreen
}

enum class TransferTrayMode {
    DOWNLOADS,
    UPLOADS
}

enum class SearchSourceFilter {
    ALL,
    FRIENDS,
    NETWORK,
    ED2K_KAD,
    BROWSABLE
}

private enum class SearchFilterGroup {
    SOURCE,
    FRIEND,
    CATEGORY,
    EXTENSION,
    FILE_TYPE,
    ARTIST,
    ALBUM,
    GENRE,
    SIZE,
    LENGTH,
    BITRATE,
    QUALITY
}

data class SearchRangeBucket(
    val id: String,
    val label: String,
    val minimum: Long,
    val maximum: Long? = null,
    val maximumAbsolute: Boolean = false
)

data class MessageDialogState(
    val title: String,
    val message: String,
    val severity: MessageDialogSeverity = MessageDialogSeverity.INFO,
    val confirmLabel: String = tr("OK"),
    val checkboxLabel: String? = null,
    val checkboxInitialChecked: Boolean = false,
    val onCloseWithCheckbox: ((Boolean) -> Unit)? = null
)

enum class MessageDialogSeverity {
    INFO,
    ERROR
}

data class ConfirmationDialogState(
    val title: String,
    val message: String,
    val confirmLabel: String,
    val alternateLabel: String? = null,
    val dismissLabel: String = tr("Cancel"),
    val checkboxLabel: String? = null,
    val checkboxInitialChecked: Boolean = false,
    val onConfirmWithCheckbox: ((Boolean) -> Unit)? = null,
    val onConfirm: () -> Unit,
    val onAlternate: (() -> Unit)? = null,
    val onDismiss: () -> Unit = {}
)

data class TextEntryDialogState(
    val title: String,
    val label: String,
    val initialValue: String = "",
    val confirmLabel: String = tr("Save"),
    val validator: (String) -> String? = { null },
    val onConfirm: (String) -> String? = { null }
)

data class LibraryDeletionChoiceDialogState(
    val title: String,
    val message: String,
    val removeLabel: String,
    val deleteLabel: String,
    val onRemove: () -> Unit,
    val onDelete: () -> Unit
)

class LibraryFolderImportCategoryOption(
    val category: Category,
    val label: String,
    val enabled: Boolean,
    selected: Boolean
) {
    var selected by mutableStateOf(selected && enabled)
}

data class KnownFileTypeGroup(
    val label: String,
    val extensions: List<String>
)

data class LibraryKnownTypesDialogState(
    val title: String,
    val groups: List<KnownFileTypeGroup>,
    val otherMessage: String
)

class LibraryFolderImportDialogState(
    val title: String,
    val message: String,
    val recursiveLabel: String,
    val topLevelLabel: String,
    val confirmLabel: String,
    val categoryOptions: List<LibraryFolderImportCategoryOption>,
    val programsDisabledMessage: String? = null,
    val documentsDisabledMessage: String? = null,
    private val onConfirmAction: (Boolean, Set<Category>, String) -> Unit
) {
    var recursive by mutableStateOf(LibrarySettings.DEFAULT_RECURSIVELY_ADD_FOLDERS_OPTION.getValue())
    var advancedExpanded by mutableStateOf(false)
    var advancedExtensions by mutableStateOf("")

    fun confirm() {
        onConfirmAction(
            recursive,
            categoryOptions
                .filter { it.enabled && it.selected }
                .mapTo(linkedSetOf(), LibraryFolderImportCategoryOption::category),
            advancedExtensions
        )
    }
}

data class SharedListSharingSummaryItem(
    val label: String,
    val removableIds: List<String> = emptyList()
)

data class SharedListSharingEditorRow(
    val id: String?,
    val label: String,
    val detail: String? = null,
    val selected: Boolean
)

data class DocumentSharingWarningDialogState(
    val title: String,
    val message: String,
    val continueLabel: String,
    val unshareLabel: String,
    val onContinue: () -> Unit,
    val onUnshareAll: () -> Unit,
    val onDismiss: () -> Unit
)

data class LibrarySection(
    val id: String,
    val title: String,
    val list: LocalFileList,
    val isShared: Boolean,
    val isPublic: Boolean = false
)

private enum class PlayerQueueMode {
    NONE,
    SNAPSHOT,
    LIBRARY_LIVE
}

class ComposeAppController(
    private val application: Application,
    private val lifecycleManager: LifecycleManager,
    private val searchFactory: SearchFactory,
    private val searchManager: SearchManager,
    private val downloadListManager: DownloadListManager,
    private val uploadListManager: org.limewire.core.api.upload.UploadListManager,
    private val libraryData: LibraryData,
    private val libraryManager: LibraryManager,
    private val sharedFileListManager: SharedFileListManager,
    private val metaDataManager: MetaDataManager,
    private val propertyDictionary: PropertyDictionary,
    private val categoryManager: CategoryManager,
    private val fileAppearanceService: ComposeFileAppearanceService,
    private val spamManager: SpamManager,
    private val magnetFactory: MagnetFactory,
    private val magnetLinkFactory: MagnetLinkFactory,
    private val connectionManager: GnutellaConnectionManager,
    private val filePicker: DesktopFilePicker,
    private val launcher: DesktopLauncher,
    private val notifications: DesktopNotifications,
    private val playerService: ComposePlayerService,
    private val friendService: ComposeFriendService,
    private val browseService: ComposeBrowseService,
    private val recentDownloadsService: ComposeRecentDownloadsService,
    private val transferRepairService: ComposeTransferRepairService,
    private val ed2kService: Ed2kService,
    private val advancedToolsService: ComposeAdvancedToolsService,
    private val searchSuggestionsService: ComposeSearchSuggestionsService,
    private val advancedSearchSuggestionsService: ComposeAdvancedSearchSuggestionsService,
    private val runtimeErrorService: ComposeRuntimeErrorService,
    private val systemMessageService: ComposeSystemMessageService,
    private val trayService: ComposeTrayService,
    private val delayedExitService: ComposeDelayedExitService,
    private val settingsService: ComposeSettingsService,
    private val localizationService: ComposeLocalizationService
) {
    companion object {
        private const val DEFAULT_LIBRARY_SECTION_ID = "library"
        private const val BULK_SEARCH_DOWNLOAD_CHUNK_SIZE = 24
        private val LEGACY_SEARCH_COLUMNS = setOf(
            SearchColumn.NAME,
            SearchColumn.TYPE,
            SearchColumn.SIZE,
            SearchColumn.SOURCES,
            SearchColumn.FRIENDS,
            SearchColumn.ARTIST,
            SearchColumn.ALBUM,
            SearchColumn.GENRE,
            SearchColumn.YEAR,
            SearchColumn.AUTHOR,
            SearchColumn.COMPANY,
            SearchColumn.PLATFORM,
            SearchColumn.DESCRIPTION,
            SearchColumn.FILES,
            SearchColumn.TRACKERS
        )
        private val LEGACY_LIBRARY_COLUMNS = setOf(
            LibraryColumn.NAME,
            LibraryColumn.TYPE,
            LibraryColumn.SIZE,
            LibraryColumn.ACTIVITY,
            LibraryColumn.HITS,
            LibraryColumn.UPLOADS,
            LibraryColumn.UPLOAD_ATTEMPTS,
            LibraryColumn.UPDATED,
            LibraryColumn.LOCATION,
            LibraryColumn.ARTIST,
            LibraryColumn.ALBUM,
            LibraryColumn.GENRE,
            LibraryColumn.YEAR,
            LibraryColumn.AUTHOR,
            LibraryColumn.COMPANY,
            LibraryColumn.PLATFORM,
            LibraryColumn.DESCRIPTION,
            LibraryColumn.FILES,
            LibraryColumn.TRACKERS
        )
        private const val ADVANCED_CONSOLE_IDEAL_SIZE = 20_000
        private const val ADVANCED_CONSOLE_MAX_EXCESS = 5_000
        private val ADVANCED_CONSOLE_LEVELS = listOf("OFF", "ALL", "DEBUG", "ERROR", "FATAL", "INFO", "WARN")
    }

    private data class SelectionUpdate(
        val selectedKeys: List<String>,
        val primaryKey: String?,
        val anchorKey: String?
    )

    private data class BulkSearchDownloadSummary(
        val total: Int,
        var started: Int = 0,
        var autoRenamed: Int = 0,
        var replacedExisting: Int = 0,
        var alreadySaved: Int = 0,
        var alreadyDownloading: Int = 0,
        var alreadyUploading: Int = 0,
        var nameConflicts: Int = 0,
        var failed: Int = 0,
        var firstFailureMessage: String? = null
    )

    private enum class SearchPresentationDirty(val mask: Int) {
        RESULTS(1 shl 0),
        FILTERS(1 shl 1),
        SORT(1 shl 2),
        SELECTION(1 shl 3),
        STATUS(1 shl 4);

        companion object {
            const val FULL: Int = (1 shl 0) or (1 shl 1) or (1 shl 2) or (1 shl 4)
        }
    }

    private data class TransferActivitySummary(
        val downloadCount: Int = 0,
        val uploadCount: Int = 0,
        val activeDownloadCount: Int = 0,
        val activeUploadCount: Int = 0,
        val totalDownloadBandwidth: Float = 0f,
        val totalUploadBandwidth: Float = 0f
    )

    private data class SearchSortSnapshot(
        val result: GroupedSearchResult,
        val relevance: Float,
        val categoryName: String,
        val nameSortKey: String,
        val baseName: String,
        val fileNameLower: String,
        val extension: String,
        val sourceCount: Int,
        val friendsCount: Int,
        val singleSourceFriendName: String?,
        val sourceLabel: String,
        val size: Long,
        val length: Long,
        val quality: Long,
        val bitrate: Long,
        val track: TrackSortValue,
        val artist: String,
        val album: String,
        val genre: String,
        val year: Long,
        val author: String,
        val company: String,
        val platform: String,
        val description: String,
        val torrentFiles: Int,
        val torrentTrackers: Int,
        val spamRank: Int,
        val localRank: Int
    )

    private data class LibraryVisibilityCache(
        val visibleItems: List<LocalFileItem> = emptyList(),
        val selectedItems: List<LocalFileItem> = emptyList(),
        val selectedItem: LocalFileItem? = null
    )

    private data class SearchDownloadIndex(
        val byUrn: Map<String, DownloadItem> = emptyMap(),
        val finishedUrns: Set<String> = emptySet(),
        val activeUrns: Set<String> = emptySet()
    )

    private data class SearchLocalAvailability(
        val jumpTargets: List<LibraryJumpTarget>,
        val availabilityLabel: String?,
        val downloadItem: DownloadItem?,
        val localRank: Int
    )

    private data class SearchLibraryIndex(
        val libraryUrns: Set<String> = emptySet(),
        val sharedTargetsByUrn: Map<String, List<LibraryJumpTarget>> = emptyMap()
    )

    private data class SearchFacetAggregation(
        val categoryCounts: Map<SearchCategory, Int> = emptyMap(),
        val sourceCounts: Map<SearchSourceFilter, Int> = emptyMap(),
        val anyFriendCount: Int = 0,
        val totalCount: Int = 0
    )

    private data class SharingSummaryCache(
        val availableCollections: List<SharedFileList> = emptyList(),
        val availableFriendCollections: List<SharedFileList> = emptyList(),
        val sharingStatusSummary: SharingStatusSummary = SharingStatusSummary(
            sharedFileCount = 0,
            publicCollectionCount = 0,
            friendSharedCollectionCount = 0,
            collections = emptyList(),
            showSignInToShareWithFriends = false
        )
    )

    private data class TorrentInspectorSnapshotKey(
        val torrentSha1: String?,
        val downloadUrn: String?,
        val includeActivity: Boolean,
        val includePieces: Boolean
    )

    private data class CachedTorrentInspectorSnapshot(
        val refreshEpoch: Int,
        val refreshedAt: Long,
        val state: TorrentInspectorLiveState
    )

    val promptBroker = BlockingPromptBroker()

    private val shuttingDown = AtomicBoolean(false)
    private val activated = AtomicBoolean(false)
    private var nextSearchTabId = 1L
    private var exitHandler: (() -> Unit)? = null
    private var windowRef: Window? = null
    private var windowListenersAttached = false
    private var windowPlacementApplied = false
    private var startupRuntimeErrorsFlushed = false
    private var startupSystemWarningsShown = false
    private var startupGnutellaConnectAttempted = false
    private var systemMessageListenerAttached = false
    private val librarySectionStates = mutableMapOf<String, LibrarySectionViewState>()
    private val downloadCompletionStates = mutableMapOf<String, Boolean>()
    private var advancedConsoleCloseable: AutoCloseable? = null
    private var advancedMojitoVisualizerSession: ComposeMojitoVisualizerSession? = null
    private val advancedConsoleDelayBuffer = StringBuilder()
    private var advancedConsoleLastFlushAt = 0L
    private var appliedConsoleDelaySeconds = 0
    private var playerQueueMode = PlayerQueueMode.NONE
    private val pendingMessageDialogs = ArrayDeque<MessageDialogState>()
    private var playerPlaybackAttemptId = 0L
    private var warnedNoSoundDeviceAttemptId = -1L
    private var visibleDownloadsDirty = true
    private var visibleUploadsDirty = true
    private var transferSummaryDirty = true
    private var libraryVisibilityDirty = true
    private var librarySelectionDirty = true
    private var sharingSummaryDirty = true
    private var searchDownloadIndexDirty = true
    private var searchLibraryIndexDirty = true
    private var cachedVisibleDownloads = emptyList<DownloadItem>()
    private var cachedVisibleUploads = emptyList<UploadItem>()
    private var cachedTransferSummary = TransferActivitySummary()
    private var cachedLibraryVisibility = LibraryVisibilityCache()
    private var cachedSharingSummary = SharingSummaryCache()
    private var cachedSearchDownloadIndex = SearchDownloadIndex()
    private var cachedSearchLibraryIndex = SearchLibraryIndex()
    private val searchLocalAvailabilityCache = mutableMapOf<String, SearchLocalAvailability>()
    private val searchResultPresentationCache = mutableMapOf<String, SearchResultPresentation>()
    private val torrentInspectorSnapshots = mutableMapOf<TorrentInspectorSnapshotKey, CachedTorrentInspectorSnapshot>()

    private val closeables = mutableListOf<AutoCloseable>()
    private var activeLibraryBinding: AutoCloseable? = null
    private var activeSharedFriendIdsBinding: AutoCloseable? = null
    private val pendingCollectionShares = mutableMapOf<Int, String>()
    private val publicDocumentWarningBindings = mutableMapOf<Int, AutoCloseable>()

    private val playerListener = object : ComposePlayerService.Listener {
        override fun progressUpdated(progress: Float) {
            playerProgress = progress
        }

        override fun mediaChanged(name: String) {
            playerTrackName = name
            refreshPlayerState()
        }

        override fun stateChanged(state: PlayerState) {
            playerState = state
            refreshPlayerState()
            handlePlayerStateFeedback(state)
        }
    }

    private val friendListener = object : ComposeFriendService.Listener {
        override fun rosterChanged(friends: List<FriendRosterItem>) {
            val friendsById = friends.associateBy(FriendRosterItem::id)
            reconcileKeyedStateList(chatFriends, friends, FriendRosterItem::id)
            chatConversations.forEach { (friendId, conversation) ->
                friendsById[friendId]?.let { updated ->
                    conversation.friend = updated
                    conversation.offTheRecordEnabled = if (updated.supportsOffTheRecord) {
                        friendService.isOffTheRecordEnabled(updated.id)
                    } else {
                        null
                    }
                }
            }
            onlineChatFriendCount = friends.count { it.signedIn }
            unreadChatFriendCount = friends.count { it.hasUnviewedMessages() }
            chatFriendsEpoch += 1
            if (selectedConversationId != null && chatFriends.none { it.id == selectedConversationId }) {
                selectedConversationId = chatFriends.firstOrNull()?.id
            }
            if (searchSuggestionsVisible) {
                refreshSearchSuggestions()
            }
        }

        override fun connectionChanged(event: FriendConnectionEvent) {
            friendConnectionState = event.type
            friendDoNotDisturb = friendService.isDoNotDisturbEnabled()
            when (event.type) {
                FriendConnectionEvent.Type.CONNECTING -> {
                    friendLoginBusy = true
                    friendLoginError = null
                }

                FriendConnectionEvent.Type.CONNECTED -> {
                    friendLoginBusy = false
                    friendLoginError = null
                    friendLoginDialogOpen = false
                }

                FriendConnectionEvent.Type.CONNECT_FAILED -> {
                    friendLoginBusy = false
                    friendLoginError = friendConnectionErrorMessage(event.exception)
                    friendLoginDialogOpen = true
                }

                FriendConnectionEvent.Type.DISCONNECTED -> {
                    friendLoginBusy = false
                    if (event.exception != null) {
                        friendLoginError = friendConnectionErrorMessage(event.exception)
                    }
                    pendingFriendRequests.clear()
                    clearChatState()
                }
            }
        }

        override fun messageReceived(message: ConversationMessage) {
            val friend = friendService.rosterItem(message.friendId) ?: return
            val conversation = ensureConversation(friend)
            upsertConversationMessage(conversation, message)
            conversation.offTheRecordEnabled = if (friend.supportsOffTheRecord) {
                friendService.isOffTheRecordEnabled(friend.id)
            } else {
                null
            }
            if (message.isIncoming) {
                if (currentScreen == ComposeScreen.Friends && selectedConversationId == friend.id) {
                    friendService.markConversationViewed(friend.id)
                } else {
                    maybeShowChatNotification(message)
                }
            }
        }

        override fun chatStateChanged(friendId: String, state: ChatState) {
            chatConversations[friendId]?.remoteState = state
        }

        override fun conversationReady(friendId: String) {
            friendService.rosterItem(friendId)?.let {
                ensureConversation(it).offTheRecordEnabled = if (it.supportsOffTheRecord) {
                    friendService.isOffTheRecordEnabled(it.id)
                } else {
                    null
                }
            }
        }

        override fun friendRequestReceived(request: PendingFriendRequest) {
            pendingFriendRequests.removeAll { it.id == request.id }
            pendingFriendRequests.add(request)
            maybeShowFriendRequestNotification(request)
        }
    }

    private val delayedExitListener = object : ComposeDelayedExitService.Listener {
        override fun stateChanged(state: DelayedExitState) {
            delayedExitState = state
            syncDesktopShellState()
        }

        override fun exitReady() {
            requestExit()
        }
    }

    private val ed2kListener = object : Ed2kListener {
        override fun downloadsChanged() {
            runOnUi {
                syncEd2kDownloads()
            }
        }

        override fun uploadsChanged() {
            runOnUi {
                syncEd2kUploads()
            }
        }

        override fun statusChanged() {
            runOnUi {
                syncEd2kStatus()
            }
        }

        override fun searchChanged(sessionId: String) {
            runOnUi {
                syncEd2kSearchSession(sessionId)
            }
        }
    }

    private val trayListener = object : ComposeTrayService.Listener {
        override fun restoreRequested() {
            restoreApplication()
        }

        override fun showTransfersRequested() {
            restoreApplication()
            openTransfersWorkspace(TransferTrayMode.DOWNLOADS)
        }

        override fun toggleExitAfterTransfersRequested() {
            if (delayedExitState.pending) {
                cancelExitAfterTransfers()
            } else {
                startExitAfterTransfers()
            }
        }

        override fun quitRequested() {
            requestExit()
        }
    }

    val searchTabs = mutableStateListOf<SearchTabSession>()
    val librarySections = mutableStateListOf<LibrarySection>()
    val libraryItems = mutableStateListOf<LocalFileItem>()
    val sharedListFriendIds = mutableStateListOf<String>()
    val sharedLists = mutableStateListOf<SharedFileList>()
    val downloads = mutableStateListOf<DownloadItem>()
    val ed2kDownloads = mutableStateListOf<DownloadItem>()
    val uploads = mutableStateListOf<UploadItem>()
    val ed2kUploads = mutableStateListOf<UploadItem>()
    val chatFriends = mutableStateListOf<FriendRosterItem>()
    val chatConversations = mutableStateMapOf<String, ChatConversationState>()
    val advancedConnections = mutableStateListOf<ConnectionItem>()
    val advancedEd2kServers = mutableStateListOf<Ed2kServerRecord>()
    val incomingSearchPhrases = mutableStateListOf<String>()

    private val defaultSearchLayout = settingsService.loadSearchLayoutPreferences()
    private val defaultLibraryLayout = settingsService.loadLibraryLayoutPreferences()
    private val defaultDownloadLayout = settingsService.loadDownloadLayoutPreferences()
    private val defaultUploadLayout = settingsService.loadUploadLayoutPreferences()
    private val defaultConnectionLayout = settingsService.loadConnectionLayoutPreferences()
    private val defaultSearchPaneLayout = settingsService.loadSearchPaneLayoutPreferences()
    private val defaultLibraryPaneLayout = settingsService.loadLibraryPaneLayoutPreferences()
    private val defaultFriendsPaneLayout = settingsService.loadFriendsPaneLayoutPreferences()
    private val defaultTransferPaneLayout = settingsService.loadTransferPaneLayoutPreferences()
    val initialWindowPlacementPreferences = settingsService.loadWindowPlacementPreferences()
    private var windowPlacementPreferences = initialWindowPlacementPreferences
    private var trayBehaviorPreferences = settingsService.loadTrayBehaviorPreferences()
    private val defaultPreferences = settingsService.loadPreferences()
    private val connectionStrengthListener = PropertyChangeListener { event ->
        if (event.propertyName == null || event.propertyName == GnutellaConnectionManager.CONNECTION_STRENGTH) {
            connectionStrengthState = connectionManager.connectionStrength
            if (isFullyConnected()) {
                searchTabs.forEach { it.startedWhileNotFullyConnected = false }
            }
            refreshSearchRunState()
        }
    }
    private val sharedFileCountListener = PropertyChangeListener { event ->
        if (event.propertyName == null || event.propertyName == SharedFileListManager.SHARED_FILE_COUNT) {
            sharedFileCount = sharedFileListManager.sharedFileCount
            invalidateSharingSummaryCache()
        }
    }
    private val fileProcessingListener = EventListener<FileProcessingEvent> { event ->
        runOnUi {
            handleFileProcessingEvent(event)
        }
    }

    var searchQuery by mutableStateOf("")
    var searchCategory by mutableStateOf(defaultPreferences.search.defaultCategory)
    val searchSuggestions = mutableStateListOf<SearchSuggestionEntry>()
    var searchSuggestionsVisible by mutableStateOf(false)
    var selectedSearchSuggestionIndex by mutableIntStateOf(-1)
    val advancedSearchSuggestions = mutableStateListOf<AdvancedSearchSuggestionEntry>()
    var advancedSearchSuggestionsVisible by mutableStateOf(false)
    var selectedAdvancedSearchSuggestionIndex by mutableIntStateOf(-1)
    var focusedAdvancedSearchFieldKey by mutableStateOf<FilePropertyKey?>(null)
    var searchFocusRequestEpoch by mutableIntStateOf(0)
    var currentScreen by mutableStateOf<ComposeScreen>(ComposeScreen.Library)
    var selectedLibrarySectionId by mutableStateOf(DEFAULT_LIBRARY_SECTION_ID)
    var libraryCategoryFilter by mutableStateOf<Category?>(null)
    var libraryFilterText by mutableStateOf("")
    var librarySortMode by mutableStateOf(defaultLibraryLayout.sortMode)
    var librarySortDescending by mutableStateOf(defaultLibraryLayout.sortDescending)
    var selectedLibraryItemPath by mutableStateOf<String?>(null)
    val selectedLibraryItemPaths = mutableStateListOf<String>()
    var librarySelectionAnchorPath by mutableStateOf<String?>(null)
    var visibleLibraryColumns by mutableStateOf(defaultLibraryLayout.visibleColumns)
    var searchRefinementRailVisible by mutableStateOf(defaultSearchPaneLayout.refinementRailVisible)
    var searchRefinementRailFraction by mutableFloatStateOf(defaultSearchPaneLayout.refinementRailFraction)
    var searchResultsPaneFraction by mutableFloatStateOf(defaultSearchPaneLayout.resultsFraction)
    var libraryNavigatorPaneFraction by mutableFloatStateOf(defaultLibraryPaneLayout.navigatorFraction)
    var libraryFiltersVisible by mutableStateOf(defaultLibraryPaneLayout.filtersVisible)
    var friendsRosterPaneFraction by mutableFloatStateOf(defaultFriendsPaneLayout.rosterFraction)
    var transferMainAreaFraction by mutableFloatStateOf(defaultTransferPaneLayout.mainAreaFraction)
    var trayExpanded by mutableStateOf(trayBehaviorPreferences.showTransfersTray)
    var trayMode by mutableStateOf(TransferTrayMode.DOWNLOADS)
    var downloadFilterMode by mutableStateOf(defaultDownloadLayout.filterMode)
    var downloadSortMode by mutableStateOf(defaultDownloadLayout.sortMode)
    var downloadSortDescending by mutableStateOf(defaultDownloadLayout.sortDescending)
    var selectedDownloadUrn by mutableStateOf<String?>(null)
    val selectedDownloadUrns = mutableStateListOf<String>()
    var downloadSelectionAnchorUrn by mutableStateOf<String?>(null)
    var visibleDownloadColumns by mutableStateOf(defaultDownloadLayout.visibleColumns)
    var downloadSearchText by mutableStateOf("")
    var uploadFilterMode by mutableStateOf(defaultUploadLayout.filterMode)
    var uploadSortMode by mutableStateOf(defaultUploadLayout.sortMode)
    var uploadSortDescending by mutableStateOf(defaultUploadLayout.sortDescending)
    var selectedUploadUrn by mutableStateOf<String?>(null)
    val selectedUploadUrns = mutableStateListOf<String>()
    var uploadSelectionAnchorUrn by mutableStateOf<String?>(null)
    var visibleUploadColumns by mutableStateOf(defaultUploadLayout.visibleColumns)
    var advancedToolsWindowOpen by mutableStateOf(false)
    var selectedAdvancedToolsTab by mutableStateOf(AdvancedToolsTab.CONNECTIONS)
    var visibleConnectionColumns by mutableStateOf(defaultConnectionLayout.visibleColumns)
    var connectionSortColumn by mutableStateOf(defaultConnectionLayout.sortColumn)
    var connectionSortDescending by mutableStateOf(defaultConnectionLayout.sortDescending)
    var resolveConnectionHostnames by mutableStateOf(settingsService.resolveConnectionHostnamesEnabled())
    var selectedConnectionKey by mutableStateOf<String?>(null)
    var addConnectionHost by mutableStateOf("")
    var addConnectionPort by mutableStateOf("6346")
    var addConnectionUseTls by mutableStateOf(true)
    var addConnectionError by mutableStateOf<String?>(null)
    var advancedEd2kServerHost by mutableStateOf("")
    var advancedEd2kServerPort by mutableStateOf("4661")
    var advancedKadBootstrapHost by mutableStateOf("")
    var advancedKadBootstrapPort by mutableStateOf("4672")
    var advancedEd2kError by mutableStateOf<String?>(null)
    var advancedToolsDhtName by mutableStateOf("")
    var advancedToolsDhtRunning by mutableStateOf(false)
    var ed2kStatus by mutableStateOf(ed2kService.status)
    var advancedMojitoVisualizerAvailable by mutableStateOf(advancedToolsService.mojitoVisualizerAvailable())
    var advancedMojitoVisualizerTitle by mutableStateOf("")
    var advancedMojitoVisualizerComponent by mutableStateOf<Component?>(null)
    var consoleAvailable by mutableStateOf(advancedToolsService.consoleAvailable())
    private var lastAdvancedToolsErrorMessage: String? = null
    val consoleLoggerNames = mutableStateListOf<String>()
    var selectedConsoleLogger by mutableStateOf("root")
    var selectedConsoleLevel by mutableStateOf("INFO")
    var consoleDelaySeconds by mutableStateOf("0")
    var consoleText by mutableStateOf("")
    var selectedConversationId by mutableStateOf<String?>(null)

    var aboutDialogOpen by mutableStateOf(false)
    var preferencesDialogOpen by mutableStateOf(false)
    var preferencesDialogVersion by mutableIntStateOf(0)
    var requestedPreferencesSection by mutableStateOf(PreferencesSection.SEARCH)
    var openUnsafeSharingEditorOnPreferencesOpen by mutableStateOf(false)
    var preferencesDialogError by mutableStateOf<String?>(null)
    var languageDialogOpen by mutableStateOf(false)
    var languageDialogVersion by mutableIntStateOf(0)
    var advancedSearchDialogOpen by mutableStateOf(false)
    var advancedSearchDraft by mutableStateOf(AdvancedSearchDraft())
    var advancedSearchDialogError by mutableStateOf<String?>(null)
    var openLinkDialogOpen by mutableStateOf(false)
    var openLinkText by mutableStateOf("")
    var openLinkDialogError by mutableStateOf<String?>(null)
    var friendLoginDialogOpen by mutableStateOf(false)
    var friendLoginDialogMode by mutableStateOf(FriendLoginDialogMode.SIGN_IN)
    var friendLoginDraft by mutableStateOf<FriendLoginDraft?>(null)
    var friendLoginBusy by mutableStateOf(false)
    var friendLoginError by mutableStateOf<String?>(null)
    var addFriendDialogOpen by mutableStateOf(false)
    var addFriendId by mutableStateOf("")
    var addFriendNickname by mutableStateOf("")
    var setupWizardOpen by mutableStateOf(application.isNewInstall)
    var setupWizardPage by mutableStateOf(
        if (settingsService.shouldShowSetupAssociationsPage()) SetupWizardPage.ASSOCIATIONS else SetupWizardPage.SHARING
    )
    var setupWizardDraft by mutableStateOf(defaultPreferences)
    var setupWizardError by mutableStateOf<String?>(null)
    var startupSaveDirectoryIssue by mutableStateOf<String?>(null)
    var startupFileAssociationPrompt by mutableStateOf<FileAssociationPromptState?>(null)
    var startupFileAssociationWarnOnChange by mutableStateOf(true)
    var textEntryDialog by mutableStateOf<TextEntryDialogState?>(null)
    var confirmationDialog by mutableStateOf<ConfirmationDialogState?>(null)
    var libraryDeletionChoiceDialog by mutableStateOf<LibraryDeletionChoiceDialogState?>(null)
    var libraryFolderImportDialog by mutableStateOf<LibraryFolderImportDialogState?>(null)
    var libraryKnownTypesDialog by mutableStateOf<LibraryKnownTypesDialogState?>(null)
    var documentSharingWarningDialog by mutableStateOf<DocumentSharingWarningDialogState?>(null)
    var messageDialog by mutableStateOf<MessageDialogState?>(null)
    val runtimeErrorDialogs = mutableStateListOf<ComposeRuntimeErrorReport>()
    var libraryFileInfoDialog by mutableStateOf<LibraryFileInfoDialogState?>(null)
    var searchFileInfoDialog by mutableStateOf<SearchFileInfoDialogState?>(null)
    var downloadFileInfoDialog by mutableStateOf<DownloadFileInfoDialogState?>(null)
    var uploadFileInfoDialog by mutableStateOf<UploadFileInfoDialogState?>(null)
    var browseFailureDialog by mutableStateOf<BrowseFailureDialogState?>(null)
    var localeEpoch by mutableIntStateOf(0)
    var restoreEpoch by mutableIntStateOf(0)
    var recentDownloadsEpoch by mutableIntStateOf(0)

    var downloadsEpoch by mutableIntStateOf(0)
    var uploadsEpoch by mutableIntStateOf(0)
    var chatFriendsEpoch by mutableIntStateOf(0)
    var onlineChatFriendCount by mutableIntStateOf(0)
    var unreadChatFriendCount by mutableIntStateOf(0)
    var playerState by mutableStateOf(playerService.status())
    var playerTrackName by mutableStateOf(playerService.trackName())
    var playerProgress by mutableFloatStateOf(0f)
    var playerVisible by mutableStateOf(playerService.isVisible())
    var playerVolume by mutableFloatStateOf(settingsService.playerVolume())
    var playerEnabled by mutableStateOf(settingsService.playerEnabled())
    var playerShuffle by mutableStateOf(playerService.isShuffle())
    var playerCurrentFile by mutableStateOf(playerService.currentFile())
    var appearance by mutableStateOf(defaultPreferences.system.appearance)
    var notificationsEnabled by mutableStateOf(defaultPreferences.friends.showNotifications)
    var playNotificationSoundEnabled by mutableStateOf(defaultPreferences.friends.playNotificationSound)
    var searchValidationError by mutableStateOf<String?>(null)
    var keepSearchHistoryEnabled by mutableStateOf(defaultPreferences.search.keepSearchHistory)
    var showSmartSuggestionsEnabled by mutableStateOf(defaultPreferences.search.showSmartSuggestions)
    var groupSimilarResultsEnabled by mutableStateOf(defaultPreferences.search.groupSimilarResults)
    var allowProgramSearchAndShareEnabled by mutableStateOf(defaultPreferences.library.allowProgramSearchAndShare)
    var minimizeToTray by mutableStateOf(trayBehaviorPreferences.minimizeToTray)
    var closeTrayWhenNoTransfers by mutableStateOf(defaultPreferences.transfers.closeTrayWhenNoTransfers)
    var showTotalBandwidth by mutableStateOf(defaultPreferences.transfers.showTotalBandwidth)
    var clearDownloadsWhenFinished by mutableStateOf(defaultPreferences.transfers.clearDownloadsWhenFinished)
    var clearUploadsWhenFinished by mutableStateOf(defaultPreferences.transfers.clearUploadsWhenFinished)
    var showUploadsInTray by mutableStateOf(settingsService.showUploadsInTray())
    var torrentEngineHealthState by mutableStateOf(settingsService.torrentEngineHealthState())
    var connectionStrengthState by mutableStateOf(connectionManager.connectionStrength)
    var sharedFileCount by mutableIntStateOf(sharedFileListManager.sharedFileCount)
    var publicSharedListCount by mutableIntStateOf(libraryData.peekPublicSharedListCount())
    var fileProcessingStatus by mutableStateOf<FileProcessingStatus?>(null)
    var showLibraryOverlayMessage by mutableStateOf(settingsService.showLibraryOverlayMessageEnabled())
    var showSharingOverlayMessage by mutableStateOf(settingsService.showSharingOverlayMessageEnabled())
    val playerQueue = mutableStateListOf<PlayerQueueEntry>()
    var playerQueueSourceLabel by mutableStateOf(tr("Current Session"))
    var playerQueueIndex by mutableIntStateOf(-1)
    var friendConnectionState by mutableStateOf<FriendConnectionEvent.Type?>(friendService.lastConnectionEvent()?.type)
    var friendDoNotDisturb by mutableStateOf(friendService.isDoNotDisturbEnabled())
    var delayedExitState by mutableStateOf(delayedExitService.state())
    var friendCollectionShareEpoch by mutableIntStateOf(0)
    var sharedListSharingEditMode by mutableStateOf(false)
    var sharedListSharingFilter by mutableStateOf("")
    val sharedListSharingSelectedIds = mutableStateListOf<String>()
    val pendingFriendRequests = mutableStateListOf<PendingFriendRequest>()
    val operationNotices = mutableStateListOf<OperationNotice>()
    private var nextOperationNoticeId = 1L
    private var nextLibraryFileInfoDialogVersion = 1
    private val runtimeErrorListener = object : ComposeRuntimeErrorService.Listener {
        override fun onRuntimeError(report: ComposeRuntimeErrorReport) {
            runOnUi {
                if (runtimeErrorDialogs.any { it.id == report.id }) {
                    return@runOnUi
                }
                if (runtimeErrorDialogs.size >= 3) {
                    return@runOnUi
                }
                runtimeErrorDialogs += report
            }
        }
    }

    private val systemMessageListener = object : ComposeSystemMessageService.Listener {
        override fun onSystemMessage(message: ComposeSystemMessage) {
            runOnUi {
                handleSystemMessage(message)
            }
        }
    }

    fun activate() {
        if (!activated.compareAndSet(false, true)) {
            return
        }

        trayService.activate(trayListener)
        closeables += AutoCloseable { trayService.deactivate(trayListener) }
        runtimeErrorService.addListener(runtimeErrorListener)
        closeables += AutoCloseable { runtimeErrorService.removeListener(runtimeErrorListener) }
        prepareStartupMessageRouting()
        closeables += AutoCloseable { releaseStartupMessageRouting() }
        delayedExitService.activate(delayedExitListener)
        closeables += AutoCloseable { delayedExitService.deactivate(delayedExitListener) }
        connectionManager.addPropertyChangeListener(connectionStrengthListener)
        closeables += AutoCloseable { connectionManager.removePropertyChangeListener(connectionStrengthListener) }
        sharedFileListManager.addPropertyChangeListener(sharedFileCountListener)
        closeables += AutoCloseable { sharedFileListManager.removePropertyChangeListener(sharedFileCountListener) }
        libraryManager.libraryManagedList.addFileProcessingListener(fileProcessingListener)
        closeables += AutoCloseable { libraryManager.libraryManagedList.removeFileProcessingListener(fileProcessingListener) }
        ed2kService.addListener(ed2kListener)
        closeables += AutoCloseable { ed2kService.removeListener(ed2kListener) }
        closeables += AutoCloseable { clearPublicDocumentWarningBindings() }
        bindEventLists()
        syncEd2kDownloads()
        syncEd2kUploads()
        syncEd2kStatus()
        primeDownloadCompletionState()
        bindPlayer()
        bindFriends()
        rebuildLibrarySections()
        selectLibrarySection(selectedLibrarySectionId)
        syncDesktopShellState()
        beginStartupWorkflows()
    }

    fun prepareStartupMessageRouting() {
        if (systemMessageListenerAttached) {
            return
        }
        systemMessageService.addListener(systemMessageListener)
        systemMessageListenerAttached = true
    }

    private fun releaseStartupMessageRouting() {
        if (!systemMessageListenerAttached) {
            return
        }
        systemMessageService.removeListener(systemMessageListener)
        systemMessageListenerAttached = false
    }

    private fun bindEventLists() {
        closeables += EventListBinding(
            sharedFileListManager.model,
            sharedLists,
            onChanged = {
                sharedFileCount = sharedFileListManager.sharedFileCount
                publicSharedListCount = libraryData.peekPublicSharedListCount()
                invalidateSharingSummaryCache()
                invalidateSearchResultPresentationCache()
                rebuildLibrarySections()
                applyPendingCollectionShares()
                syncPublicDocumentWarningBindings()
            }
        )

        closeables += EventListBinding(
            downloadListManager.swingThreadSafeDownloads,
            downloads,
            onChanged = {
                invalidateDownloadPresentationCache()
                downloadsEpoch += 1
                recentDownloadsEpoch += 1
                handleDownloadLifecycleChanges()
                maybeAutoHideTray()
            },
            coalesceOnChanged = true,
            addPropertyListener = { item, listener -> item.addPropertyChangeListener(listener) },
            removePropertyListener = { item, listener -> item.removePropertyChangeListener(listener) }
        )

        closeables += EventListBinding(
            uploadListManager.swingThreadSafeUploads,
            uploads,
            onChanged = {
                invalidateUploadPresentationCache()
                uploadsEpoch += 1
                maybeAutoHideTray()
            },
            coalesceOnChanged = true,
            addPropertyListener = { item, listener -> item.addPropertyChangeListener(listener) },
            removePropertyListener = { item, listener -> item.removePropertyChangeListener(listener) }
        )
    }

    private fun syncEd2kDownloads() {
        reconcileKeyedStateList(ed2kDownloads, ed2kService.downloads, ::downloadSelectionKey)
        syncEd2kStatus()
        invalidateDownloadPresentationCache()
        downloadsEpoch += 1
        recentDownloadsEpoch += 1
        handleDownloadLifecycleChanges()
        maybeAutoHideTray()
    }

    private fun syncEd2kUploads() {
        reconcileKeyedStateList(ed2kUploads, ed2kService.uploads, ::uploadSelectionKey)
        syncEd2kStatus()
        invalidateUploadPresentationCache()
        uploadsEpoch += 1
        maybeAutoHideTray()
    }

    private fun syncEd2kStatus() {
        ed2kStatus = ed2kService.status
        if (advancedToolsWindowOpen) {
            refreshAdvancedEd2kServersSnapshot()
        }
    }

    private fun syncEd2kSearchSession(sessionId: String) {
        val session = ed2kService.getSearchSession(sessionId)
        val matchingTabs = searchTabs.filter { it.ed2kSessionId == sessionId }
        if (matchingTabs.isEmpty()) {
            return
        }
        matchingTabs.forEach { tab ->
            reconcileKeyedStateList(
                tab.ed2kResults,
                session?.results.orEmpty(),
                ::searchResultKeyOf
            )
            tab.ed2kSearchRunning = session?.isRunning == true
            refreshCombinedSearchRunning(tab)
            syncMergedSearchResults(tab)
        }
    }

    private fun syncMergedSearchResults(tab: SearchTabSession) {
        val merged = LinkedHashMap<String, GroupedSearchResult>()
        tab.networkResults.forEach { result ->
            merged[searchResultKeyOf(result)] = result
        }
        tab.ed2kResults.forEach { result ->
            merged[searchResultKeyOf(result)] = result
        }
        reconcileKeyedStateList(tab.results, merged.values.toList(), ::searchResultKeyOf)
        scheduleSearchTabPresentationRefresh(tab, SearchPresentationDirty.RESULTS.mask)
    }

    private fun refreshCombinedSearchRunning(tab: SearchTabSession) {
        tab.searchRunning = tab.networkSearchRunning || tab.ed2kSearchRunning
    }

    private fun continueEd2kSearch(tab: SearchTabSession, clearExisting: Boolean) {
        if (tab.searchType !in setOf(SearchDetails.SearchType.KEYWORD, SearchDetails.SearchType.WHATS_NEW)) {
            return
        }
        runCatching {
            val existingSessionId = tab.ed2kSessionId
            val session = if (existingSessionId == null) {
                ed2kService.startSearch(tab.query)
            } else {
                ed2kService.repeatSearch(existingSessionId, clearExisting) ?: ed2kService.startSearch(tab.query)
            }
            tab.ed2kSessionId = session.id
            tab.ed2kSearchRunning = session.isRunning
            reconcileKeyedStateList(tab.ed2kResults, session.results, ::searchResultKeyOf)
            syncMergedSearchResults(tab)
        }.onFailure {
            tab.ed2kSearchRunning = false
            refreshCombinedSearchRunning(tab)
        }
    }

    private fun bindPlayer() {
        playerService.activate(playerListener)
        closeables += AutoCloseable { playerService.deactivate(playerListener) }
        refreshPlayerState()
    }

    private fun bindFriends() {
        friendService.addListener(friendListener)
        closeables += AutoCloseable { friendService.removeListener(friendListener) }
        friendService.activate()
        closeables += AutoCloseable { friendService.deactivate() }
        friendConnectionState = friendService.lastConnectionEvent()?.type
        val roster = friendService.roster()
        reconcileKeyedStateList(chatFriends, roster, FriendRosterItem::id)
        onlineChatFriendCount = roster.count { it.signedIn }
        unreadChatFriendCount = roster.count { it.hasUnviewedMessages() }
    }

    fun bindWindow(window: Window) {
        if (windowRef !== window) {
            windowRef = window
            windowListenersAttached = false
            windowPlacementApplied = false
        }
        if (!windowListenersAttached) {
            window.addComponentListener(object : ComponentAdapter() {
                override fun componentMoved(e: ComponentEvent?) {
                    persistWindowPlacement()
                }

                override fun componentResized(e: ComponentEvent?) {
                    persistWindowPlacement()
                }

                override fun componentShown(e: ComponentEvent?) {
                    syncDesktopShellState()
                }

                override fun componentHidden(e: ComponentEvent?) {
                    syncDesktopShellState()
                }
            })
            if (window is Frame) {
                window.addWindowStateListener(WindowStateListener { event ->
                    if (
                        minimizeToTray &&
                        trayService.supportsTray() &&
                        event.newState and Frame.ICONIFIED == Frame.ICONIFIED
                    ) {
                        hideApplicationToTray()
                    } else {
                        persistWindowPlacement()
                        syncDesktopShellState()
                    }
                })
            }
            windowListenersAttached = true
        }
        if (!windowPlacementApplied) {
            applySavedWindowPlacement(window)
            windowPlacementApplied = true
        }
        syncDesktopShellState()
    }

    fun markUiReady() {
        if (startupRuntimeErrorsFlushed) {
            return
        }
        startupRuntimeErrorsFlushed = true
        runtimeErrorService.finishStartupCapture(replay = true)
    }

    fun bindExitHandler(exitHandler: () -> Unit) {
        this.exitHandler = exitHandler
    }

    fun showAbout() {
        aboutDialogOpen = true
    }

    fun showPreferences(
        section: PreferencesSection = PreferencesSection.SEARCH,
        openUnsafeSharingEditor: Boolean = false
    ) {
        if (section == PreferencesSection.TRANSFERS) {
            refreshTorrentEngineHealthState()
        }
        requestedPreferencesSection = section
        openUnsafeSharingEditorOnPreferencesOpen = openUnsafeSharingEditor
        preferencesDialogError = null
        preferencesDialogVersion += 1
        preferencesDialogOpen = true
    }

    fun showTransferPreferences() {
        showPreferences(PreferencesSection.TRANSFERS)
    }

    fun showLanguageDialog() {
        languageDialogVersion += 1
        languageDialogOpen = true
    }

    fun showAdvancedSearchDialog(
        category: SearchCategory = searchCategory.takeIf { it in advancedSearchCategories() } ?: SearchCategory.AUDIO
    ) {
        advancedSearchDraft = AdvancedSearchDraft(category = category)
        advancedSearchDialogError = null
        dismissAdvancedSearchSuggestions()
        advancedSearchDialogOpen = true
    }

    fun showOpenLinkDialog() {
        openLinkText = ""
        openLinkDialogError = null
        openLinkDialogOpen = true
    }

    fun showAdvancedToolsWindow() {
        advancedToolsWindowOpen = true
        runAdvancedToolsAction {
            refreshAdvancedToolsSummaryState()
        }
        refreshActiveAdvancedToolsTab()
        runAdvancedToolsAction {
            attachAdvancedConsole()
            syncAdvancedMojitoVisualizer()
        }
    }

    fun closeAdvancedToolsWindow() {
        advancedToolsWindowOpen = false
        detachAdvancedConsole()
        detachAdvancedMojitoVisualizer()
    }

    fun refreshAdvancedTools() {
        if (!advancedToolsWindowOpen) {
            return
        }
        runAdvancedToolsAction {
            ComposePerformanceTracker.measure("advancedTools.refreshAll") {
                refreshAdvancedToolsSummaryState()
                refreshAdvancedToolsConnectionsSnapshot()
                refreshConsoleLoggerNames()
                flushDelayedConsoleOutput(force = false)
                syncAdvancedMojitoVisualizer()
            }
        }
    }

    fun refreshActiveAdvancedToolsTab() {
        if (!advancedToolsWindowOpen) {
            return
        }
        runAdvancedToolsAction {
            when (selectedAdvancedToolsTab) {
                AdvancedToolsTab.CONNECTIONS -> ComposePerformanceTracker.measure("advancedTools.refreshConnections") {
                    refreshAdvancedToolsSummaryState()
                    refreshAdvancedToolsConnectionsSnapshot()
                }
                AdvancedToolsTab.ED2K -> ComposePerformanceTracker.measure("advancedTools.refreshEd2k") {
                    refreshAdvancedToolsSummaryState()
                    refreshAdvancedEd2kServersSnapshot()
                }
                AdvancedToolsTab.CONSOLE -> ComposePerformanceTracker.measure("advancedTools.refreshConsole") {
                    refreshAdvancedToolsSummaryState()
                    refreshConsoleLoggerNames()
                    flushDelayedConsoleOutput(force = false)
                }
                AdvancedToolsTab.MOJITO -> ComposePerformanceTracker.measure("advancedTools.refreshMojito") {
                    refreshAdvancedToolsSummaryState()
                    syncAdvancedMojitoVisualizer()
                }
            }
        }
    }

    fun advancedToolsAutoRefreshIntervalMillis(): Long? {
        if (!advancedToolsWindowOpen) {
            return null
        }
        return when (selectedAdvancedToolsTab) {
            AdvancedToolsTab.CONNECTIONS -> 1500L
            AdvancedToolsTab.ED2K -> 2000L
            AdvancedToolsTab.MOJITO -> 2000L
            AdvancedToolsTab.CONSOLE -> null
        }
    }

    private fun refreshAdvancedToolsSummaryState() {
        if (!advancedToolsWindowOpen) {
            return
        }
        advancedToolsDhtName = advancedToolsService.dhtName()
        advancedToolsDhtRunning = advancedToolsService.dhtRunning()
        ed2kStatus = advancedToolsService.ed2kStatus()
        advancedMojitoVisualizerAvailable = advancedToolsService.mojitoVisualizerAvailable()
        consoleAvailable = advancedToolsService.consoleAvailable()
    }

    private fun refreshAdvancedToolsConnectionsSnapshot() {
        resolveConnectionHostnames = settingsService.resolveConnectionHostnamesEnabled()
        val previousSelection = selectedConnectionKey
        advancedConnections.clear()
        advancedConnections.addAll(advancedToolsService.connectionListSnapshot(resolveConnectionHostnames))
        if (previousSelection != null && advancedConnections.none { connectionKey(it) == previousSelection }) {
            selectedConnectionKey = null
        }
        incomingSearchPhrases.clear()
        incomingSearchPhrases.addAll(advancedToolsService.incomingSearchListSnapshot())
    }

    private fun refreshAdvancedEd2kServersSnapshot() {
        advancedEd2kServers.clear()
        advancedEd2kServers.addAll(advancedToolsService.ed2kServers())
    }

    private inline fun runAdvancedToolsAction(block: () -> Unit) {
        runCatching(block)
            .onSuccess {
                lastAdvancedToolsErrorMessage = null
            }
            .onFailure { failure ->
                val message = failure.message ?: tr("Advanced Tools could not be refreshed.")
                if (message != lastAdvancedToolsErrorMessage) {
                    showNotice(tr("Advanced Tools"), message, OperationNoticeLevel.ERROR)
                    lastAdvancedToolsErrorMessage = message
                }
            }
    }

    fun selectAdvancedToolsTab(tab: AdvancedToolsTab) {
        selectedAdvancedToolsTab = tab
        refreshActiveAdvancedToolsTab()
        syncAdvancedMojitoVisualizer()
    }

    fun refreshConsoleLoggerNames() {
        if (!consoleAvailable) {
            consoleLoggerNames.clear()
            selectedConsoleLogger = "root"
            selectedConsoleLevel = "INFO"
            return
        }
        val names = advancedToolsService.loggerNames().ifEmpty { listOf("root") }
        consoleLoggerNames.clear()
        consoleLoggerNames.addAll(names)
        if (selectedConsoleLogger !in consoleLoggerNames) {
            selectedConsoleLogger = consoleLoggerNames.first()
        }
        selectedConsoleLevel = advancedToolsService.loggerLevel(selectedConsoleLogger)
    }

    fun applyAdvancedConsoleSettings() {
        if (!consoleAvailable) {
            return
        }
        appliedConsoleDelaySeconds = consoleDelaySeconds.trim().toIntOrNull()?.coerceAtLeast(0) ?: 0
        val normalizedLevel = selectedConsoleLevel.takeIf { it in ADVANCED_CONSOLE_LEVELS } ?: "INFO"
        advancedToolsService.applyLoggerLevel(selectedConsoleLogger, normalizedLevel)
        flushDelayedConsoleOutput(force = true)
        selectedConsoleLevel = advancedToolsService.loggerLevel(selectedConsoleLogger)
    }

    fun clearAdvancedConsole() {
        synchronized(advancedConsoleDelayBuffer) {
            advancedConsoleDelayBuffer.setLength(0)
        }
        consoleText = ""
    }

    fun selectConsoleLogger(loggerName: String) {
        selectedConsoleLogger = loggerName
        if (consoleAvailable) {
            selectedConsoleLevel = advancedToolsService.loggerLevel(loggerName)
        }
    }

    fun saveAdvancedConsole() {
        if (!consoleAvailable) {
            return
        }
        val target = filePicker.chooseSaveFile(
            parent = windowRef,
            title = tr("Save Diagnostic Log"),
            suggestedName = "wireshare-log.txt"
        ) ?: return
        val output = if (target.extension.equals("txt", ignoreCase = true)) {
            target
        } else {
            File(target.parentFile, "${target.name}.txt")
        }
        runCatching {
            flushDelayedConsoleOutput(force = true)
            advancedToolsService.saveDiagnostics(output, consoleText)
        }.onSuccess {
            showNotice(
                tr("Console"),
                tr("Saved diagnostic log to {0}.", output.name),
                OperationNoticeLevel.SUCCESS
            )
        }.onFailure { failure ->
            showNotice(
                tr("Console"),
                failure.message ?: tr("WireShare could not save the diagnostic log."),
                OperationNoticeLevel.ERROR
            )
        }
    }

    fun selectConnectionRow(item: ConnectionItem) {
        selectedConnectionKey = connectionKey(item)
    }

    fun selectedConnectionItem(): ConnectionItem? {
        val key = selectedConnectionKey ?: return null
        return advancedConnections.firstOrNull { connectionKey(it) == key }
    }

    fun sortedAdvancedConnections(): List<ConnectionItem> {
        val comparator = Comparator<ConnectionItem> { first, second ->
            compareConnectionItems(first, second, connectionSortColumn)
        }
        val sorted = advancedConnections.sortedWith(comparator)
        return if (connectionSortDescending) sorted.reversed() else sorted
    }

    fun setConnectionSort(column: ConnectionColumn) {
        if (connectionSortColumn == column) {
            connectionSortDescending = !connectionSortDescending
        } else {
            connectionSortColumn = column
            connectionSortDescending = defaultConnectionSortDescending(column)
        }
        saveConnectionLayoutPreferences()
    }

    fun toggleConnectionColumn(column: ConnectionColumn) {
        val updated = toggledColumns(visibleConnectionColumns, column)
            .takeIf { it.isNotEmpty() }
            ?: setOf(ConnectionColumn.HOST)
        visibleConnectionColumns = updated
        saveConnectionLayoutPreferences()
    }

    fun updateResolveConnectionHostnamesPreference(enabled: Boolean) {
        resolveConnectionHostnames = enabled
        settingsService.setResolveConnectionHostnamesEnabled(enabled)
        refreshAdvancedTools()
    }

    fun submitAdvancedConnection() {
        val host = addConnectionHost.trim()
        if (host.isBlank()) {
            addConnectionError = tr("Enter a host name or IP address.")
            return
        }
        val port = addConnectionPort.trim().toIntOrNull()
        if (port == null || port !in 1..65535) {
            addConnectionError = tr("Enter a valid port between 1 and 65535.")
            return
        }
        addConnectionError = null
        runCatching {
            advancedToolsService.addConnection(host, port, addConnectionUseTls)
        }.onSuccess {
            showNotice(
                tr("Connections"),
                tr("Trying {0}:{1}.", host, port),
                OperationNoticeLevel.INFO
            )
            addConnectionHost = ""
            addConnectionPort = "6346"
            addConnectionUseTls = true
            refreshAdvancedTools()
        }.onFailure { failure ->
            addConnectionError = failure.message ?: tr("WireShare could not add that connection.")
        }
    }

    fun removeSelectedConnection() {
        val item = selectedConnectionItem() ?: return
        advancedToolsService.removeConnection(item)
        refreshAdvancedTools()
    }

    fun canBrowseSelectedConnection(): Boolean {
        return selectedConnectionItem()?.isConnected == true && selectedConnectionItem()?.friendPresence != null
    }

    fun browseSelectedConnection() {
        val item = selectedConnectionItem() ?: return
        browseConnection(item)
    }

    fun browseConnection(item: ConnectionItem) {
        val presence = item.friendPresence ?: return
        browseSourceTarget(
            BrowseSourceTarget(
                id = presence.presenceId,
                label = presence.friend.renderName,
                enabled = item.isConnected,
                anonymous = presence.friend.isAnonymous,
                presence = presence
            )
        )
    }

    fun submitIncomingSearch(query: String) {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) {
            return
        }
        if (!ensureProgramSearchAllowed(searchCategory)) {
            return
        }
        if (keepSearchHistoryEnabled) {
            settingsService.recordSearchHistoryEntry(trimmed)
        }
        searchCategory = searchCategory.takeIf { it != SearchCategory.OTHER } ?: SearchCategory.ALL
        startSearch(
            title = trimmed,
            query = trimmed,
            category = searchCategory,
            searchType = SearchDetails.SearchType.KEYWORD,
            advancedDetails = emptyMap()
        )
        currentScreen = ComposeScreen.Search(searchTabs.last().id)
    }

    fun advancedSearchCategories(): List<SearchCategory> = listOf(
        SearchCategory.AUDIO,
        SearchCategory.VIDEO
    )

    fun whatsNewCategories(): List<SearchCategory> = listOf(
        SearchCategory.ALL,
        SearchCategory.AUDIO,
        SearchCategory.DOCUMENT,
        SearchCategory.IMAGE,
        SearchCategory.PROGRAM,
        SearchCategory.VIDEO,
        SearchCategory.TORRENT
    )

    fun advancedSearchFields(category: SearchCategory): List<AdvancedSearchFieldSpec> {
        return when (category) {
            SearchCategory.AUDIO -> listOf(
                AdvancedSearchFieldSpec(FilePropertyKey.TITLE, tr("Title")),
                AdvancedSearchFieldSpec(FilePropertyKey.AUTHOR, tr("Artist")),
                AdvancedSearchFieldSpec(FilePropertyKey.ALBUM, tr("Album")),
                AdvancedSearchFieldSpec(FilePropertyKey.GENRE, tr("Genre")),
                AdvancedSearchFieldSpec(FilePropertyKey.TRACK_NUMBER, tr("Track Number")),
                AdvancedSearchFieldSpec(FilePropertyKey.YEAR, tr("Year")),
                AdvancedSearchFieldSpec(FilePropertyKey.BITRATE, tr("Bitrate"))
            )

            SearchCategory.VIDEO -> listOf(
                AdvancedSearchFieldSpec(FilePropertyKey.TITLE, tr("Title")),
                AdvancedSearchFieldSpec(FilePropertyKey.GENRE, tr("Genre")),
                AdvancedSearchFieldSpec(FilePropertyKey.YEAR, tr("Year")),
                AdvancedSearchFieldSpec(FilePropertyKey.RATING, tr("Rating"))
            )

            else -> emptyList()
        }
    }

    fun updateAdvancedSearchCategory(category: SearchCategory) {
        advancedSearchDraft = AdvancedSearchDraft(category = category)
        advancedSearchDialogError = null
        dismissAdvancedSearchSuggestions()
    }

    fun updateAdvancedSearchField(key: FilePropertyKey, value: String) {
        advancedSearchDialogError = null
        advancedSearchDraft = advancedSearchDraft.copy(
            values = advancedSearchDraft.values.toMutableMap().apply {
                if (value.isBlank()) {
                    remove(key)
                } else {
                    put(key, value)
                }
            }
        )
        if (focusedAdvancedSearchFieldKey == key) {
            refreshAdvancedSearchSuggestions()
        }
    }

    fun focusAdvancedSearchField(key: FilePropertyKey) {
        focusedAdvancedSearchFieldKey = key
        selectedAdvancedSearchSuggestionIndex = -1
        refreshAdvancedSearchSuggestions()
    }

    fun blurAdvancedSearchField(key: FilePropertyKey) {
        if (focusedAdvancedSearchFieldKey == key) {
            dismissAdvancedSearchSuggestions()
        }
    }

    fun hasVisibleAdvancedSearchSuggestions(key: FilePropertyKey): Boolean {
        return focusedAdvancedSearchFieldKey == key &&
            advancedSearchSuggestionsVisible &&
            advancedSearchSuggestions.isNotEmpty()
    }

    fun moveAdvancedSearchSuggestionSelection(delta: Int) {
        if (advancedSearchSuggestions.isEmpty()) {
            selectedAdvancedSearchSuggestionIndex = -1
            return
        }
        val nextIndex = when {
            selectedAdvancedSearchSuggestionIndex < 0 && delta > 0 -> 0
            selectedAdvancedSearchSuggestionIndex < 0 && delta < 0 -> advancedSearchSuggestions.lastIndex
            else -> (selectedAdvancedSearchSuggestionIndex + delta).coerceIn(0, advancedSearchSuggestions.lastIndex)
        }
        selectedAdvancedSearchSuggestionIndex = nextIndex
    }

    fun selectAdvancedSearchSuggestionIndex(index: Int) {
        if (index in advancedSearchSuggestions.indices) {
            selectedAdvancedSearchSuggestionIndex = index
        }
    }

    fun acceptSelectedAdvancedSearchSuggestion(): Boolean {
        val key = focusedAdvancedSearchFieldKey ?: return false
        val index = selectedAdvancedSearchSuggestionIndex
        if (index !in advancedSearchSuggestions.indices) {
            return false
        }
        acceptAdvancedSearchSuggestion(key, advancedSearchSuggestions[index])
        return true
    }

    fun acceptAdvancedSearchSuggestion(key: FilePropertyKey, entry: AdvancedSearchSuggestionEntry) {
        updateAdvancedSearchField(key, entry.value)
        dismissAdvancedSearchSuggestions()
    }

    fun dismissAdvancedSearchSuggestions() {
        advancedSearchSuggestionsVisible = false
        selectedAdvancedSearchSuggestionIndex = -1
        focusedAdvancedSearchFieldKey = null
        advancedSearchSuggestions.clear()
    }

    private fun refreshAdvancedSearchSuggestions() {
        val focusedKey = focusedAdvancedSearchFieldKey
        if (!advancedSearchDialogOpen || focusedKey == null) {
            advancedSearchSuggestionsVisible = false
            selectedAdvancedSearchSuggestionIndex = -1
            advancedSearchSuggestions.clear()
            return
        }

        val suggestions = advancedSearchSuggestionsService.suggestions(
            category = advancedSearchDraft.category,
            key = focusedKey,
            input = advancedSearchDraft.values[focusedKey].orEmpty()
        )
        advancedSearchSuggestions.clear()
        advancedSearchSuggestions.addAll(suggestions)
        if (advancedSearchSuggestions.isEmpty()) {
            advancedSearchSuggestionsVisible = false
            selectedAdvancedSearchSuggestionIndex = -1
        } else {
            advancedSearchSuggestionsVisible = true
            if (selectedAdvancedSearchSuggestionIndex !in advancedSearchSuggestions.indices) {
                selectedAdvancedSearchSuggestionIndex = -1
            }
        }
    }

    fun showFriendLoginDialog(mode: FriendLoginDialogMode = FriendLoginDialogMode.SIGN_IN) {
        friendLoginDraft = friendService.preferredLoginDraft()
        if (friendLoginDraft == null) {
            showNotice(
                tr("Friends"),
                tr("No friend services are configured."),
                OperationNoticeLevel.WARNING
            )
            return
        }
        friendLoginDialogMode = mode
        friendLoginBusy = false
        friendLoginError = null
        friendLoginDialogOpen = true
    }

    fun showFriendLoginSettingsDialog() {
        showFriendLoginDialog(FriendLoginDialogMode.SAVE_SETTINGS)
    }

    fun friendLoginOptions(): List<FriendLoginOption> = friendService.loginOptions()

    fun updateFriendLoginConfig(label: String) {
        friendLoginDraft = friendService.loginDraftFor(label)
    }

    fun submitFriendLogin(draft: FriendLoginDraft) {
        if (draft.username.trim().isEmpty() || draft.password.isEmpty()) {
            friendLoginError = tr("Enter a user name and password to continue.")
            return
        }

        if (draft.configLabel == "Jabber" && draft.serviceName.trim().isEmpty()) {
            friendLoginError = tr("Enter a domain to continue.")
            return
        }

        friendLoginBusy = true
        friendLoginError = null
        friendService.submitLogin(draft)
    }

    fun saveFriendLoginSettings(draft: FriendLoginDraft) {
        if (draft.username.trim().isEmpty() || draft.password.isEmpty()) {
            friendLoginError = tr("Enter a user name and password to continue.")
            return
        }

        if (draft.configLabel == "Jabber" && draft.serviceName.trim().isEmpty()) {
            friendLoginError = tr("Enter a domain to continue.")
            return
        }

        friendLoginError = null
        friendService.saveLoginConfiguration(draft)
        friendLoginDialogOpen = false
        requestedPreferencesSection = PreferencesSection.FRIENDS
        preferencesDialogVersion += 1
        syncPreferenceStateAfterApply(settingsService.loadPreferences())
        showNotice(
            tr("Friends"),
            tr("Saved sign-in settings for future sessions."),
            OperationNoticeLevel.SUCCESS
        )
    }

    fun logoutFriends() {
        friendService.logout()
    }

    fun openAddFriendDialog() {
        addFriendId = ""
        addFriendNickname = ""
        addFriendDialogOpen = true
    }

    fun submitAddFriend() {
        val username = addFriendId.trim()
        if (username.isEmpty()) {
            return
        }
        friendService.addFriend(username, addFriendNickname)
        addFriendDialogOpen = false
    }

    fun removeFriend(friend: FriendRosterItem) {
        confirmationDialog = ConfirmationDialogState(
            title = tr("Remove Friend"),
            message = tr("Remove {0} from your Friends list?", friend.displayName),
            confirmLabel = tr("Remove"),
            onConfirm = {
                confirmationDialog = null
                friendService.removeFriend(friend.id)
            },
            onDismiss = { confirmationDialog = null }
        )
    }

    fun restoreApplication() {
        val activeWindow = windowRef
        if (activeWindow == null || !activeWindow.isDisplayable) {
            restoreEpoch += 1
            return
        }
        if (activeWindow is Frame) {
            activeWindow.state = Frame.NORMAL
        }
        activeWindow.isVisible = true
        activeWindow.toFront()
        activeWindow.requestFocus()
        syncDesktopShellState()
        restoreEpoch += 1
    }

    fun handleWindowCloseRequest() {
        if (minimizeToTray && trayService.supportsTray()) {
            hideApplicationToTray()
        } else {
            requestExit()
        }
    }

    fun selectLibrary() {
        currentScreen = ComposeScreen.Library
    }

    fun selectLibrarySection(sectionId: String) {
        rememberLibrarySectionState()
        selectedLibrarySectionId = sectionId
        cancelCurrentSharedListSharingEdit()
        restoreLibrarySectionState(sectionId)
        invalidateLibraryVisibilityCache()
        bindLibraryItems(activeLibrarySection()?.list ?: libraryManager.libraryManagedList)
        bindSharedListFriendIds(currentSharedList())
        currentScreen = ComposeScreen.Library
        syncLiveLibraryQueueIfNeeded()
    }

    fun openTransfersWorkspace(mode: TransferTrayMode = trayMode) {
        refreshTorrentEngineHealthState()
        trayMode = mode
        currentScreen = ComposeScreen.Transfers
    }

    fun showTransferTray(mode: TransferTrayMode = trayMode) {
        trayMode = mode
        setTransferTrayExpanded(true)
    }

    fun noteTransferActivity(mode: TransferTrayMode = trayMode) {
        trayMode = mode
    }

    fun selectTransfers(mode: TransferTrayMode = trayMode) {
        openTransfersWorkspace(mode)
    }

    fun selectFriends() {
        currentScreen = ComposeScreen.Friends
        if (selectedConversationId == null) {
            chatFriends.firstOrNull()?.let { selectConversation(it) }
        }
    }

    fun openFriendsQuickEntry() {
        pendingFriendRequests.firstOrNull()?.let {
            currentScreen = ComposeScreen.Friends
            return
        }
        chatFriends.firstOrNull { it.hasUnviewedMessages() }?.let {
            selectConversation(it)
            return
        }
        selectFriends()
    }

    fun selectPlayer() {
        currentScreen = ComposeScreen.Player
    }

    fun toggleTray() {
        setTransferTrayExpanded(!trayExpanded)
    }

    private fun setTransferTrayExpanded(expanded: Boolean) {
        trayExpanded = expanded
        trayBehaviorPreferences = trayBehaviorPreferences.copy(showTransfersTray = trayExpanded)
        settingsService.saveTrayBehaviorPreferences(trayBehaviorPreferences)
    }

    fun currentFriendConnection(): FriendConnection? = friendService.currentConnection()

    fun activeFriendRequest(): PendingFriendRequest? = pendingFriendRequests.firstOrNull()

    fun acceptFriendRequest(request: PendingFriendRequest) {
        pendingFriendRequests.removeAll { it.id == request.id }
        friendService.acceptFriendRequest(request.id)
    }

    fun declineFriendRequest(request: PendingFriendRequest) {
        pendingFriendRequests.removeAll { it.id == request.id }
        friendService.declineFriendRequest(request.id)
    }

    fun supportsFriendAddRemove(): Boolean = friendService.supportsAddRemove()

    fun unreadConversationCount(): Int = unreadChatFriendCount

    fun canChatWithFriend(friendId: String): Boolean = chatFriends.any { it.id == friendId }

    private fun canBrowseFriend(friendId: String): Boolean {
        return chatFriends.any { it.id == friendId && it.browseable } ||
            friendService.canBrowseFriendLibrary(friendId)
    }

    fun chatWithFriend(friendId: String): Boolean {
        val rosterItem = chatFriends.firstOrNull { it.id == friendId } ?: return false
        selectConversation(rosterItem)
        selectFriends()
        return true
    }

    fun friendConversationPreview(friendId: String): String? {
        val message = chatConversations[friendId]?.messages?.lastOrNull() ?: return null
        val body = conversationMessageSummary(message)
        if (body.isEmpty()) {
            return null
        }
        val prefix = when {
            message.server || message.kind == ConversationMessageKind.STATUS -> tr("System")
            message.outgoing -> tr("You")
            else -> message.senderName
        }
        return tr("{0}: {1}", prefix, body)
    }

    private fun conversationMessageSummary(message: ConversationMessage): String {
        if (message.isFileOffer) {
            val offer = message.fileOffer ?: return tr("Shared file")
            return when {
                message.isOutgoing -> tr("Shared file: {0}", offer.fileName)
                offer.downloadState == DownloadState.DONE -> tr("Downloaded: {0}", offer.fileName)
                offer.downloadState == DownloadState.DOWNLOADING ||
                    offer.downloadState == DownloadState.CONNECTING ||
                    offer.downloadState == DownloadState.FINISHING ||
                    offer.downloadState == DownloadState.LOCAL_QUEUED ||
                    offer.downloadState == DownloadState.REMOTE_QUEUED ||
                    offer.downloadState == DownloadState.TRYING_AGAIN ||
                    offer.downloadState == DownloadState.RESUMING ->
                    tr("Downloading shared file: {0}", offer.fileName)
                offer.downloadState == DownloadState.ERROR ||
                    offer.downloadState == DownloadState.STALLED ||
                    offer.downloadState == DownloadState.CANCELLED ->
                    tr("Shared file needs attention: {0}", offer.fileName)
                else -> tr("Shared file: {0}", offer.fileName)
            }
        }
        return message.body.trim().replace('\n', ' ')
    }

    fun updateSearchCategory(category: SearchCategory) {
        searchCategory = category
        searchValidationError = null
        selectedSearchSuggestionIndex = -1
        refreshSearchSuggestions()
    }

    fun updateSearchQuery(value: String) {
        searchQuery = value
        searchValidationError = null
        selectedSearchSuggestionIndex = -1
        refreshSearchSuggestions()
    }

    fun showSearchSuggestions() {
        searchSuggestionsVisible = true
        selectedSearchSuggestionIndex = -1
        refreshSearchSuggestions()
    }

    fun dismissSearchSuggestions() {
        searchSuggestionsVisible = false
        selectedSearchSuggestionIndex = -1
    }

    fun moveSearchSuggestionSelection(delta: Int) {
        if (searchSuggestions.isEmpty()) {
            selectedSearchSuggestionIndex = -1
            return
        }
        val nextIndex = when {
            selectedSearchSuggestionIndex < 0 && delta > 0 -> 0
            selectedSearchSuggestionIndex < 0 && delta < 0 -> searchSuggestions.lastIndex
            else -> (selectedSearchSuggestionIndex + delta).coerceIn(0, searchSuggestions.lastIndex)
        }
        selectedSearchSuggestionIndex = nextIndex
    }

    fun clearSearchSuggestionSelection() {
        selectedSearchSuggestionIndex = -1
    }

    fun selectSearchSuggestionIndex(index: Int) {
        if (index in searchSuggestions.indices) {
            selectedSearchSuggestionIndex = index
        }
    }

    fun hasVisibleSearchSuggestions(): Boolean = searchSuggestionsVisible && searchSuggestions.isNotEmpty()

    fun acceptSelectedSearchSuggestion(): Boolean {
        val index = selectedSearchSuggestionIndex
        if (index !in searchSuggestions.indices) {
            return false
        }
        acceptSearchSuggestion(searchSuggestions[index])
        return true
    }

    fun acceptSearchSuggestion(entry: SearchSuggestionEntry) {
        dismissSearchSuggestions()
        searchValidationError = null
        when (entry.action) {
            SearchSuggestionAction.SEARCH -> {
                val queryText = entry.queryText.trim()
                if (queryText.isBlank()) {
                    return
                }
                if (!ensureProgramSearchAllowed(entry.category)) {
                    return
                }
                if (keepSearchHistoryEnabled) {
                    settingsService.recordSearchHistoryEntry(queryText)
                }
                searchCategory = entry.category
                startSearch(
                    title = entry.title,
                    query = queryText,
                    category = entry.category,
                    searchType = SearchDetails.SearchType.KEYWORD,
                    advancedDetails = entry.advancedDetails
                )
                searchQuery = ""
            }

            SearchSuggestionAction.BROWSE_FRIEND_LIBRARY -> {
                val friendId = entry.friendId ?: return
                searchQuery = ""
                browseFriendLibrary(friendId)
            }
        }
    }

    fun submitSearch() {
        if (acceptSelectedSearchSuggestion()) {
            return
        }
        val trimmed = searchQuery.trim()
        if (trimmed.isEmpty()) {
            searchValidationError = tr("Enter a search query to continue.")
            return
        }
        if (!ensureProgramSearchAllowed(searchCategory)) {
            return
        }

        searchValidationError = null
        if (keepSearchHistoryEnabled) {
            settingsService.recordSearchHistoryEntry(trimmed)
        }
        dismissSearchSuggestions()
        startSearch(
            title = trimmed,
            query = trimmed,
            category = searchCategory,
            searchType = SearchDetails.SearchType.KEYWORD,
            advancedDetails = emptyMap()
        )
        searchQuery = ""
    }

    private fun refreshSearchSuggestions() {
        if (!searchSuggestionsVisible) {
            searchSuggestions.clear()
            selectedSearchSuggestionIndex = -1
            return
        }

        val suggestions = linkedMapOf<String, SearchSuggestionEntry>()
        searchSuggestionsService.suggestions(
            input = searchQuery,
            category = searchCategory,
            includeHistory = keepSearchHistoryEnabled,
            includeSmartSuggestions = showSmartSuggestionsEnabled
        ).forEach { entry ->
            suggestions.putIfAbsent(searchSuggestionDedupKey(entry), entry)
        }
        friendSearchSuggestions(searchQuery).forEach { entry ->
            suggestions.putIfAbsent(searchSuggestionDedupKey(entry), entry)
        }
        searchSuggestions.clear()
        searchSuggestions.addAll(suggestions.values.take(12))
        if (searchSuggestions.isEmpty()) {
            selectedSearchSuggestionIndex = -1
            searchSuggestionsVisible = false
        } else if (selectedSearchSuggestionIndex !in searchSuggestions.indices) {
            selectedSearchSuggestionIndex = -1
        }
    }

    private fun friendSearchSuggestions(input: String): List<SearchSuggestionEntry> {
        if (!showSmartSuggestionsEnabled) {
            return emptyList()
        }

        val normalizedInput = input.trim().lowercase(Locale.US)
        return chatFriends
            .asSequence()
            .filter { friend ->
                normalizedInput.isBlank() ||
                    friend.displayName.lowercase(Locale.US).contains(normalizedInput) ||
                    friend.id.lowercase(Locale.US).contains(normalizedInput) ||
                    friend.status.lowercase(Locale.US).contains(normalizedInput) ||
                    friendConversationPreview(friend.id)?.lowercase(Locale.US)?.contains(normalizedInput) == true
            }
            .take(if (normalizedInput.isBlank()) 4 else 6)
            .map { friend ->
                val statusParts = buildList {
                    if (!friend.signedIn) {
                        add(tr("Signed out"))
                    } else if (friend.status.isNotBlank()) {
                        add(friend.status)
                    } else {
                        friend.mode?.let { add(humanizeEnumName(it.name)) }
                    }
                    if (friend.hasUnviewedMessages()) {
                        add(tr("Unread"))
                    }
                    friendConversationPreview(friend.id)?.takeIf(String::isNotBlank)?.let(::add)
                }
                SearchSuggestionEntry(
                    source = SearchSuggestionSource.FRIEND,
                    title = friend.displayName,
                    queryText = friend.displayName,
                    subtitle = statusParts.joinToString(" · "),
                    category = searchCategory,
                    action = SearchSuggestionAction.BROWSE_FRIEND_LIBRARY,
                    friendId = friend.id
                )
            }
            .toList()
    }

    private fun searchSuggestionDedupKey(entry: SearchSuggestionEntry): String {
        return when (entry.action) {
            SearchSuggestionAction.SEARCH -> "search:${entry.category.name}:${entry.queryText.lowercase(Locale.US)}"
            SearchSuggestionAction.BROWSE_FRIEND_LIBRARY -> "friend:${entry.friendId.orEmpty()}"
        }
    }

    private fun ensureProgramSearchAllowed(category: SearchCategory): Boolean {
        if (category != SearchCategory.PROGRAM || allowProgramSearchAndShareEnabled) {
            return true
        }
        confirmationDialog = ConfirmationDialogState(
            title = tr("Warning"),
            message = tr(
                "Program files may contain viruses. WireShare does not include a virus scanner. For your protection, program downloading has been disabled. To enable it, open Library & Sharing preferences."
            ),
            confirmLabel = tr("Open Preferences"),
            dismissLabel = tr("Close"),
            onConfirm = {
                confirmationDialog = null
                showPreferences(PreferencesSection.LIBRARY)
            },
            onDismiss = { confirmationDialog = null }
        )
        return false
    }

    fun submitAdvancedSearch() {
        if (acceptSelectedAdvancedSearchSuggestion()) {
            advancedSearchDialogError = null
            return
        }
        val populated = advancedSearchDraft.values
            .mapValues { it.value.trim() }
            .filterValues { it.isNotEmpty() }

        if (populated.isEmpty()) {
            advancedSearchDialogError = tr("Fill in at least one field to continue.")
            return
        }

        val category = advancedSearchDraft.category
        if (!ensureProgramSearchAllowed(category)) {
            return
        }
        advancedSearchDialogError = null
        val fieldLookup = advancedSearchFields(category).associateBy { it.key }
        val title = populated.entries.joinToString(" · ") { (key, value) ->
            "${fieldLookup[key]?.label ?: humanizeEnumName(key.name)}: $value"
        }

        dismissAdvancedSearchSuggestions()
        advancedSearchDialogOpen = false
        searchCategory = category
        startSearch(
            title = title,
            query = "",
            category = category,
            searchType = SearchDetails.SearchType.KEYWORD,
            advancedDetails = populated
        )
    }

    fun submitWhatsNewSearch(category: SearchCategory) {
        if (!ensureProgramSearchAllowed(category)) {
            return
        }
        searchCategory = category
        startSearch(
            title = whatsNewTitle(category),
            query = "",
            category = category,
            searchType = SearchDetails.SearchType.WHATS_NEW,
            advancedDetails = emptyMap()
        )
    }

    fun browseAllFriends() {
        if (!canBrowseFriendsFiles()) {
            showNotice(
                tr("Friends"),
                tr("Sign in to browse your friends' shared files."),
                OperationNoticeLevel.WARNING
            )
            return
        }
        val request = browseService.browseAllFriends()
        openBrowseTab(request)
        request.search.start()
    }

    fun browseFriendLibrary(friendId: String) {
        val request = browseService.browseFriend(friendId) ?: return
        openBrowseTab(request)
        request.search.start()
    }

    fun supportsFriendPresenceModes(): Boolean = friendService.supportsPresenceModes()

    fun updateFriendDoNotDisturb(enabled: Boolean) {
        friendService.setDoNotDisturb(enabled)
        friendDoNotDisturb = friendService.isDoNotDisturbEnabled()
    }

    fun browsePrimarySource(result: SearchResult) {
        searchResultSourceTarget(result)?.let(::browseSourceTarget)
    }

    fun browseSources(result: GroupedSearchResult) {
        browseSourceTargets(searchResultBrowseTargets(result))
    }

    private fun openBrowseTab(request: BrowseRequest) {
        addSearchTab(
            title = request.title,
            query = request.query,
            category = SearchCategory.ALL,
            searchType = request.searchType,
            search = request.search,
            resultList = request.resultList
        )
    }

    fun refreshBrowseTab(tab: SearchTabSession) {
        if (tab.search !is BrowseSearch) {
            return
        }
        browseFailureDialog = null
        tab.browseRefreshing = true
        tab.browseStatus = BrowseStatusPresentation(BrowseState.LOADING)
        tab.expandedSimilarResultKeys.clear()
        clearSearchSelection(tab)
        scheduleSearchTabPresentationRefresh(tab, SearchPresentationDirty.RESULTS.mask)
        tab.search.repeat()
    }

    fun showBrowseFailures(tab: SearchTabSession) {
        val failures = tab.browseStatus?.failedSources.orEmpty()
        if (failures.isEmpty()) {
            return
        }
        browseFailureDialog = BrowseFailureDialogState(
            title = tr("Unable to Browse"),
            friends = failures.filterNot(BrowseFailureSource::anonymous).map(BrowseFailureSource::label),
            users = failures.filter(BrowseFailureSource::anonymous).map(BrowseFailureSource::label)
        )
    }

    fun closeBrowseFailureDialog() {
        browseFailureDialog = null
    }

    fun canSignInFromBrowseStatus(tab: SearchTabSession): Boolean {
        val state = tab.browseStatus?.state ?: return false
        return state in setOf(BrowseState.OFFLINE, BrowseState.NO_FRIENDS_SHARING) && !canBrowseFriendsFiles()
    }

    fun canChatFromBrowseStatus(tab: SearchTabSession): Boolean = browseStatusChatFriendId(tab) != null

    fun chatFromBrowseStatus(tab: SearchTabSession) {
        browseStatusChatFriendId(tab)?.let(::chatWithFriend)
    }

    private fun browseStatusChatFriendId(tab: SearchTabSession): String? {
        return tab.browseStatus
            ?.failedSources
            ?.asSequence()
            ?.mapNotNull { source -> source.id?.takeIf(::canChatWithFriend) }
            ?.firstOrNull()
    }

    private fun applyBrowseStatus(tab: SearchTabSession, status: BrowseStatus) {
        if (tab !in searchTabs) {
            return
        }
        tab.browseStatus = BrowseStatusPresentation(
            state = status.state,
            failedSources = status.failedFriends.map { friend ->
                BrowseFailureSource(
                    id = friend.id,
                    label = friend.renderName,
                    anonymous = friend.isAnonymous
                )
            }
        )
        if (status.state != BrowseState.LOADING) {
            tab.browseRefreshing = false
        }
        scheduleSearchTabPresentationRefresh(tab, SearchPresentationDirty.RESULTS.mask)
    }

    private fun startSearch(
        title: String,
        query: String,
        category: SearchCategory,
        searchType: SearchDetails.SearchType,
        advancedDetails: Map<FilePropertyKey, String>
    ) {
        if (!ensureProgramSearchAllowed(category)) {
            return
        }
        val details = ComposeSearchDetails(
            query = query,
            category = category,
            searchType = searchType,
            advancedDetails = advancedDetails
        )
        val search = searchFactory.createSearch(details)
        val resultList = searchManager.addSearch(search, details)
        addSearchTab(
            title = title,
            query = query,
            category = category,
            searchType = searchType,
            search = search,
            resultList = resultList
        )
        search.start()
    }

    private fun addSearchTab(
        title: String,
        query: String,
        category: SearchCategory,
        searchType: SearchDetails.SearchType,
        search: Search,
        resultList: SearchResultList
    ) {
        val session = SearchTabSession(
            id = nextSearchTabId++,
            title = title,
            query = query,
            category = category,
            searchType = searchType,
            search = search,
            resultList = resultList
        )
        session.sortMode = defaultSearchLayout.sortMode
        session.sortDescending = defaultSearchLayout.sortDescending
        session.visibleColumns = defaultSearchLayout.visibleColumns + defaultAdditionalSearchColumns(category)
        session.networkSearchRunning = searchType == SearchDetails.SearchType.KEYWORD || searchType == SearchDetails.SearchType.WHATS_NEW
        if (session.networkSearchRunning) {
            runCatching {
                ed2kService.startSearch(query)
            }.onSuccess { ed2kSession ->
                session.ed2kSessionId = ed2kSession.id
                session.ed2kSearchRunning = ed2kSession.isRunning
                reconcileKeyedStateList(session.ed2kResults, ed2kSession.results, ::searchResultKeyOf)
            }.onFailure {
                session.ed2kSearchRunning = false
            }
        }
        refreshCombinedSearchRunning(session)
        session.startedWhileNotFullyConnected = session.searchRunning && !isFullyConnected()
        session.descriptor = searchTabDescriptor(session)
        session.binding = EventListBinding(
            resultList.groupedResults,
            session.networkResults,
            onChanged = { syncMergedSearchResults(session) },
            keyOf = ::searchResultKeyOf
        )
        val searchListener = object : SearchListener {
            override fun handleSearchResult(search: Search, searchResult: SearchResult) {
            }

            override fun handleSearchResults(search: Search, searchResults: Collection<out SearchResult>) {
            }

            override fun searchStarted(search: Search) {
                runOnUi {
                    if (session !in searchTabs) {
                        return@runOnUi
                    }
                    if (session.searchType in setOf(SearchDetails.SearchType.KEYWORD, SearchDetails.SearchType.WHATS_NEW)) {
                        session.networkSearchRunning = true
                        refreshCombinedSearchRunning(session)
                        session.startedWhileNotFullyConnected = !isFullyConnected()
                    }
                    scheduleSearchTabPresentationRefresh(session, SearchPresentationDirty.STATUS.mask)
                }
            }

            override fun searchStopped(search: Search) {
                runOnUi {
                    if (session !in searchTabs) {
                        return@runOnUi
                    }
                    if (session.searchType in setOf(SearchDetails.SearchType.KEYWORD, SearchDetails.SearchType.WHATS_NEW)) {
                        session.networkSearchRunning = false
                        refreshCombinedSearchRunning(session)
                    }
                    scheduleSearchTabPresentationRefresh(session, SearchPresentationDirty.STATUS.mask)
                }
            }
        }
        search.addSearchListener(searchListener)
        session.searchListenerCloseable = AutoCloseable {
            search.removeSearchListener(searchListener)
        }
        if (search is BrowseSearch) {
            session.browseStatus = BrowseStatusPresentation(BrowseState.LOADING)
            val listener = BrowseStatusListener { status ->
                runOnUi {
                    applyBrowseStatus(session, status)
                }
            }
            search.addBrowseStatusListener(listener)
            session.browseStatusCloseable = AutoCloseable {
                search.removeBrowseStatusListener(listener)
            }
        }
        session.presentationDirtyMask = SearchPresentationDirty.FULL
        refreshSearchTabPresentation(session)
        searchTabs.add(0, session)
        currentScreen = ComposeScreen.Search(session.id)
    }

    fun selectSearchTab(tabId: Long) {
        currentScreen = ComposeScreen.Search(tabId)
    }

    fun closeSearchTab(tabId: Long) {
        val tab = searchTabs.firstOrNull { it.id == tabId } ?: return
        disposeSearchTab(tab)
        currentScreen = searchTabs.firstOrNull()?.let { ComposeScreen.Search(it.id) } ?: ComposeScreen.Library
    }

    fun closeOtherSearchTabs(tabId: Long) {
        val keep = searchTabs.firstOrNull { it.id == tabId } ?: return
        searchTabs.filter { it.id != tabId }.toList().forEach(::disposeSearchTab)
        currentScreen = ComposeScreen.Search(keep.id)
    }

    fun closeAllSearchTabs() {
        searchTabs.toList().forEach(::disposeSearchTab)
        currentScreen = ComposeScreen.Library
    }

    fun activeSearchTab(): SearchTabSession? {
        val activeId = (currentScreen as? ComposeScreen.Search)?.tabId ?: return searchTabs.firstOrNull()
        return searchTabs.firstOrNull { it.id == activeId }
    }

    fun canStopSearch(tab: SearchTabSession): Boolean {
        return tab.searchType in setOf(SearchDetails.SearchType.KEYWORD, SearchDetails.SearchType.WHATS_NEW) && tab.searchRunning
    }

    fun canSearchAgain(tab: SearchTabSession): Boolean {
        return tab.searchType in setOf(SearchDetails.SearchType.KEYWORD, SearchDetails.SearchType.WHATS_NEW)
    }

    fun canRepeatSearch(tab: SearchTabSession): Boolean {
        return canSearchAgain(tab) && !tab.searchRunning
    }

    fun shouldShowSearchConnectionWarning(tab: SearchTabSession): Boolean {
        return tab.searchType in setOf(SearchDetails.SearchType.KEYWORD, SearchDetails.SearchType.WHATS_NEW) &&
            tab.startedWhileNotFullyConnected &&
            !isFullyConnected() &&
            tab.results.size <= 10
    }

    fun shouldShowSearchAwaitingConnections(tab: SearchTabSession): Boolean {
        return shouldShowSearchConnectionWarning(tab) &&
            tab.searchRunning &&
            tab.results.isEmpty() &&
            connectionStrengthState in setOf(
                ConnectionStrength.CONNECTING,
                ConnectionStrength.DISCONNECTED,
                ConnectionStrength.NO_INTERNET
            )
    }

    fun stopSearch(tab: SearchTabSession) {
        if (!canStopSearch(tab)) {
            return
        }
        tab.search.stop()
        tab.networkSearchRunning = false
        tab.ed2kSessionId?.let(ed2kService::stopSearch)
        tab.ed2kSearchRunning = false
        refreshCombinedSearchRunning(tab)
        scheduleSearchTabPresentationRefresh(tab, SearchPresentationDirty.STATUS.mask)
    }

    fun repeatSearch(tab: SearchTabSession) {
        if (!canRepeatSearch(tab)) {
            return
        }
        restartSearch(tab)
    }

    fun continueSearch(tab: SearchTabSession) {
        if (!canSearchAgain(tab)) {
            return
        }
        tab.networkSearchRunning = true
        tab.startedWhileNotFullyConnected = !isFullyConnected()
        continueEd2kSearch(tab, clearExisting = false)
        refreshCombinedSearchRunning(tab)
        scheduleSearchTabPresentationRefresh(tab, SearchPresentationDirty.STATUS.mask)
        tab.search.repeat()
    }

    fun restartSearch(tab: SearchTabSession) {
        if (!canSearchAgain(tab)) {
            return
        }
        clearSearchSelection(tab)
        tab.expandedSimilarResultKeys.clear()
        tab.resultList.clear()
        tab.networkResults.clear()
        tab.ed2kResults.clear()
        tab.networkSearchRunning = true
        tab.startedWhileNotFullyConnected = !isFullyConnected()
        continueEd2kSearch(tab, clearExisting = true)
        refreshCombinedSearchRunning(tab)
        syncMergedSearchResults(tab)
        scheduleSearchTabPresentationRefresh(tab, SearchPresentationDirty.FULL)
        tab.search.repeat()
    }

    private fun disposeSearchTab(tab: SearchTabSession) {
        tab.searchListenerCloseable?.close()
        tab.browseStatusCloseable?.close()
        tab.binding?.close()
        tab.ed2kSessionId?.let(ed2kService::stopSearch)
        searchManager.stopSearch(tab.resultList)
        tab.resultList.dispose()
        searchTabs.remove(tab)
    }

    fun handleOpenFile(file: File) {
        restoreApplication()
        if (file.extension.equals("torrent", ignoreCase = true)) {
            openTorrentFile(file)
        } else {
            addFilesToLibrary(listOf(file))
        }
    }

    fun handleOpenUri(
        uriText: String,
        reportError: (String) -> Unit = { showNotice(tr("Open Link"), it, OperationNoticeLevel.ERROR) }
    ): Boolean {
        restoreApplication()
        val trimmed = uriText.trim()
        if (trimmed.isEmpty()) {
            reportError(tr("Enter a magnet link, ED2K link, or torrent URL."))
            return false
        }

        if (trimmed.startsWith("ed2k://", ignoreCase = true)) {
            return runCatching {
                val result = ed2kService.openEd2kLink(trimmed)
                if (result.isTransferStarted) {
                    noteTransferActivity(TransferTrayMode.DOWNLOADS)
                } else {
                    result.statusMessage?.takeIf(String::isNotBlank)?.let {
                        showNotice(tr("ED2K"), it, OperationNoticeLevel.SUCCESS)
                    }
                }
                true
            }.getOrElse { failure ->
                reportError(failure.message ?: trimmed)
                false
            }
        }

        val uri = try {
            URI(trimmed)
        } catch (badUri: Exception) {
            reportError(badUri.message ?: trimmed)
            return false
        }

        if (magnetFactory.isMagnetLink(uri)) {
            val magnets = magnetFactory.parseMagnetLink(uri)
            if (magnets.isEmpty()) {
                reportError(tr("Magnet link is empty."))
                return false
            }
            magnets.forEach { magnet ->
                if (!handleMagnet(magnet, reportError)) {
                    return false
                }
            }
            return true
        }

        openTorrentUri(uri)
        return true
    }

    fun submitOpenLink(text: String) {
        val opened = handleOpenUri(text) { error ->
            openLinkDialogError = error
        }
        if (opened) {
            openLinkDialogError = null
            openLinkDialogOpen = false
        }
    }

    fun openConversationLink(target: String) {
        val trimmed = target.trim()
        if (trimmed.isEmpty()) {
            return
        }
        if (trimmed.startsWith("magnet:", ignoreCase = true)) {
            handleOpenUri(trimmed) { error ->
                showNotice(tr("Chat"), error, OperationNoticeLevel.ERROR)
            }
            return
        }
        val uri = try {
            URI(trimmed)
        } catch (failure: Exception) {
            showNotice(
                tr("Chat"),
                failure.message ?: tr("That link could not be opened."),
                OperationNoticeLevel.ERROR
            )
            return
        }
        reportDesktopLaunch(
            launcher.openUri(uri),
            fallbackTitle = tr("Chat"),
            fallbackMessage = tr("That link could not be opened.")
        )
    }

    fun requestGlobalSearchFocus() {
        searchFocusRequestEpoch += 1
    }

    private fun reportDesktopLaunch(
        result: DesktopLaunchResult,
        fallbackTitle: String,
        fallbackMessage: String,
        blocking: Boolean = false
    ): Boolean {
        return when (result) {
            DesktopLaunchResult.Success -> true
            is DesktopLaunchResult.Failure -> {
                val title = result.title.ifBlank { fallbackTitle }
                val message = result.message.ifBlank { fallbackMessage }
                if (blocking) {
                    showMessage(title, message)
                } else {
                    showNotice(title, message, OperationNoticeLevel.ERROR)
                }
                false
            }
        }
    }

    private fun openFileWithComposeFeedback(
        file: File,
        fallbackTitle: String = tr("Open File"),
        fallbackMessage: String = tr("Unable to open file: {0}", file.name)
    ): Boolean {
        return reportDesktopLaunch(
            launcher.open(file),
            fallbackTitle = fallbackTitle,
            fallbackMessage = fallbackMessage
        )
    }

    private fun revealFileWithComposeFeedback(
        file: File,
        fallbackTitle: String = tr("Locate File"),
        fallbackMessage: String = tr("Unable to locate file: {0}", file.name)
    ): Boolean {
        return reportDesktopLaunch(
            launcher.reveal(file),
            fallbackTitle = fallbackTitle,
            fallbackMessage = fallbackMessage
        )
    }

    fun handleMagnet(
        magnet: MagnetLink,
        reportError: (String) -> Unit = { showNotice(tr("Open Link"), it, OperationNoticeLevel.ERROR) }
    ): Boolean {
        when {
            magnet.isGnutellaDownloadable() -> openMagnetDownload(magnet)
            magnet.isTorrentDownloadable() -> openTorrentMagnet(magnet)
            magnet.isKeywordTopicOnly() -> {
                val query = magnet.queryString?.trim().orEmpty()
                if (query.isEmpty()) {
                    reportError(tr("The magnet link is invalid."))
                    return false
                }
                searchCategory = SearchCategory.ALL
                searchQuery = query
                submitSearch()
            }
            else -> {
                reportError(tr("The magnet link is invalid."))
                return false
            }
        }
        return true
    }

    fun addFilesToLibrary(files: List<File>) {
        if (files.isEmpty()) {
            return
        }
        importFilesIntoLibrarySection(DEFAULT_LIBRARY_SECTION_ID, files)
    }

    fun chooseTorrentFiles() {
        val files = filePicker.chooseFiles(
            parent = windowRef,
            title = tr("Open Torrent"),
            multiple = true,
            filenameFilter = FilenameFilter { _, name -> name.lowercase(Locale.US).endsWith(".torrent") }
        )
        if (files.isNotEmpty()) {
            files.forEach(::handleOpenFile)
        }
    }

    fun chooseLibraryFiles() {
        val files = filePicker.chooseFiles(
            parent = windowRef,
            title = tr("Add to Library"),
            multiple = true
        )
        if (files.isNotEmpty()) {
            addFilesToLibrary(files)
        }
    }

    fun recentDownloads(): List<File> = recentDownloadsService.recentDownloads()

    fun hasRecentDownloads(): Boolean = recentDownloads().isNotEmpty()

    fun clearRecentDownloads() {
        recentDownloadsService.clearRecentDownloads()
        recentDownloadsEpoch += 1
    }

    fun openRecentDownload(file: File) {
        if (!file.exists()) {
            showNotice(
                tr("Recent Downloads"),
                tr("{0} is no longer available at its original location.", file.name),
                OperationNoticeLevel.WARNING
            )
            return
        }

        when (categoryManager.getCategoryForFile(file)) {
            Category.AUDIO -> {
                if (playerEnabled && playerService.isPlayable(file)) {
                    playQueueEntries(
                        entries = listOf(
                            PlayerQueueEntry(
                                file = file,
                                title = file.nameWithoutExtension.ifBlank { file.name },
                                sourceLabel = tr("Recent Downloads")
                            )
                        ),
                        currentFile = file,
                        sourceLabel = tr("Recent Downloads"),
                        mode = PlayerQueueMode.SNAPSHOT
                    )
                } else {
                    openFileWithComposeFeedback(file)
                }
            }

            Category.VIDEO -> playerService.playFile(file)
            Category.DOCUMENT, Category.IMAGE -> openFileWithComposeFeedback(file)
            Category.TORRENT -> openFileWithComposeFeedback(file)
            Category.PROGRAM, Category.OTHER -> revealFileWithComposeFeedback(file)
        }
    }

    fun hasStalledDownloads(): Boolean = transferRepairService.stalledDownloadCount() > 0

    fun retryableStalledDownloadCount(): Int = transferRepairService.stalledDownloadCount()

    fun fixStalledDownloads() {
        val repaired = transferRepairService.fixStalledDownloads()
        if (repaired <= 0) {
            showNotice(
                tr("Fix Stalled Downloads"),
                tr("There are no stalled downloads to repair."),
                OperationNoticeLevel.INFO
            )
            return
        }
        noteTransferActivity(TransferTrayMode.DOWNLOADS)
        showNotice(
            tr("Fix Stalled Downloads"),
            tr("Trying {0} stalled download(s) again.", repaired),
            OperationNoticeLevel.SUCCESS
        )
        if (notificationsEnabled && notifications.supportsNotifications()) {
            notifications.showNotification(
                title = tr("Fix Stalled Downloads"),
                body = tr("Trying {0} stalled download(s) again.", repaired),
                onOpen = {
                    restoreApplication()
                    openTransfersWorkspace(TransferTrayMode.DOWNLOADS)
                }
            )
        }
    }

    fun startExitAfterTransfers() {
        delayedExitService.start()
        syncDesktopShellState()
    }

    fun cancelExitAfterTransfers() {
        delayedExitService.cancel()
        syncDesktopShellState()
    }

    fun delayedExitSummary(): String {
        if (!delayedExitState.pending) {
            return tr("Inactive")
        }
        val waitingOn = buildList {
            if (!delayedExitState.downloadsCompleted) {
                add(tr("downloads"))
            }
            if (!delayedExitState.uploadsCompleted) {
                add(tr("uploads"))
            }
        }
        return if (waitingOn.isEmpty()) {
            tr("Finishing")
        } else {
            tr("Waiting on {0}", waitingOn.joinToString(" + "))
        }
    }

    fun canRetryConnection(): Boolean {
        return displayedConnectionStrength() == ConnectionStrength.DISCONNECTED ||
            displayedConnectionStrength() == ConnectionStrength.NO_INTERNET
    }

    fun retryConnection() {
        connectionManager.restart()
    }

    fun stopLibraryFileProcessing() {
        libraryManager.libraryManagedList.cancelPendingTasks()
    }

    fun dismissFileProcessingStatus() {
        if (fileProcessingStatus?.done == true) {
            fileProcessingStatus = null
        }
    }

    fun reviewPublicSharingSafety() {
        showPreferences(PreferencesSection.LIBRARY, openUnsafeSharingEditor = true)
    }

    fun dismissRuntimeErrorDialog(id: Long) {
        runtimeErrorDialogs.removeAll { it.id == id }
    }

    fun saveRuntimeErrorReport(report: ComposeRuntimeErrorReport) {
        val target = filePicker.chooseSaveFile(
            parent = windowRef,
            title = tr("Save Diagnostic Report"),
            suggestedName = "wireshare-error-report.txt"
        ) ?: return
        val output = if (target.extension.equals("txt", ignoreCase = true)) {
            target
        } else {
            File(target.parentFile, "${target.name}.txt")
        }
        runCatching {
            runtimeErrorService.saveDiagnosticReport(output, report)
        }.onSuccess {
            showNotice(
                tr("Diagnostics"),
                tr("Saved diagnostic report to {0}.", output.name),
                OperationNoticeLevel.SUCCESS
            )
        }.onFailure { failure ->
            showNotice(
                tr("Diagnostics"),
                failure.message ?: tr("WireShare could not save the diagnostic report."),
                OperationNoticeLevel.ERROR
            )
        }
    }

    private fun invalidateDownloadPresentationCache() {
        visibleDownloadsDirty = true
        transferSummaryDirty = true
        searchDownloadIndexDirty = true
        invalidateSearchResultPresentationCache()
    }

    private fun invalidateUploadPresentationCache() {
        visibleUploadsDirty = true
        transferSummaryDirty = true
    }

    private fun invalidateLibraryVisibilityCache() {
        libraryVisibilityDirty = true
        librarySelectionDirty = true
    }

    private fun invalidateLibrarySelectionCache() {
        librarySelectionDirty = true
    }

    private fun invalidateSharingSummaryCache() {
        sharingSummaryDirty = true
        invalidateSearchResultPresentationCache()
    }

    private fun invalidateSearchResultPresentationCache() {
        searchLibraryIndexDirty = true
        searchLocalAvailabilityCache.clear()
        searchResultPresentationCache.clear()
    }

    private fun transferActivitySummary(): TransferActivitySummary {
        if (!transferSummaryDirty) {
            return cachedTransferSummary
        }
        cachedTransferSummary = ComposePerformanceTracker.measure("transfers.summary") {
            var activeDownloads = 0
            var activeUploads = 0
            var totalDown = 0.0
            var totalUp = 0.0

            allDownloads().forEach { item ->
                if (!item.state.isFinished) {
                    activeDownloads += 1
                    totalDown += item.downloadSpeed.toDouble()
                }
            }
            allUploads().forEach { item ->
                if (!item.isFinished) {
                    activeUploads += 1
                    totalUp += item.uploadSpeed.toDouble()
                }
            }

            TransferActivitySummary(
                downloadCount = allDownloads().size,
                uploadCount = allUploads().size,
                activeDownloadCount = activeDownloads,
                activeUploadCount = activeUploads,
                totalDownloadBandwidth = totalDown.toFloat(),
                totalUploadBandwidth = totalUp.toFloat()
            )
        }
        transferSummaryDirty = false
        return cachedTransferSummary
    }

    private fun searchDownloadIndex(): SearchDownloadIndex {
        if (!searchDownloadIndexDirty) {
            return cachedSearchDownloadIndex
        }
        cachedSearchDownloadIndex = SearchDownloadIndex(
            byUrn = allDownloads().asSequence()
                .mapNotNull { item -> item.urn?.toString()?.let { it to item } }
                .toMap(),
            finishedUrns = allDownloads().asSequence()
                .mapNotNull { item ->
                    item.urn?.toString()?.takeIf { item.state.isFinished || item.completeFiles.isNotEmpty() }
                }
                .toSet(),
            activeUrns = allDownloads().asSequence()
                .mapNotNull { item ->
                    item.urn?.toString()?.takeIf { !item.state.isFinished && item.completeFiles.isEmpty() }
                }
                .toSet()
        )
        searchDownloadIndexDirty = false
        return cachedSearchDownloadIndex
    }

    private fun searchLibraryIndex(): SearchLibraryIndex {
        if (!searchLibraryIndexDirty) {
            return cachedSearchLibraryIndex
        }
        cachedSearchLibraryIndex = ComposePerformanceTracker.measure("search.libraryIndex") {
            val libraryUrns = snapshotEventList(libraryManager.libraryManagedList.swingModel)
                .mapNotNull { it.urn?.toString() }
                .toHashSet()
            val sharedTargetsByUrn = linkedMapOf<String, MutableList<LibraryJumpTarget>>()
            sharedLists
                .sortedBy { it.collectionName.lowercase(Locale.US) }
                .forEach { list ->
                    val target = LibraryJumpTarget("shared:${list.id}", list.collectionName)
                    snapshotEventList(list.swingModel).forEach { item ->
                        val key = item.urn?.toString() ?: return@forEach
                        sharedTargetsByUrn.getOrPut(key) { mutableListOf() }.add(target)
                    }
                }
            SearchLibraryIndex(
                libraryUrns = libraryUrns,
                sharedTargetsByUrn = sharedTargetsByUrn
            )
        }
        searchLibraryIndexDirty = false
        return cachedSearchLibraryIndex
    }

    private fun visibleLibraryCache(): LibraryVisibilityCache {
        if (!libraryVisibilityDirty && !librarySelectionDirty) {
            return cachedLibraryVisibility
        }
        if (libraryVisibilityDirty) {
            cachedLibraryVisibility = ComposePerformanceTracker.measure("library.visibleItems") {
                val filter = libraryFilterText.trim().lowercase(Locale.US)
                val filtered = libraryItems.filter { item ->
                    (libraryCategoryFilter == null || item.category == libraryCategoryFilter) &&
                        (filter.isEmpty() || matchesLibraryFilter(item, filter))
                }
                LibraryVisibilityCache(visibleItems = sortLibraryItems(filtered))
            }
            libraryVisibilityDirty = false
            librarySelectionDirty = true
        }
        if (librarySelectionDirty) {
            val visibleItems = cachedLibraryVisibility.visibleItems
            val selectedPaths = selectedLibraryItemPaths.toSet()
            val selectedItem = selectedLibraryItemPath
                ?.let { primaryPath -> visibleItems.firstOrNull { it.file.absolutePath == primaryPath } }
                ?: visibleItems.firstOrNull { it.file.absolutePath in selectedPaths }
            cachedLibraryVisibility = cachedLibraryVisibility.copy(
                selectedItems = visibleItems.filter { it.file.absolutePath in selectedPaths },
                selectedItem = selectedItem
            )
            librarySelectionDirty = false
        }
        return cachedLibraryVisibility
    }

    private fun sharingSummaryCache(): SharingSummaryCache {
        if (!sharingSummaryDirty) {
            return cachedSharingSummary
        }
        cachedSharingSummary = ComposePerformanceTracker.measure("sharing.summary") {
            val collections = sharedLists.sortedBy { it.collectionName.lowercase(Locale.US) }
            val availableFriendCollections = collections.filterNot { it.isPublic }
            val collectionSummaries = collections.map { list ->
                SharingStatusCollectionSummary(
                    sectionId = "shared:${list.id}",
                    label = list.collectionName,
                    fileCount = list.size(),
                    friendCount = list.friendIds.size,
                    publicCollection = list.isPublic
                )
            }
            val friendSharedCollections = collectionSummaries.count { !it.publicCollection && it.friendCount > 0 }
            SharingSummaryCache(
                availableCollections = collections,
                availableFriendCollections = availableFriendCollections,
                sharingStatusSummary = SharingStatusSummary(
                    sharedFileCount = sharedFileCount,
                    publicCollectionCount = collectionSummaries.count(SharingStatusCollectionSummary::publicCollection),
                    friendSharedCollectionCount = friendSharedCollections,
                    collections = collectionSummaries,
                    showSignInToShareWithFriends = friendConnectionState != FriendConnectionEvent.Type.CONNECTED &&
                        friendSharedCollections == 0
                )
            )
        }
        sharingSummaryDirty = false
        return cachedSharingSummary
    }

    fun downloadCount(): Int = transferActivitySummary().downloadCount

    fun uploadCount(): Int = transferActivitySummary().uploadCount

    fun activeDownloadCount(): Int = transferActivitySummary().activeDownloadCount

    fun activeUploadCount(): Int = transferActivitySummary().activeUploadCount

    fun totalDownloadBandwidth(): Float = transferActivitySummary().totalDownloadBandwidth

    fun totalUploadBandwidth(): Float = transferActivitySummary().totalUploadBandwidth

    fun canBrowseFriendsFiles(): Boolean = friendConnectionState == FriendConnectionEvent.Type.CONNECTED

    fun sharingStatusSummary(): SharingStatusSummary {
        return sharingSummaryCache().sharingStatusSummary
    }

    fun openSharingStatusSignIn() {
        selectFriends()
        showFriendLoginDialog()
    }

    fun openCollections() {
        selectLibrary()
    }

    fun currentPreferences(): PreferencesDraft = settingsService.loadPreferences()

    fun refreshTorrentEngineHealthState() {
        torrentEngineHealthState = settingsService.torrentEngineHealthState()
    }

    fun torrentEngineHealthTitle(): String {
        return when (torrentEngineHealthState) {
            TorrentEngineHealthState.STARTING -> tr("BitTorrent Starting")
            TorrentEngineHealthState.ERROR -> tr("BitTorrent Unavailable")
            TorrentEngineHealthState.READY -> tr("BitTorrent Ready")
        }
    }

    fun torrentEngineHealthMessage(): String {
        return when (torrentEngineHealthState) {
            TorrentEngineHealthState.STARTING ->
                tr("BitTorrent is starting. Torrent transfers will appear when it is ready.")
            TorrentEngineHealthState.ERROR ->
                tr("Error connecting to BitTorrent. Torrents will not work until this is resolved.")
            TorrentEngineHealthState.READY -> ""
        }
    }

    fun torrentPromptBeforeDownloadingEnabled(): Boolean = settingsService.torrentPromptBeforeDownloadingEnabled()

    fun setTorrentPromptBeforeDownloadingEnabled(enabled: Boolean) {
        settingsService.setTorrentPromptBeforeDownloadingEnabled(enabled)
    }

    fun applyPreferences(draft: PreferencesDraft) {
        val result = try {
            settingsService.applyPreferences(draft)
        } catch (failure: Exception) {
            preferencesDialogError = failure.message ?: tr("WireShare could not save those settings.")
            return
        }
        preferencesDialogError = null
        val refreshed = settingsService.loadPreferences()
        syncPreferenceStateAfterApply(refreshed)
        refreshTorrentEngineHealthState()
        preferencesDialogOpen = false
        if (result.restartRequired) {
            showNotice(
                tr("Preferences Saved"),
                result.restartMessage ?: tr("One or more settings will take effect after restarting WireShare."),
                OperationNoticeLevel.INFO
            )
        }
    }

    fun clearSearchHistory() {
        try {
            settingsService.clearSearchHistory()
        } catch (failure: Exception) {
            showNotice(
                tr("Search History"),
                failure.message ?: tr("WireShare could not clear the saved search history."),
                OperationNoticeLevel.ERROR
            )
            return
        }
        refreshSearchSuggestions()
        showNotice(tr("Search History"), tr("Saved search history has been cleared."), OperationNoticeLevel.SUCCESS)
    }

    fun choosePreferencesDownloadDirectory(currentValue: String): SaveDirectoryValidationResult? {
        return choosePreferencesDirectory(currentValue, tr("Choose Download Folder"))
    }

    fun choosePreferencesDirectory(currentValue: String, title: String): SaveDirectoryValidationResult? {
        val chosen = filePicker.chooseFiles(
            parent = windowRef,
            title = title,
            directoriesOnly = true,
            initialDirectory = File(currentValue).takeIf { it.exists() && it.isDirectory }
        ).firstOrNull() ?: return null
        return settingsService.validateSaveDirectory(chosen.absolutePath)
    }

    fun resetSpamFilter() {
        settingsService.resetSpamFilter()
        showNotice(tr("Search Filters"), tr("Spam-filter training data has been reset."), OperationNoticeLevel.SUCCESS)
    }

    fun resetWarnings() {
        settingsService.resetWarnings()
        showNotice(tr("Warnings"), tr("Desktop warning prompts have been reset."), OperationNoticeLevel.SUCCESS)
    }

    fun defaultBlockedExtensions(): List<String> = settingsService.defaultBlockedExtensions()

    private fun syncPreferenceStateAfterApply(refreshed: PreferencesDraft) {
        playerEnabled = settingsService.playerEnabled()
        playerVolume = settingsService.playerVolume()
        appearance = refreshed.system.appearance
        notificationsEnabled = refreshed.friends.showNotifications
        playNotificationSoundEnabled = refreshed.friends.playNotificationSound
        keepSearchHistoryEnabled = refreshed.search.keepSearchHistory
        showSmartSuggestionsEnabled = refreshed.search.showSmartSuggestions
        groupSimilarResultsEnabled = refreshed.search.groupSimilarResults
        allowProgramSearchAndShareEnabled = refreshed.library.allowProgramSearchAndShare
        searchCategory = refreshed.search.defaultCategory
        minimizeToTray = refreshed.system.minimizeToTray
        closeTrayWhenNoTransfers = refreshed.transfers.closeTrayWhenNoTransfers
        showTotalBandwidth = refreshed.transfers.showTotalBandwidth
        clearDownloadsWhenFinished = refreshed.transfers.clearDownloadsWhenFinished
        clearUploadsWhenFinished = refreshed.transfers.clearUploadsWhenFinished
        trayExpanded = refreshed.transfers.showTransfersTrayByDefault
        showUploadsInTray = settingsService.showUploadsInTray()
        refreshTorrentEngineHealthState()
        trayBehaviorPreferences = settingsService.loadTrayBehaviorPreferences()
        windowPlacementPreferences = settingsService.loadWindowPlacementPreferences()
        if (!groupSimilarResultsEnabled) {
            searchTabs.forEach { it.expandedSimilarResultKeys.clear() }
        }
        refreshSearchSuggestions()
        maybeAutoHideTray()
        syncDesktopShellState()
    }

    private fun beginStartupWorkflows() {
        if (setupWizardOpen) {
            return
        }
        maybeShowStartupSystemWarnings()
        if (startupSaveDirectoryIssue == null) {
            settingsService.validateCurrentSaveDirectory()?.let {
                startupSaveDirectoryIssue = it
                return
            }
        }
        if (startupFileAssociationPrompt == null) {
            settingsService.startupFileAssociationPrompt()?.let { prompt ->
                startupFileAssociationWarnOnChange = prompt.warnOnChange
                startupFileAssociationPrompt = prompt
            }
        }
        beginStartupGnutellaConnect()
    }

    private fun beginStartupGnutellaConnect() {
        if (startupGnutellaConnectAttempted) {
            return
        }
        EventQueue.invokeLater {
            if (startupGnutellaConnectAttempted) {
                return@invokeLater
            }
            if (!ConnectionSettings.CONNECT_ON_STARTUP.getValue()) {
                return@invokeLater
            }
            startupGnutellaConnectAttempted = true
            if (connectionManager.isConnected || displayedConnectionStrength() == ConnectionStrength.CONNECTING) {
                return@invokeLater
            }
            runCatching {
                connectionManager.connect()
            }
        }
    }

    private fun maybeShowStartupSystemWarnings() {
        if (startupSystemWarningsShown) {
            return
        }
        startupSystemWarningsShown = true
        if (LimeWireUtils.isTemporaryDirectoryInUse()) {
            showNotice(
                tr("Settings"),
                tr("WireShare was unable to create the settings folder and is using a temporary folder. Your settings may be deleted when you close WireShare."),
                OperationNoticeLevel.WARNING
            )
        }
        if (LimeWireUtils.hasSettingsLoadSaveFailures()) {
            LimeWireUtils.resetSettingsLoadSaveFailures()
            showNotice(
                tr("Settings"),
                tr("WireShare has encountered problems while managing its settings. Your changes may not be saved on shutdown."),
                OperationNoticeLevel.WARNING
            )
        }
    }

    private fun handleSystemMessage(message: ComposeSystemMessage) {
        if (message.severity == ComposeSystemMessageSeverity.INFO && message.checkboxLabel == null) {
            showNotice(message.title, message.message, OperationNoticeLevel.INFO)
            return
        }
        enqueueMessageDialog(
            MessageDialogState(
                title = message.title,
                message = message.message,
                severity = when (message.severity) {
                    ComposeSystemMessageSeverity.INFO -> MessageDialogSeverity.INFO
                    ComposeSystemMessageSeverity.ERROR -> MessageDialogSeverity.ERROR
                },
                checkboxLabel = message.checkboxLabel,
                checkboxInitialChecked = message.checkboxInitialChecked,
                onCloseWithCheckbox = message.onCloseWithCheckbox
            )
        )
    }

    private fun enqueueMessageDialog(dialog: MessageDialogState) {
        if (messageDialog == null) {
            messageDialog = dialog
        } else {
            pendingMessageDialogs.addLast(dialog)
        }
    }

    fun applySetupWizardDraft(transform: (PreferencesDraft) -> PreferencesDraft) {
        setupWizardError = null
        setupWizardDraft = transform(setupWizardDraft)
    }

    fun showSetupAssociationsPage(): Boolean = settingsService.shouldShowSetupAssociationsPage()

    fun nextSetupWizardPage() {
        setupWizardPage = when (setupWizardPage) {
            SetupWizardPage.ASSOCIATIONS -> SetupWizardPage.SHARING
            SetupWizardPage.SHARING -> SetupWizardPage.SECURITY
            SetupWizardPage.SECURITY -> SetupWizardPage.SECURITY
        }
    }

    fun previousSetupWizardPage() {
        setupWizardPage = when (setupWizardPage) {
            SetupWizardPage.ASSOCIATIONS -> SetupWizardPage.ASSOCIATIONS
            SetupWizardPage.SHARING ->
                if (settingsService.shouldShowSetupAssociationsPage()) SetupWizardPage.ASSOCIATIONS else SetupWizardPage.SHARING
            SetupWizardPage.SECURITY -> SetupWizardPage.SHARING
        }
    }

    fun finishSetupWizard() {
        val result = try {
            settingsService.applyPreferences(setupWizardDraft)
        } catch (failure: Exception) {
            setupWizardError = failure.message ?: tr("WireShare could not save the setup choices.")
            return
        }
        setupWizardError = null
        val refreshed = settingsService.loadPreferences()
        syncPreferenceStateAfterApply(refreshed)
        setupWizardOpen = false
        InstallSettings.UPGRADED_TO_5.setValue(true)
        InstallSettings.LAST_VERSION_RUN.set(application.version)
        if (!InstallSettings.PREVIOUS_RAN_VERSIONS.get().contains(application.version)) {
            InstallSettings.PREVIOUS_RAN_VERSIONS.add(application.version)
        }
        if (result.restartRequired) {
            showNotice(
                tr("Setup Complete"),
                result.restartMessage ?: tr("Some setup choices will take effect after restarting WireShare."),
                OperationNoticeLevel.INFO
            )
        }
        beginStartupWorkflows()
    }

    fun chooseStartupSaveDirectory() {
        val currentValue = settingsService.loadPreferences().transfers.downloadDirectory
        val validation = choosePreferencesDownloadDirectory(currentValue) ?: return
        if (!validation.accepted) {
            startupSaveDirectoryIssue = validation.errorMessage ?: tr("Choose a different download folder to continue.")
            return
        }
        val approvedPath = validation.normalizedPath ?: currentValue
        if (approvedPath == currentValue) {
            return
        }
        val currentDraft = settingsService.loadPreferences()
        val updatedDraft = currentDraft.copy(
            transfers = currentDraft.transfers.copy(downloadDirectory = approvedPath)
        )
        val result = try {
            settingsService.applyPreferences(updatedDraft)
        } catch (failure: Exception) {
            startupSaveDirectoryIssue = failure.message ?: tr("Choose a different download folder to continue.")
            return
        }
        syncPreferenceStateAfterApply(settingsService.loadPreferences())
        startupSaveDirectoryIssue = null
        if (result.restartRequired) {
            showNotice(
                tr("Download Folder Updated"),
                result.restartMessage ?: tr("One or more settings will take effect after restarting WireShare."),
                OperationNoticeLevel.INFO
            )
        }
        beginStartupWorkflows()
    }

    fun quitFromStartupBlocker() {
        requestExit()
    }

    fun resolveStartupFileAssociations(reassociate: Boolean, warnOnChange: Boolean) {
        settingsService.resolveStartupFileAssociationPrompt(reassociate, warnOnChange)
        startupFileAssociationWarnOnChange = warnOnChange
        startupFileAssociationPrompt = null
    }

    fun availableLocales(): List<Locale> = localizationService.availableLocales()

    fun currentLocale(): Locale = localizationService.currentLocale()

    fun applyLanguage(locale: Locale) {
        localizationService.applyLocale(locale)
        localeEpoch += 1
        languageDialogOpen = false
    }

    fun connectionStrength(): ConnectionStrength = displayedConnectionStrength()

    fun activeLibrarySection(): LibrarySection? = librarySections.firstOrNull { it.id == selectedLibrarySectionId }

    fun currentSharedList(): SharedFileList? {
        val id = selectedLibrarySectionId.removePrefix("shared:")
        return sharedLists.firstOrNull { selectedLibrarySectionId.startsWith("shared:") && it.id.toString() == id }
    }

    fun setupWizardPublicSharingHeadline(): String {
        return tr("Files in Public Shared are shared anonymously with everyone.")
    }

    fun setupWizardPublicSharingContinuationMessage(): String? {
        if (publicSharedListCount <= 0 || InstallSettings.PREVIOUS_RAN_VERSIONS.get().isEmpty()) {
            return null
        }
        return if (publicSharedListCount == 1) {
            tr("1 file from an earlier version will stay shared publicly.")
        } else {
            tr("{0} files from an earlier version will stay shared publicly.", publicSharedListCount)
        }
    }

    fun publicSharedUpgradeMessage(): String? {
        if (application.isNewInstall || publicSharedListCount <= 0 || InstallSettings.PREVIOUS_RAN_VERSIONS.get().isEmpty()) {
            return null
        }
        return if (publicSharedListCount == 1) {
            tr("1 file from an earlier WireShare install is still public here.")
        } else {
            tr("{0} files from earlier WireShare installs are still public here.", publicSharedListCount)
        }
    }

    fun shouldShowLibrarySharingCoachmark(): Boolean {
        val section = activeLibrarySection() ?: return false
        return showLibraryOverlayMessage &&
            friendConnectionState == FriendConnectionEvent.Type.CONNECTED &&
            (!section.isShared || section.isPublic)
    }

    fun shouldShowCollectionSharingCoachmark(): Boolean {
        val section = activeLibrarySection() ?: return false
        return showSharingOverlayMessage &&
            friendConnectionState == FriendConnectionEvent.Type.CONNECTED &&
            section.isShared &&
            !section.isPublic
    }

    fun dismissLibrarySharingCoachmark() {
        showLibraryOverlayMessage = false
        settingsService.setShowLibraryOverlayMessageEnabled(false)
    }

    fun dismissCollectionSharingCoachmark() {
        showSharingOverlayMessage = false
        settingsService.setShowSharingOverlayMessageEnabled(false)
    }

    fun openPrivateSharingCollectionFromCoachmark() {
        dismissLibrarySharingCoachmark()
        val existing = availableFriendCollections().firstOrNull {
            it.collectionName.equals(tr("Private Shared"), ignoreCase = true)
        } ?: availableFriendCollections().firstOrNull()
        val sectionId = if (existing != null) {
            "shared:${existing.id}"
        } else {
            "shared:${sharedFileListManager.createNewSharedFileList(tr("Private Shared"))}"
        }
        selectLibrarySection(sectionId)
    }

    fun shareCurrentCollectionFromCoachmark() {
        dismissCollectionSharingCoachmark()
        openCurrentSharedListSharingWorkspace()
    }

    fun addFilesToCurrentCollectionFromCoachmark() {
        dismissCollectionSharingCoachmark()
        addFilesToCurrentLibrarySection()
    }

    fun currentLibrarySectionRemoveLabel(): String {
        return if (currentSharedList() != null) tr("Remove from Collection") else tr("Remove from Library")
    }

    fun canRemoveSelectedLibraryItemsFromAllOtherLists(): Boolean {
        val items = selectedLibraryItems()
        if (items.isEmpty()) {
            return false
        }
        val currentList = currentSharedList()
        return sharedLists.any { list ->
            list !== currentList && items.any { list.contains(it.file) }
        }
    }

    fun draggableLibraryFiles(item: LocalFileItem): List<File> {
        val selection = selectedLibraryItems()
        return if (item.file.absolutePath in selectedLibraryItemPaths && selection.isNotEmpty()) {
            selection.map { it.file }.distinctBy { it.absolutePath }
        } else {
            listOf(item.file)
        }
    }

    fun draggableDownloadFiles(item: DownloadItem): List<File> {
        val selection = selectedDownloadItems()
            .mapNotNull { candidate ->
                when {
                    candidate.isLaunchable -> candidate.launchableFile
                    else -> candidate.completeFiles.firstOrNull()
                }
            }
            .filter { it.exists() }
            .distinctBy { it.absolutePath }
        return if (downloadSelectionKey(item) in selectedDownloadUrns && selection.isNotEmpty()) {
            selection
        } else {
            listOfNotNull(
                item.launchableFile?.takeIf { it.exists() }
                    ?: item.completeFiles.firstOrNull()?.takeIf { it.exists() }
            )
        }
    }

    fun draggableSearchResultKeys(tab: SearchTabSession, result: GroupedSearchResult): List<String> {
        val selection = selectedSearchResults(tab).map(::searchResultKeyOf)
        return if (searchResultKeyOf(result) in tab.selectedResultKeys && selection.isNotEmpty()) {
            selection
        } else {
            listOf(searchResultKeyOf(result))
        }
    }

    fun draggableUploadFiles(item: UploadItem): List<File> {
        val selection = selectedUploadItems()
            .map { it.file }
            .filter { it.exists() }
            .distinctBy { it.absolutePath }
        return if (uploadSelectionKey(item) in selectedUploadUrns && selection.isNotEmpty()) {
            selection
        } else {
            listOfNotNull(item.file.takeIf(File::exists))
        }
    }

    fun draggableCurrentPlayerFiles(): List<File> {
        return listOfNotNull(playerCurrentFile?.takeIf(File::exists))
    }

    fun draggablePlayerQueueFiles(entry: PlayerQueueEntry): List<File> {
        return listOfNotNull(entry.file.takeIf(File::exists))
    }

    fun availableLibraryColumns(): List<LibraryColumn> {
        return libraryColumnsForCategory(libraryCategoryFilter)
    }

    fun availableLibrarySortModes(): List<LibrarySortMode> {
        return librarySortModesForCategory(libraryCategoryFilter)
    }

    fun availableSearchColumns(tab: SearchTabSession): List<SearchColumn> {
        return searchColumnsForCategory(searchPresentationCategory(tab))
    }

    fun availableSearchSortModes(tab: SearchTabSession): List<SearchSortMode> {
        return searchSortModesForCategory(searchPresentationCategory(tab))
    }

    fun searchPresentationCategory(tab: SearchTabSession): SearchCategory {
        return tab.presentationState.presentationCategory
    }

    private fun rawSearchPresentationCategory(tab: SearchTabSession): SearchCategory {
        return if (tab.category == SearchCategory.ALL) {
            tab.displayCategory ?: SearchCategory.ALL
        } else {
            tab.category
        }
    }

    fun searchPresentationCategoryLabel(tab: SearchTabSession): String {
        return friendlyCategoryName(searchPresentationCategory(tab))
    }

    fun searchActiveFilterCount(tab: SearchTabSession): Int = tab.presentationState.activeFilters.size

    fun searchActiveFilters(tab: SearchTabSession): List<SearchActiveFilterToken> = tab.presentationState.activeFilters

    fun dismissSearchActiveFilter(tab: SearchTabSession, token: SearchActiveFilterToken) {
        when (token.type) {
            SearchActiveFilterType.TEXT -> tab.filterText = ""
            SearchActiveFilterType.FRIENDS_ONLY -> tab.friendsOnly = false
            SearchActiveFilterType.SOURCE -> tab.sourceFilter = SearchSourceFilter.ALL
            SearchActiveFilterType.FRIEND -> tab.selectedFriendFacetId = null
            SearchActiveFilterType.CATEGORY -> {
                updateSearchDisplayCategory(tab, null)
                return
            }
            SearchActiveFilterType.PROPERTY ->
                SearchPropertyFacet.entries.firstOrNull { it.name == token.key }?.let(tab.selectedPropertyFacets::remove)
            SearchActiveFilterType.RANGE ->
                SearchRangeFacet.entries.firstOrNull { it.name == token.key }?.let(tab.selectedRangeFacets::remove)
        }
        normalizeSearchFacetState(tab)
        scheduleSearchTabPresentationRefresh(tab, SearchPresentationDirty.FILTERS.mask)
    }

    fun visibleLibraryItems(): List<LocalFileItem> {
        return visibleLibraryCache().visibleItems
    }

    fun selectedLibraryItem(): LocalFileItem? {
        return visibleLibraryCache().selectedItem
    }

    fun selectedLibraryItems(): List<LocalFileItem> {
        return visibleLibraryCache().selectedItems
    }

    fun availableCollections(): List<SharedFileList> = sharingSummaryCache().availableCollections

    fun availableFriendCollections(): List<SharedFileList> = sharingSummaryCache().availableFriendCollections

    fun canShareCollectionsWithFriends(): Boolean = sharingSummaryCache().availableCollections.isNotEmpty()

    fun isCollectionSharedWithFriend(collectionId: Int, friendId: String): Boolean {
        return sharedLists.firstOrNull { it.id == collectionId }?.friendIds?.contains(friendId) == true
    }

    fun shareCollectionWithFriend(collectionId: Int, friendId: String) {
        val collection = sharedLists.firstOrNull { it.id == collectionId } ?: return
        val trimmedFriendId = friendId.trim()
        if (trimmedFriendId.isEmpty()) {
            return
        }
        collection.addFriend(trimmedFriendId)
        friendCollectionShareEpoch += 1
        invalidateSharingSummaryCache()
        showNotice(
            tr("Collections"),
            tr("Shared \"{0}\" with {1}.", collection.collectionName, trimmedFriendId),
            OperationNoticeLevel.SUCCESS
        )
    }

    fun toggleCollectionShareWithFriend(collectionId: Int, friendId: String) {
        val collection = sharedLists.firstOrNull { it.id == collectionId } ?: return
        val trimmedFriendId = friendId.trim()
        if (trimmedFriendId.isEmpty()) {
            return
        }
        if (collection.friendIds.contains(trimmedFriendId)) {
            collection.removeFriend(trimmedFriendId)
            showNotice(
                tr("Collections"),
                tr("Stopped sharing \"{0}\" with {1}.", collection.collectionName, trimmedFriendId),
                OperationNoticeLevel.INFO
            )
        } else {
            collection.addFriend(trimmedFriendId)
            showNotice(
                tr("Collections"),
                tr("Shared \"{0}\" with {1}.", collection.collectionName, trimmedFriendId),
                OperationNoticeLevel.SUCCESS
            )
        }
        friendCollectionShareEpoch += 1
        invalidateSharingSummaryCache()
    }

    fun showShareNewListDialog(friend: FriendRosterItem) {
        val suggestedName = tr("{0}'s Shared Files", friend.displayName)
        textEntryDialog = TextEntryDialogState(
            title = tr("Share New Collection"),
            label = tr("Collection name"),
            initialValue = suggestedName,
            confirmLabel = tr("Create"),
            validator = { value ->
                if (value.trim().isEmpty()) tr("Enter a collection name to continue.") else null
            },
            onConfirm = { name ->
                val trimmed = name.trim()
                val listId = sharedFileListManager.createNewSharedFileList(trimmed)
                pendingCollectionShares[listId] = friend.id
                friendCollectionShareEpoch += 1
                invalidateSharingSummaryCache()
                selectLibrarySection("shared:$listId")
                null
            }
        )
    }

    fun hasActiveLibraryFilters(): Boolean = libraryFilterText.isNotBlank() || libraryCategoryFilter != null

    fun selectedLibraryItemsPlayInPlayer(): Boolean {
        val items = selectedLibraryItems()
        return items.isNotEmpty() && items.all {
            categoryManager.getCategoryForFile(it.file) == Category.AUDIO &&
                playerService.isPlayable(it.file) &&
                playerEnabled
        }
    }

    fun canAddSelectedLibraryItemsToCollection(): Boolean = selectedLibraryItems().isNotEmpty() && sharedLists.isNotEmpty()

    fun canRenameSelectedLibraryItem(): Boolean = selectedLibraryItems().singleOrNull()?.isIncomplete == false

    fun showRenameLibraryFileDialog(item: LocalFileItem? = selectedLibraryItem()) {
        val resolvedItem = item ?: return
        if (resolvedItem.isIncomplete) {
            return
        }
        textEntryDialog = TextEntryDialogState(
            title = tr("Rename File"),
            label = tr("File name"),
            initialValue = resolvedItem.name,
            confirmLabel = tr("Rename"),
            validator = { validateLibraryRenameRequest(it) },
            onConfirm = { value -> renameLibraryFile(resolvedItem, value) }
        )
    }

    fun showLibraryFileInfo(item: LocalFileItem? = selectedLibraryItem()) {
        val resolvedItem = item ?: return
        libraryFileInfoDialog = nextLibraryFileInfoDialogState(resolvedItem)
    }

    fun closeLibraryFileInfoDialog() {
        libraryFileInfoDialog = null
    }

    fun libraryMetadataEditor(item: LocalFileItem): LibraryMetadataEditorPresentation? {
        val draft = LibraryMetadataDraft(
            title = item.getPropertyString(FilePropertyKey.TITLE).orEmpty(),
            author = item.getPropertyString(FilePropertyKey.AUTHOR).orEmpty(),
            album = item.getPropertyString(FilePropertyKey.ALBUM).orEmpty(),
            genre = item.getPropertyString(FilePropertyKey.GENRE).orEmpty(),
            year = item.getProperty(FilePropertyKey.YEAR)?.toString().orEmpty(),
            track = item.getProperty(FilePropertyKey.TRACK_NUMBER)?.toString().orEmpty(),
            rating = item.getPropertyString(FilePropertyKey.RATING).orEmpty(),
            description = item.getPropertyString(FilePropertyKey.DESCRIPTION).orEmpty(),
            platform = item.getPropertyString(FilePropertyKey.PLATFORM).orEmpty(),
            company = item.getPropertyString(FilePropertyKey.COMPANY).orEmpty()
        )
        val editable = !item.isIncomplete
        return when (item.category) {
            Category.AUDIO -> LibraryMetadataEditorPresentation(
                editable = editable,
                draft = draft,
                genreChoices = metadataChoiceList(draft.genre, propertyDictionary.audioGenres)
            )

            Category.VIDEO -> LibraryMetadataEditorPresentation(
                editable = editable,
                draft = draft,
                genreChoices = metadataChoiceList(draft.genre, propertyDictionary.videoGenres),
                ratingChoices = metadataChoiceList(draft.rating, propertyDictionary.videoRatings)
            )

            Category.IMAGE -> LibraryMetadataEditorPresentation(
                editable = editable,
                draft = draft
            )

            Category.DOCUMENT -> LibraryMetadataEditorPresentation(
                editable = editable,
                draft = draft
            )

            Category.PROGRAM -> LibraryMetadataEditorPresentation(
                editable = editable,
                draft = draft,
                platformChoices = metadataChoiceList(draft.platform, propertyDictionary.applicationPlatforms)
            )

            Category.OTHER,
            Category.TORRENT -> null
        }
    }

    fun saveLibraryMetadataFromInfo(item: LocalFileItem, draft: LibraryMetadataDraft): LibraryMetadataSaveResult {
        val editor = libraryMetadataEditor(item)
            ?: return LibraryMetadataSaveResult(dialogError = tr("This file does not expose editable metadata."))
        if (!editor.editable) {
            return LibraryMetadataSaveResult(dialogError = tr("Finish this file before editing its metadata."))
        }

        val fieldErrors = linkedMapOf<FilePropertyKey, String>()
        val newData = linkedMapOf<FilePropertyKey, Any>()

        fun putText(key: FilePropertyKey, value: String) {
            newData[key] = value.trim()
        }

        fun putYear(value: String) {
            val trimmed = value.trim()
            when {
                trimmed.isEmpty() -> newData[FilePropertyKey.YEAR] = ""
                trimmed.toLongOrNull() == null -> fieldErrors[FilePropertyKey.YEAR] = tr("Enter a valid year.")
                else -> newData[FilePropertyKey.YEAR] = trimmed.toLong()
            }
        }

        fun putTrack(value: String) {
            val trimmed = value.trim()
            when {
                trimmed.isEmpty() -> newData[FilePropertyKey.TRACK_NUMBER] = ""
                trimmed.toIntOrNull() == null -> fieldErrors[FilePropertyKey.TRACK_NUMBER] = tr("Enter a valid track number.")
                else -> newData[FilePropertyKey.TRACK_NUMBER] = trimmed
            }
        }

        when (item.category) {
            Category.AUDIO -> {
                putText(FilePropertyKey.TITLE, draft.title)
                putText(FilePropertyKey.AUTHOR, draft.author)
                putText(FilePropertyKey.ALBUM, draft.album)
                putText(FilePropertyKey.GENRE, draft.genre)
                putYear(draft.year)
                putTrack(draft.track)
                putText(FilePropertyKey.DESCRIPTION, draft.description)
            }

            Category.VIDEO -> {
                putText(FilePropertyKey.TITLE, draft.title)
                putText(FilePropertyKey.GENRE, draft.genre)
                putText(FilePropertyKey.RATING, draft.rating)
                putYear(draft.year)
                putText(FilePropertyKey.DESCRIPTION, draft.description)
            }

            Category.IMAGE -> {
                putText(FilePropertyKey.TITLE, draft.title)
                putText(FilePropertyKey.DESCRIPTION, draft.description)
            }

            Category.DOCUMENT -> {
                putText(FilePropertyKey.AUTHOR, draft.author)
                putText(FilePropertyKey.DESCRIPTION, draft.description)
            }

            Category.PROGRAM -> {
                putText(FilePropertyKey.TITLE, draft.title)
                putText(FilePropertyKey.PLATFORM, draft.platform)
                putText(FilePropertyKey.COMPANY, draft.company)
            }

            Category.OTHER,
            Category.TORRENT -> Unit
        }

        if (fieldErrors.isNotEmpty()) {
            return LibraryMetadataSaveResult(fieldErrors = fieldErrors)
        }

        return try {
            metaDataManager.save(item, newData)
            refreshLibraryFileInfoDialog(item.file, item)
            showNotice(
                tr("Metadata"),
                tr("Metadata updated for {0}.", item.fileName),
                OperationNoticeLevel.SUCCESS
            )
            LibraryMetadataSaveResult(saved = true)
        } catch (_: MetaDataException) {
            val message = tr("Unable to save metadata changes.")
            showNotice(tr("Metadata"), message, OperationNoticeLevel.ERROR)
            LibraryMetadataSaveResult(dialogError = message)
        }
    }

    fun librarySharingMemberships(item: LocalFileItem): List<LibrarySharingMembershipPresentation> {
        return sharedLists
            .asSequence()
            .filter { it.contains(item.file) && (it.isPublic || it.friendIds.isNotEmpty()) }
            .sortedWith(compareBy<SharedFileList>({ !it.isPublic }, { it.collectionName.lowercase(Locale.US) }))
            .map { list ->
                val friendCount = list.friendIds.size
                LibrarySharingMembershipPresentation(
                    listId = list.id,
                    label = list.collectionName,
                    subtitle = if (list.isPublic) {
                        tr("Public collection")
                    } else if (friendCount == 1) {
                        tr("Shared with 1 friend")
                    } else {
                        tr("Shared with {0} friends", friendCount)
                    },
                    publicCollection = list.isPublic,
                    ed2kPublished = list.isPublic
                )
            }
            .toList()
    }

    fun confirmRemoveLibraryItemFromSharingList(item: LocalFileItem, listId: Int) {
        val list = sharedLists.firstOrNull { it.id == listId } ?: return
        if (!settingsService.confirmRemoveFileInfoSharingEnabled()) {
            list.removeFile(item.file)
            refreshLibraryFileInfoDialog(item.file, item)
            showNotice(
                tr("Sharing"),
                tr("\"{0}\" was removed from \"{1}\".", item.fileName, list.collectionName),
                OperationNoticeLevel.SUCCESS
            )
            return
        }
        confirmationDialog = ConfirmationDialogState(
            title = tr("Remove From Collection"),
            message = tr("Remove \"{0}\" from \"{1}\"?", item.fileName, list.collectionName),
            confirmLabel = tr("Remove"),
            checkboxLabel = tr("Don’t ask again"),
            onConfirmWithCheckbox = { suppressPrompt ->
                confirmationDialog = null
                settingsService.setConfirmRemoveFileInfoSharingEnabled(!suppressPrompt)
                list.removeFile(item.file)
                refreshLibraryFileInfoDialog(item.file, item)
                showNotice(
                    tr("Sharing"),
                    tr("\"{0}\" was removed from \"{1}\".", item.fileName, list.collectionName),
                    OperationNoticeLevel.SUCCESS
                )
            },
            onConfirm = {
                confirmationDialog = null
                list.removeFile(item.file)
                refreshLibraryFileInfoDialog(item.file, item)
                showNotice(
                    tr("Sharing"),
                    tr("\"{0}\" was removed from \"{1}\".", item.fileName, list.collectionName),
                    OperationNoticeLevel.SUCCESS
                )
            },
            onDismiss = { confirmationDialog = null }
        )
    }

    fun showDownloadFileInfo(item: DownloadItem? = selectedDownloadItem()) {
        val resolvedItem = item ?: return
        downloadFileInfoDialog = DownloadFileInfoDialogState(resolvedItem)
    }

    fun closeDownloadFileInfoDialog() {
        downloadFileInfoDialog = null
    }

    fun showUploadFileInfo(item: UploadItem? = selectedUploadItem()) {
        val resolvedItem = item ?: return
        uploadFileInfoDialog = UploadFileInfoDialogState(resolvedItem)
    }

    fun closeUploadFileInfoDialog() {
        uploadFileInfoDialog = null
    }

    fun libraryMagnetLink(item: LocalFileItem): String? {
        if (!item.isShareable) {
            return null
        }
        return magnetLinkFactory.createMagnetLink(item).takeIf(String::isNotBlank)
    }

    fun searchResultMagnetLink(result: GroupedSearchResult): String? {
        return result.searchResults.firstOrNull()?.magnetURL?.takeIf(String::isNotBlank)
    }

    fun downloadMagnetLink(item: DownloadItem): String? {
        val launchableFile = item.launchableFile ?: return null
        return findLibraryItem(launchableFile)?.let(::libraryMagnetLink)
    }

    fun uploadMagnetLink(item: UploadItem): String? {
        return findLibraryItem(item.file)?.let(::libraryMagnetLink)
    }

    fun libraryItemIdentity(item: LocalFileItem): FileIdentityPresentation {
        return fileAppearanceService.presentation(item)
    }

    fun searchResultIdentity(result: GroupedSearchResult): FileIdentityPresentation {
        return searchResultPresentation(result).identity
    }

    private fun buildSearchResultIdentity(result: GroupedSearchResult): FileIdentityPresentation {
        val primary = result.searchResults.firstOrNull()
        return if (primary != null) {
            fileAppearanceService.presentation(primary)
        } else {
            val category = categoryManager.getCategoryForFilename(result.fileName)
            FileIdentityPresentation(
                title = result.fileName,
                subtitle = category.getSingularName(),
                icon = fileAppearanceService.iconForFileName(result.fileName, category)
            )
        }
    }

    fun downloadItemIdentity(item: DownloadItem): FileIdentityPresentation {
        return fileAppearanceService.presentation(item)
    }

    fun uploadItemIdentity(item: UploadItem): FileIdentityPresentation {
        return fileAppearanceService.presentation(item)
    }

    fun currentPlayerIdentity(): FileIdentityPresentation? {
        val file = playerCurrentFile ?: return null
        return fileAppearanceService.presentation(
            file = file,
            category = categoryManager.getCategoryForFile(file),
            title = playerTrackName,
            subtitle = file.name
        )
    }

    fun playerQueueEntryIdentity(entry: PlayerQueueEntry): FileIdentityPresentation {
        return fileAppearanceService.presentation(
            file = entry.file,
            category = categoryManager.getCategoryForFile(entry.file),
            title = entry.title,
            subtitle = entry.file.name
        )
    }

    fun fileProcessingIdentity(status: FileProcessingStatus): FileIdentityPresentation {
        val title = status.currentFileName ?: if (status.done) tr("All completed") else tr("Scanning…")
        val subtitle = if (status.done) {
            tr("{0} {1} processed.", status.total, if (status.total == 1) "file" else "files")
        } else {
            tr("{0} completed of {1} queued.", status.finished, status.total)
        }
        return FileIdentityPresentation(
            title = title,
            subtitle = subtitle,
            icon = fileAppearanceService.iconForFileName(status.currentFileName, status.currentCategory)
        )
    }

    fun categoryIcon(category: Category?): FileIconPresentation {
        return fileAppearanceService.iconForCategory(category)
    }

    fun torrentDetailsPresentation(torrent: Torrent?): TorrentDetailsPresentation? {
        val resolvedTorrent = torrent ?: return null
        resolvedTorrent.lock.lock()
        return try {
            val entries = resolvedTorrent.getTorrentFileEntries().map { entry ->
                TorrentFileEntryPresentation(
                    index = entry.index,
                    path = entry.path,
                    size = entry.size,
                    totalDone = entry.totalDone,
                    progress = entry.progress,
                    priority = TorrentFilePriority.fromValue(entry.priority),
                    localPath = resolvedTorrent.getTorrentDataFile(entry)?.absolutePath
                )
            }
            TorrentDetailsPresentation(
                torrent = resolvedTorrent,
                editable = resolvedTorrent.isEditable,
                privateTorrent = resolvedTorrent.isPrivate,
                valid = resolvedTorrent.isValid,
                fileCount = entries.size,
                trackers = resolvedTorrent.getTrackers().mapIndexed { index, tracker ->
                    TorrentTrackerPresentation(
                        index = index,
                        uri = tracker.uri.toString(),
                        tier = tracker.tier,
                        removable = index != 0
                    )
                },
                entries = entries
            )
        } finally {
            resolvedTorrent.lock.unlock()
        }
    }

    fun updateTorrentFileEntryPriority(torrent: Torrent, entryIndex: Int, priority: TorrentFilePriority) {
        if (!torrent.isEditable || !torrent.isValid) {
            return
        }
        torrent.lock.lock()
        try {
            val entry = torrent.getTorrentFileEntries().firstOrNull { it.index == entryIndex } ?: return
            torrent.setTorrenFileEntryPriority(entry, priority.value)
        } finally {
            torrent.lock.unlock()
        }
    }

    fun torrentManagementDraft(torrent: Torrent?): TorrentManagementDraft? {
        val resolvedTorrent = torrent ?: return null
        val defaultRatio = BittorrentSettings.LIBTORRENT_SEED_RATIO_LIMIT.getValue().toFloat()
        val defaultTime = BittorrentSettings.LIBTORRENT_SEED_TIME_LIMIT.getValue().toInt()
        val defaultDownloadBandwidth = DownloadSettings.MAX_DOWNLOAD_SPEED.getValue().toInt() / 1024
        val defaultUploadBandwidth = UploadSettings.MAX_UPLOAD_SPEED.getValue().toInt() / 1024
        val minDownloadBandwidth = DownloadSettings.MAX_DOWNLOAD_SPEED.getMinValue().toInt() / 1024
        val minUploadBandwidth = UploadSettings.MAX_UPLOAD_SPEED.getMinValue().toInt() / 1024
        resolvedTorrent.lock.lock()
        return try {
            val ratio = resolvedTorrent.getProperty(LimeWireTorrentProperties.MAX_SEED_RATIO_LIMIT, -1f)
            val time = resolvedTorrent.getProperty(LimeWireTorrentProperties.MAX_SEED_TIME_RATIO_LIMIT, -1)
            val seedMode = when {
                ratio == Float.MAX_VALUE && time == Int.MAX_VALUE -> TorrentSeedMode.FOREVER
                (ratio >= 0f && ratio != defaultRatio) || (time >= 0 && time != defaultTime) -> TorrentSeedMode.CUSTOM
                else -> TorrentSeedMode.DEFAULT
            }
            val resolvedRatio = when {
                ratio >= 0f && ratio != Float.MAX_VALUE -> ratio
                else -> defaultRatio
            }
            val resolvedTime = when {
                time >= 0 && time != Int.MAX_VALUE -> time
                else -> defaultTime
            }
            val days = wholeDays(resolvedTime)
            val hours = remainderHours(resolvedTime, days)
            val maxDownloadBandwidth = resolvedTorrent.maxDownloadBandwidth / 1024
            val maxUploadBandwidth = resolvedTorrent.maxUploadBandwidth / 1024
            val limitDownloadBandwidth = maxDownloadBandwidth >= minDownloadBandwidth &&
                maxDownloadBandwidth != defaultDownloadBandwidth
            val limitUploadBandwidth = maxUploadBandwidth >= minUploadBandwidth &&
                maxUploadBandwidth != defaultUploadBandwidth
            TorrentManagementDraft(
                seedMode = seedMode,
                seedRatio = formatTorrentRatioInput(resolvedRatio),
                seedDays = days.toString(),
                seedHours = hours.toString(),
                limitDownloadBandwidth = limitDownloadBandwidth,
                maxDownloadBandwidth = (if (limitDownloadBandwidth) maxDownloadBandwidth else defaultDownloadBandwidth).toString(),
                limitUploadBandwidth = limitUploadBandwidth,
                maxUploadBandwidth = (if (limitUploadBandwidth) maxUploadBandwidth else defaultUploadBandwidth).toString()
            )
        } finally {
            resolvedTorrent.lock.unlock()
        }
    }

    fun applyTorrentManagementSettings(torrent: Torrent, draft: TorrentManagementDraft): String? {
        if (!torrent.isEditable || !torrent.isValid) {
            return tr("This torrent can no longer be edited.")
        }

        val ratio = when (draft.seedMode) {
            TorrentSeedMode.DEFAULT, TorrentSeedMode.FOREVER -> null
            TorrentSeedMode.CUSTOM -> parseBoundedFloat(
                value = draft.seedRatio,
                min = BittorrentSettings.LIBTORRENT_SEED_RATIO_LIMIT.getMinValue().toFloat(),
                max = BittorrentSettings.LIBTORRENT_SEED_RATIO_LIMIT.getMaxValue().toFloat(),
                label = tr("Ratio")
            )
        } ?: return tr("Enter a valid ratio.")

        val seedTimeSeconds = when (draft.seedMode) {
            TorrentSeedMode.DEFAULT, TorrentSeedMode.FOREVER -> null
            TorrentSeedMode.CUSTOM -> parseSeedTimeSeconds(draft.seedDays, draft.seedHours)
        } ?: return tr("Enter a valid day and hour limit.")

        val maxDownloadBandwidth = if (draft.limitDownloadBandwidth) {
            parseBoundedInt(
                value = draft.maxDownloadBandwidth,
                min = DownloadSettings.MAX_DOWNLOAD_SPEED.getMinValue().toInt() / 1024,
                max = effectiveDownloadBandwidthMaxKiB(),
                label = tr("Download bandwidth")
            ) ?: return tr("Enter a valid download bandwidth.")
        } else {
            0
        }

        val maxUploadBandwidth = if (draft.limitUploadBandwidth) {
            parseBoundedInt(
                value = draft.maxUploadBandwidth,
                min = UploadSettings.MAX_UPLOAD_SPEED.getMinValue().toInt() / 1024,
                max = effectiveUploadBandwidthMaxKiB(),
                label = tr("Upload bandwidth")
            ) ?: return tr("Enter a valid upload bandwidth.")
        } else {
            0
        }

        return runCatching {
            torrent.lock.lock()
            try {
                when (draft.seedMode) {
                    TorrentSeedMode.DEFAULT -> {
                        torrent.setProperty(LimeWireTorrentProperties.MAX_SEED_RATIO_LIMIT, null)
                        torrent.setProperty(LimeWireTorrentProperties.MAX_SEED_TIME_RATIO_LIMIT, null)
                    }
                    TorrentSeedMode.FOREVER -> {
                        torrent.setProperty(LimeWireTorrentProperties.MAX_SEED_RATIO_LIMIT, Float.MAX_VALUE)
                        torrent.setProperty(LimeWireTorrentProperties.MAX_SEED_TIME_RATIO_LIMIT, Int.MAX_VALUE)
                    }
                    TorrentSeedMode.CUSTOM -> {
                        torrent.setProperty(LimeWireTorrentProperties.MAX_SEED_RATIO_LIMIT, ratio)
                        torrent.setProperty(LimeWireTorrentProperties.MAX_SEED_TIME_RATIO_LIMIT, seedTimeSeconds)
                    }
                }
                torrent.setMaxDownloadBandwidth(maxDownloadBandwidth * 1024)
                torrent.setMaxUploadBandwidth(maxUploadBandwidth * 1024)
            } finally {
                torrent.lock.unlock()
            }
            showNotice(
                tr("Torrent Settings"),
                tr("Torrent controls were updated."),
                OperationNoticeLevel.SUCCESS
            )
            null
        }.getOrElse { failure ->
            failure.message ?: tr("WireShare could not update this torrent.")
        }
    }

    fun addTorrentTracker(torrent: Torrent, url: String, tierText: String): String? {
        if (!torrent.isEditable || !torrent.isValid) {
            return tr("This torrent can no longer be edited.")
        }
        val trimmedUrl = url.trim()
        if (trimmedUrl.isEmpty()) {
            return tr("Enter a tracker URL.")
        }
        try {
            URL(trimmedUrl)
        } catch (_: MalformedURLException) {
            return tr("Tracker URL Invalid")
        }
        val tier = parseBoundedInt(
            value = tierText,
            min = 0,
            max = 20,
            label = tr("Tier")
        ) ?: return tr("Enter a valid tracker tier.")

        torrent.lock.lock()
        return try {
            val duplicate = torrent.getTrackers().any { it.uri.toString().equals(trimmedUrl, ignoreCase = true) }
            if (duplicate) {
                return tr("That tracker is already listed.")
            }
            torrent.addTracker(trimmedUrl, tier)
            showNotice(
                tr("Trackers"),
                tr("Tracker added."),
                OperationNoticeLevel.SUCCESS
            )
            null
        } catch (failure: RuntimeException) {
            failure.message ?: tr("WireShare could not add that tracker.")
        } finally {
            torrent.lock.unlock()
        }
    }

    fun removeTorrentTracker(torrent: Torrent, tracker: TorrentTrackerPresentation): String? {
        if (!torrent.isEditable || !torrent.isValid) {
            return tr("This torrent can no longer be edited.")
        }
        if (!tracker.removable) {
            return tr("The first tracker cannot be removed.")
        }
        return runCatching {
            torrent.removeTracker(tracker.uri, tracker.tier)
            showNotice(
                tr("Trackers"),
                tr("Tracker removed."),
                OperationNoticeLevel.SUCCESS
            )
            null
        }.getOrElse { failure ->
            failure.message ?: tr("WireShare could not remove that tracker.")
        }
    }

    fun torrentActivityPresentation(torrent: Torrent?): TorrentActivityPresentation? {
        val resolvedTorrent = torrent ?: return null
        resolvedTorrent.lock.lock()
        return try {
            val status = resolvedTorrent.status
            val peers = resolvedTorrent.getTorrentPeers().map { peer ->
                TorrentPeerPresentation(
                    address = peer.ipAddress,
                    encrypted = peer.isEncrypted,
                    client = peer.clientName ?: tr("Unknown"),
                    uploadRateBytesPerSecond = peer.uploadSpeed,
                    downloadRateBytesPerSecond = peer.downloadSpeed
                )
            }
            val currentTracker = status.currentTracker?.takeIf(String::isNotBlank)
            val seeders = status.numComplete.takeIf { it >= 0 }
            val leechers = status.numIncomplete.takeIf { it >= 0 }
            val meaningful = peers.isNotEmpty() ||
                currentTracker != null ||
                seeders != null ||
                leechers != null ||
                status.downloadRate > 0f ||
                status.uploadRate > 0f
            if (!meaningful) {
                null
            } else {
                TorrentActivityPresentation(
                    seeders = seeders,
                    leechers = leechers,
                    currentTracker = currentTracker,
                    peerCount = peers.size,
                    uploadRateBytesPerSecond = status.uploadRate,
                    downloadRateBytesPerSecond = status.downloadRate,
                    peers = peers
                )
            }
        } catch (_: RuntimeException) {
            null
        } finally {
            resolvedTorrent.lock.unlock()
        }
    }

    fun torrentPiecesPresentation(item: DownloadItem?): TorrentPiecesPresentation? {
        val resolvedItem = item ?: return null
        val torrent = resolvedItem.getProperty(FilePropertyKey.TORRENT) as? Torrent ?: return null
        val piecesInfo = resolvedItem.piecesInfo ?: return null
        val totalPieces = piecesInfo.numPieces
        if (totalPieces <= 0) {
            return null
        }
        val maxCells = 100
        val piecesPerCell = kotlin.math.ceil(totalPieces.toDouble() / maxCells.toDouble()).toInt().coerceAtLeast(1)
        val cellCount = kotlin.math.ceil(totalPieces.toDouble() / piecesPerCell.toDouble()).toInt()
        val cells = (0 until cellCount).map { cellIndex ->
            val startIndex = cellIndex * piecesPerCell
            val piecesToCoalesce = minOf(piecesPerCell, totalPieces - startIndex)
            coalescePieceStates(piecesInfo, startIndex, piecesToCoalesce)
        }
        return TorrentPiecesPresentation(
            totalPieces = totalPieces,
            completedPieces = piecesInfo.numPiecesCompleted.takeIf { it >= 0 },
            pieceSize = piecesInfo.pieceSize,
            downloaded = resolvedItem.currentSize,
            verifiedDownloaded = resolvedItem.amountVerified,
            failedDownload = resolvedItem.amountLost,
            uploaded = torrent.totalUploaded,
            ratio = torrent.seedRatio,
            piecesPerCell = piecesPerCell,
            cells = cells
        )
    }

    fun torrentInspectorSnapshot(
        torrent: Torrent?,
        downloadItem: DownloadItem? = null,
        refreshEpoch: Int = 0,
        includeActivity: Boolean,
        includePieces: Boolean,
        force: Boolean = false
    ): TorrentInspectorLiveState {
        val key = TorrentInspectorSnapshotKey(
            torrentSha1 = torrent?.sha1,
            downloadUrn = downloadItem?.urn?.toString(),
            includeActivity = includeActivity,
            includePieces = includePieces
        )
        val now = System.currentTimeMillis()
        val cached = torrentInspectorSnapshots[key]
        if (!force && cached != null && cached.refreshEpoch == refreshEpoch && now - cached.refreshedAt < 1500L) {
            return cached.state
        }
        val state = ComposePerformanceTracker.measure("torrentInspector.snapshot") {
            TorrentInspectorLiveState(
                details = torrentDetailsPresentation(torrent),
                activity = if (includeActivity) torrentActivityPresentation(torrent) else null,
                pieces = if (includePieces) torrentPiecesPresentation(downloadItem) else null
            )
        }
        torrentInspectorSnapshots[key] = CachedTorrentInspectorSnapshot(
            refreshEpoch = refreshEpoch,
            refreshedAt = now,
            state = state
        )
        if (torrentInspectorSnapshots.size > 24) {
            val oldestKey = torrentInspectorSnapshots.minByOrNull { it.value.refreshedAt }?.key
            if (oldestKey != null) {
                torrentInspectorSnapshots.remove(oldestKey)
            }
        }
        return state
    }

    fun renameLibraryFileFromInfo(item: LocalFileItem, value: String): String? {
        return renameLibraryFile(item, value)
    }

    fun libraryListTargetsForItem(item: LocalFileItem): List<LibraryJumpTarget> {
        return libraryJumpTargetsForFile(item.file, excludeSectionId = selectedLibrarySectionId)
    }

    fun collectionTargetsForItem(item: LocalFileItem): List<LibraryJumpTarget> {
        return libraryListTargetsForItem(item).filter { it.sectionId != DEFAULT_LIBRARY_SECTION_ID }
    }

    fun showLibraryItemInTarget(item: LocalFileItem, target: LibraryJumpTarget) {
        showFileInTarget(item.file, target, item.category)
    }

    fun libraryJumpTargetsForFile(file: File): List<LibraryJumpTarget> {
        return libraryJumpTargetsForFile(file, excludeSectionId = null)
    }

    fun currentPlayerJumpTargets(): List<LibraryJumpTarget> {
        return playerCurrentFile?.let(::libraryJumpTargetsForFile).orEmpty()
    }

    fun showCurrentPlayerInMyFilesOrPlayer() {
        val myFilesTarget = currentPlayerJumpTargets().firstOrNull { it.sectionId == DEFAULT_LIBRARY_SECTION_ID }
        if (myFilesTarget != null) {
            showCurrentPlayerInTarget(myFilesTarget)
        } else {
            selectPlayer()
        }
    }

    fun showCurrentPlayerInTarget(target: LibraryJumpTarget) {
        playerCurrentFile?.let { showFileInTarget(it, target) }
    }

    fun canPreviousTrack(): Boolean = previousPlayerQueueIndex() >= 0

    fun canNextTrack(): Boolean = nextPlayerQueueIndex() >= 0

    fun recentDownloadJumpTargets(file: File): List<LibraryJumpTarget> {
        return if (file.exists()) libraryJumpTargetsForFile(file) else emptyList()
    }

    fun showRecentDownloadInTarget(file: File, target: LibraryJumpTarget) {
        if (file.exists()) {
            showFileInTarget(file, target)
        }
    }

    fun playerQueueEntryJumpTargets(entry: PlayerQueueEntry): List<LibraryJumpTarget> {
        return libraryJumpTargetsForFile(entry.file)
    }

    fun showPlayerQueueEntryInTarget(entry: PlayerQueueEntry, target: LibraryJumpTarget) {
        showFileInTarget(entry.file, target)
    }

    fun selectLibraryItem(
        item: LocalFileItem,
        extendSelection: Boolean = false,
        toggleSelection: Boolean = false
    ) {
        val result = computeSelectionUpdate(
            visibleKeys = visibleLibraryItems().map { it.file.absolutePath },
            currentSelection = selectedLibraryItemPaths,
            currentPrimary = selectedLibraryItemPath,
            currentAnchor = librarySelectionAnchorPath,
            targetKey = item.file.absolutePath,
            extendSelection = extendSelection,
            toggleSelection = toggleSelection
        )
        applyLibrarySelection(result.selectedKeys, result.primaryKey, result.anchorKey)
        rememberLibrarySectionState()
    }

    fun toggleLibraryItemChecked(item: LocalFileItem) {
        selectLibraryItem(item, toggleSelection = true)
    }

    fun selectAllVisibleLibraryItems() {
        val keys = visibleLibraryItems().map { it.file.absolutePath }
        val primary = selectedLibraryItemPath?.takeIf { it in keys } ?: keys.firstOrNull()
        applyLibrarySelection(keys, primary, primary)
        rememberLibrarySectionState()
    }

    fun clearLibrarySelection() {
        applyLibrarySelection(emptyList(), null, null)
        invalidateLibrarySelectionCache()
        rememberLibrarySectionState()
    }

    fun handleLibraryContextSelection(item: LocalFileItem) {
        if (item.file.absolutePath !in selectedLibraryItemPaths) {
            selectLibraryItem(item)
        }
    }

    fun clearLibraryFilters() {
        updateLibraryFilterText("")
        selectLibraryCategory(null)
    }

    fun toggleLibraryFiltersVisible() {
        libraryFiltersVisible = !libraryFiltersVisible
        if (!libraryFiltersVisible) {
            clearAllLibraryFilters()
        }
        persistLibraryPaneLayoutPreferences()
    }

    fun toggleLibrarySort(mode: LibrarySortMode) {
        if (librarySortMode == mode) {
            librarySortDescending = !librarySortDescending
        } else {
            librarySortMode = mode
            librarySortDescending = false
        }
        invalidateLibraryVisibilityCache()
        syncLibrarySectionLayouts()
        saveLibraryLayoutPreferences()
        rememberLibrarySectionState()
        syncLiveLibraryQueueIfNeeded()
    }

    fun updateLibraryFilterText(value: String) {
        libraryFilterText = value
        invalidateLibraryVisibilityCache()
        rememberLibrarySectionState()
        syncLiveLibraryQueueIfNeeded()
    }

    fun selectLibraryCategory(category: Category?) {
        libraryCategoryFilter = category
        invalidateLibraryVisibilityCache()
        if (visibleLibraryColumns.all { it in LEGACY_LIBRARY_COLUMNS }) {
            visibleLibraryColumns = visibleLibraryColumns + defaultAdditionalLibraryColumns(category)
            syncLibrarySectionLayouts()
            saveLibraryLayoutPreferences()
        }
        rememberLibrarySectionState()
        syncLiveLibraryQueueIfNeeded()
    }

    fun moveLibrarySelection(delta: Int) {
        val items = visibleLibraryItems()
        if (items.isEmpty()) {
            selectedLibraryItemPath = null
            selectedLibraryItemPaths.clear()
            librarySelectionAnchorPath = null
            rememberLibrarySectionState()
            return
        }
        val currentIndex = items.indexOfFirst { it.file.absolutePath == selectedLibraryItemPath }
        val nextIndex = when {
            currentIndex < 0 -> 0
            else -> (currentIndex + delta).coerceIn(0, items.lastIndex)
        }
        selectLibraryItem(items[nextIndex])
    }

    fun extendLibrarySelection(delta: Int) {
        val items = visibleLibraryItems()
        if (items.isEmpty()) {
            return
        }
        val currentIndex = items.indexOfFirst { it.file.absolutePath == selectedLibraryItemPath }
        val nextIndex = when {
            currentIndex < 0 -> 0
            else -> (currentIndex + delta).coerceIn(0, items.lastIndex)
        }
        selectLibraryItem(items[nextIndex], extendSelection = true)
    }

    fun activateSelectedLibraryItem() {
        openSelectedLibraryItems()
    }

    fun toggleLibraryColumn(column: LibraryColumn) {
        visibleLibraryColumns = toggledColumns(visibleLibraryColumns, column)
        syncLibrarySectionLayouts()
        saveLibraryLayoutPreferences()
        rememberLibrarySectionState()
    }

    fun canRenameCurrentSharedList(): Boolean = currentSharedList()?.isNameChangeAllowed == true

    fun showCreateSharedListDialog() {
        textEntryDialog = TextEntryDialogState(
            title = tr("New Collection"),
            label = tr("Collection name"),
            confirmLabel = tr("Create"),
            validator = { name ->
                if (name.trim().isEmpty()) tr("Enter a collection name to continue.") else null
            },
            onConfirm = { name ->
                val trimmed = name.trim()
                val id = sharedFileListManager.createNewSharedFileList(trimmed)
                selectLibrarySection("shared:$id")
                null
            }
        )
    }

    fun showRenameSharedListDialog() {
        val list = currentSharedList() ?: return
        textEntryDialog = TextEntryDialogState(
            title = tr("Rename Collection"),
            label = tr("Collection name"),
            initialValue = list.collectionName,
            confirmLabel = tr("Rename"),
            validator = { name ->
                if (name.trim().isEmpty()) tr("Enter a collection name to continue.") else null
            },
            onConfirm = { name ->
                val trimmed = name.trim()
                list.collectionName = trimmed
                rebuildLibrarySections()
                null
            }
        )
    }

    fun confirmDeleteSharedList() {
        val list = currentSharedList() ?: return
        confirmationDialog = ConfirmationDialogState(
            title = tr("Delete Collection"),
            message = tr("Delete the collection \"{0}\"?", list.collectionName),
            confirmLabel = tr("Delete"),
            onConfirm = {
                confirmationDialog = null
                sharedFileListManager.deleteSharedFileList(list)
                selectLibrarySection(DEFAULT_LIBRARY_SECTION_ID)
            },
            onDismiss = { confirmationDialog = null }
        )
    }

    fun openCurrentSharedListSharingWorkspace() {
        val list = currentSharedList() ?: return
        sharedListSharingEditMode = true
        sharedListSharingFilter = ""
        sharedListSharingSelectedIds.clear()
        sharedListSharingSelectedIds.addAll(list.friendIds.distinct())
    }

    fun cancelCurrentSharedListSharingEdit() {
        sharedListSharingEditMode = false
        sharedListSharingFilter = ""
        sharedListSharingSelectedIds.clear()
    }

    fun applyCurrentSharedListSharingEdit() {
        val list = currentSharedList() ?: return
        val selectedIds = sharedListSharingSelectedIds
            .map(String::trim)
            .filter(String::isNotEmpty)
            .distinct()
            .sortedBy { friendService.rosterItem(it)?.displayName?.lowercase(Locale.US) ?: it.lowercase(Locale.US) }
        list.setFriendList(selectedIds)
        friendCollectionShareEpoch += 1
        invalidateSharingSummaryCache()
        sharedListSharingEditMode = false
        sharedListSharingFilter = ""
        sharedListSharingSelectedIds.clear()
        showNotice(
            tr("Collections"),
            if (selectedIds.isEmpty()) {
                tr("Stopped sharing \"{0}\" with friends.", list.collectionName)
            } else {
                tr("Updated sharing for \"{0}\".", list.collectionName)
            },
            OperationNoticeLevel.SUCCESS
        )
    }

    fun stopSharingCurrentSharedList() {
        val list = currentSharedList() ?: return
        list.setFriendList(emptyList())
        friendCollectionShareEpoch += 1
        invalidateSharingSummaryCache()
        cancelCurrentSharedListSharingEdit()
        showNotice(
            tr("Collections"),
            tr("Stopped sharing \"{0}\" with friends.", list.collectionName),
            OperationNoticeLevel.INFO
        )
    }

    fun updateCurrentSharedListSharingFilter(value: String) {
        sharedListSharingFilter = value
    }

    fun toggleCurrentSharedListFriendSelection(friendId: String) {
        if (friendId in sharedListSharingSelectedIds) {
            sharedListSharingSelectedIds.remove(friendId)
        } else {
            sharedListSharingSelectedIds.add(friendId)
        }
    }

    fun setCurrentSharedListUnknownSelection(selected: Boolean) {
        val unknownIds = currentSharedListUnknownIds()
        if (selected) {
            unknownIds.forEach { unknownId ->
                if (unknownId !in sharedListSharingSelectedIds) {
                    sharedListSharingSelectedIds.add(unknownId)
                }
            }
        } else {
            sharedListSharingSelectedIds.removeAll(unknownIds.toSet())
        }
    }

    fun selectAllCurrentSharedListSharingRows() {
        currentSharedListSharingEditorRows().forEach { row ->
            if (row.id != null) {
                if (row.id !in sharedListSharingSelectedIds) {
                    sharedListSharingSelectedIds.add(row.id)
                }
            } else {
                setCurrentSharedListUnknownSelection(true)
            }
        }
    }

    fun clearCurrentSharedListSharingRows() {
        currentSharedListSharingEditorRows().forEach { row ->
            if (row.id != null) {
                sharedListSharingSelectedIds.remove(row.id)
            } else {
                setCurrentSharedListUnknownSelection(false)
            }
        }
    }

    fun currentSharedListSharingButtonLabel(): String {
        val hasFriends = currentSharedList()?.friendIds?.isNotEmpty() == true
        return if (hasFriends) tr("Edit Sharing") else tr("Share with Friends")
    }

    fun currentSharedListSharingSignedIn(): Boolean =
        friendConnectionState == FriendConnectionEvent.Type.CONNECTED

    fun currentSharedListSharingBusy(): Boolean =
        friendConnectionState == FriendConnectionEvent.Type.CONNECTING || friendLoginBusy

    fun currentSharedListEd2kAssociationLabel(): String {
        val list = currentSharedList()
        return when {
            list == null -> tr("ED2K/Kad publishing")
            list.isPublic -> tr("Published to ED2K/Kad")
            else -> tr("Not published to ED2K/Kad")
        }
    }

    fun currentSharedListEd2kAssociationBody(): String {
        val list = currentSharedList()
        return when {
            list == null -> tr("ED2K/Kad publishing follows Public Shared collections.")
            list.isPublic -> tr("This public collection is published to ED2K/Kad automatically.")
            else -> tr("ED2K/Kad only follows public shared collections right now. Friends-only collections stay off that network.")
        }
    }

    fun currentSharedListEd2kPublished(): Boolean =
        currentSharedList()?.isPublic == true

    fun currentSharedListSharingSummaryItems(): List<SharedListSharingSummaryItem> {
        val items = sharedListFriendIds
            .mapNotNull { friendId ->
                friendService.rosterItem(friendId)?.let { friend ->
                    SharedListSharingSummaryItem(label = friend.displayName, removableIds = listOf(friendId))
                }
            }
            .sortedBy { it.label.lowercase(Locale.US) }
            .toMutableList()
        val unknownIds = currentSharedListUnknownIds(sharedListFriendIds)
        if (unknownIds.isNotEmpty()) {
            items += SharedListSharingSummaryItem(
                label = tr("{0} friends from other accounts", unknownIds.size),
                removableIds = unknownIds
            )
        }
        return items
    }

    fun currentSharedListSharingEditorRows(): List<SharedListSharingEditorRow> {
        val filter = sharedListSharingFilter.trim().lowercase(Locale.US)
        val rows = chatFriends
            .sortedBy { it.displayName.lowercase(Locale.US) }
            .filter { friend ->
                filter.isEmpty() ||
                    friend.displayName.lowercase(Locale.US).contains(filter) ||
                    friend.id.lowercase(Locale.US).contains(filter)
            }
            .map { friend ->
                SharedListSharingEditorRow(
                    id = friend.id,
                    label = friend.displayName,
                    detail = friend.id.takeIf { it != friend.displayName },
                    selected = friend.id in sharedListSharingSelectedIds
                )
            }
            .toMutableList()
        val unknownIds = currentSharedListUnknownIds(sharedListSharingSelectedIds)
        if (unknownIds.isNotEmpty() && (filter.isEmpty() || tr("other accounts").lowercase(Locale.US).contains(filter))) {
            rows += SharedListSharingEditorRow(
                id = null,
                label = tr("{0} friends from other accounts", unknownIds.size),
                detail = tr("Preserve existing shares that are not in the current roster."),
                selected = unknownIds.all { it in sharedListSharingSelectedIds }
            )
        }
        return rows
    }

    private fun currentSharedListUnknownIds(sourceIds: Collection<String> = sharedListSharingSelectedIds): List<String> {
        return sourceIds
            .filter { friendService.rosterItem(it) == null }
            .distinct()
    }

    fun addFilesToCurrentLibrarySection() {
        val section = activeLibrarySection() ?: return
        val files = filePicker.chooseFiles(
            parent = windowRef,
            title = tr("Add Files"),
            multiple = true
        )
        if (files.isNotEmpty()) {
            importFilesIntoLibrarySection(section.id, files)
        }
    }

    fun canImportFilesIntoLibrarySection(
        sectionId: String,
        files: List<File>,
        sourceSectionId: String? = null
    ): Boolean {
        val section = librarySections.firstOrNull { it.id == sectionId } ?: return false
        if (sourceSectionId != null && sourceSectionId == sectionId) {
            return false
        }
        return files.any { file ->
            section.list.isDirectoryAllowed(file) || section.list.isFileAllowed(file)
        }
    }

    fun importFilesIntoLibrarySection(
        sectionId: String,
        files: List<File>,
        sourceSectionId: String? = null
    ) {
        val section = librarySections.firstOrNull { it.id == sectionId } ?: return
        if (!canImportFilesIntoLibrarySection(sectionId, files, sourceSectionId)) {
            return
        }
        selectLibrarySection(sectionId)
        val normalizedFiles = files.distinctBy { it.absolutePath }
        if (normalizedFiles.any { it.isDirectory }) {
            showLibraryFolderImportDialog(section, normalizedFiles)
        } else {
            addFilesToLocalFileList(section.list, normalizedFiles, section.title)
        }
    }

    fun downloadDroppedSearchResults(tabId: Long, resultKeys: List<String>) {
        val tab = searchTabs.firstOrNull { it.id == tabId } ?: return
        val keySet = resultKeys.toSet()
        val results = tab.results.filter { searchResultKeyOf(it) in keySet }
        if (results.isEmpty()) {
            return
        }
        startSearchDownloads(tab, results)
    }

    fun importCurrentLibrarySection() {
        val section = activeLibrarySection() ?: return
        val file = filePicker.chooseFiles(
            parent = windowRef,
            title = tr("Import List"),
            filenameFilter = FilenameFilter { _, name -> name.lowercase(Locale.US).endsWith(".m3u") }
        ).firstOrNull() ?: return
        val importResult = runCatching { ComposeM3uCodec.read(file) }
            .getOrElse {
                showNotice(
                    tr("Import List"),
                    it.message ?: tr("WireShare could not load that playlist."),
                    OperationNoticeLevel.ERROR
                )
                return
            }
        if (importResult.files.isNotEmpty()) {
            importFilesIntoLibrarySection(section.id, importResult.files)
        }
        val importSummary = buildString {
            if (importResult.importedCount > 0) {
                append(tr("Imported {0} entries from {1} into \"{2}\".", importResult.importedCount, file.name, section.title))
            } else {
                append(tr("WireShare did not find any importable entries in {0}.", file.name))
            }
            if (importResult.missingCount > 0 || importResult.skippedCount > 0) {
                append(" ")
                append(
                    tr(
                        "{0} missing, {1} skipped.",
                        importResult.missingCount,
                        importResult.skippedCount
                    )
                )
            }
        }
        showNotice(
            tr("Import List"),
            importSummary,
            if (importResult.missingCount > 0 || importResult.skippedCount > 0) {
                OperationNoticeLevel.WARNING
            } else {
                OperationNoticeLevel.SUCCESS
            }
        )
    }

    fun exportCurrentLibrarySection() {
        val section = activeLibrarySection() ?: return
        val suggestedName = buildM3uSuggestedName(section.title)
        val target = filePicker.chooseSaveFile(
            parent = windowRef,
            title = tr("Export List"),
            suggestedName = suggestedName,
            filenameFilter = FilenameFilter { _, name -> name.lowercase(Locale.US).endsWith(".m3u") }
        ) ?: return
        val output = if (target.name.lowercase(Locale.US).endsWith(".m3u")) target else File(target.parentFile, "${target.name}.m3u")
        val exportResult = runCatching {
            ComposeM3uCodec.write(output, section.list.getSwingModel().toList())
        }.onFailure {
            showNotice(
                tr("Export List"),
                it.message ?: tr("WireShare could not export that playlist."),
                OperationNoticeLevel.ERROR
            )
            return
        }.getOrThrow()
        val exportSummary = buildString {
            append(tr("Exported {0} entries from \"{1}\" to {2}.", exportResult.writtenCount, section.title, output.name))
            if (exportResult.skippedCount > 0) {
                append(" ")
                append(tr("{0} incomplete or non-local entries were skipped.", exportResult.skippedCount))
            }
        }
        showNotice(
            tr("Export List"),
            exportSummary,
            if (exportResult.skippedCount > 0) OperationNoticeLevel.WARNING else OperationNoticeLevel.SUCCESS
        )
    }

    fun clearCurrentLibrarySection() {
        val section = activeLibrarySection() ?: return
        confirmationDialog = ConfirmationDialogState(
            title = tr("Clear Files"),
            message = when {
                section.isShared && section.isPublic ->
                    tr("Remove all files from \"{0}\"? This will stop sharing those files with the world.", section.title)
                section.isShared ->
                    tr("Remove all files from \"{0}\"? This will stop sharing those files.", section.title)
                else ->
                    tr("Remove all files from your library? This will remove downloaded files from My Files and stop sharing them.")
            },
            confirmLabel = tr("Clear"),
            onConfirm = {
                confirmationDialog = null
                section.list.clear()
                clearLibrarySelection()
            },
            onDismiss = { confirmationDialog = null }
        )
    }

    fun openLibraryItem(item: LocalFileItem) {
        val file = item.file
        val category = categoryManager.getCategoryForFile(file)
        if (category == Category.AUDIO && playerService.isPlayable(file) && playerEnabled) {
            playLiveLibraryQueue(item)
        } else {
            openFileWithComposeFeedback(file)
        }
    }

    fun revealLibraryItem(item: LocalFileItem) {
        revealFileWithComposeFeedback(item.file)
    }

    fun openSelectedLibraryItems() {
        val items = selectedLibraryItems()
        if (items.isEmpty()) {
            return
        }
        if (items.size == 1) {
            openLibraryItem(items.first())
            return
        }
        val audioOnly = items.all {
            categoryManager.getCategoryForFile(it.file) == Category.AUDIO &&
                playerService.isPlayable(it.file) &&
                playerEnabled
        }
        if (audioOnly) {
            playLibraryQueue(
                items = items,
                currentItem = items.first(),
                sourceLabel = tr("Selected in {0}", activeLibrarySection()?.title ?: tr("My Files"))
            )
            return
        }
        items.forEach { openFileWithComposeFeedback(it.file) }
    }

    fun revealSelectedLibraryItems() {
        selectedLibraryItems().forEach { revealFileWithComposeFeedback(it.file) }
    }

    fun addSelectedLibraryItemsToCollection(collectionId: Int) {
        val collection = sharedLists.firstOrNull { it.id == collectionId } ?: return
        addFilesToCollection(
            collection = collection,
            files = selectedLibraryItems().map { it.file },
            sourceLabel = tr("My Files")
        )
    }

    fun locateLibraryItem(item: LocalFileItem) {
        selectLibrarySection(preferredSectionIdForFile(item.file))
        currentScreen = ComposeScreen.Library
        updateLibraryFilterText("")
        selectLibraryCategory(item.category)
        selectLibraryItem(item)
    }

    private fun showMyFilesForLocate() {
        selectLibrarySection(DEFAULT_LIBRARY_SECTION_ID)
        currentScreen = ComposeScreen.Library
        updateLibraryFilterText("")
        selectLibraryCategory(null)
        clearLibrarySelection()
    }

    fun confirmRemoveLibraryItem(item: LocalFileItem) {
        val sharedList = currentSharedList()
        if (sharedList != null) {
            confirmationDialog = ConfirmationDialogState(
                title = tr("Remove From Collection"),
                message = tr("Remove \"{0}\" from \"{1}\"?", item.fileName, sharedList.collectionName),
                confirmLabel = tr("Remove"),
                onConfirm = {
                    confirmationDialog = null
                    sharedList.removeFile(item.file)
                },
                onDismiss = { confirmationDialog = null }
            )
        } else {
            confirmationDialog = ConfirmationDialogState(
                title = tr("Remove From Library"),
                message = tr("Remove \"{0}\" from My Files?", item.fileName),
                confirmLabel = tr("Remove"),
                onConfirm = {
                    confirmationDialog = null
                    if (item.file == playerCurrentFile) {
                        playerService.stop()
                    }
                    libraryManager.libraryManagedList.removeFile(item.file)
                },
                onDismiss = { confirmationDialog = null }
            )
        }
    }

    fun confirmDeleteLibraryItem(item: LocalFileItem) {
        confirmationDialog = ConfirmationDialogState(
            title = deleteDialogTitle(1),
            message = deleteDialogMessage(1, item.fileName),
            confirmLabel = deleteActionLabel(),
            onConfirm = {
                confirmationDialog = null
                deleteLibraryItemsFromDisk(listOf(item))
            },
            onDismiss = { confirmationDialog = null }
        )
    }

    fun confirmRemoveSelectedLibraryItemsFromLibrary() {
        val items = selectedLibraryItems()
        if (items.isEmpty()) {
            return
        }
        confirmationDialog = ConfirmationDialogState(
            title = tr("Remove From Library"),
            message = if (items.size == 1) {
                tr("Remove \"{0}\" from My Files?", items.first().fileName)
            } else {
                tr("Remove {0} items from My Files?", items.size)
            },
            confirmLabel = tr("Remove"),
            onConfirm = {
                confirmationDialog = null
                removeLibraryItemsFromLibrary(items)
                clearLibrarySelection()
            },
            onDismiss = { confirmationDialog = null }
        )
    }

    fun confirmRemoveSelectedLibraryItemsFromAllOtherLists() {
        val items = selectedLibraryItems()
        if (items.isEmpty()) {
            return
        }
        val currentList = currentSharedList()
        val affectedLists = sharedLists.filter { list ->
            list !== currentList && items.any { list.contains(it.file) }
        }
        if (affectedLists.isEmpty()) {
            return
        }
        confirmationDialog = ConfirmationDialogState(
            title = tr("Remove From Collections"),
            message = if (items.size == 1) {
                tr("Remove this file from {0} other collection(s)?", affectedLists.size)
            } else {
                tr("Remove {0} files from {1} other collection(s)?", items.size, affectedLists.size)
            },
            confirmLabel = tr("Remove"),
            onConfirm = {
                confirmationDialog = null
                val files = uniqueLibraryFiles(items)
                affectedLists.forEach { list ->
                    removeFilesFromLocalList(list, files)
                }
            },
            onDismiss = { confirmationDialog = null }
        )
    }

    fun confirmRemoveSelectedLibraryItems() {
        val items = selectedLibraryItems()
        if (items.isEmpty()) {
            return
        }
        if (items.size == 1) {
            confirmRemoveLibraryItem(items.first())
            return
        }
        val sharedList = currentSharedList()
        confirmationDialog = ConfirmationDialogState(
            title = if (sharedList != null) tr("Remove From Collection") else tr("Remove From Library"),
            message = if (sharedList != null) {
                tr("Remove {0} items from \"{1}\"?", items.size, sharedList.collectionName)
            } else {
                tr("Remove {0} items from My Files?", items.size)
            },
            confirmLabel = tr("Remove"),
            onConfirm = {
                confirmationDialog = null
                if (sharedList != null) {
                    removeFilesFromLocalList(sharedList, uniqueLibraryFiles(items))
                } else {
                    removeLibraryItemsFromLibrary(items)
                }
                selectedLibraryItemPaths.clear()
                selectedLibraryItemPath = null
                librarySelectionAnchorPath = null
            },
            onDismiss = { confirmationDialog = null }
        )
    }

    fun confirmDeleteSelectedLibraryItems() {
        val items = selectedLibraryItems()
        if (items.isEmpty()) {
            return
        }
        if (items.size == 1) {
            confirmDeleteLibraryItem(items.first())
            return
        }
        confirmationDialog = ConfirmationDialogState(
            title = deleteDialogTitle(items.size),
            message = deleteDialogMessage(items.size),
            confirmLabel = deleteActionLabel(),
            onConfirm = {
                confirmationDialog = null
                deleteLibraryItemsFromDisk(items)
                selectedLibraryItemPaths.clear()
                selectedLibraryItemPath = null
                librarySelectionAnchorPath = null
            },
            onDismiss = { confirmationDialog = null }
        )
    }

    fun handleLibraryDeletionShortcut() {
        val items = selectedLibraryItems()
        if (items.isEmpty()) {
            return
        }
        if (currentSharedList() != null) {
            confirmRemoveSelectedLibraryItems()
            return
        }
        libraryDeletionChoiceDialog = LibraryDeletionChoiceDialogState(
            title = deleteChoiceDialogTitle(items.size),
            message = deleteChoiceDialogMessage(items.size),
            removeLabel = tr("Remove from Library"),
            deleteLabel = deleteActionLabel(),
            onRemove = {
                libraryDeletionChoiceDialog = null
                removeLibraryItemsFromLibrary(items)
                clearLibrarySelection()
            },
            onDelete = {
                libraryDeletionChoiceDialog = null
                confirmDeleteSelectedLibraryItems()
            }
        )
    }

    fun downloadSearchResult(tab: SearchTabSession, result: GroupedSearchResult) {
        try {
            addSearchDownload(tab, result)
            noteTransferActivity(TransferTrayMode.DOWNLOADS)
        } catch (failure: DownloadException) {
            handleDownloadException(
                searchDownloadAction(tab, result),
                failure,
                true
            )
        }
    }

    fun downloadSearchResultAs(tab: SearchTabSession, result: GroupedSearchResult) {
        val saveFile = filePicker.chooseSaveFile(
            parent = windowRef,
            title = tr("Download As"),
            suggestedName = result.fileName,
            filenameFilter = null
        ) ?: return
        try {
            addSearchDownload(tab, result, saveFile, false)
            noteTransferActivity(TransferTrayMode.DOWNLOADS)
        } catch (failure: DownloadException) {
            handleDownloadException(
                searchDownloadAction(tab, result),
                failure,
                true
            )
        }
    }

    private fun searchDownloadAction(tab: SearchTabSession, result: GroupedSearchResult): DownloadAction {
        return object : DownloadAction {
            override fun download(saveFile: File, overwrite: Boolean) {
                addSearchDownload(tab, result, saveFile, overwrite)
            }

            override fun downloadCanceled(e: DownloadException) {
            }
        }
    }

    private fun addSearchDownload(
        tab: SearchTabSession,
        result: GroupedSearchResult,
        saveFile: File? = null,
        overwrite: Boolean = false
    ) {
        if (isEd2kSearchResult(result)) {
            ed2kService.addSearchResultDownload(result, saveFile, overwrite)
        } else if (saveFile == null) {
            downloadListManager.addDownload(tab.search, result.searchResults)
        } else {
            downloadListManager.addDownload(tab.search, result.searchResults, saveFile, overwrite)
        }
    }

    private fun startSearchDownloads(tab: SearchTabSession, results: List<GroupedSearchResult>) {
        val dedupedResults = results.distinctBy(::searchResultKeyOf)
        when (dedupedResults.size) {
            0 -> return
            1 -> downloadSearchResult(tab, dedupedResults.first())
            else -> queueBulkSearchDownloads(tab, dedupedResults)
        }
    }

    private fun queueBulkSearchDownloads(tab: SearchTabSession, results: List<GroupedSearchResult>) {
        val pending = ArrayDeque(results)
        val summary = BulkSearchDownloadSummary(total = pending.size)
        val duplicateAction = settingsService.loadPreferences().transfers.duplicateDownloadAction
        processBulkSearchDownloads(tab, pending, summary, duplicateAction)
    }

    private fun processBulkSearchDownloads(
        tab: SearchTabSession,
        pending: ArrayDeque<GroupedSearchResult>,
        summary: BulkSearchDownloadSummary,
        duplicateAction: DuplicateDownloadAction
    ) {
        var processed = 0
        while (processed < BULK_SEARCH_DOWNLOAD_CHUNK_SIZE && pending.isNotEmpty()) {
            val result = pending.removeFirst()
            attemptBulkSearchDownload(tab, result, summary, duplicateAction)
            processed += 1
        }
        if (pending.isNotEmpty()) {
            EventQueue.invokeLater {
                processBulkSearchDownloads(tab, pending, summary, duplicateAction)
            }
        } else {
            finalizeBulkSearchDownloads(summary)
        }
    }

    private fun attemptBulkSearchDownload(
        tab: SearchTabSession,
        result: GroupedSearchResult,
        summary: BulkSearchDownloadSummary,
        duplicateAction: DuplicateDownloadAction
    ) {
        try {
            addSearchDownload(tab, result)
            summary.started += 1
        } catch (failure: DownloadException) {
            val target = failure.file
            if (target != null) {
                try {
                    when {
                        duplicateAction == DuplicateDownloadAction.RENAME &&
                            (failure.errorCode == DownloadException.ErrorCode.FILE_ALREADY_EXISTS ||
                                failure.errorCode == DownloadException.ErrorCode.FILE_IS_ALREADY_DOWNLOADED_TO) -> {
                            addSearchDownload(tab, result, nextAvailableDownloadTarget(target), false)
                            summary.started += 1
                            summary.autoRenamed += 1
                            return
                        }

                        duplicateAction == DuplicateDownloadAction.REPLACE &&
                            failure.errorCode == DownloadException.ErrorCode.FILE_ALREADY_EXISTS -> {
                            addSearchDownload(tab, result, target, true)
                            summary.started += 1
                            summary.replacedExisting += 1
                            return
                        }
                    }
                } catch (retryFailure: DownloadException) {
                    recordBulkSearchDownloadFailure(summary, retryFailure)
                    return
                }
            }
            recordBulkSearchDownloadFailure(summary, failure)
        }
    }

    private fun recordBulkSearchDownloadFailure(summary: BulkSearchDownloadSummary, failure: DownloadException) {
        when (failure.errorCode) {
            DownloadException.ErrorCode.FILE_ALREADY_DOWNLOADING -> summary.alreadyDownloading += 1
            DownloadException.ErrorCode.FILE_ALREADY_UPLOADING -> summary.alreadyUploading += 1
            DownloadException.ErrorCode.FILE_ALREADY_SAVED -> summary.alreadySaved += 1
            DownloadException.ErrorCode.FILE_ALREADY_EXISTS,
            DownloadException.ErrorCode.FILE_IS_ALREADY_DOWNLOADED_TO -> summary.nameConflicts += 1
            DownloadException.ErrorCode.DOWNLOAD_CANCELLED -> Unit
            else -> {
                summary.failed += 1
                if (summary.firstFailureMessage == null) {
                    summary.firstFailureMessage = downloadErrorMessage(failure)
                }
            }
        }
    }

    private fun finalizeBulkSearchDownloads(summary: BulkSearchDownloadSummary) {
        if (summary.started > 0 || summary.alreadyDownloading > 0) {
            noteTransferActivity(TransferTrayMode.DOWNLOADS)
        } else if (summary.alreadyUploading > 0) {
            noteTransferActivity(TransferTrayMode.UPLOADS)
        }

        val parts = buildList {
            if (summary.started > 0) add(tr("Started {0}", summary.started))
            if (summary.autoRenamed > 0) add(tr("{0} saved with a different name to avoid duplicates", summary.autoRenamed))
            if (summary.replacedExisting > 0) add(tr("{0} replaced an existing file", summary.replacedExisting))
            if (summary.alreadySaved > 0) add(tr("{0} already complete", summary.alreadySaved))
            if (summary.alreadyDownloading > 0) add(tr("{0} already in transfers", summary.alreadyDownloading))
            if (summary.alreadyUploading > 0) add(tr("{0} already seeding", summary.alreadyUploading))
            if (summary.nameConflicts > 0) add(tr("{0} skipped for existing filenames", summary.nameConflicts))
            if (summary.failed > 0) add(tr("{0} couldn't start", summary.failed))
        }
        if (parts.isEmpty()) {
            return
        }

        val message = buildString {
            append(parts.joinToString(" • "))
            if (summary.nameConflicts > 0) {
                append(". ")
                append(tr("Change duplicate download handling in Preferences, or retry individual items with Download As."))
            }
            summary.firstFailureMessage?.let { failureMessage ->
                if (summary.failed > 0) {
                    append(" ")
                    append(tr("First issue: {0}", failureMessage))
                }
            }
        }

        val level = when {
            summary.failed > 0 || summary.nameConflicts > 0 -> OperationNoticeLevel.WARNING
            summary.started > 0 && (
                summary.autoRenamed > 0 ||
                    summary.replacedExisting > 0 ||
                    summary.alreadySaved > 0 ||
                    summary.alreadyDownloading > 0 ||
                    summary.alreadyUploading > 0
                ) -> OperationNoticeLevel.INFO
            summary.started > 0 -> OperationNoticeLevel.SUCCESS
            else -> OperationNoticeLevel.INFO
        }
        showNotice(tr("Download"), message, level)
    }

    fun canMarkSearchResultsAsSpam(results: List<GroupedSearchResult>): Boolean {
        return results.any { result -> result.searchResults.any { !it.isSpam } }
    }

    fun canUnmarkSearchResultsAsSpam(results: List<GroupedSearchResult>): Boolean {
        return results.any { result -> result.searchResults.any { it.isSpam } }
    }

    fun markSearchResultsAsSpam(tab: SearchTabSession, results: List<GroupedSearchResult>) {
        val searchResults = results.flatMap { grouped -> grouped.searchResults.filterNot(SearchResult::isSpam) }
        if (searchResults.isEmpty()) {
            return
        }
        spamManager.handleUserMarkedSpam(searchResults)
        scheduleSearchTabPresentationRefresh(tab, SearchPresentationDirty.RESULTS.mask)
    }

    fun unmarkSearchResultsAsSpam(tab: SearchTabSession, results: List<GroupedSearchResult>) {
        val searchResults = results.flatMap { grouped -> grouped.searchResults.filter(SearchResult::isSpam) }
        if (searchResults.isEmpty()) {
            return
        }
        spamManager.handleUserMarkedGood(searchResults)
        scheduleSearchTabPresentationRefresh(tab, SearchPresentationDirty.RESULTS.mask)
    }

    fun showSearchFileInfo(result: GroupedSearchResult? = activeSearchTab()?.let(::selectedSearchResult)) {
        val resolvedResult = result ?: return
        searchFileInfoDialog = SearchFileInfoDialogState(resolvedResult)
    }

    fun closeSearchFileInfoDialog() {
        searchFileInfoDialog = null
    }

    fun canShowSimilarResults(result: GroupedSearchResult): Boolean {
        return groupSimilarResultsEnabled && result.searchResults.size > 1
    }

    fun areSimilarResultsExpanded(tab: SearchTabSession, result: GroupedSearchResult): Boolean {
        return searchResultKeyOf(result) in tab.expandedSimilarResultKeys
    }

    fun toggleSimilarResults(tab: SearchTabSession, result: GroupedSearchResult) {
        if (!canShowSimilarResults(result)) {
            return
        }
        val key = searchResultKeyOf(result)
        if (key in tab.expandedSimilarResultKeys) {
            tab.expandedSimilarResultKeys.remove(key)
        } else {
            tab.expandedSimilarResultKeys.add(key)
        }
        scheduleSearchTabPresentationRefresh(tab)
    }

    fun chatFromSearchResult(result: GroupedSearchResult) {
        result.friends.firstNotNullOfOrNull { friend ->
            friend.id.takeIf(::canChatWithFriend)
        }?.let(::chatWithFriend)
    }

    fun canChatFromSearchResult(result: GroupedSearchResult): Boolean = result.friends.any { canChatWithFriend(it.id) }

    fun toggleSearchSort(tab: SearchTabSession, mode: SearchSortMode) {
        if (tab.sortMode == mode) {
            tab.sortDescending = !tab.sortDescending
        } else {
            tab.sortMode = mode
            tab.sortDescending = defaultSortDescending(mode)
        }
        saveSearchLayoutPreferences(tab)
        scheduleSearchTabPresentationRefresh(tab, SearchPresentationDirty.SORT.mask)
    }

    fun updateSearchFilterText(tab: SearchTabSession, value: String) {
        tab.filterText = value
        scheduleSearchTabPresentationRefresh(tab, SearchPresentationDirty.FILTERS.mask)
    }

    fun toggleSearchFriendsOnly(tab: SearchTabSession) {
        tab.friendsOnly = !tab.friendsOnly
        scheduleSearchTabPresentationRefresh(tab, SearchPresentationDirty.FILTERS.mask)
    }

    fun updateSearchSourceFilter(tab: SearchTabSession, filter: SearchSourceFilter) {
        tab.sourceFilter = filter
        scheduleSearchTabPresentationRefresh(tab, SearchPresentationDirty.FILTERS.mask)
    }

    fun updateSearchFriendFacet(tab: SearchTabSession, friendId: String?) {
        tab.selectedFriendFacetId = friendId
        scheduleSearchTabPresentationRefresh(tab, SearchPresentationDirty.FILTERS.mask)
    }

    fun updateSearchDisplayCategory(tab: SearchTabSession, category: SearchCategory?) {
        if (tab.category != SearchCategory.ALL) {
            return
        }
        val previousCategory = searchPresentationCategory(tab)
        tab.displayCategory = category?.takeUnless { it == SearchCategory.ALL }
        if (tab.visibleColumns == defaultSearchVisibleColumnsForCategory(previousCategory)) {
            tab.visibleColumns = defaultSearchVisibleColumnsForCategory(rawSearchPresentationCategory(tab))
        }
        normalizeSearchFacetState(tab)
        scheduleSearchTabPresentationRefresh(tab, SearchPresentationDirty.FILTERS.mask)
    }

    fun updateSearchPropertyFacet(tab: SearchTabSession, facet: SearchPropertyFacet, value: String?) {
        if (value.isNullOrBlank()) {
            tab.selectedPropertyFacets.remove(facet)
        } else {
            tab.selectedPropertyFacets[facet] = value
        }
        normalizeSearchFacetState(tab)
        scheduleSearchTabPresentationRefresh(tab, SearchPresentationDirty.FILTERS.mask)
    }

    fun updateSearchRangeSelection(
        tab: SearchTabSession,
        facet: SearchRangeFacet,
        minimumId: String? = null,
        maximumId: String? = null,
        preserveExisting: Boolean = true
    ) {
        val current = if (preserveExisting) {
            tab.selectedRangeFacets[facet] ?: SearchRangeSelection()
        } else {
            SearchRangeSelection()
        }
        val next = SearchRangeSelection(
            minimumId = minimumId ?: current.minimumId,
            maximumId = maximumId ?: current.maximumId
        )
        if (next.minimumId == null && next.maximumId == null) {
            tab.selectedRangeFacets.remove(facet)
        } else {
            tab.selectedRangeFacets[facet] = normalizeRangeSelection(facet, next, searchPresentationCategory(tab))
        }
        normalizeSearchFacetState(tab)
        scheduleSearchTabPresentationRefresh(tab, SearchPresentationDirty.FILTERS.mask)
    }

    fun clearSearchRangeSelection(tab: SearchTabSession, facet: SearchRangeFacet) {
        tab.selectedRangeFacets.remove(facet)
        scheduleSearchTabPresentationRefresh(tab, SearchPresentationDirty.FILTERS.mask)
    }

    fun toggleSearchMoreFilters(tab: SearchTabSession) {
        val category = searchPresentationCategory(tab)
        val expanded = tab.expandedFilterCategories[category] == true
        tab.expandedFilterCategories[category] = !expanded
        scheduleSearchTabPresentationRefresh(tab, SearchPresentationDirty.FILTERS.mask)
    }

    fun clearSearchFilters(tab: SearchTabSession) {
        tab.filterText = ""
        tab.friendsOnly = false
        tab.sourceFilter = SearchSourceFilter.ALL
        tab.selectedFriendFacetId = null
        tab.displayCategory = null
        tab.selectedPropertyFacets.clear()
        tab.selectedRangeFacets.clear()
        scheduleSearchTabPresentationRefresh(tab, SearchPresentationDirty.FILTERS.mask)
    }

    fun hasActiveSearchFilters(tab: SearchTabSession): Boolean {
        return searchActiveFilters(tab).isNotEmpty()
    }

    fun availableSearchSourceFilters(tab: SearchTabSession): List<SearchSourceFilter> {
        val filters = mutableListOf(SearchSourceFilter.ALL)
        if (tab.results.any { it.friends.isNotEmpty() }) {
            filters += SearchSourceFilter.FRIENDS
        }
        if (tab.results.any { it.friends.isEmpty() && !isEd2kSearchResult(it) }) {
            filters += SearchSourceFilter.NETWORK
        }
        if (tab.results.any(::isEd2kSearchResult)) {
            filters += SearchSourceFilter.ED2K_KAD
        }
        if (tab.results.any { result -> result.searchResults.any { it.source.isBrowseHostEnabled } }) {
            filters += SearchSourceFilter.BROWSABLE
        }
        return filters
    }

    fun searchShouldShowSourceFacet(tab: SearchTabSession): Boolean {
        if (tab.searchType == SearchDetails.SearchType.SINGLE_BROWSE) {
            return false
        }
        return availableSearchSourceFilters(tab).size > 1
    }

    fun searchShouldShowFriendFacet(tab: SearchTabSession): Boolean {
        return tab.friendFacets.size > 1 || tab.selectedFriendFacetId != null
    }

    fun searchCategoryFacetOptions(tab: SearchTabSession): List<SearchFacetOption> {
        return tab.presentationState.categoryFacetOptions
    }

    fun searchSourceFacetOptions(tab: SearchTabSession): List<SearchFacetOption> {
        return tab.presentationState.sourceFacetOptions
    }

    fun searchFriendFacetOptions(tab: SearchTabSession): List<SearchFacetOption> {
        return tab.presentationState.friendFacetOptions
    }

    fun searchPropertyFacetOptions(tab: SearchTabSession, facet: SearchPropertyFacet): List<SearchFacetOption> {
        return tab.presentationState.propertyFacetOptions[facet].orEmpty()
    }

    fun selectedSearchPropertyFacet(tab: SearchTabSession, facet: SearchPropertyFacet): String? {
        return tab.selectedPropertyFacets[facet]
    }

    fun selectedSearchRangeFacet(tab: SearchTabSession, facet: SearchRangeFacet): SearchRangeSelection {
        return tab.selectedRangeFacets[facet] ?: SearchRangeSelection()
    }

    fun searchRangeBuckets(tab: SearchTabSession, facet: SearchRangeFacet): List<SearchRangeBucket> {
        return when (facet) {
            SearchRangeFacet.SIZE -> sizeRangeBuckets(searchPresentationCategory(tab))
            SearchRangeFacet.LENGTH -> lengthRangeBuckets()
            SearchRangeFacet.BITRATE -> bitrateRangeBuckets()
            SearchRangeFacet.QUALITY -> qualityRangeBuckets()
        }
    }

    fun searchPropertyFacetLabelText(facet: SearchPropertyFacet): String = searchPropertyFacetLabel(facet)

    fun searchRangeFacetLabelText(facet: SearchRangeFacet): String = searchRangeFacetLabel(facet)

    fun searchRangeSelectionLabel(tab: SearchTabSession, facet: SearchRangeFacet): String? {
        return rangeSelectionLabel(tab, facet, selectedSearchRangeFacet(tab, facet))
    }

    fun searchMoreFiltersToggleVisible(tab: SearchTabSession): Boolean {
        return searchPresentationCategory(tab) == SearchCategory.AUDIO
    }

    fun searchMoreFiltersExpanded(tab: SearchTabSession): Boolean {
        return tab.expandedFilterCategories[searchPresentationCategory(tab)] == true
    }

    fun searchVisibleRefinementPropertyFacets(tab: SearchTabSession): List<SearchPropertyFacet> {
        val category = searchPresentationCategory(tab)
        val all = searchPropertyFacetsForCategory(category)
        if (category != SearchCategory.AUDIO || searchMoreFiltersExpanded(tab)) {
            return all
        }
        return all.take(3)
    }

    fun searchVisibleRefinementRangeFacets(tab: SearchTabSession): List<SearchRangeFacet> {
        val category = searchPresentationCategory(tab)
        val all = searchRangeFacetsForCategory(category)
        if (category != SearchCategory.AUDIO || searchMoreFiltersExpanded(tab)) {
            return all
        }
        return emptyList()
    }

    fun toggleSearchColumn(tab: SearchTabSession, column: SearchColumn) {
        tab.visibleColumns = toggledColumns(tab.visibleColumns, column)
        saveSearchLayoutPreferences(tab)
    }

    fun selectSearchResult(
        tab: SearchTabSession,
        result: GroupedSearchResult,
        extendSelection: Boolean = false,
        toggleSelection: Boolean = false
    ) {
        val selection = computeSelectionUpdate(
            visibleKeys = visibleSearchResults(tab).map(::searchResultKeyOf),
            currentSelection = tab.selectedResultKeys,
            currentPrimary = tab.selectedResultKey,
            currentAnchor = tab.selectionAnchorKey,
            targetKey = searchResultKeyOf(result),
            extendSelection = extendSelection,
            toggleSelection = toggleSelection
        )
        applySearchSelection(tab, selection.selectedKeys, selection.primaryKey, selection.anchorKey)
    }

    fun toggleSearchResultChecked(tab: SearchTabSession, result: GroupedSearchResult) {
        selectSearchResult(tab, result, toggleSelection = true)
    }

    fun selectAllVisibleSearchResults(tab: SearchTabSession) {
        val keys = visibleSearchResults(tab).map(::searchResultKeyOf)
        val primary = tab.selectedResultKey?.takeIf { it in keys } ?: keys.firstOrNull()
        applySearchSelection(tab, keys, primary, primary)
    }

    fun clearSearchSelection(tab: SearchTabSession) {
        applySearchSelection(tab, emptyList(), null, null)
    }

    fun handleSearchContextSelection(tab: SearchTabSession, result: GroupedSearchResult) {
        if (searchResultKeyOf(result) !in tab.selectedResultKeys) {
            selectSearchResult(tab, result)
        }
    }

    fun selectedSearchResult(tab: SearchTabSession): GroupedSearchResult? {
        val results = tab.presentationState.visibleResults
        val key = tab.selectedResultKey
        if (key != null) {
            results.firstOrNull { searchResultKeyOf(it) == key }?.let { return it }
        }
        val selectedKeys = tab.selectedResultKeys.toSet()
        return results.firstOrNull { searchResultKeyOf(it) in selectedKeys } ?: results.firstOrNull()
    }

    fun selectedSearchResults(tab: SearchTabSession): List<GroupedSearchResult> {
        val selectedKeys = tab.selectedResultKeys.toSet()
        return tab.presentationState.visibleResults.filter { searchResultKeyOf(it) in selectedKeys }
    }

    fun moveSearchSelection(tab: SearchTabSession, delta: Int) {
        val results = visibleSearchResults(tab)
        if (results.isEmpty()) {
            tab.selectedResultKey = null
            tab.selectedResultKeys.clear()
            tab.selectionAnchorKey = null
            return
        }
        val currentIndex = results.indexOfFirst { searchResultKeyOf(it) == tab.selectedResultKey }
        val nextIndex = when {
            currentIndex < 0 -> 0
            else -> (currentIndex + delta).coerceIn(0, results.lastIndex)
        }
        selectSearchResult(tab, results[nextIndex])
    }

    fun extendSearchSelection(tab: SearchTabSession, delta: Int) {
        val results = visibleSearchResults(tab)
        if (results.isEmpty()) {
            return
        }
        val currentIndex = results.indexOfFirst { searchResultKeyOf(it) == tab.selectedResultKey }
        val nextIndex = when {
            currentIndex < 0 -> 0
            else -> (currentIndex + delta).coerceIn(0, results.lastIndex)
        }
        selectSearchResult(tab, results[nextIndex], extendSelection = true)
    }

    fun activateSelectedSearchResult(tab: SearchTabSession) {
        downloadSelectedSearchResults(tab)
    }

    fun downloadSelectedSearchResults(tab: SearchTabSession) {
        val results = selectedSearchResults(tab)
        if (results.isEmpty()) {
            return
        }
        startSearchDownloads(tab, results)
    }

    fun visibleSearchResults(tab: SearchTabSession): List<GroupedSearchResult> {
        return tab.presentationState.visibleResults
    }

    private fun computeVisibleSearchResults(
        tab: SearchTabSession,
        filtered: List<GroupedSearchResult> = filteredSearchResults(tab)
    ): List<GroupedSearchResult> {
        val presentationCategory = rawSearchPresentationCategory(tab)
        val snapshots = ComposePerformanceTracker.measure("search.sortSnapshots") {
            buildSearchSortSnapshots(filtered, presentationCategory)
        }
        val userComparator = when (tab.sortMode) {
            SearchSortMode.RELEVANCE -> compareBy<SearchSortSnapshot> { it.relevance }
                .thenBy { it.nameSortKey }
            SearchSortMode.TYPE -> compareBy<SearchSortSnapshot> { it.categoryName }
                .thenBy { it.nameSortKey }
            SearchSortMode.NAME -> searchNameSnapshotComparator(presentationCategory)
            SearchSortMode.FROM -> Comparator { left, right ->
                compareValues(left.sourceCount, right.sourceCount)
                    .takeIf { it != 0 }
                    ?: compareNullableIgnoreCase(left.singleSourceFriendName, right.singleSourceFriendName, nullsFirst = false)
                        .takeIf { it != 0 }
                    ?: searchNameSnapshotComparator(presentationCategory).compare(left, right)
            }
            SearchSortMode.FILENAME -> compareBy<SearchSortSnapshot> { it.baseName }
                .thenBy { it.extension }
                .thenBy { it.fileNameLower }
            SearchSortMode.EXTENSION -> compareBy<SearchSortSnapshot> { it.extension }
                .thenBy { it.baseName }
                .thenBy { it.fileNameLower }
            SearchSortMode.SIZE -> compareBy<SearchSortSnapshot> { it.size }
                .thenBy { it.nameSortKey }
            SearchSortMode.SOURCES -> compareBy<SearchSortSnapshot> { it.sourceCount }
                .thenBy { it.nameSortKey }
            SearchSortMode.FRIENDS -> compareBy<SearchSortSnapshot> { it.friendsCount }
                .thenBy { it.nameSortKey }
            SearchSortMode.LENGTH -> compareBy<SearchSortSnapshot> { it.length }
                .thenBy { it.nameSortKey }
            SearchSortMode.QUALITY -> compareBy<SearchSortSnapshot> { it.quality }
                .thenBy { it.bitrate }
                .thenBy { it.nameSortKey }
            SearchSortMode.BITRATE -> compareBy<SearchSortSnapshot> { it.bitrate }
                .thenBy { it.nameSortKey }
            SearchSortMode.TRACK -> compareBy<SearchSortSnapshot> { it.track }
                .thenBy { it.nameSortKey }
            SearchSortMode.ARTIST -> compareBy<SearchSortSnapshot> { it.artist }
                .thenBy { it.album }
                .thenBy { it.track }
                .thenBy { it.nameSortKey }
            SearchSortMode.ALBUM -> compareBy<SearchSortSnapshot> { it.album }
                .thenBy { it.track }
                .thenBy { it.nameSortKey }
            SearchSortMode.GENRE -> compareBy<SearchSortSnapshot> { it.genre }
                .thenBy { it.nameSortKey }
            SearchSortMode.YEAR -> compareBy<SearchSortSnapshot> { it.year }
                .thenBy { it.nameSortKey }
            SearchSortMode.AUTHOR -> compareBy<SearchSortSnapshot> { it.author }
                .thenBy { it.nameSortKey }
            SearchSortMode.COMPANY -> compareBy<SearchSortSnapshot> { it.company }
                .thenBy { it.nameSortKey }
            SearchSortMode.PLATFORM -> compareBy<SearchSortSnapshot> { it.platform }
                .thenBy { it.nameSortKey }
            SearchSortMode.DESCRIPTION -> compareBy<SearchSortSnapshot> { it.description }
                .thenBy { it.nameSortKey }
            SearchSortMode.FILES -> compareBy<SearchSortSnapshot> { it.torrentFiles }
                .thenBy { it.nameSortKey }
            SearchSortMode.TRACKERS -> compareBy<SearchSortSnapshot> { it.torrentTrackers }
                .thenBy { it.nameSortKey }
        }
        val activeComparator = if (tab.sortDescending) userComparator.reversed() else userComparator
        return ComposePerformanceTracker.measure("search.sortVisibleResults") {
            val sorted = snapshots.sortedWith { left, right ->
                compareValues(left.spamRank, right.spamRank)
                    .takeIf { it != 0 }
                    ?: compareValues(left.localRank, right.localRank)
                        .takeIf { it != 0 }
                    ?: activeComparator.compare(left, right)
            }
            sorted.map(SearchSortSnapshot::result)
        }
    }

    private fun buildSearchSortSnapshots(
        results: List<GroupedSearchResult>,
        presentationCategory: SearchCategory
    ): List<SearchSortSnapshot> {
        val localRanks = buildSearchLocalPresortRanks(results)
        return results.map { result ->
            SearchSortSnapshot(
                result = result,
                relevance = result.relevance,
                categoryName = searchSortText(result) { candidate -> candidate.category.getSingularName() },
                nameSortKey = searchNameSortKey(result, presentationCategory),
                baseName = searchBaseName(result),
                fileNameLower = result.fileName.lowercase(Locale.US),
                extension = searchExtension(result),
                sourceCount = result.sources.size,
                friendsCount = result.friends.size,
                singleSourceFriendName = singleSourceFriendName(result),
                sourceLabel = searchSourceLabel(result),
                size = primarySize(result),
                length = searchSortLong(result, FilePropertyKey.LENGTH),
                quality = searchQualityRank(result),
                bitrate = searchBitrateValue(result),
                track = searchTrackSortValue(result),
                artist = searchSortText(result, FilePropertyKey.AUTHOR),
                album = searchSortText(result, FilePropertyKey.ALBUM),
                genre = searchSortText(result, FilePropertyKey.GENRE),
                year = searchSortLong(result, FilePropertyKey.YEAR),
                author = searchSortText(result, FilePropertyKey.AUTHOR),
                company = searchSortText(result, FilePropertyKey.COMPANY),
                platform = searchSortText(result, FilePropertyKey.PLATFORM),
                description = searchSortText(result, FilePropertyKey.DESCRIPTION),
                torrentFiles = searchTorrentFilesCount(result),
                torrentTrackers = searchTorrentTrackersCount(result),
                spamRank = if (result.searchResults.any(SearchResult::isSpam)) 1 else 0,
                localRank = localRanks[searchResultKeyOf(result)] ?: 0
            )
        }
    }

    private fun buildSearchLocalPresortRanks(results: List<GroupedSearchResult>): Map<String, Int> {
        val downloadIndex = searchDownloadIndex()
        val libraryIndex = searchLibraryIndex()
        return results.associate { result ->
            val key = searchResultKeyOf(result)
            val rank = when {
                key in libraryIndex.libraryUrns -> 2
                key in libraryIndex.sharedTargetsByUrn -> 2
                key in downloadIndex.finishedUrns -> 2
                key in downloadIndex.activeUrns -> 1
                else -> 0
            }
            key to rank
        }
    }

    private fun searchNameSnapshotComparator(category: SearchCategory): Comparator<SearchSortSnapshot> {
        val base = compareBy<SearchSortSnapshot> { it.nameSortKey }
            .thenBy { it.baseName }
            .thenBy { it.extension }
            .thenBy { it.fileNameLower }
        return when (category) {
            SearchCategory.PROGRAM -> base.thenBy { it.size }
            else -> base
        }
    }

    private fun filteredSearchResults(
        tab: SearchTabSession,
        excludedGroups: Set<SearchFilterGroup> = emptySet()
    ): List<GroupedSearchResult> {
        if (tab.browseRefreshing) {
            return emptyList()
        }
        val textFilter = tab.filterText.trim().lowercase(Locale.US)
        return tab.results.filter { result ->
            (!tab.friendsOnly || result.friends.isNotEmpty()) &&
                (SearchFilterGroup.SOURCE in excludedGroups || matchesSearchSourceFilter(result, tab.sourceFilter)) &&
                (SearchFilterGroup.FRIEND in excludedGroups || matchesSelectedFriendFacet(result, tab.selectedFriendFacetId)) &&
                (textFilter.isEmpty() || matchesSearchFilter(result, textFilter)) &&
                matchesSearchFacetFilters(result, tab, excludedGroups)
        }
    }

    private fun facetSourceSearchResults(tab: SearchTabSession): List<GroupedSearchResult> {
        return filteredSearchResults(tab, setOf(SearchFilterGroup.FRIEND))
    }

    private fun downloadSelectionKey(item: DownloadItem): String {
        return item.urn?.toString()
            ?: item.saveFile?.absolutePath
            ?: item.downloadingFile?.absolutePath
            ?: "${item.fileName}:${item.startDate.time}"
    }

    fun downloadItemKey(item: DownloadItem): String = downloadSelectionKey(item)

    private fun uploadSelectionKey(item: UploadItem): String {
        return item.urn?.toString()
            ?: item.file?.absolutePath
            ?: "${item.fileName}:${item.startTime}"
    }

    fun uploadItemKey(item: UploadItem): String = uploadSelectionKey(item)

    private fun searchResultKeyOf(result: GroupedSearchResult): String {
        return result.urn?.toString()
            ?: result.searchResults.firstOrNull()?.magnetURL?.takeIf(String::isNotBlank)
            ?: result.fileName
    }

    private fun isEd2kSearchResult(result: GroupedSearchResult): Boolean {
        return result is Ed2kGroupedSearchResultView || result.urn?.toString()?.startsWith("urn:ed2k:") == true
    }

    fun isEd2kDownloadItem(item: DownloadItem): Boolean {
        return item is Ed2kDownloadItem || item.urn?.toString()?.startsWith("urn:ed2k:") == true
    }

    fun isEd2kUploadItem(item: UploadItem): Boolean {
        return item is Ed2kUploadItem || item.urn?.toString()?.startsWith("urn:ed2k:") == true
    }

    fun ed2kLink(item: DownloadItem): String? = (item as? Ed2kDownloadItem)?.ed2kLink

    fun ed2kLink(item: UploadItem): String? = (item as? Ed2kUploadItem)?.ed2kLink

    fun canRequestMoreSources(item: DownloadItem): Boolean {
        return isEd2kDownloadItem(item) && item.state != DownloadState.DONE
    }

    fun requestMoreSources(item: DownloadItem? = selectedDownloadItem()) {
        val resolvedItem = item ?: return
        runCatching {
            ed2kService.requestMoreSources(resolvedItem)
            showNotice(tr("ED2K"), tr("Asked ED2K/Kad to look for more sources."), OperationNoticeLevel.SUCCESS)
        }.onFailure { failure ->
            showNotice(
                tr("ED2K"),
                failure.message ?: tr("WireShare could not request more ED2K/Kad sources."),
                OperationNoticeLevel.ERROR
            )
        }
    }

    private fun allDownloads(): List<DownloadItem> = downloads + ed2kDownloads

    private fun allUploads(): List<UploadItem> = uploads + ed2kUploads

    fun trayDownloads(): List<DownloadItem> = sortDownloads(allDownloads())

    fun trayUploads(): List<UploadItem> = sortUploads(allUploads())

    fun isDownloadProblemItem(item: DownloadItem): Boolean = isDownloadProblem(item)

    fun isUploadProblemItem(item: UploadItem): Boolean = isUploadProblem(item)

    fun stalledDownloadItemCount(): Int = allDownloads().count(::isDownloadProblem)

    fun stalledUploadItemCount(): Int = allUploads().count(::isUploadProblem)

    fun openTransfersWorkspaceForDownload(item: DownloadItem? = null) {
        if (item != null) {
            if (!matchesDownloadFilter(item, downloadFilterMode)) {
                updateDownloadFilterMode(TransferFilterMode.ALL)
            }
            selectDownloadItem(item)
        }
        openTransfersWorkspace(TransferTrayMode.DOWNLOADS)
    }

    fun openTransfersWorkspaceForUpload(item: UploadItem? = null) {
        if (item != null) {
            if (!matchesUploadFilter(item, uploadFilterMode)) {
                updateUploadFilterMode(TransferFilterMode.ALL)
            }
            selectUploadItem(item)
        }
        openTransfersWorkspace(TransferTrayMode.UPLOADS)
    }

    fun selectedDownloadItem(): DownloadItem? {
        val visibleItems = visibleDownloads()
        val selectedUrn = selectedDownloadUrn
        if (selectedUrn != null) {
            visibleItems.firstOrNull { downloadSelectionKey(it) == selectedUrn }?.let { return it }
        }
        val selectedUrns = selectedDownloadUrns.toSet()
        return visibleItems.firstOrNull { downloadSelectionKey(it) in selectedUrns }
    }

    fun selectedDownloadItems(): List<DownloadItem> {
        val selectedUrns = selectedDownloadUrns.toSet()
        return visibleDownloads().filter { downloadSelectionKey(it) in selectedUrns }
    }

    fun selectedDownloadCollectionFiles(): List<File> = selectedDownloadItems()
        .mapNotNull { item ->
            when {
                item.isLaunchable -> item.launchableFile
                else -> item.completeFiles.firstOrNull()
            }
        }
        .distinctBy { it.absolutePath }

    fun selectedDownloadsPlayInPlayer(): Boolean {
        val items = selectedDownloadItems().filter { it.isLaunchable }
        return items.isNotEmpty() && items.all { item ->
            val file = item.launchableFile
            categoryManager.getCategoryForFile(file) == Category.AUDIO &&
                playerService.isPlayable(file) &&
                playerEnabled
        }
    }

    fun canAddSelectedDownloadsToCollection(): Boolean = selectedDownloadCollectionFiles().isNotEmpty() && sharedLists.isNotEmpty()

    fun selectDownloadItem(
        item: DownloadItem,
        extendSelection: Boolean = false,
        toggleSelection: Boolean = false
    ) {
        val selection = computeSelectionUpdate(
            visibleKeys = visibleDownloads().map(::downloadSelectionKey),
            currentSelection = selectedDownloadUrns,
            currentPrimary = selectedDownloadUrn,
            currentAnchor = downloadSelectionAnchorUrn,
            targetKey = downloadSelectionKey(item),
            extendSelection = extendSelection,
            toggleSelection = toggleSelection
        )
        applyDownloadSelection(selection.selectedKeys, selection.primaryKey, selection.anchorKey)
    }

    fun toggleDownloadItemChecked(item: DownloadItem) {
        selectDownloadItem(item, toggleSelection = true)
    }

    fun selectAllVisibleDownloads() {
        val keys = visibleDownloads().map(::downloadSelectionKey)
        val primary = selectedDownloadUrn?.takeIf { it in keys } ?: keys.firstOrNull()
        applyDownloadSelection(keys, primary, primary)
    }

    fun clearDownloadSelection() {
        applyDownloadSelection(emptyList(), null, null)
    }

    fun handleDownloadContextSelection(item: DownloadItem) {
        if (downloadSelectionKey(item) !in selectedDownloadUrns) {
            selectDownloadItem(item)
        }
    }

    fun moveDownloadSelection(delta: Int) {
        val items = visibleDownloads()
        if (items.isEmpty()) {
            selectedDownloadUrn = null
            selectedDownloadUrns.clear()
            downloadSelectionAnchorUrn = null
            return
        }
        val currentIndex = items.indexOfFirst { downloadSelectionKey(it) == selectedDownloadUrn }
        val nextIndex = when {
            currentIndex < 0 -> 0
            else -> (currentIndex + delta).coerceIn(0, items.lastIndex)
        }
        selectDownloadItem(items[nextIndex])
    }

    fun extendDownloadSelection(delta: Int) {
        val items = visibleDownloads()
        if (items.isEmpty()) {
            return
        }
        val currentIndex = items.indexOfFirst { downloadSelectionKey(it) == selectedDownloadUrn }
        val nextIndex = when {
            currentIndex < 0 -> 0
            else -> (currentIndex + delta).coerceIn(0, items.lastIndex)
        }
        selectDownloadItem(items[nextIndex], extendSelection = true)
    }

    fun activateSelectedDownloadItem() {
        openSelectedDownloads()
    }

    fun toggleDownloadSort(mode: DownloadSortMode) {
        if (downloadSortMode == mode) {
            downloadSortDescending = !downloadSortDescending
        } else {
            downloadSortMode = mode
            downloadSortDescending = defaultDownloadSortDescending(mode)
        }
        invalidateDownloadPresentationCache()
        saveDownloadLayoutPreferences()
    }

    fun applyDownloadSortMode(mode: DownloadSortMode) {
        downloadSortMode = mode
        downloadSortDescending = defaultDownloadSortDescending(mode)
        invalidateDownloadPresentationCache()
        saveDownloadLayoutPreferences()
    }

    fun reverseDownloadSort() {
        downloadSortDescending = !downloadSortDescending
        invalidateDownloadPresentationCache()
        saveDownloadLayoutPreferences()
    }

    fun toggleDownloadColumn(column: DownloadColumn) {
        visibleDownloadColumns = toggledColumns(visibleDownloadColumns, column)
        saveDownloadLayoutPreferences()
    }

    fun updateDownloadFilterMode(mode: TransferFilterMode) {
        downloadFilterMode = mode
        invalidateDownloadPresentationCache()
        saveDownloadLayoutPreferences()
    }

    fun updateDownloadSearchText(value: String) {
        if (downloadSearchText == value) {
            return
        }
        downloadSearchText = value
        invalidateDownloadPresentationCache()
    }

    private fun matchesDownloadFilter(item: DownloadItem, mode: TransferFilterMode): Boolean {
        return when (mode) {
            TransferFilterMode.ALL -> true
            TransferFilterMode.ACTIVE -> !item.state.isFinished && !isDownloadProblem(item)
            TransferFilterMode.FINISHED -> item.state.isFinished
            TransferFilterMode.STALLED -> isDownloadProblem(item)
        }
    }

    private fun matchesDownloadSearch(item: DownloadItem, filter: String): Boolean {
        if (filter.isBlank()) {
            return true
        }
        return listOfNotNull(
            item.fileName,
            FileUtils.getFilenameNoExtension(item.fileName),
            FileUtils.getFileExtension(item.fileName),
            item.title.takeUnless(String::isBlank),
            item.saveFile?.absolutePath,
            item.downloadingFile?.absolutePath,
            item.category.getPluralName(),
            item.category.getSingularName(),
            item.state.name.replace('_', ' '),
            item.errorState.takeUnless { it == DownloadItem.ErrorState.NONE }?.message
        ).any { it.lowercase(Locale.US).contains(filter) }
    }

    fun visibleDownloads(): List<DownloadItem> {
        if (!visibleDownloadsDirty) {
            return cachedVisibleDownloads
        }
        cachedVisibleDownloads = ComposePerformanceTracker.measure("transfers.visibleDownloads") {
            val textFilter = downloadSearchText.trim().lowercase(Locale.US)
            val filtered = allDownloads().filter { item ->
                matchesDownloadFilter(item, downloadFilterMode) && matchesDownloadSearch(item, textFilter)
            }
            sortDownloads(filtered)
        }
        visibleDownloadsDirty = false
        return cachedVisibleDownloads
    }

    fun hasPausableDownloads(): Boolean = allDownloads().any { it.state.isPausable }

    fun hasResumableDownloads(): Boolean = allDownloads().any {
        it.state.isResumable || (it.state == DownloadState.ERROR && it.isTryAgainEnabled)
    }

    fun hasErrorDownloads(): Boolean = allDownloads().any { it.state == DownloadState.ERROR }

    fun hasStalledDownloadItems(): Boolean = allDownloads().any { it.state == DownloadState.STALLED }

    fun hasAnyDownloads(): Boolean = allDownloads().isNotEmpty()

    fun pauseAllDownloads() {
        allDownloads().filter { it.state.isPausable }.forEach(DownloadItem::pause)
    }

    fun resumeAllDownloads() {
        allDownloads().forEach { item ->
            when {
                item.state.isResumable -> item.resume()
                item.state == DownloadState.ERROR && item.isTryAgainEnabled -> item.resume()
            }
        }
    }

    fun pauseSelectedDownloads() {
        selectedDownloadItems().filter { it.state.isPausable }.forEach(DownloadItem::pause)
    }

    fun pauseDownloadItem(item: DownloadItem) {
        if (item.state.isPausable) {
            item.pause()
        }
    }

    fun resumeVisibleDownloads() {
        visibleDownloads().forEach { item ->
            when {
                item.state.isResumable -> item.resume()
                item.state == DownloadState.ERROR && item.isTryAgainEnabled -> item.resume()
            }
        }
    }

    fun resumeSelectedDownloads() {
        selectedDownloadItems().forEach { item ->
            when {
                item.state.isResumable -> item.resume()
                item.state == DownloadState.ERROR && item.isTryAgainEnabled -> item.resume()
            }
        }
    }

    fun resumeDownloadItem(item: DownloadItem) {
        when {
            item.state.isResumable -> item.resume()
            item.state == DownloadState.ERROR && item.isTryAgainEnabled -> item.resume()
        }
    }

    fun canChangeDownloadLocation(item: DownloadItem? = selectedDownloadItem()): Boolean {
        return item?.isRelocatable == true
    }

    fun changeDownloadLocation(item: DownloadItem? = selectedDownloadItem()) {
        val resolvedItem = item ?: return
        if (!resolvedItem.isRelocatable) {
            return
        }
        val currentParent = resolvedItem.saveFile?.parentFile
        val selectedDirectory = filePicker.chooseFiles(
            parent = windowRef,
            title = tr("Choose Download Folder"),
            directoriesOnly = true,
            multiple = false,
            filenameFilter = null,
            initialDirectory = currentParent
        ).firstOrNull() ?: return
        val validation = settingsService.validateSaveDirectory(selectedDirectory.absolutePath)
        if (!validation.accepted) {
            showNotice(
                tr("Save Folder Error"),
                validation.errorMessage ?: tr("Choose a different download folder to continue."),
                OperationNoticeLevel.ERROR
            )
            return
        }
        val approvedDirectory = validation.normalizedPath?.let(::File) ?: selectedDirectory
        if (approvedDirectory == currentParent) {
            return
        }
        try {
            resolvedItem.setSaveFile(approvedDirectory, false)
        } catch (failure: DownloadException) {
            handleDownloadException(
                object : DownloadAction {
                    override fun download(saveFile: File, overwrite: Boolean) {
                        resolvedItem.setSaveFile(saveFile, overwrite)
                    }

                    override fun downloadCanceled(e: DownloadException) {
                    }
                },
                failure,
                false
            )
        }
    }

    fun searchAgainForDownload(item: DownloadItem? = selectedDownloadItem()) {
        val resolvedItem = item ?: return
        val category = SearchCategory.forCategory(resolvedItem.category) ?: SearchCategory.ALL
        val title = searchTitleForDownload(resolvedItem)
        searchCategory = category
        startSearch(
            title = title,
            query = title,
            category = category,
            searchType = SearchDetails.SearchType.KEYWORD,
            advancedDetails = emptyMap()
        )
    }

    fun retryOrSearchAgainForDownload(item: DownloadItem? = selectedDownloadItem()) {
        val resolvedItem = item ?: return
        if (isDownloadProblem(resolvedItem) && resolvedItem.isTryAgainEnabled) {
            resolvedItem.resume()
        } else {
            searchAgainForDownload(resolvedItem)
        }
    }

    fun downloadOpenActionLabel(item: DownloadItem): String {
        return when {
            canPreviewDownload(item) -> tr("Preview File")
            item.isLaunchable -> tr("Play/Open")
            else -> tr("Open")
        }
    }

    fun selectedDownloadsOpenActionLabel(): String {
        val items = selectedDownloadItems().filter(DownloadItem::isLaunchable)
        if (items.isEmpty()) {
            return tr("Play/Open")
        }
        if (selectedDownloadsPlayInPlayer()) {
            return if (items.size > 1) tr("Play Queue") else tr("Play/Open")
        }
        return if (items.size == 1) downloadOpenActionLabel(items.first()) else tr("Play/Open Selected")
    }

    fun downloadRetryActionLabel(item: DownloadItem): String {
        return if (isDownloadProblem(item) && item.isTryAgainEnabled) tr("Try Again") else tr("Search Again")
    }

    fun downloadRemoveActionLabel(items: List<DownloadItem> = selectedDownloadItems()): String {
        val actionableItems = items.ifEmpty { selectedDownloadItems() }
        val clearFromTray = actionableItems.isNotEmpty() && actionableItems.all { it.state.isFinished || isDownloadProblem(it) }
        return when {
            clearFromTray && actionableItems.size > 1 -> tr("Clear Selected from Tray")
            clearFromTray -> tr("Clear from Tray")
            actionableItems.size > 1 -> tr("Cancel Selected Downloads")
            else -> tr("Cancel Download")
        }
    }

    fun clearFinishedDownloads() {
        downloadListManager.clearFinished()
        if (downloadFilterMode == TransferFilterMode.FINISHED) {
            selectedDownloadUrn = null
            selectedDownloadUrns.clear()
            downloadSelectionAnchorUrn = null
        }
    }

    fun clearProblemDownloads() {
        allDownloads()
            .filter(::isDownloadProblem)
            .forEach { item ->
                removeDownloadItem(item)
            }
        selectedDownloadUrn = null
        selectedDownloadUrns.clear()
        downloadSelectionAnchorUrn = null
    }

    fun updateClearDownloadsWhenFinishedPreference(enabled: Boolean) {
        settingsService.setClearDownloadsWhenFinished(enabled)
        clearDownloadsWhenFinished = settingsService.clearDownloadsWhenFinished()
    }

    fun cancelAllStalledDownloads() {
        allDownloads()
            .filter { it.state == DownloadState.STALLED }
            .toList()
            .forEach(::removeDownloadItem)
    }

    fun cancelAllErrorDownloads() {
        allDownloads()
            .filter { it.state == DownloadState.ERROR }
            .toList()
            .forEach(::removeDownloadItem)
    }

    fun cancelAllDownloads() {
        allDownloads().toList().forEach(::removeDownloadItem)
    }

    fun openDownloadItem(item: DownloadItem) {
        if (!item.isLaunchable) {
            return
        }
        Thread {
            val file = item.launchableFile
            runOnUi {
                val category = categoryManager.getCategoryForFile(file)
                if (category == Category.AUDIO && playerService.isPlayable(file) && playerEnabled) {
                    playDownloadQueue(
                        items = visibleDownloads().filter { it.isLaunchable },
                        currentItem = item,
                        sourceLabel = tr("Completed Downloads")
                    )
                } else {
                    openFileWithComposeFeedback(file)
                }
            }
        }.start()
    }

    fun revealDownloadItem(item: DownloadItem) {
        revealFileWithComposeFeedback(item.downloadingFile ?: item.saveFile)
    }

    fun openSelectedDownloads() {
        val items = selectedDownloadItems().filter { it.isLaunchable }
        if (items.isEmpty()) {
            return
        }
        if (items.size == 1) {
            openDownloadItem(items.first())
            return
        }
        val audioOnly = items.all { item ->
            val file = item.launchableFile
            categoryManager.getCategoryForFile(file) == Category.AUDIO &&
                playerService.isPlayable(file) &&
                playerEnabled
        }
        if (audioOnly) {
            playDownloadQueue(
                items = items,
                currentItem = items.first(),
                sourceLabel = tr("Selected Downloads")
            )
            return
        }
        items.forEach(::openDownloadItem)
    }

    fun revealSelectedDownloads() {
        selectedDownloadItems().forEach(::revealDownloadItem)
    }

    fun canBrowseDownloadSources(item: DownloadItem? = selectedDownloadItem()): Boolean {
        return browseablePresences(item?.remoteHosts.orEmpty()).isNotEmpty()
    }

    fun downloadBrowseTargets(item: DownloadItem? = selectedDownloadItem()): List<BrowseSourceTarget> {
        return browseSourceTargetsForRemoteHosts(item?.remoteHosts.orEmpty())
    }

    fun browseDownloadSources(item: DownloadItem? = selectedDownloadItem()) {
        browseRemoteHosts(item?.remoteHosts.orEmpty())
    }

    fun downloadBlockTargets(item: DownloadItem? = selectedDownloadItem()): List<RemoteUserTarget> {
        return blockTargetsForRemoteHosts(item?.remoteHosts.orEmpty())
    }

    fun blockDownloadUsers(item: DownloadItem? = selectedDownloadItem()) {
        confirmBlockUsers(downloadBlockTargets(item))
    }

    fun blockUsers(targets: List<RemoteUserTarget>) {
        confirmBlockUsers(targets)
    }

    fun addSelectedDownloadsToCollection(collectionId: Int) {
        val collection = sharedLists.firstOrNull { it.id == collectionId } ?: return
        addFilesToCollection(
            collection = collection,
            files = selectedDownloadCollectionFiles(),
            sourceLabel = tr("Downloads")
        )
    }

    fun locateDownloadItem(item: DownloadItem) {
        item.launchableFile?.let { file ->
            val bestSectionId = bestLibrarySectionIdForFile(file)
            if (bestSectionId != null) {
                showFileInTarget(file, LibraryJumpTarget(bestSectionId, targetLabelForSection(bestSectionId)))
            } else {
                showMyFilesForLocate()
            }
        }
    }

    fun locateSelectedDownloads() {
        selectedDownloadItems().firstOrNull()?.let(::locateDownloadItem)
    }

    fun downloadJumpTargets(item: DownloadItem): List<LibraryJumpTarget> {
        return item.launchableFile?.let(::libraryJumpTargetsForFile).orEmpty()
    }

    fun showDownloadInTarget(item: DownloadItem, target: LibraryJumpTarget) {
        item.launchableFile?.let { showFileInTarget(it, target) }
    }

    private fun buildSearchResultPresentation(result: GroupedSearchResult): SearchResultPresentation {
        val localAvailability = searchLocalAvailability(result)
        return SearchResultPresentation(
            identity = buildSearchResultIdentity(result),
            availabilityLabel = localAvailability.availabilityLabel,
            jumpTargets = localAvailability.jumpTargets,
            browseTargets = browseSourceTargetsForRemoteHosts(result.sources),
            blockTargets = blockTargetsForRemoteHosts(result.sources),
            browsableCount = result.searchResults.count { it.source.isBrowseHostEnabled },
            downloadItem = localAvailability.downloadItem
        )
    }

    private fun searchLocalAvailability(result: GroupedSearchResult): SearchLocalAvailability {
        val key = searchResultKeyOf(result)
        return searchLocalAvailabilityCache.getOrPut(key) {
            val libraryIndex = searchLibraryIndex()
            val downloadItem = searchDownloadIndex().byUrn[key]
            val inLibrary = key in libraryIndex.libraryUrns
            val sharedTargets = libraryIndex.sharedTargetsByUrn[key].orEmpty()
            val jumpTargets = buildList {
                if (inLibrary) {
                    add(LibraryJumpTarget(DEFAULT_LIBRARY_SECTION_ID, tr("My Files")))
                }
                addAll(sharedTargets)
            }
            val localRank = when {
                inLibrary || sharedTargets.isNotEmpty() -> 2
                downloadItem?.let { it.state.isFinished || it.completeFiles.isNotEmpty() } == true -> 2
                downloadItem != null -> 1
                else -> 0
            }
            val availabilityLabel = when {
                inLibrary -> tr("In My Files")
                sharedTargets.isNotEmpty() -> tr("In Collection")
                downloadItem?.let { it.state.isFinished || it.completeFiles.isNotEmpty() } == true -> tr("In Downloads")
                downloadItem != null -> tr("Downloading")
                else -> null
            }
            SearchLocalAvailability(
                jumpTargets = jumpTargets,
                availabilityLabel = availabilityLabel,
                downloadItem = downloadItem,
                localRank = localRank
            )
        }
    }

    fun searchResultPresentation(result: GroupedSearchResult): SearchResultPresentation {
        val key = searchResultKeyOf(result)
        return searchResultPresentationCache.getOrPut(key) {
            ComposePerformanceTracker.measure("search.resultPresentation") {
                buildSearchResultPresentation(result)
            }
        }
    }

    fun searchResultBlockTargets(result: GroupedSearchResult): List<RemoteUserTarget> {
        return searchResultPresentation(result).blockTargets
    }

    fun searchResultBrowseTargets(result: GroupedSearchResult): List<BrowseSourceTarget> {
        return searchResultPresentation(result).browseTargets
    }

    fun blockSearchResultUsers(result: GroupedSearchResult) {
        confirmBlockUsers(searchResultBlockTargets(result))
    }

    fun canLocateSearchResult(result: GroupedSearchResult): Boolean {
        return searchResultPresentation(result).jumpTargets.isNotEmpty()
    }

    fun locateSearchResult(result: GroupedSearchResult) {
        val bestSectionId = bestLibrarySectionIdForSearchResult(result)
        if (bestSectionId != null) {
            navigateSearchResultToTarget(result, LibraryJumpTarget(bestSectionId, targetLabelForSection(bestSectionId)))
        }
    }

    fun searchResultJumpTargets(result: GroupedSearchResult): List<LibraryJumpTarget> {
        return searchResultPresentation(result).jumpTargets
    }

    fun searchResultAvailabilityLabel(result: GroupedSearchResult): String? {
        return searchResultPresentation(result).availabilityLabel
    }

    fun showSearchResultInTarget(result: GroupedSearchResult, target: LibraryJumpTarget) {
        navigateSearchResultToTarget(result, target)
    }

    fun removeDownloadItem(item: DownloadItem) {
        item.cancel()
        if (!isEd2kDownloadItem(item)) {
            downloadListManager.remove(item)
        }
        val selectionKey = downloadSelectionKey(item)
        if (selectedDownloadUrn == selectionKey) {
            selectedDownloadUrn = null
        }
        selectedDownloadUrns.remove(selectionKey)
    }

    fun removeSelectedDownloads() {
        selectedDownloadItems().toList().forEach(::removeDownloadItem)
    }

    fun selectedUploadItem(): UploadItem? {
        val visibleItems = visibleUploads()
        val selectedUrn = selectedUploadUrn
        if (selectedUrn != null) {
            visibleItems.firstOrNull { uploadSelectionKey(it) == selectedUrn }?.let { return it }
        }
        val selectedUrns = selectedUploadUrns.toSet()
        return visibleItems.firstOrNull { uploadSelectionKey(it) in selectedUrns }
    }

    fun selectedUploadItems(): List<UploadItem> {
        val selectedUrns = selectedUploadUrns.toSet()
        return visibleUploads().filter { uploadSelectionKey(it) in selectedUrns }
    }

    fun selectedUploadCollectionFiles(): List<File> = selectedUploadItems()
        .map { it.file }
        .distinctBy { it.absolutePath }

    fun selectedUploadsPlayInPlayer(): Boolean {
        val items = selectedUploadItems().filter(::canOpenUploadItem)
        return items.isNotEmpty() && items.all { item ->
            categoryManager.getCategoryForFile(item.file) == Category.AUDIO &&
                playerService.isPlayable(item.file) &&
                playerEnabled
        }
    }

    fun canAddSelectedUploadsToCollection(): Boolean = selectedUploadCollectionFiles().isNotEmpty() && sharedLists.isNotEmpty()

    fun canOpenUploadItem(item: UploadItem): Boolean {
        return item.state != UploadState.BROWSE_HOST && item.state != UploadState.BROWSE_HOST_DONE
    }

    fun uploadOpenActionLabel(item: UploadItem): String {
        return if (canOpenUploadItem(item)) {
            tr("Play/Open")
        } else {
            tr("Open")
        }
    }

    fun selectedUploadsOpenActionLabel(): String {
        val items = selectedUploadItems().filter(::canOpenUploadItem)
        if (items.isEmpty()) {
            return tr("Play/Open")
        }
        if (selectedUploadsPlayInPlayer()) {
            return if (items.size > 1) tr("Play Queue") else tr("Play/Open")
        }
        return if (items.size == 1) uploadOpenActionLabel(items.first()) else tr("Play/Open Selected")
    }

    fun uploadRemoveActionLabel(items: List<UploadItem> = selectedUploadItems()): String {
        val actionableItems = items.ifEmpty { selectedUploadItems() }
        val clearFromTray = actionableItems.isNotEmpty() && actionableItems.all { it.isFinished || isUploadProblem(it) }
        return when {
            clearFromTray && actionableItems.size > 1 -> tr("Clear Selected from Tray")
            clearFromTray -> tr("Clear from Tray")
            actionableItems.size > 1 -> tr("Cancel Selected Uploads")
            else -> tr("Cancel Upload")
        }
    }

    fun selectUploadItem(
        item: UploadItem,
        extendSelection: Boolean = false,
        toggleSelection: Boolean = false
    ) {
        val selection = computeSelectionUpdate(
            visibleKeys = visibleUploads().map(::uploadSelectionKey),
            currentSelection = selectedUploadUrns,
            currentPrimary = selectedUploadUrn,
            currentAnchor = uploadSelectionAnchorUrn,
            targetKey = uploadSelectionKey(item),
            extendSelection = extendSelection,
            toggleSelection = toggleSelection
        )
        applyUploadSelection(selection.selectedKeys, selection.primaryKey, selection.anchorKey)
    }

    fun toggleUploadItemChecked(item: UploadItem) {
        selectUploadItem(item, toggleSelection = true)
    }

    fun selectAllVisibleUploads() {
        val keys = visibleUploads().map(::uploadSelectionKey)
        val primary = selectedUploadUrn?.takeIf { it in keys } ?: keys.firstOrNull()
        applyUploadSelection(keys, primary, primary)
    }

    fun clearUploadSelection() {
        applyUploadSelection(emptyList(), null, null)
    }

    fun handleUploadContextSelection(item: UploadItem) {
        if (uploadSelectionKey(item) !in selectedUploadUrns) {
            selectUploadItem(item)
        }
    }

    fun moveUploadSelection(delta: Int) {
        val items = visibleUploads()
        if (items.isEmpty()) {
            selectedUploadUrn = null
            selectedUploadUrns.clear()
            uploadSelectionAnchorUrn = null
            return
        }
        val currentIndex = items.indexOfFirst { uploadSelectionKey(it) == selectedUploadUrn }
        val nextIndex = when {
            currentIndex < 0 -> 0
            else -> (currentIndex + delta).coerceIn(0, items.lastIndex)
        }
        selectUploadItem(items[nextIndex])
    }

    fun extendUploadSelection(delta: Int) {
        val items = visibleUploads()
        if (items.isEmpty()) {
            return
        }
        val currentIndex = items.indexOfFirst { uploadSelectionKey(it) == selectedUploadUrn }
        val nextIndex = when {
            currentIndex < 0 -> 0
            else -> (currentIndex + delta).coerceIn(0, items.lastIndex)
        }
        selectUploadItem(items[nextIndex], extendSelection = true)
    }

    fun activateSelectedUploadItem() {
        openSelectedUploads()
    }

    fun toggleUploadSort(mode: UploadSortMode) {
        if (uploadSortMode == mode) {
            uploadSortDescending = !uploadSortDescending
        } else {
            uploadSortMode = mode
            uploadSortDescending = defaultUploadSortDescending(mode)
        }
        invalidateUploadPresentationCache()
        saveUploadLayoutPreferences()
    }

    fun applyUploadSortMode(mode: UploadSortMode) {
        uploadSortMode = mode
        uploadSortDescending = defaultUploadSortDescending(mode)
        invalidateUploadPresentationCache()
        saveUploadLayoutPreferences()
    }

    fun reverseUploadSort() {
        uploadSortDescending = !uploadSortDescending
        invalidateUploadPresentationCache()
        saveUploadLayoutPreferences()
    }

    fun toggleUploadColumn(column: UploadColumn) {
        visibleUploadColumns = toggledColumns(visibleUploadColumns, column)
        saveUploadLayoutPreferences()
    }

    fun updateUploadFilterMode(mode: TransferFilterMode) {
        uploadFilterMode = mode
        invalidateUploadPresentationCache()
        saveUploadLayoutPreferences()
    }

    private fun matchesUploadFilter(item: UploadItem, mode: TransferFilterMode): Boolean {
        return when (mode) {
            TransferFilterMode.ALL -> true
            TransferFilterMode.ACTIVE -> !item.isFinished && !isUploadProblem(item)
            TransferFilterMode.FINISHED -> item.isFinished
            TransferFilterMode.STALLED -> isUploadProblem(item)
        }
    }

    fun visibleUploads(): List<UploadItem> {
        if (!visibleUploadsDirty) {
            return cachedVisibleUploads
        }
        cachedVisibleUploads = ComposePerformanceTracker.measure("transfers.visibleUploads") {
            val filtered = allUploads().filter { item -> matchesUploadFilter(item, uploadFilterMode) }
            sortUploads(filtered)
        }
        visibleUploadsDirty = false
        return cachedVisibleUploads
    }

    fun hasPausableUploads(): Boolean = allUploads().any { it.state == UploadState.UPLOADING }

    fun hasResumableUploads(): Boolean = allUploads().any { it.state == UploadState.PAUSED }

    fun hasErrorUploads(): Boolean = allUploads().any(::isUploadProblem)

    fun hasTorrentUploads(): Boolean = allUploads().any { it.uploadItemType == UploadItem.UploadItemType.BITTORRENT }

    fun hasAnyUploads(): Boolean = allUploads().isNotEmpty()

    fun pauseActiveUploads() {
        allUploads().filter { it.state == UploadState.UPLOADING }.forEach(UploadItem::pause)
    }

    fun resumeAllUploads() {
        allUploads().filter { it.state == UploadState.PAUSED }.forEach(UploadItem::resume)
    }

    fun pauseSelectedUploads() {
        selectedUploadItems().filter { it.state == UploadState.UPLOADING }.forEach(UploadItem::pause)
    }

    fun pauseUploadItem(item: UploadItem) {
        if (item.state == UploadState.UPLOADING) {
            item.pause()
        }
    }

    fun resumePausedUploads() {
        allUploads().filter { it.state == UploadState.PAUSED }.forEach(UploadItem::resume)
    }

    fun resumeSelectedUploads() {
        selectedUploadItems().filter { it.state == UploadState.PAUSED }.forEach(UploadItem::resume)
    }

    fun resumeUploadItem(item: UploadItem) {
        if (item.state == UploadState.PAUSED) {
            item.resume()
        }
    }

    fun clearFinishedUploads() {
        uploadListManager.clearFinished()
        if (uploadFilterMode == TransferFilterMode.FINISHED) {
            selectedUploadUrn = null
            selectedUploadUrns.clear()
            uploadSelectionAnchorUrn = null
        }
    }

    fun updateClearUploadsWhenFinishedPreference(enabled: Boolean) {
        settingsService.setClearUploadsWhenFinished(enabled)
        clearUploadsWhenFinished = settingsService.clearUploadsWhenFinished()
    }

    fun updateShowUploadsInTrayPreference(enabled: Boolean) {
        settingsService.setShowUploadsInTray(enabled)
        showUploadsInTray = settingsService.showUploadsInTray()
        if (!showUploadsInTray && trayMode == TransferTrayMode.UPLOADS && currentScreen != ComposeScreen.Transfers) {
            trayMode = TransferTrayMode.DOWNLOADS
        }
    }

    fun cancelAllErrorUploads() {
        allUploads()
            .filter(::isUploadProblem)
            .toList()
            .forEach(::removeUploadItem)
    }

    fun confirmCancelAllTorrentUploads() {
        confirmationDialog = ConfirmationDialogState(
            title = tr("Cancel"),
            message = buildString {
                append(tr("Cancel uploading all torrents?"))
                if (hasActiveTorrentDownloads()) {
                    append("\n\n")
                    append(tr("Note: Downloading torrents will be cancelled as well."))
                }
            },
            confirmLabel = tr("Cancel Torrents"),
            onConfirm = {
                confirmationDialog = null
                cancelAllTorrentUploads()
            },
            onDismiss = { confirmationDialog = null }
        )
    }

    fun confirmCancelAllUploads() {
        confirmationDialog = ConfirmationDialogState(
            title = tr("Cancel"),
            message = buildString {
                append(tr("Cancel all uploads?"))
                if (hasActiveTorrentDownloads()) {
                    append("\n\n")
                    append(tr("Note: Downloading torrents will be cancelled as well."))
                }
            },
            confirmLabel = tr("Cancel Uploads"),
            onConfirm = {
                confirmationDialog = null
                cancelAllUploads()
            },
            onDismiss = { confirmationDialog = null }
        )
    }

    fun openUploadItem(item: UploadItem) {
        if (!canOpenUploadItem(item)) {
            return
        }
        val file = item.file
        val category = categoryManager.getCategoryForFile(file)
        if (category == Category.AUDIO && playerService.isPlayable(file) && playerEnabled) {
            playUploadQueue(
                items = visibleUploads().filter(::canOpenUploadItem),
                currentItem = item,
                sourceLabel = tr("Shared Uploads")
            )
        } else {
            openFileWithComposeFeedback(file)
        }
    }

    fun openSelectedUploads() {
        val items = selectedUploadItems().filter(::canOpenUploadItem)
        if (items.isEmpty()) {
            return
        }
        if (items.size == 1) {
            openUploadItem(items.first())
            return
        }
        val audioOnly = items.all { item ->
            categoryManager.getCategoryForFile(item.file) == Category.AUDIO &&
                playerService.isPlayable(item.file) &&
                playerEnabled
        }
        if (audioOnly) {
            playUploadQueue(
                items = items,
                currentItem = items.first(),
                sourceLabel = tr("Selected Uploads")
            )
            return
        }
        items.forEach(::openUploadItem)
    }

    fun revealUploadItem(item: UploadItem) {
        if (!canOpenUploadItem(item)) {
            return
        }
        revealFileWithComposeFeedback(item.file)
    }

    fun revealSelectedUploads() {
        selectedUploadItems().forEach(::revealUploadItem)
    }

    fun canBrowseUploadSources(item: UploadItem? = selectedUploadItem()): Boolean {
        return browseablePresences(listOfNotNull(item?.remoteHost)).isNotEmpty()
    }

    fun uploadBrowseTargets(item: UploadItem? = selectedUploadItem()): List<BrowseSourceTarget> {
        return browseSourceTargetsForRemoteHosts(listOfNotNull(item?.remoteHost))
    }

    fun browseUploadSources(item: UploadItem? = selectedUploadItem()) {
        browseRemoteHosts(listOfNotNull(item?.remoteHost))
    }

    fun uploadBlockTargets(item: UploadItem? = selectedUploadItem()): List<RemoteUserTarget> {
        return blockTargetsForRemoteHosts(listOfNotNull(item?.remoteHost))
    }

    fun blockUploadUsers(item: UploadItem? = selectedUploadItem()) {
        confirmBlockUsers(uploadBlockTargets(item))
    }

    fun addSelectedUploadsToCollection(collectionId: Int) {
        val collection = sharedLists.firstOrNull { it.id == collectionId } ?: return
        addFilesToCollection(
            collection = collection,
            files = selectedUploadCollectionFiles(),
            sourceLabel = tr("Uploads")
        )
    }

    fun locateUploadItem(item: UploadItem) {
        if (!canOpenUploadItem(item)) {
            return
        }
        val bestSectionId = bestLibrarySectionIdForFile(item.file)
        if (bestSectionId != null) {
            showFileInTarget(item.file, LibraryJumpTarget(bestSectionId, targetLabelForSection(bestSectionId)))
        } else {
            showMyFilesForLocate()
        }
    }

    fun locateSelectedUploads() {
        selectedUploadItems().firstOrNull()?.let(::locateUploadItem)
    }

    fun uploadJumpTargets(item: UploadItem): List<LibraryJumpTarget> {
        return libraryJumpTargetsForFile(item.file)
    }

    fun showUploadInTarget(item: UploadItem, target: LibraryJumpTarget) {
        showFileInTarget(item.file, target)
    }

    fun removeUploadItem(item: UploadItem) {
        if (!item.isFinished) {
            item.cancel()
        }
        if (!isEd2kUploadItem(item)) {
            uploadListManager.remove(item)
        }
        val selectionKey = uploadSelectionKey(item)
        if (selectedUploadUrn == selectionKey) {
            selectedUploadUrn = null
        }
        selectedUploadUrns.remove(selectionKey)
    }

    fun removeSelectedUploads() {
        selectedUploadItems().toList().forEach(::removeUploadItem)
    }

    fun hasOpenConversation(friendId: String): Boolean = chatConversations.containsKey(friendId)

    fun selectConversation(friend: FriendRosterItem) {
        val conversation = ensureConversation(friend)
        conversation.offTheRecordEnabled = if (friend.supportsOffTheRecord) {
            friendService.isOffTheRecordEnabled(friend.id)
        } else {
            null
        }
        selectedConversationId = conversation.friend.id
        friendService.markConversationViewed(friend.id)
        currentScreen = ComposeScreen.Friends
    }

    fun closeConversation(friendId: String) {
        val conversation = chatConversations.remove(friendId) ?: return
        publishConversationLocalState(conversation, ChatState.active)
        friendService.closeConversation(friendId)
        if (selectedConversationId == friendId) {
            selectedConversationId = chatConversations.keys.firstOrNull()
        }
    }

    fun supportsConversationOffTheRecord(conversation: ChatConversationState): Boolean {
        return conversation.friend.supportsOffTheRecord
    }

    fun toggleConversationOffTheRecord(conversation: ChatConversationState) {
        if (!supportsConversationOffTheRecord(conversation)) {
            return
        }
        try {
            friendService.toggleOffTheRecord(conversation.friend.id)
            conversation.offTheRecordEnabled = friendService.isOffTheRecordEnabled(conversation.friend.id)
        } catch (failure: FriendException) {
            showNotice(
                tr("Chat"),
                failure.message ?: failure.javaClass.simpleName,
                OperationNoticeLevel.ERROR
            )
        }
    }

    fun supportsConversationFileOffers(conversation: ChatConversationState): Boolean {
        return conversation.friend.supportsFileOffers
    }

    fun updateConversationDraft(conversation: ChatConversationState, text: String) {
        conversation.draft = text
    }

    fun publishConversationLocalState(conversation: ChatConversationState, state: ChatState) {
        if (conversation.localState == state) {
            return
        }
        conversation.localState = state
        try {
            friendService.setChatState(conversation.friend.id, state)
        } catch (_: FriendException) {
        }
    }

    fun shareFileWithConversation(conversation: ChatConversationState) {
        if (!supportsConversationFileOffers(conversation)) {
            return
        }
        val file = filePicker.chooseFiles(
            parent = windowRef,
            title = tr("Share File"),
            multiple = false
        ).firstOrNull() ?: return
        try {
            friendService.offerFile(conversation.friend.id, file)
        } catch (failure: Exception) {
            showNotice(
                tr("Chat"),
                failure.message ?: tr("WireShare could not share that file."),
                OperationNoticeLevel.ERROR
            )
        }
    }

    fun shareFolderWithConversation(conversation: ChatConversationState) {
        if (!supportsConversationFileOffers(conversation)) {
            return
        }
        val folder = filePicker.chooseFiles(
            parent = windowRef,
            title = tr("Share Folder"),
            directoriesOnly = true,
            multiple = false
        ).firstOrNull() ?: return
        try {
            friendService.offerFolder(conversation.friend.id, folder)
        } catch (failure: Exception) {
            showNotice(
                tr("Chat"),
                failure.message ?: tr("WireShare could not share that folder."),
                OperationNoticeLevel.ERROR
            )
        }
    }

    fun sendConversationMessage(conversation: ChatConversationState) {
        val text = conversation.draft.trim()
        if (text.isEmpty()) {
            return
        }

        try {
            friendService.sendMessage(conversation.friend.id, text)
            conversation.draft = ""
            publishConversationLocalState(conversation, ChatState.active)
        } catch (failure: FriendException) {
            showNotice(
                tr("Chat"),
                failure.message ?: failure.javaClass.simpleName,
                OperationNoticeLevel.ERROR
            )
        }
    }

    fun canDownloadConversationFileOffer(message: ConversationMessage): Boolean {
        val state = message.fileOffer?.downloadState
        return message.isFileOffer &&
            message.isIncoming &&
            (state == null || state == DownloadState.CANCELLED || state == DownloadState.ERROR || state == DownloadState.STALLED)
    }

    fun canRetryConversationFileOffer(message: ConversationMessage): Boolean {
        val state = message.fileOffer?.downloadState
        return message.isFileOffer &&
            message.isIncoming &&
            (state == DownloadState.CANCELLED || state == DownloadState.ERROR || state == DownloadState.STALLED)
    }

    fun downloadConversationFileOffer(message: ConversationMessage) {
        if (!message.isFileOffer) {
            return
        }
        try {
            friendService.downloadFileOffer(message.id)
            showNotice(
                tr("Chat"),
                tr("Downloading \"{0}\" from chat.", message.fileOffer?.fileName ?: tr("shared file")),
                OperationNoticeLevel.INFO
            )
        } catch (failure: DownloadException) {
            showNotice(
                tr("Chat"),
                failure.message ?: tr("WireShare could not download that shared file."),
                OperationNoticeLevel.ERROR
            )
        } catch (failure: Exception) {
            showNotice(
                tr("Chat"),
                failure.message ?: tr("WireShare could not download that shared file."),
                OperationNoticeLevel.ERROR
            )
        }
    }

    fun canBrowseConversationFileOffer(message: ConversationMessage): Boolean {
        return message.isFileOffer && canBrowseFriend(message.friendId)
    }

    fun browseConversationFileOffer(message: ConversationMessage) {
        if (canBrowseConversationFileOffer(message)) {
            browseFriendLibrary(message.friendId)
        }
    }

    fun conversationFileOfferJumpTargets(message: ConversationMessage): List<LibraryJumpTarget> {
        val file = message.fileOffer?.localPath?.let(::File) ?: return emptyList()
        return libraryJumpTargetsForFile(file, excludeSectionId = null)
    }

    fun canShowConversationFileOfferInMyFiles(message: ConversationMessage): Boolean {
        return conversationFileOfferJumpTargets(message).isNotEmpty()
    }

    fun showConversationFileOfferInTarget(message: ConversationMessage, target: LibraryJumpTarget) {
        val file = message.fileOffer?.localPath?.let(::File) ?: return
        showFileInTarget(file, target)
    }

    fun conversationFileOfferIdentity(message: ConversationMessage): FileIdentityPresentation? {
        val offer = message.fileOffer ?: return null
        val localFile = offer.localPath
            ?.takeIf(String::isNotBlank)
            ?.let(::File)
            ?.takeIf(File::exists)
        return if (localFile != null) {
            fileAppearanceService.presentation(
                file = localFile,
                category = categoryManager.getCategoryForFile(localFile),
                title = offer.fileName,
                subtitle = offer.description ?: localFile.name
            )
        } else {
            val category = categoryManager.getCategoryForFilename(offer.fileName)
            FileIdentityPresentation(
                title = offer.fileName,
                subtitle = offer.description ?: category.getSingularName(),
                icon = fileAppearanceService.iconForFileName(offer.fileName, category)
            )
        }
    }

    fun togglePlayerPlayback() {
        when (playerState) {
            PlayerState.PLAYING,
            PlayerState.SEEKING_PLAY -> playerService.pause()
            else -> {
                markPlayerPlaybackAttempt()
                playerService.resume()
            }
        }
    }

    fun canTogglePlayerFromShortcut(): Boolean = playerVisible && playerCurrentFile != null

    fun seekPlayer(progress: Float) {
        playerService.seek(progress)
    }

    fun previousTrack() {
        if (playerQueue.isNotEmpty()) {
            val targetIndex = previousPlayerQueueIndex()
            if (targetIndex >= 0) {
                playPlayerQueueIndex(targetIndex)
            }
            return
        }
        playerService.previous()
    }

    fun nextTrack() {
        if (playerQueue.isNotEmpty()) {
            val targetIndex = nextPlayerQueueIndex()
            if (targetIndex >= 0) {
                playPlayerQueueIndex(targetIndex)
            }
            return
        }
        playerService.next()
    }

    fun toggleShuffle() {
        val next = !playerService.isShuffle()
        playerService.setShuffle(next)
        playerShuffle = next
    }

    fun updatePlayerVolume(volume: Float) {
        val bounded = volume.coerceIn(0f, 1f)
        settingsService.setPlayerVolume(bounded)
        playerService.setVolume(bounded)
        playerVolume = bounded
    }

    fun stopPlayer() {
        playerService.stop()
    }

    fun openCurrentPlayerFile() {
        playerCurrentFile?.let(launcher::open)
    }

    fun revealCurrentPlayerFile() {
        playerCurrentFile?.let(launcher::reveal)
    }

    fun playPlayerQueueEntry(index: Int) {
        playPlayerQueueIndex(index)
    }

    fun removePlayerQueueEntry(index: Int) {
        if (index !in playerQueue.indices) {
            return
        }
        if (playerQueueMode == PlayerQueueMode.LIBRARY_LIVE) {
            playerQueueMode = PlayerQueueMode.SNAPSHOT
        }
        val removingCurrent = index == playerQueueIndex
        playerQueue.removeAt(index)
        when {
            playerQueue.isEmpty() -> {
                playerQueueIndex = -1
                playerQueueSourceLabel = tr("Current Session")
                if (removingCurrent) {
                    playerService.stop()
                    refreshPlayerState()
                }
            }

            removingCurrent -> {
                playPlayerQueueIndex(index.coerceAtMost(playerQueue.lastIndex))
            }

            index < playerQueueIndex -> {
                playerQueueIndex = (playerQueueIndex - 1).coerceAtLeast(0)
            }
        }
    }

    fun clearPlayerQueue() {
        playerQueueMode = PlayerQueueMode.NONE
        playerQueue.clear()
        playerQueueIndex = -1
        playerQueueSourceLabel = tr("Current Session")
        refreshPlayerState()
    }

    fun requestExit() {
        if (!shuttingDown.compareAndSet(false, true)) {
            return
        }

        try {
            DesktopIntegration.detach(this)
            advancedToolsWindowOpen = false
            detachAdvancedConsole()
            detachAdvancedMojitoVisualizer()
            searchTabs.forEach {
                it.searchListenerCloseable?.close()
                it.browseStatusCloseable?.close()
                it.binding?.close()
            }
            activeLibraryBinding?.close()
            activeSharedFriendIdsBinding?.close()
            closeables.forEach { closeable ->
                try {
                    closeable.close()
                } catch (_: Exception) {
                }
            }
            if (lifecycleManager.isStarted()) {
                application.stopCore()
            }
        } finally {
            exitHandler?.invoke()
        }
    }

    fun showMessage(title: String, message: String) {
        enqueueMessageDialog(MessageDialogState(title = title, message = message))
    }

    fun closeMessageDialog(checkboxChecked: Boolean = false) {
        messageDialog?.onCloseWithCheckbox?.invoke(checkboxChecked)
        messageDialog = if (pendingMessageDialogs.isEmpty()) null else pendingMessageDialogs.removeFirst()
    }

    fun showNotice(
        title: String,
        message: String,
        level: OperationNoticeLevel = OperationNoticeLevel.INFO
    ) {
        val notice = OperationNotice(
            id = nextOperationNoticeId++,
            title = title,
            message = message,
            level = level
        )
        if (operationNotices.size >= 4) {
            operationNotices.removeAt(0)
        }
        operationNotices += notice
    }

    fun dismissOperationNotice(id: Long) {
        operationNotices.removeAll { it.id == id }
    }

    fun advancedConsoleLevelOptions(): List<String> = ADVANCED_CONSOLE_LEVELS

    fun advancedConnectionStrengthLabel(): String {
        return when (displayedConnectionStrength()) {
            ConnectionStrength.NO_INTERNET -> tr("No Internet")
            ConnectionStrength.DISCONNECTED -> tr("Disconnected")
            ConnectionStrength.CONNECTING -> tr("Connecting")
            ConnectionStrength.WEAK -> "1/5"
            ConnectionStrength.WEAK_PLUS -> "2/5"
            ConnectionStrength.MEDIUM -> "3/5"
            ConnectionStrength.MEDIUM_PLUS -> "4/5"
            ConnectionStrength.FULL -> "5/5"
            ConnectionStrength.TURBO -> "6/5"
        }
    }

    fun advancedNodeRoleText(): String {
        return if (connectionManager.isConnected) {
            if (connectionManager.isUltrapeer) tr("You are an Ultrapeer node.") else tr("You are a Leaf node.")
        } else {
            when (displayedConnectionStrength()) {
                ConnectionStrength.NO_INTERNET -> tr("You are not connected to the Internet.")
                ConnectionStrength.DISCONNECTED -> tr("You are connected to the Internet, but not connected to the Gnutella network.")
                else -> tr("WireShare is still establishing its Gnutella connections.")
            }
        }
    }

    fun advancedNodeRoleBadgeLabel(): String {
        return when {
            !displayedConnectionStrength().isOnline -> tr("Offline")
            displayedConnectionStrength() == ConnectionStrength.CONNECTING -> tr("Connecting")
            connectionManager.isUltrapeer -> tr("Ultrapeer")
            else -> tr("Leaf")
        }
    }

    fun advancedFirewallStatusText(): String {
        return when (advancedToolsService.firewallStatus()) {
            FirewallStatus.FIREWALLED -> {
                when (advancedToolsService.firewallTransferStatusEvent()?.data) {
                    FirewallTransferStatus.SUPPORTS_FWT -> tr("You are behind a firewall, but peer-to-peer transfers can still get through.")
                    FirewallTransferStatus.DOES_NOT_SUPPORT_FWT -> tr("You are behind a firewall, so some peer-to-peer transfers may fail.")
                    null -> tr("You are behind a firewall.")
                }
            }
            FirewallStatus.NOT_FIREWALLED -> tr("You are not behind a firewall.")
            null -> tr("Firewall status is not available yet.")
        }
    }

    fun advancedFirewallReasonText(): String? {
        return when (advancedToolsService.firewallTransferStatusEvent()?.type) {
            FWTStatusReason.INVALID_EXTERNAL_ADDRESS ->
                tr("WireShare could not determine your external IP address.")
            FWTStatusReason.NO_SOLICITED_INCOMING_MESSAGES ->
                tr("No incoming UDP traffic was detected.")
            FWTStatusReason.REUSING_STATUS_FROM_PREVIOUS_SESSION ->
                tr("Firewall transfer support could not be confirmed in a previous session.")
            FWTStatusReason.PORT_UNSTABLE ->
                tr("Your firewall or router keeps changing the external port.")
            FWTStatusReason.UNKNOWN,
            null -> null
        }?.takeIf { it.isNotBlank() }
    }

    fun advancedConnectedPeersText(): String {
        val connected = advancedConnections.filter(ConnectionItem::isConnected)
        val leafCount = connected.count(ConnectionItem::isLeaf)
        val ultrapeerCount = connected.count(ConnectionItem::isUltrapeer)
        val peerCount = connected.count(ConnectionItem::isPeer)
        return tr(
            "{0} total · {1} leaf · {2} ultrapeer · {3} peer",
            connected.size,
            leafCount,
            ultrapeerCount,
            peerCount
        )
    }

    fun ed2kServerStatusLabel(): String {
        return when (ed2kStatus.serverState) {
            Ed2kStatus.ConnectionState.DISCONNECTED -> tr("Disconnected")
            Ed2kStatus.ConnectionState.CONNECTING -> tr("Connecting")
            Ed2kStatus.ConnectionState.CONNECTED -> tr("Connected")
        }
    }

    fun ed2kKadStatusLabel(): String {
        val base = when (ed2kStatus.kadState) {
            Ed2kStatus.ConnectionState.DISCONNECTED -> tr("Disconnected")
            Ed2kStatus.ConnectionState.CONNECTING -> tr("Connecting")
            Ed2kStatus.ConnectionState.CONNECTED -> tr("Connected")
        }
        return when (ed2kStatus.kadBootstrapState) {
            Ed2kStatus.KadBootstrapState.NOT_BOOTSTRAPPED -> tr("{0} · No nodes", base)
            Ed2kStatus.KadBootstrapState.BOOTSTRAPPING -> tr("{0} · Bootstrapping · {1} nodes", base, ed2kStatus.kadContactCount)
            Ed2kStatus.KadBootstrapState.BOOTSTRAPPED -> tr("{0} · {1} nodes", base, ed2kStatus.kadContactCount)
        }
    }

    fun ed2kConnectedServerText(): String {
        return ed2kStatus.serverStatusDetail?.takeIf(String::isNotBlank)
            ?: ed2kStatus.connectedServerName?.takeIf(String::isNotBlank)
            ?: tr("No ED2K server connected")
    }

    fun ed2kSummaryText(): String {
        return tr(
            "{0} server(s) · {1} download(s) · {2} upload(s) · {3} shared · {4} Kad nodes",
            ed2kStatus.serverCount,
            ed2kStatus.downloadCount,
            ed2kStatus.uploadCount,
            ed2kStatus.sharedFileCount,
            ed2kStatus.kadContactCount
        )
    }

    fun canConnectEd2kServer(): Boolean = ed2kStatus.serverCount > 0 && ed2kStatus.serverState != Ed2kStatus.ConnectionState.CONNECTED

    fun canDisconnectEd2kServer(): Boolean = ed2kStatus.serverState != Ed2kStatus.ConnectionState.DISCONNECTED

    fun canConnectKad(): Boolean = ed2kStatus.kadState != Ed2kStatus.ConnectionState.CONNECTED

    fun canDisconnectKad(): Boolean = ed2kStatus.kadState != Ed2kStatus.ConnectionState.DISCONNECTED

    fun connectAnyEd2kServer() {
        runCatching {
            advancedToolsService.ed2kConnectAnyServer()
            advancedEd2kError = null
        }.onFailure { failure ->
            advancedEd2kError = failure.message ?: tr("Unable to connect to an ED2K server.")
            showNotice(tr("ED2K"), advancedEd2kError ?: "", OperationNoticeLevel.ERROR)
        }
    }

    fun disconnectEd2kServer() {
        runCatching {
            advancedToolsService.ed2kDisconnectServer()
            advancedEd2kError = null
        }.onFailure { failure ->
            advancedEd2kError = failure.message ?: tr("Unable to disconnect from the ED2K server.")
            showNotice(tr("ED2K"), advancedEd2kError ?: "", OperationNoticeLevel.ERROR)
        }
    }

    fun connectKad() {
        runCatching {
            advancedToolsService.ed2kConnectKad()
            advancedEd2kError = null
        }.onFailure { failure ->
            advancedEd2kError = failure.message ?: tr("Unable to connect Kad.")
            showNotice(tr("Kad"), advancedEd2kError ?: "", OperationNoticeLevel.ERROR)
        }
    }

    fun disconnectKad() {
        runCatching {
            advancedToolsService.ed2kDisconnectKad()
            advancedEd2kError = null
        }.onFailure { failure ->
            advancedEd2kError = failure.message ?: tr("Unable to disconnect Kad.")
            showNotice(tr("Kad"), advancedEd2kError ?: "", OperationNoticeLevel.ERROR)
        }
    }

    fun submitEd2kServerConnect() {
        val host = advancedEd2kServerHost.trim()
        val port = advancedEd2kServerPort.trim().toIntOrNull()
        when {
            host.isEmpty() -> advancedEd2kError = tr("Enter an ED2K server host.")
            port == null || port !in 1..65535 -> advancedEd2kError = tr("Enter a valid ED2K server port.")
            else -> runCatching {
                advancedToolsService.ed2kConnectServer(host, port)
                advancedEd2kError = null
            }.onFailure { failure ->
                advancedEd2kError = failure.message ?: tr("Unable to connect to the ED2K server.")
                showNotice(tr("ED2K"), advancedEd2kError ?: "", OperationNoticeLevel.ERROR)
            }
        }
    }

    fun importEd2kServers() {
        val file = filePicker.chooseFiles(
            parent = windowRef,
            title = tr("Import ED2K Server List (server.met or .txt)"),
            multiple = false,
            filenameFilter = FilenameFilter { _, name ->
                name.endsWith(".met", ignoreCase = true) || name.endsWith(".txt", ignoreCase = true)
            }
        ).firstOrNull() ?: return
        runCatching {
            advancedToolsService.ed2kImportServers(file)
            advancedEd2kError = null
            showNotice(tr("ED2K"), tr("Imported ED2K server list."), OperationNoticeLevel.SUCCESS)
        }.onFailure { failure ->
            advancedEd2kError = failure.message ?: tr("Unable to import the ED2K server list.")
            showNotice(tr("ED2K"), advancedEd2kError ?: "", OperationNoticeLevel.ERROR)
        }
    }

    fun importKadNodes() {
        val file = filePicker.chooseFiles(
            parent = windowRef,
            title = tr("Import Kad Nodes (nodes.dat)"),
            multiple = false,
            filenameFilter = FilenameFilter { _, name ->
                name.endsWith(".dat", ignoreCase = true)
            }
        ).firstOrNull() ?: return
        runCatching {
            advancedToolsService.ed2kImportKadNodes(file)
            advancedEd2kError = null
            showNotice(tr("Kad"), tr("Imported Kad nodes."), OperationNoticeLevel.SUCCESS)
        }.onFailure { failure ->
            advancedEd2kError = failure.message ?: tr("Unable to import Kad nodes.")
            showNotice(tr("Kad"), advancedEd2kError ?: "", OperationNoticeLevel.ERROR)
        }
    }

    private fun displayedConnectionStrength(): ConnectionStrength {
        if (connectionStrengthState == ConnectionStrength.DISCONNECTED &&
            advancedConnections.any { !it.isConnected && it.status == ConnectionItem.Status.CONNECTING }) {
            return ConnectionStrength.CONNECTING
        }
        return connectionStrengthState
    }

    fun bootstrapKad() {
        val host = advancedKadBootstrapHost.trim()
        val port = advancedKadBootstrapPort.trim().toIntOrNull()
        when {
            host.isEmpty() -> advancedEd2kError = tr("Enter a Kad bootstrap host.")
            port == null || port !in 1..65535 -> advancedEd2kError = tr("Enter a valid Kad bootstrap port.")
            else -> runCatching {
                advancedToolsService.ed2kBootstrapKad(host, port)
                advancedEd2kError = null
                showNotice(tr("Kad"), tr("Started Kad bootstrap from {0}:{1}.", host, port), OperationNoticeLevel.SUCCESS)
            }.onFailure { failure ->
                advancedEd2kError = failure.message ?: tr("Unable to bootstrap Kad.")
                showNotice(tr("Kad"), advancedEd2kError ?: "", OperationNoticeLevel.ERROR)
            }
        }
    }

    private fun attachAdvancedConsole() {
        if (!consoleAvailable || advancedConsoleCloseable != null) {
            return
        }
        refreshConsoleLoggerNames()
        advancedConsoleCloseable = advancedToolsService.attachConsole { text ->
            runOnUi {
                if (appliedConsoleDelaySeconds == 0) {
                    appendToConsole(text)
                } else {
                    synchronized(advancedConsoleDelayBuffer) {
                        advancedConsoleDelayBuffer.append(text)
                    }
                }
            }
        }
    }

    private fun detachAdvancedConsole() {
        flushDelayedConsoleOutput(force = true)
        try {
            advancedConsoleCloseable?.close()
        } catch (_: Exception) {
        } finally {
            advancedConsoleCloseable = null
        }
    }

    private fun syncAdvancedMojitoVisualizer() {
        val shouldRender = advancedToolsWindowOpen &&
            selectedAdvancedToolsTab == AdvancedToolsTab.MOJITO &&
            advancedToolsDhtRunning &&
            advancedMojitoVisualizerAvailable
        if (!shouldRender) {
            detachAdvancedMojitoVisualizer()
            return
        }
        if (advancedMojitoVisualizerSession != null && advancedMojitoVisualizerComponent != null) {
            return
        }
        val session = advancedToolsService.openMojitoVisualizer()
        if (session == null) {
            detachAdvancedMojitoVisualizer()
            return
        }
        advancedMojitoVisualizerSession = session
        advancedMojitoVisualizerTitle = session.title
        advancedMojitoVisualizerComponent = session.component()
        if (advancedMojitoVisualizerComponent == null) {
            detachAdvancedMojitoVisualizer()
        }
    }

    private fun detachAdvancedMojitoVisualizer() {
        try {
            advancedMojitoVisualizerSession?.close()
        } catch (_: Exception) {
        } finally {
            advancedMojitoVisualizerSession = null
            advancedMojitoVisualizerTitle = ""
            advancedMojitoVisualizerComponent = null
        }
    }

    private fun flushDelayedConsoleOutput(force: Boolean) {
        if (appliedConsoleDelaySeconds == 0) {
            synchronized(advancedConsoleDelayBuffer) {
                if (advancedConsoleDelayBuffer.isNotEmpty()) {
                    val text = advancedConsoleDelayBuffer.toString()
                    advancedConsoleDelayBuffer.setLength(0)
                    appendToConsole(text)
                }
            }
            advancedConsoleLastFlushAt = System.currentTimeMillis()
            return
        }
        val now = System.currentTimeMillis()
        if (!force && now - advancedConsoleLastFlushAt < appliedConsoleDelaySeconds * 1000L) {
            return
        }
        synchronized(advancedConsoleDelayBuffer) {
            if (advancedConsoleDelayBuffer.isEmpty()) {
                advancedConsoleLastFlushAt = now
                return
            }
            val text = advancedConsoleDelayBuffer.toString()
            advancedConsoleDelayBuffer.setLength(0)
            appendToConsole(text)
        }
        advancedConsoleLastFlushAt = now
    }

    private fun appendToConsole(text: String) {
        if (text.isEmpty()) {
            return
        }
        consoleText += text
        val excess = consoleText.length - ADVANCED_CONSOLE_IDEAL_SIZE
        if (excess >= ADVANCED_CONSOLE_MAX_EXCESS) {
            consoleText = consoleText.drop(excess)
        }
    }

    private fun connectionKey(item: ConnectionItem): String {
        return "${item.hostName}:${item.port}:${item.time}"
    }

    private fun defaultConnectionSortDescending(column: ConnectionColumn): Boolean {
        return when (column) {
            ConnectionColumn.HOST,
            ConnectionColumn.STATUS,
            ConnectionColumn.PROTOCOL,
            ConnectionColumn.VENDOR_VERSION -> false
            else -> true
        }
    }

    private fun saveConnectionLayoutPreferences() {
        settingsService.saveConnectionLayoutPreferences(
            ConnectionLayoutPreferences(
                visibleColumns = visibleConnectionColumns,
                sortColumn = connectionSortColumn,
                sortDescending = connectionSortDescending
            )
        )
    }

    private fun compareConnectionItems(
        first: ConnectionItem,
        second: ConnectionItem,
        column: ConnectionColumn
    ): Int {
        return when (column) {
            ConnectionColumn.HOST ->
                compareValuesBy(first, second, { it.hostName.lowercase(Locale.US) }, { it.port })
            ConnectionColumn.STATUS ->
                compareValuesBy(first, second, { connectionStatusLabel(it) }, { it.time })
            ConnectionColumn.MESSAGES_IO ->
                compareValues(messageIoValue(first), messageIoValue(second))
            ConnectionColumn.MESSAGES_IN ->
                compareValues(first.numMessagesReceived, second.numMessagesReceived)
            ConnectionColumn.MESSAGES_OUT ->
                compareValues(first.numMessagesSent, second.numMessagesSent)
            ConnectionColumn.BANDWIDTH_IO ->
                compareValues(bandwidthIoValue(first), bandwidthIoValue(second))
            ConnectionColumn.BANDWIDTH_IN ->
                compareValues(first.measuredDownstreamBandwidth, second.measuredDownstreamBandwidth)
            ConnectionColumn.BANDWIDTH_OUT ->
                compareValues(first.measuredUpstreamBandwidth, second.measuredUpstreamBandwidth)
            ConnectionColumn.DROPPED_IO ->
                compareValues(droppedIoValue(first), droppedIoValue(second))
            ConnectionColumn.DROPPED_IN ->
                compareValues(droppedInValue(first), droppedInValue(second))
            ConnectionColumn.DROPPED_OUT ->
                compareValues(droppedOutValue(first), droppedOutValue(second))
            ConnectionColumn.PROTOCOL ->
                compareValues(protocolWeight(first), protocolWeight(second))
            ConnectionColumn.VENDOR_VERSION ->
                compareValuesBy(first, second, { it.userAgent.orEmpty().lowercase(Locale.US) }, { it.time })
            ConnectionColumn.TIME ->
                compareValues(first.time, second.time)
            ConnectionColumn.COMPRESSED_IO ->
                compareValues(compressionIoValue(first), compressionIoValue(second))
            ConnectionColumn.COMPRESSED_IN ->
                compareValues(first.readSavedFromCompression, second.readSavedFromCompression)
            ConnectionColumn.COMPRESSED_OUT ->
                compareValues(first.sentSavedFromCompression, second.sentSavedFromCompression)
            ConnectionColumn.SSL_OVERHEAD_IO ->
                compareValues(sslIoValue(first), sslIoValue(second))
            ConnectionColumn.SSL_OVERHEAD_IN ->
                compareValues(first.readLostFromSSL, second.readLostFromSSL)
            ConnectionColumn.SSL_OVERHEAD_OUT ->
                compareValues(first.sentLostFromSSL, second.sentLostFromSSL)
            ConnectionColumn.QRP_PERCENT ->
                compareValues(first.queryRouteTablePercentFull, second.queryRouteTablePercentFull)
            ConnectionColumn.QRP_EMPTY ->
                compareValues(first.queryRouteTableEmptyUnits, second.queryRouteTableEmptyUnits)
        }.takeUnless { it == 0 } ?: compareValuesBy(
            first,
            second,
            { it.hostName.lowercase(Locale.US) },
            { it.port },
            { it.time }
        )
    }

    private fun connectionStatusLabel(item: ConnectionItem): String {
        return when (item.status) {
            ConnectionItem.Status.CONNECTING -> tr("Connecting...")
            ConnectionItem.Status.OUTGOING -> tr("Outgoing")
            ConnectionItem.Status.INCOMING -> tr("Incoming")
        }
    }

    private fun protocolWeight(item: ConnectionItem): Int {
        return when {
            item.isUltrapeerConnection && item.isUltrapeer -> 1
            item.isLeaf -> 2
            !item.isUltrapeerConnection && !item.isLeaf -> 3
            item.isUltrapeerConnection -> 4
            else -> 5
        }
    }

    private fun messageIoValue(item: ConnectionItem): Int = item.numMessagesReceived + item.numMessagesSent

    private fun bandwidthIoValue(item: ConnectionItem): Float =
        item.measuredDownstreamBandwidth + item.measuredUpstreamBandwidth

    private fun droppedInValue(item: ConnectionItem): Float {
        return item.numReceivedMessagesDropped / (item.numMessagesReceived + 1f)
    }

    private fun droppedOutValue(item: ConnectionItem): Float {
        return item.numSentMessagesDropped / (item.numMessagesSent + 1f)
    }

    private fun droppedIoValue(item: ConnectionItem): Float = droppedInValue(item) + droppedOutValue(item)

    private fun compressionIoValue(item: ConnectionItem): Float {
        return item.readSavedFromCompression + item.sentSavedFromCompression
    }

    private fun sslIoValue(item: ConnectionItem): Float {
        return item.readLostFromSSL + item.sentLostFromSSL
    }

    private fun formatTorrentRatioInput(value: Float): String {
        return String.format(Locale.US, "%.2f", value).trimEnd('0').trimEnd('.')
    }

    private fun wholeDays(totalSeconds: Int): Int {
        return kotlin.math.floor(totalSeconds.toDouble() / (60 * 60 * 24).toDouble()).toInt()
    }

    private fun remainderHours(totalSeconds: Int, days: Int): Int {
        return kotlin.math.round(totalSeconds.toDouble() / (60 * 60).toDouble() - days * 24).toInt()
    }

    private fun parseSeedTimeSeconds(daysValue: String, hoursValue: String): Int? {
        val days = parseBoundedInt(
            value = daysValue,
            min = wholeDays(BittorrentSettings.LIBTORRENT_SEED_TIME_LIMIT.getMinValue().toInt()),
            max = wholeDays(BittorrentSettings.LIBTORRENT_SEED_TIME_LIMIT.getMaxValue().toInt()),
            label = tr("Maximum days")
        ) ?: return null
        val hours = parseBoundedInt(
            value = hoursValue,
            min = 0,
            max = 24,
            label = tr("Hours")
        ) ?: return null
        return days * 24 * 60 * 60 + hours * 60 * 60
    }

    private fun parseBoundedInt(
        value: String,
        min: Int,
        max: Int,
        label: String
    ): Int? {
        val parsed = value.trim().toIntOrNull() ?: return null
        return parsed.takeIf { it in min..max }
    }

    private fun parseBoundedFloat(
        value: String,
        min: Float,
        max: Float,
        label: String
    ): Float? {
        val parsed = value.trim().toFloatOrNull() ?: return null
        return parsed.takeIf { it in min..max }
    }

    private fun effectiveDownloadBandwidthMaxKiB(): Int {
        return if (DownloadSettings.LIMIT_MAX_DOWNLOAD_SPEED.getValue()) {
            DownloadSettings.MAX_DOWNLOAD_SPEED.getValue().toInt() / 1024
        } else {
            DownloadSettings.MAX_DOWNLOAD_SPEED.getMaxValue().toInt() / 1024
        }
    }

    private fun effectiveUploadBandwidthMaxKiB(): Int {
        return if (UploadSettings.LIMIT_MAX_UPLOAD_SPEED.getValue()) {
            UploadSettings.MAX_UPLOAD_SPEED.getValue().toInt() / 1024
        } else {
            UploadSettings.MAX_UPLOAD_SPEED.getMaxValue().toInt() / 1024
        }
    }

    private fun coalescePieceStates(
        piecesInfo: DownloadPiecesInfo,
        startIndex: Int,
        piecesToCoalesce: Int
    ): TorrentPieceCellPresentation {
        var completedScore = 0
        var workingState: TorrentPieceCellState? = null

        for (index in startIndex until startIndex + piecesToCoalesce) {
            val state = normalizePieceState(piecesInfo.getPieceState(index))
            when (state) {
                TorrentPieceCellState.ACTIVE -> return TorrentPieceCellPresentation(TorrentPieceCellState.ACTIVE)
                TorrentPieceCellState.UNAVAILABLE -> return TorrentPieceCellPresentation(TorrentPieceCellState.UNAVAILABLE)
                TorrentPieceCellState.PARTIAL -> completedScore += 1
                TorrentPieceCellState.DOWNLOADED -> completedScore += 2
                TorrentPieceCellState.AVAILABLE -> Unit
            }

            workingState = if (workingState != null && workingState != state) {
                TorrentPieceCellState.PARTIAL
            } else {
                state
            }
        }

        return if (workingState == TorrentPieceCellState.PARTIAL) {
            val completedScoreMax = piecesToCoalesce * 2
            TorrentPieceCellPresentation(
                state = TorrentPieceCellState.PARTIAL,
                intensity = (completedScore.toFloat() / completedScoreMax.toFloat()).coerceIn(0f, 1f)
            )
        } else {
            TorrentPieceCellPresentation(workingState ?: TorrentPieceCellState.AVAILABLE)
        }
    }

    private fun normalizePieceState(state: DownloadPiecesInfo.PieceState): TorrentPieceCellState {
        return when (state) {
            DownloadPiecesInfo.PieceState.DOWNLOADED -> TorrentPieceCellState.DOWNLOADED
            DownloadPiecesInfo.PieceState.PARTIAL,
            DownloadPiecesInfo.PieceState.UNAVAILABLE_PARTIAL -> TorrentPieceCellState.PARTIAL
            DownloadPiecesInfo.PieceState.AVAILABLE,
            DownloadPiecesInfo.PieceState.QUEUED -> TorrentPieceCellState.AVAILABLE
            DownloadPiecesInfo.PieceState.ACTIVE -> TorrentPieceCellState.ACTIVE
            DownloadPiecesInfo.PieceState.UNAVAILABLE -> TorrentPieceCellState.UNAVAILABLE
        }
    }

    private fun searchTitleForDownload(item: DownloadItem): String {
        val title = item.getPropertyString(FilePropertyKey.TITLE)
        val resolved = title?.takeIf { it.isNotBlank() } ?: FileUtils.getFilenameNoExtension(item.fileName)
        return resolved.take(160)
    }

    private fun canPreviewDownload(item: DownloadItem): Boolean {
        return item.isLaunchable &&
            !item.state.isFinished &&
            item.category != Category.PROGRAM
    }

    private fun browseRemoteHosts(remoteHosts: Collection<RemoteHost>) {
        browseSourceTargets(browseSourceTargetsForRemoteHosts(remoteHosts))
    }

    private fun browseablePresences(remoteHosts: Collection<RemoteHost>): List<FriendPresence> {
        return remoteHosts
            .asSequence()
            .filter(RemoteHost::isBrowseHostEnabled)
            .mapNotNull(RemoteHost::getFriendPresence)
            .distinctBy(FriendPresence::getPresenceId)
            .toList()
    }

    private fun browseSourceTargetsForRemoteHosts(remoteHosts: Collection<RemoteHost>): List<BrowseSourceTarget> {
        val targetsByPresenceId = linkedMapOf<String, BrowseSourceTarget>()
        remoteHosts.forEach { host ->
            val presence = host.friendPresence ?: return@forEach
            val friend = presence.friend
            val target = BrowseSourceTarget(
                id = presence.presenceId,
                label = friend.renderName,
                enabled = host.isBrowseHostEnabled,
                anonymous = friend.isAnonymous,
                presence = presence
            )
            val existing = targetsByPresenceId[target.id]
            if (existing == null || (!existing.enabled && target.enabled)) {
                targetsByPresenceId[target.id] = target
            }
        }
        return targetsByPresenceId.values.sortedBy { it.label.lowercase(Locale.US) }
    }

    fun browseSourceTarget(target: BrowseSourceTarget) {
        if (!target.enabled) {
            return
        }
        val presence = target.presence ?: return
        val request = browseService.browsePresence(presence)
        openBrowseTab(request)
        request.search.start()
    }

    fun browseSourceTargets(targets: List<BrowseSourceTarget>) {
        val presences = targets.filter(BrowseSourceTarget::enabled).mapNotNull(BrowseSourceTarget::presence)
        val request = when (presences.size) {
            0 -> null
            1 -> browseService.browsePresence(presences.first())
            else -> browseService.browsePresences(presences)
        } ?: return
        openBrowseTab(request)
        request.search.start()
    }

    fun browseActionLabel(target: BrowseSourceTarget): String {
        return if (target.anonymous) tr("Browse Source") else tr("Browse Friend")
    }

    fun searchResultSourceTarget(result: SearchResult): BrowseSourceTarget? {
        return browseSourceTargetsForRemoteHosts(listOf(result.source)).firstOrNull()
    }

    private fun blockTargetsForRemoteHosts(remoteHosts: Collection<RemoteHost>): List<RemoteUserTarget> {
        return remoteHosts
            .asSequence()
            .mapNotNull { host ->
                val presence = host.friendPresence ?: return@mapNotNull null
                val friend = presence.friend
                if (!friend.isAnonymous) {
                    return@mapNotNull null
                }
                val id = friend.name.takeIf { it.isNotBlank() } ?: return@mapNotNull null
                RemoteUserTarget(id = id, label = friend.renderName)
            }
            .distinctBy(RemoteUserTarget::id)
            .sortedBy { it.label.lowercase(Locale.US) }
            .toList()
    }

    private fun confirmBlockUsers(targets: List<RemoteUserTarget>) {
        if (targets.isEmpty()) {
            return
        }
        if (!settingsService.confirmBlockUsersEnabled()) {
            targets.forEach { spamManager.addToBlackList(it.id) }
            return
        }
        val message = if (targets.size == 1) {
            tr("Block user {0}?", targets.first().label)
        } else {
            tr("Block {0} users?", targets.size)
        }
        confirmationDialog = ConfirmationDialogState(
            title = tr("Block User"),
            message = message,
            confirmLabel = tr("Block"),
            checkboxLabel = tr("Don’t ask again"),
            onConfirmWithCheckbox = { suppressPrompt ->
                confirmationDialog = null
                settingsService.setConfirmBlockUsersEnabled(!suppressPrompt)
                targets.forEach { spamManager.addToBlackList(it.id) }
            },
            onConfirm = {
                confirmationDialog = null
                targets.forEach { spamManager.addToBlackList(it.id) }
            },
            onDismiss = { confirmationDialog = null }
        )
    }

    private fun applySavedWindowPlacement(window: Window) {
        val preferences = windowPlacementPreferences
        if (!preferences.restoreWindowPlacement) {
            return
        }
        window.setSize(preferences.width.coerceAtLeast(640), preferences.height.coerceAtLeast(480))
        if (preferences.positionsSet) {
            window.setLocation(preferences.x, preferences.y)
        }
        if (preferences.maximized && window is Frame) {
            window.extendedState = window.extendedState or Frame.MAXIMIZED_BOTH
        }
    }

    private fun persistWindowPlacement() {
        val window = windowRef ?: return
        val frame = window as? Frame
        val maximized = frame?.let { it.extendedState and Frame.MAXIMIZED_BOTH == Frame.MAXIMIZED_BOTH } == true
        windowPlacementPreferences = windowPlacementPreferences.copy(
            positionsSet = true,
            width = window.width.coerceAtLeast(640),
            height = window.height.coerceAtLeast(480),
            x = window.x,
            y = window.y,
            maximized = maximized
        )
        settingsService.saveWindowPlacementPreferences(windowPlacementPreferences)
    }

    private fun hideApplicationToTray() {
        if (!trayService.supportsTray()) {
            requestExit()
            return
        }
        (windowRef as? Frame)?.state = Frame.NORMAL
        windowRef?.isVisible = false
        syncDesktopShellState()
    }

    private fun maybeAutoHideTray() {
        if (!closeTrayWhenNoTransfers) {
            return
        }
        val hasActiveDownloads = allDownloads().any { !it.state.isFinished }
        val hasActiveUploads = allUploads().any { !it.isFinished }
        if (!hasActiveDownloads && !hasActiveUploads) {
            trayExpanded = false
        }
    }

    private fun syncDesktopShellState() {
        trayService.updateState(
            appVisible = windowRef?.isVisible == true,
            minimizeToTray = minimizeToTray,
            notificationsEnabled = notificationsEnabled,
            delayedExitPending = delayedExitState.pending
        )
    }

    private fun rememberLibrarySectionState(sectionId: String = selectedLibrarySectionId) {
        librarySectionStates[sectionId] = LibrarySectionViewState(
            filterText = libraryFilterText,
            category = libraryCategoryFilter,
            sortMode = librarySortMode,
            sortDescending = librarySortDescending,
            selectedItemPath = selectedLibraryItemPath,
            selectedItemPaths = selectedLibraryItemPaths.toList(),
            selectionAnchorPath = librarySelectionAnchorPath,
            visibleColumns = visibleLibraryColumns
        )
    }

    private fun clearAllLibraryFilters() {
        libraryFilterText = ""
        libraryCategoryFilter = null
        invalidateLibraryVisibilityCache()
        librarySectionStates.keys.toList().forEach { sectionId ->
            val state = librarySectionStates[sectionId] ?: return@forEach
            librarySectionStates[sectionId] = state.copy(filterText = "", category = null)
        }
        rememberLibrarySectionState()
        syncLiveLibraryQueueIfNeeded()
    }

    private fun restoreLibrarySectionState(sectionId: String) {
        val state = librarySectionStates[sectionId] ?: LibrarySectionViewState()
        libraryFilterText = state.filterText
        libraryCategoryFilter = state.category
        if (!libraryFiltersVisible) {
            libraryFilterText = ""
            libraryCategoryFilter = null
        }
        librarySortMode = state.sortMode
        librarySortDescending = state.sortDescending
        selectedLibraryItemPath = state.selectedItemPath
        selectedLibraryItemPaths.clear()
        selectedLibraryItemPaths.addAll(state.selectedItemPaths)
        librarySelectionAnchorPath = state.selectionAnchorPath
        visibleLibraryColumns = state.visibleColumns
        invalidateLibraryVisibilityCache()
    }

    private fun bindLibraryItems(list: LocalFileList) {
        activeLibraryBinding?.close()
        activeLibraryBinding = EventListBinding(
            list.swingModel,
            libraryItems,
            onChanged = {
                invalidateLibraryVisibilityCache()
                invalidateSearchResultPresentationCache()
                syncLiveLibraryQueueIfNeeded()
            }
        )
    }

    private fun bindSharedListFriendIds(list: SharedFileList?) {
        activeSharedFriendIdsBinding?.close()
        if (list == null) {
            sharedListFriendIds.clear()
            return
        }
        activeSharedFriendIdsBinding = EventListBinding(list.friendIds, sharedListFriendIds)
    }

    private fun rebuildLibrarySections() {
        val sections = mutableListOf(
            LibrarySection(
                id = DEFAULT_LIBRARY_SECTION_ID,
                title = tr("My Files"),
                list = libraryManager.libraryManagedList,
                isShared = false
            )
        )

        sections += sharedLists
            .sortedBy { it.collectionName.lowercase(Locale.US) }
            .map { list ->
                LibrarySection(
                    id = "shared:${list.id}",
                    title = list.collectionName,
                    list = list,
                    isShared = true,
                    isPublic = list.isPublic
                )
            }

        librarySections.clear()
        librarySections.addAll(sections)
        librarySectionStates.keys.retainAll(sections.map(LibrarySection::id).toSet())
        if (librarySections.none { it.id == selectedLibrarySectionId }) {
            selectedLibrarySectionId = DEFAULT_LIBRARY_SECTION_ID
        }
        bindLibraryItems(activeLibrarySection()?.list ?: libraryManager.libraryManagedList)
        bindSharedListFriendIds(currentSharedList())
        if (currentSharedList() == null) {
            cancelCurrentSharedListSharingEdit()
        }
    }

    private fun addFilesToLocalFileList(
        fileList: LocalFileList,
        files: List<File>,
        destinationLabel: String,
        allowRecursiveDirectories: Boolean = true,
        allowedCategories: Set<Category>? = null,
        advancedExtensions: Collection<String> = emptyList()
    ) {
        maybeClearActiveLibraryFiltersForImport(fileList, files)
        val rejected = mutableListOf<File>()
        val failed = mutableListOf<File>()
        val allowedExtensions = buildLibraryImportExtensionSet(allowedCategories, advancedExtensions)
        val filter = FileFilter { candidate ->
            when {
                candidate.isDirectory -> allowRecursiveDirectories
                allowedExtensions == null -> fileList.isFileAllowed(candidate)
                else -> {
                    val extension = FileUtils.getFileExtension(candidate).lowercase(Locale.US)
                    fileList.isFileAllowed(candidate) && extension in allowedExtensions
                }
            }
        }

        files.forEach { file ->
            try {
                when {
                    fileList.isDirectoryAllowed(file) -> fileList.addFolder(file, filter)
                    fileList.isFileAllowed(file) -> fileList.addFile(file)
                    else -> rejected.add(file)
                }
            } catch (_: Exception) {
                failed.add(file)
            }
        }

        currentScreen = ComposeScreen.Library
        if (rejected.isNotEmpty()) {
            showNotice(
                tr("Add Files"),
                tr("{0} item(s) could not be added to {1}.", rejected.size, destinationLabel),
                OperationNoticeLevel.WARNING
            )
        }
        if (failed.isNotEmpty()) {
            showNotice(
                tr("Add Files"),
                tr("{0} item(s) could not be imported into {1}.", failed.size, destinationLabel),
                OperationNoticeLevel.ERROR
            )
        }
    }

    private fun showLibraryFolderImportDialog(section: LibrarySection, files: List<File>) {
        val folders = files.filter { it.isDirectory }
        if (folders.isEmpty()) {
            addFilesToLocalFileList(section.list, files, section.title)
            return
        }
        val singleFolder = folders.singleOrNull()
        val title = if (section.isShared) tr("Choose What to Share") else tr("Choose What to Add")
        val message = if (section.isShared) {
            tr("Choose how much of the dropped folder set to share into \"{0}\".", section.title)
        } else {
            tr("Choose how much of the dropped folder set to add into \"{0}\".", section.title)
        }
        val recursiveLabel = singleFolder?.let { tr("\"{0}\" and all its subfolders", it.name) }
            ?: tr("{0} folders and all their subfolders", folders.size)
        val topLevelLabel = singleFolder?.let { tr("\"{0}\" only", it.name) }
            ?: tr("{0} folders only", folders.size)
        val libraryPreferences = settingsService.loadPreferences().library
        val documentsEnabled = !(section.isPublic && !libraryPreferences.allowDocumentSharing)
        val programsEnabled = libraryPreferences.allowProgramSearchAndShare
        libraryFolderImportDialog = LibraryFolderImportDialogState(
            title = title,
            message = message,
            recursiveLabel = recursiveLabel,
            topLevelLabel = topLevelLabel,
            confirmLabel = if (section.isShared) tr("Share") else tr("Add"),
            categoryOptions = listOf(
                LibraryFolderImportCategoryOption(Category.AUDIO, tr("Audio"), true, libraryPreferences.addToLibraryDefaults.audio),
                LibraryFolderImportCategoryOption(Category.VIDEO, tr("Video"), true, libraryPreferences.addToLibraryDefaults.video),
                LibraryFolderImportCategoryOption(Category.IMAGE, tr("Images"), true, libraryPreferences.addToLibraryDefaults.images),
                LibraryFolderImportCategoryOption(Category.DOCUMENT, tr("Documents"), documentsEnabled, libraryPreferences.addToLibraryDefaults.documents),
                LibraryFolderImportCategoryOption(Category.PROGRAM, tr("Programs"), programsEnabled, libraryPreferences.addToLibraryDefaults.programs)
            ),
            programsDisabledMessage = if (!programsEnabled) {
                tr("Programs are currently disabled in Preferences.")
            } else {
                null
            },
            documentsDisabledMessage = if (!documentsEnabled) {
                tr("Public document sharing is disabled in Preferences.")
            } else {
                null
            },
            onConfirmAction = { recursive, categories, advancedExtensionText ->
                libraryFolderImportDialog = null
                addFilesToLocalFileList(
                    section.list,
                    files,
                    section.title,
                    allowRecursiveDirectories = recursive,
                    allowedCategories = categories,
                    advancedExtensions = parseLibraryImportExtensions(advancedExtensionText)
                )
            }
        )
    }

    private fun maybeClearActiveLibraryFiltersForImport(fileList: LocalFileList, files: List<File>) {
        val activeSection = activeLibrarySection() ?: return
        if (activeSection.list != fileList || !hasActiveLibraryFilters()) {
            return
        }
        val filterText = libraryFilterText.trim().lowercase(Locale.US)
        val shouldClear = files.any { file ->
            file.isDirectory ||
                (libraryCategoryFilter != null && categoryManager.getCategoryForFile(file) != libraryCategoryFilter) ||
                (filterText.isNotEmpty() && !matchesLibraryImportFilter(file, filterText))
        }
        if (shouldClear) {
            clearAllLibraryFilters()
        }
    }

    private fun matchesLibraryImportFilter(file: File, filter: String): Boolean {
        return listOfNotNull(
            file.name,
            FileUtils.getFilenameNoExtension(file.name),
            FileUtils.getFileExtension(file.name),
            file.absolutePath,
            categoryManager.getCategoryForFile(file).getPluralName(),
            categoryManager.getCategoryForFile(file).getSingularName()
        ).any { it.lowercase(Locale.US).contains(filter) }
    }

    private fun buildLibraryImportExtensionSet(
        allowedCategories: Set<Category>?,
        advancedExtensions: Collection<String>
    ): Set<String>? {
        if (allowedCategories == null && advancedExtensions.isEmpty()) {
            return null
        }
        val extensions = linkedSetOf<String>()
        allowedCategories.orEmpty().forEach { category ->
            categoryManager.getExtensionsForCategory(category)
                .mapTo(extensions) { it.lowercase(Locale.US) }
        }
        advancedExtensions
            .map(String::trim)
            .filter(String::isNotEmpty)
            .mapTo(extensions) { it.lowercase(Locale.US).removePrefix(".") }
        return extensions
    }

    private fun parseLibraryImportExtensions(raw: String): List<String> {
        return raw
            .split(Regex(",\\s*\\.?|\\s*\\.|\\s+"))
            .map(String::trim)
            .filter(String::isNotEmpty)
            .map { it.removePrefix(".") }
            .distinctBy { it.lowercase(Locale.US) }
    }

    fun showLibraryKnownTypesDialog() {
        libraryKnownTypesDialog = LibraryKnownTypesDialogState(
            title = tr("Known File Types"),
            groups = listOf(
                knownFileTypeGroup(Category.AUDIO, tr("Audio")),
                knownFileTypeGroup(Category.VIDEO, tr("Video")),
                knownFileTypeGroup(Category.IMAGE, tr("Images")),
                knownFileTypeGroup(Category.DOCUMENT, tr("Documents")),
                knownFileTypeGroup(Category.PROGRAM, tr("Programs"))
            ),
            otherMessage = tr("Unknown file extensions are treated as Other. Add a custom extension in Advanced when you want to include uncategorized files right away.")
        )
    }

    fun showUnsafeSharingKnownTypes(category: Category) {
        val (title, label) = when (category) {
            Category.DOCUMENT -> tr("What Are Documents?") to tr("Documents")
            Category.PROGRAM -> tr("What Are Programs?") to tr("Programs")
            else -> tr("Known File Types") to category.getPluralName()
        }
        libraryKnownTypesDialog = LibraryKnownTypesDialogState(
            title = title,
            groups = listOf(knownFileTypeGroup(category, label)),
            otherMessage = tr("Unknown file extensions stay in Other until you explicitly classify or include them.")
        )
    }

    private fun knownFileTypeGroup(category: Category, label: String): KnownFileTypeGroup {
        return KnownFileTypeGroup(
            label = label,
            extensions = categoryManager.getExtensionsForCategory(category)
                .map { ".$it" }
                .sortedBy { it.lowercase(Locale.US) }
        )
    }

    private fun syncPublicDocumentWarningBindings() {
        val desiredPublicLists = sharedLists.filter { it.isPublic }.associateBy { it.id }
        publicDocumentWarningBindings.keys
            .filter { it !in desiredPublicLists }
            .toList()
            .forEach { listId ->
                publicDocumentWarningBindings.remove(listId)?.close()
            }
        desiredPublicLists.forEach { (listId, list) ->
            if (listId !in publicDocumentWarningBindings) {
                val listener = ListEventListener<LocalFileItem> { changes ->
                    if (!settingsService.warnSharingDocumentsWithWorldEnabled()) {
                        return@ListEventListener
                    }
                    while (changes.next()) {
                        if (changes.type == ListEvent.INSERT || changes.type == ListEvent.UPDATE) {
                            val item = changes.sourceList[changes.index]
                            if (item.category == Category.DOCUMENT) {
                                runOnUi { maybeShowDocumentSharingWarning() }
                                break
                            }
                        }
                    }
                }
                list.swingModel.addListEventListener(listener)
                publicDocumentWarningBindings[listId] = AutoCloseable {
                    list.swingModel.removeListEventListener(listener)
                }
            }
        }
    }

    private fun clearPublicDocumentWarningBindings() {
        publicDocumentWarningBindings.values.forEach { binding ->
            try {
                binding.close()
            } catch (_: Exception) {
            }
        }
        publicDocumentWarningBindings.clear()
    }

    private fun maybeShowDocumentSharingWarning() {
        if (documentSharingWarningDialog != null || !settingsService.warnSharingDocumentsWithWorldEnabled()) {
            return
        }
        documentSharingWarningDialog = DocumentSharingWarningDialogState(
            title = tr("Document Sharing Warning"),
            message = tr("Shared documents can include confidential or personal information. Continue sharing documents publicly only if you are sure they are safe to expose."),
            continueLabel = tr("Continue Sharing"),
            unshareLabel = tr("Unshare All"),
            onContinue = {
                settingsService.setWarnSharingDocumentsWithWorldEnabled(false)
                documentSharingWarningDialog = null
            },
            onUnshareAll = {
                settingsService.setWarnSharingDocumentsWithWorldEnabled(true)
                settingsService.removeDocumentsFromPublicLists()
                documentSharingWarningDialog = null
                showNotice(
                    tr("Collections"),
                    tr("Removed documents from public collections."),
                    OperationNoticeLevel.INFO
                )
            },
            onDismiss = { documentSharingWarningDialog = null }
        )
    }

    private fun removeLibraryItemsFromLibrary(items: List<LocalFileItem>) {
        val files = uniqueLibraryFiles(items)
        stopPlayerIfNeeded(files)
        removeFilesFromLocalList(libraryManager.libraryManagedList, files)
    }

    private fun deleteLibraryItemsFromDisk(items: List<LocalFileItem>) {
        val files = uniqueLibraryFiles(items)
        stopPlayerIfNeeded(files)
        removeFilesFromLocalList(libraryManager.libraryManagedList, files)
        FileUtils.delete(files, OSUtils.supportsTrash())
    }

    private fun uniqueLibraryFiles(items: List<LocalFileItem>): List<File> {
        return items.map { it.file }.distinct()
    }

    private fun stopPlayerIfNeeded(files: Collection<File>) {
        val current = playerCurrentFile ?: return
        if (files.any { it == current }) {
            playerService.stop()
        }
    }

    private fun removeFilesFromLocalList(list: LocalFileList, files: Collection<File>) {
        if (files.isEmpty()) {
            return
        }
        val fileSet = files.toHashSet()
        list.removeFiles(Predicate { item -> item.file in fileSet })
    }

    private fun applyPendingCollectionShares() {
        if (pendingCollectionShares.isEmpty()) {
            return
        }
        val applied = mutableListOf<Int>()
        pendingCollectionShares.forEach { (collectionId, friendId) ->
            val collection = sharedLists.firstOrNull { it.id == collectionId } ?: return@forEach
            if (!collection.friendIds.contains(friendId)) {
                collection.addFriend(friendId)
            }
            applied += collectionId
        }
        if (applied.isNotEmpty()) {
            applied.forEach { pendingCollectionShares.remove(it) }
            friendCollectionShareEpoch += 1
            invalidateSharingSummaryCache()
        }
    }

    private fun findLibraryItem(file: File): LocalFileItem? {
        activeLibrarySection()?.list?.getFileItem(file)?.let { return it }
        sharedLists.firstNotNullOfOrNull { it.getFileItem(file) }?.let { return it }
        return libraryManager.libraryManagedList.getFileItem(file)
    }

    private fun findLibraryItemByUrn(result: GroupedSearchResult): LocalFileItem? {
        activeLibrarySection()?.list?.getFileItem(result.urn)?.let { return it }
        sharedLists.firstNotNullOfOrNull { it.getFileItem(result.urn) }?.let { return it }
        return libraryManager.libraryManagedList.getFileItem(result.urn)
    }

    private fun findLibraryItemForSection(sectionId: String, file: File): LocalFileItem? {
        return when {
            sectionId == DEFAULT_LIBRARY_SECTION_ID -> libraryManager.libraryManagedList.getFileItem(file)
            sectionId.startsWith("shared:") -> sharedLists
                .firstOrNull { "shared:${it.id}" == sectionId }
                ?.getFileItem(file)
            else -> null
        }
    }

    private fun findLibraryItemForSection(sectionId: String, result: GroupedSearchResult): LocalFileItem? {
        return when {
            sectionId == DEFAULT_LIBRARY_SECTION_ID -> libraryManager.libraryManagedList.getFileItem(result.urn)
            sectionId.startsWith("shared:") -> sharedLists
                .firstOrNull { "shared:${it.id}" == sectionId }
                ?.getFileItem(result.urn)
            else -> null
        }
    }

    private fun libraryJumpTargetsForFile(file: File, excludeSectionId: String?): List<LibraryJumpTarget> {
        val targets = mutableListOf<LibraryJumpTarget>()
        if (libraryManager.libraryManagedList.contains(file) && DEFAULT_LIBRARY_SECTION_ID != excludeSectionId) {
            targets += LibraryJumpTarget(DEFAULT_LIBRARY_SECTION_ID, tr("My Files"))
        }
        sharedLists
            .filter { it.contains(file) }
            .sortedBy { it.collectionName.lowercase(Locale.US) }
            .forEach { list ->
                val sectionId = "shared:${list.id}"
                if (sectionId != excludeSectionId) {
                    targets += LibraryJumpTarget(sectionId, list.collectionName)
                }
            }
        return targets
    }

    private fun libraryJumpTargetsForSearchResult(result: GroupedSearchResult): List<LibraryJumpTarget> {
        val targets = mutableListOf<LibraryJumpTarget>()
        if (libraryManager.libraryManagedList.contains(result.urn)) {
            targets += LibraryJumpTarget(DEFAULT_LIBRARY_SECTION_ID, tr("My Files"))
        }
        sharedLists
            .filter { it.contains(result.urn) }
            .sortedBy { it.collectionName.lowercase(Locale.US) }
            .forEach { list ->
                targets += LibraryJumpTarget("shared:${list.id}", list.collectionName)
            }
        return targets
    }

    private fun showFileInTarget(file: File, target: LibraryJumpTarget, preferredCategory: Category? = null) {
        selectLibrarySection(target.sectionId)
        currentScreen = ComposeScreen.Library
        updateLibraryFilterText("")
        val resolvedItem = findLibraryItemForSection(target.sectionId, file)
        selectLibraryCategory(resolvedItem?.category ?: preferredCategory ?: categoryManager.getCategoryForFile(file))
        resolvedItem?.let(::selectLibraryItem) ?: clearLibrarySelection()
    }

    private fun navigateSearchResultToTarget(result: GroupedSearchResult, target: LibraryJumpTarget) {
        selectLibrarySection(target.sectionId)
        currentScreen = ComposeScreen.Library
        updateLibraryFilterText("")
        val resolvedItem = findLibraryItemForSection(target.sectionId, result)
        selectLibraryCategory(resolvedItem?.category ?: result.searchResults.firstOrNull()?.category)
        resolvedItem?.let(::selectLibraryItem) ?: clearLibrarySelection()
    }

    private fun bestLibrarySectionIdForFile(file: File): String? {
        return when {
            libraryManager.libraryManagedList.contains(file) -> DEFAULT_LIBRARY_SECTION_ID
            else -> sharedLists.firstOrNull { it.contains(file) }?.let { "shared:${it.id}" }
        }
    }

    private fun bestLibrarySectionIdForSearchResult(result: GroupedSearchResult): String? {
        return when {
            libraryManager.libraryManagedList.contains(result.urn) -> DEFAULT_LIBRARY_SECTION_ID
            else -> sharedLists.firstOrNull { it.contains(result.urn) }?.let { "shared:${it.id}" }
        }
    }

    private fun targetLabelForSection(sectionId: String): String {
        return when {
            sectionId == DEFAULT_LIBRARY_SECTION_ID -> tr("My Files")
            sectionId.startsWith("shared:") -> sharedLists.firstOrNull { "shared:${it.id}" == sectionId }?.collectionName
                ?: tr("Collection")
            else -> tr("My Files")
        }
    }

    private fun preferredSectionIdForFile(file: File): String {
        val activeSectionId = activeLibrarySection()
            ?.takeIf { it.list.contains(file) }
            ?.id
        if (activeSectionId != null) {
            return activeSectionId
        }
        return sharedLists
            .firstOrNull { it.contains(file) }
            ?.let { "shared:${it.id}" }
            ?: DEFAULT_LIBRARY_SECTION_ID
    }

    private fun nextLibraryFileInfoDialogState(item: LocalFileItem): LibraryFileInfoDialogState {
        return LibraryFileInfoDialogState(item = item, version = nextLibraryFileInfoDialogVersion++)
    }

    private fun refreshLibraryFileInfoDialog(file: File, fallbackItem: LocalFileItem? = null) {
        libraryFileInfoDialog = (findLibraryItem(file) ?: fallbackItem)?.let(::nextLibraryFileInfoDialogState)
    }

    private fun metadataChoiceList(currentValue: String, choices: List<String>): List<String> {
        val trimmedCurrent = currentValue.trim()
        if (trimmedCurrent.isBlank()) {
            return choices
        }
        return if (choices.any { it.equals(trimmedCurrent, ignoreCase = true) }) {
            choices
        } else {
            listOf(trimmedCurrent) + choices
        }
    }

    private fun validateLibraryRenameRequest(requestedName: String): String? {
        val trimmed = requestedName.trim()
        if (trimmed.isEmpty() || CommonUtils.santizeString(trimmed) != trimmed) {
            return tr("Enter a valid file name to continue.")
        }
        return null
    }

    private fun renameLibraryFile(item: LocalFileItem, requestedName: String): String? {
        validateLibraryRenameRequest(requestedName)?.let { return it }
        val trimmed = requestedName.trim()
        val oldFile = item.file
        if (trimmed == item.name) {
            return null
        }
        val extension = FileUtils.getFileExtension(oldFile)
        val resolvedName = if (extension.isBlank()) trimmed else "$trimmed.$extension"
        val newFile = File(oldFile.parentFile, resolvedName)
        if (newFile == oldFile) {
            return null
        }
        if (!FileUtils.forceRename(oldFile, newFile)) {
            newFile.delete()
            return tr("WireShare could not rename {0}.", oldFile.name)
        }
        libraryManager.libraryManagedList.fileRenamed(oldFile, newFile)
        refreshLibraryFileInfoDialog(newFile)
        return null
    }

    private fun buildM3uSuggestedName(title: String): String {
        val sanitized = CommonUtils.santizeString(title).ifBlank { "collection" }
        return if (sanitized.lowercase(Locale.US).endsWith(".m3u")) sanitized else "$sanitized.m3u"
    }

    private fun clearChatState() {
        chatConversations.clear()
        selectedConversationId = null
    }

    private fun upsertConversationMessage(conversation: ChatConversationState, message: ConversationMessage) {
        val existingIndex = conversation.messages.indexOfFirst { it.id == message.id }
        if (existingIndex >= 0) {
            conversation.messages[existingIndex] = message
        } else {
            conversation.messages.add(message)
        }
        conversation.messageVersion += 1
    }

    private fun ensureConversation(friend: FriendRosterItem): ChatConversationState {
        return chatConversations.getOrPut(friend.id) {
            ChatConversationState(friend)
        }.also {
            it.friend = friend
            it.offTheRecordEnabled = if (friend.supportsOffTheRecord) {
                friendService.isOffTheRecordEnabled(friend.id)
            } else {
                null
            }
        }
    }

    private fun maybeShowChatNotification(message: ConversationMessage) {
        if (!message.isIncoming || !notificationsEnabled || !notifications.supportsNotifications()) {
            return
        }

        val summary = conversationMessageSummary(message)
        if (summary.isBlank()) {
            return
        }

        val shouldNotify = currentScreen != ComposeScreen.Friends ||
            selectedConversationId != message.friendId ||
            windowRef?.isFocused != true
        if (!shouldNotify) {
            return
        }

        playFriendNotificationSound()
        notifications.showChatNotification(message.senderName, summary) {
            restoreApplication()
            chatFriends.firstOrNull { it.id == message.friendId }?.let(::selectConversation)
        }
    }

    private fun maybeShowFriendRequestNotification(request: PendingFriendRequest) {
        if (!notificationsEnabled || !notifications.supportsNotifications()) {
            return
        }
        playFriendNotificationSound()
        val shouldNotify = currentScreen != ComposeScreen.Friends || windowRef?.isFocused != true
        if (!shouldNotify) {
            return
        }
        notifications.showNotification(
            title = tr("Friend Request"),
            body = tr("{0} wants to connect with you.", request.username),
            onOpen = {
                restoreApplication()
                selectFriends()
            }
        )
    }

    private fun playFriendNotificationSound() {
        if (!notificationsEnabled || !playNotificationSoundEnabled || friendDoNotDisturb) {
            return
        }
        notifications.playAttentionSound()
    }

    private fun primeDownloadCompletionState() {
        downloadCompletionStates.clear()
        allDownloads().forEach { item ->
            downloadCompletionStates[downloadSelectionKey(item)] = item.state.isFinished
        }
    }

    private fun handleDownloadLifecycleChanges() {
        val currentKeys = mutableSetOf<String>()
        allDownloads().forEach { item ->
            val key = downloadSelectionKey(item)
            currentKeys += key
            val finished = item.state.isFinished
            val previousFinished = downloadCompletionStates.put(key, finished)
            if (previousFinished == false && finished) {
                maybeShowDownloadCompletedNotification(item)
            }
        }
        downloadCompletionStates.keys.retainAll(currentKeys)
    }

    private fun maybeShowDownloadCompletedNotification(item: DownloadItem) {
        if (!notificationsEnabled || !notifications.supportsNotifications()) {
            return
        }
        val file = item.launchableFile
        notifications.showNotification(
            title = tr("Download Complete"),
            body = item.title ?: item.fileName,
            onOpen = {
                restoreApplication()
                openTransfersWorkspace(TransferTrayMode.DOWNLOADS)
                if (item.isLaunchable) {
                    openDownloadItem(item)
                } else {
                    revealFileWithComposeFeedback(file)
                }
            }
        )
    }

    private fun primarySize(result: GroupedSearchResult): Long = result.searchResults.firstOrNull()?.size ?: 0L

    private fun isFullyConnected(): Boolean {
        return connectionStrengthState == ConnectionStrength.FULL || connectionStrengthState == ConnectionStrength.TURBO
    }

    private fun refreshSearchRunState() {
        searchTabs.forEach { scheduleSearchTabPresentationRefresh(it, SearchPresentationDirty.STATUS.mask) }
    }

    private fun handleFileProcessingEvent(event: FileProcessingEvent) {
        when (event.type) {
            FileProcessingEvent.Type.QUEUED -> {
                val current = fileProcessingStatus
                val nextTotal = if (current == null || current.done) 1 else current.total + 1
                val nextFinished = if (current == null || current.done) 0 else current.finished
                fileProcessingStatus = FileProcessingStatus(
                    total = nextTotal,
                    finished = nextFinished,
                    currentFileName = current?.currentFileName,
                    currentCategory = current?.currentCategory,
                    done = false
                )
            }

            FileProcessingEvent.Type.PROCESSING -> {
                val current = fileProcessingStatus
                val file = event.source
                fileProcessingStatus = FileProcessingStatus(
                    total = current?.total?.coerceAtLeast(1) ?: 1,
                    finished = current?.finished ?: 0,
                    currentFileName = file.name,
                    currentCategory = categoryManager.getCategoryForFile(file),
                    done = false
                )
            }

            FileProcessingEvent.Type.FINISHED -> {
                val current = fileProcessingStatus
                val finished = (current?.finished ?: 0) + 1
                val total = maxOf(current?.total ?: finished, finished)
                fileProcessingStatus = if (finished >= total) {
                    FileProcessingStatus(
                        total = total,
                        finished = total,
                        currentFileName = null,
                        currentCategory = current?.currentCategory,
                        done = true
                    )
                } else {
                    FileProcessingStatus(
                        total = total,
                        finished = finished,
                        currentFileName = current?.currentFileName,
                        currentCategory = current?.currentCategory,
                        done = false
                    )
                }
            }
        }
    }

    private fun matchesSearchFilter(result: GroupedSearchResult, filter: String): Boolean {
        if (result.fileName.lowercase(Locale.US).contains(filter)) {
            return true
        }
        if (result.friends.any {
                it.renderName.lowercase(Locale.US).contains(filter) || it.id.lowercase(Locale.US).contains(filter)
            }
        ) {
            return true
        }
        return result.searchResults.any { searchResult ->
            val source = searchResult.source
            val fields = listOfNotNull(
                searchSourceLabel(result),
                searchResult.fileExtension,
                searchResult.fileName,
                searchResult.fileNameWithoutExtension,
                source.friendPresence?.friend?.renderName,
                propertyTrackText(searchResult.getProperty(FilePropertyKey.TRACK_NUMBER)).ifBlank { null },
                searchResult.getProperty(FilePropertyKey.BITRATE)?.toString(),
                searchResult.getProperty(FilePropertyKey.LENGTH)?.toString(),
                searchResult.getProperty(FilePropertyKey.QUALITY)?.toString(),
                searchResult.getProperty(FilePropertyKey.AUTHOR)?.toString(),
                searchResult.getProperty(FilePropertyKey.ALBUM)?.toString(),
                searchResult.getProperty(FilePropertyKey.GENRE)?.toString(),
                searchResult.getProperty(FilePropertyKey.TITLE)?.toString(),
                searchResult.getProperty(FilePropertyKey.COMPANY)?.toString(),
                searchResult.getProperty(FilePropertyKey.PLATFORM)?.toString(),
                searchResult.getProperty(FilePropertyKey.DESCRIPTION)?.toString()
            )
            fields.any { it.lowercase(Locale.US).contains(filter) }
        }
    }

    private fun matchesSearchFacetFilters(
        result: GroupedSearchResult,
        tab: SearchTabSession,
        excludedGroups: Set<SearchFilterGroup>
    ): Boolean {
        val displayCategory = when {
            SearchFilterGroup.CATEGORY in excludedGroups -> null
            tab.category != SearchCategory.ALL -> null
            else -> tab.displayCategory
        }
        val extensionFilter = tab.selectedPropertyFacets[SearchPropertyFacet.EXTENSION]
            .takeUnless { SearchFilterGroup.EXTENSION in excludedGroups }
        val fileTypeFilter = tab.selectedPropertyFacets[SearchPropertyFacet.FILE_TYPE]
            .takeUnless { SearchFilterGroup.FILE_TYPE in excludedGroups }
        val artistFilter = tab.selectedPropertyFacets[SearchPropertyFacet.ARTIST]
            .takeUnless { SearchFilterGroup.ARTIST in excludedGroups }
        val albumFilter = tab.selectedPropertyFacets[SearchPropertyFacet.ALBUM]
            .takeUnless { SearchFilterGroup.ALBUM in excludedGroups }
        val genreFilter = tab.selectedPropertyFacets[SearchPropertyFacet.GENRE]
            .takeUnless { SearchFilterGroup.GENRE in excludedGroups }
        val sizeSelection = tab.selectedRangeFacets[SearchRangeFacet.SIZE]
            .takeUnless { SearchFilterGroup.SIZE in excludedGroups }
        val lengthSelection = tab.selectedRangeFacets[SearchRangeFacet.LENGTH]
            .takeUnless { SearchFilterGroup.LENGTH in excludedGroups }
        val bitrateSelection = tab.selectedRangeFacets[SearchRangeFacet.BITRATE]
            .takeUnless { SearchFilterGroup.BITRATE in excludedGroups }
        val qualitySelection = tab.selectedRangeFacets[SearchRangeFacet.QUALITY]
            .takeUnless { SearchFilterGroup.QUALITY in excludedGroups }

        return result.searchResults.any { candidate ->
            matchesCandidateDisplayCategory(candidate, displayCategory) &&
                matchesSearchFacetValue(candidate, SearchPropertyFacet.EXTENSION, extensionFilter) &&
                matchesSearchFacetValue(candidate, SearchPropertyFacet.FILE_TYPE, fileTypeFilter) &&
                matchesSearchFacetValue(candidate, SearchPropertyFacet.ARTIST, artistFilter) &&
                matchesSearchFacetValue(candidate, SearchPropertyFacet.ALBUM, albumFilter) &&
                matchesSearchFacetValue(candidate, SearchPropertyFacet.GENRE, genreFilter) &&
                matchesSearchRangeSelection(rawSearchPresentationCategory(tab), candidate, SearchRangeFacet.SIZE, sizeSelection) &&
                matchesSearchRangeSelection(rawSearchPresentationCategory(tab), candidate, SearchRangeFacet.LENGTH, lengthSelection) &&
                matchesSearchRangeSelection(rawSearchPresentationCategory(tab), candidate, SearchRangeFacet.BITRATE, bitrateSelection) &&
                matchesSearchRangeSelection(rawSearchPresentationCategory(tab), candidate, SearchRangeFacet.QUALITY, qualitySelection)
        }
    }

    private fun matchesCandidateDisplayCategory(candidate: SearchResult, displayCategory: SearchCategory?): Boolean {
        return displayCategory == null || SearchCategory.forCategory(candidate.category) == displayCategory
    }

    private fun matchesSearchFacetValue(candidate: SearchResult, facet: SearchPropertyFacet, selectedValue: String?): Boolean {
        if (selectedValue.isNullOrBlank()) {
            return true
        }
        val normalizedSelected = selectedValue.trim().lowercase(Locale.US)
        return searchFacetValues(candidate, facet).any { (id, _) -> id == normalizedSelected }
    }

    private fun matchesSearchRangeSelection(
        category: SearchCategory,
        candidate: SearchResult,
        facet: SearchRangeFacet,
        selection: SearchRangeSelection?
    ): Boolean {
        if (selection == null || (selection.minimumId == null && selection.maximumId == null)) {
            return true
        }
        val buckets = searchRangeBucketsForCategory(category, facet)
        val minimumBucket = buckets.firstOrNull { it.id == selection.minimumId }
        val maximumBucket = buckets.firstOrNull { it.id == selection.maximumId }
        val value = when (facet) {
            SearchRangeFacet.SIZE -> candidate.size
            SearchRangeFacet.LENGTH -> searchNumericProperty(candidate.getProperty(FilePropertyKey.LENGTH)) ?: return false
            SearchRangeFacet.BITRATE -> searchNumericProperty(candidate.getProperty(FilePropertyKey.BITRATE)) ?: return false
            SearchRangeFacet.QUALITY -> {
                if (candidate.isSpam && selection.minimumId != null) {
                    return false
                }
                searchNumericProperty(candidate.getProperty(FilePropertyKey.QUALITY)) ?: return false
            }
        }
        val minMatches = minimumBucket == null || value >= minimumBucket.minimum
        val maxMatches = maximumBucket == null || value <= maximumBucket.minimum
        return minMatches && maxMatches
    }

    private fun searchNumericProperty(value: Any?): Long? {
        return when (value) {
            is Number -> value.toLong()
            is String -> value.toLongOrNull()
            else -> null
        }
    }

    private fun searchFacetValues(result: GroupedSearchResult, facet: SearchPropertyFacet): Set<Pair<String, String>> {
        return result.searchResults.flatMap { searchFacetValues(it, facet) }.toSet()
    }

    private fun searchFacetValues(candidate: SearchResult, facet: SearchPropertyFacet): List<Pair<String, String>> {
        return when (facet) {
            SearchPropertyFacet.EXTENSION -> candidate.fileExtension
                .trim()
                .takeIf(String::isNotBlank)
                ?.let { listOf(it.lowercase(Locale.US) to it.uppercase(Locale.US)) }
                ?: emptyList()
            SearchPropertyFacet.FILE_TYPE -> searchFileTypeLabel(candidate)
                ?.let { label -> listOf(label.lowercase(Locale.US) to label) }
                ?: emptyList()
            SearchPropertyFacet.ARTIST -> searchPropertyFacetValue(candidate, FilePropertyKey.AUTHOR)
            SearchPropertyFacet.ALBUM -> searchPropertyFacetValue(candidate, FilePropertyKey.ALBUM)
            SearchPropertyFacet.GENRE -> searchPropertyFacetValue(candidate, FilePropertyKey.GENRE)
        }
    }

    private fun searchPropertyFacetValue(candidate: SearchResult, key: FilePropertyKey): List<Pair<String, String>> {
        val raw = candidate.getProperty(key)?.toString()?.trim().orEmpty()
        return if (raw.isBlank()) {
            emptyList()
        } else {
            listOf(raw.lowercase(Locale.US) to raw)
        }
    }

    private fun searchFileTypeLabel(candidate: SearchResult): String? {
        val extension = candidate.fileExtension.trim().lowercase(Locale.US)
        if (extension.isBlank()) {
            return null
        }
        return fileAppearanceService.mimeDescription(extension) ?: extension.uppercase(Locale.US)
    }

    private fun buildFacetOptions(
        results: List<GroupedSearchResult>,
        values: (GroupedSearchResult) -> Set<Pair<String, String>>
    ): List<SearchFacetOption> {
        val counts = linkedMapOf<String, Pair<String, Int>>()
        results.forEach { result ->
            values(result).forEach { (id, label) ->
                val current = counts[id]
                counts[id] = label to ((current?.second ?: 0) + 1)
            }
        }
        return counts.entries
            .map { (id, pair) ->
                SearchFacetOption(
                    id = id,
                    label = pair.first,
                    resultCount = pair.second
                )
            }
            .sortedWith(compareByDescending<SearchFacetOption> { it.resultCount }.thenBy { it.label.lowercase(Locale.US) })
    }

    private fun normalizeSearchFacetState(tab: SearchTabSession) {
        if (tab.category != SearchCategory.ALL) {
            tab.displayCategory = null
        }
        val presentationCategory = rawSearchPresentationCategory(tab)
        val validPropertyFacets = searchPropertyFacetsForCategory(presentationCategory).toSet()
        tab.selectedPropertyFacets.keys.toList()
            .filterNot(validPropertyFacets::contains)
            .forEach(tab.selectedPropertyFacets::remove)
        val validRangeFacets = searchRangeFacetsForCategory(presentationCategory).toSet()
        tab.selectedRangeFacets.keys.toList()
            .filterNot(validRangeFacets::contains)
            .forEach(tab.selectedRangeFacets::remove)
        validRangeFacets.forEach { facet ->
            tab.selectedRangeFacets[facet]?.let { selection ->
                val normalized = normalizeRangeSelection(facet, selection, presentationCategory)
                if (normalized.minimumId == null && normalized.maximumId == null) {
                    tab.selectedRangeFacets.remove(facet)
                } else {
                    tab.selectedRangeFacets[facet] = normalized
                }
            }
        }
        val availableSortModes = searchSortModesForCategory(presentationCategory)
        if (tab.sortMode !in availableSortModes) {
            tab.sortMode = if (SearchSortMode.RELEVANCE in availableSortModes) {
                SearchSortMode.RELEVANCE
            } else {
                availableSortModes.firstOrNull() ?: SearchSortMode.RELEVANCE
            }
            tab.sortDescending = defaultSortDescending(tab.sortMode)
        }
    }

    private fun normalizeRangeSelection(
        facet: SearchRangeFacet,
        selection: SearchRangeSelection,
        category: SearchCategory? = null
    ): SearchRangeSelection {
        val effectiveCategory = category ?: SearchCategory.ALL
        val buckets = searchRangeBucketsForCategory(effectiveCategory, facet)
        val minimumBucket = buckets.firstOrNull { it.id == selection.minimumId }
        val maximumBucket = buckets.firstOrNull { it.id == selection.maximumId }
        if (minimumBucket == null && maximumBucket == null) {
            return SearchRangeSelection()
        }
        if (minimumBucket == null) {
            return SearchRangeSelection(maximumId = maximumBucket?.id)
        }
        if (maximumBucket == null) {
            return SearchRangeSelection(minimumId = minimumBucket.id)
        }
        return if (minimumBucket.minimum <= maximumBucket.minimum) {
            SearchRangeSelection(minimumId = minimumBucket.id, maximumId = maximumBucket.id)
        } else {
            SearchRangeSelection(minimumId = maximumBucket.id, maximumId = minimumBucket.id)
        }
    }

    private fun searchPropertyFacetsForCategory(category: SearchCategory): List<SearchPropertyFacet> {
        return when (category) {
            SearchCategory.ALL -> listOf(SearchPropertyFacet.EXTENSION)
            SearchCategory.AUDIO -> listOf(
                SearchPropertyFacet.ARTIST,
                SearchPropertyFacet.ALBUM,
                SearchPropertyFacet.GENRE,
                SearchPropertyFacet.EXTENSION
            )
            SearchCategory.VIDEO -> listOf(SearchPropertyFacet.EXTENSION)
            SearchCategory.DOCUMENT,
            SearchCategory.OTHER -> listOf(SearchPropertyFacet.FILE_TYPE, SearchPropertyFacet.EXTENSION)
            SearchCategory.IMAGE,
            SearchCategory.PROGRAM,
            SearchCategory.TORRENT -> listOf(SearchPropertyFacet.EXTENSION)
        }
    }

    private fun searchRangeFacetsForCategory(category: SearchCategory): List<SearchRangeFacet> {
        return when (category) {
            SearchCategory.ALL,
            SearchCategory.IMAGE,
            SearchCategory.PROGRAM,
            SearchCategory.TORRENT,
            SearchCategory.DOCUMENT,
            SearchCategory.OTHER -> listOf(SearchRangeFacet.SIZE)
            SearchCategory.AUDIO -> listOf(
                SearchRangeFacet.SIZE,
                SearchRangeFacet.BITRATE,
                SearchRangeFacet.LENGTH
            )
            SearchCategory.VIDEO -> listOf(
                SearchRangeFacet.SIZE,
                SearchRangeFacet.QUALITY,
                SearchRangeFacet.LENGTH
            )
        }
    }

    private fun searchPropertyFacetGroup(facet: SearchPropertyFacet): SearchFilterGroup {
        return when (facet) {
            SearchPropertyFacet.EXTENSION -> SearchFilterGroup.EXTENSION
            SearchPropertyFacet.FILE_TYPE -> SearchFilterGroup.FILE_TYPE
            SearchPropertyFacet.ARTIST -> SearchFilterGroup.ARTIST
            SearchPropertyFacet.ALBUM -> SearchFilterGroup.ALBUM
            SearchPropertyFacet.GENRE -> SearchFilterGroup.GENRE
        }
    }

    private fun searchPropertyFacetLabel(facet: SearchPropertyFacet): String {
        return when (facet) {
            SearchPropertyFacet.EXTENSION -> tr("Extension")
            SearchPropertyFacet.FILE_TYPE -> tr("File Type")
            SearchPropertyFacet.ARTIST -> tr("Artist")
            SearchPropertyFacet.ALBUM -> tr("Album")
            SearchPropertyFacet.GENRE -> tr("Genre")
        }
    }

    private fun searchRangeFacetLabel(facet: SearchRangeFacet): String {
        return when (facet) {
            SearchRangeFacet.SIZE -> tr("Size")
            SearchRangeFacet.LENGTH -> tr("Length")
            SearchRangeFacet.BITRATE -> tr("Bitrate")
            SearchRangeFacet.QUALITY -> tr("Quality")
        }
    }

    private fun rangeSelectionLabel(
        tab: SearchTabSession,
        facet: SearchRangeFacet,
        selection: SearchRangeSelection
    ): String? {
        val buckets = searchRangeBucketsForCategory(searchPresentationCategory(tab), facet)
        val minimumBucket = buckets.firstOrNull { it.id == selection.minimumId }
        val maximumBucket = buckets.firstOrNull { it.id == selection.maximumId }
        return when {
            minimumBucket == null && maximumBucket == null -> null
            minimumBucket == null && maximumBucket != null -> tr("Up to {0}", maximumBucket.label)
            minimumBucket != null && maximumBucket == null -> {
                if (minimumBucket.maximumAbsolute) minimumBucket.label else tr("{0} or above", minimumBucket.label)
            }
            minimumBucket != null && maximumBucket != null -> tr("{0} to {1}", minimumBucket.label, maximumBucket.label)
            else -> null
        }
    }

    private fun searchRangeBucketsForCategory(category: SearchCategory, facet: SearchRangeFacet): List<SearchRangeBucket> {
        return when (facet) {
            SearchRangeFacet.SIZE -> sizeRangeBuckets(category)
            SearchRangeFacet.LENGTH -> lengthRangeBuckets()
            SearchRangeFacet.BITRATE -> bitrateRangeBuckets()
            SearchRangeFacet.QUALITY -> qualityRangeBuckets()
        }
    }

    private fun sizeRangeBuckets(category: SearchCategory): List<SearchRangeBucket> {
        val values = when (category) {
            SearchCategory.AUDIO -> longArrayOf(
                0L,
                1024L * 100,
                1024L * 250,
                1024L * 500,
                1024L * 1024,
                1024L * 1024 * 2,
                1024L * 1024 * 5,
                1024L * 1024 * 10,
                1024L * 1024 * 25,
                1024L * 1024 * 50,
                1024L * 1024 * 100,
                1024L * 1024 * 250,
                1024L * 1024 * 500,
                1024L * 1024 * 1024
            )
            SearchCategory.VIDEO -> longArrayOf(
                0L,
                1024L * 1024 * 10,
                1024L * 1024 * 25,
                1024L * 1024 * 50,
                1024L * 1024 * 100,
                1024L * 1024 * 250,
                1024L * 1024 * 500,
                1024L * 1024 * 1024,
                1024L * 1024 * 1024 * 2,
                1024L * 1024 * 1024 * 5,
                1024L * 1024 * 1024 * 10,
                1024L * 1024 * 1024 * 25,
                1024L * 1024 * 1024 * 50,
                1024L * 1024 * 1024 * 100
            )
            SearchCategory.IMAGE,
            SearchCategory.DOCUMENT -> longArrayOf(
                0L,
                1024L * 10,
                1024L * 25,
                1024L * 50,
                1024L * 100,
                1024L * 250,
                1024L * 500,
                1024L * 1024,
                1024L * 1024 * 2,
                1024L * 1024 * 5,
                1024L * 1024 * 10,
                1024L * 1024 * 25,
                1024L * 1024 * 50,
                1024L * 1024 * 100
            )
            SearchCategory.ALL,
            SearchCategory.PROGRAM,
            SearchCategory.OTHER,
            SearchCategory.TORRENT -> longArrayOf(
                0L,
                1024L * 10,
                1024L * 50,
                1024L * 100,
                1024L * 500,
                1024L * 1024,
                1024L * 1024 * 5,
                1024L * 1024 * 10,
                1024L * 1024 * 50,
                1024L * 1024 * 100,
                1024L * 1024 * 500,
                1024L * 1024 * 1024,
                1024L * 1024 * 1024 * 5,
                1024L * 1024 * 1024 * 10,
                1024L * 1024 * 1024 * 50,
                1024L * 1024 * 1024 * 100
            )
        }
        return values.map { value ->
            SearchRangeBucket(
                id = value.toString(),
                label = formatSearchFacetBytes(value),
                minimum = value
            )
        }
    }

    private fun lengthRangeBuckets(): List<SearchRangeBucket> {
        val values = longArrayOf(
            0L,
            30L,
            60L,
            60L * 5,
            60L * 10,
            60L * 15,
            60L * 30,
            60L * 60,
            60L * 60 * 2,
            60L * 60 * 4
        )
        return values.map { value ->
            SearchRangeBucket(
                id = value.toString(),
                label = CommonUtils.seconds2time(value),
                minimum = value
            )
        }
    }

    private fun bitrateRangeBuckets(): List<SearchRangeBucket> {
        val values = longArrayOf(0L, 64L, 96L, 128L, 160L, 192L, 256L, 320L, 512L)
        return values.map { value ->
            SearchRangeBucket(
                id = value.toString(),
                label = value.toString(),
                minimum = value,
                maximumAbsolute = false
            )
        }
    }

    private fun qualityRangeBuckets(): List<SearchRangeBucket> {
        val labels = mapOf(
            0L to tr("Spam"),
            1L to tr("Poor"),
            2L to tr("Good"),
            3L to tr("Excellent")
        )
        return labels.entries.map { (value, label) ->
            SearchRangeBucket(
                id = value.toString(),
                label = label,
                minimum = value,
                maximumAbsolute = value == 3L
            )
        }
    }

    private fun formatSearchFacetBytes(value: Long): String {
        if (value <= 0L) {
            return tr("0 B")
        }
        val kib = 1024.0
        val mib = kib * 1024.0
        val gib = mib * 1024.0
        return when {
            value >= gib -> tr("{0} GB", (value / gib).toLong())
            value >= mib -> tr("{0} MB", (value / mib).toLong())
            value >= kib -> tr("{0} KB", (value / kib).toLong())
            else -> tr("{0} B", value)
        }
    }

    fun refreshSearchTabPresentation(tab: SearchTabSession) {
        val dirtyMask = tab.presentationDirtyMask.takeIf { it != 0 } ?: SearchPresentationDirty.FULL
        tab.presentationRefreshScheduled = false
        tab.presentationDirtyMask = 0
        if (dirtyMask == SearchPresentationDirty.SELECTION.mask) {
            val subtitle = buildSearchSubtitle(
                tab = tab,
                activeFilters = tab.presentationState.activeFilters,
                shownCount = tab.presentationState.shownCount,
                totalCount = tab.presentationState.totalCount,
                selectedCount = tab.selectedResultKeys.size
            )
            tab.subtitle = subtitle
            tab.presentationState = tab.presentationState.copy(subtitle = subtitle)
            return
        }
        if ((dirtyMask and (SearchPresentationDirty.RESULTS.mask or SearchPresentationDirty.FILTERS.mask or SearchPresentationDirty.SORT.mask)) != 0) {
            invalidateSearchResultPresentationCache()
        }
        ComposePerformanceTracker.measure("search.refreshPresentation") {
            normalizeSearchFacetState(tab)
            if (!groupSimilarResultsEnabled && tab.expandedSimilarResultKeys.isNotEmpty()) {
                tab.expandedSimilarResultKeys.clear()
            }
            val validKeys = tab.results.map(::searchResultKeyOf).toSet()
            tab.expandedSimilarResultKeys.retainAll(validKeys)
            val presentationCategory = rawSearchPresentationCategory(tab)
            val filteredResultsByExcludedGroups = hashMapOf<Set<SearchFilterGroup>, List<GroupedSearchResult>>()
            fun filtered(excludedGroups: Set<SearchFilterGroup> = emptySet()): List<GroupedSearchResult> {
                return filteredResultsByExcludedGroups.getOrPut(excludedGroups) {
                    filteredSearchResults(tab, excludedGroups)
                }
            }

            val visibleResults = computeVisibleSearchResults(tab, filtered())
            val friendFacetBase = filtered(setOf(SearchFilterGroup.FRIEND))
            val friendFacets = buildSearchFriendFacets(friendFacetBase)
            tab.friendFacets.clear()
            tab.friendFacets.addAll(friendFacets)
            if (tab.selectedFriendFacetId != null && tab.friendFacets.none { it.friendId == tab.selectedFriendFacetId }) {
                tab.selectedFriendFacetId = null
            }
            val activeFilters = buildSearchActiveFilters(tab, friendFacets)
            val shown = visibleResults.size
            val total = tab.results.size
            val selectedCount = tab.selectedResultKeys.size
            val descriptor = searchTabDescriptor(tab, presentationCategory)
            val subtitle = buildSearchSubtitle(tab, activeFilters, shown, total, selectedCount)
            val categoryFacetOptions = buildSearchCategoryFacetOptions(
                tab,
                buildSearchFacetAggregation(tab, filtered(setOf(SearchFilterGroup.CATEGORY)))
            )
            val sourceFacetOptions = buildSearchSourceFacetOptions(
                tab,
                buildSearchFacetAggregation(tab, filtered(setOf(SearchFilterGroup.SOURCE)))
            )
            val friendFacetOptions = buildSearchFriendFacetOptions(
                friendFacetBase,
                buildSearchFacetAggregation(tab, friendFacetBase).anyFriendCount
            )
            val propertyFacetOptions = searchPropertyFacetsForCategory(presentationCategory).associateWith { facet ->
                buildSearchPropertyFacetOptions(filtered(setOf(searchPropertyFacetGroup(facet))), facet)
            }
            tab.descriptor = descriptor
            tab.subtitle = subtitle
            tab.presentationState = SearchPresentationState(
                presentationCategory = presentationCategory,
                visibleResults = visibleResults,
                activeFilters = activeFilters,
                categoryFacetOptions = categoryFacetOptions,
                sourceFacetOptions = sourceFacetOptions,
                friendFacetOptions = friendFacetOptions,
                propertyFacetOptions = propertyFacetOptions,
                shownCount = shown,
                totalCount = total,
                descriptor = descriptor,
                subtitle = subtitle
            )
        }
    }

    private fun buildSearchSubtitle(
        tab: SearchTabSession,
        activeFilters: List<SearchActiveFilterToken>,
        shownCount: Int,
        totalCount: Int,
        selectedCount: Int
    ): String {
        return buildString {
            append(tr("{0} of {1} shown", shownCount, totalCount))
            if (activeFilters.isNotEmpty()) {
                append(" · ")
                append(tr("{0} filter(s)", activeFilters.size))
                append(": ")
                append(activeFilters.take(3).joinToString(" · ") { it.label })
                if (activeFilters.size > 3) {
                    append(" · ")
                    append(tr("+{0} more", activeFilters.size - 3))
                }
            }
            if (tab.searchType in setOf(SearchDetails.SearchType.KEYWORD, SearchDetails.SearchType.WHATS_NEW)) {
                append(" · ")
                append(if (tab.searchRunning) tr("search running") else tr("search stopped"))
            }
            if (selectedCount > 0) {
                append(" · ")
                append(tr("{0} selected", selectedCount))
            }
        }
    }

    private fun scheduleSearchTabPresentationRefresh(
        tab: SearchTabSession,
        dirtyMask: Int = SearchPresentationDirty.FULL
    ) {
        tab.presentationDirtyMask = tab.presentationDirtyMask or dirtyMask
        if (tab.presentationRefreshScheduled) {
            return
        }
        tab.presentationRefreshScheduled = true
        EventQueue.invokeLater {
            if (tab !in searchTabs) {
                tab.presentationRefreshScheduled = false
                tab.presentationDirtyMask = 0
                return@invokeLater
            }
            refreshSearchTabPresentation(tab)
        }
    }

    private fun buildSearchActiveFilters(
        tab: SearchTabSession,
        friendFacets: List<SearchFriendFacet>
    ): List<SearchActiveFilterToken> {
        val filters = mutableListOf<SearchActiveFilterToken>()
        if (tab.filterText.isNotBlank()) {
            filters += SearchActiveFilterToken(
                type = SearchActiveFilterType.TEXT,
                key = "text",
                label = tr("Text: {0}", tab.filterText.trim())
            )
        }
        if (tab.friendsOnly) {
            filters += SearchActiveFilterToken(
                type = SearchActiveFilterType.FRIENDS_ONLY,
                key = "friendsOnly",
                label = tr("Friends only")
            )
        }
        if (tab.sourceFilter != SearchSourceFilter.ALL) {
            filters += SearchActiveFilterToken(
                type = SearchActiveFilterType.SOURCE,
                key = tab.sourceFilter.name,
                label = searchSourceFilterLabel(tab.sourceFilter)
            )
        }
        tab.selectedFriendFacetId?.let { friendId ->
            val label = friendFacets.firstOrNull { it.friendId == friendId }?.label ?: friendId
            filters += SearchActiveFilterToken(
                type = SearchActiveFilterType.FRIEND,
                key = friendId,
                label = tr("Friend: {0}", label)
            )
        }
        if (tab.category == SearchCategory.ALL) {
            tab.displayCategory?.let { category ->
                filters += SearchActiveFilterToken(
                    type = SearchActiveFilterType.CATEGORY,
                    key = category.name,
                    label = tr("Category: {0}", friendlyCategoryName(category))
                )
            }
        }
        tab.selectedPropertyFacets.forEach { (facet, value) ->
            filters += SearchActiveFilterToken(
                type = SearchActiveFilterType.PROPERTY,
                key = facet.name,
                secondaryKey = value,
                label = tr("{0}: {1}", searchPropertyFacetLabel(facet), value)
            )
        }
        tab.selectedRangeFacets.forEach { (facet, selection) ->
            rangeSelectionLabel(tab, facet, selection)?.let { label ->
                filters += SearchActiveFilterToken(
                    type = SearchActiveFilterType.RANGE,
                    key = facet.name,
                    secondaryKey = "${selection.minimumId.orEmpty()}|${selection.maximumId.orEmpty()}",
                    label = tr("{0}: {1}", searchRangeFacetLabel(facet), label)
                )
            }
        }
        return filters
    }

    private fun buildSearchFriendFacets(results: List<GroupedSearchResult>): List<SearchFriendFacet> {
        val counts = linkedMapOf<Pair<String, String>, Int>()
        results.forEach { result ->
            result.friends
                .asSequence()
                .map { it.id to it.renderName }
                .distinctBy { it.first }
                .forEach { friend ->
                    counts[friend] = (counts[friend] ?: 0) + 1
                }
        }
        return counts.entries
            .sortedWith(compareByDescending<Map.Entry<Pair<String, String>, Int>> { it.value }.thenBy { it.key.second.lowercase(Locale.US) })
            .map { (friend, count) ->
                SearchFriendFacet(
                    friendId = friend.first,
                    label = friend.second,
                    resultCount = count
                )
            }
    }

    private fun buildSearchCategoryFacetOptions(
        tab: SearchTabSession,
        aggregation: SearchFacetAggregation
    ): List<SearchFacetOption> {
        if (tab.category != SearchCategory.ALL) {
            return emptyList()
        }
        val counts = SearchCategory.entries
            .filter { it != SearchCategory.ALL }
            .mapNotNull { category ->
                val count = aggregation.categoryCounts[category] ?: 0
                if (count > 0) {
                    SearchFacetOption(
                        id = category.name,
                        label = friendlyCategoryName(category),
                        resultCount = count
                    )
                } else {
                    null
                }
            }
        return listOf(
            SearchFacetOption(
                id = SearchCategory.ALL.name,
                label = tr("All Files"),
                resultCount = aggregation.totalCount
            )
        ) + counts
    }

    private fun buildSearchSourceFacetOptions(
        tab: SearchTabSession,
        aggregation: SearchFacetAggregation
    ): List<SearchFacetOption> {
        return availableSearchSourceFilters(tab).mapNotNull { filter ->
            val count = if (filter == SearchSourceFilter.ALL) {
                aggregation.totalCount
            } else {
                aggregation.sourceCounts[filter] ?: 0
            }
            if (filter == SearchSourceFilter.ALL || count > 0) {
                SearchFacetOption(
                    id = filter.name,
                    label = searchSourceFilterLabel(filter),
                    resultCount = count
                )
            } else {
                null
            }
        }
    }

    private fun buildSearchFriendFacetOptions(base: List<GroupedSearchResult>, anyFriendCount: Int): List<SearchFacetOption> {
        val facets = buildFacetOptions(base) { result ->
            result.friends.map { friend ->
                friend.id to friend.renderName.ifBlank { friend.id }
            }.toSet()
        }
        return listOf(
            SearchFacetOption(
                id = "",
                label = tr("Any Friend"),
                resultCount = anyFriendCount
            )
        ) + facets
    }

    private fun buildSearchFacetAggregation(
        tab: SearchTabSession,
        base: List<GroupedSearchResult>
    ): SearchFacetAggregation {
        val availableSourceFilters = availableSearchSourceFilters(tab).filter { it != SearchSourceFilter.ALL }
        val categoryCounts = linkedMapOf<SearchCategory, Int>()
        val sourceCounts = linkedMapOf<SearchSourceFilter, Int>()
        var anyFriendCount = 0
        base.forEach { result ->
            val categories = LinkedHashSet<SearchCategory>()
            result.searchResults.forEach { searchResult ->
                categories += SearchCategory.forCategory(searchResult.category)
            }
            categories.forEach { category ->
                if (category != SearchCategory.ALL) {
                    categoryCounts[category] = (categoryCounts[category] ?: 0) + 1
                }
            }
            availableSourceFilters.forEach { filter ->
                if (matchesSearchSourceFilter(result, filter)) {
                    sourceCounts[filter] = (sourceCounts[filter] ?: 0) + 1
                }
            }
            if (result.friends.isNotEmpty()) {
                anyFriendCount += 1
            }
        }
        return SearchFacetAggregation(
            categoryCounts = categoryCounts,
            sourceCounts = sourceCounts,
            anyFriendCount = anyFriendCount,
            totalCount = base.size
        )
    }

    private fun buildSearchPropertyFacetOptions(
        base: List<GroupedSearchResult>,
        facet: SearchPropertyFacet
    ): List<SearchFacetOption> {
        return buildFacetOptions(base) { result ->
            searchFacetValues(result, facet)
        }
    }

    private fun matchesSearchSourceFilter(result: GroupedSearchResult, filter: SearchSourceFilter): Boolean {
        return when (filter) {
            SearchSourceFilter.ALL -> true
            SearchSourceFilter.FRIENDS -> result.friends.isNotEmpty()
            SearchSourceFilter.NETWORK -> result.friends.isEmpty() && !isEd2kSearchResult(result)
            SearchSourceFilter.ED2K_KAD -> isEd2kSearchResult(result)
            SearchSourceFilter.BROWSABLE -> result.searchResults.any { it.source.isBrowseHostEnabled }
        }
    }

    private fun matchesSelectedFriendFacet(result: GroupedSearchResult, friendId: String?): Boolean {
        return friendId == null || result.friends.any { it.id == friendId }
    }

    private fun searchTabDescriptor(
        tab: SearchTabSession,
        presentationCategory: SearchCategory = searchPresentationCategory(tab)
    ): String {
        val typeLabel = when (tab.searchType) {
            SearchDetails.SearchType.KEYWORD -> tr("Keyword Search")
            SearchDetails.SearchType.WHATS_NEW -> tr("What’s New")
            SearchDetails.SearchType.SINGLE_BROWSE -> tr("Browse Friend")
            SearchDetails.SearchType.MULTIPLE_BROWSE -> tr("Browse Files")
            SearchDetails.SearchType.ALL_FRIENDS_BROWSE -> tr("Browse Friends' Files")
        }
        return tr("{0} · {1}", typeLabel, friendlyCategoryName(presentationCategory))
    }

    private fun searchSourceFilterLabel(filter: SearchSourceFilter): String {
        return when (filter) {
            SearchSourceFilter.ALL -> tr("All sources")
            SearchSourceFilter.FRIENDS -> tr("Friend sources")
            SearchSourceFilter.NETWORK -> tr("WireShare network")
            SearchSourceFilter.ED2K_KAD -> tr("ED2K/Kad")
            SearchSourceFilter.BROWSABLE -> tr("Browsable")
        }
    }

    private fun whatsNewTitle(category: SearchCategory): String {
        return when (category) {
            SearchCategory.AUDIO -> tr("New audio")
            SearchCategory.DOCUMENT -> tr("New documents")
            SearchCategory.IMAGE -> tr("New images")
            SearchCategory.PROGRAM -> tr("New programs")
            SearchCategory.VIDEO -> tr("New videos")
            SearchCategory.TORRENT -> tr("New torrents")
            SearchCategory.OTHER,
            SearchCategory.ALL -> tr("New files")
        }
    }

    private fun friendlyCategoryName(category: SearchCategory): String {
        return when (category) {
            SearchCategory.ALL -> tr("All")
            SearchCategory.AUDIO -> tr("Audio")
            SearchCategory.VIDEO -> tr("Video")
            SearchCategory.IMAGE -> tr("Images")
            SearchCategory.DOCUMENT -> tr("Documents")
            SearchCategory.PROGRAM -> tr("Programs")
            SearchCategory.TORRENT -> tr("Torrents")
            SearchCategory.OTHER -> tr("Other")
        }
    }

    private fun humanizeEnumName(name: String): String {
        return name
            .lowercase(Locale.US)
            .split('_')
            .joinToString(" ") { token -> token.replaceFirstChar(Char::titlecase) }
    }

    private fun searchPrimaryResult(result: GroupedSearchResult): SearchResult? = result.searchResults.firstOrNull()

    private fun searchBaseName(result: GroupedSearchResult): String {
        val primary = searchPrimaryResult(result)
        return primary?.fileNameWithoutExtension?.takeIf(String::isNotBlank)
            ?.lowercase(Locale.US)
            ?: FileUtils.getFilenameNoExtension(result.fileName).lowercase(Locale.US)
    }

    private fun searchExtension(result: GroupedSearchResult): String {
        val primary = searchPrimaryResult(result)
        return primary?.fileExtension?.takeIf(String::isNotBlank)
            ?.lowercase(Locale.US)
            ?: FileUtils.getFileExtension(result.fileName).lowercase(Locale.US)
    }

    private fun searchSourceLabel(result: GroupedSearchResult): String {
        return result.friends.firstOrNull()?.renderName?.takeIf(String::isNotBlank)?.lowercase(Locale.US)
            ?: result.sources.firstOrNull()?.friendPresence?.friend?.renderName?.takeIf(String::isNotBlank)?.lowercase(Locale.US)
            ?: if (result.isAnonymous) tr("P2P").lowercase(Locale.US) else tr("Network").lowercase(Locale.US)
    }

    private fun searchAudioTitleKey(result: GroupedSearchResult): String {
        val primary = searchPrimaryResult(result)
        return primary?.getProperty(FilePropertyKey.TITLE)?.toString()?.takeIf(String::isNotBlank)
            ?.lowercase(Locale.US)
            ?: result.fileName.lowercase(Locale.US)
    }

    private fun searchNameSortKey(result: GroupedSearchResult, category: SearchCategory): String {
        return when (category) {
            SearchCategory.AUDIO -> searchAudioTitleKey(result)
            else -> result.fileName.lowercase(Locale.US)
        }
    }

    private fun searchTrackSortValue(result: GroupedSearchResult): TrackSortValue {
        return trackSortValue(searchPrimaryResult(result)?.getProperty(FilePropertyKey.TRACK_NUMBER))
    }

    private fun searchBitrateValue(result: GroupedSearchResult): Long {
        return searchSortLong(result, FilePropertyKey.BITRATE)
    }

    private fun searchQualityRank(result: GroupedSearchResult): Long {
        return searchSortLong(result, FilePropertyKey.QUALITY)
    }

    private fun searchNameComparator(category: SearchCategory): Comparator<GroupedSearchResult> {
        val base = compareBy<GroupedSearchResult> { searchNameSortKey(it, category) }
            .thenBy { searchBaseName(it) }
            .thenBy { searchExtension(it) }
            .thenBy { it.fileName.lowercase(Locale.US) }
        return when (category) {
            SearchCategory.PROGRAM -> base.thenBy { primarySize(it) }
            else -> base
        }
    }

    private fun libraryBaseName(item: LocalFileItem): String {
        return FileUtils.getFilenameNoExtension(item.fileName).lowercase(Locale.US)
    }

    private fun libraryExtension(item: LocalFileItem): String {
        return FileUtils.getFileExtension(item.fileName).lowercase(Locale.US)
    }

    private fun libraryTrackSortValue(item: LocalFileItem): TrackSortValue {
        return trackSortValue(item.getProperty(FilePropertyKey.TRACK_NUMBER))
    }

    private fun libraryAudioTitleKey(item: LocalFileItem): String {
        return item.getPropertyString(FilePropertyKey.TITLE)?.takeIf(String::isNotBlank)
            ?.lowercase(Locale.US)
            ?: item.fileName.lowercase(Locale.US)
    }

    private fun libraryNameSortKey(item: LocalFileItem): String {
        return when (item.category) {
            Category.AUDIO -> libraryAudioTitleKey(item)
            else -> item.fileName.lowercase(Locale.US)
        }
    }

    private fun trackSortValue(value: Any?): TrackSortValue {
        if (value == null) {
            return TrackSortValue()
        }
        val raw = value.toString().trim()
        if (raw.isEmpty()) {
            return TrackSortValue()
        }
        val numeric = CommonUtils.parseLongNoException(raw)
            ?: raw.substringBefore('/').trim().takeIf(String::isNotEmpty)?.let(CommonUtils::parseLongNoException)
        return if (numeric != null) {
            TrackSortValue(numeric = numeric)
        } else {
            TrackSortValue(text = raw.lowercase(Locale.US))
        }
    }

    private data class TrackSortValue(
        val numeric: Long? = null,
        val text: String = ""
    ) : Comparable<TrackSortValue> {
        override fun compareTo(other: TrackSortValue): Int {
            return when {
                numeric != null && other.numeric != null -> numeric.compareTo(other.numeric)
                numeric != null -> -1
                other.numeric != null -> 1
                text.isBlank() && other.text.isBlank() -> 0
                text.isBlank() -> 1
                other.text.isBlank() -> -1
                else -> text.compareTo(other.text)
            }
        }
    }

    private fun defaultAdditionalSearchColumns(category: SearchCategory): Set<SearchColumn> {
        if (defaultSearchLayout.visibleColumns.any { it !in LEGACY_SEARCH_COLUMNS }) {
            return emptySet()
        }
        return when (category) {
            SearchCategory.AUDIO -> setOf(
                SearchColumn.FROM,
                SearchColumn.FILENAME,
                SearchColumn.EXTENSION,
                SearchColumn.LENGTH,
                SearchColumn.QUALITY,
                SearchColumn.BITRATE,
                SearchColumn.TRACK
            )
            SearchCategory.VIDEO,
            SearchCategory.DOCUMENT,
            SearchCategory.PROGRAM,
            SearchCategory.TORRENT,
            SearchCategory.ALL,
            SearchCategory.IMAGE,
            SearchCategory.OTHER -> setOf(SearchColumn.FROM, SearchColumn.FILENAME, SearchColumn.EXTENSION)
        }
    }

    private fun defaultSearchVisibleColumnsForCategory(category: SearchCategory): Set<SearchColumn> {
        return defaultSearchLayout.visibleColumns + defaultAdditionalSearchColumns(category)
    }

    private fun defaultAdditionalLibraryColumns(category: Category?): Set<LibraryColumn> {
        if (visibleLibraryColumns.any { it !in LEGACY_LIBRARY_COLUMNS }) {
            return emptySet()
        }
        return when (category) {
            Category.AUDIO -> setOf(
                LibraryColumn.FILENAME,
                LibraryColumn.EXTENSION,
                LibraryColumn.LENGTH,
                LibraryColumn.BITRATE,
                LibraryColumn.TRACK
            )
            Category.VIDEO,
            Category.DOCUMENT,
            Category.PROGRAM,
            Category.TORRENT,
            Category.IMAGE,
            Category.OTHER,
            null -> setOf(LibraryColumn.FILENAME, LibraryColumn.EXTENSION)
        }
    }

    private fun searchColumnsForCategory(category: SearchCategory): List<SearchColumn> {
        return buildList {
            addAll(
                listOf(
                    SearchColumn.NAME,
                    SearchColumn.FROM,
                    SearchColumn.FILENAME,
                    SearchColumn.EXTENSION,
                    SearchColumn.TYPE,
                    SearchColumn.SIZE,
                    SearchColumn.SOURCES,
                    SearchColumn.FRIENDS
                )
            )
            when (category) {
                SearchCategory.AUDIO -> addAll(
                    listOf(
                        SearchColumn.ARTIST,
                        SearchColumn.ALBUM,
                        SearchColumn.LENGTH,
                        SearchColumn.QUALITY,
                        SearchColumn.BITRATE,
                        SearchColumn.GENRE,
                        SearchColumn.TRACK,
                        SearchColumn.YEAR
                    )
                )
                SearchCategory.VIDEO -> addAll(listOf(SearchColumn.YEAR, SearchColumn.GENRE, SearchColumn.DESCRIPTION))
                SearchCategory.DOCUMENT -> addAll(listOf(SearchColumn.AUTHOR, SearchColumn.DESCRIPTION))
                SearchCategory.PROGRAM -> addAll(listOf(SearchColumn.PLATFORM, SearchColumn.COMPANY, SearchColumn.DESCRIPTION))
                SearchCategory.TORRENT -> addAll(listOf(SearchColumn.FILES, SearchColumn.TRACKERS))
                SearchCategory.ALL,
                SearchCategory.IMAGE,
                SearchCategory.OTHER -> add(SearchColumn.DESCRIPTION)
            }
        }.distinct()
    }

    private fun searchSortModesForCategory(category: SearchCategory): List<SearchSortMode> {
        return buildList {
            addAll(
                listOf(
                    SearchSortMode.RELEVANCE,
                    SearchSortMode.NAME,
                    SearchSortMode.FROM,
                    SearchSortMode.FILENAME,
                    SearchSortMode.EXTENSION,
                    SearchSortMode.TYPE,
                    SearchSortMode.SIZE,
                    SearchSortMode.SOURCES,
                    SearchSortMode.FRIENDS
                )
            )
            when (category) {
                SearchCategory.AUDIO -> addAll(
                    listOf(
                        SearchSortMode.ARTIST,
                        SearchSortMode.ALBUM,
                        SearchSortMode.LENGTH,
                        SearchSortMode.QUALITY,
                        SearchSortMode.BITRATE,
                        SearchSortMode.GENRE,
                        SearchSortMode.TRACK,
                        SearchSortMode.YEAR
                    )
                )
                SearchCategory.VIDEO -> addAll(listOf(SearchSortMode.YEAR, SearchSortMode.GENRE, SearchSortMode.DESCRIPTION))
                SearchCategory.DOCUMENT -> addAll(listOf(SearchSortMode.AUTHOR, SearchSortMode.DESCRIPTION))
                SearchCategory.PROGRAM -> addAll(listOf(SearchSortMode.PLATFORM, SearchSortMode.COMPANY, SearchSortMode.DESCRIPTION))
                SearchCategory.TORRENT -> addAll(listOf(SearchSortMode.FILES, SearchSortMode.TRACKERS))
                SearchCategory.ALL,
                SearchCategory.IMAGE,
                SearchCategory.OTHER -> add(SearchSortMode.DESCRIPTION)
            }
        }.distinct()
    }

    private fun libraryColumnsForCategory(category: Category?): List<LibraryColumn> {
        return buildList {
            addAll(
                listOf(
                    LibraryColumn.NAME,
                    LibraryColumn.FILENAME,
                    LibraryColumn.EXTENSION,
                    LibraryColumn.TYPE,
                    LibraryColumn.SIZE,
                    LibraryColumn.ACTIVITY,
                    LibraryColumn.HITS,
                    LibraryColumn.UPLOADS,
                    LibraryColumn.UPLOAD_ATTEMPTS,
                    LibraryColumn.UPDATED,
                    LibraryColumn.LOCATION
                )
            )
            when (category) {
                Category.AUDIO -> addAll(
                    listOf(
                        LibraryColumn.ARTIST,
                        LibraryColumn.ALBUM,
                        LibraryColumn.LENGTH,
                        LibraryColumn.BITRATE,
                        LibraryColumn.GENRE,
                        LibraryColumn.TRACK,
                        LibraryColumn.YEAR,
                        LibraryColumn.DESCRIPTION
                    )
                )
                Category.VIDEO -> addAll(listOf(LibraryColumn.YEAR, LibraryColumn.GENRE, LibraryColumn.DESCRIPTION))
                Category.DOCUMENT -> addAll(listOf(LibraryColumn.AUTHOR, LibraryColumn.DESCRIPTION))
                Category.PROGRAM -> addAll(listOf(LibraryColumn.PLATFORM, LibraryColumn.COMPANY, LibraryColumn.DESCRIPTION))
                Category.TORRENT -> addAll(listOf(LibraryColumn.FILES, LibraryColumn.TRACKERS))
                Category.IMAGE,
                Category.OTHER,
                null -> add(LibraryColumn.DESCRIPTION)
            }
        }.distinct()
    }

    private fun librarySortModesForCategory(category: Category?): List<LibrarySortMode> {
        return buildList {
            addAll(
                listOf(
                    LibrarySortMode.NAME,
                    LibrarySortMode.FILENAME,
                    LibrarySortMode.EXTENSION,
                    LibrarySortMode.TYPE,
                    LibrarySortMode.SIZE,
                    LibrarySortMode.ACTIVITY,
                    LibrarySortMode.HITS,
                    LibrarySortMode.UPLOADS,
                    LibrarySortMode.UPLOAD_ATTEMPTS,
                    LibrarySortMode.UPDATED,
                    LibrarySortMode.LOCATION
                )
            )
            when (category) {
                Category.AUDIO -> addAll(
                    listOf(
                        LibrarySortMode.ARTIST,
                        LibrarySortMode.ALBUM,
                        LibrarySortMode.LENGTH,
                        LibrarySortMode.BITRATE,
                        LibrarySortMode.GENRE,
                        LibrarySortMode.TRACK,
                        LibrarySortMode.YEAR,
                        LibrarySortMode.DESCRIPTION
                    )
                )
                Category.VIDEO -> addAll(listOf(LibrarySortMode.YEAR, LibrarySortMode.GENRE, LibrarySortMode.DESCRIPTION))
                Category.DOCUMENT -> addAll(listOf(LibrarySortMode.AUTHOR, LibrarySortMode.DESCRIPTION))
                Category.PROGRAM -> addAll(listOf(LibrarySortMode.PLATFORM, LibrarySortMode.COMPANY, LibrarySortMode.DESCRIPTION))
                Category.TORRENT -> addAll(listOf(LibrarySortMode.FILES, LibrarySortMode.TRACKERS))
                Category.IMAGE,
                Category.OTHER,
                null -> add(LibrarySortMode.DESCRIPTION)
            }
        }.distinct()
    }

    private fun searchSortText(result: GroupedSearchResult, key: FilePropertyKey): String {
        return searchSortText(result) { candidate -> candidate.getProperty(key) }
    }

    private fun searchSortText(result: GroupedSearchResult, value: (SearchResult) -> Any?): String {
        val primary = result.searchResults.firstOrNull() ?: return ""
        return propertyText(value(primary))
    }

    private fun searchSortLong(result: GroupedSearchResult, key: FilePropertyKey): Long {
        val primary = result.searchResults.firstOrNull() ?: return Long.MIN_VALUE
        return propertyLong(primary.getProperty(key))
    }

    private fun librarySortText(item: LocalFileItem, key: FilePropertyKey): String {
        return when (key) {
            FilePropertyKey.LOCATION -> item.getPropertyString(key)?.takeIf(String::isNotBlank)
                ?: item.file.parent.orEmpty().lowercase(Locale.US)
            else -> propertyText(item.getProperty(key))
        }
    }

    private fun librarySortLong(item: LocalFileItem, key: FilePropertyKey): Long {
        return propertyLong(item.getProperty(key))
    }

    private fun propertyTrackText(value: Any?): String {
        return value?.toString()?.trim().orEmpty()
    }

    private fun compareSearchResultsBySource(
        left: GroupedSearchResult,
        right: GroupedSearchResult,
        category: SearchCategory
    ): Int {
        val sourceCountCompare = left.sources.size.compareTo(right.sources.size)
        if (sourceCountCompare != 0) {
            return sourceCountCompare
        }
        if (left.sources.size == 1 && right.sources.size == 1) {
            val nameCompare = compareNullableIgnoreCase(singleSourceFriendName(left), singleSourceFriendName(right), nullsFirst = false)
            if (nameCompare != 0) {
                return nameCompare
            }
        }
        return searchNameComparator(category).compare(left, right)
    }

    private fun singleSourceFriendName(result: GroupedSearchResult): String? {
        if (result.sources.size != 1) {
            return null
        }
        return result.friends.singleOrNull()?.renderName
            ?: result.sources.firstOrNull()?.friendPresence?.friend?.renderName
    }

    private fun compareNullableIgnoreCase(left: String?, right: String?, nullsFirst: Boolean): Int {
        return when {
            left === right -> 0
            left == null -> if (nullsFirst) -1 else 1
            right == null -> if (nullsFirst) 1 else -1
            else -> left.compareTo(right, ignoreCase = true)
        }
    }

    private fun searchSpamPresortRank(result: GroupedSearchResult): Int {
        return if (result.searchResults.any(SearchResult::isSpam)) 1 else 0
    }

    private fun searchLocalPresortRank(result: GroupedSearchResult): Int {
        return searchLocalAvailability(result).localRank
    }

    private fun searchResultDownloadItem(result: GroupedSearchResult): DownloadItem? {
        return searchDownloadIndex().byUrn[searchResultKeyOf(result)]
    }

    private fun searchTorrentFilesCount(result: GroupedSearchResult): Int {
        val torrent = result.searchResults.firstOrNull()?.getProperty(FilePropertyKey.TORRENT) as? Torrent ?: return 0
        return torrent.torrentFileEntries?.size ?: 0
    }

    private fun searchTorrentTrackersCount(result: GroupedSearchResult): Int {
        val torrent = result.searchResults.firstOrNull()?.getProperty(FilePropertyKey.TORRENT) as? Torrent ?: return 0
        return torrent.trackers?.size ?: 0
    }

    private fun libraryTorrentFilesCount(item: LocalFileItem): Int {
        val torrent = item.getProperty(FilePropertyKey.TORRENT) as? Torrent ?: return 0
        return torrent.torrentFileEntries?.size ?: 0
    }

    private fun libraryTorrentTrackersCount(item: LocalFileItem): Int {
        val torrent = item.getProperty(FilePropertyKey.TORRENT) as? Torrent ?: return 0
        return torrent.trackers?.size ?: 0
    }

    private fun propertyText(value: Any?): String {
        return when (value) {
            null -> ""
            is String -> value.trim().lowercase(Locale.US)
            else -> value.toString().trim().lowercase(Locale.US)
        }
    }

    private fun propertyLong(value: Any?): Long {
        return when (value) {
            is Number -> value.toLong()
            is String -> value.toLongOrNull() ?: Long.MIN_VALUE
            else -> Long.MIN_VALUE
        }
    }

    private fun deleteActionLabel(): String {
        return when {
            OSUtils.isWindows() && OSUtils.supportsTrash() -> tr("Move to Recycle Bin")
            OSUtils.isMacOSX() && OSUtils.supportsTrash() -> tr("Move to Trash")
            else -> tr("Delete from Disk")
        }
    }

    private fun deleteDialogTitle(count: Int): String {
        return when {
            OSUtils.isWindows() && OSUtils.supportsTrash() ->
                tr(if (count == 1) "Move File to the Recycle Bin" else "Move Files to the Recycle Bin")
            OSUtils.isMacOSX() && OSUtils.supportsTrash() ->
                tr(if (count == 1) "Move File to the Trash" else "Move Files to the Trash")
            else ->
                tr(if (count == 1) "Delete File" else "Delete Files")
        }
    }

    private fun deleteDialogMessage(count: Int, fileName: String? = null): String {
        return when {
            OSUtils.isWindows() && OSUtils.supportsTrash() ->
                if (count == 1 && fileName != null) {
                    tr("Move \"{0}\" to the Recycle Bin?", fileName)
                } else {
                    tr("Move {0} selected files to the Recycle Bin?", count)
                }
            OSUtils.isMacOSX() && OSUtils.supportsTrash() ->
                if (count == 1 && fileName != null) {
                    tr("Move \"{0}\" to the Trash?", fileName)
                } else {
                    tr("Move {0} selected files to the Trash?", count)
                }
            else ->
                if (count == 1 && fileName != null) {
                    tr("Delete \"{0}\" from disk?", fileName)
                } else {
                    tr("Delete {0} selected files from disk?", count)
                }
        }
    }

    private fun deleteChoiceDialogTitle(count: Int): String {
        return when {
            OSUtils.isWindows() && OSUtils.supportsTrash() ->
                tr(if (count == 1) "Move File to the Recycle Bin or Remove from Library" else "Move Files to the Recycle Bin or Remove from Library")
            OSUtils.isMacOSX() && OSUtils.supportsTrash() ->
                tr(if (count == 1) "Move File to the Trash or Remove from Library" else "Move Files to the Trash or Remove from Library")
            else ->
                tr(if (count == 1) "Delete File or Remove from Library" else "Delete Files or Remove from Library")
        }
    }

    private fun deleteChoiceDialogMessage(count: Int): String {
        return when {
            OSUtils.isWindows() && OSUtils.supportsTrash() ->
                tr(if (count == 1) "Do you want to move this file to the Recycle Bin or just remove it from My Files?" else "Do you want to move these files to the Recycle Bin or just remove them from My Files?")
            OSUtils.isMacOSX() && OSUtils.supportsTrash() ->
                tr(if (count == 1) "Do you want to move this file to the Trash or just remove it from My Files?" else "Do you want to move these files to the Trash or just remove them from My Files?")
            else ->
                tr(if (count == 1) "Do you want to delete this file from disk or just remove it from My Files?" else "Do you want to delete these files from disk or just remove them from My Files?")
        }
    }

    private fun matchesLibraryFilter(item: LocalFileItem, filter: String): Boolean {
        return listOfNotNull(
            item.fileName,
            FileUtils.getFilenameNoExtension(item.fileName),
            FileUtils.getFileExtension(item.fileName),
            item.file.absolutePath,
            item.category.getPluralName(),
            item.category.getSingularName(),
            item.getPropertyString(FilePropertyKey.AUTHOR),
            item.getPropertyString(FilePropertyKey.ALBUM),
            propertyTrackText(item.getProperty(FilePropertyKey.TRACK_NUMBER)).ifBlank { null },
            item.getPropertyString(FilePropertyKey.BITRATE),
            item.getPropertyString(FilePropertyKey.LENGTH),
            item.getPropertyString(FilePropertyKey.GENRE),
            item.getPropertyString(FilePropertyKey.TITLE),
            item.getPropertyString(FilePropertyKey.COMPANY),
            item.getPropertyString(FilePropertyKey.PLATFORM),
            item.getPropertyString(FilePropertyKey.DESCRIPTION),
            item.getPropertyString(FilePropertyKey.LOCATION)
        ).any { it.lowercase(Locale.US).contains(filter) }
    }

    private fun defaultSortDescending(mode: SearchSortMode): Boolean {
        return when (mode) {
            SearchSortMode.RELEVANCE,
            SearchSortMode.FROM,
            SearchSortMode.SIZE,
            SearchSortMode.SOURCES,
            SearchSortMode.FRIENDS,
            SearchSortMode.LENGTH,
            SearchSortMode.QUALITY,
            SearchSortMode.BITRATE,
            SearchSortMode.YEAR,
            SearchSortMode.FILES,
            SearchSortMode.TRACKERS -> true

            SearchSortMode.NAME,
            SearchSortMode.FILENAME,
            SearchSortMode.EXTENSION,
            SearchSortMode.TYPE,
            SearchSortMode.TRACK,
            SearchSortMode.ARTIST,
            SearchSortMode.ALBUM,
            SearchSortMode.GENRE,
            SearchSortMode.AUTHOR,
            SearchSortMode.COMPANY,
            SearchSortMode.PLATFORM,
            SearchSortMode.DESCRIPTION -> false
        }
    }

    private fun defaultDownloadSortDescending(mode: DownloadSortMode): Boolean {
        return when (mode) {
            DownloadSortMode.ORDER_ADDED,
            DownloadSortMode.PROGRESS,
            DownloadSortMode.RATE,
            DownloadSortMode.SOURCES -> true
            DownloadSortMode.TIME_LEFT,
            DownloadSortMode.FILE_TYPE,
            DownloadSortMode.EXTENSION,
            DownloadSortMode.STATUS,
            DownloadSortMode.NAME -> false
        }
    }

    private fun defaultUploadSortDescending(mode: UploadSortMode): Boolean {
        return when (mode) {
            UploadSortMode.ORDER_STARTED,
            UploadSortMode.UPLOADED,
            UploadSortMode.RATE,
            UploadSortMode.PEERS -> true
            UploadSortMode.TIME_LEFT,
            UploadSortMode.FILE_TYPE,
            UploadSortMode.EXTENSION,
            UploadSortMode.USER_NAME,
            UploadSortMode.STATUS,
            UploadSortMode.NAME -> false
        }
    }

    private fun saveSearchLayoutPreferences(tab: SearchTabSession) {
        settingsService.saveSearchLayoutPreferences(
            SearchLayoutPreferences(
                visibleColumns = tab.visibleColumns,
                sortMode = tab.sortMode,
                sortDescending = tab.sortDescending
            )
        )
    }

    private fun saveLibraryLayoutPreferences() {
        settingsService.saveLibraryLayoutPreferences(
            LibraryLayoutPreferences(
                visibleColumns = visibleLibraryColumns,
                sortMode = librarySortMode,
                sortDescending = librarySortDescending
            )
        )
    }

    private fun saveDownloadLayoutPreferences() {
        settingsService.saveDownloadLayoutPreferences(
            DownloadLayoutPreferences(
                visibleColumns = visibleDownloadColumns,
                sortMode = downloadSortMode,
                sortDescending = downloadSortDescending,
                filterMode = downloadFilterMode
            )
        )
    }

    private fun saveUploadLayoutPreferences() {
        settingsService.saveUploadLayoutPreferences(
            UploadLayoutPreferences(
                visibleColumns = visibleUploadColumns,
                sortMode = uploadSortMode,
                sortDescending = uploadSortDescending,
                filterMode = uploadFilterMode
            )
        )
    }

    fun updateSearchPaneFraction(value: Float, persist: Boolean = false) {
        searchResultsPaneFraction = value.coerceIn(0.18f, 0.82f)
        if (persist) {
            settingsService.saveSearchPaneLayoutPreferences(
                SearchPaneLayoutPreferences(
                    refinementRailVisible = searchRefinementRailVisible,
                    refinementRailFraction = searchRefinementRailFraction,
                    resultsFraction = searchResultsPaneFraction
                )
            )
        }
    }

    fun updateSearchRefinementRailFraction(value: Float, persist: Boolean = false) {
        searchRefinementRailFraction = value.coerceIn(0.16f, 0.45f)
        if (persist) {
            settingsService.saveSearchPaneLayoutPreferences(
                SearchPaneLayoutPreferences(
                    refinementRailVisible = searchRefinementRailVisible,
                    refinementRailFraction = searchRefinementRailFraction,
                    resultsFraction = searchResultsPaneFraction
                )
            )
        }
    }

    fun setSearchRefinementRailVisible(visible: Boolean, persist: Boolean = true) {
        searchRefinementRailVisible = visible
        if (persist) {
            settingsService.saveSearchPaneLayoutPreferences(
                SearchPaneLayoutPreferences(
                    refinementRailVisible = searchRefinementRailVisible,
                    refinementRailFraction = searchRefinementRailFraction,
                    resultsFraction = searchResultsPaneFraction
                )
            )
        }
    }

    fun updateLibraryPaneFraction(value: Float, persist: Boolean = false) {
        libraryNavigatorPaneFraction = value.coerceIn(0.14f, 0.5f)
        if (persist) {
            persistLibraryPaneLayoutPreferences()
        }
    }

    private fun persistLibraryPaneLayoutPreferences() {
        settingsService.saveLibraryPaneLayoutPreferences(
            LibraryPaneLayoutPreferences(
                navigatorFraction = libraryNavigatorPaneFraction,
                filtersVisible = libraryFiltersVisible
            )
        )
    }

    fun updateFriendsPaneFraction(value: Float, persist: Boolean = false) {
        friendsRosterPaneFraction = value.coerceIn(0.16f, 0.55f)
        if (persist) {
            settingsService.saveFriendsPaneLayoutPreferences(
                FriendsPaneLayoutPreferences(rosterFraction = friendsRosterPaneFraction)
            )
        }
    }

    fun updateTransferPaneFraction(value: Float, persist: Boolean = false) {
        transferMainAreaFraction = value.coerceIn(0.4f, 0.9f)
        if (persist) {
            settingsService.saveTransferPaneLayoutPreferences(
                TransferPaneLayoutPreferences(mainAreaFraction = transferMainAreaFraction)
            )
        }
    }

    private fun syncLibrarySectionLayouts() {
        val sectionIds = librarySections.map(LibrarySection::id).ifEmpty { listOf(DEFAULT_LIBRARY_SECTION_ID) }
        sectionIds.forEach { sectionId ->
            val state = librarySectionStates[sectionId] ?: LibrarySectionViewState()
            librarySectionStates[sectionId] = state.copy(
                sortMode = librarySortMode,
                sortDescending = librarySortDescending,
                visibleColumns = visibleLibraryColumns
            )
        }
    }

    private fun applySearchSelection(
        tab: SearchTabSession,
        selectedKeys: List<String>,
        primaryKey: String?,
        anchorKey: String?
    ) {
        tab.selectedResultKeys.clear()
        tab.selectedResultKeys.addAll(selectedKeys)
        tab.selectedResultKey = primaryKey
        tab.selectionAnchorKey = anchorKey
        scheduleSearchTabPresentationRefresh(tab, SearchPresentationDirty.SELECTION.mask)
    }

    private fun applyLibrarySelection(
        selectedKeys: List<String>,
        primaryKey: String?,
        anchorKey: String?
    ) {
        selectedLibraryItemPaths.clear()
        selectedLibraryItemPaths.addAll(selectedKeys)
        selectedLibraryItemPath = primaryKey
        librarySelectionAnchorPath = anchorKey
        invalidateLibrarySelectionCache()
    }

    private fun applyDownloadSelection(
        selectedKeys: List<String>,
        primaryKey: String?,
        anchorKey: String?
    ) {
        selectedDownloadUrns.clear()
        selectedDownloadUrns.addAll(selectedKeys)
        selectedDownloadUrn = primaryKey
        downloadSelectionAnchorUrn = anchorKey
    }

    private fun applyUploadSelection(
        selectedKeys: List<String>,
        primaryKey: String?,
        anchorKey: String?
    ) {
        selectedUploadUrns.clear()
        selectedUploadUrns.addAll(selectedKeys)
        selectedUploadUrn = primaryKey
        uploadSelectionAnchorUrn = anchorKey
    }

    private fun computeSelectionUpdate(
        visibleKeys: List<String>,
        currentSelection: List<String>,
        currentPrimary: String?,
        currentAnchor: String?,
        targetKey: String,
        extendSelection: Boolean,
        toggleSelection: Boolean
    ): SelectionUpdate {
        if (targetKey !in visibleKeys) {
            return SelectionUpdate(
                selectedKeys = currentSelection.filter { it in visibleKeys },
                primaryKey = currentPrimary?.takeIf { it in visibleKeys },
                anchorKey = currentAnchor?.takeIf { it in visibleKeys }
            )
        }

        val visibleSet = visibleKeys.toSet()
        val normalizedSelection = currentSelection.filter { it in visibleSet }.distinct()
        val normalizedPrimary = currentPrimary?.takeIf { it in visibleSet }
        val normalizedAnchor = currentAnchor?.takeIf { it in visibleSet }

        if (extendSelection) {
            val anchorKey = normalizedAnchor ?: normalizedPrimary ?: targetKey
            val anchorIndex = visibleKeys.indexOf(anchorKey).coerceAtLeast(0)
            val targetIndex = visibleKeys.indexOf(targetKey)
            val range = if (anchorIndex <= targetIndex) {
                visibleKeys.subList(anchorIndex, targetIndex + 1)
            } else {
                visibleKeys.subList(targetIndex, anchorIndex + 1)
            }
            return SelectionUpdate(
                selectedKeys = range,
                primaryKey = targetKey,
                anchorKey = anchorKey
            )
        }

        if (toggleSelection) {
            val updated = normalizedSelection.toMutableList()
            if (targetKey in updated) {
                updated.remove(targetKey)
            } else {
                updated.add(targetKey)
            }
            val nextPrimary = when {
                updated.isEmpty() -> null
                targetKey in updated -> targetKey
                normalizedPrimary in updated -> normalizedPrimary
                else -> updated.lastOrNull()
            }
            return SelectionUpdate(
                selectedKeys = updated,
                primaryKey = nextPrimary,
                anchorKey = if (updated.isEmpty()) null else targetKey
            )
        }

        return SelectionUpdate(
            selectedKeys = listOf(targetKey),
            primaryKey = targetKey,
            anchorKey = targetKey
        )
    }

    private fun <T> toggledColumns(columns: Set<T>, column: T): Set<T> {
        return if (column in columns) {
            if (columns.size == 1) {
                columns
            } else {
                columns - column
            }
        } else {
            columns + column
        }
    }

    private fun sortLibraryItems(items: List<LocalFileItem>): List<LocalFileItem> {
        val comparator = when (librarySortMode) {
            LibrarySortMode.NAME -> compareBy<LocalFileItem> { libraryNameSortKey(it) }
                .thenBy { it.fileName.lowercase(Locale.US) }
            LibrarySortMode.FILENAME -> compareBy<LocalFileItem> { libraryBaseName(it) }
                .thenBy { libraryExtension(it) }
                .thenBy { it.fileName.lowercase(Locale.US) }
            LibrarySortMode.EXTENSION -> compareBy<LocalFileItem> { libraryExtension(it) }
                .thenBy { libraryBaseName(it) }
            LibrarySortMode.TYPE -> compareBy<LocalFileItem> { it.category.name }.thenBy { it.fileName.lowercase(Locale.US) }
            LibrarySortMode.SIZE -> compareBy<LocalFileItem> { it.size }.thenBy { it.fileName.lowercase(Locale.US) }
            LibrarySortMode.ACTIVITY -> compareBy<LocalFileItem> { it.numHits + it.numUploads }.thenBy { it.fileName.lowercase(Locale.US) }
            LibrarySortMode.HITS -> compareBy<LocalFileItem> { it.numHits }.thenBy { it.fileName.lowercase(Locale.US) }
            LibrarySortMode.UPLOADS -> compareBy<LocalFileItem> { it.numUploads }.thenBy { it.fileName.lowercase(Locale.US) }
            LibrarySortMode.UPLOAD_ATTEMPTS -> compareBy<LocalFileItem> { it.numUploadAttempts }.thenBy { it.fileName.lowercase(Locale.US) }
            LibrarySortMode.UPDATED -> compareBy<LocalFileItem> { it.lastModifiedTime }.thenBy { it.fileName.lowercase(Locale.US) }
            LibrarySortMode.LOCATION -> compareBy<LocalFileItem> { librarySortText(it, FilePropertyKey.LOCATION) }.thenBy { it.fileName.lowercase(Locale.US) }
            LibrarySortMode.LENGTH -> compareBy<LocalFileItem> { librarySortLong(it, FilePropertyKey.LENGTH) }
                .thenBy { libraryAudioTitleKey(it) }
            LibrarySortMode.BITRATE -> compareBy<LocalFileItem> { librarySortLong(it, FilePropertyKey.BITRATE) }
                .thenBy { libraryAudioTitleKey(it) }
            LibrarySortMode.TRACK -> compareBy<LocalFileItem> { libraryTrackSortValue(it) }
                .thenBy { libraryAudioTitleKey(it) }
            LibrarySortMode.ARTIST -> compareBy<LocalFileItem> { librarySortText(it, FilePropertyKey.AUTHOR) }
                .thenBy { librarySortText(it, FilePropertyKey.ALBUM) }
                .thenBy { libraryTrackSortValue(it) }
                .thenBy { libraryAudioTitleKey(it) }
            LibrarySortMode.ALBUM -> compareBy<LocalFileItem> { librarySortText(it, FilePropertyKey.ALBUM) }
                .thenBy { libraryTrackSortValue(it) }
                .thenBy { libraryAudioTitleKey(it) }
            LibrarySortMode.GENRE -> compareBy<LocalFileItem> { librarySortText(it, FilePropertyKey.GENRE) }.thenBy { it.fileName.lowercase(Locale.US) }
            LibrarySortMode.YEAR -> compareBy<LocalFileItem> { librarySortLong(it, FilePropertyKey.YEAR) }.thenBy { it.fileName.lowercase(Locale.US) }
            LibrarySortMode.AUTHOR -> compareBy<LocalFileItem> { librarySortText(it, FilePropertyKey.AUTHOR) }.thenBy { it.fileName.lowercase(Locale.US) }
            LibrarySortMode.COMPANY -> compareBy<LocalFileItem> { librarySortText(it, FilePropertyKey.COMPANY) }.thenBy { it.fileName.lowercase(Locale.US) }
            LibrarySortMode.PLATFORM -> compareBy<LocalFileItem> { librarySortText(it, FilePropertyKey.PLATFORM) }.thenBy { it.fileName.lowercase(Locale.US) }
            LibrarySortMode.DESCRIPTION -> compareBy<LocalFileItem> { librarySortText(it, FilePropertyKey.DESCRIPTION) }.thenBy { it.fileName.lowercase(Locale.US) }
            LibrarySortMode.FILES -> compareBy<LocalFileItem> { libraryTorrentFilesCount(it) }.thenBy { it.fileName.lowercase(Locale.US) }
            LibrarySortMode.TRACKERS -> compareBy<LocalFileItem> { libraryTorrentTrackersCount(it) }.thenBy { it.fileName.lowercase(Locale.US) }
        }
        val sorted = items.sortedWith(comparator)
        return if (librarySortDescending) sorted.reversed() else sorted
    }

    private fun sortDownloads(items: List<DownloadItem>): List<DownloadItem> {
        val snapshots = items.map { item ->
            DownloadSortSnapshot(
                item = item,
                statusRank = downloadStateRank(item.state),
                orderAdded = item.startDate.time,
                timeLeft = item.remainingDownloadTime,
                percentComplete = item.percentComplete,
                name = (item.title ?: item.fileName).lowercase(Locale.US),
                fileType = item.category.getSingularName().lowercase(Locale.US),
                extension = FileUtils.getFileExtension(item.fileName).lowercase(Locale.US),
                rate = item.downloadSpeed,
                sources = item.downloadSourceCount
            )
        }
        val comparator = when (downloadSortMode) {
            DownloadSortMode.ORDER_ADDED -> compareBy<DownloadSortSnapshot> { it.orderAdded }
                .thenBy { it.name }
            DownloadSortMode.TIME_LEFT -> compareBy<DownloadSortSnapshot> { it.timeLeft }
                .thenBy { it.name }
            DownloadSortMode.FILE_TYPE -> compareBy<DownloadSortSnapshot> { it.fileType }
                .thenBy { it.name }
            DownloadSortMode.EXTENSION -> compareBy<DownloadSortSnapshot> { it.extension }
                .thenBy { it.name }
            DownloadSortMode.STATUS -> compareBy<DownloadSortSnapshot> { it.statusRank }
                .thenByDescending { it.percentComplete }
                .thenBy { it.name }
            DownloadSortMode.NAME -> compareBy<DownloadSortSnapshot> { it.name }
            DownloadSortMode.PROGRESS -> compareBy<DownloadSortSnapshot> { it.percentComplete }
                .thenBy { it.name }
            DownloadSortMode.RATE -> compareBy<DownloadSortSnapshot> { it.rate }
                .thenBy { it.name }
            DownloadSortMode.SOURCES -> compareBy<DownloadSortSnapshot> { it.sources }
                .thenBy { it.name }
        }
        val sorted = snapshots.sortedWith(comparator)
        return if (downloadSortDescending) sorted.asReversed().map { it.item } else sorted.map { it.item }
    }

    private fun sortUploads(items: List<UploadItem>): List<UploadItem> {
        val snapshots = items.map { item ->
            UploadSortSnapshot(
                item = item,
                statusRank = uploadStateRank(item.state),
                orderStarted = item.startTime,
                timeLeft = item.remainingUploadTime,
                uploaded = item.totalAmountUploaded,
                name = item.fileName.lowercase(Locale.US),
                fileType = item.category.getSingularName().lowercase(Locale.US),
                extension = FileUtils.getFileExtension(item.fileName).lowercase(Locale.US),
                rate = item.uploadSpeed,
                peers = item.numUploadConnections,
                userName = item.renderName.lowercase(Locale.US)
            )
        }
        val comparator = when (uploadSortMode) {
            UploadSortMode.ORDER_STARTED -> compareBy<UploadSortSnapshot> { it.orderStarted }
                .thenBy { it.name }
            UploadSortMode.TIME_LEFT -> compareBy<UploadSortSnapshot> { it.timeLeft }
                .thenBy { it.name }
            UploadSortMode.FILE_TYPE -> compareBy<UploadSortSnapshot> { it.fileType }
                .thenBy { it.name }
            UploadSortMode.EXTENSION -> compareBy<UploadSortSnapshot> { it.extension }
                .thenBy { it.name }
            UploadSortMode.USER_NAME -> compareBy<UploadSortSnapshot> { it.userName }
                .thenBy { it.name }
            UploadSortMode.STATUS -> compareBy<UploadSortSnapshot> { it.statusRank }
                .thenByDescending { it.rate }
                .thenBy { it.name }
            UploadSortMode.NAME -> compareBy<UploadSortSnapshot> { it.name }
            UploadSortMode.UPLOADED -> compareBy<UploadSortSnapshot> { it.uploaded }
                .thenBy { it.name }
            UploadSortMode.RATE -> compareBy<UploadSortSnapshot> { it.rate }
                .thenBy { it.name }
            UploadSortMode.PEERS -> compareBy<UploadSortSnapshot> { it.peers }
                .thenBy { it.name }
        }
        val sorted = snapshots.sortedWith(comparator)
        return if (uploadSortDescending) sorted.asReversed().map { it.item } else sorted.map { it.item }
    }

    private data class DownloadSortSnapshot(
        val item: DownloadItem,
        val statusRank: Int,
        val orderAdded: Long,
        val timeLeft: Long,
        val percentComplete: Int,
        val name: String,
        val fileType: String,
        val extension: String,
        val rate: Float,
        val sources: Int
    )

    private data class UploadSortSnapshot(
        val item: UploadItem,
        val statusRank: Int,
        val orderStarted: Long,
        val timeLeft: Long,
        val uploaded: Long,
        val name: String,
        val fileType: String,
        val extension: String,
        val rate: Float,
        val peers: Int,
        val userName: String
    )

    private fun isDownloadProblem(item: DownloadItem): Boolean {
        return item.errorState != DownloadItem.ErrorState.NONE ||
            item.state == DownloadState.ERROR ||
            item.state == DownloadState.STALLED ||
            item.state == DownloadState.CANCELLED ||
            item.state == DownloadState.DANGEROUS
    }

    private fun isUploadProblem(item: UploadItem): Boolean {
        return item.state.isError || item.state == UploadState.REQUEST_ERROR || item.state == UploadState.LIMIT_REACHED
    }

    private fun hasActiveTorrentDownloads(): Boolean {
        return allDownloads().any {
            it.downloadItemType == DownloadItem.DownloadItemType.BITTORRENT && !it.state.isFinished
        }
    }

    private fun cancelActiveTorrentDownloads() {
        allDownloads()
            .filter {
                it.downloadItemType == DownloadItem.DownloadItemType.BITTORRENT && !it.state.isFinished
            }
            .toList()
            .forEach(::removeDownloadItem)
    }

    private fun cancelAllTorrentUploads() {
        allUploads()
            .filter { it.uploadItemType == UploadItem.UploadItemType.BITTORRENT }
            .toList()
            .forEach(::removeUploadItem)
        cancelActiveTorrentDownloads()
    }

    private fun cancelAllUploads() {
        allUploads().toList().forEach(::removeUploadItem)
        cancelActiveTorrentDownloads()
    }

    private fun downloadStateRank(state: DownloadState): Int {
        return when (state) {
            DownloadState.DOWNLOADING -> 0
            DownloadState.RESUMING -> 1
            DownloadState.CONNECTING -> 2
            DownloadState.TRYING_AGAIN -> 3
            DownloadState.REMOTE_QUEUED -> 4
            DownloadState.LOCAL_QUEUED -> 5
            DownloadState.PAUSED -> 6
            DownloadState.STALLED -> 7
            DownloadState.ERROR -> 8
            DownloadState.FINISHING -> 9
            DownloadState.DONE -> 10
            DownloadState.DANGEROUS -> 11
            DownloadState.CANCELLED -> 12
        }
    }

    private fun uploadStateRank(state: UploadState): Int {
        return when (state) {
            UploadState.UPLOADING -> 0
            UploadState.QUEUED -> 1
            UploadState.BROWSE_HOST -> 2
            UploadState.PAUSED -> 3
            UploadState.REQUEST_ERROR -> 4
            UploadState.LIMIT_REACHED -> 5
            UploadState.DONE -> 6
            UploadState.BROWSE_HOST_DONE -> 7
            UploadState.CANCELED -> 8
        }
    }

    private fun openTorrentUri(uri: URI) {
        try {
            downloadListManager.addTorrentDownload(uri, false)
            noteTransferActivity(TransferTrayMode.DOWNLOADS)
        } catch (failure: DownloadException) {
            handleDownloadException(
                object : DownloadAction {
                    override fun download(saveFile: File, overwrite: Boolean) {
                        downloadListManager.addTorrentDownload(uri, overwrite)
                    }

                    override fun downloadCanceled(e: DownloadException) {
                    }
                },
                failure,
                false
            )
        }
    }

    private fun openTorrentFile(file: File) {
        if (!file.exists()) {
            showMessage(
                tr("Unable to open torrent"),
                tr("The file {0} does not exist.", file.name)
            )
            return
        }
        if (!file.isFile || !file.canRead()) {
            showMessage(
                tr("Unable to open torrent"),
                tr("The file {0} could not be opened.", file.name)
            )
            return
        }
        try {
            downloadListManager.addTorrentDownload(file, null, false)
            noteTransferActivity(TransferTrayMode.DOWNLOADS)
        } catch (failure: DownloadException) {
            handleDownloadException(
                object : DownloadAction {
                    override fun download(saveFile: File, overwrite: Boolean) {
                        downloadListManager.addTorrentDownload(file, saveFile, overwrite)
                    }

                    override fun downloadCanceled(e: DownloadException) {
                    }
                },
                failure,
                true
            )
        }
    }

    private fun openMagnetDownload(magnet: MagnetLink) {
        try {
            downloadListManager.addDownload(magnet, null, false)
            noteTransferActivity(TransferTrayMode.DOWNLOADS)
        } catch (failure: DownloadException) {
            handleDownloadException(
                object : DownloadAction {
                    override fun download(saveFile: File, overwrite: Boolean) {
                        downloadListManager.addDownload(magnet, saveFile, overwrite)
                    }

                    override fun downloadCanceled(e: DownloadException) {
                    }
                },
                failure,
                true
            )
        }
    }

    private fun openTorrentMagnet(magnet: MagnetLink) {
        try {
            downloadListManager.addTorrentDownload(magnet.name, magnet.urn, magnet.trackerUrls)
            noteTransferActivity(TransferTrayMode.DOWNLOADS)
        } catch (failure: DownloadException) {
            handleDownloadException(
                object : DownloadAction {
                    override fun download(saveFile: File, overwrite: Boolean) {
                        downloadListManager.addTorrentDownload(magnet.name, magnet.urn, magnet.trackerUrls)
                    }

                    override fun downloadCanceled(e: DownloadException) {
                    }
                },
                failure,
                false
            )
        }
    }

    fun handleDownloadException(action: DownloadAction, failure: DownloadException, supportsNewSaveDir: Boolean) {
        when (failure.errorCode) {
            DownloadException.ErrorCode.FILE_ALREADY_DOWNLOADING -> {
                noteTransferActivity(TransferTrayMode.DOWNLOADS)
                showNotice(tr("Download"), downloadErrorMessage(failure), OperationNoticeLevel.INFO)
            }

            DownloadException.ErrorCode.FILE_ALREADY_UPLOADING -> {
                noteTransferActivity(TransferTrayMode.UPLOADS)
                showNotice(tr("Download"), downloadErrorMessage(failure), OperationNoticeLevel.INFO)
            }

            DownloadException.ErrorCode.FILE_ALREADY_EXISTS,
            DownloadException.ErrorCode.FILE_IS_ALREADY_DOWNLOADED_TO -> {
                val target = failure.file
                val duplicateAction = settingsService.loadPreferences().transfers.duplicateDownloadAction
                when {
                    duplicateAction == DuplicateDownloadAction.IGNORE -> {
                        showNotice(tr("Download"), downloadErrorMessage(failure), OperationNoticeLevel.INFO)
                        action.downloadCanceled(failure)
                        return
                    }

                    duplicateAction == DuplicateDownloadAction.RENAME &&
                        supportsNewSaveDir &&
                        target != null -> {
                        retryDownload(action, nextAvailableDownloadTarget(target), false, supportsNewSaveDir)
                        return
                    }

                    duplicateAction == DuplicateDownloadAction.REPLACE &&
                        target != null &&
                        failure.errorCode == DownloadException.ErrorCode.FILE_ALREADY_EXISTS -> {
                        retryDownload(action, target, true, supportsNewSaveDir)
                        return
                    }
                }
                if (!supportsNewSaveDir && failure.errorCode == DownloadException.ErrorCode.FILE_IS_ALREADY_DOWNLOADED_TO) {
                    showNotice(tr("Download"), downloadErrorMessage(failure), OperationNoticeLevel.WARNING)
                    action.downloadCanceled(failure)
                    return
                }
                if (supportsNewSaveDir) {
                    val chosenTarget = filePicker.chooseSaveFile(
                        parent = windowRef,
                        title = tr("Save File As..."),
                        suggestedName = target?.name ?: tr("download"),
                        initialDirectory = target?.parentFile
                    )
                    if (chosenTarget != null) {
                        retryDownload(action, chosenTarget, false, supportsNewSaveDir)
                    } else {
                        action.downloadCanceled(failure)
                    }
                    return
                }
                confirmationDialog = ConfirmationDialogState(
                    title = tr("Download"),
                    message = tr("Replace the existing file {0}?", target?.name ?: ""),
                    confirmLabel = tr("Replace"),
                    alternateLabel = tr("Choose Folder"),
                    onConfirm = {
                        confirmationDialog = null
                        if (target != null) {
                            retryDownload(action, target, true, supportsNewSaveDir)
                        } else {
                            action.downloadCanceled(failure)
                        }
                    },
                    onAlternate = {
                        confirmationDialog = null
                        chooseAlternateDownloadFolder(action, failure, target, supportsNewSaveDir)
                    },
                    onDismiss = {
                        confirmationDialog = null
                        action.downloadCanceled(failure)
                    }
                )
            }

            else -> {
                if (supportsNewSaveDir) {
                    confirmationDialog = ConfirmationDialogState(
                        title = tr("Download"),
                        message = tr("{0}\n\nChoose a different location?", downloadErrorMessage(failure)),
                        confirmLabel = tr("Choose Location"),
                        onConfirm = {
                            confirmationDialog = null
                            val directory = filePicker.chooseFiles(
                                parent = windowRef,
                                title = tr("Choose Save Location"),
                                directoriesOnly = true
                            ).firstOrNull()
                            if (directory != null) {
                                retryDownload(action, directory, false, supportsNewSaveDir)
                            } else {
                                action.downloadCanceled(failure)
                            }
                        },
                        onDismiss = {
                            confirmationDialog = null
                            action.downloadCanceled(failure)
                        }
                    )
                } else {
                    val fileLabel = failure.file?.name?.takeIf { it.isNotBlank() }
                    val message = fileLabel?.let { tr("{0}: {1}", it, downloadErrorMessage(failure)) }
                        ?: downloadErrorMessage(failure)
                    showNotice(tr("Download"), message, OperationNoticeLevel.WARNING)
                    action.downloadCanceled(failure)
                }
            }
        }
    }

    private fun retryDownload(action: DownloadAction, saveFile: File, overwrite: Boolean, supportsNewSaveDir: Boolean) {
        try {
            action.download(saveFile, overwrite)
            noteTransferActivity(TransferTrayMode.DOWNLOADS)
        } catch (retryFailure: DownloadException) {
            handleDownloadException(action, retryFailure, supportsNewSaveDir)
        }
    }

    private fun chooseAlternateDownloadFolder(
        action: DownloadAction,
        failure: DownloadException,
        target: File?,
        supportsNewSaveDir: Boolean
    ) {
        val initialDirectory = target?.parentFile
        val selectedDirectory = filePicker.chooseFiles(
            parent = windowRef,
            title = tr("Choose a new folder to save download."),
            directoriesOnly = true,
            initialDirectory = initialDirectory
        ).firstOrNull()
        if (selectedDirectory == null) {
            action.downloadCanceled(failure)
            return
        }
        val validation = settingsService.validateSaveDirectory(selectedDirectory.absolutePath)
        if (!validation.accepted) {
            showNotice(
                tr("Save Folder Error"),
                validation.errorMessage ?: tr("Choose a different download folder to continue."),
                OperationNoticeLevel.ERROR
            )
            action.downloadCanceled(failure)
            return
        }
        val approvedDirectory = validation.normalizedPath?.let(::File) ?: selectedDirectory
        val resolvedTarget = target?.let { File(approvedDirectory, it.name) } ?: approvedDirectory
        retryDownload(action, resolvedTarget, false, supportsNewSaveDir)
    }

    private fun nextAvailableDownloadTarget(initialTarget: File): File {
        val baseName = FileUtils.getFilenameNoExtension(initialTarget.name)
        val extension = FileUtils.getFileExtension(initialTarget)
        var index = 1
        var candidate = initialTarget
        while (candidate.exists() || isDownloadSaveLocationTaken(candidate)) {
            val numberedName = buildString {
                append(baseName)
                append('(')
                append(index)
                append(')')
                if (extension.isNotBlank()) {
                    append('.')
                    append(extension)
                }
            }
            candidate = File(initialTarget.parentFile, numberedName)
            index += 1
        }
        return candidate
    }

    private fun isDownloadSaveLocationTaken(candidate: File): Boolean {
        val candidatePath = canonicalPath(candidate)
        return allDownloads().any { item ->
            item.saveFile?.let(::canonicalPath) == candidatePath
        }
    }

    private fun canonicalPath(file: File): String {
        return try {
            file.canonicalPath
        } catch (_: IOException) {
            file.absolutePath
        }
    }

    private fun downloadErrorMessage(failure: DownloadException): String {
        val targetName = failure.file?.name ?: tr("the selected location")
        return when (failure.errorCode) {
            DownloadException.ErrorCode.SECURITY_VIOLATION ->
                tr("WireShare could not save to {0} because the path is not allowed.", targetName)
            DownloadException.ErrorCode.FILE_ALREADY_SAVED ->
                tr("This download is already complete.")
            DownloadException.ErrorCode.DIRECTORY_NOT_WRITEABLE ->
                tr("WireShare cannot write to {0}.", targetName)
            DownloadException.ErrorCode.DIRECTORY_DOES_NOT_EXIST ->
                tr("The folder {0} does not exist.", targetName)
            DownloadException.ErrorCode.FILE_ALREADY_EXISTS ->
                tr("A file named {0} already exists.", targetName)
            DownloadException.ErrorCode.FILE_IS_ALREADY_DOWNLOADED_TO ->
                tr("Another transfer is already using {0}.", targetName)
            DownloadException.ErrorCode.FILE_NOT_REGULAR ->
                tr("{0} is not a regular file.", targetName)
            DownloadException.ErrorCode.NOT_A_DIRECTORY ->
                tr("{0} is not a folder.", targetName)
            DownloadException.ErrorCode.FILESYSTEM_ERROR ->
                tr("WireShare hit a filesystem error while saving the download.")
            DownloadException.ErrorCode.PATH_NAME_TOO_LONG ->
                tr("That save path is too long for this system.")
            DownloadException.ErrorCode.TORRENT_FILE_TOO_LARGE ->
                tr("That torrent file is too large to open.")
            DownloadException.ErrorCode.NO_TORRENT_MANAGER -> {
                refreshTorrentEngineHealthState()
                when (torrentEngineHealthState) {
                    TorrentEngineHealthState.STARTING ->
                        tr("WireShare is starting BitTorrent. Try again in a moment.")
                    TorrentEngineHealthState.ERROR ->
                        tr("Error connecting to BitTorrent. Torrents will not work until this is resolved.")
                    TorrentEngineHealthState.READY ->
                        tr("The torrent engine is not ready yet.")
                }
            }
            DownloadException.ErrorCode.FILES_STILL_RESUMING ->
                tr("WireShare is still restoring previous downloads. Try again in a moment.")
            DownloadException.ErrorCode.DOWNLOAD_CANCELLED ->
                tr("The download was cancelled.")
            DownloadException.ErrorCode.FILE_ALREADY_UPLOADING ->
                tr("That torrent is already seeding from your library.")
            DownloadException.ErrorCode.FILE_ALREADY_DOWNLOADING ->
                tr("That item is already in your transfers.")
        }
    }

    private fun friendConnectionErrorMessage(exception: Exception?): String {
        val message = exception?.message?.lowercase(Locale.US).orEmpty()
        return if ("auth" in message) {
            tr("The user name or password you have entered is incorrect.")
        } else {
            tr("Network error.")
        }
    }

    private fun refreshPlayerState() {
        playerState = playerService.status()
        playerCurrentFile = playerService.currentFile()
        playerVisible = playerService.isVisible()
        playerShuffle = playerService.isShuffle()
        playerTrackName = playerService.trackName()
        if (playerQueueMode == PlayerQueueMode.LIBRARY_LIVE) {
            syncLiveLibraryQueueIfNeeded()
        }
        playerQueueIndex = playerCurrentFile?.let { current ->
            playerQueue.indexOfFirst { it.file == current }
        } ?: -1
    }

    private fun playLiveLibraryQueue(currentItem: LocalFileItem) {
        val sourceLabel = activeLibrarySection()?.title ?: tr("My Files")
        val queue = buildLibraryQueueEntries(visibleLibraryItems(), sourceLabel)
        playQueueEntries(queue, currentItem.file, sourceLabel, PlayerQueueMode.LIBRARY_LIVE)
    }

    private fun playLibraryQueue(items: List<LocalFileItem>, currentItem: LocalFileItem, sourceLabel: String) {
        val queue = buildLibraryQueueEntries(items, sourceLabel)
        playQueueEntries(queue, currentItem.file, sourceLabel, PlayerQueueMode.SNAPSHOT)
    }

    private fun playDownloadQueue(items: List<DownloadItem>, currentItem: DownloadItem, sourceLabel: String) {
        val queue = items
            .mapNotNull { item ->
                val file = item.launchableFile
                if (categoryManager.getCategoryForFile(file) == Category.AUDIO && playerService.isPlayable(file)) {
                    PlayerQueueEntry(
                        file = file,
                        title = (item.title ?: item.fileName).ifBlank { file.nameWithoutExtension.ifBlank { file.name } },
                        sourceLabel = sourceLabel
                    )
                } else {
                    null
                }
            }
        playQueueEntries(queue, currentItem.launchableFile, sourceLabel, PlayerQueueMode.SNAPSHOT)
    }

    private fun playUploadQueue(items: List<UploadItem>, currentItem: UploadItem, sourceLabel: String) {
        val queue = items
            .mapNotNull { item ->
                val file = item.file
                if (categoryManager.getCategoryForFile(file) == Category.AUDIO && playerService.isPlayable(file)) {
                    PlayerQueueEntry(
                        file = file,
                        title = item.fileName.ifBlank { file.nameWithoutExtension.ifBlank { file.name } },
                        sourceLabel = sourceLabel
                    )
                } else {
                    null
                }
            }
        playQueueEntries(queue, currentItem.file, sourceLabel, PlayerQueueMode.SNAPSHOT)
    }

    private fun buildLibraryQueueEntries(items: List<LocalFileItem>, sourceLabel: String): List<PlayerQueueEntry> {
        return items
            .filter { item ->
                categoryManager.getCategoryForFile(item.file) == Category.AUDIO &&
                    playerService.isPlayable(item.file)
            }
            .map {
                PlayerQueueEntry(
                    file = it.file,
                    title = it.fileName.ifBlank { it.file.nameWithoutExtension.ifBlank { it.file.name } },
                    sourceLabel = sourceLabel
                )
            }
    }

    private fun addFilesToCollection(collection: SharedFileList, files: List<File>, sourceLabel: String) {
        val distinctFiles = files.distinctBy { it.absolutePath }
        if (distinctFiles.isEmpty()) {
            return
        }
        val failed = mutableListOf<File>()
        distinctFiles.forEach { file ->
            try {
                collection.addFile(file)
            } catch (_: Exception) {
                failed.add(file)
            }
        }
        val successCount = distinctFiles.size - failed.size
        if (successCount > 0) {
            showNotice(
                tr("Collection Updated"),
                tr(
                    "Added {0} {1} from {2} to \"{3}\".",
                    successCount,
                    if (successCount == 1) tr("item") else tr("items"),
                    sourceLabel,
                    collection.collectionName
                ),
                if (failed.isEmpty()) OperationNoticeLevel.SUCCESS else OperationNoticeLevel.WARNING
            )
        }
        if (failed.isNotEmpty()) {
            showNotice(
                tr("Collection Updated"),
                tr(
                    "{0} item(s) could not be added to \"{1}\".",
                    failed.size,
                    collection.collectionName
                ),
                OperationNoticeLevel.ERROR
            )
        }
    }

    private fun playQueueEntries(
        entries: List<PlayerQueueEntry>,
        currentFile: File,
        sourceLabel: String,
        mode: PlayerQueueMode
    ) {
        if (entries.isEmpty()) {
            return
        }
        playerQueueMode = mode
        playerQueue.clear()
        playerQueue.addAll(entries.distinctBy { it.file.absolutePath })
        playerQueueSourceLabel = sourceLabel
        playerVolume = settingsService.playerVolume()
        playerShuffle = playerService.isShuffle()
        val index = playerQueue.indexOfFirst { it.file == currentFile }.coerceAtLeast(0)
        playPlayerQueueIndex(index)
    }

    private fun syncLiveLibraryQueueIfNeeded() {
        if (playerQueueMode != PlayerQueueMode.LIBRARY_LIVE) {
            return
        }
        val sourceLabel = activeLibrarySection()?.title ?: tr("My Files")
        val visibleQueue = buildLibraryQueueEntries(visibleLibraryItems(), sourceLabel)
            .distinctBy { it.file.absolutePath }
        val currentFile = playerCurrentFile
        val currentEntry = currentFile?.let { file ->
            PlayerQueueEntry(
                file = file,
                title = findLibraryItem(file)?.fileName
                    ?.ifBlank { playerTrackName.ifBlank { file.nameWithoutExtension.ifBlank { file.name } } }
                    ?: playerTrackName.ifBlank { file.nameWithoutExtension.ifBlank { file.name } },
                sourceLabel = sourceLabel
            )
        }
        val rebuiltQueue = when {
            currentEntry != null && visibleQueue.none { it.file == currentEntry.file } ->
                listOf(currentEntry) + visibleQueue.filterNot { it.file == currentEntry.file }

            else -> visibleQueue
        }

        playerQueue.clear()
        playerQueue.addAll(rebuiltQueue.distinctBy { it.file.absolutePath })
        playerQueueSourceLabel = sourceLabel
        playerQueueIndex = currentFile?.let { current ->
            playerQueue.indexOfFirst { it.file == current }
        } ?: -1
    }

    private fun handlePlayerStateFeedback(state: PlayerState) {
        when (state) {
            PlayerState.PLAYING,
            PlayerState.SEEKING_PLAY,
            PlayerState.RESUMED -> warnedNoSoundDeviceAttemptId = -1L

            PlayerState.NO_SOUND_DEVICE -> {
                if (playerPlaybackAttemptId != warnedNoSoundDeviceAttemptId) {
                    warnedNoSoundDeviceAttemptId = playerPlaybackAttemptId
                    showNotice(
                        tr("Problem Playing File"),
                        tr("WireShare could not play this file. There may not be a sound device installed on your computer or your speakers may not be plugged in."),
                        OperationNoticeLevel.WARNING
                    )
                }
            }

            else -> Unit
        }
    }

    private fun markPlayerPlaybackAttempt() {
        playerPlaybackAttemptId += 1
    }

    private fun playPlayerQueueIndex(index: Int) {
        if (index !in playerQueue.indices) {
            return
        }
        playerQueueIndex = index
        markPlayerPlaybackAttempt()
        playerService.playFile(playerQueue[index].file)
        refreshPlayerState()
        selectPlayer()
    }

    private fun nextPlayerQueueIndex(): Int {
        if (playerQueue.isEmpty()) {
            return -1
        }
        val currentIndex = playerQueueIndex.takeIf { it in playerQueue.indices } ?: return 0
        if (playerShuffle && playerQueue.size > 1) {
            val candidates = playerQueue.indices.filter { it != currentIndex }
            return candidates.random()
        }
        return if (currentIndex < playerQueue.lastIndex) currentIndex + 1 else -1
    }

    private fun previousPlayerQueueIndex(): Int {
        if (playerQueue.isEmpty()) {
            return -1
        }
        val currentIndex = playerQueueIndex.takeIf { it in playerQueue.indices } ?: return 0
        return if (currentIndex > 0) currentIndex - 1 else -1
    }
}

class ComposeGuiCallback(
    private val controller: ComposeAppController
) : GuiCallback {
    override fun handleDownloadException(
        downLoadAction: DownloadAction,
        e: DownloadException,
        supportsNewSaveDir: Boolean
    ) {
        EventQueue.invokeLater {
            controller.handleDownloadException(downLoadAction, e, supportsNewSaveDir)
        }
    }

    override fun restoreApplication() {
        EventQueue.invokeLater { controller.restoreApplication() }
    }

    override fun handleMagnet(magnetLink: MagnetLink) {
        EventQueue.invokeLater { controller.handleMagnet(magnetLink) }
    }

    override fun translate(s: String): String = tr(s)

    override fun promptUserQuestion(marktr: String): Boolean {
        return controller.promptBroker.confirm(tr("Warning"), tr(marktr))
    }

    override fun promptTorrentFilePriorities(torrent: Torrent): Boolean {
        return controller.promptBroker.promptTorrentFilePriorities(
            torrent = torrent,
            title = tr("Torrent Download"),
            message = tr("Choose files to download"),
            alwaysAskBeforeStarting = controller.torrentPromptBeforeDownloadingEnabled(),
            persistAlwaysAskBeforeStarting = controller::setTorrentPromptBeforeDownloadingEnabled
        )
    }

    override fun promptAboutTorrentWithBannedExtensions(torrent: Torrent, bannedExtensions: Set<String>): Boolean {
        return controller.promptBroker.confirm(
            tr("Warning: {0}", torrent.name),
            buildString {
                append(
                    tr(
                        "This torrent contains files with the following extensions, which WireShare is configured not to download: {0}.",
                        bannedExtensions.map { it.lowercase(Locale.US) }.sorted().joinToString(", ")
                    )
                )
                append("\n\n")
                append(tr("Downloading this torrent could damage your computer. Are you sure you want to continue?"))
            }
        )
    }

    override fun promptAboutTorrentDownloadWithFailedScan(): Boolean {
        return controller.promptBroker.confirm(
            tr("Warning"),
            buildString {
                append(tr("The torrent file download could not be virus scanned."))
                append("\n\n")
                append(tr("Do you want to continue and download the torrent?"))
            }
        )
    }
}

private class ComposeSearchDetails(
    private val query: String,
    private val category: SearchCategory,
    private val searchType: SearchDetails.SearchType = SearchDetails.SearchType.KEYWORD,
    private val advancedDetails: Map<FilePropertyKey, String> = emptyMap()
) : SearchDetails {
    override fun getSearchCategory(): SearchCategory = category

    override fun getSearchQuery(): String = query

    override fun getSearchType(): SearchDetails.SearchType = searchType

    override fun getAdvancedDetails(): Map<FilePropertyKey, String> = advancedDetails
}
