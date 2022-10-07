package moe.chen.budgeteer.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject

class PreferenceRepository(
    @Inject
    private val dataStore: DataStore<Preferences>,
) {

    private object Keys {
        val UserKey = stringPreferencesKey("current_user")
        val SecretKey = stringPreferencesKey("current_secret")
        val UserId = intPreferencesKey("current_id")
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
                    preferences[Keys.UserId]?.let { id ->
                        User(id, user, secret)
                    }
                }
            }
        }
}

