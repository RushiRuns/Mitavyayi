package com.rushi.mitavyay.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ThemeTokensTest {

    @Test
    fun spacingTokens_matchSpecifications() {
        val spacing = Spacing()
        assertEquals(0.dp, spacing.none)
        assertEquals(4.dp, spacing.xs)
        assertEquals(8.dp, spacing.sm)
        assertEquals(16.dp, spacing.md)
        assertEquals(24.dp, spacing.lg)
        assertEquals(32.dp, spacing.xl)
        assertEquals(48.dp, spacing.xxl)

        // Computed convenience tokens
        assertEquals(16.dp, spacing.screenHorizontal)
        assertEquals(16.dp, spacing.screenVertical)
        assertEquals(16.dp, spacing.cardContent)
        assertEquals(8.dp, spacing.itemSpacing)
        assertEquals(24.dp, spacing.sectionSpacing)
        assertEquals(8.dp, spacing.iconSpacing)
    }

    @Test
    fun shapeTokens_matchSpecifications() {
        val shapes = AppShapes()
        val density = androidx.compose.ui.unit.Density(1.0f)

        assertEquals(0f, shapes.none.topStart.toPx(androidx.compose.ui.geometry.Size(100f, 100f), density))
        assertEquals(4f, shapes.small.topStart.toPx(androidx.compose.ui.geometry.Size(100f, 100f), density))
        assertEquals(8f, shapes.medium.topStart.toPx(androidx.compose.ui.geometry.Size(100f, 100f), density))
        assertEquals(12f, shapes.large.topStart.toPx(androidx.compose.ui.geometry.Size(100f, 100f), density))
        assertEquals(9999f, shapes.full.topStart.toPx(androidx.compose.ui.geometry.Size(100000f, 100000f), density))
    }

    @Test
    fun typographyTokens_defaultScale() {
        val typography = getAppTypography(fontScale = 1.0f)
        assertEquals(12.sp, typography.xs.fontSize)
        assertEquals(13.sp, typography.sm.fontSize)
        assertEquals(15.sp, typography.body.fontSize)
        assertEquals(18.sp, typography.lg.fontSize)
        assertEquals(20.sp, typography.xl.fontSize)
        assertEquals(24.sp, typography.title.fontSize)
    }

    @Test
    fun typographyTokens_scalesWithFontScaleMultiplier() {
        val scaledTypography = getAppTypography(fontScale = 1.5f)
        assertEquals(18.sp, scaledTypography.xs.fontSize)
        assertEquals(19.5.sp, scaledTypography.sm.fontSize)
        assertEquals(22.5.sp, scaledTypography.body.fontSize)
        assertEquals(27.sp, scaledTypography.lg.fontSize)
        assertEquals(30.sp, scaledTypography.xl.fontSize)
        assertEquals(36.sp, scaledTypography.title.fontSize)

        val smallTypography = getAppTypography(fontScale = 0.8f)
        assertEquals(9.6.sp, smallTypography.xs.fontSize)
        assertEquals(12.sp, smallTypography.body.fontSize)
        assertEquals(19.2.sp, smallTypography.title.fontSize)
    }

    @Test
    fun materialTypography_mapsCorrectly() {
        val typography = getTypography(fontScale = 1.0f)
        assertEquals(24.sp, typography.displaySmall.fontSize)
        assertEquals(24.sp, typography.headlineLarge.fontSize)
        assertEquals(20.sp, typography.titleLarge.fontSize)
        assertEquals(18.sp, typography.titleMedium.fontSize)
        assertEquals(15.sp, typography.bodyMedium.fontSize)
        assertEquals(13.sp, typography.bodySmall.fontSize)
        assertEquals(12.sp, typography.labelSmall.fontSize)
    }

    @Test
    fun dualTheme_colorSchemesAreDistinctAndComplete() {
        // Ensure Light and Dark schemes have distinct primaries and backgrounds
        assertNotEquals(LightColorScheme.primary, DarkColorScheme.primary)
        assertNotEquals(LightColorScheme.background, DarkColorScheme.background)
        assertNotEquals(LightColorScheme.surface, DarkColorScheme.surface)
        assertNotEquals(LightColorScheme.error, DarkColorScheme.error)

        // Extended colors
        assertNotEquals(LightExtendedColorScheme.success, DarkExtendedColorScheme.success)
        assertNotEquals(LightExtendedColorScheme.warning, DarkExtendedColorScheme.warning)

        // Ensure no colors are unspecified
        assertTrue(LightColorScheme.primary != Color.Unspecified)
        assertTrue(DarkColorScheme.primary != Color.Unspecified)
        assertTrue(LightExtendedColorScheme.success != Color.Unspecified)
        assertTrue(DarkExtendedColorScheme.success != Color.Unspecified)
        assertTrue(LightExtendedColorScheme.warning != Color.Unspecified)
        assertTrue(DarkExtendedColorScheme.warning != Color.Unspecified)
    }
}
