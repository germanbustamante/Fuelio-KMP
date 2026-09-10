package com.germandebustamante.fuelio.data.gasstation.local.model

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime

data class ScheduleSegmentEntity(val startDay: DayOfWeek, val endDay: DayOfWeek, val startTime: LocalTime?, val endTime: LocalTime?)
