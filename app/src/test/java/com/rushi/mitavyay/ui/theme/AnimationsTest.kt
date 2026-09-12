package com.rushi.mitavyay.ui.theme

import androidx.compose.animation.core.Spring
import com.rushi.mitavyay.ui.navigation.NavDestination
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AnimationsTest {

    private val motion = AppMotion()

    @Test
    fun testMotionDurationTokens() {
        assertEquals(150, motion.durationFast)
        assertEquals(300, motion.durationNormal)
        assertEquals(500, motion.durationSlow)
    }

    @Test
    fun testMotionEasingTokens() {
        assertNotNull(motion.standard)
        assertNotNull(motion.emphasizedDecelerate)
        assertNotNull(motion.emphasizedAccelerate)
        assertNotNull(motion.linear)
    }

    @Test
    fun testMotionSpecs() {
        val fastTween = motion.fastTween<Float>()
        assertEquals(150, fastTween.durationMillis)

        val normalTween = motion.normalTween<Float>()
        assertEquals(300, normalTween.durationMillis)

        val slowTween = motion.slowTween<Float>()
        assertEquals(500, slowTween.durationMillis)

        val bouncySpring = motion.bouncySpring<Float>()
        assertEquals(Spring.DampingRatioMediumBouncy, bouncySpring.dampingRatio)
    }

    @Test
    fun testScreenTransitionsCreated() {
        assertNotNull(motion.screenEnterTransition())
        assertNotNull(motion.screenExitTransition())
        assertNotNull(motion.screenPopEnterTransition())
        assertNotNull(motion.screenPopExitTransition())
        assertNotNull(motion.tabCrossfadeEnter())
        assertNotNull(motion.tabCrossfadeExit())
    }

    @Test
    fun testTopLevelNavigationCheck() {
        assertTrue(NavDestination.isTopLevel(NavDestination.TransactionList.route))
        assertTrue(NavDestination.isTopLevel(NavDestination.Analysis.route))
        assertTrue(NavDestination.isTopLevel(NavDestination.Accounts.route))

        assertFalse(NavDestination.isTopLevel(NavDestination.Budget.route))
        assertFalse(NavDestination.isTopLevel(NavDestination.Goals.route))
        assertFalse(NavDestination.isTopLevel(NavDestination.Debt.route))
        assertFalse(NavDestination.isTopLevel(NavDestination.RepeatExpenses.route))
        assertFalse(NavDestination.isTopLevel(NavDestination.Categories.route))
        assertFalse(NavDestination.isTopLevel(NavDestination.ImportExport.route))
        assertFalse(NavDestination.isTopLevel("transaction_detail/tx_123"))
        assertFalse(NavDestination.isTopLevel(null))
        assertFalse(NavDestination.isTopLevel("unknown_route"))
    }
}
