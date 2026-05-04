package org.limewire.core.impl.browse;

import org.limewire.core.api.browse.BrowseFactory;
import org.limewire.inject.FactoryModules;

import com.google.inject.AbstractModule;

public class CoreGlueBrowseModule extends AbstractModule {
    
    @Override
    protected void configure() {
        install(FactoryModules.newFactory(BrowseFactory.class, CoreBrowse.class));
    }

}
