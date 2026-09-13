package com.rushi.mitavyay.ui.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NavigationTest {

    @Test
    fun testDestinationRoutes() {
        assertEquals("transactions", NavDestination.TransactionList.route)
        assertEquals("analysis", NavDestination.Analysis.route)
        assertEquals("more", NavDestination.More.route)
        assertEquals("accounts", NavDestination.Accounts.route)
        assertEquals("settings", NavDestination.Settings.route)
        assertEquals("categories", NavDestination.Categories.route)
        assertEquals("import_export", NavDestination.ImportExport.route)
        assertEquals("debts", NavDestination.Debt.route)
        assertEquals("repeat_expenses", NavDestination.RepeatExpenses.route)
        assertEquals("goals", NavDestination.Goals.route)
        assertEquals("budgets", NavDestination.Budget.route)
        assertEquals("transaction_detail/{transactionId}", NavDestination.TransactionDetail.route)
        assertEquals("transaction_detail/tx_123", NavDestination.TransactionDetail.createRoute("tx_123"))
    }

    @Test
    fun testTopLevelDestinationsList() {
        val topLevel = NavDestination.topLevelDestinations
        assertEquals(3, topLevel.size)
        assertTrue(topLevel.contains(NavDestination.TransactionList))
        assertTrue(topLevel.contains(NavDestination.Analysis))
        assertTrue(topLevel.contains(NavDestination.More))
    }

    @Test
    fun testFromRoute() {
        assertEquals(NavDestination.TransactionList, NavDestination.fromRoute("transactions"))
        assertEquals(NavDestination.Analysis, NavDestination.fromRoute("analysis"))
        assertEquals(NavDestination.More, NavDestination.fromRoute("more"))
        assertEquals(NavDestination.Accounts, NavDestination.fromRoute("accounts"))
        assertEquals(NavDestination.Settings, NavDestination.fromRoute("settings"))
        assertEquals(NavDestination.Categories, NavDestination.fromRoute("categories"))
        assertEquals(NavDestination.ImportExport, NavDestination.fromRoute("import_export"))
        assertEquals(NavDestination.Debt, NavDestination.fromRoute("debts"))
        assertEquals(NavDestination.RepeatExpenses, NavDestination.fromRoute("repeat_expenses"))
        assertEquals(NavDestination.Goals, NavDestination.fromRoute("goals"))
        assertEquals(NavDestination.Budget, NavDestination.fromRoute("budgets"))
        assertEquals(NavDestination.TransactionDetail, NavDestination.fromRoute("transaction_detail/tx_123"))
        assertEquals(NavDestination.TransactionList, NavDestination.fromRoute("transactions/item_123"))
        assertEquals(NavDestination.TransactionList, NavDestination.fromRoute("unknown_route"))
        assertEquals(NavDestination.TransactionList, NavDestination.fromRoute(null))
    }
}
