package com.germandebustamante.fuelio.core

import com.germandebustamante.fuelio.core.testing.uiTestModule
import com.germandebustamante.fuelio.di.initKoin

fun initKoinIos() {
    initKoin()
}

/**
 * Same graph as [initKoinIos] with the repositories and the location permission prompt replaced by
 * in-memory fakes. Called only when the app is launched with `-UITestMode`, so XCUITests are
 * deterministic and never hit the network.
 *
 * @param simulateStationFailure makes the gas station repository fail while provinces still load,
 * which is what the blocking error state needs in order to appear.
 */
fun initKoinIosForUiTests(simulateStationFailure: Boolean) {
    initKoin(overrides = listOf(uiTestModule(simulateStationFailure)))
}
