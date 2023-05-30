package moe.chen.budgeteer.preview

import moe.chen.budgeteer.room.BudgetEntry
import moe.chen.budgeteer.room.Category
import java.time.ZonedDateTime

fun exampleCategories() = listOf(
    Category(
        cid = 0,
        label = "Arzt",
        budget = 10.0,
        uid = 1,
        order = null,
    ),
    Category(
        cid = 1,
        label = "Haushalt",
        budget = 200.0,
        uid = 1,
        order = null,
    ),
    Category(
        cid = 2,
        label = "Einkauf",
        budget = 10.0,
        uid = 1,
        order = null,
    ),
    Category(
        cid = 3,
        label = "Spaß",
        budget = 10.0,
        uid = 1,
        order = null,
    ),
)

fun exampleEntries() = listOf(
    BudgetEntry(bid = 0, amount = 1.25, cid = 0, date = ZonedDateTime.now()),
    BudgetEntry(bid = 1, amount = 5.75, cid = 0, date = ZonedDateTime.now()),
    BudgetEntry(bid = 2, amount = 8.50, cid = 0, date = ZonedDateTime.now()),
    BudgetEntry(bid = 3, amount = 10.2, cid = 0, date = ZonedDateTime.now()),
)