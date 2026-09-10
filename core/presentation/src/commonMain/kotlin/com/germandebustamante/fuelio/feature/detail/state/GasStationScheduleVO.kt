package com.germandebustamante.fuelio.feature.detail.state

import com.germandebustamante.fuelio.core.domain.gasstation.model.ScheduleSegmentBO
import com.germandebustamante.fuelio.core.util.asHhMm
import kotlinx.datetime.DayOfWeek

sealed interface ScheduleDayStatus {
    data object Closed : ScheduleDayStatus
    data object AlwaysOpen : ScheduleDayStatus
    data class Hours(val start: String, val end: String) : ScheduleDayStatus
}

data class ScheduleDayVO(val dayOfWeek: DayOfWeek, val isToday: Boolean, val status: ScheduleDayStatus)

private val weekDayOrder = listOf(
    DayOfWeek.MONDAY,
    DayOfWeek.TUESDAY,
    DayOfWeek.WEDNESDAY,
    DayOfWeek.THURSDAY,
    DayOfWeek.FRIDAY,
    DayOfWeek.SATURDAY,
    DayOfWeek.SUNDAY,
)

fun List<ScheduleSegmentBO>.toScheduleDays(today: DayOfWeek): List<ScheduleDayVO> = weekDayOrder.map { day ->
    val segment = firstOrNull { it.coversDay(day) }
    val status = when {
        segment == null -> ScheduleDayStatus.Closed
        segment.isAlwaysOpen -> ScheduleDayStatus.AlwaysOpen
        else -> ScheduleDayStatus.Hours(
            start = segment.startTime?.asHhMm().orEmpty(),
            end = segment.endTime?.asHhMm().orEmpty(),
        )
    }
    ScheduleDayVO(dayOfWeek = day, isToday = day == today, status = status)
}
