package org.limewire.xmpp.client.impl.messages.library;

import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.filter.StanzaFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Stanza;
import org.jxmpp.jid.impl.JidCreate;
import org.limewire.friend.api.FriendException;
import org.limewire.friend.api.FriendPresence;
import org.limewire.friend.api.feature.FeatureTransport;
import org.limewire.friend.api.feature.LibraryChangedNotifier;
import org.limewire.logging.Log;
import org.limewire.logging.LogFactory;
import org.limewire.xmpp.client.impl.XMPPFriendConnectionImpl;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

public class LibraryChangedIQListener implements StanzaListener, FeatureTransport<LibraryChangedNotifier> {

    private static final Log LOG = LogFactory.getLog(LibraryChangedIQListener.class);

    private final Handler<LibraryChangedNotifier> libChangedHandler;
    private final XMPPFriendConnectionImpl connection;

    @Inject
    public LibraryChangedIQListener(Handler<LibraryChangedNotifier> libChangedListeners,
                                    @Assisted XMPPFriendConnectionImpl connection) {
        this.libChangedHandler = libChangedListeners;
        this.connection = connection;
    }

    @Override
    public void processStanza(Stanza stanza) {
        LibraryChangedIQ iq = (LibraryChangedIQ) stanza;
        if (iq.getType() == IQ.Type.set && iq.getFrom() != null) {
            LOG.debugf("received iq {0}", stanza);
            if (LOG.isDebugEnabled()) {
                LOG.debug("handling library changed set " + iq.getStanzaId());
            }
            libChangedHandler.featureReceived(iq.getFrom().toString(), new LibraryChangedNotifier() {});
        }
    }

    @Override
    public void sendFeature(FriendPresence presence, LibraryChangedNotifier localFeature) throws FriendException {
        LOG.debug("send library refresh");
        if (connection.isLoggedIn()) {
            try {
                LibraryChangedIQ libraryChangedIQ = new LibraryChangedIQ();
                libraryChangedIQ.setType(IQ.Type.set);
                libraryChangedIQ.setTo(JidCreate.from(presence.getPresenceId()));
                LOG.debugf("sending refresh to {0}", presence.getPresenceId());
                connection.sendPacket(libraryChangedIQ);
            } catch (Exception e) {
                LOG.debugf("library refresh failed", e);
            }
        }
    }

    public StanzaFilter getStanzaFilter() {
        return stanza -> stanza instanceof LibraryChangedIQ;
    }
}
