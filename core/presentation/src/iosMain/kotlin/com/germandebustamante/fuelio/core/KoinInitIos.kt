package com.germandebustamante.fuelio.core

import com.germandebustamante.fuelio.core.interop.uiTestModule
import com.germandebustamante.fuelio.di.initKoin

fun initKoinIos() {
    initKoin()
}

/**
 * Same graph as [initKoinIos] with the repositories and the location permission prompt replaced by
 * in-memory fakes. Called only when the app is launched with `-UITestMode`, so XCUITests are
 * deterministic and never hit the network.
 */
fun initKoinIosForUiTests() {
    initKoin(overrides = listOf(uiTestModule))
}
