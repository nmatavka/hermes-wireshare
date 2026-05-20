package org.limewire.ui.swing.shell;

import org.limewire.util.OSUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

final class LinuxXdgAssociationSupport {

    static final List<String> TORRENT_MIME_TYPES = Collections.unmodifiableList(Arrays.asList(
            "application/x-bittorrent",
            "application/torrent",
            "application/x-torrent"
    ));

    static final List<String> MAGNET_SCHEME_TYPES = Collections.singletonList("x-scheme-handler/magnet");

    private static final String DEFAULT_DESKTOP_ID = "hermes-wireshare.desktop";
    private static final String FLATPAK_DESKTOP_ID = "org.teamhermes.WireShare.desktop";

    private LinuxXdgAssociationSupport() {
    }

    static boolean isSupported() {
        return OSUtils.isLinux() && hasXdgMime() && resolveExecLine() != null;
    }

    static boolean isRegistered(Collection<String> mimeTypes) {
        if (!isSupported()) {
            return false;
        }
        String desktopId = desktopEntryId();
        for (String mimeType : mimeTypes) {
            if (!desktopId.equals(queryDefault(mimeType))) {
                return false;
            }
        }
        return true;
    }

    static boolean isAvailable(Collection<String> mimeTypes) {
        if (!isSupported()) {
            return false;
        }
        for (String mimeType : mimeTypes) {
            if (!queryDefault(mimeType).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    static void register(Collection<String> mimeTypes) {
        if (!isSupported()) {
            return;
        }

        try {
            ensureDesktopEntry();
            List<String> command = new ArrayList<String>();
            command.add("xdg-mime");
            command.add("default");
            command.add(desktopEntryId());
            command.addAll(mimeTypes);
            run(command);
            refreshDesktopDatabase();
        } catch (IOException ignored) {
        }
    }

    static void unregister(Collection<String> mimeTypes) {
        if (!OSUtils.isLinux()) {
            return;
        }
        for (Path mimeAppsFile : mimeAppsFiles()) {
            removeDesktopEntryFromMimeApps(mimeAppsFile, mimeTypes);
        }
        try {
            refreshDesktopDatabase();
        } catch (IOException ignored) {
        }
    }

    private static boolean hasXdgMime() {
        try {
            return run(Arrays.asList("xdg-mime", "--help")).exitCode == 0;
        } catch (IOException ignored) {
            return false;
        }
    }

    private static String queryDefault(String mimeType) {
        try {
            CommandResult result = run(Arrays.asList("xdg-mime", "query", "default", mimeType));
            if (result.exitCode != 0) {
                return "";
            }
            return result.output.trim();
        } catch (IOException ignored) {
            return "";
        }
    }

    private static void ensureDesktopEntry() throws IOException {
        String execLine = resolveExecLine();
        if (execLine == null) {
            throw new IOException("No Linux launcher command available for desktop association.");
        }

        Path desktopFile = desktopEntryFile();
        Files.createDirectories(desktopFile.getParent());
        String content = desktopEntryContents(execLine);
        if (Files.exists(desktopFile)) {
            String current = Files.readString(desktopFile, StandardCharsets.UTF_8);
            if (current.equals(content)) {
                return;
            }
        }
        Files.writeString(
                desktopFile,
                content,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
        );
    }

    private static String desktopEntryContents(String execLine) {
        return "[Desktop Entry]\n" +
                "Categories=Network;\n" +
                "Encoding=UTF-8\n" +
                "Exec=" + execLine + "\n" +
                "Icon=" + desktopIconName() + "\n" +
                "MimeType=" + String.join(";", allMimeTypes()) + ";\n" +
                "Name=WireShare\n" +
                "Terminal=false\n" +
                "Type=Application\n" +
                "Version=7.0\n";
    }

    private static String resolveExecLine() {
        String unixExecutable = System.getProperty("unix.executable", "").trim();
        if (!unixExecutable.isEmpty()) {
            return quoteExecToken(unixExecutable) + " %U";
        }

        File flatpakLauncher = new File("/app/WireShare.sh");
        if (flatpakLauncher.isFile()) {
            return quoteExecToken(flatpakLauncher.getAbsolutePath()) + " %U";
        }

        File packagedLauncher = new File("/usr/bin/WireShare.sh");
        if (packagedLauncher.isFile()) {
            return quoteExecToken(packagedLauncher.getAbsolutePath()) + " %U";
        }

        File repoLauncher = new File("start-linux.sh").getAbsoluteFile();
        if (repoLauncher.isFile()) {
            return quoteExecToken(repoLauncher.getAbsolutePath()) + " %U";
        }

        File jarFile = new File("WireShare.jar").getAbsoluteFile();
        File javaExecutable = new File(System.getProperty("java.home"), "bin/java");
        if (jarFile.isFile() && javaExecutable.isFile()) {
            return quoteExecToken(javaExecutable.getAbsolutePath()) +
                    " -jar " +
                    quoteExecToken(jarFile.getAbsolutePath()) +
                    " %U";
        }

        return null;
    }

    private static String quoteExecToken(String token) {
        return "\"" + token.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }

    private static String desktopEntryId() {
        return isFlatpakRuntime() ? FLATPAK_DESKTOP_ID : DEFAULT_DESKTOP_ID;
    }

    private static String desktopIconName() {
        return isFlatpakRuntime() ? "org.teamhermes.WireShare" : "hermes-wireshare";
    }

    private static boolean isFlatpakRuntime() {
        return new File("/app/WireShare.sh").isFile() ||
                System.getenv("FLATPAK_ID") != null ||
                System.getenv("container") != null;
    }

    private static Path desktopEntryFile() {
        return applicationsDirectory().resolve(desktopEntryId());
    }

    private static Path applicationsDirectory() {
        return localShareHome().resolve("applications");
    }

    private static Path localShareHome() {
        String xdgDataHome = System.getenv("XDG_DATA_HOME");
        if (xdgDataHome != null && !xdgDataHome.trim().isEmpty()) {
            return Path.of(xdgDataHome);
        }
        return Path.of(System.getProperty("user.home"), ".local", "share");
    }

    private static List<Path> mimeAppsFiles() {
        List<Path> files = new ArrayList<Path>();
        String xdgConfigHome = System.getenv("XDG_CONFIG_HOME");
        if (xdgConfigHome != null && !xdgConfigHome.trim().isEmpty()) {
            files.add(Path.of(xdgConfigHome, "mimeapps.list"));
        } else {
            files.add(Path.of(System.getProperty("user.home"), ".config", "mimeapps.list"));
        }
        files.add(applicationsDirectory().resolve("mimeapps.list"));
        return files;
    }

    private static void removeDesktopEntryFromMimeApps(Path file, Collection<String> mimeTypes) {
        if (!Files.exists(file)) {
            return;
        }

        try {
            List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
            List<String> updated = new ArrayList<String>(lines.size());
            String section = "";
            boolean changed = false;

            for (String line : lines) {
                String trimmed = line.trim();
                if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
                    section = trimmed;
                    updated.add(line);
                    continue;
                }

                if (("[Default Applications]".equals(section) || "[Added Associations]".equals(section))
                        && !trimmed.startsWith("#")) {
                    String rewritten = rewriteMimeAppsLine(trimmed, mimeTypes);
                    if (rewritten != null) {
                        if (!rewritten.equals(line)) {
                            changed = true;
                        }
                        updated.add(rewritten);
                    } else if (matchesManagedMimeType(trimmed, mimeTypes)) {
                        changed = true;
                    } else {
                        updated.add(line);
                    }
                } else {
                    updated.add(line);
                }
            }

            if (changed) {
                Files.createDirectories(Objects.requireNonNull(file.getParent()));
                Files.write(file, updated, StandardCharsets.UTF_8, StandardOpenOption.TRUNCATE_EXISTING);
            }
        } catch (IOException ignored) {
        }
    }

    private static boolean matchesManagedMimeType(String line, Collection<String> mimeTypes) {
        for (String mimeType : mimeTypes) {
            if (line.startsWith(mimeType + "=")) {
                return true;
            }
        }
        return false;
    }

    private static String rewriteMimeAppsLine(String line, Collection<String> mimeTypes) {
        for (String mimeType : mimeTypes) {
            String prefix = mimeType + "=";
            if (!line.startsWith(prefix)) {
                continue;
            }

            String remainder = line.substring(prefix.length());
            List<String> entries = new ArrayList<String>();
            for (String entry : remainder.split(";")) {
                String trimmed = entry.trim();
                if (!trimmed.isEmpty() && !desktopEntryId().equals(trimmed)) {
                    entries.add(trimmed);
                }
            }

            if (entries.isEmpty()) {
                return null;
            }
            return prefix + String.join(";", entries) + ";";
        }
        return line;
    }

    private static void refreshDesktopDatabase() throws IOException {
        if (!hasUpdateDesktopDatabase()) {
            return;
        }
        run(Arrays.asList("update-desktop-database", applicationsDirectory().toString()));
    }

    private static boolean hasUpdateDesktopDatabase() {
        try {
            return run(Arrays.asList("update-desktop-database", "--help")).exitCode == 0;
        } catch (IOException ignored) {
            return false;
        }
    }

    private static List<String> allMimeTypes() {
        List<String> mimeTypes = new ArrayList<String>(TORRENT_MIME_TYPES.size() + MAGNET_SCHEME_TYPES.size());
        mimeTypes.addAll(TORRENT_MIME_TYPES);
        mimeTypes.addAll(MAGNET_SCHEME_TYPES);
        return mimeTypes;
    }

    private static CommandResult run(List<String> command) throws IOException {
        Process process = new ProcessBuilder(command)
                .redirectErrorStream(true)
                .start();
        try {
            String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            return new CommandResult(process.waitFor(), output);
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted while running command: " + command, interrupted);
        }
    }

    private static final class CommandResult {
        private final int exitCode;
        private final String output;

        private CommandResult(int exitCode, String output) {
            this.exitCode = exitCode;
            this.output = output;
        }
    }
}
