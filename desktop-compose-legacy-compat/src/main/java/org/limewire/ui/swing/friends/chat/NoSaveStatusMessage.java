package org.limewire.ui.swing.friends.chat;

import org.limewire.friend.impl.feature.NoSave;
import org.limewire.ui.desktop.util.I18n;

class NoSaveStatusMessage extends AbstractMessageImpl {
    static final String SENDER_NAME = "chat server";

    private final NoSave status;

    NoSaveStatusMessage(String friendId, Type type, NoSave status) {
        super(SENDER_NAME, friendId, type);
        this.status = status;
    }

    @Override
    public String format() {
        return "<br/><b>" + getForDisplay() + "</b><br/>";
    }

    @Override
    public String toString() {
        return getForDisplay();
    }

    NoSave getStatus() {
        return status;
    }

    private String getForDisplay() {
        return status == NoSave.ENABLED
                ? I18n.tr("Chat is now off the record")
                : I18n.tr("Chat is now on the record");
    }
}
