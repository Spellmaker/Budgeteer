package moe.chen.budgeteer.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import moe.chen.budgeteer.data.PreferenceRepository
import moe.chen.budgeteer.room.User
import moe.chen.budgeteer.room.UserRepository
import moe.chen.budgeteer.room.UserSetting
import moe.chen.budgeteer.room.UserSettingDao
import java.util.*
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val preferenceRepository: PreferenceRepository,
    private val userRepository: UserRepository,
    private val settingDao: UserSettingDao,
) : ViewModel() {
    private val _preferenceUser = MutableStateFlow<User?>(null)
    private val _validatedUser = MutableStateFlow<User?>(null)

    fun validateCurrentUser() = _validatedUser.asStateFlow()

    init {
        Log.d(
            "UserViewModel",
            "initializing new view model, source: ${Arrays.toString(Thread.currentThread().stackTrace)}"
        )

        viewModelScope.launch {
            preferenceRepository.currentUserFlow
                .distinctUntilChanged()
                .collect { user ->
                    Log.d(
                        "UserViewModel",
                        "updating from preference repository with $user"
                    )
                    _preferenceUser.value = user
                    if (user != null) {
                        launch {
                            userRepository.findUser(user.username, user.secret)
                                .takeWhile {
                                    user.username == _preferenceUser.value?.username
                                            && user.secret == _preferenceUser.value?.secret
                                }
                                .collect {
                                    Log.d("UserViewModel", "new auth user $it")
                                    _validatedUser.value = it
                                }
                        }
                    }
                }
        }
    }

    fun addUser(
        user: User,
        onSuccess: (Long) -> Unit,
        onError: () -> Unit,
    ) = viewModelScope.launch {
        try {
            val id = userRepository.createUser(user)
            settingDao.createSettings(UserSetting.getDefault(id.toInt()))
            onSuccess(id)
        } catch (t: Throwable) {
            onError()
        }
    }

    fun setActiveUser(user: User) = runBlocking {
        preferenceRepository.setUser(user)
        Log.d("UserViewModel", "updated user to $user")
    }
}