package moe.chen.budgeteer.room

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Category::class,
            parentColumns = ["cid"],
            childColumns = ["cid"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [
        Index(value = ["cid"], name = "foreign_category_id", unique = false)
    ]
)
data class CategoryBudget(
    @PrimaryKey val id: Int? = null,
    @ColumnInfo(name = "budget") val budget: Double,
    @ColumnInfo(name = "cid") val cid: Int,
    @ColumnInfo(name = "year") val year: Int,
    @ColumnInfo(name = "month") val month: Int,
)

@Dao
interface CategoryBudgetDao {
    @Query("""
        SELECT * FROM categorybudget
        WHERE cid = :categoryId AND
        (year < :year OR (year = :year AND month <= :month))
        ORDER BY year DESC, month DESC
        LIMIT 1
    """)
    fun getActiveBudget(
        categoryId: Int,
        year: Int,
        month: Int
    ): Flow<CategoryBudget?>

    @Insert
    suspend fun createBudget(budget: CategoryBudget)

    @Update
    suspend fun updateBudget(budget: CategoryBudget)

    @Query("""SELECT * FROM categorybudget WHERE cid = :categoryId""")
    fun listBudgets(categoryId: Int): Flow<List<CategoryBudget>>
}