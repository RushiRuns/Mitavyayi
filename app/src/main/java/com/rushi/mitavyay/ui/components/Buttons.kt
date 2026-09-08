package com.rushi.mitavyay.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rushi.mitavyay.ui.theme.appShapes
import com.rushi.mitavyay.ui.theme.spacing

/**
 * Primary Button variant.
 * Styled with primary brand color and theme shape tokens.
 */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    leadingIcon: (@Composable () -> Unit)? = null
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled && !loading,
        shape = MaterialTheme.appShapes.small,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        contentPadding = PaddingValues(
            horizontal = MaterialTheme.spacing.md,
            vertical = MaterialTheme.spacing.sm
        )
    ) {
        ButtonContent(
            text = text,
            loading = loading,
            leadingIcon = leadingIcon,
            spinnerColor = MaterialTheme.colorScheme.onPrimary
        )
    }
}

/**
 * Secondary Button variant.
 * Outlined styling for secondary actions.
 */
@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    leadingIcon: (@Composable () -> Unit)? = null
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled && !loading,
        shape = MaterialTheme.appShapes.small,
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.primary,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        contentPadding = PaddingValues(
            horizontal = MaterialTheme.spacing.md,
            vertical = MaterialTheme.spacing.sm
        )
    ) {
        ButtonContent(
            text = text,
            loading = loading,
            leadingIcon = leadingIcon,
            spinnerColor = MaterialTheme.colorScheme.primary
        )
    }
}

/**
 * Tertiary Button variant.
 * Text button for low-emphasis actions or dialog dismissals.
 */
@Composable
fun TertiaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: (@Composable () -> Unit)? = null
) {
    TextButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = MaterialTheme.appShapes.small,
        colors = ButtonDefaults.textButtonColors(
            contentColor = MaterialTheme.colorScheme.tertiary,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        contentPadding = PaddingValues(
            horizontal = MaterialTheme.spacing.sm,
            vertical = MaterialTheme.spacing.xs
        )
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (leadingIcon != null) {
                leadingIcon()
                HSpacerSm()
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

/**
 * Destructive / Error Button variant.
 * Used for deleting items, accounts, or resetting states.
 */
@Composable
fun DangerButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled && !loading,
        shape = MaterialTheme.appShapes.small,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.error,
            contentColor = MaterialTheme.colorScheme.onError
        ),
        contentPadding = PaddingValues(
            horizontal = MaterialTheme.spacing.md,
            vertical = MaterialTheme.spacing.sm
        )
    ) {
        ButtonContent(
            text = text,
            loading = loading,
            leadingIcon = null,
            spinnerColor = MaterialTheme.colorScheme.onError
        )
    }
}

@Composable
private fun ButtonContent(
    text: String,
    loading: Boolean,
    leadingIcon: (@Composable () -> Unit)?,
    spinnerColor: androidx.compose.ui.graphics.Color
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                strokeWidth = 2.dp,
                color = spinnerColor
            )
            HSpacerSm()
        } else if (leadingIcon != null) {
            leadingIcon()
            HSpacerSm()
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge
        )
    }
}
