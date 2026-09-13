package com.rushi.mitavyay.ui.screens.Insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rushi.mitavyay.data.repository.BasicInsightsData
import com.rushi.mitavyay.data.repository.TransactionRepository
import com.rushi.mitavyay.util.DateTimeFormatter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class InsightsUiState(
    val isLoading: Boolean = false,
    val selectedMonthYear: String = DateTimeFormatter.getCurrentMonthYear(),
    val isCurrentMonth: Boolean = true,
    val insights: BasicInsightsData = BasicInsightsData()
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class InsightsViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _selectedMonthYear = MutableStateFlow(DateTimeFormatter.getCurrentMonthYear())

    val uiState: StateFlow<InsightsUiState> = _selectedMonthYear.flatMapLatest { monthYear ->
        transactionRepository.getBasicInsights(monthYear).map { data ->
            InsightsUiState(
                isLoading = false,
                selectedMonthYear = monthYear,
                isCurrentMonth = monthYear == DateTimeFormatter.getCurrentMonthYear(),
                insights = data
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = InsightsUiState(
            isLoading = true,
            selectedMonthYear = DateTimeFormatter.getCurrentMonthYear(),
            isCurrentMonth = true,
            insights = BasicInsightsData()
        )
    )

    fun selectMonth(monthYear: String) {
        _selectedMonthYear.value = monthYear
    }

    fun previousMonth() {
        _selectedMonthYear.value = DateTimeFormatter.getAdjacentMonthYear(_selectedMonthYear.value, -1)
    }

    fun nextMonth() {
        _selectedMonthYear.value = DateTimeFormatter.getAdjacentMonthYear(_selectedMonthYear.value, 1)
    }

    fun resetToCurrentMonth() {
        _selectedMonthYear.value = DateTimeFormatter.getCurrentMonthYear()
    }
}
