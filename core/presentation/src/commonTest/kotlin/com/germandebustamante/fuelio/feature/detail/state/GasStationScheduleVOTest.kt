package com.germandebustamante.fuelio.feature.detail.state

import com.germandebustamante.fuelio.core.domain.gasstation.model.ScheduleSegmentBO
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GasStationScheduleVOTest {

    @Test
    fun `toScheduleDays - GIVEN always open schedule WHEN mapped THEN every day is AlwaysOpen`() {
        val schedule = listOf(ScheduleSegmentBO(DayOfWeek.MONDAY, DayOfWeek.SUNDAY, null, null))

        val days = schedule.toScheduleDays(today = DayOfWeek.WEDNESDAY)

        assertTrue(days.all { it.status is ScheduleDayStatus.AlwaysOpen })
    }

    @Test
    fun `toScheduleDays - GIVEN Monday-to-Friday schedule WHEN mapped THEN weekend days are Closed`() {
        val schedule = listOf(ScheduleSegmentBO(DayOfWeek.MONDAY, DayOfWeek.FRIDAY, LocalTime(7, 0), LocalTime(22, 0)))

        val days = schedule.toScheduleDays(today = DayOfWeek.MONDAY)

        val saturday = days.first { it.dayOfWeek == DayOfWeek.SATURDAY }
        val sunday = days.first { it.dayOfWeek == DayOfWeek.SUNDAY }
        assertEquals(ScheduleDayStatus.Closed, saturday.status)
        assertEquals(ScheduleDayStatus.Closed, sunday.status)
    }

    @Test
    fun `toScheduleDays - GIVEN schedule with time range WHEN mapped THEN covered day has formatted Hours`() {
        val schedule = listOf(ScheduleSegmentBO(DayOfWeek.MONDAY, DayOfWeek.FRIDAY, LocalTime(7, 5), LocalTime(22, 0)))

        val days = schedule.toScheduleDays(today = DayOfWeek.MONDAY)

        val monday = days.first { it.dayOfWeek == DayOfWeek.MONDAY }
        assertEquals(ScheduleDayStatus.Hours(start = "07:05", end = "22:00"), monday.status)
    }

    @Test
    fun `toScheduleDays - GIVEN Saturday-to-Monday wraparound schedule WHEN mapped THEN Sunday is covered`() {
        val schedule = listOf(ScheduleSegmentBO(DayOfWeek.SATURDAY, DayOfWeek.MONDAY, null, null))

        val days = schedule.toScheduleDays(today = DayOfWeek.SUNDAY)

        val sunday = days.first { it.dayOfWeek == DayOfWeek.SUNDAY }
        assertEquals(ScheduleDayStatus.AlwaysOpen, sunday.status)
    }

    @Test
    fun `toScheduleDays - GIVEN any schedule WHEN mapped THEN only today is flagged isToday`() {
        val schedule = listOf(ScheduleSegmentBO(DayOfWeek.MONDAY, DayOfWeek.SUNDAY, null, null))

        val days = schedule.toScheduleDays(today = DayOfWeek.THURSDAY)

        assertTrue(days.single { it.isToday }.dayOfWeek == DayOfWeek.THURSDAY)
        assertFalse(days.filter { it.dayOfWeek != DayOfWeek.THURSDAY }.any { it.isToday })
    }

    @Test
    fun `toScheduleDays - GIVEN any schedule WHEN mapped THEN days are returned Monday through Sunday in order`() {
        val schedule = listOf(ScheduleSegmentBO(DayOfWeek.MONDAY, DayOfWeek.SUNDAY, null, null))

        val days = schedule.toScheduleDays(today = DayOfWeek.MONDAY)

        assertEquals(
            listOf(
                DayOfWeek.MONDAY,
                DayOfWeek.TUESDAY,
                DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY,
                DayOfWeek.FRIDAY,
                DayOfWeek.SATURDAY,
                DayOfWeek.SUNDAY,
            ),
            days.map { it.dayOfWeek },
        )
    }

    @Test
    fun `toScheduleDays - GIVEN empty schedule WHEN mapped THEN every day is Closed`() {
        val days = emptyList<ScheduleSegmentBO>().toScheduleDays(today = DayOfWeek.MONDAY)

        assertTrue(days.all { it.status is ScheduleDayStatus.Closed })
    }
}
