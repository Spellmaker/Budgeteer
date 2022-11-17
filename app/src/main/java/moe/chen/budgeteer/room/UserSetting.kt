package moe.chen.budgeteer.room

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["uid"],
            childColumns = ["uid"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [
        Index(value = ["uid"], name = "foreign_user_setting_uid", unique = true)
    ]
)
data class UserSetting(
    @PrimaryKey val id: Int? = null,
    @ColumnInfo(name = "uid") val uid: Int,
    @ColumnInfo(name = "currency") val currency: String,
    @ColumnInfo(name = "cat_show_budget") val catShowBudget: Int,
    @ColumnInfo(name = "cat_show_current") val catShowCurrent: Int,
    @ColumnInfo(name = "cat_show_trend") val catShowTrend: Int,
    @ColumnInfo(name = "cat_show_spen_per_day") val catShowSpendPerDay: Int,
    @ColumnInfo(name = "cat_show_unspend") val catShowUnspend: Int,
) {
    companion object {
        fun getDefault(user: User) = getDefault(user.uid!!)

        fun getDefault(userId: Int) = UserSetting(
            id = null,
            uid = userId,
            currency = "EUR",
            catShowCurrent = 0,
            catShowBudget = 1,
            catShowTrend = 2,
            catShowSpendPerDay = 3,
            catShowUnspend = -1,
        )
    }
}

@Dao
interface UserSettingDao {
    @Insert
    suspend fun createSettings(settings: UserSetting)

    @Update
    suspend fun updateSettings(settings: UserSetting)

    @Query("SELECT * FROM UserSetting WHERE uid = :uid")
    fun findForUser(uid: Int): Flow<UserSetting?>
}