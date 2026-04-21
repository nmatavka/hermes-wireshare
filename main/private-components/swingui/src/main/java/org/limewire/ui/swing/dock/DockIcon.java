package org.limewire.ui.swing.dock;

import java.awt.Component;
import java.awt.Graphics;

import javax.swing.Icon;

/**
 * A Dock Icon for LimeWire. Simple interface.
 */
public interface DockIcon extends Icon {
    
    @Override
	public int getIconWidth();
    @Override
	public int getIconHeight();
    public void draw(int complete);
    @Override
	public void paintIcon(Component c, Graphics g, int x, int y);
    
}
