package org.limewire.ui.swing.friends.chat;

import org.limewire.core.api.download.DownloadState;
import org.limewire.friend.api.FileMetaData;
import org.limewire.friend.api.FriendPresence;
import org.limewire.ui.desktop.util.I18n;

class MessageFileOfferImpl extends AbstractMessageImpl implements MessageFileOffer {
    private final FileMetaData fileMetadata;
    private final FriendPresence sourcePresence;
    private DownloadState downloadState;

    MessageFileOfferImpl(String senderName, String friendId, Type type, FileMetaData fileMetadata, FriendPresence sourcePresence) {
        super(senderName, friendId, type);
        this.fileMetadata = fileMetadata;
        this.sourcePresence = sourcePresence;
    }

    @Override
    public FileMetaData getFileOffer() {
        return fileMetadata;
    }

    @Override
    public void setDownloadState(DownloadState downloadState) {
        this.downloadState = downloadState;
    }

    @Override
    public FriendPresence getPresence() {
        return sourcePresence;
    }

    @Override
    public String toString() {
        String state = downloadState == null ? "No State" : downloadState.toString();
        return fileMetadata.getName() + "(" + state + ")";
    }

    @Override
    public String format() {
        if (getType() == Message.Type.RECEIVED) {
            if (downloadState == DownloadState.DONE) {
                return I18n.tr("{0} shared {1}", getFriendID(), fileMetadata.getName());
            }
            return I18n.tr("{0} wants to share {1}", getFriendID(), fileMetadata.getName());
        }
        return I18n.tr("Sharing {0} with {1}", fileMetadata.getName(), getFriendID());
    }
}
