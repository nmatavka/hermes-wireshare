package org.limewire.ui.compose.integration

import org.apache.log4j.Appender
import org.apache.log4j.Level
import org.apache.log4j.LogManager
import org.apache.log4j.Logger
import org.apache.log4j.PatternLayout
import org.apache.log4j.WriterAppender
import org.limewire.core.api.connection.ConnectionItem
import org.limewire.core.api.connection.FWTStatusReason
import org.limewire.core.api.connection.FirewallStatus
import org.limewire.core.api.connection.FirewallStatusEvent
import org.limewire.core.api.connection.FirewallTransferStatusEvent
import org.limewire.core.api.connection.GnutellaConnectionManager
import org.limewire.core.api.mojito.MojitoManager
import org.limewire.core.api.monitor.IncomingSearchManager
import org.limewire.core.api.support.LocalClientInfoFactory
import org.limewire.listener.EventBean
import org.limewire.ui.swing.plugin.SwingUiPlugin
import org.limewire.ui.compose.snapshotEventList
import org.limewire.ui.swing.settings.ConsoleSettings
import org.limewire.ui.swing.util.BackgroundExecutorService
import org.limewire.ui.swing.util.LogUtils
import org.limewire.util.ThreadUtils
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.PrintWriter
import java.io.Writer
import java.awt.Component
import java.net.InetAddress
import java.net.UnknownHostException
import java.util.Date
import javax.swing.JComponent

interface ComposeAdvancedToolsService {
    fun connectionListSnapshot(resolveHostnames: Boolean): List<ConnectionItem>
    fun incomingSearchListSnapshot(): List<String>
    fun firewallStatus(): FirewallStatus?
    fun firewallTransferStatusEvent(): FirewallTransferStatusEvent?
    fun dhtName(): String
    fun dhtRunning(): Boolean
    fun mojitoVisualizerAvailable(): Boolean
    fun openMojitoVisualizer(): ComposeMojitoVisualizerSession?
    fun addConnection(host: String, port: Int, useTls: Boolean)
    fun removeConnection(item: ConnectionItem)
    fun consoleAvailable(): Boolean
    fun loggerNames(): List<String>
    fun loggerLevel(loggerName: String): String
    fun applyLoggerLevel(loggerName: String, levelName: String)
    fun attachConsole(listener: (String) -> Unit): AutoCloseable
    fun saveDiagnostics(target: File, consoleText: String)
}

interface ComposeConsoleCaptureService {
    fun available(): Boolean
    fun loggerNames(): List<String>
    fun loggerLevel(loggerName: String): String
    fun applyLoggerLevel(loggerName: String, levelName: String)
    fun attach(listener: (String) -> Unit): AutoCloseable
}

interface ComposeMojitoVisualizerSession : AutoCloseable {
    val title: String
    fun component(): Component?
}

class CoreComposeAdvancedToolsService(
    private val connectionManager: GnutellaConnectionManager,
    private val incomingSearchManager: IncomingSearchManager,
    private val mojitoManager: MojitoManager,
    private val firewallStatusBean: EventBean<FirewallStatusEvent>,
    private val firewallTransferBean: EventBean<FirewallTransferStatusEvent>,
    private val localClientInfoFactory: LocalClientInfoFactory,
    private val consoleCaptureService: ComposeConsoleCaptureService,
    private val mojitoArcsPlugin: SwingUiPlugin? = null
) : ComposeAdvancedToolsService {

    init {
        incomingSearchManager.setListEnabled(true)
        incomingSearchManager.setListSize(32)
    }

    override fun connectionListSnapshot(resolveHostnames: Boolean): List<ConnectionItem> {
        return snapshotEventList(connectionManager.connectionList).onEach { item ->
            item.update()
            updateHostNameState(item, resolveHostnames)
        }
    }

    override fun incomingSearchListSnapshot(): List<String> {
        return snapshotEventList(incomingSearchManager.incomingSearchList)
    }

    override fun firewallStatus(): FirewallStatus? = firewallStatusBean.lastEvent?.data

    override fun firewallTransferStatusEvent(): FirewallTransferStatusEvent? = firewallTransferBean.lastEvent

    override fun dhtName(): String = mojitoManager.name

    override fun dhtRunning(): Boolean = mojitoManager.isRunning

    override fun mojitoVisualizerAvailable(): Boolean = mojitoArcsPlugin != null

    override fun openMojitoVisualizer(): ComposeMojitoVisualizerSession? {
        val plugin = mojitoArcsPlugin ?: return null
        val component = plugin.getPluginComponent() ?: return null
        plugin.startPlugin()
        return object : ComposeMojitoVisualizerSession {
            override val title: String = plugin.pluginName

            override fun component(): Component = component

            override fun close() {
                plugin.stopPlugin()
            }
        }
    }

    override fun addConnection(host: String, port: Int, useTls: Boolean) {
        connectionManager.tryConnection(host, port, useTls)
    }

    override fun removeConnection(item: ConnectionItem) {
        connectionManager.removeConnection(item)
    }

    override fun consoleAvailable(): Boolean = consoleCaptureService.available()

    override fun loggerNames(): List<String> = consoleCaptureService.loggerNames()

    override fun loggerLevel(loggerName: String): String = consoleCaptureService.loggerLevel(loggerName)

    override fun applyLoggerLevel(loggerName: String, levelName: String) {
        consoleCaptureService.applyLoggerLevel(loggerName, levelName)
    }

    override fun attachConsole(listener: (String) -> Unit): AutoCloseable {
        return consoleCaptureService.attach(listener)
    }

    override fun saveDiagnostics(target: File, consoleText: String) {
        val buffer = StringBuilder()
        buffer.append(Date()).append("\n\n")
        val info = localClientInfoFactory.createLocalClientInfo(
            object : Exception() {
                override fun printStackTrace(out: PrintWriter) {
                    /* intentionally blank */
                }
            },
            Thread.currentThread().name,
            "Console Log",
            false
        )
        buffer.append(info.toBugReport())
        val traces = ThreadUtils.getAllStackTraces()
        buffer.append("-- BEGIN STACK TRACES --\n")
        buffer.append(if (traces.isNotBlank()) traces else "NONE")
        buffer.append("\n-- END STACK TRACES --\n")
        buffer.append("\n-- BEGIN LOG --\n")
        buffer.append(if (consoleText.isNotBlank()) consoleText else "NONE")
        buffer.append("\n-- END LOG --\n")
        target.parentFile?.mkdirs()
        BufferedWriter(FileWriter(target)).use { writer ->
            writer.write(buffer.toString())
        }
    }

    private fun updateHostNameState(item: ConnectionItem, resolveHostnames: Boolean) {
        if (!item.isConnected) {
            return
        }
        if (!item.isAddressResolved && resolveHostnames && System.currentTimeMillis() - item.time > 10_000L) {
            item.setAddressResolved(true)
            BackgroundExecutorService.execute {
                try {
                    item.setHostName(InetAddress.getByName(item.hostName).hostName)
                } catch (_: UnknownHostException) {
                }
            }
        } else if (item.isAddressResolved && !resolveHostnames) {
            item.resetHostName()
        }
    }
}

class UnavailableComposeConsoleCaptureService : ComposeConsoleCaptureService {
    override fun available(): Boolean = false
    override fun loggerNames(): List<String> = emptyList()
    override fun loggerLevel(loggerName: String): String = "INFO"
    override fun applyLoggerLevel(loggerName: String, levelName: String) = Unit
    override fun attach(listener: (String) -> Unit): AutoCloseable = AutoCloseable { }
}

class Log4jComposeConsoleCaptureService : ComposeConsoleCaptureService {
    override fun available(): Boolean = LogUtils.isLog4JAvailable()

    override fun loggerNames(): List<String> {
        val loggerNames = mutableListOf("root")
        val enumeration = LogManager.getLoggerRepository().currentLoggers
        while (enumeration.hasMoreElements()) {
            val logger = enumeration.nextElement() as? Logger ?: continue
            if (logger.name.isNullOrBlank()) {
                continue
            }
            loggerNames += logger.name
        }
        return loggerNames.distinct().sortedWith(compareBy<String> { it != "root" }.thenBy(String::lowercase))
    }

    override fun loggerLevel(loggerName: String): String {
        return loggerForName(loggerName).effectiveLevel?.toString() ?: "INFO"
    }

    override fun applyLoggerLevel(loggerName: String, levelName: String) {
        loggerForName(loggerName).level = Level.toLevel(levelName, Level.INFO)
    }

    override fun attach(listener: (String) -> Unit): AutoCloseable {
        val appender: Appender = WriterAppender(
            PatternLayout(ConsoleSettings.CONSOLE_PATTERN_LAYOUT.get()),
            object : Writer() {
                override fun write(cbuf: CharArray, off: Int, len: Int) {
                    listener(String(cbuf, off, len))
                }

                override fun flush() = Unit

                override fun close() = Unit
            }
        )
        LogManager.getRootLogger().addAppender(appender)
        return AutoCloseable {
            LogManager.getRootLogger().removeAppender(appender)
        }
    }

    private fun loggerForName(loggerName: String): Logger {
        return if (loggerName == "root") {
            LogManager.getRootLogger()
        } else {
            LogManager.getLogger(loggerName)
        }
    }
}

fun composeConsoleCaptureService(): ComposeConsoleCaptureService {
    return if (LogUtils.isLog4JAvailable()) {
        Log4jComposeConsoleCaptureService()
    } else {
        UnavailableComposeConsoleCaptureService()
    }
}
