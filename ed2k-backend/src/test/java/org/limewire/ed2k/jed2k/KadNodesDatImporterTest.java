package org.limewire.ed2k.jed2k;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

import org.jmule.core.jkad.routingtable.KadContact;
import org.junit.Test;

public class KadNodesDatImporterTest {

    @Test
    public void parsesVersionedNodesDatContacts() throws Exception {
        File file = File.createTempFile("nodes", ".dat");
        try {
            ByteBuffer buffer = ByteBuffer.allocate(54).order(ByteOrder.LITTLE_ENDIAN);
            buffer.putInt(0);
            buffer.putInt(2);
            buffer.putInt(1);
            buffer.put(new byte[] {
                1, 2, 3, 4,
                5, 6, 7, 8,
                9, 10, 11, 12,
                13, 14, 15, 16
            });
            buffer.put(new byte[] { 1, 2, 3, 4 });
            buffer.putShort((short) 4672);
            buffer.putShort((short) 4662);
            buffer.put((byte) 8);
            buffer.put(new byte[] { 9, 8, 7, 6 });
            buffer.put(new byte[] { 4, 3, 2, 1 });
            buffer.put((byte) 1);
            buffer.putLong(0L);

            FileOutputStream outputStream = new FileOutputStream(file);
            try {
                outputStream.write(buffer.array(), 0, buffer.position());
            } finally {
                outputStream.close();
            }

            List<KadContact> contacts = KadNodesDatImporter.parse(file);

            assertEquals(1, contacts.size());
            KadContact contact = contacts.get(0);
            assertArrayEquals(new byte[] { 1, 2, 3, 4 }, contact.getContactAddress().getAddress().getAddress());
            assertEquals(4672, contact.getUDPPort());
            assertEquals(4662, contact.getTCPPort());
            assertEquals(8, contact.getVersion());
            assertTrue(contact.isIPVerified());
        } finally {
            file.delete();
        }
    }
}
