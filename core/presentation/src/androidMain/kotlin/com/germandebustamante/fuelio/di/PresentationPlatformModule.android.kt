package com.germandebustamante.fuelio.di

import android.content.pm.ApplicationInfo
import com.germandebustamante.fuelio.core.build.BuildEnvironment
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/**
 * `AndroidLocationPermissionController` is deliberately absent: it needs the hosting `Activity` for
 * its permission launcher, so `MainActivity` builds it and passes it to `GasStationsScreen`, which
 * forwards it to Koin as a resolution parameter.
 *
 * [BuildEnvironment] is resolved here because `:core:presentation` is a library module and has no
 * `BuildConfig.DEBUG` of the app to read — the debuggable flag on the application's own
 * `ApplicationInfo` is the equivalent, and it's the same check `AndroidApplication` uses to gate
 * ANR-WatchDog.
 */
actual val presentationPlatformModule = module {
    single {
        val applicationInfo = androidContext().applicationInfo
        BuildEnvironment(
            isDebug = (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0,
            platform = BuildEnvironment.PLATFORM_ANDROID,
        )
    }
}
