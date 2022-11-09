package moe.chen.budgeteer

import java.text.NumberFormat
import java.util.*

val defaultNumberFormat: NumberFormat = run {
    val format = NumberFormat.getCurrencyInstance()
    format.maximumFractionDigits = 2
    format.currency = Currency.getInstance("EUR")
    format
}

val defaultCompactFormat: NumberFormat = run {
    val format = NumberFormat.getInstance()
    format.maximumFractionDigits = 2
    format.minimumFractionDigits = 2
    format
}

fun formatCompact(value: Double) = if (value <= 0) {
    defaultCompactFormat.format(value)
} else {
    "+${defaultCompactFormat.format(value)}"
}

