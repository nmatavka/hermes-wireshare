package org.limewire.ui.swing.player;

import org.limewire.util.OSUtils;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;

public class LimeWireUiPlayerModule extends AbstractModule {

    @Override
    protected void configure() {
        if(OSUtils.isLinux())
            bind(PlayerMediator.class).to(AudioPlayerMediator.class);
        else
            bind(PlayerMediator.class).to(PlayerMediatorImpl.class);
        install(new FactoryModuleBuilder().build(VideoPanelFactory.class));
        bind(MediaPlayerFactory.class).to(MediaPlayerFactoryImpl.class);
    }

}
