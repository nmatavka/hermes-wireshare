/*
 *     Created by Angel Leon (@gubatron), Alden Torres (aldenml)
 *     Copyright (c) 2011-2026, FrostWire(R). All rights reserved.
 * 
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 * 
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 * 
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.team_hermes.wireshare.android.gui;

import android.os.Build;

import androidx.annotation.NonNull;

import org.team_hermes.wireshare.android.core.ConfigurationManager;
import org.team_hermes.wireshare.android.core.Constants;
import org.team_hermes.wireshare.android.core.TellurideCourier;
import org.team_hermes.wireshare.android.gui.adapters.SearchResultListAdapter;
import org.team_hermes.wireshare.android.gui.SoftwareUpdater;
import org.team_hermes.wireshare.search.ISearchPerformer;
import org.team_hermes.wireshare.search.frostclick.UserAgent;
import org.team_hermes.wireshare.search.frostclick.FrostClickSearchPattern;
import org.team_hermes.wireshare.search.nyaa.NyaaSearchPattern;
import org.team_hermes.wireshare.search.knaben.KnabenSearchPattern;
import org.team_hermes.wireshare.search.magnetdl.MagnetDLSearchPattern;
import org.team_hermes.wireshare.search.internetarchive.InternetArchiveSearchPattern;
import org.team_hermes.wireshare.search.internetarchive.InternetArchiveCrawlingStrategy;
import org.team_hermes.wireshare.search.tpb.TPBSearchPattern;
import org.team_hermes.wireshare.search.tpb.TPBMirrors;
import org.team_hermes.wireshare.search.soundcloud.SoundcloudSearchPattern;
import org.team_hermes.wireshare.search.SearchPerformerFactory;
import org.team_hermes.wireshare.search.torrentscsv.TorrentsCSVSearchPattern;
import org.team_hermes.wireshare.search.yt.YTSearchPattern;
import org.team_hermes.wireshare.search.one337x.One337xSearchPattern;
import org.team_hermes.wireshare.search.idope.IdopeSearchPattern;
import org.team_hermes.wireshare.search.torrentz2.Torrentz2SearchPattern;
import org.team_hermes.wireshare.util.HttpClientFactory;
import org.team_hermes.wireshare.util.UrlUtils;
import org.team_hermes.wireshare.util.http.HttpClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author gubatron
 * @author aldenml
 */
public abstract class SearchEngine {

    private static final UserAgent FROSTWIRE_ANDROID_USER_AGENT = new UserAgent(getOSVersionString(), Constants.FROSTWIRE_VERSION_STRING, Constants.FROSTWIRE_BUILD);
    private static final int DEFAULT_TIMEOUT = 5000;

    private final String name;
    private final String preferenceKey;

    private boolean active;

    private SearchEngine(String name, String preferenceKey) {
        this.name = name;
        this.preferenceKey = preferenceKey;
        this.active = true;
        postInitWork();
    }

    protected boolean isReady() {
        return true;
    }

    protected void postInitWork() {
    }

    public String getName() {
        return name;
    }

    public abstract ISearchPerformer getPerformer(long token, String keywords);

    public TellurideCourier.SearchPerformer getTelluridePerformer(long currentSearchToken, String pageUrl, SearchResultListAdapter adapter) {
        // override me
        return null;
    }

    public String getPreferenceKey() {
        return preferenceKey;
    }

    /**
     * This is what's eventually checked to perform a search
     */
    public boolean isEnabled() {
        return isActive() && ConfigurationManager.instance().getBoolean(preferenceKey);
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @NonNull
    @Override
    public String toString() {
        return name;
    }

    /**
     * This will include all engines, even if they have been marked as inactive via
     * remote config or by some other mean.
     * <p>
     * It will only exclude non-ready engines if excludeNonReady=true
     * <p>
     * We need to include inactive engines so that places like the SearchEnginesPreferenceFragment
     * can separate active engines from inactive engines and do things like hide them from the
     * user interface in the case of WireShare Basic where some engines are not allowed in google play
     */
    public static List<SearchEngine> getEngines(boolean excludeNonReady) {
        ArrayList<SearchEngine> candidates = new ArrayList<>();

        for (SearchEngine se : ALL_ENGINES) {
            if (!excludeNonReady || se.isReady()) {
                candidates.add(se);
            }
        }

        // ensure that at least one is enable
        boolean oneEnabled = false;
        for (SearchEngine se : candidates) {
            if (se.isEnabled()) {
                oneEnabled = true;
            }
        }
        if (!oneEnabled) {
            SearchEngine engineToEnable;
            engineToEnable = ARCHIVE;
            String prefKey = engineToEnable.getPreferenceKey();
            ConfigurationManager.instance().setBoolean(prefKey, true);
        }
        return candidates;
    }

    public static SearchEngine forName(String name) {
        for (SearchEngine engine : getEngines(false)) {
            if (engine.getName().equalsIgnoreCase(name)) {
                return engine;
            }
        }
        return null;
    }

    static String getOSVersionString() {
        return Build.VERSION.CODENAME + "_" + Build.VERSION.INCREMENTAL + "_" + Build.VERSION.RELEASE + "_" + Build.VERSION.SDK_INT;
    }

    public static final SearchEngine SOUNCLOUD = new SearchEngine("Soundcloud", Constants.PREF_KEY_SEARCH_USE_SOUNDCLOUD) {
        @Override
        public ISearchPerformer getPerformer(long token, String keywords) {
            // V2: Using new flat architecture with JSON API parsing for audio streaming
            // Get dynamic credentials fetched from remote server
            String clientId = SoftwareUpdater.getSoundCloudClientId();
            String appVersion = SoftwareUpdater.getSoundCloudAppVersion();
            return SearchPerformerFactory.createSearchPerformer(
                    token,
                    keywords,
                    new SoundcloudSearchPattern(clientId, appVersion),
                    null,  // No crawling needed
                    DEFAULT_TIMEOUT
            );
        }
    };

    public static final SearchEngine ARCHIVE = new SearchEngine("Archive.org", Constants.PREF_KEY_SEARCH_USE_ARCHIVEORG) {
        @Override
        public ISearchPerformer getPerformer(long token, String keywords) {
            // V2: Using new flat architecture with crawling strategy
            // Search page returns preliminary results, strategy fetches file list from detail pages
            return SearchPerformerFactory.createSearchPerformer(
                    token,
                    keywords,
                    new InternetArchiveSearchPattern(),
                    new InternetArchiveCrawlingStrategy(),
                    DEFAULT_TIMEOUT
            );
        }
    };

    public static final SearchEngine FROSTCLICK = new SearchEngine("FrostClick", Constants.PREF_KEY_SEARCH_USE_FROSTCLICK) {
        @Override
        public ISearchPerformer getPerformer(long token, String keywords) {
            // V2: Using new flat architecture with custom headers for API authentication
            return SearchPerformerFactory.createSearchPerformer(
                    token,
                    keywords,
                    new FrostClickSearchPattern(FROSTWIRE_ANDROID_USER_AGENT.toString(), FROSTWIRE_ANDROID_USER_AGENT.getUUID(), FROSTWIRE_ANDROID_USER_AGENT.getHeadersMap()),
                    null,  // No crawling needed
                    DEFAULT_TIMEOUT
            );
        }
    };

    public static final SearchEngine NYAA = new SearchEngine("Nyaa", Constants.PREF_KEY_SEARCH_USE_NYAA) {
        @Override
        public ISearchPerformer getPerformer(long token, String keywords) {
            // V2: Using SearchPerformerFactory with regex-based HTML parsing pattern
            // NyaaSearchPattern extracts anime torrent metadata from search result table
            return SearchPerformerFactory.createSearchPerformer(
                    token,
                    keywords,
                    new NyaaSearchPattern(),
                    null,  // No crawling needed
                    DEFAULT_TIMEOUT
            );
        }
    };

    public static final SearchEngine TPB = new SearchEngine("TPB", Constants.PREF_KEY_SEARCH_USE_TPB) {
        private String domainName = null;

        @Override
        public ISearchPerformer getPerformer(long token, String keywords) {
            if (domainName == null) {
                throw new RuntimeException("check your logic, this search performer has no domain name ready");
            }
            // V2: Using new flat architecture with regex pattern for dual HTML format support
            // TPB requires access to domain for URL construction, so create V2 SearchEngine directly
            String encodedKeywords = UrlUtils.encode(keywords);
            org.team_hermes.wireshare.search.SearchPerformer searchEngine = new org.team_hermes.wireshare.search.SearchPerformer(
                    token,
                    keywords,
                    encodedKeywords,
                    new TPBSearchPattern(domainName),  // Pass domain to pattern
                    null,  // No crawling needed - complete data on search page
                    DEFAULT_TIMEOUT
            );
            return searchEngine;
        }

        protected void postInitWork() {
            new Thread(() -> {
                HttpClient httpClient = HttpClientFactory.getInstance(HttpClientFactory.HttpContext.SEARCH);
                domainName = UrlUtils.getFastestMirrorDomain(httpClient, TPBMirrors.getMirrors(), 6000, 6);
            }).start();
        }

        @Override
        protected boolean isReady() {
            return domainName != null;
        }
    };

    public static final SearchEngine ONE337X = new SearchEngine("1337x", Constants.PREF_KEY_SEARCH_USE_ONE337X) {
        @Override
        public ISearchPerformer getPerformer(long token, String keywords) {
            // V2: Using new flat architecture with crawling strategy
            // Search page returns crawlable results, strategy fetches detail pages
            return SearchPerformerFactory.createSearchPerformer(
                    token,
                    keywords,
                    new One337xSearchPattern(),
                    new org.team_hermes.wireshare.search.one337x.One337xCrawlingStrategy(),
                    DEFAULT_TIMEOUT
            );
        }
    };

    public static final SearchEngine IDOPE = new SearchEngine("idope", Constants.PREF_KEY_SEARCH_USE_IDOPE) {

        @Override
        public ISearchPerformer getPerformer(long token, String keywords) {
            // V2: Using new flat architecture with JSON API parsing (no crawling needed)
            return SearchPerformerFactory.createSearchPerformer(
                    token,
                    keywords,
                    new IdopeSearchPattern(),
                    null,  // No crawling needed
                    DEFAULT_TIMEOUT
            );
        }
    };

    public static final SearchEngine TORRENTZ2 = new SearchEngine("torrentz2", Constants.PREF_KEY_SEARCH_USE_TORRENTZ2) {

        @Override
        public ISearchPerformer getPerformer(long token, String keywords) {
            // V2: Using new flat architecture (no crawling needed - Torrentz2 provides complete data)
            return SearchPerformerFactory.createSearchPerformer(
                    token,
                    keywords,
                    new Torrentz2SearchPattern(),
                    null,  // No crawling needed
                    DEFAULT_TIMEOUT
            );
        }
    };

    public static final SearchEngine MAGNETDL = new SearchEngine("magnetdl", Constants.PREF_KEY_SEARCH_USE_MAGNETDL) {

        @Override
        public ISearchPerformer getPerformer(long token, String keywords) {
            // V2: Using new flat architecture (no crawling needed - MagnetDL provides complete data via JSON API)
            return SearchPerformerFactory.createSearchPerformer(
                    token,
                    keywords,
                    new MagnetDLSearchPattern(),
                    null,  // No crawling needed
                    DEFAULT_TIMEOUT
            );
        }
    };


    public static final SearchEngine TORRENTSCSV = new SearchEngine("TorrentsCSV", Constants.PREF_KEY_SEARCH_USE_TORRENTSCSV) {
        @Override
        public ISearchPerformer getPerformer(long token, String keywords) {
            // V2: Using SearchPerformerFactory with JSON API pattern
            // TorrentsCSVSearchPattern provides complete torrent metadata via JSON API
            return SearchPerformerFactory.createSearchPerformer(
                    token,
                    keywords,
                    new TorrentsCSVSearchPattern(),
                    null,  // No crawling needed
                    DEFAULT_TIMEOUT
            );
        }
    };

    public static final SearchEngine KNABEN = new SearchEngine("Knaben", Constants.PREF_KEY_SEARCH_USE_KNABEN) {
        @Override
        public ISearchPerformer getPerformer(long token, String keywords) {
            // V2: Using SearchPerformerFactory with custom pattern that declares POST_JSON HTTP method
            // KnabenSearchPattern specifies HTTP method and constructs the JSON request body
            return SearchPerformerFactory.createSearchPerformer(
                    token,
                    keywords,
                    new KnabenSearchPattern(),
                    null,  // No crawling needed
                    DEFAULT_TIMEOUT
            );
        }
    };

    public static final SearchEngine TELLURIDE_COURIER = new SearchEngine("Telluride Courier", Constants.PREF_KEY_SEARCH_USE_TELLURIDE_COURIER) {
        @Override
        public ISearchPerformer getPerformer(long token, String keywords) {
            return null;
        }

        @Override
        public TellurideCourier.SearchPerformer<SearchResultListAdapter> getTelluridePerformer(long token, String pageUrl, SearchResultListAdapter adapter) {
            return new TellurideCourier.SearchPerformer<>(token, pageUrl, adapter);
        }
    };

    public static final SearchEngine YT = new SearchEngine("YT", Constants.PREF_KEY_SEARCH_USE_YT) {
        @Override
        public ISearchPerformer getPerformer(long token, String keywords) {
            // V2: Using new flat architecture
            return SearchPerformerFactory.createSearchPerformer(
                    token,
                    keywords,
                    new YTSearchPattern(),
                    null,  // No crawling needed for YouTube
                    DEFAULT_TIMEOUT
            );
        }

        @Override
        public boolean isActive() {
            return !Constants.IS_GOOGLE_PLAY_DISTRIBUTION || Constants.IS_BASIC_AND_DEBUG;
        }
    };

    private static final List<SearchEngine> ALL_ENGINES = Arrays.asList(
            YT,
            MAGNETDL,
            TORRENTZ2,
            ONE337X,
            IDOPE,
            FROSTCLICK,
            TPB,
            SOUNCLOUD,
            ARCHIVE,
            NYAA,
            TORRENTSCSV,
            KNABEN);
}
