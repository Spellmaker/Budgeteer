package moe.chen.budgeteer.data

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import moe.chen.budgeteer.room.User
import java.io.IOException
import javax.inject.Inject

class PreferenceRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {

    private object Keys {
        val UserKey = stringPreferencesKey("current_user")
        val SecretKey = stringPreferencesKey("current_secret")
    }

    val currentUserFlow: Flow<User?> = dataStore.data
        .catch { error ->
            when (error) {
                is IOException -> emit(emptyPreferences())
                else -> throw error
            }
        }
        .map { preferences ->
            preferences[Keys.UserKey]?.let { user ->
                preferences[Keys.SecretKey]?.let { secret ->
                    User(null, user, secret)
                }
            }
        }

    suspend fun setUser(user: User?) {
        dataStore.edit { preferences ->
            Log.d("PreferenceRepository", "editing to reflect $user")
            if (user == null) {
                preferences.minusAssign(Keys.UserKey)
                preferences.minusAssign(Keys.SecretKey)
            } else {
                preferences[Keys.UserKey] = user.username
                preferences[Keys.SecretKey] = user.secret
            }
        }
    }
}

