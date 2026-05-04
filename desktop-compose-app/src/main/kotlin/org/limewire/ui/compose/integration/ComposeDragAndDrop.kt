@file:OptIn(androidx.compose.ui.ExperimentalComposeUiApi::class)

package org.limewire.ui.compose.integration

import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTransferAction
import androidx.compose.ui.draganddrop.DragAndDropTransferData
import androidx.compose.ui.draganddrop.DragAndDropTransferable
import androidx.compose.ui.draganddrop.awtTransferable
import androidx.compose.ui.draganddrop.dragData
import androidx.compose.ui.geometry.Offset
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable
import java.awt.datatransfer.UnsupportedFlavorException
import java.io.File
import java.io.IOException
import java.io.Serializable
import java.util.Collections

sealed interface ComposeDropPayload {
    data class Files(
        val files: List<File>,
        val sourceSectionId: String? = null
    ) : ComposeDropPayload

    data class SearchResults(
        val tabId: Long,
        val resultKeys: List<String>
    ) : ComposeDropPayload
}

private data class LibraryFilesTransferPayload(
    val sourceSectionId: String?,
    val files: List<File>
) : Serializable

private data class SearchResultsTransferPayload(
    val tabId: Long,
    val resultKeys: List<String>
) : Serializable

private val libraryFilesFlavor = DataFlavor(
    LibraryFilesTransferPayload::class.java,
    "application/x-wireshare-library-files"
)

private val searchResultsFlavor = DataFlavor(
    SearchResultsTransferPayload::class.java,
    "application/x-wireshare-search-results"
)

private class WireShareTransferable(
    private val files: List<File> = emptyList(),
    private val libraryFilesPayload: LibraryFilesTransferPayload? = null,
    private val searchResultsPayload: SearchResultsTransferPayload? = null
) : Transferable {
    private val flavors: Array<DataFlavor> = buildList {
        if (files.isNotEmpty()) {
            add(DataFlavor.javaFileListFlavor)
        }
        if (libraryFilesPayload != null) {
            add(libraryFilesFlavor)
        }
        if (searchResultsPayload != null) {
            add(searchResultsFlavor)
        }
    }.toTypedArray()

    override fun getTransferDataFlavors(): Array<DataFlavor> = flavors.copyOf()

    override fun isDataFlavorSupported(flavor: DataFlavor): Boolean = flavors.contains(flavor)

    override fun getTransferData(flavor: DataFlavor): Any {
        return when {
            flavor == DataFlavor.javaFileListFlavor && files.isNotEmpty() -> Collections.unmodifiableList(files)
            flavor == libraryFilesFlavor && libraryFilesPayload != null -> libraryFilesPayload
            flavor == searchResultsFlavor && searchResultsPayload != null -> searchResultsPayload
            else -> throw UnsupportedFlavorException(flavor)
        }
    }
}

private fun transferDataFor(transferable: Transferable): DragAndDropTransferData {
    return DragAndDropTransferData(
        transferable = DragAndDropTransferable(transferable),
        supportedActions = listOf(DragAndDropTransferAction.Copy),
        dragDecorationOffset = Offset.Zero,
        onTransferCompleted = {}
    )
}

fun externalFilesTransferData(files: List<File>): DragAndDropTransferData? {
    val normalized = files.filter { it.exists() }.distinctBy { it.absolutePath }
    if (normalized.isEmpty()) {
        return null
    }
    return transferDataFor(WireShareTransferable(files = normalized))
}

fun libraryFilesTransferData(sourceSectionId: String?, files: List<File>): DragAndDropTransferData? {
    val normalized = files.filter { it.exists() }.distinctBy { it.absolutePath }
    if (normalized.isEmpty()) {
        return null
    }
    return transferDataFor(
        WireShareTransferable(
            files = normalized,
            libraryFilesPayload = LibraryFilesTransferPayload(
                sourceSectionId = sourceSectionId,
                files = normalized
            )
        )
    )
}

fun searchResultsTransferData(tabId: Long, resultKeys: List<String>): DragAndDropTransferData? {
    val normalized = resultKeys
        .map(String::trim)
        .filter(String::isNotEmpty)
        .distinct()
    if (normalized.isEmpty()) {
        return null
    }
    return transferDataFor(
        WireShareTransferable(
            searchResultsPayload = SearchResultsTransferPayload(
                tabId = tabId,
                resultKeys = normalized
            )
        )
    )
}

fun extractComposeDropPayload(event: DragAndDropEvent): ComposeDropPayload? {
    val transferable = runCatching { event.awtTransferable }.getOrNull() ?: return null

    val searchPayload = readSerializedPayload<SearchResultsTransferPayload>(transferable, searchResultsFlavor)
    if (searchPayload != null) {
        return ComposeDropPayload.SearchResults(
            tabId = searchPayload.tabId,
            resultKeys = searchPayload.resultKeys
        )
    }

    val files = when (val data = runCatching { event.dragData() }.getOrNull()) {
        is androidx.compose.ui.draganddrop.DragData.FilesList -> data.readFiles().map(::File)
        else -> emptyList()
    }.filter { it.exists() }.distinctBy { it.absolutePath }

    if (files.isEmpty()) {
        return null
    }

    val libraryPayload = readSerializedPayload<LibraryFilesTransferPayload>(transferable, libraryFilesFlavor)
    return ComposeDropPayload.Files(
        files = files,
        sourceSectionId = libraryPayload?.sourceSectionId
    )
}

fun acceptsLibraryDrop(event: DragAndDropEvent, targetSectionId: String): Boolean {
    return when (val payload = extractComposeDropPayload(event)) {
        is ComposeDropPayload.Files -> payload.files.isNotEmpty() && payload.sourceSectionId != targetSectionId
        else -> false
    }
}

fun acceptsSearchDownloadDrop(event: DragAndDropEvent): Boolean {
    return extractComposeDropPayload(event) is ComposeDropPayload.SearchResults
}

private fun <T> readSerializedPayload(
    transferable: Transferable,
    flavor: DataFlavor
): T? {
    if (!transferable.isDataFlavorSupported(flavor)) {
        return null
    }
    return try {
        @Suppress("UNCHECKED_CAST")
        transferable.getTransferData(flavor) as? T
    } catch (_: UnsupportedFlavorException) {
        null
    } catch (_: IOException) {
        null
    }
}
