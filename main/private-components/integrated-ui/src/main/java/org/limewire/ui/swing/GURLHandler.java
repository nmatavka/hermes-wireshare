package org.limewire.ui.swing;

import java.awt.Desktop;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.swing.SwingUtilities;

import org.limewire.service.ErrorService;
import org.limewire.util.OSUtils;

import com.limegroup.gnutella.browser.ExternalControl;

/**
 * Desktop based URI handler for macOS.
 */
public final class GURLHandler {
    
    private static GURLHandler instance;
    
    private volatile boolean registered = false;    
    private volatile boolean enabled = false;
    private final Queue<String> queuedUrls = new ConcurrentLinkedQueue<String>();
    private volatile ExternalControl externalControl;
    
    public static synchronized GURLHandler getInstance() {
        if(instance == null)
            instance = new GURLHandler();
        return instance;
    }
        
    /** Called by the native code. */
    @SuppressWarnings("unused")
    private void callback(final String url) {
        ExternalControl control = externalControl;
        if (enabled && control != null && control.isInitialized()) {
            Runnable runner = new Runnable() {
                @Override
				public void run() {
                    try {
                        control.handleMagnetRequest(url);
                    } catch(Throwable t) {
                        ErrorService.error(t);
                    }
                } 
            };
            SwingUtilities.invokeLater(runner);
        } else {
            queuedUrls.add(url);
        }
    }
    
    public void enable(ExternalControl externalControl) {
        this.externalControl = externalControl;
        String queuedUrl;
        while ((queuedUrl = queuedUrls.poll()) != null) {
            externalControl.enqueueControlRequest(queuedUrl);
        }
        this.enabled = true;
    }
    
    /** Registers the URI handler with the Java desktop integration. */
    public synchronized void register() {
        if (!registered) {
            if (!OSUtils.isMacOSX() || !Desktop.isDesktopSupported()) {
                return;
            }

            Desktop desktop = Desktop.getDesktop();
            if (!desktop.isSupported(Desktop.Action.APP_OPEN_URI)) {
                return;
            }

            desktop.setOpenURIHandler(event -> callback(event.getURI().toString()));
            registered = true;
        }
    }
}
