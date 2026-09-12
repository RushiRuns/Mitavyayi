package com.rushi.mitavyay.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.window.Dialog
import com.rushi.mitavyay.ui.theme.ThemeMode
import com.rushi.mitavyay.ui.theme.appShapes
import com.rushi.mitavyay.ui.theme.spacing
import com.rushi.mitavyay.util.hapticLight

/**
 * Modal dialog for switching app theme and general preferences (including haptic feedback).
 * Styled with theme tokens.
 */
@Composable
fun ThemeSelectionDialog(
    currentThemeMode: ThemeMode,
    onThemeSelected: (ThemeMode) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    hapticFeedbackEnabled: Boolean = true,
    onHapticFeedbackToggled: ((Boolean) -> Unit)? = null
) {
    val context = LocalContext.current

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
                    text = "Appearance & Settings",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                SpacerMd()

                ThemeOptionRow(
                    label = "System default",
                    description = "Follows your device settings",
                    selected = currentThemeMode == ThemeMode.SYSTEM,
                    onClick = {
                        context.hapticLight(hapticFeedbackEnabled)
                        onThemeSelected(ThemeMode.SYSTEM)
                    }
                )

                ThemeOptionRow(
                    label = "Light theme",
                    description = "Always use bright colors",
                    selected = currentThemeMode == ThemeMode.LIGHT,
                    onClick = {
                        context.hapticLight(hapticFeedbackEnabled)
                        onThemeSelected(ThemeMode.LIGHT)
                    }
                )

                ThemeOptionRow(
                    label = "Dark theme",
                    description = "Easy on the eyes in low light",
                    selected = currentThemeMode == ThemeMode.DARK,
                    onClick = {
                        context.hapticLight(hapticFeedbackEnabled)
                        onThemeSelected(ThemeMode.DARK)
                    }
                )

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = MaterialTheme.spacing.md),
                    color = MaterialTheme.colorScheme.outlineVariant
                )

                Text(
                    text = "Preferences",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                SpacerSm()

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val next = !hapticFeedbackEnabled
                            context.hapticLight(next)
                            onHapticFeedbackToggled?.invoke(next)
                        }
                        .padding(vertical = MaterialTheme.spacing.xs),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Haptic feedback",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Vibrate on button taps and actions",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = hapticFeedbackEnabled,
                        onCheckedChange = { isChecked ->
                            context.hapticLight(isChecked)
                            onHapticFeedbackToggled?.invoke(isChecked)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.primary,
                            checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }

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
