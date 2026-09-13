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
// Mitavyay Color Palette v2
// Dark theme  -> cool graphite scale (slight blue undertone), more elevation
//                steps so surfaces actually separate from the background.
// Light theme -> warm greige scale (not stark white-on-white).
// Green stays the accent in both, tuned per-theme for real AA contrast.
// =========================================================================

// -------------------------------------------------------------------------
// Green tonal ramp (single source of truth for every green used below)
// -------------------------------------------------------------------------
val Green50 = Color(0xFFE8F5E9)
val Green100 = Color(0xFFC8E6C9)
val Green200 = Color(0xFFA5D6A7)
val Green300 = Color(0xFF81C784)
val Green400 = Color(0xFF66BB6A)
val Green500 = Color(0xFF4CAF50)
val Green600 = Color(0xFF43A047)
val Green700 = Color(0xFF388E3C)
val Green800 = Color(0xFF2E7D32)
val Green900 = Color(0xFF1B5E20)

// -------------------------------------------------------------------------
// Accent Tokens (Used for CTAs, FABs, Income, Highlights)
// -------------------------------------------------------------------------
val AccentGreen = Green500                        // Base brand green (icons, decorative)
val AccentGreenDark = Green800                    // 0xFF2E7D32 - text/icon on light surfaces, ~5:1 on white
val AccentGreenLight = Green300                   // 0xFF81C784 - text/icon on dark surfaces, ~10:1 on graphite bg
val AccentGreenContainer = Color(0xFFE3F2E4)      // Warm-tinted green container for light theme
val AccentGreenContainerDark = Color(0xFF1D3324)  // Green blended into the graphite scale for dark theme

// -------------------------------------------------------------------------
// Light Theme Neutrals (warm greige, not flat white/grey)
// -------------------------------------------------------------------------
val LightBackground = Color(0xFFF7F7F4)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0xFFEFEFEA)
val LightSurfaceElevated = Color(0xFFFBFBF9)      // for a card sitting on top of another card
val LightOnSurface = Color(0xFF1B1D1B)
val LightOnSurfaceSecondary = Color(0xFF6C6F6A)
val LightDivider = Color(0xFFE5E5DF)
val LightOutline = Color(0xFFC9CAC3)

// -------------------------------------------------------------------------
// Dark Theme Neutrals (cool graphite, slight blue undertone, 4 elevation tiers)
// -------------------------------------------------------------------------
val DarkBackground = Color(0xFF0F1113)
val DarkSurface = Color(0xFF161A1D)
val DarkSurfaceVariant = Color(0xFF1E2226)
val DarkSurfaceElevated = Color(0xFF262B30)       // cards
val DarkSurfaceElevated2 = Color(0xFF31373D)      // dialogs, bottom sheets, card-on-card
val DarkOnSurface = Color(0xFFECEDEE)
val DarkOnSurfaceSecondary = Color(0xFFA6ADB4)
val DarkDivider = Color(0xFF2E3338)
val DarkOutline = Color(0xFF4A5158)

// -------------------------------------------------------------------------
// Semantic Financial Colors (flat swatches, same in both themes - used for
// category chips/icons rather than text-on-background, so vividness > contrast)
// -------------------------------------------------------------------------
val ExpenseRed = Color(0xFFE0483D)
val IncomeGreen = AccentGreen
val WarningAmber = Color(0xFFF2A93C)

// ==========================================
// Primary Brand Colors (Accent Green)
// ==========================================
val PrimaryLight = AccentGreenDark
val OnPrimaryLight = Color(0xFFFFFFFF)
val PrimaryContainerLight = AccentGreenContainer
val OnPrimaryContainerLight = Color(0xFF163519)

val PrimaryDark = AccentGreenLight
val OnPrimaryDark = Color(0xFF0B2B12)
val PrimaryContainerDark = AccentGreenContainerDark
val OnPrimaryContainerDark = Color(0xFFA8D9AC)

// ==========================================
// Secondary Colors (Neutral, matched to each theme's undertone)
// ==========================================
val SecondaryLight = Color(0xFF5F6359)            // warm slate, matches greige bg
val OnSecondaryLight = Color(0xFFFFFFFF)
val SecondaryContainerLight = LightSurfaceVariant
val OnSecondaryContainerLight = Color(0xFF1F211D)

val SecondaryDark = Color(0xFFA9B0B6)             // cool slate, matches graphite bg
val OnSecondaryDark = Color(0xFF1E2226)
val SecondaryContainerDark = DarkSurfaceVariant
val OnSecondaryContainerDark = Color(0xFFDDE2E6)

// ==========================================
// Tertiary Colors (blue-slate, for badges/charts - keeps a 2nd hue in the system)
// ==========================================
val TertiaryLight = Color(0xFF45607A)
val OnTertiaryLight = Color(0xFFFFFFFF)
val TertiaryContainerLight = Color(0xFFE1E9F0)
val OnTertiaryContainerLight = Color(0xFF16283A)

val TertiaryDark = Color(0xFF9FC1DE)
val OnTertiaryDark = Color(0xFF0C2436)
val TertiaryContainerDark = Color(0xFF223B4E)
val OnTertiaryContainerDark = Color(0xFFC6E1F2)

// ==========================================
// Background & Surface Colors
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
// Error (e.g. Expenses, over-budget) - warm terracotta-red, not the stock Material red
val ErrorLight = Color(0xFFC4453A)
val OnErrorLight = Color(0xFFFFFFFF)
val ErrorContainerLight = Color(0xFFFBE6E3)
val OnErrorContainerLight = Color(0xFF5C160E)

val ErrorDark = Color(0xFFE68A80)
val OnErrorDark = Color(0xFF3A0D08)
val ErrorContainerDark = Color(0xFF5C2620)
val OnErrorContainerDark = Color(0xFFF7CFC9)

// Success (e.g. Income, savings, debt settled) - reuses the primary green
val SuccessLight = AccentGreenDark
val OnSuccessLight = Color(0xFFFFFFFF)
val SuccessContainerLight = AccentGreenContainer
val OnSuccessContainerLight = Color(0xFF163519)

val SuccessDark = AccentGreenLight
val OnSuccessDark = Color(0xFF0B2B12)
val SuccessContainerDark = AccentGreenContainerDark
val OnSuccessContainerDark = Color(0xFFA8D9AC)

// Warning (e.g. Budget warnings 80%, upcoming dues) - warm amber, not orange-red
val WarningLight = Color(0xFFA3650C)
val OnWarningLight = Color(0xFFFFFFFF)
val WarningContainerLight = Color(0xFFFBEBD2)
val OnWarningContainerLight = Color(0xFF452C00)

val WarningDark = Color(0xFFF0B860)
val OnWarningDark = Color(0xFF3D2600)
val WarningContainerDark = Color(0xFF5B3E10)
val OnWarningContainerDark = Color(0xFFFBDCA8)

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