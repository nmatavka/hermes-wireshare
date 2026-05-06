package org.limewire.ui.compose

import org.limewire.core.api.FilePropertyKey
import org.limewire.core.api.search.SearchCategory

data class HostFilterPreferencesDraft(
    val enabled: Boolean,
    val blockedHosts: List<String>,
    val allowedHosts: List<String>
)

enum class TorrentEngineHealthState {
    READY,
    STARTING,
    ERROR
}

data class SearchLayoutPreferences(
    val visibleColumns: Set<SearchColumn> = SearchColumn.entries.toSet(),
    val sortMode: SearchSortMode = SearchSortMode.RELEVANCE,
    val sortDescending: Boolean = false
)

data class SearchPaneLayoutPreferences(
    val refinementRailVisible: Boolean = true,
    val refinementRailFraction: Float = 0.26f,
    val resultsFraction: Float = 0.62f
)

data class LibraryLayoutPreferences(
    val visibleColumns: Set<LibraryColumn> = LibraryColumn.entries.toSet(),
    val sortMode: LibrarySortMode = LibrarySortMode.NAME,
    val sortDescending: Boolean = false
)

data class LibraryPaneLayoutPreferences(
    val navigatorFraction: Float = 0.24f,
    val filtersVisible: Boolean = true
)

data class DownloadLayoutPreferences(
    val visibleColumns: Set<DownloadColumn> = DownloadColumn.entries.toSet(),
    val sortMode: DownloadSortMode = DownloadSortMode.STATUS,
    val sortDescending: Boolean = false,
    val filterMode: TransferFilterMode = TransferFilterMode.ALL
)

data class UploadLayoutPreferences(
    val visibleColumns: Set<UploadColumn> = UploadColumn.entries.toSet(),
    val sortMode: UploadSortMode = UploadSortMode.STATUS,
    val sortDescending: Boolean = false,
    val filterMode: TransferFilterMode = TransferFilterMode.ALL
)

data class FriendsPaneLayoutPreferences(
    val rosterFraction: Float = 0.28f
)

data class TransferPaneLayoutPreferences(
    val mainAreaFraction: Float = 0.72f
)

enum class ConnectionColumn {
    HOST,
    STATUS,
    MESSAGES_IO,
    MESSAGES_IN,
    MESSAGES_OUT,
    BANDWIDTH_IO,
    BANDWIDTH_IN,
    BANDWIDTH_OUT,
    DROPPED_IO,
    DROPPED_IN,
    DROPPED_OUT,
    PROTOCOL,
    VENDOR_VERSION,
    TIME,
    COMPRESSED_IO,
    COMPRESSED_IN,
    COMPRESSED_OUT,
    SSL_OVERHEAD_IO,
    SSL_OVERHEAD_IN,
    SSL_OVERHEAD_OUT,
    QRP_PERCENT,
    QRP_EMPTY
}

data class ConnectionLayoutPreferences(
    val visibleColumns: Set<ConnectionColumn> = setOf(
        ConnectionColumn.HOST,
        ConnectionColumn.STATUS,
        ConnectionColumn.MESSAGES_IO,
        ConnectionColumn.BANDWIDTH_IO,
        ConnectionColumn.DROPPED_IO,
        ConnectionColumn.PROTOCOL,
        ConnectionColumn.VENDOR_VERSION,
        ConnectionColumn.TIME
    ),
    val sortColumn: ConnectionColumn = ConnectionColumn.HOST,
    val sortDescending: Boolean = false
)

data class WindowPlacementPreferences(
    val restoreWindowPlacement: Boolean = true,
    val positionsSet: Boolean = false,
    val width: Int = 1024,
    val height: Int = 768,
    val x: Int = 0,
    val y: Int = 0,
    val maximized: Boolean = false
)

data class TrayBehaviorPreferences(
    val minimizeToTray: Boolean = false,
    val showTransfersTray: Boolean = true
)

data class FileAssociationPromptState(
    val message: String,
    val warnOnChange: Boolean
)

enum class ProxyMode {
    NONE,
    SOCKS4,
    SOCKS5,
    HTTP
}

enum class PortForwardMode {
    UPNP,
    MANUAL,
    NONE
}

enum class SecurityLevelOption(val level: Int) {
    NONE(0),
    LIGHT(1),
    LIGHT_JAPAN_BLOCK(2),
    STRONG(3),
    STRONG_JAPAN_BLOCK(4);

    companion object {
        fun fromLevel(level: Int): SecurityLevelOption {
            return entries.firstOrNull { it.level == level } ?: STRONG
        }
    }
}

data class NetworkInterfaceOption(
    val address: String,
    val displayName: String
)

data class FriendLoginOption(
    val label: String
)

data class FriendLoginDraft(
    val configLabel: String,
    val serviceName: String,
    val username: String,
    val password: String,
    val autoLogin: Boolean
)

data class AddToLibraryDefaultsDraft(
    val audio: Boolean,
    val video: Boolean,
    val images: Boolean,
    val documents: Boolean,
    val programs: Boolean
)

data class SearchPreferencesDraft(
    val defaultCategory: SearchCategory,
    val showSmartSuggestions: Boolean,
    val keepSearchHistory: Boolean,
    val groupSimilarResults: Boolean,
    val useTorrentWebSearch: Boolean,
    val filterAdultContent: Boolean,
    val hostFilters: HostFilterPreferencesDraft,
    val blockedKeywords: List<String>,
    val blockedExtensions: List<String>
)

data class CategorySaveDirectoriesDraft(
    val audio: String,
    val video: String,
    val images: String,
    val documents: String,
    val programs: String,
    val other: String
)

data class SaveDirectoryValidationResult(
    val accepted: Boolean,
    val normalizedPath: String? = null,
    val errorMessage: String? = null
)

data class ITunesPreferencesDraft(
    val supported: Boolean,
    val addDownloadedAudioToLibrary: Boolean,
    val shareAudioAcrossLan: Boolean,
    val requirePassword: Boolean,
    val password: String
)

enum class DuplicateDownloadAction {
    IGNORE,
    RENAME,
    REPLACE
}

data class LibraryPreferencesDraft(
    val playerEnabled: Boolean,
    val handleMagnets: Boolean,
    val handleTorrents: Boolean,
    val warnFileAssociationChanges: Boolean,
    val shareDownloadedFiles: Boolean,
    val allowPartialSharing: Boolean,
    val allowProgramSearchAndShare: Boolean,
    val allowDocumentSharing: Boolean,
    val addToLibraryDefaults: AddToLibraryDefaultsDraft,
    val iTunes: ITunesPreferencesDraft
)

data class TransferPreferencesDraft(
    val uploadTorrentsForever: Boolean,
    val torrentSeedRatio: String,
    val torrentSeedDays: String,
    val torrentSeedHours: String,
    val showTorrentSelectorBeforeDownloading: Boolean,
    val duplicateDownloadAction: DuplicateDownloadAction,
    val showTransfersTrayByDefault: Boolean,
    val closeTrayWhenNoTransfers: Boolean,
    val showTotalBandwidth: Boolean,
    val clearDownloadsWhenFinished: Boolean,
    val clearUploadsWhenFinished: Boolean,
    val useCategorySpecificFolders: Boolean,
    val downloadDirectory: String,
    val categorySaveDirectories: CategorySaveDirectoriesDraft,
    val maxDownloadsAtOnce: String,
    val maxUploadsAtOnce: String,
    val limitDownloadBandwidth: Boolean,
    val limitUploadBandwidth: Boolean,
    val maxDownloadKiB: String,
    val maxUploadKiB: String
)

data class FriendsNotificationsPreferencesDraft(
    val showNotifications: Boolean,
    val playNotificationSound: Boolean,
    val autoLoginConfigured: Boolean,
    val autoLoginEnabled: Boolean
)

enum class ComposeAppearance(val label: String) {
    SOLARIZED_LIGHT("Solarized Light"),
    SOLARIZED_DARK("Solarized Dark"),
    SELENIZED_DARK("Selenized Dark"),
    SELENIZED_BLACK("Selenized Black"),
    SELENIZED_LIGHT("Selenized Light"),
    SELENIZED_WHITE("Selenized White")
}

data class SystemPreferencesDraft(
    val connectOnStartup: Boolean,
    val runOnStartup: Boolean,
    val runOnStartupSupported: Boolean,
    val minimizeToTray: Boolean,
    val restoreWindowPlacement: Boolean,
    val localRestAccessEnabled: Boolean,
    val securityLevel: SecurityLevelOption,
    val appearance: ComposeAppearance
)

data class NetworkAdvancedPreferencesDraft(
    val proxyMode: ProxyMode,
    val proxyHost: String,
    val proxyPort: String,
    val proxyAuthenticate: Boolean,
    val proxyUsername: String,
    val proxyPassword: String,
    val torrentUseUpnp: Boolean,
    val torrentListenStartPort: String,
    val torrentListenEndPort: String,
    val disableUltrapeer: Boolean,
    val disableMojito: Boolean,
    val disableTls: Boolean,
    val disableOutOfBandSearch: Boolean,
    val gnutellaPort: String,
    val portForwardMode: PortForwardMode,
    val manualPort: String,
    val useCustomNetworkInterface: Boolean,
    val selectedNetworkInterfaceAddress: String?,
    val availableNetworkInterfaces: List<NetworkInterfaceOption>
)

data class PreferencesDraft(
    val search: SearchPreferencesDraft,
    val library: LibraryPreferencesDraft,
    val transfers: TransferPreferencesDraft,
    val friends: FriendsNotificationsPreferencesDraft,
    val system: SystemPreferencesDraft,
    val network: NetworkAdvancedPreferencesDraft
)

data class SettingsApplyResult(
    val restartRequired: Boolean = false,
    val restartMessage: String? = null
)
