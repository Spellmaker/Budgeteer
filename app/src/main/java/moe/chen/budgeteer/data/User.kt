package moe.chen.budgeteer.data

import androidx.room.*

@Entity(
    indices = [
        Index(
            value = ["username"],
            name = "unique_username",
            unique = true,
        )
    ]
)
data class User(
    @PrimaryKey val uid: Int? = null,
    @ColumnInfo(name = "username") val username: String,
    @ColumnInfo(name = "secret") val secret: String,
)

@Dao
interface UserDao {
    @Query("SELECT * FROM user WHERE username = :name AND secret = :secret")
    suspend fun findUser(name: String, secret: String): User?

    @Insert
    suspend fun createUser(user: User): Long
}

