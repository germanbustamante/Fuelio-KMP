package com.germandebustamante.fuelio.di

import org.koin.core.module.Module

/**
 * Platform-provided presentation bindings, mirroring the `dataPlatformModule`/`analyticsPlatformModule`
 * seam (see the "ContextProvider pattern" section of CLAUDE.md).
 *
 * On iOS this registers `LocationPermissionController`, which the Android side cannot register here
 * because its implementation needs an `Activity` for the permission launcher — `MainActivity` injects
 * it into `GasStationsScreen` instead. Android therefore contributes an empty module.
 */
expect val presentationPlatformModule: Module
