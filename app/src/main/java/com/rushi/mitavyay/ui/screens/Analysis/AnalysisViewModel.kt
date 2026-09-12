package com.rushi.mitavyay.ui.screens.Analysis

import androidx.lifecycle.ViewModel
import com.rushi.mitavyay.data.repository.CategoryRepository
import com.rushi.mitavyay.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class AnalysisUiState(
    val isLoading: Boolean = false,
    val selectedPeriod: String = "MONTHLY",
    val totalIncomePaise: Long = 0L,
    val totalExpensePaise: Long = 0L,
    val netSavingsPaise: Long = 0L
)

@HiltViewModel
class AnalysisViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnalysisUiState())
    val uiState: StateFlow<AnalysisUiState> = _uiState.asStateFlow()
}
