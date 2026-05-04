package org.limewire.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times

@Composable
fun HorizontalSplitPane(
    fraction: Float,
    onFractionChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    minStartWidth: Dp = 240.dp,
    minEndWidth: Dp = 280.dp,
    dividerThickness: Dp = 12.dp,
    onFractionChangeFinished: (Float) -> Unit = {},
    start: @Composable () -> Unit,
    end: @Composable () -> Unit
) {
    BoxWithConstraints(modifier = modifier) {
        val density = LocalDensity.current
        val totalWidth = maxWidth
        val availableWidth = (totalWidth - dividerThickness).coerceAtLeast(0.dp)
        val availableWidthPx = with(density) { availableWidth.toPx() }
        val clampedFraction = clampPaneFraction(
            fraction = fraction,
            totalSize = availableWidth,
            minFirst = minStartWidth,
            minSecond = minEndWidth
        )
        val startWidth = availableWidth * clampedFraction
        val dividerColor = MaterialTheme.colorScheme.outlineVariant
        val dividerHandleColor = MaterialTheme.colorScheme.outline
        val dragState = rememberDraggableState { delta ->
            if (availableWidthPx <= 0f) {
                return@rememberDraggableState
            }
            val deltaFraction = delta / availableWidthPx
            val next = clampPaneFraction(
                fraction = fraction + deltaFraction,
                totalSize = availableWidth,
                minFirst = minStartWidth,
                minSecond = minEndWidth
            )
            onFractionChange(next)
        }

        Row(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.width(startWidth).fillMaxHeight()) {
                start()
            }
            SplitPaneDivider(
                modifier = Modifier
                    .width(dividerThickness)
                    .fillMaxHeight()
                    .draggable(
                        state = dragState,
                        orientation = Orientation.Horizontal,
                        onDragStopped = { onFractionChangeFinished(clampedFraction) }
                    ),
                dividerColor = dividerColor,
                handleColor = dividerHandleColor,
                horizontal = true
            )
            Box(modifier = Modifier.fillMaxSize()) {
                end()
            }
        }
    }
}

@Composable
fun VerticalSplitPane(
    fraction: Float,
    onFractionChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    minTopHeight: Dp = 260.dp,
    minBottomHeight: Dp = 200.dp,
    dividerThickness: Dp = 12.dp,
    onFractionChangeFinished: (Float) -> Unit = {},
    top: @Composable () -> Unit,
    bottom: @Composable () -> Unit
) {
    BoxWithConstraints(modifier = modifier) {
        val density = LocalDensity.current
        val totalHeight = maxHeight
        val availableHeight = (totalHeight - dividerThickness).coerceAtLeast(0.dp)
        val availableHeightPx = with(density) { availableHeight.toPx() }
        val clampedFraction = clampPaneFraction(
            fraction = fraction,
            totalSize = availableHeight,
            minFirst = minTopHeight,
            minSecond = minBottomHeight
        )
        val topHeight = availableHeight * clampedFraction
        val dividerColor = MaterialTheme.colorScheme.outlineVariant
        val dividerHandleColor = MaterialTheme.colorScheme.outline
        val dragState = rememberDraggableState { delta ->
            if (availableHeightPx <= 0f) {
                return@rememberDraggableState
            }
            val deltaFraction = delta / availableHeightPx
            val next = clampPaneFraction(
                fraction = fraction + deltaFraction,
                totalSize = availableHeight,
                minFirst = minTopHeight,
                minSecond = minBottomHeight
            )
            onFractionChange(next)
        }

        Column(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.height(topHeight).fillMaxWidth()) {
                top()
            }
            SplitPaneDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(dividerThickness)
                    .draggable(
                        state = dragState,
                        orientation = Orientation.Vertical,
                        onDragStopped = { onFractionChangeFinished(clampedFraction) }
                    ),
                dividerColor = dividerColor,
                handleColor = dividerHandleColor,
                horizontal = false
            )
            Box(modifier = Modifier.fillMaxSize()) {
                bottom()
            }
        }
    }
}

@Composable
private fun SplitPaneDivider(
    modifier: Modifier,
    dividerColor: Color,
    handleColor: Color,
    horizontal: Boolean
) {
    Box(
        modifier = modifier
            .padding(horizontal = if (horizontal) 2.dp else 0.dp, vertical = if (horizontal) 0.dp else 2.dp)
            .background(dividerColor, RoundedCornerShape(999.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (horizontal) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                repeat(3) {
                    Box(
                        modifier = Modifier
                            .size(width = 2.dp, height = 18.dp)
                            .background(handleColor, RoundedCornerShape(999.dp))
                    )
                }
            }
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                repeat(3) {
                    Box(
                        modifier = Modifier
                            .size(width = 18.dp, height = 2.dp)
                            .background(handleColor, RoundedCornerShape(999.dp))
                    )
                }
            }
        }
    }
}

private fun clampPaneFraction(
    fraction: Float,
    totalSize: Dp,
    minFirst: Dp,
    minSecond: Dp
): Float {
    if (totalSize <= 0.dp) {
        return fraction.coerceIn(0.1f, 0.9f)
    }
    val min = (minFirst.value / totalSize.value).coerceIn(0f, 1f)
    val max = (1f - (minSecond.value / totalSize.value)).coerceIn(min, 1f)
    return fraction.coerceIn(min, max)
}
