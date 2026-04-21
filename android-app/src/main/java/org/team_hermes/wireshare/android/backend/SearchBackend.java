package org.team_hermes.wireshare.android.backend;

/**
 * Protocol-neutral Android search entrypoint.
 */
public interface SearchBackend {
    String id();

    boolean isEnabled();

    SearchSession search(String query, SearchBackendListener listener);
}
