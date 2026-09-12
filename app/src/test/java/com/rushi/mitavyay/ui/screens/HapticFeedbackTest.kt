package com.rushi.mitavyay.ui.screens

import com.rushi.mitavyay.data.datastore.PreferencesRepository
import com.rushi.mitavyay.ui.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HapticFeedbackTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private class TestPreferencesRepository : PreferencesRepository {
        private val _themeMode = MutableStateFlow("SYSTEM")
        private val _fontScale = MutableStateFlow(1.0f)
        private val _currency = MutableStateFlow("₹")
        private val _language = MutableStateFlow("en")
        private val _openCount = MutableStateFlow(0)
        private val _hapticEnabled = MutableStateFlow(true)

        override val themeMode: Flow<String> = _themeMode
        override val fontScaleMultiplier: Flow<Float> = _fontScale
        override val currencySymbol: Flow<String> = _currency
        override val language: Flow<String> = _language
        override val appOpenCount: Flow<Int> = _openCount
        override val hapticFeedbackEnabled: Flow<Boolean> = _hapticEnabled

        override suspend fun setThemeMode(mode: String) {
            _themeMode.value = mode
        }

        override suspend fun setFontScaleMultiplier(scale: Float) {
            _fontScale.value = scale
        }

        override suspend fun setCurrencySymbol(symbol: String) {
            _currency.value = symbol
        }

        override suspend fun setLanguage(lang: String) {
            _language.value = lang
        }

        override suspend fun incrementAppOpenCount() {
            _openCount.value += 1
        }

        override suspend fun setHapticFeedbackEnabled(enabled: Boolean) {
            _hapticEnabled.value = enabled
        }
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun preferencesRepository_hapticFeedbackDefaultsToTrue() = runBlocking {
        val repo = TestPreferencesRepository()
        assertTrue("Haptic feedback should be enabled by default", repo.hapticFeedbackEnabled.first())
    }

    @Test
    fun preferencesRepository_canToggleHapticFeedback() = runBlocking {
        val repo = TestPreferencesRepository()
        assertTrue(repo.hapticFeedbackEnabled.first())

        repo.setHapticFeedbackEnabled(false)
        assertFalse("Haptic feedback should be disabled after setting false", repo.hapticFeedbackEnabled.first())

        repo.setHapticFeedbackEnabled(true)
        assertTrue("Haptic feedback should be enabled after setting true", repo.hapticFeedbackEnabled.first())
    }

    @Test
    fun mainViewModel_exposesHapticFeedbackStateAndUpdatesCorrectly() = runBlocking {
        val repo = TestPreferencesRepository()
        val viewModel = MainViewModel(repo)

        assertTrue(viewModel.hapticFeedbackEnabled.first())

        viewModel.setHapticFeedbackEnabled(false)
        assertEquals(false, viewModel.hapticFeedbackEnabled.first { !it })

        viewModel.setHapticFeedbackEnabled(true)
        assertEquals(true, viewModel.hapticFeedbackEnabled.first { it })
    }
}
