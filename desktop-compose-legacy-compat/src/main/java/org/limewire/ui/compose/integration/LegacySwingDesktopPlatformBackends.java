package org.limewire.ui.compose.integration;

import java.io.File;
import java.net.URI;

import org.limewire.core.api.file.CategoryManager;
import org.limewire.ui.swing.settings.SwingUiSettings;
import org.limewire.ui.swing.util.NativeLaunchUtils;
import org.limewire.util.CommonUtils;

public final class LegacySwingDesktopPlatformBackends {

    private LegacySwingDesktopPlatformBackends() {
    }

    public static File lastChooserDirectory() {
        File saved = SwingUiSettings.LAST_FILECHOOSER_DIRECTORY.get();
        if (isExistingDirectory(saved)) {
            return saved;
        }

        File home = CommonUtils.getUserHomeDir();
        if (isExistingDirectory(home)) {
            return home;
        }

        File current = CommonUtils.getCurrentDirectory();
        return isExistingDirectory(current) ? current : null;
    }

    public static void rememberChooserDirectory(File file) {
        File directory = file != null && file.isDirectory() ? file : file != null ? file.getParentFile() : null;
        if (isExistingDirectory(directory)) {
            SwingUiSettings.LAST_FILECHOOSER_DIRECTORY.set(directory);
        }
    }

    public static SilentLaunchResult openFile(File file, CategoryManager categoryManager) {
        return adapt(NativeLaunchUtils.safeLaunchFileSilently(file, categoryManager));
    }

    public static SilentLaunchResult revealFile(File file) {
        return adapt(NativeLaunchUtils.launchExplorerSilently(file));
    }

    public static SilentLaunchResult openUri(URI uri) {
        return adapt(NativeLaunchUtils.openURLSilently(uri.toString()));
    }

    private static SilentLaunchResult adapt(NativeLaunchUtils.LaunchResult result) {
        return new SilentLaunchResult(result.successful(), result.userTitle(), result.userMessage());
    }

    private static boolean isExistingDirectory(File file) {
        return file != null && file.exists() && file.isDirectory();
    }

    public static final class SilentLaunchResult {
        private final boolean successful;
        private final String title;
        private final String message;

        SilentLaunchResult(boolean successful, String title, String message) {
            this.successful = successful;
            this.title = title;
            this.message = message;
        }

        public boolean isSuccessful() {
            return successful;
        }

        public String getTitle() {
            return title;
        }

        public String getMessage() {
            return message;
        }
    }
}
