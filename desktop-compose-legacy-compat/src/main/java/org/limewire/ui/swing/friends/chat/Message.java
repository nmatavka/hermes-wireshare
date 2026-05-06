package org.limewire.ui.swing.friends.chat;

interface Message {
    enum Type { SENT, RECEIVED, SERVER }

    String getSenderName();
    String getFriendID();
    Type getType();
    long getMessageTimeMillis();
    String format();
}
