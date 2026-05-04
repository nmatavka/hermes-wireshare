package org.limewire.ui.swing.properties;

import org.limewire.inject.FactoryModules;

import com.google.inject.AbstractModule;


public class LimeWireUiPropertiesModule extends AbstractModule {

    @Override
    protected void configure() {
        install(FactoryModules.newFactory(FileInfoDialogFactory.class, FileInfoDialog.class));
                
        bind(FileInfoPanelFactory.class).to(FileInfoPanelFactoryImpl.class);
    }
}
