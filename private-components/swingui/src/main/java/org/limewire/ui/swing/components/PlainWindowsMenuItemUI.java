package org.limewire.ui.swing.components;

import java.awt.Color;

//import com.sun.java.swing.plaf.windows.WindowsMenuItemUI;
import javax.swing.plaf.basic.BasicMenuItemUI;

@SuppressWarnings("restriction")
//public class PlainWindowsMenuItemUI extends WindowsMenuItemUI {
public class PlainWindowsMenuItemUI extends BasicMenuItemUI {
    private static Color originalSelectionBackground;
    private static Color originalSelectionForeground;

    public static void overrideDefaults(Color selectionForeground, Color selectionBackground) {
        originalSelectionForeground = selectionForeground;
        originalSelectionBackground = selectionBackground;
    }
    
    public PlainWindowsMenuItemUI() {
    }

    @Override
    public void installDefaults() {
        super.installDefaults();
        selectionForeground = originalSelectionForeground;
        selectionBackground = originalSelectionBackground;
    }
}
