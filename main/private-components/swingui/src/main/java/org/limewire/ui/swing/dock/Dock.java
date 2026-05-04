package org.limewire.ui.swing.dock;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Taskbar;
import java.awt.image.BufferedImage;
import java.net.URL;

import javax.swing.Icon;
import javax.swing.JPanel;

import org.limewire.util.OSUtils;

/**
 * A utility class to modify the Dock Icon and request user attention.
 */
public class Dock {

    private static final Taskbar TASKBAR;
    private static final boolean HAS_DOCK;
    private static final Image BASE_IMAGE;

    static {
        Taskbar taskbar = null;
        boolean hasDock = false;
        if (OSUtils.isMacOSX() && Taskbar.isTaskbarSupported()) {
            taskbar = Taskbar.getTaskbar();
            hasDock = taskbar.isSupported(Taskbar.Feature.ICON_IMAGE);
        }
        TASKBAR = taskbar;
        HAS_DOCK = hasDock;
        BASE_IMAGE = hasDock ? loadBaseImage(taskbar) : null;
    }

    public static enum AttentionType {
        CRITICAL(true),
        INFORMATIONAL(false);

        private final boolean critical;

        private AttentionType(boolean critical) {
            this.critical = critical;
        }
    }

    public static final int ICON_WIDTH = 128;
    public static final int ICON_HEIGHT = 128;

    private Dock() {
    }

    public static Object getDockLock() {
        return Dock.class;
    }

    public synchronized static void setDockTileImage(Icon icon) {
        setTaskbarImage(icon, false);
    }

    public synchronized static void setDockTileOverlayImage(Icon icon) {
        setTaskbarImage(icon, true);
    }

    private synchronized static void setTaskbarImage(Icon icon, boolean overlay) {
        if (!HAS_DOCK || icon == null) {
            return;
        }

        BufferedImage image = new BufferedImage(ICON_WIDTH, ICON_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        try {
            if (overlay && BASE_IMAGE != null) {
                g.drawImage(BASE_IMAGE, 0, 0, ICON_WIDTH, ICON_HEIGHT, null);
            }

            Component panel = new JPanel();
            Dimension iconSize = new Dimension(ICON_WIDTH, ICON_HEIGHT);
            panel.setSize(iconSize);
            panel.setPreferredSize(iconSize);
            panel.setMinimumSize(iconSize);
            panel.setMaximumSize(iconSize);
            icon.paintIcon(panel, g, 0, 0);
        } finally {
            g.dispose();
        }

        TASKBAR.setIconImage(image);
    }

    public synchronized static void restoreDockTileImage() {
        if (HAS_DOCK && BASE_IMAGE != null) {
            TASKBAR.setIconImage(BASE_IMAGE);
        }
    }

    public synchronized static int requestUserAttention(AttentionType requestType) {
        if (HAS_DOCK && TASKBAR.isSupported(Taskbar.Feature.USER_ATTENTION)) {
            TASKBAR.requestUserAttention(true, requestType.critical);
            return 1;
        }
        return -1;
    }

    public synchronized static void cancelUserAttentionRequest(int request) {
        if (HAS_DOCK && request >= 0 && TASKBAR.isSupported(Taskbar.Feature.USER_ATTENTION)) {
            TASKBAR.requestUserAttention(false, false);
        }
    }

    private static Image loadBaseImage(Taskbar taskbar) {
        Image iconImage = taskbar.getIconImage();
        if (iconImage != null) {
            return iconImage;
        }

        URL resource = Dock.class.getResource("/org/limewire/ui/swing/mainframe/resources/icons/lime.png");
        if (resource == null) {
            return null;
        }

        return java.awt.Toolkit.getDefaultToolkit().createImage(resource);
    }
}
