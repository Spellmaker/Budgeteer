package moe.chen.budgeteer.widgets

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowLeft
import androidx.compose.material.icons.automirrored.rounded.ArrowRight
import androidx.compose.material.icons.filled.Today
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import moe.chen.budgeteer.R
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@Preview(showBackground = true)
@Composable
fun MonthSelector(
    onMonthChange: (ZonedDateTime) -> Unit = {},
    currentMonth: ZonedDateTime = ZonedDateTime.now(),
    debugHook: () -> Unit = {},
) {
    val formatter by remember { mutableStateOf(DateTimeFormatter.ofPattern("MM/yyyy")) }

    Row(modifier = Modifier
        .padding(2.dp)
        .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        /* Button(onClick = debugHook) {
            Text("Debug")
        } */
        Button(
            modifier = Modifier.padding(5.dp),
            onClick = { onMonthChange(currentMonth.minusMonths(1)) }) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowLeft,
                contentDescription = stringResource(id = R.string.operation_previous)
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                modifier = Modifier.padding(5.dp),
                text = formatter.format(currentMonth)
            )
            Icon(
                modifier = Modifier.clickable {
                  onMonthChange(ZonedDateTime.now())
                },
                imageVector = Icons.Default.Today,
                contentDescription = stringResource(id = R.string.operation_today)
            )
        }
        Button(
            modifier = Modifier.padding(5.dp),
            onClick = { onMonthChange(currentMonth.plusMonths(1)) }) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowRight,
                contentDescription = stringResource(id = R.string.operation_previous)
            )
        }
    }
}