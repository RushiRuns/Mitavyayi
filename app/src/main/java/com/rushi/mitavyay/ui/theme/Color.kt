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

// ==========================================
// Primary Brand Colors (Emerald / Forest)
// ==========================================
val PrimaryLight = Color(0xFF006C4C)
val OnPrimaryLight = Color(0xFFFFFFFF)
val PrimaryContainerLight = Color(0xFF89F8C7)
val OnPrimaryContainerLight = Color(0xFF002114)

val PrimaryDark = Color(0xFF6CDBAC)
val OnPrimaryDark = Color(0xFF003825)
val PrimaryContainerDark = Color(0xFF005138)
val OnPrimaryContainerDark = Color(0xFF89F8C7)

// ==========================================
// Secondary Colors (Slate / Sage)
// ==========================================
val SecondaryLight = Color(0xFF4C6357)
val OnSecondaryLight = Color(0xFFFFFFFF)
val SecondaryContainerLight = Color(0xFFCEE9D9)
val OnSecondaryContainerLight = Color(0xFF092016)

val SecondaryDark = Color(0xFFB3CCBE)
val OnSecondaryDark = Color(0xFF1F352A)
val SecondaryContainerDark = Color(0xFF354B40)
val OnSecondaryContainerDark = Color(0xFFCEE9D9)

// ==========================================
// Tertiary Colors (Ocean / Steel Teal)
// ==========================================
val TertiaryLight = Color(0xFF3D6373)
val OnTertiaryLight = Color(0xFFFFFFFF)
val TertiaryContainerLight = Color(0xFFC1E8FB)
val OnTertiaryContainerLight = Color(0xFF001F29)

val TertiaryDark = Color(0xFFA5CDE0)
val OnTertiaryDark = Color(0xFF073543)
val TertiaryContainerDark = Color(0xFF244B5B)
val OnTertiaryContainerDark = Color(0xFFC1E8FB)

// ==========================================
// Background & Surface Colors (Neutral)
// ==========================================
val BackgroundLight = Color(0xFFFBFDFA)
val OnBackgroundLight = Color(0xFF191C1A)
val SurfaceLight = Color(0xFFFBFDFA)
val OnSurfaceLight = Color(0xFF191C1A)
val SurfaceVariantLight = Color(0xFFDBE5DE)
val OnSurfaceVariantLight = Color(0xFF404944)
val OutlineLight = Color(0xFF707974)
val OutlineVariantLight = Color(0xFFBFC9C2)

val BackgroundDark = Color(0xFF101412)
val OnBackgroundDark = Color(0xFFE1E3DF)
val SurfaceDark = Color(0xFF101412)
val OnSurfaceDark = Color(0xFFE1E3DF)
val SurfaceVariantDark = Color(0xFF404944)
val OnSurfaceVariantDark = Color(0xFFBFC9C2)
val OutlineDark = Color(0xFF8A938D)
val OutlineVariantDark = Color(0xFF404944)

// ==========================================
// Status & Semantic Colors (Error, Success, Warning)
// ==========================================
// Error (e.g. Expenses, over-budget)
val ErrorLight = Color(0xFFBA1A1A)
val OnErrorLight = Color(0xFFFFFFFF)
val ErrorContainerLight = Color(0xFFFFDAD6)
val OnErrorContainerLight = Color(0xFF410002)

val ErrorDark = Color(0xFFFFB4AB)
val OnErrorDark = Color(0xFF690005)
val ErrorContainerDark = Color(0xFF93000A)
val OnErrorContainerDark = Color(0xFFFFDAD6)

// Success (e.g. Income, savings, debt settled)
val SuccessLight = Color(0xFF1B6C31)
val OnSuccessLight = Color(0xFFFFFFFF)
val SuccessContainerLight = Color(0xFFA6F5AB)
val OnSuccessContainerLight = Color(0xFF002107)

val SuccessDark = Color(0xFF8BD891)
val OnSuccessDark = Color(0xFF003913)
val SuccessContainerDark = Color(0xFF00531E)
val OnSuccessContainerDark = Color(0xFFA6F5AB)

// Warning (e.g. Budget warnings 80%, upcoming dues)
val WarningLight = Color(0xFF7B5800)
val OnWarningLight = Color(0xFFFFFFFF)
val WarningContainerLight = Color(0xFFFFDEA3)
val OnWarningContainerLight = Color(0xFF261900)

val WarningDark = Color(0xFFF6BD39)
val OnWarningDark = Color(0xFF412D00)
val WarningContainerDark = Color(0xFF5D4200)
val OnWarningContainerDark = Color(0xFFFFDEA3)

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
