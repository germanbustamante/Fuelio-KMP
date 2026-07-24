package com.germandebustamante.fuelio.core.analytics

abstract class Tracker : Trackable {

    final override fun track(trace: Trace) {
        when (trace) {
            is Trace.Event -> onTrackEvent(trace)
            is Trace.Screen -> onTrackScreen(trace)
            is Trace.Error -> onTrackError(trace)
        }
    }

    protected open fun onTrackEvent(trace: Trace.Event) {
        // Empty implementation for not forcing every tracker to implement it
    }

    protected open fun onTrackScreen(trace: Trace.Screen) {
        // Empty implementation for not forcing every tracker to implement it
    }

    protected open fun onTrackError(trace: Trace.Error) {
        // Empty implementation for not forcing every tracker to implement it
    }
}
