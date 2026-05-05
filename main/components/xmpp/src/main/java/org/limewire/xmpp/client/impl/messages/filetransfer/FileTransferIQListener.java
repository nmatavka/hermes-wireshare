package org.limewire.xmpp.client.impl.messages.filetransfer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.filter.StanzaFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Stanza;
import org.jxmpp.jid.impl.JidCreate;
import org.limewire.friend.api.FileMetaData;
import org.limewire.friend.api.FriendException;
import org.limewire.friend.api.FriendPresence;
import org.limewire.friend.api.feature.FeatureTransport;
import org.limewire.xmpp.client.impl.XMPPFriendConnectionImpl;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

public class FileTransferIQListener implements StanzaListener, FeatureTransport<FileMetaData> {
    private static final Log LOG = LogFactory.getLog(FileTransferIQListener.class);
    private final XMPPFriendConnectionImpl connection;
    private final Handler<FileMetaData> fileMetaDataHandler;

    @Inject
    public FileTransferIQListener(@Assisted XMPPFriendConnectionImpl connection,
                                  FeatureTransport.Handler<FileMetaData> fileMetaDataHandler) {
        this.connection = connection;
        this.fileMetaDataHandler = fileMetaDataHandler;
    }

    @Override
    public void processStanza(Stanza stanza) {
        FileTransferIQ iq = (FileTransferIQ) stanza;
        if (iq.getType() == IQ.Type.get && iq.getFrom() != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("handling file transfer get " + iq.getStanzaId());
            }
            fileMetaDataHandler.featureReceived(iq.getFrom().toString(), iq.getFileMetaData());
        }
    }

    @Override
    public void sendFeature(FriendPresence presence, FileMetaData localFeature) throws FriendException {
        if (LOG.isInfoEnabled()) {
            LOG.info("offering file " + localFeature.toString() + " to " + presence.getPresenceId());
        }
        try {
            FileTransferIQ transferIQ = new FileTransferIQ(localFeature, FileTransferIQ.TransferType.OFFER);
            transferIQ.setType(IQ.Type.get);
            transferIQ.setTo(JidCreate.from(presence.getPresenceId()));
            connection.sendPacket(transferIQ);
        } catch (Exception e) {
            throw new FriendException(e);
        }
    }

    public StanzaFilter getStanzaFilter() {
        return stanza -> stanza instanceof FileTransferIQ;
    }
}
