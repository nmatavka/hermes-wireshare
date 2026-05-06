package org.limewire.ed2k.jed2k;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.limewire.ed2k.api.Ed2kLinkTargetType;

public class EmuleLinkTest {

    @Test
    public void parsesFileLinks() {
        EmuleLink link = EmuleLink.parse("ed2k://|file|Example.bin|12345|0123456789ABCDEF0123456789ABCDEF|/");

        assertEquals(Ed2kLinkTargetType.FILE, link.getType());
        assertEquals("Example.bin", link.getStringValue());
        assertEquals(12345L, link.getNumberValue());
        assertEquals("0123456789ABCDEF0123456789ABCDEF", link.getHashValue());
    }

    @Test
    public void parsesServerLinks() {
        EmuleLink link = EmuleLink.parse("ed2k://|server|server.example|4661|/");

        assertEquals(Ed2kLinkTargetType.SERVER, link.getType());
        assertEquals("server.example", link.getStringValue());
        assertEquals(4661L, link.getNumberValue());
    }

    @Test
    public void parsesServerListAndNodesListLinks() {
        EmuleLink serverList = EmuleLink.parse("ed2k://|serverlist|https://example.com/server.met|/");
        EmuleLink nodesList = EmuleLink.parse("ed2k://|nodeslist|https://example.com/nodes.dat|/");

        assertEquals(Ed2kLinkTargetType.SERVERLIST, serverList.getType());
        assertEquals("https://example.com/server.met", serverList.getStringValue());
        assertEquals(Ed2kLinkTargetType.NODESLIST, nodesList.getType());
        assertEquals("https://example.com/nodes.dat", nodesList.getStringValue());
    }
}
