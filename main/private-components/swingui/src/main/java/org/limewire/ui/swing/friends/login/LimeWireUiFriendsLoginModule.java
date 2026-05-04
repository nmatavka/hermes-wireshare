package org.limewire.ui.swing.friends.login;

import org.limewire.inject.FactoryModules;

import com.google.inject.AbstractModule;

public class LimeWireUiFriendsLoginModule extends AbstractModule {
    
    @Override
    protected void configure() {
        install(FactoryModules.newFactory(XMPPUserEntryLoginPanelFactory.class, XMPPUserEntryLoginPanel.class));
        bind(AutoLoginService.class);
    }

}
