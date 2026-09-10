package com.germandebustamante.fuelio.core.analytics.crash

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class IosCrashReporterTest {

    @Test
    fun `getCrashReporter - GIVEN no native reporter registered THEN it falls back to a no-op`() {
        // Previews and the test bundle both skip FirebaseApp.configure(), so an unregistered bridge
        // has to be a supported state rather than a crash.
        assertEquals(NoOpCrashReporter, getCrashReporter())
    }

    @Test
    fun `recordException - GIVEN a throwable THEN name reason and stack trace are flattened for Swift`() {
        val native = RecordingNativeCrashReporter()
        val sut = IosCrashReporter(native)

        sut.recordException(IllegalStateException("boom"), tag = "StartupTask")

        val recorded = assertNotNull(native.recorded)
        assertEquals("IllegalStateException", recorded.name)
        assertEquals("boom", recorded.reason)
        assertTrue(recorded.stackTrace.isNotEmpty(), "an empty stack trace would collapse every non-fatal into one issue")
        assertEquals(listOf("StartupTask"), native.logs)
    }

    @Test
    fun `recordException - GIVEN a throwable with no message THEN the reason falls back to toString`() {
        val native = RecordingNativeCrashReporter()

        IosCrashReporter(native).recordException(IllegalStateException())

        assertTrue(assertNotNull(native.recorded).reason.contains("IllegalStateException"))
    }

    private class RecordingNativeCrashReporter : NativeCrashReporter {
        data class Recorded(val name: String, val reason: String, val stackTrace: List<String>)

        val logs = mutableListOf<String>()
        var recorded: Recorded? = null

        override fun setEnabled(enabled: Boolean) = Unit

        override fun log(message: String) {
            logs += message
        }

        override fun record(name: String, reason: String, stackTrace: List<String>) {
            recorded = Recorded(name, reason, stackTrace)
        }

        override fun setCustomKey(key: String, value: String) = Unit
    }
}
