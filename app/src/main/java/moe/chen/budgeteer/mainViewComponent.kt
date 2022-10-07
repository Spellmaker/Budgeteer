package moe.chen.budgeteer

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp


@Composable
fun MainViewComponent() = Surface(
    modifier = Modifier.fillMaxSize(),
    color = MaterialTheme.colors.background
) {
    Scaffold(
        topBar = {
            TopAppBar() {
                Text("This is the main view")
                Button(onClick = { /*TODO*/ }) {
                    Text("Logout")
                }
            }
        },
        content = {
            Column(
                modifier = Modifier
                    .padding(it)
                    .fillMaxSize()
                    .background(Color(-0x72919d)),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("Remaining content")
                Text("More content")

                (0 until 5).forEach { budgetIndex ->
                    Row(
                        modifier = Modifier
                            .border(
                                BorderStroke(Dp(2f), Color.Black)
                            )
                            .fillMaxWidth()
                        ,
                    ) {
                        Text("Budget $budgetIndex")
                    }
                }
            }
        }
    )


}