package moe.chen.budgeteer.room

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.DeleteColumn
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.AutoMigrationSpec
import moe.chen.budgeteer.data.DateTimeHandler

@Database(
    entities = [
        User::class,
        Category::class,
        BudgetEntry::class,
        UserSetting::class,
        CategoryBudget::class,
        LabelRecommendation::class,
    ],
    version = 9,
    autoMigrations = [
        AutoMigration(from = 2, to = 3),
        AutoMigration(from = 6, to = 7, spec = AppDatabase.AutoMigrate_6_7::class),
        AutoMigration(from = 7, to = 8),
        AutoMigration(from = 8, to = 9),
    ],
    exportSchema = true,
)
@TypeConverters(DateTimeHandler::class)
abstract class AppDatabase : RoomDatabase() {
    @DeleteColumn(tableName = "Category", columnName = "budget")
    class AutoMigrate_6_7: AutoMigrationSpec

    abstract fun userDao(): UserDao

    abstract fun categoryDao(): CategoryDao

    abstract fun budgetEntryDao(): BudgetEntryDao

    abstract fun userSettingDao(): UserSettingDao

    abstract fun categoryBudgetDao(): CategoryBudgetDao

    abstract fun labelRecommendationDao(): LabelRecommendationDao
}