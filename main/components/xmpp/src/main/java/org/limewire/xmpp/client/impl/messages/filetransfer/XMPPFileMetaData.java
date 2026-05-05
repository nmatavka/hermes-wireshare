package org.limewire.xmpp.client.impl.messages.filetransfer;

import java.io.IOException;

import org.jivesoftware.smack.xml.XmlPullParser;
import org.jivesoftware.smack.xml.XmlPullParser.Event;
import org.jivesoftware.smack.xml.XmlPullParserException;
import org.limewire.friend.impl.FileMetaDataImpl;
import org.limewire.xmpp.client.impl.messages.InvalidIQException;

public class XMPPFileMetaData extends FileMetaDataImpl {
    public XMPPFileMetaData(XmlPullParser parser) throws XmlPullParserException, IOException, InvalidIQException {
        outer: while (true) {
            Event event = parser.next();
            switch (event) {
            case START_ELEMENT:
                data.put(parser.getName(), parser.nextText());
                break;
            case END_ELEMENT:
                if ("file".equals(parser.getName())) {
                    break outer;
                }
                break;
            default:
                break;
            }
        }
        if (!isValid()) {
            throw new InvalidIQException("is missing mandatory fields: " + this);
        }
    }
}
