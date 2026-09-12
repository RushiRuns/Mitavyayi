package com.rushi.mitavyay.util

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/**
 * Utility helpers providing contextual haptic vibration feedback for touch interactions,
 * action confirmations, and errors across the Mitavyay app.
 *
 * Honors Android API level capabilities (VibratorManager on API 31+, VibrationEffect on API 26+)
 * with safe degradation and error suppression.
 */
object HapticFeedbackHelper {

    /**
     * Resolves the system Vibrator instance across Android API levels.
     */
    fun getVibrator(context: Context): Vibrator? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator ?: (context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator)
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
        } catch (_: Throwable) {
            null
        }
    }
}

/**
 * Triggers a light tactile tap for standard interactive UI elements (chips, toggles, icon buttons).
 */
fun Context.hapticLight(enabled: Boolean = true) {
    if (!enabled) return
    try {
        val vibrator = HapticFeedbackHelper.getVibrator(this) ?: return
        if (!vibrator.hasVibrator()) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(15L, 60))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(15L)
        }
    } catch (_: Throwable) {
        // Suppress any vibration failure on devices without hardware support or restricted permissions
    }
}

/**
 * Triggers a medium tactile click for button presses and navigation items.
 */
fun Context.hapticMedium(enabled: Boolean = true) {
    if (!enabled) return
    try {
        val vibrator = HapticFeedbackHelper.getVibrator(this) ?: return
        if (!vibrator.hasVibrator()) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(30L, 140))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(30L)
        }
    } catch (_: Throwable) {
        // Suppress any vibration failure
    }
}

/**
 * Triggers a heavy tactile feedback for prominent or emphatic actions.
 */
fun Context.hapticHeavy(enabled: Boolean = true) {
    if (!enabled) return
    try {
        val vibrator = HapticFeedbackHelper.getVibrator(this) ?: return
        if (!vibrator.hasVibrator()) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK))
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(50L, 255))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(50L)
        }
    } catch (_: Throwable) {
        // Suppress any vibration failure
    }
}

/**
 * Triggers a double-pulse tactile confirmation pattern for successful actions (e.g. transaction saved,
 * goal reached, budget created).
 */
fun Context.hapticSuccess(enabled: Boolean = true) {
    if (!enabled) return
    try {
        val vibrator = HapticFeedbackHelper.getVibrator(this) ?: return
        if (!vibrator.hasVibrator()) return

        val timings = longArrayOf(0L, 30L, 60L, 45L)
        val amplitudes = intArrayOf(0, 100, 0, 180)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(timings, -1)
        }
    } catch (_: Throwable) {
        // Suppress any vibration failure
    }
}

/**
 * Triggers a distinctive multi-pulse tactile pattern for validation failures, rejected actions,
 * or destructive dialogs.
 */
fun Context.hapticError(enabled: Boolean = true) {
    if (!enabled) return
    try {
        val vibrator = HapticFeedbackHelper.getVibrator(this) ?: return
        if (!vibrator.hasVibrator()) return

        val timings = longArrayOf(0L, 40L, 50L, 40L, 50L, 40L)
        val amplitudes = intArrayOf(0, 200, 0, 200, 0, 200)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(timings, -1)
        }
    } catch (_: Throwable) {
        // Suppress any vibration failure
    }
}
