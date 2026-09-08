package com.rushi.mitavyay.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.rushi.mitavyay.R
import com.rushi.mitavyay.ui.theme.appShapes

/**
 * Standard AlertDialog wrapper enforcing theme tokens for typography, shapes, and colors.
 */
@Composable
fun AppAlertDialog(
    onDismissRequest: () -> Unit,
    title: String,
    text: String,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
    confirmText: String = stringResource(R.string.action_confirm),
    dismissText: String? = stringResource(R.string.action_cancel),
    onDismiss: (() -> Unit)? = onDismissRequest,
    isDestructive: Boolean = false
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        confirmButton = {
            if (isDestructive) {
                DangerButton(text = confirmText, onClick = onConfirm)
            } else {
                PrimaryButton(text = confirmText, onClick = onConfirm)
            }
        },
        dismissButton = if (dismissText != null && onDismiss != null) {
            { TertiaryButton(text = dismissText, onClick = onDismiss) }
        } else null,
        shape = MaterialTheme.appShapes.large,
        containerColor = MaterialTheme.colorScheme.surface,
        iconContentColor = MaterialTheme.colorScheme.primary,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurfaceVariant
    )
}
