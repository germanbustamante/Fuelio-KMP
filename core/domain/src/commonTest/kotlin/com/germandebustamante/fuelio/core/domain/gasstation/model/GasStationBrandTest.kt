package com.germandebustamante.fuelio.core.domain.gasstation.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class GasStationBrandTest {

    @Test
    fun `GIVEN station name contains a known brand as a whole word THEN matching brand is returned`() {
        assertEquals(GasStationBrand.REPSOL, GasStationBrand.fromName("REPSOL ES KM 5"))
        assertEquals(GasStationBrand.CEPSA, GasStationBrand.fromName("CEPSA"))
        assertEquals(GasStationBrand.SHELL, GasStationBrand.fromName("ESTACION SHELL CENTRO"))
    }

    @Test
    fun `GIVEN station name in lowercase THEN brand match is case insensitive`() {
        assertEquals(GasStationBrand.GALP, GasStationBrand.fromName("galp"))
    }

    @Test
    fun `GIVEN station name matches no known brand THEN null is returned`() {
        assertNull(GasStationBrand.fromName("ESTACION DESCONOCIDA"))
    }

    @Test
    fun `GIVEN station name is blank THEN null is returned`() {
        assertNull(GasStationBrand.fromName(""))
    }

    @Test
    fun `GIVEN short brand alias only appears as a substring of another word THEN it is not matched`() {
        assertNull(GasStationBrand.fromName("CAMPBP EXPRESS"))
        assertNull(GasStationBrand.fromName("Q80 STATION"))
    }

    @Test
    fun `GIVEN short brand alias appears as its own word THEN it is matched`() {
        assertEquals(GasStationBrand.BP, GasStationBrand.fromName("E.S. BP CENTRO"))
        assertEquals(GasStationBrand.Q8, GasStationBrand.fromName("Q8 AUTOSERVICIO"))
    }
}
