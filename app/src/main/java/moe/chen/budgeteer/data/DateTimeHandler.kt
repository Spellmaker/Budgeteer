package moe.chen.budgeteer.data

import androidx.room.TypeConverter
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class DateTimeHandler {

    private val format = DateTimeFormatter.ISO_OFFSET_DATE_TIME

    @TypeConverter
    fun fromTimestamp(value: String): ZonedDateTime {
        return ZonedDateTime.parse(value, format)
    }

    @TypeConverter
    fun dateToTimestamp(date: ZonedDateTime): String {
        return date.format(format)
    }
}