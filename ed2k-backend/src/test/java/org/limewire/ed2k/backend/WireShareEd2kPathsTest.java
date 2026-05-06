package org.limewire.ed2k.backend;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.file.Files;

import org.junit.Test;

public class WireShareEd2kPathsTest {

    @Test
    public void remembersAndRestoresPreferredTargets() throws Exception {
        File root = Files.createTempDirectory("ed2k-paths").toFile();
        try {
            File incoming = new File(root, "incoming");
            File incomplete = new File(root, "incomplete");
            File state = new File(root, "state");
            WireShareEd2kPaths.configure(incoming, incomplete, state);

            File target = new File(root, "custom/Example.bin");
            WireShareEd2kPaths.rememberPreferredTarget("ABCDEF", target, true);

            assertEquals(target.getAbsoluteFile(), WireShareEd2kPaths.preferredTarget("abcdef"));
            assertTrue(WireShareEd2kPaths.preferredTargetOverwrite("abcdef"));
            assertEquals(target.getAbsoluteFile(), WireShareEd2kPaths.completedFileTarget("abcdef", "fallback.bin"));

            WireShareEd2kPaths.configure(incoming, incomplete, state);
            assertEquals(target.getAbsoluteFile(), WireShareEd2kPaths.preferredTarget("abcdef"));

            WireShareEd2kPaths.clearPreferredTarget("abcdef");
            assertNull(WireShareEd2kPaths.preferredTarget("abcdef"));
            assertFalse(WireShareEd2kPaths.preferredTargetOverwrite("abcdef"));
            assertEquals(new File(incoming, "fallback.bin").getAbsoluteFile(), WireShareEd2kPaths.completedFileTarget("abcdef", "fallback.bin"));
        } finally {
            deleteRecursively(root);
        }
    }

    @Test
    public void remembersAndRestoresLastConnectedServer() throws Exception {
        File root = Files.createTempDirectory("ed2k-last-server").toFile();
        try {
            File incoming = new File(root, "incoming");
            File incomplete = new File(root, "incomplete");
            File state = new File(root, "state");
            WireShareEd2kPaths.configure(incoming, incomplete, state);

            WireShareEd2kPaths.rememberLastConnectedServer("45.82.80.155", 5687);
            assertEquals("45.82.80.155", WireShareEd2kPaths.lastConnectedServerAddress());
            assertEquals(5687, WireShareEd2kPaths.lastConnectedServerPort());

            WireShareEd2kPaths.configure(incoming, incomplete, state);
            assertEquals("45.82.80.155", WireShareEd2kPaths.lastConnectedServerAddress());
            assertEquals(5687, WireShareEd2kPaths.lastConnectedServerPort());

            WireShareEd2kPaths.clearLastConnectedServer();
            assertEquals("", WireShareEd2kPaths.lastConnectedServerAddress());
            assertEquals(0, WireShareEd2kPaths.lastConnectedServerPort());
        } finally {
            deleteRecursively(root);
        }
    }

    private static void deleteRecursively(File file) {
        if (file == null || !file.exists()) {
            return;
        }
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    deleteRecursively(child);
                }
            }
        }
        file.delete();
    }
}
