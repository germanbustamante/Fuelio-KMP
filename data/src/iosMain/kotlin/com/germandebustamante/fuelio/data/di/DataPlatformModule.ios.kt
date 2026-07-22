package com.germandebustamante.fuelio.data.di

import org.koin.dsl.module

actual val dataPlatformModule = module {
    single { ContextProvider(Unit) }
}
