package com.germandebustamante.fuelio.core.analytics

interface Trackable {

    val type: AnalyticsProviderType

    fun track(trace: Trace)
}
