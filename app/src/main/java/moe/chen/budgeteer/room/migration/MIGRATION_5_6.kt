package moe.chen.budgeteer.room.migration

import android.annotation.SuppressLint
import android.util.Log
import androidx.core.database.getIntOrNull
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

val MIGRATION_5_6 = object : Migration(5, 6) {
    @SuppressLint("Range")
    override fun migrate(database: SupportSQLiteDatabase) {
        Log.d("migration", "Migration execution is starting")

        database.execSQL("""
            CREATE TABLE IF NOT EXISTS `CategoryBudget` 
            (`id` INTEGER, `budget` REAL NOT NULL, `cid` INTEGER NOT NULL, `year` INTEGER NOT NULL, `month` INTEGER NOT NULL, PRIMARY KEY(`id`), FOREIGN KEY(`cid`) REFERENCES `Category`(`cid`) ON UPDATE NO ACTION ON DELETE CASCADE )
        """.trimIndent())

        database.execSQL("""
            CREATE INDEX IF NOT EXISTS `foreign_category_id` ON `CategoryBudget` (`cid`)
        """.trimIndent())

        Log.d("migration", "Created table")

        database.query(
            """
            SELECT * FROM Category
        """.trimIndent()
        ).use { categoryCursor ->
            if (categoryCursor.moveToFirst()) {
                while (!categoryCursor.isAfterLast) {
                    val cid = categoryCursor
                        .getIntOrNull(categoryCursor.getColumnIndex("cid"))
                    val budget = categoryCursor
                        .getDouble(categoryCursor.getColumnIndex("cid"))

                    database.query("""SELECT * FROM BudgetEntry WHERE cid = $cid""".trimIndent())
                            .use { budgetEntryCursor ->
                        val entryDates = mutableListOf<ZonedDateTime>()
                        if (budgetEntryCursor.moveToFirst()) {
                            while (!budgetEntryCursor.isAfterLast) {
                                val value = budgetEntryCursor
                                    .getString(budgetEntryCursor
                                        .getColumnIndex("date"))
                                entryDates.add(ZonedDateTime.parse(
                                    value,
                                    DateTimeFormatter.ISO_OFFSET_DATE_TIME
                                ))
                                budgetEntryCursor.moveToNext()
                            }
                        }
                        val minDate = entryDates.minOrNull() ?: ZonedDateTime.now()

                        database.execSQL("""
                            INSERT INTO `CategoryBudget` (`budget`, `cid`, `month`, `year`)
                            VALUES ($budget, $cid, ${minDate.monthValue}, ${minDate.year});
                        """.trimIndent())

                        categoryCursor.moveToNext()
                    }
                }
            }
        }
    }
}