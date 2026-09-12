package com.rushi.mitavyay.ui.screens.Settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rushi.mitavyay.data.repository.CsvImportResult
import com.rushi.mitavyay.ui.components.AppAlertDialog
import com.rushi.mitavyay.ui.components.LoadingState
import com.rushi.mitavyay.ui.components.PrimaryButton
import com.rushi.mitavyay.ui.components.SecondaryButton
import com.rushi.mitavyay.ui.components.SpacerLg
import com.rushi.mitavyay.ui.components.SpacerMd
import com.rushi.mitavyay.ui.components.SpacerSm
import com.rushi.mitavyay.ui.components.SpacerXs
import com.rushi.mitavyay.ui.theme.appShapes
import com.rushi.mitavyay.ui.theme.extendedColorScheme
import com.rushi.mitavyay.ui.theme.spacing

@Composable
fun ImportExportScreen(
    modifier: Modifier = Modifier,
    viewModel: ImportExportViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // SAF Document Creator launcher for exporting CSV to custom user directory
    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri: Uri? ->
        uri?.let { viewModel.exportToTargetUri(context, it) }
    }

    // SAF Document Picker launcher for importing CSV file
    val openDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { viewModel.importFromUri(context, it) }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(MaterialTheme.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.lg)
        ) {
            // Header Description
            Column {
                Text(
                    text = "Backup & Data Management",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                SpacerXs()
                Text(
                    text = "Export your transaction ledger for spreadsheet analysis or backup. Import past records with strict all-or-nothing validation.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Export Card
            Card(
                modifier = Modifier.fillMaxWidth(),
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
                        Text(
                            text = "Export Transactions",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        SuggestionChip(
                            onClick = {},
                            label = {
                                Text("${uiState.totalTransactionsCount} Records")
                            },
                            shape = MaterialTheme.appShapes.small,
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                labelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            border = null
                        )
                    }

                    SpacerSm()

                    Text(
                        text = "Generates a standard CSV file including date, amount, category, account, and transfer information.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    SpacerMd()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm)
                    ) {
                        PrimaryButton(
                            text = "Share CSV",
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            onClick = {
                                viewModel.exportToCache(context) { shareUri ->
                                    shareCsvFile(context, shareUri)
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )

                        SecondaryButton(
                            text = "Save as File",
                            onClick = {
                                createDocumentLauncher.launch("mitavyay_transactions_${System.currentTimeMillis()}.csv")
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Import Card
            Card(
                modifier = Modifier.fillMaxWidth(),
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
                    Text(
                        text = "Import Transactions",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    SpacerSm()

                    Text(
                        text = "Import transactions from a CSV file into your ledger. All rows are validated upfront before writing to database.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    SpacerSm()

                    Surface(
                        shape = MaterialTheme.appShapes.small,
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(MaterialTheme.spacing.sm),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "All-or-Nothing Policy: If any line has invalid dates or amounts, the entire import is rolled back to protect your balance history.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(start = MaterialTheme.spacing.xs)
                            )
                        }
                    }

                    SpacerMd()

                    PrimaryButton(
                        text = "Select CSV File to Import",
                        onClick = {
                            openDocumentLauncher.launch(arrayOf("text/*", "text/comma-separated-values", "text/csv"))
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // CSV Schema Reference Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.appShapes.medium,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(MaterialTheme.spacing.cardContent)
                ) {
                    Text(
                        text = "Supported CSV Format",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    SpacerXs()
                    Text(
                        text = "Headers may be arranged in any order (case-insensitive). Required columns: Date, Amount, Category, Account.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    SpacerSm()

                    Surface(
                        shape = MaterialTheme.appShapes.small,
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outlineVariant,
                                shape = MaterialTheme.appShapes.small
                            )
                    ) {
                        Text(
                            text = "Date, Amount, Category, Account, Description, Notes\n2026-09-12, -450.00, Groceries, Bank, Market veggies, Fresh market\n2026-09-12, 50000.00, Salary, Bank, Monthly salary, Direct deposit",
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(MaterialTheme.spacing.sm)
                        )
                    }
                }
            }
        }

        if (uiState.isLoading) {
            LoadingState(message = "Processing CSV data...")
        }
    }

    // Success Dialog for Import
    val importRes = uiState.importResult
    if (importRes is CsvImportResult.Success) {
        AppAlertDialog(
            onDismissRequest = { viewModel.dismissDialogs() },
            title = "Import Completed Successfully",
            text = "Successfully imported ${importRes.importedCount} transactions.\nCreated ${importRes.accountsCreatedCount} new accounts.\n\nAll balances have been reconciled.",
            confirmText = "OK",
            onConfirm = { viewModel.dismissDialogs() }
        )
    }

    // Error Dialog for Import
    if (importRes is CsvImportResult.Error) {
        AppAlertDialog(
            onDismissRequest = { viewModel.dismissDialogs() },
            title = "Import Failed",
            text = "${importRes.message}\n\nPer ADR-006, zero transactions were committed to prevent ledger corruption. Please correct the CSV and retry.",
            confirmText = "Dismiss",
            isDestructive = true,
            onConfirm = { viewModel.dismissDialogs() }
        )
    }

    // Export Success Dialog
    uiState.exportSuccessMessage?.let { msg ->
        AppAlertDialog(
            onDismissRequest = { viewModel.dismissDialogs() },
            title = "Export Complete",
            text = msg,
            confirmText = "OK",
            onConfirm = { viewModel.dismissDialogs() }
        )
    }

    // General Error Dialog
    uiState.errorMessage?.let { err ->
        AppAlertDialog(
            onDismissRequest = { viewModel.dismissDialogs() },
            title = "Error",
            text = err,
            confirmText = "Dismiss",
            isDestructive = true,
            onConfirm = { viewModel.dismissDialogs() }
        )
    }
}

private fun shareCsvFile(context: Context, contentUri: Uri) {
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/csv"
        putExtra(Intent.EXTRA_STREAM, contentUri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(shareIntent, "Share Transactions CSV"))
}
