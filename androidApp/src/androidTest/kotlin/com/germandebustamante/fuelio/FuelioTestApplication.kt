package com.germandebustamante.fuelio

import android.app.Application
import androidx.test.platform.app.InstrumentationRegistry
import com.germandebustamante.fuelio.core.testing.uiTestModule
import com.germandebustamante.fuelio.di.initKoin
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger

/**
 * Instrumentation-test `Application`, installed by [FuelioTestRunner] in place of
 * [AndroidApplication]. The Android mirror of iOS's `initKoinIosForUiTests`: swaps the repositories
 * and the location permission prompt for the shared in-memory fakes in `uiTestModule`, so the suite
 * runs deterministically without the network, Room, or a system permission dialog blocking a test —
 * every layer above (use cases, ViewModels, `Navigator`, analytics) stays the real production graph.
 *
 * Duplicates `AndroidApplication.onCreate`'s Koin setup rather than extending it, since the ANR
 * watchdog it also starts has no place in an instrumentation run and `initKoin` there takes no
 * `overrides` parameter to hook into.
 *
 * `-e simulateFailure true` (an instrumentation argument, the Android equivalent of iOS's
 * `-UITestFailure` launch argument) makes the gas station repository fail so a test can reach the
 * blocking error state.
 */
class FuelioTestApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        val simulateFailure = InstrumentationRegistry.getArguments()
            .getString("simulateFailure")
            .toBoolean()

        initKoin(overrides = listOf(uiTestModule(simulateStationFailure = simulateFailure))) {
            androidContext(this@FuelioTestApplication)
            androidLogger()
        }
    }
}
