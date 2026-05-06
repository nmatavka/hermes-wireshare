package org.limewire.ed2k.api;

public final class Ed2kOpenLinkResult {

    private final Ed2kLinkTargetType targetType;
    private final boolean transferStarted;
    private final String statusMessage;

    public Ed2kOpenLinkResult(Ed2kLinkTargetType targetType, boolean transferStarted, String statusMessage) {
        this.targetType = targetType;
        this.transferStarted = transferStarted;
        this.statusMessage = statusMessage;
    }

    public Ed2kLinkTargetType getTargetType() {
        return targetType;
    }

    public boolean isTransferStarted() {
        return transferStarted;
    }

    public String getStatusMessage() {
        return statusMessage;
    }
}
