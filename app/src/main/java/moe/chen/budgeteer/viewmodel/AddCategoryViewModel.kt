package moe.chen.budgeteer.viewmodel

import android.database.sqlite.SQLiteConstraintException
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
import moe.chen.budgeteer.room.CategoryBudget
import moe.chen.budgeteer.room.CategoryBudgetDao
import moe.chen.budgeteer.room.CategoryDao
import java.time.ZonedDateTime
import javax.inject.Inject

@HiltViewModel
class AddCategoryViewModel @Inject constructor(
    private val categoryRepository: CategoryDao,
    savedStateHandle: SavedStateHandle,
    private val categoryDao: CategoryDao,
    private val entryDao: BudgetEntryDao,
    private val budgetDao: CategoryBudgetDao,
) : ViewModel() {

    val invalidCategory = Category(42, "invalid", 42, null)

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

    fun getBudget(
        category: Category
    ) = budgetDao
        .getActiveBudget(category.cid!!, ZonedDateTime.now().year, ZonedDateTime.now().monthValue)

    fun updateOrSetBudget(
        budget: CategoryBudget?,
        newBudget: Double,
    ) {
        viewModelScope.launch {
            val today = ZonedDateTime.now()
            if (budget == null
                || budget.year != today.year
                || budget.month != today.monthValue
            ) {
                Log.d("AddCategoryViewModel", "Create budget for cid $categoryId")
                budgetDao.createBudget(CategoryBudget(
                    id = null,
                    budget = newBudget,
                    cid = categoryId!!,
                    year = today.year,
                    month = today.monthValue,
                ))
            } else {
                Log.d("AddCategoryViewModel", "Update budget for cid $categoryId")
                budgetDao.updateBudget(budget.copy(budget = newBudget))
            }
        }
    }

    fun addCategory(
        name: String,
        budget: Double,
        user: Int,
        callback: (Boolean) -> Unit,
    ) {
        viewModelScope.launch {
            try {
                val id = categoryRepository.createCategory(
                    Category(
                        label = name,
                        uid = user,
                        order = null
                    )
                )
                val now = ZonedDateTime.now()
                Log.d("AddCategoryViewModel", "Create budget for cid $id (fresh category)")
                budgetDao.createBudget(CategoryBudget(
                    id = null,
                    budget = budget,
                    cid = id.toInt(),
                    year = now.year,
                    month = now.monthValue,
                ))
                callback(true)
            } catch (e: SQLiteConstraintException) {
                Log.e("AddCategoryViewModel", e.message ?: "null")
                callback(false)
            }
        }
    }

    fun updateCategory(
        id: Int,
        user: Int,
        name: String,
        budget: Double,
        order: Int?,
        callback: (Boolean) -> Unit,
    ) {
        viewModelScope.launch {
            Log.d("AddCategoryViewModel", "update with new budget $budget")
            try {
                categoryRepository.updateCategory(
                    Category(
                        cid = id,
                        label = name,
                        uid = user,
                        order = order
                    )
                )
                callback(true)
            } catch (e: SQLiteConstraintException) {
                callback(false)
            }
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