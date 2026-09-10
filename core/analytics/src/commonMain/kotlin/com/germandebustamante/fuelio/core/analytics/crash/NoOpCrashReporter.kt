package com.germandebustamante.fuelio.core.analytics.crash

/**
 * Does nothing, successfully.
 *
 * This is the iOS fallback when no native bridge has been registered — SwiftUI previews and the unit
 * test bundle both skip `FirebaseApp.configure()`, and a hard `requireNotNull` there would turn a
 * missing optional dependency into a crash. It is also what the UI-test Koin overrides bind, so a
 * deterministic test run never talks to Crashlytics.
 */
object NoOpCrashReporter : CrashReporter {
    override fun setEnabled(enabled: Boolean) = Unit

    override fun log(message: String) = Unit

    override fun recordException(throwable: Throwable, tag: String?) = Unit

    override fun setCustomKey(key: String, value: String) = Unit
}
