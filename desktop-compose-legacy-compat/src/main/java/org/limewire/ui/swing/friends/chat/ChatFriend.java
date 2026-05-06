package org.limewire.ui.swing.friends.chat;

import java.beans.PropertyChangeListener;

import org.limewire.friend.api.Friend;
import org.limewire.friend.api.FriendPresence;
import org.limewire.friend.api.MessageReader;
import org.limewire.friend.api.MessageWriter;

public interface ChatFriend {
    Friend getFriend();
    String getID();
    String getName();
    String getStatus();
    FriendPresence.Mode getMode();
    boolean isChatting();
    boolean isSignedInToLimewire();
    boolean isSignedIn();
    void startChat();
    void stopChat();
    long getChatStartTime();
    boolean hasUnviewedMessages();
    boolean isFlashState();
    void setHasUnviewedMessages(boolean hasMessages);
    MessageWriter createChat(MessageReader reader);
    void addPropertyChangeListener(PropertyChangeListener listener);
    void removePropertyChangeListener(PropertyChangeListener listener);
    void update();
}
