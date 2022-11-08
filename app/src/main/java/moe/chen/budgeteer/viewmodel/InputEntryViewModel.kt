package moe.chen.budgeteer.viewmodel

import android.util.Log
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
) : ViewModel() {

    private val _category = MutableStateFlow<Category?>(null)

    val category = _category.asStateFlow()

    private var currentId: Int? = null

    fun listenForCategory(category: Int) {
        viewModelScope.launch {
            currentId = category
            categoryDao.findCategory(category)
                .takeWhile { currentId == category }
                .distinctUntilChanged()
                .collect { category -> _category.value = category }
        }
    }

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