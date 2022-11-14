package moe.chen.budgeteer.screens

import androidx.compose.foundation.background
import androidx.compose.material.Surface
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import moe.chen.budgeteer.formatCompact
import moe.chen.budgeteer.room.Category
import moe.chen.budgeteer.room.User
import moe.chen.budgeteer.viewmodel.InputEntryViewModel
import moe.chen.budgeteer.viewmodel.UserSettingViewModel
import moe.chen.budgeteer.widgets.MainViewWidget

@Composable
fun InputEntryScreen(
    navController: NavController,
    user: User,
    categoryId: Int,
    logout: () -> Unit,
    accessSettings: () -> Unit,
) {
    val amount = remember { mutableStateOf(0.0) }
    val model = hiltViewModel<InputEntryViewModel>()
    model.listenForCategory(categoryId)
    val category = model.category.collectAsState()

    val settingsModel = hiltViewModel<UserSettingViewModel>()
    settingsModel.listenToUser(user)

    val converterDefault = settingsModel.converterDefault.collectAsState()

    MainViewWidget(logout = logout, settings = accessSettings) {
        if (category.value != null) {
            InputWidget(
                category = category.value,
                amount = amount.value,
                setAmount = {
                    try {
                        amount.value = converterDefault.value?.parse(it)?.toDouble() ?: 0.0
                    } catch (_: Throwable) {

                    }
                },
                changeAmount = { amount.value += it },
                abort = {
                    navController.popBackStack()
                },
                createEntry = {
                    model.addEntry(amount.value)
                    navController.popBackStack()
                },
                formatter = {
                    converterDefault.value?.format(it) ?: "N"
                },
                formatterCompact = {
                    formatCompact(it)
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun InputWidget(
    category: Category? = Category(null, "test", 0.0, 0),
    amount: Double = 0.0,
    setAmount: (String) -> Unit = {},
    changeAmount: (Double) -> Unit = {},
    createEntry: () -> Unit = {},
    abort: () -> Unit = {},
    formatter: @Composable (Double) -> String = { "n/a" },
    formatterCompact: (Double) -> String = { it.toString() },
) {
    Column(
        modifier = Modifier.fillMaxHeight()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(category?.label ?: "null") //TODO
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row {
            TextField(
                value = formatter(amount),
                onValueChange = setAmount,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(5.dp),
                label = { Text("Amount") },
                singleLine = true,
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f, true)) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    AmountButton(amount = -10.0, changeAmount = changeAmount, formatterCompact)
                    AmountButton(amount = -5.0, changeAmount = changeAmount, formatterCompact)
                    AmountButton(amount = -1.0, changeAmount = changeAmount, formatterCompact)
                }
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    AmountButton(amount = -0.5, changeAmount = changeAmount, formatterCompact)
                    AmountButton(amount = -0.1, changeAmount = changeAmount, formatterCompact)
                    AmountButton(amount = -0.01, changeAmount = changeAmount, formatterCompact)
                }
            }
            Column(modifier = Modifier.weight(1f, true)) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    AmountButton(amount = 10.0, changeAmount = changeAmount, formatterCompact)
                    AmountButton(amount = 5.0, changeAmount = changeAmount, formatterCompact)
                    AmountButton(amount = 1.0, changeAmount = changeAmount, formatterCompact)
                }
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    AmountButton(amount = 0.5, changeAmount = changeAmount, formatterCompact)
                    AmountButton(amount = 0.1, changeAmount = changeAmount, formatterCompact)
                    AmountButton(amount = 0.01, changeAmount = changeAmount, formatterCompact)
                }
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row {
            Column {
                Button(
                    onClick = createEntry, modifier = Modifier
                        .fillMaxWidth()
                        .padding(5.dp)
                ) {
                    Text("Submit")
                }
                Button(
                    onClick = abort, modifier = Modifier
                        .fillMaxWidth()
                        .padding(5.dp)
                ) {
                    Text("Abort")
                }
            }
        }
    }
}

@Composable
fun AmountButton(amount: Double, changeAmount: (Double) -> Unit, formatter: (Double) -> String) {
    Surface(
        shape = MaterialTheme.shapes.small,
        border = ButtonDefaults.outlinedBorder,
        modifier = Modifier
            .width(64.dp)
            .height(50.dp)
            .padding(2.dp)

    ) {
        Row(
            modifier = Modifier.background(MaterialTheme.colors.primary),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                formatter(amount),
                fontSize = 12.sp,
                modifier = Modifier
                    .clickable(onClick = { changeAmount(amount) }
                    )
                    .padding(5.dp)
                    .background(MaterialTheme.colors.primary),
                color = MaterialTheme.colors.onPrimary
            )
        }
    }
}