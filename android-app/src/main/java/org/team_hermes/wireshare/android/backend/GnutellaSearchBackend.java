package org.team_hermes.wireshare.android.backend;

public final class GnutellaSearchBackend implements SearchBackend {
    public static final String ID = "gnutella";

    @Override
    public String id() {
        return ID;
    }

    @Override
    public boolean isEnabled() {
        return false;
    }

    @Override
    public SearchSession search(String query, SearchBackendListener listener) {
        listener.onError(ID, new UnsupportedOperationException("Gnutella is not wired into the Android runtime yet."));
        return () -> {
        };
    }
}
