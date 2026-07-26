package com.germandebustamante.fuelio.core.analytics

sealed class Trace {
    abstract val eventName: String
    abstract val targets: List<AnalyticsProviderType>
    abstract val params: Map<String, Any>?

    open class Event(
        override val eventName: String,
        override val targets: List<AnalyticsProviderType>,
        override val params: Map<String, Any>? = null,
    ) : Trace() {
        override fun equals(other: Any?): Boolean =
            other is Event && eventName == other.eventName && targets == other.targets && params == other.params

        override fun hashCode(): Int = listOf(eventName, targets, params).hashCode()

        override fun toString(): String = "Trace.Event(eventName=$eventName, targets=$targets, params=$params)"
    }

    open class Screen(
        val screenName: String,
        override val targets: List<AnalyticsProviderType>,
        override val params: Map<String, Any>? = null,
    ) : Trace() {
        override val eventName: String get() = screenName

        override fun equals(other: Any?): Boolean =
            other is Screen && screenName == other.screenName && targets == other.targets && params == other.params

        override fun hashCode(): Int = listOf(screenName, targets, params).hashCode()

        override fun toString(): String = "Trace.Screen(screenName=$screenName, targets=$targets, params=$params)"
    }

    open class Error(
        override val eventName: String,
        override val targets: List<AnalyticsProviderType>,
        override val params: Map<String, Any>? = null,
    ) : Trace() {
        override fun equals(other: Any?): Boolean =
            other is Error && eventName == other.eventName && targets == other.targets && params == other.params

        override fun hashCode(): Int = listOf(eventName, targets, params).hashCode()

        override fun toString(): String = "Trace.Error(eventName=$eventName, targets=$targets, params=$params)"
    }
}
