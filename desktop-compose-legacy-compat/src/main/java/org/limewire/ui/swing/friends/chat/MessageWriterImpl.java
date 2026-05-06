package org.limewire.ui.swing.friends.chat;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.limewire.concurrent.ThreadExecutor;
import org.limewire.friend.api.ChatState;
import org.limewire.friend.api.FriendException;
import org.limewire.friend.api.MessageWriter;
import org.limewire.listener.EventBroadcaster;
import org.limewire.ui.desktop.util.I18n;

class MessageWriterImpl implements MessageWriter {
    private static final Logger LOG = Logger.getLogger(MessageWriterImpl.class.getName());

    private final ChatFriend chatFriend;
    private final MessageWriter writer;
    private final EventBroadcaster<ChatMessageEvent> messageList;

    MessageWriterImpl(ChatFriend chatFriend, MessageWriter writer,
            EventBroadcaster<ChatMessageEvent> messageList) {
        this.chatFriend = chatFriend;
        this.writer = writer;
        this.messageList = messageList;
    }

    @Override
    public void writeMessage(final String message) throws FriendException {
        final Message msg = new MessageTextImpl(I18n.tr("me"), chatFriend.getID(), Message.Type.SENT, message);
        if (chatFriend.isSignedIn()) {
            ThreadExecutor.startThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        writer.writeMessage(message);
                    } catch (FriendException e) {
                        LOG.log(Level.WARNING, "send message failed", e);
                    }
                }
            }, "send-message");
            messageList.broadcast(new ChatMessageEvent(msg));
        } else {
            messageList.broadcast(new ChatMessageEvent(
                    new ErrorMessage(I18n.tr("Message not sent because friend signed off."), msg)));
        }
    }

    @Override
    public void setChatState(final ChatState chatState) throws FriendException {
        if (!chatFriend.isSignedIn()) {
            return;
        }
        ThreadExecutor.startThread(new Runnable() {
            @Override
            public void run() {
                try {
                    writer.setChatState(chatState);
                } catch (FriendException e) {
                    LOG.log(Level.WARNING, "set chat state failed", e);
                }
            }
        }, "set-chat-state");
    }
}
