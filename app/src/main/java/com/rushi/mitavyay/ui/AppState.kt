package com.rushi.mitavyay.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.navigation.NavGraph.Companion.findStartDestination
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
 */
@Stable
class MitavyayAppState(
    val navController: NavHostController
) {
    val currentRoute: String?
        @Composable get() = navController.currentBackStackEntryAsState().value?.destination?.route

    val currentDestination: NavDestination
        @Composable get() = NavDestination.fromRoute(currentRoute)

    val isTopLevelDestination: Boolean
        @Composable get() = NavDestination.topLevelDestinations.any { it.route == currentRoute }

    fun navigateToTopLevelDestination(destination: NavDestination) {
        navController.navigate(destination.route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    fun navigateBack() {
        navController.popBackStack()
    }
}
