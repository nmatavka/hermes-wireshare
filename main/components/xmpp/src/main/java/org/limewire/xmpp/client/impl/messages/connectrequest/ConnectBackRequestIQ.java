package org.limewire.xmpp.client.impl.messages.connectrequest;

import java.io.IOException;
import java.text.MessageFormat;

import org.apache.commons.codec.binary.Base64;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.xml.XmlPullParser;
import org.jivesoftware.smack.xml.XmlPullParser.Event;
import org.jivesoftware.smack.xml.XmlPullParserException;
import org.limewire.io.Connectable;
import org.limewire.io.GUID;
import org.limewire.io.NetworkUtils;
import org.limewire.net.ConnectBackRequest;
import org.limewire.net.address.ConnectableSerializer;
import org.limewire.util.Objects;
import org.limewire.util.StringUtils;
import org.limewire.xmpp.client.impl.messages.InvalidIQException;

/**
 * IQ to be sent to request the other peer to open a connection back to this peer.
 */
public class ConnectBackRequestIQ extends IQ {

    private final ConnectBackRequest request;

    public static final String ELEMENT_NAME = "connect-back-request";

    public static final String NAME_SPACE = "jabber:iq:lw-connect-request";

    public ConnectBackRequestIQ(ConnectBackRequest request) {
        super(ELEMENT_NAME, NAME_SPACE);
        this.request = Objects.nonNull(request, "request");
    }

    static ConnectBackRequestIQ parse(XmlPullParser parser, int initialDepth)
            throws IOException, XmlPullParserException, InvalidIQException {
        GUID guid = null;
        int fwtVersion = -1;
        Connectable connectable = null;

        if (parser.getEventType() == Event.START_ELEMENT && ELEMENT_NAME.equals(parser.getName())) {
            String value = parser.getAttributeValue(null, "client-guid");
            if (value == null) {
                throw new InvalidIQException("no guid provided");
            }
            try {
                guid = new GUID(value);
            } catch (IllegalArgumentException iae) {
                throw new InvalidIQException("invalid guid: " + value, iae);
            }

            value = parser.getAttributeValue(null, "supported-fwt-version");
            if (value == null) {
                throw new InvalidIQException("no fwt version provided");
            }
            try {
                fwtVersion = Integer.parseInt(value);
            } catch (NumberFormatException nfe) {
                throw new InvalidIQException("fwt version not a valid number: " + value, nfe);
            }
        }

        outer: while (true) {
            Event event = parser.next();
            switch (event) {
            case START_ELEMENT:
                if ("address".equals(parser.getName())) {
                    String type = parser.getAttributeValue(null, "type");
                    ConnectableSerializer serializer = new ConnectableSerializer();
                    if (type == null || !type.equals(serializer.getAddressType())) {
                        throw new InvalidIQException("no address type provided or invalid: " + type);
                    }
                    String value = parser.getAttributeValue(null, "value");
                    if (value == null) {
                        throw new InvalidIQException("no address value found");
                    }
                    connectable = serializer.deserialize(Base64.decodeBase64(StringUtils.toUTF8Bytes(value)));
                    if (!NetworkUtils.isValidIpPort(connectable)) {
                        throw new InvalidIQException("invalid address: " + connectable);
                    }
                }
                break;
            case END_ELEMENT:
                if (parser.getDepth() == initialDepth) {
                    break outer;
                }
                break;
            default:
                break;
            }
        }

        if (guid == null || fwtVersion == -1 || connectable == null) {
            throw new InvalidIQException(MessageFormat.format("incomplete connect request, {0}, {1}, {2}",
                    guid, fwtVersion, connectable));
        }
        return new ConnectBackRequestIQ(new ConnectBackRequest(connectable, guid, fwtVersion));
    }

    @Override
    protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml) {
        ConnectableSerializer serializer = new ConnectableSerializer();
        xml.attribute("client-guid", request.getClientGuid().toHexString());
        xml.attribute("supported-fwt-version", String.valueOf(request.getSupportedFWTVersion()));
        xml.rightAngleBracket();
        try {
            xml.halfOpenElement("address")
                    .attribute("type", serializer.getAddressType())
                    .attribute("value",
                            StringUtils.getUTF8String(Base64.encodeBase64(serializer.serialize(request.getAddress()))))
                    .closeEmptyElement();
            return xml;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public ConnectBackRequest getConnectBackRequest() {
        return request;
    }
}
