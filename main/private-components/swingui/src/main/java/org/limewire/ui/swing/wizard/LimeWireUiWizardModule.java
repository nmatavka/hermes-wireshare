package org.limewire.ui.swing.wizard;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;

public class LimeWireUiWizardModule extends AbstractModule {

    @Override
    protected void configure() {
        install(new FactoryModuleBuilder().build(SetupComponentDecoratorFactory.class));
    }

}
