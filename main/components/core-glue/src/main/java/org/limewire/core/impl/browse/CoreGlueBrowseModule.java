package org.limewire.core.impl.browse;

import org.limewire.core.api.browse.BrowseFactory;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;

public class CoreGlueBrowseModule extends AbstractModule {
    
    @Override
    protected void configure() {
        install(new FactoryModuleBuilder()
                .implement(org.limewire.core.api.browse.Browse.class, CoreBrowse.class)
                .build(BrowseFactory.class));
    }

}
