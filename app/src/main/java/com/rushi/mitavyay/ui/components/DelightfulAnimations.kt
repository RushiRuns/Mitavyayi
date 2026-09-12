package com.rushi.mitavyay.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Single confetti particle definition for [CelebrationEffect].
 */
private data class ConfettiParticle(
    val initialX: Float,
    val initialY: Float,
    val angle: Double,
    val speed: Float,
    val color: Color,
    val size: Float,
    val rotationSpeed: Float
)

private val CelebrationPalette = listOf(
    Color(0xFF4CAF50), // Green
    Color(0xFFFFC107), // Amber
    Color(0xFF2196F3), // Blue
    Color(0xFFE91E63), // Pink
    Color(0xFF9C27B0), // Purple
    Color(0xFFFF5722)  // Deep Orange
)

/**
 * Pure Compose Canvas celebration / confetti burst effect.
 * Fully offline, high-performance particle animation for financial milestones (e.g. goal reached, budget set).
 */
@Composable
fun CelebrationEffect(
    modifier: Modifier = Modifier,
    particleCount: Int = 45,
    onFinished: (() -> Unit)? = null
) {
    val progress = remember { Animatable(0f) }

    val particles = remember(particleCount) {
        val random = Random(System.currentTimeMillis())
        List(particleCount) {
            ConfettiParticle(
                initialX = 0.5f,
                initialY = 0.4f,
                angle = random.nextDouble(0.0, 2 * Math.PI),
                speed = random.nextFloat() * 450f + 250f,
                color = CelebrationPalette[random.nextInt(CelebrationPalette.size)],
                size = random.nextFloat() * 10f + 8f,
                rotationSpeed = random.nextFloat() * 720f - 360f
            )
        }
    }

    LaunchedEffect(Unit) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1400, easing = LinearEasing)
        )
        onFinished?.invoke()
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val t = progress.value

        particles.forEach { p ->
            val currentDistance = p.speed * t
            val x = (p.initialX * canvasWidth) + (cos(p.angle).toFloat() * currentDistance)
            // Add gravity curve
            val y = (p.initialY * canvasHeight) + (sin(p.angle).toFloat() * currentDistance) + (400f * t * t)
            val alpha = (1f - t).coerceIn(0f, 1f)

            rotate(degrees = p.rotationSpeed * t, pivot = Offset(x, y)) {
                drawRect(
                    color = p.color.copy(alpha = alpha),
                    topLeft = Offset(x - p.size / 2, y - p.size / 2),
                    size = Size(p.size, p.size * 0.6f)
                )
            }
        }
    }
}

/**
 * Reusable wrapper for offline Lottie vector animations.
 *
 * @param rawResId Android raw resource ID for local JSON animation.
 * @param modifier Composable modifier.
 * @param iterations Number of times the animation should loop (default: 1).
 */
@Composable
fun OfflineLottieAnimation(
    rawResId: Int,
    modifier: Modifier = Modifier,
    iterations: Int = 1
) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(rawResId))
    val animProgress by animateLottieCompositionAsState(
        composition = composition,
        iterations = iterations
    )

    LottieAnimation(
        composition = composition,
        progress = { animProgress },
        modifier = modifier
    )
}
