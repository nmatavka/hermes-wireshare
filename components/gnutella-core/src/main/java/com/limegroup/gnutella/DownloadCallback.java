package com.limegroup.gnutella;

import java.util.Set;

import org.limewire.bittorrent.Torrent;

/**
 * A callback for download information.
 */
public interface DownloadCallback {
    

    /** Add a file to the download window */
    public void addDownload(Downloader d);

    /** Remove a downloader from the download window. */
    public void removeDownload(Downloader d);

     /** 
      * Notifies the GUI that all active downloads have been completed.
      */   
    public void downloadsComplete();

    
    /**
     * Prompts the user about what priorities to assign the files in this
     * torrent. Returns true if ok was selected in the end false if cancel.
     */
    public boolean promptTorrentFilePriorities(Torrent torrent);
    
    /**
     * Asks the user whether to continue with a torrent download that contains
     * files with banned extensions.
     * @return true if the download should continue.
     */
    public boolean promptAboutTorrentWithBannedExtensions(Torrent torrent,
            Set<String> bannedExtensions);
}