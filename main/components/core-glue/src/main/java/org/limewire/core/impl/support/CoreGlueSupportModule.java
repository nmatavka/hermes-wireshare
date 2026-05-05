package org.limewire.core.impl.support;

import org.limewire.core.api.support.LocalClientInfoFactory;
import org.limewire.core.api.support.SessionInfo;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;

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
        install(new FactoryModuleBuilder()
                .implement(org.limewire.core.api.support.LocalClientInfo.class,
                        LocalClientInfoImpl.class)
                .build(LocalClientInfoFactory.class));
    }

}
