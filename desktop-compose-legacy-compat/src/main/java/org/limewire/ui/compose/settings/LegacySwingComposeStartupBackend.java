package org.limewire.ui.compose.settings;

import org.limewire.ui.swing.settings.StartupSettings;
import org.limewire.ui.swing.util.LocaleUtils;
import org.limewire.ui.swing.util.WindowsUtils;
import org.limewire.util.OSUtils;

public final class LegacySwingComposeStartupBackend {

    private LegacySwingComposeStartupBackend() {
    }

    public static void applyPreferredLocale() {
        LocaleUtils.setLocaleFromPreferences();
        LocaleUtils.validateLocaleAndFonts();
    }

    public static boolean runOnStartupEnabled() {
        return StartupSettings.RUN_ON_STARTUP.getValue();
    }

    public static boolean allowMultipleInstances() {
        return StartupSettings.ALLOW_MULTIPLE_INSTANCES.getValue();
    }

    public static void ensureRunOnStartupConfigured() {
        if (!OSUtils.isWindows() || !runOnStartupEnabled()) {
            return;
        }
        try {
            if (WindowsUtils.isLoginStatusAvailable()) {
                WindowsUtils.setLoginStatus(true);
            }
        } catch (Throwable ignored) {
        }
    }
}
