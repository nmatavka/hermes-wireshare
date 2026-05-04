package org.limewire.ui.swing.friends.chat;

import org.limewire.inject.FactoryModules;

import com.google.inject.AbstractModule;

public class LimeWireUiFriendsChatModule extends AbstractModule {
    
    @Override
    protected void configure() {
        install(FactoryModules.newFactory(ChatHyperlinkListenerFactory.class, ChatHyperlinkListener.class));        
        install(FactoryModules.newFactory(ConversationPaneFactory.class, ConversationPane.class));
    }
}
