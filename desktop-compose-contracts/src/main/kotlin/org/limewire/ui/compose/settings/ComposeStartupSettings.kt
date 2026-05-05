package org.limewire.ui.compose.settings

interface ComposeStartupSettings {
    fun applyPreferredLocale()
    fun runOnStartupEnabled(): Boolean
    fun allowMultipleInstances(): Boolean
    fun ensureRunOnStartupConfigured()
}
