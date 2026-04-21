package org.limewire.ui.swing.dock;

import java.awt.Component;
import java.awt.Graphics;

/**
 * For platforms that are not Mac OS X. I've heard
 * rumors of their existence, but I honestly don't
 * believe that sort of thing.
 *
 */
class DockIconNoOpImpl implements DockIcon {

    @Override
	public int getIconHeight() { return 0; }
    @Override
	public int getIconWidth() { return 0; }
    @Override
	public void paintIcon(Component c, Graphics g, int x, int y) {}
    @Override
	public void draw (int complete) { }
    
}
