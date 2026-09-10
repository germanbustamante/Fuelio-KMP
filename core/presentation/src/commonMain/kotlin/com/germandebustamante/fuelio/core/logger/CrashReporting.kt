package com.germandebustamante.fuelio.core.logger

import com.germandebustamante.fuelio.core.analytics.crash.CrashReporter
import com.germandebustamante.fuelio.core.analytics.crash.NoOpCrashReporter

/**
 * Logs an error locally **and** reports it as a non-fatal.
 *
 * This is deliberately a separate facade rather than a change to [AppLogger]. `AppLogger` is an
 * `expect object` with no constructor and no dependencies — that is what lets any source set call it
 * without wiring — so giving it a [CrashReporter] would mean a hidden global inside the logging
 * primitive itself. Keeping the two apart means call sites choose: [AppLogger] for developer noise,
 * [CrashReporting] for something a user actually hit.
 *
 * [install] is called by `CrashReporterStartupTask`; until then, and in any test that never starts
 * Koin, the reporter is [NoOpCrashReporter] and this degrades to plain logging.
 */
object CrashReporting {

    private var reporter: CrashReporter = NoOpCrashReporter

    fun install(reporter: CrashReporter) {
        this.reporter = reporter
    }

    fun logError(tag: String, message: String, throwable: Throwable? = null) {
        AppLogger.e(tag, message, throwable)
        reporter.log("[$tag] $message")
        throwable?.let { reporter.recordException(it, tag) }
    }

    /** Test seam: resets the installed reporter so one test can't leak into the next. */
    fun reset() {
        reporter = NoOpCrashReporter
    }
}
