package com.rushi.mitavyay.ui.screens.Analysis

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rushi.mitavyay.data.model.AccountDisplayItem
import com.rushi.mitavyay.data.model.NeedWantData
import com.rushi.mitavyay.data.model.toDisplayItem
import com.rushi.mitavyay.data.repository.AccountRepository
import com.rushi.mitavyay.data.repository.AccountSpending
import com.rushi.mitavyay.data.repository.AnalysisPeriod
import com.rushi.mitavyay.data.repository.AnalysisSummary
import com.rushi.mitavyay.data.repository.CategoryRepository
import com.rushi.mitavyay.data.repository.CategorySpending
import com.rushi.mitavyay.data.repository.PeriodComparisonData
import com.rushi.mitavyay.data.repository.SpendingTrendInsight
import com.rushi.mitavyay.data.repository.TimeSpendingPoint
import com.rushi.mitavyay.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

enum class TrendChartType {
    LINE,
    BAR
}

data class CalculatedDateRange(
    val startTimestamp: Long,
    val endTimestamp: Long,
    val label: String
)

data class AnalysisUiState(
    val isLoading: Boolean = false,
    val selectedPeriod: AnalysisPeriod = AnalysisPeriod.MONTH,
    val periodOffset: Int = 0,
    val selectedAccountId: String? = null,
    val chartType: TrendChartType = TrendChartType.LINE,
    val dateRangeLabel: String = "",
    val summary: AnalysisSummary = AnalysisSummary(),
    val totalIncomePaise: Long = 0L,
    val totalExpensePaise: Long = 0L,
    val netSavingsPaise: Long = 0L,
    val categorySpendings: List<CategorySpending> = emptyList(),
    val accountSpendings: List<AccountSpending> = emptyList(),
    val timeTrendPoints: List<TimeSpendingPoint> = emptyList(),
    val needWantData: NeedWantData = NeedWantData(),
    val trendInsight: SpendingTrendInsight = SpendingTrendInsight(),
    val periodComparison: PeriodComparisonData? = null,
    val availableAccounts: List<AccountDisplayItem> = emptyList(),
    val isEmpty: Boolean = true
)

private data class BaseMetrics(
    val categories: List<CategorySpending>,
    val trend: List<TimeSpendingPoint>,
    val summary: AnalysisSummary,
    val needWant: NeedWantData
)

private data class FilterConfig(
    val period: AnalysisPeriod,
    val offset: Int,
    val accountId: String?,
    val chartType: TrendChartType
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AnalysisViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val accountRepository: AccountRepository? = null
) : ViewModel() {

    private val _selectedPeriod = MutableStateFlow(AnalysisPeriod.MONTH)
    private val _periodOffset = MutableStateFlow(0)
    private val _selectedAccountId = MutableStateFlow<String?>(null)
    private val _chartType = MutableStateFlow(TrendChartType.LINE)

    val uiState: StateFlow<AnalysisUiState> = combine(
        _selectedPeriod,
        _periodOffset,
        _selectedAccountId,
        _chartType
    ) { period, offset, accountId, chartType ->
        FilterConfig(period, offset, accountId, chartType)
    }.flatMapLatest { config ->
        val period = config.period
        val offset = config.offset
        val accountId = config.accountId
        val chartType = config.chartType

        val currentRange = calculateDateRange(period, offset)
        val previousRange = calculateDateRange(period, offset - 1)

        val accountsFlow = accountRepository?.getActiveAccounts() ?: flowOf(emptyList())

        val baseMetricsFlow = combine(
            transactionRepository.getCategorySpending(currentRange.startTimestamp, currentRange.endTimestamp, accountId),
            transactionRepository.getTimeSpendingTrend(currentRange.startTimestamp, currentRange.endTimestamp, period, accountId),
            transactionRepository.getAnalysisSummary(currentRange.startTimestamp, currentRange.endTimestamp, accountId),
            transactionRepository.getNeedWantSpending(currentRange.startTimestamp, currentRange.endTimestamp, accountId)
        ) { categories, trend, summary, needWant ->
            BaseMetrics(categories, trend, summary, needWant)
        }

        val extendedMetricsFlow = combine(
            transactionRepository.getAccountSpending(currentRange.startTimestamp, currentRange.endTimestamp),
            transactionRepository.getPeriodComparison(
                currentRange.startTimestamp,
                currentRange.endTimestamp,
                previousRange.startTimestamp,
                previousRange.endTimestamp,
                accountId
            ),
            accountsFlow
        ) { accountSpendings, comparison, accounts ->
            Triple(accountSpendings, comparison, accounts)
        }

        combine(baseMetricsFlow, extendedMetricsFlow) { base, ext ->
            val categories = base.categories
            val trend = base.trend
            val summary = base.summary
            val needWant = base.needWant

            val accountSpendings = ext.first
            val comparison = ext.second
            val accounts = ext.third

            val isEmpty = categories.isEmpty() && summary.totalIncomePaise == 0L && summary.totalExpensePaise == 0L

            // Calculate burn rate and run-rate forecast if in the current period (offset == 0)
            val now = System.currentTimeMillis()
            val cal = Calendar.getInstance().apply { timeInMillis = now }
            val (elapsedDays, totalDays) = when (period) {
                AnalysisPeriod.WEEK -> {
                    val dayOfWeek = (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7 + 1
                    dayOfWeek to 7
                }
                AnalysisPeriod.MONTH -> {
                    val dayOfMonth = cal.get(Calendar.DAY_OF_MONTH)
                    val maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
                    dayOfMonth to maxDays
                }
                AnalysisPeriod.YEAR -> {
                    val dayOfYear = cal.get(Calendar.DAY_OF_YEAR)
                    val maxDays = cal.getActualMaximum(Calendar.DAY_OF_YEAR)
                    dayOfYear to maxDays
                }
            }

            val dailyBurnRate = if (offset == 0 && elapsedDays > 0) {
                summary.totalExpensePaise / elapsedDays
            } else 0L

            val projectedTotal = if (offset == 0) {
                dailyBurnRate * totalDays
            } else 0L

            val hasComparison = comparison.previousTotalExpensePaise > 0L || comparison.currentTotalExpensePaise > 0L

            val trendInsight = SpendingTrendInsight(
                currentExpensePaise = summary.totalExpensePaise,
                previousExpensePaise = comparison.previousTotalExpensePaise,
                deltaExpensePaise = comparison.expenseDeltaPaise,
                percentageChange = comparison.expensePercentageChange,
                isIncreasing = comparison.isExpenseIncreasing,
                dailyBurnRatePaise = dailyBurnRate,
                projectedPeriodExpensePaise = projectedTotal,
                hasComparisonData = hasComparison
            )

            AnalysisUiState(
                isLoading = false,
                selectedPeriod = period,
                periodOffset = offset,
                selectedAccountId = accountId,
                chartType = chartType,
                dateRangeLabel = currentRange.label,
                summary = summary,
                totalIncomePaise = summary.totalIncomePaise,
                totalExpensePaise = summary.totalExpensePaise,
                netSavingsPaise = summary.netSavingsPaise,
                categorySpendings = categories,
                accountSpendings = accountSpendings,
                timeTrendPoints = trend,
                needWantData = needWant,
                trendInsight = trendInsight,
                periodComparison = comparison,
                availableAccounts = accounts.map { it.toDisplayItem() },
                isEmpty = isEmpty
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = AnalysisUiState(isLoading = false)
    )

    fun selectPeriod(period: AnalysisPeriod) {
        _selectedPeriod.value = period
        _periodOffset.value = 0
    }

    fun previousPeriod() {
        _periodOffset.value -= 1
    }

    fun nextPeriod() {
        if (_periodOffset.value < 0) {
            _periodOffset.value += 1
        }
    }

    fun resetPeriod() {
        _periodOffset.value = 0
    }

    fun selectAccount(accountId: String?) {
        _selectedAccountId.value = accountId
    }

    fun setChartType(type: TrendChartType) {
        _chartType.value = type
    }

    companion object {
        fun calculateDateRange(
            period: AnalysisPeriod,
            offset: Int,
            referenceTimeMs: Long = System.currentTimeMillis()
        ): CalculatedDateRange {
            val calendar = Calendar.getInstance().apply {
                timeInMillis = referenceTimeMs
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            return when (period) {
                AnalysisPeriod.WEEK -> {
                    calendar.firstDayOfWeek = Calendar.MONDAY
                    calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                    if (offset != 0) {
                        calendar.add(Calendar.WEEK_OF_YEAR, offset)
                    }
                    val start = calendar.timeInMillis
                    calendar.add(Calendar.DAY_OF_MONTH, 6)
                    calendar.set(Calendar.HOUR_OF_DAY, 23)
                    calendar.set(Calendar.MINUTE, 59)
                    calendar.set(Calendar.SECOND, 59)
                    calendar.set(Calendar.MILLISECOND, 999)
                    val end = calendar.timeInMillis

                    val df = SimpleDateFormat("dd MMM", Locale.getDefault())
                    val yf = SimpleDateFormat("yyyy", Locale.getDefault())
                    val label = if (offset == 0) {
                        "This Week (${df.format(Date(start))} - ${df.format(Date(end))})"
                    } else {
                        "${df.format(Date(start))} - ${df.format(Date(end))} ${yf.format(Date(end))}"
                    }
                    CalculatedDateRange(start, end, label)
                }

                AnalysisPeriod.MONTH -> {
                    calendar.set(Calendar.DAY_OF_MONTH, 1)
                    if (offset != 0) {
                        calendar.add(Calendar.MONTH, offset)
                    }
                    val start = calendar.timeInMillis
                    val maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
                    calendar.set(Calendar.DAY_OF_MONTH, maxDay)
                    calendar.set(Calendar.HOUR_OF_DAY, 23)
                    calendar.set(Calendar.MINUTE, 59)
                    calendar.set(Calendar.SECOND, 59)
                    calendar.set(Calendar.MILLISECOND, 999)
                    val end = calendar.timeInMillis

                    val mf = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
                    val label = if (offset == 0) {
                        "This Month (${mf.format(Date(start))})"
                    } else {
                        mf.format(Date(start))
                    }
                    CalculatedDateRange(start, end, label)
                }

                AnalysisPeriod.YEAR -> {
                    calendar.set(Calendar.DAY_OF_YEAR, 1)
                    if (offset != 0) {
                        calendar.add(Calendar.YEAR, offset)
                    }
                    val start = calendar.timeInMillis
                    val maxDay = calendar.getActualMaximum(Calendar.DAY_OF_YEAR)
                    calendar.set(Calendar.DAY_OF_YEAR, maxDay)
                    calendar.set(Calendar.HOUR_OF_DAY, 23)
                    calendar.set(Calendar.MINUTE, 59)
                    calendar.set(Calendar.SECOND, 59)
                    calendar.set(Calendar.MILLISECOND, 999)
                    val end = calendar.timeInMillis

                    val yf = SimpleDateFormat("yyyy", Locale.getDefault())
                    val label = if (offset == 0) {
                        "This Year (${yf.format(Date(start))})"
                    } else {
                        yf.format(Date(start))
                    }
                    CalculatedDateRange(start, end, label)
                }
            }
        }
    }
}
