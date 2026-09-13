package com.rushi.mitavyay.data.repository

import com.rushi.mitavyay.data.db.AccountDao
import com.rushi.mitavyay.data.db.Category
import com.rushi.mitavyay.data.db.CategoryDao
import com.rushi.mitavyay.data.db.DatabaseTransactionRunner
import com.rushi.mitavyay.data.db.Transaction
import com.rushi.mitavyay.data.db.TransactionDao
import com.rushi.mitavyay.util.DateTimeFormatter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

enum class AnalysisPeriod {
    WEEK,
    MONTH,
    YEAR
}

data class CategorySpending(
    val category: String,
    val totalExpensePaise: Long,
    val transactionCount: Int,
    val colorHex: String? = null,
    val percentage: Float = 0f
)

data class AccountSpending(
    val accountId: String,
    val accountName: String,
    val accountType: String,
    val totalExpensePaise: Long,
    val transactionCount: Int,
    val percentage: Float = 0f
)

data class TimeSpendingPoint(
    val label: String,
    val startTimestamp: Long,
    val endTimestamp: Long,
    val expensePaise: Long,
    val incomePaise: Long
)

data class AnalysisSummary(
    val totalIncomePaise: Long = 0L,
    val totalExpensePaise: Long = 0L,
    val netSavingsPaise: Long = 0L,
    val savingsRate: Float = 0f
)

data class CategoryComparison(
    val category: String,
    val currentExpensePaise: Long,
    val previousExpensePaise: Long,
    val deltaPaise: Long,
    val percentageChange: Float,
    val colorHex: String? = null
)

data class PeriodComparisonData(
    val currentTotalExpensePaise: Long = 0L,
    val previousTotalExpensePaise: Long = 0L,
    val currentTotalIncomePaise: Long = 0L,
    val previousTotalIncomePaise: Long = 0L,
    val expenseDeltaPaise: Long = 0L,
    val expensePercentageChange: Float = 0f,
    val isExpenseIncreasing: Boolean = false,
    val categoryMovers: List<CategoryComparison> = emptyList()
)

data class SpendingTrendInsight(
    val currentExpensePaise: Long = 0L,
    val previousExpensePaise: Long = 0L,
    val deltaExpensePaise: Long = 0L,
    val percentageChange: Float = 0f,
    val isIncreasing: Boolean = false,
    val dailyBurnRatePaise: Long = 0L,
    val projectedPeriodExpensePaise: Long = 0L,
    val hasComparisonData: Boolean = false
)

data class BasicInsightsData(
    val monthYear: String = "",
    val monthYearFormatted: String = "",
    val previousMonthYearFormatted: String = "",
    val totalSpentThisMonthPaise: Long = 0L,
    val totalSpentPreviousMonthPaise: Long = 0L,
    val spendingDeltaPaise: Long = 0L,
    val spendingPercentageChange: Float = 0f,
    val isSpendingIncreasing: Boolean = false,
    val averageDailySpendPaise: Long = 0L,
    val previousAverageDailySpendPaise: Long = 0L,
    val daysElapsed: Int = 1,
    val totalDaysInMonth: Int = 30,
    val largestTransaction: Transaction? = null,
    val mostUsedCategory: String? = null,
    val mostUsedCategoryCount: Int = 0,
    val mostUsedCategoryTotalPaise: Long = 0L,
    val mostUsedCategoryColorHex: String? = null,
    val totalExpenseTransactionsCount: Int = 0,
    val totalIncomeThisMonthPaise: Long = 0L,
    val netSavingsPaise: Long = 0L,
    val hasData: Boolean = false
)

interface TransactionRepository {
    fun getAllTransactions(): Flow<List<Transaction>>
    suspend fun getTransactionById(id: String): Transaction?
    suspend fun addTransaction(transaction: Transaction)
    suspend fun addTransactions(transactions: List<Transaction>) {
        transactions.forEach { addTransaction(it) }
    }
    suspend fun updateTransaction(transaction: Transaction)
    suspend fun deleteTransaction(id: String)
    fun getTransactionsByDateRange(start: Long, end: Long): Flow<List<Transaction>>
    fun getTransactionsByCategory(category: String): Flow<List<Transaction>>
    fun getTransactionsByAccount(accountId: String): Flow<List<Transaction>>
    fun searchTransactions(query: String): Flow<List<Transaction>> = flowOf(emptyList())
    fun getTotalExpensesByDateRange(start: Long, end: Long): Flow<Long?>
    fun getTotalIncomeByDateRange(start: Long, end: Long): Flow<Long?>

    // Analysis Aggregations
    fun getCategorySpending(start: Long, end: Long): Flow<List<CategorySpending>> = getCategorySpending(start, end, null)
    fun getCategorySpending(start: Long, end: Long, accountId: String?): Flow<List<CategorySpending>> = flowOf(emptyList())

    fun getTimeSpendingTrend(start: Long, end: Long, period: AnalysisPeriod): Flow<List<TimeSpendingPoint>> = getTimeSpendingTrend(start, end, period, null)
    fun getTimeSpendingTrend(start: Long, end: Long, period: AnalysisPeriod, accountId: String?): Flow<List<TimeSpendingPoint>> = flowOf(emptyList())

    fun getAnalysisSummary(start: Long, end: Long): Flow<AnalysisSummary> = getAnalysisSummary(start, end, null)
    fun getAnalysisSummary(start: Long, end: Long, accountId: String?): Flow<AnalysisSummary> = flowOf(AnalysisSummary())

    fun getAccountSpending(start: Long, end: Long): Flow<List<AccountSpending>> = flowOf(emptyList())
    fun getPeriodComparison(currentStart: Long, currentEnd: Long, previousStart: Long, previousEnd: Long, accountId: String? = null): Flow<PeriodComparisonData> = flowOf(PeriodComparisonData())

    // Feature 4.9: Insights
    fun getBasicInsights(monthYear: String = ""): Flow<BasicInsightsData> = flowOf(BasicInsightsData())
}

@Singleton
class TransactionRepositoryImpl @Inject constructor(
    private val transactionDao: TransactionDao,
    private val accountDao: AccountDao? = null,
    private val transactionRunner: DatabaseTransactionRunner? = null,
    private val categoryDao: CategoryDao? = null
) : TransactionRepository {

    override fun getAllTransactions(): Flow<List<Transaction>> = transactionDao.getAll()

    override suspend fun getTransactionById(id: String): Transaction? = transactionDao.getById(id)

    override suspend fun addTransaction(transaction: Transaction) {
        val action: suspend () -> Unit = {
            transactionDao.insert(transaction)
            accountDao?.let { dao ->
                val account = dao.getById(transaction.accountId)
                if (account != null) {
                    dao.updateBalance(account.id, account.balance + transaction.amount)
                }
            }
        }

        if (transactionRunner != null) {
            transactionRunner { action() }
        } else {
            action()
        }
    }

    override suspend fun addTransactions(transactions: List<Transaction>) {
        if (transactions.isEmpty()) return
        val action: suspend () -> Unit = {
            transactionDao.insertAll(transactions)
            if (accountDao != null) {
                val accountDeltas = transactions.groupBy { it.accountId }
                    .mapValues { (_, txList) -> txList.sumOf { it.amount } }
                for ((accId, netDelta) in accountDeltas) {
                    val account = accountDao.getById(accId)
                    if (account != null) {
                        accountDao.updateBalance(accId, account.balance + netDelta)
                    }
                }
            }
        }

        if (transactionRunner != null) {
            transactionRunner { action() }
        } else {
            action()
        }
    }

    override suspend fun updateTransaction(transaction: Transaction) {
        val action: suspend () -> Unit = {
            val oldTx = transactionDao.getById(transaction.id)
            transactionDao.update(transaction)
            if (oldTx != null && accountDao != null) {
                if (oldTx.accountId == transaction.accountId) {
                    val delta = transaction.amount - oldTx.amount
                    val account = accountDao.getById(transaction.accountId)
                    if (account != null) {
                        accountDao.updateBalance(account.id, account.balance + delta)
                    }
                } else {
                    val oldAccount = accountDao.getById(oldTx.accountId)
                    if (oldAccount != null) {
                        accountDao.updateBalance(oldAccount.id, oldAccount.balance - oldTx.amount)
                    }
                    val newAccount = accountDao.getById(transaction.accountId)
                    if (newAccount != null) {
                        accountDao.updateBalance(newAccount.id, newAccount.balance + transaction.amount)
                    }
                }
            }
        }

        if (transactionRunner != null) {
            transactionRunner { action() }
        } else {
            action()
        }
    }

    override suspend fun deleteTransaction(id: String) {
        val action: suspend () -> Unit = {
            val oldTx = transactionDao.getById(id)
            if (oldTx != null) {
                accountDao?.let { dao ->
                    val account = dao.getById(oldTx.accountId)
                    if (account != null) {
                        dao.updateBalance(account.id, account.balance - oldTx.amount)
                    }
                }
                transactionDao.deleteById(id)
            }
        }

        if (transactionRunner != null) {
            transactionRunner { action() }
        } else {
            action()
        }
    }

    override fun getTransactionsByDateRange(start: Long, end: Long): Flow<List<Transaction>> =
        transactionDao.getByDateRange(start, end)

    override fun getTransactionsByCategory(category: String): Flow<List<Transaction>> =
        transactionDao.getByCategory(category)

    override fun getTransactionsByAccount(accountId: String): Flow<List<Transaction>> =
        transactionDao.getByAccount(accountId)

    override fun searchTransactions(query: String): Flow<List<Transaction>> {
        val trimmed = query.trim()
        return if (trimmed.isBlank()) {
            transactionDao.getAll()
        } else {
            transactionDao.search(trimmed)
        }
    }

    override fun getTotalExpensesByDateRange(start: Long, end: Long): Flow<Long?> =
        transactionDao.getTotalExpensesByDateRange(start, end)

    override fun getTotalIncomeByDateRange(start: Long, end: Long): Flow<Long?> =
        transactionDao.getTotalIncomeByDateRange(start, end)

    override fun getCategorySpending(
        start: Long,
        end: Long,
        accountId: String?
    ): Flow<List<CategorySpending>> =
        if (accountId == null) {
            combine(
                transactionDao.getCategoryExpensesByDateRange(start, end),
                categoryDao?.getAll() ?: flowOf(emptyList())
            ) { rawList, categories ->
                val total = rawList.sumOf { it.totalExpensePaise }
                val colorMap = categories.associate { it.name to it.color }
                rawList.map { raw ->
                    val pct = if (total > 0L) {
                        (raw.totalExpensePaise.toFloat() / total.toFloat()) * 100f
                    } else 0f
                    CategorySpending(
                        category = raw.category,
                        totalExpensePaise = raw.totalExpensePaise,
                        transactionCount = raw.transactionCount,
                        colorHex = colorMap[raw.category],
                        percentage = pct
                    )
                }
            }
        } else {
            combine(
                transactionDao.getByDateRange(start, end),
                categoryDao?.getAll() ?: flowOf(emptyList())
            ) { txs, categories ->
                val filtered = txs.filter { it.accountId == accountId && it.amount < 0 }
                val total = filtered.sumOf { abs(it.amount) }
                val colorMap = categories.associate { it.name to it.color }
                filtered.groupBy { it.category }
                    .map { (cat, catTxs) ->
                        val catTotal = catTxs.sumOf { abs(it.amount) }
                        val pct = if (total > 0L) {
                            (catTotal.toFloat() / total.toFloat()) * 100f
                        } else 0f
                        CategorySpending(
                            category = cat,
                            totalExpensePaise = catTotal,
                            transactionCount = catTxs.size,
                            colorHex = colorMap[cat],
                            percentage = pct
                        )
                    }
                    .sortedByDescending { it.totalExpensePaise }
            }
        }

    override fun getTimeSpendingTrend(
        start: Long,
        end: Long,
        period: AnalysisPeriod,
        accountId: String?
    ): Flow<List<TimeSpendingPoint>> =
        transactionDao.getByDateRange(start, end).map { transactions ->
            val filtered = if (accountId != null) transactions.filter { it.accountId == accountId } else transactions
            aggregateTimeSpending(filtered, start, end, period)
        }

    override fun getAnalysisSummary(
        start: Long,
        end: Long,
        accountId: String?
    ): Flow<AnalysisSummary> =
        if (accountId == null) {
            combine(
                transactionDao.getTotalIncomeByDateRange(start, end),
                transactionDao.getTotalExpensesByDateRange(start, end)
            ) { income, expense ->
                val totalIncome = income ?: 0L
                val totalExpense = abs(expense ?: 0L)
                val netSavings = totalIncome - totalExpense
                val savingsRate = if (totalIncome > 0L) {
                    (netSavings.toFloat() / totalIncome.toFloat()).coerceIn(0f, 1f)
                } else 0f
                AnalysisSummary(
                    totalIncomePaise = totalIncome,
                    totalExpensePaise = totalExpense,
                    netSavingsPaise = netSavings,
                    savingsRate = savingsRate
                )
            }
        } else {
            transactionDao.getByDateRange(start, end).map { txs ->
                val filtered = txs.filter { it.accountId == accountId }
                val totalIncome = filtered.filter { it.amount > 0 }.sumOf { it.amount }
                val totalExpense = filtered.filter { it.amount < 0 }.sumOf { abs(it.amount) }
                val netSavings = totalIncome - totalExpense
                val savingsRate = if (totalIncome > 0L) {
                    (netSavings.toFloat() / totalIncome.toFloat()).coerceIn(0f, 1f)
                } else 0f
                AnalysisSummary(
                    totalIncomePaise = totalIncome,
                    totalExpensePaise = totalExpense,
                    netSavingsPaise = netSavings,
                    savingsRate = savingsRate
                )
            }
        }

    override fun getAccountSpending(start: Long, end: Long): Flow<List<AccountSpending>> =
        combine(
            transactionDao.getByDateRange(start, end),
            accountDao?.getAll() ?: flowOf(emptyList())
        ) { txs, accounts ->
            val accountMap = accounts.associateBy { it.id }
            val expenseTxs = txs.filter { it.amount < 0 }
            val totalExpense = expenseTxs.sumOf { abs(it.amount) }

            expenseTxs.groupBy { it.accountId }
                .map { (accId, accTxs) ->
                    val accTotal = accTxs.sumOf { abs(it.amount) }
                    val acc = accountMap[accId]
                    val pct = if (totalExpense > 0L) {
                        (accTotal.toFloat() / totalExpense.toFloat()) * 100f
                    } else 0f
                    AccountSpending(
                        accountId = accId,
                        accountName = acc?.name ?: "Unknown Account",
                        accountType = acc?.type ?: "general",
                        totalExpensePaise = accTotal,
                        transactionCount = accTxs.size,
                        percentage = pct
                    )
                }
                .sortedByDescending { it.totalExpensePaise }
        }

    override fun getPeriodComparison(
        currentStart: Long,
        currentEnd: Long,
        previousStart: Long,
        previousEnd: Long,
        accountId: String?
    ): Flow<PeriodComparisonData> =
        combine(
            getAnalysisSummary(currentStart, currentEnd, accountId),
            getAnalysisSummary(previousStart, previousEnd, accountId),
            getCategorySpending(currentStart, currentEnd, accountId),
            getCategorySpending(previousStart, previousEnd, accountId)
        ) { currentSummary, prevSummary, currentCategories, prevCategories ->
            val deltaExpense = currentSummary.totalExpensePaise - prevSummary.totalExpensePaise
            val pctChange = if (prevSummary.totalExpensePaise > 0L) {
                ((deltaExpense.toFloat() / prevSummary.totalExpensePaise.toFloat()) * 100f)
            } else if (currentSummary.totalExpensePaise > 0L) {
                100f
            } else 0f

            val prevCatMap = prevCategories.associateBy { it.category }
            val currentCatMap = currentCategories.associateBy { it.category }
            val allCategoryNames = (currentCatMap.keys + prevCatMap.keys).toSet()

            val movers = allCategoryNames.map { cat ->
                val curVal = currentCatMap[cat]?.totalExpensePaise ?: 0L
                val prevVal = prevCatMap[cat]?.totalExpensePaise ?: 0L
                val catDelta = curVal - prevVal
                val catPct = if (prevVal > 0L) {
                    ((catDelta.toFloat() / prevVal.toFloat()) * 100f)
                } else if (curVal > 0L) 100f else 0f
                val color = currentCatMap[cat]?.colorHex ?: prevCatMap[cat]?.colorHex

                CategoryComparison(
                    category = cat,
                    currentExpensePaise = curVal,
                    previousExpensePaise = prevVal,
                    deltaPaise = catDelta,
                    percentageChange = catPct,
                    colorHex = color
                )
            }.sortedByDescending { abs(it.deltaPaise) }

            PeriodComparisonData(
                currentTotalExpensePaise = currentSummary.totalExpensePaise,
                previousTotalExpensePaise = prevSummary.totalExpensePaise,
                currentTotalIncomePaise = currentSummary.totalIncomePaise,
                previousTotalIncomePaise = prevSummary.totalIncomePaise,
                expenseDeltaPaise = deltaExpense,
                expensePercentageChange = pctChange,
                isExpenseIncreasing = deltaExpense > 0L,
                categoryMovers = movers
            )
        }

    private fun aggregateTimeSpending(
        transactions: List<Transaction>,
        start: Long,
        end: Long,
        period: AnalysisPeriod
    ): List<TimeSpendingPoint> {
        if (start > end) return emptyList()

        val points = mutableListOf<TimeSpendingPoint>()
        val calendar = Calendar.getInstance().apply {
            timeInMillis = start
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        when (period) {
            AnalysisPeriod.WEEK -> {
                val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())
                while (calendar.timeInMillis <= end) {
                    val dayStart = calendar.timeInMillis
                    val label = dayFormat.format(Date(dayStart))
                    calendar.add(Calendar.DAY_OF_MONTH, 1)
                    val dayEnd = calendar.timeInMillis - 1

                    val txs = transactions.filter { it.timestamp in dayStart..dayEnd }
                    val expense = txs.filter { it.amount < 0 }.sumOf { abs(it.amount) }
                    val income = txs.filter { it.amount > 0 }.sumOf { it.amount }

                    points.add(TimeSpendingPoint(label, dayStart, dayEnd, expense, income))
                }
            }

            AnalysisPeriod.MONTH -> {
                val dayFormat = SimpleDateFormat("d", Locale.getDefault())
                while (calendar.timeInMillis <= end) {
                    val dayStart = calendar.timeInMillis
                    val label = dayFormat.format(Date(dayStart))
                    calendar.add(Calendar.DAY_OF_MONTH, 1)
                    val dayEnd = calendar.timeInMillis - 1

                    val txs = transactions.filter { it.timestamp in dayStart..dayEnd }
                    val expense = txs.filter { it.amount < 0 }.sumOf { abs(it.amount) }
                    val income = txs.filter { it.amount > 0 }.sumOf { it.amount }

                    points.add(TimeSpendingPoint(label, dayStart, dayEnd, expense, income))
                }
            }

            AnalysisPeriod.YEAR -> {
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                val monthFormat = SimpleDateFormat("MMM", Locale.getDefault())
                while (calendar.timeInMillis <= end) {
                    val monthStart = calendar.timeInMillis
                    val label = monthFormat.format(Date(monthStart))
                    calendar.add(Calendar.MONTH, 1)
                    val monthEnd = calendar.timeInMillis - 1

                    val txs = transactions.filter { it.timestamp in monthStart..monthEnd }
                    val expense = txs.filter { it.amount < 0 }.sumOf { abs(it.amount) }
                    val income = txs.filter { it.amount > 0 }.sumOf { it.amount }

                    points.add(TimeSpendingPoint(label, monthStart, monthEnd, expense, income))
                }
            }
        }

        return points
    }

    override fun getBasicInsights(monthYear: String): Flow<BasicInsightsData> {
        val targetMonthYear = if (monthYear.isBlank()) DateTimeFormatter.getCurrentMonthYear() else monthYear
        val (curStart, curEnd) = DateTimeFormatter.getMonthStartAndEndTimestamps(targetMonthYear)
        val prevMonthYear = DateTimeFormatter.getAdjacentMonthYear(targetMonthYear, -1)
        val (prevStart, prevEnd) = DateTimeFormatter.getMonthStartAndEndTimestamps(prevMonthYear)

        val categoriesFlow = categoryDao?.getAll() ?: flowOf(emptyList())

        return combine(
            transactionDao.getByDateRange(curStart, curEnd),
            transactionDao.getByDateRange(prevStart, prevEnd),
            categoriesFlow
        ) { currentTxs, prevTxs, categories ->
            BasicInsightsCalculator.compute(
                monthYear = targetMonthYear,
                currentTxs = currentTxs,
                prevTxs = prevTxs,
                categories = categories
            )
        }
    }
}

object BasicInsightsCalculator {
    fun compute(
        monthYear: String,
        currentTxs: List<Transaction>,
        prevTxs: List<Transaction>,
        categories: List<Category> = emptyList(),
        currentDateProvider: () -> Pair<String, Int> = {
            val cal = Calendar.getInstance()
            Pair(
                DateTimeFormatter.getCurrentMonthYear(),
                cal.get(Calendar.DAY_OF_MONTH)
            )
        }
    ): BasicInsightsData {
        val parts = monthYear.split("-")
        val year = parts.getOrNull(0)?.toIntOrNull() ?: 2026
        val month = parts.getOrNull(1)?.toIntOrNull() ?: 1
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month - 1)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        val totalDaysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

        val prevMonthYear = DateTimeFormatter.getAdjacentMonthYear(monthYear, -1)
        val prevParts = prevMonthYear.split("-")
        val prevYear = prevParts.getOrNull(0)?.toIntOrNull() ?: year
        val prevMonth = prevParts.getOrNull(1)?.toIntOrNull() ?: (if (month == 1) 12 else month - 1)
        val prevCal = Calendar.getInstance().apply {
            set(Calendar.YEAR, prevYear)
            set(Calendar.MONTH, prevMonth - 1)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        val prevTotalDaysInMonth = prevCal.getActualMaximum(Calendar.DAY_OF_MONTH)

        val (nowMonthYear, nowDayOfMonth) = currentDateProvider()
        val daysElapsed = when {
            monthYear == nowMonthYear -> nowDayOfMonth.coerceIn(1, totalDaysInMonth)
            monthYear < nowMonthYear -> totalDaysInMonth
            else -> 1 // Future month
        }

        val prevDaysElapsed = when {
            prevMonthYear == nowMonthYear -> nowDayOfMonth.coerceIn(1, prevTotalDaysInMonth)
            prevMonthYear < nowMonthYear -> prevTotalDaysInMonth
            else -> 1
        }

        val currentExpenses = currentTxs.filter { it.amount < 0 }
        val currentIncomes = currentTxs.filter { it.amount > 0 }
        val totalSpentThisMonthPaise = currentExpenses.sumOf { abs(it.amount) }
        val totalIncomeThisMonthPaise = currentIncomes.sumOf { it.amount }
        val netSavingsPaise = totalIncomeThisMonthPaise - totalSpentThisMonthPaise
        val totalExpenseTransactionsCount = currentExpenses.size
        val hasData = currentTxs.isNotEmpty()

        val prevExpenses = prevTxs.filter { it.amount < 0 }
        val totalSpentPreviousMonthPaise = prevExpenses.sumOf { abs(it.amount) }

        val spendingDeltaPaise = totalSpentThisMonthPaise - totalSpentPreviousMonthPaise
        val spendingPercentageChange = if (totalSpentPreviousMonthPaise > 0L) {
            ((spendingDeltaPaise.toFloat() / totalSpentPreviousMonthPaise.toFloat()) * 100f)
        } else if (totalSpentThisMonthPaise > 0L) {
            100f
        } else {
            0f
        }
        val isSpendingIncreasing = spendingDeltaPaise > 0L

        val averageDailySpendPaise = if (daysElapsed > 0) totalSpentThisMonthPaise / daysElapsed else 0L
        val previousAverageDailySpendPaise = if (prevDaysElapsed > 0) totalSpentPreviousMonthPaise / prevDaysElapsed else 0L

        val largestTransaction = currentExpenses.maxByOrNull { abs(it.amount) }

        val categoryColorMap = categories.associate { it.name.lowercase(Locale.ROOT) to it.color }
        val categoryGroups = currentExpenses.groupBy { it.category }
        val mostUsedEntry = categoryGroups.maxWithOrNull(
            compareBy({ it.value.size }, { it.value.sumOf { tx -> abs(tx.amount) } })
        )
        val mostUsedCategory = mostUsedEntry?.key
        val mostUsedCategoryCount = mostUsedEntry?.value?.size ?: 0
        val mostUsedCategoryTotalPaise = mostUsedEntry?.value?.sumOf { abs(it.amount) } ?: 0L
        val mostUsedCategoryColorHex = mostUsedCategory?.let { categoryColorMap[it.lowercase(Locale.ROOT)] }

        return BasicInsightsData(
            monthYear = monthYear,
            monthYearFormatted = DateTimeFormatter.formatMonthYear(monthYear),
            previousMonthYearFormatted = DateTimeFormatter.formatMonthYear(prevMonthYear),
            totalSpentThisMonthPaise = totalSpentThisMonthPaise,
            totalSpentPreviousMonthPaise = totalSpentPreviousMonthPaise,
            spendingDeltaPaise = spendingDeltaPaise,
            spendingPercentageChange = spendingPercentageChange,
            isSpendingIncreasing = isSpendingIncreasing,
            averageDailySpendPaise = averageDailySpendPaise,
            previousAverageDailySpendPaise = previousAverageDailySpendPaise,
            daysElapsed = daysElapsed,
            totalDaysInMonth = totalDaysInMonth,
            largestTransaction = largestTransaction,
            mostUsedCategory = mostUsedCategory,
            mostUsedCategoryCount = mostUsedCategoryCount,
            mostUsedCategoryTotalPaise = mostUsedCategoryTotalPaise,
            mostUsedCategoryColorHex = mostUsedCategoryColorHex,
            totalExpenseTransactionsCount = totalExpenseTransactionsCount,
            totalIncomeThisMonthPaise = totalIncomeThisMonthPaise,
            netSavingsPaise = netSavingsPaise,
            hasData = hasData
        )
    }
}
