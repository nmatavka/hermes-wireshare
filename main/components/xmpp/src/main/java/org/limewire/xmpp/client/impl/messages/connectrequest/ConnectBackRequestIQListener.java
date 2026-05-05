package org.limewire.xmpp.client.impl.messages.connectrequest;

import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.filter.StanzaFilter;
import org.jivesoftware.smack.packet.Stanza;
import org.jxmpp.jid.impl.JidCreate;
import org.limewire.friend.api.FriendException;
import org.limewire.friend.api.FriendPresence;
import org.limewire.friend.api.feature.ConnectBackRequestFeature;
import org.limewire.friend.api.feature.FeatureInitializer;
import org.limewire.friend.api.feature.FeatureRegistry;
import org.limewire.friend.api.feature.FeatureTransport;
import org.limewire.logging.Log;
import org.limewire.logging.LogFactory;
import org.limewire.net.ConnectBackRequest;
import org.limewire.xmpp.client.impl.XMPPFriendConnectionImpl;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

/**
 * Listens for connect back request IQs and fires the feature transport handler.
 */
public class ConnectBackRequestIQListener implements StanzaListener, FeatureTransport<ConnectBackRequest> {

    private static final Log LOG = LogFactory.getLog(ConnectBackRequestIQListener.class);

    private final XMPPFriendConnectionImpl connection;
    private final Handler<ConnectBackRequest> connectBackRequestHandler;

    @Inject
    public ConnectBackRequestIQListener(@Assisted XMPPFriendConnectionImpl connection,
                                        FeatureTransport.Handler<ConnectBackRequest> connectBackRequestHandler,
                                        FeatureRegistry featureRegistry) {
        this.connection = connection;
        this.connectBackRequestHandler = connectBackRequestHandler;
        new ConnectBackRequestIQFeatureInitializer().register(featureRegistry);
    }

    @Override
    public void processStanza(Stanza stanza) {
        ConnectBackRequestIQ connectRequest = (ConnectBackRequestIQ) stanza;
        LOG.debugf("processing connect request: {0}", connectRequest);
        if (connectRequest.getFrom() != null) {
            connectBackRequestHandler.featureReceived(connectRequest.getFrom().toString(),
                    connectRequest.getConnectBackRequest());
        }
    }

    @Override
    public void sendFeature(FriendPresence presence, ConnectBackRequest connectBackRequest)
            throws FriendException {
        try {
            ConnectBackRequestIQ connectRequest = new ConnectBackRequestIQ(connectBackRequest);
            connectRequest.setTo(JidCreate.from(presence.getPresenceId()));
            connectRequest.setFrom(JidCreate.from(connection.getLocalJid()));
            LOG.debugf("sending request: {0}", connectRequest);
            connection.sendPacket(connectRequest);
        } catch (Exception e) {
            throw new FriendException(e);
        }
    }

    public StanzaFilter getStanzaFilter() {
        return stanza -> stanza instanceof ConnectBackRequestIQ;
    }

    private static class ConnectBackRequestIQFeatureInitializer implements FeatureInitializer {
        @Override
        public void register(FeatureRegistry registry) {
            registry.registerPublicInitializer(ConnectBackRequestFeature.ID, this);
        }

        @Override
        public void initializeFeature(FriendPresence friendPresence) {
            friendPresence.addFeature(new ConnectBackRequestFeature());
        }

        @Override
        public void removeFeature(FriendPresence friendPresence) {
            friendPresence.removeFeature(ConnectBackRequestFeature.ID);
        }
    }

}
