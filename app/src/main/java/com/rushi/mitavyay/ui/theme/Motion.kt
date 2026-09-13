package com.rushi.mitavyay.ui.theme

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Standardized motion and animation tokens for Mitavyay.
 * Provides consistent durations, easings, and transition specifications.
 */
@Immutable
data class AppMotion(
    // Standard durations (in milliseconds)
    val durationFast: Int = 150,     // Button presses, micro-interactions, chip selection
    val durationNormal: Int = 300,   // Dialogs, card expand/collapse, list items
    val durationSlow: Int = 500,     // Screen transitions, chart reveals

    // Standard easing curves
    val emphasizedDecelerate: Easing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f),
    val emphasizedAccelerate: Easing = CubicBezierEasing(0.3f, 0.0f, 0.8f, 0.15f),
    val standard: Easing = FastOutSlowInEasing,
    val linear: Easing = LinearEasing
) {
    // Standard tween specs
    fun <T> fastTween(): TweenSpec<T> = tween(durationMillis = durationFast, easing = standard)
    fun <T> normalTween(): TweenSpec<T> = tween(durationMillis = durationNormal, easing = standard)
    fun <T> slowTween(): TweenSpec<T> = tween(durationMillis = durationSlow, easing = emphasizedDecelerate)

    // Bouncy spring spec for micro-interactions
    fun <T> bouncySpring(): SpringSpec<T> = spring(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMedium
    )

    // Standard Screen Transitions
    fun screenEnterTransition(): EnterTransition =
        slideInHorizontally(
            initialOffsetX = { fullWidth -> (fullWidth * 0.15f).toInt() },
            animationSpec = tween(durationMillis = durationNormal, easing = emphasizedDecelerate)
        ) + fadeIn(animationSpec = tween(durationMillis = durationNormal, easing = standard))

    fun screenExitTransition(): ExitTransition =
        slideOutHorizontally(
            targetOffsetX = { fullWidth -> -(fullWidth * 0.15f).toInt() },
            animationSpec = tween(durationMillis = durationNormal, easing = emphasizedAccelerate)
        ) + fadeOut(animationSpec = tween(durationMillis = durationFast, easing = standard))

    fun screenPopEnterTransition(): EnterTransition =
        slideInHorizontally(
            initialOffsetX = { fullWidth -> -(fullWidth * 0.15f).toInt() },
            animationSpec = tween(durationMillis = durationNormal, easing = emphasizedDecelerate)
        ) + fadeIn(animationSpec = tween(durationMillis = durationNormal, easing = standard))

    fun screenPopExitTransition(): ExitTransition =
        slideOutHorizontally(
            targetOffsetX = { fullWidth -> (fullWidth * 0.15f).toInt() },
            animationSpec = tween(durationMillis = durationNormal, easing = emphasizedAccelerate)
        ) + fadeOut(animationSpec = tween(durationMillis = durationFast, easing = standard))

    // Tab crossfade transitions for top-level navigation
    fun tabCrossfadeEnter(): EnterTransition =
        fadeIn(animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing))

    fun tabCrossfadeExit(): ExitTransition =
        fadeOut(animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing))
}

val LocalAppMotion = staticCompositionLocalOf { AppMotion() }

val MaterialTheme.appMotion: AppMotion
    @Composable
    @ReadOnlyComposable
    get() = LocalAppMotion.current
