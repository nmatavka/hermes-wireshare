package org.limewire.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.draganddrop.dragAndDropSource
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.focusable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.InsertDriveFile
import androidx.compose.material.icons.automirrored.rounded.PlaylistAdd
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.automirrored.rounded.TextSnippet
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.Archive
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.Forum
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material.icons.rounded.Photo
import androidx.compose.material.icons.rounded.PictureAsPdf
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material.icons.rounded.Slideshow
import androidx.compose.material.icons.rounded.SwapVert
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material.icons.rounded.TableChart
import androidx.compose.material.icons.rounded.Upload
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.awt.SwingPanel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.KeyShortcut
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isCtrlPressed
import androidx.compose.ui.input.pointer.isMetaPressed
import androidx.compose.ui.input.pointer.isPrimaryPressed
import androidx.compose.ui.input.pointer.isSecondaryPressed
import androidx.compose.ui.input.pointer.isShiftPressed
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.rememberWindowState
import com.limegroup.gnutella.util.LimeWireUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import org.limewire.bittorrent.Torrent
import org.limewire.core.api.Category
import org.limewire.core.api.FilePropertyKey
import org.limewire.core.api.connection.ConnectionItem
import org.limewire.core.api.download.DownloadItem
import org.limewire.core.api.download.DownloadState
import org.limewire.core.api.library.LocalFileItem
import org.limewire.core.api.library.SharedFileList
import org.limewire.core.api.search.GroupedSearchResult
import org.limewire.core.api.search.SearchCategory
import org.limewire.core.api.search.SearchDetails
import org.limewire.core.api.search.SearchResult
import org.limewire.core.api.search.browse.BrowseStatus.BrowseState
import org.limewire.core.api.upload.UploadItem
import org.limewire.core.api.upload.UploadState
import org.limewire.friend.api.ChatState
import org.limewire.friend.api.FriendConnectionEvent
import org.limewire.player.api.PlayerState
import org.limewire.ui.compose.integration.ComposeDropPayload
import org.limewire.ui.compose.integration.FileIconPresentation
import org.limewire.ui.compose.integration.FileIconToken
import org.limewire.ui.compose.integration.ComposeRuntimeErrorReport
import org.limewire.ui.compose.integration.ComposeRuntimeErrorService
import org.limewire.ui.compose.integration.DesktopFilePicker
import org.limewire.ui.compose.integration.acceptsLibraryDrop
import org.limewire.ui.compose.integration.acceptsSearchDownloadDrop
import org.limewire.ui.compose.integration.externalFilesTransferData
import org.limewire.ui.compose.integration.extractComposeDropPayload
import org.limewire.ui.compose.integration.libraryFilesTransferData
import org.limewire.ui.compose.integration.searchResultsTransferData
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.io.File
import java.net.URI
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt
import org.limewire.util.FileUtils

private data class DesktopDensity(
    val windowWidth: Dp,
    val windowHeight: Dp,
    val compactVertical: Boolean,
    val compactHorizontal: Boolean,
    val shellPadding: Dp,
    val shellGap: Dp,
    val cardPadding: Dp,
    val sectionGap: Dp,
    val chipGap: Dp,
    val headerPadding: Dp,
    val overlayInset: Dp,
    val overlayGap: Dp,
    val statusBarPadding: Dp,
    val statusBarGap: Dp,
    val tabWidth: Dp,
    val controlHeight: Dp,
    val iconButtonSize: Dp,
    val railWidth: Dp,
    val railIconSize: Dp,
    val railItemCorner: Dp,
    val railItemPadding: Dp,
    val surfaceCorner: Dp,
    val tabCorner: Dp,
    val badgeHorizontalPadding: Dp,
    val badgeVerticalPadding: Dp,
    val dialogOuterMargin: Dp,
    val dialogHeaderPadding: Dp,
    val dialogBodyPadding: Dp,
    val dialogFooterPadding: Dp,
    val dialogPreferredHeight: Dp,
    val dialogHeightFraction: Float,
    val dialogWidthFraction: Float
)

private val LocalDesktopDensity = staticCompositionLocalOf {
    DesktopDensity(
        windowWidth = 1280.dp,
        windowHeight = 900.dp,
        compactVertical = false,
        compactHorizontal = false,
        shellPadding = 10.dp,
        shellGap = 8.dp,
        cardPadding = 12.dp,
        sectionGap = 10.dp,
        chipGap = 8.dp,
        headerPadding = 12.dp,
        overlayInset = 12.dp,
        overlayGap = 10.dp,
        statusBarPadding = 8.dp,
        statusBarGap = 8.dp,
        tabWidth = 208.dp,
        controlHeight = 36.dp,
        iconButtonSize = 36.dp,
        railWidth = 86.dp,
        railIconSize = 20.dp,
        railItemCorner = 14.dp,
        railItemPadding = 9.dp,
        surfaceCorner = 14.dp,
        tabCorner = 10.dp,
        badgeHorizontalPadding = 8.dp,
        badgeVerticalPadding = 4.dp,
        dialogOuterMargin = 24.dp,
        dialogHeaderPadding = 16.dp,
        dialogBodyPadding = 16.dp,
        dialogFooterPadding = 14.dp,
        dialogPreferredHeight = 720.dp,
        dialogHeightFraction = 0.9f,
        dialogWidthFraction = 0.92f
    )
}

@Composable
private fun ProvideDesktopDensity(content: @Composable () -> Unit) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val compactVertical = maxHeight <= 900.dp
        val compactHorizontal = maxWidth <= 1240.dp
        val density = DesktopDensity(
            windowWidth = maxWidth,
            windowHeight = maxHeight,
            compactVertical = compactVertical,
            compactHorizontal = compactHorizontal,
            shellPadding = if (compactVertical) 6.dp else 8.dp,
            shellGap = if (compactVertical) 5.dp else 6.dp,
            cardPadding = if (compactVertical) 8.dp else 10.dp,
            sectionGap = if (compactVertical) 6.dp else 8.dp,
            chipGap = if (compactVertical) 4.dp else 6.dp,
            headerPadding = if (compactVertical) 8.dp else 10.dp,
            overlayInset = if (compactVertical) 10.dp else 12.dp,
            overlayGap = if (compactVertical) 6.dp else 8.dp,
            statusBarPadding = if (compactVertical) 6.dp else 7.dp,
            statusBarGap = if (compactVertical) 4.dp else 6.dp,
            tabWidth = if (compactHorizontal) 176.dp else 192.dp,
            controlHeight = if (compactVertical) 34.dp else 36.dp,
            iconButtonSize = if (compactVertical) 32.dp else 34.dp,
            railWidth = if (compactHorizontal) 80.dp else 84.dp,
            railIconSize = if (compactVertical) 18.dp else 19.dp,
            railItemCorner = if (compactVertical) 12.dp else 14.dp,
            railItemPadding = if (compactVertical) 7.dp else 8.dp,
            surfaceCorner = if (compactVertical) 12.dp else 14.dp,
            tabCorner = if (compactVertical) 8.dp else 10.dp,
            badgeHorizontalPadding = if (compactVertical) 7.dp else 8.dp,
            badgeVerticalPadding = if (compactVertical) 3.dp else 4.dp,
            dialogOuterMargin = if (compactVertical) 16.dp else 24.dp,
            dialogHeaderPadding = if (compactVertical) 12.dp else 14.dp,
            dialogBodyPadding = if (compactVertical) 12.dp else 14.dp,
            dialogFooterPadding = if (compactVertical) 10.dp else 12.dp,
            dialogPreferredHeight = if (compactVertical) 600.dp else 700.dp,
            dialogHeightFraction = if (compactVertical) 0.92f else 0.88f,
            dialogWidthFraction = if (compactHorizontal) 0.96f else 0.9f
        )
        val typography = MaterialTheme.typography
        val typeScale = remember(typography, compactVertical) {
            DesktopTypeScale(
                screenTitle = typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                sectionTitle = typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                summary = typography.bodyMedium,
                body = typography.bodyMedium,
                meta = typography.bodySmall,
                label = typography.labelMedium,
                badgeLabel = typography.labelSmall,
                badgeValue = typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                hint = typography.bodySmall.copy(
                    fontSize = if (compactVertical) 12.5.sp else typography.bodySmall.fontSize
                )
            )
        }
        CompositionLocalProvider(
            LocalDesktopDensity provides density,
            LocalDesktopTypeScale provides typeScale
        ) {
            content()
        }
    }
}

private data class DesktopTypeScale(
    val screenTitle: TextStyle,
    val sectionTitle: TextStyle,
    val summary: TextStyle,
    val body: TextStyle,
    val meta: TextStyle,
    val label: TextStyle,
    val badgeLabel: TextStyle,
    val badgeValue: TextStyle,
    val hint: TextStyle
)

private val LocalDesktopTypeScale = staticCompositionLocalOf {
    DesktopTypeScale(
        screenTitle = TextStyle.Default,
        sectionTitle = TextStyle.Default,
        summary = TextStyle.Default,
        body = TextStyle.Default,
        meta = TextStyle.Default,
        label = TextStyle.Default,
        badgeLabel = TextStyle.Default,
        badgeValue = TextStyle.Default,
        hint = TextStyle.Default
    )
}

@Composable
private fun CompactOutlinedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit
) {
    val desktopDensity = LocalDesktopDensity.current
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.defaultMinSize(minHeight = desktopDensity.controlHeight),
        enabled = enabled,
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
        content = content
    )
}

@Composable
private fun CompactFilledTonalButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit
) {
    val desktopDensity = LocalDesktopDensity.current
    FilledTonalButton(
        onClick = onClick,
        modifier = modifier.defaultMinSize(minHeight = desktopDensity.controlHeight),
        enabled = enabled,
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
        content = content
    )
}

@Composable
private fun CompactButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit
) {
    val desktopDensity = LocalDesktopDensity.current
    Button(
        onClick = onClick,
        modifier = modifier.defaultMinSize(minHeight = desktopDensity.controlHeight),
        enabled = enabled,
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
        content = content
    )
}

@Composable
private fun CompactTextButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit
) {
    val desktopDensity = LocalDesktopDensity.current
    TextButton(
        onClick = onClick,
        modifier = modifier.defaultMinSize(minHeight = desktopDensity.controlHeight),
        enabled = enabled,
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
        content = content
    )
}

@Composable
private fun CompactIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    val desktopDensity = LocalDesktopDensity.current
    IconButton(
        onClick = onClick,
        modifier = modifier.size(desktopDensity.iconButtonSize),
        enabled = enabled,
        content = content
    )
}

private fun compactSummary(vararg parts: Pair<String, String?>): String {
    return parts.mapNotNull { (label, value) ->
        value
            ?.takeIf { it.isNotBlank() }
            ?.let { "$label: $it" }
    }.joinToString(" • ")
}

@Composable
private fun ResponsiveDesktopDialog(
    onDismissRequest: () -> Unit,
    title: @Composable () -> Unit,
    confirmButton: @Composable RowScope.() -> Unit,
    dismissButton: (@Composable RowScope.() -> Unit)? = null,
    preferredWidth: Dp = 760.dp,
    preferredHeight: Dp = LocalDesktopDensity.current.dialogPreferredHeight,
    allowDismissOnOutsideClick: Boolean = true,
    scrollBody: Boolean = true,
    body: @Composable ColumnScope.() -> Unit
) {
    val density = LocalDesktopDensity.current
    val dialogWidth = minOf(preferredWidth, density.windowWidth * density.dialogWidthFraction)
        .coerceAtLeast(340.dp)
    val dialogHeight = minOf(preferredHeight, density.windowHeight * density.dialogHeightFraction)
        .coerceAtLeast(240.dp)
    val bodyScrollState = rememberScrollState()

    Dialog(onDismissRequest = {
        if (allowDismissOnOutsideClick) {
            onDismissRequest()
        }
    }) {
        Surface(
            shape = RoundedCornerShape(density.surfaceCorner + 2.dp),
            tonalElevation = 6.dp,
            shadowElevation = 18.dp,
            modifier = Modifier
                .width(dialogWidth)
                .heightIn(max = dialogHeight)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.fillMaxWidth().padding(density.dialogHeaderPadding)) {
                    title()
                }
                HorizontalDivider()
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(
                                if (scrollBody) {
                                    Modifier.verticalScroll(bodyScrollState)
                                } else {
                                    Modifier
                                }
                            )
                            .padding(density.dialogBodyPadding),
                        verticalArrangement = Arrangement.spacedBy(density.sectionGap)
                    ) {
                        body()
                    }
                }
                HorizontalDivider()
                Row(
                    modifier = Modifier.fillMaxWidth().padding(density.dialogFooterPadding),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(density.chipGap)) {
                        dismissButton?.invoke(this)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(density.chipGap)) {
                        confirmButton()
                    }
                }
            }
        }
    }
}

@Composable
fun WireShareDesktopApp(controller: ComposeAppController, exitApplication: () -> Unit) {
    val initialWindowPlacement = controller.initialWindowPlacementPreferences
    val windowState = rememberWindowState(
        width = initialWindowPlacement.width.dp,
        height = initialWindowPlacement.height.dp
    )
    val localeEpoch = controller.localeEpoch
    val restoreEpoch = controller.restoreEpoch

    controller.bindExitHandler(exitApplication)

    Window(
        onCloseRequest = { controller.handleWindowCloseRequest() },
        title = "WireShare",
        state = windowState
    ) {
        controller.bindWindow(window)
        LaunchedEffect(Unit) {
            controller.markUiReady()
        }

        LaunchedEffect(restoreEpoch) {
            if (restoreEpoch > 0) {
                window.toFront()
                window.requestFocus()
            }
        }

        WireShareTheme(appearance = controller.appearance, localeEpoch = localeEpoch) {
            AppMenuBar(controller)
            ProvideDesktopDensity {
                Dialogs(controller)
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppShell(controller)
                }
            }
        }
    }

    if (controller.advancedToolsWindowOpen) {
        val advancedWindowState = rememberWindowState(width = 1180.dp, height = 780.dp)
        Window(
            onCloseRequest = { controller.closeAdvancedToolsWindow() },
            title = "Advanced Tools",
            state = advancedWindowState
        ) {
            LaunchedEffect(controller.advancedToolsWindowOpen, controller.selectedAdvancedToolsTab) {
                val selectedTab = controller.selectedAdvancedToolsTab
                val refreshInterval = controller.advancedToolsAutoRefreshIntervalMillis()
                    ?: return@LaunchedEffect
                while (controller.advancedToolsWindowOpen && controller.selectedAdvancedToolsTab == selectedTab) {
                    controller.refreshActiveAdvancedToolsTab()
                    delay(refreshInterval)
                }
            }
            WireShareTheme(appearance = controller.appearance, localeEpoch = localeEpoch) {
                ProvideDesktopDensity {
                    Surface(modifier = Modifier.fillMaxSize()) {
                        AdvancedToolsWindow(controller)
                    }
                }
            }
        }
    }
}

@Composable
fun FatalStartupErrorApp(
    report: ComposeRuntimeErrorReport,
    runtimeErrorService: ComposeRuntimeErrorService,
    filePicker: DesktopFilePicker,
    onQuit: () -> Unit
) {
    val windowState = rememberWindowState(width = 760.dp, height = 680.dp)
    Window(
        onCloseRequest = onQuit,
        title = report.title,
        state = windowState
    ) {
        var showDetails by remember(report.id) { mutableStateOf(false) }
        var statusMessage by remember(report.id) { mutableStateOf<String?>(null) }

        WireShareTheme {
            Surface(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(report.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
                    Text(report.message)
                    report.detail?.let { detail ->
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.45f),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text(
                                detail,
                                modifier = Modifier.fillMaxWidth().padding(14.dp),
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                "WireShare could not finish starting. You can save or copy the diagnostic details before quitting.",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                "Diagnostic details are available below if you need them.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = {
                            copyTextToClipboard(report.bugReport)
                            statusMessage = "Copied diagnostic details to the clipboard."
                        }) {
                            Text("Copy Details")
                        }
                        OutlinedButton(onClick = {
                            val target = filePicker.chooseSaveFile(
                                parent = window,
                                title = "Save Diagnostic Report",
                                suggestedName = "wireshare-startup-error.txt"
                            ) ?: return@OutlinedButton
                            val output = if (target.extension.equals("txt", ignoreCase = true)) {
                                target
                            } else {
                                File(target.parentFile, "${target.name}.txt")
                            }
                            runCatching {
                                runtimeErrorService.saveDiagnosticReport(output, report)
                            }.onSuccess {
                                statusMessage = "Saved diagnostic report to ${output.name}."
                            }.onFailure { failure ->
                                statusMessage = failure.message ?: "WireShare could not save the diagnostic report."
                            }
                        }) {
                            Text("Save Diagnostic Report…")
                        }
                        FilledTonalButton(onClick = onQuit) {
                            Text("Quit")
                        }
                    }
                    TextButton(onClick = { showDetails = !showDetails }) {
                        Text(if (showDetails) "Hide Details" else "Show Details")
                    }
                    if (showDetails) {
                        Surface(
                            modifier = Modifier.fillMaxWidth().weight(1f),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize().padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("Diagnostic Details", fontWeight = FontWeight.SemiBold)
                                Text(
                                    report.bugReport,
                                    modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                    statusMessage?.let { message ->
                        Text(
                            message,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AlreadyRunningApp(
    onContinue: () -> Unit,
    onQuit: () -> Unit
) {
    val windowState = rememberWindowState(width = 520.dp, height = 320.dp)
    Window(
        onCloseRequest = onQuit,
        title = "WireShare is already running",
        state = windowState
    ) {
        WireShareTheme {
            Surface(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        Text("WireShare is already running", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
                    }
                    Text(
                        "Another instance of WireShare appears to be running. Completely shut down the other instance before continuing. If the problem keeps happening, restart the computer and try again."
                    )
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(
                            "Choose Continue Anyway only if you know the other instance is stale or unreachable.",
                            modifier = Modifier.fillMaxWidth().padding(14.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onQuit) {
                            Text("Quit")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        FilledTonalButton(onClick = onContinue) {
                            Text("Continue Anyway")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun FrameWindowScope.AppMenuBar(controller: ComposeAppController) {
    val _downloads = controller.downloadsEpoch
    val _recentDownloads = controller.recentDownloadsEpoch
    val recentDownloads = controller.recentDownloads()
    val stalledDownloadsAvailable = controller.hasStalledDownloads()
    val delayedExitState = controller.delayedExitState
    val friendSignedIn = controller.friendConnectionState == FriendConnectionEvent.Type.CONNECTED
    val supportsPresenceModes = controller.supportsFriendPresenceModes()
    val friendCollections = controller.availableFriendCollections()

    MenuBar {
        Menu("File") {
            Item("Open Torrent...", onClick = { controller.chooseTorrentFiles() }, shortcut = menuShortcut(Key.O))
            Item("Open Link...", onClick = { controller.showOpenLinkDialog() }, shortcut = menuShortcut(Key.L))
            Menu("Recent Downloads") {
                if (recentDownloads.isEmpty()) {
                    Item("(empty)", enabled = false, onClick = {})
                } else {
                    recentDownloads.forEach { file ->
                        val jumpTargets = controller.recentDownloadJumpTargets(file)
                        if (jumpTargets.isEmpty()) {
                            Item(file.name, onClick = { controller.openRecentDownload(file) })
                        } else {
                            Menu(file.name) {
                                Item("Open", onClick = { controller.openRecentDownload(file) })
                                Separator()
                                jumpTargets.forEach { target ->
                                    Item(
                                        showInTargetLabel(target),
                                        onClick = { controller.showRecentDownloadInTarget(file, target) }
                                    )
                                }
                            }
                        }
                    }
                    Separator()
                    Item("Clear List", onClick = { controller.clearRecentDownloads() })
                }
            }
            Item("Add to Library...", onClick = { controller.chooseLibraryFiles() })
            Separator()
            Item(
                "Fix Stalled Downloads",
                enabled = stalledDownloadsAvailable,
                onClick = { controller.fixStalledDownloads() }
            )
            Item(
                if (delayedExitState.pending) "Cancel Exit After Transfers" else "Exit After Transfers",
                onClick = {
                    if (delayedExitState.pending) {
                        controller.cancelExitAfterTransfers()
                    } else {
                        controller.startExitAfterTransfers()
                    }
                }
            )
            Separator()
            Item("Quit", onClick = { controller.requestExit() }, shortcut = menuShortcut(Key.Q))
        }
        Menu("View") {
            Item("My Files", onClick = { controller.selectLibrary() })
            Item("Transfers", onClick = { controller.selectTransfers() })
            Item("Friends", onClick = { controller.selectFriends() })
            Item("Player", onClick = { controller.selectPlayer() })
            Separator()
            Item("Show Downloads Tray", onClick = { controller.selectTransfers(TransferTrayMode.DOWNLOADS) })
            Item("Show Uploads Tray", onClick = { controller.selectTransfers(TransferTrayMode.UPLOADS) })
            Item(
                if (controller.trayExpanded) "Hide Transfer Tray" else "Show Transfer Tray",
                onClick = { controller.toggleTray() }
            )
            Separator()
            Item("Focus Search", onClick = { controller.requestGlobalSearchFocus() }, shortcut = menuShortcut(Key.F))
            Item("Change Language...", onClick = { controller.showLanguageDialog() })
        }
        Menu("Friends") {
            Item(
                "Browse Friends' Files",
                enabled = controller.canBrowseFriendsFiles(),
                onClick = { controller.browseAllFriends() }
            )
            Item("Collections", onClick = { controller.openCollections() })
            Menu("Share Collection") {
                if (!friendSignedIn) {
                    Item("Sign in to share collections", enabled = false, onClick = {})
                } else {
                    if (friendCollections.isEmpty()) {
                        Item("(no collections)", enabled = false, onClick = {})
                    } else {
                        friendCollections.forEach { collection ->
                            Item(
                                collection.collectionName,
                                onClick = { controller.selectLibrarySection("shared:${collection.id}") }
                            )
                        }
                    }
                    Separator()
                    Item("Share New Collection…", onClick = { controller.showCreateSharedListDialog() })
                }
            }
            Item(
                "Add Friend...",
                enabled = controller.supportsFriendAddRemove(),
                onClick = { controller.openAddFriendDialog() }
            )
            if (friendSignedIn && supportsPresenceModes) {
                Separator()
                Item(
                    "Available",
                    enabled = controller.friendDoNotDisturb,
                    onClick = { controller.updateFriendDoNotDisturb(false) }
                )
                Item(
                    "Do Not Disturb",
                    enabled = !controller.friendDoNotDisturb,
                    onClick = { controller.updateFriendDoNotDisturb(true) }
                )
            }
            Separator()
            if (friendSignedIn) {
                Item("Sign Out", onClick = { controller.logoutFriends() })
            } else {
                Item("Sign In...", onClick = { controller.showFriendLoginDialog() })
            }
        }
        Menu("Tools") {
            Item(
                "Advanced Search...",
                onClick = { controller.showAdvancedSearchDialog() },
                shortcut = menuShortcut(Key.F, shift = true)
            )
            Item("Advanced Tools...", onClick = { controller.showAdvancedToolsWindow() })
            Menu("What's New Search") {
                controller.whatsNewCategories().forEach { category ->
                    Item(
                        whatsNewMenuLabel(category),
                        onClick = { controller.submitWhatsNewSearch(category) }
                    )
                }
            }
            Separator()
            Item("Preferences...", onClick = { controller.showPreferences() }, shortcut = menuShortcut(Key.Comma))
        }
        Menu("Help") {
            Item("About WireShare", onClick = { controller.showAbout() })
        }
    }
}

@Composable
private fun Dialogs(controller: ComposeAppController) {
    if (controller.aboutDialogOpen) {
        AlertDialog(
            onDismissRequest = { controller.aboutDialogOpen = false },
            confirmButton = {
                TextButton(onClick = { controller.aboutDialogOpen = false }) {
                    Text("Close")
                }
            },
            title = { Text("About WireShare") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("WireShare ${LimeWireUtils.getLimeWireVersion()}.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        MetricBadge("Platform", "Desktop")
                        MetricBadge("Opens To", "My Files")
                        MetricBadge("Searches", "Own tab")
                        MetricBadge("Video", "Default player")
                    }
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("Runtime", fontWeight = FontWeight.SemiBold)
                            Text("Java ${System.getProperty("java.version")}", style = MaterialTheme.typography.bodySmall)
                            Text(
                                "${System.getProperty("os.name")} ${System.getProperty("os.version")} • ${System.getProperty("os.arch")}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                    Text(
                        "WireShare opens to My Files, and each search gets its own tab.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        )
    }

    if (controller.preferencesDialogOpen) {
        PreferencesDialog(controller)
    }

    if (controller.languageDialogOpen) {
        LanguageDialog(controller)
    }

    if (controller.advancedSearchDialogOpen) {
        AdvancedSearchDialog(controller)
    }

    if (controller.setupWizardOpen) {
        SetupWizardDialog(controller)
    }

    controller.startupSaveDirectoryIssue?.let { issue ->
        StartupSaveDirectoryDialog(controller, issue)
    }

    controller.startupFileAssociationPrompt?.let { prompt ->
        StartupAssociationDialog(controller, prompt)
    }

    if (controller.openLinkDialogOpen) {
        OpenLinkDialog(controller)
    }

    if (controller.friendLoginDialogOpen) {
        FriendLoginDialog(controller)
    }

    if (controller.addFriendDialogOpen) {
        AddFriendDialog(controller)
    }

    controller.textEntryDialog?.let { dialog ->
        TextEntryDialog(dialog) { controller.textEntryDialog = null }
    }

    controller.confirmationDialog?.let { dialog ->
        ConfirmationDialog(dialog) { controller.confirmationDialog = null }
    }

    controller.libraryDeletionChoiceDialog?.let { dialog ->
        LibraryDeletionChoiceDialog(controller, dialog)
    }

    controller.libraryFolderImportDialog?.let { dialog ->
        LibraryFolderImportDialog(controller, dialog)
    }

    controller.libraryKnownTypesDialog?.let { dialog ->
        LibraryKnownTypesDialog(controller, dialog)
    }

    controller.documentSharingWarningDialog?.let { dialog ->
        DocumentSharingWarningDialog(controller, dialog)
    }

    controller.libraryFileInfoDialog?.let { dialog ->
        LibraryFileInfoDialog(controller, dialog)
    }

    controller.searchFileInfoDialog?.let { dialog ->
        SearchFileInfoDialog(controller, dialog)
    }

    controller.downloadFileInfoDialog?.let { dialog ->
        DownloadFileInfoDialog(controller, dialog)
    }

    controller.uploadFileInfoDialog?.let { dialog ->
        UploadFileInfoDialog(controller, dialog)
    }

    controller.browseFailureDialog?.let { dialog ->
        BrowseFailureDialog(controller, dialog)
    }

    controller.runtimeErrorDialogs.forEach { report ->
        RuntimeErrorDialog(controller, report)
    }

    controller.messageDialog?.let { dialog ->
        var checkboxChecked by remember(dialog.title, dialog.message, dialog.checkboxLabel, dialog.checkboxInitialChecked) {
            mutableStateOf(dialog.checkboxInitialChecked)
        }
        AlertDialog(
            onDismissRequest = { controller.closeMessageDialog(checkboxChecked) },
            confirmButton = {
                TextButton(onClick = { controller.closeMessageDialog(checkboxChecked) }) {
                    Text(dialog.confirmLabel)
                }
            },
            title = {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (dialog.severity == MessageDialogSeverity.ERROR) {
                        Icon(Icons.Rounded.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    } else {
                        Icon(Icons.Rounded.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }
                    Text(dialog.title)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(dialog.message)
                    dialog.checkboxLabel?.let { label ->
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable { checkboxChecked = !checkboxChecked },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Checkbox(checked = checkboxChecked, onCheckedChange = { checkboxChecked = it })
                            Text(label, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        )
    }

    when (val prompt = controller.promptBroker.activePrompt) {
        is BlockingConfirmationPrompt -> {
            AlertDialog(
                onDismissRequest = { controller.promptBroker.resolveActivePrompt(false) },
                confirmButton = {
                    Button(onClick = { controller.promptBroker.resolveActivePrompt(true) }) {
                        Text("Continue")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { controller.promptBroker.resolveActivePrompt(false) }) {
                        Text("Cancel")
                    }
                },
                title = { Text(prompt.title) },
                text = { Text(prompt.message) }
            )
        }

        is BlockingTorrentSelectionPrompt -> {
            BlockingTorrentSelectionDialog(controller, prompt)
        }

        null -> Unit
    }
}

@Composable
private fun BlockingTorrentSelectionDialog(
    controller: ComposeAppController,
    prompt: BlockingTorrentSelectionPrompt
) {
    val desktopDensity = LocalDesktopDensity.current

    ResponsiveDesktopDialog(
        onDismissRequest = { controller.promptBroker.resolveActivePrompt(false) },
        confirmButton = {
            Button(
                onClick = { controller.promptBroker.resolveActivePrompt(true) },
                enabled = prompt.selectedCount > 0
            ) {
                Text("Start Download")
            }
        },
        dismissButton = {
            TextButton(onClick = { controller.promptBroker.resolveActivePrompt(false) }) {
                Text("Cancel")
            }
        },
        title = { Text(prompt.title) },
        preferredWidth = 920.dp,
        preferredHeight = if (desktopDensity.compactVertical) 620.dp else 740.dp
    ) {
        Text(
            prompt.message,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(desktopDensity.cardPadding),
                verticalArrangement = Arrangement.spacedBy(desktopDensity.chipGap)
            ) {
                Text(prompt.torrentName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    "${prompt.entries.size} file(s) · ${prompt.selectedCount} selected",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(desktopDensity.chipGap),
            verticalArrangement = Arrangement.spacedBy(desktopDensity.chipGap)
        ) {
            OutlinedButton(onClick = { prompt.selectAll() }) {
                Text("Select All")
            }
            OutlinedButton(onClick = { prompt.selectNone() }) {
                Text("Select None")
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(desktopDensity.chipGap)
        ) {
            Checkbox(
                checked = prompt.alwaysAskBeforeStarting,
                onCheckedChange = { checked -> prompt.alwaysAskBeforeStarting = checked }
            )
            Text(
                "Always ask before starting torrent",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        TorrentFileEntriesTable(
            entries = prompt.entries,
            editable = true,
            onPriorityChange = { index, priority ->
                prompt.setPriority(index, priority)
            }
        )
    }
}

@Composable
private fun AdvancedToolsWindow(controller: ComposeAppController) {
    val desktopDensity = LocalDesktopDensity.current
    val desktopType = LocalDesktopTypeScale.current
    Column(
        modifier = Modifier.fillMaxSize().padding(desktopDensity.cardPadding),
        verticalArrangement = Arrangement.spacedBy(desktopDensity.sectionGap)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Advanced Tools", style = desktopType.screenTitle)
            Text(
                "Inspect connections, incoming searches, Mojito activity, and diagnostic output.",
                style = desktopType.summary,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        PrimaryTabRow(selectedTabIndex = controller.selectedAdvancedToolsTab.ordinal) {
            AdvancedToolsTab.entries.forEach { tab ->
                Tab(
                    selected = controller.selectedAdvancedToolsTab == tab,
                    onClick = { controller.selectAdvancedToolsTab(tab) },
                    text = { Text(friendlyName(tab.name)) }
                )
            }
        }
        when (controller.selectedAdvancedToolsTab) {
            AdvancedToolsTab.CONNECTIONS -> AdvancedToolsConnectionsTab(controller, Modifier.weight(1f))
            AdvancedToolsTab.CONSOLE -> AdvancedToolsConsoleTab(controller, Modifier.weight(1f))
            AdvancedToolsTab.MOJITO -> AdvancedToolsMojitoTab(controller, Modifier.weight(1f))
        }
    }
}

@Composable
private fun AdvancedToolsConnectionsTab(
    controller: ComposeAppController,
    modifier: Modifier = Modifier
) {
    val desktopDensity = LocalDesktopDensity.current
    val desktopType = LocalDesktopTypeScale.current
    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(desktopDensity.sectionGap)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(desktopDensity.cardPadding),
                verticalArrangement = Arrangement.spacedBy(desktopDensity.sectionGap)
            ) {
                Text(
                    compactSummary(
                        "Connection" to controller.advancedConnectionStrengthLabel(),
                        "Node" to controller.advancedNodeRoleBadgeLabel(),
                        "Peers" to controller.advancedConnectedPeersText(),
                        "DHT" to if (controller.advancedToolsDhtRunning) {
                            "${controller.advancedToolsDhtName} running"
                        } else {
                            "${controller.advancedToolsDhtName} disabled"
                        }
                    ),
                    style = desktopType.summary,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(controller.advancedNodeRoleText(), style = desktopType.sectionTitle)
                Text(controller.advancedFirewallStatusText(), color = MaterialTheme.colorScheme.onSurfaceVariant, style = desktopType.summary)
                controller.advancedFirewallReasonText()?.let { reason ->
                    Text(reason, style = desktopType.meta, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Checkbox(
                        checked = controller.resolveConnectionHostnames,
                        onCheckedChange = { checked -> controller.updateResolveConnectionHostnamesPreference(checked) }
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Resolve hostnames of connected peers")
                        Text(
                            "Turn this off to keep the connections view on raw IPs only.",
                            style = desktopType.meta,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Card {
            Column(
                modifier = Modifier.fillMaxWidth().padding(desktopDensity.cardPadding),
                verticalArrangement = Arrangement.spacedBy(desktopDensity.sectionGap)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("Connections", style = desktopType.sectionTitle)
                        Text(
                            "View live peer connections, sort them, or remove individual peers.",
                            style = desktopType.meta,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    ConnectionHeaderMenu(controller)
                }
                if (controller.advancedConnections.isEmpty()) {
                    Text(
                        "No live Gnutella connections are available right now.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    val visibleColumns = ConnectionColumn.entries.filter { it in controller.visibleConnectionColumns }
                    val rows = controller.sortedAdvancedConnections()
                    val scrollState = rememberScrollState()
                    Box(modifier = Modifier.fillMaxWidth().horizontalScroll(scrollState)) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                visibleColumns.forEach { column ->
                                    TextButton(
                                        onClick = { controller.setConnectionSort(column) },
                                        modifier = Modifier.width(connectionColumnWidth(column))
                                    ) {
                                        Text(
                                            connectionColumnLabel(column) + if (controller.connectionSortColumn == column) {
                                                if (controller.connectionSortDescending) " ↓" else " ↑"
                                            } else {
                                                ""
                                            },
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                                Text("Actions", modifier = Modifier.width(180.dp), fontWeight = FontWeight.SemiBold)
                            }
                            rows.forEach { item ->
                                val selected = controller.selectedConnectionKey == connectionIdentity(item)
                                Surface(
                                    tonalElevation = if (selected) 3.dp else 0.dp,
                                    color = if (selected) {
                                        MaterialTheme.colorScheme.secondaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.surface
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth().clickable { controller.selectConnectionRow(item) }
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        visibleColumns.forEach { column ->
                                            Text(
                                                connectionColumnValue(item, column),
                                                modifier = Modifier.width(connectionColumnWidth(column)),
                                                style = MaterialTheme.typography.bodySmall,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                        Row(
                                            modifier = Modifier.width(180.dp),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            TextButton(
                                                onClick = { controller.browseConnection(item) },
                                                enabled = item.isConnected && item.friendPresence != null
                                            ) {
                                                Text("Browse Files")
                                            }
                                            TextButton(onClick = {
                                                controller.selectConnectionRow(item)
                                                controller.removeSelectedConnection()
                                            }) {
                                                Text("Remove")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Card {
            Column(
                modifier = Modifier.fillMaxWidth().padding(desktopDensity.cardPadding),
                verticalArrangement = Arrangement.spacedBy(desktopDensity.sectionGap)
            ) {
                Text("Add Connection", style = desktopType.sectionTitle)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = controller.addConnectionHost,
                        onValueChange = {
                            controller.addConnectionHost = it
                            controller.addConnectionError = null
                        },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        label = { Text("Host") }
                    )
                    OutlinedTextField(
                        value = controller.addConnectionPort,
                        onValueChange = {
                            controller.addConnectionPort = it.filter(Char::isDigit)
                            controller.addConnectionError = null
                        },
                        modifier = Modifier.width(140.dp),
                        singleLine = true,
                        label = { Text("Port") }
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Checkbox(
                            checked = controller.addConnectionUseTls,
                            onCheckedChange = { checked -> controller.addConnectionUseTls = checked }
                        )
                        Text("Use TLS")
                    }
                    CompactFilledTonalButton(onClick = { controller.submitAdvancedConnection() }) {
                        Text("Add Connection")
                    }
                }
                controller.addConnectionError?.let { error ->
                    Text(error, color = MaterialTheme.colorScheme.error, style = desktopType.meta)
                }
            }
        }

        Card {
            Column(
                modifier = Modifier.fillMaxWidth().padding(desktopDensity.cardPadding),
                verticalArrangement = Arrangement.spacedBy(desktopDensity.sectionGap)
            ) {
                Text("Incoming Searches", style = desktopType.sectionTitle)
                if (controller.incomingSearchPhrases.isEmpty()) {
                    Text(
                        "Searches from other peers will appear here.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = desktopType.summary
                    )
                } else {
                    controller.incomingSearchPhrases.forEach { query ->
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().clickable { controller.submitIncomingSearch(query) }
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(Icons.Rounded.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(query, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                    Text(
                                        "Run this search in the current category.",
                                        style = desktopType.meta,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                CompactTextButton(onClick = { controller.submitIncomingSearch(query) }) {
                                    Text("Search")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ConnectionHeaderMenu(controller: ComposeAppController) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(Icons.Rounded.MoreVert, contentDescription = "Connection options")
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(text = { Text("Sort by") }, enabled = false, onClick = {})
            ConnectionColumn.entries.forEach { column ->
                DropdownMenuItem(
                    text = { Text(connectionColumnLabel(column)) },
                    onClick = {
                        controller.setConnectionSort(column)
                        expanded = false
                    }
                )
            }
            HorizontalDivider()
            DropdownMenuItem(
                text = {
                    Text(if (controller.connectionSortDescending) "Use Ascending Order" else "Use Descending Order")
                },
                onClick = {
                    controller.setConnectionSort(controller.connectionSortColumn)
                    expanded = false
                }
            )
            HorizontalDivider()
            DropdownMenuItem(text = { Text("Visible Columns") }, enabled = false, onClick = {})
            ConnectionColumn.entries.forEach { column ->
                DropdownMenuItem(
                    text = {
                        Text("${if (column in controller.visibleConnectionColumns) "Hide" else "Show"} ${connectionColumnLabel(column)}")
                    },
                    onClick = { controller.toggleConnectionColumn(column) }
                )
            }
        }
    }
}

@Composable
private fun AdvancedToolsConsoleTab(
    controller: ComposeAppController,
    modifier: Modifier = Modifier
) {
    if (!controller.consoleAvailable) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Card {
                Column(
                    modifier = Modifier.widthIn(max = 520.dp).padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Rounded.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text("Console not available", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(
                        "Live log capture is not available in this runtime.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        return
    }

    Column(modifier = modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AdvancedToolsChoiceButton(
                label = controller.selectedConsoleLogger,
                options = controller.consoleLoggerNames,
                onOptionSelected = { controller.selectConsoleLogger(it) },
                modifier = Modifier.weight(1f)
            )
            AdvancedToolsChoiceButton(
                label = controller.selectedConsoleLevel,
                options = controller.advancedConsoleLevelOptions(),
                onOptionSelected = { controller.selectedConsoleLevel = it },
                modifier = Modifier.width(150.dp)
            )
            OutlinedTextField(
                value = controller.consoleDelaySeconds,
                onValueChange = { controller.consoleDelaySeconds = it.filter(Char::isDigit).take(3) },
                label = { Text("Delay") },
                singleLine = true,
                modifier = Modifier.width(120.dp)
            )
            FilledTonalButton(onClick = { controller.applyAdvancedConsoleSettings() }) {
                Text("Apply")
            }
            OutlinedButton(onClick = { controller.clearAdvancedConsole() }) {
                Text("Clear")
            }
            OutlinedButton(onClick = { controller.saveAdvancedConsole() }) {
                Text("Save")
            }
        }
        Surface(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Console", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    "Logger level and delay changes take effect immediately.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Surface(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    color = Color(0xFF10141A),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(12.dp).verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            controller.consoleText.ifBlank { "Waiting for log output…" },
                            color = Color(0xFFD7E3F4),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AdvancedToolsMojitoTab(
    controller: ComposeAppController,
    modifier: Modifier = Modifier
) {
    when {
        !controller.advancedMojitoVisualizerAvailable -> {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Card {
                    Column(
                        modifier = Modifier.widthIn(max = 540.dp).padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Rounded.CloudOff, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text("Mojito Arcs View not available", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text(
                            "The Mojito visualizer is not available in this runtime. Only the DHT summary can be shown.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        !controller.advancedToolsDhtRunning -> {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Card {
                    Column(
                        modifier = Modifier.widthIn(max = 560.dp).padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Rounded.Sync, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text("Mojito is not running", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text(
                            "The arcs view will appear automatically when Mojito starts.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        controller.advancedMojitoVisualizerComponent == null -> {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Card {
                    Column(
                        modifier = Modifier.widthIn(max = 520.dp).padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Rounded.Sync, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text("Preparing Mojito Arcs", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text(
                            "WireShare is attaching the live DHT visualizer now.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        else -> {
            val component = controller.advancedMojitoVisualizerComponent ?: return
            Column(modifier = modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            controller.advancedMojitoVisualizerTitle.ifBlank { "Mojito Arcs Visualizer" },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            "${controller.advancedToolsDhtName} is running. The view below shows live Mojito traffic.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Surface(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    SwingPanel(
                        factory = {
                            component.requestFocusInWindow()
                            component
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
private fun AdvancedToolsChoiceButton(
    label: String,
    options: List<String>,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
            Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun SetupWizardDialog(controller: ComposeAppController) {
    val draft = controller.setupWizardDraft
    val page = controller.setupWizardPage
    val desktopDensity = LocalDesktopDensity.current
    val showAssociationsPage = remember { controller.showSetupAssociationsPage() }
    val pageNumber = when (page) {
        SetupWizardPage.ASSOCIATIONS -> 1
        SetupWizardPage.SHARING -> if (showAssociationsPage) 2 else 1
        SetupWizardPage.SECURITY -> if (showAssociationsPage) 3 else 2
    }
    val pageCount = if (showAssociationsPage) 3 else 2
    val canGoBack = when (page) {
        SetupWizardPage.ASSOCIATIONS -> false
        SetupWizardPage.SHARING -> showAssociationsPage
        SetupWizardPage.SECURITY -> true
    }

    ResponsiveDesktopDialog(
        onDismissRequest = {},
        confirmButton = {
            Button(
                onClick = {
                    if (page == SetupWizardPage.SECURITY) {
                        controller.finishSetupWizard()
                    } else {
                        controller.nextSetupWizardPage()
                    }
                }
            ) {
                Text(if (page == SetupWizardPage.SECURITY) "Finish" else "Next")
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (canGoBack) {
                    TextButton(onClick = { controller.previousSetupWizardPage() }) {
                        Text("Back")
                    }
                }
                TextButton(onClick = { controller.quitFromStartupBlocker() }) {
                    Text("Quit")
                }
            }
        },
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Set Up WireShare")
                Text(
                    "Step $pageNumber of $pageCount",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        preferredWidth = 760.dp,
        preferredHeight = if (desktopDensity.compactVertical) 540.dp else 620.dp,
        allowDismissOnOutsideClick = false
    ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(desktopDensity.sectionGap)
            ) {
                controller.setupWizardError?.let { error ->
                    PreferenceInlineError(error)
                }
                when (page) {
                    SetupWizardPage.ASSOCIATIONS -> {
                        Text("Choose file handling, startup behavior, and search safety.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        PreferenceToggle("Handle magnet links", draft.library.handleMagnets) {
                            controller.applySetupWizardDraft {
                                it.copy(library = it.library.copy(handleMagnets = !it.library.handleMagnets))
                            }
                        }
                        PreferenceToggle("Handle torrent files", draft.library.handleTorrents) {
                            controller.applySetupWizardDraft {
                                it.copy(library = it.library.copy(handleTorrents = !it.library.handleTorrents))
                            }
                        }
                        PreferenceToggle(
                            label = "Run at login",
                            value = draft.system.runOnStartup,
                            enabled = draft.system.runOnStartupSupported,
                            supportingText = if (draft.system.runOnStartupSupported) {
                                "Start WireShare when you sign in."
                            } else {
                                "Run-at-login is not available on this platform."
                            }
                        ) {
                            controller.applySetupWizardDraft {
                                it.copy(system = it.system.copy(runOnStartup = !it.system.runOnStartup))
                            }
                        }
                        PreferenceToggle("Filter adult content from search", draft.search.filterAdultContent) {
                            controller.applySetupWizardDraft {
                                it.copy(search = it.search.copy(filterAdultContent = !it.search.filterAdultContent))
                            }
                        }
                    }

                    SetupWizardPage.SHARING -> {
                        val continuationMessage = controller.setupWizardPublicSharingContinuationMessage()
                        Text("Choose what should be shared automatically.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(controller.setupWizardPublicSharingHeadline(), fontWeight = FontWeight.SemiBold)
                                continuationMessage?.let { message ->
                                    Text(
                                        message,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                                Text(
                                    "Later, open My Files > Public Shared to review what is public.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                        PreferenceToggle("Add downloaded peer-to-peer files to Public Shared", draft.library.shareDownloadedFiles) {
                            controller.applySetupWizardDraft {
                                it.copy(library = it.library.copy(shareDownloadedFiles = !it.library.shareDownloadedFiles))
                            }
                        }
                        PreferenceToggle("Share partially downloaded files", draft.library.allowPartialSharing) {
                            controller.applySetupWizardDraft {
                                it.copy(library = it.library.copy(allowPartialSharing = !it.library.allowPartialSharing))
                            }
                        }
                        PreferenceToggle("Allow document sharing on the public network", draft.library.allowDocumentSharing) {
                            controller.applySetupWizardDraft {
                                it.copy(library = it.library.copy(allowDocumentSharing = !it.library.allowDocumentSharing))
                            }
                        }
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text("Before you share publicly", fontWeight = FontWeight.SemiBold)
                                Text(
                                    "Only share files publicly when you expect to distribute them widely. Be careful with programs, documents, and partial downloads.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    "Files shared automatically, and anything already in Public Shared, stay public until you move or unshare them.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    SetupWizardPage.SECURITY -> {
                        Text(
                            "Choose how strictly WireShare filters content and network access. Some changes may require a restart.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        PreferenceDropdownField(
                            label = "Security level",
                            value = when (draft.system.securityLevel) {
                                SecurityLevelOption.NONE -> "Off"
                                SecurityLevelOption.LIGHT -> "Light"
                                SecurityLevelOption.LIGHT_JAPAN_BLOCK -> "Light + Japan block"
                                SecurityLevelOption.STRONG -> "Strong"
                                SecurityLevelOption.STRONG_JAPAN_BLOCK -> "Strong + Japan block"
                            },
                            options = listOf(
                                SecurityLevelOption.NONE to "Off",
                                SecurityLevelOption.LIGHT to "Light",
                                SecurityLevelOption.LIGHT_JAPAN_BLOCK to "Light + Japan block",
                                SecurityLevelOption.STRONG to "Strong",
                                SecurityLevelOption.STRONG_JAPAN_BLOCK to "Strong + Japan block"
                            ),
                            onSelected = { level ->
                                controller.applySetupWizardDraft {
                                    it.copy(system = it.system.copy(securityLevel = level))
                                }
                            }
                        )
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text("Restart note", fontWeight = FontWeight.SemiBold)
                                Text(
                                    "Some port, file-association, and security settings may require a restart before they take effect.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
    }
}

@Composable
private fun StartupSaveDirectoryDialog(controller: ComposeAppController, issue: String) {
    AlertDialog(
        onDismissRequest = {},
        confirmButton = {
            Button(onClick = { controller.chooseStartupSaveDirectory() }) {
                Text("Choose Folder…")
            }
        },
        dismissButton = {
            TextButton(onClick = { controller.quitFromStartupBlocker() }) {
                Text("Quit")
            }
        },
        title = { Text("Choose a Download Folder") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(issue)
                Text(
                    "Choose a valid save folder before continuing.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    )
}

@Composable
private fun StartupAssociationDialog(controller: ComposeAppController, prompt: FileAssociationPromptState) {
    var warnOnChange by remember(prompt.message, prompt.warnOnChange) { mutableStateOf(prompt.warnOnChange) }

    AlertDialog(
        onDismissRequest = {},
        confirmButton = {
            Button(onClick = { controller.resolveStartupFileAssociations(reassociate = true, warnOnChange = warnOnChange) }) {
                Text("Re-Associate")
            }
        },
        dismissButton = {
            TextButton(onClick = { controller.resolveStartupFileAssociations(reassociate = false, warnOnChange = warnOnChange) }) {
                Text("Keep Current")
            }
        },
        title = { Text("File Associations") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(prompt.message)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(checked = warnOnChange, onCheckedChange = { warnOnChange = it })
                    Column {
                        Text("Warn when associations change")
                        Text(
                            "Keep checking magnet and torrent ownership at startup.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    )
}

@Composable
private fun LibraryFileInfoDialog(controller: ComposeAppController, state: LibraryFileInfoDialogState) {
    val item = state.item
    val identity = controller.libraryItemIdentity(item)
    var renameValue by remember(state.version, item.file.absolutePath) { mutableStateOf(item.name) }
    val metadataEditor = controller.libraryMetadataEditor(item)
    var metadataDraft by remember(state.version, item.file.absolutePath) {
        mutableStateOf(metadataEditor?.draft ?: LibraryMetadataDraft())
    }
    var metadataFieldErrors by remember(state.version, item.file.absolutePath) {
        mutableStateOf<Map<FilePropertyKey, String>>(emptyMap())
    }
    var metadataError by remember(state.version, item.file.absolutePath) { mutableStateOf<String?>(null) }
    var renameError by remember(state.version, item.file.absolutePath) { mutableStateOf<String?>(null) }
    val jumpTargets = controller.libraryListTargetsForItem(item)
    val collectionTargets = controller.collectionTargetsForItem(item)
    val sharingMemberships = controller.librarySharingMemberships(item)
    val magnetLink = controller.libraryMagnetLink(item)
    val torrent = item.getProperty(FilePropertyKey.TORRENT) as? Torrent
    var torrentRefreshEpoch by remember(torrent?.sha1) { mutableIntStateOf(0) }
    val torrentState = rememberTorrentInspectorState(
        controller = controller,
        torrent = torrent,
        refreshEpoch = torrentRefreshEpoch,
        includeActivity = true,
        includePieces = false
    )
    val renameEnabled = !item.isIncomplete && renameValue.trim().isNotEmpty() && renameValue.trim() != item.name
    val metadataDirty = metadataEditor != null && metadataDraft != metadataEditor.draft

    fun updateMetadataDraft(
        updated: LibraryMetadataDraft,
        clearedKeys: Set<FilePropertyKey> = emptySet()
    ) {
        metadataDraft = updated
        metadataError = null
        metadataFieldErrors = if (clearedKeys.isEmpty()) {
            emptyMap()
        } else {
            metadataFieldErrors - clearedKeys
        }
    }

    ResponsiveDesktopDialog(
        onDismissRequest = { controller.closeLibraryFileInfoDialog() },
        confirmButton = {
            TextButton(onClick = { controller.closeLibraryFileInfoDialog() }) {
                Text("Close")
            }
        },
        title = { Text("File Info") },
        preferredWidth = 940.dp,
        preferredHeight = if (LocalDesktopDensity.current.compactVertical) 620.dp else 760.dp
    ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FileIdentityHeaderCard(
                    title = identity.title,
                    subtitle = identity.subtitle,
                    icon = identity.icon,
                    tertiary = "${item.category.getSingularName()} · ${formatBytes(item.size)}"
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilledTonalButton(onClick = {
                        controller.selectLibraryItem(item)
                        controller.openSelectedLibraryItems()
                    }) {
                        Text(if (item.category == Category.AUDIO) "Play / Open" else "Open")
                    }
                    OutlinedButton(onClick = {
                        controller.selectLibraryItem(item)
                        controller.revealSelectedLibraryItems()
                    }) {
                        Text("Reveal")
                    }
                    if (jumpTargets.isNotEmpty()) {
                        LibraryJumpMenuButton(
                            label = if (jumpTargets.size == 1) showInTargetLabel(jumpTargets.first()) else "Show In",
                            targets = jumpTargets,
                            onJump = { controller.showLibraryItemInTarget(item, it) }
                        )
                    }
                    if (collectionTargets.isNotEmpty() && collectionTargets.size != jumpTargets.size) {
                        LibraryJumpMenuButton(
                            label = "Show in Collection",
                            targets = collectionTargets,
                            onJump = { controller.showLibraryItemInTarget(item, it) }
                        )
                    }
                    magnetLink?.let {
                        OutlinedButton(onClick = { copyTextToClipboard(it) }) {
                            Text("Copy Magnet Link")
                        }
                    }
                }
                InfoSectionCard("Overview") {
                    OutlinedTextField(
                        value = renameValue,
                        onValueChange = {
                            renameValue = it
                            renameError = null
                        },
                        label = { Text("File name") },
                        enabled = !item.isIncomplete,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        isError = renameError != null
                    )
                    renameError?.let {
                        Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                    if (!item.isIncomplete) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            OutlinedButton(
                                onClick = { renameError = controller.renameLibraryFileFromInfo(item, renameValue) },
                                enabled = renameEnabled
                            ) {
                                Text("Rename")
                            }
                        }
                    }
                    InfoField("Path", item.file.absolutePath)
                    InfoField("Category", item.category.getSingularName())
                    InfoField("Size", formatBytes(item.size))
                    InfoField("Created", formatDate(item.creationTime))
                    InfoField("Updated", formatDate(item.lastModifiedTime))
                    InfoField("URN", item.urn?.toString() ?: if (item.isLoaded) "Unavailable" else "Calculating…")
                    InfoField("Hits", item.numHits.toString())
                    InfoField("Uploads", item.numUploads.toString())
                    InfoField("Upload Attempts", item.numUploadAttempts.toString())
                }
                metadataEditor?.let { editor ->
                    LibraryMetadataSection(
                        category = item.category,
                        editor = editor,
                        draft = metadataDraft,
                        fieldErrors = metadataFieldErrors,
                        dialogError = metadataError,
                        saveEnabled = metadataDirty,
                        onDraftChange = ::updateMetadataDraft,
                        onSave = {
                            val result = controller.saveLibraryMetadataFromInfo(item, metadataDraft)
                            metadataFieldErrors = result.fieldErrors
                            metadataError = result.dialogError
                        }
                    )
                }
                LibrarySharingSection(
                    item = item,
                    memberships = sharingMemberships,
                    onRemove = { controller.confirmRemoveLibraryItemFromSharingList(item, it.listId) }
                )
                torrentState.details?.let { details ->
                    TorrentDetailsSection(
                        controller = controller,
                        details = details,
                        activity = torrentState.activity,
                        pieces = null,
                        allowEditing = true,
                        onRefreshRequested = { torrentRefreshEpoch += 1 }
                    )
                }
            }
    }
}

@Composable
private fun SearchFileInfoDialog(controller: ComposeAppController, state: SearchFileInfoDialogState) {
    val result = state.result
    val primary = result.searchResults.firstOrNull()
    val presentation = controller.searchResultPresentation(result)
    val identity = presentation.identity
    val jumpTargets = presentation.jumpTargets
    val browseTargets = presentation.browseTargets
    val blockTargets = presentation.blockTargets
    val spamMarked = result.searchResults.any { it.isSpam }
    val canUnmarkSpam = controller.canUnmarkSearchResultsAsSpam(listOf(result))
    val canMarkSpam = controller.canMarkSearchResultsAsSpam(listOf(result))
    val magnetLink = controller.searchResultMagnetLink(result)
    val torrent = primary?.getProperty(FilePropertyKey.TORRENT) as? Torrent
    var torrentRefreshEpoch by remember(torrent?.sha1) { mutableIntStateOf(0) }
    val torrentState = rememberTorrentInspectorState(
        controller = controller,
        torrent = torrent,
        refreshEpoch = torrentRefreshEpoch,
        includeActivity = false,
        includePieces = false
    )

    ResponsiveDesktopDialog(
        onDismissRequest = { controller.closeSearchFileInfoDialog() },
        confirmButton = {
            TextButton(onClick = { controller.closeSearchFileInfoDialog() }) {
                Text("Close")
            }
        },
        title = { Text("File Info") },
        preferredWidth = 940.dp,
        preferredHeight = if (LocalDesktopDensity.current.compactVertical) 620.dp else 760.dp
    ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FileIdentityHeaderCard(
                    title = identity.title,
                    subtitle = identity.subtitle,
                    icon = identity.icon,
                    tertiary = "${primary?.category?.getSingularName() ?: "File"} · ${primary?.size?.let(::formatBytes) ?: "Unknown"}"
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilledTonalButton(onClick = { controller.activeSearchTab()?.let { controller.downloadSearchResult(it, result) } }) {
                        Text("Download")
                    }
                    OutlinedButton(onClick = { controller.activeSearchTab()?.let { controller.downloadSearchResultAs(it, result) } }) {
                        Text("Download As")
                    }
                    if (jumpTargets.isNotEmpty()) {
                        LibraryJumpMenuButton(
                            label = if (jumpTargets.size == 1) showInTargetLabel(jumpTargets.first()) else "Show In",
                            targets = jumpTargets,
                            onJump = { controller.showSearchResultInTarget(result, it) }
                        )
                    }
                    if (controller.canChatFromSearchResult(result)) {
                        OutlinedButton(onClick = { controller.chatFromSearchResult(result) }) {
                            Text("Chat")
                        }
                    }
                    BrowseSourceButton(controller, browseTargets)
                    BlockUsersButton(controller, blockTargets)
                    magnetLink?.let {
                        OutlinedButton(onClick = { copyTextToClipboard(it) }) {
                            Text("Copy Link")
                        }
                    }
                    when {
                        canUnmarkSpam -> OutlinedButton(
                            onClick = { controller.activeSearchTab()?.let { controller.unmarkSearchResultsAsSpam(it, listOf(result)) } }
                        ) {
                            Text("Unmark Spam")
                        }

                        canMarkSpam -> OutlinedButton(
                            onClick = { controller.activeSearchTab()?.let { controller.markSearchResultsAsSpam(it, listOf(result)) } }
                        ) {
                            Text("Mark as Spam")
                        }
                    }
                }
                InfoField("Category", primary?.category?.getSingularName() ?: "File")
                InfoField("Size", primary?.size?.let(::formatBytes) ?: "Unknown")
                InfoField("Sources", result.sources.size.toString())
                InfoField("Friend Sources", result.friends.size.toString())
                InfoField("URN", result.urn.toString())
                InfoField("Extension", primary?.fileExtension?.ifBlank { "Unknown" } ?: "Unknown")
                InfoField("Spam", if (spamMarked) "Marked as spam" else "Not marked")
                InfoField("Licensed", if (primary?.isLicensed == true) "Yes" else "No")
                listOf(
                    "Title" to primary?.getProperty(FilePropertyKey.TITLE),
                    "Artist" to primary?.getProperty(FilePropertyKey.AUTHOR),
                    "Album" to primary?.getProperty(FilePropertyKey.ALBUM),
                    "Genre" to primary?.getProperty(FilePropertyKey.GENRE),
                    "Year" to primary?.getProperty(FilePropertyKey.YEAR),
                    "Length" to primary?.getProperty(FilePropertyKey.LENGTH),
                    "Bitrate" to primary?.getProperty(FilePropertyKey.BITRATE),
                    "Company" to primary?.getProperty(FilePropertyKey.COMPANY),
                    "Description" to primary?.getProperty(FilePropertyKey.DESCRIPTION)
                ).forEach { (label, value) ->
                    value?.toString()?.takeIf(String::isNotBlank)?.let { InfoField(label, it) }
                }
                torrentState.details?.let { details ->
                    TorrentDetailsSection(
                        controller = controller,
                        details = details,
                        activity = null,
                        pieces = null,
                        allowEditing = false,
                        onRefreshRequested = { torrentRefreshEpoch += 1 }
                    )
                }
                primary?.let { SearchSourceDetails(controller, result, it) }
            }
        }
}

@Composable
private fun DownloadFileInfoDialog(controller: ComposeAppController, state: DownloadFileInfoDialogState) {
    val item = state.item
    val identity = controller.downloadItemIdentity(item)
    val jumpTargets = controller.downloadJumpTargets(item)
    val browseTargets = controller.downloadBrowseTargets(item)
    val blockTargets = controller.downloadBlockTargets(item)
    val magnetLink = controller.downloadMagnetLink(item)
    val torrent = item.getProperty(FilePropertyKey.TORRENT) as? Torrent
    var torrentRefreshEpoch by remember(torrent?.sha1) { mutableIntStateOf(0) }
    val torrentState = rememberTorrentInspectorState(
        controller = controller,
        torrent = torrent,
        downloadItem = item,
        refreshEpoch = torrentRefreshEpoch,
        includeActivity = true,
        includePieces = true
    )
    val desktopDensity = LocalDesktopDensity.current

    ResponsiveDesktopDialog(
        onDismissRequest = { controller.closeDownloadFileInfoDialog() },
        confirmButton = {
            TextButton(onClick = { controller.closeDownloadFileInfoDialog() }) {
                Text("Close")
            }
        },
        title = { Text("File Info") },
        preferredWidth = 940.dp,
        preferredHeight = if (desktopDensity.compactVertical) 620.dp else 760.dp
    ) {
        FileIdentityHeaderCard(
            title = identity.title,
            subtitle = identity.subtitle,
            icon = identity.icon,
            tertiary = "${item.category.getSingularName()} · ${formatBytes(item.currentSize)} of ${formatBytes(item.totalSize)}"
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(desktopDensity.chipGap),
            verticalArrangement = Arrangement.spacedBy(desktopDensity.chipGap)
        ) {
            if (item.isLaunchable) {
                FilledTonalButton(onClick = { controller.openDownloadItem(item) }) {
                    Text(controller.downloadOpenActionLabel(item))
                }
            }
            OutlinedButton(onClick = { controller.revealDownloadItem(item) }) {
                Text("Locate on Disk")
            }
            if (jumpTargets.isNotEmpty()) {
                LibraryJumpMenuButton(
                    label = if (jumpTargets.size == 1) showInTargetLabel(jumpTargets.first()) else "Show In",
                    targets = jumpTargets,
                    onJump = { controller.showDownloadInTarget(item, it) }
                )
            } else if (item.isLaunchable) {
                OutlinedButton(onClick = { controller.locateDownloadItem(item) }) {
                    Text("Locate in My Files")
                }
            }
            OutlinedButton(onClick = { controller.retryOrSearchAgainForDownload(item) }) {
                Text(controller.downloadRetryActionLabel(item))
            }
            if (controller.canChangeDownloadLocation(item)) {
                OutlinedButton(onClick = { controller.changeDownloadLocation(item) }) {
                    Text("Change Location")
                }
            }
            BrowseSourceButton(controller, browseTargets)
            BlockUsersButton(controller, blockTargets)
            magnetLink?.let {
                OutlinedButton(onClick = { copyTextToClipboard(it) }) {
                    Text("Copy Magnet Link")
                }
            }
        }
        InfoField("State", friendlyName(item.state.name))
        InfoField("Category", item.category.getSingularName())
        InfoField("Progress", "${item.percentComplete}%")
        InfoField("Saved To", item.saveFile.absolutePath)
        InfoField("Current File", item.downloadingFile?.absolutePath ?: item.saveFile.absolutePath)
        InfoField("URN", item.urn?.toString() ?: "Unavailable")
        InfoField("Sources", item.downloadSourceCount.toString())
        InfoField("Download Rate", formatRate(item.downloadSpeed))
        InfoField("Remaining", formatDuration(item.remainingDownloadTime))
        InfoField("Started", formatDate(item.startDate.time))
        if (item.errorState != DownloadItem.ErrorState.NONE) {
            InfoField("Error", item.errorState.message)
        }
        item.getPropertyString(FilePropertyKey.TITLE)?.takeIf(String::isNotBlank)?.let { InfoField("Title", it) }
        item.getPropertyString(FilePropertyKey.AUTHOR)?.takeIf(String::isNotBlank)?.let { InfoField("Artist", it) }
        item.getPropertyString(FilePropertyKey.ALBUM)?.takeIf(String::isNotBlank)?.let { InfoField("Album", it) }
        item.getPropertyString(FilePropertyKey.GENRE)?.takeIf(String::isNotBlank)?.let { InfoField("Genre", it) }
        item.getPropertyString(FilePropertyKey.DESCRIPTION)?.takeIf(String::isNotBlank)?.let { InfoField("Description", it) }
        torrentState.details?.let { details ->
            TorrentDetailsSection(
                controller = controller,
                details = details,
                activity = torrentState.activity,
                pieces = torrentState.pieces,
                allowEditing = true,
                onRefreshRequested = { torrentRefreshEpoch += 1 }
            )
        }
    }
}

@Composable
private fun UploadFileInfoDialog(controller: ComposeAppController, state: UploadFileInfoDialogState) {
    val item = state.item
    val identity = controller.uploadItemIdentity(item)
    val jumpTargets = controller.uploadJumpTargets(item)
    val browseTargets = controller.uploadBrowseTargets(item)
    val blockTargets = controller.uploadBlockTargets(item)
    val magnetLink = controller.uploadMagnetLink(item)
    val torrent = item.getProperty(FilePropertyKey.TORRENT) as? Torrent
    var torrentRefreshEpoch by remember(torrent?.sha1) { mutableIntStateOf(0) }
    val torrentState = rememberTorrentInspectorState(
        controller = controller,
        torrent = torrent,
        refreshEpoch = torrentRefreshEpoch,
        includeActivity = true,
        includePieces = false
    )
    val desktopDensity = LocalDesktopDensity.current

    ResponsiveDesktopDialog(
        onDismissRequest = { controller.closeUploadFileInfoDialog() },
        confirmButton = {
            TextButton(onClick = { controller.closeUploadFileInfoDialog() }) {
                Text("Close")
            }
        },
        title = { Text("File Info") },
        preferredWidth = 940.dp,
        preferredHeight = if (desktopDensity.compactVertical) 620.dp else 760.dp
    ) {
        FileIdentityHeaderCard(
            title = identity.title,
            subtitle = identity.subtitle,
            icon = identity.icon,
            tertiary = "${item.category.getSingularName()} · ${formatBytes(item.fileSize)}"
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(desktopDensity.chipGap),
            verticalArrangement = Arrangement.spacedBy(desktopDensity.chipGap)
        ) {
            if (controller.canOpenUploadItem(item)) {
                FilledTonalButton(onClick = { controller.openUploadItem(item) }) {
                    Text(controller.uploadOpenActionLabel(item))
                }
                OutlinedButton(onClick = { controller.revealUploadItem(item) }) {
                    Text("Locate on Disk")
                }
                if (jumpTargets.isNotEmpty()) {
                    LibraryJumpMenuButton(
                        label = if (jumpTargets.size == 1) showInTargetLabel(jumpTargets.first()) else "Show In",
                        targets = jumpTargets,
                        onJump = { controller.showUploadInTarget(item, it) }
                    )
                } else {
                    OutlinedButton(onClick = { controller.locateUploadItem(item) }) {
                        Text("Locate in My Files")
                    }
                }
            }
            BrowseSourceButton(controller, browseTargets)
            BlockUsersButton(controller, blockTargets)
            magnetLink?.let {
                OutlinedButton(onClick = { copyTextToClipboard(it) }) {
                    Text("Copy Magnet Link")
                }
            }
        }
        InfoField("State", friendlyName(item.state.name))
        InfoField("Category", item.category.getSingularName())
        InfoField("Path", item.file.absolutePath)
        InfoField("URN", item.urn?.toString() ?: "Unavailable")
        InfoField("Uploaded", formatBytes(item.totalAmountUploaded))
        InfoField("Size", formatBytes(item.fileSize))
        InfoField("Upload Rate", formatRate(item.uploadSpeed))
        InfoField("Peers", item.numUploadConnections.toString())
        InfoField("Remote Host", item.renderName)
        InfoField("Browse Mode", friendlyName(item.browseType.name))
        if (item.seedRatio >= 0f) {
            InfoField("Seed Ratio", String.format(Locale.US, "%.2f", item.seedRatio))
        }
        item.getPropertyString(FilePropertyKey.TITLE)?.takeIf(String::isNotBlank)?.let { InfoField("Title", it) }
        item.getPropertyString(FilePropertyKey.AUTHOR)?.takeIf(String::isNotBlank)?.let { InfoField("Artist", it) }
        item.getPropertyString(FilePropertyKey.ALBUM)?.takeIf(String::isNotBlank)?.let { InfoField("Album", it) }
        item.getPropertyString(FilePropertyKey.GENRE)?.takeIf(String::isNotBlank)?.let { InfoField("Genre", it) }
        item.getPropertyString(FilePropertyKey.DESCRIPTION)?.takeIf(String::isNotBlank)?.let { InfoField("Description", it) }
        torrentState.details?.let { details ->
            TorrentDetailsSection(
                controller = controller,
                details = details,
                activity = torrentState.activity,
                pieces = null,
                allowEditing = true,
                onRefreshRequested = { torrentRefreshEpoch += 1 }
            )
        }
    }
}

@Composable
private fun PreferencesDialog(controller: ComposeAppController) {
    var draft by remember(controller.preferencesDialogVersion) {
        mutableStateOf(controller.currentPreferences())
    }
    var section by remember(controller.preferencesDialogVersion) { mutableStateOf(controller.requestedPreferencesSection) }
    var keywordsEditorOpen by remember(controller.preferencesDialogVersion) { mutableStateOf(false) }
    var extensionsEditorOpen by remember(controller.preferencesDialogVersion) { mutableStateOf(false) }
    var blockedHostsEditorOpen by remember(controller.preferencesDialogVersion) { mutableStateOf(false) }
    var allowedHostsEditorOpen by remember(controller.preferencesDialogVersion) { mutableStateOf(false) }
    var unsafeSharingEditorOpen by remember(controller.preferencesDialogVersion) {
        mutableStateOf(controller.openUnsafeSharingEditorOnPreferencesOpen)
    }
    var saveFoldersEditorOpen by remember(controller.preferencesDialogVersion) { mutableStateOf(false) }
    val validationError = preferencesValidationError(draft)
    val contentScrollState = rememberScrollState()
    val desktopDensity = LocalDesktopDensity.current

    LaunchedEffect(controller.preferencesDialogVersion, section) {
        if (section == PreferencesSection.TRANSFERS) {
            controller.refreshTorrentEngineHealthState()
        }
    }

    ResponsiveDesktopDialog(
        onDismissRequest = { controller.preferencesDialogOpen = false },
        confirmButton = {
            Button(
                onClick = { controller.applyPreferences(draft) },
                enabled = validationError == null
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = { controller.preferencesDialogOpen = false }) {
                Text("Cancel")
            }
        },
        title = { Text("Preferences") },
        preferredWidth = 1120.dp,
        preferredHeight = if (desktopDensity.compactVertical) 640.dp else 760.dp,
        scrollBody = false
    ) {
            Row(
                modifier = Modifier.fillMaxWidth().heightIn(min = 360.dp, max = if (desktopDensity.compactVertical) 620.dp else 700.dp),
                horizontalArrangement = Arrangement.spacedBy(desktopDensity.sectionGap)
            ) {
                Surface(
                    modifier = Modifier.width(if (desktopDensity.compactHorizontal) 164.dp else 180.dp).fillMaxHeight(),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(desktopDensity.cardPadding),
                        verticalArrangement = Arrangement.spacedBy(desktopDensity.shellGap)
                    ) {
                        PreferencesSection.entries.forEach { entry ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (section == entry) {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    Color.Transparent
                                },
                                modifier = Modifier.fillMaxWidth().clickable { section = entry }
                            ) {
                                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)) {
                                    Text(entry.label, fontWeight = if (section == entry) FontWeight.SemiBold else FontWeight.Medium)
                                    Text(
                                        entry.summary,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
                Column(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(desktopDensity.sectionGap)
                ) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(desktopDensity.cardPadding).verticalScroll(contentScrollState),
                            verticalArrangement = Arrangement.spacedBy(desktopDensity.sectionGap)
                        ) {
                            Text(section.label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            Text(
                                section.summary,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            when (section) {
                                PreferencesSection.SEARCH -> {
                                    PreferenceDropdownField(
                                        label = "Default search category",
                                        value = when (draft.search.defaultCategory) {
                                            SearchCategory.ALL -> "All Files"
                                            else -> friendlyName(draft.search.defaultCategory.name)
                                        },
                                        options = buildList {
                                            add(SearchCategory.ALL to "All Files")
                                            add(SearchCategory.AUDIO to "Audio")
                                            add(SearchCategory.VIDEO to "Video")
                                            add(SearchCategory.IMAGE to "Images")
                                            add(SearchCategory.DOCUMENT to "Documents")
                                            if (draft.library.allowProgramSearchAndShare) {
                                                add(SearchCategory.PROGRAM to "Programs")
                                            }
                                            add(SearchCategory.TORRENT to "Torrents")
                                        },
                                        onSelected = { category ->
                                            draft = draft.copy(search = draft.search.copy(defaultCategory = category))
                                        }
                                    )
                                    PreferenceToggle("Show smart suggestions", draft.search.showSmartSuggestions) {
                                        draft = draft.copy(search = draft.search.copy(showSmartSuggestions = it))
                                    }
                                    PreferenceToggle("Keep search history", draft.search.keepSearchHistory) {
                                        draft = draft.copy(search = draft.search.copy(keepSearchHistory = it))
                                    }
                                    PreferenceToggle("Group similar results", draft.search.groupSimilarResults) {
                                        draft = draft.copy(search = draft.search.copy(groupSimilarResults = it))
                                    }
                                    PreferenceToggle("Use torrent web search", draft.search.useTorrentWebSearch) {
                                        draft = draft.copy(search = draft.search.copy(useTorrentWebSearch = it))
                                    }
                                    PreferenceToggle("Filter adult content", draft.search.filterAdultContent) {
                                        draft = draft.copy(search = draft.search.copy(filterAdultContent = it))
                                    }
                                    PreferenceToggle(
                                        "Use host filters",
                                        draft.search.hostFilters.enabled,
                                        supportingText = "Apply your allowed and blocked host rules to new searches."
                                    ) {
                                        draft = draft.copy(
                                            search = draft.search.copy(
                                                hostFilters = draft.search.hostFilters.copy(enabled = it)
                                            )
                                        )
                                    }
                                    PreferenceActionRow(
                                        title = "Blocked hosts",
                                        description = if (draft.search.hostFilters.blockedHosts.isEmpty()) {
                                            "No blocked hosts or IP ranges yet."
                                        } else {
                                            "${draft.search.hostFilters.blockedHosts.size} blocked rule(s)."
                                        }
                                    ) {
                                        OutlinedButton(onClick = { blockedHostsEditorOpen = true }) {
                                            Text("Edit…")
                                        }
                                    }
                                    PreferenceActionRow(
                                        title = "Allowed hosts",
                                        description = if (draft.search.hostFilters.allowedHosts.isEmpty()) {
                                            "No allowed hosts yet."
                                        } else {
                                            "${draft.search.hostFilters.allowedHosts.size} allowed rule(s)."
                                        }
                                    ) {
                                        OutlinedButton(onClick = { allowedHostsEditorOpen = true }) {
                                            Text("Edit…")
                                        }
                                    }
                                    PreferenceActionRow(
                                        title = "Search history",
                                        description = if (draft.search.keepSearchHistory) {
                                            "Recent searches are saved while history is on."
                                        } else {
                                            "Search history is off."
                                        }
                                    ) {
                                        OutlinedButton(onClick = { controller.clearSearchHistory() }) {
                                            Text("Clear History")
                                        }
                                    }
                                    PreferenceActionRow(
                                        title = "Blocked keywords",
                                        description = if (draft.search.blockedKeywords.isEmpty()) {
                                            "No custom blocked phrases are configured."
                                        } else {
                                            "${draft.search.blockedKeywords.size} blocked phrase(s) configured."
                                        }
                                    ) {
                                        OutlinedButton(onClick = { keywordsEditorOpen = true }) {
                                            Text("Edit…")
                                        }
                                    }
                                    PreferenceActionRow(
                                        title = "Blocked file extensions",
                                        description = "${draft.search.blockedExtensions.size} blocked extension(s) configured."
                                    ) {
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            OutlinedButton(onClick = { extensionsEditorOpen = true }) {
                                                Text("Edit…")
                                            }
                                            TextButton(onClick = {
                                                draft = draft.copy(
                                                    search = draft.search.copy(
                                                        blockedExtensions = controller.defaultBlockedExtensions()
                                                    )
                                                )
                                            }) {
                                                Text("Reset Defaults")
                                            }
                                        }
                                    }
                                    PreferenceActionRow(
                                        title = "Spam filter training",
                                        description = "Clear learned spam data and let the filter learn again."
                                    ) {
                                        OutlinedButton(onClick = { controller.resetSpamFilter() }) {
                                            Text("Reset Spam Filter")
                                        }
                                    }
                                }

                                PreferencesSection.LIBRARY -> {
                                    PreferenceToggle("Use built-in player", draft.library.playerEnabled) {
                                        draft = draft.copy(library = draft.library.copy(playerEnabled = it))
                                    }
                                    PreferenceToggle("Handle magnet links", draft.library.handleMagnets) {
                                        draft = draft.copy(library = draft.library.copy(handleMagnets = it))
                                    }
                                    PreferenceToggle("Handle torrent files", draft.library.handleTorrents) {
                                        draft = draft.copy(library = draft.library.copy(handleTorrents = it))
                                    }
                                    PreferenceToggle("Warn before changing file associations", draft.library.warnFileAssociationChanges) {
                                        draft = draft.copy(library = draft.library.copy(warnFileAssociationChanges = it))
                                    }
                                    PreferenceToggle("Share downloaded files automatically", draft.library.shareDownloadedFiles) {
                                        draft = draft.copy(library = draft.library.copy(shareDownloadedFiles = it))
                                    }
                                    PreferenceToggle("Share partially downloaded files", draft.library.allowPartialSharing) {
                                        draft = draft.copy(library = draft.library.copy(allowPartialSharing = it))
                                    }
                                    PreferenceToggle("Allow program searching and sharing", draft.library.allowProgramSearchAndShare) {
                                        draft = draft.copy(
                                            library = draft.library.copy(allowProgramSearchAndShare = it),
                                            search = draft.search.copy(
                                                defaultCategory = when {
                                                    it -> draft.search.defaultCategory
                                                    draft.search.defaultCategory == SearchCategory.PROGRAM -> SearchCategory.ALL
                                                    else -> draft.search.defaultCategory
                                                }
                                            )
                                        )
                                    }
                                    PreferenceToggle("Allow public document sharing", draft.library.allowDocumentSharing) {
                                        draft = draft.copy(library = draft.library.copy(allowDocumentSharing = it))
                                    }
                                    PreferenceActionRow(
                                        title = "Public sharing safety",
                                        description = buildUnsafeSharingSummary(draft.library)
                                    ) {
                                        OutlinedButton(onClick = { unsafeSharingEditorOpen = true }) {
                                            Text("Edit Rules…")
                                        }
                                    }
                                    Text("Default categories when adding folders", fontWeight = FontWeight.Medium)
                                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        listOf(
                                            "Audio" to draft.library.addToLibraryDefaults.audio,
                                            "Video" to draft.library.addToLibraryDefaults.video,
                                            "Images" to draft.library.addToLibraryDefaults.images,
                                            "Documents" to draft.library.addToLibraryDefaults.documents,
                                            "Programs" to draft.library.addToLibraryDefaults.programs
                                        ).forEach { (label, selected) ->
                                            FilterChip(
                                                selected = selected,
                                                onClick = {
                                                    val defaults = draft.library.addToLibraryDefaults
                                                    draft = draft.copy(
                                                        library = draft.library.copy(
                                                            addToLibraryDefaults = when (label) {
                                                                "Audio" -> defaults.copy(audio = !selected)
                                                                "Video" -> defaults.copy(video = !selected)
                                                                "Images" -> defaults.copy(images = !selected)
                                                                "Documents" -> defaults.copy(documents = !selected)
                                                                else -> defaults.copy(programs = !selected)
                                                            }
                                                        )
                                                    )
                                                },
                                                label = { Text(label) }
                                            )
                                        }
                                    }
                                    if (draft.library.iTunes.supported) {
                                        Surface(
                                            color = MaterialTheme.colorScheme.surface,
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Column(
                                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                                verticalArrangement = Arrangement.spacedBy(10.dp)
                                            ) {
                                                Text("iTunes & DAAP", fontWeight = FontWeight.Medium)
                                                PreferenceToggle(
                                                    "Add downloaded audio to the local library",
                                                    draft.library.iTunes.addDownloadedAudioToLibrary
                                                ) {
                                                    draft = draft.copy(
                                                        library = draft.library.copy(
                                                            iTunes = draft.library.iTunes.copy(addDownloadedAudioToLibrary = it)
                                                        )
                                                    )
                                                }
                                                PreferenceToggle(
                                                    "Share audio across the local network",
                                                    draft.library.iTunes.shareAudioAcrossLan
                                                ) {
                                                    draft = draft.copy(
                                                        library = draft.library.copy(
                                                            iTunes = draft.library.iTunes.copy(shareAudioAcrossLan = it)
                                                        )
                                                    )
                                                }
                                                PreferenceToggle(
                                                    "Require a password for DAAP sharing",
                                                    draft.library.iTunes.requirePassword,
                                                    enabled = draft.library.iTunes.shareAudioAcrossLan
                                                ) {
                                                    draft = draft.copy(
                                                        library = draft.library.copy(
                                                            iTunes = draft.library.iTunes.copy(requirePassword = it)
                                                        )
                                                    )
                                                }
                                                OutlinedTextField(
                                                    value = draft.library.iTunes.password,
                                                    onValueChange = {
                                                        draft = draft.copy(
                                                            library = draft.library.copy(
                                                                iTunes = draft.library.iTunes.copy(password = it)
                                                            )
                                                        )
                                                    },
                                                    singleLine = true,
                                                    label = { Text("DAAP password") },
                                                    enabled = draft.library.iTunes.shareAudioAcrossLan && draft.library.iTunes.requirePassword,
                                                    modifier = Modifier.fillMaxWidth()
                                                )
                                            }
                                        }
                                    } else {
                                        Text(
                                            "iTunes and DAAP sharing are not available on this platform.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Text(
                                        "These settings affect My Files, shared collections, and future downloads.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                PreferencesSection.TRANSFERS -> {
                                    if (controller.torrentEngineHealthState == TorrentEngineHealthState.ERROR) {
                                        InlineStatusBanner(
                                            title = "BitTorrent Unavailable",
                                            message = controller.torrentEngineHealthMessage(),
                                            level = OperationNoticeLevel.WARNING
                                        )
                                    }
                                    PreferenceToggle("Upload torrents forever", draft.transfers.uploadTorrentsForever) {
                                        draft = draft.copy(transfers = draft.transfers.copy(uploadTorrentsForever = it))
                                    }
                                    Text(
                                        if (draft.transfers.uploadTorrentsForever) {
                                            "WireShare will keep seeding torrents until you stop them manually."
                                        } else {
                                            "WireShare will stop seeding when either the target ratio or elapsed time is reached."
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    OutlinedTextField(
                                        value = draft.transfers.torrentSeedRatio,
                                        onValueChange = {
                                            draft = draft.copy(
                                                transfers = draft.transfers.copy(
                                                    torrentSeedRatio = sanitizeDecimalPreferenceInput(it)
                                                )
                                            )
                                        },
                                        label = { Text("Upload/download ratio") },
                                        singleLine = true,
                                        enabled = !draft.transfers.uploadTorrentsForever,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        OutlinedTextField(
                                            value = draft.transfers.torrentSeedDays,
                                            onValueChange = {
                                                draft = draft.copy(
                                                    transfers = draft.transfers.copy(
                                                        torrentSeedDays = it.filter(Char::isDigit)
                                                    )
                                                )
                                            },
                                            label = { Text("Days") },
                                            singleLine = true,
                                            enabled = !draft.transfers.uploadTorrentsForever,
                                            modifier = Modifier.weight(1f)
                                        )
                                        OutlinedTextField(
                                            value = draft.transfers.torrentSeedHours,
                                            onValueChange = {
                                                draft = draft.copy(
                                                    transfers = draft.transfers.copy(
                                                        torrentSeedHours = it.filter(Char::isDigit)
                                                    )
                                                )
                                            },
                                            label = { Text("Hours") },
                                            singleLine = true,
                                            enabled = !draft.transfers.uploadTorrentsForever,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                    PreferenceToggle(
                                        "Always ask before starting torrent",
                                        draft.transfers.showTorrentSelectorBeforeDownloading
                                    ) {
                                        draft = draft.copy(
                                            transfers = draft.transfers.copy(showTorrentSelectorBeforeDownloading = it)
                                        )
                                    }
                                    PreferenceToggle("Automatically rename duplicate files", draft.transfers.autoRenameDuplicateFiles) {
                                        draft = draft.copy(transfers = draft.transfers.copy(autoRenameDuplicateFiles = it))
                                    }
                                    PreferenceToggle("Show Transfers by default", draft.transfers.showTransfersTrayByDefault) {
                                        draft = draft.copy(transfers = draft.transfers.copy(showTransfersTrayByDefault = it))
                                    }
                                    PreferenceToggle("Hide Transfers when nothing is active", draft.transfers.closeTrayWhenNoTransfers) {
                                        draft = draft.copy(transfers = draft.transfers.copy(closeTrayWhenNoTransfers = it))
                                    }
                                    PreferenceToggle("Show total bandwidth in Transfers", draft.transfers.showTotalBandwidth) {
                                        draft = draft.copy(transfers = draft.transfers.copy(showTotalBandwidth = it))
                                    }
                                    PreferenceToggle("Clear finished downloads automatically", draft.transfers.clearDownloadsWhenFinished) {
                                        draft = draft.copy(transfers = draft.transfers.copy(clearDownloadsWhenFinished = it))
                                    }
                                    PreferenceToggle("Clear finished uploads automatically", draft.transfers.clearUploadsWhenFinished) {
                                        draft = draft.copy(transfers = draft.transfers.copy(clearUploadsWhenFinished = it))
                                    }
                                    OutlinedTextField(
                                        value = draft.transfers.maxDownloadsAtOnce,
                                        onValueChange = {
                                            draft = draft.copy(transfers = draft.transfers.copy(maxDownloadsAtOnce = it.filter(Char::isDigit)))
                                        },
                                        label = { Text("Max simultaneous downloads") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    OutlinedTextField(
                                        value = draft.transfers.maxUploadsAtOnce,
                                        onValueChange = {
                                            draft = draft.copy(transfers = draft.transfers.copy(maxUploadsAtOnce = it.filter(Char::isDigit)))
                                        },
                                        label = { Text("Max simultaneous uploads") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    PreferenceToggle("Limit download bandwidth", draft.transfers.limitDownloadBandwidth) {
                                        draft = draft.copy(transfers = draft.transfers.copy(limitDownloadBandwidth = it))
                                    }
                                    OutlinedTextField(
                                        value = draft.transfers.maxDownloadKiB,
                                        onValueChange = {
                                            draft = draft.copy(transfers = draft.transfers.copy(maxDownloadKiB = it.filter(Char::isDigit)))
                                        },
                                        label = { Text("Max download KiB/s (0 = unlimited)") },
                                        singleLine = true,
                                        enabled = draft.transfers.limitDownloadBandwidth,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    PreferenceToggle("Limit upload bandwidth", draft.transfers.limitUploadBandwidth) {
                                        draft = draft.copy(transfers = draft.transfers.copy(limitUploadBandwidth = it))
                                    }
                                    OutlinedTextField(
                                        value = draft.transfers.maxUploadKiB,
                                        onValueChange = {
                                            draft = draft.copy(transfers = draft.transfers.copy(maxUploadKiB = it.filter(Char::isDigit)))
                                        },
                                        label = { Text("Max upload KiB/s (0 = unlimited)") },
                                        singleLine = true,
                                        enabled = draft.transfers.limitUploadBandwidth,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Text("Download folder", fontWeight = FontWeight.Medium)
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                        OutlinedTextField(
                                            value = draft.transfers.downloadDirectory,
                                            onValueChange = {
                                                controller.preferencesDialogError = null
                                                draft = draft.copy(transfers = draft.transfers.copy(downloadDirectory = it))
                                            },
                                            singleLine = true,
                                            modifier = Modifier.weight(1f),
                                            label = { Text("Save completed downloads to") }
                                        )
                                        OutlinedButton(
                                            onClick = {
                                                controller.choosePreferencesDownloadDirectory(
                                                    draft.transfers.downloadDirectory
                                                )?.let { result ->
                                                    if (result.accepted) {
                                                        controller.preferencesDialogError = null
                                                        draft = draft.copy(
                                                            transfers = draft.transfers.copy(
                                                                downloadDirectory = result.normalizedPath ?: draft.transfers.downloadDirectory
                                                            )
                                                        )
                                                    } else {
                                                        controller.preferencesDialogError = result.errorMessage
                                                    }
                                                }
                                            }
                                        ) {
                                            Text("Browse…")
                                        }
                                    }
                                    PreferenceToggle("Use category-specific save folders", draft.transfers.useCategorySpecificFolders) {
                                        draft = draft.copy(transfers = draft.transfers.copy(useCategorySpecificFolders = it))
                                    }
                                    PreferenceActionRow(
                                        title = "Category save folders",
                                        description = if (draft.transfers.useCategorySpecificFolders) {
                                            "Audio, video, image, document, program, and other downloads can each land in a different folder."
                                        } else {
                                            "All downloads currently use the main save folder."
                                        }
                                    ) {
                                        OutlinedButton(
                                            enabled = draft.transfers.useCategorySpecificFolders,
                                            onClick = { saveFoldersEditorOpen = true }
                                        ) {
                                            Text("Edit Folders…")
                                        }
                                    }
                                    Text(
                                        "Incomplete downloads stay beside the selected save folder so torrents can resume reliably.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                PreferencesSection.FRIENDS -> {
                                    PreferenceToggle("Show notifications", draft.friends.showNotifications) {
                                        draft = draft.copy(friends = draft.friends.copy(showNotifications = it))
                                    }
                                    PreferenceToggle("Play notification sounds", draft.friends.playNotificationSound) {
                                        draft = draft.copy(friends = draft.friends.copy(playNotificationSound = it))
                                    }
                                    Surface(
                                        color = MaterialTheme.colorScheme.surface,
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text("Saved Friends sign-in", fontWeight = FontWeight.Medium)
                                                Text(
                                                    if (draft.friends.autoLoginConfigured) {
                                                        "Saved sign-in settings are ready for next time."
                                                    } else {
                                                        "Save Friends sign-in here without signing in right now."
                                                    },
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            OutlinedButton(onClick = { controller.showFriendLoginSettingsDialog() }) {
                                                Text(if (draft.friends.autoLoginConfigured) "Manage Sign-In" else "Set Up")
                                            }
                                        }
                                    }
                                    Text(
                                        "These settings only control automatic sign-in. Use Friends to sign in or out right now.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                PreferencesSection.SYSTEM -> {
                                    PreferenceDropdownField(
                                        label = "Appearance",
                                        value = draft.system.appearance.label,
                                        options = ComposeAppearance.entries.map { it to it.label },
                                        onSelected = { appearance ->
                                            draft = draft.copy(system = draft.system.copy(appearance = appearance))
                                        }
                                    )
                                    PreferenceToggle("Connect on startup", draft.system.connectOnStartup) {
                                        draft = draft.copy(system = draft.system.copy(connectOnStartup = it))
                                    }
                                    PreferenceToggle(
                                        label = "Run at login",
                                        value = draft.system.runOnStartup,
                                        enabled = draft.system.runOnStartupSupported,
                                        supportingText = if (draft.system.runOnStartupSupported) {
                                            null
                                        } else {
                                            "Run-at-login is not available on this platform."
                                        }
                                    ) {
                                        draft = draft.copy(system = draft.system.copy(runOnStartup = it))
                                    }
                                    PreferenceToggle("Minimize to tray when closing", draft.system.minimizeToTray) {
                                        draft = draft.copy(system = draft.system.copy(minimizeToTray = it))
                                    }
                                    PreferenceToggle("Restore previous window placement", draft.system.restoreWindowPlacement) {
                                        draft = draft.copy(system = draft.system.copy(restoreWindowPlacement = it))
                                    }
                                    PreferenceDropdownField(
                                        label = "Security level",
                                        value = when (draft.system.securityLevel) {
                                            SecurityLevelOption.NONE -> "Off"
                                            SecurityLevelOption.LIGHT -> "Light"
                                            SecurityLevelOption.LIGHT_JAPAN_BLOCK -> "Light + Japan block"
                                            SecurityLevelOption.STRONG -> "Strong"
                                            SecurityLevelOption.STRONG_JAPAN_BLOCK -> "Strong + Japan block"
                                        },
                                        options = listOf(
                                            SecurityLevelOption.NONE to "Off",
                                            SecurityLevelOption.LIGHT to "Light",
                                            SecurityLevelOption.LIGHT_JAPAN_BLOCK to "Light + Japan block",
                                            SecurityLevelOption.STRONG to "Strong",
                                            SecurityLevelOption.STRONG_JAPAN_BLOCK to "Strong + Japan block"
                                        ),
                                        onSelected = { level ->
                                            draft = draft.copy(system = draft.system.copy(securityLevel = level))
                                        }
                                    )
                                    Surface(
                                        color = MaterialTheme.colorScheme.surface,
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text("Language", fontWeight = FontWeight.Medium)
                                                Text(
                                                    controller.currentLocale().getDisplayName(controller.currentLocale())
                                                        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(controller.currentLocale()) else it.toString() },
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            OutlinedButton(onClick = {
                                                controller.preferencesDialogOpen = false
                                                controller.showLanguageDialog()
                                            }) {
                                                Text("Change Language…")
                                            }
                                        }
                                    }
                                    PreferenceActionRow(
                                        title = "Desktop warnings",
                                        description = "Reset download, sharing, block-user, threat, and torrent confirmation prompts back to their default behavior."
                                    ) {
                                        OutlinedButton(onClick = { controller.resetWarnings() }) {
                                            Text("Reset Warnings")
                                        }
                                    }
                                }

                                PreferencesSection.NETWORK -> {
                                    PreferenceToggle(
                                        "Allow local REST and external control",
                                        draft.system.localRestAccessEnabled,
                                        supportingText = "Turn this on only if another app or script needs to control WireShare."
                                    ) {
                                        draft = draft.copy(system = draft.system.copy(localRestAccessEnabled = it))
                                    }
                                    PreferenceDropdownField(
                                        label = "Proxy mode",
                                        value = when (draft.network.proxyMode) {
                                            ProxyMode.NONE -> "No proxy"
                                            ProxyMode.SOCKS4 -> "SOCKS 4"
                                            ProxyMode.SOCKS5 -> "SOCKS 5"
                                            ProxyMode.HTTP -> "HTTP"
                                        },
                                        options = listOf(
                                            ProxyMode.NONE to "No proxy",
                                            ProxyMode.SOCKS4 to "SOCKS 4",
                                            ProxyMode.SOCKS5 to "SOCKS 5",
                                            ProxyMode.HTTP to "HTTP"
                                        ),
                                        onSelected = { mode ->
                                            draft = draft.copy(network = draft.network.copy(proxyMode = mode))
                                        }
                                    )
                                    val proxyShowsHostAndPort = draft.network.proxyMode != ProxyMode.NONE
                                    val proxySupportsAuthentication =
                                        draft.network.proxyMode == ProxyMode.SOCKS4 || draft.network.proxyMode == ProxyMode.SOCKS5
                                    val proxyShowsPassword = draft.network.proxyMode == ProxyMode.SOCKS5
                                    if (proxyShowsHostAndPort) {
                                        OutlinedTextField(
                                            value = draft.network.proxyHost,
                                            onValueChange = {
                                                draft = draft.copy(network = draft.network.copy(proxyHost = it))
                                            },
                                            label = { Text("Proxy host") },
                                            singleLine = true,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        OutlinedTextField(
                                            value = draft.network.proxyPort,
                                            onValueChange = {
                                                draft = draft.copy(network = draft.network.copy(proxyPort = it.filter(Char::isDigit)))
                                            },
                                            label = { Text("Proxy port") },
                                            singleLine = true,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        if (proxySupportsAuthentication) {
                                            PreferenceToggle("Use proxy authentication", draft.network.proxyAuthenticate) {
                                                draft = draft.copy(network = draft.network.copy(proxyAuthenticate = it))
                                            }
                                        }
                                        if (proxySupportsAuthentication && draft.network.proxyAuthenticate) {
                                            OutlinedTextField(
                                                value = draft.network.proxyUsername,
                                                onValueChange = {
                                                    draft = draft.copy(network = draft.network.copy(proxyUsername = it))
                                                },
                                                label = { Text("Proxy username") },
                                                singleLine = true,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                            if (proxyShowsPassword) {
                                                OutlinedTextField(
                                                    value = draft.network.proxyPassword,
                                                    onValueChange = {
                                                        draft = draft.copy(network = draft.network.copy(proxyPassword = it))
                                                    },
                                                    label = { Text("Proxy password") },
                                                    singleLine = true,
                                                    modifier = Modifier.fillMaxWidth()
                                                )
                                            }
                                        }
                                    }
                                    OutlinedTextField(
                                        value = draft.network.gnutellaPort,
                                        onValueChange = {
                                            draft = draft.copy(network = draft.network.copy(gnutellaPort = it.filter(Char::isDigit)))
                                        },
                                        label = { Text("Listening port") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    PreferenceToggle(
                                        "Use UPnP for torrent port mapping",
                                        draft.network.torrentUseUpnp
                                    ) {
                                        draft = draft.copy(network = draft.network.copy(torrentUseUpnp = it))
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        OutlinedTextField(
                                            value = draft.network.torrentListenStartPort,
                                            onValueChange = {
                                                draft = draft.copy(
                                                    network = draft.network.copy(
                                                        torrentListenStartPort = it.filter(Char::isDigit)
                                                    )
                                                )
                                            },
                                            label = { Text("BitTorrent start port") },
                                            singleLine = true,
                                            modifier = Modifier.weight(1f)
                                        )
                                        OutlinedTextField(
                                            value = draft.network.torrentListenEndPort,
                                            onValueChange = {
                                                draft = draft.copy(
                                                    network = draft.network.copy(
                                                        torrentListenEndPort = it.filter(Char::isDigit)
                                                    )
                                                )
                                            },
                                            label = { Text("BitTorrent end port") },
                                            singleLine = true,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                    PreferenceDropdownField(
                                        label = "Port forwarding",
                                        value = when (draft.network.portForwardMode) {
                                            PortForwardMode.UPNP -> "UPnP"
                                            PortForwardMode.MANUAL -> "Manual"
                                            PortForwardMode.NONE -> "Disabled"
                                        },
                                        options = listOf(
                                            PortForwardMode.UPNP to "UPnP",
                                            PortForwardMode.MANUAL to "Manual",
                                            PortForwardMode.NONE to "Disabled"
                                        ),
                                        onSelected = { mode ->
                                            draft = draft.copy(network = draft.network.copy(portForwardMode = mode))
                                        }
                                    )
                                    if (draft.network.portForwardMode == PortForwardMode.MANUAL) {
                                        OutlinedTextField(
                                            value = draft.network.manualPort,
                                            onValueChange = {
                                                draft = draft.copy(network = draft.network.copy(manualPort = it.filter(Char::isDigit)))
                                            },
                                            label = { Text("Manual port") },
                                            singleLine = true,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                    PreferenceToggle("Choose a network interface manually", draft.network.useCustomNetworkInterface) {
                                        draft = draft.copy(network = draft.network.copy(useCustomNetworkInterface = it))
                                    }
                                    if (draft.network.useCustomNetworkInterface) {
                                        PreferenceDropdownField(
                                            label = "Network interface",
                                            value = draft.network.availableNetworkInterfaces.firstOrNull {
                                                it.address == draft.network.selectedNetworkInterfaceAddress
                                            }?.let { "${it.displayName} (${it.address})" } ?: "Choose an interface",
                                            options = draft.network.availableNetworkInterfaces.map {
                                                it to "${it.displayName} (${it.address})"
                                            },
                                            enabled = draft.network.availableNetworkInterfaces.isNotEmpty(),
                                            onSelected = { option ->
                                                draft = draft.copy(network = draft.network.copy(selectedNetworkInterfaceAddress = option.address))
                                            }
                                        )
                                    }
                                    PreferenceToggle("Disable Ultrapeer mode", draft.network.disableUltrapeer) {
                                        draft = draft.copy(network = draft.network.copy(disableUltrapeer = it))
                                    }
                                    PreferenceToggle("Disable Mojito / DHT", draft.network.disableMojito) {
                                        draft = draft.copy(network = draft.network.copy(disableMojito = it))
                                    }
                                    PreferenceToggle("Disable TLS connections", draft.network.disableTls) {
                                        draft = draft.copy(network = draft.network.copy(disableTls = it))
                                    }
                                    PreferenceToggle("Disable out-of-band search", draft.network.disableOutOfBandSearch) {
                                        draft = draft.copy(network = draft.network.copy(disableOutOfBandSearch = it))
                                    }
                                    Text(
                                        "Some network, security, and interface changes may require a restart before they take effect.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                    controller.preferencesDialogError?.let { error ->
                        PreferenceInlineError(error)
                    }
                    validationError?.let { error ->
                        Text(
                            error,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
    }

    if (keywordsEditorOpen) {
        StringListEditorDialog(
            title = "Blocked Keywords",
            label = "Keyword or phrase",
            initialValues = draft.search.blockedKeywords,
            helpText = "Blocked words are removed from new search results and spam filtering.",
            onDismiss = { keywordsEditorOpen = false },
            onSave = { values ->
                draft = draft.copy(search = draft.search.copy(blockedKeywords = values))
                keywordsEditorOpen = false
            }
        )
    }

    if (extensionsEditorOpen) {
        StringListEditorDialog(
            title = "Blocked File Extensions",
            label = "Extension",
            initialValues = draft.search.blockedExtensions,
            helpText = "Extensions are normalized with a leading dot and saved in lowercase.",
            defaultValues = controller.defaultBlockedExtensions(),
            normalizeEntry = { value ->
                value.trim()
                    .removePrefix(".")
                    .lowercase(Locale.US)
                    .takeIf { it.isNotBlank() }
                    ?.let { ".$it" }
            },
            onDismiss = { extensionsEditorOpen = false },
            onSave = { values ->
                draft = draft.copy(search = draft.search.copy(blockedExtensions = values))
                extensionsEditorOpen = false
            }
        )
    }

    if (blockedHostsEditorOpen) {
        StringListEditorDialog(
            title = "Blocked Hosts",
            label = "Host, IP, or range",
            initialValues = draft.search.hostFilters.blockedHosts,
            helpText = "Block hosts or IP ranges from search participation. Exact IPs are normalized when the preferences are saved.",
            onDismiss = { blockedHostsEditorOpen = false },
            onSave = { values ->
                draft = draft.copy(
                    search = draft.search.copy(
                        hostFilters = draft.search.hostFilters.copy(blockedHosts = values)
                    )
                )
                blockedHostsEditorOpen = false
            }
        )
    }

    if (allowedHostsEditorOpen) {
        StringListEditorDialog(
            title = "Allowed Hosts",
            label = "Host, IP, or range",
            initialValues = draft.search.hostFilters.allowedHosts,
            helpText = "Allowlist entries override broader host filters for trusted peers or local ranges.",
            onDismiss = { allowedHostsEditorOpen = false },
            onSave = { values ->
                draft = draft.copy(
                    search = draft.search.copy(
                        hostFilters = draft.search.hostFilters.copy(allowedHosts = values)
                    )
                )
                allowedHostsEditorOpen = false
            }
        )
    }

    if (saveFoldersEditorOpen) {
        CategorySaveFoldersDialog(
            controller = controller,
            defaultDirectory = draft.transfers.downloadDirectory,
            initial = draft.transfers.categorySaveDirectories,
            onDismiss = { saveFoldersEditorOpen = false },
            onSave = { directories ->
                controller.preferencesDialogError = null
                draft = draft.copy(transfers = draft.transfers.copy(categorySaveDirectories = directories))
                saveFoldersEditorOpen = false
            }
        )
    }

    if (unsafeSharingEditorOpen) {
        UnsafeSharingRulesDialog(
            initial = draft.library,
            onShowKnownTypes = { category -> controller.showUnsafeSharingKnownTypes(category) },
            onDismiss = { unsafeSharingEditorOpen = false },
            onSave = { updated ->
                draft = draft.copy(library = updated)
                unsafeSharingEditorOpen = false
            }
        )
    }
}

@Composable
private fun PreferenceToggle(
    label: String,
    value: Boolean,
    enabled: Boolean = true,
    supportingText: String? = null,
    onChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(label)
            supportingText?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        FilterChip(
            selected = value,
            enabled = enabled,
            onClick = { onChange(!value) },
            label = { Text(if (value) "On" else "Off") }
        )
    }
}

@Composable
private fun <T> PreferenceDropdownField(
    label: String,
    value: String,
    options: List<Pair<T, String>>,
    enabled: Boolean = true,
    onSelected: (T) -> Unit
) {
    var expanded by remember(label, value, options) { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, fontWeight = FontWeight.Medium)
        Box {
            OutlinedButton(
                onClick = { expanded = true },
                enabled = enabled,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(value, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Icon(Icons.Rounded.SwapVert, contentDescription = null)
                }
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                options.forEach { (option, title) ->
                    DropdownMenuItem(
                        text = { Text(title) },
                        onClick = {
                            expanded = false
                            onSelected(option)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun PreferenceActionRow(
    title: String,
    description: String,
    actions: @Composable () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                Text(title, fontWeight = FontWeight.Medium)
                Text(
                    description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            actions()
        }
    }
}

@Composable
private fun StringListEditorDialog(
    title: String,
    label: String,
    initialValues: List<String>,
    helpText: String,
    defaultValues: List<String>? = null,
    normalizeEntry: (String) -> String? = { value -> value.trim().takeIf { it.isNotEmpty() } },
    onDismiss: () -> Unit,
    onSave: (List<String>) -> Unit
) {
    var values by remember(initialValues) { mutableStateOf(initialValues.distinct()) }
    var entry by remember(initialValues) { mutableStateOf("") }
    val normalizedEntry = normalizeEntry(entry)
    val canAdd = normalizedEntry != null && normalizedEntry !in values
    val desktopDensity = LocalDesktopDensity.current

    ResponsiveDesktopDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = { onSave(values) }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        title = { Text(title) },
        preferredWidth = 760.dp,
        preferredHeight = if (desktopDensity.compactVertical) 520.dp else 600.dp,
        scrollBody = false
    ) {
        Text(
            helpText,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = entry,
                onValueChange = { entry = it },
                label = { Text(label) },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            Button(
                onClick = {
                    normalizedEntry?.let {
                        values = (values + it).distinct()
                        entry = ""
                    }
                },
                enabled = canAdd
            ) {
                Text("Add")
            }
        }
        if (defaultValues != null) {
            TextButton(onClick = { values = defaultValues.distinct() }) {
                Text("Reset to Defaults")
            }
        }
        LazyColumn(
            modifier = Modifier.fillMaxWidth().heightIn(min = 180.dp, max = if (desktopDensity.compactVertical) 220.dp else 280.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(values) { value ->
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(value, modifier = Modifier.weight(1f))
                        TextButton(onClick = { values = values.filterNot { it == value } }) {
                            Text("Remove")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategorySaveFoldersDialog(
    controller: ComposeAppController,
    defaultDirectory: String,
    initial: CategorySaveDirectoriesDraft,
    onDismiss: () -> Unit,
    onSave: (CategorySaveDirectoriesDraft) -> Unit
) {
    var audio by remember(initial) { mutableStateOf(initial.audio) }
    var video by remember(initial) { mutableStateOf(initial.video) }
    var images by remember(initial) { mutableStateOf(initial.images) }
    var documents by remember(initial) { mutableStateOf(initial.documents) }
    var programs by remember(initial) { mutableStateOf(initial.programs) }
    var other by remember(initial) { mutableStateOf(initial.other) }
    var validationMessage by remember(initial) { mutableStateOf<String?>(null) }
    val error = listOf(
        "Audio" to audio,
        "Video" to video,
        "Images" to images,
        "Documents" to documents,
        "Programs" to programs,
        "Other" to other
    ).firstOrNull { (_, value) -> value.trim().isEmpty() }?.let { "${it.first} needs a folder path." }

    val desktopDensity = LocalDesktopDensity.current

    ResponsiveDesktopDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        CategorySaveDirectoriesDraft(
                            audio = audio.trim(),
                            video = video.trim(),
                            images = images.trim(),
                            documents = documents.trim(),
                            programs = programs.trim(),
                            other = other.trim()
                        )
                    )
                },
                enabled = error == null
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        title = { Text("Category Save Folders") },
        preferredWidth = 820.dp,
        preferredHeight = if (desktopDensity.compactVertical) 620.dp else 720.dp
    ) {
        Text(
            "Choose where each category should land when category-specific save folders are enabled.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        validationMessage?.let { PreferenceInlineError(it) }
        CategoryFolderField(Category.AUDIO, "Audio", audio, defaultDirectory, controller, { audio = it }) { validationMessage = it }
        CategoryFolderField(Category.VIDEO, "Video", video, defaultDirectory, controller, { video = it }) { validationMessage = it }
        CategoryFolderField(Category.IMAGE, "Images", images, defaultDirectory, controller, { images = it }) { validationMessage = it }
        CategoryFolderField(Category.DOCUMENT, "Documents", documents, defaultDirectory, controller, { documents = it }) { validationMessage = it }
        CategoryFolderField(Category.PROGRAM, "Programs", programs, defaultDirectory, controller, { programs = it }) { validationMessage = it }
        CategoryFolderField(Category.OTHER, "Other", other, defaultDirectory, controller, { other = it }) { validationMessage = it }
        error?.let {
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun CategoryFolderField(
    category: Category,
    label: String,
    value: String,
    defaultDirectory: String,
    controller: ComposeAppController,
    onChange: (String) -> Unit,
    onValidationError: (String?) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            FileIdentityIcon(icon = controller.categoryIcon(category), modifier = Modifier.size(18.dp))
            Text(label, fontWeight = FontWeight.Medium)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = value,
                onValueChange = {
                    onChange(it)
                    onValidationError(null)
                },
                singleLine = true,
                modifier = Modifier.weight(1f),
                label = { Text("$label folder") }
            )
            OutlinedButton(
                onClick = {
                    controller.choosePreferencesDirectory(
                        currentValue = value.ifBlank { defaultDirectory },
                        title = "Choose $label Folder"
                    )?.let { result ->
                        if (result.accepted) {
                            onChange(result.normalizedPath ?: value)
                            onValidationError(null)
                        } else {
                            onValidationError(result.errorMessage)
                        }
                    }
                }
            ) {
                Text("Browse…")
            }
        }
    }
}

@Composable
private fun UnsafeSharingRulesDialog(
    initial: LibraryPreferencesDraft,
    onShowKnownTypes: (Category) -> Unit,
    onDismiss: () -> Unit,
    onSave: (LibraryPreferencesDraft) -> Unit
) {
    var shareDownloads by remember(initial) { mutableStateOf(initial.shareDownloadedFiles) }
    var partialSharing by remember(initial) { mutableStateOf(initial.allowPartialSharing) }
    var documentSharing by remember(initial) { mutableStateOf(initial.allowDocumentSharing) }
    var programSharing by remember(initial) { mutableStateOf(initial.allowProgramSearchAndShare) }

    val desktopDensity = LocalDesktopDensity.current

    ResponsiveDesktopDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        initial.copy(
                            shareDownloadedFiles = shareDownloads,
                            allowPartialSharing = partialSharing,
                            allowDocumentSharing = documentSharing,
                            allowProgramSearchAndShare = programSharing
                        )
                    )
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        title = { Text("Public Sharing Safety") },
        preferredWidth = 820.dp,
        preferredHeight = if (desktopDensity.compactVertical) 600.dp else 700.dp
    ) {
        Text(
            "These settings control how WireShare shares downloads and publicly shared files.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        PreferenceToggle("Share downloaded files outside shared folders", shareDownloads) {
            shareDownloads = it
        }
        PreferenceToggle("Allow partial file sharing", partialSharing) {
            partialSharing = it
        }
        PreferenceToggle("Allow document sharing on the public network", documentSharing) {
            documentSharing = it
        }
        Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(12.dp)) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(desktopDensity.cardPadding),
                verticalArrangement = Arrangement.spacedBy(desktopDensity.chipGap)
            ) {
                Text(
                    "Documents can include personal or confidential information. Unknown extensions stay in Other until you explicitly classify them.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(onClick = { onShowKnownTypes(Category.DOCUMENT) }) {
                    Text("What are documents?")
                }
            }
        }
        PreferenceToggle("Allow program searching and sharing", programSharing) {
            programSharing = it
        }
        Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(12.dp)) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(desktopDensity.cardPadding),
                verticalArrangement = Arrangement.spacedBy(desktopDensity.chipGap)
            ) {
                Text(
                    "Program files can carry malware or other unsafe payloads. Unknown extensions stay in Other unless they match known program types.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(onClick = { onShowKnownTypes(Category.PROGRAM) }) {
                    Text("What are programs?")
                }
            }
        }
    }
}

@Composable
private fun LanguageDialog(controller: ComposeAppController) {
    val locales = remember(controller.languageDialogVersion) { controller.availableLocales() }
    var selectedLocale by remember(controller.languageDialogVersion) {
        mutableStateOf(controller.currentLocale())
    }
    val desktopDensity = LocalDesktopDensity.current

    ResponsiveDesktopDialog(
        onDismissRequest = { controller.languageDialogOpen = false },
        confirmButton = {
            Button(onClick = { controller.applyLanguage(selectedLocale) }) {
                Text("Apply")
            }
        },
        dismissButton = {
            TextButton(onClick = { controller.languageDialogOpen = false }) {
                Text("Cancel")
            }
        },
        title = { Text("Change Language") },
        preferredWidth = 560.dp,
        preferredHeight = if (desktopDensity.compactVertical) 420.dp else 520.dp,
        scrollBody = false
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth().heightIn(min = 180.dp, max = if (desktopDensity.compactVertical) 240.dp else 340.dp)
        ) {
            items(locales) { locale ->
                val displayName = locale.getDisplayName(locale).replaceFirstChar {
                    if (it.isLowerCase()) it.titlecase(locale) else it.toString()
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedLocale = locale }
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(displayName)
                    if (locale == selectedLocale) {
                        Text("Selected", color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

@Composable
private fun OpenLinkDialog(controller: ComposeAppController) {
    val validationError = controller.openLinkDialogError ?: validateOpenLinkDraft(controller.openLinkText)

    AlertDialog(
        onDismissRequest = { controller.openLinkDialogOpen = false },
        confirmButton = {
            Button(onClick = { controller.submitOpenLink(controller.openLinkText) }, enabled = validationError == null) {
                Text("Open")
            }
        },
        dismissButton = {
            TextButton(onClick = { controller.openLinkDialogOpen = false }) {
                Text("Cancel")
            }
        },
        title = { Text("Open Link") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = controller.openLinkText,
                    onValueChange = {
                        controller.openLinkText = it
                        controller.openLinkDialogError = null
                    },
                    label = { Text("Magnet or torrent URL") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = validationError != null
                )
                validationError?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    )
}

@Composable
private fun AdvancedSearchDialog(controller: ComposeAppController) {
    val draft = controller.advancedSearchDraft
    var categoryMenuExpanded by remember(draft.category) { mutableStateOf(false) }
    val categories = remember { controller.advancedSearchCategories() }
    val fields = remember(draft.category) { controller.advancedSearchFields(draft.category) }
    val hasValues = draft.values.values.any { it.trim().isNotEmpty() }

    AlertDialog(
        onDismissRequest = {
            controller.dismissAdvancedSearchSuggestions()
            controller.advancedSearchDialogOpen = false
        },
        confirmButton = {
            Button(onClick = { controller.submitAdvancedSearch() }) {
                Text("Search")
            }
        },
        dismissButton = {
            TextButton(onClick = {
                controller.dismissAdvancedSearchSuggestions()
                controller.advancedSearchDialogOpen = false
            }) {
                Text("Cancel")
            }
        },
        title = { Text("Advanced Search") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box {
                    OutlinedButton(
                        onClick = { categoryMenuExpanded = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Category: ${friendlyName(draft.category.name)}")
                    }
                    DropdownMenu(
                        expanded = categoryMenuExpanded,
                        onDismissRequest = { categoryMenuExpanded = false }
                    ) {
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(friendlyName(category.name)) },
                                onClick = {
                                    categoryMenuExpanded = false
                                    controller.updateAdvancedSearchCategory(category)
                                }
                            )
                        }
                    }
                }
                fields.forEach { field ->
                    AdvancedSearchFieldInput(
                        controller = controller,
                        field = field,
                        value = draft.values[field.key].orEmpty()
                    )
                }
                Text(
                    "Advanced searches open in new search tabs.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                when {
                    controller.advancedSearchDialogError != null -> Text(
                        controller.advancedSearchDialogError!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                    !hasValues -> {
                        Text(
                            "Fill in at least one field to continue.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    )
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun AdvancedSearchFieldInput(
    controller: ComposeAppController,
    field: AdvancedSearchFieldSpec,
    value: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        OutlinedTextField(
            value = value,
            onValueChange = { controller.updateAdvancedSearchField(field.key, it) },
            label = { Text(field.label) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged {
                    if (it.isFocused) {
                        controller.focusAdvancedSearchField(field.key)
                    }
                }
                .onPreviewKeyEvent { event ->
                    if (event.type != KeyEventType.KeyDown || !controller.hasVisibleAdvancedSearchSuggestions(field.key)) {
                        return@onPreviewKeyEvent false
                    }
                    when (event.key) {
                        Key.DirectionDown -> {
                            controller.moveAdvancedSearchSuggestionSelection(1)
                            true
                        }

                        Key.DirectionUp -> {
                            controller.moveAdvancedSearchSuggestionSelection(-1)
                            true
                        }

                        Key.Enter -> {
                            controller.acceptSelectedAdvancedSearchSuggestion()
                            true
                        }

                        Key.Escape -> {
                            controller.dismissAdvancedSearchSuggestions()
                            true
                        }

                        else -> false
                    }
                }
        )
        if (controller.hasVisibleAdvancedSearchSuggestions(field.key)) {
            AdvancedSearchSuggestionsPopup(controller)
        }
    }
}

@Composable
private fun AdvancedSearchSuggestionsPopup(controller: ComposeAppController) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 4.dp,
        shadowElevation = 8.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Friend Library Matches",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    controller.advancedSearchSuggestions.size.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            controller.advancedSearchSuggestions.forEachIndexed { index, suggestion ->
                AdvancedSearchSuggestionRow(
                    suggestion = suggestion,
                    selected = controller.selectedAdvancedSearchSuggestionIndex == index,
                    onHover = { controller.selectAdvancedSearchSuggestionIndex(index) },
                    onClick = {
                        controller.focusedAdvancedSearchFieldKey?.let { key ->
                            controller.acceptAdvancedSearchSuggestion(key, suggestion)
                        }
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun AdvancedSearchSuggestionRow(
    suggestion: AdvancedSearchSuggestionEntry,
    selected: Boolean,
    onHover: () -> Unit,
    onClick: () -> Unit
) {
    Surface(
        color = if (selected) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.75f)
        } else {
            Color.Transparent
        },
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .onPointerEvent(PointerEventType.Enter) { onHover() }
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 9.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Rounded.AutoAwesome,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    suggestion.value,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    suggestion.subtitle.ifBlank { "Field autocomplete from shared library metadata" },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                "Match",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun FriendLoginDialog(controller: ComposeAppController) {
    val baseDraft = controller.friendLoginDraft ?: return
    val mode = controller.friendLoginDialogMode
    var localDraft by remember(baseDraft) { mutableStateOf(baseDraft) }
    val configs = remember(controller.friendLoginDialogOpen) { controller.friendLoginOptions() }
    var configMenuExpanded by remember { mutableStateOf(false) }
    val validationError = when {
        localDraft.configLabel == "Jabber" && localDraft.serviceName.trim().isEmpty() ->
            "Enter a domain to continue."
        localDraft.username.trim().isEmpty() || localDraft.password.isEmpty() ->
            "Enter a user name and password to continue."
        else -> null
    }
    val displayedError = controller.friendLoginError ?: validationError
    val title = if (mode == FriendLoginDialogMode.SAVE_SETTINGS) {
        "Friends Sign-In Settings"
    } else {
        "Friends Sign In"
    }
    val confirmLabel = if (mode == FriendLoginDialogMode.SAVE_SETTINGS) {
        "Save Sign-In"
    } else if (controller.friendLoginBusy) {
        "Signing in..."
    } else {
        "Sign In"
    }

    AlertDialog(
        onDismissRequest = { controller.friendLoginDialogOpen = false },
        confirmButton = {
            Button(
                onClick = {
                    if (mode == FriendLoginDialogMode.SAVE_SETTINGS) {
                        controller.saveFriendLoginSettings(localDraft)
                    } else {
                        controller.submitFriendLogin(localDraft)
                    }
                },
                enabled = !controller.friendLoginBusy && validationError == null
            ) {
                Text(confirmLabel)
            }
        },
        dismissButton = {
            TextButton(
                onClick = { controller.friendLoginDialogOpen = false },
                enabled = !controller.friendLoginBusy
            ) {
                Text("Cancel")
            }
        },
        title = { Text(title) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    if (mode == FriendLoginDialogMode.SAVE_SETTINGS) {
                        "Save account details for automatic sign-in without starting a live Friends connection right now."
                    } else {
                        "Use the saved backend account configuration to sign in, browse friends, and start chatting."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Box {
                    OutlinedButton(
                        onClick = { configMenuExpanded = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(localDraft.configLabel)
                    }
                    DropdownMenu(
                        expanded = configMenuExpanded,
                        onDismissRequest = { configMenuExpanded = false }
                    ) {
                        configs.forEach { config ->
                            DropdownMenuItem(
                                text = { Text(config.label) },
                                onClick = {
                                    configMenuExpanded = false
                                    controller.updateFriendLoginConfig(config.label)
                                }
                            )
                        }
                    }
                }
                if (localDraft.configLabel == "Jabber") {
                    OutlinedTextField(
                        value = localDraft.serviceName,
                        onValueChange = { localDraft = localDraft.copy(serviceName = it) },
                        label = { Text("Domain") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                OutlinedTextField(
                    value = localDraft.username,
                    onValueChange = { localDraft = localDraft.copy(username = it) },
                    label = { Text(if (localDraft.configLabel == "Gmail") "Email" else "Username") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = localDraft.password,
                    onValueChange = { localDraft = localDraft.copy(password = it) },
                    label = { Text("Password") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                PreferenceToggle("Sign in automatically", localDraft.autoLogin) {
                    localDraft = localDraft.copy(autoLogin = it)
                }
                displayedError?.let { error ->
                    Text(error, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    )
}

@Composable
private fun AddFriendDialog(controller: ComposeAppController) {
    var username by remember(controller.addFriendId) { mutableStateOf(controller.addFriendId) }
    var nickname by remember(controller.addFriendNickname) { mutableStateOf(controller.addFriendNickname) }
    val validationError = when {
        username.trim().isEmpty() -> "Enter a user name to continue."
        else -> null
    }

    AlertDialog(
        onDismissRequest = { controller.addFriendDialogOpen = false },
        confirmButton = {
            Button(
                onClick = {
                    controller.addFriendId = username
                    controller.addFriendNickname = nickname
                    controller.submitAddFriend()
                },
                enabled = validationError == null
            ) {
                Text("Add Friend")
            }
        },
        dismissButton = {
            TextButton(onClick = { controller.addFriendDialogOpen = false }) {
                Text("Cancel")
            }
        },
        title = { Text("Add Friend") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    isError = validationError != null
                )
                OutlinedTextField(
                    value = nickname,
                    onValueChange = { nickname = it },
                    label = { Text("Nickname") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                validationError?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    )
}

@Composable
private fun TextEntryDialog(dialog: TextEntryDialogState, dismiss: () -> Unit) {
    var value by remember(dialog) { mutableStateOf(dialog.initialValue) }
    var submitError by remember(dialog) { mutableStateOf<String?>(null) }
    val validationError = dialog.validator(value)
    val displayedError = submitError ?: validationError

    AlertDialog(
        onDismissRequest = dismiss,
        confirmButton = {
            Button(onClick = {
                val inlineError = dialog.validator(value)
                if (inlineError != null) {
                    submitError = inlineError
                } else {
                    val confirmError = dialog.onConfirm(value)
                    if (confirmError == null) {
                        dismiss()
                    } else {
                        submitError = confirmError
                    }
                }
            }) {
                Text(dialog.confirmLabel)
            }
        },
        dismissButton = {
            TextButton(onClick = dismiss) {
                Text("Cancel")
            }
        },
        title = { Text(dialog.title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = value,
                    onValueChange = {
                        value = it
                        submitError = null
                    },
                    label = { Text(dialog.label) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    isError = displayedError != null
                )
                displayedError?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    )
}

@Composable
private fun ConfirmationDialog(dialog: ConfirmationDialogState, dismiss: () -> Unit) {
    var checkboxChecked by remember(dialog.checkboxLabel, dialog.checkboxInitialChecked) {
        mutableStateOf(dialog.checkboxInitialChecked)
    }
    AlertDialog(
        onDismissRequest = {
            dialog.onDismiss()
            dismiss()
        },
        confirmButton = {
            Button(
                onClick = {
                    dialog.onConfirmWithCheckbox?.invoke(checkboxChecked) ?: dialog.onConfirm()
                }
            ) {
                Text(dialog.confirmLabel)
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                dialog.alternateLabel?.let { alternateLabel ->
                    TextButton(
                        onClick = {
                            dialog.onAlternate?.invoke()
                        }
                    ) {
                        Text(alternateLabel)
                    }
                }
                TextButton(
                    onClick = {
                        dialog.onDismiss()
                        dismiss()
                    }
                ) {
                    Text(dialog.dismissLabel)
                }
            }
        },
        title = { Text(dialog.title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(dialog.message)
                dialog.checkboxLabel?.let { label ->
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { checkboxChecked = !checkboxChecked },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Checkbox(checked = checkboxChecked, onCheckedChange = { checkboxChecked = it })
                        Text(label, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    )
}

@Composable
private fun LibraryDeletionChoiceDialog(
    controller: ComposeAppController,
    dialog: LibraryDeletionChoiceDialogState
) {
    AlertDialog(
        onDismissRequest = { controller.libraryDeletionChoiceDialog = null },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = {
                    controller.libraryDeletionChoiceDialog = null
                    dialog.onRemove()
                }) {
                    Text(dialog.removeLabel)
                }
                Button(onClick = {
                    controller.libraryDeletionChoiceDialog = null
                    dialog.onDelete()
                }) {
                    Text(dialog.deleteLabel)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = { controller.libraryDeletionChoiceDialog = null }) {
                Text("Cancel")
            }
        },
        title = { Text(dialog.title) },
        text = { Text(dialog.message) }
    )
}

@Composable
private fun LibraryFolderImportDialog(
    controller: ComposeAppController,
    dialog: LibraryFolderImportDialogState
) {
    val desktopDensity = LocalDesktopDensity.current

    ResponsiveDesktopDialog(
        onDismissRequest = {
            controller.libraryFolderImportDialog = null
            controller.libraryKnownTypesDialog = null
        },
        confirmButton = {
            Button(onClick = {
                controller.libraryKnownTypesDialog = null
                dialog.confirm()
            }) { Text(dialog.confirmLabel) }
        },
        dismissButton = {
            TextButton(onClick = {
                controller.libraryFolderImportDialog = null
                controller.libraryKnownTypesDialog = null
            }) {
                Text("Cancel")
            }
        },
        title = { Text(dialog.title) },
        preferredWidth = 860.dp,
        preferredHeight = if (desktopDensity.compactVertical) 640.dp else 760.dp
    ) {
        Text(dialog.message)
        TextButton(onClick = { controller.showLibraryKnownTypesDialog() }) {
            Text("Known File Types")
        }
        Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(12.dp)) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(desktopDensity.cardPadding),
                verticalArrangement = Arrangement.spacedBy(desktopDensity.sectionGap)
            ) {
                Text("Choose file types", fontWeight = FontWeight.SemiBold)
                dialog.categoryOptions.forEach { option ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(desktopDensity.chipGap)
                    ) {
                        Checkbox(
                            checked = option.selected,
                            enabled = option.enabled,
                            onCheckedChange = { checked -> option.selected = checked }
                        )
                        Text(
                            option.label,
                            color = if (option.enabled) {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            }
                        )
                    }
                }
                dialog.documentsDisabledMessage?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                dialog.programsDisabledMessage?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(
                    "Unknown extensions stay in Other unless you include them in Advanced.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(12.dp)) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(desktopDensity.cardPadding),
                verticalArrangement = Arrangement.spacedBy(desktopDensity.sectionGap)
            ) {
                Text("From", fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { dialog.recursive = true },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = dialog.recursive, onClick = { dialog.recursive = true })
                    Text(dialog.recursiveLabel)
                }
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { dialog.recursive = false },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = !dialog.recursive, onClick = { dialog.recursive = false })
                    Text(dialog.topLevelLabel)
                }
            }
        }
        Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(12.dp)) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(desktopDensity.cardPadding),
                verticalArrangement = Arrangement.spacedBy(desktopDensity.sectionGap)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Advanced", fontWeight = FontWeight.SemiBold)
                    TextButton(onClick = { dialog.advancedExpanded = !dialog.advancedExpanded }) {
                        Text(if (dialog.advancedExpanded) "Hide" else "Show")
                    }
                }
                if (dialog.advancedExpanded) {
                    Text(
                        "Include files with extra extensions. Use commas, spaces, or dots between entries.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = dialog.advancedExtensions,
                        onValueChange = { dialog.advancedExtensions = it },
                        label = { Text("Extensions") },
                        placeholder = { Text("abc, xyz") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }
        }
    }
}

@Composable
private fun LibraryKnownTypesDialog(
    controller: ComposeAppController,
    dialog: LibraryKnownTypesDialogState
) {
    val desktopDensity = LocalDesktopDensity.current

    ResponsiveDesktopDialog(
        onDismissRequest = { controller.libraryKnownTypesDialog = null },
        confirmButton = {
            TextButton(onClick = { controller.libraryKnownTypesDialog = null }) {
                Text("Close")
            }
        },
        title = { Text(dialog.title) },
        preferredWidth = 720.dp,
        preferredHeight = if (desktopDensity.compactVertical) 480.dp else 560.dp
    ) {
        dialog.groups.forEach { group ->
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(group.label, fontWeight = FontWeight.SemiBold)
                Text(
                    group.extensions.joinToString(", "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(12.dp)) {
            Text(
                dialog.otherMessage,
                modifier = Modifier.fillMaxWidth().padding(desktopDensity.cardPadding),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DocumentSharingWarningDialog(
    controller: ComposeAppController,
    dialog: DocumentSharingWarningDialogState
) {
    AlertDialog(
        onDismissRequest = { dialog.onDismiss() },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { dialog.onUnshareAll() }) {
                    Text(dialog.unshareLabel)
                }
                Button(onClick = { dialog.onContinue() }) {
                    Text(dialog.continueLabel)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = { dialog.onDismiss() }) {
                Text("Cancel")
            }
        },
        title = { Text(dialog.title) },
        text = { Text(dialog.message) }
    )
}

@Composable
private fun SharedCollectionSharingPanel(controller: ComposeAppController) {
    val summaryItems = controller.currentSharedListSharingSummaryItems()
    val editorRows = controller.currentSharedListSharingEditorRows()
    val signedIn = controller.currentSharedListSharingSignedIn()
    val busy = controller.currentSharedListSharingBusy()

    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Sharing", fontWeight = FontWeight.SemiBold)
            when {
                controller.sharedListSharingEditMode && signedIn -> {
                    OutlinedTextField(
                        value = controller.sharedListSharingFilter,
                        onValueChange = { controller.updateCurrentSharedListSharingFilter(it) },
                        label = { Text("Filter friends") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = { controller.selectAllCurrentSharedListSharingRows() }) {
                            Text("Select all")
                        }
                        TextButton(onClick = { controller.clearCurrentSharedListSharingRows() }) {
                            Text("Select none")
                        }
                    }
                    Column(
                        modifier = Modifier.height(220.dp).verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (editorRows.isEmpty()) {
                            Text(
                                "No friends match the current filter.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            editorRows.forEach { row ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Checkbox(
                                        checked = row.selected,
                                        onCheckedChange = {
                                            if (row.id != null) {
                                                controller.toggleCurrentSharedListFriendSelection(row.id)
                                            } else {
                                                controller.setCurrentSharedListUnknownSelection(it)
                                            }
                                        }
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(row.label)
                                        row.detail?.let {
                                            Text(
                                                it,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { controller.applyCurrentSharedListSharingEdit() }, modifier = Modifier.weight(1f)) {
                            Text("Apply")
                        }
                        OutlinedButton(onClick = { controller.cancelCurrentSharedListSharingEdit() }, modifier = Modifier.weight(1f)) {
                            Text("Cancel")
                        }
                    }
                }

                !signedIn -> {
                    Text(
                        if (busy) {
                            "Logging in..."
                        } else {
                            "Sign in to share this collection."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilledTonalButton(onClick = { controller.showFriendLoginDialog() }, enabled = !busy) {
                            Text("Sign In")
                        }
                        if (controller.sharedListFriendIds.isNotEmpty()) {
                            OutlinedButton(onClick = { controller.stopSharingCurrentSharedList() }) {
                                Text("Stop Sharing")
                            }
                        }
                    }
                }

                else -> {
                    if (summaryItems.isEmpty()) {
                        Text(
                            "Not shared with friends yet.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text(
                            "Shared with...",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            summaryItems.forEach { item ->
                                Surface(
                                    color = MaterialTheme.colorScheme.surface,
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(
                                        item.label,
                                        modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 8.dp),
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }
                    OutlinedButton(
                        onClick = { controller.openCurrentSharedListSharingWorkspace() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Rounded.PersonAdd, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text(controller.currentSharedListSharingButtonLabel())
                    }
                }
            }
        }
    }
}

@Composable
private fun AppShell(controller: ComposeAppController) {
    val desktopDensity = LocalDesktopDensity.current
    val density = LocalDensity.current
    val searchFocusRequester = remember { FocusRequester() }
    var shellChromeHeightPx by remember { mutableIntStateOf(0) }
    val overlayTopInset = with(density) { shellChromeHeightPx.toDp() + desktopDensity.overlayGap }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .onPreviewKeyEvent { event ->
                if (event.type == KeyEventType.KeyDown && event.key == Key.F && (event.isMetaPressed || event.isCtrlPressed)) {
                    controller.requestGlobalSearchFocus()
                    true
                } else {
                    false
                }
            }
            .onKeyEvent { event ->
                if (
                    event.type == KeyEventType.KeyUp &&
                    event.key == Key.Spacebar &&
                    !event.isMetaPressed &&
                    !event.isCtrlPressed &&
                    !event.isShiftPressed &&
                    controller.canTogglePlayerFromShortcut()
                ) {
                    controller.togglePlayerPlayback()
                    true
                } else {
                    false
                }
            }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .onGloballyPositioned { coordinates ->
                        shellChromeHeightPx = coordinates.size.height
                    }
            ) {
                ShellHeader(controller, searchFocusRequester)
                SearchTabs(controller)
            }
            if (controller.trayExpanded) {
                VerticalSplitPane(
                    fraction = controller.transferMainAreaFraction,
                    onFractionChange = { controller.updateTransferPaneFraction(it) },
                    onFractionChangeFinished = { controller.updateTransferPaneFraction(it, persist = true) },
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    minTopHeight = 340.dp,
                    minBottomHeight = 200.dp,
                    top = {
                        MainWorkspace(controller)
                    },
                    bottom = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = desktopDensity.shellPadding, vertical = desktopDensity.shellPadding)
                        ) {
                            TransferTray(controller, embedded = false)
                        }
                    }
                )
            } else {
                MainWorkspace(controller)
            }
            StatusBar(controller)
        }
        val activeFriendRequest = controller.activeFriendRequest()
        if (
            (activeFriendRequest != null && controller.currentScreen != ComposeScreen.Friends) ||
            controller.operationNotices.isNotEmpty()
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = overlayTopInset, end = desktopDensity.overlayInset),
                verticalArrangement = Arrangement.spacedBy(desktopDensity.overlayGap),
                horizontalAlignment = Alignment.End
            ) {
                if (activeFriendRequest != null && controller.currentScreen != ComposeScreen.Friends) {
                    FriendRequestOverlay(
                        controller = controller,
                        request = activeFriendRequest
                    )
                }
                OperationNoticeHost(controller)
            }
        }
    }
}

@Composable
private fun MainWorkspace(controller: ComposeAppController) {
    val desktopDensity = LocalDesktopDensity.current
    Row(modifier = Modifier.fillMaxSize()) {
        SideRail(controller)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(horizontal = desktopDensity.shellPadding, vertical = desktopDensity.shellPadding)
        ) {
            MainContent(controller)
        }
    }
}

@Composable
private fun FriendRequestOverlay(
    controller: ComposeAppController,
    request: PendingFriendRequest,
    modifier: Modifier = Modifier
) {
    val desktopDensity = LocalDesktopDensity.current
    Card(
        modifier = modifier.width(if (desktopDensity.compactHorizontal) 300.dp else 320.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(desktopDensity.cardPadding),
            verticalArrangement = Arrangement.spacedBy(desktopDensity.sectionGap)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(desktopDensity.sectionGap),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Rounded.PersonAdd, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("Friend Request", fontWeight = FontWeight.SemiBold)
                    Text(
                        "${request.username} wants to be your friend.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = { controller.openFriendsQuickEntry() }) {
                    Text("Open Friends")
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = { controller.declineFriendRequest(request) }) {
                        Text("No")
                    }
                    FilledTonalButton(onClick = { controller.acceptFriendRequest(request) }) {
                        Text("Yes")
                    }
                }
            }
        }
    }
}

@Composable
private fun OperationNoticeHost(controller: ComposeAppController, modifier: Modifier = Modifier) {
    val desktopDensity = LocalDesktopDensity.current
    Column(
        modifier = modifier.widthIn(max = if (desktopDensity.compactHorizontal) 332.dp else 360.dp),
        verticalArrangement = Arrangement.spacedBy(desktopDensity.overlayGap),
        horizontalAlignment = Alignment.End
    ) {
        controller.operationNotices.forEach { notice ->
            LaunchedEffect(notice.id) {
                delay(
                    when (notice.level) {
                        OperationNoticeLevel.ERROR -> 6500
                        OperationNoticeLevel.WARNING -> 5500
                        OperationNoticeLevel.INFO,
                        OperationNoticeLevel.SUCCESS -> 4200
                    }.toLong()
                )
                controller.dismissOperationNotice(notice.id)
            }
            OperationNoticeCard(
                notice = notice,
                onDismiss = { controller.dismissOperationNotice(notice.id) }
            )
        }
    }
}

@Composable
private fun OperationNoticeCard(notice: OperationNotice, onDismiss: () -> Unit) {
    val desktopDensity = LocalDesktopDensity.current
    val containerColor = when (notice.level) {
        OperationNoticeLevel.INFO -> MaterialTheme.colorScheme.surface
        OperationNoticeLevel.SUCCESS -> MaterialTheme.colorScheme.secondaryContainer
        OperationNoticeLevel.WARNING -> MaterialTheme.colorScheme.tertiaryContainer
        OperationNoticeLevel.ERROR -> MaterialTheme.colorScheme.errorContainer
    }
    val contentColor = when (notice.level) {
        OperationNoticeLevel.INFO -> MaterialTheme.colorScheme.onSurface
        OperationNoticeLevel.SUCCESS -> MaterialTheme.colorScheme.onSecondaryContainer
        OperationNoticeLevel.WARNING -> MaterialTheme.colorScheme.onTertiaryContainer
        OperationNoticeLevel.ERROR -> MaterialTheme.colorScheme.onErrorContainer
    }
    val icon = when (notice.level) {
        OperationNoticeLevel.INFO -> Icons.Rounded.Info
        OperationNoticeLevel.SUCCESS -> Icons.Rounded.Check
        OperationNoticeLevel.WARNING -> Icons.Rounded.Warning
        OperationNoticeLevel.ERROR -> Icons.Rounded.Warning
    }

    Card(
        modifier = Modifier.widthIn(max = if (desktopDensity.compactHorizontal) 332.dp else 360.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor, contentColor = contentColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(desktopDensity.cardPadding),
            horizontalArrangement = Arrangement.spacedBy(desktopDensity.sectionGap),
            verticalAlignment = Alignment.Top
        ) {
            Icon(icon, contentDescription = null, tint = contentColor, modifier = Modifier.padding(top = 2.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(notice.title, fontWeight = FontWeight.SemiBold)
                Text(
                    notice.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = contentColor.copy(alpha = 0.92f)
                )
            }
            IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Rounded.Close, contentDescription = "Dismiss", tint = contentColor)
            }
        }
    }
}

@Composable
private fun InlineStatusBanner(
    title: String,
    message: String,
    level: OperationNoticeLevel,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    val desktopDensity = LocalDesktopDensity.current
    val containerColor = when (level) {
        OperationNoticeLevel.INFO -> MaterialTheme.colorScheme.surfaceVariant
        OperationNoticeLevel.SUCCESS -> MaterialTheme.colorScheme.secondaryContainer
        OperationNoticeLevel.WARNING -> MaterialTheme.colorScheme.tertiaryContainer
        OperationNoticeLevel.ERROR -> MaterialTheme.colorScheme.errorContainer
    }
    val contentColor = when (level) {
        OperationNoticeLevel.INFO -> MaterialTheme.colorScheme.onSurfaceVariant
        OperationNoticeLevel.SUCCESS -> MaterialTheme.colorScheme.onSecondaryContainer
        OperationNoticeLevel.WARNING -> MaterialTheme.colorScheme.onTertiaryContainer
        OperationNoticeLevel.ERROR -> MaterialTheme.colorScheme.onErrorContainer
    }
    val icon = when (level) {
        OperationNoticeLevel.INFO -> Icons.Rounded.Info
        OperationNoticeLevel.SUCCESS -> Icons.Rounded.Check
        OperationNoticeLevel.WARNING,
        OperationNoticeLevel.ERROR -> Icons.Rounded.Warning
    }

    Card(colors = CardDefaults.cardColors(containerColor = containerColor, contentColor = contentColor)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(desktopDensity.cardPadding),
            horizontalArrangement = Arrangement.spacedBy(desktopDensity.sectionGap),
            verticalAlignment = Alignment.Top
        ) {
            Icon(icon, contentDescription = null, tint = contentColor, modifier = Modifier.padding(top = 2.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(title, fontWeight = FontWeight.SemiBold)
                Text(message, style = MaterialTheme.typography.bodySmall)
                if (actionLabel != null && onAction != null) {
                    TextButton(onClick = onAction) {
                        Text(actionLabel)
                    }
                }
            }
        }
    }
}

@Composable
private fun ShellHeader(controller: ComposeAppController, searchFocusRequester: FocusRequester) {
    val desktopDensity = LocalDesktopDensity.current
    val desktopType = LocalDesktopTypeScale.current
    var categoryMenuExpanded by remember { mutableStateOf(false) }
    val categories = remember {
        listOf(
            SearchCategory.ALL,
            SearchCategory.AUDIO,
            SearchCategory.VIDEO,
            SearchCategory.IMAGE,
            SearchCategory.DOCUMENT,
            SearchCategory.PROGRAM,
            SearchCategory.TORRENT
        )
    }

    LaunchedEffect(controller.searchFocusRequestEpoch) {
        if (controller.searchFocusRequestEpoch > 0) {
            searchFocusRequester.requestFocus()
        }
    }

    Surface(tonalElevation = 4.dp, modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(horizontal = desktopDensity.headerPadding, vertical = desktopDensity.headerPadding),
            verticalArrangement = Arrangement.spacedBy(desktopDensity.shellGap)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        "WireShare",
                        style = desktopType.screenTitle
                    )
                    Text(
                        buildShellSummary(controller),
                        style = desktopType.summary,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(desktopDensity.chipGap),
                    verticalArrangement = Arrangement.spacedBy(desktopDensity.chipGap)
                ) {
                    CompactOutlinedButton(onClick = { controller.chooseTorrentFiles() }) {
                        Icon(Icons.Rounded.Folder, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("Torrent")
                    }
                    CompactOutlinedButton(onClick = { controller.showOpenLinkDialog() }) {
                        Icon(Icons.Rounded.Link, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("Link")
                    }
                    CompactFilledTonalButton(onClick = { controller.chooseLibraryFiles() }) {
                        Icon(Icons.AutoMirrored.Rounded.PlaylistAdd, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("Add Files")
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(desktopDensity.chipGap)
            ) {
                Box {
                    CompactOutlinedButton(onClick = { categoryMenuExpanded = true }) {
                        Text(friendlyName(controller.searchCategory.name))
                    }
                    DropdownMenu(
                        expanded = categoryMenuExpanded,
                        onDismissRequest = { categoryMenuExpanded = false }
                    ) {
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(friendlyName(category.name)) },
                                onClick = {
                                    controller.updateSearchCategory(category)
                                    categoryMenuExpanded = false
                                }
                            )
                        }
                    }
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    OutlinedTextField(
                        value = controller.searchQuery,
                        onValueChange = { controller.updateSearchQuery(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(searchFocusRequester)
                            .onFocusChanged { state ->
                                if (state.isFocused) {
                                    controller.showSearchSuggestions()
                                } else if (controller.selectedSearchSuggestionIndex < 0) {
                                    controller.dismissSearchSuggestions()
                                }
                            }
                            .onPreviewKeyEvent { event ->
                                when {
                                    event.type == KeyEventType.KeyDown && event.key == Key.DirectionDown -> {
                                        controller.showSearchSuggestions()
                                        controller.moveSearchSuggestionSelection(1)
                                        true
                                    }

                                    event.type == KeyEventType.KeyDown && event.key == Key.DirectionUp -> {
                                        controller.showSearchSuggestions()
                                        controller.moveSearchSuggestionSelection(-1)
                                        true
                                    }

                                    event.type == KeyEventType.KeyDown && event.key == Key.Escape && controller.hasVisibleSearchSuggestions() -> {
                                        controller.dismissSearchSuggestions()
                                        true
                                    }

                                    event.type == KeyEventType.KeyUp && event.key == Key.Enter -> {
                                        controller.submitSearch()
                                        true
                                    }

                                    else -> false
                                }
                            },
                        singleLine = true,
                        isError = controller.searchValidationError != null,
                        label = { Text("Search") },
                        placeholder = { Text("Artists, files, friends, or keywords") }
                    )
                    if (controller.hasVisibleSearchSuggestions()) {
                        SearchSuggestionsPopup(controller)
                    }
                    controller.searchValidationError?.let {
                        Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
                CompactButton(onClick = { controller.submitSearch() }) {
                    Icon(Icons.Rounded.Search, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Search")
                }
            }
        }
    }
}

private fun buildShellSummary(controller: ComposeAppController): String {
    val activeDownloads = controller.activeDownloadCount()
    val activeUploads = controller.activeUploadCount()
    val parts = mutableListOf(
        "${controller.searchTabs.size} tab${if (controller.searchTabs.size == 1) "" else "s"}",
        "$activeDownloads download${if (activeDownloads == 1) "" else "s"}",
        "$activeUploads upload${if (activeUploads == 1) "" else "s"}",
        friendlyName(controller.connectionStrength().name)
    )
    controller.friendConnectionState?.name?.let { parts += friendlyName(it) }
    if (controller.delayedExitState.pending) {
        parts += controller.delayedExitSummary()
    }
    return parts.joinToString(" • ")
}

private fun buildSearchSummaryLine(
    controller: ComposeAppController,
    tab: SearchTabSession,
    shownCount: Int,
    filterCount: Int,
    selectedCount: Int
): String {
    val parts = mutableListOf(
        searchTypeLabel(tab.searchType),
        controller.searchPresentationCategoryLabel(tab),
        "$shownCount shown",
        "${tab.results.size} total"
    )
    if (filterCount > 0) {
        parts += "$filterCount filter${if (filterCount == 1) "" else "s"}"
    }
    if (selectedCount > 0) {
        parts += "$selectedCount selected"
    }
    return parts.joinToString(" • ")
}

@Composable
private fun SearchSuggestionsPopup(controller: ComposeAppController) {
    val desktopDensity = LocalDesktopDensity.current
    val suggestions = controller.searchSuggestions.toList()
    val historySuggestions = remember(suggestions) { suggestions.filter { it.source == SearchSuggestionSource.HISTORY } }
    val smartSuggestions = remember(suggestions) { suggestions.filter { it.source == SearchSuggestionSource.SMART } }
    val friendSuggestions = remember(suggestions) { suggestions.filter { it.source == SearchSuggestionSource.FRIEND } }

    Surface(
        shape = RoundedCornerShape(LocalDesktopDensity.current.surfaceCorner),
        tonalElevation = 6.dp,
        shadowElevation = 10.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = desktopDensity.chipGap, vertical = desktopDensity.chipGap),
            verticalArrangement = Arrangement.spacedBy(desktopDensity.chipGap)
        ) {
            val sections = listOf(
                "Recent Searches" to historySuggestions,
                "Friend Libraries" to friendSuggestions,
                "Smart Suggestions" to smartSuggestions
            ).filter { it.second.isNotEmpty() }

            sections.forEachIndexed { index, (label, suggestions) ->
                SearchSuggestionSection(
                    controller = controller,
                    label = label,
                    suggestions = suggestions
                )
                if (index < sections.lastIndex) {
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun SearchSuggestionSection(
    controller: ComposeAppController,
    label: String,
    suggestions: List<SearchSuggestionEntry>
) {
    val desktopType = LocalDesktopTypeScale.current
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                label,
                style = desktopType.label,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                suggestions.size.toString(),
                style = desktopType.label,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        suggestions.forEach { suggestion ->
            val index = controller.searchSuggestions.indexOf(suggestion)
            SearchSuggestionRow(
                suggestion = suggestion,
                selected = controller.selectedSearchSuggestionIndex == index,
                onHover = { controller.selectSearchSuggestionIndex(index) },
                onClick = { controller.acceptSearchSuggestion(suggestion) }
            )
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun SearchSuggestionRow(
    suggestion: SearchSuggestionEntry,
    selected: Boolean,
    onHover: () -> Unit,
    onClick: () -> Unit
) {
    val desktopType = LocalDesktopTypeScale.current
    Surface(
        color = if (selected) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.75f)
        } else {
            Color.Transparent
        },
        shape = RoundedCornerShape(LocalDesktopDensity.current.surfaceCorner),
        modifier = Modifier
            .fillMaxWidth()
            .onPointerEvent(PointerEventType.Enter) { onHover() }
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 7.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = when (suggestion.source) {
                    SearchSuggestionSource.HISTORY -> Icons.Rounded.Search
                    SearchSuggestionSource.SMART -> Icons.Rounded.Edit
                    SearchSuggestionSource.FRIEND -> Icons.Rounded.Forum
                },
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    suggestion.title,
                    style = desktopType.body,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Medium
                )
                if (suggestion.subtitle.isNotBlank()) {
                    Text(
                        suggestion.subtitle,
                        style = desktopType.meta,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    when (suggestion.source) {
                        SearchSuggestionSource.HISTORY -> "Recent Search · ${friendlyName(suggestion.category.name)}"
                        SearchSuggestionSource.SMART -> "Fielded Search · ${friendlyName(suggestion.category.name)}"
                        SearchSuggestionSource.FRIEND -> "Browse Friend Library"
                    },
                    style = desktopType.label,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                when (suggestion.source) {
                SearchSuggestionSource.HISTORY -> "History"
                SearchSuggestionSource.SMART -> "Smart"
                SearchSuggestionSource.FRIEND -> "Friend"
            },
                style = desktopType.label,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun SearchTabs(controller: ComposeAppController) {
    if (controller.searchTabs.isEmpty()) {
        return
    }

    val desktopDensity = LocalDesktopDensity.current
    val desktopType = LocalDesktopTypeScale.current
    val activeId = (controller.currentScreen as? ComposeScreen.Search)?.tabId
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = desktopDensity.headerPadding, vertical = desktopDensity.shellGap / 2),
        horizontalArrangement = Arrangement.spacedBy(desktopDensity.chipGap / 2)
    ) {
        controller.searchTabs.forEach { tab ->
            val isActive = tab.id == activeId
            var tabMenuExpanded by remember(tab.id) { mutableStateOf(false) }
            Box(
                modifier = Modifier.onPointerEvent(PointerEventType.Press) { event ->
                    if (event.buttons.isSecondaryPressed) {
                        controller.selectSearchTab(tab.id)
                        tabMenuExpanded = true
                    }
                }
            ) {
                Surface(
                    shape = RoundedCornerShape(desktopDensity.tabCorner),
                    tonalElevation = if (isActive) 3.dp else 0.dp,
                    color = if (isActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                    modifier = Modifier.clickable { controller.selectSearchTab(tab.id) }
                ) {
                    Row(
                        modifier = Modifier
                            .width(desktopDensity.tabWidth)
                            .defaultMinSize(minHeight = desktopDensity.controlHeight + 8.dp)
                            .padding(
                                start = desktopDensity.cardPadding,
                                end = 2.dp,
                                top = 5.dp,
                                bottom = 5.dp
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                tab.title,
                                style = desktopType.body,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Medium
                            )
                            Text(
                                listOf(tab.descriptor, tab.subtitle)
                                    .filter { it.isNotBlank() }
                                    .distinct()
                                    .joinToString(" • "),
                                style = desktopType.meta,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        CompactIconButton(onClick = { controller.closeSearchTab(tab.id) }) {
                            Icon(Icons.Rounded.Close, contentDescription = "Close")
                        }
                    }
                }
                DropdownMenu(expanded = tabMenuExpanded, onDismissRequest = { tabMenuExpanded = false }) {
                    DropdownMenuItem(
                        text = { Text("Close") },
                        onClick = {
                            tabMenuExpanded = false
                            controller.closeSearchTab(tab.id)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Close Others") },
                        enabled = controller.searchTabs.size > 1,
                        onClick = {
                            tabMenuExpanded = false
                            controller.closeOtherSearchTabs(tab.id)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Close All Search Tabs") },
                        enabled = controller.searchTabs.isNotEmpty(),
                        onClick = {
                            tabMenuExpanded = false
                            controller.closeAllSearchTabs()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SideRail(controller: ComposeAppController) {
    val desktopDensity = LocalDesktopDensity.current
    val desktopType = LocalDesktopTypeScale.current

    Surface(tonalElevation = 1.dp) {
        Column(
            modifier = Modifier
                .width(desktopDensity.railWidth)
                .fillMaxHeight()
                .padding(vertical = desktopDensity.shellPadding),
            verticalArrangement = Arrangement.spacedBy(desktopDensity.chipGap),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SideRailItem(
                selected = controller.currentScreen is ComposeScreen.Library,
                icon = Icons.Rounded.Folder,
                label = "My Files",
                onClick = { controller.selectLibrary() },
                desktopDensity = desktopDensity,
                desktopType = desktopType
            )
            SideRailItem(
                selected = controller.currentScreen is ComposeScreen.Transfers,
                icon = Icons.Rounded.SwapVert,
                label = "Transfers",
                onClick = { controller.selectTransfers() },
                desktopDensity = desktopDensity,
                desktopType = desktopType
            )
            SideRailItem(
                selected = controller.currentScreen is ComposeScreen.Friends,
                icon = Icons.Rounded.Forum,
                label = "Friends",
                onClick = { controller.selectFriends() },
                desktopDensity = desktopDensity,
                desktopType = desktopType
            )
            SideRailItem(
                selected = controller.currentScreen is ComposeScreen.Player,
                icon = Icons.Rounded.LibraryMusic,
                label = "Player",
                onClick = { controller.selectPlayer() },
                desktopDensity = desktopDensity,
                desktopType = desktopType
            )
        }
    }
}

@Composable
private fun SideRailItem(
    selected: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    desktopDensity: DesktopDensity,
    desktopType: DesktopTypeScale
) {
    Surface(
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
        shape = RoundedCornerShape(desktopDensity.railItemCorner),
        tonalElevation = if (selected) 2.dp else 0.dp,
        modifier = Modifier
            .width(desktopDensity.railWidth - 10.dp)
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = desktopDensity.railItemPadding),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(desktopDensity.railIconSize)
            )
            Text(
                label,
                style = desktopType.label,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun MainContent(controller: ComposeAppController) {
    when (val screen = controller.currentScreen) {
        ComposeScreen.Library -> LibraryScreen(controller)
        ComposeScreen.Transfers -> TransfersScreen(controller)
        ComposeScreen.Friends -> FriendsScreen(controller)
        ComposeScreen.Player -> PlayerScreen(controller)
        is ComposeScreen.Search -> {
            val tab = controller.searchTabs.firstOrNull { it.id == screen.tabId }
            if (tab == null) {
                controller.selectLibrary()
            } else {
                SearchScreen(controller, tab)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalComposeUiApi::class)
@Composable
private fun LibraryScreen(controller: ComposeAppController) {
    val desktopDensity = LocalDesktopDensity.current
    val desktopType = LocalDesktopTypeScale.current
    val playerState = controller.playerState
    val currentFile = controller.playerCurrentFile
    val activeSection = controller.activeLibrarySection()
    val visibleItems = controller.visibleLibraryItems()
    val availableLibraryColumns = controller.availableLibraryColumns()
    val visibleLibraryColumns = controller.visibleLibraryColumns.intersect(availableLibraryColumns.toSet())
        .ifEmpty { setOf(LibraryColumn.NAME) }
    val availableLibrarySortModes = controller.availableLibrarySortModes()
    var activeSectionDropTargetActive by remember(controller.selectedLibrarySectionId) { mutableStateOf(false) }
    val selectedItems = controller.selectedLibraryItems()
    val selectedItem = controller.selectedLibraryItem()
    val categories = remember { listOf<Category?>(null) + Category.values().toList() }
    val tableFocusRequester = remember { FocusRequester() }

    LaunchedEffect(
        controller.selectedLibrarySectionId,
        visibleItems.size,
        controller.selectedLibraryItemPath,
        controller.libraryFilterText,
        controller.libraryCategoryFilter,
        controller.librarySortMode,
        controller.librarySortDescending
    ) {
        when {
            visibleItems.isEmpty() -> controller.clearLibrarySelection()
        }
    }

    HorizontalSplitPane(
        fraction = controller.libraryNavigatorPaneFraction,
        onFractionChange = { controller.updateLibraryPaneFraction(it) },
        onFractionChangeFinished = { controller.updateLibraryPaneFraction(it, persist = true) },
        modifier = Modifier.fillMaxSize(),
        minStartWidth = if (desktopDensity.compactHorizontal) 220.dp else 250.dp,
        minEndWidth = if (desktopDensity.compactHorizontal) 360.dp else 420.dp,
        start = {
        Card(
            modifier = Modifier.fillMaxSize(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(desktopDensity.cardPadding),
                verticalArrangement = Arrangement.spacedBy(desktopDensity.sectionGap)
            ) {
                Text("Library", style = desktopType.screenTitle)
                Text(
                    "${controller.librarySections.size} sections",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = desktopType.summary
                )
                HorizontalDivider()
                controller.librarySections.forEach { section ->
                    var menuExpanded by remember(section.id) { mutableStateOf(false) }
                    var dropTargetActive by remember(section.id) { mutableStateOf(false) }
                    val selected = controller.selectedLibrarySectionId == section.id
                    val librarySectionDropTarget = remember(section.id) {
                        object : DragAndDropTarget {
                            override fun onDrop(event: DragAndDropEvent): Boolean {
                                dropTargetActive = false
                                val payload = extractComposeDropPayload(event) as? ComposeDropPayload.Files ?: return false
                                if (!controller.canImportFilesIntoLibrarySection(section.id, payload.files, payload.sourceSectionId)) {
                                    return false
                                }
                                controller.importFilesIntoLibrarySection(section.id, payload.files, payload.sourceSectionId)
                                tableFocusRequester.requestFocus()
                                return true
                            }

                            override fun onEntered(event: DragAndDropEvent) {
                                val payload = extractComposeDropPayload(event) as? ComposeDropPayload.Files
                                dropTargetActive = payload != null && controller.canImportFilesIntoLibrarySection(
                                    section.id,
                                    payload.files,
                                    payload.sourceSectionId
                                )
                            }

                            override fun onExited(event: DragAndDropEvent) {
                                dropTargetActive = false
                            }

                            override fun onEnded(event: DragAndDropEvent) {
                                dropTargetActive = false
                            }
                        }
                    }
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .dragAndDropTarget(
                                shouldStartDragAndDrop = { event ->
                                    val payload = extractComposeDropPayload(event) as? ComposeDropPayload.Files
                                    payload != null && controller.canImportFilesIntoLibrarySection(
                                        section.id,
                                        payload.files,
                                        payload.sourceSectionId
                                    )
                                },
                                target = librarySectionDropTarget
                            )
                            .onPointerEvent(PointerEventType.Press) { event ->
                                if (event.buttons.isSecondaryPressed) {
                                    controller.selectLibrarySection(section.id)
                                    if (section.isShared) {
                                        menuExpanded = true
                                    }
                                }
                            },
                        shape = RoundedCornerShape(12.dp),
                        tonalElevation = if (selected) 3.dp else 0.dp,
                        color = when {
                            dropTargetActive -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.75f)
                            selected -> MaterialTheme.colorScheme.primaryContainer
                            else -> Color.Transparent
                        }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    controller.selectLibrarySection(section.id)
                                    tableFocusRequester.requestFocus()
                                }
                                .padding(
                                    horizontal = desktopDensity.cardPadding,
                                    vertical = if (desktopDensity.compactVertical) 8.dp else 10.dp
                                ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(section.title, fontWeight = FontWeight.Medium)
                                Text(
                                    if (dropTargetActive) {
                                        if (section.isShared) {
                                            "Drop files here to add them to this collection."
                                        } else {
                                            "Drop files or folders here to add them to My Files."
                                        }
                                    } else {
                                        librarySectionSummary(section)
                                    },
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            if (section.isShared) {
                                Box {
                                    IconButton(onClick = { menuExpanded = true }) {
                                        Icon(Icons.Rounded.MoreVert, contentDescription = "Collection actions")
                                    }
                                    DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                                        DropdownMenuItem(
                                            text = { Text("Open") },
                                            onClick = {
                                                menuExpanded = false
                                                controller.selectLibrarySection(section.id)
                                                tableFocusRequester.requestFocus()
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Add Files…") },
                                            onClick = {
                                                menuExpanded = false
                                                controller.selectLibrarySection(section.id)
                                                controller.addFilesToCurrentLibrarySection()
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Import Collection…") },
                                            onClick = {
                                                menuExpanded = false
                                                controller.selectLibrarySection(section.id)
                                                controller.importCurrentLibrarySection()
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Export Collection…") },
                                            onClick = {
                                                menuExpanded = false
                                                controller.selectLibrarySection(section.id)
                                                controller.exportCurrentLibrarySection()
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Clear") },
                                            onClick = {
                                                menuExpanded = false
                                                controller.selectLibrarySection(section.id)
                                                controller.clearCurrentLibrarySection()
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Rename") },
                                            enabled = if (section.id == controller.selectedLibrarySectionId) {
                                                controller.canRenameCurrentSharedList()
                                            } else {
                                                true
                                            },
                                            onClick = {
                                                menuExpanded = false
                                                controller.selectLibrarySection(section.id)
                                                controller.showRenameSharedListDialog()
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Edit Sharing") },
                                            onClick = {
                                                menuExpanded = false
                                                controller.selectLibrarySection(section.id)
                                                controller.openCurrentSharedListSharingWorkspace()
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Delete") },
                                            onClick = {
                                                menuExpanded = false
                                                controller.selectLibrarySection(section.id)
                                                controller.confirmDeleteSharedList()
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                CompactFilledTonalButton(onClick = { controller.showCreateSharedListDialog() }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.AutoMirrored.Rounded.PlaylistAdd, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("New Collection")
                }
                if (activeSection?.isShared == true) {
                    Text(
                        "Visibility: ${if (activeSection.isPublic) "Public" else "Friends Only"} • Shared with ${controller.sharedListFriendIds.size} account${if (controller.sharedListFriendIds.size == 1) "" else "s"}",
                        style = desktopType.summary,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(desktopDensity.chipGap),
                        verticalArrangement = Arrangement.spacedBy(desktopDensity.chipGap)
                    ) {
                        CompactFilledTonalButton(onClick = { controller.addFilesToCurrentLibrarySection() }) {
                            Text("Add Files…")
                        }
                        CompactOutlinedButton(onClick = { controller.importCurrentLibrarySection() }) {
                            Text("Import…")
                        }
                        CompactOutlinedButton(onClick = { controller.exportCurrentLibrarySection() }) {
                            Text("Export…")
                        }
                        CompactOutlinedButton(onClick = { controller.clearCurrentLibrarySection() }) {
                            Text("Clear")
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CompactOutlinedButton(
                            onClick = { controller.showRenameSharedListDialog() },
                            enabled = controller.canRenameCurrentSharedList(),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Rounded.Edit, contentDescription = null)
                            Spacer(Modifier.width(6.dp))
                            Text("Rename")
                        }
                        CompactOutlinedButton(
                            onClick = { controller.confirmDeleteSharedList() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Rounded.Delete, contentDescription = null)
                            Spacer(Modifier.width(6.dp))
                            Text("Delete")
                        }
                    }
                    SharedCollectionSharingPanel(controller)
                } else {
                    Text(
                        "My Files shows your full library. Shared collections stay here for quick access.",
                        style = desktopType.summary,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        },
        end = {
        val activeLibraryDropTarget = remember(controller.selectedLibrarySectionId) {
            object : DragAndDropTarget {
                override fun onDrop(event: DragAndDropEvent): Boolean {
                    activeSectionDropTargetActive = false
                    val payload = extractComposeDropPayload(event) as? ComposeDropPayload.Files ?: return false
                    if (!controller.canImportFilesIntoLibrarySection(
                            controller.selectedLibrarySectionId,
                            payload.files,
                            payload.sourceSectionId
                        )
                    ) {
                        return false
                    }
                    controller.importFilesIntoLibrarySection(
                        controller.selectedLibrarySectionId,
                        payload.files,
                        payload.sourceSectionId
                    )
                    tableFocusRequester.requestFocus()
                    return true
                }

                override fun onEntered(event: DragAndDropEvent) {
                    val payload = extractComposeDropPayload(event) as? ComposeDropPayload.Files
                    activeSectionDropTargetActive = payload != null && controller.canImportFilesIntoLibrarySection(
                        controller.selectedLibrarySectionId,
                        payload.files,
                        payload.sourceSectionId
                    )
                }

                override fun onExited(event: DragAndDropEvent) {
                    activeSectionDropTargetActive = false
                }

                override fun onEnded(event: DragAndDropEvent) {
                    activeSectionDropTargetActive = false
                }
            }
        }
	        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(desktopDensity.sectionGap)) {
            Card(
                modifier = Modifier.dragAndDropTarget(
                    shouldStartDragAndDrop = { event ->
                        val payload = extractComposeDropPayload(event) as? ComposeDropPayload.Files
                        payload != null && controller.canImportFilesIntoLibrarySection(
                            controller.selectedLibrarySectionId,
                            payload.files,
                            payload.sourceSectionId
                        )
                    },
                    target = activeLibraryDropTarget
                ),
                colors = CardDefaults.cardColors(
                    containerColor = if (activeSectionDropTargetActive) {
                        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f)
                    } else {
                        MaterialTheme.colorScheme.surface
                    }
                )
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(desktopDensity.cardPadding),
                    verticalArrangement = Arrangement.spacedBy(desktopDensity.sectionGap)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                activeSection?.title ?: "My Files",
                                style = desktopType.screenTitle
                            )
                            Text(
                                "${visibleItems.size} items in view · ${librarySectionSummary(activeSection)}",
                                style = desktopType.summary,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(desktopDensity.chipGap),
                            verticalArrangement = Arrangement.spacedBy(desktopDensity.chipGap)
                        ) {
                            FilterChip(
                                selected = controller.libraryFiltersVisible,
                                onClick = { controller.toggleLibraryFiltersVisible() },
                                label = { Text("Filter") }
                            )
                            ColumnVisibilityMenu(
                                entries = availableLibraryColumns.map { column ->
                                    ColumnToggleEntry(
                                        label = libraryColumnLabel(controller.libraryCategoryFilter, column),
                                        visible = column in visibleLibraryColumns
                                    ) {
                                        controller.toggleLibraryColumn(column)
                                    }
                                }
                            )
                            CompactFilledTonalButton(onClick = { controller.addFilesToCurrentLibrarySection() }) {
                                Icon(Icons.Rounded.Folder, contentDescription = null)
                                Spacer(Modifier.width(6.dp))
                                Text(if (activeSection?.isShared == true) "Add Files" else "Add to Library")
                            }
                        }
                    }
                    if (controller.shouldShowLibrarySharingCoachmark()) {
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(
                                    horizontal = desktopDensity.cardPadding,
                                    vertical = if (desktopDensity.compactVertical) 8.dp else 10.dp
                                ),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Text("Share with friends using Private Shared", fontWeight = FontWeight.SemiBold)
                                    Text(
                                        "Keep My Files private, then move files into Private Shared when you want to share them with friends.",
                                        style = desktopType.meta,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(desktopDensity.chipGap),
                                    verticalArrangement = Arrangement.spacedBy(desktopDensity.chipGap)
                                ) {
                                    CompactFilledTonalButton(onClick = { controller.openPrivateSharingCollectionFromCoachmark() }) {
                                        Text("Open Private Shared")
                                    }
                                    CompactTextButton(onClick = { controller.dismissLibrarySharingCoachmark() }) {
                                        Text("Dismiss")
                                    }
                                }
                            }
                        }
                    }
                    if (controller.shouldShowCollectionSharingCoachmark()) {
                        Surface(
                            color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.38f),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(
                                    horizontal = desktopDensity.cardPadding,
                                    vertical = if (desktopDensity.compactVertical) 8.dp else 10.dp
                                ),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Text("Share this collection with friends", fontWeight = FontWeight.SemiBold)
                                    Text(
                                        "Choose who can see this collection, then add the files you want to share.",
                                        style = desktopType.meta,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(desktopDensity.chipGap),
                                    verticalArrangement = Arrangement.spacedBy(desktopDensity.chipGap)
                                ) {
                                    CompactFilledTonalButton(onClick = { controller.shareCurrentCollectionFromCoachmark() }) {
                                        Text("Share this collection")
                                    }
                                    CompactOutlinedButton(onClick = { controller.addFilesToCurrentCollectionFromCoachmark() }) {
                                        Text("Add files")
                                    }
                                    CompactTextButton(onClick = { controller.dismissCollectionSharingCoachmark() }) {
                                        Text("Dismiss")
                                    }
                                }
                            }
                        }
                    }
                    if (activeSection?.isPublic == true) {
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(
                                    horizontal = desktopDensity.cardPadding,
                                    vertical = if (desktopDensity.compactVertical) 8.dp else 10.dp
                                ),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text("Files in this collection are shared anonymously with everyone.", fontWeight = FontWeight.SemiBold)
                                    controller.publicSharedUpgradeMessage()?.let { message ->
                                        Text(
                                            message,
                                            style = desktopType.meta,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Text(
                                        "Review the safety guidance if this collection contains documents, partial files, or anything you do not want to share widely.",
                                        style = desktopType.meta,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                CompactTextButton(onClick = { controller.reviewPublicSharingSafety() }) {
                                    Text("Review Safety")
                                }
                            }
                        }
                    }
                    if (controller.libraryFiltersVisible) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(desktopDensity.chipGap),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = controller.libraryFilterText,
                                onValueChange = { controller.updateLibraryFilterText(it) },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                label = { Text("Filter files") },
                                placeholder = { Text("Name, path, or category") },
                                trailingIcon = {
                                    if (controller.libraryFilterText.isNotEmpty()) {
                                        IconButton(onClick = { controller.updateLibraryFilterText("") }) {
                                            Icon(Icons.Rounded.Close, contentDescription = "Clear filter")
                                        }
                                    }
                                }
                            )
                            if (controller.libraryFilterText.isNotEmpty() || controller.libraryCategoryFilter != null) {
                                CompactOutlinedButton(onClick = { controller.clearLibraryFilters() }) {
                                    Text("Clear Filters")
                                }
                            }
                        }
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(desktopDensity.chipGap),
                            verticalArrangement = Arrangement.spacedBy(desktopDensity.chipGap)
                        ) {
                            categories.forEach { category ->
                                val selected = controller.libraryCategoryFilter == category
                                FilterChip(
                                    selected = selected,
                                    onClick = { controller.selectLibraryCategory(category) },
                                    label = { Text(category?.getPluralName() ?: "All") }
                                )
                            }
                        }
                    }
                    if (selectedItems.isNotEmpty()) {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(desktopDensity.cardPadding),
                                verticalArrangement = Arrangement.spacedBy(desktopDensity.chipGap)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (selectedItems.size == 1 && selectedItem != null) {
                                        val identity = controller.libraryItemIdentity(selectedItem)
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                FileIdentityIcon(
                                                    icon = identity.icon,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(identity.title, fontWeight = FontWeight.SemiBold)
                                                    Text(
                                                        "${selectedItem.category.getSingularName()} · ${formatBytes(selectedItem.size)} · ${buildLibraryItemMeta(identity.subtitle, selectedItem, currentFile == selectedItem.file && playerState in setOf(PlayerState.PLAYING, PlayerState.SEEKING_PLAY, PlayerState.PAUSED))}",
                                                        style = desktopType.summary,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }
                                        }
                                    } else {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(selectedSummaryLabel(selectedItems.size, "file"), fontWeight = FontWeight.SemiBold)
                                            Text(
                                                "Bulk actions apply to the entire selected set in this section.",
                                                style = desktopType.summary,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    Text(
                                        compactSummary("Selected" to selectedItems.size.toString()),
                                        style = desktopType.summary,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(desktopDensity.chipGap),
                                    verticalArrangement = Arrangement.spacedBy(desktopDensity.chipGap)
                                ) {
                                    CompactFilledTonalButton(onClick = { controller.openSelectedLibraryItems() }) {
                                        Text(
                                            if (controller.selectedLibraryItemsPlayInPlayer()) {
                                                if (selectedItems.size > 1) "Play Queue" else "Play"
                                            } else {
                                                "Open"
                                            }
                                        )
                                    }
                                    if (selectedItems.size == 1 && selectedItem != null) {
                                        CompactOutlinedButton(onClick = { controller.showLibraryFileInfo(selectedItem) }) {
                                            Text("File Info")
                                        }
                                        if (!selectedItem.isIncomplete) {
                                            CompactOutlinedButton(onClick = { controller.showRenameLibraryFileDialog(selectedItem) }) {
                                                Text("Rename")
                                            }
                                        }
                                        val jumpTargets = controller.libraryListTargetsForItem(selectedItem)
                                        if (jumpTargets.isNotEmpty()) {
                                            LibraryJumpMenuButton(
                                                label = if (jumpTargets.size == 1) showInTargetLabel(jumpTargets.first()) else "Show In",
                                                targets = jumpTargets,
                                                onJump = { controller.showLibraryItemInTarget(selectedItem, it) }
                                            )
                                        }
                                    }
                                    CompactOutlinedButton(onClick = { controller.revealSelectedLibraryItems() }) {
                                        Text("Reveal")
                                    }
                                    if (controller.canAddSelectedLibraryItemsToCollection()) {
                                        CollectionMenuButton(
                                            label = "Add to Collection",
                                            collections = controller.availableCollections(),
                                            onAddToCollection = controller::addSelectedLibraryItemsToCollection
                                        )
                                    }
                                    CompactOutlinedButton(onClick = { controller.confirmRemoveSelectedLibraryItems() }) {
                                        Text(controller.currentLibrarySectionRemoveLabel())
                                    }
                                    if (activeSection?.isShared == true) {
                                        CompactOutlinedButton(onClick = { controller.confirmRemoveSelectedLibraryItemsFromLibrary() }) {
                                            Text("Remove from Library")
                                        }
                                    }
                                    if (controller.canRemoveSelectedLibraryItemsFromAllOtherLists()) {
                                        CompactTextButton(onClick = { controller.confirmRemoveSelectedLibraryItemsFromAllOtherLists() }) {
                                            Text("Remove from All Other Collections")
                                        }
                                    }
                                    CompactTextButton(onClick = { controller.confirmDeleteSelectedLibraryItems() }) {
                                        Text("Delete from Disk")
                                    }
                                    TextButton(onClick = { controller.clearLibrarySelection() }) {
                                        Text("Clear Selection")
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .focusRequester(tableFocusRequester)
                    .focusable()
                    .onPreviewKeyEvent { event ->
                        handleTableKeyEvent(
                            event = event,
                            moveSelection = controller::moveLibrarySelection,
                            extendSelection = controller::extendLibrarySelection,
                            selectAll = controller::selectAllVisibleLibraryItems,
                            activateSelection = controller::activateSelectedLibraryItem,
                            deleteSelection = controller::handleLibraryDeletionShortcut
                        )
                    },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    HeaderContextMenuArea(
                        menuContent = {
                            availableLibrarySortModes.forEach { mode ->
                                DropdownMenuItem(
                                    text = { Text("Sort by ${libraryColumnLabel(controller.libraryCategoryFilter, mode.asLibraryColumn())}") },
                                    onClick = {
                                        controller.toggleLibrarySort(mode)
                                        dismiss()
                                    }
                                )
                            }
                            HorizontalDivider()
                            availableLibraryColumns.forEach { column ->
                                DropdownMenuItem(
                                    text = {
                                        Text("${if (column in visibleLibraryColumns) "Hide" else "Show"} ${libraryColumnLabel(controller.libraryCategoryFilter, column)}")
                                    },
                                    onClick = {
                                        controller.toggleLibraryColumn(column)
                                        dismiss()
                                    }
                                )
                            }
                        }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = visibleItems.isNotEmpty() && visibleItems.all { it.file.absolutePath in controller.selectedLibraryItemPaths },
                                onCheckedChange = { checked ->
                                    if (checked) {
                                        controller.selectAllVisibleLibraryItems()
                                    } else {
                                        controller.clearLibrarySelection()
                                    }
                                }
                            )
                            if (LibraryColumn.NAME in visibleLibraryColumns) {
                                SortableHeaderText(
                                    label = "Name",
                                    active = controller.librarySortMode == LibrarySortMode.NAME,
                                    descending = controller.librarySortDescending,
                                    modifier = Modifier.weight(1.4f)
                                ) {
                                    controller.toggleLibrarySort(LibrarySortMode.NAME)
                                }
                            }
                            if (LibraryColumn.TYPE in visibleLibraryColumns) {
                                SortableHeaderText(
                                    label = "Type",
                                    active = controller.librarySortMode == LibrarySortMode.TYPE,
                                    descending = controller.librarySortDescending,
                                    modifier = Modifier.width(96.dp)
                                ) {
                                    controller.toggleLibrarySort(LibrarySortMode.TYPE)
                                }
                            }
                            if (LibraryColumn.FILENAME in visibleLibraryColumns) {
                                SortableHeaderText(
                                    label = "Filename",
                                    active = controller.librarySortMode == LibrarySortMode.FILENAME,
                                    descending = controller.librarySortDescending,
                                    modifier = Modifier.width(172.dp)
                                ) {
                                    controller.toggleLibrarySort(LibrarySortMode.FILENAME)
                                }
                            }
                            if (LibraryColumn.EXTENSION in visibleLibraryColumns) {
                                SortableHeaderText(
                                    label = "Extension",
                                    active = controller.librarySortMode == LibrarySortMode.EXTENSION,
                                    descending = controller.librarySortDescending,
                                    modifier = Modifier.width(92.dp)
                                ) {
                                    controller.toggleLibrarySort(LibrarySortMode.EXTENSION)
                                }
                            }
                            if (LibraryColumn.SIZE in visibleLibraryColumns) {
                                SortableHeaderText(
                                    label = "Size",
                                    active = controller.librarySortMode == LibrarySortMode.SIZE,
                                    descending = controller.librarySortDescending,
                                    modifier = Modifier.width(92.dp)
                                ) {
                                    controller.toggleLibrarySort(LibrarySortMode.SIZE)
                                }
                            }
                            if (LibraryColumn.ACTIVITY in visibleLibraryColumns) {
                                SortableHeaderText(
                                    label = "Activity",
                                    active = controller.librarySortMode == LibrarySortMode.ACTIVITY,
                                    descending = controller.librarySortDescending,
                                    modifier = Modifier.width(126.dp)
                                ) {
                                    controller.toggleLibrarySort(LibrarySortMode.ACTIVITY)
                                }
                            }
                            if (LibraryColumn.HITS in visibleLibraryColumns) {
                                SortableHeaderText(
                                    label = "Hits",
                                    active = controller.librarySortMode == LibrarySortMode.HITS,
                                    descending = controller.librarySortDescending,
                                    modifier = Modifier.width(72.dp)
                                ) {
                                    controller.toggleLibrarySort(LibrarySortMode.HITS)
                                }
                            }
                            if (LibraryColumn.UPLOADS in visibleLibraryColumns) {
                                SortableHeaderText(
                                    label = "Uploads",
                                    active = controller.librarySortMode == LibrarySortMode.UPLOADS,
                                    descending = controller.librarySortDescending,
                                    modifier = Modifier.width(78.dp)
                                ) {
                                    controller.toggleLibrarySort(LibrarySortMode.UPLOADS)
                                }
                            }
                            if (LibraryColumn.UPLOAD_ATTEMPTS in visibleLibraryColumns) {
                                SortableHeaderText(
                                    label = "Upload Attempts",
                                    active = controller.librarySortMode == LibrarySortMode.UPLOAD_ATTEMPTS,
                                    descending = controller.librarySortDescending,
                                    modifier = Modifier.width(132.dp)
                                ) {
                                    controller.toggleLibrarySort(LibrarySortMode.UPLOAD_ATTEMPTS)
                                }
                            }
                            if (LibraryColumn.UPDATED in visibleLibraryColumns) {
                                SortableHeaderText(
                                    label = "Updated",
                                    active = controller.librarySortMode == LibrarySortMode.UPDATED,
                                    descending = controller.librarySortDescending,
                                    modifier = Modifier.width(108.dp)
                                ) {
                                    controller.toggleLibrarySort(LibrarySortMode.UPDATED)
                                }
                            }
                            if (LibraryColumn.LOCATION in visibleLibraryColumns) {
                                SortableHeaderText(
                                    label = "Location",
                                    active = controller.librarySortMode == LibrarySortMode.LOCATION,
                                    descending = controller.librarySortDescending,
                                    modifier = Modifier.width(180.dp)
                                ) {
                                    controller.toggleLibrarySort(LibrarySortMode.LOCATION)
                                }
                            }
                            if (LibraryColumn.LENGTH in visibleLibraryColumns) {
                                SortableHeaderText(
                                    label = "Length",
                                    active = controller.librarySortMode == LibrarySortMode.LENGTH,
                                    descending = controller.librarySortDescending,
                                    modifier = Modifier.width(84.dp)
                                ) {
                                    controller.toggleLibrarySort(LibrarySortMode.LENGTH)
                                }
                            }
                            if (LibraryColumn.BITRATE in visibleLibraryColumns) {
                                SortableHeaderText(
                                    label = "Bitrate",
                                    active = controller.librarySortMode == LibrarySortMode.BITRATE,
                                    descending = controller.librarySortDescending,
                                    modifier = Modifier.width(86.dp)
                                ) {
                                    controller.toggleLibrarySort(LibrarySortMode.BITRATE)
                                }
                            }
                            if (LibraryColumn.TRACK in visibleLibraryColumns) {
                                SortableHeaderText(
                                    label = "Track",
                                    active = controller.librarySortMode == LibrarySortMode.TRACK,
                                    descending = controller.librarySortDescending,
                                    modifier = Modifier.width(72.dp)
                                ) {
                                    controller.toggleLibrarySort(LibrarySortMode.TRACK)
                                }
                            }
                            if (LibraryColumn.ARTIST in visibleLibraryColumns) {
                                SortableHeaderText(
                                    label = "Artist",
                                    active = controller.librarySortMode == LibrarySortMode.ARTIST,
                                    descending = controller.librarySortDescending,
                                    modifier = Modifier.width(120.dp)
                                ) {
                                    controller.toggleLibrarySort(LibrarySortMode.ARTIST)
                                }
                            }
                            if (LibraryColumn.ALBUM in visibleLibraryColumns) {
                                SortableHeaderText(
                                    label = "Album",
                                    active = controller.librarySortMode == LibrarySortMode.ALBUM,
                                    descending = controller.librarySortDescending,
                                    modifier = Modifier.width(120.dp)
                                ) {
                                    controller.toggleLibrarySort(LibrarySortMode.ALBUM)
                                }
                            }
                            if (LibraryColumn.GENRE in visibleLibraryColumns) {
                                SortableHeaderText(
                                    label = "Genre",
                                    active = controller.librarySortMode == LibrarySortMode.GENRE,
                                    descending = controller.librarySortDescending,
                                    modifier = Modifier.width(96.dp)
                                ) {
                                    controller.toggleLibrarySort(LibrarySortMode.GENRE)
                                }
                            }
                            if (LibraryColumn.YEAR in visibleLibraryColumns) {
                                SortableHeaderText(
                                    label = "Year",
                                    active = controller.librarySortMode == LibrarySortMode.YEAR,
                                    descending = controller.librarySortDescending,
                                    modifier = Modifier.width(72.dp)
                                ) {
                                    controller.toggleLibrarySort(LibrarySortMode.YEAR)
                                }
                            }
                            if (LibraryColumn.AUTHOR in visibleLibraryColumns) {
                                SortableHeaderText(
                                    label = libraryColumnLabel(controller.libraryCategoryFilter, LibraryColumn.AUTHOR),
                                    active = controller.librarySortMode == LibrarySortMode.AUTHOR,
                                    descending = controller.librarySortDescending,
                                    modifier = Modifier.width(120.dp)
                                ) {
                                    controller.toggleLibrarySort(LibrarySortMode.AUTHOR)
                                }
                            }
                            if (LibraryColumn.COMPANY in visibleLibraryColumns) {
                                SortableHeaderText(
                                    label = "Company",
                                    active = controller.librarySortMode == LibrarySortMode.COMPANY,
                                    descending = controller.librarySortDescending,
                                    modifier = Modifier.width(120.dp)
                                ) {
                                    controller.toggleLibrarySort(LibrarySortMode.COMPANY)
                                }
                            }
                            if (LibraryColumn.PLATFORM in visibleLibraryColumns) {
                                SortableHeaderText(
                                    label = "Platform",
                                    active = controller.librarySortMode == LibrarySortMode.PLATFORM,
                                    descending = controller.librarySortDescending,
                                    modifier = Modifier.width(100.dp)
                                ) {
                                    controller.toggleLibrarySort(LibrarySortMode.PLATFORM)
                                }
                            }
                            if (LibraryColumn.DESCRIPTION in visibleLibraryColumns) {
                                SortableHeaderText(
                                    label = "Description",
                                    active = controller.librarySortMode == LibrarySortMode.DESCRIPTION,
                                    descending = controller.librarySortDescending,
                                    modifier = Modifier.width(180.dp)
                                ) {
                                    controller.toggleLibrarySort(LibrarySortMode.DESCRIPTION)
                                }
                            }
                            if (LibraryColumn.FILES in visibleLibraryColumns) {
                                SortableHeaderText(
                                    label = "Files",
                                    active = controller.librarySortMode == LibrarySortMode.FILES,
                                    descending = controller.librarySortDescending,
                                    modifier = Modifier.width(68.dp)
                                ) {
                                    controller.toggleLibrarySort(LibrarySortMode.FILES)
                                }
                            }
                            if (LibraryColumn.TRACKERS in visibleLibraryColumns) {
                                SortableHeaderText(
                                    label = "Trackers",
                                    active = controller.librarySortMode == LibrarySortMode.TRACKERS,
                                    descending = controller.librarySortDescending,
                                    modifier = Modifier.width(82.dp)
                                ) {
                                    controller.toggleLibrarySort(LibrarySortMode.TRACKERS)
                                }
                            }
                            Spacer(Modifier.width(54.dp))
                        }
                    }
                    HorizontalDivider()
                    if (visibleItems.isEmpty()) {
                        EmptyState(
                            icon = Icons.Rounded.Folder,
                            title = libraryEmptyStateTitle(controller.hasActiveLibraryFilters(), activeSection),
                            body = libraryEmptyStateBody(controller.hasActiveLibraryFilters(), activeSection)
                        )
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(visibleItems, key = { it.file.absolutePath }) { item ->
                                val isPlaying = currentFile == item.file &&
                                    playerState in setOf(PlayerState.PLAYING, PlayerState.SEEKING_PLAY, PlayerState.PAUSED)
                                FileRow(
                                    controller = controller,
                                    item = item,
                                    selected = item.file.absolutePath in controller.selectedLibraryItemPaths,
                                    primarySelected = controller.selectedLibraryItemPath == item.file.absolutePath,
                                    selectedCount = selectedItems.size,
                                    isPlaying = isPlaying,
                                    visibleColumns = visibleLibraryColumns,
                                    onSelect = { extendSelection, toggleSelection ->
                                        controller.selectLibraryItem(item, extendSelection, toggleSelection)
                                        tableFocusRequester.requestFocus()
                                    },
                                    onToggleChecked = {
                                        controller.toggleLibraryItemChecked(item)
                                        tableFocusRequester.requestFocus()
                                    }
                                )
                                HorizontalDivider()
                            }
                        }
                    }
                }
            }
        }
        }
    )
}

@Composable
private fun RuntimeErrorDialog(controller: ComposeAppController, report: ComposeRuntimeErrorReport) {
    var showDetails by remember(report.id) { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { controller.dismissRuntimeErrorDialog(report.id) },
        confirmButton = {
            TextButton(onClick = { controller.dismissRuntimeErrorDialog(report.id) }) {
                Text("Close")
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = {
                    copyTextToClipboard(report.bugReport)
                    controller.showNotice("Diagnostics", "Copied diagnostic details to the clipboard.", OperationNoticeLevel.SUCCESS)
                }) {
                    Text("Copy Details")
                }
                OutlinedButton(onClick = { controller.saveRuntimeErrorReport(report) }) {
                    Text("Save Diagnostic Report…")
                }
            }
        },
        title = { Text(report.title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(report.message)
                report.detail?.let { detail ->
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            detail,
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
                TextButton(onClick = { showDetails = !showDetails }) {
                    Text(if (showDetails) "Hide Details" else "Show Details")
                }
                if (showDetails) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(
                            report.bugReport,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(240.dp)
                                .verticalScroll(rememberScrollState())
                                .padding(12.dp),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    )
}

@Composable
private fun FileRow(
    controller: ComposeAppController,
    item: LocalFileItem,
    selected: Boolean,
    primarySelected: Boolean,
    selectedCount: Int,
    isPlaying: Boolean,
    visibleColumns: Set<LibraryColumn>,
    onSelect: (extendSelection: Boolean, toggleSelection: Boolean) -> Unit,
    onToggleChecked: () -> Unit
) {
    var menuExpanded by remember(item.file.absolutePath) { mutableStateOf(false) }
    val identity = controller.libraryItemIdentity(item)
    val dragTransferData = libraryFilesTransferData(
        controller.selectedLibrarySectionId,
        controller.draggableLibraryFiles(item)
    )
    Surface(
        color = when {
            primarySelected -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
            selected -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
            isPlaying -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
            else -> Color.Transparent
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .let { base ->
                    if (dragTransferData != null) {
                        base.dragAndDropSource { dragTransferData }
                    } else {
                        base
                    }
                }
                .then(
                    rememberSelectableRowModifier(
                        rowKey = item.file.absolutePath,
                        onSelect = onSelect,
                        onActivate = { controller.openSelectedLibraryItems() },
                        onContextRequest = {
                            controller.handleLibraryContextSelection(item)
                            menuExpanded = true
                        }
                    )
                )
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = selected,
                onCheckedChange = { onToggleChecked() }
            )
            if (LibraryColumn.NAME in visibleColumns) {
                Row(modifier = Modifier.weight(1.4f), verticalAlignment = Alignment.CenterVertically) {
                    FileIdentityIcon(
                        icon = identity.icon,
                        modifier = Modifier.size(18.dp),
                        fallback = if (isPlaying) FileIconToken.AUDIO else FileIconToken.FOLDER
                    )
                    Spacer(Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(identity.title, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(
                            buildLibraryItemMeta(identity.subtitle, item, isPlaying),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
            if (LibraryColumn.TYPE in visibleColumns) {
                Text(item.category.getSingularName(), modifier = Modifier.width(96.dp), style = MaterialTheme.typography.bodySmall)
            }
            if (LibraryColumn.FILENAME in visibleColumns) {
                Text(
                    libraryColumnValueText(item, LibraryColumn.FILENAME),
                    modifier = Modifier.width(172.dp),
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (LibraryColumn.EXTENSION in visibleColumns) {
                Text(libraryColumnValueText(item, LibraryColumn.EXTENSION), modifier = Modifier.width(92.dp), style = MaterialTheme.typography.bodySmall)
            }
            if (LibraryColumn.SIZE in visibleColumns) {
                Text(formatBytes(item.size), modifier = Modifier.width(92.dp), style = MaterialTheme.typography.bodySmall)
            }
            if (LibraryColumn.ACTIVITY in visibleColumns) {
                Column(modifier = Modifier.width(126.dp)) {
                    Text("${item.numHits} hits", style = MaterialTheme.typography.bodySmall)
                    Text(
                        "${item.numUploads} uploads",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (LibraryColumn.HITS in visibleColumns) {
                Text(item.numHits.toString(), modifier = Modifier.width(72.dp), style = MaterialTheme.typography.bodySmall)
            }
            if (LibraryColumn.UPLOADS in visibleColumns) {
                Text(item.numUploads.toString(), modifier = Modifier.width(78.dp), style = MaterialTheme.typography.bodySmall)
            }
            if (LibraryColumn.UPLOAD_ATTEMPTS in visibleColumns) {
                Text(item.numUploadAttempts.toString(), modifier = Modifier.width(132.dp), style = MaterialTheme.typography.bodySmall)
            }
            if (LibraryColumn.UPDATED in visibleColumns) {
                Text(
                    formatDate(item.lastModifiedTime),
                    modifier = Modifier.width(108.dp),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            if (LibraryColumn.LOCATION in visibleColumns) {
                Text(
                    libraryColumnValueText(item, LibraryColumn.LOCATION),
                    modifier = Modifier.width(180.dp),
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (LibraryColumn.LENGTH in visibleColumns) {
                Text(libraryColumnValueText(item, LibraryColumn.LENGTH), modifier = Modifier.width(84.dp), style = MaterialTheme.typography.bodySmall)
            }
            if (LibraryColumn.BITRATE in visibleColumns) {
                Text(libraryColumnValueText(item, LibraryColumn.BITRATE), modifier = Modifier.width(86.dp), style = MaterialTheme.typography.bodySmall)
            }
            if (LibraryColumn.TRACK in visibleColumns) {
                Text(libraryColumnValueText(item, LibraryColumn.TRACK), modifier = Modifier.width(72.dp), style = MaterialTheme.typography.bodySmall)
            }
            if (LibraryColumn.ARTIST in visibleColumns) {
                Text(libraryColumnValueText(item, LibraryColumn.ARTIST), modifier = Modifier.width(120.dp), style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            if (LibraryColumn.ALBUM in visibleColumns) {
                Text(libraryColumnValueText(item, LibraryColumn.ALBUM), modifier = Modifier.width(120.dp), style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            if (LibraryColumn.GENRE in visibleColumns) {
                Text(libraryColumnValueText(item, LibraryColumn.GENRE), modifier = Modifier.width(96.dp), style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            if (LibraryColumn.YEAR in visibleColumns) {
                Text(libraryColumnValueText(item, LibraryColumn.YEAR), modifier = Modifier.width(72.dp), style = MaterialTheme.typography.bodySmall)
            }
            if (LibraryColumn.AUTHOR in visibleColumns) {
                Text(libraryColumnValueText(item, LibraryColumn.AUTHOR), modifier = Modifier.width(120.dp), style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            if (LibraryColumn.COMPANY in visibleColumns) {
                Text(libraryColumnValueText(item, LibraryColumn.COMPANY), modifier = Modifier.width(120.dp), style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            if (LibraryColumn.PLATFORM in visibleColumns) {
                Text(libraryColumnValueText(item, LibraryColumn.PLATFORM), modifier = Modifier.width(100.dp), style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            if (LibraryColumn.DESCRIPTION in visibleColumns) {
                Text(libraryColumnValueText(item, LibraryColumn.DESCRIPTION), modifier = Modifier.width(180.dp), style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            if (LibraryColumn.FILES in visibleColumns) {
                Text(libraryColumnValueText(item, LibraryColumn.FILES), modifier = Modifier.width(68.dp), style = MaterialTheme.typography.bodySmall)
            }
            if (LibraryColumn.TRACKERS in visibleColumns) {
                Text(libraryColumnValueText(item, LibraryColumn.TRACKERS), modifier = Modifier.width(82.dp), style = MaterialTheme.typography.bodySmall)
            }
            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(Icons.Rounded.MoreVert, contentDescription = "Actions")
                }
                DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                    val jumpTargets = controller.libraryListTargetsForItem(item)
                    DropdownMenuItem(
                        text = {
                            Text(
                                if (selected && selectedCount > 1) {
                                    "Open Selected"
                                } else if (item.category == Category.AUDIO) {
                                    "Play"
                                } else {
                                    "Open"
                                }
                            )
                        },
                        onClick = {
                            menuExpanded = false
                            controller.openSelectedLibraryItems()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Reveal") },
                        onClick = {
                            menuExpanded = false
                            controller.revealSelectedLibraryItems()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("View File Info") },
                        onClick = {
                            menuExpanded = false
                            controller.showLibraryFileInfo(item)
                        }
                    )
                    if (!item.isIncomplete) {
                        DropdownMenuItem(
                            text = { Text("Rename File") },
                            onClick = {
                                menuExpanded = false
                                controller.showRenameLibraryFileDialog(item)
                            }
                        )
                    }
                    jumpTargets.forEach { target ->
                        DropdownMenuItem(
                            text = { Text(showInTargetLabel(target)) },
                            onClick = {
                                menuExpanded = false
                                controller.showLibraryItemInTarget(item, target)
                            }
                        )
                    }
                    DropdownMenuItem(
                        text = { Text(controller.currentLibrarySectionRemoveLabel()) },
                        onClick = {
                            menuExpanded = false
                            controller.confirmRemoveSelectedLibraryItems()
                        }
                    )
                    if (controller.currentSharedList() != null) {
                        DropdownMenuItem(
                            text = { Text("Remove from Library") },
                            onClick = {
                                menuExpanded = false
                                controller.confirmRemoveSelectedLibraryItemsFromLibrary()
                            }
                        )
                    }
                    if (controller.canRemoveSelectedLibraryItemsFromAllOtherLists()) {
                        DropdownMenuItem(
                            text = { Text("Remove from All Other Collections") },
                            onClick = {
                                menuExpanded = false
                                controller.confirmRemoveSelectedLibraryItemsFromAllOtherLists()
                            }
                        )
                    }
                    DropdownMenuItem(
                        text = { Text("Delete from Disk") },
                        onClick = {
                            menuExpanded = false
                            controller.confirmDeleteSelectedLibraryItems()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchScreen(controller: ComposeAppController, tab: SearchTabSession) {
    val desktopDensity = LocalDesktopDensity.current
    val desktopType = LocalDesktopTypeScale.current
    val results = controller.visibleSearchResults(tab)
    val selectedResults = controller.selectedSearchResults(tab)
    val presentationCategory = controller.searchPresentationCategory(tab)
    val availableSearchColumns = controller.availableSearchColumns(tab)
    val visibleSearchColumns = tab.visibleColumns.intersect(availableSearchColumns.toSet()).ifEmpty { setOf(SearchColumn.NAME) }
    val availableSearchSortModes = controller.availableSearchSortModes(tab)
    val activeFilters = controller.searchActiveFilters(tab)
    var sortExpanded by remember { mutableStateOf(false) }
    val tableFocusRequester = remember(tab.id) { FocusRequester() }
    val resultsListState = remember(tab.id) { androidx.compose.foundation.lazy.LazyListState() }
    val selectedResult = controller.selectedSearchResult(tab)

    LaunchedEffect(tab.id, results.size, tab.selectedResultKey) {
        if (results.isEmpty()) {
            controller.clearSearchSelection(tab)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .onPreviewKeyEvent { event ->
                if (event.type == KeyEventType.KeyDown && event.key == Key.Escape && controller.hasActiveSearchFilters(tab)) {
                    controller.clearSearchFilters(tab)
                    true
                } else {
                    false
                }
            },
        verticalArrangement = Arrangement.spacedBy(desktopDensity.sectionGap)
    ) {
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(desktopDensity.cardPadding),
                verticalArrangement = Arrangement.spacedBy(desktopDensity.sectionGap)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(tab.title, style = desktopType.screenTitle)
                        Text(
                            listOf(tab.descriptor, tab.subtitle)
                                .filter { it.isNotBlank() }
                                .distinct()
                                .joinToString(" • "),
                            style = desktopType.summary,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(desktopDensity.chipGap),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CompactOutlinedButton(onClick = { controller.setSearchRefinementRailVisible(!controller.searchRefinementRailVisible) }) {
                            Text(
                                if (controller.searchRefinementRailVisible) {
                                    "Hide Filters"
                                } else if (activeFilters.isNotEmpty()) {
                                    "Show Filters (${activeFilters.size})"
                                } else {
                                    "Show Filters"
                                }
                            )
                        }
                        Box {
                            CompactOutlinedButton(onClick = { sortExpanded = true }) {
                                Text("Sort: ${searchColumnLabel(presentationCategory, tab.sortMode.asSearchColumn())}")
                            }
                            DropdownMenu(expanded = sortExpanded, onDismissRequest = { sortExpanded = false }) {
                                availableSearchSortModes.forEach { mode ->
                                    DropdownMenuItem(
                                        text = { Text(searchColumnLabel(presentationCategory, mode.asSearchColumn())) },
                                        onClick = {
                                            controller.toggleSearchSort(tab, mode)
                                            sortExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                        ColumnVisibilityMenu(
                            entries = availableSearchColumns.map { column ->
                                ColumnToggleEntry(
                                    label = searchColumnLabel(presentationCategory, column),
                                    visible = column in visibleSearchColumns
                                ) {
                                    controller.toggleSearchColumn(tab, column)
                                }
                            }
                        )
                        when {
                            controller.canStopSearch(tab) -> CompactFilledTonalButton(onClick = { controller.stopSearch(tab) }) {
                                Text("Stop Search")
                            }

                            controller.canRepeatSearch(tab) -> CompactOutlinedButton(onClick = { controller.repeatSearch(tab) }) {
                                Text("Repeat Search")
                            }
                        }
                    }
                }
                Text(
                    buildSearchSummaryLine(controller, tab, results.size, activeFilters.size, selectedResults.size),
                    style = desktopType.summary,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        SearchConnectionBanner(controller, tab)
        BrowseStatusBanner(controller, tab)

        val workspace: @Composable () -> Unit = {
            if (results.isEmpty()) {
                val filteredOut = controller.hasActiveSearchFilters(tab)
                if (controller.shouldShowSearchAwaitingConnections(tab)) {
                        EmptyState(
                            icon = Icons.Rounded.Sync,
                            title = "Connecting…",
                            body = "Results will appear as more connections come online."
                        )
                } else {
                    EmptyState(
                        icon = Icons.Rounded.Search,
                        title = searchEmptyStateTitle(tab, filteredOut),
                        body = searchEmptyStateBody(tab, filteredOut)
                    )
                }
            } else {
                SearchResultsWorkspace(
                    controller = controller,
                    tab = tab,
                    results = results,
                    selectedResults = selectedResults,
                    selectedResult = selectedResult,
                    presentationCategory = presentationCategory,
                    visibleSearchColumns = visibleSearchColumns,
                    availableSearchColumns = availableSearchColumns,
                    availableSearchSortModes = availableSearchSortModes,
                    tableFocusRequester = tableFocusRequester,
                    resultsListState = resultsListState
                )
            }
        }

        if (controller.searchRefinementRailVisible) {
            HorizontalSplitPane(
                fraction = controller.searchRefinementRailFraction,
                onFractionChange = { controller.updateSearchRefinementRailFraction(it) },
                onFractionChangeFinished = { controller.updateSearchRefinementRailFraction(it, persist = true) },
                modifier = Modifier.fillMaxSize(),
                minStartWidth = 260.dp,
                minEndWidth = 520.dp,
                start = {
                    SearchRefinementRail(
                        controller = controller,
                        tab = tab,
                        presentationCategory = presentationCategory,
                        activeFilters = activeFilters
                    )
                },
                end = workspace
            )
        } else {
            workspace()
        }
    }
}

@Composable
private fun SearchResultsWorkspace(
    controller: ComposeAppController,
    tab: SearchTabSession,
    results: List<GroupedSearchResult>,
    selectedResults: List<GroupedSearchResult>,
    selectedResult: GroupedSearchResult?,
    presentationCategory: SearchCategory,
    visibleSearchColumns: Set<SearchColumn>,
    availableSearchColumns: List<SearchColumn>,
    availableSearchSortModes: List<SearchSortMode>,
    tableFocusRequester: FocusRequester,
    resultsListState: androidx.compose.foundation.lazy.LazyListState
) {
    HorizontalSplitPane(
        fraction = controller.searchResultsPaneFraction,
        onFractionChange = { controller.updateSearchPaneFraction(it) },
        onFractionChangeFinished = { controller.updateSearchPaneFraction(it, persist = true) },
        modifier = Modifier.fillMaxSize(),
        minStartWidth = 420.dp,
        minEndWidth = 300.dp,
        start = {
            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .focusRequester(tableFocusRequester)
                    .focusable()
                    .onPreviewKeyEvent { event ->
                        handleTableKeyEvent(
                            event = event,
                            moveSelection = { controller.moveSearchSelection(tab, it) },
                            extendSelection = { controller.extendSearchSelection(tab, it) },
                            selectAll = { controller.selectAllVisibleSearchResults(tab) },
                            activateSelection = { controller.activateSelectedSearchResult(tab) }
                        )
                    },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    HeaderContextMenuArea(
                        menuContent = {
                            availableSearchSortModes.forEach { mode ->
                                DropdownMenuItem(
                                    text = { Text("Sort by ${searchColumnLabel(presentationCategory, mode.asSearchColumn())}") },
                                    onClick = {
                                        controller.toggleSearchSort(tab, mode)
                                        dismiss()
                                    }
                                )
                            }
                            HorizontalDivider()
                            availableSearchColumns.forEach { column ->
                                DropdownMenuItem(
                                    text = {
                                        Text("${if (column in visibleSearchColumns) "Hide" else "Show"} ${searchColumnLabel(presentationCategory, column)}")
                                    },
                                    onClick = {
                                        controller.toggleSearchColumn(tab, column)
                                        dismiss()
                                    }
                                )
                            }
                        }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = results.isNotEmpty() && results.all { searchResultKey(it) in tab.selectedResultKeys },
                                onCheckedChange = { checked ->
                                    if (checked) {
                                        controller.selectAllVisibleSearchResults(tab)
                                    } else {
                                        controller.clearSearchSelection(tab)
                                    }
                                }
                            )
                            if (SearchColumn.NAME in visibleSearchColumns) {
                                SortableHeaderText(
                                    label = "Name",
                                    active = tab.sortMode == SearchSortMode.NAME,
                                    descending = tab.sortDescending,
                                    modifier = Modifier.weight(1.4f)
                                ) {
                                    controller.toggleSearchSort(tab, SearchSortMode.NAME)
                                }
                            }
                            if (SearchColumn.TYPE in visibleSearchColumns) {
                                SortableHeaderText(
                                    label = "Type",
                                    active = tab.sortMode == SearchSortMode.TYPE,
                                    descending = tab.sortDescending,
                                    modifier = Modifier.width(96.dp)
                                ) {
                                    controller.toggleSearchSort(tab, SearchSortMode.TYPE)
                                }
                            }
                            if (SearchColumn.FROM in visibleSearchColumns) {
                                SortableHeaderText(
                                    label = "From",
                                    active = tab.sortMode == SearchSortMode.FROM,
                                    descending = tab.sortDescending,
                                    modifier = Modifier.width(120.dp)
                                ) {
                                    controller.toggleSearchSort(tab, SearchSortMode.FROM)
                                }
                            }
                            if (SearchColumn.FILENAME in visibleSearchColumns) {
                                SortableHeaderText(
                                    label = "Filename",
                                    active = tab.sortMode == SearchSortMode.FILENAME,
                                    descending = tab.sortDescending,
                                    modifier = Modifier.width(172.dp)
                                ) {
                                    controller.toggleSearchSort(tab, SearchSortMode.FILENAME)
                                }
                            }
                            if (SearchColumn.EXTENSION in visibleSearchColumns) {
                                SortableHeaderText(
                                    label = "Extension",
                                    active = tab.sortMode == SearchSortMode.EXTENSION,
                                    descending = tab.sortDescending,
                                    modifier = Modifier.width(92.dp)
                                ) {
                                    controller.toggleSearchSort(tab, SearchSortMode.EXTENSION)
                                }
                            }
                            if (SearchColumn.SIZE in visibleSearchColumns) {
                                SortableHeaderText(
                                    label = "Size",
                                    active = tab.sortMode == SearchSortMode.SIZE,
                                    descending = tab.sortDescending,
                                    modifier = Modifier.width(90.dp)
                                ) {
                                    controller.toggleSearchSort(tab, SearchSortMode.SIZE)
                                }
                            }
                            if (SearchColumn.SOURCES in visibleSearchColumns) {
                                SortableHeaderText(
                                    label = "Sources",
                                    active = tab.sortMode == SearchSortMode.SOURCES,
                                    descending = tab.sortDescending,
                                    modifier = Modifier.width(72.dp)
                                ) {
                                    controller.toggleSearchSort(tab, SearchSortMode.SOURCES)
                                }
                            }
                            if (SearchColumn.FRIENDS in visibleSearchColumns) {
                                SortableHeaderText(
                                    label = "Friends",
                                    active = tab.sortMode == SearchSortMode.FRIENDS,
                                    descending = tab.sortDescending,
                                    modifier = Modifier.width(72.dp)
                                ) {
                                    controller.toggleSearchSort(tab, SearchSortMode.FRIENDS)
                                }
                            }
                            if (SearchColumn.LENGTH in visibleSearchColumns) {
                                SortableHeaderText(
                                    label = "Length",
                                    active = tab.sortMode == SearchSortMode.LENGTH,
                                    descending = tab.sortDescending,
                                    modifier = Modifier.width(84.dp)
                                ) {
                                    controller.toggleSearchSort(tab, SearchSortMode.LENGTH)
                                }
                            }
                            if (SearchColumn.QUALITY in visibleSearchColumns) {
                                SortableHeaderText(
                                    label = "Quality",
                                    active = tab.sortMode == SearchSortMode.QUALITY,
                                    descending = tab.sortDescending,
                                    modifier = Modifier.width(118.dp)
                                ) {
                                    controller.toggleSearchSort(tab, SearchSortMode.QUALITY)
                                }
                            }
                            if (SearchColumn.BITRATE in visibleSearchColumns) {
                                SortableHeaderText(
                                    label = "Bitrate",
                                    active = tab.sortMode == SearchSortMode.BITRATE,
                                    descending = tab.sortDescending,
                                    modifier = Modifier.width(86.dp)
                                ) {
                                    controller.toggleSearchSort(tab, SearchSortMode.BITRATE)
                                }
                            }
                            if (SearchColumn.TRACK in visibleSearchColumns) {
                                SortableHeaderText(
                                    label = "Track",
                                    active = tab.sortMode == SearchSortMode.TRACK,
                                    descending = tab.sortDescending,
                                    modifier = Modifier.width(72.dp)
                                ) {
                                    controller.toggleSearchSort(tab, SearchSortMode.TRACK)
                                }
                            }
                            if (SearchColumn.ARTIST in visibleSearchColumns) {
                                SortableHeaderText(
                                    label = "Artist",
                                    active = tab.sortMode == SearchSortMode.ARTIST,
                                    descending = tab.sortDescending,
                                    modifier = Modifier.width(120.dp)
                                ) {
                                    controller.toggleSearchSort(tab, SearchSortMode.ARTIST)
                                }
                            }
                            if (SearchColumn.ALBUM in visibleSearchColumns) {
                                SortableHeaderText(
                                    label = "Album",
                                    active = tab.sortMode == SearchSortMode.ALBUM,
                                    descending = tab.sortDescending,
                                    modifier = Modifier.width(120.dp)
                                ) {
                                    controller.toggleSearchSort(tab, SearchSortMode.ALBUM)
                                }
                            }
                            if (SearchColumn.GENRE in visibleSearchColumns) {
                                SortableHeaderText(
                                    label = "Genre",
                                    active = tab.sortMode == SearchSortMode.GENRE,
                                    descending = tab.sortDescending,
                                    modifier = Modifier.width(96.dp)
                                ) {
                                    controller.toggleSearchSort(tab, SearchSortMode.GENRE)
                                }
                            }
                            if (SearchColumn.YEAR in visibleSearchColumns) {
                                SortableHeaderText(
                                    label = "Year",
                                    active = tab.sortMode == SearchSortMode.YEAR,
                                    descending = tab.sortDescending,
                                    modifier = Modifier.width(72.dp)
                                ) {
                                    controller.toggleSearchSort(tab, SearchSortMode.YEAR)
                                }
                            }
                            if (SearchColumn.AUTHOR in visibleSearchColumns) {
                                SortableHeaderText(
                                    label = searchColumnLabel(presentationCategory, SearchColumn.AUTHOR),
                                    active = tab.sortMode == SearchSortMode.AUTHOR,
                                    descending = tab.sortDescending,
                                    modifier = Modifier.width(120.dp)
                                ) {
                                    controller.toggleSearchSort(tab, SearchSortMode.AUTHOR)
                                }
                            }
                            if (SearchColumn.COMPANY in visibleSearchColumns) {
                                SortableHeaderText(
                                    label = "Company",
                                    active = tab.sortMode == SearchSortMode.COMPANY,
                                    descending = tab.sortDescending,
                                    modifier = Modifier.width(120.dp)
                                ) {
                                    controller.toggleSearchSort(tab, SearchSortMode.COMPANY)
                                }
                            }
                            if (SearchColumn.PLATFORM in visibleSearchColumns) {
                                SortableHeaderText(
                                    label = "Platform",
                                    active = tab.sortMode == SearchSortMode.PLATFORM,
                                    descending = tab.sortDescending,
                                    modifier = Modifier.width(100.dp)
                                ) {
                                    controller.toggleSearchSort(tab, SearchSortMode.PLATFORM)
                                }
                            }
                            if (SearchColumn.DESCRIPTION in visibleSearchColumns) {
                                SortableHeaderText(
                                    label = "Description",
                                    active = tab.sortMode == SearchSortMode.DESCRIPTION,
                                    descending = tab.sortDescending,
                                    modifier = Modifier.width(180.dp)
                                ) {
                                    controller.toggleSearchSort(tab, SearchSortMode.DESCRIPTION)
                                }
                            }
                            if (SearchColumn.FILES in visibleSearchColumns) {
                                SortableHeaderText(
                                    label = "Files",
                                    active = tab.sortMode == SearchSortMode.FILES,
                                    descending = tab.sortDescending,
                                    modifier = Modifier.width(68.dp)
                                ) {
                                    controller.toggleSearchSort(tab, SearchSortMode.FILES)
                                }
                            }
                            if (SearchColumn.TRACKERS in visibleSearchColumns) {
                                SortableHeaderText(
                                    label = "Trackers",
                                    active = tab.sortMode == SearchSortMode.TRACKERS,
                                    descending = tab.sortDescending,
                                    modifier = Modifier.width(82.dp)
                                ) {
                                    controller.toggleSearchSort(tab, SearchSortMode.TRACKERS)
                                }
                            }
                            Spacer(Modifier.width(54.dp))
                        }
                    }
                    HorizontalDivider()
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        state = resultsListState
                    ) {
                        items(results, key = { searchResultKey(it) }) { result ->
                            SearchResultRow(
                                controller = controller,
                                tab = tab,
                                result = result,
                                visibleColumns = visibleSearchColumns,
                                selected = searchResultKey(result) in tab.selectedResultKeys,
                                primarySelected = searchResultKey(result) == selectedResult?.let(::searchResultKey),
                                onSelect = { extendSelection, toggleSelection ->
                                    controller.selectSearchResult(tab, result, extendSelection, toggleSelection)
                                    tableFocusRequester.requestFocus()
                                },
                                onToggleChecked = {
                                    controller.toggleSearchResultChecked(tab, result)
                                    tableFocusRequester.requestFocus()
                                }
                            )
                            HorizontalDivider()
                        }
                    }
                }
            }
        },
        end = {
            Card(
                modifier = Modifier.fillMaxSize(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                when {
                    selectedResults.isEmpty() -> {
                        EmptyState(
                            icon = Icons.Rounded.Info,
                            title = "Choose a result",
                            body = "Select a result to view sources and download options."
                        )
                    }

                    selectedResults.size > 1 -> {
                        MultiSearchSelectionDetails(
                            controller = controller,
                            tab = tab,
                            results = selectedResults
                        )
                    }

                    selectedResult == null -> {
                        EmptyState(
                            icon = Icons.Rounded.Info,
                            title = "Choose a result",
                            body = "Select a result to view sources and download options."
                        )
                    }

                    else -> {
                        SearchResultDetails(
                            controller = controller,
                            tab = tab,
                            result = selectedResult
                        )
                    }
                }
            }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SearchRefinementRail(
    controller: ComposeAppController,
    tab: SearchTabSession,
    presentationCategory: SearchCategory,
    activeFilters: List<SearchActiveFilterToken>
) {
    val categoryOptions = controller.searchCategoryFacetOptions(tab)
    val sourceOptions = controller.searchSourceFacetOptions(tab)
    val friendOptions = controller.searchFriendFacetOptions(tab)
    val visiblePropertyFacets = controller.searchVisibleRefinementPropertyFacets(tab)
    val visibleRangeFacets = controller.searchVisibleRefinementRangeFacets(tab)

    Card(
        modifier = Modifier.fillMaxSize(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("Refine results", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(
                        if (activeFilters.isEmpty()) {
                            "Filter by category, source, friend, and metadata."
                        } else {
                            "${activeFilters.size} active filter${if (activeFilters.size == 1) "" else "s"}"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (activeFilters.isNotEmpty()) {
                    TextButton(onClick = { controller.clearSearchFilters(tab) }) {
                        Text("Reset Filters")
                    }
                }
            }

            OutlinedTextField(
                value = tab.filterText,
                onValueChange = { controller.updateSearchFilterText(tab, it) },
                singleLine = true,
                label = { Text("Refine results") },
                placeholder = { Text("Name, friend, source, or metadata") },
                trailingIcon = {
                    if (tab.filterText.isNotEmpty()) {
                        IconButton(onClick = { controller.updateSearchFilterText(tab, "") }) {
                            Icon(Icons.Rounded.Close, contentDescription = "Clear text filter")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            if (activeFilters.isNotEmpty()) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    activeFilters.forEach { token ->
                        AssistChip(
                            onClick = { controller.dismissSearchActiveFilter(tab, token) },
                            label = { Text(token.label, maxLines = 1) },
                            leadingIcon = { Icon(Icons.Rounded.Close, contentDescription = null) }
                        )
                    }
                }
            }

            FilterChip(
                selected = tab.friendsOnly,
                onClick = { controller.toggleSearchFriendsOnly(tab) },
                label = { Text("Friends only") }
            )

            if (tab.category == SearchCategory.ALL && categoryOptions.size > 1) {
                SearchFacetSection(
                    title = "Category",
                    options = categoryOptions,
                    selectedId = tab.displayCategory?.name ?: SearchCategory.ALL.name,
                    onSelect = { selectedId ->
                        controller.updateSearchDisplayCategory(
                            tab,
                            SearchCategory.entries.firstOrNull { it.name == selectedId }?.takeUnless { it == SearchCategory.ALL }
                        )
                    }
                )
            }

            if (controller.searchShouldShowSourceFacet(tab)) {
                SearchFacetSection(
                    title = "Sources",
                    options = sourceOptions,
                    selectedId = tab.sourceFilter.name,
                    onSelect = { selectedId ->
                        SearchSourceFilter.entries.firstOrNull { it.name == selectedId }?.let {
                            controller.updateSearchSourceFilter(tab, it)
                        }
                    }
                )
            }

            if (controller.searchShouldShowFriendFacet(tab)) {
                SearchFacetSection(
                    title = "Friends",
                    options = friendOptions,
                    selectedId = tab.selectedFriendFacetId.orEmpty(),
                    onSelect = { selectedId ->
                        controller.updateSearchFriendFacet(tab, selectedId.ifBlank { null })
                    }
                )
            }

            visiblePropertyFacets.forEach { facet ->
                val options = controller.searchPropertyFacetOptions(tab, facet)
                if (options.isNotEmpty()) {
                    SearchFacetSection(
                        title = controller.searchPropertyFacetLabelText(facet),
                        options = options,
                        selectedId = controller.selectedSearchPropertyFacet(tab, facet)?.lowercase(Locale.US),
                        onSelect = { selectedId ->
                            val current = controller.selectedSearchPropertyFacet(tab, facet)?.lowercase(Locale.US)
                            val selectedOption = options.firstOrNull { it.id == selectedId }
                            controller.updateSearchPropertyFacet(
                                tab,
                                facet,
                                if (current == selectedId) null else selectedOption?.label ?: selectedId
                            )
                        }
                    )
                }
            }

            visibleRangeFacets.forEach { facet ->
                SearchRangeFacetSection(controller = controller, tab = tab, facet = facet)
            }

            if (controller.searchMoreFiltersToggleVisible(tab)) {
                TextButton(onClick = { controller.toggleSearchMoreFilters(tab) }) {
                    Text(if (controller.searchMoreFiltersExpanded(tab)) "Fewer filters" else "More filters")
                }
            }

            if (presentationCategory == SearchCategory.ALL && visiblePropertyFacets.isEmpty() && visibleRangeFacets.isEmpty()) {
                Text(
                    "Choose a category above for more filters and details.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SearchFacetSection(
    title: String,
    options: List<SearchFacetOption>,
    selectedId: String?,
    onSelect: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            options.forEach { option ->
                FilterChip(
                    selected = selectedId == option.id,
                    onClick = { onSelect(option.id) },
                    label = { Text("${option.label} (${option.resultCount})") }
                )
            }
        }
    }
}

@Composable
private fun SearchRangeFacetSection(
    controller: ComposeAppController,
    tab: SearchTabSession,
    facet: SearchRangeFacet
) {
    val selection = controller.selectedSearchRangeFacet(tab, facet)
    val buckets = controller.searchRangeBuckets(tab, facet)
    val lowerBoundOnly = facet == SearchRangeFacet.BITRATE || facet == SearchRangeFacet.QUALITY
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(controller.searchRangeFacetLabelText(facet), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
                controller.searchRangeSelectionLabel(tab, facet)?.let { label ->
                    Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            if (selection.minimumId != null || selection.maximumId != null) {
                TextButton(onClick = { controller.clearSearchRangeSelection(tab, facet) }) {
                    Text("Clear")
                }
            }
        }
        if (lowerBoundOnly) {
            SearchRangeSelector(
                label = "Minimum",
                currentId = selection.minimumId,
                buckets = buckets,
                allowAny = true,
                onSelected = { selectedId ->
                    controller.updateSearchRangeSelection(
                        tab = tab,
                        facet = facet,
                        minimumId = selectedId,
                        maximumId = null,
                        preserveExisting = false
                    )
                }
            )
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                SearchRangeSelector(
                    label = "Minimum",
                    currentId = selection.minimumId,
                    buckets = buckets,
                    allowAny = true,
                    modifier = Modifier.weight(1f),
                    onSelected = { selectedId ->
                        controller.updateSearchRangeSelection(
                            tab = tab,
                            facet = facet,
                            minimumId = selectedId,
                            maximumId = selection.maximumId,
                            preserveExisting = false
                        )
                    }
                )
                SearchRangeSelector(
                    label = "Maximum",
                    currentId = selection.maximumId,
                    buckets = buckets,
                    allowAny = true,
                    modifier = Modifier.weight(1f),
                    onSelected = { selectedId ->
                        controller.updateSearchRangeSelection(
                            tab = tab,
                            facet = facet,
                            minimumId = selection.minimumId,
                            maximumId = selectedId,
                            preserveExisting = false
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun SearchRangeSelector(
    label: String,
    currentId: String?,
    buckets: List<SearchRangeBucket>,
    allowAny: Boolean,
    modifier: Modifier = Modifier,
    onSelected: (String?) -> Unit
) {
    var expanded by remember(label, currentId, buckets.size) { mutableStateOf(false) }
    val currentLabel = buckets.firstOrNull { it.id == currentId }?.label ?: if (allowAny) "Any" else "None"
    Box(modifier = modifier) {
        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
            Text("$label: $currentLabel", maxLines = 1)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            if (allowAny) {
                DropdownMenuItem(
                    text = { Text("Any") },
                    onClick = {
                        expanded = false
                        onSelected(null)
                    }
                )
            }
            buckets
                .filterNot { allowAny && it.minimum == 0L }
                .forEach { bucket ->
                DropdownMenuItem(
                    text = { Text(bucket.label) },
                    onClick = {
                        expanded = false
                        onSelected(bucket.id)
                    }
                )
            }
        }
    }
}

@Composable
private fun SearchConnectionBanner(controller: ComposeAppController, tab: SearchTabSession) {
    if (!controller.shouldShowSearchConnectionWarning(tab)) {
        return
    }

    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Rounded.Sync, contentDescription = null)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Still connecting", fontWeight = FontWeight.SemiBold)
                    Text(
                        "Results may stay limited until WireShare finishes connecting.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                if (controller.canStopSearch(tab)) {
                    TextButton(onClick = { controller.stopSearch(tab) }) {
                        Text("Stop Search")
                    }
                } else if (controller.canRepeatSearch(tab)) {
                    TextButton(onClick = { controller.repeatSearch(tab) }) {
                        Text("Repeat Search")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BrowseStatusBanner(controller: ComposeAppController, tab: SearchTabSession) {
    val status = tab.browseStatus ?: return
    if (status.state == BrowseState.LOADED) {
        return
    }

    Surface(
        color = when (status.state) {
            BrowseState.LOADING -> MaterialTheme.colorScheme.surfaceVariant
            BrowseState.UPDATED,
            BrowseState.UPDATED_PARTIAL_FAIL -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
            else -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.45f)
        },
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Top) {
                Icon(
                    imageVector = when (status.state) {
                        BrowseState.LOADING -> Icons.Rounded.Sync
                        BrowseState.UPDATED,
                        BrowseState.UPDATED_PARTIAL_FAIL -> Icons.Rounded.Refresh
                        BrowseState.OFFLINE,
                        BrowseState.NO_FRIENDS_SHARING -> Icons.Rounded.CloudOff
                        else -> Icons.Rounded.Warning
                    },
                    contentDescription = null
                )
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        browseStatusTitle(tab, status),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        browseStatusBody(tab, status, controller),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                when (status.state) {
                    BrowseState.UPDATED,
                    BrowseState.UPDATED_PARTIAL_FAIL -> {
                        Button(onClick = { controller.refreshBrowseTab(tab) }) {
                            Text("Refresh")
                        }
                    }

                    BrowseState.FAILED,
                    BrowseState.PARTIAL_FAIL -> {
                        Button(onClick = { controller.refreshBrowseTab(tab) }) {
                            Text("Retry")
                        }
                    }

                    BrowseState.OFFLINE,
                    BrowseState.NO_FRIENDS_SHARING -> {
                        when {
                            controller.canSignInFromBrowseStatus(tab) -> Button(onClick = { controller.showFriendLoginDialog() }) {
                                Text("Sign In")
                            }

                            controller.canChatFromBrowseStatus(tab) -> Button(onClick = { controller.chatFromBrowseStatus(tab) }) {
                                Text("Chat")
                            }
                        }
                    }

                    else -> Unit
                }
                if (status.failedSources.isNotEmpty()) {
                    OutlinedButton(onClick = { controller.showBrowseFailures(tab) }) {
                        Text("View Failed Sources")
                    }
                }
            }
        }
    }
}

@Composable
private fun SimilarSearchResultsSection(controller: ComposeAppController, result: GroupedSearchResult) {
    if (result.searchResults.size <= 1) {
        return
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Similar Files", fontWeight = FontWeight.SemiBold)
            Text(
                "${result.searchResults.size} source variants",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        result.searchResults.forEachIndexed { index, variant ->
            val sourceFriend = variant.source.friendPresence?.friend
            val sourceLabel = sourceFriend?.renderName ?: "Network source"
            val canBrowse = variant.source.isBrowseHostEnabled
            val canChat = sourceFriend != null && variant.source.isChatEnabled && controller.canChatWithFriend(sourceFriend.id)
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(
                                variant.fileName,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                "$sourceLabel · ${variant.category.getSingularName()} · ${formatBytes(variant.size)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            "#${index + 1}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (canBrowse) {
                            OutlinedButton(onClick = { controller.browsePrimarySource(variant) }) {
                                Text("Browse")
                            }
                        }
                        if (canChat) {
                            OutlinedButton(onClick = { controller.chatWithFriend(sourceFriend!!.id) }) {
                                Text("Chat")
                            }
                        }
                        MetricBadge("Extension", variant.fileExtension.ifBlank { "Unknown" })
                        MetricBadge("Spam", if (variant.isSpam) "Marked" else "Clear")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SearchResultRow(
    controller: ComposeAppController,
    tab: SearchTabSession,
    result: GroupedSearchResult,
    visibleColumns: Set<SearchColumn>,
    selected: Boolean,
    primarySelected: Boolean,
    onSelect: (extendSelection: Boolean, toggleSelection: Boolean) -> Unit,
    onToggleChecked: () -> Unit
) {
    val primary = result.searchResults.firstOrNull()
    val presentation = controller.searchResultPresentation(result)
    val browsableCount = presentation.browsableCount
    val jumpTargets = presentation.jumpTargets
    val availabilityLabel = presentation.availabilityLabel
    val identity = presentation.identity
    val dragTransferData = searchResultsTransferData(tab.id, controller.draggableSearchResultKeys(tab, result))
    var menuExpanded by remember(searchResultKey(result)) { mutableStateOf(false) }
    Surface(
        color = when {
            primarySelected -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            selected -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
            else -> Color.Transparent
        },
        modifier = Modifier
            .let { base ->
                if (dragTransferData != null) {
                    base.dragAndDropSource { dragTransferData }
                } else {
                    base
                }
            }
            .then(
                rememberSelectableRowModifier(
                    rowKey = searchResultKey(result),
                    onSelect = onSelect,
                    onActivate = { controller.activateSelectedSearchResult(tab) },
                    onContextRequest = {
                        controller.handleSearchContextSelection(tab, result)
                        menuExpanded = true
                    }
                )
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = selected,
                onCheckedChange = { onToggleChecked() }
            )
            if (SearchColumn.NAME in visibleColumns) {
                Row(modifier = Modifier.weight(1.4f), verticalAlignment = Alignment.CenterVertically) {
                    FileIdentityIcon(
                        icon = identity.icon,
                        modifier = Modifier.size(18.dp),
                        fallback = FileIconToken.OTHER
                    )
                    Spacer(Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(identity.title, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                buildSearchMetadata(identity.subtitle, primary, result),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            availabilityLabel?.let { label ->
                                InlineStatusBadge(label = label)
                            }
                        }
                    }
                }
            }
            if (SearchColumn.TYPE in visibleColumns) {
                Text(
                    primary?.category?.getSingularName() ?: "File",
                    modifier = Modifier.width(96.dp),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            if (SearchColumn.FROM in visibleColumns) {
                Text(
                    searchColumnValueText(primary, result, SearchColumn.FROM),
                    modifier = Modifier.width(120.dp),
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (SearchColumn.FILENAME in visibleColumns) {
                Text(
                    searchColumnValueText(primary, result, SearchColumn.FILENAME),
                    modifier = Modifier.width(172.dp),
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (SearchColumn.EXTENSION in visibleColumns) {
                Text(
                    searchColumnValueText(primary, result, SearchColumn.EXTENSION),
                    modifier = Modifier.width(92.dp),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            if (SearchColumn.SIZE in visibleColumns) {
                Text(
                    primary?.size?.let(::formatBytes) ?: "Unknown",
                    modifier = Modifier.width(90.dp),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            if (SearchColumn.SOURCES in visibleColumns) {
                Text(result.sources.size.toString(), modifier = Modifier.width(72.dp), style = MaterialTheme.typography.bodySmall)
            }
            if (SearchColumn.FRIENDS in visibleColumns) {
                Text(result.friends.size.toString(), modifier = Modifier.width(72.dp), style = MaterialTheme.typography.bodySmall)
            }
            if (SearchColumn.LENGTH in visibleColumns) {
                Text(searchColumnValueText(primary, result, SearchColumn.LENGTH), modifier = Modifier.width(84.dp), style = MaterialTheme.typography.bodySmall)
            }
            if (SearchColumn.QUALITY in visibleColumns) {
                Text(
                    searchColumnValueText(primary, result, SearchColumn.QUALITY),
                    modifier = Modifier.width(118.dp),
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (SearchColumn.BITRATE in visibleColumns) {
                Text(searchColumnValueText(primary, result, SearchColumn.BITRATE), modifier = Modifier.width(86.dp), style = MaterialTheme.typography.bodySmall)
            }
            if (SearchColumn.TRACK in visibleColumns) {
                Text(searchColumnValueText(primary, result, SearchColumn.TRACK), modifier = Modifier.width(72.dp), style = MaterialTheme.typography.bodySmall)
            }
            if (SearchColumn.ARTIST in visibleColumns) {
                Text(searchColumnValueText(primary, result, SearchColumn.ARTIST), modifier = Modifier.width(120.dp), style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            if (SearchColumn.ALBUM in visibleColumns) {
                Text(searchColumnValueText(primary, result, SearchColumn.ALBUM), modifier = Modifier.width(120.dp), style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            if (SearchColumn.GENRE in visibleColumns) {
                Text(searchColumnValueText(primary, result, SearchColumn.GENRE), modifier = Modifier.width(96.dp), style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            if (SearchColumn.YEAR in visibleColumns) {
                Text(searchColumnValueText(primary, result, SearchColumn.YEAR), modifier = Modifier.width(72.dp), style = MaterialTheme.typography.bodySmall)
            }
            if (SearchColumn.AUTHOR in visibleColumns) {
                Text(searchColumnValueText(primary, result, SearchColumn.AUTHOR), modifier = Modifier.width(120.dp), style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            if (SearchColumn.COMPANY in visibleColumns) {
                Text(searchColumnValueText(primary, result, SearchColumn.COMPANY), modifier = Modifier.width(120.dp), style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            if (SearchColumn.PLATFORM in visibleColumns) {
                Text(searchColumnValueText(primary, result, SearchColumn.PLATFORM), modifier = Modifier.width(100.dp), style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            if (SearchColumn.DESCRIPTION in visibleColumns) {
                Text(searchColumnValueText(primary, result, SearchColumn.DESCRIPTION), modifier = Modifier.width(180.dp), style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            if (SearchColumn.FILES in visibleColumns) {
                Text(searchColumnValueText(primary, result, SearchColumn.FILES), modifier = Modifier.width(68.dp), style = MaterialTheme.typography.bodySmall)
            }
            if (SearchColumn.TRACKERS in visibleColumns) {
                Text(searchColumnValueText(primary, result, SearchColumn.TRACKERS), modifier = Modifier.width(82.dp), style = MaterialTheme.typography.bodySmall)
            }
            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(Icons.Rounded.MoreVert, contentDescription = "Actions")
                }
                DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                    val selectedResults = controller.selectedSearchResults(tab)
                    val multiSelection = selectedResults.size > 1
                    val actionResults = selectedResults.ifEmpty { listOf(result) }
                    val browseTargets = presentation.browseTargets
                    val blockTargets = presentation.blockTargets
                    val magnetLinks = actionResults.mapNotNull(controller::searchResultMagnetLink)
                    DropdownMenuItem(
                        text = { Text(if (multiSelection) "Download Selected" else "Download") },
                        onClick = {
                            menuExpanded = false
                            controller.downloadSelectedSearchResults(tab)
                        }
                    )
                    if (!multiSelection) {
                        DropdownMenuItem(
                            text = { Text("Download As…") },
                            onClick = {
                                menuExpanded = false
                                controller.downloadSearchResultAs(tab, result)
                            }
                        )
                    }
                    if (!multiSelection && controller.canChatFromSearchResult(result)) {
                        DropdownMenuItem(
                            text = { Text("Chat") },
                            onClick = {
                                menuExpanded = false
                                controller.chatFromSearchResult(result)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Browse Friend") },
                            onClick = {
                                menuExpanded = false
                                controller.browseFriendLibrary(result.friends.first().id)
                            }
                        )
                    }
                    when {
                        controller.canUnmarkSearchResultsAsSpam(actionResults) -> {
                            DropdownMenuItem(
                                text = { Text(if (multiSelection) "Unmark Selected as Spam" else "Unmark Spam") },
                                onClick = {
                                    menuExpanded = false
                                    controller.unmarkSearchResultsAsSpam(tab, actionResults)
                                }
                            )
                        }

                        controller.canMarkSearchResultsAsSpam(actionResults) -> {
                            DropdownMenuItem(
                                text = { Text(if (multiSelection) "Mark Selected as Spam" else "Mark as Spam") },
                                onClick = {
                                    menuExpanded = false
                                    controller.markSearchResultsAsSpam(tab, actionResults)
                                }
                            )
                        }
                    }
                    if (!multiSelection) {
                        BrowseSourceMenuItems(controller, browseTargets) { menuExpanded = false }
                        BlockUserMenuItems(controller, blockTargets) { menuExpanded = false }
                    }
                    if (!multiSelection && controller.canShowSimilarResults(result)) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    if (controller.areSimilarResultsExpanded(tab, result)) {
                                        "Hide Similar Files"
                                    } else {
                                        "Show Similar Files"
                                    }
                                )
                            },
                            onClick = {
                                menuExpanded = false
                                controller.toggleSimilarResults(tab, result)
                            }
                        )
                    }
                    if (!multiSelection) {
                        jumpTargets.forEach { target ->
                            DropdownMenuItem(
                                text = { Text(showInTargetLabel(target)) },
                                onClick = {
                                    menuExpanded = false
                                    controller.showSearchResultInTarget(result, target)
                                }
                            )
                        }
                    }
                    if (!multiSelection) {
                        DropdownMenuItem(
                            text = { Text("View File Info") },
                            onClick = {
                                menuExpanded = false
                                controller.showSearchFileInfo(result)
                            }
                        )
                    }
                    if (magnetLinks.isNotEmpty()) {
                        DropdownMenuItem(
                            text = { Text(if (multiSelection) "Copy Links" else "Copy Link") },
                            leadingIcon = { Icon(Icons.Rounded.Link, contentDescription = null) },
                            onClick = {
                                menuExpanded = false
                                copyTextToClipboard(magnetLinks.joinToString("\n"))
                            }
                        )
                    }
                    DropdownMenuItem(
                        text = { Text(if (multiSelection) "Copy Names" else "Copy Name") },
                        leadingIcon = { Icon(Icons.Rounded.ContentCopy, contentDescription = null) },
                        onClick = {
                            menuExpanded = false
                            copyTextToClipboard(
                                if (multiSelection) {
                                    selectedResults.joinToString("\n") { it.fileName }
                                } else {
                                    result.fileName
                                }
                            )
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(if (multiSelection) "Copy URNs" else "Copy URN") },
                        leadingIcon = { Icon(Icons.Rounded.Link, contentDescription = null) },
                        onClick = {
                            menuExpanded = false
                            copyTextToClipboard(
                                if (multiSelection) {
                                    selectedResults.joinToString("\n") { searchResultKey(it) }
                                } else {
                                    searchResultKey(result)
                                }
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchResultDetails(
    controller: ComposeAppController,
    tab: SearchTabSession,
    result: GroupedSearchResult
) {
    val primary = result.searchResults.firstOrNull()
    val presentation = controller.searchResultPresentation(result)
    val browsableCount = presentation.browsableCount
    val jumpTargets = presentation.jumpTargets
    val blockTargets = presentation.blockTargets
    val identity = presentation.identity
    val magnetLink = controller.searchResultMagnetLink(result)
    val canUnmarkSpam = controller.canUnmarkSearchResultsAsSpam(listOf(result))
    val canMarkSpam = controller.canMarkSearchResultsAsSpam(listOf(result))
    val showSimilarFiles = controller.areSimilarResultsExpanded(tab, result)
    val torrent = primary?.getProperty(FilePropertyKey.TORRENT) as? Torrent
    var torrentRefreshEpoch by remember(torrent?.sha1) { mutableIntStateOf(0) }
    val torrentState = rememberTorrentInspectorState(
        controller = controller,
        torrent = torrent,
        refreshEpoch = torrentRefreshEpoch,
        includeActivity = false,
        includePieces = false
    )

    Column(
        modifier = Modifier.fillMaxSize().padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        FileIdentityHeaderCard(
            title = identity.title,
            subtitle = identity.subtitle,
            icon = identity.icon,
            tertiary = "${result.sources.size} source(s) · ${result.friends.size} friend source(s)"
        )

        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { controller.downloadSearchResult(tab, result) }) {
                Icon(Icons.Rounded.Download, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("Download")
            }
            OutlinedButton(onClick = { controller.downloadSearchResultAs(tab, result) }) { Text("Download As") }
            if (jumpTargets.isNotEmpty()) {
                LibraryJumpMenuButton(
                    label = if (jumpTargets.size == 1) showInTargetLabel(jumpTargets.first()) else "Show In",
                    targets = jumpTargets,
                    onJump = { controller.showSearchResultInTarget(result, it) }
                )
            }
            if (controller.canChatFromSearchResult(result)) {
                OutlinedButton(onClick = { controller.chatFromSearchResult(result) }) {
                    Icon(Icons.Rounded.Forum, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Chat")
                }
            }
            BrowseSourceButton(controller, presentation.browseTargets)
            BlockUsersButton(controller, blockTargets)
            when {
                canUnmarkSpam -> OutlinedButton(onClick = { controller.unmarkSearchResultsAsSpam(tab, listOf(result)) }) { Text("Unmark Spam") }

                canMarkSpam -> OutlinedButton(onClick = { controller.markSearchResultsAsSpam(tab, listOf(result)) }) { Text("Mark as Spam") }
            }
            if (controller.canShowSimilarResults(result)) {
                OutlinedButton(onClick = { controller.toggleSimilarResults(tab, result) }) {
                    Text(if (showSimilarFiles) "Hide Similar Files" else "Show Similar Files")
                }
            }
            OutlinedButton(onClick = { controller.showSearchFileInfo(result) }) { Text("File Info") }
            magnetLink?.let {
                OutlinedButton(onClick = { copyTextToClipboard(it) }) {
                    Icon(Icons.Rounded.Link, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Copy Link")
                }
            }
            OutlinedButton(onClick = { copyTextToClipboard(result.fileName) }) {
                Icon(Icons.Rounded.ContentCopy, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("Copy Name")
            }
        }

        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            MetricBadge("Tab Type", searchTypeLabel(tab.searchType))
            MetricBadge("Anonymous", if (result.isAnonymous) "Yes" else "No")
            MetricBadge("Browseable Sources", browsableCount.toString())
            MetricBadge("Friend Sources", result.friends.size.toString())
            MetricBadge("URN", compactResultKey(searchResultKey(result)))
        }

        if (primary != null) {
            SearchSourceDetails(controller, result, primary)
        }

        torrentState.details?.let { details ->
            TorrentDetailsSection(
                controller = controller,
                details = details,
                activity = null,
                pieces = null,
                allowEditing = false,
                onRefreshRequested = { torrentRefreshEpoch += 1 }
            )
        }

        if (showSimilarFiles) {
            SimilarSearchResultsSection(controller, result)
        }

        if (result.friends.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Friend sources", fontWeight = FontWeight.SemiBold)
                result.friends.forEach { friend ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(friend.renderName, fontWeight = FontWeight.Medium)
                                Text(friend.id, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                if (controller.canChatWithFriend(friend.id)) {
                                    TextButton(onClick = { controller.chatWithFriend(friend.id) }) { Text("Chat") }
                                }
                                TextButton(onClick = { controller.browseFriendLibrary(friend.id) }) { Text("Browse") }
                            }
                        }
                    }
                }
            }
        } else {
            Text(
                "This result is coming from the wider network rather than a signed-in friend source.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun MultiSearchSelectionDetails(
    controller: ComposeAppController,
    tab: SearchTabSession,
    results: List<GroupedSearchResult>
) {
    val friendSources = results.sumOf { it.friends.size }
    val browsableResults = results.count { result ->
        result.searchResults.any { it.source.isBrowseHostEnabled }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(selectedSummaryLabel(results.size, "result"), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        Text(
            "Bulk actions apply to all selected search rows in this tab.",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { controller.downloadSelectedSearchResults(tab) }) {
                Icon(Icons.Rounded.Download, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("Download Selected")
            }
            OutlinedButton(
                onClick = {
                    copyTextToClipboard(results.joinToString("\n") { it.fileName })
                }
            ) {
                Icon(Icons.Rounded.ContentCopy, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("Copy Names")
            }
            OutlinedButton(
                onClick = {
                    copyTextToClipboard(results.joinToString("\n") { searchResultKey(it) })
                }
            ) {
                Icon(Icons.Rounded.Link, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("Copy URNs")
            }
            TextButton(onClick = { controller.clearSearchSelection(tab) }) {
                Text("Clear Selection")
            }
        }

        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            MetricBadge("Tab Type", searchTypeLabel(tab.searchType))
            MetricBadge("Selected", results.size.toString())
            MetricBadge("Friend Sources", friendSources.toString())
            MetricBadge("Browsable", browsableResults.toString())
        }

        Text(
            results.take(8).joinToString("\n") { "\u2022 ${it.fileName}" },
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (results.size > 8) {
            Text(
                "…and ${results.size - 8} more selected results.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun SearchSourceDetails(controller: ComposeAppController, result: GroupedSearchResult, primary: SearchResult) {
    val source = primary.source
    val sourceFriend = source.friendPresence?.friend
    val chatFriend = sourceFriend?.takeIf { source.isChatEnabled && controller.canChatWithFriend(it.id) }
    val browseTargets = controller.searchResultBrowseTargets(result)
    val blockTargets = controller.searchResultBlockTargets(result)

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Primary source", fontWeight = FontWeight.SemiBold)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            BrowseSourceButton(controller, browseTargets)
            BlockUsersButton(controller, blockTargets)
            if (chatFriend != null) {
                OutlinedButton(onClick = { controller.chatWithFriend(chatFriend.id) }) {
                    Text("Chat ${chatFriend.renderName}")
                }
            }
        }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            MetricBadge("Browse", if (source.isBrowseHostEnabled) "Available" else "Unavailable")
            MetricBadge(
                "Chat",
                when {
                    chatFriend != null -> "Ready"
                    source.isChatEnabled -> "Sign In Required"
                    else -> "Unavailable"
                }
            )
            MetricBadge("Sharing", if (source.isSharingEnabled) "Enabled" else "Off")
            sourceFriend?.let { MetricBadge("Friend", it.renderName) }
        }
    }
}

@Composable
private fun TransfersScreen(controller: ComposeAppController) {
    LaunchedEffect(controller.currentScreen, controller.torrentEngineHealthState) {
        if (controller.currentScreen != ComposeScreen.Transfers) {
            return@LaunchedEffect
        }
        while (
            controller.currentScreen == ComposeScreen.Transfers &&
            controller.torrentEngineHealthState != TorrentEngineHealthState.READY
        ) {
            controller.refreshTorrentEngineHealthState()
            delay(1500)
        }
    }

    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Transfers", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    MetricBadge("Downloads", controller.downloadCount().toString())
                    MetricBadge("Uploads", controller.uploadCount().toString())
                    MetricBadge("Active", controller.activeDownloadCount().toString())
                    MetricBadge("Connection", friendlyName(controller.connectionStrength().name))
                    if (controller.showTotalBandwidth) {
                        MetricBadge("Down Rate", formatRate(controller.totalDownloadBandwidth()))
                        MetricBadge("Up Rate", formatRate(controller.totalUploadBandwidth()))
                    }
                }
            }
        }
        if (controller.torrentEngineHealthState != TorrentEngineHealthState.READY) {
            InlineStatusBanner(
                title = controller.torrentEngineHealthTitle(),
                message = controller.torrentEngineHealthMessage(),
                level = if (controller.torrentEngineHealthState == TorrentEngineHealthState.ERROR) {
                    OperationNoticeLevel.WARNING
                } else {
                    OperationNoticeLevel.INFO
                },
                actionLabel = "Transfer Options..."
            ) {
                controller.showTransferPreferences()
            }
        }
        TransferTray(controller, embedded = true)
    }
}

@Composable
private fun TransferTray(controller: ComposeAppController, embedded: Boolean) {
    val downloadsEpoch = controller.downloadsEpoch
    val uploadsEpoch = controller.uploadsEpoch
    val downloads = controller.visibleDownloads()
    val uploads = controller.visibleUploads()
    val downloadCount = controller.downloadCount()
    val uploadCount = controller.uploadCount()
    val selectedDownload = controller.selectedDownloadItem()
    val selectedUpload = controller.selectedUploadItem()
    val uploadsTabVisible = controller.currentScreen == ComposeScreen.Transfers || controller.showUploadsInTray
    val selectedIndex = if (!uploadsTabVisible || controller.trayMode == TransferTrayMode.DOWNLOADS) 0 else 1
    var dropTargetActive by remember { mutableStateOf(false) }
    val transferDropTarget = remember {
        object : DragAndDropTarget {
            override fun onDrop(event: DragAndDropEvent): Boolean {
                dropTargetActive = false
                val payload = extractComposeDropPayload(event) as? ComposeDropPayload.SearchResults ?: return false
                controller.downloadDroppedSearchResults(payload.tabId, payload.resultKeys)
                return true
            }

            override fun onEntered(event: DragAndDropEvent) {
                dropTargetActive = acceptsSearchDownloadDrop(event)
            }

            override fun onExited(event: DragAndDropEvent) {
                dropTargetActive = false
            }

            override fun onEnded(event: DragAndDropEvent) {
                dropTargetActive = false
            }
        }
    }

    LaunchedEffect(
        controller.trayMode,
        downloadsEpoch,
        uploadsEpoch,
        controller.downloadFilterMode,
        controller.uploadFilterMode,
        controller.downloadSortMode,
        controller.uploadSortMode,
        controller.selectedDownloadUrn,
        controller.selectedUploadUrn
    ) {
        if (controller.trayMode == TransferTrayMode.DOWNLOADS) {
            when {
                downloads.isEmpty() -> controller.clearDownloadSelection()
            }
        } else {
            when {
                uploads.isEmpty() -> controller.clearUploadSelection()
            }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxSize()
            .dragAndDropTarget(
                shouldStartDragAndDrop = ::acceptsSearchDownloadDrop,
                target = transferDropTarget
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (dropTargetActive) {
                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        if (embedded) "Transfers" else "Transfer Tray",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        if (dropTargetActive) {
                            "Drop search results here to start downloading them."
                        } else {
                            "Watch current downloads and uploads here."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    MetricBadge("Downloads", downloadCount.toString())
                    MetricBadge("Uploads", uploadCount.toString())
                    if (controller.showTotalBandwidth) {
                        MetricBadge("Down Rate", formatRate(controller.totalDownloadBandwidth()))
                        MetricBadge("Up Rate", formatRate(controller.totalUploadBandwidth()))
                    }
                }
            }
            PrimaryTabRow(selectedTabIndex = selectedIndex) {
                Tab(
                    selected = selectedIndex == 0,
                    onClick = { controller.trayMode = TransferTrayMode.DOWNLOADS },
                    text = { Text("Downloads (${downloads.size})") }
                )
                if (uploadsTabVisible) {
                    Tab(
                        selected = selectedIndex == 1,
                        onClick = { controller.trayMode = TransferTrayMode.UPLOADS },
                        text = { Text("Uploads (${uploads.size})") }
                    )
                }
            }
            if (controller.trayMode == TransferTrayMode.DOWNLOADS || !uploadsTabVisible) {
                val _ignored = downloadsEpoch
                DownloadWorkspaceHeader(controller, selectedDownload)
                TransferFilterChips(
                    current = controller.downloadFilterMode,
                    onSelect = { controller.updateDownloadFilterMode(it) }
                )
                TransferList(controller, downloads)
            } else {
                val _ignored = uploadsEpoch
                UploadWorkspaceHeader(controller, selectedUpload)
                TransferFilterChips(
                    current = controller.uploadFilterMode,
                    onSelect = { controller.updateUploadFilterMode(it) }
                )
                UploadList(controller, uploads)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DownloadWorkspaceHeader(controller: ComposeAppController, selected: DownloadItem?) {
    val selectedItems = controller.selectedDownloadItems()
    val jumpTargets = selected?.let(controller::downloadJumpTargets).orEmpty()
    val selectedIdentity = selected?.let(controller::downloadItemIdentity)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            FilledTonalButton(onClick = { controller.pauseAllDownloads() }) { Text("Pause Active") }
            OutlinedButton(onClick = { controller.resumeVisibleDownloads() }) { Text("Resume Visible") }
            OutlinedButton(onClick = { controller.clearFinishedDownloads() }) { Text("Clear Finished") }
            TextButton(onClick = { controller.clearProblemDownloads() }) { Text("Clear Stalled") }
            DownloadsHeaderMenuButton(controller)
            ColumnVisibilityMenu(
                entries = DownloadColumn.entries.map { column ->
                    ColumnToggleEntry(
                        label = friendlyName(column.name),
                        visible = column in controller.visibleDownloadColumns
                    ) {
                        controller.toggleDownloadColumn(column)
                    }
                }
            )
        }
        if (selectedItems.isNotEmpty() && selected != null) {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (selectedItems.size == 1) {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            FileIdentityIcon(icon = selectedIdentity?.icon, modifier = Modifier.size(20.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    selectedIdentity?.title ?: selected.fileName,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    listOf(selectedIdentity?.subtitle.orEmpty(), downloadRowStatusMessage(selected))
                                        .filter(String::isNotBlank)
                                        .joinToString(" · "),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        Text(selectedSummaryLabel(selectedItems.size, "download"), fontWeight = FontWeight.SemiBold)
                        Text(
                            "Actions below apply to the selected downloads.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        when {
                            selectedItems.any { it.state.isPausable } -> FilledTonalButton(onClick = { controller.pauseSelectedDownloads() }) { Text("Pause") }
                            selectedItems.any { it.state.isResumable || (it.state == DownloadState.ERROR && it.isTryAgainEnabled) } ->
                                FilledTonalButton(onClick = { controller.resumeSelectedDownloads() }) { Text("Resume") }
                        }
                        if (selectedItems.any { it.isLaunchable }) {
                            OutlinedButton(onClick = { controller.openSelectedDownloads() }) {
                                Text(controller.selectedDownloadsOpenActionLabel())
                            }
                        }
                        OutlinedButton(onClick = { controller.revealSelectedDownloads() }) { Text("Locate on Disk") }
                        if (selectedItems.any { it.isLaunchable }) {
                            OutlinedButton(onClick = { controller.locateSelectedDownloads() }) { Text("Locate in My Files") }
                        }
                        if (selectedItems.size == 1) {
                            OutlinedButton(onClick = { controller.showDownloadFileInfo(selected) }) { Text("File Info") }
                            OutlinedButton(onClick = { controller.retryOrSearchAgainForDownload(selected) }) {
                                Text(controller.downloadRetryActionLabel(selected))
                            }
                            if (controller.canChangeDownloadLocation(selected)) {
                                OutlinedButton(onClick = { controller.changeDownloadLocation(selected) }) { Text("Change Location") }
                            }
                            BrowseSourceButton(controller, controller.downloadBrowseTargets(selected))
                            BlockUsersButton(controller, controller.downloadBlockTargets(selected))
                        }
                        if (selectedItems.size == 1 && jumpTargets.isNotEmpty()) {
                            LibraryJumpMenuButton(
                                label = if (jumpTargets.size == 1) showInTargetLabel(jumpTargets.first()) else "Show In",
                                targets = jumpTargets,
                                onJump = { target -> selected?.let { controller.showDownloadInTarget(it, target) } }
                            )
                        }
                        if (controller.canAddSelectedDownloadsToCollection()) {
                            CollectionMenuButton(
                                label = "Add to Collection",
                                collections = controller.availableCollections(),
                                onAddToCollection = controller::addSelectedDownloadsToCollection
                            )
                        }
                        TextButton(onClick = { controller.removeSelectedDownloads() }) {
                            Text(controller.downloadRemoveActionLabel(selectedItems))
                        }
                        TextButton(onClick = { controller.clearDownloadSelection() }) {
                            Text("Clear Selection")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun UploadWorkspaceHeader(controller: ComposeAppController, selected: UploadItem?) {
    val selectedItems = controller.selectedUploadItems()
    val jumpTargets = selected?.let(controller::uploadJumpTargets).orEmpty()
    val selectedIdentity = selected?.let(controller::uploadItemIdentity)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            FilledTonalButton(onClick = { controller.pauseActiveUploads() }) { Text("Pause Active") }
            OutlinedButton(onClick = { controller.resumePausedUploads() }) { Text("Resume Paused") }
            OutlinedButton(onClick = { controller.clearFinishedUploads() }) { Text("Clear Finished") }
            UploadsHeaderMenuButton(controller)
            ColumnVisibilityMenu(
                entries = UploadColumn.entries.map { column ->
                    ColumnToggleEntry(
                        label = friendlyName(column.name),
                        visible = column in controller.visibleUploadColumns
                    ) {
                        controller.toggleUploadColumn(column)
                    }
                }
            )
        }
        if (selectedItems.isNotEmpty() && selected != null) {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (selectedItems.size == 1) {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            FileIdentityIcon(icon = selectedIdentity?.icon, modifier = Modifier.size(20.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    selectedIdentity?.title ?: selected.fileName,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    listOf(selectedIdentity?.subtitle.orEmpty(), uploadRowStatusMessage(selected))
                                        .filter(String::isNotBlank)
                                        .joinToString(" · "),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        Text(selectedSummaryLabel(selectedItems.size, "upload"), fontWeight = FontWeight.SemiBold)
                        Text(
                            "Actions below apply to the selected uploads.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (selectedItems.any { it.state == UploadState.UPLOADING }) {
                            FilledTonalButton(onClick = { controller.pauseSelectedUploads() }) { Text("Pause") }
                        } else if (selectedItems.any { it.state == UploadState.PAUSED }) {
                            FilledTonalButton(onClick = { controller.resumeSelectedUploads() }) { Text("Resume") }
                        }
                        if (selectedItems.any(controller::canOpenUploadItem)) {
                            OutlinedButton(onClick = { controller.openSelectedUploads() }) {
                                Text(controller.selectedUploadsOpenActionLabel())
                            }
                            OutlinedButton(onClick = { controller.revealSelectedUploads() }) { Text("Locate on Disk") }
                            OutlinedButton(onClick = { controller.locateSelectedUploads() }) { Text("Locate in My Files") }
                        }
                        if (selectedItems.size == 1) {
                            if (controller.canOpenUploadItem(selected)) {
                                OutlinedButton(onClick = { controller.showUploadFileInfo(selected) }) { Text("File Info") }
                            }
                            BrowseSourceButton(controller, controller.uploadBrowseTargets(selected))
                            BlockUsersButton(controller, controller.uploadBlockTargets(selected))
                        }
                        if (selectedItems.size == 1 && jumpTargets.isNotEmpty()) {
                            LibraryJumpMenuButton(
                                label = if (jumpTargets.size == 1) showInTargetLabel(jumpTargets.first()) else "Show In",
                                targets = jumpTargets,
                                onJump = { target -> selected?.let { controller.showUploadInTarget(it, target) } }
                            )
                        }
                        if (controller.canAddSelectedUploadsToCollection()) {
                            CollectionMenuButton(
                                label = "Add to Collection",
                                collections = controller.availableCollections(),
                                onAddToCollection = controller::addSelectedUploadsToCollection
                            )
                        }
                        TextButton(onClick = { controller.removeSelectedUploads() }) {
                            Text(controller.uploadRemoveActionLabel(selectedItems))
                        }
                        TextButton(onClick = { controller.clearUploadSelection() }) {
                            Text("Clear Selection")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TransferFilterChips(current: TransferFilterMode, onSelect: (TransferFilterMode) -> Unit) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        TransferFilterMode.entries.forEach { mode ->
            FilterChip(
                selected = current == mode,
                onClick = { onSelect(mode) },
                label = { Text(friendlyName(mode.name)) }
            )
        }
    }
}

@Composable
private fun TransferList(controller: ComposeAppController, items: List<DownloadItem>) {
    val tableFocusRequester = remember { FocusRequester() }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .focusRequester(tableFocusRequester)
            .focusable()
            .onPreviewKeyEvent { event ->
                handleTableKeyEvent(
                    event = event,
                    moveSelection = controller::moveDownloadSelection,
                    extendSelection = controller::extendDownloadSelection,
                    selectAll = controller::selectAllVisibleDownloads,
                    activateSelection = controller::activateSelectedDownloadItem
                )
            }
    ) {
        HeaderContextMenuArea(
            menuContent = {
                DownloadSortMode.entries.forEach { mode ->
                    DropdownMenuItem(
                        text = { Text("Sort by ${friendlyName(mode.name)}") },
                        onClick = {
                            controller.toggleDownloadSort(mode)
                            dismiss()
                        }
                    )
                }
                HorizontalDivider()
                DownloadColumn.entries.forEach { column ->
                    DropdownMenuItem(
                        text = {
                            Text("${if (column in controller.visibleDownloadColumns) "Hide" else "Show"} ${friendlyName(column.name)}")
                        },
                        onClick = {
                            controller.toggleDownloadColumn(column)
                            dismiss()
                        }
                    )
                }
            }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = items.isNotEmpty() && items.all { it.urn.toString() in controller.selectedDownloadUrns },
                    onCheckedChange = { checked ->
                        if (checked) {
                            controller.selectAllVisibleDownloads()
                        } else {
                            controller.clearDownloadSelection()
                        }
                    }
                )
                if (DownloadColumn.NAME in controller.visibleDownloadColumns) {
                    SortableHeaderText(
                        label = "Name",
                        active = controller.downloadSortMode == DownloadSortMode.NAME,
                        descending = controller.downloadSortDescending,
                        modifier = Modifier.weight(1.25f)
                    ) {
                        controller.toggleDownloadSort(DownloadSortMode.NAME)
                    }
                }
                if (DownloadColumn.ORDER_ADDED in controller.visibleDownloadColumns) {
                    SortableHeaderText(
                        label = "Order Added",
                        active = controller.downloadSortMode == DownloadSortMode.ORDER_ADDED,
                        descending = controller.downloadSortDescending,
                        modifier = Modifier.width(136.dp)
                    ) {
                        controller.toggleDownloadSort(DownloadSortMode.ORDER_ADDED)
                    }
                }
                if (DownloadColumn.TIME_LEFT in controller.visibleDownloadColumns) {
                    SortableHeaderText(
                        label = "Time Left",
                        active = controller.downloadSortMode == DownloadSortMode.TIME_LEFT,
                        descending = controller.downloadSortDescending,
                        modifier = Modifier.width(92.dp)
                    ) {
                        controller.toggleDownloadSort(DownloadSortMode.TIME_LEFT)
                    }
                }
                if (DownloadColumn.FILE_TYPE in controller.visibleDownloadColumns) {
                    SortableHeaderText(
                        label = "File Type",
                        active = controller.downloadSortMode == DownloadSortMode.FILE_TYPE,
                        descending = controller.downloadSortDescending,
                        modifier = Modifier.width(96.dp)
                    ) {
                        controller.toggleDownloadSort(DownloadSortMode.FILE_TYPE)
                    }
                }
                if (DownloadColumn.EXTENSION in controller.visibleDownloadColumns) {
                    SortableHeaderText(
                        label = "Extension",
                        active = controller.downloadSortMode == DownloadSortMode.EXTENSION,
                        descending = controller.downloadSortDescending,
                        modifier = Modifier.width(92.dp)
                    ) {
                        controller.toggleDownloadSort(DownloadSortMode.EXTENSION)
                    }
                }
                if (DownloadColumn.STATUS in controller.visibleDownloadColumns) {
                    SortableHeaderText(
                        label = "Status",
                        active = controller.downloadSortMode == DownloadSortMode.STATUS,
                        descending = controller.downloadSortDescending,
                        modifier = Modifier.width(112.dp)
                    ) {
                        controller.toggleDownloadSort(DownloadSortMode.STATUS)
                    }
                }
                if (DownloadColumn.PROGRESS in controller.visibleDownloadColumns) {
                    SortableHeaderText(
                        label = "Progress",
                        active = controller.downloadSortMode == DownloadSortMode.PROGRESS,
                        descending = controller.downloadSortDescending,
                        modifier = Modifier.width(220.dp)
                    ) {
                        controller.toggleDownloadSort(DownloadSortMode.PROGRESS)
                    }
                }
                if (DownloadColumn.RATE in controller.visibleDownloadColumns) {
                    SortableHeaderText(
                        label = "Rate",
                        active = controller.downloadSortMode == DownloadSortMode.RATE,
                        descending = controller.downloadSortDescending,
                        modifier = Modifier.width(90.dp)
                    ) {
                        controller.toggleDownloadSort(DownloadSortMode.RATE)
                    }
                }
                if (DownloadColumn.SOURCES in controller.visibleDownloadColumns) {
                    SortableHeaderText(
                        label = "Sources",
                        active = controller.downloadSortMode == DownloadSortMode.SOURCES,
                        descending = controller.downloadSortDescending,
                        modifier = Modifier.width(72.dp)
                    ) {
                        controller.toggleDownloadSort(DownloadSortMode.SOURCES)
                    }
                }
                Spacer(Modifier.width(54.dp))
            }
        }
        HorizontalDivider()
        if (items.isEmpty()) {
            EmptyState(
                icon = Icons.Rounded.Download,
                title = when (controller.downloadFilterMode) {
                    TransferFilterMode.ALL -> "No downloads"
                    TransferFilterMode.ACTIVE -> "No active downloads"
                    TransferFilterMode.FINISHED -> "No finished downloads"
                    TransferFilterMode.STALLED -> "No stalled downloads"
                },
                body = when (controller.downloadFilterMode) {
                    TransferFilterMode.ALL -> "Start a download from search, a torrent file, or a link to see it here."
                    TransferFilterMode.ACTIVE -> "Active downloads appear here."
                    TransferFilterMode.FINISHED -> "Completed downloads stay here until you clear them."
                    TransferFilterMode.STALLED -> "Problem downloads appear here so you can retry or remove them."
                }
            )
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(items, key = { it.urn.toString() }) { item ->
                    DownloadTransferRow(
                        controller = controller,
                        item = item,
                        selected = item.urn.toString() in controller.selectedDownloadUrns,
                        primarySelected = controller.selectedDownloadUrn == item.urn.toString(),
                        visibleColumns = controller.visibleDownloadColumns,
                        onSelect = { extendSelection, toggleSelection ->
                            controller.selectDownloadItem(item, extendSelection, toggleSelection)
                            tableFocusRequester.requestFocus()
                        },
                        onToggleChecked = {
                            controller.toggleDownloadItemChecked(item)
                            tableFocusRequester.requestFocus()
                        }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun UploadList(controller: ComposeAppController, items: List<UploadItem>) {
    val tableFocusRequester = remember { FocusRequester() }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .focusRequester(tableFocusRequester)
            .focusable()
            .onPreviewKeyEvent { event ->
                handleTableKeyEvent(
                    event = event,
                    moveSelection = controller::moveUploadSelection,
                    extendSelection = controller::extendUploadSelection,
                    selectAll = controller::selectAllVisibleUploads,
                    activateSelection = controller::activateSelectedUploadItem
                )
            }
    ) {
        HeaderContextMenuArea(
            menuContent = {
                UploadSortMode.entries.forEach { mode ->
                    DropdownMenuItem(
                        text = { Text("Sort by ${friendlyName(mode.name)}") },
                        onClick = {
                            controller.toggleUploadSort(mode)
                            dismiss()
                        }
                    )
                }
                HorizontalDivider()
                UploadColumn.entries.forEach { column ->
                    DropdownMenuItem(
                        text = {
                            Text("${if (column in controller.visibleUploadColumns) "Hide" else "Show"} ${friendlyName(column.name)}")
                        },
                        onClick = {
                            controller.toggleUploadColumn(column)
                            dismiss()
                        }
                    )
                }
            }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = items.isNotEmpty() && items.all { it.urn.toString() in controller.selectedUploadUrns },
                    onCheckedChange = { checked ->
                        if (checked) {
                            controller.selectAllVisibleUploads()
                        } else {
                            controller.clearUploadSelection()
                        }
                    }
                )
                if (UploadColumn.NAME in controller.visibleUploadColumns) {
                    SortableHeaderText(
                        label = "Name",
                        active = controller.uploadSortMode == UploadSortMode.NAME,
                        descending = controller.uploadSortDescending,
                        modifier = Modifier.weight(1.25f)
                    ) {
                        controller.toggleUploadSort(UploadSortMode.NAME)
                    }
                }
                if (UploadColumn.ORDER_STARTED in controller.visibleUploadColumns) {
                    SortableHeaderText(
                        label = "Order Started",
                        active = controller.uploadSortMode == UploadSortMode.ORDER_STARTED,
                        descending = controller.uploadSortDescending,
                        modifier = Modifier.width(136.dp)
                    ) {
                        controller.toggleUploadSort(UploadSortMode.ORDER_STARTED)
                    }
                }
                if (UploadColumn.TIME_LEFT in controller.visibleUploadColumns) {
                    SortableHeaderText(
                        label = "Time Left",
                        active = controller.uploadSortMode == UploadSortMode.TIME_LEFT,
                        descending = controller.uploadSortDescending,
                        modifier = Modifier.width(92.dp)
                    ) {
                        controller.toggleUploadSort(UploadSortMode.TIME_LEFT)
                    }
                }
                if (UploadColumn.FILE_TYPE in controller.visibleUploadColumns) {
                    SortableHeaderText(
                        label = "File Type",
                        active = controller.uploadSortMode == UploadSortMode.FILE_TYPE,
                        descending = controller.uploadSortDescending,
                        modifier = Modifier.width(96.dp)
                    ) {
                        controller.toggleUploadSort(UploadSortMode.FILE_TYPE)
                    }
                }
                if (UploadColumn.EXTENSION in controller.visibleUploadColumns) {
                    SortableHeaderText(
                        label = "Extension",
                        active = controller.uploadSortMode == UploadSortMode.EXTENSION,
                        descending = controller.uploadSortDescending,
                        modifier = Modifier.width(92.dp)
                    ) {
                        controller.toggleUploadSort(UploadSortMode.EXTENSION)
                    }
                }
                if (UploadColumn.USER_NAME in controller.visibleUploadColumns) {
                    SortableHeaderText(
                        label = "User Name",
                        active = controller.uploadSortMode == UploadSortMode.USER_NAME,
                        descending = controller.uploadSortDescending,
                        modifier = Modifier.width(132.dp)
                    ) {
                        controller.toggleUploadSort(UploadSortMode.USER_NAME)
                    }
                }
                if (UploadColumn.STATUS in controller.visibleUploadColumns) {
                    SortableHeaderText(
                        label = "Status",
                        active = controller.uploadSortMode == UploadSortMode.STATUS,
                        descending = controller.uploadSortDescending,
                        modifier = Modifier.width(112.dp)
                    ) {
                        controller.toggleUploadSort(UploadSortMode.STATUS)
                    }
                }
                if (UploadColumn.UPLOADED in controller.visibleUploadColumns) {
                    SortableHeaderText(
                        label = "Uploaded",
                        active = controller.uploadSortMode == UploadSortMode.UPLOADED,
                        descending = controller.uploadSortDescending,
                        modifier = Modifier.width(220.dp)
                    ) {
                        controller.toggleUploadSort(UploadSortMode.UPLOADED)
                    }
                }
                if (UploadColumn.RATE in controller.visibleUploadColumns) {
                    SortableHeaderText(
                        label = "Rate",
                        active = controller.uploadSortMode == UploadSortMode.RATE,
                        descending = controller.uploadSortDescending,
                        modifier = Modifier.width(90.dp)
                    ) {
                        controller.toggleUploadSort(UploadSortMode.RATE)
                    }
                }
                if (UploadColumn.PEERS in controller.visibleUploadColumns) {
                    SortableHeaderText(
                        label = "Peers",
                        active = controller.uploadSortMode == UploadSortMode.PEERS,
                        descending = controller.uploadSortDescending,
                        modifier = Modifier.width(72.dp)
                    ) {
                        controller.toggleUploadSort(UploadSortMode.PEERS)
                    }
                }
                Spacer(Modifier.width(54.dp))
            }
        }
        HorizontalDivider()
        if (items.isEmpty()) {
            EmptyState(
                icon = Icons.Rounded.Upload,
                title = when (controller.uploadFilterMode) {
                    TransferFilterMode.ALL -> "No uploads"
                    TransferFilterMode.ACTIVE -> "No active uploads"
                    TransferFilterMode.FINISHED -> "No finished uploads"
                    TransferFilterMode.STALLED -> "No stalled uploads"
                },
                body = when (controller.uploadFilterMode) {
                    TransferFilterMode.ALL -> "Uploads appear here while files or torrents are being shared."
                    TransferFilterMode.ACTIVE -> "Active uploads appear here."
                    TransferFilterMode.FINISHED -> "Completed uploads stay here until you clear them."
                    TransferFilterMode.STALLED -> "Uploads with errors or stalled sessions appear here."
                }
            )
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(items, key = { it.urn.toString() }) { item ->
                    UploadTransferRow(
                        controller = controller,
                        item = item,
                        selected = item.urn.toString() in controller.selectedUploadUrns,
                        primarySelected = controller.selectedUploadUrn == item.urn.toString(),
                        visibleColumns = controller.visibleUploadColumns,
                        onSelect = { extendSelection, toggleSelection ->
                            controller.selectUploadItem(item, extendSelection, toggleSelection)
                            tableFocusRequester.requestFocus()
                        },
                        onToggleChecked = {
                            controller.toggleUploadItemChecked(item)
                            tableFocusRequester.requestFocus()
                        }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DownloadTransferRow(
    controller: ComposeAppController,
    item: DownloadItem,
    selected: Boolean,
    primarySelected: Boolean,
    visibleColumns: Set<DownloadColumn>,
    onSelect: (extendSelection: Boolean, toggleSelection: Boolean) -> Unit,
    onToggleChecked: () -> Unit
) {
    val progress = when {
        item.totalSize > 0L -> item.currentSize.toFloat() / item.totalSize.toFloat()
        else -> item.percentComplete / 100f
    }.coerceIn(0f, 1f)
    val isPlaying = item.isLaunchable &&
        item.launchableFile == controller.playerCurrentFile &&
        controller.playerState in setOf(PlayerState.PLAYING, PlayerState.SEEKING_PLAY, PlayerState.PAUSED)
    val jumpTargets = controller.downloadJumpTargets(item)
    val identity = controller.downloadItemIdentity(item)
    val statusMessage = downloadRowStatusMessage(item)
    val dragTransferData = externalFilesTransferData(controller.draggableDownloadFiles(item))
    var menuExpanded by remember(item.urn.toString()) { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .let { base ->
                if (dragTransferData != null) {
                    base.dragAndDropSource { dragTransferData }
                } else {
                    base
                }
            }
            .background(
                when {
                    primarySelected -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    selected -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                    else -> Color.Transparent
                }
            )
            .then(
                rememberSelectableRowModifier(
                    rowKey = item.urn.toString(),
                    onSelect = onSelect,
                    onActivate = { controller.activateSelectedDownloadItem() },
                    onContextRequest = {
                        controller.handleDownloadContextSelection(item)
                        menuExpanded = true
                    }
                )
            )
            .padding(horizontal = 10.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = selected,
            onCheckedChange = { onToggleChecked() }
        )
        if (DownloadColumn.NAME in visibleColumns) {
            Row(modifier = Modifier.weight(1.25f), verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FileIdentityIcon(icon = identity.icon, modifier = Modifier.size(18.dp))
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(identity.title, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(
                        listOf(identity.subtitle, if (isPlaying) "$statusMessage · Playing now" else statusMessage)
                            .filter(String::isNotBlank)
                            .joinToString(" · "),
                        color = when {
                            item.state == DownloadState.ERROR ||
                                item.state == DownloadState.STALLED ||
                                item.state == DownloadState.DANGEROUS -> MaterialTheme.colorScheme.error
                            isPlaying -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        style = MaterialTheme.typography.bodySmall
                    )
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        when {
                            item.state.isPausable -> TransferInlineActionChip("Pause") {
                                controller.pauseDownloadItem(item)
                            }

                            item.state.isResumable -> TransferInlineActionChip("Resume") {
                                controller.resumeDownloadItem(item)
                            }

                            item.state == DownloadState.ERROR || item.state == DownloadState.STALLED -> TransferInlineActionChip(
                                controller.downloadRetryActionLabel(item)
                            ) {
                                controller.retryOrSearchAgainForDownload(item)
                            }
                        }
                        if (item.isLaunchable) {
                            TransferInlineActionChip(controller.downloadOpenActionLabel(item)) {
                                controller.openDownloadItem(item)
                            }
                        }
                        TransferInlineActionChip("Locate on Disk") {
                            controller.revealDownloadItem(item)
                        }
                        if (item.isLaunchable) {
                            TransferInlineActionChip("Locate in My Files") {
                                controller.locateDownloadItem(item)
                            }
                        }
                        TransferInlineActionChip(controller.downloadRemoveActionLabel(listOf(item))) {
                            controller.removeDownloadItem(item)
                        }
                    }
                }
            }
        }
        if (DownloadColumn.ORDER_ADDED in visibleColumns) {
            Text(
                formatDateTimeCompact(item.startDate.time),
                modifier = Modifier.width(136.dp),
                style = MaterialTheme.typography.bodySmall
            )
        }
        if (DownloadColumn.TIME_LEFT in visibleColumns) {
            Text(
                formatDuration(item.remainingDownloadTime),
                modifier = Modifier.width(92.dp),
                style = MaterialTheme.typography.bodySmall
            )
        }
        if (DownloadColumn.FILE_TYPE in visibleColumns) {
            Text(item.category.getSingularName(), modifier = Modifier.width(96.dp), style = MaterialTheme.typography.bodySmall)
        }
        if (DownloadColumn.EXTENSION in visibleColumns) {
            Text(fileExtensionText(item.fileName).displayFallback(), modifier = Modifier.width(92.dp), style = MaterialTheme.typography.bodySmall)
        }
        if (DownloadColumn.STATUS in visibleColumns) {
            Column(modifier = Modifier.width(112.dp)) {
                Text(friendlyName(item.state.name), style = MaterialTheme.typography.bodySmall)
                Text(
                    formatDuration(item.remainingDownloadTime),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        if (DownloadColumn.PROGRESS in visibleColumns) {
            Column(modifier = Modifier.width(220.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
                Text(
                    "${formatBytes(item.currentSize)} of ${formatBytes(item.totalSize)}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        if (DownloadColumn.RATE in visibleColumns) {
            Text(formatRate(item.downloadSpeed), modifier = Modifier.width(90.dp), style = MaterialTheme.typography.bodySmall)
        }
        if (DownloadColumn.SOURCES in visibleColumns) {
            Text(item.downloadSourceCount.toString(), modifier = Modifier.width(72.dp), style = MaterialTheme.typography.bodySmall)
        }
        Box {
            IconButton(onClick = { menuExpanded = true }) {
                Icon(Icons.Rounded.MoreVert, contentDescription = "Actions")
            }
            DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                val selectedItems = controller.selectedDownloadItems()
                val multiSelection = selectedItems.size > 1
                val browseTargets = controller.downloadBrowseTargets(item)
                val blockTargets = controller.downloadBlockTargets(item)
                when {
                    selectedItems.any { it.state.isPausable } -> DropdownMenuItem(
                        text = { Text(if (multiSelection) "Pause Selected" else "Pause") },
                        onClick = {
                            menuExpanded = false
                            controller.pauseSelectedDownloads()
                        }
                    )
                    selectedItems.any { it.state.isResumable || (it.state == DownloadState.ERROR && it.isTryAgainEnabled) } -> DropdownMenuItem(
                        text = { Text(if (multiSelection) "Resume Selected" else "Resume") },
                        onClick = {
                            menuExpanded = false
                            controller.resumeSelectedDownloads()
                        }
                    )
                }
                if (selectedItems.any { it.isLaunchable }) {
                    DropdownMenuItem(
                        text = { Text(controller.selectedDownloadsOpenActionLabel()) },
                        onClick = {
                            menuExpanded = false
                            controller.openSelectedDownloads()
                        }
                    )
                }
                DropdownMenuItem(
                    text = { Text(if (multiSelection) "Locate Selected on Disk" else "Locate on Disk") },
                    onClick = {
                        menuExpanded = false
                        controller.revealSelectedDownloads()
                    }
                )
                if (!multiSelection) {
                    DropdownMenuItem(
                        text = { Text("View File Info") },
                        onClick = {
                            menuExpanded = false
                            controller.showDownloadFileInfo(item)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(controller.downloadRetryActionLabel(item)) },
                        onClick = {
                            menuExpanded = false
                            controller.retryOrSearchAgainForDownload(item)
                        }
                    )
                    if (controller.canChangeDownloadLocation(item)) {
                        DropdownMenuItem(
                            text = { Text("Change Location") },
                            onClick = {
                                menuExpanded = false
                                controller.changeDownloadLocation(item)
                            }
                        )
                    }
                    BrowseSourceMenuItems(controller, browseTargets) { menuExpanded = false }
                    BlockUserMenuItems(controller, blockTargets) { menuExpanded = false }
                }
                if (selectedItems.any { it.isLaunchable }) {
                    if (multiSelection) {
                        DropdownMenuItem(
                            text = { Text("Locate Selected in My Files") },
                            onClick = {
                                menuExpanded = false
                                controller.locateSelectedDownloads()
                            }
                        )
                    } else {
                        if (jumpTargets.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text("Locate in My Files") },
                                onClick = {
                                    menuExpanded = false
                                    controller.locateSelectedDownloads()
                                }
                            )
                        } else {
                            jumpTargets.forEach { target ->
                                DropdownMenuItem(
                                    text = { Text(showInTargetLabel(target)) },
                                    onClick = {
                                        menuExpanded = false
                                        controller.showDownloadInTarget(item, target)
                                    }
                                )
                            }
                        }
                    }
                }
                DropdownMenuItem(
                    text = { Text(controller.downloadRemoveActionLabel(selectedItems)) },
                    onClick = {
                        menuExpanded = false
                        controller.removeSelectedDownloads()
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun UploadTransferRow(
    controller: ComposeAppController,
    item: UploadItem,
    selected: Boolean,
    primarySelected: Boolean,
    visibleColumns: Set<UploadColumn>,
    onSelect: (extendSelection: Boolean, toggleSelection: Boolean) -> Unit,
    onToggleChecked: () -> Unit
) {
    val isPlaying = item.file == controller.playerCurrentFile &&
        controller.playerState in setOf(PlayerState.PLAYING, PlayerState.SEEKING_PLAY, PlayerState.PAUSED)
    val jumpTargets = controller.uploadJumpTargets(item)
    val identity = controller.uploadItemIdentity(item)
    val statusMessage = uploadRowStatusMessage(item)
    val dragTransferData = externalFilesTransferData(controller.draggableUploadFiles(item))
    var menuExpanded by remember(item.urn.toString()) { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .let { base ->
                if (dragTransferData != null) {
                    base.dragAndDropSource { dragTransferData }
                } else {
                    base
                }
            }
            .background(
                when {
                    primarySelected -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    selected -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                    else -> Color.Transparent
                }
            )
            .then(
                rememberSelectableRowModifier(
                    rowKey = item.urn.toString(),
                    onSelect = onSelect,
                    onActivate = { controller.activateSelectedUploadItem() },
                    onContextRequest = {
                        controller.handleUploadContextSelection(item)
                        menuExpanded = true
                    }
                )
            )
            .padding(horizontal = 10.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = selected,
            onCheckedChange = { onToggleChecked() }
        )
        if (UploadColumn.NAME in visibleColumns) {
            Row(modifier = Modifier.weight(1.25f), verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FileIdentityIcon(icon = identity.icon, modifier = Modifier.size(18.dp))
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(identity.title, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(
                        listOf(identity.subtitle, if (isPlaying) "$statusMessage · Playing now" else statusMessage)
                            .filter(String::isNotBlank)
                            .joinToString(" · "),
                        color = when {
                            item.state == UploadState.REQUEST_ERROR || item.state == UploadState.LIMIT_REACHED -> MaterialTheme.colorScheme.error
                            isPlaying -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        style = MaterialTheme.typography.bodySmall
                    )
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        when (item.state) {
                            UploadState.UPLOADING -> TransferInlineActionChip("Pause") {
                                controller.pauseUploadItem(item)
                            }

                            UploadState.PAUSED -> TransferInlineActionChip("Resume") {
                                controller.resumeUploadItem(item)
                            }

                            else -> Unit
                        }
                        if (controller.canOpenUploadItem(item)) {
                            TransferInlineActionChip(controller.uploadOpenActionLabel(item)) {
                                controller.openUploadItem(item)
                            }
                            TransferInlineActionChip("Locate on Disk") {
                                controller.revealUploadItem(item)
                            }
                            TransferInlineActionChip("Locate in My Files") {
                                controller.locateUploadItem(item)
                            }
                        }
                        TransferInlineActionChip(controller.uploadRemoveActionLabel(listOf(item))) {
                            controller.removeUploadItem(item)
                        }
                    }
                }
            }
        }
        if (UploadColumn.ORDER_STARTED in visibleColumns) {
            Text(
                formatDateTimeCompact(item.startTime),
                modifier = Modifier.width(136.dp),
                style = MaterialTheme.typography.bodySmall
            )
        }
        if (UploadColumn.TIME_LEFT in visibleColumns) {
            Text(
                formatDuration(item.remainingUploadTime),
                modifier = Modifier.width(92.dp),
                style = MaterialTheme.typography.bodySmall
            )
        }
        if (UploadColumn.FILE_TYPE in visibleColumns) {
            Text(item.category.getSingularName(), modifier = Modifier.width(96.dp), style = MaterialTheme.typography.bodySmall)
        }
        if (UploadColumn.EXTENSION in visibleColumns) {
            Text(fileExtensionText(item.fileName).displayFallback(), modifier = Modifier.width(92.dp), style = MaterialTheme.typography.bodySmall)
        }
        if (UploadColumn.USER_NAME in visibleColumns) {
            Text(
                item.renderName.displayFallback(),
                modifier = Modifier.width(132.dp),
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (UploadColumn.STATUS in visibleColumns) {
            Column(modifier = Modifier.width(112.dp)) {
                Text(friendlyName(item.state.name), style = MaterialTheme.typography.bodySmall)
                Text(
                    formatDuration(item.remainingUploadTime),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        if (UploadColumn.UPLOADED in visibleColumns) {
            Column(modifier = Modifier.width(220.dp)) {
                Text(formatBytes(item.totalAmountUploaded), style = MaterialTheme.typography.bodySmall)
                Text(
                    "Seed ratio ${if (item.seedRatio >= 0f) String.format(Locale.US, "%.2f", item.seedRatio) else "n/a"}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        if (UploadColumn.RATE in visibleColumns) {
            Text(formatRate(item.uploadSpeed), modifier = Modifier.width(90.dp), style = MaterialTheme.typography.bodySmall)
        }
        if (UploadColumn.PEERS in visibleColumns) {
            Text(item.numUploadConnections.toString(), modifier = Modifier.width(72.dp), style = MaterialTheme.typography.bodySmall)
        }
        Box {
            IconButton(onClick = { menuExpanded = true }) {
                Icon(Icons.Rounded.MoreVert, contentDescription = "Actions")
            }
            DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                val selectedItems = controller.selectedUploadItems()
                val multiSelection = selectedItems.size > 1
                val browseTargets = controller.uploadBrowseTargets(item)
                val blockTargets = controller.uploadBlockTargets(item)
                if (selectedItems.any { it.state == UploadState.UPLOADING }) {
                    DropdownMenuItem(
                        text = { Text(if (multiSelection) "Pause Selected" else "Pause") },
                        onClick = {
                            menuExpanded = false
                            controller.pauseSelectedUploads()
                        }
                    )
                } else if (selectedItems.any { it.state == UploadState.PAUSED }) {
                    DropdownMenuItem(
                        text = { Text(if (multiSelection) "Resume Selected" else "Resume") },
                        onClick = {
                            menuExpanded = false
                            controller.resumeSelectedUploads()
                        }
                    )
                }
                if (selectedItems.any(controller::canOpenUploadItem)) {
                    DropdownMenuItem(
                        text = { Text(controller.selectedUploadsOpenActionLabel()) },
                        onClick = {
                            menuExpanded = false
                            controller.openSelectedUploads()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(if (multiSelection) "Locate Selected on Disk" else "Locate on Disk") },
                        onClick = {
                            menuExpanded = false
                            controller.revealSelectedUploads()
                        }
                    )
                }
                if (!multiSelection) {
                    if (controller.canOpenUploadItem(item)) {
                        DropdownMenuItem(
                            text = { Text("View File Info") },
                            onClick = {
                                menuExpanded = false
                                controller.showUploadFileInfo(item)
                            }
                        )
                    }
                    BrowseSourceMenuItems(controller, browseTargets) { menuExpanded = false }
                    BlockUserMenuItems(controller, blockTargets) { menuExpanded = false }
                }
                if (selectedItems.any(controller::canOpenUploadItem)) {
                    if (multiSelection) {
                        DropdownMenuItem(
                            text = { Text("Locate Selected in My Files") },
                            onClick = {
                                menuExpanded = false
                                controller.locateSelectedUploads()
                            }
                        )
                    } else {
                        if (jumpTargets.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text("Locate in My Files") },
                                onClick = {
                                    menuExpanded = false
                                    controller.locateSelectedUploads()
                                }
                            )
                        } else {
                            jumpTargets.forEach { target ->
                                DropdownMenuItem(
                                    text = { Text(showInTargetLabel(target)) },
                                    onClick = {
                                        menuExpanded = false
                                        controller.showUploadInTarget(item, target)
                                    }
                                )
                            }
                        }
                    }
                }
                DropdownMenuItem(
                    text = { Text(controller.uploadRemoveActionLabel(selectedItems)) },
                    onClick = {
                        menuExpanded = false
                        controller.removeSelectedUploads()
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun FriendsScreen(controller: ComposeAppController) {
    val desktopDensity = LocalDesktopDensity.current
    val desktopType = LocalDesktopTypeScale.current
    val friendsEpoch = controller.chatFriendsEpoch
    val friends = controller.chatFriends
    val connectionState = controller.friendConnectionState
    val selectedConversation = controller.selectedConversationId?.let { controller.chatConversations[it] }
    val _ignored = friendsEpoch
    val unreadCount = controller.unreadChatFriendCount
    val onlineCount = controller.onlineChatFriendCount

    if (connectionState != FriendConnectionEvent.Type.CONNECTED) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Card(
                modifier = Modifier.width(560.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(Icons.Rounded.Forum, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text(
                        when (connectionState) {
                            FriendConnectionEvent.Type.CONNECTING -> "Signing into Friends…"
                            FriendConnectionEvent.Type.CONNECT_FAILED -> "Friends sign-in failed"
                            else -> "Friends are offline"
                        },
                        style = desktopType.screenTitle
                    )
                    Text(
                        controller.friendLoginError ?: "Sign in to browse friends, chat, and manage your contacts.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = desktopType.summary
                    )
                    CompactFilledTonalButton(onClick = { controller.showFriendLoginDialog() }) {
                        Icon(Icons.Rounded.PersonAdd, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("Sign In")
                    }
                }
            }
        }
        return
    }

    HorizontalSplitPane(
        fraction = controller.friendsRosterPaneFraction,
        onFractionChange = { controller.updateFriendsPaneFraction(it) },
        onFractionChangeFinished = { controller.updateFriendsPaneFraction(it, persist = true) },
        modifier = Modifier.fillMaxSize(),
        minStartWidth = 280.dp,
        minEndWidth = 360.dp,
        start = {
        Card(
            modifier = Modifier.fillMaxSize(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(desktopDensity.cardPadding), verticalArrangement = Arrangement.spacedBy(desktopDensity.sectionGap)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Friends", style = desktopType.screenTitle)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (controller.supportsFriendPresenceModes()) {
                            FriendPresenceModeButton(controller)
                        }
                        if (controller.supportsFriendAddRemove()) {
                            CompactIconButton(onClick = { controller.openAddFriendDialog() }) {
                                Icon(Icons.Rounded.PersonAdd, contentDescription = "Add friend")
                            }
                        }
                        CompactFilledTonalButton(onClick = { controller.browseAllFriends() }) {
                            Text("Browse All")
                        }
                    }
                }
                Text(
                    compactSummary(
                        "Contacts" to friends.size.toString(),
                        "Online" to onlineCount.toString(),
                        "Unread" to unreadCount.toString()
                    ),
                    style = desktopType.summary,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (controller.pendingFriendRequests.isNotEmpty()) {
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(desktopDensity.surfaceCorner)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(desktopDensity.cardPadding),
                            verticalArrangement = Arrangement.spacedBy(desktopDensity.sectionGap)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Friend Requests", style = desktopType.sectionTitle)
                                Text(
                                    compactSummary("Pending" to controller.pendingFriendRequests.size.toString()),
                                    style = desktopType.meta,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            controller.pendingFriendRequests.take(3).forEach { request ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        Text(request.username, fontWeight = FontWeight.Medium)
                                        Text(
                                            "Wants to be your friend.",
                                            style = desktopType.meta,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        CompactTextButton(onClick = { controller.declineFriendRequest(request) }) {
                                            Text("No")
                                        }
                                        CompactFilledTonalButton(onClick = { controller.acceptFriendRequest(request) }) {
                                            Text("Yes")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                HorizontalDivider()
                if (friends.isEmpty()) {
                    EmptyState(
                        icon = Icons.Rounded.Forum,
                        title = "Your friends list is empty",
                        body = "Add a friend or browse shared files from connected friends."
                    )
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(friends, key = { it.id }) { friend ->
                            val selected = controller.selectedConversationId == friend.id
                            val preview = controller.friendConversationPreview(friend.id)
                            var menuExpanded by remember(friend.id) { mutableStateOf(false) }
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .onPointerEvent(PointerEventType.Press) { event ->
                                        if (event.buttons.isSecondaryPressed) {
                                            controller.selectConversation(friend)
                                            menuExpanded = true
                                        }
                                    }
                                    .clickable { controller.selectConversation(friend) },
                                color = if (selected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f) else Color.Transparent,
                                shape = RoundedCornerShape(desktopDensity.surfaceCorner),
                                tonalElevation = if (selected) 2.dp else 0.dp
                            ) {
                                Box {
                                    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = desktopDensity.cardPadding, vertical = desktopDensity.sectionGap), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                        Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                            Text(friend.displayName, fontWeight = FontWeight.Medium)
                                            if (friend.hasUnviewedMessages()) {
                                                AssistChip(onClick = {}, label = { Text("Unread") })
                                            }
                                        }
                                        Text(
                                            "${if (friend.signedIn) "Online" else "Offline"} · ${friend.mode?.name?.let(::friendlyName) ?: "Unavailable"}",
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            style = desktopType.meta
                                        )
                                        if (!preview.isNullOrBlank()) {
                                            Text(
                                                preview,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                style = desktopType.meta,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        } else if (friend.status.isNotBlank()) {
                                            Text(friend.status, color = MaterialTheme.colorScheme.secondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        }
                                    }
                                    DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                                        val _shareEpoch = controller.friendCollectionShareEpoch
                                        DropdownMenuItem(
                                            text = { Text("Open Conversation") },
                                            onClick = {
                                                menuExpanded = false
                                                controller.selectConversation(friend)
                                            }
                                        )
                                        if (controller.hasOpenConversation(friend.id)) {
                                            DropdownMenuItem(
                                                text = { Text("Close Conversation") },
                                                onClick = {
                                                    menuExpanded = false
                                                    controller.closeConversation(friend.id)
                                                }
                                            )
                                        }
                                        DropdownMenuItem(
                                            text = { Text("Browse Files") },
                                            enabled = friend.browseable,
                                            onClick = {
                                                menuExpanded = false
                                                controller.browseFriendLibrary(friend.id)
                                            }
                                        )
                                        if (controller.availableFriendCollections().isNotEmpty()) {
                                            HorizontalDivider()
                                            controller.availableFriendCollections().forEach { collection ->
                                                DropdownMenuItem(
                                                    text = { Text(collection.collectionName) },
                                                    leadingIcon = {
                                                        if (controller.isCollectionSharedWithFriend(collection.id, friend.id)) {
                                                            Icon(Icons.Rounded.Check, contentDescription = null)
                                                        }
                                                    },
                                                    onClick = {
                                                        menuExpanded = false
                                                        controller.toggleCollectionShareWithFriend(collection.id, friend.id)
                                                    }
                                                )
                                            }
                                            DropdownMenuItem(
                                                    text = { Text("Share New Collection…") },
                                                onClick = {
                                                    menuExpanded = false
                                                    controller.showShareNewListDialog(friend)
                                                }
                                            )
                                        }
                                        if (controller.supportsFriendAddRemove()) {
                                            DropdownMenuItem(
                                                text = { Text("Remove Friend") },
                                                onClick = {
                                                    menuExpanded = false
                                                    controller.removeFriend(friend)
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
        },
        end = {
        if (selectedConversation == null) {
            EmptyState(
                icon = Icons.Rounded.Forum,
                title = "Choose a conversation",
                body = "Select a friend on the left to start chatting."
            )
        } else {
            ConversationPanel(controller, selectedConversation)
        }
        }
    )
}

private const val CHAT_PAUSE_DELAY_MS = 3_000L
private const val CHAT_OUTGOING_GROUP_GAP_MS = 60_000L
private const val CHAT_SOFT_WRAP_RUN_LIMIT = 24
private const val CHAT_LINK_ANNOTATION_TAG = "chat-link"

private sealed interface ConversationTimelineItem {
    val key: String

    data class TextGroup(
        override val key: String,
        val senderLabel: String,
        val outgoing: Boolean,
        val messages: List<ConversationMessage>
    ) : ConversationTimelineItem

    data class FileOfferRow(
        override val key: String,
        val message: ConversationMessage
    ) : ConversationTimelineItem

    data class StatusRow(
        override val key: String,
        val message: ConversationMessage
    ) : ConversationTimelineItem
}

@Composable
private fun ConversationPanel(controller: ComposeAppController, conversation: ChatConversationState) {
    val desktopDensity = LocalDesktopDensity.current
    val desktopType = LocalDesktopTypeScale.current
    val friend = conversation.friend
    val transcriptItems = remember(friend.id, conversation.messageVersion) {
        buildConversationTimelineItems(conversation.messages)
    }
    val inlineConversationState = inlineConversationStateText(friend, conversation.remoteState)
    val transcriptListState = rememberLazyListState()
    var followTranscript by remember(friend.id) { mutableStateOf(true) }
    val transcriptCount = transcriptItems.size + if (inlineConversationState != null) 1 else 0

    DisposableEffect(friend.id) {
        onDispose {
            controller.publishConversationLocalState(conversation, ChatState.active)
        }
    }

    LaunchedEffect(friend.id, conversation.draft) {
        if (conversation.draft.isBlank()) {
            controller.publishConversationLocalState(conversation, ChatState.active)
        } else {
            controller.publishConversationLocalState(conversation, ChatState.composing)
            delay(CHAT_PAUSE_DELAY_MS)
            if (conversation.draft.isNotBlank()) {
                controller.publishConversationLocalState(conversation, ChatState.paused)
            }
        }
    }

    LaunchedEffect(friend.id) {
        if (transcriptCount > 0) {
            transcriptListState.scrollToItem((transcriptCount - 1).coerceAtLeast(0))
        }
    }

    LaunchedEffect(friend.id, transcriptListState) {
        snapshotFlow { transcriptListState.isNearBottom() }
            .distinctUntilChanged()
            .collect { followTranscript = it }
    }

    LaunchedEffect(friend.id, transcriptCount, transcriptItems.lastOrNull()?.key, inlineConversationState, followTranscript) {
        if (followTranscript && transcriptCount > 0) {
            transcriptListState.animateScrollToItem((transcriptCount - 1).coerceAtLeast(0))
        }
    }

    Card(
        modifier = Modifier.fillMaxSize(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(desktopDensity.cardPadding), verticalArrangement = Arrangement.spacedBy(desktopDensity.sectionGap)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(friend.displayName, style = desktopType.screenTitle)
                    Text(
                        buildString {
                            append(friend.mode?.name?.let(::friendlyName) ?: "Unavailable")
                            append(" · ")
                            append(if (friend.signedIn) "Online" else "Offline")
                            if (conversation.remoteState != null && conversation.remoteState != ChatState.active) {
                                append(" · ")
                                append(
                                    when (conversation.remoteState) {
                                        ChatState.composing -> "Typing…"
                                        ChatState.paused -> "Paused"
                                        ChatState.inactive -> "Inactive"
                                        ChatState.gone -> "Gone"
                                        else -> "Active"
                                    }
                                )
                            }
                        },
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = desktopType.summary
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CompactFilledTonalButton(
                        onClick = { controller.browseFriendLibrary(friend.id) },
                        enabled = friend.browseable
                    ) { Text("Browse Files") }
                    if (controller.supportsConversationOffTheRecord(conversation)) {
                        CompactOutlinedButton(onClick = { controller.toggleConversationOffTheRecord(conversation) }) {
                            Text(
                                if (conversation.offTheRecordEnabled == true) {
                                    "Off the Record On"
                                } else {
                                    "Off the Record Off"
                                }
                            )
                        }
                    }
                    if (controller.supportsConversationFileOffers(conversation)) {
                        CompactOutlinedButton(onClick = { controller.shareFileWithConversation(conversation) }) {
                            Text("Share File…")
                        }
                        CompactOutlinedButton(onClick = { controller.shareFolderWithConversation(conversation) }) {
                            Text("Share Folder…")
                        }
                    }
                    if (controller.availableFriendCollections().isNotEmpty()) {
                        val _shareEpoch = controller.friendCollectionShareEpoch
                        FriendCollectionShareMenuButton(
                            label = "Share Collection",
                            friend = friend,
                            collections = controller.availableFriendCollections(),
                            isShared = controller::isCollectionSharedWithFriend,
                            onToggleShare = controller::toggleCollectionShareWithFriend,
                            onShareNewList = { controller.showShareNewListDialog(friend) }
                        )
                    }
                    if (controller.supportsFriendAddRemove()) {
                        CompactOutlinedButton(onClick = { controller.removeFriend(friend) }) { Text("Remove Friend") }
                    }
                    CompactTextButton(onClick = { controller.closeConversation(friend.id) }) { Text("Close Conversation") }
                }
            }

            Text(
                compactSummary(
                    "Presence" to (friend.mode?.name?.let(::friendlyName) ?: "Unavailable"),
                    "Status" to if (friend.signedIn) "Online" else "Offline",
                    "Record" to if (controller.supportsConversationOffTheRecord(conversation)) {
                        if (conversation.offTheRecordEnabled == true) "Off the Record" else "On the Record"
                    } else {
                        null
                    },
                    "Chat" to conversation.remoteState
                        ?.takeUnless { it == ChatState.active }
                        ?.name
                        ?.let(::friendlyName)
                ),
                style = desktopType.summary,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            controller.friendConversationPreview(friend.id)?.let { preview ->
                Text(
                    preview,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = desktopType.meta,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            HorizontalDivider()

            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                state = transcriptListState,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(transcriptItems, key = { it.key }) { item ->
                    when (item) {
                        is ConversationTimelineItem.FileOfferRow ->
                            FileOfferMessageBubble(controller, item.message)

                        is ConversationTimelineItem.StatusRow ->
                            ConversationSystemRow(item.message.body)

                        is ConversationTimelineItem.TextGroup ->
                            ConversationTextGroupBubble(
                                senderLabel = item.senderLabel,
                                messages = item.messages,
                                isOutgoing = item.outgoing,
                                onLinkClick = controller::openConversationLink
                            )
                    }
                }
                if (inlineConversationState != null) {
                    item(key = "conversation-state:${friend.id}:${friend.signedIn}:${conversation.remoteState}") {
                        ConversationSystemRow(inlineConversationState)
                    }
                }
            }

            OutlinedTextField(
                value = conversation.draft,
                onValueChange = { controller.updateConversationDraft(conversation, it) },
                label = { Text("Message") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 4
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                CompactButton(onClick = { controller.sendConversationMessage(conversation) }) {
                    Icon(Icons.AutoMirrored.Rounded.Send, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Send")
                }
            }
        }
    }
}

@Composable
private fun ConversationTextGroupBubble(
    senderLabel: String,
    messages: List<ConversationMessage>,
    isOutgoing: Boolean,
    onLinkClick: (String) -> Unit
) {
    val desktopType = LocalDesktopTypeScale.current
    val lastTimestamp = messages.lastOrNull()?.timestamp ?: 0L
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isOutgoing) Arrangement.End else Arrangement.Start
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (isOutgoing) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }
            ),
            modifier = Modifier.width(500.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(senderLabel, fontWeight = FontWeight.SemiBold)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    messages.forEach { message ->
                        ConversationLinkedText(
                            text = message.body,
                            onLinkClick = onLinkClick
                        )
                    }
                }
                Text(
                    formatClock(lastTimestamp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = desktopType.meta
                )
            }
        }
    }
}

@Composable
private fun FileOfferMessageBubble(controller: ComposeAppController, message: ConversationMessage) {
    val desktopType = LocalDesktopTypeScale.current
    val offer = message.fileOffer ?: return
    val jumpTargets = controller.conversationFileOfferJumpTargets(message)
    val identity = controller.conversationFileOfferIdentity(message)
    val isOutgoing = message.isOutgoing
    val statusLabel = when (offer.downloadState) {
        null -> if (isOutgoing) "Shared with friend" else "Ready to download"
        DownloadState.DONE -> "Downloaded"
        DownloadState.ERROR -> "Download error"
        DownloadState.STALLED -> "Download stalled"
        DownloadState.CANCELLED -> "Download cancelled"
        DownloadState.DOWNLOADING -> "Downloading"
        DownloadState.CONNECTING -> "Connecting"
        DownloadState.FINISHING -> "Finishing"
        DownloadState.LOCAL_QUEUED, DownloadState.REMOTE_QUEUED -> "Queued"
        DownloadState.TRYING_AGAIN, DownloadState.RESUMING -> "Retrying"
        DownloadState.PAUSED -> "Paused"
        DownloadState.DANGEROUS -> "Blocked as dangerous"
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isOutgoing) Arrangement.End else Arrangement.Start
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (isOutgoing) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.secondaryContainer
                }
            ),
            modifier = Modifier.widthIn(max = 560.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Text(if (isOutgoing) "You" else message.senderName, fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    FileIdentityIcon(
                        icon = identity?.icon,
                        modifier = Modifier.size(20.dp),
                        fallback = FileIconToken.OTHER
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(identity?.title ?: offer.fileName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
                        (identity?.subtitle ?: offer.description)
                            ?.takeIf(String::isNotBlank)
                            ?.let {
                                Text(
                                    it,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = desktopType.meta,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                    }
                }
                Text(
                    compactSummary(
                        "Status" to statusLabel,
                        "Size" to offer.size.takeIf { it > 0L }?.let(::formatBytes)
                    ),
                    style = desktopType.summary,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (!offer.localPath.isNullOrBlank()) {
                    Text(
                        offer.localPath,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = desktopType.meta,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (controller.canDownloadConversationFileOffer(message)) {
                        CompactFilledTonalButton(onClick = { controller.downloadConversationFileOffer(message) }) {
                            Text(if (controller.canRetryConversationFileOffer(message)) "Retry" else "Download")
                        }
                    }
                    if (controller.canBrowseConversationFileOffer(message)) {
                        CompactOutlinedButton(onClick = { controller.browseConversationFileOffer(message) }) {
                            Text("Browse Files")
                        }
                    }
                    if (jumpTargets.isNotEmpty()) {
                        LibraryJumpMenuButton(
                            label = if (jumpTargets.size == 1) showInTargetLabel(jumpTargets.first()) else "Show in My Files",
                            targets = jumpTargets,
                            onJump = { controller.showConversationFileOfferInTarget(message, it) }
                        )
                    }
                }
                Text(
                    formatClock(message.timestamp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = desktopType.meta
                )
            }
        }
    }
}

@Composable
private fun ConversationSystemRow(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        Surface(
            color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.55f),
            shape = RoundedCornerShape(999.dp)
        ) {
            Text(
                text = text,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ConversationLinkedText(
    text: String,
    onLinkClick: (String) -> Unit
) {
    val linkColor = MaterialTheme.colorScheme.primary
    val annotated = remember(text, linkColor) {
        buildConversationAnnotatedText(text, linkColor)
    }
    ClickableText(
        text = annotated,
        style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
        onClick = { offset ->
            annotated
                .getStringAnnotations(CHAT_LINK_ANNOTATION_TAG, offset, offset)
                .firstOrNull()
                ?.item
                ?.let(onLinkClick)
        }
    )
}

private fun buildConversationTimelineItems(messages: List<ConversationMessage>): List<ConversationTimelineItem> {
    if (messages.isEmpty()) {
        return emptyList()
    }
    val items = mutableListOf<ConversationTimelineItem>()
    var groupedMessages = mutableListOf<ConversationMessage>()

    fun flushGroup() {
        if (groupedMessages.isEmpty()) {
            return
        }
        val first = groupedMessages.first()
        items += ConversationTimelineItem.TextGroup(
            key = groupedMessages.first().id,
            senderLabel = if (first.isOutgoing) tr("You") else first.senderName,
            outgoing = first.isOutgoing,
            messages = groupedMessages.toList()
        )
        groupedMessages = mutableListOf()
    }

    messages.forEach { message ->
        val isTextGroupCandidate = !message.isFileOffer && !message.isServer && message.kind == ConversationMessageKind.TEXT
        if (!isTextGroupCandidate) {
            flushGroup()
            items += when {
                message.isFileOffer -> ConversationTimelineItem.FileOfferRow(message.id, message)
                else -> ConversationTimelineItem.StatusRow(message.id, message)
            }
            return@forEach
        }

        val previous = groupedMessages.lastOrNull()
        val startNewGroup = previous == null ||
            previous.isOutgoing != message.isOutgoing ||
            (message.isOutgoing && message.timestamp - previous.timestamp > CHAT_OUTGOING_GROUP_GAP_MS)
        if (startNewGroup) {
            flushGroup()
        }
        groupedMessages += message
    }

    flushGroup()
    return items
}

private fun inlineConversationStateText(friend: FriendRosterItem, remoteState: ChatState?): String? {
    return when {
        !friend.signedIn -> "${friend.displayName} has signed off"
        remoteState == ChatState.composing -> "${friend.displayName} is typing…"
        remoteState == ChatState.paused -> "${friend.displayName} has entered text"
        else -> null
    }
}

private val conversationLinkPattern = Regex("""(?i)\b((?:https?|ftp)://[^\s<>()]+|magnet:[^\s<>()]+)""")

private fun buildConversationAnnotatedText(text: String, linkColor: Color): AnnotatedString {
    return buildAnnotatedString {
        var cursor = 0
        for (match in conversationLinkPattern.findAll(text)) {
            val start = match.range.first
            val endExclusive = match.range.last + 1
            append(withConversationSoftBreaks(text.substring(cursor, start)))

            val rawLink = text.substring(start, endExclusive)
            val normalizedLink = normalizeConversationLink(rawLink)
            val trailing = rawLink.substring(normalizedLink.length)
            if (normalizedLink.isNotEmpty()) {
                val annotationStart = length
                val displayLink = withConversationSoftBreaks(normalizedLink)
                append(displayLink)
                addStyle(
                    SpanStyle(
                        color = linkColor,
                        textDecoration = TextDecoration.Underline
                    ),
                    annotationStart,
                    annotationStart + displayLink.length
                )
                addStringAnnotation(
                    tag = CHAT_LINK_ANNOTATION_TAG,
                    annotation = normalizedLink,
                    start = annotationStart,
                    end = annotationStart + displayLink.length
                )
            }
            append(withConversationSoftBreaks(trailing))
            cursor = endExclusive
        }
        append(withConversationSoftBreaks(text.substring(cursor)))
    }
}

private fun normalizeConversationLink(raw: String): String {
    var trimmed = raw
    while (trimmed.isNotEmpty() && trimmed.last() in listOf('.', ',', '!', '?', ';', ':', ')', ']', '}')) {
        trimmed = trimmed.dropLast(1)
    }
    return trimmed
}

private fun withConversationSoftBreaks(text: String): String {
    if (text.isBlank()) {
        return text
    }
    val builder = StringBuilder(text.length + text.length / 6)
    var uninterruptedRun = 0
    text.forEachIndexed { index, char ->
        builder.append(char)
        uninterruptedRun = if (char.isWhitespace()) 0 else uninterruptedRun + 1
        val shouldBreak = !char.isWhitespace() && (
            char in listOf('/', '?', '&', '=', '#', '-', '_', '.', ':') ||
                uninterruptedRun >= CHAT_SOFT_WRAP_RUN_LIMIT
            )
        if (shouldBreak && index < text.lastIndex) {
            builder.append('\u200B')
            if (uninterruptedRun >= CHAT_SOFT_WRAP_RUN_LIMIT) {
                uninterruptedRun = 0
            }
        }
    }
    return builder.toString()
}

private fun LazyListState.isNearBottom(): Boolean {
    val totalItems = layoutInfo.totalItemsCount
    if (totalItems == 0) {
        return true
    }
    val lastVisibleIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: return true
    return lastVisibleIndex >= totalItems - 2
}

@Composable
private fun PlayerScreen(controller: ComposeAppController) {
    val desktopDensity = LocalDesktopDensity.current
    val desktopType = LocalDesktopTypeScale.current
    val currentFile = controller.playerCurrentFile
    val status = controller.playerState
    val currentIdentity = controller.currentPlayerIdentity()
    val queueEntries = controller.playerQueue.toList()
    val currentQueueIndex = controller.playerQueueIndex
    val currentFileJumpTargets = controller.currentPlayerJumpTargets()
    val upNextEntries = if (currentQueueIndex in queueEntries.indices) {
        queueEntries.drop(currentQueueIndex + 1)
    } else {
        queueEntries
    }
    val canPreviousTrack = controller.canPreviousTrack()
    val canNextTrack = controller.canNextTrack()
    val currentFileDragTransferData = externalFilesTransferData(controller.draggableCurrentPlayerFiles())
    var sliderValue by remember(controller.playerProgress) {
        mutableFloatStateOf(controller.playerProgress)
    }

    Card(
        modifier = Modifier.fillMaxSize(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(desktopDensity.cardPadding),
            verticalArrangement = Arrangement.spacedBy(desktopDensity.sectionGap)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Player", style = desktopType.screenTitle)
                Text(
                    "Audio plays in WireShare. Video opens in your default player.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = desktopType.summary
                )
            }
            if (currentFile == null) {
                EmptyState(
                    icon = Icons.Rounded.LibraryMusic,
                    title = "Nothing playing",
                    body = "Play an audio file from My Files or a completed download."
                )
            } else {
                Text(
                    compactSummary(
                        "Now Playing" to if (status == PlayerState.PLAYING || status == PlayerState.SEEKING_PLAY) "Live" else "Idle",
                        "State" to friendlyName(status.name),
                        "Shuffle" to if (controller.playerShuffle) "On" else "Off",
                        "Volume" to "${(controller.playerVolume * 100).toInt()}%",
                        "File Size" to formatBytes(currentFile.length()),
                        "Queue" to queueEntries.takeIf { it.isNotEmpty() }?.size?.toString(),
                        "Up Next" to upNextEntries.takeIf { queueEntries.isNotEmpty() }?.size?.toString()
                    ),
                    style = desktopType.summary,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(desktopDensity.surfaceCorner),
                    modifier = Modifier.let { base ->
                        if (currentFileDragTransferData != null) {
                            base.dragAndDropSource { currentFileDragTransferData }
                        } else {
                            base
                        }
                    }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(desktopDensity.cardPadding),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FileIdentityIcon(
                            icon = currentIdentity?.icon,
                            modifier = Modifier.size(28.dp),
                            fallback = FileIconToken.AUDIO
                        )
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(currentIdentity?.title ?: controller.playerTrackName, style = desktopType.sectionTitle, fontWeight = FontWeight.Medium)
                            Text(currentIdentity?.subtitle ?: currentFile.name, color = MaterialTheme.colorScheme.onSurfaceVariant, style = desktopType.summary)
                            if (queueEntries.isNotEmpty()) {
                                Text(
                                    "From: ${controller.playerQueueSourceLabel}",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = desktopType.meta
                                )
                            }
                            Text(
                                currentFile.absolutePath,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = desktopType.meta,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
                LinearProgressIndicator(progress = { controller.playerProgress }, modifier = Modifier.fillMaxWidth())
                Slider(
                    value = sliderValue,
                    onValueChange = { sliderValue = it },
                    valueRange = 0f..1f,
                    onValueChangeFinished = { controller.seekPlayer(sliderValue) },
                    enabled = controller.playerCurrentFile != null
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    CompactIconButton(onClick = { controller.previousTrack() }, enabled = canPreviousTrack) {
                        Icon(Icons.Rounded.SkipPrevious, contentDescription = "Previous")
                    }
                    CompactFilledTonalButton(onClick = { controller.togglePlayerPlayback() }) {
                        Icon(
                            if (status == PlayerState.PLAYING || status == PlayerState.SEEKING_PLAY) {
                                Icons.Rounded.Pause
                            } else {
                                Icons.Rounded.PlayArrow
                            },
                            contentDescription = null
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(if (status == PlayerState.PLAYING || status == PlayerState.SEEKING_PLAY) "Pause" else "Play")
                    }
                    CompactIconButton(onClick = { controller.nextTrack() }, enabled = canNextTrack) {
                        Icon(Icons.Rounded.SkipNext, contentDescription = "Next")
                    }
                    CompactOutlinedButton(onClick = { controller.toggleShuffle() }) {
                        Text(if (controller.playerShuffle) "Shuffle On" else "Shuffle Off")
                    }
                    CompactOutlinedButton(onClick = { controller.openCurrentPlayerFile() }) {
                        Text("Open")
                    }
                    CompactOutlinedButton(onClick = { controller.revealCurrentPlayerFile() }) {
                        Text("Reveal")
                    }
                    if (currentFileJumpTargets.isNotEmpty()) {
                        LibraryJumpMenuButton(
                            label = if (currentFileJumpTargets.size == 1) showInTargetLabel(currentFileJumpTargets.first()) else "Show In",
                            targets = currentFileJumpTargets,
                            onJump = controller::showCurrentPlayerInTarget
                        )
                    }
                    if (queueEntries.isNotEmpty()) {
                        CompactTextButton(onClick = { controller.clearPlayerQueue() }) {
                            Text("Clear Queue")
                        }
                    }
                    CompactTextButton(onClick = { controller.stopPlayer() }) {
                        Text("Stop")
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Volume")
                    Slider(
                        value = controller.playerVolume,
                        onValueChange = { controller.updatePlayerVolume(it) },
                        valueRange = 0f..1f
                    )
                }
                if (queueEntries.isNotEmpty()) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(desktopDensity.surfaceCorner)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(desktopDensity.cardPadding),
                            verticalArrangement = Arrangement.spacedBy(desktopDensity.sectionGap)
                        ) {
                            Text("Playback Queue", style = desktopType.sectionTitle)
                            Text(
                                "Queue from ${controller.playerQueueSourceLabel}.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = desktopType.meta
                            )
                            queueEntries.forEachIndexed { index, entry ->
                                val isCurrent = index == currentQueueIndex
                                val identity = controller.playerQueueEntryIdentity(entry)
                                val jumpTargets = controller.playerQueueEntryJumpTargets(entry)
                                val dragTransferData = externalFilesTransferData(controller.draggablePlayerQueueFiles(entry))
                                Surface(
                                    color = if (isCurrent) {
                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
                                    } else {
                                        Color.Transparent
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .let { base ->
                                            if (dragTransferData != null) {
                                                base.dragAndDropSource { dragTransferData }
                                            } else {
                                                base
                                            }
                                        }
                                        .clickable { controller.playPlayerQueueEntry(index) }
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            modifier = Modifier.weight(1f),
                                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            FileIdentityIcon(
                                                icon = identity.icon,
                                                modifier = Modifier.size(18.dp),
                                                fallback = FileIconToken.AUDIO
                                            )
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    identity.title,
                                                    fontWeight = if (isCurrent) FontWeight.SemiBold else FontWeight.Medium,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    identity.subtitle.ifBlank { entry.file.name },
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    style = desktopType.meta,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(
                                                when {
                                                    isCurrent -> "Current"
                                                    index < currentQueueIndex -> "Played"
                                                    else -> "Up Next"
                                                },
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                style = desktopType.meta
                                            )
                                            if (!isCurrent) {
                                                CompactTextButton(onClick = { controller.playPlayerQueueEntry(index) }) {
                                                    Text("Play Now")
                                                }
                                            }
                                            if (jumpTargets.isNotEmpty()) {
                                                LibraryJumpMenuButton(
                                                    label = if (jumpTargets.size == 1) "Show In" else "Show In",
                                                    targets = jumpTargets,
                                                    onJump = { target -> controller.showPlayerQueueEntryInTarget(entry, target) }
                                                )
                                            }
                                            CompactTextButton(onClick = { controller.removePlayerQueueEntry(index) }) {
                                                Text("Remove")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                Text(
                    "Play audio from My Files or completed downloads.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = desktopType.meta
                )
            }
        }
    }
}

@Composable
private fun StatusBar(controller: ComposeAppController) {
    val desktopDensity = LocalDesktopDensity.current
    val desktopType = LocalDesktopTypeScale.current
    val downloadsEpoch = controller.downloadsEpoch
    val uploadsEpoch = controller.uploadsEpoch
    val currentIdentity = controller.currentPlayerIdentity()
    val activeDownloads = controller.activeDownloadCount()
    val activeUploads = controller.activeUploadCount()
    val nextQueueEntry = if (controller.playerQueueIndex in controller.playerQueue.indices) {
        controller.playerQueue.getOrNull(controller.playerQueueIndex + 1)
    } else {
        controller.playerQueue.firstOrNull()
    }
    val canPreviousTrack = controller.canPreviousTrack()
    val canNextTrack = controller.canNextTrack()
    val _downloads = downloadsEpoch
    val _uploads = uploadsEpoch

    Surface(tonalElevation = 4.dp, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = desktopDensity.headerPadding, vertical = desktopDensity.statusBarPadding),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(desktopDensity.statusBarGap)
            ) {
                ConnectionStatusButton(controller)
                FriendsQuickButton(controller)
                if (controller.playerVisible && controller.playerCurrentFile != null) {
                    HorizontalDivider(modifier = Modifier.height(28.dp).width(1.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(desktopDensity.chipGap)
                    ) {
                        if (canPreviousTrack) {
                            CompactIconButton(onClick = { controller.previousTrack() }) {
                                Icon(Icons.Rounded.SkipPrevious, contentDescription = "Previous track")
                            }
                        }
                        CompactIconButton(onClick = { controller.togglePlayerPlayback() }) {
                            Icon(
                                if (controller.playerState == PlayerState.PLAYING || controller.playerState == PlayerState.SEEKING_PLAY) {
                                    Icons.Rounded.Pause
                                } else {
                                    Icons.Rounded.PlayArrow
                                },
                                contentDescription = null
                            )
                        }
                        if (canNextTrack) {
                            CompactIconButton(onClick = { controller.nextTrack() }) {
                                Icon(Icons.Rounded.SkipNext, contentDescription = "Next track")
                            }
                        }
                        Row(
                            modifier = Modifier.clickable { controller.showCurrentPlayerInMyFilesOrPlayer() },
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FileIdentityIcon(
                                icon = currentIdentity?.icon,
                                modifier = Modifier.size(18.dp),
                                fallback = FileIconToken.AUDIO
                            )
                            Column {
                                Text(
                                    currentIdentity?.title ?: controller.playerTrackName,
                                    style = desktopType.body,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    nextQueueEntry?.let {
                                        if (desktopDensity.compactVertical) {
                                            "Next: ${it.title}"
                                        } else {
                                            "Up next: ${it.title} · Queue from ${controller.playerQueueSourceLabel}"
                                        }
                                    } ?: if (controller.playerQueue.isNotEmpty()) {
                                        if (desktopDensity.compactVertical) {
                                            friendlyName(controller.playerState.name)
                                        } else {
                                            "Queue from ${controller.playerQueueSourceLabel} · ${friendlyName(controller.playerState.name)}"
                                        }
                                    } else {
                                        "${currentIdentity?.subtitle ?: controller.playerCurrentFile?.name.orEmpty()} · ${friendlyName(controller.playerState.name)}"
                                    },
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = desktopType.meta
                                )
                            }
                        }
                        CompactTextButton(onClick = { controller.selectPlayer() }) {
                            Text("Player")
                        }
                    }
                }
            }
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    compactSummary(
                        "Downloads" to activeDownloads.toString(),
                        "Uploads" to activeUploads.toString(),
                        "Down" to controller.showTotalBandwidth.takeIf { it && !desktopDensity.compactVertical }?.let { formatRate(controller.totalDownloadBandwidth()) },
                        "Up" to controller.showTotalBandwidth.takeIf { it && !desktopDensity.compactVertical }?.let { formatRate(controller.totalUploadBandwidth()) },
                        "Exit" to controller.delayedExitState.pending.takeIf { it }?.let { controller.delayedExitSummary() }
                    ),
                    modifier = Modifier
                        .padding(end = desktopDensity.statusBarGap)
                        .weight(1f, fill = false),
                    style = desktopType.summary,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                SharingStatusButton(controller)
                FileProcessingStatusButton(controller)
                if (controller.delayedExitState.pending) {
                    CompactTextButton(onClick = { controller.cancelExitAfterTransfers() }) {
                        Text("Cancel Exit")
                    }
                }
                CompactTextButton(onClick = { controller.toggleTray() }) {
                    Text(if (controller.trayExpanded) "Hide Tray" else "Show Tray")
                }
            }
        }
    }
}

@Composable
private fun ConnectionStatusButton(controller: ComposeAppController) {
    val strength = controller.connectionStrength()
    var expanded by remember(strength, controller.canRetryConnection()) { mutableStateOf(false) }
    var verbose by remember(strength) { mutableStateOf(true) }

    LaunchedEffect(strength) {
        verbose = true
        if (strength == org.limewire.core.api.connection.ConnectionStrength.FULL ||
            strength == org.limewire.core.api.connection.ConnectionStrength.TURBO
        ) {
            delay(3000)
            verbose = false
        }
    }

    Box {
        CompactOutlinedButton(onClick = { expanded = true }) {
            Icon(
                when (strength) {
                    org.limewire.core.api.connection.ConnectionStrength.NO_INTERNET -> Icons.Rounded.CloudOff
                    org.limewire.core.api.connection.ConnectionStrength.DISCONNECTED -> Icons.Rounded.Warning
                    org.limewire.core.api.connection.ConnectionStrength.CONNECTING -> Icons.Rounded.Sync
                    else -> Icons.Rounded.Wifi
                },
                contentDescription = null
            )
            Spacer(Modifier.width(6.dp))
            Text(connectionStatusLabel(strength, verbose))
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            Column(
                modifier = Modifier.widthIn(min = 280.dp, max = 360.dp).padding(horizontal = 14.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(connectionStatusLabel(strength, true), fontWeight = FontWeight.SemiBold)
                Text(
                    connectionStatusBody(strength, controller.friendConnectionState),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    if (controller.currentFriendConnection()?.isLoggedIn == true) {
                        "Friends are signed in."
                    } else {
                        "Friends are signed out."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (controller.canRetryConnection()) {
                HorizontalDivider()
                DropdownMenuItem(
                    text = { Text("Try Again") },
                    onClick = {
                        expanded = false
                        controller.retryConnection()
                    }
                )
            }
        }
    }
}

@Composable
private fun FriendsQuickButton(controller: ComposeAppController) {
    val requestCount = controller.pendingFriendRequests.size
    val unreadCount = controller.unreadConversationCount()
    val state = controller.friendConnectionState
    val label = when {
        requestCount > 0 -> "$requestCount request${if (requestCount == 1) "" else "s"}"
        unreadCount > 0 -> "$unreadCount unread"
        state == FriendConnectionEvent.Type.CONNECTED -> "Friends online"
        state == FriendConnectionEvent.Type.CONNECTING -> "Signing in"
        else -> "Friends offline"
    }

    CompactTextButton(onClick = { controller.openFriendsQuickEntry() }) {
        Icon(Icons.Rounded.Forum, contentDescription = null)
        Spacer(Modifier.width(6.dp))
        Text(label)
    }
}

@Composable
private fun MetricBadge(label: String, value: String) {
    val desktopDensity = LocalDesktopDensity.current
    val desktopType = LocalDesktopTypeScale.current
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = desktopDensity.badgeHorizontalPadding,
                vertical = desktopDensity.badgeVerticalPadding
            ),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, style = desktopType.badgeLabel, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
            Text(value, style = desktopType.badgeValue, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun InlineStatusBadge(label: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.75f)
    ) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun TransferInlineActionChip(label: String, onClick: () -> Unit) {
    AssistChip(
        onClick = onClick,
        label = {
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    )
}

private fun whatsNewMenuLabel(category: SearchCategory): String {
    return when (category) {
        SearchCategory.ALL -> "All"
        SearchCategory.AUDIO -> "Audio"
        SearchCategory.DOCUMENT -> "Documents"
        SearchCategory.IMAGE -> "Images"
        SearchCategory.PROGRAM -> "Programs"
        SearchCategory.VIDEO -> "Videos"
        SearchCategory.TORRENT -> "Torrents"
        SearchCategory.OTHER -> "Other"
    }
}

@Composable
private fun BrowseSourceButton(
    controller: ComposeAppController,
    targets: List<BrowseSourceTarget>
) {
    if (targets.isEmpty()) {
        return
    }
    val enabledTargets = targets.filter(BrowseSourceTarget::enabled)
    if (targets.size == 1) {
        val target = targets.first()
        OutlinedButton(
            onClick = { controller.browseSourceTarget(target) },
            enabled = target.enabled
        ) {
            Text(controller.browseActionLabel(target))
        }
        return
    }

    var expanded by remember(targets.map { it.id to it.enabled to it.label }) { mutableStateOf(false) }
    Box {
        OutlinedButton(
            onClick = { expanded = true },
            enabled = enabledTargets.isNotEmpty()
        ) {
            Text("Browse Files")
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            BrowseSourceMenuItems(controller, targets) { expanded = false }
        }
    }
}

@Composable
private fun BlockUsersButton(
    controller: ComposeAppController,
    targets: List<RemoteUserTarget>
) {
    if (targets.isEmpty()) {
        return
    }
    if (targets.size == 1) {
        OutlinedButton(onClick = { controller.blockUsers(listOf(targets.first())) }) {
            Text("Block User")
        }
        return
    }

    var expanded by remember(targets.map { it.id to it.label }) { mutableStateOf(false) }
    Box {
        OutlinedButton(onClick = { expanded = true }) {
            Text("Block Users")
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            BlockUserMenuItems(controller, targets) { expanded = false }
        }
    }
}

@Composable
private fun BrowseSourceMenuItems(
    controller: ComposeAppController,
    targets: List<BrowseSourceTarget>,
    closeMenu: () -> Unit
) {
    if (targets.isEmpty()) {
        return
    }
    if (targets.size == 1) {
        val target = targets.first()
        DropdownMenuItem(
            text = { Text(controller.browseActionLabel(target)) },
            enabled = target.enabled,
            onClick = {
                closeMenu()
                controller.browseSourceTarget(target)
            }
        )
        return
    }

    DropdownMenuItem(
        text = { Text("Browse Files") },
        enabled = false,
        onClick = {}
    )
    targets.forEach { target ->
        DropdownMenuItem(
            text = { Text(target.label) },
            enabled = target.enabled,
            onClick = {
                closeMenu()
                controller.browseSourceTarget(target)
            }
        )
    }
}

@Composable
private fun BlockUserMenuItems(
    controller: ComposeAppController,
    targets: List<RemoteUserTarget>,
    closeMenu: () -> Unit
) {
    if (targets.isEmpty()) {
        return
    }
    if (targets.size == 1) {
        DropdownMenuItem(
            text = { Text("Block User") },
            onClick = {
                closeMenu()
                controller.blockUsers(listOf(targets.first()))
            }
        )
        return
    }

    DropdownMenuItem(
        text = { Text("Block User") },
        enabled = false,
        onClick = {}
    )
    DropdownMenuItem(
        text = { Text("All Users") },
        onClick = {
            closeMenu()
            controller.blockUsers(targets)
        }
    )
    targets.forEach { target ->
        DropdownMenuItem(
            text = { Text(target.label) },
            onClick = {
                closeMenu()
                controller.blockUsers(listOf(target))
            }
        )
    }
}

@Composable
private fun rememberTorrentInspectorState(
    controller: ComposeAppController,
    torrent: Torrent?,
    downloadItem: DownloadItem? = null,
    refreshEpoch: Int,
    includeActivity: Boolean,
    includePieces: Boolean
): TorrentInspectorLiveState {
    var state by remember(torrent?.sha1, downloadItem?.urn, refreshEpoch, includeActivity, includePieces) {
        mutableStateOf(
            controller.torrentInspectorSnapshot(
                torrent = torrent,
                downloadItem = downloadItem,
                refreshEpoch = refreshEpoch,
                includeActivity = includeActivity,
                includePieces = includePieces,
                force = true
            )
        )
    }

    LaunchedEffect(torrent?.sha1, downloadItem?.urn, refreshEpoch, includeActivity, includePieces) {
        state = controller.torrentInspectorSnapshot(
            torrent = torrent,
            downloadItem = downloadItem,
            refreshEpoch = refreshEpoch,
            includeActivity = includeActivity,
            includePieces = includePieces,
            force = true
        )
        if (torrent == null) {
            return@LaunchedEffect
        }
        while (true) {
            delay(1500)
            val next = controller.torrentInspectorSnapshot(
                torrent = torrent,
                downloadItem = downloadItem,
                refreshEpoch = refreshEpoch,
                includeActivity = includeActivity,
                includePieces = includePieces
            )
            if (state != next) {
                state = next
            }
        }
    }

    return state
}

@Composable
private fun TorrentDetailsSection(
    controller: ComposeAppController,
    details: TorrentDetailsPresentation,
    activity: TorrentActivityPresentation?,
    pieces: TorrentPiecesPresentation?,
    allowEditing: Boolean,
    onRefreshRequested: () -> Unit
) {
    val editable = allowEditing && details.editable && details.valid
    var managementDraft by remember(details.torrent.sha1) {
        mutableStateOf(controller.torrentManagementDraft(details.torrent))
    }
    var managementError by remember(details.torrent.sha1) { mutableStateOf<String?>(null) }
    var trackerUrl by remember(details.torrent.sha1) { mutableStateOf("") }
    var trackerTier by remember(details.torrent.sha1) { mutableStateOf("0") }
    var trackerError by remember(details.torrent.sha1) { mutableStateOf<String?>(null) }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Torrent", fontWeight = FontWeight.SemiBold)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            MetricBadge("Files", details.fileCount.toString())
            MetricBadge("Trackers", details.trackers.size.toString())
            MetricBadge("Privacy", if (details.privateTorrent) "Private" else "Public")
            MetricBadge("Controls", if (editable) "Editable" else "Read Only")
        }

        if (allowEditing && !details.valid) {
            TorrentSectionCard("Status") {
                Text(
                    "This torrent is no longer valid, so editing controls are unavailable.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        if (editable) {
            TorrentSectionCard("Torrent Controls") {
                managementDraft?.let { draft ->
                    TorrentSeedMode.entries.forEach { mode ->
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable {
                                managementDraft = draft.copy(seedMode = mode)
                                managementError = null
                            },
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = draft.seedMode == mode,
                                onClick = {
                                    managementDraft = draft.copy(seedMode = mode)
                                    managementError = null
                                }
                            )
                            Text(
                                when (mode) {
                                    TorrentSeedMode.DEFAULT -> "Use default torrent options"
                                    TorrentSeedMode.FOREVER -> "Upload this torrent forever"
                                    TorrentSeedMode.CUSTOM -> "Upload this torrent until either of the following"
                                }
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = draft.seedRatio,
                            onValueChange = {
                                managementDraft = draft.copy(seedRatio = it)
                                managementError = null
                            },
                            label = { Text("Ratio") },
                            enabled = draft.seedMode == TorrentSeedMode.CUSTOM,
                            singleLine = true,
                            modifier = Modifier.width(140.dp)
                        )
                        OutlinedTextField(
                            value = draft.seedDays,
                            onValueChange = {
                                managementDraft = draft.copy(seedDays = it)
                                managementError = null
                            },
                            label = { Text("Maximum days") },
                            enabled = draft.seedMode == TorrentSeedMode.CUSTOM,
                            singleLine = true,
                            modifier = Modifier.width(140.dp)
                        )
                        OutlinedTextField(
                            value = draft.seedHours,
                            onValueChange = {
                                managementDraft = draft.copy(seedHours = it)
                                managementError = null
                            },
                            label = { Text("Hours") },
                            enabled = draft.seedMode == TorrentSeedMode.CUSTOM,
                            singleLine = true,
                            modifier = Modifier.width(120.dp)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = draft.limitDownloadBandwidth,
                            onCheckedChange = {
                                managementDraft = draft.copy(limitDownloadBandwidth = it)
                                managementError = null
                            }
                        )
                        Text("Limit this torrent's download bandwidth")
                        OutlinedTextField(
                            value = draft.maxDownloadBandwidth,
                            onValueChange = {
                                managementDraft = draft.copy(maxDownloadBandwidth = it)
                                managementError = null
                            },
                            label = { Text("KB/s") },
                            enabled = draft.limitDownloadBandwidth,
                            singleLine = true,
                            modifier = Modifier.width(120.dp)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = draft.limitUploadBandwidth,
                            onCheckedChange = {
                                managementDraft = draft.copy(limitUploadBandwidth = it)
                                managementError = null
                            }
                        )
                        Text("Limit this torrent's upload bandwidth")
                        OutlinedTextField(
                            value = draft.maxUploadBandwidth,
                            onValueChange = {
                                managementDraft = draft.copy(maxUploadBandwidth = it)
                                managementError = null
                            },
                            label = { Text("KB/s") },
                            enabled = draft.limitUploadBandwidth,
                            singleLine = true,
                            modifier = Modifier.width(120.dp)
                        )
                    }

                    managementError?.let { message ->
                        Text(
                            message,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    FilledTonalButton(
                        onClick = {
                            val failure = controller.applyTorrentManagementSettings(details.torrent, draft)
                            if (failure == null) {
                                managementDraft = controller.torrentManagementDraft(details.torrent)
                                managementError = null
                                onRefreshRequested()
                            } else {
                                managementError = failure
                            }
                        }
                    ) {
                        Text("Apply Torrent Settings")
                    }
                }
            }
        }

        TorrentSectionCard("Trackers") {
            if (details.trackers.isEmpty()) {
                Text(
                    "No trackers are available for this torrent yet.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                details.trackers.forEach { tracker ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(tracker.uri, style = MaterialTheme.typography.bodySmall)
                            Text(
                                "Tier ${tracker.tier}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (editable && tracker.removable) {
                            IconButton(onClick = {
                                val failure = controller.removeTorrentTracker(details.torrent, tracker)
                                if (failure == null) {
                                    trackerError = null
                                    onRefreshRequested()
                                } else {
                                    trackerError = failure
                                }
                            }) {
                                Icon(Icons.Rounded.Delete, contentDescription = "Remove tracker")
                            }
                        }
                    }
                }
            }

            if (editable) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = trackerUrl,
                        onValueChange = {
                            trackerUrl = it
                            trackerError = null
                        },
                        label = { Text("Add Tracker") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = trackerTier,
                        onValueChange = {
                            trackerTier = it
                            trackerError = null
                        },
                        label = { Text("Tier") },
                        singleLine = true,
                        modifier = Modifier.width(100.dp)
                    )
                    FilledTonalButton(
                        onClick = {
                            val failure = controller.addTorrentTracker(details.torrent, trackerUrl, trackerTier)
                            if (failure == null) {
                                trackerUrl = ""
                                trackerTier = "0"
                                trackerError = null
                                onRefreshRequested()
                            } else {
                                trackerError = failure
                            }
                        }
                    ) {
                        Text("Add")
                    }
                }
                trackerError?.let { message ->
                    Text(
                        message,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        activity?.let { TorrentActivitySection(it) }
        pieces?.let { TorrentPiecesSection(it) }

        TorrentFileEntriesTable(
            entries = details.entries,
            editable = editable,
            onPriorityChange = { index, priority ->
                controller.updateTorrentFileEntryPriority(details.torrent, index, priority)
                onRefreshRequested()
            }
        )
    }
}

@Composable
private fun InfoSectionCard(
    title: String,
    content: @Composable () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(title, fontWeight = FontWeight.Medium)
            content()
        }
    }
}

@Composable
private fun LibraryMetadataSection(
    category: Category,
    editor: LibraryMetadataEditorPresentation,
    draft: LibraryMetadataDraft,
    fieldErrors: Map<FilePropertyKey, String>,
    dialogError: String?,
    saveEnabled: Boolean,
    onDraftChange: (LibraryMetadataDraft, Set<FilePropertyKey>) -> Unit,
    onSave: () -> Unit
) {
    InfoSectionCard("Metadata") {
        if (!editor.editable) {
            Text(
                "Finish this file before editing its metadata.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        when (category) {
            Category.AUDIO -> {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    MetadataTextField(
                        label = "Title",
                        value = draft.title,
                        enabled = editor.editable,
                        error = fieldErrors[FilePropertyKey.TITLE],
                        modifier = Modifier.weight(1f)
                    ) { onDraftChange(draft.copy(title = it), setOf(FilePropertyKey.TITLE)) }
                    MetadataTextField(
                        label = "Artist",
                        value = draft.author,
                        enabled = editor.editable,
                        error = fieldErrors[FilePropertyKey.AUTHOR],
                        modifier = Modifier.weight(1f)
                    ) { onDraftChange(draft.copy(author = it), setOf(FilePropertyKey.AUTHOR)) }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    MetadataTextField(
                        label = "Album",
                        value = draft.album,
                        enabled = editor.editable,
                        error = fieldErrors[FilePropertyKey.ALBUM],
                        modifier = Modifier.weight(1f)
                    ) { onDraftChange(draft.copy(album = it), setOf(FilePropertyKey.ALBUM)) }
                    MetadataChoiceField(
                        label = "Genre",
                        value = draft.genre,
                        choices = editor.genreChoices,
                        enabled = editor.editable,
                        modifier = Modifier.weight(1f)
                    ) { onDraftChange(draft.copy(genre = it), setOf(FilePropertyKey.GENRE)) }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    MetadataTextField(
                        label = "Year",
                        value = draft.year,
                        enabled = editor.editable,
                        error = fieldErrors[FilePropertyKey.YEAR],
                        modifier = Modifier.weight(1f)
                    ) { onDraftChange(draft.copy(year = it), setOf(FilePropertyKey.YEAR)) }
                    MetadataTextField(
                        label = "Track",
                        value = draft.track,
                        enabled = editor.editable,
                        error = fieldErrors[FilePropertyKey.TRACK_NUMBER],
                        modifier = Modifier.weight(1f)
                    ) { onDraftChange(draft.copy(track = it), setOf(FilePropertyKey.TRACK_NUMBER)) }
                }
                MetadataDescriptionField(
                    value = draft.description,
                    enabled = editor.editable,
                    error = fieldErrors[FilePropertyKey.DESCRIPTION]
                ) { onDraftChange(draft.copy(description = it), setOf(FilePropertyKey.DESCRIPTION)) }
            }

            Category.VIDEO -> {
                MetadataTextField(
                    label = "Title",
                    value = draft.title,
                    enabled = editor.editable,
                    error = fieldErrors[FilePropertyKey.TITLE]
                ) { onDraftChange(draft.copy(title = it), setOf(FilePropertyKey.TITLE)) }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    MetadataChoiceField(
                        label = "Genre",
                        value = draft.genre,
                        choices = editor.genreChoices,
                        enabled = editor.editable,
                        modifier = Modifier.weight(1f)
                    ) { onDraftChange(draft.copy(genre = it), setOf(FilePropertyKey.GENRE)) }
                    MetadataChoiceField(
                        label = "Rating",
                        value = draft.rating,
                        choices = editor.ratingChoices,
                        enabled = editor.editable,
                        modifier = Modifier.weight(1f)
                    ) { onDraftChange(draft.copy(rating = it), setOf(FilePropertyKey.RATING)) }
                }
                MetadataTextField(
                    label = "Year",
                    value = draft.year,
                    enabled = editor.editable,
                    error = fieldErrors[FilePropertyKey.YEAR]
                ) { onDraftChange(draft.copy(year = it), setOf(FilePropertyKey.YEAR)) }
                MetadataDescriptionField(
                    value = draft.description,
                    enabled = editor.editable,
                    error = fieldErrors[FilePropertyKey.DESCRIPTION]
                ) { onDraftChange(draft.copy(description = it), setOf(FilePropertyKey.DESCRIPTION)) }
            }

            Category.IMAGE -> {
                MetadataTextField(
                    label = "Title",
                    value = draft.title,
                    enabled = editor.editable,
                    error = fieldErrors[FilePropertyKey.TITLE]
                ) { onDraftChange(draft.copy(title = it), setOf(FilePropertyKey.TITLE)) }
                MetadataDescriptionField(
                    value = draft.description,
                    enabled = editor.editable,
                    error = fieldErrors[FilePropertyKey.DESCRIPTION]
                ) { onDraftChange(draft.copy(description = it), setOf(FilePropertyKey.DESCRIPTION)) }
            }

            Category.DOCUMENT -> {
                MetadataTextField(
                    label = "Author",
                    value = draft.author,
                    enabled = editor.editable,
                    error = fieldErrors[FilePropertyKey.AUTHOR]
                ) { onDraftChange(draft.copy(author = it), setOf(FilePropertyKey.AUTHOR)) }
                MetadataDescriptionField(
                    value = draft.description,
                    enabled = editor.editable,
                    error = fieldErrors[FilePropertyKey.DESCRIPTION]
                ) { onDraftChange(draft.copy(description = it), setOf(FilePropertyKey.DESCRIPTION)) }
            }

            Category.PROGRAM -> {
                MetadataTextField(
                    label = "Title",
                    value = draft.title,
                    enabled = editor.editable,
                    error = fieldErrors[FilePropertyKey.TITLE]
                ) { onDraftChange(draft.copy(title = it), setOf(FilePropertyKey.TITLE)) }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    MetadataChoiceField(
                        label = "Platform",
                        value = draft.platform,
                        choices = editor.platformChoices,
                        enabled = editor.editable,
                        modifier = Modifier.weight(1f)
                    ) { onDraftChange(draft.copy(platform = it), setOf(FilePropertyKey.PLATFORM)) }
                    MetadataTextField(
                        label = "Company",
                        value = draft.company,
                        enabled = editor.editable,
                        error = fieldErrors[FilePropertyKey.COMPANY],
                        modifier = Modifier.weight(1f)
                    ) { onDraftChange(draft.copy(company = it), setOf(FilePropertyKey.COMPANY)) }
                }
            }

            Category.OTHER,
            Category.TORRENT -> Unit
        }
        dialogError?.let { message ->
            InlineStatusBanner(
                title = "Metadata",
                message = message,
                level = OperationNoticeLevel.ERROR
            )
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            Button(onClick = onSave, enabled = editor.editable && saveEnabled) {
                Text("Save Metadata")
            }
        }
    }
}

@Composable
private fun LibrarySharingSection(
    item: LocalFileItem,
    memberships: List<LibrarySharingMembershipPresentation>,
    onRemove: (LibrarySharingMembershipPresentation) -> Unit
) {
    InfoSectionCard("Sharing") {
        when {
            memberships.isNotEmpty() -> {
                memberships.forEachIndexed { index, membership ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(membership.label, fontWeight = FontWeight.Medium)
                            Text(
                                membership.subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                if (membership.publicCollection) "Public" else "Friends Only",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        TextButton(onClick = { onRemove(membership) }) {
                            Text("Remove from Collection")
                        }
                    }
                    if (index != memberships.lastIndex) {
                        HorizontalDivider()
                    }
                }
            }

            item.isShareable -> {
                Text(
                    "This file is not currently shared from any collection.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            else -> {
                Text(
                    "This file cannot be shared.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun MetadataTextField(
    label: String,
    value: String,
    enabled: Boolean,
    error: String?,
    modifier: Modifier = Modifier,
    onValueChange: (String) -> Unit
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            enabled = enabled,
            singleLine = true,
            isError = error != null,
            modifier = Modifier.fillMaxWidth()
        )
        error?.let {
            Text(
                it,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun MetadataDescriptionField(
    value: String,
    enabled: Boolean,
    error: String?,
    onValueChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text("Description") },
            enabled = enabled,
            isError = error != null,
            modifier = Modifier.fillMaxWidth().height(120.dp)
        )
        error?.let {
            Text(
                it,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun MetadataChoiceField(
    label: String,
    value: String,
    choices: List<String>,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onValueChange: (String) -> Unit
) {
    var expanded by remember(label, value, enabled) { mutableStateOf(false) }
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = { expanded = true },
                enabled = enabled,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(value.ifBlank { "Choose ${label.lowercase(Locale.US)}" }, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                DropdownMenuItem(
                    text = { Text("Clear") },
                    onClick = {
                        expanded = false
                        onValueChange("")
                    }
                )
                choices.forEach { choice ->
                    DropdownMenuItem(
                        text = { Text(choice) },
                        onClick = {
                            expanded = false
                            onValueChange(choice)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun TorrentSectionCard(
    title: String,
    content: @Composable () -> Unit
) {
    InfoSectionCard(title = title, content = content)
}

@Composable
private fun TorrentActivitySection(activity: TorrentActivityPresentation) {
    TorrentSectionCard("Activity") {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            MetricBadge("Seeders", activity.seeders?.toString() ?: "?")
            MetricBadge("Leechers", activity.leechers?.toString() ?: "?")
            MetricBadge("Peers", activity.peerCount.toString())
            MetricBadge("Down Rate", formatTorrentRate(activity.downloadRateBytesPerSecond))
            MetricBadge("Up Rate", formatTorrentRate(activity.uploadRateBytesPerSecond))
        }
        activity.currentTracker?.let { tracker ->
            Text(
                "Current tracker: $tracker",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (activity.peers.isEmpty()) {
            Text(
                "No active peer rows are available right now.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Address", modifier = Modifier.weight(1.2f), fontWeight = FontWeight.Medium)
                Text("Enc", modifier = Modifier.width(42.dp), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                Text("Client", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                Text("Up", modifier = Modifier.width(84.dp), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                Text("Down", modifier = Modifier.width(84.dp), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
            }
            HorizontalDivider()
            activity.peers.forEach { peer ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(peer.address, modifier = Modifier.weight(1.2f), style = MaterialTheme.typography.bodySmall)
                    Text(if (peer.encrypted) "Yes" else "No", modifier = Modifier.width(42.dp), style = MaterialTheme.typography.bodySmall)
                    Text(
                        peer.client.ifBlank { "Unknown" },
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        formatTorrentRate(peer.uploadRateBytesPerSecond),
                        modifier = Modifier.width(84.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        formatTorrentRate(peer.downloadRateBytesPerSecond),
                        modifier = Modifier.width(84.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
private fun TorrentPiecesSection(pieces: TorrentPiecesPresentation) {
    TorrentSectionCard("Pieces") {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            MetricBadge("Pieces", pieces.totalPieces.toString())
            MetricBadge("Completed", pieces.completedPieces?.toString() ?: "?")
            MetricBadge("Piece Size", formatBytes(pieces.pieceSize))
            MetricBadge("Per Cell", pieces.piecesPerCell.toString())
            MetricBadge("Ratio", String.format(Locale.US, "%.2f", pieces.ratio))
        }
        Text(
            buildString {
                append("Downloaded ${formatBytes(pieces.downloaded)}")
                if (pieces.downloaded - pieces.verifiedDownloaded > 1000) {
                    append(" (${formatBytes(pieces.verifiedDownloaded)} verified)")
                }
                append(" · Failed ${formatBytes(pieces.failedDownload)} · Uploaded ${formatBytes(pieces.uploaded)}")
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TorrentPieceLegend("Active", TorrentPieceCellPresentation(TorrentPieceCellState.ACTIVE))
            TorrentPieceLegend("Available", TorrentPieceCellPresentation(TorrentPieceCellState.AVAILABLE))
            TorrentPieceLegend("Done", TorrentPieceCellPresentation(TorrentPieceCellState.DOWNLOADED))
            TorrentPieceLegend("Unavailable", TorrentPieceCellPresentation(TorrentPieceCellState.UNAVAILABLE))
            TorrentPieceLegend("Partially Done", TorrentPieceCellPresentation(TorrentPieceCellState.PARTIAL, 0.8f))
        }
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            pieces.cells.chunked(10).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    row.forEach { cell ->
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .background(
                                    color = torrentPieceColor(cell),
                                    shape = RoundedCornerShape(4.dp)
                                )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TorrentPieceLegend(label: String, cell: TorrentPieceCellPresentation) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(
                    color = torrentPieceColor(cell),
                    shape = RoundedCornerShape(3.dp)
                )
        )
        Text(label, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun TorrentFileEntriesTable(
    entries: List<TorrentFileEntryPresentation>,
    editable: Boolean,
    onPriorityChange: (Int, TorrentFilePriority) -> Unit
) {
    TorrentSectionCard("Files") {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("File", modifier = Modifier.weight(1f), fontWeight = FontWeight.Medium)
            Text("Size", modifier = Modifier.width(92.dp), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
            Text("Done", modifier = Modifier.width(86.dp), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
            Text("Priority", modifier = Modifier.width(148.dp), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
        }
        HorizontalDivider()
        entries.forEach { entry ->
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(entry.path, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        entry.localPath
                            ?.takeIf { it.isNotBlank() && it != entry.path }
                            ?.let { localPath ->
                                Text(
                                    localPath,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                    }
                    Text(formatBytes(entry.size), modifier = Modifier.width(92.dp), style = MaterialTheme.typography.bodySmall)
                    Text("${(entry.progress.coerceIn(0f, 1f) * 100f).roundToInt()}%", modifier = Modifier.width(86.dp), style = MaterialTheme.typography.bodySmall)
                    if (editable) {
                        TorrentPriorityButton(
                            priority = entry.priority,
                            onSelect = { priority -> onPriorityChange(entry.index, priority) }
                        )
                    } else {
                        Text(entry.priority.label, modifier = Modifier.width(148.dp), style = MaterialTheme.typography.bodySmall)
                    }
                }
                LinearProgressIndicator(
                    progress = { entry.progress.coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    "${formatBytes(entry.totalDone)} of ${formatBytes(entry.size)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (entry != entries.last()) {
                HorizontalDivider()
            }
        }
    }
}

@Composable
private fun TorrentPriorityButton(
    priority: TorrentFilePriority,
    onSelect: (TorrentFilePriority) -> Unit
) {
    var expanded by remember(priority) { mutableStateOf(false) }
    Box(modifier = Modifier.width(148.dp)) {
        OutlinedButton(onClick = { expanded = true }) {
            Text(priority.label, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            TorrentFilePriority.entries.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.label) },
                    leadingIcon = {
                        if (option == priority) {
                            Icon(Icons.Rounded.Check, contentDescription = null)
                        }
                    },
                    onClick = {
                        expanded = false
                        onSelect(option)
                    }
                )
            }
        }
    }
}

@Composable
private fun SharingStatusButton(controller: ComposeAppController) {
    val desktopType = LocalDesktopTypeScale.current
    val summary = controller.sharingStatusSummary()
    val label = when {
        summary.showSignInToShareWithFriends && summary.sharedFileCount == 1 -> "Sharing 1 file · public only"
        summary.showSignInToShareWithFriends -> "Sharing ${summary.sharedFileCount} files · public only"
        summary.sharedFileCount == 1 -> "Sharing 1 file"
        else -> "Sharing ${summary.sharedFileCount} files"
    }
    var expanded by remember(
        summary.sharedFileCount,
        summary.publicCollectionCount,
        summary.friendSharedCollectionCount,
        summary.collections.map { it.sectionId to it.fileCount to it.friendCount },
        summary.showSignInToShareWithFriends
    ) {
        mutableStateOf(false)
    }
    Box {
        CompactTextButton(onClick = { expanded = true }) {
            Text(label)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            Column(
                modifier = Modifier.widthIn(min = 280.dp, max = 360.dp).padding(horizontal = 14.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(label, fontWeight = FontWeight.SemiBold)
                Text(
                    "${summary.publicCollectionCount} public collection(s) · ${summary.friendSharedCollectionCount} collection(s) shared with friends",
                    style = desktopType.meta,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    if (summary.collections.isEmpty()) {
                        "Create or share a collection from My Files or Friends to see it here."
                    } else {
                        "Open the collections you are sharing now."
                    },
                    style = desktopType.meta,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            HorizontalDivider()
            DropdownMenuItem(
                text = { Text("Open My Files") },
                onClick = {
                    expanded = false
                    controller.selectLibrarySection("library")
                }
            )
            summary.collections.forEach { collection ->
                DropdownMenuItem(
                    text = {
                        Text(
                            if (collection.publicCollection) {
                                "${collection.label} (${collection.fileCount}) · Public"
                            } else if (collection.friendCount > 0) {
                                "${collection.label} (${collection.fileCount}) · Shared with ${collection.friendCount}"
                            } else {
                                "${collection.label} (${collection.fileCount})"
                            }
                        )
                    },
                    onClick = {
                        expanded = false
                        controller.selectLibrarySection(collection.sectionId)
                    }
                )
            }
            if (summary.showSignInToShareWithFriends) {
                HorizontalDivider()
                DropdownMenuItem(
                    text = { Text("Sign in to share with friends") },
                    onClick = {
                        expanded = false
                        controller.openSharingStatusSignIn()
                    }
                )
            }
        }
    }
}

@Composable
private fun FileProcessingStatusButton(controller: ComposeAppController) {
    val desktopType = LocalDesktopTypeScale.current
    val status = controller.fileProcessingStatus ?: return
    val identity = controller.fileProcessingIdentity(status)
    var expanded by remember(status) { mutableStateOf(false) }
    val progress = if (status.total <= 0) 0f else status.finished.toFloat() / status.total.toFloat()
    val label = if (status.done) {
        "Done"
    } else {
        "Adding ${status.finished + 1} of ${status.total}"
    }

    Box {
        CompactTextButton(onClick = { expanded = true }) {
            FileIdentityIcon(icon = identity.icon, modifier = Modifier.size(16.dp), fallback = FileIconToken.FOLDER)
            Spacer(Modifier.width(6.dp))
            Text(label)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            Column(
                modifier = Modifier.widthIn(min = 280.dp, max = 360.dp).padding(horizontal = 14.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(if (status.done) "Adding Files Complete" else "Adding Files", fontWeight = FontWeight.SemiBold)
                Text(
                    identity.title,
                    style = desktopType.meta,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                LinearProgressIndicator(progress = { progress.coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth())
                Text(
                    identity.subtitle,
                    style = desktopType.meta,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            HorizontalDivider()
            if (status.done) {
                DropdownMenuItem(
                    text = { Text("Dismiss") },
                    onClick = {
                        expanded = false
                        controller.dismissFileProcessingStatus()
                    }
                )
            } else {
                DropdownMenuItem(
                    text = { Text("Stop") },
                    onClick = {
                        expanded = false
                        controller.stopLibraryFileProcessing()
                    }
                )
            }
        }
    }
}

private fun buildUnsafeSharingSummary(draft: LibraryPreferencesDraft): String {
    val enabledRules = buildList {
        if (draft.shareDownloadedFiles) add("download sharing")
        if (draft.allowPartialSharing) add("partial sharing")
        if (draft.allowDocumentSharing) add("public documents")
        if (draft.allowProgramSearchAndShare) add("program sharing")
    }
    return if (enabledRules.isEmpty()) {
        "No risky public-sharing rules are currently enabled."
    } else {
        enabledRules.joinToString(prefix = "Enabled: ")
    }
}

@Composable
private fun PreferenceInlineError(message: String) {
    Surface(
        color = MaterialTheme.colorScheme.errorContainer,
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            message,
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            color = MaterialTheme.colorScheme.onErrorContainer,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

private fun preferencesValidationError(draft: PreferencesDraft): String? {
    return when {
        draft.transfers.downloadDirectory.trim().isEmpty() ->
            "Choose a download folder to continue."
        !draft.transfers.uploadTorrentsForever &&
            draft.transfers.torrentSeedRatio.trim().toFloatOrNull() == null ->
            "Enter a numeric upload/download ratio."
        !draft.transfers.uploadTorrentsForever &&
            draft.transfers.torrentSeedDays.trim().toIntOrNull() == null ->
            "Enter a numeric number of seeding days."
        !draft.transfers.uploadTorrentsForever &&
            draft.transfers.torrentSeedHours.trim().toIntOrNull() == null ->
            "Enter a numeric number of seeding hours."
        !draft.transfers.uploadTorrentsForever &&
            (draft.transfers.torrentSeedHours.trim().toIntOrNull() ?: 0) !in 0..24 ->
            "Enter seeding hours between 0 and 24."
        draft.transfers.maxDownloadsAtOnce.trim().toIntOrNull() == null ->
            "Enter a numeric maximum for simultaneous downloads."
        draft.transfers.maxUploadsAtOnce.trim().toIntOrNull() == null ->
            "Enter a numeric maximum for simultaneous uploads."
        draft.transfers.maxDownloadKiB.trim().isNotEmpty() && draft.transfers.maxDownloadKiB.trim().toIntOrNull() == null ->
            "Enter a numeric max download rate."
        draft.transfers.maxUploadKiB.trim().isNotEmpty() && draft.transfers.maxUploadKiB.trim().toIntOrNull() == null ->
            "Enter a numeric max upload rate."
        draft.transfers.useCategorySpecificFolders && listOf(
            draft.transfers.categorySaveDirectories.audio,
            draft.transfers.categorySaveDirectories.video,
            draft.transfers.categorySaveDirectories.images,
            draft.transfers.categorySaveDirectories.documents,
            draft.transfers.categorySaveDirectories.programs,
            draft.transfers.categorySaveDirectories.other
        ).any { it.trim().isEmpty() } ->
            "Choose folders for every category-specific save location."
        draft.network.proxyMode != ProxyMode.NONE && draft.network.proxyHost.trim().isEmpty() ->
            "Enter a proxy host or disable the proxy."
        draft.network.proxyMode != ProxyMode.NONE && draft.network.proxyPort.trim().toIntOrNull() == null ->
            "Enter a numeric proxy port."
        (draft.network.proxyMode == ProxyMode.SOCKS4 || draft.network.proxyMode == ProxyMode.SOCKS5) &&
            draft.network.proxyAuthenticate &&
            draft.network.proxyUsername.trim().isEmpty() ->
            "Enter a proxy username or turn off proxy authentication."
        draft.network.gnutellaPort.trim().toIntOrNull() == null ->
            "Enter a numeric listening port."
        draft.network.torrentListenStartPort.trim().toIntOrNull() == null ->
            "Enter a numeric BitTorrent start port."
        draft.network.torrentListenEndPort.trim().toIntOrNull() == null ->
            "Enter a numeric BitTorrent end port."
        (draft.network.torrentListenStartPort.trim().toIntOrNull() ?: 0) !in 1..65535 ->
            "Enter a BitTorrent start port between 1 and 65535."
        (draft.network.torrentListenEndPort.trim().toIntOrNull() ?: 0) !in 1..65535 ->
            "Enter a BitTorrent end port between 1 and 65535."
        (draft.network.torrentListenStartPort.trim().toIntOrNull() ?: 0) >
            (draft.network.torrentListenEndPort.trim().toIntOrNull() ?: 0) ->
            "BitTorrent start port must be less than or equal to the end port."
        draft.network.portForwardMode == PortForwardMode.MANUAL && draft.network.manualPort.trim().toIntOrNull() == null ->
            "Enter a numeric manual port."
        draft.network.useCustomNetworkInterface &&
            draft.network.selectedNetworkInterfaceAddress.isNullOrBlank() ->
            "Choose a network interface or turn off manual interface binding."
        draft.library.iTunes.shareAudioAcrossLan &&
            draft.library.iTunes.requirePassword &&
            draft.library.iTunes.password.isBlank() ->
            "Enter a DAAP password or turn off protected iTunes sharing."
        else -> null
    }
}

private fun sanitizeDecimalPreferenceInput(value: String): String {
    val filtered = value.filter { it.isDigit() || it == '.' }
    val firstDecimal = filtered.indexOf('.')
    return if (firstDecimal < 0) {
        filtered
    } else {
        filtered.substring(0, firstDecimal + 1) +
            filtered.substring(firstDecimal + 1).replace(".", "")
    }
}

private fun validateOpenLinkDraft(value: String): String? {
    val trimmed = value.trim()
    if (trimmed.isEmpty()) {
        return "Enter a magnet link or torrent URL."
    }
    val uriError = runCatching { URI(trimmed) }.exceptionOrNull()?.message
    if (uriError != null) {
        return uriError
    }
    return if (trimmed.startsWith("magnet:", ignoreCase = true) || "://" in trimmed) {
        null
    } else {
        "Enter a magnet link or a full torrent URL."
    }
}

private fun menuShortcut(key: Key, shift: Boolean = false): KeyShortcut {
    val isMac = System.getProperty("os.name", "").contains("mac", ignoreCase = true)
    return KeyShortcut(
        key = key,
        meta = isMac,
        ctrl = !isMac,
        shift = shift
    )
}

private data class ColumnToggleEntry(
    val label: String,
    val visible: Boolean,
    val onToggle: () -> Unit
)

private data class PendingSelectionModifiers(
    val extendSelection: Boolean = false,
    val toggleSelection: Boolean = false
)

private class HeaderContextMenuScope(
    private val closeMenu: () -> Unit
) {
    fun dismiss() {
        closeMenu()
    }
}

@Composable
private fun ColumnVisibilityMenu(entries: List<ColumnToggleEntry>) {
    var expanded by remember(entries.map { it.label to it.visible }) { mutableStateOf(false) }
    Box {
        OutlinedButton(onClick = { expanded = true }) {
            Text("Columns")
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            entries.forEach { entry ->
                DropdownMenuItem(
                    text = { Text("${if (entry.visible) "Hide" else "Show"} ${entry.label}") },
                    onClick = {
                        entry.onToggle()
                    }
                )
            }
        }
    }
}

@Composable
private fun CollectionMenuButton(
    label: String,
    collections: List<SharedFileList>,
    onAddToCollection: (Int) -> Unit
) {
    var expanded by remember(collections.map { it.id to it.collectionName }) { mutableStateOf(false) }
    Box {
        OutlinedButton(onClick = { expanded = true }) {
            Text(label)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            collections.forEach { collection ->
                DropdownMenuItem(
                    text = { Text(collection.collectionName) },
                    onClick = {
                        expanded = false
                        onAddToCollection(collection.id)
                    }
                )
            }
        }
    }
}

@Composable
private fun DownloadsHeaderMenuButton(controller: ComposeAppController) {
    var expanded by remember(
        controller.downloadsEpoch,
        controller.clearDownloadsWhenFinished,
        controller.showUploadsInTray,
        controller.downloadSortMode,
        controller.downloadSortDescending
    ) { mutableStateOf(false) }
    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(Icons.Rounded.MoreVert, contentDescription = "Download header menu")
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text("Pause All") },
                enabled = controller.hasPausableDownloads(),
                onClick = {
                    expanded = false
                    controller.pauseAllDownloads()
                }
            )
            DropdownMenuItem(
                text = { Text("Resume All") },
                enabled = controller.hasResumableDownloads(),
                onClick = {
                    expanded = false
                    controller.resumeAllDownloads()
                }
            )
            HorizontalDivider()
            DropdownMenuItem(text = { Text("Sort by") }, enabled = false, onClick = {})
            DownloadSortMode.entries.forEach { mode ->
                DropdownMenuItem(
                    text = { Text(friendlyName(mode.name)) },
                    leadingIcon = {
                        if (controller.downloadSortMode == mode) {
                            Icon(Icons.Rounded.Check, contentDescription = null)
                        }
                    },
                    onClick = {
                        expanded = false
                        controller.applyDownloadSortMode(mode)
                    }
                )
            }
            DropdownMenuItem(
                text = { Text("Reverse Order") },
                trailingIcon = {
                    if (controller.downloadSortDescending) {
                        Icon(Icons.Rounded.Check, contentDescription = null)
                    }
                },
                onClick = {
                    expanded = false
                    controller.reverseDownloadSort()
                }
            )
            HorizontalDivider()
            DropdownMenuItem(text = { Text("Cancel") }, enabled = false, onClick = {})
            DropdownMenuItem(
                text = { Text("All Stalled") },
                enabled = controller.hasStalledDownloadItems(),
                onClick = {
                    expanded = false
                    controller.cancelAllStalledDownloads()
                }
            )
            DropdownMenuItem(
                text = { Text("All Error") },
                enabled = controller.hasErrorDownloads(),
                onClick = {
                    expanded = false
                    controller.cancelAllErrorDownloads()
                }
            )
            DropdownMenuItem(
                text = { Text("All Downloads") },
                enabled = controller.hasAnyDownloads(),
                onClick = {
                    expanded = false
                    controller.cancelAllDownloads()
                }
            )
            HorizontalDivider()
            DropdownMenuItem(
                text = { Text("Clear When Finished") },
                trailingIcon = {
                    if (controller.clearDownloadsWhenFinished) {
                        Icon(Icons.Rounded.Check, contentDescription = null)
                    }
                },
                onClick = {
                    expanded = false
                    controller.updateClearDownloadsWhenFinishedPreference(!controller.clearDownloadsWhenFinished)
                }
            )
            DropdownMenuItem(
                text = { Text("Show Uploads in Tray") },
                trailingIcon = {
                    if (controller.showUploadsInTray) {
                        Icon(Icons.Rounded.Check, contentDescription = null)
                    }
                },
                onClick = {
                    expanded = false
                    controller.updateShowUploadsInTrayPreference(!controller.showUploadsInTray)
                }
            )
            HorizontalDivider()
            DropdownMenuItem(
                text = { Text("More Transfer Options...") },
                onClick = {
                    expanded = false
                    controller.showTransferPreferences()
                }
            )
        }
    }
}

@Composable
private fun UploadsHeaderMenuButton(controller: ComposeAppController) {
    var expanded by remember(
        controller.uploadsEpoch,
        controller.clearUploadsWhenFinished,
        controller.showUploadsInTray,
        controller.uploadSortMode,
        controller.uploadSortDescending
    ) { mutableStateOf(false) }
    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(Icons.Rounded.MoreVert, contentDescription = "Upload header menu")
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text("Pause All") },
                enabled = controller.hasPausableUploads(),
                onClick = {
                    expanded = false
                    controller.pauseActiveUploads()
                }
            )
            DropdownMenuItem(
                text = { Text("Resume All") },
                enabled = controller.hasResumableUploads(),
                onClick = {
                    expanded = false
                    controller.resumeAllUploads()
                }
            )
            HorizontalDivider()
            DropdownMenuItem(text = { Text("Sort by") }, enabled = false, onClick = {})
            UploadSortMode.entries.forEach { mode ->
                DropdownMenuItem(
                    text = { Text(friendlyName(mode.name)) },
                    leadingIcon = {
                        if (controller.uploadSortMode == mode) {
                            Icon(Icons.Rounded.Check, contentDescription = null)
                        }
                    },
                    onClick = {
                        expanded = false
                        controller.applyUploadSortMode(mode)
                    }
                )
            }
            DropdownMenuItem(
                text = { Text("Reverse Order") },
                trailingIcon = {
                    if (controller.uploadSortDescending) {
                        Icon(Icons.Rounded.Check, contentDescription = null)
                    }
                },
                onClick = {
                    expanded = false
                    controller.reverseUploadSort()
                }
            )
            HorizontalDivider()
            DropdownMenuItem(text = { Text("Cancel") }, enabled = false, onClick = {})
            DropdownMenuItem(
                text = { Text("All Error") },
                enabled = controller.hasErrorUploads(),
                onClick = {
                    expanded = false
                    controller.cancelAllErrorUploads()
                }
            )
            DropdownMenuItem(
                text = { Text("All Torrents") },
                enabled = controller.hasTorrentUploads(),
                onClick = {
                    expanded = false
                    controller.confirmCancelAllTorrentUploads()
                }
            )
            DropdownMenuItem(
                text = { Text("All Uploads") },
                enabled = controller.hasAnyUploads(),
                onClick = {
                    expanded = false
                    controller.confirmCancelAllUploads()
                }
            )
            HorizontalDivider()
            DropdownMenuItem(
                text = { Text("Clear When Finished") },
                trailingIcon = {
                    if (controller.clearUploadsWhenFinished) {
                        Icon(Icons.Rounded.Check, contentDescription = null)
                    }
                },
                onClick = {
                    expanded = false
                    controller.updateClearUploadsWhenFinishedPreference(!controller.clearUploadsWhenFinished)
                }
            )
            DropdownMenuItem(
                text = { Text("Show Uploads in Tray") },
                trailingIcon = {
                    if (controller.showUploadsInTray) {
                        Icon(Icons.Rounded.Check, contentDescription = null)
                    }
                },
                onClick = {
                    expanded = false
                    controller.updateShowUploadsInTrayPreference(!controller.showUploadsInTray)
                }
            )
            HorizontalDivider()
            DropdownMenuItem(
                text = { Text("More Transfer Options...") },
                onClick = {
                    expanded = false
                    controller.showTransferPreferences()
                }
            )
        }
    }
}

@Composable
private fun CollectionTargetMenuButton(
    label: String,
    collections: List<SharedFileList>,
    onSelect: (Int) -> Unit
) {
    var expanded by remember(collections.map { it.id to it.collectionName }) { mutableStateOf(false) }
    Box {
        OutlinedButton(onClick = { expanded = true }) {
            Text(label)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            collections.forEach { collection ->
                DropdownMenuItem(
                    text = { Text(collection.collectionName) },
                    onClick = {
                        expanded = false
                        onSelect(collection.id)
                    }
                )
            }
        }
    }
}

@Composable
private fun FriendCollectionShareMenuButton(
    label: String,
    friend: FriendRosterItem,
    collections: List<SharedFileList>,
    isShared: (Int, String) -> Boolean,
    onToggleShare: (Int, String) -> Unit,
    onShareNewList: () -> Unit
) {
    var expanded by remember(friend.id, collections.map { it.id to it.collectionName }) { mutableStateOf(false) }
    Box {
        OutlinedButton(onClick = { expanded = true }) {
            Text(label)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            collections.forEach { collection ->
                val shared = isShared(collection.id, friend.id)
                DropdownMenuItem(
                    text = { Text(collection.collectionName) },
                    leadingIcon = {
                        if (shared) {
                            Icon(Icons.Rounded.Check, contentDescription = null)
                        }
                    },
                    onClick = {
                        expanded = false
                        onToggleShare(collection.id, friend.id)
                    }
                )
            }
            HorizontalDivider()
            DropdownMenuItem(
                text = { Text("Share New Collection…") },
                onClick = {
                    expanded = false
                    onShareNewList()
                }
            )
        }
    }
}

@Composable
private fun FriendPresenceModeButton(controller: ComposeAppController) {
    var expanded by remember(controller.friendDoNotDisturb, controller.friendConnectionState) { mutableStateOf(false) }
    Box {
        OutlinedButton(onClick = { expanded = true }) {
            Text(if (controller.friendDoNotDisturb) "Do Not Disturb" else "Available")
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text("Available") },
                onClick = {
                    expanded = false
                    controller.updateFriendDoNotDisturb(false)
                }
            )
            DropdownMenuItem(
                text = { Text("Do Not Disturb") },
                onClick = {
                    expanded = false
                    controller.updateFriendDoNotDisturb(true)
                }
            )
        }
    }
}

private fun showInTargetLabel(target: LibraryJumpTarget): String {
    return if (target.sectionId == "library") {
        "Show in My Files"
    } else {
        "Show in ${target.label}"
    }
}

@Composable
private fun LibraryJumpMenuButton(
    label: String,
    targets: List<LibraryJumpTarget>,
    onJump: (LibraryJumpTarget) -> Unit
) {
    var expanded by remember(targets.map { it.sectionId to it.label }) { mutableStateOf(false) }
    Box {
        OutlinedButton(onClick = { expanded = true }) {
            Text(label)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            targets.forEach { target ->
                DropdownMenuItem(
                    text = { Text(target.label) },
                    onClick = {
                        expanded = false
                        onJump(target)
                    }
                )
            }
        }
    }
}

@Composable
private fun InfoField(label: String, value: String) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun HeaderContextMenuArea(
    modifier: Modifier = Modifier,
    menuContent: @Composable HeaderContextMenuScope.() -> Unit,
    content: @Composable () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box(
        modifier = modifier.onPointerEvent(PointerEventType.Press) { event ->
            if (event.buttons.isSecondaryPressed) {
                expanded = true
            }
        }
    ) {
        content()
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            HeaderContextMenuScope { expanded = false }.menuContent()
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun rememberSelectableRowModifier(
    rowKey: Any,
    onSelect: (extendSelection: Boolean, toggleSelection: Boolean) -> Unit,
    onActivate: () -> Unit,
    onContextRequest: () -> Unit
): Modifier {
    var pending by remember(rowKey) { mutableStateOf(PendingSelectionModifiers()) }
    return Modifier
        .onPointerEvent(PointerEventType.Press) { event ->
            when {
                event.buttons.isSecondaryPressed -> onContextRequest()
                event.buttons.isPrimaryPressed -> {
                    pending = PendingSelectionModifiers(
                        extendSelection = event.keyboardModifiers.isShiftPressed,
                        toggleSelection = event.keyboardModifiers.isMetaPressed || event.keyboardModifiers.isCtrlPressed
                    )
                }
            }
        }
        .combinedClickable(
            onClick = { onSelect(pending.extendSelection, pending.toggleSelection) },
            onDoubleClick = onActivate
        )
}

private fun handleTableKeyEvent(
    event: KeyEvent,
    moveSelection: (Int) -> Unit,
    extendSelection: (Int) -> Unit,
    selectAll: () -> Unit,
    activateSelection: () -> Unit,
    deleteSelection: (() -> Unit)? = null
): Boolean {
    if (event.type != KeyEventType.KeyDown) {
        return false
    }
    return when {
        event.key == Key.DirectionUp -> {
            if (event.isShiftPressed) {
                extendSelection(-1)
            } else {
                moveSelection(-1)
            }
            true
        }

        event.key == Key.DirectionDown -> {
            if (event.isShiftPressed) {
                extendSelection(1)
            } else {
                moveSelection(1)
            }
            true
        }

        event.key == Key.A && (event.isMetaPressed || event.isCtrlPressed) -> {
            selectAll()
            true
        }

        event.key == Key.Enter -> {
            activateSelection()
            true
        }

        deleteSelection != null && (event.key == Key.Delete || event.key == Key.Backspace) -> {
            deleteSelection()
            true
        }

        else -> false
    }
}

private fun selectedSummaryLabel(count: Int, noun: String): String {
    return if (count == 1) "1 $noun selected" else "$count ${noun}s selected"
}

@Composable
private fun SortableHeaderText(
    label: String,
    active: Boolean,
    descending: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Text(
        text = if (active) "$label ${if (descending) "↓" else "↑"}" else label,
        modifier = modifier.clickable(onClick = onClick),
        fontWeight = FontWeight.SemiBold,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
private fun EmptyState(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    body: String
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.width(560.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary)
                Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
                Text(body, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

private fun buildSearchTabSummary(tab: SearchTabSession, resultCount: Int): String {
    val total = tab.results.size
    val shown = if (resultCount == total) "$resultCount grouped results" else "$resultCount shown of $total grouped results"
    val filters = buildList {
        if (tab.filterText.isNotBlank()) add("text")
        if (tab.friendsOnly) add("friends only")
        if (tab.sourceFilter != SearchSourceFilter.ALL) add(searchSourceFilterLabel(tab.sourceFilter).lowercase(Locale.US))
        tab.selectedFriendFacetId?.let { add("friend") }
        tab.displayCategory?.let { add(friendlyName(it.name).lowercase(Locale.US)) }
        tab.selectedPropertyFacets.keys.forEach { add(it.name.lowercase(Locale.US)) }
        tab.selectedRangeFacets.keys.forEach { add(it.name.lowercase(Locale.US)) }
    }
    val base = when (tab.searchType) {
        org.limewire.core.api.search.SearchDetails.SearchType.KEYWORD -> "Keyword search for \"${tab.query}\""
        org.limewire.core.api.search.SearchDetails.SearchType.WHATS_NEW -> "What's New"
        org.limewire.core.api.search.SearchDetails.SearchType.SINGLE_BROWSE -> "Browse Friend"
        org.limewire.core.api.search.SearchDetails.SearchType.MULTIPLE_BROWSE -> "Browse Files"
        org.limewire.core.api.search.SearchDetails.SearchType.ALL_FRIENDS_BROWSE -> "Browse Friends' Files"
    }
    val categoryLabel = friendlyName((tab.displayCategory ?: tab.category).name)
    return listOf(base, shown, categoryLabel, filters.takeIf { it.isNotEmpty() }?.joinToString(" · "))
        .filterNotNull()
        .joinToString(" · ")
}

private fun searchSourceFilterLabel(filter: SearchSourceFilter): String {
    return when (filter) {
        SearchSourceFilter.ALL -> "All sources"
        SearchSourceFilter.FRIENDS -> "Friend sources"
        SearchSourceFilter.NETWORK -> "Network sources"
        SearchSourceFilter.BROWSABLE -> "Browsable"
    }
}

private fun librarySectionSummary(section: LibrarySection?): String {
    if (section == null) {
        return "Complete library"
    }
    val itemCount = section.list.size()
    val count = "$itemCount file${if (itemCount == 1) "" else "s"}"
    return if (section.isShared) {
        "${if (section.isPublic) "Public collection" else "Friend collection"} · $count"
    } else {
        "Complete library · $count"
    }
}

private fun buildLibraryItemMeta(identitySubtitle: String, item: LocalFileItem, isPlaying: Boolean): String {
    val parts = mutableListOf<String>()
    identitySubtitle.takeIf(String::isNotBlank)?.let(parts::add)
    val flags = mutableListOf<String>()
    if (isPlaying) {
        flags += "Playing"
    }
    if (!item.isLoaded) {
        flags += "Indexing"
    }
    if (item.isIncomplete) {
        flags += "Incomplete"
    }
    if (item.isShareable) {
        flags += "Shareable"
    }
    if (flags.isEmpty()) {
        flags += "Ready"
    }
    parts += flags.joinToString(" · ")
    return parts.filter(String::isNotBlank).joinToString(" · ")
}

private fun searchResultKey(result: GroupedSearchResult): String = result.urn.toString()

private fun compactResultKey(value: String): String {
    return if (value.length <= 22) value else value.take(10) + "…" + value.takeLast(8)
}

private fun searchTypeLabel(searchType: org.limewire.core.api.search.SearchDetails.SearchType): String {
    return when (searchType) {
        org.limewire.core.api.search.SearchDetails.SearchType.KEYWORD -> "Keyword Search"
        org.limewire.core.api.search.SearchDetails.SearchType.WHATS_NEW -> "What's New"
        org.limewire.core.api.search.SearchDetails.SearchType.SINGLE_BROWSE -> "Browse Friend"
        org.limewire.core.api.search.SearchDetails.SearchType.MULTIPLE_BROWSE -> "Browse Files"
        org.limewire.core.api.search.SearchDetails.SearchType.ALL_FRIENDS_BROWSE -> "Browse Friends' Files"
    }
}

private fun connectionStatusLabel(
    strength: org.limewire.core.api.connection.ConnectionStrength,
    verbose: Boolean
): String {
    return when (strength) {
        org.limewire.core.api.connection.ConnectionStrength.NO_INTERNET -> "No Internet"
        org.limewire.core.api.connection.ConnectionStrength.DISCONNECTED -> "Disconnected"
        org.limewire.core.api.connection.ConnectionStrength.CONNECTING -> "Connecting…"
        org.limewire.core.api.connection.ConnectionStrength.WEAK -> if (verbose) "Limited connection" else "Connected"
        org.limewire.core.api.connection.ConnectionStrength.WEAK_PLUS -> if (verbose) "Fair connection" else "Connected"
        org.limewire.core.api.connection.ConnectionStrength.MEDIUM -> if (verbose) "Good connection" else "Connected"
        org.limewire.core.api.connection.ConnectionStrength.MEDIUM_PLUS -> if (verbose) "Strong connection" else "Connected"
        org.limewire.core.api.connection.ConnectionStrength.FULL -> if (verbose) "Fully connected" else "Connected"
        org.limewire.core.api.connection.ConnectionStrength.TURBO -> if (verbose) "Fully connected" else "Connected"
    }
}

private fun connectionStatusBody(
    strength: org.limewire.core.api.connection.ConnectionStrength,
    friendConnectionState: FriendConnectionEvent.Type?
): String {
    val networkCopy = when (strength) {
        org.limewire.core.api.connection.ConnectionStrength.NO_INTERNET -> "This computer is not connected to the internet."
        org.limewire.core.api.connection.ConnectionStrength.DISCONNECTED -> "WireShare could not stay connected to the peer network."
        org.limewire.core.api.connection.ConnectionStrength.CONNECTING -> "WireShare is still connecting to the peer network."
        org.limewire.core.api.connection.ConnectionStrength.WEAK -> "WireShare is connected to only a few peers, so search and browse may be limited."
        org.limewire.core.api.connection.ConnectionStrength.WEAK_PLUS -> "WireShare is connected, but results may still be limited."
        org.limewire.core.api.connection.ConnectionStrength.MEDIUM -> "Search and browse are working while the network continues to build."
        org.limewire.core.api.connection.ConnectionStrength.MEDIUM_PLUS -> "WireShare has a strong connection for normal searching and transfers."
        org.limewire.core.api.connection.ConnectionStrength.FULL -> "WireShare is fully connected and ready for search, browse, and transfers."
        org.limewire.core.api.connection.ConnectionStrength.TURBO -> "WireShare has a strong network connection with many peers available."
    }
    val friendsCopy = when (friendConnectionState) {
        FriendConnectionEvent.Type.CONNECTED -> "Friends are signed in."
        FriendConnectionEvent.Type.CONNECTING -> "Friends are still signing in."
        FriendConnectionEvent.Type.CONNECT_FAILED -> "Friends sign-in needs attention."
        FriendConnectionEvent.Type.DISCONNECTED, null -> "Friends are signed out."
    }
    return "$networkCopy $friendsCopy"
}

@Composable
private fun FileIdentityIcon(
    icon: FileIconPresentation?,
    modifier: Modifier = Modifier,
    fallback: FileIconToken = FileIconToken.FOLDER
) {
    val resolved = icon ?: FileIconPresentation(fallback)
    BoxWithConstraints(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = fileIdentityVector(resolved.token),
            contentDescription = null,
            tint = fileIdentityTint(resolved.token),
            modifier = Modifier.fillMaxSize(0.84f)
        )
        val showBadge = resolved.badge != null && minWidth >= 22.dp && minHeight >= 22.dp
        if (showBadge) {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(4.dp),
                tonalElevation = 1.dp,
                shadowElevation = 0.dp,
                modifier = Modifier.align(Alignment.BottomEnd)
            ) {
                Text(
                    text = resolved.badge,
                    modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 7.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun FileIdentityHeaderCard(
    title: String,
    subtitle: String,
    icon: FileIconPresentation,
    tertiary: String? = null
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FileIdentityIcon(
                icon = icon,
                modifier = Modifier.size(28.dp)
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                if (subtitle.isNotBlank()) {
                    Text(
                        subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                tertiary?.takeIf(String::isNotBlank)?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

private fun fileIdentityVector(token: FileIconToken) = when (token) {
    FileIconToken.FOLDER -> Icons.Rounded.Folder
    FileIconToken.AUDIO -> Icons.Rounded.LibraryMusic
    FileIconToken.VIDEO -> Icons.Rounded.Movie
    FileIconToken.IMAGE -> Icons.Rounded.Photo
    FileIconToken.DOCUMENT -> Icons.Rounded.Description
    FileIconToken.TEXT -> Icons.AutoMirrored.Rounded.TextSnippet
    FileIconToken.PDF -> Icons.Rounded.PictureAsPdf
    FileIconToken.SPREADSHEET -> Icons.Rounded.TableChart
    FileIconToken.PRESENTATION -> Icons.Rounded.Slideshow
    FileIconToken.CODE -> Icons.Rounded.Code
    FileIconToken.PLAYLIST -> Icons.AutoMirrored.Rounded.PlaylistAdd
    FileIconToken.ARCHIVE -> Icons.Rounded.Archive
    FileIconToken.PROGRAM -> Icons.Rounded.Apps
    FileIconToken.TORRENT -> Icons.Rounded.Download
    FileIconToken.OTHER -> Icons.AutoMirrored.Rounded.InsertDriveFile
}

@Composable
private fun fileIdentityTint(token: FileIconToken): Color {
    return when (token) {
        FileIconToken.FOLDER -> MaterialTheme.colorScheme.primary
        FileIconToken.AUDIO,
        FileIconToken.PLAYLIST -> MaterialTheme.colorScheme.tertiary

        FileIconToken.VIDEO,
        FileIconToken.PRESENTATION,
        FileIconToken.TORRENT -> MaterialTheme.colorScheme.primary

        FileIconToken.IMAGE -> MaterialTheme.colorScheme.secondary

        FileIconToken.DOCUMENT,
        FileIconToken.TEXT,
        FileIconToken.SPREADSHEET -> MaterialTheme.colorScheme.onSurfaceVariant

        FileIconToken.PDF -> MaterialTheme.colorScheme.error

        FileIconToken.CODE,
        FileIconToken.PROGRAM -> MaterialTheme.colorScheme.secondary

        FileIconToken.ARCHIVE,
        FileIconToken.OTHER -> MaterialTheme.colorScheme.onSurfaceVariant
    }
}

private fun friendlyName(name: String): String {
    return name
        .lowercase(Locale.US)
        .split('_')
        .joinToString(" ") { token ->
            token.replaceFirstChar { character ->
                if (character.isLowerCase()) {
                    character.titlecase(Locale.US)
                } else {
                    character.toString()
                }
            }
        }
}

private fun LibrarySortMode.asLibraryColumn(): LibraryColumn {
    return when (this) {
        LibrarySortMode.NAME -> LibraryColumn.NAME
        LibrarySortMode.FILENAME -> LibraryColumn.FILENAME
        LibrarySortMode.EXTENSION -> LibraryColumn.EXTENSION
        LibrarySortMode.TYPE -> LibraryColumn.TYPE
        LibrarySortMode.SIZE -> LibraryColumn.SIZE
        LibrarySortMode.ACTIVITY -> LibraryColumn.ACTIVITY
        LibrarySortMode.HITS -> LibraryColumn.HITS
        LibrarySortMode.UPLOADS -> LibraryColumn.UPLOADS
        LibrarySortMode.UPLOAD_ATTEMPTS -> LibraryColumn.UPLOAD_ATTEMPTS
        LibrarySortMode.UPDATED -> LibraryColumn.UPDATED
        LibrarySortMode.LOCATION -> LibraryColumn.LOCATION
        LibrarySortMode.LENGTH -> LibraryColumn.LENGTH
        LibrarySortMode.BITRATE -> LibraryColumn.BITRATE
        LibrarySortMode.TRACK -> LibraryColumn.TRACK
        LibrarySortMode.ARTIST -> LibraryColumn.ARTIST
        LibrarySortMode.ALBUM -> LibraryColumn.ALBUM
        LibrarySortMode.GENRE -> LibraryColumn.GENRE
        LibrarySortMode.YEAR -> LibraryColumn.YEAR
        LibrarySortMode.AUTHOR -> LibraryColumn.AUTHOR
        LibrarySortMode.COMPANY -> LibraryColumn.COMPANY
        LibrarySortMode.PLATFORM -> LibraryColumn.PLATFORM
        LibrarySortMode.DESCRIPTION -> LibraryColumn.DESCRIPTION
        LibrarySortMode.FILES -> LibraryColumn.FILES
        LibrarySortMode.TRACKERS -> LibraryColumn.TRACKERS
    }
}

private fun SearchSortMode.asSearchColumn(): SearchColumn {
    return when (this) {
        SearchSortMode.RELEVANCE -> SearchColumn.NAME
        SearchSortMode.NAME -> SearchColumn.NAME
        SearchSortMode.FROM -> SearchColumn.FROM
        SearchSortMode.FILENAME -> SearchColumn.FILENAME
        SearchSortMode.EXTENSION -> SearchColumn.EXTENSION
        SearchSortMode.TYPE -> SearchColumn.TYPE
        SearchSortMode.SIZE -> SearchColumn.SIZE
        SearchSortMode.SOURCES -> SearchColumn.SOURCES
        SearchSortMode.FRIENDS -> SearchColumn.FRIENDS
        SearchSortMode.LENGTH -> SearchColumn.LENGTH
        SearchSortMode.QUALITY -> SearchColumn.QUALITY
        SearchSortMode.BITRATE -> SearchColumn.BITRATE
        SearchSortMode.TRACK -> SearchColumn.TRACK
        SearchSortMode.ARTIST -> SearchColumn.ARTIST
        SearchSortMode.ALBUM -> SearchColumn.ALBUM
        SearchSortMode.GENRE -> SearchColumn.GENRE
        SearchSortMode.YEAR -> SearchColumn.YEAR
        SearchSortMode.AUTHOR -> SearchColumn.AUTHOR
        SearchSortMode.COMPANY -> SearchColumn.COMPANY
        SearchSortMode.PLATFORM -> SearchColumn.PLATFORM
        SearchSortMode.DESCRIPTION -> SearchColumn.DESCRIPTION
        SearchSortMode.FILES -> SearchColumn.FILES
        SearchSortMode.TRACKERS -> SearchColumn.TRACKERS
    }
}

private fun libraryColumnLabel(category: Category?, column: LibraryColumn): String {
    return when (column) {
        LibraryColumn.FILENAME -> "Filename"
        LibraryColumn.EXTENSION -> "Extension"
        LibraryColumn.UPLOAD_ATTEMPTS -> "Upload Attempts"
        LibraryColumn.LOCATION -> "Location"
        LibraryColumn.LENGTH -> "Length"
        LibraryColumn.BITRATE -> "Bitrate"
        LibraryColumn.TRACK -> "Track"
        LibraryColumn.ARTIST -> "Artist"
        LibraryColumn.ALBUM -> "Album"
        LibraryColumn.GENRE -> "Genre"
        LibraryColumn.YEAR -> "Year"
        LibraryColumn.AUTHOR -> if (category == Category.AUDIO) "Artist" else "Author"
        LibraryColumn.COMPANY -> "Company"
        LibraryColumn.PLATFORM -> "Platform"
        LibraryColumn.DESCRIPTION -> "Description"
        LibraryColumn.FILES -> "Files"
        LibraryColumn.TRACKERS -> "Trackers"
        else -> friendlyName(column.name)
    }
}

private fun searchColumnLabel(category: SearchCategory, column: SearchColumn): String {
    return when (column) {
        SearchColumn.FROM -> "From"
        SearchColumn.FILENAME -> "Filename"
        SearchColumn.EXTENSION -> "Extension"
        SearchColumn.LENGTH -> "Length"
        SearchColumn.QUALITY -> "Quality"
        SearchColumn.BITRATE -> "Bitrate"
        SearchColumn.TRACK -> "Track"
        SearchColumn.ARTIST -> "Artist"
        SearchColumn.ALBUM -> "Album"
        SearchColumn.GENRE -> "Genre"
        SearchColumn.YEAR -> "Year"
        SearchColumn.AUTHOR -> if (category == SearchCategory.AUDIO) "Artist" else "Author"
        SearchColumn.COMPANY -> "Company"
        SearchColumn.PLATFORM -> "Platform"
        SearchColumn.DESCRIPTION -> "Description"
        SearchColumn.FILES -> "Files"
        SearchColumn.TRACKERS -> "Trackers"
        else -> friendlyName(column.name)
    }
}

private fun libraryColumnValueText(item: LocalFileItem, column: LibraryColumn): String {
    return when (column) {
        LibraryColumn.FILENAME -> fileNameWithoutExtension(item.fileName).displayFallback()
        LibraryColumn.EXTENSION -> fileExtensionText(item.fileName).displayFallback()
        LibraryColumn.TYPE -> item.category.getSingularName()
        LibraryColumn.SIZE -> formatBytes(item.size)
        LibraryColumn.ACTIVITY -> "${item.numHits} hits · ${item.numUploads} uploads"
        LibraryColumn.HITS -> item.numHits.toString()
        LibraryColumn.UPLOADS -> item.numUploads.toString()
        LibraryColumn.UPLOAD_ATTEMPTS -> item.numUploadAttempts.toString()
        LibraryColumn.UPDATED -> formatDate(item.lastModifiedTime)
        LibraryColumn.LOCATION -> item.getPropertyString(FilePropertyKey.LOCATION).takeIf(String::isNotBlank)
            ?: item.file.parent
            ?: "—"
        LibraryColumn.LENGTH -> metadataDurationText(item.getProperty(FilePropertyKey.LENGTH))
        LibraryColumn.BITRATE -> bitrateText(item.getProperty(FilePropertyKey.BITRATE))
        LibraryColumn.TRACK -> trackText(item.getProperty(FilePropertyKey.TRACK_NUMBER))
        LibraryColumn.ARTIST -> item.getPropertyString(FilePropertyKey.AUTHOR).displayFallback()
        LibraryColumn.ALBUM -> item.getPropertyString(FilePropertyKey.ALBUM).displayFallback()
        LibraryColumn.GENRE -> item.getPropertyString(FilePropertyKey.GENRE).displayFallback()
        LibraryColumn.YEAR -> item.getPropertyString(FilePropertyKey.YEAR).displayFallback()
        LibraryColumn.AUTHOR -> item.getPropertyString(FilePropertyKey.AUTHOR).displayFallback()
        LibraryColumn.COMPANY -> item.getPropertyString(FilePropertyKey.COMPANY).displayFallback()
        LibraryColumn.PLATFORM -> item.getPropertyString(FilePropertyKey.PLATFORM).displayFallback()
        LibraryColumn.DESCRIPTION -> item.getPropertyString(FilePropertyKey.DESCRIPTION).displayFallback()
        LibraryColumn.FILES -> torrentFilesValue(item.getProperty(FilePropertyKey.TORRENT) as? Torrent)
        LibraryColumn.TRACKERS -> torrentTrackersValue(item.getProperty(FilePropertyKey.TORRENT) as? Torrent)
        LibraryColumn.NAME -> item.fileName
    }
}

private fun searchColumnValueText(
    primary: SearchResult?,
    result: GroupedSearchResult,
    column: SearchColumn
): String {
    return when (column) {
        SearchColumn.FROM -> searchSourceText(result)
        SearchColumn.FILENAME -> primary?.fileNameWithoutExtension?.displayFallback()
            ?: fileNameWithoutExtension(result.fileName).displayFallback()
        SearchColumn.EXTENSION -> primary?.fileExtension?.displayFallback()
            ?: fileExtensionText(result.fileName).displayFallback()
        SearchColumn.TYPE -> primary?.category?.getSingularName() ?: "File"
        SearchColumn.SIZE -> primary?.size?.let(::formatBytes) ?: "Unknown"
        SearchColumn.SOURCES -> result.sources.size.toString()
        SearchColumn.FRIENDS -> result.friends.size.toString()
        SearchColumn.LENGTH -> metadataDurationText(primary?.getProperty(FilePropertyKey.LENGTH))
        SearchColumn.QUALITY -> qualityText(primary)
        SearchColumn.BITRATE -> bitrateText(primary?.getProperty(FilePropertyKey.BITRATE))
        SearchColumn.TRACK -> trackText(primary?.getProperty(FilePropertyKey.TRACK_NUMBER))
        SearchColumn.ARTIST -> primary?.getProperty(FilePropertyKey.AUTHOR)?.toString().displayFallback()
        SearchColumn.ALBUM -> primary?.getProperty(FilePropertyKey.ALBUM)?.toString().displayFallback()
        SearchColumn.GENRE -> primary?.getProperty(FilePropertyKey.GENRE)?.toString().displayFallback()
        SearchColumn.YEAR -> primary?.getProperty(FilePropertyKey.YEAR)?.toString().displayFallback()
        SearchColumn.AUTHOR -> primary?.getProperty(FilePropertyKey.AUTHOR)?.toString().displayFallback()
        SearchColumn.COMPANY -> primary?.getProperty(FilePropertyKey.COMPANY)?.toString().displayFallback()
        SearchColumn.PLATFORM -> primary?.getProperty(FilePropertyKey.PLATFORM)?.toString().displayFallback()
        SearchColumn.DESCRIPTION -> primary?.getProperty(FilePropertyKey.DESCRIPTION)?.toString().displayFallback()
        SearchColumn.FILES -> torrentFilesValue(primary?.getProperty(FilePropertyKey.TORRENT) as? Torrent)
        SearchColumn.TRACKERS -> torrentTrackersValue(primary?.getProperty(FilePropertyKey.TORRENT) as? Torrent)
        SearchColumn.NAME -> result.fileName
    }
}

private fun torrentFilesValue(torrent: Torrent?): String {
    return torrent?.torrentFileEntries?.size?.toString() ?: "—"
}

private fun torrentTrackersValue(torrent: Torrent?): String {
    return torrent?.trackers?.size?.toString() ?: "—"
}

private fun fileNameWithoutExtension(fileName: String): String {
    return FileUtils.getFilenameNoExtension(fileName)
}

private fun fileExtensionText(fileName: String): String {
    return FileUtils.getFileExtension(fileName)
}

private fun searchSourceText(result: GroupedSearchResult): String {
    return result.friends.firstOrNull()?.renderName?.displayFallback()
        ?: result.sources.firstOrNull()?.friendPresence?.friend?.renderName?.displayFallback()
        ?: if (result.isAnonymous) "P2P" else "Network"
}

private fun metadataDurationText(value: Any?): String {
    val seconds = when (value) {
        is Number -> value.toLong()
        is String -> value.toLongOrNull()
        else -> null
    } ?: return "—"
    if (seconds <= 0L) {
        return "—"
    }
    return formatDuration(seconds)
}

private fun bitrateText(value: Any?): String {
    val bitrate = when (value) {
        is Number -> value.toLong()
        is String -> value.toLongOrNull()
        else -> null
    } ?: return "—"
    return bitrate.toString()
}

private fun trackText(value: Any?): String {
    return value?.toString()?.trim().takeUnless { it.isNullOrBlank() } ?: "—"
}

private fun qualityText(primary: SearchResult?): String {
    val score = when (val value = primary?.getProperty(FilePropertyKey.QUALITY)) {
        is Number -> value.toLong()
        is String -> value.toLongOrNull()
        else -> null
    } ?: return "—"
    val label = when (score) {
        1L -> "Poor"
        2L -> "Good"
        3L -> "Excellent"
        else -> "Unknown"
    }
    val bitrate = bitrateText(primary?.getProperty(FilePropertyKey.BITRATE))
    return if (bitrate == "—") label else "$label ($bitrate)"
}

private fun String?.displayFallback(): String = this?.takeIf(String::isNotBlank) ?: "—"

private fun buildSearchMetadata(identitySubtitle: String, primary: SearchResult?, result: GroupedSearchResult): String {
    val parts = mutableListOf<String>()
    identitySubtitle.takeIf(String::isNotBlank)?.let(parts::add)
    if (parts.isEmpty()) {
        val category = primary?.category?.getSingularName() ?: "File"
        val sizeText = primary?.size?.let(::formatBytes) ?: "Unknown size"
        parts += "$category · $sizeText"
    }
    parts += "${result.sources.size} source(s)"
    parts += "${result.friends.size} friend source(s)"
    return parts.joinToString(" · ")
}

private fun downloadRowStatusMessage(item: DownloadItem): String {
    return when (item.state) {
        DownloadState.RESUMING -> "Resuming"
        DownloadState.CANCELLED -> "Cancelled"
        DownloadState.FINISHING -> "Finishing..."
        DownloadState.DONE -> "Done"
        DownloadState.CONNECTING -> {
            val sourceCount = item.downloadSourceCount
            if (sourceCount <= 0) {
                "Connecting..."
            } else {
                "Connecting to ${countLabel(sourceCount, "source")}"
            }
        }

        DownloadState.DOWNLOADING -> {
            val progress = "${formatBytes(item.currentSize)} of ${formatBytes(item.totalSize)} (${formatRate(item.downloadSpeed)})"
            val sourceCount = item.downloadSourceCount
            if (sourceCount <= 0) {
                progress
            } else {
                "$progress from ${countLabel(sourceCount, "source")}"
            }
        }

        DownloadState.PAUSED -> "Paused - ${formatBytes(item.currentSize)} of ${formatBytes(item.totalSize)}"
        DownloadState.LOCAL_QUEUED -> queueStatusMessage(item.remainingTimeInState)
        DownloadState.REMOTE_QUEUED -> {
            when (val queuePosition = item.remoteQueuePosition) {
                -1, Int.MAX_VALUE -> queueStatusMessage(item.remainingTimeInState)
                1 -> "Waiting - Next in line"
                else -> "Waiting - $queuePosition in line"
            }
        }

        DownloadState.STALLED -> {
            if (item.downloadItemType == DownloadItem.DownloadItemType.BITTORRENT) {
                "Error downloading torrent"
            } else {
                "Stalled - ${formatBytes(item.currentSize)} of ${formatBytes(item.totalSize)}"
            }
        }

        DownloadState.TRYING_AGAIN -> {
            if (item.remainingTimeInState == DownloadItem.UNKNOWN_TIME) {
                "Looking for file..."
            } else {
                "Looking for file (${formatDuration(item.remainingTimeInState)} left)"
            }
        }

        DownloadState.ERROR -> {
            val message = item.errorState.message
            if (item.errorState == DownloadItem.ErrorState.INVALID) {
                message
            } else {
                "Unable to download: $message"
            }
        }

        DownloadState.DANGEROUS -> "File deleted - Dangerous file"
    }
}

private fun uploadRowStatusMessage(item: UploadItem): String {
    return when (item.state) {
        UploadState.BROWSE_HOST,
        UploadState.BROWSE_HOST_DONE -> "Library was browsed"

        UploadState.DONE -> "Done uploading"

        UploadState.UPLOADING -> {
            if (item.uploadItemType == UploadItem.UploadItemType.BITTORRENT) {
                val ratioText = if (item.seedRatio >= 0f) {
                    String.format(Locale.US, "%.2f", item.seedRatio)
                } else {
                    "n/a"
                }
                "${formatRate(item.uploadSpeed)} to ${countLabel(item.numUploadConnections, "person", "people")} · ratio: $ratioText"
            } else {
                "${formatBytes(item.totalAmountUploaded)} of ${formatBytes(item.fileSize)} (${formatRate(item.uploadSpeed)})"
            }
        }

        UploadState.PAUSED -> "Paused"
        UploadState.QUEUED -> "Waiting..."
        UploadState.REQUEST_ERROR -> "Unable to upload: invalid request"
        UploadState.LIMIT_REACHED -> "Unable to upload: upload limit reached"
        UploadState.CANCELED -> "Cancelled"
    }
}

private fun queueStatusMessage(remaining: Long): String {
    return if (remaining == DownloadItem.UNKNOWN_TIME) {
        "Waiting..."
    } else {
        "Waiting - ${formatDuration(remaining)}"
    }
}

private fun countLabel(count: Int, singular: String, plural: String = "${singular}s"): String {
    return if (count == 1) {
        "1 $singular"
    } else {
        "$count $plural"
    }
}

@Composable
private fun BrowseFailureDialog(controller: ComposeAppController, state: BrowseFailureDialogState) {
    AlertDialog(
        onDismissRequest = { controller.closeBrowseFailureDialog() },
        confirmButton = {
            TextButton(onClick = { controller.closeBrowseFailureDialog() }) {
                Text("Close")
            }
        },
        title = { Text(state.title) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (state.friends.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Friends", fontWeight = FontWeight.SemiBold)
                        state.friends.forEach { name ->
                            Text("• $name", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                if (state.users.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Users", fontWeight = FontWeight.SemiBold)
                        state.users.forEach { name ->
                            Text("• $name", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                if (state.friends.isEmpty() && state.users.isEmpty()) {
                    Text(
                        "WireShare could not browse one or more sources for this tab.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    )
}

private fun browseStatusTitle(tab: SearchTabSession, status: BrowseStatusPresentation): String {
    return when (status.state) {
        BrowseState.LOADING -> when (tab.searchType) {
            SearchDetails.SearchType.ALL_FRIENDS_BROWSE -> "Loading friends' shared files"
            SearchDetails.SearchType.SINGLE_BROWSE -> "Loading shared files"
            SearchDetails.SearchType.MULTIPLE_BROWSE -> "Loading source libraries"
            else -> "Loading browse results"
        }

        BrowseState.UPDATED -> "New shared files are ready"
        BrowseState.PARTIAL_FAIL -> "Some sources could not be opened"
        BrowseState.UPDATED_PARTIAL_FAIL -> "New files are ready, but some sources failed"
        BrowseState.FAILED -> "Browse failed"
        BrowseState.OFFLINE -> if (tab.searchType == SearchDetails.SearchType.SINGLE_BROWSE) {
            "This friend is offline"
        } else {
            "Sources are offline"
        }
        BrowseState.NO_FRIENDS_SHARING -> "No friends are sharing files with you"
        BrowseState.LOADED -> "Browse complete"
    }
}

private fun browseStatusBody(
    tab: SearchTabSession,
    status: BrowseStatusPresentation,
    controller: ComposeAppController
): String {
    return when (status.state) {
        BrowseState.LOADING -> if (tab.browseRefreshing) {
            "This browse is refreshing. Updated files will appear when it finishes."
        } else {
            "Waiting for shared files to respond."
        }

        BrowseState.UPDATED -> "This view has changed. Refresh to load the latest shared files."
        BrowseState.PARTIAL_FAIL -> "Some sources responded, but others could not be reached. Retry the browse or review the failed sources."
        BrowseState.UPDATED_PARTIAL_FAIL -> "New results are ready, but one or more sources failed during the last browse."
        BrowseState.FAILED -> "This browse could not be completed. Retry it or review the failed sources."
        BrowseState.OFFLINE -> when {
            controller.canSignInFromBrowseStatus(tab) ->
                "You are signed out. Sign in to continue."
            controller.canChatFromBrowseStatus(tab) ->
                "This friend is not sharing with you right now. You can chat to follow up."
            else ->
                "This source is not signed in right now."
        }

        BrowseState.NO_FRIENDS_SHARING -> when {
            controller.canSignInFromBrowseStatus(tab) ->
                "You are signed out. Sign in to see shared friend libraries."
            controller.canChatFromBrowseStatus(tab) ->
                "No shared files are available from this source right now. You can chat to ask them to share."
            else ->
                "None of the current friend sources are sharing files with you yet."
        }

        BrowseState.LOADED -> "Browse complete."
    }
}

private fun searchEmptyStateTitle(tab: SearchTabSession, filteredOut: Boolean): String {
    if (filteredOut) {
        return "No results match the current refinements"
    }
    when (tab.browseStatus?.state) {
        BrowseState.FAILED -> return "Browse failed"
        BrowseState.OFFLINE -> return "Browse target is offline"
        BrowseState.NO_FRIENDS_SHARING -> return "No shared files are available"
        BrowseState.PARTIAL_FAIL -> return "Some browse sources failed"
        BrowseState.UPDATED,
        BrowseState.UPDATED_PARTIAL_FAIL,
        BrowseState.LOADING,
        BrowseState.LOADED,
        null -> Unit
    }
    return when (tab.searchType) {
        SearchDetails.SearchType.KEYWORD -> "Searching…"
        SearchDetails.SearchType.WHATS_NEW -> "Looking for what's new…"
        SearchDetails.SearchType.SINGLE_BROWSE -> "Waiting for shared files"
        SearchDetails.SearchType.MULTIPLE_BROWSE -> "Waiting for source libraries"
        SearchDetails.SearchType.ALL_FRIENDS_BROWSE -> "Waiting for friends' libraries"
    }
}

private fun searchEmptyStateBody(tab: SearchTabSession, filteredOut: Boolean): String {
    if (filteredOut) {
        return "Try clearing the text filter, removing a facet, or resetting refinements."
    }
    tab.browseStatus?.let { status ->
        if (tab.searchType != SearchDetails.SearchType.KEYWORD && tab.searchType != SearchDetails.SearchType.WHATS_NEW) {
            return browseEmptyStateBody(tab, status)
        }
    }
    return when (tab.searchType) {
        SearchDetails.SearchType.KEYWORD -> "Results will appear here as they arrive."
        SearchDetails.SearchType.WHATS_NEW -> "Looking for the latest results in this category."
        SearchDetails.SearchType.SINGLE_BROWSE -> "This friend's shared files will appear here when the browse responds."
        SearchDetails.SearchType.MULTIPLE_BROWSE -> "Shared libraries will appear here as sources respond."
        SearchDetails.SearchType.ALL_FRIENDS_BROWSE -> "Friends' shared libraries will appear here as they respond."
    }
}

private fun browseEmptyStateBody(tab: SearchTabSession, status: BrowseStatusPresentation): String {
    return when (status.state) {
        BrowseState.LOADING -> if (tab.browseRefreshing) {
            "This browse is refreshing. Updated results will appear when it finishes."
        } else {
            "Waiting for shared files to respond."
        }
        BrowseState.UPDATED -> "Refresh to load the latest shared files."
        BrowseState.PARTIAL_FAIL -> "Some sources responded, but others failed. Retry the browse or review the failed sources above."
        BrowseState.UPDATED_PARTIAL_FAIL -> "New results are ready, but one or more sources failed during the last browse."
        BrowseState.FAILED -> "This browse could not be completed. Retry it or review the failed sources above."
        BrowseState.OFFLINE -> "This source is offline right now."
        BrowseState.NO_FRIENDS_SHARING -> "No shared files are currently available for this browse tab."
        BrowseState.LOADED -> "Browse complete."
    }
}

private fun libraryEmptyStateTitle(hasFilters: Boolean, activeSection: LibrarySection?): String {
    return when {
        hasFilters -> "No files match the current view"
        activeSection?.isShared == true -> "No files in this collection"
        else -> "No files in this view"
    }
}

private fun libraryEmptyStateBody(hasFilters: Boolean, activeSection: LibrarySection?): String {
    return when {
        hasFilters -> "Clear the text or category filter to widen this section."
        activeSection?.isShared == true -> "Add files from My Files or import files into this collection."
        else -> "Add files to My Files or change the category filter to see more."
    }
}

private fun connectionIdentity(item: ConnectionItem): String {
    return "${item.hostName}:${item.port}:${item.time}"
}

private fun connectionColumnLabel(column: ConnectionColumn): String {
    return when (column) {
        ConnectionColumn.HOST -> "Host"
        ConnectionColumn.STATUS -> "Status"
        ConnectionColumn.MESSAGES_IO -> "Messages (I/O)"
        ConnectionColumn.MESSAGES_IN -> "Messages In"
        ConnectionColumn.MESSAGES_OUT -> "Messages Out"
        ConnectionColumn.BANDWIDTH_IO -> "Bandwidth (I/O)"
        ConnectionColumn.BANDWIDTH_IN -> "Bandwidth In"
        ConnectionColumn.BANDWIDTH_OUT -> "Bandwidth Out"
        ConnectionColumn.DROPPED_IO -> "Dropped (I/O)"
        ConnectionColumn.DROPPED_IN -> "Dropped In"
        ConnectionColumn.DROPPED_OUT -> "Dropped Out"
        ConnectionColumn.PROTOCOL -> "Protocol"
        ConnectionColumn.VENDOR_VERSION -> "Vendor/Version"
        ConnectionColumn.TIME -> "Time"
        ConnectionColumn.COMPRESSED_IO -> "Compressed (I/O)"
        ConnectionColumn.COMPRESSED_IN -> "Compressed In"
        ConnectionColumn.COMPRESSED_OUT -> "Compressed Out"
        ConnectionColumn.SSL_OVERHEAD_IO -> "SSL Overhead (I/O)"
        ConnectionColumn.SSL_OVERHEAD_IN -> "SSL Overhead In"
        ConnectionColumn.SSL_OVERHEAD_OUT -> "SSL Overhead Out"
        ConnectionColumn.QRP_PERCENT -> "QRP (%)"
        ConnectionColumn.QRP_EMPTY -> "QRP Empty"
    }
}

private fun connectionColumnWidth(column: ConnectionColumn) = when (column) {
    ConnectionColumn.HOST -> 240.dp
    ConnectionColumn.STATUS -> 120.dp
    ConnectionColumn.MESSAGES_IO,
    ConnectionColumn.MESSAGES_IN,
    ConnectionColumn.MESSAGES_OUT -> 128.dp
    ConnectionColumn.BANDWIDTH_IO,
    ConnectionColumn.BANDWIDTH_IN,
    ConnectionColumn.BANDWIDTH_OUT -> 140.dp
    ConnectionColumn.DROPPED_IO,
    ConnectionColumn.DROPPED_IN,
    ConnectionColumn.DROPPED_OUT,
    ConnectionColumn.QRP_PERCENT,
    ConnectionColumn.QRP_EMPTY -> 118.dp
    ConnectionColumn.PROTOCOL -> 120.dp
    ConnectionColumn.VENDOR_VERSION -> 180.dp
    ConnectionColumn.TIME -> 90.dp
    ConnectionColumn.COMPRESSED_IO,
    ConnectionColumn.COMPRESSED_IN,
    ConnectionColumn.COMPRESSED_OUT,
    ConnectionColumn.SSL_OVERHEAD_IO,
    ConnectionColumn.SSL_OVERHEAD_IN,
    ConnectionColumn.SSL_OVERHEAD_OUT -> 144.dp
}

private fun connectionColumnValue(item: ConnectionItem, column: ConnectionColumn): String {
    return when (column) {
        ConnectionColumn.HOST -> "${item.hostName}:${item.port}"
        ConnectionColumn.STATUS -> when (item.status) {
            ConnectionItem.Status.CONNECTING -> "Connecting..."
            ConnectionItem.Status.OUTGOING -> "Outgoing"
            ConnectionItem.Status.INCOMING -> "Incoming"
        }
        ConnectionColumn.MESSAGES_IO -> "${item.numMessagesReceived} / ${item.numMessagesSent}"
        ConnectionColumn.MESSAGES_IN -> item.numMessagesReceived.toString()
        ConnectionColumn.MESSAGES_OUT -> item.numMessagesSent.toString()
        ConnectionColumn.BANDWIDTH_IO -> "${formatRate(item.measuredDownstreamBandwidth)} / ${formatRate(item.measuredUpstreamBandwidth)}"
        ConnectionColumn.BANDWIDTH_IN -> formatRate(item.measuredDownstreamBandwidth)
        ConnectionColumn.BANDWIDTH_OUT -> formatRate(item.measuredUpstreamBandwidth)
        ConnectionColumn.DROPPED_IO -> "${formatConnectionPercent(item.numReceivedMessagesDropped / (item.numMessagesReceived + 1f))} / ${formatConnectionPercent(item.numSentMessagesDropped / (item.numMessagesSent + 1f))}"
        ConnectionColumn.DROPPED_IN -> formatConnectionPercent(item.numReceivedMessagesDropped / (item.numMessagesReceived + 1f))
        ConnectionColumn.DROPPED_OUT -> formatConnectionPercent(item.numSentMessagesDropped / (item.numMessagesSent + 1f))
        ConnectionColumn.PROTOCOL -> connectionProtocolLabel(item)
        ConnectionColumn.VENDOR_VERSION -> item.userAgent.orEmpty().ifBlank { "Unknown" }
        ConnectionColumn.TIME -> formatConnectionAge(item)
        ConnectionColumn.COMPRESSED_IO -> "${formatConnectionPercent(item.readSavedFromCompression)} / ${formatConnectionPercent(item.sentSavedFromCompression)}"
        ConnectionColumn.COMPRESSED_IN -> formatConnectionPercent(item.readSavedFromCompression)
        ConnectionColumn.COMPRESSED_OUT -> formatConnectionPercent(item.sentSavedFromCompression)
        ConnectionColumn.SSL_OVERHEAD_IO -> "${formatConnectionPercent(item.readLostFromSSL)} / ${formatConnectionPercent(item.sentLostFromSSL)}"
        ConnectionColumn.SSL_OVERHEAD_IN -> formatConnectionPercent(item.readLostFromSSL)
        ConnectionColumn.SSL_OVERHEAD_OUT -> formatConnectionPercent(item.sentLostFromSSL)
        ConnectionColumn.QRP_PERCENT -> "${(item.queryRouteTablePercentFull * 100).roundToInt()}%"
        ConnectionColumn.QRP_EMPTY -> item.queryRouteTableEmptyUnits.toString()
    }
}

private fun connectionProtocolLabel(item: ConnectionItem): String {
    return when {
        item.isLeaf -> "Leaf"
        item.isUltrapeer -> "Ultrapeer"
        item.isPeer -> "Peer"
        else -> "Standard"
    }
}

private fun formatConnectionPercent(value: Float): String {
    return "${(value * 100f).coerceAtLeast(0f).roundToInt()}%"
}

private fun formatConnectionAge(item: ConnectionItem): String {
    return formatDuration(((System.currentTimeMillis() - item.time) / 1000L).coerceAtLeast(0L))
}

private fun formatBytes(value: Long): String {
    if (value <= 0L) {
        return "0 B"
    }

    val units = listOf("B", "KB", "MB", "GB", "TB")
    var bytes = value.toDouble()
    var unitIndex = 0
    while (bytes >= 1024 && unitIndex < units.lastIndex) {
        bytes /= 1024.0
        unitIndex += 1
    }

    val format = NumberFormat.getNumberInstance()
    format.maximumFractionDigits = if (unitIndex == 0) 0 else 1
    format.minimumFractionDigits = 0
    return "${format.format(bytes)} ${units[unitIndex]}"
}

private fun formatRate(value: Float): String {
    if (value <= 0f) {
        return "0 KB/s"
    }
    val format = NumberFormat.getNumberInstance()
    format.maximumFractionDigits = 1
    return "${format.format(value)} KB/s"
}

private fun formatTorrentRate(bytesPerSecond: Float): String {
    return formatRate(bytesPerSecond / 1024f)
}

private fun torrentPieceColor(cell: TorrentPieceCellPresentation): Color {
    return when (cell.state) {
        TorrentPieceCellState.DOWNLOADED -> Color(0xFF2E7D32)
        TorrentPieceCellState.PARTIAL -> Color(0xFF43A047).copy(alpha = 0.35f + 0.65f * cell.intensity.coerceIn(0f, 1f))
        TorrentPieceCellState.AVAILABLE -> Color(0xFFF9A825)
        TorrentPieceCellState.ACTIVE -> Color(0xFF1E88E5)
        TorrentPieceCellState.UNAVAILABLE -> Color(0xFF757575)
    }
}

private fun formatDuration(value: Long): String {
    if (value == Long.MAX_VALUE || value < 0) {
        return "unknown"
    }
    val minutes = value / 60
    val seconds = value % 60
    return if (minutes > 0) "${minutes}m ${seconds}s" else "${seconds}s"
}

private fun formatDate(timestamp: Long): String {
    if (timestamp <= 0L) {
        return "unknown date"
    }
    return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(timestamp))
}

private fun formatDateTimeCompact(timestamp: Long): String {
    if (timestamp <= 0L) {
        return "unknown"
    }
    return SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(Date(timestamp))
}

private fun formatClock(timestamp: Long): String {
    return SimpleDateFormat("HH:mm", Locale.US).format(Date(timestamp))
}

private fun copyTextToClipboard(text: String) {
    Toolkit.getDefaultToolkit().systemClipboard.setContents(StringSelection(text), null)
}
