package org.limewire.xmpp.client.impl.messages.authtoken;

import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.filter.StanzaFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Stanza;
import org.jxmpp.jid.impl.JidCreate;
import org.limewire.friend.api.FriendException;
import org.limewire.friend.api.FriendPresence;
import org.limewire.friend.api.feature.AuthToken;
import org.limewire.friend.api.feature.FeatureTransport;
import org.limewire.xmpp.client.impl.XMPPFriendConnectionImpl;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

public class AuthTokenIQListener implements StanzaListener, FeatureTransport<AuthToken> {

    private final XMPPFriendConnectionImpl connection;
    private final FeatureTransport.Handler<AuthToken> handler;

    @Inject
    public AuthTokenIQListener(@Assisted XMPPFriendConnectionImpl connection,
                               Handler<AuthToken> handler) {
        this.connection = connection;
        this.handler = handler;
    }

    @Override
    public void processStanza(Stanza stanza) {
        AuthTokenIQ iq = (AuthTokenIQ) stanza;
        if (iq.getType() == IQ.Type.set && iq.getFrom() != null) {
            handler.featureReceived(iq.getFrom().toString(), iq.getAuthToken());
        }
    }

    @Override
    public void sendFeature(FriendPresence presence, AuthToken localFeature) throws FriendException {
        try {
            AuthTokenIQ queryResult = new AuthTokenIQ(localFeature);
            queryResult.setTo(JidCreate.from(presence.getPresenceId()));
            queryResult.setFrom(JidCreate.from(connection.getLocalJid()));
            queryResult.setType(IQ.Type.set);
            connection.sendPacket(queryResult);
        } catch (Exception e) {
            throw new FriendException(e);
        }
    }

    public StanzaFilter getStanzaFilter() {
        return stanza -> stanza instanceof AuthTokenIQ;
    }
}
