package com.germandebustamante.fuelio

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.germandebustamante.fuelio.core.navigation.FuelioNavHost
import com.germandebustamante.fuelio.core.ui.theme.FuelioTheme
import com.germandebustamante.fuelio.core.ui.theme.isDark
import com.germandebustamante.fuelio.feature.app.state.AppViewModel
import com.germandebustamante.fuelio.feature.common.permission.location.LocationPermissionController
import com.germandebustamante.fuelio.feature.common.permission.location.LocationPermissionState
import org.koin.compose.viewmodel.koinViewModel

/**
 * The theme is resolved here rather than inside a screen: it has to wrap the whole navigation graph,
 * which is above any screen ViewModel. `AppViewModel` is the shared holder for exactly that — iOS
 * applies the same state as `.preferredColorScheme` on `RootView`.
 */
@Composable
fun App(locationPermissionController: LocationPermissionController, viewModel: AppViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    FuelioTheme(darkTheme = state.themeMode.isDark()) {
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
    FuelioTheme { FuelioNavHost(PreviewLocationPermissionController) }
}
