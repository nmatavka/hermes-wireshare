package org.limewire.ui.swing.tray;

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.EventObject;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

import javax.swing.ActionMap;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.jdesktop.application.Application;
import org.jdesktop.application.Resource;
import org.limewire.ui.swing.settings.SwingUiSettings;
import org.limewire.ui.swing.util.GuiUtils;
import org.limewire.ui.swing.util.I18n;
import org.limewire.util.OSUtils;

/**
 * Puts an icon and menu in the system tray. Delegates System Notifications to
 * an Internal BasicNotifier.
 */
class SystemTrayNotifier implements TrayNotifier {

    private final SystemTray tray;

    private final TrayIcon trayIcon;

    private final PopupMenu popupMenu;


    @Resource
    private Icon windowsIconResource16;
    @Resource
    private Icon windowsIconResource32;
    @Resource
    private Icon windowsIconResource48;
    @Resource
    private Icon linuxIconResource16;
    @Resource
    private Icon linuxIconResource24;
    @Resource
    private Icon linuxIconResource32;
    @Resource
    private Icon linuxIconResource48;

    public SystemTrayNotifier() {

        if (SystemTray.isSupported()) {
            GuiUtils.assignResources(this);
            tray = SystemTray.getSystemTray();
            popupMenu = buildPopupMenu();
            trayIcon = buildTrayIcon("WireShare");
        } else {
            tray = null;
            trayIcon = null;
            popupMenu = null;
        }
    }

    private TrayIcon buildTrayIcon(String desc) {
        Icon icon = getIcon();
        TrayIcon trayIcon = new TrayIcon(((ImageIcon) icon).getImage(), desc, popupMenu);

        // left click restores. This happens on the awt thread.
        trayIcon.addActionListener(new ActionListener() {
            @Override
			public void actionPerformed(ActionEvent e) {
                ActionMap map = Application.getInstance().getContext().getActionManager()
                        .getActionMap();
                map.get("restoreView").actionPerformed(e);
            }
        });

        trayIcon.setImageAutoSize(true);
        return trayIcon;
    }

    private Icon getIcon() {
        Dimension iconSize = SystemTray.getSystemTray().getTrayIconSize();
        if(iconSize == null || iconSize.getWidth() <= 16) {
            return OSUtils.isWindows() ? windowsIconResource16 : linuxIconResource16;
        } else if(iconSize.getWidth() <= 24) {
            return OSUtils.isWindows() ? windowsIconResource16 : linuxIconResource24;
        } else if(iconSize.getWidth() <= 32) {
            return OSUtils.isWindows() ? windowsIconResource32 : linuxIconResource32;
        } else {
            return OSUtils.isWindows() ? windowsIconResource48 : linuxIconResource48;
        }
    }

    private PopupMenu buildPopupMenu() {
        PopupMenu menu = new PopupMenu();

        // restore
        MenuItem item = new MenuItem(I18n.tr("Show WireShare"));
        item.addActionListener(new ActionListener() {
            @Override
			public void actionPerformed(ActionEvent e) {
                ActionMap map = Application.getInstance().getContext().getActionManager()
                        .getActionMap();
                map.get("restoreView").actionPerformed(e);
            }
        });
        menu.add(item);

        menu.addSeparator();

        // exit after transfers
        item = new MenuItem(I18n.tr("Exit After Transfers"));
        item.addActionListener(new ActionListener() {
            @Override
			public void actionPerformed(ActionEvent e) {
                ActionMap map = Application.getInstance().getContext().getActionManager()
                        .getActionMap();
                map.get("shutdownAfterTransfers").actionPerformed(e);
            }
        });
        menu.add(item);

        // exit
        item = new MenuItem(I18n.tr("Exit"));
        item.addActionListener(new ActionListener() {
            @Override
			public void actionPerformed(ActionEvent e) {
                Application.getInstance().exit(e);
            }
        });
        menu.add(item);

        return menu;
    }

    @Override
    public boolean isExitEvent(EventObject event) {
        if (!SwingUiSettings.MINIMIZE_TO_TRAY.getValue()) {
            return true;
        }

        if ((event != null) && (event.getSource() instanceof MenuItem)) {
            // Return true on exit from system tray popup menu.
            MenuItem item = (MenuItem) event.getSource();
            return item.getParent() == popupMenu;

        } else if (event instanceof ActionEvent) {
            // Return true on action to shutdown application.
            return "Shutdown".equals(((ActionEvent) event).getActionCommand());

        } else {
            return false;
        }
    }

    @Override
	public boolean showTrayIcon() {
        if (tray == null) {
            return false;
        }

        try {
            tray.add(trayIcon);
        } catch (AWTException e) {
            return false;
        } catch (IllegalArgumentException iae) {
            return false;
        }

        return true;
    }

    @Override
	public boolean supportsSystemTray() {
        return trayIcon != null && tray != null;
    }

    @Override
	public void hideTrayIcon() {
        if (tray != null) {
            tray.remove(trayIcon);
        }
    }

    @Override
	public void showMessage(Notification notification) {
        if (OSUtils.isWindows()) {
            try {
                Image image = Toolkit.getDefaultToolkit().getImage("check.png");
                SystemTray systemTray = SystemTray.getSystemTray();
                TrayIcon trayIconImage = new TrayIcon(image, "Tray Demo");
                trayIconImage.setImageAutoSize(true);
                systemTray.add(trayIconImage);
                trayIconImage.displayMessage(notification.getTitle(), notification.getMessage(), TrayIcon.MessageType.INFO);
            } catch(Exception e) {
                Logger.getLogger(SystemTrayNotifier.class.getName()).log(Level.SEVERE, null, e);
            }
            }
            else if (OSUtils.isMacOSX()) {
                // needs an icon!
                try {
                    ProcessBuilder builder = new ProcessBuilder(
                            "osascript", "-e",
                            "display notification \"" + notification.getMessage() + "\""
                                    + " with title \"" + notification.getTitle() + "\"");                  
				   builder.inheritIO().start();
                } catch (IOException ex) {
                    Logger.getLogger(SystemTrayNotifier.class.getName()).log(Level.SEVERE, null, ex);
                }
        }
            else
                try {
                    ProcessBuilder builder = new ProcessBuilder(
                                                                "zenity",
                                                                "--notification",
                                                                "--window-icon=org.team_hermes.XferDone",
                                                                "--title=" + notification.getTitle(),
                                                                "--text=" + notification.getMessage());
                    
                    builder.inheritIO().start();
                } catch (IOException ex) {
                    Logger.getLogger(SystemTrayNotifier.class.getName()).log(Level.SEVERE, null, ex);
                } try {Thread.sleep(2500);}
        catch(InterruptedException e)
            {
                // this part is executed when an exception (in this example InterruptedException) occurs
            }
    }
}
