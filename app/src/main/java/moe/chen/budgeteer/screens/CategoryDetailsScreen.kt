package moe.chen.budgeteer.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import moe.chen.budgeteer.defaultNumberFormat
import moe.chen.budgeteer.room.BudgetEntry
import moe.chen.budgeteer.viewmodel.CategoryDetailViewModel
import moe.chen.budgeteer.widgets.MainViewWidget
import java.time.format.DateTimeFormatter

@Composable
fun CategoryDetailsScreen(
    navController: NavController,
    categoryId: Int,
    logout: () -> Unit,
) {
    val model = hiltViewModel<CategoryDetailViewModel>()
    model.listenForCategory(categoryId)
    val category = model.category.collectAsState()

    if (category.value != null) {
        val entries =
            model.categoryEntryFlow(category.value!!).collectAsState(initial = emptyList())

        MainViewWidget(logout = logout) {
            Column(
                modifier = Modifier.fillMaxHeight()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(category.value?.label ?: "null")
                }
                Spacer(modifier = Modifier.height(10.dp))
                LazyColumn {
                    items(items = entries.value.sortedByDescending { it.date }) {
                        ExpenseWidget(it) { model.removeEntry(it) }
                    }
                }
            }
        }
    }
}

@Composable
fun ExpenseWidget(
    item: BudgetEntry,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .padding(4.dp)
            .fillMaxWidth()
            .height(40.dp),
        shape = RoundedCornerShape(corner = CornerSize(14.dp)),
        elevation = 4.dp,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(DateTimeFormatter.ISO_ZONED_DATE_TIME.format(item.date))
            Text(defaultNumberFormat.format(item.amount))
        }
    }
}