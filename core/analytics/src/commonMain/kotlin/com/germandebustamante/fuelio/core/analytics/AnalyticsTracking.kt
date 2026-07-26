package com.germandebustamante.fuelio.core.analytics

interface AnalyticsTracking {

    suspend fun track(trace: Trace)
}
