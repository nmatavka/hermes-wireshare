package org.limewire.ui.desktop.shell;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.limewire.ui.desktop.util.I18n;
import org.limewire.util.OSUtils;
import org.limewire.util.SystemUtils;

public class LimeAssociations {

    private enum AssociationType {
        TORRENT, MAGNET
    }

    private static final String PROGRAM;

    private static final String UNSUPPORTED_PLATFORM = "";

    static {
        if (OSUtils.isWindows())
            PROGRAM = "WireShare";
        else if (OSUtils.isUnix())
            PROGRAM = System.getProperty("unix.executable", UNSUPPORTED_PLATFORM);
        else
            PROGRAM = UNSUPPORTED_PLATFORM;
    }

    private static Map<AssociationType, LimeAssociationOption> fileAssociations = null;

    private static Map<AssociationType, LimeAssociationOption> createWindowsAssociations() {
        Map<AssociationType, LimeAssociationOption> associations = new HashMap<AssociationType, LimeAssociationOption>();

        String runningPath = SystemUtils.getRunningPath();
        if (runningPath == null || !runningPath.endsWith(PROGRAM + ".exe")) {
            return Collections.emptyMap();
        }

        String protocolOpener = runningPath;
        String fileOpener = "\"" + runningPath + "\" \"%1\"";
        String fileIcon = runningPath + ",1";

        ShellAssociation file = new WindowsFileTypeAssociation(
                "torrent",
                "application/x-bittorrent",
                fileOpener,
                "open",
                "WireShare Torrent",
                fileIcon,
                PROGRAM
        );
        associations.put(
                AssociationType.TORRENT,
                new LimeAssociationOption(file, ".torrent", I18n.tr("\".torrent\" files"))
        );

        ShellAssociation magnet = new MagnetAssociation(PROGRAM, protocolOpener);
        associations.put(
                AssociationType.MAGNET,
                new LimeAssociationOption(magnet, "magnet:", I18n.tr("\"magnet:\" links"))
        );

        return associations;
    }

    private static Map<AssociationType, LimeAssociationOption> createLinuxAssociations() {
        if (!LinuxXdgAssociationSupport.isSupported()) {
            return Collections.emptyMap();
        }

        Map<AssociationType, LimeAssociationOption> associations = new HashMap<AssociationType, LimeAssociationOption>();

        ShellAssociation torrent = new LinuxXdgAssociation(LinuxXdgAssociationSupport.TORRENT_MIME_TYPES);
        associations.put(
                AssociationType.TORRENT,
                new LimeAssociationOption(torrent, ".torrent", I18n.tr("\".torrent\" files"))
        );

        ShellAssociation magnet = new LinuxXdgAssociation(LinuxXdgAssociationSupport.MAGNET_SCHEME_TYPES);
        associations.put(
                AssociationType.MAGNET,
                new LimeAssociationOption(magnet, "magnet:", I18n.tr("\"magnet:\" links"))
        );

        return associations;
    }

    public synchronized static boolean anyAssociationsSupported() {
        return isTorrentAssociationSupported() || isMagnetAssociationSupported();
    }

    public synchronized static boolean isTorrentAssociationSupported() {
        return getTorrentAssociation() != null;
    }

    public synchronized static boolean isMagnetAssociationSupported() {
        return getMagnetAssociation() != null;
    }

    /**
     * Returns a torrent association option if available. Null otherwise.
     */
    public static LimeAssociationOption getTorrentAssociation() {
        return getSupportedAssociations().get(AssociationType.TORRENT);
    }

    /**
     * Returns a magnet association option if available. Null otherwise.
     */
    public static LimeAssociationOption getMagnetAssociation() {
        return getSupportedAssociations().get(AssociationType.MAGNET);
    }

    private static Map<AssociationType, LimeAssociationOption> getSupportedAssociations() {
        if (fileAssociations == null) {
            if (OSUtils.isWindows()) {
                fileAssociations = createWindowsAssociations();
            } else if (OSUtils.isLinux()) {
                fileAssociations = createLinuxAssociations();
            } else if (OSUtils.isMacOSX()) {
                fileAssociations = new HashMap<AssociationType, LimeAssociationOption>();
                if (OSXFileTypeAssociation.isNativeLibraryLoadedCorrectly()) {
                    LimeAssociationOption torrent = new LimeAssociationOption(new OSXFileTypeAssociation("torrent"), 
                                                                              ".torrent",
                                                                              I18n.tr("\".torrent\" files"));
                    fileAssociations.put(AssociationType.TORRENT, torrent);
                    
                    LimeAssociationOption magnet = new LimeAssociationOption(new OSXURLSchemeAssociation("magnet"), 
                                                                             "magnet:",
                                                                             I18n.tr("\".magnet:\" links"));
                    fileAssociations.put(AssociationType.MAGNET, magnet);
                }
            }
        }
        return Collections.unmodifiableMap(fileAssociations);
    }
}
