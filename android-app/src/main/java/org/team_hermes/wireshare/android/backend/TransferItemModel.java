package org.team_hermes.wireshare.android.backend;

public final class TransferItemModel {
    public final String backendId;
    public final String protocol;
    public final String displayName;
    public final String state;
    public final long size;
    public final int progress;

    public TransferItemModel(String backendId,
                             String protocol,
                             String displayName,
                             String state,
                             long size,
                             int progress) {
        this.backendId = backendId;
        this.protocol = protocol;
        this.displayName = displayName;
        this.state = state;
        this.size = size;
        this.progress = progress;
    }
}
