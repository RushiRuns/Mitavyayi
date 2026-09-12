package com.rushi.mitavyay.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Modern, charming vector/canvas empty state illustrations for Mitavyay.
 * Built entirely with offline Compose Canvas operations adapting dynamically to theme palettes.
 */

/**
 * Empty transaction history illustration: A stylized leather wallet with a floating coin.
 */
@Composable
fun EmptyTransactionsIllustration(
    modifier: Modifier = Modifier,
    size: Dp = 140.dp
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val outlineColor = MaterialTheme.colorScheme.outlineVariant

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val width = size.toPx()
            val height = size.toPx()
            val center = Offset(width / 2f, height / 2f)

            // 1. Soft ambient background circle
            drawCircle(
                color = surfaceVariant.copy(alpha = 0.45f),
                radius = width * 0.42f,
                center = center
            )

            // 2. Wallet body (rounded rectangle)
            val walletWidth = width * 0.55f
            val walletHeight = height * 0.38f
            val walletLeft = center.x - walletWidth / 2f
            val walletTop = center.y - walletHeight * 0.15f

            drawRoundRect(
                color = primaryColor.copy(alpha = 0.85f),
                topLeft = Offset(walletLeft, walletTop),
                size = Size(walletWidth, walletHeight),
                cornerRadius = CornerRadius(16f, 16f)
            )

            // 3. Wallet card slot flap
            val flapWidth = walletWidth * 0.4f
            val flapHeight = walletHeight * 0.5f
            val flapLeft = walletLeft + walletWidth - flapWidth
            val flapTop = walletTop + (walletHeight - flapHeight) / 2f

            drawRoundRect(
                color = secondaryColor.copy(alpha = 0.9f),
                topLeft = Offset(flapLeft, flapTop),
                size = Size(flapWidth, flapHeight),
                cornerRadius = CornerRadius(10f, 10f)
            )

            // 4. Clasp button
            drawCircle(
                color = Color.White,
                radius = 7f,
                center = Offset(flapLeft + flapWidth * 0.35f, flapTop + flapHeight / 2f)
            )

            // 5. Floating Coin (above wallet)
            val coinCenter = Offset(center.x - 14f, walletTop - 18f)
            val coinRadius = width * 0.12f

            drawCircle(
                color = primaryColor,
                radius = coinRadius,
                center = coinCenter
            )

            // Coin inner rim
            drawCircle(
                color = Color.White.copy(alpha = 0.4f),
                radius = coinRadius * 0.72f,
                center = coinCenter,
                style = Stroke(width = 3f)
            )

            // Floating sparkle dots
            drawCircle(
                color = primaryColor.copy(alpha = 0.6f),
                radius = 5f,
                center = Offset(center.x + width * 0.28f, center.y - height * 0.25f)
            )
            drawCircle(
                color = secondaryColor.copy(alpha = 0.5f),
                radius = 3.5f,
                center = Offset(center.x - width * 0.32f, center.y - height * 0.18f)
            )
        }
    }
}

/**
 * Empty search results illustration: Magnifying glass over document lines with dashed search rays.
 */
@Composable
fun EmptySearchIllustration(
    modifier: Modifier = Modifier,
    size: Dp = 140.dp
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val outlineColor = MaterialTheme.colorScheme.outlineVariant

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val width = size.toPx()
            val height = size.toPx()
            val center = Offset(width / 2f, height / 2f)

            // 1. Soft background circle
            drawCircle(
                color = surfaceVariant.copy(alpha = 0.45f),
                radius = width * 0.42f,
                center = center
            )

            // 2. Document card in background
            val docWidth = width * 0.46f
            val docHeight = height * 0.54f
            val docLeft = center.x - docWidth * 0.6f
            val docTop = center.y - docHeight * 0.6f

            drawRoundRect(
                color = surfaceVariant.copy(alpha = 0.8f),
                topLeft = Offset(docLeft, docTop),
                size = Size(docWidth, docHeight),
                cornerRadius = CornerRadius(12f, 12f)
            )

            // Document line placeholders
            drawLine(
                color = outlineColor,
                start = Offset(docLeft + 16f, docTop + 24f),
                end = Offset(docLeft + docWidth - 28f, docTop + 24f),
                strokeWidth = 6f,
                cap = StrokeCap.Round
            )
            drawLine(
                color = outlineColor,
                start = Offset(docLeft + 16f, docTop + 44f),
                end = Offset(docLeft + docWidth - 16f, docTop + 44f),
                strokeWidth = 6f,
                cap = StrokeCap.Round
            )
            drawLine(
                color = outlineColor,
                start = Offset(docLeft + 16f, docTop + 64f),
                end = Offset(docLeft + docWidth * 0.5f, docTop + 64f),
                strokeWidth = 6f,
                cap = StrokeCap.Round
            )

            // 3. Foreground Magnifying Glass
            val glassCenter = Offset(center.x + width * 0.12f, center.y + height * 0.05f)
            val glassRadius = width * 0.18f

            drawCircle(
                color = primaryColor,
                radius = glassRadius,
                center = glassCenter,
                style = Stroke(width = 8f)
            )

            // Glass reflection highlight
            val highlightPath = Path().apply {
                val r = glassRadius * 0.65f
                moveTo(glassCenter.x - r * 0.7f, glassCenter.y - r * 0.7f)
                cubicTo(
                    glassCenter.x - r * 0.3f, glassCenter.y - r * 0.9f,
                    glassCenter.x + r * 0.3f, glassCenter.y - r * 0.9f,
                    glassCenter.x + r * 0.7f, glassCenter.y - r * 0.7f
                )
            }
            drawPath(
                path = highlightPath,
                color = primaryColor.copy(alpha = 0.4f),
                style = Stroke(width = 3.5f, cap = StrokeCap.Round)
            )

            // Handle
            val handleStart = Offset(
                glassCenter.x + glassRadius * 0.707f,
                glassCenter.y + glassRadius * 0.707f
            )
            val handleEnd = Offset(
                handleStart.x + width * 0.14f,
                handleStart.y + height * 0.14f
            )

            drawLine(
                color = primaryColor,
                start = handleStart,
                end = handleEnd,
                strokeWidth = 10f,
                cap = StrokeCap.Round
            )
        }
    }
}

/**
 * Empty budget illustration: Balance target and spending ring.
 */
@Composable
fun EmptyBudgetIllustration(
    modifier: Modifier = Modifier,
    size: Dp = 140.dp
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val width = size.toPx()
            val height = size.toPx()
            val center = Offset(width / 2f, height / 2f)

            // 1. Soft background
            drawCircle(
                color = surfaceVariant.copy(alpha = 0.45f),
                radius = width * 0.42f,
                center = center
            )

            // 2. Target outer arc
            drawCircle(
                color = primaryColor.copy(alpha = 0.3f),
                radius = width * 0.28f,
                center = center,
                style = Stroke(width = 6f)
            )

            // 3. Inner donut ring
            drawCircle(
                color = primaryColor,
                radius = width * 0.18f,
                center = center,
                style = Stroke(width = 10f)
            )

            // 4. Center bullseye
            drawCircle(
                color = secondaryColor,
                radius = width * 0.07f,
                center = center
            )
        }
    }
}

/**
 * Empty goals illustration: Target flag milestone with stars.
 */
@Composable
fun EmptyGoalsIllustration(
    modifier: Modifier = Modifier,
    size: Dp = 140.dp
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val width = size.toPx()
            val height = size.toPx()
            val center = Offset(width / 2f, height / 2f)

            // 1. Soft background
            drawCircle(
                color = surfaceVariant.copy(alpha = 0.45f),
                radius = width * 0.42f,
                center = center
            )

            // 2. Base platform
            val baseLeft = center.x - width * 0.28f
            val baseTop = center.y + height * 0.2f
            drawRoundRect(
                color = primaryColor.copy(alpha = 0.7f),
                topLeft = Offset(baseLeft, baseTop),
                size = Size(width * 0.56f, height * 0.08f),
                cornerRadius = CornerRadius(8f, 8f)
            )

            // 3. Flag pole
            val poleX = center.x - width * 0.1f
            drawLine(
                color = primaryColor,
                start = Offset(poleX, baseTop),
                end = Offset(poleX, center.y - height * 0.28f),
                strokeWidth = 6f,
                cap = StrokeCap.Round
            )

            // 4. Flag triangle
            val flagPath = Path().apply {
                moveTo(poleX, center.y - height * 0.28f)
                lineTo(poleX + width * 0.3f, center.y - height * 0.18f)
                lineTo(poleX, center.y - height * 0.08f)
                close()
            }
            drawPath(path = flagPath, color = secondaryColor)

            // 5. Star sparkle
            drawCircle(
                color = primaryColor,
                radius = 5f,
                center = Offset(center.x + width * 0.26f, center.y - height * 0.22f)
            )
        }
    }
}
