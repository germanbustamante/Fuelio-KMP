package com.germandebustamante.fuelio.core.analytics

interface AnalyticsTracking {

    suspend fun track(trace: Trace)

    /**
     * Associates every future event with [distinctId] and attaches [properties] as super-properties
     * (sent alongside every subsequent event, not just this call) — platform and default fuel today,
     * see `AnalyticsIdentityStartupTask`. Unlike [track], this fans out to every registered tracker
     * unconditionally: identity/super-properties are not a per-event choice the way `Trace.targets` is.
     */
    suspend fun identify(distinctId: String, properties: Map<String, Any>)
}
