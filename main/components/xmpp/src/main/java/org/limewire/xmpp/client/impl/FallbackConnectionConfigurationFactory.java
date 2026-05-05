package org.limewire.xmpp.client.impl;

import java.util.List;

import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.limewire.friend.api.FriendConnectionConfiguration;
import org.limewire.io.UnresolvedIpPort;
import org.jxmpp.jid.DomainBareJid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;

/**
 * Uses the list of default servers in the XMPPConnectionConfiguration to return
 * a ConnectionConfiguration. 
 */
public class FallbackConnectionConfigurationFactory implements ConnectionConfigurationFactory {

    @Override
    public boolean hasMore(FriendConnectionConfiguration connectionConfiguration, RequestContext requestContext) {
        return requestContext.getNumRequests() < connectionConfiguration.getDefaultServers().size();
    }

    @Override
    public XMPPTCPConnectionConfiguration getConnectionConfiguration(FriendConnectionConfiguration connectionConfiguration,
                                                                     RequestContext requestContext) {
        checkHasMore(connectionConfiguration, requestContext);
        List<UnresolvedIpPort> defaultServers = connectionConfiguration.getDefaultServers();
        UnresolvedIpPort defaultServer = defaultServers.get(requestContext.getNumRequests());
        try {
            DomainBareJid domain = JidCreate.domainBareFrom(connectionConfiguration.getServiceName());
            return XMPPTCPConnectionConfiguration.builder()
                    .setXmppDomain(domain)
                    .setHost(defaultServer.getAddress())
                    .setPort(defaultServer.getPort())
                    .build();
        } catch (XmppStringprepException e) {
            throw new IllegalArgumentException("invalid service name: " + connectionConfiguration.getServiceName(), e);
        }
    }

    private void checkHasMore(FriendConnectionConfiguration connectionConfiguration, RequestContext requestContext) {
        if(!hasMore(connectionConfiguration, requestContext)) {
            throw new IllegalStateException("no more ConnectionConfigurations");
        }
    }
}
