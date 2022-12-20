package moe.chen.budgeteer.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import moe.chen.budgeteer.room.UserSetting
import moe.chen.budgeteer.room.UserSettingDao
import java.text.NumberFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class UserSettingViewModel @Inject constructor(
    private val userSettingDao: UserSettingDao,
) : ViewModel() {

    private val userId: Int = 0

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
                        Log.d(
                            "UserSettingViewModel",
                            "Updating converters with new symbol ${entry.currency}"
                        )
                        updateConverters(entry)
                    }
                }
        }
    }

    companion object {
        fun makeConverter(currency: String): NumberFormat {
            val format1 = NumberFormat.getCurrencyInstance(Locale.GERMAN)
            format1.maximumFractionDigits = 2
            format1.currency = Currency.getInstance(currency)
            return format1
        }
    }


    private fun updateConverters(currentSettings: UserSetting) {
        _converterDefault.value = makeConverter(currentSettings.currency)
    }

    fun createDefault() {
        viewModelScope.launch {
            userSettingDao.createSettings(UserSetting.getDefault(0))
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