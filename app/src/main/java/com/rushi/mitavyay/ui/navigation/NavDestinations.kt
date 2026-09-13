package com.rushi.mitavyay.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingCart
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

    data object More : NavDestination(
        route = "more",
        title = "More",
        icon = Icons.Default.Menu
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

    data object Goals : NavDestination(
        route = "goals",
        title = "Savings Goals",
        icon = Icons.Default.Favorite
    )

    data object Budget : NavDestination(
        route = "budgets",
        title = "Budget Planning",
        icon = Icons.Default.ShoppingCart
    )

    data object Insights : NavDestination(
        route = "insights",
        title = "Insights",
        icon = Icons.Default.Info
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
            More
        )

        fun isTopLevel(route: String?): Boolean =
            topLevelDestinations.any { it.route == route }

        fun fromRoute(route: String?): NavDestination {
            return when (route?.substringBefore("/")) {
                TransactionList.route -> TransactionList
                Analysis.route -> Analysis
                More.route -> More
                Accounts.route -> Accounts
                Categories.route -> Categories
                ImportExport.route -> ImportExport
                Debt.route -> Debt
                RepeatExpenses.route -> RepeatExpenses
                Goals.route -> Goals
                Budget.route -> Budget
                Insights.route -> Insights
                "transaction_detail" -> TransactionDetail
                else -> TransactionList
            }
        }
    }
}
