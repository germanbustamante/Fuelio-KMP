package com.germandebustamante.fuelio.feature.common.analytics

import com.germandebustamante.fuelio.core.analytics.AnalyticsProviderType
import com.germandebustamante.fuelio.core.analytics.Trace

class ApiCallFailed(screenName: String, operation: String, errorType: String, errorMessage: String? = null) :
    Trace.Error(
        eventName = "${screenName}_${operation}_failed",
        targets = listOf(AnalyticsProviderType.FIREBASE, AnalyticsProviderType.POSTHOG),
        params = buildMap {
            put(PARAM_ERROR_TYPE, errorType)
            errorMessage?.let { put(PARAM_ERROR_MESSAGE, it) }
        },
    ) {
    companion object {
        const val PARAM_ERROR_TYPE = "error_type"
        const val PARAM_ERROR_MESSAGE = "error_message"
    }
}
