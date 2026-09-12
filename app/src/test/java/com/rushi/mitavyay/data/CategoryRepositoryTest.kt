package com.rushi.mitavyay.data

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
    private lateinit var repository: CategoryRepositoryImpl

    @Before
    fun setUp() {
        fakeDao = FakeCategoryDao()
        repository = CategoryRepositoryImpl(fakeDao)
    }

    @Test
    fun seedDefaultCategories_insertsAllDefaultCategories() = runBlocking {
        repository.seedDefaultCategories()

        val all = fakeDao.list
        assertEquals(11, all.size)
        assertTrue(all.none { it.isCustom })
        assertTrue(all.any { it.name == "Food & Dining" })
        assertTrue(all.any { it.name == "Groceries" })
    }

    @Test
    fun getAllCategories_triggersAutoSeedingWhenEmpty() = runBlocking {
        assertTrue(fakeDao.list.isEmpty())

        val categories = repository.getAllCategories().first { it.isNotEmpty() }
        assertEquals(11, categories.size)
        assertEquals(11, fakeDao.list.size)
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
    fun updateCustomCategory_failsForDefaultCategory() = runBlocking {
        repository.seedDefaultCategories()
        val defaultCat = fakeDao.list.first { !it.isCustom }

        val result = repository.updateCustomCategory(
            id = defaultCat.id,
            name = "Changed Name",
            colorHex = "#000000",
            icon = "star"
        )

        assertTrue(result.isFailure)
        assertEquals("Default categories cannot be edited", result.exceptionOrNull()?.message)
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
    fun deleteCategory_onlyDeletesCustomCategory() = runBlocking {
        repository.seedDefaultCategories()
        val defaultCat = fakeDao.list.first { !it.isCustom }

        // Attempt to delete default category
        repository.deleteCategory(defaultCat.id)
        assertNotNull(repository.getCategoryById(defaultCat.id))
        assertFalse(repository.canDeleteCategory(defaultCat.id))

        // Create and delete custom category
        val customCat = repository.createCustomCategory("Custom To Delete", "#555555", "star").getOrThrow()
        assertTrue(repository.canDeleteCategory(customCat.id))

        repository.deleteCategory(customCat.id)
        assertNull(repository.getCategoryById(customCat.id))
    }
}
