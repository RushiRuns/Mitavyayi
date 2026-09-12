package com.rushi.mitavyay.ui.screens.Budget

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.rushi.mitavyay.data.model.BudgetDisplayItem
import com.rushi.mitavyay.data.model.CategoryDisplayItem
import com.rushi.mitavyay.ui.components.CurrencyInput
import com.rushi.mitavyay.ui.components.PrimaryButton
import com.rushi.mitavyay.ui.components.SecondaryButton
import com.rushi.mitavyay.ui.components.getCategoryIcon
import com.rushi.mitavyay.ui.components.parseCategoryColor
import com.rushi.mitavyay.ui.theme.appShapes
import com.rushi.mitavyay.ui.theme.spacing

/**
 * Modal dialog for creating or modifying a monthly category budget.
 */
@Composable
fun SetBudgetDialog(
    availableCategories: List<CategoryDisplayItem>,
    initialBudget: BudgetDisplayItem? = null,
    onDismiss: () -> Unit,
    onSave: (category: String, amountPaise: Long) -> Unit
) {
    val isEdit = initialBudget != null
    var selectedCategory by remember {
        mutableStateOf(initialBudget?.category ?: availableCategories.firstOrNull()?.name ?: "")
    }
    var amountPaise by remember {
        mutableLongStateOf(initialBudget?.budgetAmountPaise ?: 0L)
    }

    val isValid = selectedCategory.isNotBlank() && amountPaise > 0L

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(MaterialTheme.spacing.md),
            shape = MaterialTheme.appShapes.large,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(MaterialTheme.spacing.lg)
            ) {
                // Title
                Text(
                    text = if (isEdit) "Edit Budget" else "Set Monthly Budget",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(MaterialTheme.spacing.md))

                // Category Selection
                Text(
                    text = "Category",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(MaterialTheme.spacing.xs))

                if (isEdit) {
                    Text(
                        text = selectedCategory,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                } else {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xs),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(availableCategories, key = { it.id }) { cat ->
                            val isSelected = selectedCategory == cat.name
                            val categoryColor = parseCategoryColor(cat.colorHex)

                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedCategory = cat.name },
                                leadingIcon = {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(categoryColor)
                                    )
                                },
                                label = {
                                    Text(
                                        text = cat.name,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(MaterialTheme.spacing.md))

                // Budget Amount Input
                Text(
                    text = "Monthly Budget Limit",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(MaterialTheme.spacing.xs))

                CurrencyInput(
                    amountPaise = amountPaise,
                    onAmountChange = { amountPaise = it },
                    label = "Budget Amount",
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(MaterialTheme.spacing.lg))

                // Actions Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm)
                ) {
                    SecondaryButton(
                        text = "Cancel",
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    )
                    PrimaryButton(
                        text = if (isEdit) "Save Changes" else "Set Budget",
                        onClick = {
                            if (isValid) {
                                onSave(selectedCategory, amountPaise)
                                onDismiss()
                            }
                        },
                        enabled = isValid,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
