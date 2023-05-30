package moe.chen.budgeteer.room

import androidx.room.*
import kotlinx.coroutines.flow.Flow
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
    suspend fun createEntry(entry: BudgetEntry)

    @Query("SELECT * FROM budgetentry WHERE bid = :bid")
    fun findEntry(bid: Int): Flow<BudgetEntry>

    @Delete
    suspend fun deleteEntry(entry: BudgetEntry)

    @Query("DELETE FROM budgetentry WHERE cid = :cid")
    suspend fun deleteAll(cid: Int)

    @Query("SELECT * FROM budgetentry " +
            "WHERE cid = :cid ")
    fun listEntries(cid: Int): Flow<List<BudgetEntry>>

    @Query("SELECT * FROM budgetentry " +
            "WHERE cid = :cid " +
            "AND strftime('%m', date) = :month " +
            "AND strftime('%Y', date) = :year")
    fun internalListEntries(cid: Int, month: String, year: String): Flow<List<BudgetEntry>>

    @Query("SELECT * FROM budgetentry")
    suspend fun findAllEntries(): List<BudgetEntry>

    @Update
    suspend fun updateEntry(entry: BudgetEntry)

    fun listEntries(cid: Int, month: Int, year: Int): Flow<List<BudgetEntry>> {
        val actualMonth = if (month < 10) {
            "0$month"
        } else {
            "$month"
        }
        return internalListEntries(cid, actualMonth, "$year")
    }
}