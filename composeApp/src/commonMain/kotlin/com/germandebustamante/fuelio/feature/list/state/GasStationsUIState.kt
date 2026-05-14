package com.germandebustamante.fuelio.feature.list.state

import com.germandebustamante.fuelio.core.domain.error.DomainError
import com.germandebustamante.fuelio.core.domain.province.model.ProvinceBO
import com.germandebustamante.fuelio.feature.common.permission.location.LocationPermissionState

data class GasStationsUIState(
    val gasStations: List<GasStationItemVO> = emptyList(),
    val provinces: List<ProvinceBO> = emptyList(),
    val selectedProvince: ProvinceBO? = null,
    val selectedFuelFilter: FuelFilter = FuelFilter.Gasoline95,
    val searchQuery: String = "",
    val isLoading: Boolean = true,
    val showFilterProvince: Boolean = false,
    val error: DomainError? = null,
    val locationPermissionState: LocationPermissionState? = null,
) {
    fun isContentReady() = selectedProvince != null && !isLoading

    fun withProvinces(provinces: List<ProvinceBO>) = copy(provinces = provinces)

    fun withLoadingProvince(province: ProvinceBO) = copy(isLoading = true, selectedProvince = province)

    fun withStationsLoaded(stations: List<GasStationItemVO>) = copy(gasStations = stations, isLoading = false)

    fun withFuelFilter(filter: FuelFilter, stations: List<GasStationItemVO>) = copy(selectedFuelFilter = filter, gasStations = stations)

    fun withSearchQuery(query: String, stations: List<GasStationItemVO>) = copy(searchQuery = query, gasStations = stations)

    fun withProvinceFilterVisible(visible: Boolean) = copy(showFilterProvince = visible)

    fun withLocationPermission(permissionState: LocationPermissionState?) = copy(locationPermissionState = permissionState)

    fun withError(error: DomainError) = copy(error = error, isLoading = false)

    fun withErrorCleared() = copy(error = null)
}
