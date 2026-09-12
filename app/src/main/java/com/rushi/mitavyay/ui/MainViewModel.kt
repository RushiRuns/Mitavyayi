package com.rushi.mitavyay.ui

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

/**
 * App-level ViewModel for MainActivity to observe global preferences (theme, font scale).
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    init {
        viewModelScope.launch {
            preferencesRepository.incrementAppOpenCount()
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
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ThemeMode.SYSTEM
        )

    val fontScale: StateFlow<Float> = preferencesRepository.fontScaleMultiplier
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = 1.0f
        )
}
