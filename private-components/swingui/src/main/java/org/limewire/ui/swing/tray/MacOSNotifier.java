package org.limewire.ui.swing.tray;

import java.util.EventObject;
import com.sun.jna.Library;
import com.sun.jna.Native;
import org.limewire.ui.swing.util.I18n;

class MacOSNotifier implements TrayNotifier {
	
	private static final int timedelayshow = 0;
	
	private NsUserNotificationsBridge notifier;
	
    public MacOSNotifier() {
    	notifier = NsUserNotificationsBridge.instance;
    }
    
    interface NsUserNotificationsBridge extends Library {
    	NsUserNotificationsBridge instance = (NsUserNotificationsBridge)
    			Native.loadLibrary("NsUserNotificationsBridge", NsUserNotificationsBridge.class);

    	public int sendNotification(String title, String subtitle, String text, int timedelayshow);
    }
    
    @Override
	public boolean supportsSystemTray() {
        return false;
    }

    @Override
	public boolean showTrayIcon() {
        return false;
    }

    @Override
	public void hideTrayIcon() {
        
    }

    @Override
	public void showMessage(Notification notification) {
        String title = notification.getTitle() != null ? notification.getTitle() : I18n.tr("WireShare"); 
        notifier.sendNotification(title, "", notification.getMessage(), timedelayshow );
    }
    
    @Override
	public void hideMessage(Notification notification) {
        
    }

    public void updateUI() {
        
    }
    
    @Override
    public boolean isExitEvent(EventObject event) {
        return false;
    }
}
