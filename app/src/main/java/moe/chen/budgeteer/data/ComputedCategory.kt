package moe.chen.budgeteer.data

import android.util.Log
import androidx.compose.ui.graphics.Color
import moe.chen.budgeteer.room.BudgetEntry
import moe.chen.budgeteer.room.Category
import moe.chen.budgeteer.ui.theme.NotOkColor
import moe.chen.budgeteer.ui.theme.OkColor
import java.time.LocalDateTime
import java.time.YearMonth

data class ComputedField(
    val label: String,
    val computation: (Category, List<BudgetEntry>) -> Double,
    val colorSelector: (Category, Double) -> Color,
)

val BudgetField = ComputedField(
    label = "Budget",
    computation = { category, _ -> category.budget },
    colorSelector = { _, _ -> Color.Blue }
)

val CurrentField = ComputedField(
    label = "Current",
    computation = { _, entries -> entries.sumOf { it.amount } },
    colorSelector = { category, value ->
        if (category.budget <= value) {
            NotOkColor
        } else {
            OkColor
        }
    }
)

val TrendField = ComputedField(
    label = "Trend",
    computation = { _, entries ->
        val sum = entries.sumOf { it.amount }
        val today = LocalDateTime.now()
        val monthLength = YearMonth.of(today.year, today.month).lengthOfMonth()
        Log.d("TrendField", "length of month: $monthLength " +
                "day of month: ${today.dayOfMonth}")
        ((sum / today.dayOfMonth) * monthLength)
    },
    colorSelector = { category, value ->
        if (category.budget <= value) {
            NotOkColor
        } else {
            OkColor
        }
    }
)

val SpendPerDayField = ComputedField(
    label = "To spend/day",
    computation = { category, entries ->
        val sum = entries.sumOf { it.amount }
        val today = LocalDateTime.now()
        val monthLength = YearMonth.of(today.year, today.month).lengthOfMonth()
        ((category.budget - sum) / (monthLength - today.dayOfMonth))
    },
    colorSelector = { _, value ->
        if (value <= 0.0) {
            NotOkColor
        } else {
            OkColor
        }
    }
)

val UnspendMoney = ComputedField(
    label = "Unspend Money per day",
    computation = { category, entries ->
        val sum = entries.sumOf { it.amount }
        val today = LocalDateTime.now()
        val monthLength = YearMonth.of(today.year, today.month).lengthOfMonth()
        val budgetUntilNow = (category.budget / monthLength) * today.dayOfMonth
        budgetUntilNow - sum
    },
    colorSelector = { _, value ->
        if (value <= 0.0) {
            NotOkColor
        } else {
            OkColor
        }
    }
)