package com.germandebustamante.fuelio.feature.common.permission.location

interface LocationPermissionController {
    suspend fun requestPermission(): LocationPermissionState
    suspend fun checkCurrentStatus(): LocationPermissionState
    fun openAppSettings()

    suspend fun getCurrentLocation(): Location?

    data class Location(val province: String, val latitude: Double, val longitude: Double)
}
