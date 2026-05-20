package org.limewire.search.frostwire;

import org.limewire.core.api.search.SearchCategory;
import org.limewire.core.settings.SearchSettings;
import org.limewire.search.frostwire.search.CrawlingStrategy;
import org.limewire.search.frostwire.search.ISearchPerformer;
import org.limewire.search.frostwire.search.SearchPattern;
import org.limewire.search.frostwire.search.SearchPerformerFactory;
import org.limewire.search.frostwire.search.frostclick.FrostClickSearchPattern;
import org.limewire.search.frostwire.search.frostclick.UserAgent;
import org.limewire.search.frostwire.search.idope.IdopeSearchPattern;
import org.limewire.search.frostwire.search.internetarchive.InternetArchiveCrawlingStrategy;
import org.limewire.search.frostwire.search.internetarchive.InternetArchiveSearchPattern;
import org.limewire.search.frostwire.search.knaben.KnabenSearchPattern;
import org.limewire.search.frostwire.search.magnetdl.MagnetDLSearchPattern;
import org.limewire.search.frostwire.search.nyaa.NyaaSearchPattern;
import org.limewire.search.frostwire.search.one337x.One337xCrawlingStrategy;
import org.limewire.search.frostwire.search.one337x.One337xSearchPattern;
import org.limewire.search.frostwire.search.soundcloud.SoundcloudSearchPattern;
import org.limewire.search.frostwire.search.torrentz2.Torrentz2SearchPattern;
import org.limewire.search.frostwire.search.torrentscsv.TorrentsCSVSearchPattern;
import org.limewire.search.frostwire.search.tpb.TPBMirrors;
import org.limewire.search.frostwire.search.tpb.TPBSearchPattern;
import org.limewire.search.frostwire.search.yt.YTSearchPattern;
import org.limewire.search.frostwire.util.OSUtils;
import org.limewire.setting.BooleanSetting;

/**
 * WireShare-owned registry for the FrostWire donor search engines.
 */
public enum WireShareFrostWireSearchProvider {
    YT("YT", SearchSettings.FROSTWIRE_SEARCH_YT_ENABLED, false) {
        @Override
        SearchPattern pattern() {
            return new YTSearchPattern();
        }
    },
    INTERNET_ARCHIVE("Archive.org", SearchSettings.FROSTWIRE_SEARCH_INTERNET_ARCHIVE_ENABLED, false) {
        @Override
        SearchPattern pattern() {
            return new InternetArchiveSearchPattern();
        }

        @Override
        CrawlingStrategy crawler() {
            return new InternetArchiveCrawlingStrategy();
        }
    },
    IDOPE("idope", SearchSettings.FROSTWIRE_SEARCH_IDOPE_ENABLED, true) {
        @Override
        SearchPattern pattern() {
            return new IdopeSearchPattern();
        }
    },
    KNABEN("Knaben", SearchSettings.FROSTWIRE_SEARCH_KNABEN_ENABLED, true) {
        @Override
        SearchPattern pattern() {
            return new KnabenSearchPattern();
        }
    },
    MAGNETDL("magnetdl", SearchSettings.FROSTWIRE_SEARCH_MAGNETDL_ENABLED, true) {
        @Override
        SearchPattern pattern() {
            return new MagnetDLSearchPattern();
        }
    },
    NYAA("Nyaa", SearchSettings.FROSTWIRE_SEARCH_NYAA_ENABLED, true) {
        @Override
        SearchPattern pattern() {
            return new NyaaSearchPattern();
        }
    },
    ONE337X("1337x", SearchSettings.FROSTWIRE_SEARCH_ONE337X_ENABLED, true) {
        @Override
        SearchPattern pattern() {
            return new One337xSearchPattern();
        }

        @Override
        CrawlingStrategy crawler() {
            return new One337xCrawlingStrategy();
        }
    },
    TPB("TPB", SearchSettings.FROSTWIRE_SEARCH_TPB_ENABLED, true) {
        @Override
        SearchPattern pattern() {
            return new TPBSearchPattern(TPBMirrors.getMirrors()[0]);
        }
    },
    TORRENTZ2("torrentz2", SearchSettings.FROSTWIRE_SEARCH_TORRENTZ2_ENABLED, true) {
        @Override
        SearchPattern pattern() {
            return new Torrentz2SearchPattern();
        }
    },
    TORRENTSCSV("TorrentsCSV", SearchSettings.FROSTWIRE_SEARCH_TORRENTSCSV_ENABLED, true) {
        @Override
        SearchPattern pattern() {
            return new TorrentsCSVSearchPattern();
        }
    },
    SOUNDCLOUD("Soundcloud", SearchSettings.FROSTWIRE_SEARCH_SOUNDCLOUD_ENABLED, false) {
        @Override
        SearchPattern pattern() {
            return new SoundcloudSearchPattern(null, null);
        }
    },
    FROSTCLICK("FrostClick", SearchSettings.FROSTWIRE_SEARCH_FROSTCLICK_ENABLED, false) {
        @Override
        SearchPattern pattern() {
            UserAgent userAgent = new UserAgent(OSUtils.getFullOS(), "7.0", "0");
            return new FrostClickSearchPattern(userAgent.toString(), userAgent.getUUID(), userAgent.getHeadersMap());
        }
    };

    private final String displayName;
    private final BooleanSetting setting;
    private final boolean torrentProvider;

    WireShareFrostWireSearchProvider(String displayName, BooleanSetting setting, boolean torrentProvider) {
        this.displayName = displayName;
        this.setting = setting;
        this.torrentProvider = torrentProvider;
    }

    public String displayName() {
        return displayName;
    }

    public String settingKey() {
        return setting.getKey();
    }

    public boolean isEnabled() {
        return setting.getValue();
    }

    public void setEnabled(boolean enabled) {
        setting.setValue(enabled);
    }

    public boolean supports(SearchCategory category) {
        if (category == SearchCategory.ALL) {
            return true;
        }
        if (category == SearchCategory.TORRENT) {
            return torrentProvider;
        }
        return !torrentProvider;
    }

    public ISearchPerformer createPerformer(long token, String query) {
        return SearchPerformerFactory.createSearchPerformer(token, query, pattern(), crawler(), 5000);
    }

    abstract SearchPattern pattern();

    CrawlingStrategy crawler() {
        return null;
    }
}
