package moe.chen.budgeteer.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import moe.chen.budgeteer.room.BudgetEntryDao
import moe.chen.budgeteer.room.Category
import moe.chen.budgeteer.room.CategoryDao
import javax.inject.Inject

@HiltViewModel
class AddCategoryViewModel @Inject constructor(
    private val categoryRepository: CategoryDao,
    savedStateHandle: SavedStateHandle,
    private val categoryDao: CategoryDao,
    private val entryDao: BudgetEntryDao,
) : ViewModel() {

    val invalidCategory = Category(42, "invalid", 0.0, 42)

    private val categoryId: Int? = savedStateHandle.get<Int>("category")

    private val _category = MutableStateFlow<Category?>(invalidCategory)

    val category = _category.asStateFlow()

    init {
        if (categoryId != null && categoryId >= 0) {
            viewModelScope.launch {
                categoryDao.findCategory(categoryId)
                    .distinctUntilChanged()
                    .collect { category -> _category.value = category }
            }
        } else {
            _category.value = null
        }
    }

    fun addCategory(
        name: String,
        budget: Double,
        user: Int,
    ) {
        viewModelScope.launch {
            categoryRepository.createCategory(Category(label = name, budget = budget, uid = user))
        }
    }

    fun updateCategory(
        id: Int,
        user: Int,
        name: String,
        budget: Double,
    ) {
        viewModelScope.launch {
            Log.d("AddCategoryViewModel", "update with new budget $budget")
            categoryRepository.updateCategory(
                Category(
                    cid = id,
                    label = name,
                    budget = budget,
                    uid = user,
                )
            )
        }
    }

    fun removeCategory() {
        categoryId?.let { id ->
            category.value?.let { c ->
                viewModelScope.launch {
                    entryDao.deleteAll(id)
                    categoryDao.deleteCategory(c)
                }
            }
        }
    }
}