package com.rushi.mitavyay.ui.screens.Categories

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rushi.mitavyay.data.model.CategoryDisplayItem
import com.rushi.mitavyay.ui.components.AddCategoryDialog
import com.rushi.mitavyay.ui.components.AppAlertDialog
import com.rushi.mitavyay.ui.components.LoadingState
import com.rushi.mitavyay.ui.components.getCategoryIcon
import com.rushi.mitavyay.ui.components.pressScale
import com.rushi.mitavyay.ui.components.parseCategoryColor
import com.rushi.mitavyay.ui.theme.appShapes
import com.rushi.mitavyay.ui.theme.spacing

@Composable
fun CategoriesScreen(
    modifier: Modifier = Modifier,
    viewModel: CategoriesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    CategoriesContent(
        uiState = uiState,
        onAddClick = viewModel::onAddCategoryClick,
        onEditClick = viewModel::onEditCategoryClick,
        onDeleteClick = viewModel::onDeleteCategoryClick,
        onSaveCategory = viewModel::saveCategory,
        onConfirmDelete = viewModel::confirmDeleteCategory,
        onDismissDialog = viewModel::onDismissDialog,
        modifier = modifier
    )
}

@Composable
fun CategoriesContent(
    uiState: CategoriesUiState,
    onAddClick: () -> Unit,
    onEditClick: (CategoryDisplayItem) -> Unit,
    onDeleteClick: (CategoryDisplayItem) -> Unit,
    onSaveCategory: (name: String, colorHex: String, icon: String) -> Unit,
    onConfirmDelete: () -> Unit,
    onDismissDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddClick,
                modifier = Modifier.pressScale(),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Custom Category"
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.isLoading) {
                LoadingState(message = "Loading categories...")
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = MaterialTheme.spacing.md,
                        end = MaterialTheme.spacing.md,
                        top = MaterialTheme.spacing.md,
                        bottom = MaterialTheme.spacing.xxl + 48.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm)
                ) {
                    // 1. Custom Categories Header
                    item {
                        Text(
                            text = "Custom Categories",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = MaterialTheme.spacing.xs)
                        )
                    }

                    if (uiState.customCategories.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = MaterialTheme.appShapes.medium,
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                )
                            ) {
                                Text(
                                    text = "No custom categories yet. Tap '+' to create your own.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(MaterialTheme.spacing.md)
                                )
                            }
                        }
                    } else {
                        items(uiState.customCategories, key = { it.id }) { category ->
                            @OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
                            CategoryItemRow(
                                category = category,
                                onEditClick = { onEditClick(category) },
                                onDeleteClick = { onDeleteClick(category) },
                                modifier = Modifier.animateItemPlacement()
                            )
                        }
                    }

                    // 2. Default Categories Header
                    item {
                        Text(
                            text = "Default Categories",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = MaterialTheme.spacing.lg)
                        )
                    }

                    items(uiState.defaultCategories, key = { it.id }) { category ->
                        @OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
                        CategoryItemRow(
                            category = category,
                            onEditClick = null,
                            onDeleteClick = null,
                            modifier = Modifier.animateItemPlacement()
                        )
                    }
                }
            }
        }
    }

    // Add or Edit Dialog
    if (uiState.showAddDialog || uiState.editingCategory != null) {
        AddCategoryDialog(
            initialCategory = uiState.editingCategory,
            onDismiss = onDismissDialog,
            onSave = onSaveCategory,
            errorMessage = uiState.errorMessage
        )
    }

    // Delete Confirmation Dialog
    if (uiState.deletingCategory != null) {
        AppAlertDialog(
            onDismissRequest = onDismissDialog,
            title = "Delete Category?",
            text = "Are you sure you want to delete '${uiState.deletingCategory!!.name}'? Past transactions in this category will keep their label.",
            confirmText = "Delete",
            dismissText = "Cancel",
            onConfirm = onConfirmDelete,
            isDestructive = true
        )
    }
}

@Composable
fun CategoryItemRow(
    category: CategoryDisplayItem,
    onEditClick: (() -> Unit)?,
    onDeleteClick: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    val parsedColor = parseCategoryColor(category.colorHex, 0)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.appShapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = MaterialTheme.spacing.xs)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MaterialTheme.spacing.md, vertical = MaterialTheme.spacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(parsedColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getCategoryIcon(category.icon),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(MaterialTheme.spacing.md))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            if (category.isCustom) {
                if (onEditClick != null) {
                    IconButton(onClick = onEditClick) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Category",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                if (onDeleteClick != null) {
                    IconButton(onClick = onDeleteClick) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Category",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            } else {
                Surface(
                    shape = MaterialTheme.appShapes.small,
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = "Default",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}
