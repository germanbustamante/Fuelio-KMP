package com.germandebustamante.fuelio.data.gasstation.remote.model

import com.germandebustamante.fuelio.core.domain.gasstation.model.GasStationBO
import com.germandebustamante.fuelio.core.domain.gasstation.model.ScheduleSegmentBO
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime

fun GasStationDTO.toDomain() = GasStationBO(
    id = id,
    name = name,
    address = address,
    city = city,
    municipality = municipality,
    province = province,
    zipCode = zipCode,
    latitude = latitude.parseToDoubleOrNull() ?: 0.0,
    longitude = longitude.parseToDoubleOrNull() ?: 0.0,
    schedule = schedule.parseSchedule(),
    gasolinePrice95 = gasolinePrice95?.parseToDoubleOrNull(),
    gasolinePrice98 = gasolinePrice98?.parseToDoubleOrNull(),
    dieselPrice = dieselPrice?.parseToDoubleOrNull(),
    dieselPremiumPrice = dieselPremiumPrice?.parseToDoubleOrNull(),
)

private fun String.parseToDoubleOrNull(): Double? = replace(",", ".").toDoubleOrNull()

internal fun String.parseSchedule(): List<ScheduleSegmentBO> = split(";").mapNotNull { segment ->
    runCatching {
        val colonIndex = segment.indexOf(':')
        if (colonIndex == -1) return@mapNotNull null
        val daysPart = segment.substring(0, colonIndex).trim()
        val timePart = segment.substring(colonIndex + 1).trim()
        val (startDay, endDay) = parseDayRange(daysPart) ?: return@mapNotNull null
        val (startTime, endTime) = parseTimeRange(timePart)
        ScheduleSegmentBO(startDay, endDay, startTime, endTime)
    }.getOrNull()
}

private val dayMap = mapOf(
    "L" to DayOfWeek.MONDAY,
    "M" to DayOfWeek.TUESDAY,
    "X" to DayOfWeek.WEDNESDAY,
    "J" to DayOfWeek.THURSDAY,
    "V" to DayOfWeek.FRIDAY,
    "S" to DayOfWeek.SATURDAY,
    "D" to DayOfWeek.SUNDAY,
)

private fun parseDayRange(daysPart: String): Pair<DayOfWeek, DayOfWeek>? {
    if (!daysPart.contains("-")) {
        val day = dayMap[daysPart] ?: return null
        return day to day
    }
    val dashIndex = daysPart.indexOf('-')
    val startCode = daysPart.substring(0, dashIndex).trim()
    val endCode = daysPart.substring(dashIndex + 1).trim()
    val start = dayMap[startCode] ?: return null
    val end = dayMap[endCode] ?: return null
    return start to end
}

private const val ALWAYS_OPEN = "24H"

private fun parseTimeRange(timePart: String): Pair<LocalTime?, LocalTime?> {
    if (timePart == ALWAYS_OPEN) return null to null
    val dashIndex = timePart.indexOf('-')
    if (dashIndex == -1) return null to null
    val start = runCatching { LocalTime.parse(timePart.substring(0, dashIndex).trim().padTimeHour()) }.getOrNull()
    val end = runCatching { LocalTime.parse(timePart.substring(dashIndex + 1).trim().padTimeHour()) }.getOrNull()
    return start to end
}

private fun String.padTimeHour(): String = if (indexOf(':') == 1) "0$this" else this
