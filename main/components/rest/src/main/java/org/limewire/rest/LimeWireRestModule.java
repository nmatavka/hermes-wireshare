package org.limewire.rest;

import org.limewire.rest.oauth.OAuthValidatorFactory;
import org.limewire.rest.oauth.OAuthValidatorImpl;
import org.limewire.inject.FactoryModules;

import com.google.inject.AbstractModule;

/**
 * Guice module to configure the REST API components.
 */
public class LimeWireRestModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(RestRequestHandlerFactory.class).to(RestRequestHandlerFactoryImpl.class);
        
        install(FactoryModules.newFactory(AuthorizationInterceptorFactory.class, AuthorizationInterceptor.class));
        install(FactoryModules.newFactory(RestAuthorityFactory.class, RestAuthorityImpl.class));
        install(FactoryModules.newFactory(OAuthValidatorFactory.class, OAuthValidatorImpl.class));
    }

}
