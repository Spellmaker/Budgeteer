package moe.chen.budgeteer.screens

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import moe.chen.budgeteer.data.ComputedField
import moe.chen.budgeteer.data.allCategories
import moe.chen.budgeteer.room.Category
import moe.chen.budgeteer.room.User
import moe.chen.budgeteer.room.UserSetting
import moe.chen.budgeteer.viewmodel.UserSettingViewModel
import moe.chen.budgeteer.widgets.FilteredSelectionWidget
import moe.chen.budgeteer.widgets.MainViewWidget
import moe.chen.budgeteer.widgets.OrderSelectionWidget
import java.util.*

@Composable
fun UserSettingsScreen(
    navController: NavController,
    user: User,
    logout: () -> Unit,
    accessSettings: () -> Unit,
) {
    val model = hiltViewModel<UserSettingViewModel>()
    val converter = model.converterDefault.collectAsState()
    val settings = model.listenToUser(user).collectAsState()
    if (settings.value == null) {
        model.createDefaultSetting()
    } else if (settings.value != model.invalidSettings) {
        MainViewWidget(logout = logout, settings = accessSettings) {
            UserSettingEditor(
                settings.value!!,
                { converter.value?.format(it) ?: it.toString() },
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
    currentSettings: UserSetting = UserSetting.getDefault(
        User(
            uid = 1,
            username = "",
            secret = ""
        )
    ),
    formatter: (Double) -> String = { it.toString() },
    updateSettings: (UserSetting) -> Unit = {},
) {
    var orderVisible by remember { mutableStateOf(false) }
    Column(modifier = Modifier.fillMaxWidth()) {
        Spacer(modifier = Modifier.height(10.dp))
        val elements = Currency.getAvailableCurrencies().toList()

        Log.d("UserSettingsScreen", "currency symbol is: ${currentSettings.currency}")
        var currentState by remember {
            mutableStateOf(
                try {
                    Currency.getInstance(
                        if (currentSettings.currency == "€") {
                            "EUR"
                        } else {
                            currentSettings.currency
                        }
                    )
                } catch (e: Exception) {
                    Log.d("UserSettingsScreen", e.message ?: "error")
                    Currency.getInstance("EUR")
                }
            )
        }
        FilteredSelectionWidget<Currency>(
            label = "Input Currency",
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

        var categoryElements by remember { mutableStateOf(convertSettingsToList(currentSettings)) }

        if (!orderVisible) {
            CategoryRow(
                category = Category(
                    cid = 0,
                    label = "Example Category",
                    budget = 0.0,
                    uid = 0,
                ),
                entries = emptyList(),
                clicked = { },
                longPress = { },
                fields = categoryElements
                    .filter { it.second >= 0 }
                    .sortedBy { it.second }
                    .map { it.first }
            ) {
                formatter(it)
            }
        }

        Row(modifier = Modifier.padding(5.dp)) {
            Button(onClick = { orderVisible = !orderVisible }, modifier = Modifier.fillMaxWidth()) {
                if (orderVisible) {
                    Text("Stop editing computed fields per category")
                } else {
                    Text("Edit computed fields per category")
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
                    Text(element.first.label)
                    Text(element.first.description, style = MaterialTheme.typography.subtitle1)
                }
            }
        }
        Button(
            onClick = {
                val newSettings = categoryElements.fold(
                    currentSettings.copy(
                        currency = currentState.currencyCode,
                    )
                ) { settings, element -> element.first.setter(settings, element.second) }
                updateSettings(newSettings)
            }, modifier = Modifier
                .padding(5.dp)
                .fillMaxWidth()
        ) {
            Text("Save Changes")
        }
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
