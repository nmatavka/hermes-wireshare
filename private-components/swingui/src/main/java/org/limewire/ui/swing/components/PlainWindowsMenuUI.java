package org.limewire.ui.swing.components;

import java.awt.Color;

//import com.sun.java.swing.plaf.windows.WindowsMenuUI;
import javax.swing.plaf.basic.BasicMenuUI;

@SuppressWarnings("restriction")
//public class PlainWindowsMenuUI extends WindowsMenuUI {
public class PlainWindowsMenuUI extends BasicMenuUI {
    private static Color originalSelectionBackground;
    private static Color originalSelectionForeground;

    public static void overrideDefaults(Color selectionForeground, Color selectionBackground) {
        originalSelectionForeground = selectionForeground;
        originalSelectionBackground = selectionBackground;
    }
    
    public PlainWindowsMenuUI() {
    }

    @Override
    public void installDefaults() {
        super.installDefaults();
        selectionForeground = originalSelectionForeground;
        selectionBackground = originalSelectionBackground;
    }
}
