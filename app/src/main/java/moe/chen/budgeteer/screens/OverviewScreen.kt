package moe.chen.budgeteer.screens

import android.util.Log
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import kotlinx.coroutines.flow.Flow
import moe.chen.budgeteer.R
import moe.chen.budgeteer.data.*
import moe.chen.budgeteer.navigation.BudgeteerScreens
import moe.chen.budgeteer.preview.exampleCategories
import moe.chen.budgeteer.preview.exampleEntries
import moe.chen.budgeteer.room.BudgetEntry
import moe.chen.budgeteer.room.Category
import moe.chen.budgeteer.viewmodel.OverviewViewModel
import moe.chen.budgeteer.viewmodel.UserSettingViewModel
import moe.chen.budgeteer.widgets.MainViewWidget
import java.time.ZonedDateTime

@Composable
fun OverviewScreen(
    navController: NavController,
) {
    val model = hiltViewModel<OverviewViewModel>()
    val settingsModel = hiltViewModel<UserSettingViewModel>()

    val settings = settingsModel.settings.collectAsState()
    if (settings.value == null) {
        settingsModel.createDefault()
    } else if (settings.value != settingsModel.invalidSettings) {
        val categories = model.categories.collectAsState()
        val converter = settingsModel.converterDefault.collectAsState()

        OverviewWidget(
            navController = navController,
            categories = categories.value,
            onAddCategory = {
                navController.navigate(
                    BudgeteerScreens.AddCategoryScreen.name +
                            "/0/-1"
                )
            },
            getCategoryFlow = { model.categoryEntryFlow(it) },
            clickCategory = {
                navController
                    .navigate("${BudgeteerScreens.ExpenseInputScreen.name}/${it.cid!!}")
            },
            longPress = {
                navController
                    .navigate(
                        BudgeteerScreens.CategoryScreen.name +
                                "/0/${it.cid!!}"
                    )
            },
            formatter = { (converter.value?.format(it) ?: it.toString()) },
            accessSettings = {
                navController.navigate(
                    "${BudgeteerScreens.UserSettingsScreen.name}/0"
                )
            },
            fields = allCategories.map { it to it.extractor(settings.value!!) }
                .filter { it.second >= 0 }
                .sortedBy { it.second }
                .map { it.first }
        )
    }
}

@Composable
fun OverviewWidget(
    navController: NavController,
    categories: List<Category> = exampleCategories(),
    getCategoryFlow: (Category) -> Flow<List<BudgetEntry>>,
    onAddCategory: () -> Unit = {},
    clickCategory: (Category) -> Unit = {},
    longPress: (Category) -> Unit = {},
    accessSettings: () -> Unit,
    fields: List<ComputedField>,
    formatter: @Composable (Double) -> String,
) {
    MainViewWidget(navController = navController) {
        if (categories.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    modifier = Modifier.padding(5.dp),
                    text = stringResource(id = R.string.no_categories)
                )

                FabColumn(
                    accessSettings = accessSettings,
                    onAddCategory = onAddCategory,
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(it)
                    .fillMaxHeight()
            ) {
                Scaffold(
                    content = { padding ->
                        CategoryListWidget(
                            padding,
                            categories,
                            getCategoryFlow,
                            clickCategory,
                            longPress,
                            fields,
                            formatter,
                        )
                    },
                    bottomBar = {
                        FabColumn(
                            accessSettings = accessSettings,
                            onAddCategory = onAddCategory,
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun FabColumn(
    accessSettings: () -> Unit,
    onAddCategory: () -> Unit,
) {
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
            FloatingActionButton(onClick = accessSettings) {
                Icon(
                    Icons.Rounded.Settings,
                    contentDescription = stringResource(R.string.operation_settings)
                )
            }
            FloatingActionButton(onClick = onAddCategory) {
                Icon(
                    Icons.Rounded.Add,
                    contentDescription = stringResource(R.string.operation_add_category)
                )

            }
        }
    }

}

private val dummyCategory = Category(
    cid = null,
    label = "",
    budget = 0.0,
    uid = -1,
)

@Composable
fun CategoryListWidget(
    paddingValues: PaddingValues,
    categories: List<Category> = exampleCategories(),
    getCategoryFlow: (Category) -> Flow<List<BudgetEntry>>,
    clickCategory: (Category) -> Unit = {},
    longPress: (Category) -> Unit = {},
    fields: List<ComputedField>,
    formatter: @Composable (Double) -> String,
) {
    LazyColumn(modifier = Modifier) {
        items(items = categories.plus(dummyCategory)) {
            if (it == dummyCategory) {
                Column(modifier = Modifier.height(100.dp)) {

                }
            } else {
                val entries = getCategoryFlow(it).collectAsState(initial = emptyList()).value

                Log.d("OverviewScreen", "cat $it entries in screen: $entries")

                CategoryRow(
                    category = it,
                    entries = entries,
                    clicked = { clickCategory(it) },
                    longPress = { longPress(it) },
                    formatter = formatter,
                    fields = fields
                )
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
fun RowPreview() {
    val converter = UserSettingViewModel.makeConverter("EUR")
    CategoryRow(
        category = Category(
            cid = 0,
            label = "Test",
            budget = 50.0,
            uid = 0,
        ),
        entries = listOf(
            BudgetEntry(
                bid = 0,
                amount = 10.0,
                cid = 0,
                date = ZonedDateTime.now(),
            )
        ),
        clicked = {},
        longPress = {},
        fields = listOf(BudgetField, CurrentField, TrendField, SpendPerDayField),
        formatter = { converter.format(it) }
    )
}

@Composable
fun CategoryRow(
    category: Category,
    entries: List<BudgetEntry>,
    clicked: () -> Unit,
    longPress: () -> Unit,
    fields: List<ComputedField>,
    formatter: @Composable (Double) -> String,
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
            },
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
                    .fillMaxWidth(0.5f)
                    .padding(20.dp),
                verticalArrangement = Arrangement.Center,
            ) {
                Text(category.label, style = MaterialTheme.typography.h6)
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .padding(5.dp),
                verticalArrangement = Arrangement.Center
            ) {
                CategorySummary(
                    category = category,
                    entries = entries,
                    formatter = formatter,
                    fields = fields,
                )
            }
        }

    }
}

@Composable
fun CategorySummary(
    category: Category,
    entries: List<BudgetEntry> = exampleEntries(),
    fields: List<ComputedField>,
    formatter: @Composable (Double) -> String,
) {
    val computed = fields.map { field ->
        val value = field.computation(category, entries)
        val color = field.colorSelector(category, value, isSystemInDarkTheme())
        Triple(value, color, field)
    }

    Row(
        modifier = Modifier
            .padding(5.dp)
            .fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth(0.6f)) {
            computed.forEach { Text(stringResource(it.third.label), color = it.second) }
        }
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.End,
        ) {
            computed.forEach { Text(formatter(it.first), color = it.second) }
        }
    }
}