package org.limewire.ui.swing.wizard;

import org.limewire.inject.FactoryModules;

import com.google.inject.AbstractModule;

public class LimeWireUiWizardModule extends AbstractModule {

    @Override
    protected void configure() {
        
        install(FactoryModules.newFactory(SetupComponentDecoratorFactory.class, SetupComponentDecorator.class));  
    }

}
