package com.germandebustamante.fuelio.core.analytics.impl

import com.germandebustamante.fuelio.core.analytics.AnalyticsProviderType
import com.germandebustamante.fuelio.core.analytics.Tracker

expect fun getFirebaseTracker(): FirebaseTracker

abstract class FirebaseTracker : Tracker() {
    final override val type: AnalyticsProviderType = AnalyticsProviderType.FIREBASE
}
