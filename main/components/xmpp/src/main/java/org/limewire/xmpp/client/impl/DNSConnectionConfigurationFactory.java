package org.limewire.xmpp.client.impl;

import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.limewire.logging.Log;
import org.limewire.logging.LogFactory;
import org.limewire.friend.api.FriendConnectionConfiguration;
import org.jxmpp.jid.DomainBareJid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;

/**
 * Creates a Smack TCP configuration that lets modern Smack perform its own DNS lookup through
 * its configured resolver stack.
 */
public class DNSConnectionConfigurationFactory implements ConnectionConfigurationFactory {
    
    private static final Log LOG = LogFactory.getLog(DNSConnectionConfigurationFactory.class);

    @Override
    public boolean hasMore(FriendConnectionConfiguration connectionConfiguration, RequestContext requestContext) {
        return requestContext.getNumRequests() == 0;
    }

    /**
     * Smack 4.5 performs SRV resolution internally via MiniDNS, so the DNS-backed
     * configuration only needs the target XMPP domain.
     */
    public XMPPTCPConnectionConfiguration getConnectionConfiguration(FriendConnectionConfiguration configuration,
                                                                     RequestContext requestContext) {
        checkHasMore(configuration, requestContext);
        try {
            DomainBareJid domain = JidCreate.domainBareFrom(configuration.getServiceName());
            return XMPPTCPConnectionConfiguration.builder()
                    .setXmppDomain(domain)
                    .build();
        } catch (XmppStringprepException e) {
            LOG.debug("Failed to build DNS-backed XMPP domain", e);
            throw new IllegalArgumentException("invalid service name: " + configuration.getServiceName(), e);
        }
    }
    
    private void checkHasMore(FriendConnectionConfiguration connectionConfiguration, RequestContext requestContext) {
        if(!hasMore(connectionConfiguration, requestContext)) {
            throw new IllegalArgumentException("no more ConnectionConfigurations");
        }
    }
    
}
