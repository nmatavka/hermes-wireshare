package org.limewire.ui.compose

import org.limewire.core.api.FilePropertyKey
import org.limewire.core.api.library.LocalFileItem
import java.io.File

private const val M3U_HEADER = "#EXTM3U"
private const val M3U_INFO = "#EXTINF"

data class M3uImportResult(
    val files: List<File>,
    val importedCount: Int,
    val missingCount: Int,
    val skippedCount: Int
)

data class M3uExportResult(
    val writtenCount: Int,
    val skippedCount: Int
)

object ComposeM3uCodec {
    fun read(file: File): M3uImportResult {
        val files = linkedMapOf<String, File>()
        var missing = 0
        var skipped = 0
        val lines = file.readLines()
        val structured = lines.any { line ->
            val trimmed = line.trim()
            trimmed.startsWith(M3U_HEADER, ignoreCase = true) || trimmed.startsWith(M3U_INFO, ignoreCase = true)
        }

        fun resolveEntry(rawPath: String?) {
            val trimmed = rawPath?.trim().orEmpty()
            if (trimmed.isEmpty()) {
                skipped += 1
                return
            }
            val direct = File(trimmed)
            val resolved = try {
                val candidate = if (direct.isAbsolute) direct else File(file.parentFile, trimmed)
                candidate.canonicalFile
            } catch (_: Exception) {
                if (direct.isAbsolute) direct.absoluteFile else File(file.parentFile, trimmed).absoluteFile
            }
            when {
                !resolved.exists() -> missing += 1
                resolved.isDirectory -> skipped += 1
                else -> files.putIfAbsent(resolved.absolutePath, resolved)
            }
        }

        if (structured) {
            var index = 0
            while (index < lines.size) {
                val trimmed = lines[index].trim()
                when {
                    trimmed.isEmpty() -> Unit
                    trimmed.startsWith(M3U_HEADER, ignoreCase = true) -> Unit
                    trimmed.startsWith(M3U_INFO, ignoreCase = true) -> {
                        if (index + 1 >= lines.size) {
                            skipped += 1
                            break
                        }
                        resolveEntry(lines[index + 1])
                        index += 1
                    }
                    trimmed.startsWith("#") -> Unit
                    else -> resolveEntry(trimmed)
                }
                index += 1
            }
        } else {
            lines.forEach { line ->
                val trimmed = line.trim()
                if (trimmed.isNotEmpty() && !trimmed.startsWith("#")) {
                    resolveEntry(trimmed)
                }
            }
        }

        return M3uImportResult(
            files = files.values.toList(),
            importedCount = files.size,
            missingCount = missing,
            skippedCount = skipped
        )
    }

    fun write(file: File, items: List<LocalFileItem>): M3uExportResult {
        var written = 0
        var skipped = 0
        file.parentFile?.mkdirs()
        file.printWriter().use { writer ->
            writer.println(M3U_HEADER)
            items.forEach { item ->
                val localFile = item.file
                if (!localFile.isFile || item.isIncomplete) {
                    skipped += 1
                    return@forEach
                }
                val title = item.getPropertyString(FilePropertyKey.TITLE)
                    ?.takeIf { it.isNotBlank() }
                    ?: item.name
                val seconds = item.getPropertyString(FilePropertyKey.LENGTH)
                    ?.trim()
                    ?.takeIf { it.isNotBlank() }
                    ?: "-1"
                writer.println("$M3U_INFO:$seconds,$title")
                writer.println(localFile.canonicalPath)
                written += 1
            }
        }
        return M3uExportResult(writtenCount = written, skippedCount = skipped)
    }
}
