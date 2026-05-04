package org.limewire.ui.swing.components;

import org.limewire.inject.FactoryModules;

import com.google.inject.AbstractModule;

/**
 * Module to configure Guice bindings for the UI components.
 */
public class LimeWireUiComponentsModule extends AbstractModule {
    
    /**
     * Configures the bindings for the UI components.
     */
    @Override
    protected void configure() {
        install(FactoryModules.newFactory(FlexibleTabListFactory.class, FlexibleTabList.class));   
        
//        bind(ShapeDialog.class);
        
        install(FactoryModules.newFactory(RemoteHostWidgetFactory.class, RemoteHostWidget.class)); 
    }
}
