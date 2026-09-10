package com.germandebustamante.fuelio.core.analytics.crash

import com.google.firebase.crashlytics.FirebaseCrashlytics

actual fun getCrashReporter(): CrashReporter = AndroidCrashReporter()

class AndroidCrashReporter : CrashReporter {

    private val crashlytics: FirebaseCrashlytics get() = FirebaseCrashlytics.getInstance()

    override fun setEnabled(enabled: Boolean) {
        crashlytics.isCrashlyticsCollectionEnabled = enabled
    }

    override fun log(message: String) {
        crashlytics.log(message)
    }

    override fun recordException(throwable: Throwable, tag: String?) {
        // Crashlytics groups non-fatals by stack trace, not by message, so the tag goes in as a
        // breadcrumb rather than being folded into the exception.
        tag?.let { crashlytics.log(it) }
        crashlytics.recordException(throwable)
    }

    override fun setCustomKey(key: String, value: String) {
        crashlytics.setCustomKey(key, value)
    }
}
