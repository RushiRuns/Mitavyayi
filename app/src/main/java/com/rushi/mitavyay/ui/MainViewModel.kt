package com.rushi.mitavyay.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rushi.mitavyay.data.datastore.PreferencesRepository
import com.rushi.mitavyay.data.repository.AccountRepository
import com.rushi.mitavyay.data.repository.RepeatExpenseRepository
import com.rushi.mitavyay.ui.theme.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * App-level ViewModel for MainActivity to observe and modify global preferences (theme, font scale),
 * and trigger lazy evaluation of due recurring expenses on startup.
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
    private val repeatExpenseRepository: RepeatExpenseRepository? = null,
    private val accountRepository: AccountRepository? = null
) : ViewModel() {

    init {
        viewModelScope.launch {
            preferencesRepository.incrementAppOpenCount()
            triggerLazyRecurringExpenses()
        }
    }

    private suspend fun triggerLazyRecurringExpenses() {
        try {
            if (repeatExpenseRepository != null && accountRepository != null) {
                val activeAccounts = accountRepository.getActiveAccounts().first()
                val primaryAccount = activeAccounts.firstOrNull()
                if (primaryAccount != null) {
                    repeatExpenseRepository.processAllDueOccurrences(primaryAccount.id)
                }
            }
        } catch (_: Exception) {
            // Ignore offline startup generation issues
        }
    }

    val themeMode: StateFlow<ThemeMode> = preferencesRepository.themeMode
        .map { modeString ->
            when (modeString.uppercase()) {
                "LIGHT" -> ThemeMode.LIGHT
                "DARK" -> ThemeMode.DARK
                else -> ThemeMode.SYSTEM
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = ThemeMode.SYSTEM
        )

    val fontScale: StateFlow<Float> = preferencesRepository.fontScaleMultiplier
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = 1.0f
        )

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            preferencesRepository.setThemeMode(mode.name)
        }
    }

    fun toggleTheme() {
        viewModelScope.launch {
            val nextMode = when (themeMode.value) {
                ThemeMode.LIGHT -> ThemeMode.DARK
                ThemeMode.DARK -> ThemeMode.LIGHT
                ThemeMode.SYSTEM -> ThemeMode.DARK
            }
            preferencesRepository.setThemeMode(nextMode.name)
        }
    }
}
