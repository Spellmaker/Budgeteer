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
import moe.chen.budgeteer.room.BudgetEntryDao
import moe.chen.budgeteer.room.Category
import moe.chen.budgeteer.room.CategoryDao
import moe.chen.budgeteer.room.User
import java.time.ZonedDateTime
import javax.inject.Inject

@HiltViewModel
class OverviewViewModel @Inject constructor(
    private val categoryDao: CategoryDao,
    private val entryDao: BudgetEntryDao,
) : ViewModel() {

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    private var selectedUser: User? = null

    val categories = _categories.asStateFlow()

    fun categoryEntryFlow(category: Category) =
        entryDao.listEntries(category.cid!!, ZonedDateTime.now().monthValue)

    fun listenForUser(user: User) {
        viewModelScope.launch {
            selectedUser = user
            categoryDao.listCategories(user.uid!!)
                .takeWhile { selectedUser?.uid == user.uid }
                .distinctUntilChanged()
                .collect { entries ->
                    Log.d("OverviewViewModel", "listen for user $user")
                    _categories.value = entries
                }
        }
    }
}