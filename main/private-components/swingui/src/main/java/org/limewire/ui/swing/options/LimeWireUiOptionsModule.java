package org.limewire.ui.swing.options;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;

public class LimeWireUiOptionsModule extends AbstractModule {

    @Override
    protected void configure() {
        install(new FactoryModuleBuilder().build(ManageSaveFoldersOptionPanelFactory.class));
    }
}
