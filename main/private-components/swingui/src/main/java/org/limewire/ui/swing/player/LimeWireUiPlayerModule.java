package org.limewire.ui.swing.player;

import org.limewire.inject.FactoryModules;
import org.limewire.util.OSUtils;

import com.google.inject.AbstractModule;

public class LimeWireUiPlayerModule extends AbstractModule {

    @Override
    protected void configure() {
        if(OSUtils.isLinux())
            bind(PlayerMediator.class).to(AudioPlayerMediator.class);
        else
            bind(PlayerMediator.class).to(PlayerMediatorImpl.class);
        install(FactoryModules.newFactory(VideoPanelFactory.class, VideoPanel.class));
        bind(MediaPlayerFactory.class).to(MediaPlayerFactoryImpl.class);
    }

}
