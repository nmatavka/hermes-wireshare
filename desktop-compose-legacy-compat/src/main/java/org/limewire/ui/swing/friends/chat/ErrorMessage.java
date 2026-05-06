package org.limewire.ui.swing.friends.chat;

class ErrorMessage extends AbstractMessageImpl {
    static final String SENDER_NAME = "chat server";

    private final Message message;
    private final String errorMessage;

    ErrorMessage(String friendId, String errorMessage, Type type) {
        super(SENDER_NAME, friendId, type);
        this.message = null;
        this.errorMessage = errorMessage;
    }

    ErrorMessage(String errorMessage, Message message) {
        super(message.getSenderName(), message.getFriendID(), message.getType());
        this.message = message;
        this.errorMessage = errorMessage;
    }

    @Override
    public String format() {
        StringBuilder buffer = new StringBuilder();
        if (message != null) {
            buffer.append(message.format());
        }
        buffer.append("<br/><b><font color=red>")
                .append(errorMessage)
                .append("</font></b><br/>");
        return buffer.toString();
    }

    @Override
    public String toString() {
        return errorMessage;
    }
}
