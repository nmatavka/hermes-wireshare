package org.limewire.ui.compose.settings

class LegacySwingComposeStartupSettings : ComposeStartupSettings {
    override fun applyPreferredLocale() {
        LegacySwingComposeStartupBackend.applyPreferredLocale()
    }

    override fun runOnStartupEnabled(): Boolean = LegacySwingComposeStartupBackend.runOnStartupEnabled()

    override fun allowMultipleInstances(): Boolean = LegacySwingComposeStartupBackend.allowMultipleInstances()

    override fun ensureRunOnStartupConfigured() {
        LegacySwingComposeStartupBackend.ensureRunOnStartupConfigured()
    }
}
