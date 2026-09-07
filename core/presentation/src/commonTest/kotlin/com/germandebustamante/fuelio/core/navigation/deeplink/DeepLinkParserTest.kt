package com.germandebustamante.fuelio.core.navigation.deeplink

import com.germandebustamante.fuelio.core.navigation.destination.Destination
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DeepLinkParserTest {

    private val gasStationId = "station-123"
    private val validUri = "${DeepLinkRoutes.SCHEME_FUELIO}://${DeepLinkRoutes.RESOURCE_STATION}/" +
        "$gasStationId/${DeepLinkRoutes.ACTION_DETAIL}"

    @Test
    fun `parseDeepLink - GIVEN valid station detail uri THEN returns GasStationDetails`() {
        val result = parseDeepLink(validUri)

        assertEquals(Destination.GasStationDetails(gasStationId), result)
    }

    @Test
    fun `parseDeepLink - GIVEN unknown scheme THEN returns null`() {
        assertNull(parseDeepLink("https://${DeepLinkRoutes.RESOURCE_STATION}/$gasStationId/${DeepLinkRoutes.ACTION_DETAIL}"))
    }

    @Test
    fun `parseDeepLink - GIVEN unknown resource THEN returns null`() {
        assertNull(parseDeepLink("${DeepLinkRoutes.SCHEME_FUELIO}://unknown/$gasStationId/${DeepLinkRoutes.ACTION_DETAIL}"))
    }

    @Test
    fun `parseDeepLink - GIVEN unknown action THEN returns null`() {
        assertNull(parseDeepLink("${DeepLinkRoutes.SCHEME_FUELIO}://${DeepLinkRoutes.RESOURCE_STATION}/$gasStationId/map"))
    }

    @Test
    fun `parseDeepLink - GIVEN missing action segment THEN returns null`() {
        assertNull(parseDeepLink("${DeepLinkRoutes.SCHEME_FUELIO}://${DeepLinkRoutes.RESOURCE_STATION}/$gasStationId"))
    }

    @Test
    fun `parseDeepLink - GIVEN extra segments THEN returns null`() {
        assertNull(parseDeepLink("$validUri/extra"))
    }

    @Test
    fun `parseDeepLink - GIVEN malformed uri THEN returns null`() {
        assertNull(parseDeepLink("not-a-uri"))
    }
}
