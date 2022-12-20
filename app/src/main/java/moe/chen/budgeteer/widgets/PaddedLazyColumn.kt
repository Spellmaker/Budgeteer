package moe.chen.budgeteer.widgets

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp

private sealed class Container<T> {
    class Dummy<T>: Container<T>()

    class Element<T>(val e: T): Container<T>()
}

@Composable
fun <T> PaddedLazyColumn(
    modifier: Modifier,
    bottomPadding: Dp,
    elements: List<T>,
    content: @Composable (Int, T) -> Unit,
) {
    LazyColumn(modifier = modifier) {
        itemsIndexed(items = elements.map { Container.Element(it) }.plus(Container.Dummy())) {
            index, element ->
            when (element) {
                is Container.Dummy -> Column(modifier = Modifier.height(bottomPadding)) {
                    /* intentionally empty */
                }
                is Container.Element -> content(index, element.e)
            }
        }
    }
}