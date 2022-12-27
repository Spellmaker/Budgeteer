package moe.chen.budgeteer.screens

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Save
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import moe.chen.budgeteer.R
import moe.chen.budgeteer.data.ComputedField
import moe.chen.budgeteer.data.allCategories
import moe.chen.budgeteer.room.Category
import moe.chen.budgeteer.room.CategoryType
import moe.chen.budgeteer.room.UserSetting
import moe.chen.budgeteer.viewmodel.UserSettingViewModel
import moe.chen.budgeteer.widgets.FilteredSelectionWidget
import moe.chen.budgeteer.widgets.MainViewWidget
import moe.chen.budgeteer.widgets.OrderSelectionWidget
import java.util.*

@Composable
fun UserSettingsScreen(
    navController: NavController,
) {
    val model = hiltViewModel<UserSettingViewModel>()
    val settings = model.settings.collectAsState()
    if (settings.value != null && settings.value != model.invalidSettings) {
        MainViewWidget(navController = navController) {
            UserSettingEditor(
                settings.value!!,
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
                            budget = 0.0,
                            uid = 0,
                            order = null,
                            type = CategoryType.PER_MONTH,
                        ),
                        entries = emptyList(),
                        clicked = { },
                        longPress = { },
                        fields = categoryElements
                            .filter { it.second >= 0 }
                            .sortedBy { it.second }
                            .map { it.first },
                        categoryToEdit = null
                        ) {
                        UserSettingViewModel.makeConverter(currentState.currencyCode).format(it)
                    }
                //}

                Row(modifier = Modifier.padding(5.dp)) {
                    Button(onClick = { orderVisible = !orderVisible }, modifier = Modifier.fillMaxWidth()) {
                        if (orderVisible) {
                            Text(stringResource(R.string.operation_stop_editing))
                        } else {
                            Text(stringResource(R.string.operation_start_editing))
                        }
                    }
                }
                AnimatedVisibility(visible = orderVisible) {
                    OrderSelectionWidget(
                        elements = categoryElements,
                        visibilities = categoryElements
                            .map { it.second >= 0 },
                        visibilityChanged = { pos, value ->
                            if (value) {
                                val newElements = categoryElements.toMutableList()
                                newElements[pos] = newElements[pos].first to pos
                                categoryElements = ensureHiddenLast(newElements)
                            } else {
                                val newElements = categoryElements.toMutableList()
                                newElements[pos] = newElements[pos].first to -1
                                categoryElements = ensureHiddenLast(newElements)
                            }
                        },
                        orderChanged = { newList ->
                            categoryElements = ensureHiddenLast(newList)
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
                            Icons.Rounded.ArrowBack,
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
