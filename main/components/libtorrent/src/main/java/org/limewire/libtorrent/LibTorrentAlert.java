package org.limewire.libtorrent;

import org.limewire.bittorrent.TorrentAlert;
import com.frostwire.jlibtorrent.AddTorrentParams;

/**
 * Lightweight alert representation used by the compatibility wrapper.
 */
public class LibTorrentAlert implements TorrentAlert {

    public int category;
    public String sha1;
    public String message;

    private final AddTorrentParams resumeData;

    public LibTorrentAlert(int category, String sha1, String message) {
        this(category, sha1, message, null);
    }

    public LibTorrentAlert(int category, String sha1, String message, AddTorrentParams resumeData) {
        this.category = category;
        this.sha1 = sha1;
        this.message = message;
        this.resumeData = resumeData;
    }

    @Override
    public int getCategory() {
        return category;
    }

    @Override
    public String getSha1() {
        return sha1;
    }

    @Override
    public String getMessage() {
        return message;
    }

    boolean hasResumeData() {
        return resumeData != null;
    }

    AddTorrentParams getResumeData() {
        return resumeData;
    }

    @Override
    public String toString() {
        return sha1 + " " + message + " [" + category + "] ";
    }
}
