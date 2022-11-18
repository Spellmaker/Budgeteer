package moe.chen.budgeteer.widgets

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Logout
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import moe.chen.budgeteer.R

@Composable
fun MainViewWidget(
    navController: NavController,
    title: String = "",
    logout: () -> Unit,
    content: @Composable (PaddingValues) -> Unit,
) {
    val addendum = if (title.isEmpty()) {
        ""
    } else {
        " - $title"
    }
    Scaffold(
        topBar = {
            TopAppBar(
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        modifier = Modifier.padding(5.dp),
                        text = stringResource(R.string.application_title, addendum),
                        style = MaterialTheme.typography.h6
                    )

                    Row {
                        HelpWidget(navController = navController)
                        Button(onClick = logout, modifier = Modifier.padding(2.dp)) {
                            Icon(Icons.Rounded.Logout, stringResource(R.string.operation_logout))
                        }
                    }
                }
            }
        },
        content = content
    )
}