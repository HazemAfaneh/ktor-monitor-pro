package io.github.hazemafaneh.networkinspectionpro.internal.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import io.github.hazemafaneh.networkinspectionpro.NetworkInspectionPro
import io.github.hazemafaneh.networkinspectionpro.internal.shake.ShakeDetector
import io.github.hazemafaneh.networkinspectionpro.internal.viewmodel.NetworkInspectorViewModel
import kotlin.math.roundToInt

@Composable
fun NetworkInspectorOverlay(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    if (!NetworkInspectionPro.isEnabled) {
        content()
        return
    }

    var isBubbleVisible by remember { mutableStateOf(false) }
    var isInspectorVisible by remember { mutableStateOf(false) }
    val viewModel = remember { NetworkInspectorViewModel() }

    var bubbleX by remember { mutableStateOf(40f) }
    var bubbleY by remember { mutableStateOf(200f) }

    val shakeDetector = remember {
        ShakeDetector(onShake = { isBubbleVisible = true })
    }

    DisposableEffect(Unit) {
        shakeDetector.start()
        onDispose {
            shakeDetector.stop()
            viewModel.onDestroy()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        content()

        if (isBubbleVisible) {
            FloatingInspectorBubble(
                x = bubbleX,
                y = bubbleY,
                onDrag = { dx, dy ->
                    bubbleX += dx
                    bubbleY += dy
                },
                onTap = { isInspectorVisible = true },
                onClose = { isBubbleVisible = false }
            )
        }

        AnimatedVisibility(
            visible = isInspectorVisible,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it })
        ) {
            val selectedEntryId by viewModel.selectedEntryId.collectAsState()
            BackHandler(enabled = isInspectorVisible) {
                if (selectedEntryId != null) {
                    viewModel.onEntryIdSelected(null)
                } else {
                    isInspectorVisible = false
                }
            }
            Box(modifier = Modifier.fillMaxSize()) {
                if (selectedEntryId != null) {
                    NetworkInspectorDetailScreen(
                        logEntryId = selectedEntryId!!,
                        onBack = { viewModel.onEntryIdSelected(null) }
                    )
                } else {
                    NetworkInspectorListScreen(
                        viewModel = viewModel,
                        onNavigateToDetail = { viewModel.onEntryIdSelected(it) },
                        onBack = { isInspectorVisible = false }
                    )
                }
            }
        }
    }
}

@Composable
private fun FloatingInspectorBubble(
    x: Float,
    y: Float,
    onDrag: (dx: Float, dy: Float) -> Unit,
    onTap: () -> Unit,
    onClose: () -> Unit
) {
    Box(
        modifier = Modifier.offset { IntOffset(x.roundToInt(), y.roundToInt()) }
    ) {
        Surface(
            shape = CircleShape,
            color = Color(0xFF6200EE),
            shadowElevation = 6.dp,
            modifier = Modifier
                .size(56.dp)
                .pointerInput(Unit) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        down.consume()
                        var isDragging = false
                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull { it.id == down.id } ?: break
                            if (!change.pressed) {
                                if (!isDragging) onTap()
                                break
                            }
                            val delta = change.positionChange()
                            if (isDragging || delta.getDistance() > viewConfiguration.touchSlop) {
                                isDragging = true
                                change.consume()
                                onDrag(delta.x, delta.y)
                            }
                        }
                    }
                }
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.NetworkCheck,
                    contentDescription = "Open Network Inspector",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        Box(
            modifier = Modifier
                .size(20.dp)
                .align(Alignment.TopEnd)
                .background(Color(0xFFE53935), CircleShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClose
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                tint = Color.White,
                modifier = Modifier.size(12.dp)
            )
        }
    }
}
