package org.limewire.libtorrent;

import java.net.URI;
import java.net.URISyntaxException;

import org.limewire.bittorrent.TorrentTracker;
import org.limewire.util.URIUtils;

public class LibTorrentAnnounceEntry implements TorrentTracker {

    public String uri;
    public int tier;

    public LibTorrentAnnounceEntry() {
    }

    public LibTorrentAnnounceEntry(String uri, int tier) {
        this.uri = uri;
        this.tier = tier;
    }

    @Override
    public int getTier() {
        return tier;
    }

    @Override
    public URI getURI() {
        try {
            return URIUtils.toURI(uri);
        } catch (URISyntaxException e) {
            return null;
        }
    }
    
    @Override
    public String toString() {
        return uri;
    }
}
