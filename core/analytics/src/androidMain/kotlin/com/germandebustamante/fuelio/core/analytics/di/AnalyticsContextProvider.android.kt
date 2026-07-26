package com.germandebustamante.fuelio.core.analytics.di

import android.app.Application

actual class AnalyticsContextProvider actual constructor(private val context: Any) {
    actual fun getContext(): Any = context

    fun getApplication(): Application = context as Application
}
