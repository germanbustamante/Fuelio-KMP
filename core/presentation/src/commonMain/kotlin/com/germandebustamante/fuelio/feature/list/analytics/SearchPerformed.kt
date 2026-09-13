package com.germandebustamante.fuelio.feature.list.analytics

import com.germandebustamante.fuelio.core.analytics.AnalyticsProviderType
import com.germandebustamante.fuelio.core.analytics.Trace

/**
 * Tracked once per debounced, non-blank query — not per keystroke. `GasStationsViewModel` fires this
 * from `observeSearchQuery`'s collector, after the same debounce the filtering itself waits on, so a
 * user typing "repsol" produces one event rather than six.
 *
 * The query text itself is never sent, only its length — enough to see whether search is used at
 * all and roughly how, without capturing free-text user input.
 */
data class SearchPerformed(val queryLength: Int) :
    Trace.Event(
        eventName = EVENT_NAME,
        targets = listOf(AnalyticsProviderType.FIREBASE, AnalyticsProviderType.POSTHOG),
        params = mapOf(PARAM_QUERY_LENGTH to queryLength),
    ) {
    companion object {
        const val EVENT_NAME = "gas_stations_list_search_performed"
        const val PARAM_QUERY_LENGTH = "query_length"
    }
}
