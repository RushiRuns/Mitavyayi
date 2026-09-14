package com.rushi.mitavyay.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.rushi.mitavyay.data.repository.CategorySpending
import com.rushi.mitavyay.ui.theme.appShapes
import com.rushi.mitavyay.ui.theme.spacing
import com.rushi.mitavyay.util.CurrencyFormatter
import java.util.Locale

private val FallbackCategoryPalette = listOf(
    Color(0xFF26A69A),
    Color(0xFFFF7043),
    Color(0xFF42A5F5),
    Color(0xFFAB47BC),
    Color(0xFFFFA726),
    Color(0xFFEC407A),
    Color(0xFF66BB6A),
    Color(0xFF29B6F6),
    Color(0xFFEF5350),
    Color(0xFF78909C),
    Color(0xFF8D6E63)
)

fun parseCategoryColor(hex: String?, index: Int = 0): Color {
    if (!hex.isNullOrBlank()) {
        try {
            val clean = hex.removePrefix("#")
            if (clean.length == 6) {
                val colorLong = clean.toLong(16) or 0xFF000000L
                return Color(colorLong)
            } else if (clean.length == 8) {
                val colorLong = clean.toLong(16)
                return Color(colorLong)
            }
        } catch (_: Exception) {
            // fallback
        }
    }
    return FallbackCategoryPalette[index % FallbackCategoryPalette.size]
}

/**
 * Donut / Pie chart visualizing spending by category using Compose Canvas.
 */
@Composable
fun CategoryDonutChart(
    categorySpendings: List<CategorySpending>,
    totalExpensePaise: Long,
    modifier: Modifier = Modifier,
    chartSize: Dp = 210.dp,
    strokeWidth: Dp = 32.dp
) {
    var lastDataHash by androidx.compose.runtime.saveable.rememberSaveable { androidx.compose.runtime.mutableIntStateOf(0) }
    val currentHash = categorySpendings.hashCode()
    val isRestored = lastDataHash == currentHash && currentHash != 0
    val animProgress = remember { Animatable(if (isRestored) 1f else 0f) }

    LaunchedEffect(categorySpendings) {
        if (currentHash != lastDataHash) {
            animProgress.snapTo(0f)
            animProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 750, easing = FastOutSlowInEasing)
            )
            lastDataHash = currentHash
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(chartSize),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(chartSize)) {
                val strokePx = strokeWidth.toPx()
                val diameter = size.minDimension - strokePx
                val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
                val arcSize = Size(diameter, diameter)
                val stroke = Stroke(width = strokePx, cap = StrokeCap.Butt)

                if (categorySpendings.isEmpty() || totalExpensePaise <= 0L) {
                    drawArc(
                        color = Color.LightGray.copy(alpha = 0.3f),
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = stroke
                    )
                } else {
                    var currentStartAngle = -90f
                    categorySpendings.forEachIndexed { index, item ->
                        val sweepAngle = (item.percentage / 100f) * 360f * animProgress.value
                        val color = parseCategoryColor(item.colorHex, index)
                        drawArc(
                            color = color,
                            startAngle = currentStartAngle,
                            sweepAngle = sweepAngle,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = stroke
                        )
                        currentStartAngle += sweepAngle
                    }
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(horizontal = MaterialTheme.spacing.md)
            ) {
                Text(
                    text = "Total Spent",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = CurrencyFormatter.format(totalExpensePaise),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * Ranked category legend list displaying color dot, category name, percentage, and amount.
 */
@Composable
fun CategorySpendingLegendList(
    categorySpendings: List<CategorySpending>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm)
    ) {
        categorySpendings.forEachIndexed { index, item ->
            val color = parseCategoryColor(item.colorHex, index)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.appShapes.medium,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = MaterialTheme.spacing.md, vertical = MaterialTheme.spacing.sm),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(color)
                    )
                    Spacer(modifier = Modifier.width(MaterialTheme.spacing.sm))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.category,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${item.transactionCount} transaction${if (item.transactionCount > 1) "s" else ""}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.width(MaterialTheme.spacing.sm))
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = CurrencyFormatter.format(item.totalExpensePaise),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Surface(
                            shape = MaterialTheme.appShapes.small,
                            color = color.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = String.format(Locale.getDefault(), "%.1f%%", item.percentage),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = color,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
