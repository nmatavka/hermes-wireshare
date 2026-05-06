package org.limewire.ui.desktop.shell;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

class LinuxXdgAssociation implements ShellAssociation {

    private final List<String> mimeTypes;

    LinuxXdgAssociation(String... mimeTypes) {
        this(Arrays.asList(mimeTypes));
    }

    LinuxXdgAssociation(Collection<String> mimeTypes) {
        this.mimeTypes = Collections.unmodifiableList(new ArrayList<String>(mimeTypes));
    }

    @Override
    public boolean isRegistered() {
        return LinuxXdgAssociationSupport.isRegistered(mimeTypes);
    }

    @Override
    public boolean isAvailable() {
        return LinuxXdgAssociationSupport.isAvailable(mimeTypes);
    }

    @Override
    public void register() {
        LinuxXdgAssociationSupport.register(mimeTypes);
    }

    @Override
    public boolean canUnregister() {
        return true;
    }

    @Override
    public void unregister() {
        LinuxXdgAssociationSupport.unregister(mimeTypes);
    }
}
