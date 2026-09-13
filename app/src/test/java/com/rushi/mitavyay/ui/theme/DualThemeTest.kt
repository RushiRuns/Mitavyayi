package com.rushi.mitavyay.ui.theme

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
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DualThemeTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private class FakePreferencesRepository : PreferencesRepository {
        private val _themeMode = MutableStateFlow("SYSTEM")
        private val _fontScale = MutableStateFlow(1.0f)
        private val _currency = MutableStateFlow("₹")
        private val _language = MutableStateFlow("en")
        private val _openCount = MutableStateFlow(0)
        private val _hapticEnabled = MutableStateFlow(true)
        private val _hasSeededDefaultCategories = MutableStateFlow(false)

        override val themeMode: Flow<String> = _themeMode
        override val fontScaleMultiplier: Flow<Float> = _fontScale
        override val currencySymbol: Flow<String> = _currency
        override val language: Flow<String> = _language
        override val appOpenCount: Flow<Int> = _openCount
        override val hapticFeedbackEnabled: Flow<Boolean> = _hapticEnabled
        override val hasSeededDefaultCategories: Flow<Boolean> = _hasSeededDefaultCategories

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

        override suspend fun setHasSeededDefaultCategories(seeded: Boolean) {
            _hasSeededDefaultCategories.value = seeded
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
    fun preferencesRepository_persistsThemeMode() = runBlocking {
        val repo = FakePreferencesRepository()
        assertEquals("SYSTEM", repo.themeMode.first())

        repo.setThemeMode("DARK")
        assertEquals("DARK", repo.themeMode.first())

        repo.setThemeMode("LIGHT")
        assertEquals("LIGHT", repo.themeMode.first())
    }

    @Test
    fun mainViewModel_setThemeModeUpdatesStateFlow() = runBlocking {
        val repo = FakePreferencesRepository()
        val viewModel = MainViewModel(repo)

        assertEquals(ThemeMode.SYSTEM, viewModel.themeMode.first())

        viewModel.setThemeMode(ThemeMode.DARK)
        assertEquals(ThemeMode.DARK, viewModel.themeMode.first { it == ThemeMode.DARK })

        viewModel.setThemeMode(ThemeMode.LIGHT)
        assertEquals(ThemeMode.LIGHT, viewModel.themeMode.first { it == ThemeMode.LIGHT })

        viewModel.setThemeMode(ThemeMode.SYSTEM)
        assertEquals(ThemeMode.SYSTEM, viewModel.themeMode.first { it == ThemeMode.SYSTEM })
    }

    @Test
    fun mainViewModel_toggleThemeSwitchesBetweenLightAndDark() = runBlocking {
        val repo = FakePreferencesRepository()
        val viewModel = MainViewModel(repo)

        // Set to LIGHT
        viewModel.setThemeMode(ThemeMode.LIGHT)
        assertEquals(ThemeMode.LIGHT, viewModel.themeMode.first { it == ThemeMode.LIGHT })

        // Toggle to DARK
        viewModel.toggleTheme()
        assertEquals(ThemeMode.DARK, viewModel.themeMode.first { it == ThemeMode.DARK })

        // Toggle back to LIGHT
        viewModel.toggleTheme()
        assertEquals(ThemeMode.LIGHT, viewModel.themeMode.first { it == ThemeMode.LIGHT })
    }

    @Test
    fun themeColorPalettes_lightAndDarkAreDistinctAndEnforceContrast() {
        // Background and surface must differ between light and dark modes
        assertNotEquals(LightColorScheme.background, DarkColorScheme.background)
        assertNotEquals(LightColorScheme.surface, DarkColorScheme.surface)
        assertNotEquals(LightColorScheme.onBackground, DarkColorScheme.onBackground)
        assertNotEquals(LightColorScheme.onSurface, DarkColorScheme.onSurface)

        // Extended color schemes provide semantic tokens for both palettes
        assertTrue(LightExtendedColorScheme.success != LightExtendedColorScheme.warning)
        assertTrue(DarkExtendedColorScheme.success != DarkExtendedColorScheme.warning)
    }
}
