package com.germandebustamante.fuelio.core.analytics.crash

import kotlin.experimental.ExperimentalNativeApi

/**
 * Same native-bridge shape as `FirebaseTracker.ios.kt`: the Crashlytics SDK is an iOS framework, so
 * Swift implements [NativeCrashReporter] and registers it at launch.
 *
 * Unlike `getFirebaseTracker`, a missing registration falls back to [NoOpCrashReporter] instead of
 * throwing. `iOSApp.swift` deliberately skips Firebase setup under `XCODE_RUNNING_FOR_PREVIEWS` and
 * in the test bundle, and crash reporting is not worth failing those for.
 */
actual fun getCrashReporter(): CrashReporter = registeredNativeCrashReporter?.let(::IosCrashReporter) ?: NoOpCrashReporter

private var registeredNativeCrashReporter: NativeCrashReporter? = null

fun registerNativeCrashReporter(reporter: NativeCrashReporter) {
    registeredNativeCrashReporter = reporter
}

interface NativeCrashReporter {
    fun setEnabled(enabled: Boolean)

    fun log(message: String)

    /**
     * Takes the exception pre-flattened: `Throwable` does not cross the bridge as anything Swift can
     * hand to `Crashlytics.record`, so the Kotlin side supplies the name, reason and stack trace and
     * Swift assembles an `ExceptionModel`.
     */
    fun record(name: String, reason: String, stackTrace: List<String>)

    fun setCustomKey(key: String, value: String)
}

class IosCrashReporter(private val nativeReporter: NativeCrashReporter) : CrashReporter {

    override fun setEnabled(enabled: Boolean) {
        nativeReporter.setEnabled(enabled)
    }

    override fun log(message: String) {
        nativeReporter.log(message)
    }

    @OptIn(ExperimentalNativeApi::class)
    override fun recordException(throwable: Throwable, tag: String?) {
        tag?.let { nativeReporter.log(it) }
        nativeReporter.record(
            name = throwable::class.simpleName ?: DEFAULT_EXCEPTION_NAME,
            reason = throwable.message ?: throwable.toString(),
            stackTrace = throwable.getStackTrace().toList(),
        )
    }

    override fun setCustomKey(key: String, value: String) {
        nativeReporter.setCustomKey(key, value)
    }

    private companion object {
        const val DEFAULT_EXCEPTION_NAME = "KotlinException"
    }
}
