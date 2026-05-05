package org.limewire.ui.compose.integration

import org.limewire.core.api.Category
import org.limewire.core.api.FilePropertyKey
import org.limewire.core.api.download.DownloadItem
import org.limewire.core.api.file.CategoryManager
import org.limewire.core.api.library.LocalFileItem
import org.limewire.core.api.library.PropertiableFile
import org.limewire.core.api.search.SearchResult
import org.limewire.core.api.upload.UploadItem
import org.limewire.ui.compose.ComposeLocalization
import org.limewire.ui.compose.tr
import org.limewire.util.CommonUtils
import org.limewire.util.FileUtils
import java.io.File
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class FileIconToken {
    FOLDER,
    AUDIO,
    VIDEO,
    IMAGE,
    DOCUMENT,
    TEXT,
    PDF,
    SPREADSHEET,
    PRESENTATION,
    CODE,
    PLAYLIST,
    ARCHIVE,
    PROGRAM,
    TORRENT,
    OTHER
}

data class FileIconPresentation(
    val token: FileIconToken,
    val badge: String? = null
)

data class FileIdentityPresentation(
    val title: String,
    val subtitle: String,
    val icon: FileIconPresentation
)

interface ComposeFileAppearanceService {
    fun presentation(item: PropertiableFile): FileIdentityPresentation
    fun presentation(result: SearchResult): FileIdentityPresentation
    fun presentation(file: File, category: Category? = null, title: String = file.name, subtitle: String = ""): FileIdentityPresentation
    fun iconForFileName(fileName: String?, category: Category?): FileIconPresentation
    fun iconForCategory(category: Category?): FileIconPresentation
    fun mimeDescription(extension: String): String?
}

class CoreComposeFileAppearanceService(
    private val categoryManager: CategoryManager
) : ComposeFileAppearanceService {
    override fun presentation(item: PropertiableFile): FileIdentityPresentation {
        return FileIdentityPresentation(
            title = itemHeading(item),
            subtitle = itemSubtitle(item),
            icon = itemIcon(item)
        )
    }

    override fun presentation(result: SearchResult): FileIdentityPresentation {
        return FileIdentityPresentation(
            title = searchHeading(result),
            subtitle = searchSubtitle(result),
            icon = iconForFileName(result.fileName, result.category)
        )
    }

    override fun presentation(file: File, category: Category?, title: String, subtitle: String): FileIdentityPresentation {
        val resolvedCategory = category ?: categoryManager.getCategoryForFile(file)
        return FileIdentityPresentation(
            title = title,
            subtitle = subtitle,
            icon = iconForFile(file, resolvedCategory)
        )
    }

    override fun iconForFileName(fileName: String?, category: Category?): FileIconPresentation {
        val resolvedCategory = category ?: fileName?.let(categoryManager::getCategoryForFilename) ?: Category.OTHER
        val normalizedExtension = fileName
            ?.let(FileUtils::getFileExtension)
            ?.trim()
            ?.lowercase(Locale.US)
            .orEmpty()
        return iconForExtension(normalizedExtension, resolvedCategory)
    }

    override fun iconForCategory(category: Category?): FileIconPresentation {
        return FileIconPresentation(categoryToken(category ?: Category.OTHER))
    }

    override fun mimeDescription(extension: String): String? {
        val normalized = extension.trim().lowercase(Locale.US)
        if (normalized.isBlank()) {
            return null
        }
        return friendlyExtensionDescription(normalized)
            ?: tr("{0} file", normalized.uppercase(currentLocale()))
    }

    private fun itemHeading(item: PropertiableFile): String {
        val displayName = item.getPropertyString(FilePropertyKey.NAME).orEmpty().trim()
        return when (item.categoryOrOther()) {
            Category.AUDIO -> {
                val artist = item.getPropertyString(FilePropertyKey.AUTHOR).orEmpty().trim()
                val title = item.getPropertyString(FilePropertyKey.TITLE).orEmpty().trim()
                if (artist.isNotBlank() && title.isNotBlank()) {
                    "$artist - $title"
                } else {
                    displayName.takeIf(String::isNotBlank)
                        ?: FileUtils.getFilenameNoExtension(item.fileName).takeIf(String::isNotBlank)
                        ?: item.fileName
                }
            }

            else -> buildDisplayFileName(item.fileName, displayName)
        }
    }

    private fun itemSubtitle(item: PropertiableFile): String {
        return when (item.categoryOrOther()) {
            Category.AUDIO -> buildAudioSubtitle(
                album = item.getPropertyString(FilePropertyKey.ALBUM),
                quality = item.getProperty(FilePropertyKey.QUALITY),
                bitrate = item.getProperty(FilePropertyKey.BITRATE),
                length = item.getProperty(FilePropertyKey.LENGTH),
                fileSize = item.getProperty(FilePropertyKey.FILE_SIZE)
            )

            Category.VIDEO -> buildVideoSubtitle(
                quality = item.getProperty(FilePropertyKey.QUALITY),
                length = item.getProperty(FilePropertyKey.LENGTH),
                fileSize = item.getProperty(FilePropertyKey.FILE_SIZE)
            )

            Category.IMAGE -> buildImageSubtitle(
                createdAt = item.getProperty(FilePropertyKey.DATE_CREATED),
                fileSize = item.getProperty(FilePropertyKey.FILE_SIZE)
            )

            Category.PROGRAM -> fileSizeLabel(item.getProperty(FilePropertyKey.FILE_SIZE))
                ?: item.categoryOrOther().getSingularName()

            Category.TORRENT -> fileSizeLabel(item.getProperty(FilePropertyKey.FILE_SIZE))
                ?: item.categoryOrOther().getSingularName()

            Category.DOCUMENT,
            Category.OTHER -> buildDocumentLikeSubtitle(
                extension = FileUtils.getFileExtension(item.fileName),
                fileSize = item.getProperty(FilePropertyKey.FILE_SIZE),
                category = item.categoryOrOther()
            )
        }
    }

    private fun itemIcon(item: PropertiableFile): FileIconPresentation {
        return when (item) {
            is LocalFileItem -> iconForFile(item.file, item.categoryOrOther())
            is DownloadItem -> iconForFile(item.saveFile, item.categoryOrOther(), item.fileName)
            is UploadItem -> iconForFile(item.file, item.categoryOrOther())
            else -> iconForFileName(item.fileName, item.categoryOrOther())
        }
    }

    private fun iconForFile(file: File, category: Category, fileNameFallback: String = file.name): FileIconPresentation {
        if (file.isDirectory) {
            return FileIconPresentation(FileIconToken.FOLDER)
        }
        val fileName = file.name.takeIf(String::isNotBlank) ?: fileNameFallback
        return iconForFileName(fileName, category)
    }

    private fun iconForExtension(extension: String, category: Category): FileIconPresentation {
        val badge = extensionBadge(extension)
        return when {
            extension in PLAYLIST_EXTENSIONS -> FileIconPresentation(FileIconToken.PLAYLIST, badge)
            extension == "pdf" -> FileIconPresentation(FileIconToken.PDF, badge)
            extension in SPREADSHEET_EXTENSIONS -> FileIconPresentation(FileIconToken.SPREADSHEET, badge)
            extension in PRESENTATION_EXTENSIONS -> FileIconPresentation(FileIconToken.PRESENTATION, badge)
            extension in TEXT_EXTENSIONS -> FileIconPresentation(FileIconToken.TEXT, badge)
            extension in CODE_EXTENSIONS -> FileIconPresentation(FileIconToken.CODE, badge)
            extension in ARCHIVE_EXTENSIONS -> FileIconPresentation(FileIconToken.ARCHIVE, badge)
            extension in PROGRAM_EXTENSIONS -> FileIconPresentation(FileIconToken.PROGRAM, badge)
            else -> FileIconPresentation(categoryToken(category), badge.takeIf {
                category == Category.DOCUMENT || category == Category.OTHER || category == Category.PROGRAM
            })
        }
    }

    private fun categoryToken(category: Category): FileIconToken {
        return when (category) {
            Category.AUDIO -> FileIconToken.AUDIO
            Category.VIDEO -> FileIconToken.VIDEO
            Category.IMAGE -> FileIconToken.IMAGE
            Category.DOCUMENT -> FileIconToken.DOCUMENT
            Category.PROGRAM -> FileIconToken.PROGRAM
            Category.TORRENT -> FileIconToken.TORRENT
            Category.OTHER -> FileIconToken.OTHER
        }
    }

    private fun searchHeading(result: SearchResult): String {
        val baseName = result.getProperty(FilePropertyKey.NAME)?.toString()
            ?.takeIf(String::isNotBlank)
            ?: result.fileNameWithoutExtension.takeIf(String::isNotBlank)
            ?: result.fileName
        return when (result.category) {
            Category.AUDIO -> {
                val artist = result.getProperty(FilePropertyKey.AUTHOR)?.toString().orEmpty().trim()
                val title = result.getProperty(FilePropertyKey.TITLE)?.toString().orEmpty().trim()
                if (artist.isNotBlank() && title.isNotBlank()) {
                    "$artist - $title"
                } else {
                    baseName
                }
            }

            else -> result.fileName.takeIf(String::isNotBlank)
                ?: buildDisplayFileName(result.fileName, baseName)
        }
    }

    private fun searchSubtitle(result: SearchResult): String {
        return when (result.category) {
            Category.AUDIO -> buildAudioSubtitle(
                album = result.getProperty(FilePropertyKey.ALBUM)?.toString(),
                quality = result.getProperty(FilePropertyKey.QUALITY),
                bitrate = result.getProperty(FilePropertyKey.BITRATE),
                length = result.getProperty(FilePropertyKey.LENGTH),
                fileSize = result.size
            )

            Category.VIDEO -> buildVideoSubtitle(
                quality = result.getProperty(FilePropertyKey.QUALITY),
                length = result.getProperty(FilePropertyKey.LENGTH),
                fileSize = result.size
            )

            Category.IMAGE -> buildImageSubtitle(
                createdAt = result.getProperty(FilePropertyKey.DATE_CREATED),
                fileSize = result.size
            )

            Category.PROGRAM,
            Category.TORRENT -> fileSizeLabel(result.size) ?: result.category.getSingularName()

            Category.DOCUMENT,
            Category.OTHER -> buildDocumentLikeSubtitle(
                extension = result.fileExtension,
                fileSize = result.size,
                category = result.category
            )
        }
    }

    private fun buildAudioSubtitle(
        album: String?,
        quality: Any?,
        bitrate: Any?,
        length: Any?,
        fileSize: Any?
    ): String {
        val parts = mutableListOf<String>()
        album?.trim()?.takeIf(String::isNotBlank)?.let(parts::add)
        qualityLabel(quality, bitrate)?.let(parts::add)
        lengthLabel(length)?.let(parts::add) ?: fileSizeLabel(fileSize)?.let(parts::add)
        return parts.joinToString(" - ").ifBlank { Category.AUDIO.getSingularName() }
    }

    private fun buildVideoSubtitle(
        quality: Any?,
        length: Any?,
        fileSize: Any?
    ): String {
        val parts = mutableListOf<String>()
        qualityLabel(quality, null)?.let(parts::add)
        lengthLabel(length)?.let(parts::add)
        fileSizeLabel(fileSize)?.let(parts::add)
        return parts.joinToString(" - ").ifBlank { Category.VIDEO.getSingularName() }
    }

    private fun buildImageSubtitle(
        createdAt: Any?,
        fileSize: Any?
    ): String {
        val parts = mutableListOf<String>()
        timeMillis(createdAt)
            ?.takeIf { it > 0L }
            ?.let { formatDate(it) }
            ?.let(parts::add)
        fileSizeLabel(fileSize)?.let(parts::add)
        return parts.joinToString(" - ").ifBlank { Category.IMAGE.getSingularName() }
    }

    private fun buildDocumentLikeSubtitle(
        extension: String,
        fileSize: Any?,
        category: Category
    ): String {
        val parts = mutableListOf<String>()
        mimeDescription(extension)?.takeIf(String::isNotBlank)?.let(parts::add)
        fileSizeLabel(fileSize)?.let(parts::add)
        return parts.joinToString(" - ").ifBlank { category.getSingularName() }
    }

    private fun buildDisplayFileName(fileName: String, displayName: String): String {
        if (fileName.isBlank()) {
            return displayName
        }
        val extension = FileUtils.getFileExtension(fileName).trim()
        val baseName = displayName.ifBlank {
            FileUtils.getFilenameNoExtension(fileName).takeIf(String::isNotBlank) ?: fileName
        }
        return when {
            extension.isBlank() -> baseName
            baseName.endsWith(".$extension", ignoreCase = true) -> baseName
            else -> "$baseName.$extension"
        }
    }

    private fun extensionBadge(extension: String): String? {
        if (extension.isBlank()) {
            return null
        }
        val badge = extension.uppercase(currentLocale())
        return badge.takeIf { it.length in 2..4 }
    }

    private fun timeMillis(value: Any?): Long? {
        return when (value) {
            is Number -> value.toLong()
            is String -> value.toLongOrNull()
            else -> null
        }
    }

    private fun formatDate(timeMillis: Long): String {
        return SimpleDateFormat("M/d/yyyy", currentLocale()).format(Date(timeMillis))
    }

    private fun fileSizeLabel(size: Any?): String? {
        val bytes = when (size) {
            is Number -> size.toLong()
            is String -> size.toLongOrNull()
            else -> null
        } ?: return null
        return if (bytes > 0L) compactSize(bytes) else null
    }

    private fun lengthLabel(value: Any?): String? {
        val seconds = when (value) {
            is Number -> value.toLong()
            is String -> value.toLongOrNull()
            else -> null
        } ?: return null
        return if (seconds > 0L) CommonUtils.seconds2time(seconds) else null
    }

    private fun qualityLabel(quality: Any?, bitrate: Any?): String? {
        val qualityScore = when (quality) {
            is Number -> quality.toLong()
            is String -> quality.toLongOrNull()
            else -> null
        } ?: return null
        val label = when {
            qualityScore <= 1L -> tr("Poor Quality")
            qualityScore == 2L -> tr("Good Quality")
            else -> tr("Excellent Quality")
        }
        val bitrateValue = when (bitrate) {
            is Number -> bitrate.toLong()
            is String -> bitrate.toLongOrNull()
            else -> null
        }
        return if (bitrateValue != null && bitrateValue > 0L) {
            "$label ($bitrateValue)"
        } else {
            label
        }
    }

    private fun compactSize(bytes: Long): String {
        if (bytes < 0L) {
            return tr("? KB")
        }
        val locale = currentLocale()
        val unit = when {
            bytes < KILOBYTE -> FileSizeUnit("{0} bytes", 1.0)
            bytes < MEGABYTE -> FileSizeUnit("{0} KB", KILOBYTE.toDouble())
            bytes < GIGABYTE -> FileSizeUnit("{0} MB", MEGABYTE.toDouble())
            bytes < TERABYTE -> FileSizeUnit("{0} GB", GIGABYTE.toDouble())
            else -> FileSizeUnit("{0} TB", TERABYTE.toDouble())
        }
        val formatter = NumberFormat.getNumberInstance(locale).apply {
            isGroupingUsed = true
            maximumFractionDigits = if (bytes < MEGABYTE) 1 else 2
            minimumFractionDigits = 0
        }
        return tr(unit.pattern, formatter.format(bytes / unit.divisor))
    }

    private fun friendlyExtensionDescription(extension: String): String? {
        return when (extension) {
            "pdf" -> tr("PDF document")
            in PLAYLIST_EXTENSIONS -> tr("Playlist")
            in ARCHIVE_EXTENSIONS -> tr("Archive")
            in SPREADSHEET_EXTENSIONS -> tr("Spreadsheet")
            in PRESENTATION_EXTENSIONS -> tr("Presentation")
            in TEXT_EXTENSIONS -> tr("Text document")
            in CODE_EXTENSIONS -> when (extension) {
                "html", "htm" -> tr("Web page")
                "json" -> tr("JSON file")
                "xml" -> tr("XML file")
                "yaml", "yml" -> tr("YAML file")
                "md" -> tr("Markdown document")
                else -> tr("Code file")
            }

            "doc", "docx", "odt", "pages" -> tr("Document")
            "epub", "mobi" -> tr("eBook")
            "apk", "app", "appx", "deb", "dmg", "exe", "msi", "pkg", "rpm" -> tr("App installer")
            else -> null
        }
    }

    private fun currentLocale(): Locale {
        return runCatching { ComposeLocalization.service.currentLocale() }.getOrDefault(Locale.getDefault())
    }

    private fun PropertiableFile.categoryOrOther(): Category = category ?: Category.OTHER

    private data class FileSizeUnit(
        val pattern: String,
        val divisor: Double
    )

    companion object {
        private const val KILOBYTE = 1024L
        private const val MEGABYTE = KILOBYTE * 1024L
        private const val GIGABYTE = MEGABYTE * 1024L
        private const val TERABYTE = GIGABYTE * 1024L

        private val PLAYLIST_EXTENSIONS = setOf("cue", "m3u", "m3u8", "pls", "xspf")
        private val ARCHIVE_EXTENSIONS = setOf("7z", "bz2", "gz", "rar", "tar", "tgz", "tbz", "xz", "zip")
        private val SPREADSHEET_EXTENSIONS = setOf("csv", "numbers", "ods", "tsv", "xls", "xlsx")
        private val PRESENTATION_EXTENSIONS = setOf("key", "odp", "ppt", "pptx")
        private val TEXT_EXTENSIONS = setOf("cfg", "conf", "ini", "log", "md", "nfo", "rtf", "txt")
        private val CODE_EXTENSIONS = setOf(
            "c", "cc", "cpp", "css", "go", "h", "hpp", "htm", "html", "java", "js",
            "json", "kt", "kts", "php", "py", "rb", "rs", "sh", "sql", "swift",
            "ts", "tsx", "xml", "yaml", "yml"
        )
        private val PROGRAM_EXTENSIONS = setOf("apk", "app", "appx", "bat", "cmd", "deb", "dmg", "exe", "jar", "msi", "pkg", "rpm")
    }
}
