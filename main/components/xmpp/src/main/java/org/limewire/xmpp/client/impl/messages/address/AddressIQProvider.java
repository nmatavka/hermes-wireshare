package org.limewire.xmpp.client.impl.messages.address;

import java.io.IOException;
import java.text.ParseException;

import org.jivesoftware.smack.packet.IqData;
import org.jivesoftware.smack.packet.XmlEnvironment;
import org.jivesoftware.smack.provider.IqProvider;
import org.jivesoftware.smack.xml.XmlPullParser;
import org.jivesoftware.smack.xml.XmlPullParserException;
import org.jxmpp.JxmppContext;
import org.limewire.logging.Log;
import org.limewire.logging.LogFactory;
import org.limewire.net.address.AddressFactory;
import org.limewire.xmpp.client.impl.messages.InvalidIQException;

public class AddressIQProvider extends IqProvider<AddressIQ> {

    private static final Log LOG = LogFactory.getLog(AddressIQProvider.class);

    private final AddressFactory factory;

    public AddressIQProvider(AddressFactory factory) {
        this.factory = factory;
    }

    @Override
    public AddressIQ parse(XmlPullParser parser, int initialDepth, IqData iqData, XmlEnvironment xmlEnvironment,
                           JxmppContext jxmppContext) throws XmlPullParserException, IOException, ParseException {
        try {
            return AddressIQ.parse(parser, factory, initialDepth);
        } catch (InvalidIQException ie) {
            LOG.debug("invalid iq", ie);
            return null;
        }
    }
}
