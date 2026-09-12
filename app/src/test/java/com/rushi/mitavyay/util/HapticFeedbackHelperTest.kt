package com.rushi.mitavyay.util

import android.content.Context
import android.content.ContextWrapper
import org.junit.Assert.assertNull
import org.junit.Test

class HapticFeedbackHelperTest {

    private class TestContext : ContextWrapper(null) {
        override fun getSystemService(name: String): Any? {
            return null
        }
    }

    @Test
    fun getVibrator_returnsNull_whenSystemServiceNotAvailable() {
        val testContext = TestContext()
        val vibrator = HapticFeedbackHelper.getVibrator(testContext)
        assertNull(vibrator)
    }

    @Test
    fun hapticFunctions_doNotThrow_whenDisabled() {
        val testContext = TestContext()

        // When enabled = false, functions should exit early without touching system services
        testContext.hapticLight(enabled = false)
        testContext.hapticMedium(enabled = false)
        testContext.hapticHeavy(enabled = false)
        testContext.hapticSuccess(enabled = false)
        testContext.hapticError(enabled = false)
    }

    @Test
    fun hapticFunctions_doNotThrow_whenEnabledAndNoVibratorService() {
        val testContext = TestContext()

        // When enabled = true but vibrator is null, functions safely complete without throwing
        testContext.hapticLight(enabled = true)
        testContext.hapticMedium(enabled = true)
        testContext.hapticHeavy(enabled = true)
        testContext.hapticSuccess(enabled = true)
        testContext.hapticError(enabled = true)
    }

    @Test
    fun hapticFunctions_doNotThrow_whenContextThrowsException() {
        val throwingContext = object : ContextWrapper(null) {
            override fun getSystemService(name: String): Any? {
                throw SecurityException("Mock permission denied or missing hardware")
            }
        }

        // Functions must catch all Throwables and suppress them cleanly
        throwingContext.hapticLight(enabled = true)
        throwingContext.hapticMedium(enabled = true)
        throwingContext.hapticHeavy(enabled = true)
        throwingContext.hapticSuccess(enabled = true)
        throwingContext.hapticError(enabled = true)
    }
}
