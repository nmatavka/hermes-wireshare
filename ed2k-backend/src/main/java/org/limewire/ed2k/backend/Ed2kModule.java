package org.limewire.ed2k.backend;

import org.limewire.ed2k.api.Ed2kService;

import com.google.inject.AbstractModule;

public class Ed2kModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(Ed2kService.class).to(Ed2kServiceImpl.class);
    }
}
