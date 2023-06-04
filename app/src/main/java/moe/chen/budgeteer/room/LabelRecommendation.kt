package moe.chen.budgeteer.room

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Entity(
    foreignKeys =[
        ForeignKey(
            entity = Category::class,
            parentColumns = ["cid"],
            childColumns = ["cid"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [
        Index(value = ["cid"], name = "foreign_label_category", unique = false)
    ]
)
data class LabelRecommendation(
    @PrimaryKey val id: Int? = null,
    @ColumnInfo(name = "cid") val cid: Int,
    @ColumnInfo(name = "label") val label: String,
    @ColumnInfo(name = "priority") val priority: Int,
)

@Dao
interface LabelRecommendationDao {
    @Query("SELECT * FROM labelrecommendation WHERE cid = :cid ORDER BY priority ASC")
    fun getForCategory(cid: Int): Flow<List<LabelRecommendation>>

    @Query("DELETE FROM labelrecommendation WHERE cid = :cid")
    suspend fun deleteAll(cid: Int)

    @Insert
    suspend fun storeAll(labels: List<LabelRecommendation>)
}