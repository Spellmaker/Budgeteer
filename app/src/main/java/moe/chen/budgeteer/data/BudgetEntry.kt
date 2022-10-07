package moe.chen.budgeteer.data

import androidx.room.*
import java.time.ZonedDateTime

@Entity(
    foreignKeys = [ForeignKey(
        entity = Category::class,
        parentColumns = ["cid"],
        childColumns = ["cid"],
    )],
    indices = [
        Index(value = ["cid"], name = "foreign_cid", unique = false)
    ]
)
data class BudgetEntry(
    @PrimaryKey val bid: Int? = null,
    @ColumnInfo(name = "amount") val amount: Double,
    @ColumnInfo(name = "cid") val cid: Int,
    @ColumnInfo(name = "date") val date: ZonedDateTime,
)

@Dao
interface BudgetEntryDao {
    @Insert
    fun createEntry(entry: BudgetEntry)

    @Delete
    fun deleteEntry(entry: BudgetEntry)

    @Query("SELECT * FROM budgetentry " +
            "WHERE cid = :cid " +
            "AND strftime('%m', date) = :month")
    fun internalListEntries(cid: Int, month: String): List<BudgetEntry>

    fun listEntries(cid: Int, month: Int): List<BudgetEntry> {
        val actualMonth = if (month < 10) {
            "0$month"
        } else {
            "$month"
        }
        return internalListEntries(cid, actualMonth)
    }
}