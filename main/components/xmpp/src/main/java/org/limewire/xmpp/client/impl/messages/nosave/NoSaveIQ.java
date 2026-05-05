package org.limewire.xmpp.client.impl.messages.nosave;

import java.io.IOException;
import java.text.ParseException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.IqData;
import org.jivesoftware.smack.packet.XmlEnvironment;
import org.jivesoftware.smack.provider.IqProvider;
import org.jivesoftware.smack.xml.XmlPullParser;
import org.jivesoftware.smack.xml.XmlPullParser.Event;
import org.jivesoftware.smack.xml.XmlPullParserException;
import org.jxmpp.JxmppContext;
import org.limewire.friend.impl.feature.NoSave;
import org.limewire.util.Objects;
import org.limewire.xmpp.client.impl.messages.InvalidIQException;

public class NoSaveIQ extends IQ {

    private final Map<String, NoSave> items = new HashMap<String, NoSave>();

    public static final String ELEMENT_NAME = "query";
    public static final String NAME_SPACE = "google:nosave";

    private NoSaveIQ() {
        super(ELEMENT_NAME, NAME_SPACE);
    }

    private NoSaveIQ(String jid, NoSave value) {
        this();
        items.put(Objects.nonNull(jid, "jid"), value);
    }

    static NoSaveIQ parse(XmlPullParser parser, int initialDepth)
            throws IOException, XmlPullParserException, InvalidIQException {
        NoSaveIQ iq = new NoSaveIQ();
        outer: while (true) {
            Event event = parser.next();
            switch (event) {
            case START_ELEMENT:
                if ("item".equals(parser.getName())) {
                    String jid = parser.getAttributeValue(null, "jid");
                    if (jid == null) {
                        throw new InvalidIQException("no jid value");
                    }
                    String value = parser.getAttributeValue(null, "value");
                    if (value == null) {
                        throw new InvalidIQException("no value in value attribute");
                    }
                    iq.items.put(jid, value.equals(NoSave.ENABLED.getPacketIdentifier()) ? NoSave.ENABLED : NoSave.DISABLED);
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
        return iq;
    }

    public static NoSaveIQ getNoSaveSetMessage(String userId, NoSave value) {
        NoSaveIQ setMsg = new NoSaveIQ(userId, value);
        setMsg.setType(Type.set);
        return setMsg;
    }

    public static NoSaveIQ getNoSaveStatesMessage() {
        return new NoSaveIQ();
    }

    public Map<String, NoSave> getNoSaveUsers() {
        return Collections.unmodifiableMap(items);
    }

    @Override
    protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml) {
        if (items.isEmpty()) {
            xml.setEmptyElement();
            return xml;
        }

        xml.rightAngleBracket();
        for (Map.Entry<String, NoSave> entry : items.entrySet()) {
            xml.halfOpenElement("item")
                    .attribute("jid", entry.getKey())
                    .attribute("value", entry.getValue().getPacketIdentifier())
                    .closeEmptyElement();
        }
        return xml;
    }

    public static IqProvider<NoSaveIQ> getIQProvider() {
        return new NoSaveIQProvider();
    }

    private static class NoSaveIQProvider extends IqProvider<NoSaveIQ> {

        @Override
        public NoSaveIQ parse(XmlPullParser parser, int initialDepth, IqData iqData,
                              XmlEnvironment xmlEnvironment, JxmppContext jxmppContext)
                throws XmlPullParserException, IOException, ParseException {
            try {
                return NoSaveIQ.parse(parser, initialDepth);
            } catch (InvalidIQException iie) {
                return null;
            }
        }
    }
}
