package com.rushi.mitavyay.ui.screens.Analysis

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.rushi.mitavyay.ui.components.EmptyState
import com.rushi.mitavyay.ui.components.LoadingState

@Composable
fun AnalysisScreen(
    modifier: Modifier = Modifier,
    viewModel: AnalysisViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    AnalysisContent(
        uiState = uiState,
        modifier = modifier
    )
}

@Composable
fun AnalysisContent(
    uiState: AnalysisUiState,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        if (uiState.isLoading) {
            LoadingState(message = "Analyzing your spending...")
        } else {
            EmptyState(
                title = "Analysis & Insights",
                description = "Visual charts, spending trends, and breakdown reports will appear here as you log transactions."
            )
        }
    }
}
