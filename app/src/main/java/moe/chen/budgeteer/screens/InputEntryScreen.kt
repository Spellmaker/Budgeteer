package moe.chen.budgeteer.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import moe.chen.budgeteer.R
import moe.chen.budgeteer.formatCompact
import moe.chen.budgeteer.room.Category
import moe.chen.budgeteer.viewmodel.InputEntryViewModel
import moe.chen.budgeteer.widgets.MainViewWidget

@Composable
fun InputEntryScreen(
    navController: NavController,
    logout: () -> Unit,
) {
    var amountDouble by remember { mutableStateOf(0.0) }
    val model = hiltViewModel<InputEntryViewModel>()
    val category = model.category.collectAsState()
    var amountString by remember {
        mutableStateOf<String>(
            formatCompact(0.0)
        )
    }
    var isValid by remember { mutableStateOf(true) }

    MainViewWidget(logout = logout) {
        if (category.value != null) {
            InputWidget(
                category = category.value,
                amount = amountString,
                setAmount = {
                    Log.d("InputEntryScreen", "changing to string $it")
                    amountString = it
                    try {
                        amountDouble = it.replace(",", ".").toDouble()
                        Log.d("InputEntryScreen", "parsing successful, new value $amountDouble")
                        isValid = true
                    } catch (_: Throwable) {
                        isValid = false
                        Log.d(
                            "InputEntryScreen",
                            "parsing failed, value remains $amountDouble, is valid is $isValid"
                        )
                    }
                },
                changeAmount = {
                    amountDouble += it
                    amountString = formatCompact(amountDouble)
                },
                abort = {
                    navController.popBackStack()
                },
                createEntry = {
                    if (isValid) {
                        model.addEntry(amountDouble)
                        navController.popBackStack()
                    }
                },
                isValid = isValid,
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
    isValid: Boolean = true,
    amount: String = "0.0",
    setAmount: (String) -> Unit = {},
    changeAmount: (Double) -> Unit = {},
    createEntry: () -> Unit = {},
    abort: () -> Unit = {},
    formatterCompact: (Double) -> String = { it.toString() },
) {
    Column(
        modifier = Modifier.fillMaxHeight()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(category?.label ?: "null", style = MaterialTheme.typography.h6)
        }
        Row {
            TextField(
                value = amount,
                onValueChange = setAmount,
                isError = !isValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(5.dp),
                label = { Text(stringResource(R.string.label_amount)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Decimal
                ),
                keyboardActions = KeyboardActions(onDone = {
                    if (isValid) {
                        setAmount(amount)
                        createEntry()
                    }
                })
            )
        }
        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(
                modifier = Modifier.weight(1f, true),
                horizontalAlignment = Alignment.Start,
            ) {
                AmountButton(isValid, -50.0, changeAmount, formatterCompact)
                AmountButton(isValid, -5.0, changeAmount, formatterCompact)
                AmountButton(isValid, -0.5, changeAmount, formatterCompact)
            }
            Column(
                modifier = Modifier.weight(1f, true),
                horizontalAlignment = Alignment.Start,
            ) {
                AmountButton(isValid, -10.0, changeAmount, formatterCompact)
                AmountButton(isValid, -1.0, changeAmount, formatterCompact)
                AmountButton(isValid, -0.1, changeAmount, formatterCompact)
            }

            Spacer(
                modifier = Modifier
                    .width(20.dp)
            )

            Column(
                modifier = Modifier.weight(1f, true),
                horizontalAlignment = Alignment.End,
            ) {
                AmountButton(isValid, 50.0, changeAmount, formatterCompact)
                AmountButton(isValid, 5.0, changeAmount, formatterCompact)
                AmountButton(isValid, 0.5, changeAmount, formatterCompact)

            }

            Column(
                modifier = Modifier.weight(1f, true),
                horizontalAlignment = Alignment.End,
            ) {
                AmountButton(isValid, 10.0, changeAmount, formatterCompact)
                AmountButton(isValid, 1.0, changeAmount, formatterCompact)
                AmountButton(isValid, 0.1, changeAmount, formatterCompact)

            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(10.dp),
            verticalArrangement = Arrangement.Bottom
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                FloatingActionButton(onClick = abort) {
                    Icon(
                        Icons.Rounded.ArrowBack,
                        contentDescription = stringResource(R.string.operation_cancel)
                    )
                }
                if (isValid) {
                    FloatingActionButton(onClick = createEntry) {
                        Icon(
                            Icons.Rounded.Add,
                            contentDescription = stringResource(R.string.operation_submit)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AmountButton(
    enabled: Boolean,
    amount: Double,
    changeAmount: (Double) -> Unit,
    formatter: (Double) -> String
) {
    Surface(
        elevation = 2.dp,
        shape = CircleShape,//MaterialTheme.shapes.small,
        border = ButtonDefaults.outlinedBorder,
        modifier = Modifier
            .width(100.dp)
            .height(74.dp)
            .padding(5.dp)
            .let {
                if (enabled) {
                    it
                } else {
                    it.background(Color.Gray)
                }
            }

    ) {
        Row(
            modifier = Modifier.background(MaterialTheme.colors.primary),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                formatter(amount),
                fontSize = 20.sp,
                modifier = Modifier
                    .clickable(onClick = {
                        if (enabled) {
                            changeAmount(amount)
                        }
                    }
                    )
                    .padding(5.dp)
                    .background(MaterialTheme.colors.primary),
                color = MaterialTheme.colors.onPrimary
            )
        }
    }
}