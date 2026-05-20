package org.limewire.search.frostwire;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.limewire.core.api.search.SearchCategory;
import org.limewire.core.api.search.SearchResult;
import org.limewire.core.settings.SearchSettings;
import org.limewire.search.frostwire.search.CompositeFileSearchResult;
import org.limewire.search.frostwire.search.FileSearchResult;
import org.limewire.search.frostwire.search.HttpSearchResult;
import org.limewire.search.frostwire.search.ISearchPerformer;
import org.limewire.search.frostwire.search.SearchError;
import org.limewire.search.frostwire.search.SearchListener;
import org.limewire.search.frostwire.search.StreamableSearchResult;
import org.limewire.search.frostwire.search.torrent.TorrentSearchResult;

public final class WireShareFrostWireSearchBackend {
    private static final AtomicLong TOKENS = new AtomicLong(1L);
    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(6, new ThreadFactory() {
        private final AtomicLong ids = new AtomicLong(1L);

        @Override
        public Thread newThread(Runnable runnable) {
            Thread thread = new Thread(runnable, "WireShare-FrostWire-search-" + ids.getAndIncrement());
            thread.setDaemon(true);
            return thread;
        }
    });

    public WireShareFrostWireSearchSession search(String query, SearchCategory category, WireShareFrostWireSearchListener listener) {
        if (!SearchSettings.USE_FROSTWIRE_WEB_SEARCH.getValue()) {
            return WireShareFrostWireSearchSession.empty();
        }
        List<ISearchPerformer> performers = new ArrayList<ISearchPerformer>();
        long token = TOKENS.getAndIncrement();
        for (WireShareFrostWireSearchProvider provider : WireShareFrostWireSearchProvider.values()) {
            if (!provider.isEnabled() || !provider.supports(category)) {
                continue;
            }
            try {
                ISearchPerformer performer = provider.createPerformer(token, query);
                performer.setListener(new Adapter(provider, listener));
                performers.add(performer);
                EXECUTOR.execute(new PerformerTask(provider, performer, listener));
            } catch (Throwable throwable) {
                listener.onProviderError(provider, throwable.getMessage());
            }
        }
        return new WireShareFrostWireSearchSession(performers);
    }

    private static final class Adapter implements SearchListener {
        private final WireShareFrostWireSearchProvider provider;
        private final WireShareFrostWireSearchListener listener;

        Adapter(WireShareFrostWireSearchProvider provider, WireShareFrostWireSearchListener listener) {
            this.provider = provider;
            this.listener = listener;
        }

        @Override
        public void onResults(long token, List<? extends org.limewire.search.frostwire.search.SearchResult> results) {
            List<SearchResult> adapted = new ArrayList<SearchResult>(results.size());
            for (org.limewire.search.frostwire.search.SearchResult result : results) {
                if (result instanceof FileSearchResult) {
                    addResolvedFileResult(adapted, (FileSearchResult) result);
                }
            }
            if (!adapted.isEmpty()) {
                listener.onResults(adapted);
            }
        }

        private void addResolvedFileResult(List<SearchResult> adapted, FileSearchResult result) {
            if (result instanceof CompositeFileSearchResult) {
                CompositeFileSearchResult composite = (CompositeFileSearchResult) result;
                if (composite.getCrawledChildren().isPresent() && !composite.getCrawledChildren().get().isEmpty()) {
                    for (FileSearchResult child : composite.getCrawledChildren().get()) {
                        addResolvedFileResult(adapted, child);
                    }
                    return;
                }
            }
            if (isDownloadable(result)) {
                adapted.add(new WireShareFrostWireSearchResult(provider, result));
            }
        }

        private boolean isDownloadable(FileSearchResult result) {
            if (result == null || result.isPreliminary()) {
                return false;
            }
            if (result instanceof CompositeFileSearchResult) {
                CompositeFileSearchResult composite = (CompositeFileSearchResult) result;
                return hasText(composite.getTorrentUrl().orElse(null)) || hasText(composite.getStreamUrl().orElse(null));
            }
            if (result instanceof TorrentSearchResult) {
                return hasText(((TorrentSearchResult) result).getTorrentUrl());
            }
            return result instanceof HttpSearchResult || result instanceof StreamableSearchResult;
        }

        private boolean hasText(String value) {
            return value != null && !value.trim().isEmpty();
        }

        @Override
        public void onStopped(long token) {
            listener.onProviderStopped(provider);
        }

        @Override
        public void onError(long token, SearchError error) {
            listener.onProviderError(provider, error == null ? null : error.message());
        }
    }

    private static final class PerformerTask implements Runnable {
        private final WireShareFrostWireSearchProvider provider;
        private final ISearchPerformer performer;
        private final WireShareFrostWireSearchListener listener;

        PerformerTask(WireShareFrostWireSearchProvider provider, ISearchPerformer performer, WireShareFrostWireSearchListener listener) {
            this.provider = provider;
            this.performer = performer;
            this.listener = listener;
        }

        @Override
        public void run() {
            try {
                performer.perform();
            } catch (Throwable throwable) {
                listener.onProviderError(provider, throwable.getMessage());
            }
        }
    }

    public static final class WireShareFrostWireSearchSession {
        private final List<ISearchPerformer> performers;
        private final AtomicBoolean stopped = new AtomicBoolean(false);

        WireShareFrostWireSearchSession(List<ISearchPerformer> performers) {
            this.performers = performers;
        }

        static WireShareFrostWireSearchSession empty() {
            return new WireShareFrostWireSearchSession(new ArrayList<ISearchPerformer>());
        }

        public void stop() {
            if (!stopped.compareAndSet(false, true)) {
                return;
            }
            for (ISearchPerformer performer : performers) {
                performer.stop();
            }
        }
    }
}
