package org.limewire.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Loads native libraries from packaged resources or well-known filesystem
 * locations before falling back to the JVM's library path.
 */
public final class NativeLibraryLoader {

    private static final Log LOG = LogFactory.getLog(NativeLibraryLoader.class);

    private NativeLibraryLoader() {
    }

    public static boolean loadFirstAvailable(String libraryName, String... candidates) {
        UnsatisfiedLinkError lastLoadError = null;

        for (String candidate : candidates) {
            if (candidate == null || candidate.length() == 0) {
                continue;
            }

            try {
                if (loadFromResource(candidate) || loadFromFile(candidate)) {
                    return true;
                }
            } catch (UnsatisfiedLinkError e) {
                lastLoadError = e;
            }
        }

        try {
            System.loadLibrary(libraryName);
            return true;
        } catch (UnsatisfiedLinkError e) {
            lastLoadError = e;
        }

        if (lastLoadError != null) {
            LOG.warn("Unable to load native library " + libraryName, lastLoadError);
        }

        return false;
    }

    private static boolean loadFromResource(String candidate) {
        String normalized = normalize(candidate);
        ClassLoader classLoader = NativeLibraryLoader.class.getClassLoader();

        InputStream stream = classLoader.getResourceAsStream(normalized);
        if (stream == null) {
            return false;
        }

        try (InputStream in = stream) {
            Path extractDir = Files.createTempDirectory("wireshare-native-");
            Path extractedLibrary = extractDir.resolve(Paths.get(normalized).getFileName().toString());

            Files.copy(in, extractedLibrary, StandardCopyOption.REPLACE_EXISTING);
            extractedLibrary.toFile().deleteOnExit();
            extractDir.toFile().deleteOnExit();

            System.load(extractedLibrary.toAbsolutePath().toString());
            return true;
        } catch (IOException e) {
            LOG.warn("Unable to extract native library resource " + normalized, e);
            return false;
        }
    }

    private static boolean loadFromFile(String candidate) {
        Path path = Paths.get(candidate);
        if (!path.isAbsolute()) {
            path = Paths.get(System.getProperty("user.dir")).resolve(path).normalize();
        }

        if (!Files.isRegularFile(path)) {
            return false;
        }

        System.load(path.toAbsolutePath().toString());
        return true;
    }

    private static String normalize(String candidate) {
        if (candidate.startsWith("/")) {
            return candidate.substring(1);
        }
        return candidate;
    }
}
