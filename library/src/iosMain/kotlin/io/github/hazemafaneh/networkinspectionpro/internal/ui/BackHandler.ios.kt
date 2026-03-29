package io.github.hazemafaneh.networkinspectionpro.internal.ui

import androidx.compose.runtime.Composable

@Composable
internal actual fun BackHandler(enabled: Boolean, onBack: () -> Unit) {
    // iOS back navigation is handled by the host navigation controller (swipe-from-edge)
}
