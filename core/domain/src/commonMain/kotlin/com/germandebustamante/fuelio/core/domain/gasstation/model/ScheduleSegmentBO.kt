package com.germandebustamante.fuelio.core.domain.gasstation.model

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime

data class ScheduleSegmentBO(val startDay: DayOfWeek, val endDay: DayOfWeek, val startTime: LocalTime?, val endTime: LocalTime?) {
    val isAlwaysOpen: Boolean = startTime == null && endTime == null

    fun coversDay(day: DayOfWeek): Boolean = if (startDay.ordinal <= endDay.ordinal) {
        day.ordinal in startDay.ordinal..endDay.ordinal
    } else {
        day.ordinal >= startDay.ordinal || day.ordinal <= endDay.ordinal
    }
}
