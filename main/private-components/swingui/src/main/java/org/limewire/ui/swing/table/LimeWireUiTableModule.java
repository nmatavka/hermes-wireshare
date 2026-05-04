package org.limewire.ui.swing.table;

import org.limewire.inject.AbstractModule;
import org.limewire.inject.FactoryModules;

public class LimeWireUiTableModule extends AbstractModule {

    @Override
    protected void configure() {
        install(FactoryModules.newFactory(IconLabelRendererFactory.class, IconLabelRenderer.class));
    }
}
