package moe.chen.budgeteer.widgets

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Help
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import moe.chen.budgeteer.R
import moe.chen.budgeteer.navigation.BudgeteerScreens

@Composable
fun HelpWidget(
    navController: NavController,
    additionalHelpContent: @Composable () -> Unit,
) {
    var isOpen by remember { mutableStateOf(false) }

    Button(onClick = { isOpen = true }, modifier = Modifier.padding(2.dp)) {
        Icon(Icons.AutoMirrored.Rounded.Help, stringResource(R.string.operation_help))
    }

    if (isOpen) {

        val routeFull = navController.currentDestination?.route ?: ""
        val routeSubstring = if (routeFull.contains("/")) {
            routeFull.substring(0, routeFull.indexOf("/"))
        } else {
            routeFull
        }

        val helpResourceId = when (BudgeteerScreens.fromRoute(routeSubstring)) {
            BudgeteerScreens.OverviewScreen -> R.string.help_overview_screen
            BudgeteerScreens.CategoryScreen -> R.string.help_category_overview
            BudgeteerScreens.ExpenseInputScreen -> R.string.help_input_screen
            BudgeteerScreens.AddCategoryScreen -> R.string.help_add_category
            BudgeteerScreens.UserSettingsScreen -> R.string.help_settings_screen
        }
        AlertDialog(
            onDismissRequest = { isOpen = false },
            title = { Text(stringResource(R.string.operation_help)) },
            text = {
                Column {
                    Text(stringResource(helpResourceId))
                    additionalHelpContent()
                }
            },
            confirmButton = {
                Button(onClick = { isOpen = false }) {
                    Text(stringResource(R.string.operation_ok))
                }
            },
        )
    }
}