package com.rushi.mitavyay.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import com.rushi.mitavyay.ui.theme.appMotion

/**
 * Modifier that animates a smooth scale down on press and spring bounce on release.
 *
 * @param pressedScale The target scale when the component is pressed (default: 0.96f).
 * @param enabled Whether the press scale effect is active.
 * @param interactionSource Optional shared interaction source to observe press state.
 */
fun Modifier.pressScale(
    pressedScale: Float = 0.96f,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource? = null
): Modifier = composed {
    val source = interactionSource ?: remember { MutableInteractionSource() }
    val isPressed by source.collectIsPressedAsState()
    val motion = MaterialTheme.appMotion
    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) pressedScale else 1f,
        animationSpec = motion.bouncySpring(),
        label = "pressScaleAnimation"
    )

    this.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

/**
 * Convenience modifier combining clickable with a tactile bouncy press scale effect.
 */
fun Modifier.bounceClickable(
    enabled: Boolean = true,
    pressedScale: Float = 0.96f,
    onClick: () -> Unit
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    this
        .pressScale(
            pressedScale = pressedScale,
            enabled = enabled,
            interactionSource = interactionSource
        )
        .clickable(
            interactionSource = interactionSource,
            indication = null,
            enabled = enabled,
            onClick = onClick
        )
}
