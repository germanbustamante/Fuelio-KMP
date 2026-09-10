package com.germandebustamante.fuelio.feature.common.permission.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import kotlin.coroutines.resume

class AndroidLocationPermissionController(private val activity: ComponentActivity, private val launcher: ActivityResultLauncher<String>) :
    LocationPermissionController {

    private val fusedLocationProvider: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(activity)

    override suspend fun requestPermission(): LocationPermissionState {
        if (isPermissionGranted()) return LocationPermissionState.Granted

        val alreadyAsked = getAlreadyAskedFlag()
        val shouldShowRationale = ActivityCompat.shouldShowRequestPermissionRationale(activity, LOCATION_PERMISSION)

        if (alreadyAsked && !shouldShowRationale) return LocationPermissionState.DeniedAlways

        setAlreadyAskedFlag()
        launcher.launch(LOCATION_PERMISSION)

        val isGranted = PermissionResultBridge.awaitResult()

        return if (isGranted) {
            LocationPermissionState.Granted
        } else {
            val canAskAgain = ActivityCompat.shouldShowRequestPermissionRationale(activity, LOCATION_PERMISSION)
            if (canAskAgain) LocationPermissionState.Denied else LocationPermissionState.DeniedAlways
        }
    }

    override suspend fun checkCurrentStatus(): LocationPermissionState {
        val alreadyAsked = getAlreadyAskedFlag()
        val shouldShowRationale = ActivityCompat.shouldShowRequestPermissionRationale(activity, LOCATION_PERMISSION)
        return when {
            isPermissionGranted() -> LocationPermissionState.Granted
            alreadyAsked && !shouldShowRationale -> LocationPermissionState.DeniedAlways
            alreadyAsked -> LocationPermissionState.Denied
            else -> LocationPermissionState.NotDetermined
        }
    }

    override fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", activity.packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        activity.startActivity(intent)
    }

    @SuppressLint("MissingPermission")
    override suspend fun getCurrentLocation(): LocationPermissionController.Location? {
        val location = getLastLocation() ?: return null
        return reverseGeocode(location)
    }

    @SuppressLint("MissingPermission")
    private suspend fun getLastLocation(): Location? = suspendCancellableCoroutine { continuation ->
        fusedLocationProvider.lastLocation.addOnSuccessListener { continuation.resume(it) }
            .addOnFailureListener { continuation.resume(null) }
    }

    private suspend fun reverseGeocode(location: Location): LocationPermissionController.Location? =
        suspendCancellableCoroutine { continuation ->
            val geocoder = Geocoder(activity, Locale.getDefault())
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                geocoder.getFromLocation(location.latitude, location.longitude, CITY_MAX_RESULTS) { addresses ->
                    continuation.resume(
                        addresses.firstProvince()
                            ?.let { LocationPermissionController.Location(it, location.latitude, location.longitude) },
                    )
                }
            } else {
                @Suppress("DEPRECATION")
                val province =
                    geocoder.getFromLocation(location.latitude, location.longitude, CITY_MAX_RESULTS)?.firstProvince()
                continuation.resume(
                    province?.let {
                        LocationPermissionController.Location(
                            it,
                            location.latitude,
                            location.longitude,
                        )
                    },
                )
            }
        }

    private fun isPermissionGranted() =
        ContextCompat.checkSelfPermission(activity, LOCATION_PERMISSION) == PackageManager.PERMISSION_GRANTED

    private fun getAlreadyAskedFlag() = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getBoolean(KEY_ALREADY_ASKED, false)

    private fun setAlreadyAskedFlag() {
        activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit { putBoolean(KEY_ALREADY_ASKED, true) }
    }

    private fun List<Address>.firstProvince() = firstOrNull()?.subAdminArea

    companion object {
        private const val LOCATION_PERMISSION = Manifest.permission.ACCESS_FINE_LOCATION
        private const val PREFS_NAME = "location_permission_prefs"
        private const val KEY_ALREADY_ASKED = "already_asked_location"
        private const val CITY_MAX_RESULTS = 1
    }
}
