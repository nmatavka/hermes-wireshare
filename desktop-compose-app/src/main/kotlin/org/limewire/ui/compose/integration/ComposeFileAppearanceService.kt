package org.limewire.ui.compose.integration

import androidx.compose.runtime.mutableStateMapOf
import org.limewire.core.api.Category
import org.limewire.core.api.FilePropertyKey
import org.limewire.core.api.download.DownloadItem
import org.limewire.core.api.file.CategoryManager
import org.limewire.core.api.library.LocalFileItem
import org.limewire.core.api.library.PropertiableFile
import org.limewire.core.api.search.SearchResult
import org.limewire.core.api.upload.UploadItem
import org.limewire.ui.swing.util.CategoryIconManager
import org.limewire.ui.swing.util.GuiUtils
import org.limewire.ui.swing.util.IconManager
import org.limewire.ui.swing.util.PropertiableHeadings
import org.limewire.util.CommonUtils
import org.limewire.util.FileUtils
import java.awt.EventQueue
import java.awt.image.BufferedImage
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.swing.Icon

data class FileIdentityPresentation(
    val title: String,
    val subtitle: String,
    val icon: BufferedImage?
)

interface ComposeFileAppearanceService {
    fun presentation(item: PropertiableFile): FileIdentityPresentation
    fun presentation(result: SearchResult): FileIdentityPresentation
    fun presentation(file: File, category: Category? = null, title: String = file.name, subtitle: String = ""): FileIdentityPresentation
    fun iconForFileName(fileName: String?, category: Category?): BufferedImage?
    fun iconForCategory(category: Category?): BufferedImage?
    fun mimeDescription(extension: String): String?
}

class SwingComposeFileAppearanceService(
    private val categoryManager: CategoryManager,
    private val iconManager: IconManager,
    private val categoryIconManager: CategoryIconManager,
    private val propertiableHeadings: PropertiableHeadings
) : ComposeFileAppearanceService {
    private val iconCache = mutableStateMapOf<String, BufferedImage>()
    private val categoryIconCache = mutableMapOf<Category, BufferedImage?>()
    private val pendingLoads = ConcurrentHashMap.newKeySet<String>()
    private val loader: ExecutorService = Executors.newSingleThreadExecutor { runnable ->
        Thread(runnable, "ComposeFileAppearance").apply { isDaemon = true }
    }
    private val dateFormat = SimpleDateFormat("M/d/yyyy")

    override fun presentation(item: PropertiableFile): FileIdentityPresentation {
        val title = propertiableHeadings.getHeading(item)
            .takeIf(String::isNotBlank)
            ?: defaultHeading(item)
        val subtitle = propertiableHeadings.getSubHeading(item)
            .takeIf(String::isNotBlank)
            ?: defaultSubtitle(item)
        return FileIdentityPresentation(
            title = title,
            subtitle = subtitle,
            icon = localIconFor(item)
        )
    }

    override fun presentation(result: SearchResult): FileIdentityPresentation {
        return FileIdentityPresentation(
            title = searchHeading(result),
            subtitle = searchSubtitle(result),
            icon = searchIconFor(result)
        )
    }

    override fun presentation(file: File, category: Category?, title: String, subtitle: String): FileIdentityPresentation {
        return FileIdentityPresentation(
            title = title,
            subtitle = subtitle,
            icon = localIconFor(file, category)
        )
    }

    override fun iconForFileName(fileName: String?, category: Category?): BufferedImage? {
        val resolvedCategory = category ?: fileName?.let(categoryManager::getCategoryForFilename)
        if (!usesMimeAwareIcons(resolvedCategory)) {
            return iconForCategory(resolvedCategory)
        }
        val extension = fileName
            ?.let(FileUtils::getFileExtension)
            ?.trim()
            .orEmpty()
        if (extension.isBlank()) {
            return iconForCategory(resolvedCategory)
        }
        val normalized = extension.lowercase(Locale.US)
        val key = "ext:$normalized"
        val fallback = iconForCategory(resolvedCategory)
        iconCache[key]?.let { return it }
        queueIconLoad(key, fallback) { iconManager.getIconForExtension(normalized) }
        return fallback
    }

    override fun iconForCategory(category: Category?): BufferedImage? {
        val resolvedCategory = category ?: Category.OTHER
        return categoryIconCache.getOrPut(resolvedCategory) {
            renderIcon(categoryIconManager.getIcon(resolvedCategory))
        }
    }

    override fun mimeDescription(extension: String): String? {
        val normalized = extension.trim().lowercase(Locale.US)
        if (normalized.isBlank()) {
            return null
        }
        return iconManager.getMIMEDescription(normalized)
    }

    private fun defaultHeading(item: PropertiableFile): String {
        return when (item) {
            is LocalFileItem -> item.fileName
            is DownloadItem -> item.fileName
            is UploadItem -> item.fileName
            else -> item.fileName
        }
    }

    private fun defaultSubtitle(item: PropertiableFile): String {
        val size = fileSizeOf(item)
        return if (size != null && size > 0L) {
            compactSize(size)
        } else {
            item.category.getSingularName()
        }
    }

    private fun localIconFor(item: PropertiableFile): BufferedImage? {
        return when (item) {
            is LocalFileItem -> localIconFor(item.file, item.category)
            is DownloadItem -> localIconFor(item.saveFile, item.category)
            is UploadItem -> localIconFor(item.file, item.category)
            else -> iconForFileName(item.fileName, item.category)
        }
    }

    private fun localIconFor(file: File, category: Category?): BufferedImage? {
        val resolvedCategory = category ?: categoryManager.getCategoryForFile(file)
        if (!usesMimeAwareIcons(resolvedCategory)) {
            return iconForCategory(resolvedCategory)
        }
        val key = "file:${file.absolutePath}"
        val fallback = iconForCategory(resolvedCategory)
        iconCache[key]?.let { return it }
        queueIconLoad(key, fallback) { iconManager.getIconForFile(file) }
        return fallback
    }

    private fun searchIconFor(result: SearchResult): BufferedImage? {
        val category = result.category
        if (!usesMimeAwareIcons(category)) {
            return iconForCategory(category)
        }
        val extension = result.fileExtension.trim()
        if (extension.isBlank()) {
            return iconForCategory(category)
        }
        val normalized = extension.lowercase(Locale.US)
        val key = "ext:$normalized"
        val fallback = iconForCategory(category)
        iconCache[key]?.let { return it }
        queueIconLoad(key, fallback) { iconManager.getIconForExtension(normalized) }
        return fallback
    }

    private fun queueIconLoad(key: String, fallback: BufferedImage?, resolver: () -> Icon?) {
        if (!pendingLoads.add(key)) {
            return
        }
        loader.execute {
            val resolved = renderIcon(runCatching { resolver() }.getOrNull()) ?: fallback
            EventQueue.invokeLater {
                resolved?.let { iconCache[key] = it }
                pendingLoads.remove(key)
            }
        }
    }

    private fun searchHeading(result: SearchResult): String {
        val baseName = result.getProperty(FilePropertyKey.NAME)?.toString()
            ?.takeIf(String::isNotBlank)
            ?: result.fileNameWithoutExtension.takeIf(String::isNotBlank)
            ?: result.fileName
        return when (result.category) {
            Category.AUDIO -> {
                val artist = result.getProperty(FilePropertyKey.AUTHOR)?.toString().orEmpty()
                val title = result.getProperty(FilePropertyKey.TITLE)?.toString().orEmpty()
                if (artist.isNotBlank() && title.isNotBlank()) {
                    "$artist - $title"
                } else {
                    baseName
                }
            }
            else -> result.fileName.takeIf(String::isNotBlank) ?: baseName
        }
    }

    private fun searchSubtitle(result: SearchResult): String {
        return when (result.category) {
            Category.AUDIO -> {
                val parts = mutableListOf<String>()
                result.getProperty(FilePropertyKey.ALBUM)?.toString()
                    ?.takeIf(String::isNotBlank)
                    ?.let(parts::add)
                qualityLabel(result)?.let(parts::add)
                lengthLabel(result.getProperty(FilePropertyKey.LENGTH))?.let(parts::add)
                    ?: fileSizeLabel(result.size)?.let(parts::add)
                parts.joinToString(" - ").ifBlank { result.category.getSingularName() }
            }

            Category.VIDEO -> {
                val parts = mutableListOf<String>()
                qualityLabel(result)?.let(parts::add)
                lengthLabel(result.getProperty(FilePropertyKey.LENGTH))?.let(parts::add)
                fileSizeLabel(result.size)?.let(parts::add)
                parts.joinToString(" - ").ifBlank { result.category.getSingularName() }
            }

            Category.IMAGE -> {
                val parts = mutableListOf<String>()
                (result.getProperty(FilePropertyKey.DATE_CREATED) as? Number)
                    ?.toLong()
                    ?.takeIf { it > 0L }
                    ?.let { dateFormat.format(Date(it)) }
                    ?.let(parts::add)
                fileSizeLabel(result.size)?.let(parts::add)
                parts.joinToString(" - ").ifBlank { result.category.getSingularName() }
            }

            Category.PROGRAM -> fileSizeLabel(result.size) ?: result.category.getSingularName()

            Category.DOCUMENT,
            Category.OTHER -> {
                val parts = mutableListOf<String>()
                mimeDescription(result.fileExtension)?.takeIf(String::isNotBlank)?.let(parts::add)
                fileSizeLabel(result.size)?.let(parts::add)
                parts.joinToString(" - ").ifBlank { result.category.getSingularName() }
            }

            Category.TORRENT -> fileSizeLabel(result.size) ?: result.category.getSingularName()
        }
    }

    private fun fileSizeOf(item: PropertiableFile): Long? {
        return when (val value = item.getProperty(FilePropertyKey.FILE_SIZE)) {
            is Number -> value.toLong()
            is String -> value.toLongOrNull()
            else -> null
        }
    }

    private fun lengthLabel(value: Any?): String? {
        val seconds = when (value) {
            is Number -> value.toLong()
            is String -> value.toLongOrNull()
            else -> null
        } ?: return null
        return if (seconds > 0L) CommonUtils.seconds2time(seconds) else null
    }

    private fun qualityLabel(result: SearchResult): String? {
        val quality = when (val value = result.getProperty(FilePropertyKey.QUALITY)) {
            is Number -> value.toLong()
            is String -> value.toLongOrNull()
            else -> null
        } ?: return null
        val label = GuiUtils.toQualityString(quality) ?: return null
        val bitrate = when (val value = result.getProperty(FilePropertyKey.BITRATE)) {
            is Number -> value.toLong()
            is String -> value.toLongOrNull()
            else -> null
        }
        return if (bitrate != null && bitrate > 0L) {
            "$label ($bitrate)"
        } else {
            label
        }
    }

    private fun fileSizeLabel(size: Long): String? {
        return if (size > 0L) compactSize(size) else null
    }

    private fun compactSize(size: Long): String {
        return GuiUtils.formatUnitFromBytes(size)
    }

    private fun usesMimeAwareIcons(category: Category?): Boolean {
        return category == Category.DOCUMENT || category == Category.OTHER
    }

    private fun renderIcon(icon: Icon?): BufferedImage? {
        icon ?: return null
        val width = icon.iconWidth.coerceAtLeast(16)
        val height = icon.iconHeight.coerceAtLeast(16)
        val image = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        val graphics = image.createGraphics()
        try {
            icon.paintIcon(null, graphics, 0, 0)
        } finally {
            graphics.dispose()
        }
        return image
    }
}
