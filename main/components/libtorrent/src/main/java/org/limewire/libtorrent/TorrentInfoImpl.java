package org.limewire.libtorrent;

import java.util.Arrays;
import java.util.List;

import org.limewire.bittorrent.TorrentFileEntry;
import org.limewire.bittorrent.TorrentInfo;
import org.limewire.bittorrent.TorrentTracker;

class TorrentInfoImpl implements TorrentInfo {
    private final String name;
    private final int pieceLength;
    private final List<TorrentTracker> trackers;
    private final List<String> seeds;
    private final List<TorrentFileEntry> fileEntries;

    public TorrentInfoImpl(String name, int pieceLength, List<TorrentTracker> trackers,
            List<String> seeds, TorrentFileEntry[] fileEntries) {
        this.name = name;
        this.pieceLength = pieceLength;
        this.trackers = trackers;
        this.seeds = seeds;
        this.fileEntries = Arrays.asList(fileEntries);
    }

    @Override
    public List<TorrentFileEntry> getTorrentFileEntries() {
        return fileEntries;
    }

    @Override
    public int getPieceLength() {
        return pieceLength;
    }

    @Override
    public List<TorrentTracker> getTrackers() {
        return trackers;
    }

    @Override
    public List<String> getSeeds() {
        return seeds;
    }

    @Override
    public String getName() {
        return name;
    }
}
