package com.rushi.mitavyay.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ==============================================================================
// App Typography Tokens (xs: 12sp, sm: 13sp, body: 15sp, lg: 18sp, xl: 20sp, title: 24sp)
// Supports global font scaling dynamically via fontScale parameter
// ==============================================================================

@Immutable
data class AppTypography(
    val xs: TextStyle,
    val sm: TextStyle,
    val body: TextStyle,
    val lg: TextStyle,
    val xl: TextStyle,
    val title: TextStyle
)

val LocalAppTypography = staticCompositionLocalOf { getAppTypography() }

val MaterialTheme.appTypography: AppTypography
    @Composable
    @ReadOnlyComposable
    get() = LocalAppTypography.current

fun getAppTypography(
    fontScale: Float = 1.0f,
    fontFamily: FontFamily = FontFamily.Default
): AppTypography {
    val xsSize = (12 * fontScale).sp
    val smSize = (13 * fontScale).sp
    val bodySize = (15 * fontScale).sp
    val lgSize = (18 * fontScale).sp
    val xlSize = (20 * fontScale).sp
    val titleSize = (24 * fontScale).sp

    return AppTypography(
        xs = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = xsSize,
            lineHeight = (16 * fontScale).sp,
            letterSpacing = 0.4.sp
        ),
        sm = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = smSize,
            lineHeight = (18 * fontScale).sp,
            letterSpacing = 0.25.sp
        ),
        body = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = bodySize,
            lineHeight = (22 * fontScale).sp,
            letterSpacing = 0.15.sp
        ),
        lg = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = lgSize,
            lineHeight = (24 * fontScale).sp,
            letterSpacing = 0.1.sp
        ),
        xl = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = xlSize,
            lineHeight = (26 * fontScale).sp,
            letterSpacing = 0.sp
        ),
        title = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = titleSize,
            lineHeight = (30 * fontScale).sp,
            letterSpacing = 0.sp
        )
    )
}

fun getTypography(
    fontScale: Float = 1.0f,
    fontFamily: FontFamily = FontFamily.Default
): Typography {
    val tokens = getAppTypography(fontScale, fontFamily)

    return Typography(
        displayLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = (32 * fontScale).sp,
            lineHeight = (40 * fontScale).sp,
            letterSpacing = 0.sp
        ),
        displayMedium = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = (28 * fontScale).sp,
            lineHeight = (36 * fontScale).sp,
            letterSpacing = 0.sp
        ),
        displaySmall = tokens.title,
        headlineLarge = tokens.title.copy(fontWeight = FontWeight.SemiBold),
        headlineMedium = tokens.xl,
        headlineSmall = tokens.lg,
        titleLarge = tokens.xl.copy(fontWeight = FontWeight.Medium),
        titleMedium = tokens.lg.copy(fontWeight = FontWeight.Medium),
        titleSmall = tokens.body.copy(fontWeight = FontWeight.SemiBold),
        bodyLarge = tokens.lg.copy(fontWeight = FontWeight.Normal),
        bodyMedium = tokens.body,
        bodySmall = tokens.sm,
        labelLarge = tokens.body.copy(fontWeight = FontWeight.Medium),
        labelMedium = tokens.sm.copy(fontWeight = FontWeight.Medium),
        labelSmall = tokens.xs.copy(fontWeight = FontWeight.Medium)
    )
}
