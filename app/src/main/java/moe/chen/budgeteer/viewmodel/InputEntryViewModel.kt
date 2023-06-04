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
import moe.chen.budgeteer.room.LabelRecommendation
import moe.chen.budgeteer.room.LabelRecommendationDao
import java.time.ZonedDateTime
import javax.inject.Inject

@HiltViewModel
class InputEntryViewModel @Inject constructor(
    private val categoryDao: CategoryDao,
    private val entryDao: BudgetEntryDao,
    private val labelDao: LabelRecommendationDao,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val invalidEntry = BudgetEntry(
        bid = 42,
        amount = -42.0,
        cid = 42,
        date = ZonedDateTime.now(),
        label = null,
    )

    private val categoryId: Int = savedStateHandle.get<Int>("category")!!

    private val budgetId: Int? = savedStateHandle.get<Int>("entry")

    private val _category = MutableStateFlow<Category?>(null)

    private val _entry = MutableStateFlow<BudgetEntry?>(invalidEntry)

    private val _recommendations = MutableStateFlow<List<LabelRecommendation>>(emptyList())

    val entry = _entry.asStateFlow()

    val labels = _recommendations.asStateFlow()

    init {
        Log.d("InputEntryViewModel", "launching things in scope")
        viewModelScope.launch {
            currentId = categoryId
            categoryDao.findCategory(categoryId)
                .takeWhile { currentId == categoryId }
                .distinctUntilChanged()
                .collect { category -> _category.value = category }
        }
        viewModelScope.launch {
            currentId = categoryId
            labelDao
                .getForCategory(categoryId)
                .takeWhile { currentId == categoryId }
                .distinctUntilChanged()
                .collect { labels ->
                    Log.d("InputEntryViewModel", "emitting labels $labels")
                    _recommendations.value = labels
                }
        }
        if (budgetId != null && budgetId >= 0) {
            viewModelScope.launch {
                entryDao.findEntry(budgetId)
                    .distinctUntilChanged()
                    .collect { entry -> _entry.value = entry }
            }
        } else {
            _entry.value = null
        }
    }

    val category = _category.asStateFlow()

    private var currentId: Int? = null

    fun addOrModifyEntry(amount: Double, label: String?) {
        viewModelScope.launch {
            val existing = entry.value

            val id = currentId
            if (id != null) {
                if (existing != null) {
                    Log.d(
                        "InputEntryViewModel", "modify entry with id ${existing.bid} " +
                                "amount $amount for category $currentId"
                    )
                    entryDao.updateEntry(
                        BudgetEntry(
                            bid = existing.bid,
                            amount = amount,
                            cid = existing.cid,
                            date = existing.date,
                            label = label?.trim()
                        )
                    )
                } else {
                    Log.d("InputEntryViewModel", "add amount $amount to category $currentId")
                    entryDao.createEntry(
                        BudgetEntry(
                            bid = null,
                            amount = amount,
                            cid = id,
                            date = ZonedDateTime.now(),
                            label = label?.trim(),
                        )
                    )
                }
                // updated recommendations
                if (!label.isNullOrBlank()) {
                    val currentLabels = labels.value
                    val result = mutableListOf<String>()
                    result.add(label.trim())
                    result.addAll(currentLabels.map { it.label }.filter { it != label.trim() } )

                    Log.d("InputEntryViewModel", "created labels: $result")

                    labelDao.deleteAll(id)
                    labelDao.storeAll(
                        result.take(10).mapIndexed { index, l ->
                            LabelRecommendation(
                                id = null,
                                cid = id,
                                label = l,
                                priority = index
                            )
                        }
                    )
                }
            }
        }
    }
}