package com.germandebustamante.fuelio

import androidx.compose.ui.window.ComposeUIViewController
import com.germandebustamante.fuelio.feature.common.permission.location.IosLocationPermissionController

fun MainViewController() = ComposeUIViewController {
    App(IosLocationPermissionController())
}