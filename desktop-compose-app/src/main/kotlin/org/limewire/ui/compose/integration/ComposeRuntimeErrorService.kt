package org.limewire.ui.compose.integration

import org.limewire.core.api.support.LocalClientInfoFactory
import org.limewire.service.ErrorCallback
import org.limewire.service.ErrorService
import org.limewire.ui.compose.tr
import org.limewire.util.FileUtils
import org.limewire.util.NotImplementedException
import org.limewire.util.ThreadUtils
import com.sun.jna.Callback
import com.sun.jna.Native
import java.awt.AWTEvent
import java.awt.EventQueue
import java.awt.IllegalComponentStateException
import java.awt.Toolkit
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.io.PrintWriter
import java.io.StringWriter
import java.util.Date
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference

private const val SWING_PACKAGE_PREFIX = "javax" + ".swing."
private const val SWING_REPAINT_MANAGER = SWING_PACKAGE_PREFIX + "RepaintManager"
private const val SWING_TABBED_PANE_BOUNDS = SWING_PACKAGE_PREFIX + "plaf.basic.BasicTabbedPaneUI.getTabBounds"
private const val SWING_DEFAULT_ROW_SORTER = SWING_PACKAGE_PREFIX + "DefaultRowSorter"
private const val SWING_JCOMPONENT = SWING_PACKAGE_PREFIX + "JComponent"

data class ComposeRuntimeErrorReport(
    val id: Long,
    val title: String,
    val message: String,
    val detail: String?,
    val bugReport: String,
    val fatal: Boolean,
    val threadName: String
)

interface ComposeRuntimeErrorService {
    interface Listener {
        fun onRuntimeError(report: ComposeRuntimeErrorReport)
    }

    fun install()
    fun setLocalClientInfoFactory(factory: LocalClientInfoFactory)
    fun beginStartupCapture()
    fun finishStartupCapture(replay: Boolean)
    fun addListener(listener: Listener)
    fun removeListener(listener: Listener)
    fun fatalStartupReport(problem: Throwable, detail: String? = null): ComposeRuntimeErrorReport
    fun saveDiagnosticReport(target: File, report: ComposeRuntimeErrorReport)
}

class CoreComposeRuntimeErrorService(
    private var localClientInfoFactory: LocalClientInfoFactory? = null,
    private val diagnosticsSettings: ComposeDiagnosticsSettings = legacySwingComposeDiagnosticsSettings()
) : ComposeRuntimeErrorService {
    private enum class Source {
        ERROR_SERVICE,
        EVENT_DISPATCH,
        UNCAUGHT
    }

    private val listeners = CopyOnWriteArrayList<ComposeRuntimeErrorService.Listener>()
    private val startupErrors = CopyOnWriteArrayList<ComposeRuntimeErrorReport>()
    private val nextReportId = AtomicLong(1L)
    private val errorCallback = ComposeErrorCallback(this)
    private val uncaughtExceptionHandler = ComposeUncaughtExceptionHandler(this)
    private val callbackUncaughtExceptionHandler = ComposeCallbackUncaughtExceptionHandler(this)

    @Volatile
    private var startupCapture = false

    @Volatile
    private var installed = false

    override fun install() {
        activeService = this
        reassertGlobalHandlers()
        if (!installed) {
            installed = true
            installEventQueueCatcher()
        }
        System.setProperty(
            "sun.awt.exception.handler",
            ComposeEventDispatchErrorCatcher::class.java.name
        )
        runCatching {
            Native.setCallbackExceptionHandler(callbackUncaughtExceptionHandler)
        }.onFailure { failure ->
            System.err.println(failure.stackTraceToString())
        }
    }

    override fun setLocalClientInfoFactory(factory: LocalClientInfoFactory) {
        localClientInfoFactory = factory
    }

    override fun beginStartupCapture() {
        startupErrors.clear()
        startupCapture = true
    }

    override fun finishStartupCapture(replay: Boolean) {
        val pending = startupErrors.toList()
        startupErrors.clear()
        startupCapture = false
        if (replay) {
            pending.forEach(::dispatch)
        }
    }

    override fun addListener(listener: ComposeRuntimeErrorService.Listener) {
        listeners += listener
    }

    override fun removeListener(listener: ComposeRuntimeErrorService.Listener) {
        listeners -= listener
    }

    override fun fatalStartupReport(problem: Throwable, detail: String?): ComposeRuntimeErrorReport {
        val report = buildReport(
            problem = problem,
            threadName = Thread.currentThread().name,
            detail = detail,
            fatal = true
        )
        persistLatestDiagnosticReport(report)
        logLocallyIfEnabled(report)
        return report
    }

    override fun saveDiagnosticReport(target: File, report: ComposeRuntimeErrorReport) {
        val buffer = StringBuilder()
        buffer.append(Date()).append("\n\n")
        buffer.append(report.bugReport)
        if (!report.bugReport.endsWith("\n")) {
            buffer.append('\n')
        }
        buffer.append("\n-- BEGIN STACK TRACES --\n")
        val traces = ThreadUtils.getAllStackTraces()
        buffer.append(if (traces.isNotBlank()) traces else "NONE")
        buffer.append("\n-- END STACK TRACES --\n")
        target.parentFile?.mkdirs()
        FileOutputStream(target).bufferedWriter().use { writer ->
            writer.write(buffer.toString())
        }
    }

    internal fun handleErrorService(problem: Throwable, detail: String?, threadName: String) {
        if (problem is ThreadDeath) {
            throw problem
        }
        capture(problem, threadName, detail, fatal = false, source = Source.ERROR_SERVICE)
    }

    internal fun handleEventDispatchThrowable(problem: Throwable) {
        if (problem is ThreadDeath) {
            throw problem
        }
        if (isIgnorableEventDispatch(problem)) {
            System.err.println(problem.stackTraceToString())
            return
        }
        capture(
            problem = problem,
            threadName = Thread.currentThread().name,
            detail = "Uncaught event-thread error.",
            fatal = false,
            source = Source.EVENT_DISPATCH
        )
    }

    internal fun handleUncaughtThrowable(threadName: String, problem: Throwable) {
        if (!uncaughtHandlerActive.compareAndSet(false, true)) {
            System.err.println("Recursive uncaught exception while handling thread $threadName")
            problem.printStackTrace()
            return
        }
        try {
            if (problem is ThreadDeath) {
                throw problem
            }
            if (problem is StackOverflowError) {
                System.err.println("Uncaught StackOverflowError on thread $threadName")
                problem.printStackTrace()
                return
            }
            if (matchesIgnoredUncaughtTrace(problem)) {
                System.err.println(problem.stackTraceToString())
                return
            }
            capture(
                problem = problem,
                threadName = threadName,
                detail = "Uncaught thread error: $threadName",
                fatal = false,
                source = Source.UNCAUGHT
            )
        } catch (handlerFailure: Throwable) {
            System.err.println("Compose runtime error handler failed on thread $threadName")
            handlerFailure.printStackTrace()
            problem.printStackTrace()
        } finally {
            uncaughtHandlerActive.set(false)
        }
    }

    private fun capture(
        problem: Throwable,
        threadName: String,
        detail: String?,
        fatal: Boolean,
        source: Source
    ) {
        if (isDuplicate(problem, threadName, detail, fatal)) {
            return
        }
        val report = buildReport(problem, threadName, detail, fatal)
        persistLatestDiagnosticReport(report)
        logLocallyIfEnabled(report)
        if (startupCapture && !fatal && source != Source.ERROR_SERVICE) {
            startupErrors += report
            return
        }
        dispatch(report)
    }

    private fun installEventQueueCatcher() {
        if (!eventQueueInstalled.compareAndSet(false, true)) {
            return
        }
        runCatching {
            Toolkit.getDefaultToolkit().systemEventQueue.push(
                object : EventQueue() {
                    override fun dispatchEvent(event: AWTEvent) {
                        try {
                            super.dispatchEvent(event)
                        } catch (problem: Throwable) {
                            if (problem is ThreadDeath) {
                                throw problem
                            }
                            handleEventDispatchThrowable(problem)
                        }
                    }
                }
            )
        }.onFailure { failure ->
            System.err.println("Unable to install Compose event queue error catcher")
            System.err.println(failure.stackTraceToString())
        }
    }

    private fun dispatch(report: ComposeRuntimeErrorReport) {
        listeners.forEach { listener ->
            runCatching { listener.onRuntimeError(report) }
        }
    }

    private fun reassertGlobalHandlers() {
        ErrorService.setProtectedErrorCallback(errorCallback)
        Thread.setDefaultUncaughtExceptionHandler(uncaughtExceptionHandler)
        System.setProperty(
            "sun.awt.exception.handler",
            ComposeEventDispatchErrorCatcher::class.java.name
        )
    }

    private fun buildReport(
        problem: Throwable,
        threadName: String,
        detail: String?,
        fatal: Boolean
    ): ComposeRuntimeErrorReport {
        val title = if (fatal) {
            tr("WireShare Couldn't Start")
        } else if (problem is NotImplementedException) {
            tr("Error")
        } else {
            tr("An Internal Error Has Occurred")
        }
        val message = when {
            fatal -> tr("WireShare ran into a problem while it was starting up.")
            problem is NotImplementedException ->
                tr("This function has not yet been implemented. WireShare can continue running.")
            else -> tr("WireShare ran into an internal problem, but it can continue running.")
        }
        return ComposeRuntimeErrorReport(
            id = nextReportId.getAndIncrement(),
            title = title,
            message = message,
            detail = detail?.takeIf { it.isNotBlank() },
            bugReport = buildBugReport(problem, threadName, detail, fatal),
            fatal = fatal,
            threadName = threadName
        )
    }

    private fun buildBugReport(
        problem: Throwable,
        threadName: String,
        detail: String?,
        fatal: Boolean
    ): String {
        val factory = localClientInfoFactory
        if (factory != null) {
            runCatching {
                return factory.createLocalClientInfo(problem, threadName, detail, fatal).toBugReport()
            }
        }
        val writer = StringWriter()
        PrintWriter(writer).use { printWriter ->
            printWriter.println("WireShare Diagnostic Report")
            printWriter.println("Date: ${Date()}")
            printWriter.println("Thread: $threadName")
            detail?.takeIf(String::isNotBlank)?.let { printWriter.println("Detail: $it") }
            printWriter.println("Fatal: $fatal")
            printWriter.println()
            problem.printStackTrace(printWriter)
        }
        return writer.toString()
    }

    private fun logLocallyIfEnabled(report: ComposeRuntimeErrorReport) {
        if (!diagnosticsSettings.logBugsLocally()) {
            return
        }
        val target = diagnosticsSettings.bugLogFile()
        FileUtils.setWriteable(target)
        var output: OutputStream? = null
        try {
            synchronized(localLogWriteLock) {
                if (target.length() > diagnosticsSettings.maxBugFileSizeBytes()) {
                    target.delete()
                }
                output = BufferedOutputStream(FileOutputStream(target.path, true))
                output?.write((Date().toString() + "\n").toByteArray())
                output?.write(report.bugReport.toByteArray())
                output?.write(localLogSeparator)
                output?.flush()
            }
        } catch (_: IOException) {
        } finally {
            output?.close()
        }
    }

    private fun persistLatestDiagnosticReport(report: ComposeRuntimeErrorReport) {
        val configuredLogFile = diagnosticsSettings.bugLogFile()
        val targetDirectory = configuredLogFile.parentFile ?: configuredLogFile.absoluteFile.parentFile ?: File(".")
        val target = File(targetDirectory, "latest-crash.txt")
        runCatching {
            FileUtils.setWriteable(target)
            saveDiagnosticReport(target, report)
            System.err.println("Saved crash report to ${target.absolutePath}")
        }
    }

    private fun isDuplicate(
        problem: Throwable,
        threadName: String,
        detail: String?,
        fatal: Boolean
    ): Boolean {
        val fingerprint = buildString {
            append(problem.javaClass.name)
            append('|')
            append(problem.message)
            append('|')
            append(threadName)
            append('|')
            append(detail)
            append('|')
            append(fatal)
            problem.stackTrace.take(3).forEach { element ->
                append('|')
                append(element.className)
                append('#')
                append(element.methodName)
                append(':')
                append(element.lineNumber)
            }
        }
        val now = System.currentTimeMillis()
        val previous = lastErrorFingerprint.get()
        val previousAt = lastErrorFingerprintAt.get()
        if (previous == fingerprint && now - previousAt < 1500L) {
            return true
        }
        lastErrorFingerprint.set(fingerprint)
        lastErrorFingerprintAt.set(now)
        return false
    }

    private fun matchesIgnoredUncaughtTrace(problem: Throwable): Boolean {
        return problem.stackTrace.any { element ->
            element.className == "javax.jmdns.DNSRecord" && element.methodName == "suppressedBy"
        }
    }

    private fun isIgnorableEventDispatch(problem: Throwable): Boolean {
        if (problem is StackOverflowError) {
            return true
        }
        val stackTraceText = StringWriter().also { writer ->
            PrintWriter(writer).use(problem::printStackTrace)
        }.toString()
        if (stackTraceText.isBlank()) {
            return true
        }
        if (stackTraceText.contains(SWING_REPAINT_MANAGER)) {
            return true
        }
        if (stackTraceText.contains("sun.awt.RepaintArea.paint")) {
            return true
        }
        if (problem is ArrayIndexOutOfBoundsException) {
            if (stackTraceText.contains("apple.awt.CWindow.displayChanged")) {
                return true
            }
            if (stackTraceText.contains(SWING_TABBED_PANE_BOUNDS)) {
                return true
            }
        }
        if (problem is IllegalStateException && stackTraceText.contains("cannot open system clipboard")) {
            return true
        }
        if (problem is IllegalComponentStateException &&
            stackTraceText.contains("component must be showing on the screen to determine its location")
        ) {
            return true
        }
        if (problem is NullPointerException) {
            if (stackTraceText.contains("MetalFileChooserUI")) return true
            if (stackTraceText.contains("WindowsFileChooserUI")) return true
            if (stackTraceText.contains("AquaDirectoryModel")) return true
            if (stackTraceText.contains("SizeRequirements.calculateAlignedPositions")) return true
            if (stackTraceText.contains("BasicTextUI.damageRange")) return true
            if (stackTraceText.contains("null pData")) return true
            if (stackTraceText.contains("disposed component")) return true
        }
        if (problem is InternalError && stackTraceText.contains("getGraphics not implemented for this component")) {
            return true
        }
        if (!stackTraceText.contains("com.limegroup.gnutella") && !stackTraceText.contains("org.limewire")) {
            return true
        }
        val stack = problem.stackTrace
        if (problem is IndexOutOfBoundsException && stack.size > 1) {
            if (stack[0].className == SWING_DEFAULT_ROW_SORTER &&
                stack[1].className == "sun.swing.FilePane\$SortableListModel"
            ) {
                return true
            }
        }
        if (problem is NullPointerException && stack.size > 1) {
            if (stack[0].className == SWING_JCOMPONENT &&
                stack[1].className == "sun.swing.FilePane\$2"
            ) {
                return true
            }
        }
        return false
    }

    private class ComposeErrorCallback(
        private val service: CoreComposeRuntimeErrorService
    ) : ErrorCallback {
        override fun error(t: Throwable) {
            error(t, null)
        }

        override fun error(t: Throwable, msg: String?) {
            service.handleErrorService(t, msg, Thread.currentThread().name)
        }
    }

    private class ComposeUncaughtExceptionHandler(
        private val service: CoreComposeRuntimeErrorService
    ) : Thread.UncaughtExceptionHandler {
        override fun uncaughtException(thread: Thread, throwable: Throwable) {
            service.handleUncaughtThrowable(thread.name, throwable)
        }
    }

    private class ComposeCallbackUncaughtExceptionHandler(
        private val service: CoreComposeRuntimeErrorService
    ) : Callback.UncaughtExceptionHandler {
        override fun uncaughtException(callback: Callback, throwable: Throwable) {
            service.handleUncaughtThrowable(Thread.currentThread().name, throwable)
        }
    }

    companion object {
        private val localLogSeparator = "-----------------\n".toByteArray()
        private val localLogWriteLock = Any()
        private val uncaughtHandlerActive = AtomicBoolean(false)
        private val eventQueueInstalled = AtomicBoolean(false)
        private val lastErrorFingerprint = AtomicReference<String?>(null)
        private val lastErrorFingerprintAt = AtomicLong(0L)

        @Volatile
        private var activeService: CoreComposeRuntimeErrorService? = null

        @JvmStatic
        fun handleEventDispatchThrowable(problem: Throwable) {
            activeService?.handleEventDispatchThrowable(problem)
                ?: System.err.println(problem.stackTraceToString())
        }
    }
}
