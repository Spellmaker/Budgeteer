package moe.chen.budgeteer.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import moe.chen.budgeteer.R
import moe.chen.budgeteer.navigation.BudgeteerScreens
import moe.chen.budgeteer.room.BudgetEntry
import moe.chen.budgeteer.viewmodel.CategoryDetailViewModel
import moe.chen.budgeteer.viewmodel.UserSettingViewModel
import moe.chen.budgeteer.widgets.MainViewWidget
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

private val dateFormatter = DateTimeFormatter.ofPattern(
    "yyyy-MM-dd @ HH:mm"
)

@Composable
fun CategoryDetailsScreen(
    navController: NavController,
    categoryId: Int,
) {
    val model = hiltViewModel<CategoryDetailViewModel>()
    model.listenForCategory(categoryId)
    val category = model.category.collectAsState()

    val settingsModel = hiltViewModel<UserSettingViewModel>()

    val convertDefault = settingsModel.converterDefault.collectAsState()

    if (category.value != null) {
        val entries =
            model.categoryEntryFlow(category.value!!).collectAsState(initial = emptyList())

        MainViewWidget(navController = navController) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(5.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        category.value?.label ?: "null",
                        modifier = Modifier.padding(5.dp),
                        style = MaterialTheme.typography.h6
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                LazyColumn {
                    items(items = entries.value.sortedByDescending { it.date }) {
                        ExpenseWidget(
                            item = it,
                            onEdit = { navController.navigate(
                                BudgeteerScreens.ExpenseInputScreen.name + "/${it.cid}/${
                                    it.bid
                                }"
                            )},
                            onDelete = { model.removeEntry(it) },
                            formatter = { n -> convertDefault.value?.format(n) ?: n.toString() })
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth()
                        .padding(10.dp),
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        FloatingActionButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                Icons.Rounded.ArrowBack,
                                contentDescription = stringResource(R.string.operation_cancel)
                            )
                        }
                        FloatingActionButton(onClick = {
                            navController.navigate(
                                BudgeteerScreens.AddCategoryScreen.name +
                                        "/0/${category.value?.cid}"
                            )
                        }) {
                            Icon(
                                Icons.Rounded.Edit,
                                contentDescription = stringResource(R.string.operation_edit)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
fun ExpenseWidget(
    item: BudgetEntry = BudgetEntry(
        bid = 0,
        amount = 10.0,
        cid = 0,
        date = ZonedDateTime.now(),
        label = "Eine lange Schlange"
    ),
    onEdit: () -> Unit = {},
    onDelete: () -> Unit = {},
    formatter: @Composable (Double) -> String = { it.toString() },
) {
    Card(
        modifier = Modifier
            .padding(4.dp)
            .fillMaxWidth()
            .height(60.dp),
        shape = RoundedCornerShape(corner = CornerSize(14.dp)),
        elevation = 4.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(5.dp)
                .clickable(onClick = onEdit),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.Rounded.Delete,
                contentDescription = stringResource(R.string.operation_delete),
                modifier = Modifier
                    .padding(
                        PaddingValues(horizontal = 10.dp)
                    )
                    .clickable(onClick = onDelete),
            )
            Column(modifier = Modifier) {
                Text(
                    item.label ?: "",
                    modifier = Modifier
                )
                Text(
                    dateFormatter.format(item.date.toLocalDateTime()),
                    //DateTimeFormatter.ISO_ZONED_DATE_TIME.format(item.date),
                    modifier = Modifier
                )
            }
            Text(
                formatter(item.amount),
                modifier = Modifier.padding(
                    PaddingValues(horizontal = 10.dp)
                )
            )
        }
    }
}