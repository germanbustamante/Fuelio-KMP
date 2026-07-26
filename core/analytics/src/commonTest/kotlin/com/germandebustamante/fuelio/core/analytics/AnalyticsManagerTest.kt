package com.germandebustamante.fuelio.core.analytics

import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode.Companion.not
import dev.mokkery.verifySuspend
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class AnalyticsManagerTest {

    private val firebaseTracker: Trackable = mock {
        every { type } returns AnalyticsProviderType.FIREBASE
        everySuspend { track(any()) } returns Unit
    }

    private val sut = AnalyticsManager(listOf(firebaseTracker))

    @Test
    fun `track - GIVEN a trace targeting a registered tracker THEN it is forwarded to that tracker`() = runTest {
        val trace = Trace.Event("purchase", targets = listOf(AnalyticsProviderType.FIREBASE))

        sut.track(trace)

        verifySuspend { firebaseTracker.track(trace) }
    }

    @Test
    fun `track - GIVEN a trace with no matching targets THEN no tracker is called`() = runTest {
        val trace = Trace.Event("purchase", targets = emptyList())

        sut.track(trace)

        verifySuspend(mode = not) { firebaseTracker.track(any()) }
    }

    @Test
    fun `constructor - GIVEN a null tracker filtered out via listOfNotNull THEN it never receives traces`() = runTest {
        val postHogTracker: Trackable? = null
        val managerWithoutPostHog = AnalyticsManager(listOfNotNull(firebaseTracker, postHogTracker))
        val trace = Trace.Event("purchase", targets = listOf(AnalyticsProviderType.FIREBASE, AnalyticsProviderType.POSTHOG))

        managerWithoutPostHog.track(trace)

        verifySuspend { firebaseTracker.track(trace) }
    }
}
