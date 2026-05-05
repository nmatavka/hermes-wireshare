package org.limewire.xmpp.client.impl.messages.address;

import java.io.IOException;

import org.apache.commons.codec.binary.Base64;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.provider.IqProvider;
import org.jivesoftware.smack.xml.XmlPullParser;
import org.jivesoftware.smack.xml.XmlPullParser.Event;
import org.jivesoftware.smack.xml.XmlPullParserException;
import org.jxmpp.JxmppContext;
import org.limewire.io.Address;
import org.limewire.net.address.AddressFactory;
import org.limewire.net.address.AddressSerializer;
import org.limewire.util.StringUtils;
import org.limewire.xmpp.client.impl.messages.InvalidIQException;

public class AddressIQ extends IQ {

    public static final String ELEMENT = "address";
    public static final String NAMESPACE = "jabber:iq:lw-address";

    private final Address address;
    private final AddressFactory factory;

    public AddressIQ(Address address, AddressFactory factory) {
        super(ELEMENT, NAMESPACE);
        this.address = address;
        this.factory = factory;
    }

    public Address getAddress() {
        return address;
    }

    @Override
    protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml) {
        if (address == null) {
            xml.setEmptyElement();
            return xml;
        }

        try {
            AddressSerializer addressSerializer = factory.getSerializer(address);
            xml.rightAngleBracket();
            xml.halfOpenElement(addressSerializer.getAddressType())
                    .attribute("value", StringUtils.toUTF8String(Base64.encodeBase64(addressSerializer.serialize(address))))
                    .closeEmptyElement();
            return xml;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static IqProvider<AddressIQ> provider(AddressFactory factory) {
        return new AddressIQProvider(factory);
    }

    static AddressIQ parse(XmlPullParser parser, AddressFactory factory, int initialDepth)
            throws IOException, XmlPullParserException, InvalidIQException {
        Address parsedAddress = null;

        outer: while (true) {
            Event event = parser.next();
            switch (event) {
            case START_ELEMENT:
                if (ELEMENT.equals(parser.getName())) {
                    break;
                }

                String type = parser.getName();
                String value = parser.getAttributeValue(null, "value");
                if (value == null) {
                    throw new InvalidIQException("no value attribute");
                }
                try {
                    parsedAddress = factory.deserialize(type, Base64.decodeBase64(StringUtils.toUTF8Bytes(value)));
                } catch (IOException ie) {
                    throw new InvalidIQException("invalid address: " + value, ie);
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

        if (parsedAddress == null) {
            throw new InvalidIQException("no address to be parsed");
        }
        return new AddressIQ(parsedAddress, factory);
    }
}
