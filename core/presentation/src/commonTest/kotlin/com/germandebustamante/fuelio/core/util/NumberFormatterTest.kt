package com.germandebustamante.fuelio.core.util

import kotlin.test.Test
import kotlin.test.assertTrue

class NumberFormatterTest {

    // format() — expect/actual, runs with each platform's actual implementation

    @Test
    fun `GIVEN value WHEN format with 3 digits THEN last 3 chars are digits`() {
        assertTrue(1.759.format(3).takeLast(3).all { it.isDigit() })
    }

    @Test
    fun `GIVEN value WHEN format with 1 digit THEN last char is digit`() {
        assertTrue(12.5.format(1).takeLast(1).all { it.isDigit() })
    }

    @Test
    fun `GIVEN zero WHEN format with 3 digits THEN last 3 chars are digits`() {
        assertTrue(0.0.format(3).takeLast(3).all { it.isDigit() })
    }

    @Test
    fun `GIVEN negative value WHEN format THEN result is not empty`() {
        assertTrue((-1.5).format(2).isNotEmpty())
    }

    @Test
    fun `GIVEN negative value WHEN format THEN last 2 chars are digits`() {
        assertTrue((-1.5).format(2).takeLast(2).all { it.isDigit() })
    }

    // formatAsEuros / formatAsKilometers — common extensions

    @Test
    fun `GIVEN price WHEN formatAsEuros THEN result ends with euros suffix`() {
        assertTrue(1.759.formatAsEuros().endsWith(" €/L"))
    }

    @Test
    fun `GIVEN price WHEN formatAsEuros THEN number part has 3 decimal digits`() {
        assertTrue(1.759.formatAsEuros().removeSuffix(" €/L").takeLast(3).all { it.isDigit() })
    }

    @Test
    fun `GIVEN distance WHEN formatAsKilometers THEN result ends with km suffix`() {
        assertTrue(12.5.formatAsKilometers().endsWith(" km"))
    }

    @Test
    fun `GIVEN distance WHEN formatAsKilometers THEN number part has 1 decimal digit`() {
        assertTrue(12.5.formatAsKilometers().removeSuffix(" km").takeLast(1).all { it.isDigit() })
    }
}
