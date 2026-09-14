package com.rushi.mitavyay.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.rushi.mitavyay.ui.navigation.NavDestination

/**
 * Remembers and initializes [MitavyayAppState].
 */
@Composable
fun rememberMitavyayAppState(
    navController: NavHostController = rememberNavController()
): MitavyayAppState {
    return remember(navController) {
        MitavyayAppState(navController)
    }
}

/**
 * State holder managing navigation and app-level UI state.
 *
 * IMPORTANT: the Transactions / Analysis / More tabs are switched purely via
 * [selectedTab], a plain Compose state - never via navController.navigate(). The
 * NavHost only ever has ONE top-level back stack entry ("root") that hosts all
 * three tabs internally via a Crossfade. This is deliberate: routing tab switches
 * through navController.navigate() made "leaving the start destination" a push and
 * "returning to it" a pop, and NavHost applies different internal stacking/z-order
 * handling to those two cases even when the same enter/exit specs are used for
 * both - that's what caused the tab switch to glitch in only one direction. With
 * tab switching handled entirely outside the NavController, that distinction can't
 * arise anymore.
 */
@Stable
class MitavyayAppState(
    val navController: NavHostController
) {
    var selectedTab: NavDestination by mutableStateOf(NavDestination.TransactionList)
        private set

    val currentRoute: String?
        @Composable get() = navController.currentBackStackEntryAsState().value?.destination?.route

    val isAtRoot: Boolean
        @Composable get() = currentRoute == ROOT_ROUTE

    val currentDestination: NavDestination
        @Composable get() = if (isAtRoot) selectedTab else NavDestination.fromRoute(currentRoute)

    val isTopLevelDestination: Boolean
        @Composable get() = isAtRoot

    /**
     * Switches the active tab. Only ever called while at "root" (the bottom bar
     * that triggers this is hidden on every other screen), so there's nothing to
     * pop or navigate - it's a plain state change, always the identical code path
     * regardless of which tab you're coming from or going to.
     */
    fun selectTab(destination: NavDestination) {
        selectedTab = destination
    }

    fun navigateBack() {
        navController.popBackStack()
    }

    companion object {
        const val ROOT_ROUTE = "root"
    }
}