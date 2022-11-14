package moe.chen.budgeteer.widgets

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import moe.chen.budgeteer.R

@Preview(showBackground = true)
@Composable
fun Preview() {
    OrderSelectionWidget(
        elements = listOf("A", "B", "C", "D"),
        orderChanged = {},
        visibilities = listOf(true, true, true, false),
        visibilityChanged = { index, newValue -> }
    ) { text, modifier ->
        Text(text, modifier = modifier)
    }
}

@Composable
fun <T> OrderSelectionWidget(
    elements: List<T>,
    visibilities: List<Boolean>,
    visibilityChanged: (Int, Boolean) -> Unit,
    orderChanged: (List<T>) -> Unit,
    display: @Composable (T, Modifier) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
        ) {
            itemsIndexed(elements) { index, item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(5.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxHeight(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        display(item, Modifier.padding(5.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(stringResource(R.string.operation_show))
                                Checkbox(
                                    checked = visibilities[index],
                                    onCheckedChange = {
                                        visibilityChanged(index, it)
                                    },
                                )
                            }
                            Column(modifier = Modifier.padding(5.dp)) {
                                Button(
                                    onClick = {
                                        val mutable = elements.toMutableList()
                                        val prev = mutable[index - 1]
                                        mutable[index - 1] = mutable[index]
                                        mutable[index] = prev
                                        orderChanged(mutable)
                                    },
                                    enabled = visibilities[index] && index >= 1
                                ) {
                                    Icon(
                                        Icons.Rounded.KeyboardArrowUp,
                                        stringResource(R.string.operation_up)
                                    )
                                }
                                Button(
                                    onClick = {
                                        val mutable = elements.toMutableList()
                                        val prev = mutable[index + 1]
                                        mutable[index + 1] = mutable[index]
                                        mutable[index] = prev
                                        orderChanged(mutable)
                                    },
                                    enabled = visibilities[index]
                                            && index < elements.size - 1
                                            && visibilities[index + 1]
                                ) {
                                    Icon(
                                        Icons.Rounded.KeyboardArrowDown,
                                        stringResource(R.string.operation_down)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}