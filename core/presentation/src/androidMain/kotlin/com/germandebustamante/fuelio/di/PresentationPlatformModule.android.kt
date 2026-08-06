package com.germandebustamante.fuelio.di

import org.koin.dsl.module

/**
 * Empty on Android: `AndroidLocationPermissionController` needs the hosting `Activity` for its
 * permission launcher, so `MainActivity` builds it and passes it to `GasStationsScreen`, which
 * forwards it to Koin as a resolution parameter.
 */
actual val presentationPlatformModule = module { }
