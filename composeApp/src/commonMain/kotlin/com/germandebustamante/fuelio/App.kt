package com.germandebustamante.fuelio

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.germandebustamante.fuelio.core.navigation.FuelioNavHost
import com.germandebustamante.fuelio.core.ui.theme.FuelioTheme
import com.germandebustamante.fuelio.feature.common.permission.location.LocationPermissionController
import com.germandebustamante.fuelio.feature.common.permission.location.LocationPermissionState

@Composable
fun App(locationPermissionController: LocationPermissionController) {
    FuelioTheme {
        FuelioNavHost(locationPermissionController)
    }
}

private object PreviewLocationPermissionController : LocationPermissionController {
    override suspend fun requestPermission() = LocationPermissionState.Granted
    override suspend fun checkCurrentStatus() = LocationPermissionState.Granted
    override fun openAppSettings() = Unit
    override suspend fun getCurrentLocation(): LocationPermissionController.Location? = null
}

@Preview
@Composable
private fun AppPreview() {
    FuelioTheme { App(PreviewLocationPermissionController) }
}
