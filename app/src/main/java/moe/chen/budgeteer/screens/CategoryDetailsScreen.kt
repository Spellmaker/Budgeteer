package moe.chen.budgeteer.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import moe.chen.budgeteer.navigation.BudgeteerScreens
import moe.chen.budgeteer.room.BudgetEntry
import moe.chen.budgeteer.room.User
import moe.chen.budgeteer.viewmodel.CategoryDetailViewModel
import moe.chen.budgeteer.viewmodel.UserSettingViewModel
import moe.chen.budgeteer.widgets.MainViewWidget
import java.time.format.DateTimeFormatter

@Composable
fun CategoryDetailsScreen(
    navController: NavController,
    categoryId: Int,
    user: User,
    logout: () -> Unit,
) {
    val model = hiltViewModel<CategoryDetailViewModel>()
    model.listenForCategory(categoryId)
    val category = model.category.collectAsState()

    val settingsModel = hiltViewModel<UserSettingViewModel>()

    val convertDefault = settingsModel.converterDefault.collectAsState()

    if (category.value != null) {
        val entries =
            model.categoryEntryFlow(category.value!!).collectAsState(initial = emptyList())

        MainViewWidget(logout = logout) {
            Column(
                modifier = Modifier.fillMaxHeight()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(category.value?.label ?: "null", modifier = Modifier.padding(5.dp))
                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(onClick = {
                            navController.navigate(
                                BudgeteerScreens.AddCategoryScreen.name +
                                        "/${user.uid!!}/${category.value?.cid}"
                            )
                        }, modifier = Modifier.padding(5.dp)) {
                            Text("Edit")
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                LazyColumn {
                    items(items = entries.value.sortedByDescending { it.date }) {
                        ExpenseWidget(
                            it,
                            { model.removeEntry(it) },
                            { n -> convertDefault.value?.format(n) ?: n.toString() })
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
    formatter: @Composable (Double) -> String,
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
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(DateTimeFormatter.ISO_ZONED_DATE_TIME.format(item.date))
            Text(formatter(item.amount))
        }
    }
}