package org.limewire.xmpp.client.impl.messages.authtoken;

import java.io.IOException;

import org.apache.commons.codec.binary.Base64;
import org.jivesoftware.smack.packet.IQ;
import org.limewire.friend.api.feature.AuthToken;
import org.limewire.friend.impl.feature.AuthTokenImpl;
import org.limewire.util.Objects;
import org.limewire.util.StringUtils;
import org.limewire.xmpp.client.impl.messages.InvalidIQException;
import org.jivesoftware.smack.xml.XmlPullParser;
import org.jivesoftware.smack.xml.XmlPullParser.Event;
import org.jivesoftware.smack.xml.XmlPullParserException;

public class AuthTokenIQ extends IQ {

    public static final String ELEMENT = "auth-token";
    public static final String NAMESPACE = "jabber:iq:lw-auth-token";

    private final AuthToken authToken;

    public AuthTokenIQ(AuthToken authToken) {
        super(ELEMENT, NAMESPACE);
        this.authToken = Objects.nonNull(authToken, "authToken");
    }

    public AuthToken getAuthToken() {
        return authToken;
    }

    @Override
    protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml) {
        xml.rightAngleBracket();
        xml.halfOpenElement("token")
                .attribute("value", authToken.getBase64())
                .closeEmptyElement();
        return xml;
    }

    static AuthTokenIQ parse(XmlPullParser parser, int initialDepth)
            throws IOException, XmlPullParserException, InvalidIQException {
        AuthToken parsedAuthToken = null;
        outer: while (true) {
            Event event = parser.next();
            switch (event) {
            case START_ELEMENT:
                if ("token".equals(parser.getName())) {
                    String value = parser.getAttributeValue(null, "value");
                    if (value == null) {
                        throw new InvalidIQException("no value");
                    }
                    parsedAuthToken = new AuthTokenImpl(value);
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

        if (parsedAuthToken == null) {
            throw new InvalidIQException("no auth token parsed");
        }
        return new AuthTokenIQ(parsedAuthToken);
    }
}
