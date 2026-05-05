package org.limewire.xmpp.client.impl.messages.discoinfo;

import java.net.URI;

import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.filter.StanzaFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smackx.disco.ServiceDiscoveryManager;
import org.jivesoftware.smackx.disco.packet.DiscoverInfo;
import org.jxmpp.jid.Jid;
import org.jxmpp.jid.impl.JidCreate;
import org.limewire.friend.api.Friend;
import org.limewire.friend.api.FriendConnection;
import org.limewire.friend.api.FriendConnectionEvent;
import org.limewire.friend.api.FriendPresence;
import org.limewire.friend.api.FriendPresenceEvent;
import org.limewire.friend.api.feature.FeatureRegistry;
import org.limewire.listener.BlockingEvent;
import org.limewire.listener.EventListener;
import org.limewire.listener.ListenerSupport;
import org.limewire.logging.Log;
import org.limewire.logging.LogFactory;
import org.limewire.xmpp.client.impl.XMPPFriendConnectionImpl;

/**
 * Sends disco info queries to newly available presences and initializes features for the responses.
 */
public class DiscoInfoListener implements StanzaListener {

    private static final Log LOG = LogFactory.getLog(DiscoInfoListener.class);

    private final FriendConnection connection;
    private final XMPPConnection smackConnection;

    private final XMPPConnectionListener connectionListener = new XMPPConnectionListener();
    private ListenerSupport<FriendConnectionEvent> connectionSupport;
    private ListenerSupport<FriendPresenceEvent> friendPresenceSupport;
    private final FriendPresenceListener friendPresenceListener = new FriendPresenceListener();

    private final FeatureRegistry featureRegistry;

    public DiscoInfoListener(FriendConnection connection,
                             XMPPConnection smackConnection,
                             FeatureRegistry featureRegistry) {
        this.connection = connection;
        this.smackConnection = smackConnection;
        this.featureRegistry = featureRegistry;
    }

    public void addListeners(ListenerSupport<FriendConnectionEvent> connectionSupport,
                             ListenerSupport<FriendPresenceEvent> friendPresenceSupport) {
        this.connectionSupport = connectionSupport;
        this.friendPresenceSupport = friendPresenceSupport;

        connectionSupport.addListener(connectionListener);
        friendPresenceSupport.addListener(friendPresenceListener);
        smackConnection.addAsyncStanzaListener(this, getStanzaFilter());
    }

    @Override
    public void processStanza(Stanza stanza) {
        DiscoverInfo discoverInfo = (DiscoverInfo) stanza;
        Jid from = discoverInfo.getFrom();

        if (from == null) {
            LOG.debug("null from field in disco info");
            return;
        }

        FriendPresence friendPresence = matchValidPresence(from);
        if (friendPresence == null && !isForThisConnection(from)) {
            LOG.debugf("no presence found for and not for this connection: {0}", from);
            return;
        }

        String featureInitializer = friendPresence != null ? friendPresence.getPresenceId() : from.toString();
        for (URI uri : featureRegistry.getAllFeatureUris()) {
            if (discoverInfo.containsFeature(uri.toASCIIString())) {
                LOG.debugf("initializing feature {0} for {1}", uri.toASCIIString(), featureInitializer);
                featureRegistry.get(uri).initializeFeature(friendPresence);
            }
        }
    }

    public StanzaFilter getStanzaFilter() {
        return stanza -> stanza instanceof DiscoverInfo
                && ((DiscoverInfo) stanza).getType() == IQ.Type.result;
    }

    public void cleanup() {
        if (connectionListener != null) {
            connectionSupport.removeListener(connectionListener);
        }
        if (friendPresenceSupport != null) {
            friendPresenceSupport.removeListener(friendPresenceListener);
        }
        smackConnection.removeAsyncStanzaListener(this);
    }

    private void discoverFeatures(String entityName) {
        try {
            ServiceDiscoveryManager serviceDiscoveryManager = ServiceDiscoveryManager.getInstanceFor(smackConnection);
            if (serviceDiscoveryManager != null) {
                LOG.debugf("discovering presence: {0}", entityName);
                serviceDiscoveryManager.discoverInfo(JidCreate.from(entityName));
            } else {
                LOG.debug("no service discovery manager");
            }
        } catch (Exception exception) {
            LOG.info(exception.getMessage(), exception);
        }
    }

    private boolean isForThisConnection(Jid from) {
        return connection.getConfiguration().getServiceName().equals(from.toString());
    }

    private FriendPresence matchValidPresence(Jid from) {
        Friend friend = connection.getFriend(from.asBareJid().toString());
        if (friend != null) {
            return friend.getPresences().get(from.toString());
        }
        return null;
    }

    private class FriendPresenceListener implements EventListener<FriendPresenceEvent> {
        @BlockingEvent(queueName = "feature discovery")
        @Override
        public void handleEvent(final FriendPresenceEvent event) {
            if (event.getType() == FriendPresenceEvent.Type.ADDED) {
                discoverFeatures(event.getData().getPresenceId());
            }
        }
    }

    private class XMPPConnectionListener implements EventListener<FriendConnectionEvent> {
        @BlockingEvent(queueName = "feature discovery")
        @Override
        public void handleEvent(FriendConnectionEvent event) {
            if (!(event.getSource() instanceof XMPPFriendConnectionImpl)) {
                return;
            }
            if (event.getType() == FriendConnectionEvent.Type.CONNECTED) {
                discoverFeatures(connection.getConfiguration().getServiceName());
            }
        }
    }
}
