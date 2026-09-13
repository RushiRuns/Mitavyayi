package com.rushi.mitavyay.ui.theme

import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.dp

// ==============================================================================
// App Shape Tokens
// none (0dp), small (4dp), medium (8dp), large (12dp), full (9999dp)
// ==============================================================================

@Immutable
data class AppShapes(
    val none: CornerBasedShape = RoundedCornerShape(0.dp),
    val small: CornerBasedShape = RoundedCornerShape(4.dp),
    val medium: CornerBasedShape = RoundedCornerShape(8.dp),
    val large: CornerBasedShape = RoundedCornerShape(12.dp),
    val full: CornerBasedShape = RoundedCornerShape(9999.dp)
)

val LocalAppShapes = staticCompositionLocalOf { AppShapes() }

val MaterialTheme.appShapes: AppShapes
    @Composable
    @ReadOnlyComposable
    get() = LocalAppShapes.current

val MitavyayShapes = Shapes(
    extraSmall = RoundedCornerShape(0.dp),
    small = RoundedCornerShape(4.dp),
    medium = RoundedCornerShape(8.dp),
    large = RoundedCornerShape(12.dp),
    extraLarge = RoundedCornerShape(16.dp)
)
