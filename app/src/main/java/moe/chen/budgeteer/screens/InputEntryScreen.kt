package moe.chen.budgeteer.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InputEntryScreen(
    navController: NavController,
) {
    val model = hiltViewModel<InputEntryViewModel>()
    val existingEntry = model.entry.collectAsState()
    if (existingEntry.value == model.invalidEntry) {
        return
    }

    val recommendations = model.labels.collectAsState()

    var amountDouble by remember { mutableDoubleStateOf(existingEntry.value?.amount ?: 0.0) }
    var entryLabel by remember { mutableStateOf(existingEntry.value?.label ?: "") }
    val category = model.category.collectAsState()
    var amountString by remember {
        mutableStateOf<String>(
            formatCompact(existingEntry.value?.amount ?: 0.0)
        )
    }

    var isValid by remember { mutableStateOf(true) }

    MainViewWidget(navController = navController) {
        if (category.value != null) {
            InputWidget(
                recommendations = recommendations.value.map { it.label },
                category = category.value,
                amount = amountString,
                entryLabel = entryLabel,
                setLabel = { entryLabel = it },
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
                        model.addOrModifyEntry(amountDouble, entryLabel)
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

@ExperimentalLayoutApi
@Preview(showBackground = true)
@Composable
fun InputWidget(
    recommendations: List<String> = listOf("doener", "dm", "rewe", "longelong", "schlangelang"),
    category: Category? = Category(null, "test", 0, null),
    isValid: Boolean = true,
    amount: String = "0.0",
    entryLabel: String = "a label",
    setLabel: (String) -> Unit = {},
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
                value = entryLabel,
                onValueChange = setLabel,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(5.dp),
                label = { Text(stringResource(R.string.label_label)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Decimal
                ),
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        FlowRow(modifier = Modifier.fillMaxWidth()) {
            recommendations.forEach { label ->
                RoundedButton(enabled = true, text = label, modifier = Modifier) {
                    setLabel(label)
                }
            }
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
                        Icons.AutoMirrored.Rounded.ArrowBack,
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
    RoundedButton(
        modifier = Modifier.width(100.dp),
        enabled = enabled,
        text = formatter(amount),
        onClick = {
            if (enabled) {
                changeAmount(amount)
            }
        }
    )
}

@Composable
fun RoundedButton(
    modifier: Modifier,
    enabled: Boolean,
    text: String,
    onClick: () -> Unit,
) {
    Surface(
        elevation = 2.dp,
        shape = CircleShape,//MaterialTheme.shapes.small,
        border = ButtonDefaults.outlinedBorder,
        modifier = modifier
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
                text,
                fontSize = 20.sp,
                modifier = Modifier
                    .clickable(onClick = onClick)
                    .padding(5.dp)
                    .background(MaterialTheme.colors.primary),
                color = MaterialTheme.colors.onPrimary
            )
        }
    }
}