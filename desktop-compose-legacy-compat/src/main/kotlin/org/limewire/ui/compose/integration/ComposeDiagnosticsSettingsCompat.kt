package org.limewire.ui.compose.integration

import org.limewire.ui.swing.settings.BugSettings
import java.io.File

fun legacySwingComposeDiagnosticsSettings(): ComposeDiagnosticsSettings {
    return object : ComposeDiagnosticsSettings {
        override fun logBugsLocally(): Boolean = BugSettings.LOG_BUGS_LOCALLY.getValue()

        override fun bugLogFile(): File = BugSettings.BUG_LOG_FILE.get()

        override fun maxBugFileSizeBytes(): Long = BugSettings.MAX_BUGFILE_SIZE.getValue().toLong()
    }
}
