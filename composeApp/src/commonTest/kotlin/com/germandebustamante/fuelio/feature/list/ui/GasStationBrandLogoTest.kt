package com.germandebustamante.fuelio.feature.list.ui

import fuelio.composeapp.generated.resources.Res
import fuelio.composeapp.generated.resources.logo_cepsa
import fuelio.composeapp.generated.resources.logo_galp
import fuelio.composeapp.generated.resources.logo_repsol
import fuelio.composeapp.generated.resources.logo_shell
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class GasStationBrandLogoTest {

    @Test
    fun `GIVEN station name contains a known brand THEN matching logo is returned`() {
        assertEquals(Res.drawable.logo_repsol, gasStationBrandLogo("REPSOL ES KM 5"))
        assertEquals(Res.drawable.logo_cepsa, gasStationBrandLogo("CEPSA"))
        assertEquals(Res.drawable.logo_shell, gasStationBrandLogo("ESTACION SHELL CENTRO"))
    }

    @Test
    fun `GIVEN station name in lowercase THEN brand match is case insensitive`() {
        assertEquals(Res.drawable.logo_galp, gasStationBrandLogo("galp"))
    }

    @Test
    fun `GIVEN station name matches no known brand THEN null is returned`() {
        assertNull(gasStationBrandLogo("ESTACION DESCONOCIDA"))
    }

    @Test
    fun `GIVEN station name is blank THEN null is returned`() {
        assertNull(gasStationBrandLogo(""))
    }
}
