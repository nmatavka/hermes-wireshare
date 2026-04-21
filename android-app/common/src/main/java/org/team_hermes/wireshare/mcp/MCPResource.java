package org.team_hermes.wireshare.mcp;

public interface MCPResource {
    String uri();
    String name();
    String description();
    String mimeType();
    String read();
}
