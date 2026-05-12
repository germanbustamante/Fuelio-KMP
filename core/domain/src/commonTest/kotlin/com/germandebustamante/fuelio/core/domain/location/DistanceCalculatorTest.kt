package com.germandebustamante.fuelio.core.domain.location

import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertTrue

class DistanceCalculatorTest {

    @Test
    fun `distanceBetween - GIVEN Madrid and Barcelona WHEN calculated THEN result is approximately 504km`() {
        val result = distanceBetween(MADRID_LAT, MADRID_LON, BARCELONA_LAT, BARCELONA_LON)

        assertTrue(abs(result - MADRID_BARCELONA_KM) < DISTANCE_TOLERANCE_KM, "Expected ~${MADRID_BARCELONA_KM}km but was $result")
    }

    @Test
    fun `distanceBetween - GIVEN same coordinates WHEN calculated THEN returns zero`() {
        val result = distanceBetween(MADRID_LAT, MADRID_LON, MADRID_LAT, MADRID_LON)

        assertTrue(result < ZERO_TOLERANCE_KM, "Expected ~0 but was $result")
    }

    @Test
    fun `distanceBetween - GIVEN two nearby points WHEN calculated THEN returns small distance`() {
        val result = distanceBetween(MADRID_LAT, MADRID_LON, MADRID_1KM_NORTH_LAT, MADRID_LON)

        assertTrue(result < NEARBY_MAX_KM, "Expected under ${NEARBY_MAX_KM}km but was $result")
        assertTrue(result > NEARBY_MIN_KM, "Expected over ${NEARBY_MIN_KM}km but was $result")
    }

    @Test
    fun `distanceBetween - GIVEN symmetric points WHEN calculated THEN result is the same in both directions`() {
        val ab = distanceBetween(MADRID_LAT, MADRID_LON, BARCELONA_LAT, BARCELONA_LON)
        val ba = distanceBetween(BARCELONA_LAT, BARCELONA_LON, MADRID_LAT, MADRID_LON)

        assertTrue(abs(ab - ba) < ZERO_TOLERANCE_KM, "Expected symmetric results but got $ab vs $ba")
    }

    private companion object {
        // Madrid Puerta del Sol
        const val MADRID_LAT = 40.4168
        const val MADRID_LON = -3.7038
        // ~1km north of Puerta del Sol
        const val MADRID_1KM_NORTH_LAT = 40.4258

        // Barcelona Plaça Catalunya
        const val BARCELONA_LAT = 41.3874
        const val BARCELONA_LON = 2.1686

        const val MADRID_BARCELONA_KM = 504.0
        const val DISTANCE_TOLERANCE_KM = 5.0
        const val NEARBY_MIN_KM = 0.5
        const val NEARBY_MAX_KM = 1.5
        const val ZERO_TOLERANCE_KM = 0.001
    }
}
