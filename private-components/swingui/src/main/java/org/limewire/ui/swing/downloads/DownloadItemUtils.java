package org.limewire.ui.swing.downloads;

import java.awt.Cursor;
import java.awt.EventQueue;
import java.io.File;
import java.util.concurrent.ExecutionException;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import org.limewire.core.api.download.DownloadItem;
import org.limewire.core.api.download.DownloadState;
import org.limewire.ui.swing.components.FocusJOptionPane;
import org.limewire.ui.swing.components.MultiLineLabel;
import org.limewire.ui.swing.player.PlayerMediator;
import org.limewire.ui.swing.util.GuiUtils;
import org.limewire.ui.swing.util.I18n;

import com.google.inject.Provider;

public class DownloadItemUtils {
    
    private DownloadItemUtils() {}
    
    private static volatile int downloadsLaunchedV2 = 0;
    
    private static volatile int previewsLaunched = 0;
        
    /**
     * Launches the download, loading the launchable portion in the background
     * if necessary.
     */
    // 00000 DEPRECATED?
    public static void launch(final DownloadItem downloadItem, final Provider<PlayerMediator> playerMediator) {
        assert EventQueue.isDispatchThread();
        assert downloadItem.isLaunchable();
                
        GuiUtils.getMainFrame().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        new SwingWorker<File, Void>() {
            @Override
            protected File doInBackground() throws Exception {
                return downloadItem.getLaunchableFile();
            }
            
            @Override
            protected void done() {
                GuiUtils.getMainFrame().setCursor(Cursor.getDefaultCursor());
                File file;
                try {
                    file = get();
                    if(file != null) {
                        playerMediator.get().playOrLaunchNatively(file);
                    }
                } catch (InterruptedException ignored) {
                } catch (ExecutionException e) {
                    throw new RuntimeException(e);
                }
            }
        }.execute();
        
        if (downloadItem.getState().isFinished()) {
            downloadsLaunchedV2++;
        } else {
            previewsLaunched++;
        }
    }
    
    

}
