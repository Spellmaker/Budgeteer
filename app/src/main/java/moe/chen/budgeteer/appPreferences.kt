package moe.chen.budgeteer

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import moe.chen.budgeteer.data.AppDatabase
import moe.chen.budgeteer.data.User

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

private val USER_KEY = stringPreferencesKey("user_name")
private val SECRET_KEY = stringPreferencesKey("user_secret")

suspend fun Context.storeUser(user: User) {
    dataStore.edit { settings ->
        settings[USER_KEY] = user.username
        settings[SECRET_KEY] = user.secret
    }
}

suspend fun Context.findCachedUser(): User? {
    val store = dataStore.data.first()

    val user = store[USER_KEY]
    val secret = store[SECRET_KEY]

    if (user == null || secret == null) {
        return null
    }
    return AppDatabase.DB(this).userDao().findUser(user, secret)
}