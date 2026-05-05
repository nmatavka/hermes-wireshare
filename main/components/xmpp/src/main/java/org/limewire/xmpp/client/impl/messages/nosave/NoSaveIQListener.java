package org.limewire.xmpp.client.impl.messages.nosave;

import java.util.HashMap;
import java.util.Map;

import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.filter.StanzaFilter;
import org.jivesoftware.smack.packet.Stanza;
import org.limewire.friend.api.Friend;
import org.limewire.friend.api.FriendException;
import org.limewire.friend.api.FriendPresence;
import org.limewire.friend.api.FriendPresenceEvent;
import org.limewire.friend.impl.feature.NoSave;
import org.limewire.friend.impl.feature.NoSaveFeature;
import org.limewire.friend.impl.feature.NoSaveStatus;
import org.limewire.listener.EventListener;
import org.limewire.listener.ListenerSupport;
import org.limewire.xmpp.client.impl.XMPPFriendConnectionImpl;

/**
 * Tracks google:nosave state and keeps friend presences updated with the NoSave feature.
 */
public class NoSaveIQListener implements StanzaListener {

    private final XMPPFriendConnectionImpl connection;
    private Map<String, NoSave> noSaveMap = new HashMap<String, NoSave>();

    private ListenerSupport<FriendPresenceEvent> friendPresenceSupport;
    private EventListener<FriendPresenceEvent> friendPresenceListener;

    private NoSaveIQListener(XMPPFriendConnectionImpl connection) {
        this.connection = connection;
    }

    public static NoSaveIQListener createNoSaveIQListener(XMPPFriendConnectionImpl connection,
                                                          ListenerSupport<FriendPresenceEvent> friendPresenceSupport) {
        NoSaveIQListener noSaveIQListener = new NoSaveIQListener(connection);
        noSaveIQListener.register(friendPresenceSupport);
        return noSaveIQListener;
    }

    @Override
    public void processStanza(Stanza stanza) {
        NoSaveIQ noSave = (NoSaveIQ) stanza;
        Map<String, NoSave> friends = noSave.getNoSaveUsers();
        updateNoSaveStatusMap(friends);

        for (String friendName : friends.keySet()) {
            Friend noSaveFriend = connection.getFriend(friendName);
            if (noSaveFriend != null) {
                for (FriendPresence presence : noSaveFriend.getPresences().values()) {
                    addNoSaveFeatureIfNecessary(presence, friends.get(friendName));
                }
            }
        }
    }

    public StanzaFilter getStanzaFilter() {
        return stanza -> stanza instanceof NoSaveIQ;
    }

    public void cleanup() {
        this.friendPresenceSupport.removeListener(friendPresenceListener);
    }

    private void register(ListenerSupport<FriendPresenceEvent> friendPresenceSupport) {
        this.friendPresenceSupport = friendPresenceSupport;
        this.friendPresenceListener = new FriendPresenceListener();
        this.friendPresenceSupport.addListener(friendPresenceListener);
    }

    private void updateNoSaveStatusMap(Map<String, NoSave> noSaveMap) {
        synchronized (this) {
            this.noSaveMap = new HashMap<String, NoSave>(noSaveMap);
        }
    }

    private void updateNewPresenceNoSave(FriendPresence presence) {
        String friendId = presence.getFriend().getId();
        NoSave noSaveStatus;

        synchronized (this) {
            noSaveStatus = noSaveMap.get(friendId);
        }
        if (noSaveStatus != null) {
            addNoSaveFeatureIfNecessary(presence, noSaveStatus);
        }
    }

    private void addNoSaveFeatureIfNecessary(FriendPresence presence, NoSave nosave) {
        if (shouldAddFeature(presence, nosave)) {
            presence.addFeature(new NoSaveFeature(new NoSaveStatusImpl(nosave, presence.getFriend().getId())));
        }
    }

    private boolean shouldAddFeature(FriendPresence presence, NoSave nosave) {
        return !(presence.hasFeatures(NoSaveFeature.ID)
                && nosave == ((NoSaveFeature) presence.getFeature(NoSaveFeature.ID)).getFeature().getStatus());
    }

    private class FriendPresenceListener implements EventListener<FriendPresenceEvent> {
        @Override
        public void handleEvent(FriendPresenceEvent event) {
            if (event.getType() == FriendPresenceEvent.Type.ADDED) {
                updateNewPresenceNoSave(event.getData());
            }
        }
    }

    private class NoSaveStatusImpl implements NoSaveStatus {

        private final NoSave noSave;
        private final String userName;

        NoSaveStatusImpl(NoSave noSave, String userName) {
            this.noSave = noSave;
            this.userName = userName;
        }

        @Override
        public NoSave getStatus() {
            return noSave;
        }

        @Override
        public void toggleStatus() throws FriendException {
            NoSaveIQ noSaveMsg = NoSaveIQ.getNoSaveSetMessage(
                    userName, noSave == NoSave.ENABLED ? NoSave.DISABLED : NoSave.ENABLED);
            NoSaveIQListener.this.connection.sendPacket(noSaveMsg);
        }
    }
}
