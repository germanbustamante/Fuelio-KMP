package com.germandebustamante.fuelio.core.startup

import com.germandebustamante.fuelio.core.analytics.crash.CrashReporter
import com.germandebustamante.fuelio.core.build.BuildEnvironment
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CrashReporterStartupTaskTest {

    @Test
    fun `invoke - GIVEN a release build WHEN run THEN collection is enabled`() = runTest {
        val reporter = RecordingCrashReporter()

        createSut(reporter, isDebug = false)()

        assertEquals(listOf(true), reporter.enabledCalls)
    }

    @Test
    fun `invoke - GIVEN a debug build WHEN run THEN collection is disabled`() = runTest {
        val reporter = RecordingCrashReporter()

        createSut(reporter, isDebug = true)()

        assertEquals(listOf(false), reporter.enabledCalls)
    }

    @Test
    fun `invoke - GIVEN any build WHEN run THEN the platform is attached as a custom key`() = runTest {
        val reporter = RecordingCrashReporter()

        createSut(reporter, platform = BuildEnvironment.PLATFORM_IOS)()

        assertEquals(BuildEnvironment.PLATFORM_IOS, reporter.customKeys["platform"])
    }

    @Test
    fun `invoke - GIVEN a reporter that throws WHEN run THEN the failure propagates for initKoin to catch`() = runTest {
        val sut = CrashReporterStartupTask(ThrowingCrashReporter, BuildEnvironment(isDebug = false, platform = "android"))

        val result = runCatching { sut() }

        assertTrue(result.isFailure, "the task must not swallow failures itself — initKoin owns that")
    }

    private fun createSut(
        reporter: CrashReporter,
        isDebug: Boolean = false,
        platform: String = BuildEnvironment.PLATFORM_ANDROID,
    ) = CrashReporterStartupTask(reporter, BuildEnvironment(isDebug = isDebug, platform = platform))

    private class RecordingCrashReporter : CrashReporter {
        val enabledCalls = mutableListOf<Boolean>()
        val customKeys = mutableMapOf<String, String>()

        override fun setEnabled(enabled: Boolean) {
            enabledCalls += enabled
        }

        override fun log(message: String) = Unit

        override fun recordException(throwable: Throwable, tag: String?) = Unit

        override fun setCustomKey(key: String, value: String) {
            customKeys[key] = value
        }
    }

    private object ThrowingCrashReporter : CrashReporter {
        override fun setEnabled(enabled: Boolean) = throw IllegalStateException("Crashlytics unavailable")

        override fun log(message: String) = Unit

        override fun recordException(throwable: Throwable, tag: String?) = Unit

        override fun setCustomKey(key: String, value: String) = Unit
    }
}
