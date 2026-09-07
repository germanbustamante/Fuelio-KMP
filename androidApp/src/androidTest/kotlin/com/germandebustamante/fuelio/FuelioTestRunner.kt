package com.germandebustamante.fuelio

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner

/**
 * Registered as `testInstrumentationRunner` — substitutes [FuelioTestApplication] for
 * [AndroidApplication] so every instrumentation test starts with the deterministic Koin graph
 * instead of the real network/Room-backed one.
 */
class FuelioTestRunner : AndroidJUnitRunner() {

    override fun newApplication(cl: ClassLoader?, className: String?, context: Context?): Application =
        super.newApplication(cl, FuelioTestApplication::class.java.name, context)
}
