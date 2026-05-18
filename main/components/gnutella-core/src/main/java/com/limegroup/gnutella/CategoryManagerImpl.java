package com.limegroup.gnutella;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicReference;

import org.limewire.core.api.Category;
import org.limewire.core.api.file.CategoryManager;
import org.limewire.core.api.file.FileKind;
import org.limewire.core.settings.LibrarySettings;
import org.limewire.setting.StringArraySetting;
import org.limewire.util.FileUtils;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSortedSet;
import com.google.inject.Singleton;

@Singleton
class CategoryManagerImpl implements CategoryManager {

    /** A secondary category class designed to preserve protocol-era program filters. */
    private enum InternalCategory {
        AUDIO(Category.AUDIO),
        VIDEO(Category.VIDEO),
        IMAGE(Category.IMAGE),
        DOCUMENT(Category.DOCUMENT),
        PROGRAM_OSX_LINUX(Category.PROGRAM),
        PROGRAM_WINDOWS(Category.PROGRAM),
        PROGRAM_ALL(Category.PROGRAM),
        OTHER(Category.OTHER),
        TORRENT(Category.TORRENT);

        private final Category category;

        InternalCategory(Category category) {
            this.category = category;
        }

        Category getCategory() {
            return category;
        }

        static InternalCategory fromCategory(Category category) {
            switch (category) {
            case AUDIO:
                return AUDIO;
            case DOCUMENT:
                return DOCUMENT;
            case IMAGE:
                return IMAGE;
            case PROGRAM:
                return PROGRAM_ALL;
            case VIDEO:
                return VIDEO;
            case OTHER:
                return OTHER;
            case TORRENT:
                return TORRENT;
            default:
                throw new IllegalArgumentException(category.toString());
            }
        }
    }

    private static final List<FileKind> PRIMARY_FILE_KIND_ORDER = Arrays.asList(
        FileKind.TORRENT,
        FileKind.EXEC,
        FileKind.MODEL_3D,
        FileKind.WEB,
        FileKind.BOOK,
        FileKind.SHEET,
        FileKind.SLIDE,
        FileKind.TEXT,
        FileKind.AUDIO,
        FileKind.VIDEO,
        FileKind.IMAGE,
        FileKind.FONT,
        FileKind.CODE,
        FileKind.ARCHIVE
    );

    private static final Collection<String> OSX_LINUX_EXEC_EXTENSIONS =
        extensions("bin", "command", "sh", "bash", "csh", "fish", "ksh", "zsh");

    private static final Collection<String> WINDOWS_EXEC_EXTENSIONS =
        extensions("exe", "msi", "bin", "cmd", "com", "bat", "crx");

    private static final Map<FileKind, Collection<String>> BUILT_IN_FILE_KIND_EXTENSIONS =
        buildBuiltInFileKindExtensions();

    private final Map<FileKind, AtomicReference<Collection<String>>> fileKindExtensionMap;
    private final Map<FileKind, Predicate<String>> fileKindPredicateMap;
    private final AtomicReference<Map<String, FileKind>> primaryFileKindByExtension =
        new AtomicReference<Map<String, FileKind>>();
    private final Map<InternalCategory, AtomicReference<Collection<String>>> extensionMap;
    private final Map<InternalCategory, Predicate<String>> predicateMap;
    private final Map<InternalCategory, StringArraySetting> settingMap;

    CategoryManagerImpl() {
        fileKindExtensionMap = new EnumMap<FileKind, AtomicReference<Collection<String>>>(FileKind.class);
        for (FileKind fileKind : FileKind.values()) {
            if (fileKind != FileKind.OTHER) {
                fileKindExtensionMap.put(fileKind, new AtomicReference<Collection<String>>());
            }
        }

        extensionMap = new EnumMap<InternalCategory, AtomicReference<Collection<String>>>(InternalCategory.class);
        for (InternalCategory category : InternalCategory.values()) {
            if (category != InternalCategory.OTHER) {
                extensionMap.put(category, new AtomicReference<Collection<String>>());
            }
        }

        settingMap = new EnumMap<InternalCategory, StringArraySetting>(InternalCategory.class);
        settingMap.put(InternalCategory.AUDIO, LibrarySettings.ADDITIONAL_AUDIO_EXTS);
        settingMap.put(InternalCategory.DOCUMENT, LibrarySettings.ADDITIONAL_DOCUMENT_EXTS);
        settingMap.put(InternalCategory.IMAGE, LibrarySettings.ADDITIONAL_IMAGE_EXTS);
        settingMap.put(InternalCategory.PROGRAM_OSX_LINUX, LibrarySettings.ADDITIONAL_PROGRAM_OSX_LINUX_EXTS);
        settingMap.put(InternalCategory.PROGRAM_WINDOWS, LibrarySettings.ADDITIONAL_PROGRAM_WINDOWS_EXTS);
        settingMap.put(InternalCategory.VIDEO, LibrarySettings.ADDITIONAL_VIDEO_EXTS);
        settingMap.put(InternalCategory.TORRENT, LibrarySettings.ADDITIONAL_TORRENT_EXTS);

        fileKindPredicateMap = new EnumMap<FileKind, Predicate<String>>(FileKind.class);
        for (final FileKind fileKind : FileKind.values()) {
            fileKindPredicateMap.put(fileKind, new Predicate<String>() {
                @Override
                public boolean apply(String input) {
                    return getFileKindForExtension(input) == fileKind;
                }
            });
        }

        predicateMap = new EnumMap<InternalCategory, Predicate<String>>(InternalCategory.class);
        for (final InternalCategory category : InternalCategory.values()) {
            predicateMap.put(category, new Predicate<String>() {
                @Override
                public boolean apply(String input) {
                    return matchesInternalCategory(input, category);
                }
            });
        }

        rebuildExtensions();
    }

    private void rebuildExtensions() {
        Map<FileKind, Set<String>> mutableFileKindExtensions = new EnumMap<FileKind, Set<String>>(FileKind.class);
        for (FileKind fileKind : FileKind.values()) {
            if (fileKind != FileKind.OTHER) {
                mutableFileKindExtensions.put(fileKind, new TreeSet<String>(String.CASE_INSENSITIVE_ORDER));
                Collection<String> builtIn = BUILT_IN_FILE_KIND_EXTENSIONS.get(fileKind);
                if (builtIn != null) {
                    mutableFileKindExtensions.get(fileKind).addAll(builtIn);
                }
            }
        }

        addRemoteExtensions(mutableFileKindExtensions, FileKind.AUDIO, LibrarySettings.ADDITIONAL_AUDIO_EXTS);
        addRemoteExtensions(mutableFileKindExtensions, FileKind.VIDEO, LibrarySettings.ADDITIONAL_VIDEO_EXTS);
        addRemoteExtensions(mutableFileKindExtensions, FileKind.IMAGE, LibrarySettings.ADDITIONAL_IMAGE_EXTS);
        addRemoteExtensions(mutableFileKindExtensions, FileKind.TEXT, LibrarySettings.ADDITIONAL_DOCUMENT_EXTS);
        addRemoteExtensions(mutableFileKindExtensions, FileKind.EXEC, LibrarySettings.ADDITIONAL_PROGRAM_OSX_LINUX_EXTS);
        addRemoteExtensions(mutableFileKindExtensions, FileKind.EXEC, LibrarySettings.ADDITIONAL_PROGRAM_WINDOWS_EXTS);
        addRemoteExtensions(mutableFileKindExtensions, FileKind.TORRENT, LibrarySettings.ADDITIONAL_TORRENT_EXTS);

        for (Map.Entry<FileKind, Set<String>> entry : mutableFileKindExtensions.entrySet()) {
            fileKindExtensionMap.get(entry.getKey()).set(immutableExtensions(entry.getValue()));
        }

        Map<String, FileKind> primaryKinds = buildPrimaryFileKindMap();
        primaryFileKindByExtension.set(primaryKinds);
        rebuildBroadCategoryExtensions(primaryKinds);
    }

    private void addRemoteExtensions(Map<FileKind, Set<String>> extensions, FileKind fileKind, StringArraySetting setting) {
        Set<String> target = extensions.get(fileKind);
        if (target == null) {
            return;
        }
        for (String extension : setting.get()) {
            String normalized = normalizeExtension(extension);
            if (normalized.length() > 0) {
                target.add(normalized);
            }
        }
    }

    private Map<String, FileKind> buildPrimaryFileKindMap() {
        Map<String, FileKind> primaryKinds = new LinkedHashMap<String, FileKind>();
        for (FileKind fileKind : PRIMARY_FILE_KIND_ORDER) {
            Collection<String> extensions = fileKindExtensionMap.get(fileKind).get();
            for (String extension : extensions) {
                String normalized = normalizeExtension(extension);
                if (normalized.length() > 0 && !primaryKinds.containsKey(normalized)) {
                    primaryKinds.put(normalized, fileKind);
                }
            }
        }
        return Collections.unmodifiableMap(primaryKinds);
    }

    private void rebuildBroadCategoryExtensions(Map<String, FileKind> primaryKinds) {
        Map<InternalCategory, Set<String>> broad = new EnumMap<InternalCategory, Set<String>>(InternalCategory.class);
        for (InternalCategory category : extensionMap.keySet()) {
            broad.put(category, new TreeSet<String>(String.CASE_INSENSITIVE_ORDER));
        }

        for (Map.Entry<String, FileKind> entry : primaryKinds.entrySet()) {
            String extension = entry.getKey();
            FileKind fileKind = entry.getValue();
            Category category = fileKind.getBroadCategory();
            if (category == Category.PROGRAM) {
                broad.get(InternalCategory.PROGRAM_ALL).add(extension);
                if (OSX_LINUX_EXEC_EXTENSIONS.contains(extension)) {
                    broad.get(InternalCategory.PROGRAM_OSX_LINUX).add(extension);
                }
                if (WINDOWS_EXEC_EXTENSIONS.contains(extension)) {
                    broad.get(InternalCategory.PROGRAM_WINDOWS).add(extension);
                }
            } else {
                InternalCategory internalCategory = InternalCategory.fromCategory(category);
                Set<String> extensions = broad.get(internalCategory);
                if (extensions != null) {
                    extensions.add(extension);
                }
            }
        }

        for (Map.Entry<InternalCategory, StringArraySetting> entry : settingMap.entrySet()) {
            Set<String> extensions = broad.get(entry.getKey());
            if (extensions != null) {
                for (String extension : entry.getValue().get()) {
                    String normalized = normalizeExtension(extension);
                    if (normalized.length() > 0) {
                        extensions.add(normalized);
                    }
                }
            }
        }

        broad.get(InternalCategory.PROGRAM_ALL).addAll(broad.get(InternalCategory.PROGRAM_OSX_LINUX));
        broad.get(InternalCategory.PROGRAM_ALL).addAll(broad.get(InternalCategory.PROGRAM_WINDOWS));

        for (Map.Entry<InternalCategory, Set<String>> entry : broad.entrySet()) {
            extensionMap.get(entry.getKey()).set(immutableExtensions(entry.getValue()));
        }
    }

    @Override
    public Category getCategoryForExtension(String extension) {
        return getFileKindForExtension(extension).getBroadCategory();
    }

    @Override
    public Category getCategoryForFilename(String filename) {
        return getFileKindForFilename(filename).getBroadCategory();
    }

    @Override
    public Category getCategoryForFile(File file) {
        return getFileKindForFile(file).getBroadCategory();
    }

    @Override
    public FileKind getFileKindForExtension(String extension) {
        FileKind fileKind = primaryFileKindByExtension.get().get(normalizeExtension(extension));
        return fileKind == null ? FileKind.OTHER : fileKind;
    }

    @Override
    public FileKind getFileKindForFilename(String filename) {
        if (filename == null) {
            return FileKind.OTHER;
        }
        return getFileKindForExtension(extensionForFilename(filename));
    }

    @Override
    public FileKind getFileKindForFile(File file) {
        if (file == null) {
            return FileKind.OTHER;
        }
        return getFileKindForFilename(file.getName());
    }

    @Override
    public Collection<String> getExtensionsForCategory(Category category) {
        AtomicReference<Collection<String>> ref = extensionMap.get(InternalCategory.fromCategory(category));
        if (ref != null) {
            return ref.get();
        } else {
            assert category == Category.OTHER;
            return Collections.emptySet();
        }
    }

    @Override
    public Collection<String> getExtensionsForFileKind(FileKind fileKind) {
        AtomicReference<Collection<String>> ref = fileKindExtensionMap.get(fileKind);
        return ref == null ? Collections.<String>emptySet() : ref.get();
    }

    @Override
    public Predicate<String> getExtensionFilterForCategory(Category category) {
        return predicateMap.get(InternalCategory.fromCategory(category));
    }

    @Override
    public Predicate<String> getOsxAndLinuxProgramsFilter() {
        return predicateMap.get(InternalCategory.PROGRAM_OSX_LINUX);
    }

    @Override
    public Predicate<String> getWindowsProgramsFilter() {
        return predicateMap.get(InternalCategory.PROGRAM_WINDOWS);
    }

    @Override
    public boolean containsCategory(Category category, List<String> paths) {
        if (paths == null) {
            return false;
        }

        for (String path : paths) {
            if (getCategoryForFilename(path) == category) {
                return true;
            }
        }

        return false;
    }

    private boolean matchesInternalCategory(String extension, InternalCategory category) {
        if (category == InternalCategory.OTHER) {
            return getCategoryForExtension(extension) == Category.OTHER;
        }
        AtomicReference<Collection<String>> ref = extensionMap.get(category);
        return ref != null && ref.get().contains(normalizeExtension(extension));
    }

    private String extensionForFilename(String filename) {
        String normalizedName = filename.trim().toLowerCase(Locale.US);
        for (String extension : primaryFileKindByExtension.get().keySet()) {
            if (extension.indexOf('.') >= 0 && normalizedName.endsWith("." + extension)) {
                return extension;
            }
        }
        return FileUtils.getFileExtension(filename);
    }

    private static String normalizeExtension(String extension) {
        if (extension == null) {
            return "";
        }
        String normalized = extension.trim().toLowerCase(Locale.US);
        while (normalized.startsWith(".")) {
            normalized = normalized.substring(1);
        }
        return normalized;
    }

    private static Collection<String> immutableExtensions(Collection<String> extensions) {
        return ImmutableSortedSet.orderedBy(String.CASE_INSENSITIVE_ORDER)
            .addAll(extensions)
            .build();
    }

    private static Collection<String> extensions(String... extensions) {
        return ImmutableSortedSet.orderedBy(String.CASE_INSENSITIVE_ORDER)
            .add(extensions)
            .build();
    }

    private static Map<FileKind, Collection<String>> buildBuiltInFileKindExtensions() {
        Map<FileKind, Collection<String>> map = new EnumMap<FileKind, Collection<String>>(FileKind.class);
        map.put(FileKind.MODEL_3D, extensions(
            "3ds", "f3d", "3mf", "smt", "stp", "step", "stl", "obj", "gcode", "scad"
        ));
        map.put(FileKind.ARCHIVE, extensions(
            "7z", "a", "aar", "apk", "ar", "bz2", "br", "cab", "cpio", "deb", "dmg", "egg",
            "gz", "iso", "jar", "lha", "lz", "lz4", "lzma", "lzo", "mar", "pea", "rar", "rpm",
            "s7z", "shar", "tar", "tbz2", "tgz", "tlz", "txz", "war", "whl", "xpi", "zip",
            "zipx", "zst", "xz", "pak"
        ));
        map.put(FileKind.AUDIO, extensions(
            "aac", "aiff", "ape", "au", "flac", "gsm", "it", "m3u", "m4a", "mid", "mod",
            "mp3", "mpa", "ogg", "opus", "pls", "ra", "s3m", "sid", "wav", "wma", "xm"
        ));
        map.put(FileKind.BOOK, extensions(
            "mobi", "epub", "azw1", "azw3", "azw4", "azw6", "azw", "cbr", "cbz"
        ));
        map.put(FileKind.CODE, extensions(
            "1.ada", "2.ada", "ada", "adb", "ads", "asm", "asp", "aspx", "bas", "bash",
            "bat", "c++", "c", "cbl", "cc", "class", "clj", "cob", "cpp", "cs", "csh",
            "cxx", "d", "diff", "dll", "e", "el", "f", "f77", "f90", "fish", "for", "fth",
            "ftn", "go", "groovy", "h", "hh", "hpp", "hs", "htm", "html", "hxx", "inc",
            "java", "js", "json", "jsp", "jsx", "ksh", "kt", "kts", "lhs", "lisp", "lua",
            "m", "m4", "nim", "patch", "php", "php3", "php4", "php5", "phtml", "pl", "po",
            "pp", "prql", "py", "ps1", "psd1", "psm1", "ps1xml", "psc1", "pssc", "psrc",
            "r", "rb", "rs", "s", "scala", "sh", "sql", "swg", "swift", "v", "vb",
            "vcxproj", "wll", "xcodeproj", "xml", "xll", "zig", "zsh"
        ));
        map.put(FileKind.EXEC, extensions(
            "exe", "msi", "bin", "cmd", "com", "command", "sh", "bat", "crx", "bash",
            "csh", "fish", "ksh", "zsh"
        ));
        map.put(FileKind.FONT, extensions(
            "eot", "otf", "ttf", "woff", "woff2"
        ));
        map.put(FileKind.IMAGE, extensions(
            "3dm", "3ds", "max", "avif", "bmp", "dds", "gif", "heic", "heif", "jpg",
            "jpeg", "jxl", "png", "psd", "xcf", "tga", "thm", "tif", "tiff", "yuv",
            "ai", "eps", "ps", "svg", "dwg", "dxf", "gpx", "kml", "kmz", "webp"
        ));
        map.put(FileKind.SHEET, extensions(
            "ods", "xls", "xlsx", "csv", "tsv", "ics", "vcf"
        ));
        map.put(FileKind.SLIDE, extensions(
            "ppt", "pptx", "odp"
        ));
        map.put(FileKind.TEXT, extensions(
            "doc", "docx", "ebook", "log", "md", "msg", "odt", "org", "pages", "pdf",
            "rtf", "rst", "tex", "txt", "wpd", "wps"
        ));
        map.put(FileKind.VIDEO, extensions(
            "3g2", "3gp", "aaf", "asf", "avchd", "avi", "car", "dav", "drc", "flv",
            "m2v", "m2ts", "m4p", "m4v", "mkv", "mng", "mov", "mp2", "mp4", "mpe",
            "mpeg", "mpg", "mpv", "mts", "mxf", "nsv", "ogv", "ogm", "ogx", "qt",
            "rm", "rmvb", "roq", "srt", "svi", "vob", "webm", "wmv", "xba", "yuv"
        ));
        map.put(FileKind.WEB, extensions(
            "asp", "aspx", "css", "htm", "html", "inc", "js", "jsp", "jsx", "less",
            "php", "php3", "php4", "php5", "phtml", "scss", "ts", "tsx", "wasm"
        ));
        map.put(FileKind.TORRENT, extensions("torrent"));
        map.put(FileKind.OTHER, Collections.<String>emptySet());
        return Collections.unmodifiableMap(map);
    }
}
