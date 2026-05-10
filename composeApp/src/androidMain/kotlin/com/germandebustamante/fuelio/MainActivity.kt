package com.germandebustamante.fuelio

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import com.germandebustamante.fuelio.feature.common.permission.location.AndroidLocationPermissionController
import com.germandebustamante.fuelio.feature.common.permission.location.LocationPermissionController
import com.germandebustamante.fuelio.feature.common.permission.location.PermissionResultBridge

class MainActivity : ComponentActivity() {

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted -> PermissionResultBridge.deliverResult(isGranted) }

    private val locationPermissionController: LocationPermissionController by lazy {
        AndroidLocationPermissionController(this, locationPermissionLauncher)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            App(locationPermissionController)
        }
    }
}