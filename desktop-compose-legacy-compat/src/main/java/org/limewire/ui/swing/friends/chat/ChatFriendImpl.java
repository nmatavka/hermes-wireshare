package org.limewire.ui.swing.friends.chat;

import java.awt.EventQueue;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Timer;
import java.util.TimerTask;

import org.limewire.friend.api.Friend;
import org.limewire.friend.api.FriendPresence;
import org.limewire.friend.api.MessageReader;
import org.limewire.friend.api.MessageWriter;
import org.limewire.friend.api.feature.LimewireFeature;

class ChatFriendImpl implements ChatFriend {
    private final PropertyChangeSupport propertyChanges = new PropertyChangeSupport(this);
    private final Friend friend;

    private boolean chatting;
    private String status;
    private FriendPresence.Mode mode;
    private long chatStartTime;
    private boolean hasUnviewedMessages;
    private Timer timer;
    private int flashCount;

    ChatFriendImpl(FriendPresence presence) {
        this.friend = presence.getFriend();
        this.status = presence.getStatus();
        this.mode = presence.getMode();
    }

    @Override
    public Friend getFriend() {
        return friend;
    }

    @Override
    public String getID() {
        return friend.getId();
    }

    @Override
    public String getName() {
        return friend.getRenderName();
    }

    @Override
    public String getStatus() {
        return status;
    }

    @Override
    public FriendPresence.Mode getMode() {
        return mode;
    }

    @Override
    public boolean isChatting() {
        return chatting;
    }

    @Override
    public boolean isSignedInToLimewire() {
        for (FriendPresence presence : friend.getPresences().values()) {
            if (presence.getFeature(LimewireFeature.ID) != null) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isSignedIn() {
        return friend.isSignedIn();
    }

    @Override
    public void startChat() {
        if (!chatting) {
            chatStartTime = System.currentTimeMillis();
            setChatting(true);
        }
    }

    @Override
    public void stopChat() {
        stopTimer();
        setChatting(false);
        setHasUnviewedMessages(false);
    }

    @Override
    public long getChatStartTime() {
        return chatStartTime;
    }

    @Override
    public boolean hasUnviewedMessages() {
        return hasUnviewedMessages;
    }

    @Override
    public boolean isFlashState() {
        return flashCount % 2 == 0;
    }

    @Override
    public void setHasUnviewedMessages(boolean hasMessages) {
        if (hasMessages) {
            startTimer();
        } else {
            stopTimer();
        }
        boolean old = this.hasUnviewedMessages;
        this.hasUnviewedMessages = hasMessages;
        firePropertyChange("receivingUnviewedMessages", old, hasMessages);
    }

    @Override
    public MessageWriter createChat(MessageReader reader) {
        return friend.createChat(reader);
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChanges.addPropertyChangeListener(listener);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChanges.removePropertyChangeListener(listener);
    }

    @Override
    public void update() {
        FriendPresence presence = getPresenceForModeAndStatus();
        if (presence != null) {
            setStatus(presence.getStatus());
            setMode(presence.getMode());
        }
    }

    private void setChatting(final boolean chatting) {
        final boolean oldChatting = this.chatting;
        this.chatting = chatting;
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                propertyChanges.firePropertyChange("chatting", oldChatting, chatting);
            }
        });
    }

    private void setStatus(String status) {
        String old = this.status;
        this.status = status;
        firePropertyChange("status", old, status);
    }

    private void setMode(FriendPresence.Mode mode) {
        FriendPresence.Mode old = this.mode;
        this.mode = mode;
        firePropertyChange("mode", old, mode);
    }

    private void startTimer() {
        if (timer != null) {
            flashCount = 0;
            return;
        }
        timer = new Timer("chat-flash-" + getID(), true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (flashCount > 4) {
                    stopTimer();
                    return;
                }
                final int oldValue = flashCount;
                flashCount += 1;
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        propertyChanges.firePropertyChange("flashIncrement", oldValue, flashCount);
                    }
                });
            }
        }, 1500L, 1500L);
    }

    private synchronized void stopTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        flashCount = 0;
    }

    private void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        propertyChanges.firePropertyChange(propertyName, oldValue, newValue);
    }

    private FriendPresence getPresenceForModeAndStatus() {
        ArrayList<FriendPresence> presences = new ArrayList<FriendPresence>(friend.getPresences().values());
        Collections.sort(presences, new ModeAndPriorityPresenceComparator());
        return presences.isEmpty() ? null : presences.get(presences.size() - 1);
    }

    private static final class ModeAndPriorityPresenceComparator implements Comparator<FriendPresence> {
        @Override
        public int compare(FriendPresence o1, FriendPresence o2) {
            if (!o1.getMode().equals(o2.getMode())) {
                if (o1.getMode() == FriendPresence.Mode.available) {
                    return 1;
                } else if (o2.getMode() == FriendPresence.Mode.available) {
                    return -1;
                }
            }
            return Integer.valueOf(o1.getPriority()).compareTo(o2.getPriority());
        }
    }
}
