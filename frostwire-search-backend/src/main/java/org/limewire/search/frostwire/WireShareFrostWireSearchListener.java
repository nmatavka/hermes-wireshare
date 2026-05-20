package org.limewire.search.frostwire;

import java.util.Collection;

import org.limewire.core.api.search.SearchResult;

public interface WireShareFrostWireSearchListener {
    void onResults(Collection<? extends SearchResult> results);

    void onProviderStopped(WireShareFrostWireSearchProvider provider);

    void onProviderError(WireShareFrostWireSearchProvider provider, String message);
}
