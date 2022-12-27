package moe.chen.budgeteer.room.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import moe.chen.budgeteer.room.CategoryType

val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            "ALTER TABLE category ADD COLUMN 'type' TEXT " +
                    "NOT NULL DEFAULT '${CategoryType.PER_MONTH.name}'"
        )
    }
}