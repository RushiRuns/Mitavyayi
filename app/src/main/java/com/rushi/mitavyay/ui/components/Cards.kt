package com.rushi.mitavyay.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rushi.mitavyay.R
import com.rushi.mitavyay.ui.theme.appShapes
import com.rushi.mitavyay.ui.theme.extendedColorScheme
import com.rushi.mitavyay.ui.theme.spacing
import com.rushi.mitavyay.data.model.AccountDisplayItem
import com.rushi.mitavyay.data.model.DebtDisplayItem
import com.rushi.mitavyay.data.model.TransactionDisplayItem
import com.rushi.mitavyay.util.CurrencyFormatter
import com.rushi.mitavyay.util.DateTimeFormatter

/**
 * Card representing a single financial transaction.
 * Uses theme tokens and semantic income/expense colors.
 */
@Composable
fun TransactionCard(
    title: String,
    category: String,
    amountPaise: Long,
    timestampMs: Long,
    modifier: Modifier = Modifier,
    accountName: String? = null,
    isTransfer: Boolean = false,
    onClick: () -> Unit = {}
) {
    val isCredit = amountPaise >= 0
    val amountColor = if (isCredit) {
        MaterialTheme.extendedColorScheme.success
    } else {
        MaterialTheme.colorScheme.error
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = MaterialTheme.appShapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = MaterialTheme.spacing.xs
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(MaterialTheme.spacing.cardContent),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                SpacerXs()
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xs)
                ) {
                    Text(
                        text = category,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (accountName != null) {
                        Text(
                            text = "• $accountName",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (isTransfer) {
                        Surface(
                            shape = MaterialTheme.appShapes.small,
                            color = MaterialTheme.colorScheme.tertiaryContainer
                        ) {
                            Text(
                                text = "Transfer",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                SpacerXs()
                Text(
                    text = DateTimeFormatter.formatDate(timestampMs),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            Text(
                text = CurrencyFormatter.format(amountPaise),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = amountColor
            )
        }
    }
}

/**
 * Overload for [TransactionCard] accepting a decoupled [TransactionDisplayItem].
 */
@Composable
fun TransactionCard(
    item: TransactionDisplayItem,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val amountColor = if (item.isCredit) {
        MaterialTheme.extendedColorScheme.success
    } else {
        MaterialTheme.colorScheme.error
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = MaterialTheme.appShapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = MaterialTheme.spacing.xs
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(MaterialTheme.spacing.cardContent),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                SpacerXs()
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xs)
                ) {
                    Text(
                        text = item.category,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (item.accountName != null) {
                        Text(
                            text = "• ${item.accountName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (item.isTransfer) {
                        Surface(
                            shape = MaterialTheme.appShapes.small,
                            color = MaterialTheme.colorScheme.tertiaryContainer
                        ) {
                            Text(
                                text = "Transfer",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                SpacerXs()
                Text(
                    text = item.date,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            Text(
                text = item.amountFormatted,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = amountColor
            )
        }
    }
}

/**
 * Card representing a user financial account (e.g., Bank, Cash, Credit).
 */
@Composable
fun AccountCard(
    accountName: String,
    accountType: String,
    balancePaise: Long,
    modifier: Modifier = Modifier,
    isActive: Boolean = true,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = MaterialTheme.appShapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = MaterialTheme.spacing.xs
        )
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
                Text(
                    text = accountName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                SuggestionChip(
                    onClick = {},
                    label = {
                        Text(
                            text = accountType,
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    shape = MaterialTheme.appShapes.small,
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        labelColor = MaterialTheme.colorScheme.onSecondaryContainer
                    ),
                    border = null
                )
            }

            SpacerSm()

            Text(
                text = stringResource(
                    R.string.account_balance,
                    CurrencyFormatter.format(balancePaise)
                ),
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = if (balancePaise >= 0) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.error
                }
            )

            SpacerXs()

            Text(
                text = stringResource(
                    if (isActive) R.string.account_status_active else R.string.account_status_inactive
                ),
                style = MaterialTheme.typography.labelSmall,
                color = if (isActive) {
                    MaterialTheme.extendedColorScheme.success
                } else {
                    MaterialTheme.colorScheme.outline
                }
            )
        }
    }
}

/**
 * Overload for [AccountCard] accepting a decoupled [AccountDisplayItem].
 */
@Composable
fun AccountCard(
    item: AccountDisplayItem,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = MaterialTheme.appShapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = MaterialTheme.spacing.xs
        )
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
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                SuggestionChip(
                    onClick = {},
                    label = {
                        Text(
                            text = item.type,
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    shape = MaterialTheme.appShapes.small,
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        labelColor = MaterialTheme.colorScheme.onSecondaryContainer
                    ),
                    border = null
                )
            }

            SpacerSm()

            Text(
                text = stringResource(
                    R.string.account_balance,
                    item.balanceFormatted
                ),
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = if (!item.isNegative) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.error
                }
            )

            SpacerXs()

            Text(
                text = stringResource(
                    if (item.isActive) R.string.account_status_active else R.string.account_status_inactive
                ),
                style = MaterialTheme.typography.labelSmall,
                color = if (item.isActive) {
                    MaterialTheme.extendedColorScheme.success
                } else {
                    MaterialTheme.colorScheme.outline
                }
            )
        }
    }
}

/**
 * Card representing a savings or financial goal with progress tracking.
 */
@Composable
fun GoalCard(
    goalName: String,
    category: String,
    currentAmountPaise: Long,
    targetAmountPaise: Long,
    deadlineMs: Long,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val progress = if (targetAmountPaise > 0) {
        (currentAmountPaise.toFloat() / targetAmountPaise.toFloat()).coerceIn(0f, 1f)
    } else 0f

    val progressColor = if (progress >= 1.0f) {
        MaterialTheme.extendedColorScheme.success
    } else {
        MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = MaterialTheme.appShapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = MaterialTheme.spacing.xs
        )
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
                Text(
                    text = goalName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.labelMedium,
                    color = progressColor
                )
            }

            SpacerXs()

            Text(
                text = category,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            SpacerSm()

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth(),
                color = progressColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            SpacerSm()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(
                        R.string.goal_saved,
                        CurrencyFormatter.format(currentAmountPaise)
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(
                        R.string.goal_target,
                        CurrencyFormatter.format(targetAmountPaise)
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            if (deadlineMs > 0) {
                SpacerXs()
                Text(
                    text = stringResource(
                        R.string.goal_deadline,
                        DateTimeFormatter.formatDate(deadlineMs)
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

/**
 * Card representing a debt or loan record (money lent or borrowed).
 * Invariant: Debts are never deleted, only marked as settled.
 */
@Composable
fun DebtCard(
    item: DebtDisplayItem,
    modifier: Modifier = Modifier,
    onMarkSettled: (() -> Unit)? = null,
    onClick: () -> Unit = {}
) {
    val isLent = item.isLent
    val amountColor = if (isLent) {
        MaterialTheme.extendedColorScheme.success
    } else {
        MaterialTheme.colorScheme.error
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = MaterialTheme.appShapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = MaterialTheme.spacing.xs
        )
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
                        text = item.counterparty,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    SpacerXs()
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xs)
                    ) {
                        SuggestionChip(
                            onClick = {},
                            label = {
                                Text(
                                    text = if (isLent) "Lent (To Receive)" else "Borrowed (To Pay)",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            shape = MaterialTheme.appShapes.small,
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = if (isLent) {
                                    MaterialTheme.extendedColorScheme.success.copy(alpha = 0.15f)
                                } else {
                                    MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                                },
                                labelColor = if (isLent) {
                                    MaterialTheme.extendedColorScheme.success
                                } else {
                                    MaterialTheme.colorScheme.error
                                }
                            ),
                            border = null
                        )

                        if (item.isSettled) {
                            Surface(
                                shape = MaterialTheme.appShapes.small,
                                color = MaterialTheme.colorScheme.tertiaryContainer
                            ) {
                                Text(
                                    text = "Settled",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                Text(
                    text = item.amountFormatted,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = amountColor
                )
            }

            SpacerSm()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (item.isSettled && item.settledDateFormatted != null) {
                        "Settled on ${item.settledDateFormatted}"
                    } else {
                        "Created on ${item.createdDateFormatted}"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )

                if (!item.isSettled && onMarkSettled != null) {
                    SecondaryButton(
                        text = "Mark Settled",
                        onClick = onMarkSettled
                    )
                }
            }

            if (!item.notes.isNullOrBlank()) {
                SpacerXs()
                Text(
                    text = item.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

