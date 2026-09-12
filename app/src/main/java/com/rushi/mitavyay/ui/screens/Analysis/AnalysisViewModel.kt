package com.rushi.mitavyay.ui.screens.Analysis

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rushi.mitavyay.data.repository.AnalysisPeriod
import com.rushi.mitavyay.data.repository.AnalysisSummary
import com.rushi.mitavyay.data.repository.CategoryRepository
import com.rushi.mitavyay.data.repository.CategorySpending
import com.rushi.mitavyay.data.repository.TimeSpendingPoint
import com.rushi.mitavyay.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class CalculatedDateRange(
    val startTimestamp: Long,
    val endTimestamp: Long,
    val label: String
)

data class AnalysisUiState(
    val isLoading: Boolean = false,
    val selectedPeriod: AnalysisPeriod = AnalysisPeriod.MONTH,
    val periodOffset: Int = 0,
    val dateRangeLabel: String = "",
    val summary: AnalysisSummary = AnalysisSummary(),
    val totalIncomePaise: Long = 0L,
    val totalExpensePaise: Long = 0L,
    val netSavingsPaise: Long = 0L,
    val categorySpendings: List<CategorySpending> = emptyList(),
    val timeTrendPoints: List<TimeSpendingPoint> = emptyList(),
    val isEmpty: Boolean = true
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AnalysisViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _selectedPeriod = MutableStateFlow(AnalysisPeriod.MONTH)
    private val _periodOffset = MutableStateFlow(0)

    val uiState: StateFlow<AnalysisUiState> = combine(_selectedPeriod, _periodOffset, ::Pair)
        .flatMapLatest { (period, offset) ->
            val range = calculateDateRange(period, offset)
            combine(
                transactionRepository.getCategorySpending(range.startTimestamp, range.endTimestamp),
                transactionRepository.getTimeSpendingTrend(range.startTimestamp, range.endTimestamp, period),
                transactionRepository.getAnalysisSummary(range.startTimestamp, range.endTimestamp)
            ) { categories, trend, summary ->
                val isEmpty = categories.isEmpty() && summary.totalIncomePaise == 0L && summary.totalExpensePaise == 0L
                AnalysisUiState(
                    isLoading = false,
                    selectedPeriod = period,
                    periodOffset = offset,
                    dateRangeLabel = range.label,
                    summary = summary,
                    totalIncomePaise = summary.totalIncomePaise,
                    totalExpensePaise = summary.totalExpensePaise,
                    netSavingsPaise = summary.netSavingsPaise,
                    categorySpendings = categories,
                    timeTrendPoints = trend,
                    isEmpty = isEmpty
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AnalysisUiState(isLoading = true)
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
