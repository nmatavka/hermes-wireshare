package org.limewire.ui.swing.shell;

import java.io.IOException;

import org.limewire.util.SystemUtils;

/**
 * Registers a per-user Windows file type association without relying on JDIC.
 */
class WindowsFileTypeAssociation extends WindowsAssociation {

    private static final String HKCU = "HKEY_CURRENT_USER";

    private final String extension;
    private final String mimeType;
    private final String verb;
    private final String description;
    private final String iconPath;
    private final String fileClass;

    WindowsFileTypeAssociation(String extension,
            String mimeType,
            String executable,
            String verb,
            String description,
            String iconPath,
            String program) {
        super(executable);
        this.extension = extension.startsWith(".") ? extension : "." + extension;
        this.mimeType = mimeType;
        this.verb = verb;
        this.description = description;
        this.iconPath = iconPath;
        this.fileClass = program + this.extension.replace('.', '_');
    }

    @Override
    public boolean isAvailable() {
        String extHandler = SystemUtils.getDefaultExtensionHandler(extension);
        return "".equals(extHandler) && "".equals(SystemUtils.getDefaultMimeHandler(mimeType));
    }

    @Override
    public boolean isRegistered() {
        String extHandler = SystemUtils.getDefaultExtensionHandler(extension);
        return executable.equals(extHandler) && executable.equals(SystemUtils.getDefaultMimeHandler(mimeType));
    }

    @Override
    protected String get() throws IOException {
        return parsePath(SystemUtils.registryReadText(HKCU, "Software\\Classes\\" + fileClass + "\\shell\\" + verb + "\\command", ""));
    }

    @Override
    public void register() {
        String extensionKey = "Software\\Classes\\" + extension;
        String classKey = "Software\\Classes\\" + fileClass;
        SystemUtils.registryWriteText(HKCU, extensionKey, "", fileClass);
        SystemUtils.registryWriteText(HKCU, extensionKey, "Content Type", mimeType);
        SystemUtils.registryWriteText(HKCU, classKey, "", description);
        if (iconPath != null) {
            SystemUtils.registryWriteText(HKCU, classKey + "\\DefaultIcon", "", iconPath);
        }
        SystemUtils.registryWriteText(HKCU, classKey + "\\shell\\" + verb + "\\command", "", executable);
        SystemUtils.flushIconCache();
    }

    @Override
    public boolean canUnregister() {
        return true;
    }

    @Override
    public void unregister() {
        SystemUtils.registryDelete(HKCU, "Software\\Classes\\" + extension);
        SystemUtils.registryDelete(HKCU, "Software\\Classes\\" + fileClass);
        SystemUtils.flushIconCache();
    }
}
