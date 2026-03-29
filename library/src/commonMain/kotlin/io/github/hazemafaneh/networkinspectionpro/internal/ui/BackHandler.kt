package io.github.hazemafaneh.networkinspectionpro.internal.ui

import androidx.compose.runtime.Composable

@Composable
internal expect fun BackHandler(enabled: Boolean = true, onBack: () -> Unit)
