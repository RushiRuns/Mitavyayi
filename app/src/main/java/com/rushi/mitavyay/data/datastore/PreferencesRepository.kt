package com.rushi.mitavyay.data.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

interface PreferencesRepository {
    val themeMode: Flow<String>
    val fontScaleMultiplier: Flow<Float>
    val currencySymbol: Flow<String>
    val language: Flow<String>
    val appOpenCount: Flow<Int>
    val hapticFeedbackEnabled: Flow<Boolean>

    suspend fun setThemeMode(mode: String)
    suspend fun setFontScaleMultiplier(scale: Float)
    suspend fun setCurrencySymbol(symbol: String)
    suspend fun setLanguage(lang: String)
    suspend fun incrementAppOpenCount()
    suspend fun setHapticFeedbackEnabled(enabled: Boolean)
}

@Singleton
class PreferencesRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : PreferencesRepository {

    override val themeMode: Flow<String> = dataStore.data.map { prefs ->
        prefs[PreferenceKeys.THEME_MODE] ?: "SYSTEM"
    }

    override val fontScaleMultiplier: Flow<Float> = dataStore.data.map { prefs ->
        prefs[PreferenceKeys.FONT_SCALE_MULTIPLIER] ?: 1.0f
    }

    override val currencySymbol: Flow<String> = dataStore.data.map { prefs ->
        prefs[PreferenceKeys.CURRENCY_SYMBOL] ?: "₹"
    }

    override val language: Flow<String> = dataStore.data.map { prefs ->
        prefs[PreferenceKeys.LANGUAGE] ?: "en"
    }

    override val appOpenCount: Flow<Int> = dataStore.data.map { prefs ->
        prefs[PreferenceKeys.APP_OPEN_COUNT] ?: 0
    }

    override val hapticFeedbackEnabled: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[PreferenceKeys.HAPTIC_FEEDBACK_ENABLED] ?: true
    }

    override suspend fun setThemeMode(mode: String) {
        dataStore.edit { prefs ->
            prefs[PreferenceKeys.THEME_MODE] = mode
        }
    }

    override suspend fun setFontScaleMultiplier(scale: Float) {
        dataStore.edit { prefs ->
            prefs[PreferenceKeys.FONT_SCALE_MULTIPLIER] = scale
        }
    }

    override suspend fun setCurrencySymbol(symbol: String) {
        dataStore.edit { prefs ->
            prefs[PreferenceKeys.CURRENCY_SYMBOL] = symbol
        }
    }

    override suspend fun setLanguage(lang: String) {
        dataStore.edit { prefs ->
            prefs[PreferenceKeys.LANGUAGE] = lang
        }
    }

    override suspend fun incrementAppOpenCount() {
        dataStore.edit { prefs ->
            val current = prefs[PreferenceKeys.APP_OPEN_COUNT] ?: 0
            prefs[PreferenceKeys.APP_OPEN_COUNT] = current + 1
        }
    }

    override suspend fun setHapticFeedbackEnabled(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[PreferenceKeys.HAPTIC_FEEDBACK_ENABLED] = enabled
        }
    }
}
