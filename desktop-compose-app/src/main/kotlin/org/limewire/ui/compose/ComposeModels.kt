package org.limewire.ui.compose

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.limewire.bittorrent.Torrent
import org.limewire.core.api.download.DownloadPiecesInfo
import org.limewire.core.api.Category
import org.limewire.core.api.FilePropertyKey
import org.limewire.core.api.download.DownloadItem
import org.limewire.core.api.library.LocalFileItem
import org.limewire.core.api.search.Search
import org.limewire.core.api.search.SearchCategory
import org.limewire.core.api.search.SearchDetails
import org.limewire.core.api.search.GroupedSearchResult
import org.limewire.core.api.search.SearchResultList
import org.limewire.core.api.search.browse.BrowseStatus.BrowseState
import org.limewire.core.api.download.DownloadState
import org.limewire.core.api.upload.UploadItem
import org.limewire.friend.api.FriendRequest
import org.limewire.friend.api.ChatState
import org.limewire.friend.api.FriendPresence
import org.limewire.ui.compose.integration.FileIdentityPresentation
import java.io.File

data class SearchFriendFacet(
    val friendId: String,
    val label: String,
    val resultCount: Int
)

enum class SearchPropertyFacet {
    EXTENSION,
    FILE_TYPE,
    ARTIST,
    ALBUM,
    GENRE
}

enum class SearchRangeFacet {
    SIZE,
    LENGTH,
    BITRATE,
    QUALITY
}

enum class SearchActiveFilterType {
    TEXT,
    FRIENDS_ONLY,
    SOURCE,
    FRIEND,
    CATEGORY,
    PROPERTY,
    RANGE
}

data class SearchFacetOption(
    val id: String,
    val label: String,
    val resultCount: Int
)

data class SearchRangeSelection(
    val minimumId: String? = null,
    val maximumId: String? = null
)

data class SearchActiveFilterToken(
    val type: SearchActiveFilterType,
    val key: String,
    val secondaryKey: String? = null,
    val label: String
)

data class SearchPresentationState(
    val presentationCategory: SearchCategory,
    val visibleResults: List<GroupedSearchResult> = emptyList(),
    val activeFilters: List<SearchActiveFilterToken> = emptyList(),
    val categoryFacetOptions: List<SearchFacetOption> = emptyList(),
    val sourceFacetOptions: List<SearchFacetOption> = emptyList(),
    val friendFacetOptions: List<SearchFacetOption> = emptyList(),
    val propertyFacetOptions: Map<SearchPropertyFacet, List<SearchFacetOption>> = emptyMap(),
    val shownCount: Int = 0,
    val totalCount: Int = 0,
    val descriptor: String = "",
    val subtitle: String = ""
)

data class SearchResultPresentation(
    val identity: FileIdentityPresentation,
    val availabilityLabel: String? = null,
    val jumpTargets: List<LibraryJumpTarget> = emptyList(),
    val browseTargets: List<BrowseSourceTarget> = emptyList(),
    val blockTargets: List<RemoteUserTarget> = emptyList(),
    val browsableCount: Int = 0,
    val downloadItem: DownloadItem? = null
)

data class TorrentInspectorLiveState(
    val details: TorrentDetailsPresentation?,
    val activity: TorrentActivityPresentation?,
    val pieces: TorrentPiecesPresentation?
)

enum class SearchSuggestionSource {
    HISTORY,
    SMART,
    FRIEND
}

enum class SearchSuggestionAction {
    SEARCH,
    BROWSE_FRIEND_LIBRARY
}

data class SearchSuggestionEntry(
    val source: SearchSuggestionSource,
    val title: String,
    val queryText: String,
    val subtitle: String = "",
    val category: SearchCategory,
    val advancedDetails: Map<FilePropertyKey, String> = emptyMap(),
    val action: SearchSuggestionAction = SearchSuggestionAction.SEARCH,
    val friendId: String? = null
)

data class AdvancedSearchFieldSpec(
    val key: FilePropertyKey,
    val label: String
)

data class AdvancedSearchSuggestionEntry(
    val value: String,
    val subtitle: String = ""
)

data class AdvancedSearchDraft(
    val category: SearchCategory = SearchCategory.AUDIO,
    val values: Map<FilePropertyKey, String> = emptyMap()
)

data class BrowseFailureSource(
    val id: String?,
    val label: String,
    val anonymous: Boolean
)

data class BrowseStatusPresentation(
    val state: BrowseState,
    val failedSources: List<BrowseFailureSource> = emptyList()
)

data class BrowseFailureDialogState(
    val title: String,
    val friends: List<String>,
    val users: List<String>
)

data class PendingFriendRequest(
    val id: Long,
    val username: String,
    internal val request: FriendRequest
)

data class FileProcessingStatus(
    val total: Int,
    val finished: Int,
    val currentFileName: String? = null,
    val currentCategory: Category? = null,
    val done: Boolean = false
)

enum class OperationNoticeLevel {
    INFO,
    SUCCESS,
    WARNING,
    ERROR
}

data class OperationNotice(
    val id: Long,
    val title: String,
    val message: String,
    val level: OperationNoticeLevel = OperationNoticeLevel.INFO
)

enum class SetupWizardPage {
    ASSOCIATIONS,
    SHARING,
    SECURITY
}

data class DelayedExitState(
    val pending: Boolean = false,
    val downloadsCompleted: Boolean = false,
    val uploadsCompleted: Boolean = false,
    val disconnected: Boolean = false
)

class SearchTabSession(
    val id: Long,
    val title: String,
    val query: String,
    val category: SearchCategory,
    val searchType: SearchDetails.SearchType,
    val search: Search,
    val resultList: SearchResultList
) {
    val results = mutableStateListOf<org.limewire.core.api.search.GroupedSearchResult>()
    var sortMode by mutableStateOf(SearchSortMode.RELEVANCE)
    var sortDescending by mutableStateOf(false)
    var friendsOnly by mutableStateOf(false)
    var sourceFilter by mutableStateOf(SearchSourceFilter.ALL)
    var selectedFriendFacetId by mutableStateOf<String?>(null)
    var filterText by mutableStateOf("")
    var displayCategory by mutableStateOf<SearchCategory?>(null)
    var descriptor by mutableStateOf("")
    var subtitle by mutableStateOf("")
    var selectedResultKey by mutableStateOf<String?>(null)
    val selectedResultKeys = mutableStateListOf<String>()
    var selectionAnchorKey by mutableStateOf<String?>(null)
    var visibleColumns by mutableStateOf(SearchColumn.entries.toSet())
    val friendFacets = mutableStateListOf<SearchFriendFacet>()
    val selectedPropertyFacets = mutableStateMapOf<SearchPropertyFacet, String>()
    val selectedRangeFacets = mutableStateMapOf<SearchRangeFacet, SearchRangeSelection>()
    val expandedFilterCategories = mutableStateMapOf<SearchCategory, Boolean>()
    var browseStatus by mutableStateOf<BrowseStatusPresentation?>(null)
    var browseRefreshing by mutableStateOf(false)
    var searchRunning by mutableStateOf(false)
    var startedWhileNotFullyConnected by mutableStateOf(false)
    val expandedSimilarResultKeys = mutableStateListOf<String>()
    var presentationState by mutableStateOf(
        SearchPresentationState(presentationCategory = category)
    )
    internal var binding: AutoCloseable? = null
    internal var browseStatusCloseable: AutoCloseable? = null
    internal var searchListenerCloseable: AutoCloseable? = null
    internal var presentationRefreshScheduled = false
    internal var presentationDirtyMask = 0
}

data class LibrarySectionViewState(
    val filterText: String = "",
    val category: Category? = null,
    val sortMode: LibrarySortMode = LibrarySortMode.NAME,
    val sortDescending: Boolean = false,
    val selectedItemPath: String? = null,
    val selectedItemPaths: List<String> = emptyList(),
    val selectionAnchorPath: String? = null,
    val visibleColumns: Set<LibraryColumn> = LibraryColumn.entries.toSet()
)

enum class AdvancedToolsTab {
    CONNECTIONS,
    CONSOLE,
    MOJITO
}

data class LibraryJumpTarget(
    val sectionId: String,
    val label: String
)

data class RemoteUserTarget(
    val id: String,
    val label: String
)

data class BrowseSourceTarget(
    val id: String,
    val label: String,
    val enabled: Boolean,
    val anonymous: Boolean,
    val presence: FriendPresence?
)

enum class TorrentFilePriority(val value: Int, val label: String) {
    DO_NOT_DOWNLOAD(0, "Do Not Download"),
    LOW(1, "Low"),
    NORMAL(2, "Normal"),
    HIGH(3, "High");

    companion object {
        fun fromValue(value: Int): TorrentFilePriority {
            return entries.firstOrNull { it.value == value } ?: LOW
        }
    }
}

data class TorrentFileEntryPresentation(
    val index: Int,
    val path: String,
    val size: Long,
    val totalDone: Long,
    val progress: Float,
    val priority: TorrentFilePriority,
    val localPath: String? = null
)

data class TorrentTrackerPresentation(
    val index: Int,
    val uri: String,
    val tier: Int,
    val removable: Boolean
)

enum class TorrentSeedMode {
    DEFAULT,
    FOREVER,
    CUSTOM
}

data class TorrentManagementDraft(
    val seedMode: TorrentSeedMode,
    val seedRatio: String,
    val seedDays: String,
    val seedHours: String,
    val limitDownloadBandwidth: Boolean,
    val maxDownloadBandwidth: String,
    val limitUploadBandwidth: Boolean,
    val maxUploadBandwidth: String
)

data class TorrentPeerPresentation(
    val address: String,
    val encrypted: Boolean,
    val client: String,
    val uploadRateBytesPerSecond: Float,
    val downloadRateBytesPerSecond: Float
)

data class TorrentActivityPresentation(
    val seeders: Int?,
    val leechers: Int?,
    val currentTracker: String?,
    val peerCount: Int,
    val uploadRateBytesPerSecond: Float,
    val downloadRateBytesPerSecond: Float,
    val peers: List<TorrentPeerPresentation>
)

enum class TorrentPieceCellState {
    DOWNLOADED,
    PARTIAL,
    AVAILABLE,
    ACTIVE,
    UNAVAILABLE
}

data class TorrentPieceCellPresentation(
    val state: TorrentPieceCellState,
    val intensity: Float = 1f
)

data class TorrentPiecesPresentation(
    val totalPieces: Int,
    val completedPieces: Int?,
    val pieceSize: Long,
    val downloaded: Long,
    val verifiedDownloaded: Long,
    val failedDownload: Long,
    val uploaded: Long,
    val ratio: Float,
    val piecesPerCell: Int,
    val cells: List<TorrentPieceCellPresentation>
)

data class TorrentDetailsPresentation(
    val torrent: Torrent,
    val editable: Boolean,
    val privateTorrent: Boolean,
    val valid: Boolean,
    val fileCount: Int,
    val trackers: List<TorrentTrackerPresentation>,
    val entries: List<TorrentFileEntryPresentation>
)

data class LibraryMetadataDraft(
    val title: String = "",
    val author: String = "",
    val album: String = "",
    val genre: String = "",
    val year: String = "",
    val track: String = "",
    val rating: String = "",
    val description: String = "",
    val platform: String = "",
    val company: String = ""
)

data class LibraryMetadataEditorPresentation(
    val editable: Boolean,
    val draft: LibraryMetadataDraft,
    val genreChoices: List<String> = emptyList(),
    val ratingChoices: List<String> = emptyList(),
    val platformChoices: List<String> = emptyList()
)

data class LibraryMetadataSaveResult(
    val saved: Boolean = false,
    val dialogError: String? = null,
    val fieldErrors: Map<FilePropertyKey, String> = emptyMap()
)

data class LibrarySharingMembershipPresentation(
    val listId: Int,
    val label: String,
    val subtitle: String,
    val publicCollection: Boolean
)

data class LibraryFileInfoDialogState(
    val item: LocalFileItem,
    val version: Int = 0
)

data class SearchFileInfoDialogState(
    val result: GroupedSearchResult
)

data class DownloadFileInfoDialogState(
    val item: DownloadItem
)

data class UploadFileInfoDialogState(
    val item: UploadItem
)

enum class PreferencesSection(val label: String, val summary: String) {
    SEARCH("Search", "Defaults, history, grouping, and search filters"),
    LIBRARY("Library & Sharing", "Player, file handling, sharing, and add-to-library rules"),
    TRANSFERS("Transfers", "Save folders, speeds, duplicate handling, and tray behavior"),
    FRIENDS("Friends & Notifications", "Notifications and saved Friends sign-in"),
    SYSTEM("System Integration", "Startup, tray, restore, security, and appearance"),
    NETWORK("Network & Advanced", "Proxy, ports, network interface, and advanced controls")
}

data class SharingStatusCollectionSummary(
    val sectionId: String,
    val label: String,
    val fileCount: Int,
    val friendCount: Int,
    val publicCollection: Boolean
)

data class SharingStatusSummary(
    val sharedFileCount: Int,
    val publicCollectionCount: Int,
    val friendSharedCollectionCount: Int,
    val collections: List<SharingStatusCollectionSummary>,
    val showSignInToShareWithFriends: Boolean
)

data class PlayerQueueEntry(
    val file: File,
    val title: String,
    val sourceLabel: String
)

enum class ConversationMessageKind {
    TEXT,
    FILE_OFFER,
    STATUS
}

data class ConversationFileOffer(
    val offerId: String,
    val fileName: String,
    val size: Long,
    val description: String?,
    val urns: List<String>,
    val downloadState: DownloadState?,
    val localPath: String?
)

data class ConversationMessage(
    val id: String,
    val friendId: String,
    val senderName: String,
    val body: String,
    val timestamp: Long,
    val incoming: Boolean,
    val outgoing: Boolean,
    val server: Boolean,
    val kind: ConversationMessageKind = ConversationMessageKind.TEXT,
    val fileOffer: ConversationFileOffer? = null
) {
    val isIncoming: Boolean get() = incoming
    val isOutgoing: Boolean get() = outgoing
    val isServer: Boolean get() = server
    val isFileOffer: Boolean get() = kind == ConversationMessageKind.FILE_OFFER && fileOffer != null
}

data class FriendRosterItem(
    val id: String,
    val displayName: String,
    val status: String,
    val mode: FriendPresence.Mode?,
    val signedIn: Boolean,
    val unreadMessages: Boolean,
    val browseable: Boolean = false,
    val supportsOffTheRecord: Boolean = false,
    val supportsFileOffers: Boolean = false
) {
    fun hasUnviewedMessages(): Boolean = unreadMessages
}

class ChatConversationState(
    friend: FriendRosterItem
) {
    var friend by mutableStateOf(friend)
    val messages = mutableStateListOf<ConversationMessage>()
    var messageVersion by mutableStateOf(0)
    var draft by mutableStateOf("")
    var localState by mutableStateOf(ChatState.active)
    var remoteState by mutableStateOf<ChatState?>(null)
    var offTheRecordEnabled by mutableStateOf<Boolean?>(null)
}

enum class FriendLoginDialogMode {
    SIGN_IN,
    SAVE_SETTINGS
}
