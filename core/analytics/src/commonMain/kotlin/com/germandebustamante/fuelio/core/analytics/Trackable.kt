package com.germandebustamante.fuelio.core.analytics

interface Trackable {

    val type: AnalyticsProviderType

    suspend fun track(trace: Trace)

    suspend fun identify(distinctId: String, properties: Map<String, Any>)
}
