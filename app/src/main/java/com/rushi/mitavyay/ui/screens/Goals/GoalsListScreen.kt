package com.rushi.mitavyay.ui.screens.Goals

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rushi.mitavyay.data.model.GoalStatus
import com.rushi.mitavyay.ui.components.AppAlertDialog
import com.rushi.mitavyay.ui.components.EmptyState
import com.rushi.mitavyay.ui.components.GoalCard
import com.rushi.mitavyay.ui.components.LoadingState
import com.rushi.mitavyay.ui.components.pressScale
import com.rushi.mitavyay.ui.components.SpacerSm
import com.rushi.mitavyay.ui.components.SpacerXs
import com.rushi.mitavyay.ui.theme.appShapes
import com.rushi.mitavyay.ui.theme.extendedColorScheme
import com.rushi.mitavyay.ui.theme.spacing
import com.rushi.mitavyay.util.CurrencyFormatter

/**
 * Screen displaying the user's savings goals with summary analytics, tabbed filtering,
 * real-time progress indicators, and goal management dialogs.
 */
@Composable
fun GoalsListScreen(
    modifier: Modifier = Modifier,
    viewModel: GoalViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(modifier = modifier.fillMaxSize()) {
        if (uiState.isLoading) {
            LoadingState(message = "Loading savings goals...")
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                // Summary Metrics Card
                GoalSummarySection(
                    totalTarget = uiState.totalTargetPaise,
                    totalSaved = uiState.totalSavedPaise,
                    overallProgress = uiState.overallProgress,
                    activeCount = uiState.activeCount,
                    achievedCount = uiState.achievedCount,
                    modifier = Modifier.padding(MaterialTheme.spacing.md)
                )

                // Tabs: All, In Progress, Achieved, Overdue
                val inProgressCount = uiState.allGoals.count { it.status == GoalStatus.ON_TRACK || it.status == GoalStatus.WARNING }
                val overdueCount = uiState.allGoals.count { it.status == GoalStatus.OVERDUE }

                ScrollableTabRow(
                    selectedTabIndex = uiState.selectedTab.ordinal,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary,
                    edgePadding = MaterialTheme.spacing.md
                ) {
                    Tab(
                        selected = uiState.selectedTab == GoalFilterTab.ALL,
                        onClick = { viewModel.selectTab(GoalFilterTab.ALL) },
                        text = {
                            Text(
                                text = "All (${uiState.allGoals.size})",
                                style = MaterialTheme.typography.titleSmall
                            )
                        }
                    )
                    Tab(
                        selected = uiState.selectedTab == GoalFilterTab.IN_PROGRESS,
                        onClick = { viewModel.selectTab(GoalFilterTab.IN_PROGRESS) },
                        text = {
                            Text(
                                text = "In Progress ($inProgressCount)",
                                style = MaterialTheme.typography.titleSmall
                            )
                        }
                    )
                    Tab(
                        selected = uiState.selectedTab == GoalFilterTab.ACHIEVED,
                        onClick = { viewModel.selectTab(GoalFilterTab.ACHIEVED) },
                        text = {
                            Text(
                                text = "Achieved (${uiState.achievedCount})",
                                style = MaterialTheme.typography.titleSmall
                            )
                        }
                    )
                    Tab(
                        selected = uiState.selectedTab == GoalFilterTab.OVERDUE,
                        onClick = { viewModel.selectTab(GoalFilterTab.OVERDUE) },
                        text = {
                            Text(
                                text = "Overdue ($overdueCount)",
                                style = MaterialTheme.typography.titleSmall
                            )
                        }
                    )
                }

                // Goals List / Empty State
                if (uiState.displayedGoals.isEmpty()) {
                    val emptyTitle = when (uiState.selectedTab) {
                        GoalFilterTab.ALL -> "No savings goals yet"
                        GoalFilterTab.IN_PROGRESS -> "No goals in progress"
                        GoalFilterTab.ACHIEVED -> "No achieved goals yet"
                        GoalFilterTab.OVERDUE -> "No overdue goals"
                    }
                    val emptyDesc = when (uiState.selectedTab) {
                        GoalFilterTab.ALL -> "Set financial targets and track your progress to stay motivated."
                        GoalFilterTab.IN_PROGRESS -> "Create a goal to start tracking your savings target."
                        GoalFilterTab.ACHIEVED -> "Keep saving! Achieved goals will appear here once targets are reached."
                        GoalFilterTab.OVERDUE -> "Great job! All your savings goals are currently on schedule."
                    }

                    EmptyState(
                        title = emptyTitle,
                        description = emptyDesc,
                        actionText = if (uiState.selectedTab != GoalFilterTab.OVERDUE) "Create Goal" else null,
                        onAction = { viewModel.onAddGoalClick() }
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(MaterialTheme.spacing.md),
                        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm)
                    ) {
                        items(
                            items = uiState.displayedGoals,
                            key = { it.id }
                        ) { goal ->
                            @OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
                            GoalCard(
                                item = goal,
                                onAddSavingsClick = { viewModel.onAddSavingsClick(goal) },
                                onDeleteClick = { viewModel.onDeleteClick(goal) },
                                modifier = Modifier.animateItemPlacement()
                            )
                        }
                    }
                }
            }
        }

        // Add Goal Floating Action Button
        FloatingActionButton(
            onClick = { viewModel.onAddGoalClick() },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            shape = MaterialTheme.appShapes.large,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(MaterialTheme.spacing.md)
                .pressScale()
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Create Savings Goal"
            )
        }
    }

    // Add Goal Dialog
    if (uiState.isAddDialogOpen) {
        AddGoalDialog(
            accounts = uiState.availableAccounts,
            categories = uiState.availableCategories,
            onDismiss = { viewModel.dismissDialogs() },
            onSave = { name, targetAmountPaise, deadlineMs, linkedAccountId, initialDepositPaise, category, notes ->
                viewModel.createGoal(
                    name = name,
                    targetAmountPaise = targetAmountPaise,
                    deadlineMs = deadlineMs,
                    linkedAccountId = linkedAccountId,
                    initialDepositPaise = initialDepositPaise,
                    category = category,
                    notes = notes
                )
            }
        )
    }

    // Add Savings Dialog
    uiState.goalForSavings?.let { goal ->
        AddGoalSavingsDialog(
            goal = goal,
            onDismiss = { viewModel.dismissDialogs() },
            onConfirm = { amountPaise ->
                viewModel.addSavings(goal.id, amountPaise)
            }
        )
    }

    // Delete Confirmation Dialog
    uiState.goalToDelete?.let { goal ->
        AppAlertDialog(
            onDismissRequest = { viewModel.dismissDialogs() },
            title = "Delete Savings Goal?",
            text = "Are you sure you want to delete '${goal.name}'?\n\nThis will remove the goal and its progress tracking. Account balances will remain unchanged.",
            confirmText = "Delete",
            dismissText = "Cancel",
            isDestructive = true,
            onConfirm = { viewModel.confirmDelete() }
        )
    }

    // Notice / Error Dialog
    uiState.errorMessage?.let { err ->
        AppAlertDialog(
            onDismissRequest = { viewModel.dismissDialogs() },
            title = "Notice",
            text = err,
            confirmText = "Dismiss",
            onConfirm = { viewModel.dismissDialogs() }
        )
    }
}

@Composable
private fun GoalSummarySection(
    totalTarget: Long,
    totalSaved: Long,
    overallProgress: Float,
    activeCount: Int,
    achievedCount: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.appShapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = MaterialTheme.spacing.xs)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(MaterialTheme.spacing.cardContent)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Total Saved",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    SpacerXs()
                    Text(
                        text = CurrencyFormatter.format(totalSaved),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.extendedColorScheme.success
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "Total Target",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    SpacerXs()
                    Text(
                        text = CurrencyFormatter.format(totalTarget),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            SpacerSm()

            // Progress bar
            LinearProgressIndicator(
                progress = { overallProgress.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(MaterialTheme.appShapes.full),
                color = MaterialTheme.extendedColorScheme.success,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            SpacerSm()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val percentageInt = (overallProgress * 100).toInt()
                Text(
                    text = "$percentageInt% of active targets saved",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = "$activeCount Active • $achievedCount Achieved",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
