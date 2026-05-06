package org.limewire.ui.desktop.shell;

import org.limewire.ui.desktop.util.MacOSXUtils;

class OSXFileTypeAssociation implements ShellAssociation {
	private final String extension;
	
	protected OSXFileTypeAssociation(String extension) {
	    this.extension = extension;
	}
	
	public static boolean isNativeLibraryLoadedCorrectly() {
	    return MacOSXUtils.isNativeLibraryLoadedCorrectly();
	}
	
	@Override
	public boolean isAvailable() {
	    return !MacOSXUtils.isFileTypeHandled(extension);
	}

	@Override
	public boolean isRegistered() {
        return MacOSXUtils.isLimewireDefaultFileTypeHandler(extension);
	}

    @Override
    public void register() {
        MacOSXUtils.setLimewireAsDefaultFileTypeHandler(extension);
    }

    @Override
	public boolean canUnregister() {
        return MacOSXUtils.canChangeDefaultFileTypeHandler(extension);
    }
    
    @Override
    public void unregister() {
        // There is no way to unregister a file type association on OS-X except
        // completely destroying and recreating the registration database,
        // which would wipe out ALL file associations.
        // So, we'll try to set another registered handler for this file
        // type as the default handler.
        MacOSXUtils.tryChangingDefaultFileTypeHandler(extension);
    }

}
