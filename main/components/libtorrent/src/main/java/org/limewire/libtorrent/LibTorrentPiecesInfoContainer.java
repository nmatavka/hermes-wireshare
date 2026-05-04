package org.limewire.libtorrent;

/**
 * Legacy container retained for compatibility with older helper code.
 */
public class LibTorrentPiecesInfoContainer {
    public int numPiecesCompleted;
    public String stateInfo;
    
    public String getStateInfo() {
        return stateInfo;
    }
    
    public int getNumPiecesCompleted() {
        return numPiecesCompleted;
    }
}
