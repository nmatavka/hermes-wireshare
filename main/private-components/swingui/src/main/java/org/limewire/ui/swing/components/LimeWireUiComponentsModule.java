package org.limewire.ui.swing.components;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;

/**
 * Module to configure Guice bindings for the UI components.
 */
public class LimeWireUiComponentsModule extends AbstractModule {
    
    /**
     * Configures the bindings for the UI components.
     */
    @Override
    protected void configure() {
        install(new FactoryModuleBuilder().build(FlexibleTabListFactory.class));
        
//        bind(ShapeDialog.class);
        
        install(new FactoryModuleBuilder().build(RemoteHostWidgetFactory.class));
    }
}
