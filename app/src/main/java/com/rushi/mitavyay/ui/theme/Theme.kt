package com.rushi.mitavyay.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

/**
 * Root theme composable for Mitavyay.
 *
 * Supports:
 * - Dual Theme: Light and Dark mode with WCAG AA compliance.
 * - Dynamic Font Scaling: Scales all typography proportionally via [fontScale].
 * - ThemeMode selection: Accepts explicit [darkTheme] boolean or [ThemeMode] enum.
 * - Extended Semantic Colors: Provides success and warning color tokens via [MaterialTheme.extendedColorScheme].
 * - Standardized Spacing and Shapes: Bound to [MaterialTheme.spacing] and [MaterialTheme.appShapes].
 */
@Composable
fun MitavyayTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    fontScale: Float = 1.0f,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val extendedColorScheme = if (darkTheme) DarkExtendedColorScheme else LightExtendedColorScheme
    val typography = getTypography(fontScale)
    val appTypography = getAppTypography(fontScale)
    val spacing = Spacing()
    val appShapes = AppShapes()
    val appMotion = AppMotion()

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.surface.toArgb()
                window.navigationBarColor = colorScheme.surface.toArgb()
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = !darkTheme
                insetsController.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(
        LocalExtendedColorScheme provides extendedColorScheme,
        LocalSpacing provides spacing,
        LocalAppShapes provides appShapes,
        LocalAppTypography provides appTypography,
        LocalAppMotion provides appMotion
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = typography,
            shapes = MitavyayShapes,
            content = content
        )
    }
}

/**
 * Convenience overload accepting [ThemeMode] (e.g., loaded from DataStore preferences).
 */
@Composable
fun MitavyayTheme(
    themeMode: ThemeMode,
    fontScale: Float = 1.0f,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    MitavyayTheme(
        darkTheme = darkTheme,
        fontScale = fontScale,
        content = content
    )
}
