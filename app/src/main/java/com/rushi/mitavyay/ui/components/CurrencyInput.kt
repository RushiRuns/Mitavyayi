package com.rushi.mitavyay.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import com.rushi.mitavyay.R
import com.rushi.mitavyay.ui.theme.appShapes
import com.rushi.mitavyay.ui.theme.spacing
import com.rushi.mitavyay.util.CurrencyFormatter

/**
 * Dedicated monetary input field for entering financial amounts.
 *
 * Enforces:
 * - Only valid decimal numbers (digits and at most 2 decimal places).
 * - Preserves monetary integrity by converting directly to/from [Long] paise without floats.
 * - Currency prefix styled using theme tokens.
 */
@Composable
fun CurrencyInput(
    amountPaise: Long,
    onAmountChange: (Long) -> Unit,
    modifier: Modifier = Modifier,
    label: String = stringResource(R.string.amount_label),
    currencySymbol: String = stringResource(R.string.currency_symbol),
    isError: Boolean = false,
    errorMessage: String? = null,
    enabled: Boolean = true,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    focusRequester: FocusRequester? = null
) {
    // Maintain internal string representation while user is typing
    var textValue by remember(amountPaise) {
        val initialText = if (amountPaise == 0L) "" else {
            val whole = amountPaise / 100
            val frac = amountPaise % 100
            if (frac == 0L) whole.toString() else String.format(java.util.Locale.ROOT, "%d.%02d", whole, frac)
        }
        mutableStateOf(initialText)
    }

    Column(modifier = modifier) {
        OutlinedTextField(
            value = textValue,
            onValueChange = { input ->
                val filtered = sanitizeCurrencyInput(input)
                textValue = filtered
                val parsedPaise = CurrencyFormatter.parseToPaise(filtered)
                onAmountChange(parsedPaise)
            },
            modifier = Modifier
                .fillMaxWidth()
                .then(if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier),
            enabled = enabled,
            singleLine = true,
            label = { Text(text = label, style = MaterialTheme.typography.bodySmall) },
            placeholder = {
                Text(
                    text = stringResource(R.string.amount_placeholder),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.outline
                )
            },
            prefix = {
                Text(
                    text = "$currencySymbol ",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
            },
            textStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
            isError = isError,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            keyboardActions = keyboardActions,
            shape = MaterialTheme.appShapes.small,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                errorBorderColor = MaterialTheme.colorScheme.error,
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                cursorColor = MaterialTheme.colorScheme.primary
            )
        )

        if (isError && !errorMessage.isNullOrBlank()) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(
                    start = MaterialTheme.spacing.sm,
                    top = MaterialTheme.spacing.xs
                )
            )
        }
    }
}

/**
 * Ensures user input consists only of digits and at most one decimal point with up to two decimal digits.
 */
private fun sanitizeCurrencyInput(input: String): String {
    val clean = input.filter { it.isDigit() || it == '.' }
    val parts = clean.split(".")
    return when {
        parts.size == 1 -> parts[0]
        parts.size >= 2 -> {
            val whole = parts[0]
            val decimal = parts[1].take(2)
            "$whole.$decimal"
        }
        else -> ""
    }
}
