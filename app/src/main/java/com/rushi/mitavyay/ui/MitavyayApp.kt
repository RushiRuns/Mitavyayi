package com.rushi.mitavyay.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.rushi.mitavyay.ui.components.MitavyayBottomBar
import com.rushi.mitavyay.ui.navigation.MitavyayNavHost

/**
 * Root scaffold Composable for Mitavyay.
 * Orchestrates TopAppBar, BottomNavBar, FloatingActionButton, and NavHost content.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MitavyayApp(
    appState: MitavyayAppState = rememberMitavyayAppState(),
    onQuickAddClick: () -> Unit = {}
) {
    val currentDestination = appState.currentDestination
    val currentRoute = appState.currentRoute

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = currentDestination.title,
                        style = MaterialTheme.typography.titleLarge
                    )
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
                    currentRoute = currentRoute,
                    onNavigateToDestination = { appState.navigateToTopLevelDestination(it) }
                )
            }
        },
        floatingActionButton = {
            if (appState.isTopLevelDestination) {
                FloatingActionButton(
                    onClick = onQuickAddClick,
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
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        )
    }
}
