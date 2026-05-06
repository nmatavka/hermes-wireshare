package org.limewire.ui.swing.friends.chat;

import org.limewire.core.api.download.DownloadState;
import org.limewire.friend.api.FileMetaData;
import org.limewire.friend.api.FriendPresence;

interface MessageFileOffer extends Message {
    FileMetaData getFileOffer();
    void setDownloadState(DownloadState downloadState);
    FriendPresence getPresence();
}
