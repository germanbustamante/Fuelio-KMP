package com.germandebustamante.fuelio.core.analytics.crash

/**
 * Crash and non-fatal reporting, the observability sibling of [com.germandebustamante.fuelio.core.analytics.AnalyticsTracking].
 *
 * Kept separate from the `Trackable`/`Tracker` hierarchy on purpose: analytics answers "what did the
 * user do", crash reporting answers "what went wrong", and only the latter needs to survive a
 * process that is about to die. Consumers depend on this interface, never on a concrete
 * implementation, so it can be faked in tests the same way `AnalyticsTracking` is.
 */
interface CrashReporter {

    /** Turns collection on or off. Called once at startup; off in debug builds. */
    fun setEnabled(enabled: Boolean)

    /** Breadcrumb attached to whatever report comes next. */
    fun log(message: String)

    /** Reports a handled error as a non-fatal, keeping the app running. */
    fun recordException(throwable: Throwable, tag: String? = null)

    /** Attaches a key/value that shows up on every subsequent report. */
    fun setCustomKey(key: String, value: String)
}

/**
 * The platform's reporter.
 *
 * An `expect fun` returning an interface rather than an `expect object`, matching
 * [com.germandebustamante.fuelio.core.analytics.impl.getFirebaseTracker]: an object could not be
 * substituted in tests, and iOS needs to resolve a bridge registered from Swift at runtime.
 */
expect fun getCrashReporter(): CrashReporter
