package moe.chen.budgeteer.room

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import moe.chen.budgeteer.data.DateTimeHandler

@Database(
    entities = [
        User::class,
        Category::class,
        BudgetEntry::class,
        UserSetting::class,
    ],
    version = 6,
    autoMigrations = [
        AutoMigration(from = 2, to = 3),
    ],
    exportSchema = true,
)
@TypeConverters(DateTimeHandler::class, CategoryTypeConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao

    abstract fun categoryDao(): CategoryDao

    abstract fun budgetEntryDao(): BudgetEntryDao

    abstract fun userSettingDao(): UserSettingDao
}