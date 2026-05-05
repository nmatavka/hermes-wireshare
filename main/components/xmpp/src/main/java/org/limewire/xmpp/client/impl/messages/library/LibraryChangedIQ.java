package org.limewire.xmpp.client.impl.messages.library;

import java.io.IOException;
import java.text.ParseException;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.IqData;
import org.jivesoftware.smack.packet.SimpleIQ;
import org.jivesoftware.smack.packet.XmlEnvironment;
import org.jivesoftware.smack.provider.IqProvider;
import org.jivesoftware.smack.xml.XmlPullParser;
import org.jivesoftware.smack.xml.XmlPullParser.Event;
import org.jivesoftware.smack.xml.XmlPullParserException;
import org.jxmpp.JxmppContext;

public class LibraryChangedIQ extends SimpleIQ {

    public static final String ELEMENT = "library-changed";
    public static final String NAMESPACE = "jabber:iq:lw-lib-change";

    public LibraryChangedIQ() {
        super(ELEMENT, NAMESPACE);
    }

    static LibraryChangedIQ parse(XmlPullParser parser, int initialDepth) throws IOException, XmlPullParserException {
        outer: while (true) {
            Event event = parser.next();
            switch (event) {
            case END_ELEMENT:
                if (parser.getDepth() == initialDepth) {
                    break outer;
                }
                break;
            default:
                break;
            }
        }
        return new LibraryChangedIQ();
    }

    public static IqProvider<LibraryChangedIQ> getIQProvider() {
        return new LibraryChangedIQProvider();
    }

    private static class LibraryChangedIQProvider extends IqProvider<LibraryChangedIQ> {

        @Override
        public LibraryChangedIQ parse(XmlPullParser parser, int initialDepth, IqData iqData,
                                      XmlEnvironment xmlEnvironment, JxmppContext jxmppContext)
                throws XmlPullParserException, IOException, ParseException {
            return LibraryChangedIQ.parse(parser, initialDepth);
        }
    }
}
