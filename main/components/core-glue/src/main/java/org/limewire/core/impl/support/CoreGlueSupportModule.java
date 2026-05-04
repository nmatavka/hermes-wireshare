package org.limewire.core.impl.support;

import org.limewire.core.api.support.LocalClientInfoFactory;
import org.limewire.core.api.support.SessionInfo;
import org.limewire.inject.FactoryModules;

import com.google.inject.AbstractModule;

/**
 * Guice module to configure the Support API for the live core. 
 */
public class CoreGlueSupportModule extends AbstractModule {

    /**
     * Configures Support API for the live core. 
     */
    @Override
    protected void configure() {
        bind(SessionInfo.class).to(LimeSessionInfo.class);
        install(FactoryModules.newFactory(LocalClientInfoFactory.class, LocalClientInfoImpl.class));
    }

}
