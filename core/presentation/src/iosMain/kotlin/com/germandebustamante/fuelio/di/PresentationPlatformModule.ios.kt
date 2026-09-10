package com.germandebustamante.fuelio.di

import com.germandebustamante.fuelio.core.build.BuildEnvironment
import com.germandebustamante.fuelio.feature.common.permission.location.IosLocationPermissionController
import com.germandebustamante.fuelio.feature.common.permission.location.LocationPermissionController
import org.koin.dsl.module
import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.Platform

/**
 * `IosLocationPermissionController` owns a `CLLocationManager` and its delegates, so it is a
 * `single`: creating one per screen would leave several managers competing for the same
 * authorization callbacks.
 */
@OptIn(ExperimentalNativeApi::class)
actual val presentationPlatformModule = module {
    single<LocationPermissionController> { IosLocationPermissionController() }
    single {
        BuildEnvironment(
            isDebug = Platform.isDebugBinary,
            platform = BuildEnvironment.PLATFORM_IOS,
        )
    }
}
