package org.limewire.ui.swing.options;

import org.limewire.inject.FactoryModules;

import com.google.inject.AbstractModule;

public class LimeWireUiOptionsModule extends AbstractModule {

    @Override
    protected void configure() {
        
        install(FactoryModules.newFactory(ManageSaveFoldersOptionPanelFactory.class, ManageSaveFoldersOptionPanel.class));   
    }
}
