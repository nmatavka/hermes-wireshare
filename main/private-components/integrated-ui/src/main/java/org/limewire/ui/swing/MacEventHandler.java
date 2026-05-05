package org.limewire.ui.swing;

import java.awt.Desktop;
import java.awt.desktop.AboutEvent;
import java.awt.desktop.AboutHandler;
import java.awt.desktop.AppReopenedEvent;
import java.awt.desktop.AppReopenedListener;
import java.awt.desktop.OpenFilesEvent;
import java.awt.desktop.OpenFilesHandler;
import java.awt.desktop.PreferencesEvent;
import java.awt.desktop.PreferencesHandler;
import java.awt.desktop.QuitEvent;
import java.awt.desktop.QuitHandler;
import java.awt.desktop.QuitResponse;
import java.io.File;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.swing.SwingUtilities;

import org.limewire.core.api.download.DownloadAction;
import org.limewire.core.api.download.DownloadException;
import org.limewire.core.api.file.CategoryManager;
import org.limewire.core.api.lifecycle.LifeCycleManager;
import org.limewire.ui.swing.mainframe.AboutAction;
import org.limewire.ui.swing.mainframe.OptionsAction;
import org.limewire.ui.swing.menu.ExitAction;
import org.limewire.ui.swing.util.NativeLaunchUtils;
import org.limewire.util.OSUtils;

import com.google.inject.Inject;
import com.limegroup.gnutella.ActivityCallback;
import com.limegroup.gnutella.DownloadManager;
import com.limegroup.gnutella.browser.ExternalControl;
import com.limegroup.gnutella.util.LimeWireUtils;


public class MacEventHandler implements AboutHandler, PreferencesHandler, AppReopenedListener,
        OpenFilesHandler, QuitHandler {

    private static MacEventHandler INSTANCE;

    public static synchronized MacEventHandler instance() {
        if (INSTANCE == null) {
            INSTANCE = new MacEventHandler();
        }
        return INSTANCE;
    }

    private final Queue<File> queuedFiles = new ConcurrentLinkedQueue<File>();
    private volatile boolean enabled;
    private volatile boolean registered;

    @Inject private volatile ExternalControl externalControl = null;
    @Inject private volatile DownloadManager downloadManager = null;
    @Inject private volatile LifeCycleManager lifecycleManager = null;
    @Inject private volatile ActivityCallback activityCallback = null;
    @Inject private volatile AboutAction aboutAction = null;
    @Inject private volatile OptionsAction optionsAction = null;
    @Inject private volatile ExitAction exitAction = null;
    @Inject private volatile CategoryManager categoryManager = null;

    private boolean allMenusAndActionsEnabled = false;

    public MacEventHandler() {
        assert ( OSUtils.isMacOSX() ) : "MacEventHandler should only be used on Mac OS-X operating systems.";
    }

    public synchronized void register() {
        if (registered || !Desktop.isDesktopSupported()) {
            return;
        }

        Desktop desktop = Desktop.getDesktop();
        if (desktop.isSupported(Desktop.Action.APP_ABOUT)) {
            desktop.setAboutHandler(this);
        }
        if (desktop.isSupported(Desktop.Action.APP_PREFERENCES)) {
            desktop.setPreferencesHandler(this);
        }
        if (desktop.isSupported(Desktop.Action.APP_QUIT_HANDLER)) {
            desktop.setQuitHandler(this);
        }
        if (desktop.isSupported(Desktop.Action.APP_OPEN_FILE)) {
            desktop.setOpenFileHandler(this);
        }
        desktop.addAppEventListener(this);
        registered = true;
    }

    @Inject
    public void startup() {
        this.enabled = true;

        File queuedFile;
        while ((queuedFile = queuedFiles.poll()) != null) {
            runFileOpen(queuedFile);
        }
    }

    /**
     * Enable preferences.
     */
    public void enableAllMacMenusAndEventHandlers() {
        allMenusAndActionsEnabled = true;
    }

    @Override
    public void handleAbout(AboutEvent e) {
        if (!allMenusAndActionsEnabled || aboutAction == null) {
            return;
        }
        runOnEdt(new Runnable() {
            @Override
            public void run() {
                aboutAction.actionPerformed(null);
            }
        });
    }

    @Override
    public void appReopened(AppReopenedEvent e) {
        if (!allMenusAndActionsEnabled || activityCallback == null) {
            return;
        }
        runOnEdt(new Runnable() {
            @Override
            public void run() {
                activityCallback.restoreApplication();
            }
        });
    }

    @Override
    public void openFiles(OpenFilesEvent event) {
        // This handler unlike the others may receive OS events before the UI is created.
        // So, don't check if allMenusAndActionsEnabled is true.

        for (File file : event.getFiles()) {
            if (!enabled) {
                queuedFiles.add(file);
            } else {
                runFileOpen(file);
            }
        }
    }

    @Override
    public void handlePreferences(PreferencesEvent e) {
        if (!allMenusAndActionsEnabled || optionsAction == null) {
            return;
        }
        runOnEdt(new Runnable() {
            @Override
            public void run() {
                optionsAction.actionPerformed(null);
            }
        });
    }

    @Override
    public void handleQuitRequestWith(QuitEvent e, final QuitResponse response) {
        response.cancelQuit();
        if (!allMenusAndActionsEnabled || exitAction == null) {
            return;
        }
        runOnEdt(new Runnable() {
            @Override
            public void run() {
                exitAction.actionPerformed(null);
            }
        });
    }

    private void runFileOpen(final File file) {
        String filename = file.getPath();
        if (filename.endsWith("limestart")) {
            LimeWireUtils.setAutoStartupLaunch(true);
        } else if (filename.endsWith("torrent")) {
            if (!lifecycleManager.isStarted()) {
                externalControl.enqueueControlRequest(file.getAbsolutePath());
            } else {
                try {
                    downloadManager.downloadTorrent(file, null, false);
                } catch (DownloadException e) {
                    activityCallback.handleDownloadException(new DownloadAction() {
                        @Override
                        public void download(File saveDirectory, boolean overwrite)
                                throws DownloadException {
                            downloadManager.downloadTorrent(file, saveDirectory, overwrite);
                        }

                        @Override
                        public void downloadCanceled(DownloadException ignored) {
                            //nothing to do
                        }

                    }, e, false);

                }
            }
        } else {
            NativeLaunchUtils.safeLaunchFile(file, categoryManager);
        }
    }

    private void runOnEdt(Runnable runnable) {
        if (SwingUtilities.isEventDispatchThread()) {
            runnable.run();
        } else {
            SwingUtilities.invokeLater(runnable);
        }
    }
	
}
