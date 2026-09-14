package com.rushi.mitavyay.ui.navigation

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.rushi.mitavyay.ui.MitavyayAppState
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
 *
 * Only ONE top-level destination ("root") ever lives in this back stack. The
 * Transactions / Analysis / More tabs are switched inside it with a plain
 * Crossfade driven by [MitavyayAppState.selectedTab] - never via
 * navController.navigate(). That means tab switching is always the same code
 * path in both directions: no push, no pop, no asymmetric transition to worry
 * about. Every other route below is a genuine push onto the shared back stack,
 * using the normal screen transitions exactly as before.
 */
@Composable
fun MitavyayNavHost(
    navController: NavHostController,
    appState: MitavyayAppState,
    modifier: Modifier = Modifier,
    startDestination: String = MitavyayAppState.ROOT_ROUTE
) {
    val motion = MaterialTheme.appMotion

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
        enterTransition = { motion.screenEnterTransition() },
        exitTransition = { motion.screenExitTransition() },
        popEnterTransition = { motion.screenPopEnterTransition() },
        popExitTransition = { motion.screenPopExitTransition() }
    ) {
        composable(route = MitavyayAppState.ROOT_ROUTE) {
            val tabStateHolder = rememberSaveableStateHolder()
            Crossfade(
                targetState = appState.selectedTab,
                animationSpec = tween(durationMillis = 220),
                label = "TabCrossfade"
            ) { tab ->
                // Keyed per tab so each one keeps its own scroll position /
                // rememberSaveable state across switches, the same way NavHost's
                // saveState/restoreState used to - just without touching the
                // NavController to get it.
                tabStateHolder.SaveableStateProvider(key = tab.route) {
                    when (tab) {
                        NavDestination.Analysis -> AnalysisScreen(
                            onNavigateToInsights = {
                                navController.navigate(NavDestination.Insights.route)
                            }
                        )
                        NavDestination.More -> HubScreen(
                            onNavigateToAccounts = { navController.navigate(NavDestination.Accounts.route) },
                            onNavigateToDebt = { navController.navigate(NavDestination.Debt.route) },
                            onNavigateToInsights = { navController.navigate(NavDestination.Insights.route) },
                            onNavigateToRepeatExpenses = { navController.navigate(NavDestination.RepeatExpenses.route) },
                            onNavigateToGoals = { navController.navigate(NavDestination.Goals.route) },
                            onNavigateToBudget = { navController.navigate(NavDestination.Budget.route) },
                            onNavigateToCategories = { navController.navigate(NavDestination.Categories.route) },
                            onNavigateToSettings = { navController.navigate(NavDestination.Settings.route) }
                        )
                        // NavDestination.TransactionList, and the default tab.
                        else -> TransactionListScreen(
                            onTransactionClick = { transactionId ->
                                navController.navigate(NavDestination.TransactionDetail.createRoute(transactionId))
                            }
                        )
                    }
                }
            }
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