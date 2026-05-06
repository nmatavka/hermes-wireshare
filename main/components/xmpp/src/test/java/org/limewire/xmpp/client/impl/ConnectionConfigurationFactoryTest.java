package org.limewire.xmpp.client.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.junit.Test;
import org.limewire.friend.api.FriendConnectionConfiguration;
import org.limewire.io.UnresolvedIpPort;
import org.limewire.io.UnresolvedIpPortImpl;
import org.limewire.listener.EventListener;
import org.limewire.friend.api.Network;
import org.limewire.friend.api.RosterEvent;

public class ConnectionConfigurationFactoryTest {

    @Test
    public void dnsFactoryUsesXmppDomainWithoutPinnedHost() {
        DNSConnectionConfigurationFactory factory = new DNSConnectionConfigurationFactory();
        ConnectionConfigurationFactory.RequestContext requestContext = new ConnectionConfigurationFactory.RequestContext();
        FriendConnectionConfiguration configuration = new TestFriendConnectionConfiguration(
                "example.com",
                Arrays.<UnresolvedIpPort>asList(new UnresolvedIpPortImpl("fallback.example.com", 5222)));

        assertTrue(factory.hasMore(configuration, requestContext));
        XMPPTCPConnectionConfiguration smackConfig = factory.getConnectionConfiguration(configuration, requestContext);

        assertEquals("example.com", smackConfig.getXMPPServiceDomain().toString());
        assertNull(smackConfig.getHost());
        requestContext.incrementRequests();
        assertFalse(factory.hasMore(configuration, requestContext));
    }

    @Test
    public void fallbackFactoryPreservesConfiguredServerOrder() {
        FallbackConnectionConfigurationFactory factory = new FallbackConnectionConfigurationFactory();
        ConnectionConfigurationFactory.RequestContext requestContext = new ConnectionConfigurationFactory.RequestContext();
        FriendConnectionConfiguration configuration = new TestFriendConnectionConfiguration(
                "example.com",
                Arrays.<UnresolvedIpPort>asList(
                        new UnresolvedIpPortImpl("one.example.com", 5223),
                        new UnresolvedIpPortImpl("two.example.com", 5224)));

        assertTrue(factory.hasMore(configuration, requestContext));
        XMPPTCPConnectionConfiguration first = factory.getConnectionConfiguration(configuration, requestContext);
        assertEquals("one.example.com", first.getHost().toString());
        assertEquals(5223, first.getPort().intValue());

        requestContext.incrementRequests();
        assertTrue(factory.hasMore(configuration, requestContext));
        XMPPTCPConnectionConfiguration second = factory.getConnectionConfiguration(configuration, requestContext);
        assertEquals("two.example.com", second.getHost().toString());
        assertEquals(5224, second.getPort().intValue());

        requestContext.incrementRequests();
        assertFalse(factory.hasMore(configuration, requestContext));
    }

    private static final class TestFriendConnectionConfiguration implements FriendConnectionConfiguration {
        private final String serviceName;
        private final List<UnresolvedIpPort> defaultServers;

        private TestFriendConnectionConfiguration(String serviceName, List<UnresolvedIpPort> defaultServers) {
            this.serviceName = serviceName;
            this.defaultServers = defaultServers;
        }

        @Override
        public boolean isDebugEnabled() {
            return false;
        }

        @Override
        public String getUserInputLocalID() {
            return "tester";
        }

        @Override
        public String getPassword() {
            return "password";
        }

        @Override
        public String getLabel() {
            return "Test";
        }

        @Override
        public String getServiceName() {
            return serviceName;
        }

        @Override
        public String getResource() {
            return "WireShare";
        }

        @Override
        public EventListener<RosterEvent> getRosterListener() {
            return null;
        }

        @Override
        public List<UnresolvedIpPort> getDefaultServers() {
            return defaultServers;
        }

        @Override
        public void setAttribute(String key, Object property) {
        }

        @Override
        public Object getAttribute(String key) {
            return null;
        }

        @Override
        public String getNetworkName() {
            return serviceName;
        }

        @Override
        public String getCanonicalizedLocalID() {
            return getUserInputLocalID();
        }

        @Override
        public Type getType() {
            return Network.Type.XMPP;
        }
    }
}
