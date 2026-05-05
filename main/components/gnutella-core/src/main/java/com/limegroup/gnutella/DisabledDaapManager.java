package com.limegroup.gnutella;

import java.io.IOException;

import org.limewire.inject.EagerSingleton;

/**
 * DAAP sharing is retired from the active runtime. Keep a no-op service bound
 * so legacy settings surfaces can still resolve the dependency without pulling
 * the old DAAP server stack back onto the classpath.
 */
@EagerSingleton
public class DisabledDaapManager implements org.limewire.core.api.daap.DaapManager {

    @Override
    public boolean isServerRunning() {
        return false;
    }

    @Override
    public void restart() throws IOException {
    }

    @Override
    public void updateService() throws IOException {
    }

    @Override
    public void stop() {
    }

    @Override
    public void disconnectAll() {
    }

    @Override
    public void start() throws IOException {
    }
}
