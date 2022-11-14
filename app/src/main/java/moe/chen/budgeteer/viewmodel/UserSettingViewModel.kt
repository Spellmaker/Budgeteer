package moe.chen.budgeteer.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import moe.chen.budgeteer.room.User
import moe.chen.budgeteer.room.UserSetting
import moe.chen.budgeteer.room.UserSettingDao
import java.text.NumberFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class UserSettingViewModel @Inject constructor(
    private val userSettingDao: UserSettingDao,
) : ViewModel() {

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

    private var currentUser: User? = null

    private val _settings = MutableStateFlow<UserSetting?>(invalidSettings)

    private val _converterDefault = MutableStateFlow<NumberFormat?>(null)

    val converterDefault = _converterDefault.asStateFlow()


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

    fun listenToUser(user: User): StateFlow<UserSetting?> {
        _settings.value = invalidSettings
        viewModelScope.launch {
            currentUser = user
            userSettingDao.findForUser(user.uid!!)
                .takeWhile { currentUser?.uid == user.uid }
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
        return _settings.asStateFlow()
    }

    fun createDefaultSetting() {
        if (_settings.value == null) {
            viewModelScope.launch {
                userSettingDao.createSettings(
                    UserSetting.getDefault(currentUser!!)
                )
            }
        }
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