package org.team_hermes.wireshare.android.backend;

import java.util.List;

public interface SearchBackendListener {
    void onResults(String backendId, List<SearchResultModel> results);

    void onStopped(String backendId);

    void onError(String backendId, Throwable error);
}
