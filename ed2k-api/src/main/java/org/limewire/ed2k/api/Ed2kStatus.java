package org.limewire.ed2k.api;

public final class Ed2kStatus {

    public enum ConnectionState {
        DISCONNECTED,
        CONNECTING,
        CONNECTED
    }

    public enum KadBootstrapState {
        NOT_BOOTSTRAPPED,
        BOOTSTRAPPING,
        BOOTSTRAPPED
    }

    private final boolean running;
    private final ConnectionState serverState;
    private final ConnectionState kadState;
    private final KadBootstrapState kadBootstrapState;
    private final String connectedServerName;
    private final String serverStatusDetail;
    private final int serverCount;
    private final int downloadCount;
    private final int uploadCount;
    private final int sharedFileCount;
    private final int kadContactCount;

    public Ed2kStatus(
            boolean running,
            ConnectionState serverState,
            ConnectionState kadState,
            KadBootstrapState kadBootstrapState,
            String connectedServerName,
            String serverStatusDetail,
            int serverCount,
            int downloadCount,
            int uploadCount,
            int sharedFileCount,
            int kadContactCount) {
        this.running = running;
        this.serverState = serverState;
        this.kadState = kadState;
        this.kadBootstrapState = kadBootstrapState;
        this.connectedServerName = connectedServerName;
        this.serverStatusDetail = serverStatusDetail;
        this.serverCount = serverCount;
        this.downloadCount = downloadCount;
        this.uploadCount = uploadCount;
        this.sharedFileCount = sharedFileCount;
        this.kadContactCount = kadContactCount;
    }

    public boolean isRunning() {
        return running;
    }

    public ConnectionState getServerState() {
        return serverState;
    }

    public ConnectionState getKadState() {
        return kadState;
    }

    public KadBootstrapState getKadBootstrapState() {
        return kadBootstrapState;
    }

    public String getConnectedServerName() {
        return connectedServerName;
    }

    public String getServerStatusDetail() {
        return serverStatusDetail;
    }

    public int getServerCount() {
        return serverCount;
    }

    public int getDownloadCount() {
        return downloadCount;
    }

    public int getUploadCount() {
        return uploadCount;
    }

    public int getSharedFileCount() {
        return sharedFileCount;
    }

    public int getKadContactCount() {
        return kadContactCount;
    }
}
