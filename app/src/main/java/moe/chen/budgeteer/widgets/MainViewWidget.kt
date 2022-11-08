package moe.chen.budgeteer.widgets

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MainViewWidget(
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
                        text = "Budgeteer$addendum",
                        style = MaterialTheme.typography.h6
                    )
                    Button(onClick = logout) {
                        Text("Logout")
                    }
                }
            }
        },
        content = content
    )
}