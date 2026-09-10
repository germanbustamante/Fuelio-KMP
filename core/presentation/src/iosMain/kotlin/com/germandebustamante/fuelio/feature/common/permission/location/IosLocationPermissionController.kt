package com.germandebustamante.fuelio.feature.common.permission.location

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.CoreLocation.CLAuthorizationStatus
import platform.CoreLocation.CLGeocoder
import platform.CoreLocation.CLLocation
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.CoreLocation.CLPlacemark
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse
import platform.CoreLocation.kCLAuthorizationStatusDenied
import platform.CoreLocation.kCLAuthorizationStatusNotDetermined
import platform.CoreLocation.kCLAuthorizationStatusRestricted
import platform.CoreLocation.kCLDistanceFilterNone
import platform.CoreLocation.kCLLocationAccuracyBest
import platform.Foundation.NSError
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString
import platform.darwin.NSObject
import kotlin.coroutines.resume

class IosLocationPermissionController : LocationPermissionController {

    private val locationManager = CLLocationManager()
    private var activeDelegate: LocationDelegate? = null
    private var activeRequestDelegate: LocationRequestDelegate? = null

    override suspend fun requestPermission(): LocationPermissionState {
        val currentStatus = locationManager.authorizationStatus
        if (currentStatus != kCLAuthorizationStatusNotDetermined) return mapStatus(currentStatus)

        return suspendCancellableCoroutine { continuation ->
            val delegate = LocationDelegate(continuation, onDone = { activeDelegate = null })
            activeDelegate = delegate
            locationManager.delegate = delegate
            locationManager.requestWhenInUseAuthorization()
            continuation.invokeOnCancellation {
                locationManager.delegate = null
                activeDelegate = null
            }
        }
    }

    override suspend fun checkCurrentStatus(): LocationPermissionState = mapStatus(locationManager.authorizationStatus)

    override suspend fun getCurrentLocation(): LocationPermissionController.Location? {
        val clLocation = requestCurrentLocation() ?: return null
        return reverseGeocode(clLocation)
    }

    private suspend fun requestCurrentLocation(): CLLocation? = suspendCancellableCoroutine { continuation ->
        val delegate = LocationRequestDelegate(continuation, onDone = { activeRequestDelegate = null })
        activeRequestDelegate = delegate
        locationManager.distanceFilter = kCLDistanceFilterNone
        locationManager.desiredAccuracy = kCLLocationAccuracyBest
        locationManager.delegate = delegate
        locationManager.requestLocation()
        continuation.invokeOnCancellation {
            locationManager.delegate = null
            activeRequestDelegate = null
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    private suspend fun reverseGeocode(location: CLLocation): LocationPermissionController.Location? =
        suspendCancellableCoroutine { continuation ->
            CLGeocoder().reverseGeocodeLocation(location) { placemarks, _ ->
                val province = (placemarks?.firstOrNull() as? CLPlacemark)?.administrativeArea
                continuation.resume(
                    province?.let {
                        LocationPermissionController.Location(
                            it,
                            location.coordinate.useContents {
                                latitude
                            },
                            location.coordinate.useContents { longitude },
                        )
                    },
                )
            }
        }

    override fun openAppSettings() {
        val url = NSURL.URLWithString(UIApplicationOpenSettingsURLString) ?: return
        UIApplication.sharedApplication.openURL(
            url,
            options = emptyMap<Any?, Any>(),
            completionHandler = null,
        )
    }

    private fun mapStatus(status: CLAuthorizationStatus): LocationPermissionState = when (status) {
        kCLAuthorizationStatusNotDetermined -> LocationPermissionState.NotDetermined
        kCLAuthorizationStatusAuthorizedWhenInUse,
        kCLAuthorizationStatusAuthorizedAlways,
        -> LocationPermissionState.Granted
        kCLAuthorizationStatusDenied,
        kCLAuthorizationStatusRestricted,
        -> LocationPermissionState.DeniedAlways
        else -> LocationPermissionState.DeniedAlways
    }
}

private class LocationDelegate(
    private var continuation: CancellableContinuation<LocationPermissionState>?,
    private val onDone: () -> Unit,
) : NSObject(),
    CLLocationManagerDelegateProtocol {

    override fun locationManager(manager: CLLocationManager, didChangeAuthorizationStatus: CLAuthorizationStatus) {
        if (didChangeAuthorizationStatus == kCLAuthorizationStatusNotDetermined) return

        val state = when (didChangeAuthorizationStatus) {
            kCLAuthorizationStatusAuthorizedWhenInUse,
            kCLAuthorizationStatusAuthorizedAlways,
            -> LocationPermissionState.Granted
            else -> LocationPermissionState.DeniedAlways
        }

        continuation?.resume(state)
        continuation = null
        manager.delegate = null
        onDone()
    }
}

private class LocationRequestDelegate(private var continuation: CancellableContinuation<CLLocation?>?, private val onDone: () -> Unit) :
    NSObject(),
    CLLocationManagerDelegateProtocol {

    override fun locationManager(manager: CLLocationManager, didUpdateLocations: List<*>) {
        val location = didUpdateLocations.lastOrNull() as? CLLocation
        continuation?.resume(location)
        continuation = null
        manager.delegate = null
        onDone()
    }

    override fun locationManager(manager: CLLocationManager, didFailWithError: NSError) {
        continuation?.resume(null)
        continuation = null
        manager.delegate = null
        onDone()
    }
}
