package com.germandebustamante.fuelio.data.di

import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

actual val dataPlatformModule = module {
    single { ContextProvider(androidContext()) }
}
