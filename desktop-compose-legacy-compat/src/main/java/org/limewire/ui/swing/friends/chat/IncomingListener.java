package org.limewire.ui.swing.friends.chat;

import org.limewire.friend.api.MessageWriter;

public interface IncomingListener {
    void incomingChat(ChatFriend chatFriend, MessageWriter messageWriter);
}
