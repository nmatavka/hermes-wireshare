package org.limewire.ui.swing.search.resultpanel;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;

public class LimeWireUiSearchResultPanelModule extends AbstractModule {

    @Override
    protected void configure() {
        install(new FactoryModuleBuilder().build(SearchResultMenuFactory.class));
    }

}
