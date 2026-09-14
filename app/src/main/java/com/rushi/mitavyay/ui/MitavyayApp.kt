package com.rushi.mitavyay.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.rushi.mitavyay.ui.components.MitavyayBottomBar
import androidx.compose.ui.platform.LocalContext
import com.rushi.mitavyay.ui.components.pressScale
import com.rushi.mitavyay.ui.navigation.MitavyayNavHost
import com.rushi.mitavyay.ui.navigation.NavDestination
import com.rushi.mitavyay.ui.screens.BatchAdd.BatchAddTransactionsDialog
import com.rushi.mitavyay.ui.screens.QuickAddExpense.QuickAddExpenseSheet
import com.rushi.mitavyay.util.hapticLight

/**
 * Root scaffold Composable for Mitavyay.
 * Orchestrates TopAppBar, BottomNavBar, FloatingActionButton, and NavHost content.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MitavyayApp(
    appState: MitavyayAppState = rememberMitavyayAppState(),
    onQuickAddClick: (() -> Unit)? = null,
    mainViewModel: MainViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var showQuickAddSheet by remember { mutableStateOf(false) }
    var showBatchAddDialog by remember { mutableStateOf(false) }
    val currentDestination = appState.currentDestination
    val showFab = appState.isAtRoot && NavDestination.shouldShowQuickAddFab(appState.selectedTab.route)
    val currentThemeMode by mainViewModel.themeMode.collectAsState()
    val hapticFeedbackEnabled by mainViewModel.hapticFeedbackEnabled.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    AnimatedContent(
                        targetState = currentDestination.title,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing)) togetherWith
                                    fadeOut(animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing))
                        },
                        label = "TopAppBarTitleCrossfade"
                    ) { title ->
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                },
                navigationIcon = {
                    if (!appState.isTopLevelDestination) {
                        IconButton(onClick = { appState.navigateBack() }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                },
                actions = {
                    // Feature shortcuts moved into 'More' Hub screen
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            if (appState.isTopLevelDestination) {
                MitavyayBottomBar(
                    currentRoute = appState.selectedTab.route,
                    onNavigateToDestination = { appState.selectTab(it) }
                )
            }
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = showFab,
                enter = scaleIn(animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing)) +
                        fadeIn(animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing)),
                exit = scaleOut(animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing)) +
                        fadeOut(animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing))
            ) {
                FloatingActionButton(
                    onClick = {
                        context.hapticLight(hapticFeedbackEnabled)
                        if (onQuickAddClick != null) {
                            onQuickAddClick()
                        } else {
                            showQuickAddSheet = true
                        }
                    },
                    modifier = Modifier.pressScale(),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = MaterialTheme.shapes.large
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Quick Add"
                    )
                }
            }
        }
    ) { paddingValues ->
        MitavyayNavHost(
            navController = appState.navController,
            appState = appState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        )
    }

    if (showQuickAddSheet) {
        QuickAddExpenseSheet(
            onDismiss = { showQuickAddSheet = false },
            onBatchAddClick = {
                showQuickAddSheet = false
                showBatchAddDialog = true
            },
            hapticFeedbackEnabled = hapticFeedbackEnabled
        )
    }

    if (showBatchAddDialog) {
        BatchAddTransactionsDialog(
            onDismiss = { showBatchAddDialog = false }
        )
    }
}