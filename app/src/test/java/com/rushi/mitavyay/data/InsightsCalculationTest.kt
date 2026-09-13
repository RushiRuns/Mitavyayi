package com.rushi.mitavyay.data

import com.rushi.mitavyay.data.db.Category
import com.rushi.mitavyay.data.db.Transaction
import com.rushi.mitavyay.data.repository.BasicInsightsCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

class InsightsCalculationTest {

    private fun createTx(
        amount: Long,
        category: String = "Food",
        description: String = "Test",
        timestamp: Long = 1726200000000L
    ): Transaction = Transaction(
        id = UUID.randomUUID().toString(),
        amount = amount,
        category = category,
        description = description,
        timestamp = timestamp,
        accountId = "acc-1"
    )

    @Test
    fun testEmptyStateWhenNoData() {
        val insights = BasicInsightsCalculator.compute(
            monthYear = "2026-09",
            currentTxs = emptyList(),
            prevTxs = emptyList(),
            categories = emptyList(),
            currentDateProvider = { Pair("2026-09", 15) }
        )

        assertFalse(insights.hasData)
        assertEquals(0L, insights.totalSpentThisMonthPaise)
        assertEquals(0L, insights.totalSpentPreviousMonthPaise)
        assertEquals(0L, insights.averageDailySpendPaise)
        assertNull(insights.largestTransaction)
        assertNull(insights.mostUsedCategory)
        assertEquals(0, insights.mostUsedCategoryCount)
        assertEquals(0, insights.totalExpenseTransactionsCount)
        assertEquals(0L, insights.netSavingsPaise)
    }

    @Test
    fun testTotalSpentAndAverageDailySpendCalculations() {
        val txs = listOf(
            createTx(amount = -100000L, category = "Food", description = "Groceries"),
            createTx(amount = -50000L, category = "Travel", description = "Metro"),
            createTx(amount = -150000L, category = "Shopping", description = "Shoes"),
            createTx(amount = 500000L, category = "Salary", description = "Paycheck") // Income shouldn't count as spent
        )

        val insights = BasicInsightsCalculator.compute(
            monthYear = "2026-09",
            currentTxs = txs,
            prevTxs = emptyList(),
            categories = emptyList(),
            currentDateProvider = { Pair("2026-09", 15) }
        )

        assertTrue(insights.hasData)
        // Total spent should be ₹1000 + ₹500 + ₹1500 = ₹3000 (300000 paise)
        assertEquals(300000L, insights.totalSpentThisMonthPaise)
        assertEquals(3, insights.totalExpenseTransactionsCount)

        // 15 days elapsed: 300000 / 15 = 20000 paise/day (₹200/day)
        assertEquals(15, insights.daysElapsed)
        assertEquals(20000L, insights.averageDailySpendPaise)

        // Income & Net Savings
        assertEquals(500000L, insights.totalIncomeThisMonthPaise)
        assertEquals(200000L, insights.netSavingsPaise)
    }

    @Test
    fun testLargestTransactionIdentifiesHighestExpense() {
        val txSmall = createTx(amount = -20000L, category = "Snacks", description = "Tea")
        val txLargest = createTx(amount = -250000L, category = "Electronics", description = "Headphones")
        val txMedium = createTx(amount = -75000L, category = "Fuel", description = "Petrol")
        val txLargeIncome = createTx(amount = 1000000L, category = "Bonus", description = "Bonus")

        val txs = listOf(txSmall, txLargest, txMedium, txLargeIncome)

        val insights = BasicInsightsCalculator.compute(
            monthYear = "2026-09",
            currentTxs = txs,
            prevTxs = emptyList(),
            currentDateProvider = { Pair("2026-09", 15) }
        )

        assertNotNull(insights.largestTransaction)
        assertEquals(txLargest.id, insights.largestTransaction?.id)
        assertEquals(-250000L, insights.largestTransaction?.amount)
        assertEquals("Headphones", insights.largestTransaction?.description)
    }

    @Test
    fun testMostUsedCategoryIdentifiesMostFrequentCategory() {
        val categories = listOf(
            Category(id = "c1", name = "Food", color = "#FF5722"),
            Category(id = "c2", name = "Gadgets", color = "#2196F3")
        )

        val txs = listOf(
            createTx(amount = -10000L, category = "Food", description = "Breakfast"),
            createTx(amount = -20000L, category = "Food", description = "Lunch"),
            createTx(amount = -30000L, category = "Food", description = "Dinner"),
            createTx(amount = -500000L, category = "Gadgets", description = "Phone")
        )

        val insights = BasicInsightsCalculator.compute(
            monthYear = "2026-09",
            currentTxs = txs,
            prevTxs = emptyList(),
            categories = categories,
            currentDateProvider = { Pair("2026-09", 15) }
        )

        assertEquals("Food", insights.mostUsedCategory)
        assertEquals(3, insights.mostUsedCategoryCount)
        assertEquals(60000L, insights.mostUsedCategoryTotalPaise)
        assertEquals("#FF5722", insights.mostUsedCategoryColorHex)
    }

    @Test
    fun testPreviousMonthComparisonIncrease() {
        val currentTxs = listOf(
            createTx(amount = -300000L, category = "Bills")
        )
        val prevTxs = listOf(
            createTx(amount = -200000L, category = "Bills")
        )

        val insights = BasicInsightsCalculator.compute(
            monthYear = "2026-09",
            currentTxs = currentTxs,
            prevTxs = prevTxs,
            currentDateProvider = { Pair("2026-09", 15) }
        )

        assertEquals(300000L, insights.totalSpentThisMonthPaise)
        assertEquals(200000L, insights.totalSpentPreviousMonthPaise)
        assertEquals(100000L, insights.spendingDeltaPaise)
        assertTrue(insights.isSpendingIncreasing)
        assertEquals(50.0f, insights.spendingPercentageChange, 0.01f)
    }

    @Test
    fun testPreviousMonthComparisonDecrease() {
        val currentTxs = listOf(
            createTx(amount = -150000L, category = "Food")
        )
        val prevTxs = listOf(
            createTx(amount = -300000L, category = "Food")
        )

        val insights = BasicInsightsCalculator.compute(
            monthYear = "2026-09",
            currentTxs = currentTxs,
            prevTxs = prevTxs,
            currentDateProvider = { Pair("2026-09", 15) }
        )

        assertEquals(150000L, insights.totalSpentThisMonthPaise)
        assertEquals(300000L, insights.totalSpentPreviousMonthPaise)
        assertEquals(-150000L, insights.spendingDeltaPaise)
        assertFalse(insights.isSpendingIncreasing)
        assertEquals(-50.0f, insights.spendingPercentageChange, 0.01f)
    }

    @Test
    fun testDaysElapsedForPastCurrentAndFutureMonths() {
        // 1. Current month (September 2026 on day 10)
        val currentInsights = BasicInsightsCalculator.compute(
            monthYear = "2026-09",
            currentTxs = emptyList(),
            prevTxs = emptyList(),
            currentDateProvider = { Pair("2026-09", 10) }
        )
        assertEquals(10, currentInsights.daysElapsed)
        assertEquals(30, currentInsights.totalDaysInMonth)

        // 2. Past month (August 2026 has 31 days)
        val pastInsights = BasicInsightsCalculator.compute(
            monthYear = "2026-08",
            currentTxs = emptyList(),
            prevTxs = emptyList(),
            currentDateProvider = { Pair("2026-09", 10) }
        )
        assertEquals(31, pastInsights.daysElapsed)
        assertEquals(31, pastInsights.totalDaysInMonth)

        // 3. Future month (October 2026)
        val futureInsights = BasicInsightsCalculator.compute(
            monthYear = "2026-10",
            currentTxs = emptyList(),
            prevTxs = emptyList(),
            currentDateProvider = { Pair("2026-09", 10) }
        )
        assertEquals(1, futureInsights.daysElapsed)
        assertEquals(31, futureInsights.totalDaysInMonth)
    }
}
