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
        ),
    ],
    indices = [
        Index(
            value = ["label"],
            name = "unique_label",
            unique = true
        ),
        Index(value = ["uid"], name = "foreign_uid", unique = false)
    ]
)
data class Category(
    @PrimaryKey val cid: Int? = null,
    @ColumnInfo(name = "label") val label: String,
    @ColumnInfo(name = "uid") val uid: Int,
    @ColumnInfo(name = "order") val order: Int?,
)

@Dao
interface CategoryDao {
    @Query("SELECT * FROM category WHERE uid = :uid")
    fun listCategories(uid: Int): Flow<List<Category>>

    @Query("SELECT * FROM category WHERE cid = :cid")
    fun findCategory(cid: Int): Flow<Category>

    @Insert
    suspend fun createCategory(category: Category): Long

    @Update
    suspend fun updateCategory(category: Category)

    @Update
    suspend fun updateCategories(categories: List<Category>)

    @Delete
    suspend fun deleteCategory(category: Category)
}