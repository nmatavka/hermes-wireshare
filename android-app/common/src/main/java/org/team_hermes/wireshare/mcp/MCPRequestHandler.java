package org.team_hermes.wireshare.mcp;

import com.google.gson.JsonObject;

public interface MCPRequestHandler {
    JsonObject handleRequest(JsonObject request);
}
