package org.limewire.ui.swing.friends.chat;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.limewire.core.api.download.DownloadException;
import org.limewire.friend.api.ChatState;
import org.limewire.friend.api.Friend;
import org.limewire.friend.api.FriendConnection;
import org.limewire.friend.api.FriendConnectionEvent;
import org.limewire.friend.api.FriendException;
import org.limewire.io.InvalidDataException;
import org.limewire.ui.compose.integration.ComposeChatCompatBridge;
import org.limewire.ui.compose.integration.ComposeChatCompatFileOffer;
import org.limewire.ui.compose.integration.ComposeChatCompatMessage;
import org.limewire.ui.compose.integration.ComposeChatCompatMessageKind;
import org.limewire.ui.compose.integration.ComposeChatRosterEntry;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.event.ListEventListener;

public class SwingComposeChatCompatBridge implements ComposeChatCompatBridge {
    private final ComposeChatBridge bridge;
    private final Map<ComposeChatCompatBridge.Listener, ComposeChatBridge.Listener> listeners =
            new LinkedHashMap<ComposeChatCompatBridge.Listener, ComposeChatBridge.Listener>();
    private final Map<ChatFriend, PropertyChangeListener> rosterPropertyListeners =
            new IdentityHashMap<ChatFriend, PropertyChangeListener>();
    private final LinkedHashMap<String, ChatFriend> rosterById = new LinkedHashMap<String, ChatFriend>();

    private volatile boolean active;

    private final ListEventListener<ChatFriend> rosterListListener = listChanges -> syncRoster(true);

    public SwingComposeChatCompatBridge(ComposeChatBridge bridge) {
        this.bridge = bridge;
    }

    @Override
    public FriendConnection getCurrentConnection() {
        return bridge.getCurrentConnection();
    }

    @Override
    public FriendConnectionEvent getLastConnectionEvent() {
        return bridge.getLastConnectionEvent();
    }

    @Override
    public synchronized void activate() {
        if (active) {
            return;
        }
        active = true;
        bridge.getFriends().addListEventListener(rosterListListener);
        bridge.activate();
        syncRoster(false);
    }

    @Override
    public synchronized void deactivate() {
        if (!active) {
            return;
        }
        active = false;
        bridge.getFriends().removeListEventListener(rosterListListener);
        detachRosterPropertyListeners();
        rosterById.clear();
        bridge.deactivate();
    }

    @Override
    public synchronized void addListener(final ComposeChatCompatBridge.Listener listener) {
        if (listeners.containsKey(listener)) {
            return;
        }
        ComposeChatBridge.Listener adapted = new ComposeChatBridge.Listener() {
            @Override
            public void connectionChanged(FriendConnectionEvent event) {
                listener.connectionChanged(event);
                syncRoster(true);
            }

            @Override
            public void messageReceived(ComposeChatBridge.ComposeChatMessage message) {
                listener.messageReceived(toCompat(message));
                syncRoster(true);
            }

            @Override
            public void chatStateChanged(String friendId, ChatState state) {
                listener.chatStateChanged(friendId, state);
            }

            @Override
            public void conversationReady(ChatFriend chatFriend) {
                syncRoster(true);
                listener.conversationReady(chatFriend.getID());
            }
        };
        listeners.put(listener, adapted);
        bridge.addListener(adapted);
    }

    @Override
    public synchronized void removeListener(ComposeChatCompatBridge.Listener listener) {
        ComposeChatBridge.Listener adapted = listeners.remove(listener);
        if (adapted != null) {
            bridge.removeListener(adapted);
        }
    }

    @Override
    public List<ComposeChatRosterEntry> rosterSnapshot() {
        syncRoster(false);
        synchronized (this) {
            List<ComposeChatRosterEntry> snapshot = new ArrayList<ComposeChatRosterEntry>(rosterById.size());
            for (ChatFriend friend : rosterById.values()) {
                snapshot.add(toRosterEntry(friend));
            }
            return snapshot;
        }
    }

    @Override
    public Friend friendById(String friendId) {
        synchronized (this) {
            ChatFriend chatFriend = rosterById.get(friendId);
            if (chatFriend != null) {
                return chatFriend.getFriend();
            }
        }
        ChatFriend chatFriend = bridge.getFriend(friendId);
        return chatFriend != null ? chatFriend.getFriend() : null;
    }

    @Override
    public void markConversationViewed(String friendId) {
        ChatFriend chatFriend = bridge.getFriend(friendId);
        if (chatFriend != null) {
            chatFriend.setHasUnviewedMessages(false);
            syncRoster(true);
        }
    }

    @Override
    public void sendMessage(String friendId, String text) throws FriendException {
        ChatFriend chatFriend = bridge.getFriend(friendId);
        if (chatFriend == null) {
            return;
        }
        bridge.ensureConversation(chatFriend).writeMessage(text);
    }

    @Override
    public void closeConversation(String friendId) {
        ChatFriend chatFriend = bridge.getFriend(friendId);
        if (chatFriend == null) {
            return;
        }
        bridge.closeConversation(chatFriend);
        syncRoster(true);
    }

    @Override
    public boolean supportsBrowse(String friendId) {
        return bridge.supportsBrowse(friendId);
    }

    @Override
    public void setChatState(String friendId, ChatState state) throws FriendException {
        bridge.setChatState(friendId, state);
    }

    @Override
    public boolean supportsOffTheRecord(String friendId) {
        return bridge.supportsOffTheRecord(friendId);
    }

    @Override
    public boolean isOffTheRecord(String friendId) {
        return bridge.isOffTheRecord(friendId);
    }

    @Override
    public void toggleOffTheRecord(String friendId) throws FriendException {
        bridge.toggleOffTheRecord(friendId);
        syncRoster(true);
    }

    @Override
    public boolean supportsFileOffers(String friendId) {
        return bridge.supportsFileOffers(friendId);
    }

    @Override
    public void offerFile(String friendId, File file) {
        bridge.offerFile(friendId, file);
    }

    @Override
    public void offerFolder(String friendId, File folder) {
        bridge.offerFolder(friendId, folder);
    }

    @Override
    public void downloadFileOffer(String messageId) throws DownloadException, InvalidDataException {
        bridge.downloadFileOffer(messageId);
    }

    private void syncRoster(boolean notifyListeners) {
        List<ChatFriend> chatFriends = snapshotFriends();
        synchronized (this) {
            LinkedHashMap<String, ChatFriend> desiredById = new LinkedHashMap<String, ChatFriend>(chatFriends.size());
            for (ChatFriend friend : chatFriends) {
                desiredById.put(friend.getID(), friend);
                attachRosterPropertyListener(friend);
            }

            List<Map.Entry<String, ChatFriend>> existingEntries = new ArrayList<Map.Entry<String, ChatFriend>>(rosterById.entrySet());
            for (Map.Entry<String, ChatFriend> entry : existingEntries) {
                ChatFriend replacement = desiredById.get(entry.getKey());
                if (replacement == entry.getValue()) {
                    continue;
                }
                detachRosterPropertyListener(entry.getValue());
                if (replacement == null) {
                    rosterById.remove(entry.getKey());
                }
            }

            for (Map.Entry<String, ChatFriend> entry : desiredById.entrySet()) {
                if (rosterById.get(entry.getKey()) != entry.getValue()) {
                    rosterById.put(entry.getKey(), entry.getValue());
                }
            }
        }

        if (notifyListeners) {
            List<ComposeChatCompatBridge.Listener> listenerSnapshot;
            synchronized (this) {
                listenerSnapshot = new ArrayList<ComposeChatCompatBridge.Listener>(listeners.keySet());
            }
            for (ComposeChatCompatBridge.Listener listener : listenerSnapshot) {
                listener.rosterChanged();
            }
        }
    }

    private List<ChatFriend> snapshotFriends() {
        EventList<ChatFriend> friends = bridge.getFriends();
        friends.getReadWriteLock().readLock().lock();
        try {
            return new ArrayList<ChatFriend>(friends);
        } finally {
            friends.getReadWriteLock().readLock().unlock();
        }
    }

    private synchronized void attachRosterPropertyListener(final ChatFriend friend) {
        if (rosterPropertyListeners.containsKey(friend)) {
            return;
        }
        PropertyChangeListener listener = event -> syncRoster(true);
        friend.addPropertyChangeListener(listener);
        rosterPropertyListeners.put(friend, listener);
    }

    private synchronized void detachRosterPropertyListener(ChatFriend friend) {
        PropertyChangeListener listener = rosterPropertyListeners.remove(friend);
        if (listener != null) {
            friend.removePropertyChangeListener(listener);
        }
    }

    private synchronized void detachRosterPropertyListeners() {
        for (Map.Entry<ChatFriend, PropertyChangeListener> entry : rosterPropertyListeners.entrySet()) {
            entry.getKey().removePropertyChangeListener(entry.getValue());
        }
        rosterPropertyListeners.clear();
    }

    private ComposeChatRosterEntry toRosterEntry(ChatFriend friend) {
        return new ComposeChatRosterEntry(
                friend.getID(),
                friend.getName(),
                friend.getStatus() != null ? friend.getStatus() : "",
                friend.getMode(),
                friend.isSignedIn(),
                friend.hasUnviewedMessages(),
                bridge.supportsBrowse(friend.getID()),
                bridge.supportsOffTheRecord(friend.getID()),
                bridge.supportsFileOffers(friend.getID()));
    }

    private ComposeChatCompatMessage toCompat(ComposeChatBridge.ComposeChatMessage message) {
        return new ComposeChatCompatMessage(
                message.getId(),
                message.getFriendId(),
                message.getSenderName(),
                message.getBody(),
                message.getTimestamp(),
                message.isIncoming(),
                message.isOutgoing(),
                message.isServer(),
                toCompatKind(message.getKind()),
                toCompatFileOffer(message.getFileOffer()));
    }

    private ComposeChatCompatMessageKind toCompatKind(ComposeChatBridge.MessageKind kind) {
        switch (kind) {
        case FILE_OFFER:
            return ComposeChatCompatMessageKind.FILE_OFFER;
        case STATUS:
            return ComposeChatCompatMessageKind.STATUS;
        case TEXT:
        default:
            return ComposeChatCompatMessageKind.TEXT;
        }
    }

    private ComposeChatCompatFileOffer toCompatFileOffer(ComposeChatBridge.ComposeFileOffer offer) {
        if (offer == null) {
            return null;
        }
        return new ComposeChatCompatFileOffer(
                offer.getOfferId(),
                offer.getFileName(),
                offer.getSize(),
                offer.getDescription(),
                new ArrayList<String>(offer.getUrns()),
                offer.getDownloadState(),
                offer.getLocalPath());
    }
}
