package moe.chen.budgeteer.data

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Update

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
    @ColumnInfo(name = "budget") val budget: Double,
    @ColumnInfo(name = "uid") val uid: Int,
)

@Dao
interface CategoryDao {
    @Query("SELECT * FROM category WHERE uid = :uid")
    suspend fun listCategories(uid: Int): List<Category>

    @Insert
    suspend fun createCategory(category: Category): Long

    @Update
    suspend fun updateCategory(category: Category)
}