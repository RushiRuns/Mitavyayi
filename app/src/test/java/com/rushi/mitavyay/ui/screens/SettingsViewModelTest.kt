package com.rushi.mitavyay.ui.screens

import com.rushi.mitavyay.data.datastore.PreferencesRepository
import com.rushi.mitavyay.ui.screens.Settings.SettingsViewModel
import com.rushi.mitavyay.ui.theme.ThemeMode
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
class SettingsViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var fakePrefsRepo: FakePreferencesRepository
    private lateinit var viewModel: SettingsViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakePrefsRepo = FakePreferencesRepository()
        viewModel = SettingsViewModel(fakePrefsRepo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private class FakePreferencesRepository : PreferencesRepository {
        val themeFlow = MutableStateFlow("SYSTEM")
        val fontScaleFlow = MutableStateFlow(1.0f)
        val currencyFlow = MutableStateFlow("₹")
        val languageFlow = MutableStateFlow("en")
        val appOpenCountFlow = MutableStateFlow(1)
        val hapticFlow = MutableStateFlow(true)
        val hasSeededFlow = MutableStateFlow(false)

        override val themeMode: Flow<String> = themeFlow
        override val fontScaleMultiplier: Flow<Float> = fontScaleFlow
        override val currencySymbol: Flow<String> = currencyFlow
        override val language: Flow<String> = languageFlow
        override val appOpenCount: Flow<Int> = appOpenCountFlow
        override val hapticFeedbackEnabled: Flow<Boolean> = hapticFlow
        override val hasSeededDefaultCategories: Flow<Boolean> = hasSeededFlow

        override suspend fun setThemeMode(mode: String) {
            themeFlow.value = mode
        }

        override suspend fun setFontScaleMultiplier(scale: Float) {
            fontScaleFlow.value = scale
        }

        override suspend fun setCurrencySymbol(symbol: String) {
            currencyFlow.value = symbol
        }

        override suspend fun setLanguage(lang: String) {
            languageFlow.value = lang
        }

        override suspend fun incrementAppOpenCount() {
            appOpenCountFlow.value += 1
        }

        override suspend fun setHapticFeedbackEnabled(enabled: Boolean) {
            hapticFlow.value = enabled
        }

        override suspend fun setHasSeededDefaultCategories(seeded: Boolean) {
            hasSeededFlow.value = seeded
        }
    }

    @Test
    fun testInitialState() = runBlocking {
        assertEquals(ThemeMode.SYSTEM, viewModel.themeMode.first())
        assertTrue(viewModel.hapticFeedbackEnabled.first())
        assertEquals(1.0f, viewModel.fontScale.first())
    }

    @Test
    fun testSetThemeMode() = runBlocking {
        viewModel.setThemeMode(ThemeMode.DARK)
        assertEquals(ThemeMode.DARK, viewModel.themeMode.first())
        assertEquals("DARK", fakePrefsRepo.themeFlow.value)

        viewModel.setThemeMode(ThemeMode.LIGHT)
        assertEquals(ThemeMode.LIGHT, viewModel.themeMode.first())
        assertEquals("LIGHT", fakePrefsRepo.themeFlow.value)
    }

    @Test
    fun testSetHapticFeedbackEnabled() = runBlocking {
        viewModel.setHapticFeedbackEnabled(false)
        assertFalse(viewModel.hapticFeedbackEnabled.first())
        assertFalse(fakePrefsRepo.hapticFlow.value)

        viewModel.setHapticFeedbackEnabled(true)
        assertTrue(viewModel.hapticFeedbackEnabled.first())
        assertTrue(fakePrefsRepo.hapticFlow.value)
    }
}
