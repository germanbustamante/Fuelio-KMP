package com.germandebustamante.fuelio.core.analytics

class AnalyticsManager(private val trackers: List<Trackable>) : AnalyticsTracking {

    override suspend fun track(trace: Trace) {
        trace.targets.forEach { target ->
            trackers.firstOrNull { it.type == target }?.track(trace)
        }
    }

    override suspend fun identify(distinctId: String, properties: Map<String, Any>) {
        trackers.forEach { it.identify(distinctId, properties) }
    }
}
