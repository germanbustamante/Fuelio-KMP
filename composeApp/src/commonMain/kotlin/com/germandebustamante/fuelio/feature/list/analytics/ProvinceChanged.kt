package com.germandebustamante.fuelio.feature.list.analytics

import com.germandebustamante.fuelio.core.analytics.AnalyticsProviderType
import com.germandebustamante.fuelio.core.analytics.Trace

data class ProvinceChanged(val provinceId: String, val provinceName: String) : Trace.Event(
    eventName = EVENT_NAME,
    targets = listOf(AnalyticsProviderType.FIREBASE, AnalyticsProviderType.POSTHOG),
    params = mapOf(PARAM_PROVINCE_ID to provinceId, PARAM_PROVINCE_NAME to provinceName),
) {
    companion object {
        const val EVENT_NAME = "gas_stations_list_province_changed"
        const val PARAM_PROVINCE_ID = "province_id"
        const val PARAM_PROVINCE_NAME = "province_name"
    }
}
