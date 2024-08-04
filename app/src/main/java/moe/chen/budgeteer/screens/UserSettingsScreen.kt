package moe.chen.budgeteer.screens

import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Save
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import moe.chen.budgeteer.R
import moe.chen.budgeteer.data.ComputedField
import moe.chen.budgeteer.data.allCategories
import moe.chen.budgeteer.room.Category
import moe.chen.budgeteer.room.CategoryBudget
import moe.chen.budgeteer.room.UserSetting
import moe.chen.budgeteer.viewmodel.UserSettingViewModel
import moe.chen.budgeteer.widgets.FilteredSelectionWidget
import moe.chen.budgeteer.widgets.MainViewWidget
import moe.chen.budgeteer.widgets.OrderSelectionWidget
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Currency

private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm")

@Composable
fun UserSettingsScreen(
    navController: NavController,
) {
    val model = hiltViewModel<UserSettingViewModel>()
    val contentResolver = LocalContext.current.contentResolver
    val context = LocalContext.current
    val errorExport = stringResource(id = R.string.error_export)
    val exportSuccess = stringResource(id = R.string.label_export_success)
    val settings = model.settings.collectAsState()
    if (settings.value != null && settings.value != model.invalidSettings) {
        MainViewWidget(navController = navController) {
            UserSettingEditor(
                currentSettings = settings.value!!,
                exportData = { uri -> model.exportData(uri, contentResolver) {
                    if (!it) {
                        Toast.makeText(context, errorExport, Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(context, exportSuccess, Toast.LENGTH_LONG).show()
                    }
                }
                },
                goBack = { navController.popBackStack() }
            ) {
                model.updateSettings(it)
                navController.popBackStack()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun UserSettingEditor(
    currentSettings: UserSetting = UserSetting.getDefault(0),
    exportData: (Uri) -> Unit = {},
    goBack: () -> Unit = {},
    updateSettings: (UserSetting) -> Unit = {},
) {
    var currentState by remember {
        mutableStateOf(
            try {
                Currency.getInstance(currentSettings.currency)
            } catch (e: Exception) {
                Log.d("UserSettingsScreen", e.message ?: "error")
                Currency.getInstance("EUR")
            }
        )
    }
    var categoryElements by remember { mutableStateOf(convertSettingsToList(currentSettings)) }
    Scaffold(
        content  = { padding ->
            var orderVisible by remember { mutableStateOf(false) }
            Column(modifier = Modifier.fillMaxWidth()) {
                Spacer(modifier = Modifier.height(10.dp))
                val elements = Currency.getAvailableCurrencies().toList()

                Log.d("UserSettingsScreen", "currency symbol is: ${currentSettings.currency}")
                FilteredSelectionWidget<Currency>(
                    label = stringResource(R.string.label_input_currency),
                    items = elements,
                    selectionChanged = { currentState = it },
                    filterSelector = { it.currencyCode },
                    stringSelector = { it.currencyCode + it.displayName + it.currencyCode },
                    currentItem = currentState,
                    initialExpanded = false
                ) { item, modifier ->
                    Row(modifier = modifier, horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = item.currencyCode)
                        Text(text = item.displayName)
                        Text(text = item.symbol)
                    }
                }

                CategoryRow(
                    category = Category(
                        cid = 0,
                        label = stringResource(R.string.label_example_category),
                        uid = 0,
                        order = null,
                    ),
                    entries = emptyList(),
                    clicked = { },
                    longPress = { },
                    fields = categoryElements
                        .filter { it.second >= 0 }
                        .sortedBy { it.second }
                        .map { it.first },
                    categoryToEdit = null,
                    budget = CategoryBudget(0, 0.0, 0, 0, 0)
                    ) {
                    UserSettingViewModel.makeConverter(currentState.currencyCode).format(it)
                }

                Row(modifier = Modifier.padding(5.dp)) {
                    Button(
                        onClick = { orderVisible = !orderVisible },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (orderVisible) {
                            Text(stringResource(R.string.operation_stop_editing))
                        } else {
                            Text(stringResource(R.string.operation_start_editing))
                        }
                    }
                }
                OrderSelector(
                    orderVisible = orderVisible,
                    categoryElements = categoryElements,
                    updateElements = { categoryElements = it }
                )

                Spacer(modifier = Modifier.height(10.dp))


                Row(modifier = Modifier.padding(5.dp)) {
                    ExportButton(handleExport = exportData)
                }
            }
        },
        bottomBar = {
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
                    FloatingActionButton(onClick = goBack) {
                        Icon(
                            Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = stringResource(R.string.operation_cancel)
                        )
                    }
                    FloatingActionButton(onClick = {
                        val newSettings = categoryElements.fold(
                            currentSettings.copy(
                                currency = currentState.currencyCode,
                            )
                        ) { settings, element -> element.first.setter(settings, element.second) }
                        updateSettings(newSettings)
                    }) {
                        Icon(
                            Icons.Rounded.Save,
                            contentDescription = stringResource(R.string.operation_save_changes)
                        )
                    }
                }
            }
        }
    )
}

@Composable
fun OrderSelector(
    orderVisible: Boolean,
    categoryElements: List<Pair<ComputedField, Int>>,
    updateElements: (List<Pair<ComputedField, Int>>) -> Unit,
) {
    AnimatedVisibility(visible = orderVisible) {
        OrderSelectionWidget(
            elements = categoryElements,
            visibilities = categoryElements
                .map { it.second >= 0 },
            visibilityChanged = { pos, value ->
                if (value) {
                    val newElements = categoryElements.toMutableList()
                    newElements[pos] = newElements[pos].first to pos
                    updateElements(ensureHiddenLast(newElements))
                    ensureHiddenLast(newElements)
                } else {
                    val newElements = categoryElements.toMutableList()
                    newElements[pos] = newElements[pos].first to -1
                    updateElements(ensureHiddenLast(newElements))
                }
            },
            orderChanged = { newList ->
                updateElements(ensureHiddenLast(newList))
            }
        ) { element, modifier ->
            Column(modifier = modifier.width(200.dp)) {
                Text(stringResource(element.first.label))
                Text(
                    stringResource(element.first.description),
                    style = MaterialTheme.typography.subtitle1
                )
            }
        }
    }
}

@Composable
fun ExportButton(
    modifier: Modifier = Modifier,
    handleExport: (Uri) -> Unit,
) {
    val createDocumentResult = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv")
    ) { uri: Uri? ->
        if (uri != null) {
            handleExport(uri)
        }
    }
    Button(
        modifier = modifier.fillMaxWidth(),
        onClick = {
            createDocumentResult.launch(
                "budgeteer_export_${formatter.format(ZonedDateTime.now())}.csv"
            )
        },
    ) {
        Text(text = stringResource(R.string.label_export))
    }
}


private fun convertSettingsToList(userSetting: UserSetting): List<Pair<ComputedField, Int>> =
    allCategories
        .map { it to it.extractor(userSetting) }
        .let { elements ->
            elements.filter { it.second >= 0 }
                .sortedBy { it.second }
                .plus(elements.filter { it.second == -1 })
        }

private fun ensureHiddenLast(input: List<Pair<ComputedField, Int>>) =
    input
        .mapIndexed { index, pair ->
            val newIndex = if (pair.second == -1) {
                pair.second
            } else {
                index
            }
            pair.first to newIndex
        }.let { elements ->
            elements.filter { it.second >= 0 }
                .sortedBy { it.second }
                .plus(elements.filter { it.second == -1 })
        }
