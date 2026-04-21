package org.team_hermes.wireshare.mcp;

import com.google.gson.JsonObject;

public interface MCPNotification {
    String method();
    JsonObject payload();
}
