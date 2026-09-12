package com.rushi.mitavyay.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.rushi.mitavyay.ui.screens.Accounts.AccountsScreen
import com.rushi.mitavyay.ui.screens.Analysis.AnalysisScreen
import com.rushi.mitavyay.ui.screens.TransactionDetail.TransactionDetailScreen
import com.rushi.mitavyay.ui.screens.TransactionList.TransactionListScreen

/**
 * Top-level Jetpack Navigation Host for Mitavyay.
 * Declares all screen composables and routes.
 */
@Composable
fun MitavyayNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: String = NavDestination.TransactionList.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(NavDestination.TransactionList.route) {
            TransactionListScreen(
                onTransactionClick = { transactionId ->
                    navController.navigate(NavDestination.TransactionDetail.createRoute(transactionId))
                }
            )
        }
        composable(NavDestination.Analysis.route) {
            AnalysisScreen()
        }
        composable(NavDestination.Accounts.route) {
            AccountsScreen()
        }
        composable(NavDestination.TransactionDetail.route) {
            TransactionDetailScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
