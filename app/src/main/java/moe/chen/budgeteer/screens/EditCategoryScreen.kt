package moe.chen.budgeteer.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Save
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import moe.chen.budgeteer.R
import moe.chen.budgeteer.formatCompact
import moe.chen.budgeteer.navigation.BudgeteerScreens
import moe.chen.budgeteer.room.User
import moe.chen.budgeteer.viewmodel.AddCategoryViewModel
import moe.chen.budgeteer.viewmodel.UserSettingViewModel
import moe.chen.budgeteer.widgets.MainViewWidget

@Composable
fun EditCategoryScreen(
    navController: NavController,
    user: User,
    logout: () -> Unit,
) {
    val viewModel = hiltViewModel<AddCategoryViewModel>()

    val settingsModel = hiltViewModel<UserSettingViewModel>()
    val convertDefault = settingsModel.converterDefault.collectAsState()
    val existingCategory = viewModel.category.collectAsState()

    var currencyIsError by remember { mutableStateOf(false) }
    if (existingCategory.value == viewModel.invalidCategory) {
        return
    }

    val label = remember { mutableStateOf(existingCategory.value?.label ?: "") }
    val budget = remember { mutableStateOf<Double?>(existingCategory.value?.budget ?: 0.0) }
    var budgetString by remember { mutableStateOf(formatCompact(budget.value!!)) }

    MainViewWidget(navController = navController, logout = logout) {
        if (convertDefault.value != null) {
            Column(
                modifier = Modifier
                    .padding(5.dp)
                    .fillMaxWidth()
            ) {
                if (existingCategory.value == null) {
                    Text(
                        stringResource(R.string.add_new_category),
                        style = MaterialTheme.typography.h6,
                        modifier = Modifier.padding(5.dp)
                    )
                } else {
                    Text(
                        stringResource(R.string.modify_category),
                        style = MaterialTheme.typography.h6,
                        modifier = Modifier.padding(5.dp)
                    )
                }

                EditWidget(
                    currencyIsError = currencyIsError,
                    label = label.value,
                    updateLabel = { newLabel -> label.value = newLabel.trim() },
                    budget = budgetString,
                    updateBudget = { newBudget ->
                        budgetString = newBudget
                        try {
                            budget.value = newBudget
                                .replace(",", ".")
                                .toDouble()
                            currencyIsError = false
                        } catch (t: Throwable) {
                            // noop
                            budget.value = null
                            currencyIsError = true
                        }
                    },
                    create = {
                        if (budget.value != null) {
                            if (existingCategory.value == null) {
                                viewModel.addCategory(label.value, budget.value!!, user.uid!!)
                            } else {
                                viewModel.updateCategory(
                                    existingCategory.value!!.cid!!,
                                    existingCategory.value!!.uid,
                                    label.value,
                                    budget.value!!,
                                )
                            }
                            navController.navigate(
                                BudgeteerScreens.OverviewScreen.name +
                                        "/${user.uid!!}"
                            )
                        }
                    }
                )


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


                        if (existingCategory.value != null) {
                            FloatingActionButton(onClick = {
                                if (existingCategory.value != null) {
                                    viewModel.removeCategory()
                                    navController.navigate(
                                        BudgeteerScreens.OverviewScreen.name +
                                                "/${user.uid!!}"
                                    )
                                }
                            }) {
                                Icon(
                                    Icons.Rounded.Delete,
                                    contentDescription = stringResource(R.string.operation_delete)
                                )
                            }
                        }
                        FloatingActionButton(onClick = {
                            if (budget.value != null) {
                                if (existingCategory.value == null) {
                                    viewModel.addCategory(label.value, budget.value!!, user.uid!!)
                                } else {
                                    viewModel.updateCategory(
                                        existingCategory.value!!.cid!!,
                                        existingCategory.value!!.uid,
                                        label.value,
                                        budget.value!!,
                                    )
                                }
                                navController.navigate(
                                    BudgeteerScreens.OverviewScreen.name +
                                            "/${user.uid!!}"
                                )
                            }
                        }) {
                            Icon(
                                Icons.Rounded.Save,
                                contentDescription = stringResource(R.string.operation_save_changes)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview()
@Composable
fun EditWidget(
    label: String = "",
    budget: String = "",
    updateLabel: (String) -> Unit = {},
    updateBudget: (String) -> Unit = {},
    create: () -> Unit = {},
    currencyIsError: Boolean = false,
) {
    TextField(
        singleLine = true,
        value = label,
        onValueChange = updateLabel,
        label = { Text(stringResource(R.string.category_name)) },
        modifier = Modifier
            .padding(5.dp)
            .fillMaxWidth(),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
    )
    TextField(
        singleLine = true,
        value = budget,
        onValueChange = updateBudget,
        label = { Text(stringResource(R.string.category_budget)) },
        modifier = Modifier
            .padding(5.dp)
            .fillMaxWidth(),
        keyboardOptions = KeyboardOptions.Default.copy(
            keyboardType = KeyboardType.Decimal,
        ),
        keyboardActions = KeyboardActions(
            onDone = { create() }
        ),
        isError = currencyIsError,
    )
}