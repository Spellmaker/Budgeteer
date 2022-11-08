package moe.chen.budgeteer.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import moe.chen.budgeteer.room.Category
import moe.chen.budgeteer.room.CategoryDao
import javax.inject.Inject

@HiltViewModel
class AddCategoryViewModel @Inject constructor(
    private val categoryRepository: CategoryDao
): ViewModel() {

    fun addCategory(
        name: String,
        budget: Double,
        user: Int,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            categoryRepository.createCategory(Category(label = name, budget = budget, uid = user))
        }
    }

    fun updateCategory(
        id: Int,
        user: Int,
        name: String,
        budget: Double,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            categoryRepository.updateCategory(Category(
                cid = id,
                label = name,
                budget = budget,
                uid = user,
            ))
        }
    }
}