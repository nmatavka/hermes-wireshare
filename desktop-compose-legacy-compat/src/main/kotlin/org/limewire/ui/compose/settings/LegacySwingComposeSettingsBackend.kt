package org.limewire.ui.compose.settings

import com.google.inject.Provider
import org.limewire.bittorrent.TorrentManager
import org.limewire.bittorrent.TorrentManagerSettings
import org.limewire.collection.AutoCompleteDictionary
import org.limewire.core.api.Category
import org.limewire.core.api.connection.GnutellaConnectionManager
import org.limewire.core.api.daap.DaapManager
import org.limewire.core.api.file.CategoryManager
import org.limewire.core.api.library.LibraryManager
import org.limewire.core.api.library.LocalFileItem
import org.limewire.core.api.library.SharedFileList
import org.limewire.core.api.library.SharedFileListManager
import org.limewire.core.api.network.NetworkManager
import org.limewire.core.api.spam.SpamManager
import org.limewire.core.settings.ApplicationSettings
import org.limewire.core.settings.BittorrentSettings
import org.limewire.core.settings.ConnectionSettings
import org.limewire.core.settings.DHTSettings
import org.limewire.core.settings.DaapSettings
import org.limewire.core.settings.DownloadSettings
import org.limewire.core.settings.FilterSettings
import org.limewire.core.settings.InstallSettings
import org.limewire.core.settings.LibrarySettings
import org.limewire.core.settings.NetworkSettings
import org.limewire.core.settings.SearchSettings
import org.limewire.core.settings.SharingSettings
import org.limewire.core.settings.UltrapeerSettings
import org.limewire.core.settings.UploadSettings
import org.limewire.core.settings.iTunesSettings
import org.limewire.io.IP
import org.limewire.io.IOUtils
import org.limewire.setting.BooleanSetting
import org.limewire.ui.compose.AddToLibraryDefaultsDraft
import org.limewire.ui.compose.CategorySaveDirectoriesDraft
import org.limewire.ui.compose.ComposeAppearance
import org.limewire.ui.compose.ConnectionColumn
import org.limewire.ui.compose.ConnectionLayoutPreferences
import org.limewire.ui.compose.DownloadColumn
import org.limewire.ui.compose.DownloadLayoutPreferences
import org.limewire.ui.compose.DownloadSortMode
import org.limewire.ui.compose.DuplicateDownloadAction
import org.limewire.ui.compose.FileAssociationPromptState
import org.limewire.ui.compose.FriendLoginDraft
import org.limewire.ui.compose.FriendLoginOption
import org.limewire.ui.compose.FriendsNotificationsPreferencesDraft
import org.limewire.ui.compose.FrostWireSearchProviderDraft
import org.limewire.ui.compose.FriendsPaneLayoutPreferences
import org.limewire.ui.compose.HostFilterPreferencesDraft
import org.limewire.ui.compose.ITunesPreferencesDraft
import org.limewire.ui.compose.LibraryColumn
import org.limewire.ui.compose.LibraryLayoutPreferences
import org.limewire.ui.compose.LibraryPaneLayoutPreferences
import org.limewire.ui.compose.LibraryPreferencesDraft
import org.limewire.ui.compose.LibrarySortMode
import org.limewire.ui.compose.NetworkAdvancedPreferencesDraft
import org.limewire.ui.compose.NetworkInterfaceOption
import org.limewire.ui.compose.PortForwardMode
import org.limewire.ui.compose.PreferencesDraft
import org.limewire.ui.compose.ProxyMode
import org.limewire.ui.compose.SaveDirectoryValidationResult
import org.limewire.ui.compose.SearchColumn
import org.limewire.ui.compose.SearchLayoutPreferences
import org.limewire.ui.compose.SearchPaneLayoutPreferences
import org.limewire.ui.compose.SearchPreferencesDraft
import org.limewire.ui.compose.SearchSortMode
import org.limewire.ui.compose.SecurityLevelOption
import org.limewire.ui.compose.SettingsApplyResult
import org.limewire.ui.compose.SystemPreferencesDraft
import org.limewire.ui.compose.TorrentEngineHealthState
import org.limewire.ui.compose.TransferFilterMode
import org.limewire.ui.compose.TransferPaneLayoutPreferences
import org.limewire.ui.compose.TransferPreferencesDraft
import org.limewire.ui.compose.TrayBehaviorPreferences
import org.limewire.ui.compose.UploadColumn
import org.limewire.ui.compose.UploadLayoutPreferences
import org.limewire.ui.compose.UploadSortMode
import org.limewire.ui.compose.WindowPlacementPreferences
import org.limewire.ui.compose.integration.ComposeFriendLoginStore
import org.limewire.ui.compose.integration.ComposeSettingsService
import org.limewire.ui.desktop.settings.QuestionsHandler
import org.limewire.ui.desktop.settings.StartupSettings
import org.limewire.ui.desktop.settings.SwingUiSettings
import org.limewire.ui.desktop.shell.LimeAssociationOption
import org.limewire.ui.desktop.shell.LimeAssociations
import org.limewire.ui.desktop.util.I18n
import org.limewire.ui.desktop.util.MacOSXUtils
import org.limewire.ui.desktop.util.WindowsUtils
import org.limewire.util.CommonUtils
import org.limewire.util.FileUtils
import org.limewire.util.OSUtils
import java.io.File
import java.io.FileFilter
import java.io.IOException
import java.io.RandomAccessFile
import java.net.NetworkInterface
import java.net.SocketException
import java.util.Locale
import java.util.Random

private fun tr(text: String, vararg args: Any?): String = I18n.tr(text, *args)

fun readComposeAppearanceSetting(): ComposeAppearance {
    return runCatching {
        ComposeAppearance.valueOf(SwingUiSettings.COMPOSE_APPEARANCE.get())
    }.getOrElse { ComposeAppearance.SOLARIZED_LIGHT }
}

interface ComposeRunOnStartupPlatform {
    fun isSupported(): Boolean
    fun apply(enabled: Boolean)
}

object LegacySwingComposeRunOnStartupPlatform : ComposeRunOnStartupPlatform {
    override fun isSupported(): Boolean = OSUtils.isMacOSX() || WindowsUtils.isLoginStatusAvailable()

    override fun apply(enabled: Boolean) {
        when {
            OSUtils.isMacOSX() -> MacOSXUtils.setLoginStatus(enabled)
            WindowsUtils.isLoginStatusAvailable() -> WindowsUtils.setLoginStatus(enabled)
        }
    }
}

interface ComposeFileAssociation {
    fun isEnabled(): Boolean
    fun setEnabled(enabled: Boolean)
    fun isAvailable(): Boolean
}

interface ComposeFileAssociationPlatform {
    fun isMagnetAssociationSupported(): Boolean
    fun isTorrentAssociationSupported(): Boolean
    fun magnetAssociation(): ComposeFileAssociation?
    fun torrentAssociation(): ComposeFileAssociation?
}

object LegacySwingComposeFileAssociationPlatform : ComposeFileAssociationPlatform {
    override fun isMagnetAssociationSupported(): Boolean = LimeAssociations.isMagnetAssociationSupported()

    override fun isTorrentAssociationSupported(): Boolean = LimeAssociations.isTorrentAssociationSupported()

    override fun magnetAssociation(): ComposeFileAssociation? {
        return LimeAssociations.getMagnetAssociation()?.let(::LegacySwingComposeFileAssociation)
    }

    override fun torrentAssociation(): ComposeFileAssociation? {
        return LimeAssociations.getTorrentAssociation()?.let(::LegacySwingComposeFileAssociation)
    }
}

private class LegacySwingComposeFileAssociation(
    private val delegate: LimeAssociationOption
) : ComposeFileAssociation {
    override fun isEnabled(): Boolean = delegate.isEnabled()

    override fun setEnabled(enabled: Boolean) {
        delegate.setEnabled(enabled)
    }

    override fun isAvailable(): Boolean = delegate.isAvailable()
}

class LegacySwingComposeSettingsBackend(
    private val friendLoginStore: ComposeFriendLoginStore? = null,
    private val searchHistory: AutoCompleteDictionary? = null,
    private val spamManager: SpamManager? = null,
    private val daapManager: DaapManager? = null,
    private val categoryManager: CategoryManager? = null,
    private val libraryManager: LibraryManager? = null,
    private val sharedFileListManager: SharedFileListManager? = null,
    private val networkManager: NetworkManager? = null,
    private val connectionManager: GnutellaConnectionManager? = null,
    private val torrentManager: Provider<TorrentManager>? = null,
    private val torrentManagerSettings: TorrentManagerSettings? = null,
    private val runOnStartupPlatform: ComposeRunOnStartupPlatform = LegacySwingComposeRunOnStartupPlatform,
    private val fileAssociationPlatform: ComposeFileAssociationPlatform = LegacySwingComposeFileAssociationPlatform
) : ComposeSettingsService {
    private data class FrostWireSearchProviderSetting(
        val label: String,
        val setting: BooleanSetting
    )

    private val frostWireSearchProviderSettings = listOf(
        FrostWireSearchProviderSetting("YT", SearchSettings.FROSTWIRE_SEARCH_YT_ENABLED),
        FrostWireSearchProviderSetting("Archive.org", SearchSettings.FROSTWIRE_SEARCH_INTERNET_ARCHIVE_ENABLED),
        FrostWireSearchProviderSetting("idope", SearchSettings.FROSTWIRE_SEARCH_IDOPE_ENABLED),
        FrostWireSearchProviderSetting("Knaben", SearchSettings.FROSTWIRE_SEARCH_KNABEN_ENABLED),
        FrostWireSearchProviderSetting("magnetdl", SearchSettings.FROSTWIRE_SEARCH_MAGNETDL_ENABLED),
        FrostWireSearchProviderSetting("Nyaa", SearchSettings.FROSTWIRE_SEARCH_NYAA_ENABLED),
        FrostWireSearchProviderSetting("1337x", SearchSettings.FROSTWIRE_SEARCH_ONE337X_ENABLED),
        FrostWireSearchProviderSetting("TPB", SearchSettings.FROSTWIRE_SEARCH_TPB_ENABLED),
        FrostWireSearchProviderSetting("torrentz2", SearchSettings.FROSTWIRE_SEARCH_TORRENTZ2_ENABLED),
        FrostWireSearchProviderSetting("TorrentsCSV", SearchSettings.FROSTWIRE_SEARCH_TORRENTSCSV_ENABLED),
        FrostWireSearchProviderSetting("SoundCloud", SearchSettings.FROSTWIRE_SEARCH_SOUNDCLOUD_ENABLED),
        FrostWireSearchProviderSetting("FrostClick", SearchSettings.FROSTWIRE_SEARCH_FROSTCLICK_ENABLED)
    )

    private fun frostWireSearchProviderDrafts(): List<FrostWireSearchProviderDraft> {
        return frostWireSearchProviderSettings.map { provider ->
            FrostWireSearchProviderDraft(
                key = provider.setting.key,
                label = provider.label,
                enabled = provider.setting.value
            )
        }
    }

    private fun applyFrostWireSearchProviderDrafts(drafts: List<FrostWireSearchProviderDraft>) {
        val enabledByKey = drafts.associate { it.key to it.enabled }
        frostWireSearchProviderSettings.forEach { provider ->
            enabledByKey[provider.setting.key]?.let(provider.setting::setValue)
        }
    }

    override fun loadPreferences(): PreferencesDraft {
        val autoLoginConfigured = friendLoginStore?.preferredLoginDraft()?.autoLogin == true
        return PreferencesDraft(
            search = SearchPreferencesDraft(
                defaultCategory = defaultSearchCategory(),
                showSmartSuggestions = SwingUiSettings.SHOW_SMART_SUGGESTIONS.getValue(),
                keepSearchHistory = SwingUiSettings.KEEP_SEARCH_HISTORY.getValue(),
                groupSimilarResults = SwingUiSettings.GROUP_SIMILAR_RESULTS_ENABLED.getValue(),
                useFrostWireSearch = SearchSettings.USE_FROSTWIRE_WEB_SEARCH.getValue(),
                frostWireSearchProviders = frostWireSearchProviderDrafts(),
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
                duplicateDownloadAction = readDuplicateDownloadAction(),
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
                autoLoginConfigured = autoLoginConfigured,
                autoLoginEnabled = autoLoginConfigured
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
        SearchSettings.USE_FROSTWIRE_WEB_SEARCH.setValue(draft.search.useFrostWireSearch)
        applyFrostWireSearchProviderDrafts(draft.search.frostWireSearchProviders)
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
            libraryManager?.libraryManagedList?.removeFiles(com.google.common.base.Predicate<LocalFileItem> { it.category == Category.PROGRAM })
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
        ensureAutomaticLibraryRoots()

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
        writeDuplicateDownloadAction(draft.transfers.duplicateDownloadAction)
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
        return fileAssociationPlatform.isMagnetAssociationSupported() ||
            fileAssociationPlatform.isTorrentAssociationSupported() ||
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

    private fun ensureAutomaticLibraryRoots() {
        val managedList = libraryManager?.libraryManagedList
        currentSaveRoots().forEach { root ->
            if (!root.exists()) {
                root.mkdirs()
            }
            root.listFiles()?.forEach { candidate ->
                if (candidate.isFile) {
                    managedList?.addFile(candidate)
                }
            }
        }

        val publicSharedList = findPublicSharedList()
        val sharedRoot = canonicalizeFile(SharingSettings.DEFAULT_SHARE_DIR)
        if (!sharedRoot.exists()) {
            sharedRoot.mkdirs()
        }
        if (publicSharedList != null && publicSharedList.isDirectoryAllowed(sharedRoot)) {
            publicSharedList.addFolder(sharedRoot, FileFilter { candidate ->
                candidate.isDirectory || publicSharedList.isFileAllowed(candidate)
            })
        }
    }

    private fun currentSaveRoots(): LinkedHashSet<File> {
        val roots = linkedSetOf<File>()
        roots += canonicalizeFile(SharingSettings.getSaveDirectory())
        roots += canonicalizeFile(SharingSettings.getSaveDirectory(Category.AUDIO))
        roots += canonicalizeFile(SharingSettings.getSaveDirectory(Category.VIDEO))
        roots += canonicalizeFile(SharingSettings.getSaveDirectory(Category.IMAGE))
        roots += canonicalizeFile(SharingSettings.getSaveDirectory(Category.DOCUMENT))
        roots += canonicalizeFile(SharingSettings.getSaveDirectory(Category.PROGRAM))
        roots += canonicalizeFile(SharingSettings.getSaveDirectory(Category.OTHER))
        return roots
    }

    private fun findPublicSharedList(): SharedFileList? {
        val manager = sharedFileListManager ?: return null
        val model = manager.model
        model.readWriteLock.readLock().lock()
        return try {
            model.firstOrNull { it.isPublic }
        } finally {
            model.readWriteLock.readLock().unlock()
        }
    }

    override fun startupFileAssociationPrompt(): FileAssociationPromptState? {
        val torrentAssociation = fileAssociationPlatform.torrentAssociation()
        applyAvailableAssociation(torrentAssociation, SwingUiSettings.HANDLE_TORRENTS)

        val magnetAssociation = fileAssociationPlatform.magnetAssociation()
        applyAvailableAssociation(magnetAssociation, SwingUiSettings.HANDLE_MAGNETS)

        val torrentsStolen = isSettingStolen(torrentAssociation, SwingUiSettings.HANDLE_TORRENTS)
        val magnetsStolen = isSettingStolen(magnetAssociation, SwingUiSettings.HANDLE_MAGNETS)

        if (SwingUiSettings.WARN_FILE_ASSOCIATION_CHANGES.getValue() && (torrentsStolen || magnetsStolen)) {
            return FileAssociationPromptState(
                message = startupAssociationMessage(torrentsStolen, magnetsStolen),
                warnOnChange = SwingUiSettings.WARN_FILE_ASSOCIATION_CHANGES.getValue()
            )
        }

        syncAssociationSettings(torrentAssociation, magnetAssociation)
        return null
    }

    override fun resolveStartupFileAssociationPrompt(reassociate: Boolean, warnOnChange: Boolean) {
        SwingUiSettings.WARN_FILE_ASSOCIATION_CHANGES.setValue(warnOnChange)
        val torrentAssociation = fileAssociationPlatform.torrentAssociation()
        val magnetAssociation = fileAssociationPlatform.magnetAssociation()
        if (reassociate) {
            fixAssociation(torrentAssociation, SwingUiSettings.HANDLE_TORRENTS)
            fixAssociation(magnetAssociation, SwingUiSettings.HANDLE_MAGNETS)
        } else {
            syncAssociationSettings(torrentAssociation, magnetAssociation)
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
        val visibleColumns = parseEnumSet(
            SwingUiSettings.COMPOSE_SEARCH_VISIBLE_COLUMNS.get(),
            SearchColumn.entries.toList()
        ).let { columns ->
            if (SwingUiSettings.COMPOSE_SEARCH_MARKER_COLUMN_MIGRATED.getValue()) {
                columns
            } else {
                val migratedColumns = columns + SearchColumn.MARKER
                SwingUiSettings.COMPOSE_SEARCH_VISIBLE_COLUMNS.set(
                    joinEnumNames(SearchColumn.entries.toList(), migratedColumns)
                )
                SwingUiSettings.COMPOSE_SEARCH_MARKER_COLUMN_MIGRATED.setValue(true)
                migratedColumns
            }
        }
        return SearchLayoutPreferences(
            visibleColumns = visibleColumns,
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

    private fun defaultSearchCategory(): org.limewire.core.api.search.SearchCategory {
        val configuredId = SwingUiSettings.DEFAULT_SEARCH_CATEGORY_ID.getValue()
        return org.limewire.core.api.search.SearchCategory.values()
            .firstOrNull { it.id == configuredId && it != org.limewire.core.api.search.SearchCategory.OTHER }
            ?: org.limewire.core.api.search.SearchCategory.ALL
    }

    private fun sanitizeDefaultSearchCategory(
        category: org.limewire.core.api.search.SearchCategory,
        programsAllowed: Boolean
    ): org.limewire.core.api.search.SearchCategory {
        return when {
            category == org.limewire.core.api.search.SearchCategory.OTHER -> org.limewire.core.api.search.SearchCategory.ALL
            category == org.limewire.core.api.search.SearchCategory.PROGRAM && !programsAllowed ->
                org.limewire.core.api.search.SearchCategory.ALL
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

    private fun supportsRunOnStartup(): Boolean = runOnStartupPlatform.isSupported()

    private fun applyRunOnStartup(enabled: Boolean) {
        runOnStartupPlatform.apply(enabled)
    }

    private fun applyAssociationSettings(handleMagnets: Boolean, handleTorrents: Boolean) {
        fileAssociationPlatform.magnetAssociation()?.setEnabled(handleMagnets)
        fileAssociationPlatform.torrentAssociation()?.setEnabled(handleTorrents)
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
            } catch (_: IOException) {
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
                ConnectionSettings.PROXY_PORT.setValue(
                    parseBoundedInt(draft.proxyPort, 0, 0xFFFF, ConnectionSettings.PROXY_PORT.getValue())
                )
                ConnectionSettings.PROXY_AUTHENTICATE.setValue(false)
                ConnectionSettings.PROXY_USERNAME.set("")
                ConnectionSettings.PROXY_PASS.set("")
            }

            ProxyMode.SOCKS4 -> {
                ConnectionSettings.PROXY_HOST.set(draft.proxyHost.trim())
                ConnectionSettings.PROXY_PORT.setValue(
                    parseBoundedInt(draft.proxyPort, 0, 0xFFFF, ConnectionSettings.PROXY_PORT.getValue())
                )
                ConnectionSettings.PROXY_AUTHENTICATE.setValue(draft.proxyAuthenticate)
                ConnectionSettings.PROXY_USERNAME.set(if (draft.proxyAuthenticate) draft.proxyUsername else "")
                ConnectionSettings.PROXY_PASS.set("")
            }

            ProxyMode.SOCKS5 -> {
                ConnectionSettings.PROXY_HOST.set(draft.proxyHost.trim())
                ConnectionSettings.PROXY_PORT.setValue(
                    parseBoundedInt(draft.proxyPort, 0, 0xFFFF, ConnectionSettings.PROXY_PORT.getValue())
                )
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

    private fun readDuplicateDownloadAction(): DuplicateDownloadAction {
        if (SwingUiSettings.DUPLICATE_DOWNLOAD_ACTION.isDefault()) {
            return if (SwingUiSettings.AUTO_RENAME_DUPLICATE_FILES.getValue()) {
                DuplicateDownloadAction.RENAME
            } else {
                DuplicateDownloadAction.IGNORE
            }
        }
        return when (SwingUiSettings.DUPLICATE_DOWNLOAD_ACTION.getValue()) {
            0 -> DuplicateDownloadAction.IGNORE
            2 -> DuplicateDownloadAction.REPLACE
            else -> DuplicateDownloadAction.RENAME
        }
    }

    private fun writeDuplicateDownloadAction(action: DuplicateDownloadAction) {
        val storedValue = when (action) {
            DuplicateDownloadAction.IGNORE -> 0
            DuplicateDownloadAction.RENAME -> 1
            DuplicateDownloadAction.REPLACE -> 2
        }
        SwingUiSettings.DUPLICATE_DOWNLOAD_ACTION.setValue(storedValue)
        SwingUiSettings.AUTO_RENAME_DUPLICATE_FILES.setValue(action == DuplicateDownloadAction.RENAME)
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
        torrentAssociation: ComposeFileAssociation?,
        magnetAssociation: ComposeFileAssociation?
    ) {
        updateAssociationSetting(torrentAssociation, SwingUiSettings.HANDLE_TORRENTS)
        updateAssociationSetting(magnetAssociation, SwingUiSettings.HANDLE_MAGNETS)
    }

    private fun updateAssociationSetting(
        association: ComposeFileAssociation?,
        handleType: BooleanSetting
    ) {
        handleType.setValue(association != null && association.isEnabled())
    }

    private fun isSettingStolen(
        association: ComposeFileAssociation?,
        handleType: BooleanSetting
    ): Boolean {
        return association != null && handleType.getValue() && !association.isEnabled()
    }

    private fun fixAssociation(
        association: ComposeFileAssociation?,
        handleType: BooleanSetting
    ) {
        if (association != null) {
            association.setEnabled(handleType.getValue())
        }
    }

    private fun applyAvailableAssociation(
        association: ComposeFileAssociation?,
        handleType: BooleanSetting
    ) {
        if (association != null && !association.isEnabled() && handleType.getValue() && association.isAvailable()) {
            association.setEnabled(true)
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
