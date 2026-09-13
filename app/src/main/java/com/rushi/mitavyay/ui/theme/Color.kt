package com.rushi.mitavyay.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// =========================================================================
// Mitavyay Color Palette - Neutral Grey Foundation with Green Accent
// =========================================================================

// -------------------------------------------------------------------------
// Accent Tokens (Green - Used selectively for CTAs, FABs, Income, Highlights)
// -------------------------------------------------------------------------
val AccentGreen = Color(0xFF4CAF50)              // Primary brand green / Income accent
val AccentGreenDark = Color(0xFF388E3C)          // High contrast green for light surfaces (WCAG AA)
val AccentGreenLight = Color(0xFF81C784)         // High contrast pastel green for dark surfaces (WCAG AA)
val AccentGreenContainer = Color(0xFFE8F5E9)     // Subtle green container for light theme highlights
val AccentGreenContainerDark = Color(0xFF1B382B) // Subtle dark green container for dark theme highlights

// -------------------------------------------------------------------------
// Light Theme Neutrals (Off-White & Pure Grey Hierarchy)
// -------------------------------------------------------------------------
val LightBackground = Color(0xFFF5F5F5)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0xFFEEEEEE)
val LightOnSurface = Color(0xFF121212)
val LightOnSurfaceSecondary = Color(0xFF757575)
val LightDivider = Color(0xFFE0E0E0)
val LightOutline = Color(0xFFBDBDBD)

// -------------------------------------------------------------------------
// Dark Theme Neutrals (Deep Charcoal & Pure Grey Hierarchy)
// -------------------------------------------------------------------------
val DarkBackground = Color(0xFF121212)
val DarkSurface = Color(0xFF1E1E1E)
val DarkSurfaceVariant = Color(0xFF2A2A2A)
val DarkSurfaceElevated = Color(0xFF333333)
val DarkOnSurface = Color(0xFFE0E0E0)
val DarkOnSurfaceSecondary = Color(0xFF9E9E9E)
val DarkDivider = Color(0xFF3A3A3A)
val DarkOutline = Color(0xFF555555)

// -------------------------------------------------------------------------
// Semantic Financial Colors
// -------------------------------------------------------------------------
val ExpenseRed = Color(0xFFE53935)
val IncomeGreen = AccentGreen                    // 0xFF4CAF50
val WarningAmber = Color(0xFFFFA000)

// ==========================================
// Primary Brand Colors (Accent Green)
// ==========================================
val PrimaryLight = AccentGreenDark
val OnPrimaryLight = Color(0xFFFFFFFF)
val PrimaryContainerLight = AccentGreenContainer
val OnPrimaryContainerLight = Color(0xFF1B5E20)

val PrimaryDark = AccentGreenLight
val OnPrimaryDark = Color(0xFF003816)
val PrimaryContainerDark = AccentGreenContainerDark
val OnPrimaryContainerDark = Color(0xFFA5D6A7)

// ==========================================
// Secondary Colors (Neutral Slate / Grey)
// ==========================================
val SecondaryLight = Color(0xFF5F6368)
val OnSecondaryLight = Color(0xFFFFFFFF)
val SecondaryContainerLight = LightSurfaceVariant
val OnSecondaryContainerLight = Color(0xFF212121)

val SecondaryDark = Color(0xFFB0B0B0)
val OnSecondaryDark = Color(0xFF1E1E1E)
val SecondaryContainerDark = DarkSurfaceVariant
val OnSecondaryContainerDark = Color(0xFFE0E0E0)

// ==========================================
// Tertiary Colors (Cool Slate for Badges/Charts)
// ==========================================
val TertiaryLight = Color(0xFF455A64)
val OnTertiaryLight = Color(0xFFFFFFFF)
val TertiaryContainerLight = Color(0xFFECEFF1)
val OnTertiaryContainerLight = Color(0xFF1C2B32)

val TertiaryDark = Color(0xFF90A4AE)
val OnTertiaryDark = Color(0xFF101B20)
val TertiaryContainerDark = Color(0xFF263238)
val OnTertiaryContainerDark = Color(0xFFCFD8DC)

// ==========================================
// Background & Surface Colors (Neutral Grey)
// ==========================================
val BackgroundLight = LightBackground
val OnBackgroundLight = LightOnSurface
val SurfaceLight = LightSurface
val OnSurfaceLight = LightOnSurface
val SurfaceVariantLight = LightSurfaceVariant
val OnSurfaceVariantLight = LightOnSurfaceSecondary
val OutlineLight = LightOutline
val OutlineVariantLight = LightDivider

val BackgroundDark = DarkBackground
val OnBackgroundDark = DarkOnSurface
val SurfaceDark = DarkSurface
val OnSurfaceDark = DarkOnSurface
val SurfaceVariantDark = DarkSurfaceVariant
val OnSurfaceVariantDark = DarkOnSurfaceSecondary
val OutlineDark = DarkOutline
val OutlineVariantDark = DarkDivider

// ==========================================
// Status & Semantic Colors (Error, Success, Warning)
// ==========================================
// Error (e.g. Expenses, over-budget)
val ErrorLight = Color(0xFFD32F2F)
val OnErrorLight = Color(0xFFFFFFFF)
val ErrorContainerLight = Color(0xFFFFEBEE)
val OnErrorContainerLight = Color(0xFFC62828)

val ErrorDark = Color(0xFFFF8A80)
val OnErrorDark = Color(0xFF490005)
val ErrorContainerDark = Color(0xFF8C1D18)
val OnErrorContainerDark = Color(0xFFFFCDD2)

// Success (e.g. Income, savings, debt settled)
val SuccessLight = AccentGreenDark
val OnSuccessLight = Color(0xFFFFFFFF)
val SuccessContainerLight = AccentGreenContainer
val OnSuccessContainerLight = Color(0xFF1B5E20)

val SuccessDark = AccentGreenLight
val OnSuccessDark = Color(0xFF003816)
val SuccessContainerDark = AccentGreenContainerDark
val OnSuccessContainerDark = Color(0xFFA5D6A7)

// Warning (e.g. Budget warnings 80%, upcoming dues)
val WarningLight = Color(0xFFE65100)
val OnWarningLight = Color(0xFFFFFFFF)
val WarningContainerLight = Color(0xFFFFF3E0)
val OnWarningContainerLight = Color(0xFFBF360C)

val WarningDark = Color(0xFFFFB74D)
val OnWarningDark = Color(0xFF3E2723)
val WarningContainerDark = Color(0xFF5D2A00)
val OnWarningContainerDark = Color(0xFFFFE0B2)

// ==========================================
// Extended Semantic Color Scheme
// ==========================================
@Immutable
data class ExtendedColorScheme(
    val success: Color,
    val onSuccess: Color,
    val successContainer: Color,
    val onSuccessContainer: Color,
    val warning: Color,
    val onWarning: Color,
    val warningContainer: Color,
    val onWarningContainer: Color
)

val LightExtendedColorScheme = ExtendedColorScheme(
    success = SuccessLight,
    onSuccess = OnSuccessLight,
    successContainer = SuccessContainerLight,
    onSuccessContainer = OnSuccessContainerLight,
    warning = WarningLight,
    onWarning = OnWarningLight,
    warningContainer = WarningContainerLight,
    onWarningContainer = OnWarningContainerLight
)

val DarkExtendedColorScheme = ExtendedColorScheme(
    success = SuccessDark,
    onSuccess = OnSuccessDark,
    successContainer = SuccessContainerDark,
    onSuccessContainer = OnSuccessContainerDark,
    warning = WarningDark,
    onWarning = OnWarningDark,
    warningContainer = WarningContainerDark,
    onWarningContainer = OnWarningContainerDark
)

val LocalExtendedColorScheme = staticCompositionLocalOf { LightExtendedColorScheme }

val MaterialTheme.extendedColorScheme: ExtendedColorScheme
    @Composable
    @ReadOnlyComposable
    get() = LocalExtendedColorScheme.current

// Material 3 Color Schemes
val LightColorScheme: ColorScheme = lightColorScheme(
    primary = PrimaryLight,
    onPrimary = OnPrimaryLight,
    primaryContainer = PrimaryContainerLight,
    onPrimaryContainer = OnPrimaryContainerLight,
    secondary = SecondaryLight,
    onSecondary = OnSecondaryLight,
    secondaryContainer = SecondaryContainerLight,
    onSecondaryContainer = OnSecondaryContainerLight,
    tertiary = TertiaryLight,
    onTertiary = OnTertiaryLight,
    tertiaryContainer = TertiaryContainerLight,
    onTertiaryContainer = OnTertiaryContainerLight,
    background = BackgroundLight,
    onBackground = OnBackgroundLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    outline = OutlineLight,
    outlineVariant = OutlineVariantLight,
    error = ErrorLight,
    onError = OnErrorLight,
    errorContainer = ErrorContainerLight,
    onErrorContainer = OnErrorContainerLight
)

val DarkColorScheme: ColorScheme = darkColorScheme(
    primary = PrimaryDark,
    onPrimary = OnPrimaryDark,
    primaryContainer = PrimaryContainerDark,
    onPrimaryContainer = OnPrimaryContainerDark,
    secondary = SecondaryDark,
    onSecondary = OnSecondaryDark,
    secondaryContainer = SecondaryContainerDark,
    onSecondaryContainer = OnSecondaryContainerDark,
    tertiary = TertiaryDark,
    onTertiary = OnTertiaryDark,
    tertiaryContainer = TertiaryContainerDark,
    onTertiaryContainer = OnTertiaryContainerDark,
    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    outline = OutlineDark,
    outlineVariant = OutlineVariantDark,
    error = ErrorDark,
    onError = OnErrorDark,
    errorContainer = ErrorContainerDark,
    onErrorContainer = OnErrorContainerDark
)
