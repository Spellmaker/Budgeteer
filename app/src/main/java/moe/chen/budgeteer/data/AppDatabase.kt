package moe.chen.budgeteer.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

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

    companion object {
        private var instance: AppDatabase? = null

        fun DB(context: Context): AppDatabase {
            if (instance == null) {
                instance = Room.databaseBuilder(
                    context,
                    AppDatabase::class.java, "database-name"
                ).build()
            }
            return instance!!
        }
    }
}