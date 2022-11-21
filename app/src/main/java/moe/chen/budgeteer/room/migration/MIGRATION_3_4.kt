package moe.chen.budgeteer.room.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migrate to new schema version by making all user-reference reference user with id 0 and
 * ensuring a default user with id 0 exists
 */
val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(database: SupportSQLiteDatabase) {
        /* figure out of user with id 0 exists */
        val userExists = database.query("SELECT * FROM user WHERE uid = 0").use { cursor ->
            cursor.count > 0
        }
        if (!userExists) {
            database
                .execSQL(
                    "INSERT INTO user (uid, username, secret) " +
                            "VALUES (0, 'default', 'default');"
                )
        }
        val settingsExists =
            database.query("SELECT * FROM UserSetting WHERE uid = 0").use { cursor ->
                cursor.count > 0
            }
        if (!settingsExists) {
            database.execSQL(
                "INSERT INTO UserSetting (" +
                        "id, uid, currency, cat_show_budget, cat_show_current, cat_show_trend," +
                        "cat_show_spen_per_day, cat_show_unspend) VALUES (" +
                        "0, 0, 'EUR', 0, 1, 2, 3, -1);")
        }
        /* remap all categories to point towards user */
        database.execSQL("UPDATE category SET uid = 0")
    }
}