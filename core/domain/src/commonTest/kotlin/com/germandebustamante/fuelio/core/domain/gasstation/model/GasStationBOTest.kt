package com.germandebustamante.fuelio.core.domain.gasstation.model

import com.germandebustamante.fuelio.core.domain.gasstation.testing.GasStationBOMother
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GasStationBOTest {

    //region 24H all week — "L-D: 24H"

    @Test
    fun `isOpen - GIVEN L-D 24H schedule WHEN any day and time THEN returns true`() {
        val station = GasStationBOMother.gasStationBO(
            schedule = listOf(ScheduleSegmentBO(DayOfWeek.MONDAY, DayOfWeek.SUNDAY, null, null))
        )

        assertTrue(station.isOpen(monday(10, 0)))
        assertTrue(station.isOpen(saturday(3, 0)))
        assertTrue(station.isOpen(sunday(23, 59)))
    }

    //endregion

    //region Fixed daily range — "L-D: 07:00-22:00"

    @Test
    fun `isOpen - GIVEN L-D 07-22 schedule WHEN time is within range THEN returns true`() {
        val station = stationWithSchedule("L-D: 07:00-22:00")

        assertTrue(station.isOpen(monday(7, 0)))
        assertTrue(station.isOpen(monday(14, 30)))
        assertTrue(station.isOpen(monday(22, 0)))
    }

    @Test
    fun `isOpen - GIVEN L-D 07-22 schedule WHEN time is outside range THEN returns false`() {
        val station = stationWithSchedule("L-D: 07:00-22:00")

        assertFalse(station.isOpen(monday(6, 59)))
        assertFalse(station.isOpen(monday(22, 1)))
        assertFalse(station.isOpen(monday(3, 0)))
    }

    //endregion

    //region Midnight-crossing range — "L-D: 06:00-00:00"

    @Test
    fun `isOpen - GIVEN L-D 06-00 schedule WHEN time is after opening THEN returns true`() {
        val station = stationWithSchedule("L-D: 06:00-00:00")

        assertTrue(station.isOpen(monday(6, 0)))
        assertTrue(station.isOpen(monday(23, 59)))
        assertTrue(station.isOpen(monday(0, 0)))
    }

    @Test
    fun `isOpen - GIVEN L-D 06-00 schedule WHEN time is in early morning THEN returns false`() {
        val station = stationWithSchedule("L-D: 06:00-00:00")

        assertFalse(station.isOpen(monday(0, 1)))
        assertFalse(station.isOpen(monday(5, 59)))
    }

    //endregion

    //region Overnight range — "L-D: 08:00-01:00"

    @Test
    fun `isOpen - GIVEN L-D 08-01 schedule WHEN time is after midnight within range THEN returns true`() {
        val station = stationWithSchedule("L-D: 08:00-01:00")

        assertTrue(station.isOpen(monday(8, 0)))
        assertTrue(station.isOpen(monday(23, 0)))
        assertTrue(station.isOpen(monday(0, 30)))
        assertTrue(station.isOpen(monday(1, 0)))
    }

    @Test
    fun `isOpen - GIVEN L-D 08-01 schedule WHEN time is between 01 and 08 THEN returns false`() {
        val station = stationWithSchedule("L-D: 08:00-01:00")

        assertFalse(station.isOpen(monday(1, 1)))
        assertFalse(station.isOpen(monday(7, 59)))
    }

    //endregion

    //region Multi-segment — "L-J: 07:00-23:00; V-D: 24H"

    @Test
    fun `isOpen - GIVEN L-J day range WHEN day is within range and time matches THEN returns true`() {
        val station = stationWithSchedule("L-J: 07:00-23:00; V-D: 24H")

        assertTrue(station.isOpen(monday(10, 0)))
        assertTrue(station.isOpen(thursday(22, 0)))
    }

    @Test
    fun `isOpen - GIVEN L-J day range WHEN day is within range but time outside THEN returns false`() {
        val station = stationWithSchedule("L-J: 07:00-23:00; V-D: 24H")

        assertFalse(station.isOpen(monday(6, 0)))
    }

    @Test
    fun `isOpen - GIVEN V-D 24H segment WHEN weekend THEN returns true regardless of time`() {
        val station = stationWithSchedule("L-J: 07:00-23:00; V-D: 24H")

        assertTrue(station.isOpen(friday(3, 0)))
        assertTrue(station.isOpen(saturday(0, 0)))
        assertTrue(station.isOpen(sunday(23, 59)))
    }

    //endregion

    //region Complex 4-segment — "L-J: 06:00-22:00; V: 06:00-00:00; S: 24H; D: 00:00-22:00"

    @Test
    fun `isOpen - GIVEN 4-segment schedule WHEN friday at midnight THEN returns true`() {
        val station = stationWithSchedule("L-J: 06:00-22:00; V: 06:00-00:00; S: 24H; D: 00:00-22:00")

        assertTrue(station.isOpen(friday(23, 59)))
        assertTrue(station.isOpen(friday(0, 0)))
    }

    @Test
    fun `isOpen - GIVEN 4-segment schedule WHEN sunday after 22h THEN returns false`() {
        val station = stationWithSchedule("L-J: 06:00-22:00; V: 06:00-00:00; S: 24H; D: 00:00-22:00")

        assertFalse(station.isOpen(sunday(22, 1)))
    }

    //endregion

    //region Empty/malformed schedule

    @Test
    fun `isOpen - GIVEN empty schedule WHEN any time THEN returns false`() {
        val station = GasStationBOMother.gasStationBO(schedule = emptyList())

        assertFalse(station.isOpen(monday(10, 0)))
    }

    //endregion

    private fun stationWithSchedule(raw: String): GasStationBO {
        val segments = raw.split(";").mapNotNull { segment ->
            val colonIndex = segment.indexOf(':')
            if (colonIndex == -1) return@mapNotNull null
            val daysPart = segment.substring(0, colonIndex).trim()
            val timePart = segment.substring(colonIndex + 1).trim()
            val (startDay, endDay) = parseDayRange(daysPart) ?: return@mapNotNull null
            val (startTime, endTime) = parseTimeRange(timePart)
            ScheduleSegmentBO(startDay, endDay, startTime, endTime)
        }
        return GasStationBOMother.gasStationBO(schedule = segments)
    }

    private val dayMap = mapOf(
        "L" to DayOfWeek.MONDAY, "M" to DayOfWeek.TUESDAY, "X" to DayOfWeek.WEDNESDAY,
        "J" to DayOfWeek.THURSDAY, "V" to DayOfWeek.FRIDAY, "S" to DayOfWeek.SATURDAY, "D" to DayOfWeek.SUNDAY,
    )

    private fun parseDayRange(daysPart: String): Pair<DayOfWeek, DayOfWeek>? {
        if (!daysPart.contains("-")) {
            val day = dayMap[daysPart] ?: return null
            return day to day
        }
        val dash = daysPart.indexOf('-')
        val start = dayMap[daysPart.substring(0, dash).trim()] ?: return null
        val end = dayMap[daysPart.substring(dash + 1).trim()] ?: return null
        return start to end
    }

    private fun parseTimeRange(timePart: String): Pair<LocalTime?, LocalTime?> {
        if (timePart == ALWAYS_OPEN) return null to null
        val dash = timePart.indexOf('-')
        if (dash == -1) return null to null
        val start = runCatching { LocalTime.parse(timePart.substring(0, dash).trim()) }.getOrNull()
        val end = runCatching { LocalTime.parse(timePart.substring(dash + 1).trim()) }.getOrNull()
        return start to end
    }

    private fun monday(h: Int, m: Int) = LocalDateTime(YEAR, MONTH, MONDAY_DAY, h, m)
    private fun thursday(h: Int, m: Int) = LocalDateTime(YEAR, MONTH, THURSDAY_DAY, h, m)
    private fun friday(h: Int, m: Int) = LocalDateTime(YEAR, MONTH, FRIDAY_DAY, h, m)
    private fun saturday(h: Int, m: Int) = LocalDateTime(YEAR, MONTH, SATURDAY_DAY, h, m)
    private fun sunday(h: Int, m: Int) = LocalDateTime(YEAR, MONTH, SUNDAY_DAY, h, m)

    private companion object {
        const val ALWAYS_OPEN = "24H"

        // Week of 2026-05-11 (Monday) used as fixed reference for all tests
        const val YEAR = 2026
        const val MONTH = 5
        const val MONDAY_DAY = 11
        const val THURSDAY_DAY = 14
        const val FRIDAY_DAY = 15
        const val SATURDAY_DAY = 16
        const val SUNDAY_DAY = 17
    }
}
