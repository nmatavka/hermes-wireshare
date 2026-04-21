package org.limewire.ui.swing.table;

import org.limewire.inject.AbstractModule;

import com.google.inject.assistedinject.FactoryModuleBuilder;

public class LimeWireUiTableModule extends AbstractModule {

    @Override
    protected void configure() {
        install(new FactoryModuleBuilder().build(IconLabelRendererFactory.class));
    }
}
