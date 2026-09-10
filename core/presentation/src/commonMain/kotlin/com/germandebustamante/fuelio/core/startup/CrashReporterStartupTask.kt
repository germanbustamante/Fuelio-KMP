package com.germandebustamante.fuelio.core.startup

import com.germandebustamante.fuelio.core.analytics.crash.CrashReporter
import com.germandebustamante.fuelio.core.build.BuildEnvironment

/**
 * Configures crash reporting for this launch.
 *
 * The first real inhabitant of the [StartupTask] set: it has no screen owner and must run exactly
 * once per launch. `initKoin` runs each task inside its own `runCatching`, so a Crashlytics problem
 * can't take the launch down with it. (`initKoin` also installs the reporter into `CrashReporting`
 * synchronously beforehand — the tasks run concurrently, so that can't be left to this one.)
 *
 * Collection stays off in debug builds so local crash-loops don't pollute the dashboard.
 */
class CrashReporterStartupTask(private val crashReporter: CrashReporter, private val buildEnvironment: BuildEnvironment) : StartupTask {

    override suspend fun invoke() {
        crashReporter.setEnabled(!buildEnvironment.isDebug)
        // Reports arrive from two very different UI stacks against one shared Kotlin core, so the
        // platform is the first thing worth knowing when triaging.
        crashReporter.setCustomKey(CUSTOM_KEY_PLATFORM, buildEnvironment.platform)
    }

    private companion object {
        const val CUSTOM_KEY_PLATFORM = "platform"
    }
}
