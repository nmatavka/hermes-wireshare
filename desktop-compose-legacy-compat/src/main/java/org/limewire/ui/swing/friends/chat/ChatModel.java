package org.limewire.ui.swing.friends.chat;

import java.awt.EventQueue;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.limewire.friend.api.Friend;
import org.limewire.friend.api.FriendPresence;
import org.limewire.friend.api.FriendPresenceEvent;
import org.limewire.friend.api.IncomingChatListener;
import org.limewire.friend.api.MessageReader;
import org.limewire.friend.api.MessageWriter;
import org.limewire.inject.LazySingleton;
import org.limewire.listener.EventBroadcaster;
import org.limewire.listener.EventListener;
import org.limewire.listener.ListenerSupport;
import org.limewire.listener.SwingEDTEvent;

import com.google.inject.Inject;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;

@LazySingleton
class ChatModel {
    private final EventList<ChatFriend> chatFriends = new BasicEventList<ChatFriend>();
    private final Map<String, ChatFriend> idToFriendMap = new HashMap<String, ChatFriend>();
    private final ListenerSupport<FriendPresenceEvent> presenceSupport;
    private final EventBroadcaster<ChatMessageEvent> chatMessageList;
    private final EventBroadcaster<ChatStateEvent> chatStateList;
    private final List<IncomingListener> incomingListeners = new CopyOnWriteArrayList<IncomingListener>();

    private PresenceListener presenceEvent;

    @Inject
    ChatModel(ListenerSupport<FriendPresenceEvent> presenceSupport,
            EventBroadcaster<ChatMessageEvent> chatMessageList,
            EventBroadcaster<ChatStateEvent> chatStateList) {
        this.presenceSupport = presenceSupport;
        this.chatMessageList = chatMessageList;
        this.chatStateList = chatStateList;
    }

    EventList<ChatFriend> getChatFriendList() {
        return chatFriends;
    }

    ChatFriend getChatFriend(String friendId) {
        return idToFriendMap.get(friendId);
    }

    void addIncomingListener(IncomingListener listener) {
        incomingListeners.add(listener);
    }

    void removeIncomingListener(IncomingListener listener) {
        incomingListeners.remove(listener);
    }

    void registerListeners() {
        if (presenceEvent == null) {
            presenceEvent = new PresenceListener();
        }
        presenceSupport.addListener(presenceEvent);
    }

    void unregisterListeners() {
        if (presenceEvent != null) {
            presenceSupport.removeListener(presenceEvent);
        }
        idToFriendMap.clear();
        chatFriends.clear();
    }

    void handlePresenceEvent(FriendPresenceEvent event) {
        FriendPresence presence = event.getData();
        Friend friend = presence.getFriend();
        ChatFriend chatFriend = idToFriendMap.get(friend.getId());
        switch (event.getType()) {
        case ADDED:
            addFriend(chatFriend, presence);
            break;
        case UPDATE:
            if (chatFriend != null) {
                chatFriend.update();
            }
            break;
        case REMOVED:
            if (chatFriend != null) {
                removeFriendIfNecessary(chatFriend);
                chatFriend.update();
            }
            break;
        }
    }

    void removeFriendIfNecessary(ChatFriend chatFriend) {
        if (!chatFriend.isChatting() && !chatFriend.isSignedIn()) {
            Friend friend = chatFriend.getFriend();
            chatFriends.remove(idToFriendMap.remove(friend.getId()));
            friend.removeChatListener();
        }
    }

    private void addFriend(ChatFriend chatFriend, final FriendPresence presence) {
        if (chatFriend == null) {
            chatFriend = new ChatFriendImpl(presence);
            chatFriends.add(chatFriend);
            idToFriendMap.put(presence.getFriend().getId(), chatFriend);
        }

        final ChatFriend friend = chatFriend;
        IncomingChatListener incomingChatListener = new IncomingChatListener() {
            @Override
            public MessageReader incomingChat(final MessageWriter writer) {
                runOnEventQueue(new Runnable() {
                    @Override
                    public void run() {
                        MessageWriter writerWrapper = new MessageWriterImpl(friend, writer, chatMessageList);
                        fireIncomingEvent(friend, writerWrapper);
                    }
                });
                return new MessageReaderImpl(friend, chatMessageList, chatStateList);
            }
        };
        presence.getFriend().setChatListenerIfNecessary(incomingChatListener);
        chatFriend.update();
    }

    private void fireIncomingEvent(ChatFriend chatFriend, MessageWriter messageWriter) {
        for (IncomingListener listener : incomingListeners) {
            listener.incomingChat(chatFriend, messageWriter);
        }
    }

    private static void runOnEventQueue(Runnable runnable) {
        if (EventQueue.isDispatchThread()) {
            runnable.run();
            return;
        }
        try {
            EventQueue.invokeAndWait(runnable);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e.getCause());
        }
    }

    private final class PresenceListener implements EventListener<FriendPresenceEvent> {
        @Override
        @SwingEDTEvent
        public void handleEvent(FriendPresenceEvent event) {
            handlePresenceEvent(event);
        }
    }
}
