package org.limewire.ui.swing.properties;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;


public class LimeWireUiPropertiesModule extends AbstractModule {

    @Override
    protected void configure() {
        install(new FactoryModuleBuilder().build(FileInfoDialogFactory.class));
                
        bind(FileInfoPanelFactory.class).to(FileInfoPanelFactoryImpl.class);
    }
}
