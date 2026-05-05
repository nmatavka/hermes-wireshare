package org.limewire.xmpp.client.impl.messages;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.Map;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.util.PacketParserUtils;
import org.jxmpp.JxmppContext;
import org.junit.BeforeClass;
import org.junit.Test;
import org.limewire.friend.api.FileMetaData;
import org.limewire.friend.api.feature.AuthToken;
import org.limewire.friend.impl.FileMetaDataImpl;
import org.limewire.friend.impl.feature.AuthTokenImpl;
import org.limewire.friend.impl.feature.NoSave;
import org.limewire.io.ConnectableImpl;
import org.limewire.io.GUID;
import org.limewire.net.ConnectBackRequest;
import org.limewire.net.address.AddressFactoryImpl;
import org.limewire.net.address.ConnectableSerializer;
import org.limewire.xmpp.client.impl.messages.address.AddressIQ;
import org.limewire.xmpp.client.impl.messages.address.AddressIQProvider;
import org.limewire.xmpp.client.impl.messages.authtoken.AuthTokenIQ;
import org.limewire.xmpp.client.impl.messages.authtoken.AuthTokenIQProvider;
import org.limewire.xmpp.client.impl.messages.connectrequest.ConnectBackRequestIQ;
import org.limewire.xmpp.client.impl.messages.connectrequest.ConnectBackRequestIQProvider;
import org.limewire.xmpp.client.impl.messages.filetransfer.FileTransferIQ;
import org.limewire.xmpp.client.impl.messages.library.LibraryChangedIQ;
import org.limewire.xmpp.client.impl.messages.nosave.NoSaveIQ;

public class XmppIqRoundTripTest {

    private static AddressFactoryImpl addressFactory;

    @BeforeClass
    public static void registerProviders() {
        addressFactory = new AddressFactoryImpl();
        addressFactory.registerSerializer(new ConnectableSerializer());
        ProviderManager.addIQProvider(AddressIQ.ELEMENT, AddressIQ.NAMESPACE, new AddressIQProvider(addressFactory));
        ProviderManager.addIQProvider(AuthTokenIQ.ELEMENT, AuthTokenIQ.NAMESPACE, new AuthTokenIQProvider());
        ProviderManager.addIQProvider(ConnectBackRequestIQ.ELEMENT_NAME, ConnectBackRequestIQ.NAME_SPACE,
                new ConnectBackRequestIQProvider());
        ProviderManager.addIQProvider(FileTransferIQ.ELEMENT, FileTransferIQ.NAMESPACE, FileTransferIQ.getIQProvider());
        ProviderManager.addIQProvider(LibraryChangedIQ.ELEMENT, LibraryChangedIQ.NAMESPACE, LibraryChangedIQ.getIQProvider());
        ProviderManager.addIQProvider(NoSaveIQ.ELEMENT_NAME, NoSaveIQ.NAME_SPACE, NoSaveIQ.getIQProvider());
    }

    @Test
    public void addressAndAuthTokenIqRoundTrip() throws Exception {
        AddressIQ addressIQ = new AddressIQ(new ConnectableImpl("1.2.3.4", 6346, false), addressFactory);
        addressIQ.setType(IQ.Type.set);
        AddressIQ parsedAddress = (AddressIQ) parse(asIqXml(addressIQ.toXML().toString(), AddressIQ.ELEMENT, AddressIQ.NAMESPACE, IQ.Type.set));
        ConnectableImpl parsedConnectable = (ConnectableImpl) parsedAddress.getAddress();
        assertEquals("1.2.3.4", parsedConnectable.getAddress());
        assertEquals(6346, parsedConnectable.getPort());

        AuthToken token = new AuthTokenImpl("ZmFrZS10b2tlbg==");
        AuthTokenIQ authTokenIQ = new AuthTokenIQ(token);
        authTokenIQ.setType(IQ.Type.set);
        AuthTokenIQ parsedAuthToken = (AuthTokenIQ) parse(asIqXml(authTokenIQ.toXML().toString(), AuthTokenIQ.ELEMENT,
                AuthTokenIQ.NAMESPACE, IQ.Type.set));
        assertEquals(token.getBase64(), parsedAuthToken.getAuthToken().getBase64());
    }

    @Test
    public void connectBackAndFileTransferIqRoundTrip() throws Exception {
        ConnectBackRequest request = new ConnectBackRequest(new ConnectableImpl("5.6.7.8", 6347, false), new GUID(), 1);
        ConnectBackRequestIQ connectBackRequestIQ = new ConnectBackRequestIQ(request);
        connectBackRequestIQ.setType(IQ.Type.set);
        ConnectBackRequestIQ parsedConnectBack = (ConnectBackRequestIQ) parse(asIqXml(connectBackRequestIQ.toXML().toString(),
                ConnectBackRequestIQ.ELEMENT_NAME, ConnectBackRequestIQ.NAME_SPACE, IQ.Type.set));
        assertEquals(request.getClientGuid(), parsedConnectBack.getConnectBackRequest().getClientGuid());
        assertEquals(request.getSupportedFWTVersion(), parsedConnectBack.getConnectBackRequest().getSupportedFWTVersion());
        assertEquals(request.getAddress(), parsedConnectBack.getConnectBackRequest().getAddress());

        FileTransferIQ fileTransferIQ = new FileTransferIQ(sampleFileMetaData(), FileTransferIQ.TransferType.OFFER);
        fileTransferIQ.setType(IQ.Type.get);
        FileTransferIQ parsedFileTransfer = (FileTransferIQ) parse(asIqXml(fileTransferIQ.toXML().toString(),
                FileTransferIQ.ELEMENT, FileTransferIQ.NAMESPACE, IQ.Type.get));
        assertEquals(FileTransferIQ.TransferType.OFFER, parsedFileTransfer.getTransferType());
        assertEquals(sampleFileMetaData().getSerializableMap(), parsedFileTransfer.getFileMetaData().getSerializableMap());
    }

    @Test
    public void noSaveAndLibraryChangedIqRoundTrip() throws Exception {
        NoSaveIQ noSaveIQ = NoSaveIQ.getNoSaveSetMessage("friend@example.com", NoSave.ENABLED);
        NoSaveIQ parsedNoSave = (NoSaveIQ) parse(asIqXml(noSaveIQ.toXML().toString(), NoSaveIQ.ELEMENT_NAME,
                NoSaveIQ.NAME_SPACE, IQ.Type.set));
        Map<String, NoSave> noSaveUsers = parsedNoSave.getNoSaveUsers();
        assertEquals(NoSave.ENABLED, noSaveUsers.get("friend@example.com"));

        LibraryChangedIQ libraryChangedIQ = new LibraryChangedIQ();
        libraryChangedIQ.setType(IQ.Type.set);
        Stanza parsedLibraryChanged = parse(asIqXml(libraryChangedIQ.toXML().toString(), LibraryChangedIQ.ELEMENT,
                LibraryChangedIQ.NAMESPACE, IQ.Type.set));
        assertTrue(parsedLibraryChanged instanceof LibraryChangedIQ);
    }

    private static FileMetaData sampleFileMetaData() {
        FileMetaDataImpl fileMetaData = new FileMetaDataImpl();
        fileMetaData.setId("file-id");
        fileMetaData.setName("song.mp3");
        fileMetaData.setSize(42L);
        fileMetaData.setDescription("sample");
        fileMetaData.setIndex(7L);
        fileMetaData.setCreateTime(new Date(123456789L));
        fileMetaData.setURNs(java.util.Set.of("urn:sha1:ABCDEFGHIJKLMNOPQRSTUV1234567890AB"));
        return fileMetaData;
    }

    private static Stanza parse(String xml) throws Exception {
        return PacketParserUtils.parseStanza(PacketParserUtils.getParserFor(xml), null, JxmppContext.getDefaultContext());
    }

    private static String asIqXml(String childXml, String elementName, String namespace, IQ.Type type) {
        String body = childXml;
        if (body.startsWith("<iq")) {
            int firstRightBracket = body.indexOf('>');
            int lastOpenIq = body.lastIndexOf("</iq>");
            body = body.substring(firstRightBracket + 1, lastOpenIq);
        }
        return "<iq type='" + type + "' from='sender@example.com/resource' to='receiver@example.com/resource' id='test-1'>"
                + body
                + "</iq>";
    }
}
