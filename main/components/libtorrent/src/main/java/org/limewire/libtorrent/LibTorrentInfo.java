package org.limewire.libtorrent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.limewire.bittorrent.TorrentTracker;

public class LibTorrentInfo {
    public String sha1;

    public String name;
    
    public long total_size;

    public int piece_length;

    public String created_by;

    public String comment;

    private final List<TorrentTracker> trackers_internal = new ArrayList<TorrentTracker>();

    private final List<String> seeds_internal = new ArrayList<String>();

    public LibTorrentInfo() {
    }

    public LibTorrentInfo(String sha1, String name, int pieceLength, List<TorrentTracker> trackers,
            List<String> seeds) {
        this.sha1 = sha1;
        this.name = name;
        this.piece_length = pieceLength;
        if (trackers != null) {
            trackers_internal.addAll(trackers);
        }
        if (seeds != null) {
            seeds_internal.addAll(seeds);
        }
    }
    
    
    public List<TorrentTracker> getTrackers() {
        return Collections.unmodifiableList(trackers_internal);
    }
    
    public List<String> getSeeds() {
        return Collections.unmodifiableList(seeds_internal);
    }
}
