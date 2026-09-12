package com.rushi.mitavyay.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Sealed hierarchy defining all navigation destinations in Mitavyay.
 */
sealed class NavDestination(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    data object TransactionList : NavDestination(
        route = "transactions",
        title = "Transactions",
        icon = Icons.Default.List
    )

    data object Analysis : NavDestination(
        route = "analysis",
        title = "Analysis",
        icon = Icons.Default.DateRange
    )

    data object Accounts : NavDestination(
        route = "accounts",
        title = "Accounts",
        icon = Icons.Default.AccountBox
    )

    data object Categories : NavDestination(
        route = "categories",
        title = "Categories",
        icon = Icons.Default.Star
    )

    data object ImportExport : NavDestination(
        route = "import_export",
        title = "Backup & Data",
        icon = Icons.Default.Share
    )

    data object Debt : NavDestination(
        route = "debts",
        title = "Debts & Loans",
        icon = Icons.Default.AccountCircle
    )

    data object RepeatExpenses : NavDestination(
        route = "repeat_expenses",
        title = "Recurring Expenses",
        icon = Icons.Default.Refresh
    )

    data object TransactionDetail : NavDestination(
        route = "transaction_detail/{transactionId}",
        title = "Transaction Details",
        icon = Icons.Default.List
    ) {
        const val ARG_TRANSACTION_ID = "transactionId"
        fun createRoute(transactionId: String) = "transaction_detail/$transactionId"
    }

    companion object {
        val topLevelDestinations = listOf(
            TransactionList,
            Analysis,
            Accounts
        )

        fun fromRoute(route: String?): NavDestination {
            return when (route?.substringBefore("/")) {
                TransactionList.route -> TransactionList
                Analysis.route -> Analysis
                Accounts.route -> Accounts
                Categories.route -> Categories
                ImportExport.route -> ImportExport
                Debt.route -> Debt
                RepeatExpenses.route -> RepeatExpenses
                "transaction_detail" -> TransactionDetail
                else -> TransactionList
            }
        }
    }
}
