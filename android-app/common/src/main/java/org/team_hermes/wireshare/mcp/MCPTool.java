package org.team_hermes.wireshare.mcp;

import com.google.gson.JsonObject;

public interface MCPTool {
    String name();
    String description();
    JsonObject inputSchema();
    JsonObject execute(JsonObject arguments);
}
