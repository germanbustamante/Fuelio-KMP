package com.germandebustamante.fuelio.di

import com.germandebustamante.fuelio.feature.common.permission.location.IosLocationPermissionController
import com.germandebustamante.fuelio.feature.common.permission.location.LocationPermissionController
import org.koin.dsl.module

/**
 * `IosLocationPermissionController` owns a `CLLocationManager` and its delegates, so it is a
 * `single`: creating one per screen would leave several managers competing for the same
 * authorization callbacks.
 */
actual val presentationPlatformModule = module {
    single<LocationPermissionController> { IosLocationPermissionController() }
}
