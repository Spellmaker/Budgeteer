package moe.chen.budgeteer.widgets

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import java.util.*


@Preview(showBackground = true)
@Composable
fun WidgetPreview() {
    val elements = Currency.getAvailableCurrencies().toList()//listOf("AA", "BB", "AC", "AD")
    var currentState by remember { mutableStateOf(Currency.getInstance("EUR")) }
    FilteredSelectionWidget<Currency>(
        label = "Input Currency",
        items = elements,
        selectionChanged = { currentState = it },
        stringSelector = { it.currencyCode },
        filterSelector = { it.currencyCode },
        currentItem = currentState,
        initialExpanded = true
    ) { item, modifier ->
        Row(modifier = modifier) {
            Text(text = item.currencyCode)
        }
    }
}

@Composable
fun <T> FilteredSelectionWidget(
    label: String,
    items: List<T>,
    selectionChanged: (T) -> Unit,
    stringSelector: (T) -> String,
    filterSelector: (T) -> String,
    currentItem: T,
    initialExpanded: Boolean,
    displayElement: @Composable (T, Modifier) -> Unit,
) {
    val localFocusManager = LocalFocusManager.current
    var expanded by remember { mutableStateOf(initialExpanded) }
    var filter by remember { mutableStateOf(filterSelector(currentItem)) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    localFocusManager.clearFocus()
                })
            }
    ) {
        TextField(
            value = filter,
            onValueChange = { filter = it },
            label = { Text(label) },
            singleLine = true,
            modifier = Modifier
                .clickable {
                    Log.d("FilteredSelectionWidget", "clicked on text field")
                    expanded = true
                }
                .onFocusEvent {
                    if (it.hasFocus) {
                        Log.d("FilteredSelectionWidget", "focussed text field")
                        expanded = true
                    }
                }
                .padding(5.dp)
                .fillMaxWidth()

        )
        if (expanded) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth()
            ) {
                items(
                    items
                        .map { it to stringSelector(it).lowercase() }
                        .filter { it.second.contains(filter.lowercase()) }
                        .sortedBy { it.second }
                ) { (element, _) ->
                    Card(modifier = Modifier
                        .fillMaxWidth()
                        .padding(5.dp)) {
                        displayElement(element, Modifier
                            .padding(5.dp)
                            .clickable {
                                localFocusManager.clearFocus()
                                expanded = false
                                filter = filterSelector(element)
                                selectionChanged(element)
                            })
                    }
                }
            }
        }
    }
}