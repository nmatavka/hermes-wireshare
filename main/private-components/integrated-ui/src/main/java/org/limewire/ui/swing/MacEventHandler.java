package org.limewire.ui.swing;

import java.awt.Desktop;
import java.awt.desktop.*;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;
import javax.swing.ActionMap;
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
        OpenFilesHandler, OpenURIHandler, QuitHandler {

    private static MacEventHandler INSTANCE;

    public static synchronized MacEventHandler instance() {
        if (INSTANCE == null) {
            INSTANCE = new MacEventHandler();
        }
        return INSTANCE;
    }

    private final List<File> pendingFiles = new ArrayList<File>();
    private final List<URI> pendingUris = new ArrayList<URI>();
    private volatile boolean enabled;

    @Inject private volatile ExternalControl externalControl = null;
    @Inject private volatile DownloadManager downloadManager = null;
    @Inject private volatile LifeCycleManager lifecycleManager = null;
    @Inject private volatile ActivityCallback activityCallback = null;
    @Inject private volatile AboutAction aboutAction = null;
    @Inject private volatile OptionsAction optionsAction = null;
    @Inject private volatile ExitAction exitAction = null;
    @Inject private volatile CategoryManager categoryManager = null;

    private boolean allMenusAndActionsEnabled = false;
    private volatile boolean desktopHandlersRegistered = false;

    @Inject
    public MacEventHandler() {
        assert ( OSUtils.isMacOSX() ) : "MacEventHandler should only be used on Mac OS-X operating systems.";
        registerDesktopHandlers();
    }


    @Inject
    public void startup() {
        this.enabled = true;

        synchronized (pendingFiles) {
            for (File file : pendingFiles) {
                runFileOpen(file);
            }
            pendingFiles.clear();
        }

        synchronized (pendingUris) {
            for (URI uri : pendingUris) {
                runUriOpen(uri);
            }
            pendingUris.clear();
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

        if (allMenusAndActionsEnabled ) {
            aboutAction.actionPerformed(null);
        }

    }

    @Override
    public void appReopened(AppReopenedEvent e) {
        if (allMenusAndActionsEnabled ) {
            restoreApplication();
        }

    }

    @Override
    public void openFiles(OpenFilesEvent event) {
        // This handler unlike the others may receive OS events before the UI is created.
        // So, don't check if allMenusAndActionsEnabled is true.

        for (File file : event.getFiles()) {
            if (!enabled) {
                synchronized (pendingFiles) {
                    pendingFiles.add(file);
                }
            } else {
                runFileOpen(file);
            }
        }

    }

    @Override
    public void handlePreferences(PreferencesEvent e) {

        if (allMenusAndActionsEnabled ) {
            optionsAction.actionPerformed(null);
        }
    }

    @Override
    public void openURI(OpenURIEvent event) {
        URI uri = event.getURI();
        if (uri == null) {
            return;
        }

        if (!enabled) {
            synchronized (pendingUris) {
                pendingUris.add(uri);
            }
        } else {
            runUriOpen(uri);
        }
    }

    @Override
    public void handleQuitRequestWith(QuitEvent event, QuitResponse response) {
        if (allMenusAndActionsEnabled) {
            exitAction.actionPerformed(null);
            response.cancelQuit();
        } else {
            response.performQuit();
        }
    }

    private synchronized void registerDesktopHandlers() {
        if (desktopHandlersRegistered || !Desktop.isDesktopSupported()) {
            return;
        }

        Desktop desktop = Desktop.getDesktop();
        if (desktop.isSupported(Desktop.Action.APP_ABOUT)) {
            desktop.setAboutHandler(this);
        }
        if (desktop.isSupported(Desktop.Action.APP_PREFERENCES)) {
            desktop.setPreferencesHandler(this);
        }
        desktop.addAppEventListener(this);
        if (desktop.isSupported(Desktop.Action.APP_OPEN_FILE)) {
            desktop.setOpenFileHandler(this);
        }
        if (desktop.isSupported(Desktop.Action.APP_OPEN_URI)) {
            desktop.setOpenURIHandler(this);
        }
        if (desktop.isSupported(Desktop.Action.APP_QUIT_HANDLER)) {
            desktop.setQuitHandler(this);
        }
        desktopHandlersRegistered = true;
    }

    private void runFileOpen(final File file) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
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
        });
    }

    private void runUriOpen(final URI uri) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                String request = uri.toString();
                if (!lifecycleManager.isStarted() || !externalControl.isInitialized()) {
                    externalControl.enqueueControlRequest(request);
                } else {
                    externalControl.handleMagnetRequest(request);
                }
            }
        });
    }

    private void restoreApplication() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                ActionMap actionMap = org.jdesktop.application.Application.getInstance()
                        .getContext().getActionMap();
                Action restoreView = actionMap.get("restoreView");
                if (restoreView != null) {
                    restoreView.actionPerformed(null);
                }
            }
        });
    }

}
