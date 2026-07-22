package com.germandebustamante.fuelio.data.local.typeconverter

import androidx.room.TypeConverter
import kotlinx.datetime.DayOfWeek

object DayOfWeekConverter {
    @TypeConverter
    fun toDate(dateString: String?): DayOfWeek? = dateString?.let { DayOfWeek.valueOf(dateString) }

    @TypeConverter
    fun toDateString(date: DayOfWeek?): String? = date?.name
}