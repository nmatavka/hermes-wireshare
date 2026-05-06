package org.limewire.ed2k.backend;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

public final class WireShareEd2kPaths {

    private static volatile File incomingDir = new File("incoming");
    private static volatile File tempDir = new File("temp");
    private static volatile File logsDir = new File("logs");
    private static volatile File settingsDir = new File("settings");
    private static volatile File preferredTargetsFile = new File("preferred-download-targets.properties");
    private static volatile File lastConnectedServerFile = new File("last-connected-server.properties");
    private static final Map<String, PreferredTarget> preferredTargets = new ConcurrentHashMap<String, PreferredTarget>();
    private static volatile String lastConnectedServerAddress = "";
    private static volatile int lastConnectedServerPort = 0;

    private WireShareEd2kPaths() {
    }

    public static synchronized void configure(File incoming, File temp, File stateRoot) {
        Objects.requireNonNull(incoming, "incoming");
        Objects.requireNonNull(temp, "temp");
        Objects.requireNonNull(stateRoot, "stateRoot");
        incomingDir = incoming.getAbsoluteFile();
        tempDir = temp.getAbsoluteFile();
        settingsDir = stateRoot.getAbsoluteFile();
        logsDir = new File(settingsDir, "logs").getAbsoluteFile();
        preferredTargetsFile = new File(settingsDir, "preferred-download-targets.properties").getAbsoluteFile();
        lastConnectedServerFile = new File(settingsDir, "last-connected-server.properties").getAbsoluteFile();
        loadPreferredTargets();
        loadLastConnectedServer();
    }

    public static String incomingDir() {
        return incomingDir.getAbsolutePath();
    }

    public static String tempDir() {
        return tempDir.getAbsolutePath();
    }

    public static String logsDir() {
        return logsDir.getAbsolutePath();
    }

    public static String settingsDir() {
        return settingsDir.getAbsolutePath();
    }

    public static String settingsFile(String fileName) {
        return new File(settingsDir, fileName).getAbsolutePath();
    }

    public static synchronized void rememberPreferredTarget(String hash, File target, boolean overwrite) {
        if (hash == null || hash.trim().isEmpty() || target == null) {
            return;
        }
        preferredTargets.put(normalizeHash(hash), new PreferredTarget(target.getAbsoluteFile(), overwrite));
        storePreferredTargets();
    }

    public static synchronized void clearPreferredTarget(String hash) {
        if (hash == null || hash.trim().isEmpty()) {
            return;
        }
        preferredTargets.remove(normalizeHash(hash));
        storePreferredTargets();
    }

    public static File preferredTarget(String hash) {
        PreferredTarget target = preferredTargets.get(normalizeHash(hash));
        return target == null ? null : target.file;
    }

    public static boolean preferredTargetOverwrite(String hash) {
        PreferredTarget target = preferredTargets.get(normalizeHash(hash));
        return target != null && target.overwrite;
    }

    public static File completedFileTarget(String hash, String fallbackName) {
        File preferred = preferredTarget(hash);
        return preferred != null ? preferred : new File(incomingDir, fallbackName).getAbsoluteFile();
    }

    public static synchronized void rememberLastConnectedServer(String address, int port) {
        if (address == null || address.trim().isEmpty() || port <= 0) {
            return;
        }
        lastConnectedServerAddress = address.trim();
        lastConnectedServerPort = port;
        storeLastConnectedServer();
    }

    public static synchronized void clearLastConnectedServer() {
        lastConnectedServerAddress = "";
        lastConnectedServerPort = 0;
        storeLastConnectedServer();
    }

    public static String lastConnectedServerAddress() {
        return lastConnectedServerAddress;
    }

    public static int lastConnectedServerPort() {
        return lastConnectedServerPort;
    }

    private static String normalizeHash(String hash) {
        return hash == null ? "" : hash.trim().toLowerCase(java.util.Locale.US);
    }

    private static synchronized void loadPreferredTargets() {
        preferredTargets.clear();
        if (!preferredTargetsFile.isFile()) {
            return;
        }
        Properties properties = new Properties();
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(preferredTargetsFile);
            properties.load(inputStream);
            for (String key : properties.stringPropertyNames()) {
                PreferredTarget target = PreferredTarget.parse(properties.getProperty(key));
                if (target != null) {
                    preferredTargets.put(normalizeHash(key), target);
                }
            }
        } catch (IOException ignored) {
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    private static synchronized void storePreferredTargets() {
        Properties properties = new Properties();
        for (Map.Entry<String, PreferredTarget> entry : preferredTargets.entrySet()) {
            properties.setProperty(entry.getKey(), entry.getValue().serialize());
        }
        preferredTargetsFile.getParentFile().mkdirs();
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(preferredTargetsFile);
            properties.store(outputStream, "WireShare ED2K preferred download targets");
        } catch (IOException ignored) {
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    private static synchronized void loadLastConnectedServer() {
        lastConnectedServerAddress = "";
        lastConnectedServerPort = 0;
        if (!lastConnectedServerFile.isFile()) {
            return;
        }
        Properties properties = new Properties();
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(lastConnectedServerFile);
            properties.load(inputStream);
            String address = properties.getProperty("address", "").trim();
            String portValue = properties.getProperty("port", "0").trim();
            int port = Integer.parseInt(portValue);
            if (!address.isEmpty() && port > 0) {
                lastConnectedServerAddress = address;
                lastConnectedServerPort = port;
            }
        } catch (IOException ignored) {
        } catch (NumberFormatException ignored) {
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    private static synchronized void storeLastConnectedServer() {
        Properties properties = new Properties();
        if (!lastConnectedServerAddress.isEmpty() && lastConnectedServerPort > 0) {
            properties.setProperty("address", lastConnectedServerAddress);
            properties.setProperty("port", Integer.toString(lastConnectedServerPort));
        }
        lastConnectedServerFile.getParentFile().mkdirs();
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(lastConnectedServerFile);
            properties.store(outputStream, "WireShare ED2K last connected server");
        } catch (IOException ignored) {
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    private static final class PreferredTarget {
        private final File file;
        private final boolean overwrite;

        private PreferredTarget(File file, boolean overwrite) {
            this.file = file;
            this.overwrite = overwrite;
        }

        private String serialize() {
            return (overwrite ? "1" : "0") + "|" + URLEncoder.encode(file.getAbsolutePath(), StandardCharsets.UTF_8);
        }

        private static PreferredTarget parse(String rawValue) {
            if (rawValue == null || rawValue.isEmpty()) {
                return null;
            }
            int separator = rawValue.indexOf('|');
            if (separator < 0) {
                return new PreferredTarget(new File(rawValue), false);
            }
            boolean overwrite = "1".equals(rawValue.substring(0, separator));
            String path = URLDecoder.decode(rawValue.substring(separator + 1), StandardCharsets.UTF_8);
            return new PreferredTarget(new File(path), overwrite);
        }
    }
}
