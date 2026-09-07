package com.germandebustamante.fuelio.core.navigation.deeplink

import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ExternalUriHandlerTest {

    @AfterTest
    fun tearDown() {
        ExternalUriHandler.listener = null
    }

    @Test
    fun `onNewUri - GIVEN listener already registered THEN it receives the uri immediately`() {
        var received: String? = null
        ExternalUriHandler.listener = { received = it }

        ExternalUriHandler.onNewUri("fuelio://station/abc")

        assertEquals("fuelio://station/abc", received)
    }

    @Test
    fun `onNewUri - GIVEN uri arrives before any listener THEN late-registered listener receives cached uri`() {
        ExternalUriHandler.onNewUri("fuelio://station/abc")

        var received: String? = null
        ExternalUriHandler.listener = { received = it }

        assertEquals("fuelio://station/abc", received)
    }

    @Test
    fun `listener - GIVEN no pending uri WHEN listener set THEN it is not invoked`() {
        var callCount = 0
        ExternalUriHandler.listener = { callCount++ }

        assertEquals(0, callCount)
    }
}
