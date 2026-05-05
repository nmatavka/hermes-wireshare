package org.team_hermes.wireshare.android.backend;

import org.team_hermes.wireshare.android.gui.SearchMediator;
import org.team_hermes.wireshare.android.util.SystemUtils;
import org.team_hermes.wireshare.search.SearchError;
import org.team_hermes.wireshare.search.FileSearchResult;
import org.team_hermes.wireshare.search.SearchListener;
import org.team_hermes.wireshare.search.SearchResult;
import org.team_hermes.wireshare.search.torrent.TorrentSearchResult;

import java.util.ArrayList;
import java.util.List;

public final class TorrentSearchBackend implements SearchBackend {
    public static final String ID = "torrent";

    @Override
    public String id() {
        return ID;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public SearchSession search(String query, SearchBackendListener listener) {
        SearchMediator mediator = SearchMediator.instance();
        mediator.setSearchListener(new SearchListener() {
            @Override
            public void onResults(long token, List<? extends SearchResult> results) {
                listener.onResults(ID, toModels(results));
            }

            @Override
            public void onStopped(long token) {
                listener.onStopped(ID);
            }

            @Override
            public void onError(long token, SearchError error) {
                listener.onError(ID, new IllegalStateException(String.valueOf(error)));
            }
        });
        SystemUtils.postToHandler(SystemUtils.HandlerThreadName.SEARCH_PERFORMER, () -> mediator.performSearch(query));
        return () -> SystemUtils.postToHandler(SystemUtils.HandlerThreadName.SEARCH_PERFORMER, mediator::cancelSearch);
    }

    private static List<SearchResultModel> toModels(List<? extends SearchResult> results) {
        List<SearchResultModel> models = new ArrayList<>(results.size());
        for (SearchResult result : results) {
            String protocol = result instanceof TorrentSearchResult ? "bittorrent" : "http";
            models.add(new SearchResultModel(
                    ID,
                    protocol,
                    result.getDisplayName(),
                    result.getSource(),
                    result.getDetailsUrl(),
                    result instanceof FileSearchResult ? ((FileSearchResult) result).getSize() : -1
            ));
        }
        return models;
    }
}
