package org.limewire.ui.desktop.shell;

public class MagnetAssociation implements ShellAssociation {

	/** The extension for magnet: links, "magnet", without punctuation. */
	private static final String MAGNET_EXTENSION = "magnet";
	/** The name of the magnet: link protocol, "Magnet Protocol". */
	private static final String MAGNET_PROTOCOL = "Magnet Protocol";
	
	private final ShellAssociation protocol, handler;
	
	public MagnetAssociation(String program, String executable) {
		protocol = new WindowsProtocolShellAssociation(executable,
				MAGNET_EXTENSION,
				MAGNET_PROTOCOL);
		handler = new WindowsMagnetHandlerAssociation(program, executable);
	}
	
	@Override
	public boolean isAvailable() {
		return protocol.isAvailable();
	}

	@Override
	public boolean isRegistered() {
		return protocol.isRegistered();
	}

    @Override
	public boolean canUnregister() {
        return true;
    }

    @Override
	public void register() {
		protocol.register();
		handler.register();
	}

	@Override
	public void unregister() {
		protocol.unregister();
		if (handler.isRegistered())
			handler.unregister();
	}
}
