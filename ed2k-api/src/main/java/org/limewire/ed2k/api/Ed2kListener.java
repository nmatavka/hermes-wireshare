package org.limewire.ed2k.api;

public interface Ed2kListener {

    default void downloadsChanged() {
    }

    default void uploadsChanged() {
    }

    default void statusChanged() {
    }

    default void searchChanged(String sessionId) {
    }
}
