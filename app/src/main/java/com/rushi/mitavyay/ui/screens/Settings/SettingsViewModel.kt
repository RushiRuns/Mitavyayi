package com.rushi.mitavyay.ui.screens.Settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rushi.mitavyay.data.datastore.PreferencesRepository
import com.rushi.mitavyay.ui.theme.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

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

    val hapticFeedbackEnabled: StateFlow<Boolean> = preferencesRepository.hapticFeedbackEnabled
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = true
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

    fun setHapticFeedbackEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setHapticFeedbackEnabled(enabled)
        }
    }
}
