package moe.chen.budgeteer.viewmodel

import android.content.ContentResolver
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import moe.chen.budgeteer.room.BudgetEntryDao
import moe.chen.budgeteer.room.CategoryBudgetDao
import moe.chen.budgeteer.room.CategoryDao
import moe.chen.budgeteer.room.UserSetting
import moe.chen.budgeteer.room.UserSettingDao
import java.io.BufferedWriter
import java.io.OutputStreamWriter
import java.text.NumberFormat
import java.time.format.DateTimeFormatter
import java.util.Currency
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class UserSettingViewModel @Inject constructor(
    private val userSettingDao: UserSettingDao,
    private val categoryDao: CategoryDao,
    private val entryDao: BudgetEntryDao,
    private val budgetDAo: CategoryBudgetDao,
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

    fun exportData(uri: Uri, contentResolver: ContentResolver, callback: (Boolean) -> Unit) {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        viewModelScope.launch {
            try {
                Log.d("UserSettingViewModel", "Starting file write")
                contentResolver.openOutputStream(uri)!!.use { os ->
                    OutputStreamWriter(os).use { oswriter ->
                        BufferedWriter(oswriter).use { writer ->
                            Log.d("UserSettingViewModel", "emitting title line")
                            writer.write("date;amount;title;category;budget;\r\n")

                            val categoryList = categoryDao.listCategories(0).firstOrNull()
                            categoryList?.forEach { category ->
                                val entries = entryDao.listEntries(category.cid!!).firstOrNull()
                                Log.d("UserSettingViewModel", "entries $entries")
                                entries?.forEach { entry ->
                                    Log.d(
                                        "UserSettingViewModel",
                                        "handling entry $entry"
                                    )
                                    val budget = budgetDAo.getActiveBudget(
                                        categoryId = category.cid,
                                        year = entry.date.year,
                                        month = entry.date.monthValue,
                                    ).firstOrNull()

                                    Log.d("UserSettingViewModel", "emitting line")
                                    writer.write(
                                        "${formatter.format(entry.date)};" +
                                                "${entry.amount};" +
                                                "${entry.label};" +
                                                "${category.label};" +
                                                "${budget?.budget ?: -1};\r\n"
                                    )
                                }
                            }
                        }
                    }
                }
                callback(true)
            } catch(e: Exception) {
                Log.e("UserSettingViewModel", "error exporting", e)
                callback(false)
            }
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