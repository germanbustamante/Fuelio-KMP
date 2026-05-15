package com.germandebustamante.fuelio.feature.list.state

import com.germandebustamante.fuelio.core.domain.error.DomainError
import com.germandebustamante.fuelio.core.domain.province.model.ProvinceBO

sealed interface ContentState {
    data object Initial : ContentState
    data object Loading : ContentState
    data class Success(val stations: List<GasStationItemVO>) : ContentState
    data object Empty : ContentState
    data class Error(val message: String) : ContentState
}

data class GasStationsUIState(
    val gasStations: List<GasStationItemVO> = emptyList(),
    val provinces: List<ProvinceBO> = emptyList(),
    val selectedProvince: ProvinceBO? = null,
    val selectedFuelFilter: FuelFilter = FuelFilter.Gasoline95,
    val searchQuery: String = "",
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val showFilterProvince: Boolean = false,
    val error: DomainError? = null,
    val showPermissionDeniedPermanentlySnackbar: Boolean = false,
    val favorites: Set<String> = emptySet(),
) {
    fun isContentReady() = selectedProvince != null && !isLoading

    val contentState: ContentState
        get() = when {
            selectedProvince == null || isLoading -> ContentState.Loading
            error != null -> ContentState.Error(error.message.orEmpty())
            gasStations.isEmpty() -> ContentState.Empty
            else -> ContentState.Success(gasStations)
        }

    fun withProvinces(provinces: List<ProvinceBO>) = copy(provinces = provinces)

    fun withLoadingProvince(province: ProvinceBO) = copy(isLoading = true, selectedProvince = province)

    fun withStationsLoaded(stations: List<GasStationItemVO>) = copy(gasStations = stations, isLoading = false, isRefreshing = false)

    fun withFuelFilter(filter: FuelFilter, stations: List<GasStationItemVO>) = copy(selectedFuelFilter = filter, gasStations = stations)

    fun withSearchQuery(query: String, stations: List<GasStationItemVO>) = copy(searchQuery = query, gasStations = stations)

    fun withProvinceFilterVisible(visible: Boolean) = copy(showFilterProvince = visible)

    fun withPermissionDeniedPermanently() = copy(showPermissionDeniedPermanentlySnackbar = true)

    fun withPermissionSnackbarDismissed() = copy(showPermissionDeniedPermanentlySnackbar = false)

    fun withError(error: DomainError) = copy(error = error, isLoading = false, isRefreshing = false)

    fun withErrorCleared() = copy(error = null)

    fun withRefreshing() = copy(isRefreshing = true, error = null)

    fun withFavoriteToggled(stationId: String) = copy(
        favorites = if (stationId in favorites) favorites - stationId else favorites + stationId,
    )
}
