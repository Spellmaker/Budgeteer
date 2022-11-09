package moe.chen.budgeteer.screens

import android.util.Log
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import kotlinx.coroutines.flow.Flow
import moe.chen.budgeteer.data.*
import moe.chen.budgeteer.defaultNumberFormat
import moe.chen.budgeteer.navigation.BudgeteerScreens
import moe.chen.budgeteer.preview.exampleCategories
import moe.chen.budgeteer.preview.exampleEntries
import moe.chen.budgeteer.room.BudgetEntry
import moe.chen.budgeteer.room.Category
import moe.chen.budgeteer.room.User
import moe.chen.budgeteer.viewmodel.OverviewViewModel
import moe.chen.budgeteer.widgets.MainViewWidget

@Composable
fun OverviewScreen(
    navController: NavController,
    user: User,
    logout: () -> Unit,
) {
    val model = hiltViewModel<OverviewViewModel>()
    model.listenForUser(user)

    val categories = model.categories.collectAsState()

    OverviewWidget(
        logout = logout,
        categories = categories.value,
        onAddCategory = {
            navController.navigate(BudgeteerScreens.AddCategoryScreen.name)
        },
        getCategoryFlow = { model.categoryEntryFlow(it) },
        clickCategory = {
            navController
                .navigate("${BudgeteerScreens.ExpenseInputScreen.name}/${it.cid!!}")
        },
        longPress = {
            navController
                .navigate("${BudgeteerScreens.CategoryScreen.name}/${it.cid!!}")
        }
    )
}

@Composable
fun OverviewWidget(
    categories: List<Category> = exampleCategories(),
    getCategoryFlow: (Category) -> Flow<List<BudgetEntry>>,
    onAddCategory: () -> Unit = {},
    clickCategory: (Category) -> Unit = {},
    longPress: (Category) -> Unit = {},
    logout: () -> Unit,
) {
    MainViewWidget(logout = logout) {
        Column(
            modifier = Modifier.padding(it)
        ) {
            CategoryListWidget(categories, getCategoryFlow, clickCategory, longPress)
            Button(
                onClick = onAddCategory, modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp)
            ) {
                Text("Add new category")
            }
        }
    }
}

@Composable
fun CategoryListWidget(
    categories: List<Category> = exampleCategories(),
    getCategoryFlow: (Category) -> Flow<List<BudgetEntry>>,
    clickCategory: (Category) -> Unit = {},
    longPress: (Category) -> Unit = {},
) {

    LazyColumn {
        items(items = categories) {
            val entries = getCategoryFlow(it).collectAsState(initial = emptyList()).value

            Log.d("OverviewScreen", "cat $it entries in screen: $entries")

            CategoryRow(
                category = it,
                entries = entries,
                clicked = { clickCategory(it) },
                longPress = { longPress(it) },
            )
        }
    }
}

@Composable
fun CategoryRow(
    category: Category,
    entries: List<BudgetEntry>,
    clicked: () -> Unit,
    longPress: () -> Unit,
) {
    Card(
        modifier = Modifier
            .padding(4.dp)
            .fillMaxWidth()
            .height(150.dp)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { clicked() },
                    onLongPress = { longPress() }
                )
            }
        ,
        shape = RoundedCornerShape(corner = CornerSize(14.dp)),
        elevation = 4.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(20.dp),
                verticalArrangement = Arrangement.Center,
            ) {
                Text(category.label, style = MaterialTheme.typography.h6)
            }
            Column(
                modifier = Modifier
                    .width(200.dp)
                    .fillMaxHeight()
                    .padding(5.dp),
                verticalArrangement = Arrangement.Center
            ) {
                CategorySummary(category = category, entries = entries)
            }
        }

    }
}

@Composable
fun CategorySummary(
    category: Category,
    entries: List<BudgetEntry> = exampleEntries(),
    fields: List<ComputedField> = listOf(
        CurrentField,
        BudgetField,
        TrendField,
        SpendPerDayField,
    )
) {
    val computed = fields.map { field ->
        val value = field.computation(category, entries)
        val color = field.colorSelector(category, value)
        Triple(value, color, field)
    }


    Row(modifier = Modifier.padding(5.dp)) {
        Column {
            computed.forEach { Text(it.third.label, color = it.second) }
        }
        Spacer(modifier = Modifier.width(10.dp))

        Column {
            computed.forEach { Text(defaultNumberFormat.format(it.first), color = it.second) }
        }
    }
}