package moe.chen.budgeteer.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.launch
import moe.chen.budgeteer.room.BudgetEntry
import moe.chen.budgeteer.room.BudgetEntryDao
import moe.chen.budgeteer.room.Category
import moe.chen.budgeteer.room.CategoryDao
import java.time.ZonedDateTime
import javax.inject.Inject

@HiltViewModel
class InputEntryViewModel @Inject constructor(
    private val categoryDao: CategoryDao,
    private val entryDao: BudgetEntryDao,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val categoryId: Int = savedStateHandle.get<Int>("category")!!

    private val _category = MutableStateFlow<Category?>(null)

    init {
        viewModelScope.launch {
            currentId = categoryId
            categoryDao.findCategory(categoryId)
                .takeWhile { currentId == categoryId }
                .distinctUntilChanged()
                .collect { category -> _category.value = category }
        }
    }

    val category = _category.asStateFlow()

    private var currentId: Int? = null

    fun addEntry(amount: Double) {
        viewModelScope.launch {
            Log.d("InputEntryViewModel", "add amount $amount to category $currentId")
            val id = currentId
            if (id != null) {
                Log.d("InputEntryViewModel", "actually creating entry")
                entryDao.createEntry(
                    BudgetEntry(
                        bid = null,
                        amount = amount,
                        cid = id,
                        date = ZonedDateTime.now(),
                    )
                )
                Log.d("InputEntryViewModel", "" + entryDao.findAllEntries())

            }
        }
    }
}