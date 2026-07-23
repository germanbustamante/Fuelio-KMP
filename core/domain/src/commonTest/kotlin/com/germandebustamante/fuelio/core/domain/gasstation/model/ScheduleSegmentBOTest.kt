package com.germandebustamante.fuelio.core.domain.gasstation.model

import kotlinx.datetime.DayOfWeek
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ScheduleSegmentBOTest {

    @Test
    fun `coversDay - GIVEN single day segment WHEN day matches THEN returns true`() {
        val segment = ScheduleSegmentBO(DayOfWeek.WEDNESDAY, DayOfWeek.WEDNESDAY, null, null)

        assertTrue(segment.coversDay(DayOfWeek.WEDNESDAY))
    }

    @Test
    fun `coversDay - GIVEN single day segment WHEN day differs THEN returns false`() {
        val segment = ScheduleSegmentBO(DayOfWeek.WEDNESDAY, DayOfWeek.WEDNESDAY, null, null)

        assertFalse(segment.coversDay(DayOfWeek.THURSDAY))
    }

    @Test
    fun `coversDay - GIVEN Monday-to-Friday range WHEN day is within range THEN returns true`() {
        val segment = ScheduleSegmentBO(DayOfWeek.MONDAY, DayOfWeek.FRIDAY, null, null)

        assertTrue(segment.coversDay(DayOfWeek.MONDAY))
        assertTrue(segment.coversDay(DayOfWeek.WEDNESDAY))
        assertTrue(segment.coversDay(DayOfWeek.FRIDAY))
    }

    @Test
    fun `coversDay - GIVEN Monday-to-Friday range WHEN day is outside range THEN returns false`() {
        val segment = ScheduleSegmentBO(DayOfWeek.MONDAY, DayOfWeek.FRIDAY, null, null)

        assertFalse(segment.coversDay(DayOfWeek.SATURDAY))
        assertFalse(segment.coversDay(DayOfWeek.SUNDAY))
    }

    @Test
    fun `coversDay - GIVEN Saturday-to-Monday wraparound range WHEN day is within range THEN returns true`() {
        val segment = ScheduleSegmentBO(DayOfWeek.SATURDAY, DayOfWeek.MONDAY, null, null)

        assertTrue(segment.coversDay(DayOfWeek.SATURDAY))
        assertTrue(segment.coversDay(DayOfWeek.SUNDAY))
        assertTrue(segment.coversDay(DayOfWeek.MONDAY))
    }

    @Test
    fun `coversDay - GIVEN Saturday-to-Monday wraparound range WHEN day is outside range THEN returns false`() {
        val segment = ScheduleSegmentBO(DayOfWeek.SATURDAY, DayOfWeek.MONDAY, null, null)

        assertFalse(segment.coversDay(DayOfWeek.TUESDAY))
        assertFalse(segment.coversDay(DayOfWeek.FRIDAY))
    }

    @Test
    fun `coversDay - GIVEN Monday-to-Sunday full week range WHEN any day THEN returns true`() {
        val segment = ScheduleSegmentBO(DayOfWeek.MONDAY, DayOfWeek.SUNDAY, null, null)

        DayOfWeek.entries.forEach { day -> assertTrue(segment.coversDay(day)) }
    }
}
