package com.germandebustamante.fuelio.core.analytics

import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify
import dev.mokkery.verify.VerifyMode.Companion.not
import kotlin.test.Test

class AnalyticsManagerTest {

    private val firebaseTracker: Trackable = mock {
        every { type } returns AnalyticsProviderType.FIREBASE
        every { track(any()) } returns Unit
    }

    private val sut = AnalyticsManager(listOf(firebaseTracker))

    @Test
    fun `track - GIVEN a trace targeting a registered tracker THEN it is forwarded to that tracker`() {
        val trace = Trace.Event("purchase", targets = listOf(AnalyticsProviderType.FIREBASE))

        sut.track(trace)

        verify { firebaseTracker.track(trace) }
    }

    @Test
    fun `track - GIVEN a trace with no matching targets THEN no tracker is called`() {
        val trace = Trace.Event("purchase", targets = emptyList())

        sut.track(trace)

        verify(mode = not) { firebaseTracker.track(any()) }
    }
}
