package com.rushi.mitavyay.ui.screens.Settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rushi.mitavyay.ui.components.pressScale
import com.rushi.mitavyay.ui.theme.ThemeMode
import com.rushi.mitavyay.ui.theme.appShapes
import com.rushi.mitavyay.ui.theme.spacing
import com.rushi.mitavyay.util.hapticLight

/**
 * Full-page Settings destination in Mitavyay.
 * Centralizes theme appearance, backup & data access, tactile haptic preferences, and app information.
 */
@Composable
fun SettingsScreen(
    onNavigateToBackupData: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val currentThemeMode by viewModel.themeMode.collectAsState()
    val hapticFeedbackEnabled by viewModel.hapticFeedbackEnabled.collectAsState()

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(MaterialTheme.spacing.md),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.lg)
    ) {
        // Section: Appearance & Theme
        item {
            SettingsSectionHeader(title = "Appearance")
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.xs))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.appShapes.medium,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(MaterialTheme.spacing.md)
                ) {
                    ThemeOptionRow(
                        label = "System default",
                        description = "Follows your device settings",
                        selected = currentThemeMode == ThemeMode.SYSTEM,
                        onClick = {
                            context.hapticLight(hapticFeedbackEnabled)
                            viewModel.setThemeMode(ThemeMode.SYSTEM)
                        }
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = MaterialTheme.spacing.xs),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )

                    ThemeOptionRow(
                        label = "Light theme",
                        description = "Always use bright surfaces",
                        selected = currentThemeMode == ThemeMode.LIGHT,
                        onClick = {
                            context.hapticLight(hapticFeedbackEnabled)
                            viewModel.setThemeMode(ThemeMode.LIGHT)
                        }
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = MaterialTheme.spacing.xs),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )

                    ThemeOptionRow(
                        label = "Dark theme",
                        description = "Easy on the eyes in low light",
                        selected = currentThemeMode == ThemeMode.DARK,
                        onClick = {
                            context.hapticLight(hapticFeedbackEnabled)
                            viewModel.setThemeMode(ThemeMode.DARK)
                        }
                    )
                }
            }
        }

        // Section: Data & Backup
        item {
            SettingsSectionHeader(title = "Data Management")
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.xs))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .pressScale(),
                onClick = {
                    context.hapticLight(hapticFeedbackEnabled)
                    onNavigateToBackupData()
                },
                shape = MaterialTheme.appShapes.medium,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
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
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Backup & Data",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Export and import transaction records via CSV",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
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

        // Section: Behavior & Preferences
        item {
            SettingsSectionHeader(title = "Preferences")
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.xs))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.appShapes.medium,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val next = !hapticFeedbackEnabled
                            context.hapticLight(next)
                            viewModel.setHapticFeedbackEnabled(next)
                        }
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Haptic feedback",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Tactile vibration on buttons and actions",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Switch(
                        checked = hapticFeedbackEnabled,
                        onCheckedChange = { isChecked ->
                            context.hapticLight(isChecked)
                            viewModel.setHapticFeedbackEnabled(isChecked)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.primary,
                            checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }
        }

        // Section: About
        item {
            SettingsSectionHeader(title = "About")
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.xs))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.appShapes.medium,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
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
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Mitavyay",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Version 1.0 • 100% Offline & Private",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Your data never leaves this device. No trackers, no network calls.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsSectionHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier.padding(start = 4.dp)
    )
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
            .padding(vertical = MaterialTheme.spacing.xs),
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
        Spacer(modifier = Modifier.width(MaterialTheme.spacing.sm))
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
