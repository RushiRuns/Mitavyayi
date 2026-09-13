package com.rushi.mitavyay.data

import com.rushi.mitavyay.data.datastore.PreferencesRepository
import com.rushi.mitavyay.data.db.Category
import com.rushi.mitavyay.data.db.CategoryDao
import com.rushi.mitavyay.data.repository.CategoryRepositoryImpl
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CategoryRepositoryTest {

    private class FakePreferencesRepository : PreferencesRepository {
        private val _themeMode = MutableStateFlow("SYSTEM")
        private val _fontScale = MutableStateFlow(1.0f)
        private val _currency = MutableStateFlow("₹")
        private val _language = MutableStateFlow("en")
        private val _openCount = MutableStateFlow(0)
        private val _hapticEnabled = MutableStateFlow(true)
        val hasSeededFlow = MutableStateFlow(false)

        override val themeMode: Flow<String> = _themeMode
        override val fontScaleMultiplier: Flow<Float> = _fontScale
        override val currencySymbol: Flow<String> = _currency
        override val language: Flow<String> = _language
        override val appOpenCount: Flow<Int> = _openCount
        override val hapticFeedbackEnabled: Flow<Boolean> = _hapticEnabled
        override val hasSeededDefaultCategories: Flow<Boolean> = hasSeededFlow

        override suspend fun setThemeMode(mode: String) { _themeMode.value = mode }
        override suspend fun setFontScaleMultiplier(scale: Float) { _fontScale.value = scale }
        override suspend fun setCurrencySymbol(symbol: String) { _currency.value = symbol }
        override suspend fun setLanguage(lang: String) { _language.value = lang }
        override suspend fun incrementAppOpenCount() { _openCount.value += 1 }
        override suspend fun setHapticFeedbackEnabled(enabled: Boolean) { _hapticEnabled.value = enabled }
        override suspend fun setHasSeededDefaultCategories(seeded: Boolean) { hasSeededFlow.value = seeded }
    }

    private class FakeCategoryDao : CategoryDao {
        val list = mutableListOf<Category>()
        private val state = MutableStateFlow<List<Category>>(emptyList())

        private fun notifyChange() {
            state.value = list.toList()
        }

        override suspend fun insert(category: Category) {
            list.removeAll { it.id == category.id }
            list.add(category)
            notifyChange()
        }

        override suspend fun insertAll(categories: List<Category>) {
            categories.forEach { cat ->
                if (list.none { it.id == cat.id }) {
                    list.add(cat)
                }
            }
            notifyChange()
        }

        override suspend fun update(category: Category) {
            list.removeAll { it.id == category.id }
            list.add(category)
            notifyChange()
        }

        override suspend fun delete(category: Category) {
            list.removeAll { it.id == category.id }
            notifyChange()
        }

        override suspend fun deleteById(id: String) {
            list.removeAll { it.id == id }
            notifyChange()
        }

        override suspend fun getById(id: String): Category? = list.find { it.id == id }

        override fun getAll(): Flow<List<Category>> = state.map { l ->
            l.sortedWith(compareBy<Category> { it.isCustom }.thenBy { it.name })
        }

        override suspend fun getByName(name: String): Category? =
            list.find { it.name.equals(name, ignoreCase = true) }

        override fun getCustomCategories(): Flow<List<Category>> = state.map { l ->
            l.filter { it.isCustom }.sortedBy { it.name }
        }
    }

    private lateinit var fakeDao: FakeCategoryDao
    private lateinit var fakePreferencesRepository: FakePreferencesRepository
    private lateinit var repository: CategoryRepositoryImpl

    @Before
    fun setUp() {
        fakeDao = FakeCategoryDao()
        fakePreferencesRepository = FakePreferencesRepository()
        repository = CategoryRepositoryImpl(fakeDao, fakePreferencesRepository)
    }

    @Test
    fun seedDefaultCategories_insertsAllDefaultCategories() = runBlocking {
        repository.seedDefaultCategories()

        val all = fakeDao.list
        assertEquals(11, all.size)
        assertTrue(all.none { it.isCustom })
        assertTrue(all.any { it.name == "Food & Dining" })
        assertTrue(all.any { it.name == "Groceries" })
        assertTrue(fakePreferencesRepository.hasSeededFlow.value)
    }

    @Test
    fun getAllCategories_triggersAutoSeedingWhenEmpty() = runBlocking {
        assertTrue(fakeDao.list.isEmpty())

        val categories = repository.getAllCategories().first { it.isNotEmpty() }
        assertEquals(11, categories.size)
        assertEquals(11, fakeDao.list.size)
        assertTrue(fakePreferencesRepository.hasSeededFlow.value)
    }

    @Test
    fun getAllCategories_doesNotAutoSeedWhenAlreadySeededEvenIfEmpty() = runBlocking {
        fakePreferencesRepository.setHasSeededDefaultCategories(true)
        assertTrue(fakeDao.list.isEmpty())

        val categories = repository.getAllCategories().first()
        assertTrue(categories.isEmpty())
        assertTrue(fakeDao.list.isEmpty())
    }

    @Test
    fun createCustomCategory_success() = runBlocking {
        repository.seedDefaultCategories()

        val result = repository.createCustomCategory(
            name = "Gaming",
            colorHex = "#9C27B0",
            icon = "star"
        )

        assertTrue(result.isSuccess)
        val created = result.getOrNull()
        assertNotNull(created)
        assertEquals("Gaming", created?.name)
        assertEquals("#9C27B0", created?.color)
        assertEquals("star", created?.icon)
        assertTrue(created!!.isCustom)

        val retrieved = repository.getCategoryByName("gaming")
        assertNotNull(retrieved)
        assertEquals("Gaming", retrieved?.name)
    }

    @Test
    fun createCustomCategory_failsOnBlankName() = runBlocking {
        val result = repository.createCustomCategory(
            name = "   ",
            colorHex = "#FF5722",
            icon = "star"
        )

        assertTrue(result.isFailure)
        assertEquals("Category name cannot be blank", result.exceptionOrNull()?.message)
    }

    @Test
    fun createCustomCategory_failsOnDuplicateName() = runBlocking {
        repository.seedDefaultCategories()

        // Attempt duplicate of default category (case-insensitive)
        val resultDefault = repository.createCustomCategory(
            name = "food & dining",
            colorHex = "#FF0000",
            icon = "star"
        )
        assertTrue(resultDefault.isFailure)
        assertTrue(resultDefault.exceptionOrNull()?.message?.contains("already exists") == true)

        // Attempt duplicate of custom category
        repository.createCustomCategory("Hobbies", "#123456", "star")
        val resultCustom = repository.createCustomCategory("hobbies", "#654321", "star")
        assertTrue(resultCustom.isFailure)
    }

    @Test
    fun updateCustomCategory_success() = runBlocking {
        val createResult = repository.createCustomCategory("Books", "#333333", "star")
        val categoryId = createResult.getOrThrow().id

        val updateResult = repository.updateCustomCategory(
            id = categoryId,
            name = "Reading & Books",
            colorHex = "#444444",
            icon = "favorite"
        )

        assertTrue(updateResult.isSuccess)
        val updated = repository.getCategoryById(categoryId)
        assertNotNull(updated)
        assertEquals("Reading & Books", updated?.name)
        assertEquals("#444444", updated?.color)
        assertEquals("favorite", updated?.icon)
    }

    @Test
    fun updateCustomCategory_succeedsForDefaultCategory() = runBlocking {
        repository.seedDefaultCategories()
        val defaultCat = fakeDao.list.first { !it.isCustom }

        val result = repository.updateCustomCategory(
            id = defaultCat.id,
            name = "Changed Name",
            colorHex = "#000000",
            icon = "star"
        )

        assertTrue(result.isSuccess)
        val updated = repository.getCategoryById(defaultCat.id)
        assertNotNull(updated)
        assertEquals("Changed Name", updated?.name)
        assertEquals("#000000", updated?.color)
    }

    @Test
    fun updateCustomCategory_failsOnDuplicateName() = runBlocking {
        val cat1 = repository.createCustomCategory("Music", "#111111", "star").getOrThrow()
        val cat2 = repository.createCustomCategory("Movies", "#222222", "star").getOrThrow()

        val result = repository.updateCustomCategory(
            id = cat2.id,
            name = "music",
            colorHex = "#222222",
            icon = "star"
        )

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("already exists") == true)
    }

    @Test
    fun deleteCategory_deletesBothDefaultAndCustomCategories() = runBlocking {
        repository.seedDefaultCategories()
        val defaultCat = fakeDao.list.first { !it.isCustom }

        // Attempt to delete default category
        assertTrue(repository.canDeleteCategory(defaultCat.id))
        repository.deleteCategory(defaultCat.id)
        assertNull(repository.getCategoryById(defaultCat.id))

        // Create and delete custom category
        val customCat = repository.createCustomCategory("Custom To Delete", "#555555", "star").getOrThrow()
        assertTrue(repository.canDeleteCategory(customCat.id))

        repository.deleteCategory(customCat.id)
        assertNull(repository.getCategoryById(customCat.id))
    }
}
