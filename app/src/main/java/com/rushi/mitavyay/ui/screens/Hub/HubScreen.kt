package com.rushi.mitavyay.ui.screens.Hub

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rushi.mitavyay.ui.components.pressScale
import com.rushi.mitavyay.ui.theme.appShapes
import com.rushi.mitavyay.util.hapticLight

/**
 * Menu item model for HubScreen items.
 */
data class HubMenuItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

/**
 * Hub screen that centralizes access to accounts, debt & loans, insights,
 * recurring expenses, saving goals, budget planning, categories, and settings.
 * Replaces top bar clutter with an organized, card-based navigation dashboard.
 */
@Composable
fun HubScreen(
    onNavigateToAccounts: () -> Unit,
    onNavigateToDebt: () -> Unit,
    onNavigateToInsights: () -> Unit,
    onNavigateToRepeatExpenses: () -> Unit,
    onNavigateToGoals: () -> Unit,
    onNavigateToBudget: () -> Unit,
    onNavigateToCategories: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier,
    hapticFeedbackEnabled: Boolean = true
) {
    val context = LocalContext.current

    val menuItems = listOf(
        HubMenuItem(
            id = "accounts",
            title = "Accounts",
            subtitle = "Manage bank accounts, wallets & cards",
            icon = Icons.Default.AccountBox,
            onClick = onNavigateToAccounts
        ),
        HubMenuItem(
            id = "debts",
            title = "Debt & Loans",
            subtitle = "Track money lent to and borrowed from others",
            icon = Icons.Default.AccountCircle,
            onClick = onNavigateToDebt
        ),
        HubMenuItem(
            id = "insights",
            title = "Insights",
            subtitle = "Detailed spending analytics & key metrics",
            icon = Icons.Default.Info,
            onClick = onNavigateToInsights
        ),
        HubMenuItem(
            id = "recurring",
            title = "Recurring Expenses",
            subtitle = "Manage regular bills & subscriptions",
            icon = Icons.Default.Refresh,
            onClick = onNavigateToRepeatExpenses
        ),
        HubMenuItem(
            id = "goals",
            title = "Saving Goals",
            subtitle = "Set and track progress toward financial targets",
            icon = Icons.Default.Favorite,
            onClick = onNavigateToGoals
        ),
        HubMenuItem(
            id = "budgets",
            title = "Budget Planning",
            subtitle = "Set monthly category spending limits",
            icon = Icons.Default.ShoppingCart,
            onClick = onNavigateToBudget
        ),
        HubMenuItem(
            id = "categories",
            title = "Categories",
            subtitle = "Manage expense and income categories",
            icon = Icons.Default.Star,
            onClick = onNavigateToCategories
        ),
        HubMenuItem(
            id = "settings",
            title = "Settings",
            subtitle = "Preferences, theme & haptic feedback",
            icon = Icons.Default.Settings,
            onClick = onNavigateToSettings
        )
    )

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                text = "Manage & Plan",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 4.dp, start = 4.dp)
            )
        }

        items(
            items = menuItems,
            key = { it.id }
        ) { item ->
            HubMenuItemCard(
                item = item,
                onClick = {
                    context.hapticLight(hapticFeedbackEnabled)
                    item.onClick()
                }
            )
        }
    }
}

@Composable
fun HubMenuItemCard(
    item: HubMenuItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .pressScale(),
        onClick = onClick,
        shape = MaterialTheme.appShapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = MaterialTheme.appShapes.small
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (item.subtitle.isNotEmpty()) {
                    Text(
                        text = item.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
