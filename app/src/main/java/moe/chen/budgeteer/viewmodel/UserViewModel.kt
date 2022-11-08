package moe.chen.budgeteer.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import moe.chen.budgeteer.data.PreferenceRepository
import moe.chen.budgeteer.room.User
import moe.chen.budgeteer.room.UserRepository
import java.util.*
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val preferenceRepository: PreferenceRepository,
    private val userRepository: UserRepository,
) : ViewModel() {
    private val _validatedUserFlow = MutableStateFlow<User?>(null)

    val validUser = _validatedUserFlow.asStateFlow()

    private var currentUserFlow: Flow<User?> = flow { }

    init {
        Log.d(
            "UserViewModel",
            "initializing new view model, source: ${Arrays.toString(Thread.currentThread().stackTrace)}"
        )

        viewModelScope.launch(Dispatchers.IO) {
            preferenceRepository.currentUserFlow
                .distinctUntilChanged()
                .collect { user ->
                    Log.d("UserViewModel", "something emitted user $user")
                    if (user == null) {
                        // flow becomes a new flow emitting just null
                        updateFlow(flow { emit(null) })
                    } else {
                        updateFlow(userRepository.findUser(user.username, user.secret))
                    }
                }
        }
    }

    private fun updateFlow(usersFlow: Flow<User?>) {
        currentUserFlow = usersFlow
        viewModelScope.launch(Dispatchers.IO) {
            usersFlow
                .distinctUntilChanged()
                .takeWhile { usersFlow == currentUserFlow }
                .collect { user ->
                    _validatedUserFlow.value = user
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