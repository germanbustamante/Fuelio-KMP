package com.germandebustamante.fuelio.feature.common.permission.location

sealed class LocationPermissionState {
    data object NotDetermined : LocationPermissionState()
    data object Granted : LocationPermissionState()
    data object Denied : LocationPermissionState()
    data object DeniedAlways : LocationPermissionState()
}