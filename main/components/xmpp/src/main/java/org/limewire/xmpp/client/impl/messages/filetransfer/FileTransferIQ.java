package org.limewire.xmpp.client.impl.messages.filetransfer;

import java.io.IOException;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.Map;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.IqData;
import org.jivesoftware.smack.packet.XmlEnvironment;
import org.jivesoftware.smack.provider.IqProvider;
import org.jivesoftware.smack.xml.XmlPullParser;
import org.jivesoftware.smack.xml.XmlPullParser.Event;
import org.jivesoftware.smack.xml.XmlPullParserException;
import org.jxmpp.JxmppContext;
import org.limewire.friend.api.FileMetaData;
import org.limewire.logging.Log;
import org.limewire.logging.LogFactory;
import org.limewire.xmpp.client.impl.messages.InvalidIQException;

public class FileTransferIQ extends IQ {

    private static final Log LOG = LogFactory.getLog(FileTransferIQ.class);

    public static final String ELEMENT = "file-transfer";
    public static final String NAMESPACE = "jabber:iq:lw-file-transfer";

    public enum TransferType { OFFER, REQUEST }

    private final FileMetaData fileMetaData;
    private final TransferType transferType;

    public FileTransferIQ(FileMetaData fileMetaData, TransferType transferType) {
        super(ELEMENT, NAMESPACE);
        this.fileMetaData = fileMetaData;
        this.transferType = transferType;
    }

    public FileMetaData getFileMetaData() {
        return fileMetaData;
    }

    public TransferType getTransferType() {
        return transferType;
    }

    @Override
    protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml) {
        xml.attribute("type", transferType.toString());
        if (fileMetaData == null) {
            xml.setEmptyElement();
            return xml;
        }
        xml.rightAngleBracket();
        xml.openElement("file");
        for (Map.Entry<String, String> entry : fileMetaData.getSerializableMap().entrySet()) {
            xml.element(entry.getKey(), entry.getValue());
        }
        xml.closeElement("file");
        return xml;
    }

    static FileTransferIQ parse(XmlPullParser parser, int initialDepth)
            throws IOException, XmlPullParserException, InvalidIQException {
        FileMetaData parsedMetaData = null;
        TransferType parsedTransferType = null;

        if (parser.getEventType() == Event.START_ELEMENT && ELEMENT.equals(parser.getName())) {
            String transferTypeValue = parser.getAttributeValue(null, "type");
            if (transferTypeValue == null) {
                throw new InvalidIQException("no transfer type specified");
            }
            try {
                parsedTransferType = TransferType.valueOf(transferTypeValue);
            } catch (IllegalArgumentException iae) {
                throw new InvalidIQException("unknown transfer type: " + transferTypeValue);
            }
        }

        outer: while (true) {
            Event event = parser.next();
            switch (event) {
            case START_ELEMENT:
                if ("file".equals(parser.getName())) {
                    parsedMetaData = new XMPPFileMetaData(parser);
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

        if (parsedMetaData == null || parsedTransferType == null) {
            throw new InvalidIQException(MessageFormat.format("parsedMetaData {0}, parsedTransferType {1}",
                    parsedMetaData, parsedTransferType));
        }
        return new FileTransferIQ(parsedMetaData, parsedTransferType);
    }

    public static IqProvider<FileTransferIQ> getIQProvider() {
        return new FileTransferIQProvider();
    }

    private static class FileTransferIQProvider extends IqProvider<FileTransferIQ> {

        @Override
        public FileTransferIQ parse(XmlPullParser parser, int initialDepth, IqData iqData,
                                    XmlEnvironment xmlEnvironment, JxmppContext jxmppContext)
                throws XmlPullParserException, IOException, ParseException {
            try {
                return FileTransferIQ.parse(parser, initialDepth);
            } catch (InvalidIQException ie) {
                LOG.debug("invalid iq", ie);
                return null;
            }
        }
    }
}
