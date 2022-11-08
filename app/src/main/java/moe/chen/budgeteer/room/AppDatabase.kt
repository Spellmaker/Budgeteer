package moe.chen.budgeteer.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import moe.chen.budgeteer.data.DateTimeHandler

@Database(
    entities = [
        User::class,
        Category::class,
        BudgetEntry::class,
    ], version = 2
)
@TypeConverters(DateTimeHandler::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao

    abstract fun categoryDao(): CategoryDao

    abstract fun budgetEntryDao(): BudgetEntryDao
}