package com.rushi.mitavyay.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.window.Dialog
import com.rushi.mitavyay.ui.theme.ThemeMode
import com.rushi.mitavyay.ui.theme.appShapes
import com.rushi.mitavyay.ui.theme.spacing

/**
 * Modal dialog for switching app theme between System default, Light mode, and Dark mode.
 * Styled with theme tokens.
 */
@Composable
fun ThemeSelectionDialog(
    currentThemeMode: ThemeMode,
    onThemeSelected: (ThemeMode) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.appShapes.large,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = MaterialTheme.spacing.sm,
            modifier = modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(MaterialTheme.spacing.lg)
            ) {
                Text(
                    text = "Choose Theme",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                SpacerMd()

                ThemeOptionRow(
                    label = "System default",
                    description = "Follows your device settings",
                    selected = currentThemeMode == ThemeMode.SYSTEM,
                    onClick = { onThemeSelected(ThemeMode.SYSTEM) }
                )

                ThemeOptionRow(
                    label = "Light theme",
                    description = "Always use bright colors",
                    selected = currentThemeMode == ThemeMode.LIGHT,
                    onClick = { onThemeSelected(ThemeMode.LIGHT) }
                )

                ThemeOptionRow(
                    label = "Dark theme",
                    description = "Easy on the eyes in low light",
                    selected = currentThemeMode == ThemeMode.DARK,
                    onClick = { onThemeSelected(ThemeMode.DARK) }
                )

                SpacerLg()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TertiaryButton(
                        text = "Done",
                        onClick = onDismiss
                    )
                }
            }
        }
    }
}

@Composable
private fun ThemeOptionRow(
    label: String,
    description: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = MaterialTheme.spacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = MaterialTheme.colorScheme.primary,
                unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
        HSpacerSm()
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
