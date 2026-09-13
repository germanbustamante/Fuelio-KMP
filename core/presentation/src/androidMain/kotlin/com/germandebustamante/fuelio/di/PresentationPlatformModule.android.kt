package com.germandebustamante.fuelio.di

import android.content.pm.ApplicationInfo
import com.germandebustamante.fuelio.core.build.BuildEnvironment
import com.germandebustamante.fuelio.feature.common.permission.location.LocationPermissionController
import com.germandebustamante.fuelio.feature.common.permission.location.LocationPermissionControllerHolder
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/**
 * [BuildEnvironment] is resolved here because `:core:presentation` is a library module and has no
 * `BuildConfig.DEBUG` of the app to read — the debuggable flag on the application's own
 * `ApplicationInfo` is the equivalent, and it's the same check `AndroidApplication` uses to gate
 * ANR-WatchDog.
 *
 * `LocationPermissionController` reads through [LocationPermissionControllerHolder] rather than
 * being constructed here: `AndroidLocationPermissionController` needs the hosting `Activity`, which
 * `MainActivity.onCreate` sets on the holder before `setContent` — this `single` is what lets
 * `GasStationListModule`'s `viewModel {}` resolve it with a plain `get()` on both platforms, instead
 * of Android alone needing it passed as a Koin resolution parameter.
 */
actual val presentationPlatformModule = module {
    single {
        val applicationInfo = androidContext().applicationInfo
        BuildEnvironment(
            isDebug = (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0,
            platform = BuildEnvironment.PLATFORM_ANDROID,
        )
    }
    single<LocationPermissionController> { LocationPermissionControllerHolder.controller }
}
