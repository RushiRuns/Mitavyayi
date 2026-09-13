package com.rushi.mitavyay.ui.navigation

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.rushi.mitavyay.ui.screens.Accounts.AccountsScreen
import com.rushi.mitavyay.ui.screens.Analysis.AnalysisScreen
import com.rushi.mitavyay.ui.screens.Budget.BudgetListScreen
import com.rushi.mitavyay.ui.screens.Categories.CategoriesScreen
import com.rushi.mitavyay.ui.screens.Debt.DebtListScreen
import com.rushi.mitavyay.ui.screens.Goals.GoalsListScreen
import com.rushi.mitavyay.ui.screens.Hub.HubScreen
import com.rushi.mitavyay.ui.screens.Insights.InsightsScreen
import com.rushi.mitavyay.ui.screens.RepeatExpense.RepeatExpenseListScreen
import com.rushi.mitavyay.ui.screens.Settings.ImportExportScreen
import com.rushi.mitavyay.ui.screens.Settings.SettingsScreen
import com.rushi.mitavyay.ui.screens.TransactionDetail.TransactionDetailScreen
import com.rushi.mitavyay.ui.screens.TransactionList.TransactionListScreen

import androidx.compose.material3.MaterialTheme
import com.rushi.mitavyay.ui.theme.appMotion

/**
 * Top-level Jetpack Navigation Host for Mitavyay.
 * Declares all screen composables and routes with smooth standardized transitions.
 */
@Composable
fun MitavyayNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: String = NavDestination.TransactionList.route
) {
    val motion = MaterialTheme.appMotion

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
        enterTransition = {
            val targetRoute = targetState.destination.route
            val initialRoute = initialState.destination.route
            if (NavDestination.isTopLevel(targetRoute) && NavDestination.isTopLevel(initialRoute)) {
                motion.tabCrossfadeEnter()
            } else {
                motion.screenEnterTransition()
            }
        },
        exitTransition = {
            val targetRoute = targetState.destination.route
            val initialRoute = initialState.destination.route
            if (NavDestination.isTopLevel(targetRoute) && NavDestination.isTopLevel(initialRoute)) {
                motion.tabCrossfadeExit()
            } else {
                motion.screenExitTransition()
            }
        },
        popEnterTransition = {
            motion.screenPopEnterTransition()
        },
        popExitTransition = {
            motion.screenPopExitTransition()
        }
    ) {
        composable(
            route = NavDestination.TransactionList.route,
            enterTransition = { fadeIn(animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing)) },
            exitTransition = { fadeOut(animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing)) }
        ) {
            TransactionListScreen(
                onTransactionClick = { transactionId ->
                    navController.navigate(NavDestination.TransactionDetail.createRoute(transactionId))
                }
            )
        }
        composable(
            route = NavDestination.Analysis.route,
            enterTransition = { fadeIn(animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing)) },
            exitTransition = { fadeOut(animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing)) }
        ) {
            AnalysisScreen(
                onNavigateToInsights = {
                    navController.navigate(NavDestination.Insights.route)
                }
            )
        }
        composable(
            route = NavDestination.More.route,
            enterTransition = { fadeIn(animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing)) },
            exitTransition = { fadeOut(animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing)) }
        ) {
            HubScreen(
                onNavigateToAccounts = { navController.navigate(NavDestination.Accounts.route) },
                onNavigateToDebt = { navController.navigate(NavDestination.Debt.route) },
                onNavigateToInsights = { navController.navigate(NavDestination.Insights.route) },
                onNavigateToRepeatExpenses = { navController.navigate(NavDestination.RepeatExpenses.route) },
                onNavigateToGoals = { navController.navigate(NavDestination.Goals.route) },
                onNavigateToBudget = { navController.navigate(NavDestination.Budget.route) },
                onNavigateToCategories = { navController.navigate(NavDestination.Categories.route) },
                onNavigateToSettings = { navController.navigate(NavDestination.Settings.route) }
            )
        }
        composable(NavDestination.Settings.route) {
            SettingsScreen(
                onNavigateToBackupData = {
                    navController.navigate(NavDestination.ImportExport.route)
                }
            )
        }
        composable(NavDestination.Accounts.route) {
            AccountsScreen()
        }
        composable(NavDestination.Categories.route) {
            CategoriesScreen()
        }
        composable(NavDestination.ImportExport.route) {
            ImportExportScreen()
        }
        composable(NavDestination.Debt.route) {
            DebtListScreen()
        }
        composable(NavDestination.RepeatExpenses.route) {
            RepeatExpenseListScreen()
        }
        composable(NavDestination.Goals.route) {
            GoalsListScreen()
        }
        composable(NavDestination.Budget.route) {
            BudgetListScreen()
        }
        composable(NavDestination.Insights.route) {
            InsightsScreen(
                onTransactionClick = { transactionId ->
                    navController.navigate(NavDestination.TransactionDetail.createRoute(transactionId))
                }
            )
        }
        composable(NavDestination.TransactionDetail.route) {
            TransactionDetailScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
