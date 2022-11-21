package moe.chen.budgeteer.room

import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Callback hook creating default user and settings on startup
 */
val SetupCallback = object : RoomDatabase.Callback() {
    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)

        db.execSQL(
            "INSERT INTO user (uid, username, secret) " +
                    "VALUES (0, 'default', 'default');"
        )
        db.execSQL(
            "INSERT INTO UserSetting (" +
                    "id, uid, currency, cat_show_budget, cat_show_current, cat_show_trend," +
                    "cat_show_spen_per_day, cat_show_unspend) VALUES (" +
                    "0, 0, 'EUR', 0, 1, 2, 3, -1);"
        )
    }
}