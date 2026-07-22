package com.germandebustamante.fuelio.data.di

import android.content.Context

actual class ContextProvider actual constructor(private val context: Any) {
    actual fun getContext(): Any = context

    fun getAndroidContext(): Context = context as Context
}
