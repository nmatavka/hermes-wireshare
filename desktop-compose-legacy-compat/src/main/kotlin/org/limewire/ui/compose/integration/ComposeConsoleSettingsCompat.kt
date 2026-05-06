package org.limewire.ui.compose.integration

import org.limewire.ui.desktop.settings.ConsoleSettings

fun legacySwingComposeConsoleSettings(): ComposeConsoleSettings {
    return object : ComposeConsoleSettings {
        override fun patternLayout(): String = ConsoleSettings.CONSOLE_PATTERN_LAYOUT.get()
    }
}
