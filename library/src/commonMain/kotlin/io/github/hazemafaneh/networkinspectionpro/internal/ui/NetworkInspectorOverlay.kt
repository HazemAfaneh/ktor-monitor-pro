package io.github.hazemafaneh.networkinspectionpro.internal.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import io.github.hazemafaneh.networkinspectionpro.NetworkInspectionPro
import io.github.hazemafaneh.networkinspectionpro.internal.shake.ShakeDetector
import io.github.hazemafaneh.networkinspectionpro.internal.viewmodel.NetworkInspectorViewModel

@Composable
fun NetworkInspectorOverlay(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    if (!NetworkInspectionPro.isEnabled) {
        content()
        return
    }

    var isVisible by remember { mutableStateOf(false) }
    val viewModel = remember { NetworkInspectorViewModel() }

    val shakeDetector = remember {
        ShakeDetector(onShake = { isVisible = !isVisible })
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
        AnimatedVisibility(
            visible = isVisible,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it })
        ) {
            val selectedEntryId by viewModel.selectedEntryId.collectAsState()
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
                        onBack = { isVisible = false }
                    )
                }
            }
        }
    }
}
