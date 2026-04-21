package org.limewire.ui.swing.util;

public abstract class NonBlockFileIconController implements FileIconController {
    
    /** This controller is always valid. */
    @Override
	public boolean isValid() {
        return true;
    }
    
}
