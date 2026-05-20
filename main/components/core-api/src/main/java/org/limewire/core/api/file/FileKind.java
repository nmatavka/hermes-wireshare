package org.limewire.core.api.file;

import org.limewire.core.api.Category;

/**
 * Narrow file classification used for presentation and local filtering.
 *
 * <p>The older {@link Category} enum remains the protocol/storage boundary;
 * each kind maps back to a broad category for legacy search, sharing, and
 * metadata paths.</p>
 */
public enum FileKind {
    MODEL_3D("3D", Category.DOCUMENT),
    ARCHIVE("Archive", Category.DOCUMENT),
    AUDIO("Audio", Category.AUDIO),
    BOOK("Book", Category.DOCUMENT),
    CODE("Code", Category.DOCUMENT),
    EXEC("Executable", Category.PROGRAM),
    FONT("Font", Category.DOCUMENT),
    IMAGE("Image", Category.IMAGE),
    SHEET("Sheet", Category.DOCUMENT),
    SLIDE("Slide", Category.DOCUMENT),
    TEXT("Text", Category.DOCUMENT),
    VIDEO("Video", Category.VIDEO),
    WEB("Web", Category.DOCUMENT),
    TORRENT("Torrent", Category.TORRENT),
    OTHER("Other", Category.OTHER);

    private final String displayName;
    private final Category broadCategory;

    FileKind(String displayName, Category broadCategory) {
        this.displayName = displayName;
        this.broadCategory = broadCategory;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Category getBroadCategory() {
        return broadCategory;
    }
}
