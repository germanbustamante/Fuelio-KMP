package com.germandebustamante.fuelio.core.util

import kotlinx.datetime.LocalTime
import kotlin.test.Test
import kotlin.test.assertEquals

class TimeFormatterTest {

    @Test
    fun `GIVEN hour and minute with two digits WHEN asHhMm THEN formats as HH-mm`() {
        assertEquals("14:35", LocalTime(14, 35).asHhMm())
    }

    @Test
    fun `GIVEN hour and minute below ten WHEN asHhMm THEN pads with leading zero`() {
        assertEquals("07:05", LocalTime(7, 5).asHhMm())
    }

    @Test
    fun `GIVEN midnight WHEN asHhMm THEN formats as 00-00`() {
        assertEquals("00:00", LocalTime(0, 0).asHhMm())
    }
}
