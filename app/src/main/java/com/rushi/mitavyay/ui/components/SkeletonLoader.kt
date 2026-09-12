package com.rushi.mitavyay.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.rushi.mitavyay.ui.theme.appShapes
import com.rushi.mitavyay.ui.theme.spacing

/**
 * Creates an animated linear shimmer brush that sweeps across placeholder components.
 */
@Composable
fun shimmerBrush(
    showShimmer: Boolean = true,
    targetValue: Float = 1200f
): Brush {
    if (!showShimmer) {
        return Brush.linearGradient(
            colors = listOf(
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        )
    }

    val shimmerColors = listOf(
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
    )

    val transition = rememberInfiniteTransition(label = "shimmer_transition")
    val translateAnimation = transition.animateFloat(
        initialValue = 0f,
        targetValue = targetValue,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translation"
    )

    return Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateAnimation.value - 400f, translateAnimation.value - 400f),
        end = Offset(translateAnimation.value, translateAnimation.value)
    )
}

/**
 * Modifier for applying a shimmering background placeholder to any Box, Text, or container.
 */
fun Modifier.shimmerPlaceholder(
    visible: Boolean = true,
    shape: Shape = RoundedCornerShape(4.dp)
): Modifier = composed {
    if (visible) {
        this.background(brush = shimmerBrush(), shape = shape)
    } else {
        this
    }
}

/**
 * Shimmer skeleton loading placeholder mirroring the visual structure of [TransactionCard].
 */
@Composable
fun SkeletonTransactionCard(
    modifier: Modifier = Modifier
) {
    val brush = shimmerBrush()

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.appShapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(MaterialTheme.spacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
                modifier = Modifier.weight(1f)
            ) {
                // Category Icon placeholder
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(brush = brush, shape = CircleShape)
                )

                // Title and Subtitle placeholders
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(140.dp)
                            .height(14.dp)
                            .background(brush = brush, shape = RoundedCornerShape(4.dp))
                    )
                    Box(
                        modifier = Modifier
                            .width(85.dp)
                            .height(10.dp)
                            .background(brush = brush, shape = RoundedCornerShape(4.dp))
                    )
                }
            }

            // Amount and Date placeholders
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(75.dp)
                        .height(16.dp)
                        .background(brush = brush, shape = RoundedCornerShape(4.dp))
                )
                Box(
                    modifier = Modifier
                        .width(50.dp)
                        .height(10.dp)
                        .background(brush = brush, shape = RoundedCornerShape(4.dp))
                )
            }
        }
    }
}

/**
 * List of skeleton transaction cards displayed while transactions are loading.
 */
@Composable
fun SkeletonTransactionList(
    modifier: Modifier = Modifier,
    count: Int = 6,
    contentPadding: PaddingValues = PaddingValues(16.dp)
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(count) {
            SkeletonTransactionCard()
        }
    }
}

/**
 * Generic skeleton card placeholder of specified height.
 */
@Composable
fun SkeletonCard(
    modifier: Modifier = Modifier,
    height: Dp = 80.dp,
    shape: Shape = MaterialTheme.appShapes.medium
) {
    val brush = shimmerBrush()
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .background(brush = brush, shape = shape)
    )
}
