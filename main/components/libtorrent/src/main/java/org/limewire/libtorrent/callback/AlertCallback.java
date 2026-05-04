package org.limewire.libtorrent.callback;

import org.limewire.libtorrent.LibTorrentAlert;

/**
 * Callback used by the compatibility layer to forward libtorrent-style alerts.
 */
public interface AlertCallback {
    void callback(LibTorrentAlert alert);
}
