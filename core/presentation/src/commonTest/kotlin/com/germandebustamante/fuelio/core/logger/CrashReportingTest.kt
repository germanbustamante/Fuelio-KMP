package com.germandebustamante.fuelio.core.logger

import com.germandebustamante.fuelio.core.analytics.crash.CrashReporter
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CrashReportingTest {

    @AfterTest
    fun tearDown() {
        CrashReporting.reset()
    }

    @Test
    fun `logError - GIVEN a throwable WHEN logged THEN it is recorded as a non-fatal with its tag`() {
        val reporter = RecordingCrashReporter()
        CrashReporting.install(reporter)
        val cause = IllegalStateException("boom")

        CrashReporting.logError("StartupTask", "Startup task failed", cause)

        assertEquals(listOf<Pair<Throwable, String?>>(cause to "StartupTask"), reporter.recorded)
    }

    @Test
    fun `logError - GIVEN no throwable WHEN logged THEN only a breadcrumb is left`() {
        val reporter = RecordingCrashReporter()
        CrashReporting.install(reporter)

        CrashReporting.logError("ANRWatchDog", "ANR detected")

        assertTrue(reporter.recorded.isEmpty())
        assertEquals(listOf("[ANRWatchDog] ANR detected"), reporter.logs)
    }

    @Test
    fun `logError - GIVEN no reporter installed WHEN logged THEN it degrades to plain logging`() {
        CrashReporting.reset()

        // The assertion is that this does not throw: every test and SwiftUI preview runs without a
        // reporter installed, and logging must stay safe there.
        CrashReporting.logError("Anything", "still fine", IllegalStateException("boom"))
    }

    private class RecordingCrashReporter : CrashReporter {
        val logs = mutableListOf<String>()
        val recorded = mutableListOf<Pair<Throwable, String?>>()

        override fun setEnabled(enabled: Boolean) = Unit

        override fun log(message: String) {
            logs += message
        }

        override fun recordException(throwable: Throwable, tag: String?) {
            recorded += throwable to tag
        }

        override fun setCustomKey(key: String, value: String) = Unit
    }
}
