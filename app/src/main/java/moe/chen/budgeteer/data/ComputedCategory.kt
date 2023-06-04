package moe.chen.budgeteer.data

import android.util.Log
import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import moe.chen.budgeteer.R
import moe.chen.budgeteer.room.BudgetEntry
import moe.chen.budgeteer.room.Category
import moe.chen.budgeteer.room.CategoryBudget
import moe.chen.budgeteer.room.UserSetting
import moe.chen.budgeteer.ui.theme.*
import java.time.LocalDateTime
import java.time.YearMonth

data class ComputedField(
    @StringRes val label: Int,
    @StringRes val description: Int,
    val extractor: (UserSetting) -> Int,
    val setter: (UserSetting, Int) -> UserSetting,
    val computation: (Category, CategoryBudget, List<BudgetEntry>) -> Double,
    val colorSelector: (Category, CategoryBudget, Double, Boolean) -> Color,
)

val BudgetField = ComputedField(
    label = R.string.field_budget_label,
    description = R.string.field_budget_description,
    extractor = { it.catShowBudget },
    setter = { setting, pos -> setting.copy(catShowBudget = pos) },
    computation = { category, budget, _ -> budget.budget },
    colorSelector = { _, _, _, darkMode ->
        if (darkMode) {
            BaseDarkMode
        } else {
            BaseLightMode
        }
    }
)

val CurrentField = ComputedField(
    label = R.string.field_current_label,
    description = R.string.field_current_description,
    extractor = { it.catShowCurrent },
    setter = { setting, pos -> setting.copy(catShowCurrent = pos) },
    computation = { _, _, entries -> entries.sumOf { it.amount } },
    colorSelector = { category, budget, value, darkMode ->
        if (budget.budget <= value) {
            if (darkMode) {
                NotOkColorDarkMode
            } else {
                NotOkColor
            }
        } else {
            OkColor
        }
    }
)

val TrendField = ComputedField(
    label = R.string.field_trend_label,
    description = R.string.field_trend_description,
    extractor = { it.catShowTrend },
    setter = { setting, pos -> setting.copy(catShowTrend = pos) },
    computation = { _, _, entries ->
        val sum = entries.sumOf { it.amount }
        val today = LocalDateTime.now()
        val monthLength = YearMonth.of(today.year, today.month).lengthOfMonth()
        Log.d(
            "TrendField", "length of month: $monthLength " +
                    "day of month: ${today.dayOfMonth}"
        )
        ((sum / today.dayOfMonth) * monthLength)
    },
    colorSelector = { category, budget, value, darkMode ->
        if (budget.budget <= value) {
            if (darkMode) {
                NotOkColorDarkMode
            } else {
                NotOkColor
            }
        } else {
            OkColor
        }
    }
)

val SpendPerDayField = ComputedField(
    label = R.string.field_spend_per_day_label,
    description = R.string.field_spend_per_day_description,
    extractor = { it.catShowSpendPerDay },
    setter = { setting, pos -> setting.copy(catShowSpendPerDay = pos) },
    computation = { category, budget, entries ->
        val sum = entries.sumOf { it.amount }
        val today = LocalDateTime.now()
        val monthLength = YearMonth.of(today.year, today.month).lengthOfMonth()
        ((budget.budget - sum) / (monthLength - (today.dayOfMonth - 1)))
    },
    colorSelector = { _, _, value, darkMode ->
        if (value <= 0.0) {
            if (darkMode) {
                NotOkColorDarkMode
            } else {
                NotOkColor
            }
        } else {
            OkColor
        }
    }
)

val UnspendMoney = ComputedField(
    label = R.string.field_unspend_label,
    description = R.string.field_unspend_description,
    extractor = { it.catShowUnspend },
    setter = { setting, pos -> setting.copy(catShowUnspend = pos) },
    computation = { category, budget, entries ->
        val sum = entries.sumOf { it.amount }
        val today = LocalDateTime.now()
        val monthLength = YearMonth.of(today.year, today.month).lengthOfMonth()
        val budgetUntilNow = (budget.budget / monthLength) * today.dayOfMonth
        budgetUntilNow - sum
    },
    colorSelector = { _, _, value, darkMode ->
        if (value <= 0.0) {
            if (darkMode) {
                NotOkColorDarkMode
            } else {
                NotOkColor
            }
        } else {
            OkColor
        }
    }
)

val allCategories = listOf(
    BudgetField, CurrentField, TrendField, SpendPerDayField, UnspendMoney
)