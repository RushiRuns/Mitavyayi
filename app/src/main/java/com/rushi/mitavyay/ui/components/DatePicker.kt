package com.rushi.mitavyay.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.rushi.mitavyay.R
import com.rushi.mitavyay.ui.theme.appShapes
import com.rushi.mitavyay.util.DateTimeFormatter

/**
 * Clickable date selection input field that launches [AppDatePickerDialog].
 * Formats and emits selected date as Unix timestamp milliseconds ([Long]).
 */
@Composable
fun DatePickerField(
    selectedDateMs: Long,
    onDateSelected: (Long) -> Unit,
    modifier: Modifier = Modifier,
    label: String = stringResource(R.string.select_date),
    enabled: Boolean = true
) {
    var showDialog by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        AppTextField(
            value = if (selectedDateMs > 0) DateTimeFormatter.formatDate(selectedDateMs) else "",
            onValueChange = {},
            modifier = Modifier.fillMaxWidth(),
            label = label,
            readOnly = true,
            enabled = enabled,
            trailingIcon = {
                Icon(
                    imageVector = Icons.Outlined.DateRange,
                    contentDescription = label,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        )

        // Overlay transparent clickable area so tapping anywhere on the field opens the picker
        if (enabled) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { showDialog = true }
                    )
            )
        }
    }

    if (showDialog) {
        AppDatePickerDialog(
            initialSelectedDateMs = if (selectedDateMs > 0) selectedDateMs else System.currentTimeMillis(),
            onDateSelected = { timestamp ->
                onDateSelected(timestamp)
                showDialog = false
            },
            onDismissRequest = { showDialog = false }
        )
    }
}

/**
 * Material 3 DatePickerDialog wrapper using application theme tokens.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDatePickerDialog(
    initialSelectedDateMs: Long,
    onDateSelected: (Long) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialSelectedDateMs
    )

    DatePickerDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            PrimaryButton(
                text = stringResource(R.string.action_confirm),
                onClick = {
                    datePickerState.selectedDateMillis?.let { onDateSelected(it) }
                }
            )
        },
        dismissButton = {
            TertiaryButton(
                text = stringResource(R.string.action_cancel),
                onClick = onDismissRequest
            )
        },
        modifier = modifier,
        shape = MaterialTheme.appShapes.large,
        colors = DatePickerDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        DatePicker(
            state = datePickerState,
            colors = DatePickerDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surface,
                titleContentColor = MaterialTheme.colorScheme.onSurface,
                headlineContentColor = MaterialTheme.colorScheme.onSurface,
                weekdayContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                subheadContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                yearContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                currentYearContentColor = MaterialTheme.colorScheme.primary,
                selectedYearContentColor = MaterialTheme.colorScheme.onPrimary,
                selectedYearContainerColor = MaterialTheme.colorScheme.primary,
                dayContentColor = MaterialTheme.colorScheme.onSurface,
                selectedDayContentColor = MaterialTheme.colorScheme.onPrimary,
                selectedDayContainerColor = MaterialTheme.colorScheme.primary,
                todayContentColor = MaterialTheme.colorScheme.primary,
                todayDateBorderColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}

/**
 * Material 3 DateRangePickerDialog wrapper using application theme tokens.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDateRangePickerDialog(
    initialSelectedStartDateMs: Long? = null,
    initialSelectedEndDateMs: Long? = null,
    onDateRangeSelected: (startDateMs: Long, endDateMs: Long) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateRangePickerState = rememberDateRangePickerState(
        initialSelectedStartDateMillis = initialSelectedStartDateMs,
        initialSelectedEndDateMillis = initialSelectedEndDateMs
    )

    DatePickerDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            PrimaryButton(
                text = stringResource(R.string.action_confirm),
                onClick = {
                    val start = dateRangePickerState.selectedStartDateMillis
                    val end = dateRangePickerState.selectedEndDateMillis ?: start
                    if (start != null && end != null) {
                        onDateRangeSelected(start, end)
                    }
                },
                enabled = dateRangePickerState.selectedStartDateMillis != null
            )
        },
        dismissButton = {
            TertiaryButton(
                text = stringResource(R.string.action_cancel),
                onClick = onDismissRequest
            )
        },
        modifier = modifier,
        shape = MaterialTheme.appShapes.large,
        colors = DatePickerDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        DateRangePicker(
            state = dateRangePickerState,
            colors = DatePickerDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surface,
                titleContentColor = MaterialTheme.colorScheme.onSurface,
                headlineContentColor = MaterialTheme.colorScheme.onSurface,
                weekdayContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                subheadContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                yearContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                currentYearContentColor = MaterialTheme.colorScheme.primary,
                selectedYearContentColor = MaterialTheme.colorScheme.onPrimary,
                selectedYearContainerColor = MaterialTheme.colorScheme.primary,
                dayContentColor = MaterialTheme.colorScheme.onSurface,
                selectedDayContentColor = MaterialTheme.colorScheme.onPrimary,
                selectedDayContainerColor = MaterialTheme.colorScheme.primary,
                todayContentColor = MaterialTheme.colorScheme.primary,
                todayDateBorderColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}
