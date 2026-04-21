package org.limewire.ui.swing.dock;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Taskbar;
import java.awt.image.BufferedImage;

import javax.swing.Icon;
import javax.swing.JPanel;

import org.limewire.ui.swing.components.LimeIconInfo;
import org.limewire.util.OSUtils;


/**
 * A utility class to modify the Dock Icon and request
 * for user attention.
 */
public class Dock {
    
    private static final boolean HAS_DOCK;
    private static final Taskbar TASKBAR;
    private static final Image BASE_DOCK_IMAGE;
    
    static {
        Taskbar taskbar = null;
        Image baseDockImage = null;
        boolean hasDock = OSUtils.isMacOSX() && Taskbar.isTaskbarSupported();
        if (hasDock) {
            try {
                taskbar = Taskbar.getTaskbar();
                if (taskbar.isSupported(Taskbar.Feature.ICON_IMAGE)) {
                    baseDockImage = taskbar.getIconImage();
                    if (baseDockImage == null) {
                        baseDockImage = new LimeIconInfo().getImage();
                    }
                }
            } catch (UnsupportedOperationException ignored) {
                hasDock = false;
            }
        }
        TASKBAR = taskbar;
        BASE_DOCK_IMAGE = baseDockImage;
        HAS_DOCK = hasDock;
    }   

    /**
     * These constants specify the level of severity of a user 
     * attention request and are used by 
     * {@link Dock#requestUserAttention(com.limegroup.gnutella.gui.Dock.AttentionType)}
     * and {@link Dock#cancelUserAttentionRequest(int)}.
     * 
     * See the documentation for NSApplication and its Constants for
     * more information.
     */
    public static enum AttentionType {
        
        /**
         * The Dock Icon will bounce until either the application
         * becomes active or the request is canceled.
         */
        CRITICAL(0),
        
        /**
         * The Dock Icon will bounce for one second. The request, 
         * though, remains active until either the application 
         * becomes active or the request is canceled.
         * 
         * NOTE: The Dock Icon will bounce like {@link #CRITICAL} 
         * endlessly until either the application becomes active or 
         * the request is canceled.
         */
        INFORMATIONAL(10);
        
        private final int type;
        
        private AttentionType(int type) {
            this.type = type;
        }
    }
    
    /** 
     * The width of the Dock Icon (128px).
     */
    public static final int ICON_WIDTH = 128;
    
    /**
     * The height of the Dock Icon (128px).
     */
    public static final int ICON_HEIGHT = 128;
    
    private Dock() {
        
    }
    
    /**
     * Returns the Lock the Dock is using to synchronize
     * access to this class.
     */
    public static Object getDockLock() {
        return Dock.class;
    }
    
    /**
     * Replaces the current Dock Icon with the given Icon.
     * 
     * @param icon The new Icon to be used in the Dock
     */
    public synchronized static void setDockTileImage(Icon icon) {
        paintIcon(icon, false);
    }
    
    /**
     * Overlays the current Dock icon with the given Icon.
     * 
     * @param icon The new overlay Icon
     */
    public synchronized static void setDockTileOverlayImage(Icon icon) {
        paintIcon(icon, true);
    }
    
    /**
     * Paints the Icon, extracts the pixel data and passes it to
     * the native code which draws the Icon in the Dock.
     * 
     * @param icon The new Icon
     * @param overlay Whether it's an overlay or replacement Icon
     */
    private synchronized static void paintIcon(Icon icon, boolean overlay) {
        if (!HAS_DOCK) {
            return;
        }
        
        if (icon == null) {
            throw new NullPointerException("Icon is null");
        }
        
        // We draw the Icon in this BufferedImage
        BufferedImage image = new BufferedImage(ICON_WIDTH, ICON_HEIGHT,
                BufferedImage.TYPE_INT_ARGB);
        
        // Setup a fake Component
        Component panel = new JPanel();
        Dimension iconSize = new Dimension(ICON_WIDTH, ICON_HEIGHT);
        panel.setSize(iconSize);
        panel.setPreferredSize(iconSize);
        panel.setMinimumSize(iconSize);
        panel.setMaximumSize(iconSize);
        
        // Create a Graphics Object and paint the Icon on it
        Graphics2D g = image.createGraphics();
        try {
            icon.paintIcon(panel, g, 0, 0);
        } finally {
            g.dispose();
        }

        if (overlay) {
            BufferedImage composedImage = new BufferedImage(ICON_WIDTH, ICON_HEIGHT,
                    BufferedImage.TYPE_INT_ARGB);
            Graphics2D composedGraphics = composedImage.createGraphics();
            try {
                if (BASE_DOCK_IMAGE != null) {
                    composedGraphics.drawImage(BASE_DOCK_IMAGE, 0, 0, ICON_WIDTH, ICON_HEIGHT,
                            null);
                }
                composedGraphics.drawImage(image, 0, 0, null);
            } finally {
                composedGraphics.dispose();
            }
            TASKBAR.setIconImage(composedImage);
        } else {
            TASKBAR.setIconImage(image);
        }
    }
    
    /**
     * Restores the Dock Icon to its original state.
     */
    public synchronized static void restoreDockTileImage() {
        if (HAS_DOCK && BASE_DOCK_IMAGE != null) {
            TASKBAR.setIconImage(BASE_DOCK_IMAGE);
        }
    }
    
    /**
     * Starts a user attention request. Calling this method has no
     * effect if the application is already active. The value returned
     * by this method can be used to manually cancel an attention
     * request.
     * 
     * @param requestType The type of the attention request
     * @return request identifier
     */
    public synchronized static int requestUserAttention(AttentionType requestType) {
        if (HAS_DOCK) {
            TASKBAR.requestUserAttention(requestType == AttentionType.CRITICAL, false);
            return 0;
        }
        return -1;
    }
    
    /**
     * Cancels a previous user attention request. A request is also canceled
     * automatically by user activation of the application.
     * 
     * @param request The return value from a previous call to 
     *  {@link #requestUserAttention(com.limegroup.gnutella.gui.Dock.AttentionType)}
     */
    public synchronized static void cancelUserAttentionRequest(int request) {
        // Taskbar user-attention requests clear automatically when the app
        // becomes active again.
    }
}
