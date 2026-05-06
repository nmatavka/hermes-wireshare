package org.limewire.ui.swing.friends.chat;

import java.util.StringTokenizer;

class MessageTextImpl extends AbstractMessageImpl implements MessageText {
    private final String message;

    MessageTextImpl(String senderName, String chatFriendId, Type type, String message) {
        super(senderName, chatFriendId, type);
        this.message = message;
    }

    @Override
    public String getMessageText() {
        return message;
    }

    @Override
    public String toString() {
        return message;
    }

    @Override
    public String format() {
        StringTokenizer tokenizer = new StringTokenizer(escape(message), " \n\t\r");
        StringBuilder html = new StringBuilder();
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            if (URLWrapper.isURL(token)) {
                html.append(URLWrapper.createAnchorTag(token, token));
            } else {
                html.append(token);
            }
            if (tokenizer.hasMoreTokens()) {
                html.append(' ');
            }
        }
        return html.toString();
    }

    private static String escape(String value) {
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
