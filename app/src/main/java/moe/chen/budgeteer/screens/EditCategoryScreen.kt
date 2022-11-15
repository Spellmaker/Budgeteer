package moe.chen.budgeteer.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import moe.chen.budgeteer.R
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
    accessSettings: () -> Unit,
) {
    val viewModel = hiltViewModel<AddCategoryViewModel>()

    val settingsModel = hiltViewModel<UserSettingViewModel>()
    settingsModel.listenToUser(user)

    val convertDefault = settingsModel.converterDefault.collectAsState()
    val existingCategory = viewModel.category.collectAsState()

    if (existingCategory.value == viewModel.invalidCategory) {
        return
    }

    val label = remember { mutableStateOf<String>(existingCategory.value?.label ?: "") }
    val budget = remember { mutableStateOf<Double>(existingCategory.value?.budget ?: 0.0) }

    MainViewWidget(logout = logout, settings = accessSettings) {
        if (convertDefault.value != null) {
            Column(
                modifier = Modifier
                    .padding(it)
                    .fillMaxWidth()
            ) {
                if (existingCategory.value == null) {
                    Text(
                        stringResource(R.string.add_new_category),
                        style = MaterialTheme.typography.caption
                    )
                } else {
                    Text(
                        stringResource(R.string.modify_category),
                        style = MaterialTheme.typography.caption
                    )
                }

                EditWidget(
                    label = label.value,
                    updateLabel = { newLabel -> label.value = newLabel },
                    budget = convertDefault.value?.format(budget.value) ?: budget.toString(),
                    updateBudget = { newBudget ->
                        try {
                            budget.value = convertDefault.value?.parse(newBudget)!!.toDouble()
                        } catch (t: Throwable) {
                            // noop
                        }
                    },
                    create = {
                        if (existingCategory.value == null) {
                            viewModel.addCategory(label.value, budget.value, user.uid!!)
                        } else {
                            viewModel.updateCategory(
                                existingCategory.value!!.cid!!,
                                existingCategory.value!!.uid,
                                label.value,
                                budget.value,
                            )
                        }
                        navController.navigate(BudgeteerScreens.OverviewScreen.name)
                    }
                )
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
    create: () -> Unit = {}
) {
    TextField(
        singleLine = true,
        value = label,
        onValueChange = updateLabel,
        label = { Text(stringResource(R.string.category_name)) },
        modifier = Modifier
            .padding(5.dp)
            .fillMaxWidth()
    )
    TextField(
        singleLine = true,
        value = budget,
        onValueChange = updateBudget,
        label = { Text(stringResource(R.string.category_budget)) },
        modifier = Modifier
            .padding(5.dp)
            .fillMaxWidth(),

        )
    Button(
        onClick = create,
        modifier = Modifier
            .fillMaxWidth()
            .padding(5.dp)
    ) {
        Text(stringResource(R.string.operation_submit))
    }
}