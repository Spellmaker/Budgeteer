package moe.chen.budgeteer.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import moe.chen.budgeteer.navigation.BudgeteerScreens
import moe.chen.budgeteer.room.User
import moe.chen.budgeteer.viewmodel.AddCategoryViewModel
import moe.chen.budgeteer.widgets.MainViewWidget
import java.text.NumberFormat
import java.util.*

@Composable
fun EditCategoryScreen(
    navController: NavController,
    user: User,
    logout: () -> Unit,
) {
    val viewModel = hiltViewModel<AddCategoryViewModel>()
    var label = remember { mutableStateOf<String>("") }
    var budget = remember { mutableStateOf<Double>(0.0) }

    val format = NumberFormat.getCurrencyInstance()
    format.currency = Currency.getInstance("EUR")
    format.maximumFractionDigits = 2

    MainViewWidget(logout = logout) {
        Column(
            modifier = Modifier
                .padding(it)
                .fillMaxWidth()
        ) {
            Text("Add new category", style = MaterialTheme.typography.caption)

            EditWidget(
                label = label.value,
                updateLabel = { newLabel -> label.value = newLabel },
                budget = format.format(budget.value),
                updateBudget = { newBudget ->
                    try {
                        budget.value = format.parse(newBudget)!!.toDouble()
                    } catch(t: Throwable) {
                        // noop
                    }
                },
                create = {
                    viewModel.addCategory(label.value, budget.value, user.uid!!)
                    navController.navigate(BudgeteerScreens.OverviewScreen.name)
                }
            )
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
        label = { Text("Category Name") },
        modifier = Modifier
            .padding(5.dp)
            .fillMaxWidth()
    )
    TextField(
        singleLine = true,
        value = budget,
        onValueChange = updateBudget,
        label = { Text("Budget") },
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
        Text("Add")
    }
}