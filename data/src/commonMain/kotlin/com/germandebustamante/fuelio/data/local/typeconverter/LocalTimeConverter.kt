package com.germandebustamante.fuelio.data.local.typeconverter

import androidx.room.TypeConverter
import kotlinx.datetime.LocalTime

object LocalTimeConverter {
    @TypeConverter
    fun toDate(dateString: String?): LocalTime? = dateString?.let { LocalTime.parse(dateString) }

    @TypeConverter
    fun toDateString(date: LocalTime?): String? {
        return date?.toString()
    }
}