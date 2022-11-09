package moe.chen.budgeteer.room

import android.util.Log
import androidx.room.*
import kotlinx.coroutines.flow.Flow

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
) {

    override fun equals(other: Any?): Boolean {
        Log.d(
            "User",
            "comparing $this with $other (${System.identityHashCode(this)} with ${
                System.identityHashCode(other)
            }) result is ${super.equals(other)}"
        )
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as User

        if (uid != other.uid) return false
        if (username != other.username) return false
        if (secret != other.secret) return false

        return true
    }

    override fun hashCode(): Int {
        var result = uid ?: 0
        result = 31 * result + username.hashCode()
        result = 31 * result + secret.hashCode()
        return result
    }
}

/*

    override fun equals(other: Any?): Boolean {


        return super.equals(other)
    }
 */

@Dao
interface UserDao {
    @Query("SELECT * FROM user WHERE username = :name AND secret = :secret")
    fun findUser(name: String, secret: String): Flow<User?>

    @Insert
    suspend fun createUser(user: User): Long
}
