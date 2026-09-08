package com.rushi.mitavyay.data.datastore

import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object PreferenceKeys {
    val THEME_MODE = stringPreferencesKey("theme_mode")
    val FONT_SCALE_MULTIPLIER = floatPreferencesKey("font_scale_multiplier")
    val CURRENCY_SYMBOL = stringPreferencesKey("currency_symbol")
    val LANGUAGE = stringPreferencesKey("language")
    val APP_OPEN_COUNT = intPreferencesKey("app_open_count")
}
