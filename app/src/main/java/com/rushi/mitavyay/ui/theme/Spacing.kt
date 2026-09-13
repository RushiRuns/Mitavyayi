package com.rushi.mitavyay.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// ==============================================================================
// Spacing scale: xs (4dp), sm (8dp), md (16dp), lg (24dp), xl (32dp)
// Unchanged from before - layout spacing doesn't need to differ by theme.
// ==============================================================================

@Immutable
data class Spacing(
    val none: Dp = 0.dp,
    val xs: Dp = 4.dp,
    val sm: Dp = 8.dp,
    val md: Dp = 16.dp,
    val lg: Dp = 24.dp,
    val xl: Dp = 32.dp,
    val xxl: Dp = 48.dp
) {
    // Computed values for consistent component layouts
    val screenHorizontal: Dp get() = md
    val screenVertical: Dp get() = md
    val cardContent: Dp get() = md
    val itemSpacing: Dp get() = sm
    val sectionSpacing: Dp get() = lg
    val iconSpacing: Dp get() = sm
}

val LocalSpacing = staticCompositionLocalOf { Spacing() }

val MaterialTheme.spacing: Spacing
    @Composable
    @ReadOnlyComposable
    get() = LocalSpacing.current