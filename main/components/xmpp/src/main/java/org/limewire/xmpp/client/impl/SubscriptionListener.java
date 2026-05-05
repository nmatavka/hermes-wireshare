package org.limewire.xmpp.client.impl;

import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.filter.StanzaFilter;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jxmpp.jid.BareJid;
import org.jxmpp.jid.impl.JidCreate;
import org.limewire.friend.api.FriendRequest;
import org.limewire.friend.api.FriendRequestDecisionHandler;
import org.limewire.friend.api.FriendRequestEvent;
import org.limewire.friend.impl.util.PresenceUtils;
import org.limewire.listener.EventBroadcaster;
import org.limewire.logging.Log;
import org.limewire.logging.LogFactory;

/**
 * Handles presence subscriptions and unsubscriptions on an XMPP connection.
 */
class SubscriptionListener implements StanzaListener, FriendRequestDecisionHandler {

    private static final Log LOG = LogFactory.getLog(SubscriptionListener.class);

    private final XMPPConnection connection;
    private final EventBroadcaster<FriendRequestEvent> friendRequestBroadcaster;

    SubscriptionListener(XMPPConnection connection,
            EventBroadcaster<FriendRequestEvent> friendRequestBroadcaster) {
        this.connection = connection;
        this.friendRequestBroadcaster = friendRequestBroadcaster;
    }

    @Override
    public void processStanza(Stanza stanza) {
        try {
            Presence presence = (Presence) stanza;
            String friendUsername = PresenceUtils.parseBareAddress(stanza.getFrom().toString());
            if (presence.getType() == Presence.Type.subscribe) {
                LOG.debugf("subscribe from {0}", friendUsername);
                Roster roster = Roster.getInstanceFor(connection);
                RosterEntry entry = roster.getEntry(JidCreate.bareFrom(friendUsername));
                if (entry == null) {
                    LOG.debug("it's a new subscription");
                    friendRequestBroadcaster.broadcast(new FriendRequestEvent(
                            new FriendRequest(friendUsername, this),
                            FriendRequestEvent.Type.REQUESTED));
                } else {
                    LOG.debug("it's a response to our subscription");
                    Presence subbed = connection.getStanzaFactory().buildPresenceStanza()
                            .ofType(Presence.Type.subscribed)
                            .to(JidCreate.bareFrom(friendUsername))
                            .build();
                    connection.sendStanza(subbed);
                }
            } else if (presence.getType() == Presence.Type.subscribed) {
                LOG.debugf("subscribed from {0}", friendUsername);
            } else if (presence.getType() == Presence.Type.unsubscribe) {
                LOG.debugf("unsubscribe from {0}", friendUsername);
                Presence unsubbed = connection.getStanzaFactory().buildPresenceStanza()
                        .ofType(Presence.Type.unsubscribed)
                        .to(JidCreate.bareFrom(friendUsername))
                        .build();
                connection.sendStanza(unsubbed);
                Roster roster = Roster.getInstanceFor(connection);
                RosterEntry entry = roster.getEntry(JidCreate.bareFrom(friendUsername));
                if (entry == null) {
                    LOG.debug("it's a response to our unsubscription");
                } else {
                    LOG.debug("it's a new unsubscription");
                    Presence unsub = connection.getStanzaFactory().buildPresenceStanza()
                            .ofType(Presence.Type.unsubscribe)
                            .to(JidCreate.bareFrom(friendUsername))
                            .build();
                    connection.sendStanza(unsub);
                    roster.removeEntry(entry);
                }
            } else if (presence.getType() == Presence.Type.unsubscribed) {
                LOG.debugf("unsubscribed from {0}", friendUsername);
            }
        } catch (Exception e) {
            LOG.debug("processStanza failed", e);
        }
    }

    public StanzaFilter getStanzaFilter() {
        return stanza -> {
            if (!(stanza instanceof Presence)) {
                return false;
            }
            Presence presence = (Presence) stanza;
            return presence.getType() == Presence.Type.subscribe
                    || presence.getType() == Presence.Type.subscribed
                    || presence.getType() == Presence.Type.unsubscribe
                    || presence.getType() == Presence.Type.unsubscribed;
        };
    }

    @Override
    public void handleDecision(String friendUsername, boolean accepted) {
        try {
            if (!connection.isConnected()) {
                return;
            }
            BareJid friendJid = JidCreate.bareFrom(friendUsername);
            if (accepted) {
                LOG.debug("user accepted");
                Presence subbed = connection.getStanzaFactory().buildPresenceStanza()
                        .ofType(Presence.Type.subscribed)
                        .to(friendJid)
                        .build();
                connection.sendStanza(subbed);
                Roster.getInstanceFor(connection).createItemAndRequestSubscription(friendJid, friendUsername, null);
            } else {
                LOG.debug("user declined");
                Presence unsubbed = connection.getStanzaFactory().buildPresenceStanza()
                        .ofType(Presence.Type.unsubscribed)
                        .to(friendJid)
                        .build();
                connection.sendStanza(unsubbed);
            }
        } catch (Exception e) {
            LOG.debug("handleDecision failed", e);
        }
    }
}
