package com.germandebustamante.fuelio.core.navigation.destination

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SyntheticBackStackTest {

    @Test
    fun `buildSyntheticBackStack - GIVEN a deep link target with a parent THEN returns parent then target`() {
        val target = Destination.GasStationDetails(gasStationId = "station-1")

        val result = buildSyntheticBackStack(target)

        assertEquals(listOf(Destination.GasStations, target), result)
    }

    @Test
    fun `buildSyntheticBackStack - GIVEN the root destination THEN returns only the root`() {
        val result = buildSyntheticBackStack(Destination.GasStations)

        assertEquals(listOf(Destination.GasStations), result)
    }

    @Test
    fun `parent - GIVEN the root destination THEN is null`() {
        assertNull(Destination.GasStations.parent)
    }

    @Test
    fun `parent - GIVEN a detail destination THEN is the list`() {
        assertEquals(Destination.GasStations, Destination.GasStationDetails("station-1").parent)
    }
}
