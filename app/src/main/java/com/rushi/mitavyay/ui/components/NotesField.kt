package com.rushi.mitavyay.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rushi.mitavyay.ui.theme.appShapes
import com.rushi.mitavyay.ui.theme.spacing
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Rich multi-line notes input component for transactions.
 *
 * Features:
 * - Multi-line editing with configurable line constraints.
 * - Character count limiter and counter indicator.
 * - Clear action icon when text is populated.
 * - Quick formatting chips: itemized bullet lists (`• `), timestamped date stamps (`[dd MMM]`),
 *   and common financial tags (`#tax`, `#reimbursable`, `#split`, `#warranty`, `#bill`).
 */
@Composable
fun NotesField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Notes (Optional)",
    placeholder: String = "Add details, receipt items, tags, or reminders...",
    maxLength: Int = 500,
    minLines: Int = 3,
    maxLines: Int = 6,
    enabled: Boolean = true,
    showQuickHelpers: Boolean = true
) {
    val isNearLimit = value.length >= maxLength * 0.9

    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = { newText ->
                if (newText.length <= maxLength) {
                    onValueChange(newText)
                }
            },
            label = { Text(label) },
            placeholder = {
                Text(
                    text = placeholder,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            },
            minLines = minLines,
            maxLines = maxLines,
            enabled = enabled,
            shape = MaterialTheme.appShapes.medium,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            ),
            trailingIcon = {
                if (value.isNotEmpty() && enabled) {
                    IconButton(onClick = { onValueChange("") }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear notes",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            supportingText = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = "${value.length} / $maxLength",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isNearLimit) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        AnimatedVisibility(visible = showQuickHelpers && enabled) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Spacer(modifier = Modifier.height(MaterialTheme.spacing.xs))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xs),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Itemized bullet helper
                    AssistChip(
                        onClick = {
                            val newText = if (value.isBlank()) {
                                "• "
                            } else if (value.endsWith("\n")) {
                                "$value• "
                            } else {
                                "$value\n• "
                            }
                            if (newText.length <= maxLength) onValueChange(newText)
                        },
                        label = { Text("• Bullet") },
                        shape = MaterialTheme.appShapes.small,
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )

                    // Date stamp helper
                    AssistChip(
                        onClick = {
                            val dateTag = "[${SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date())}] "
                            val newText = if (value.isBlank()) {
                                dateTag
                            } else if (value.endsWith(" ") || value.endsWith("\n")) {
                                "$value$dateTag"
                            } else {
                                "$value $dateTag"
                            }
                            if (newText.length <= maxLength) onValueChange(newText)
                        },
                        label = { Text("📅 Today") },
                        shape = MaterialTheme.appShapes.small,
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )

                    // Financial quick tags
                    val quickTags = listOf("#tax", "#reimbursable", "#split", "#warranty", "#bill")
                    quickTags.forEach { tag ->
                        AssistChip(
                            onClick = {
                                if (!value.contains(tag)) {
                                    val newText = if (value.isBlank()) {
                                        tag
                                    } else if (value.endsWith(" ") || value.endsWith("\n")) {
                                        "$value$tag"
                                    } else {
                                        "$value $tag"
                                    }
                                    if (newText.length <= maxLength) onValueChange(newText)
                                }
                            },
                            label = { Text(tag) },
                            shape = MaterialTheme.appShapes.small,
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                                labelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
            }
        }
    }
}
