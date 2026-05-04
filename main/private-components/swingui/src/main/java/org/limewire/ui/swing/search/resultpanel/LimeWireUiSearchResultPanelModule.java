package org.limewire.ui.swing.search.resultpanel;

import org.limewire.inject.FactoryModules;

import com.google.inject.AbstractModule;

public class LimeWireUiSearchResultPanelModule extends AbstractModule {

    @Override
    protected void configure() {
        install(FactoryModules.newFactory(SearchResultMenuFactory.class, SearchResultMenu.class));
    }

}
