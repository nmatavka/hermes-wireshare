/*
 * Provenance note:
 * This file adapts nodes.dat parsing behavior from the local JED2K
 * donor tree, primarily org.dkf.jed2k.protocol.kad.KadNodesDat and related
 * Kad entry structures. It is now carried as WireShare-owned source and the
 * build must not depend on that donor folder at compile time.
 */

package org.limewire.ed2k.jed2k;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jmule.core.jkad.ClientID;
import org.jmule.core.jkad.ContactAddress;
import org.jmule.core.jkad.IPAddress;
import org.jmule.core.jkad.JKadUDPKey;
import org.jmule.core.jkad.routingtable.KadContact;
import org.jmule.core.jkad.utils.Utils;

public final class KadNodesDatImporter {

    private KadNodesDatImporter() {
    }

    public static List<KadContact> parse(File file) throws IOException {
        if (file == null || !file.isFile()) {
            return Collections.emptyList();
        }
        byte[] data = readAllBytes(file);
        if (data.length < 12) {
            return Collections.emptyList();
        }

        ByteBuffer buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
        List<KadContact> contacts = new ArrayList<KadContact>();

        int firstCount = buffer.getInt();
        if (firstCount == 0 && buffer.remaining() >= 8) {
            int version = buffer.getInt();
            if (version == 3 && buffer.remaining() >= 4) {
                int bootstrapEdition = buffer.getInt();
                if (bootstrapEdition == 1 && buffer.remaining() >= 4) {
                    int bootstrapCount = buffer.getInt();
                    readContacts(buffer, bootstrapCount, true, contacts);
                    return contacts;
                }
            }
            if (version >= 1 && version <= 3 && buffer.remaining() >= 4) {
                firstCount = buffer.getInt();
                readContacts(buffer, firstCount, version >= 2, contacts);
                return contacts;
            }
        }

        buffer.position(8);
        readContacts(buffer, buffer.getInt(), false, contacts);
        return contacts;
    }

    private static void readContacts(ByteBuffer buffer, int contactCount, boolean readExtendedData, List<KadContact> contacts) {
        for (int i = 0; i < contactCount && buffer.remaining() >= 29; i++) {
            byte[] contactId = new byte[16];
            byte[] endpointAddress = new byte[4];
            byte[] key = new byte[4];
            byte[] keyAddress = new byte[4];
            buffer.get(contactId);
            buffer.get(endpointAddress);
            int udpPort = Short.toUnsignedInt(buffer.getShort());
            int tcpPort = Short.toUnsignedInt(buffer.getShort());
            byte version = buffer.get();
            buffer.get(key);
            buffer.get(keyAddress);
            boolean verified = buffer.get() == 1;

            if (readExtendedData) {
                if (buffer.remaining() < 8) {
                    break;
                }
                buffer.position(buffer.position() + 8);
            }

            IPAddress address = new IPAddress(endpointAddress);
            if (!Utils.isGoodAddress(address)) {
                continue;
            }
            KadContact contact = new KadContact(
                new ClientID(contactId),
                new ContactAddress(address, udpPort),
                tcpPort,
                version,
                new JKadUDPKey(key, keyAddress),
                verified
            );
            contacts.add(contact);
        }
    }

    private static byte[] readAllBytes(File file) throws IOException {
        FileInputStream inputStream = new FileInputStream(file);
        try {
            byte[] buffer = new byte[(int) file.length()];
            int offset = 0;
            while (offset < buffer.length) {
                int read = inputStream.read(buffer, offset, buffer.length - offset);
                if (read < 0) {
                    break;
                }
                offset += read;
            }
            if (offset == buffer.length) {
                return buffer;
            }
            byte[] exact = new byte[offset];
            System.arraycopy(buffer, 0, exact, 0, offset);
            return exact;
        } finally {
            inputStream.close();
        }
    }
}
