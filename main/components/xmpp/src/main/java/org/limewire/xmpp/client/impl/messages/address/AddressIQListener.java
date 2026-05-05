package org.limewire.xmpp.client.impl.messages.address;

import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.filter.StanzaFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Stanza;
import org.jxmpp.jid.impl.JidCreate;
import org.limewire.friend.api.FriendException;
import org.limewire.friend.api.FriendPresence;
import org.limewire.friend.api.feature.FeatureTransport;
import org.limewire.io.Address;
import org.limewire.logging.Log;
import org.limewire.logging.LogFactory;
import org.limewire.net.address.AddressFactory;
import org.limewire.xmpp.client.impl.XMPPFriendConnectionImpl;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

public class AddressIQListener implements StanzaListener, FeatureTransport<Address> {
    private static final Log LOG = LogFactory.getLog(AddressIQListener.class);

    private final XMPPFriendConnectionImpl connection;
    private final AddressFactory factory;
    private final Handler<Address> handler;

    @Inject
    public AddressIQListener(@Assisted XMPPFriendConnectionImpl connection,
                             @Assisted AddressFactory factory,
                             Handler<Address> handler) {
        this.connection = connection;
        this.factory = factory;
        this.handler = handler;
    }

    @Override
    public void processStanza(Stanza stanza) {
        AddressIQ iq = (AddressIQ) stanza;
        if (iq.getType() == IQ.Type.set && iq.getFrom() != null) {
            handler.featureReceived(iq.getFrom().toString(), iq.getAddress());
        }
    }

    public StanzaFilter getStanzaFilter() {
        return stanza -> stanza instanceof AddressIQ;
    }

    @Override
    public void sendFeature(FriendPresence presence, Address address) throws FriendException {
        LOG.debugf("sending new address to {0}", presence);
        try {
            AddressIQ queryResult = new AddressIQ(address, factory);
            queryResult.setTo(JidCreate.from(presence.getPresenceId()));
            queryResult.setFrom(JidCreate.from(connection.getLocalJid()));
            queryResult.setType(IQ.Type.set);
            connection.sendPacket(queryResult);
        } catch (Exception e) {
            throw new FriendException(e);
        }
    }
}
