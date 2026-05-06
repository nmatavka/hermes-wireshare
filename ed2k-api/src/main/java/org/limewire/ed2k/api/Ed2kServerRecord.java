package org.limewire.ed2k.api;

public final class Ed2kServerRecord {

    private final String address;
    private final int port;
    private final String name;
    private final String description;
    private final int userCount;
    private final int fileCount;
    private final boolean connected;

    public Ed2kServerRecord(
            String address,
            int port,
            String name,
            String description,
            int userCount,
            int fileCount,
            boolean connected) {
        this.address = address == null ? "" : address;
        this.port = port;
        this.name = name == null ? "" : name;
        this.description = description == null ? "" : description;
        this.userCount = userCount;
        this.fileCount = fileCount;
        this.connected = connected;
    }

    public String getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getUserCount() {
        return userCount;
    }

    public int getFileCount() {
        return fileCount;
    }

    public boolean isConnected() {
        return connected;
    }
}
