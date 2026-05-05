package org.limewire.ui.swing.friends.login;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;

public class LimeWireUiFriendsLoginModule extends AbstractModule {
    
    @Override
    protected void configure() {
        install(new FactoryModuleBuilder().build(XMPPUserEntryLoginPanelFactory.class));
        bind(AutoLoginService.class);
    }

}
