package moe.chen.budgeteer.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import moe.chen.budgeteer.data.PreferenceRepository
import moe.chen.budgeteer.room.User
import moe.chen.budgeteer.room.UserSetting
import moe.chen.budgeteer.room.UserSettingDao
import java.text.NumberFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class UserSettingViewModel @Inject constructor(
    private val userSettingDao: UserSettingDao,
    private val preferenceRepository: PreferenceRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val userId: Int = savedStateHandle.get<Int>("user")!!

    val invalidSettings = UserSetting(
        id = -1,
        uid = -1,
        currency = "",
        catShowBudget = -1,
        catShowCurrent = -1,
        catShowTrend = -1,
        catShowSpendPerDay = -1,
        catShowUnspend = -1,
    )

    private val _settings = MutableStateFlow<UserSetting?>(invalidSettings)

    val settings = _settings.asStateFlow()

    private val _converterDefault = MutableStateFlow<NumberFormat?>(null)

    val converterDefault = _converterDefault.asStateFlow()

    init {
        _settings.value = invalidSettings
        viewModelScope.launch {
            userSettingDao.findForUser(userId)
                .distinctUntilChanged()
                .collect { entry ->
                    Log.d("UserSettingViewModel", "emitting settings $entry")
                    _settings.value = entry
                    if (entry != null && entry != invalidSettings) {
                        Log.d("UserSettingViewModel", "Updating converters with new symbol ${entry.currency}")
                        updateConverters(entry)
                    }
                }
        }
    }


    private fun updateConverters(currentSettings: UserSetting) {
        val currencyToUse = if (currentSettings.currency == "€") {
            "EUR"
        } else {
            currentSettings.currency
        }
        val format1 = NumberFormat.getCurrencyInstance()
        format1.maximumFractionDigits = 2
        format1.currency = Currency.getInstance(currencyToUse)
        _converterDefault.value = format1
    }

    fun createDefaultSetting() {
        if (_settings.value == null) {
            viewModelScope.launch {
                userSettingDao.createSettings(
                    UserSetting.getDefault(userId)
                )
            }
        }
    }

    fun setActiveUser(user: User) = runBlocking {
        preferenceRepository.setUser(user)
        Log.d("UserViewModel", "updated user to $user")
    }

    fun updateSettings(settings: UserSetting) {
        val currentSettings = _settings.value
        if (currentSettings != null && currentSettings != invalidSettings) {
            viewModelScope.launch {
                userSettingDao.updateSettings(settings.copy(id = currentSettings.id!!))
            }
        }
    }
}