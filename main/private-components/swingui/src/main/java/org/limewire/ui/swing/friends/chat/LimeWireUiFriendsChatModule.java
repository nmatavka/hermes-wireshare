package org.limewire.ui.swing.friends.chat;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;

public class LimeWireUiFriendsChatModule extends AbstractModule {
    
    @Override
    protected void configure() {
        install(new FactoryModuleBuilder().build(ChatHyperlinkListenerFactory.class));
        install(new FactoryModuleBuilder().build(ConversationPaneFactory.class));
    }
}
