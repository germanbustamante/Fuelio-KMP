package com.germandebustamante.fuelio.core.analytics

sealed class Trace {
    abstract val eventName: String
    abstract val targets: List<AnalyticsProviderType>
    abstract val params: Map<String, Any>?

    data class Event(
        override val eventName: String,
        override val targets: List<AnalyticsProviderType>,
        override val params: Map<String, Any>? = null,
    ) : Trace()

    data class Screen(
        val screenName: String,
        override val targets: List<AnalyticsProviderType>,
        override val params: Map<String, Any>? = null,
    ) : Trace() {
        override val eventName: String get() = screenName
    }

    data class Error(
        override val eventName: String,
        override val targets: List<AnalyticsProviderType>,
        override val params: Map<String, Any>? = null,
    ) : Trace()
}
