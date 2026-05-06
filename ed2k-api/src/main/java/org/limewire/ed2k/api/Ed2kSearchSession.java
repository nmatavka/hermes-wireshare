package org.limewire.ed2k.api;

import java.util.List;

import org.limewire.core.api.search.GroupedSearchResult;

public interface Ed2kSearchSession {

    String getId();

    String getQuery();

    boolean isRunning();

    boolean isFailed();

    String getFailureMessage();

    List<GroupedSearchResult> getResults();
}
