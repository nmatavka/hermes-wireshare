package org.limewire.ui.swing.friends.chat;

import org.limewire.friend.api.ChatState;
import org.limewire.listener.DefaultSourceTypeEvent;

public class ChatStateEvent extends DefaultSourceTypeEvent<String, ChatState> {
    public ChatStateEvent(String friendId, ChatState event) {
        super(friendId, event);
    }
}
