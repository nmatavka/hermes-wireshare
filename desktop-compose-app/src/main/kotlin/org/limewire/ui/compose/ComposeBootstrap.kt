package org.limewire.ui.compose

import androidx.compose.ui.window.application
import com.google.inject.Guice
import com.google.inject.Injector
import com.google.inject.Key
import com.google.inject.Stage
import com.google.inject.TypeLiteral
import com.limegroup.gnutella.ActiveLimeWireCheck
import com.limegroup.gnutella.LifecycleManager
import com.limegroup.gnutella.LimeCoreGlue
import com.limegroup.gnutella.LimeCoreGlue.InstallFailedException
import com.limegroup.gnutella.LimeWireCoreModule
import com.limegroup.gnutella.util.LimeWireUtils
import org.limewire.bittorrent.TorrentManager
import org.limewire.bittorrent.TorrentManagerSettings
import org.limewire.bittorrent.TorrentSettingsAnnotation
import org.limewire.collection.StringTrieSet
import org.limewire.core.api.Application
import org.limewire.core.api.callback.GuiCallbackService
import org.limewire.core.api.connection.GnutellaConnectionManager
import org.limewire.core.api.daap.DaapManager
import org.limewire.core.api.download.DownloadListManager
import org.limewire.core.api.file.CategoryManager
import org.limewire.core.api.library.FriendAutoCompleterFactory
import org.limewire.core.api.library.LibraryData
import org.limewire.core.api.library.LibraryManager
import org.limewire.core.api.library.MagnetLinkFactory
import org.limewire.core.api.library.MetaDataManager
import org.limewire.core.api.library.SharedFileListManager
import org.limewire.core.api.mojito.MojitoManager
import org.limewire.core.api.magnet.MagnetFactory
import org.limewire.core.api.network.NetworkManager
import org.limewire.core.api.connection.FirewallStatusEvent
import org.limewire.core.api.connection.FirewallTransferStatusEvent
import org.limewire.core.api.properties.PropertyDictionary
import org.limewire.core.api.search.SearchFactory
import org.limewire.core.api.search.SearchManager
import org.limewire.core.api.search.browse.BrowseSearchFactory
import org.limewire.core.api.spam.SpamManager
import org.limewire.core.api.support.LocalClientInfoFactory
import org.limewire.core.api.monitor.IncomingSearchManager
import org.limewire.core.api.upload.UploadListManager
import org.limewire.core.api.xmpp.XMPPResourceFactory
import org.limewire.friend.api.FriendConnectionFactory
import org.limewire.inject.GuiceUtils
import org.limewire.net.FirewallService
import org.limewire.nio.NIODispatcher
import org.limewire.player.api.AudioPlayer
import org.limewire.ui.compose.integration.AwtDesktopFilePicker
import org.limewire.ui.compose.integration.AwtDesktopShellService
import org.limewire.ui.compose.integration.ComposeSystemMessageService
import org.limewire.ui.compose.integration.CoreComposeAdvancedToolsService
import org.limewire.ui.compose.integration.CoreComposeDelayedExitService
import org.limewire.ui.compose.integration.CoreComposeBrowseService
import org.limewire.ui.compose.integration.CoreComposeAdvancedSearchSuggestionsService
import org.limewire.ui.compose.integration.CoreComposePlayerService
import org.limewire.ui.compose.integration.CoreComposeRecentDownloadsService
import org.limewire.ui.compose.integration.CoreComposeSearchSuggestionsService
import org.limewire.ui.compose.integration.CoreComposeTransferRepairService
import org.limewire.ui.compose.integration.SwingComposeLocalizationService
import org.limewire.ui.compose.integration.SwingDesktopLauncher
import org.limewire.ui.compose.integration.CoreComposeRuntimeErrorService
import org.limewire.ui.compose.integration.ComposeRuntimeErrorService
import org.limewire.ui.compose.integration.CoreComposeSystemMessageService
import org.limewire.ui.compose.integration.SwingComposeFriendService
import org.limewire.ui.compose.integration.createLegacyChatCompatBridge
import org.limewire.ui.compose.integration.createLegacyFriendLoginStore
import org.limewire.ui.compose.integration.findOptionalMojitoVisualizerPlugin
import org.limewire.ui.compose.integration.legacyComposeSwingCompatModule
import org.limewire.ui.compose.integration.CoreComposeFileAppearanceService
import org.limewire.ui.compose.integration.composeConsoleCaptureService
import org.limewire.ui.compose.settings.LegacySwingComposeSettingsAdapter
import org.limewire.ui.compose.settings.ComposeStartupSettings
import org.limewire.ui.compose.settings.LegacySwingComposeStartupSettings
import org.limewire.util.I18NConvert
import org.limewire.util.OSUtils
import org.limewire.util.SystemUtils
import org.limewire.core.impl.CoreGlueModule
import kotlin.system.exitProcess

object ComposeBootstrap {
    fun launch(args: Array<String>) {
        val runtimeErrorService = CoreComposeRuntimeErrorService()
        val systemMessageService = CoreComposeSystemMessageService()
        runtimeErrorService.install()
        systemMessageService.install()
        try {
            val controller = createController(args, runtimeErrorService, systemMessageService)
            application {
                WireShareDesktopApp(controller, ::exitApplication)
            }
        } catch (failure: Throwable) {
            runtimeErrorService.finishStartupCapture(replay = false)
            val startupProblem = when (failure) {
                is ComposeStartupFailureException -> failure.cause ?: failure
                else -> failure
            }
            val report = runtimeErrorService.fatalStartupReport(
                problem = startupProblem,
                detail = fatalStartupDetail(failure)
            )
            runCatching {
                application {
                    FatalStartupErrorApp(
                        report = report,
                        runtimeErrorService = runtimeErrorService,
                        filePicker = AwtDesktopFilePicker(),
                        onQuit = ::exitApplication
                    )
                }
                exitProcess(1)
            }.getOrElse {
                System.err.println(report.title)
                report.detail?.takeIf(String::isNotBlank)?.let(System.err::println)
                System.err.println(report.bugReport)
                exitProcess(1)
            }
        }
    }

    private fun createController(
        args: Array<String>,
        runtimeErrorService: ComposeRuntimeErrorService,
        systemMessageService: ComposeSystemMessageService
    ): ComposeAppController {
        val startupSettings = LegacySwingComposeStartupSettings()
        configureRuntimeFlags()
        validateStartupArguments(args, startupSettings)
        startupSettings.applyPreferredLocale()
        I18NConvert.instance()
        if (OSUtils.isMacOSX()) {
            SystemUtils.setOpenFileLimit(1024)
        }

        try {
            LimeCoreGlue.preinstall()
        } catch (failure: InstallFailedException) {
            throw ComposeStartupFailureException(
                detail = "Fatal error: insufficient permissions to create the settings directory. Check the WireShare preferences folder permissions and try again.",
                cause = failure
            )
        }
        val injector = Guice.createInjector(
            Stage.DEVELOPMENT,
            LimeWireCoreModule(),
            CoreGlueModule(),
            legacyComposeSwingCompatModule()
        )
        GuiceUtils.loadEagerSingletons(injector)
        injector.getInstance(LimeCoreGlue::class.java).install()
        validateEarlyCore(injector)

        val localizationService = SwingComposeLocalizationService()
        ComposeLocalization.service = localizationService
        runtimeErrorService.setLocalClientInfoFactory(
            injector.getInstance(LocalClientInfoFactory::class.java)
        )
        runtimeErrorService.beginStartupCapture()
        val searchHistory = StringTrieSet(true)
        val friendLoginStore = createLegacyFriendLoginStore(injector)
        val composeSettingsService = LegacySwingComposeSettingsAdapter(
            friendLoginStore = friendLoginStore,
            searchHistory = searchHistory,
            spamManager = injector.getInstance(SpamManager::class.java),
            daapManager = injector.getInstance(DaapManager::class.java),
            categoryManager = injector.getInstance(CategoryManager::class.java),
            libraryManager = injector.getInstance(LibraryManager::class.java),
            sharedFileListManager = injector.getInstance(SharedFileListManager::class.java),
            networkManager = injector.getInstance(NetworkManager::class.java),
            connectionManager = injector.getInstance(GnutellaConnectionManager::class.java),
            torrentManager = injector.getProvider(TorrentManager::class.java),
            torrentManagerSettings = injector.getInstance(
                Key.get(TorrentManagerSettings::class.java, TorrentSettingsAnnotation::class.java)
            )
        )
        val launcher = SwingDesktopLauncher(injector.getInstance(CategoryManager::class.java))
        val desktopShellService = AwtDesktopShellService()
        val friendService = SwingComposeFriendService(
            friendLoginStore = friendLoginStore,
            chatBridge = createLegacyChatCompatBridge(injector),
            friendRequestListeners = injector.getInstance(
                Key.get(object : TypeLiteral<org.limewire.listener.ListenerSupport<org.limewire.friend.api.FriendRequestEvent>>() {})
            )
        )
        val advancedToolsService = CoreComposeAdvancedToolsService(
            connectionManager = injector.getInstance(GnutellaConnectionManager::class.java),
            incomingSearchManager = injector.getInstance(IncomingSearchManager::class.java),
            mojitoManager = injector.getInstance(MojitoManager::class.java),
            firewallStatusBean = injector.getInstance(
                Key.get(object : TypeLiteral<org.limewire.listener.EventBean<FirewallStatusEvent>>() {})
            ),
            firewallTransferBean = injector.getInstance(
                Key.get(object : TypeLiteral<org.limewire.listener.EventBean<FirewallTransferStatusEvent>>() {})
            ),
            localClientInfoFactory = injector.getInstance(LocalClientInfoFactory::class.java),
            consoleCaptureService = composeConsoleCaptureService(),
            mojitoVisualizerPlugin = findOptionalMojitoVisualizerPlugin(injector)
        )
        val fileAppearanceService = CoreComposeFileAppearanceService(
            injector.getInstance(CategoryManager::class.java)
        )

        val controller = ComposeAppController(
            application = injector.getInstance(Application::class.java),
            lifecycleManager = injector.getInstance(LifecycleManager::class.java),
            searchFactory = injector.getInstance(SearchFactory::class.java),
            searchManager = injector.getInstance(SearchManager::class.java),
            downloadListManager = injector.getInstance(DownloadListManager::class.java),
            uploadListManager = injector.getInstance(UploadListManager::class.java),
            libraryData = injector.getInstance(LibraryData::class.java),
            libraryManager = injector.getInstance(LibraryManager::class.java),
            sharedFileListManager = injector.getInstance(SharedFileListManager::class.java),
            metaDataManager = injector.getInstance(MetaDataManager::class.java),
            propertyDictionary = injector.getInstance(PropertyDictionary::class.java),
            categoryManager = injector.getInstance(CategoryManager::class.java),
            fileAppearanceService = fileAppearanceService,
            spamManager = injector.getInstance(SpamManager::class.java),
            magnetFactory = injector.getInstance(MagnetFactory::class.java),
            magnetLinkFactory = injector.getInstance(MagnetLinkFactory::class.java),
            connectionManager = injector.getInstance(GnutellaConnectionManager::class.java),
            filePicker = AwtDesktopFilePicker(),
            launcher = launcher,
            notifications = desktopShellService,
            playerService = CoreComposePlayerService(
                injector.getInstance(AudioPlayer::class.java),
                injector.getInstance(CategoryManager::class.java),
                launcher,
                composeSettingsService.playerVolume()
            ),
            friendService = friendService,
            browseService = CoreComposeBrowseService(
                injector.getInstance(BrowseSearchFactory::class.java),
                injector.getInstance(SearchManager::class.java),
                friendService
            ),
            recentDownloadsService = CoreComposeRecentDownloadsService(),
            transferRepairService = CoreComposeTransferRepairService(
                injector.getInstance(DownloadListManager::class.java)
            ),
            advancedToolsService = advancedToolsService,
            searchSuggestionsService = CoreComposeSearchSuggestionsService(searchHistory),
            advancedSearchSuggestionsService = CoreComposeAdvancedSearchSuggestionsService(
                injector.getInstance(FriendAutoCompleterFactory::class.java)
            ),
            runtimeErrorService = runtimeErrorService,
            systemMessageService = systemMessageService,
            trayService = desktopShellService,
            delayedExitService = CoreComposeDelayedExitService(
                injector.getInstance(GnutellaConnectionManager::class.java),
                injector.getInstance(DownloadListManager::class.java),
                injector.getInstance(UploadListManager::class.java)
            ),
            settingsService = composeSettingsService,
            localizationService = localizationService
        )

        injector.getInstance(GuiCallbackService::class.java).setGuiCallback(ComposeGuiCallback(controller))
        DesktopIntegration.register()
        controller.prepareStartupMessageRouting()
        startCoreServices(injector)
        startupSettings.ensureRunOnStartupConfigured()
        controller.activate()
        DesktopIntegration.attach(controller)
        DesktopIntegration.enqueueLaunchArgs(args.filter { it != "-startup" })
        return controller
    }

    private fun configureRuntimeFlags() {
        System.setProperty("http.agent", LimeWireUtils.getHttpServer())
        System.setProperty("apple.laf.useScreenMenuBar", "true")
        System.setProperty("apple.awt.application.name", "WireShare")
        System.setProperty(
            "user.fullname",
            System.getProperty("user.fullname", System.getProperty("user.name", ""))
        )
    }

    private fun validateStartupArguments(args: Array<String>, startupSettings: ComposeStartupSettings) {
        if (startupSettings.runOnStartupEnabled()) {
            Thread.yield()
        }
        if (args.contains("-startup")) {
            LimeWireUtils.setAutoStartupLaunch(true)
            if (!startupSettings.runOnStartupEnabled()) {
                exitProcess(0)
            }
        }

        if (!startupSettings.allowMultipleInstances()) {
            try {
                if (ActiveLimeWireCheck.instance().checkForActiveLimeWire(args)) {
                    exitProcess(0)
                }
            } catch (_: ActiveLimeWireCheck.ActiveLimeWireException) {
                if (!confirmContinueWhenAlreadyRunning()) {
                    exitProcess(0)
                }
            }
        }
    }

    private fun startCoreServices(injector: Injector) {
        val firewallService = injector.getInstance(FirewallService::class.java)
        val lifecycleManager = injector.getInstance(LifecycleManager::class.java)
        if (!firewallService.addToFirewall()) {
            lifecycleManager.loadBackgroundTasks()
        }
        lifecycleManager.start()
    }

    private fun validateEarlyCore(injector: Injector) {
        if (!injector.getInstance(NIODispatcher::class.java).isRunning) {
            throw ComposeStartupFailureException(
                detail = "Fatal error: a firewall has prevented WireShare from connecting to itself (loopback). Verify your local firewall or whitelist settings, then try again."
            )
        }
    }

    private fun confirmContinueWhenAlreadyRunning(): Boolean {
        var continueAnyway = false
        runCatching {
            application {
                AlreadyRunningApp(
                    onContinue = {
                        continueAnyway = true
                        exitApplication()
                    },
                    onQuit = ::exitApplication
                )
            }
        }.onFailure { failure ->
            System.err.println(failure.message ?: failure.javaClass.name)
        }
        return continueAnyway
    }

    private fun fatalStartupDetail(failure: Throwable): String? {
        return when (failure) {
            is ComposeStartupFailureException -> failure.detail
            else -> failure.message?.takeIf { it.isNotBlank() } ?: failure.javaClass.name
        }
    }

}

private class ComposeStartupFailureException(
    val detail: String,
    cause: Throwable? = null
) : RuntimeException(detail, cause)
