package moe.chen.budgeteer.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import moe.chen.budgeteer.room.BudgetEntryDao
import moe.chen.budgeteer.room.Category
import moe.chen.budgeteer.room.CategoryBudget
import moe.chen.budgeteer.room.CategoryBudgetDao
import moe.chen.budgeteer.room.CategoryDao
import java.time.ZonedDateTime
import javax.inject.Inject

@HiltViewModel
class OverviewViewModel @Inject constructor(
    private val categoryDao: CategoryDao,
    private val entryDao: BudgetEntryDao,
    private val categoryBudgetDao: CategoryBudgetDao,
) : ViewModel() {

    fun debug() {
        viewModelScope.launch {
            Log.d("OverviewViewModel", "Debug hook called")
            categories.value.forEach {
                val budgets = categoryBudgetDao.listBudgets(it.cid!!).firstOrNull()
                Log.d("OverviewViewModel", "Category $it size: ${budgets?.size}")
                budgets?.forEach { budget ->
                    Log.d("OverviewViewModel", "$budget")
                }
            }

            categoryBudgetDao.updateBudget(CategoryBudget(
                id = 1,
                budget = 250.0,
                cid = 1,
                year = 2022,
                month = 11,
            ))
            categoryBudgetDao.updateBudget(CategoryBudget(
                id = 2,
                budget = 80.0,
                cid = 2,
                year = 2022,
                month = 12,
            ))
        }
    }

    private val _categories = MutableStateFlow<List<Category>>(emptyList())

    private val userId: Int = 0

    val categories = _categories.asStateFlow()

    init {
        viewModelScope.launch {
            categoryDao.listCategories(userId)
                .distinctUntilChanged()
                .collect { entries ->
                    Log.d("OverviewViewModel", "listen for user $userId")
                    _categories.value = entries
                }
        }
    }

    fun categoryEntryFlow(category: Category, month: ZonedDateTime) =
        entryDao.listEntries(category.cid!!, month.monthValue, month.year)

    fun categoryBudgetFlow(category: Category, month: ZonedDateTime) =
        categoryBudgetDao.getActiveBudget(category.cid!!, month.year, month.monthValue)

    fun saveCategories(categories: List<Category>) {
        viewModelScope.launch { categoryDao.updateCategories(categories) }
    }
}