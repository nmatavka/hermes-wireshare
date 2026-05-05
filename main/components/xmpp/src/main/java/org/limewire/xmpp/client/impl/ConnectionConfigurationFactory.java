package org.limewire.xmpp.client.impl;

import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.limewire.friend.api.FriendConnectionConfiguration;

/**
 * Creates connection configurations for XMPP friend connections. Typically backed by a
 * collection of hosts and ports.
 */
public interface ConnectionConfigurationFactory {

    /**
     * Used to track state between multiple calls to hasMore and getConnectionConfiguration.
     */
    class RequestContext {
        private int numRequests;
        
        int getNumRequests() {
            return numRequests;
        }
        
        void incrementRequests() {
            numRequests++;
        }
    }

    /**
     * @param requestContext; callers should increment the requests after each call to 
     * getConnectionConfiguration 
     * @return whether there are remaining ConnectionConfigurations that can be retrieved
     */
    boolean hasMore(FriendConnectionConfiguration connectionConfiguration, RequestContext requestContext);
    
    /**
     * Looks up the next Smack TCP configuration for the given XMPP connection configuration.
     * @return a connection configuration; never null
     * @throws IllegalStateException is hasMore returns false
     */
    XMPPTCPConnectionConfiguration getConnectionConfiguration(
            FriendConnectionConfiguration connectionConfiguration,
            RequestContext requestContext);
}
