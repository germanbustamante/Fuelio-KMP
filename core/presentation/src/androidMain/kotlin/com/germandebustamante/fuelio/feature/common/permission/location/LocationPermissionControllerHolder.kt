package com.germandebustamante.fuelio.feature.common.permission.location

/**
 * Backs the Android `single<LocationPermissionController>` Koin registration in
 * `PresentationPlatformModule.android.kt`.
 *
 * `AndroidLocationPermissionController` needs the hosting `Activity` for its permission launcher, so
 * it cannot be constructed inside a Koin module lambda the way `IosLocationPermissionController` is —
 * `MainActivity.onCreate` sets [controller] before `setContent`, once its launcher is registered.
 * Defaults to a no-op rather than `lateinit`: Compose Previews run the real `Application.onCreate`
 * (so Koin is initialized) but never run an `Activity`, and a `single` that resolves a harmless
 * no-op is safer than one that crashes on first preview render.
 */
object LocationPermissionControllerHolder {
    var controller: LocationPermissionController = NoOpLocationPermissionController
}

private object NoOpLocationPermissionController : LocationPermissionController {
    override suspend fun requestPermission() = LocationPermissionState.DeniedAlways

    override suspend fun checkCurrentStatus() = LocationPermissionState.DeniedAlways

    override suspend fun getCurrentLocation(): LocationPermissionController.Location? = null

    override fun openAppSettings() = Unit
}
