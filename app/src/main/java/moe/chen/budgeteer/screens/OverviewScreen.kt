package moe.chen.budgeteer.screens

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
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
import moe.chen.budgeteer.room.CategoryBudget
import moe.chen.budgeteer.ui.theme.NotOkColor
import moe.chen.budgeteer.ui.theme.NotOkColorDarkMode
import moe.chen.budgeteer.viewmodel.OverviewViewModel
import moe.chen.budgeteer.viewmodel.UserSettingViewModel
import moe.chen.budgeteer.widgets.EFloatingActionButton
import moe.chen.budgeteer.widgets.MainViewWidget
import moe.chen.budgeteer.widgets.MonthSelector
import moe.chen.budgeteer.widgets.PaddedLazyColumn
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
        var categoryToEdit by remember { mutableStateOf<Category?>(null) }
        var selectedMonth by remember { mutableStateOf(ZonedDateTime.now()) }

        OverviewWidget(
            navController = navController,
            selectedMonth = selectedMonth,
            categories = categories.value.sortedBy {
                it.order ?: it.cid
            },
            onAddCategory = {
                navController.navigate(
                    BudgeteerScreens.AddCategoryScreen.name +
                            "/0/-1"
                )
            },
            getCategoryFlow = { model.categoryEntryFlow(it, selectedMonth) },
            getBudgetFlow = { model.categoryBudgetFlow(it, selectedMonth) },
            clickCategory = {
                if (categoryToEdit == null) {
                    navController
                        .navigate("${BudgeteerScreens.ExpenseInputScreen.name}/${it.cid!!}")
                } else {
                    categoryToEdit = null
                }
            },
            navToCategoryEdit = {
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
                .map { it.first },
            categoryToEdit = categoryToEdit,
            setCategoryToEdit = { categoryToEdit = it },
            onSaveCategories = { categoriesToSave ->
                model.saveCategories(categoriesToSave)
            },
            onChangeMonth = { month -> selectedMonth = month },
            debugHook = { model.debug() }
        )
    }
}

@Composable
fun OverviewWidget(
    navController: NavController,
    selectedMonth: ZonedDateTime,
    categories: List<Category> = exampleCategories(),
    getCategoryFlow: (Category) -> Flow<List<BudgetEntry>>,
    getBudgetFlow: (Category) -> Flow<CategoryBudget?>,
    onAddCategory: () -> Unit = {},
    clickCategory: (Category) -> Unit = {},
    navToCategoryEdit: (Category) -> Unit = {},
    accessSettings: () -> Unit,
    fields: List<ComputedField>,
    categoryToEdit: Category?,
    setCategoryToEdit: (Category?) -> Unit,
    formatter: @Composable (Double) -> String,
    onSaveCategories: (List<Category>) -> Unit,
    onChangeMonth: (ZonedDateTime) -> Unit,
    debugHook: () -> Unit,
) {
    MainViewWidget(
        navController = navController,
        additionalHelpContent = {
            Text(stringResource(R.string.field_help_intro))
            fields.forEach { field ->
                Spacer(modifier = Modifier.height(10.dp))
                Text(stringResource(field.label), fontWeight = FontWeight.Bold)
                Text(stringResource(field.description))
            }
        }
    ) {
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
                    .clickable(onClick = { setCategoryToEdit(null) })
            ) {
                Scaffold(
                    content = { padding ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight()
                        ) {
                            MonthSelector(
                                onMonthChange = onChangeMonth,
                                currentMonth = selectedMonth,
                                debugHook = debugHook
                            )
                            CategoryListWidget(
                                padding,
                                categories,
                                getCategoryFlow,
                                getBudgetFlow,
                                clickCategory,
                                { category -> setCategoryToEdit(category) },
                                fields,
                                formatter,
                                categoryToEdit
                            )
                        }
                    },
                    bottomBar = {
                        if (categoryToEdit == null) {
                            FabColumn(
                                accessSettings = accessSettings,
                                onAddCategory = onAddCategory,
                            )
                        } else {
                            EditFabColumn(
                                currentCategory = categoryToEdit,
                                categories = categories,
                                onSaveCategories = onSaveCategories,
                                categoryEdit = navToCategoryEdit,
                            )
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun EditFabColumn(
    currentCategory: Category,
    categories: List<Category>,
    onSaveCategories: (List<Category>) -> Unit,
    categoryEdit: (Category) -> Unit,
) {
    if (categories.any { it.order == null }) {
        onSaveCategories(categories.mapIndexed { index, c -> c.copy(order = index) })
    } else {
        val categoryIndex = categories.indexOfFirst { it.cid == currentCategory.cid }
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
                EFloatingActionButton(
                    enabled = categoryIndex < categories.size - 1,
                    onClick = {
                        Log.d("OverviewScreen", "category-size: ${categories.size} " +
                                "index: $categoryIndex")
                        val prev = categories[categoryIndex]
                        val nxt = categories[categoryIndex + 1]
                        onSaveCategories(
                            listOf(
                                prev.copy(order = nxt.order),
                                nxt.copy(order = prev.order),
                            )
                        )
                    },
                    icon = Icons.Rounded.ArrowDownward,
                    contentDescription = R.string.operation_down
                )
                FloatingActionButton(onClick = { categoryEdit(currentCategory) }) {
                    Icon(
                        Icons.Rounded.Edit,
                        contentDescription = stringResource(R.string.operation_edit)
                    )
                }

                EFloatingActionButton(
                    enabled = categoryIndex > 0,
                    onClick = {
                        Log.d("OverviewScreen", "category-size: ${categories.size} " +
                                "index: $categoryIndex")
                        val prev = categories[categoryIndex - 1]
                        val nxt = categories[categoryIndex]
                        onSaveCategories(
                            listOf(
                                prev.copy(order = nxt.order),
                                nxt.copy(order = prev.order),
                            )
                        )
                    },
                    icon = Icons.Rounded.ArrowUpward,
                    contentDescription = R.string.operation_up
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

@Composable
fun CategoryListWidget(
    paddingValues: PaddingValues,
    categories: List<Category> = exampleCategories(),
    getCategoryFlow: (Category) -> Flow<List<BudgetEntry>>,
    getBudgetFlow: (Category) -> Flow<CategoryBudget?>,
    clickCategory: (Category) -> Unit = {},
    longPress: (Category) -> Unit = {},
    fields: List<ComputedField>,
    formatter: @Composable (Double) -> String,
    categoryToEdit: Category?,
) {
    PaddedLazyColumn(
        modifier = Modifier,
        bottomPadding = 100.dp,
        elements = categories
    ) { _, it ->
        val entries = getCategoryFlow(it).collectAsState(initial = emptyList()).value
        val budget = getBudgetFlow(it).collectAsState(initial = CategoryBudget(
            id = 0,
            budget = 0.0,
            cid = 0,
            year = 0,
            month = 0,
        )).value

        Log.d("OverviewScreen", "cat $it entries in screen: $entries")

        CategoryRow(
            category = it,
            budget = budget,
            entries = entries,
            clicked = { clickCategory(it) },
            longPress = { longPress(it) },
            formatter = formatter,
            fields = fields,
            categoryToEdit = categoryToEdit,
        )
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
            uid = 0,
            order = null,
        ),
        entries = listOf(
            BudgetEntry(
                bid = 0,
                amount = 10.0,
                cid = 0,
                label = null,
                date = ZonedDateTime.now(),
            )
        ),
        clicked = {},
        longPress = {},
        categoryToEdit = null,
        fields = listOf(BudgetField, CurrentField, TrendField, SpendPerDayField),
        formatter = { converter.format(it) },
        budget = CategoryBudget(id = 0, budget = 50.0, year = 10, month = 10, cid = 0)
    )
}

@Composable
fun CategoryRow(
    category: Category,
    budget: CategoryBudget?,
    entries: List<BudgetEntry>,
    clicked: () -> Unit,
    longPress: () -> Unit,
    fields: List<ComputedField>,
    categoryToEdit: Category?,
    formatter: @Composable (Double) -> String,
) {
    val elevation = if (categoryToEdit?.cid == category.cid) {
        20.dp
    } else {
        4.dp
    }

    var modifier = Modifier
        .padding(4.dp)
        .fillMaxWidth()
        .height(150.dp)
        .pointerInput(Unit) {
            detectTapGestures(
                onTap = { clicked() },
                onLongPress = { longPress() }
            )
        }
    if (categoryToEdit?.cid == category.cid) {
        modifier = modifier.padding(10.dp)
    }

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(corner = CornerSize(14.dp)),
        elevation = elevation
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.45f)
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
                    budget = budget,
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
    budget: CategoryBudget?,
    fields: List<ComputedField>,
    formatter: @Composable (Double) -> String,
) {

    val computed = budget?.let { b ->
        fields.map { field ->
            val value = field.computation(category, b, entries)
            val color = field.colorSelector(category, b, value, isSystemInDarkTheme())
            Triple(value, color, field)
        }
    } ?: emptyList()

    Row(
        modifier = Modifier
            .padding(5.dp)
            .fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth(0.6f)) {
            if (budget == null) {
                Text(stringResource(R.string.label_no_budget), color = if (isSystemInDarkTheme()) {
                    NotOkColorDarkMode
                } else {
                    NotOkColor
                })
            }
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