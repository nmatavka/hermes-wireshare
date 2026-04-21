package org.limewire.rest;

import org.limewire.rest.oauth.OAuthValidatorFactory;
import org.limewire.rest.oauth.OAuthValidatorImpl;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;

/**
 * Guice module to configure the REST API components.
 */
public class LimeWireRestModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(RestRequestHandlerFactory.class).to(RestRequestHandlerFactoryImpl.class);
        
        install(new FactoryModuleBuilder().build(AuthorizationInterceptorFactory.class));
        install(new FactoryModuleBuilder()
                .implement(RestAuthority.class, RestAuthorityImpl.class)
                .build(RestAuthorityFactory.class));
        install(new FactoryModuleBuilder()
                .implement(org.limewire.rest.oauth.OAuthValidator.class, OAuthValidatorImpl.class)
                .build(OAuthValidatorFactory.class));
    }

}
