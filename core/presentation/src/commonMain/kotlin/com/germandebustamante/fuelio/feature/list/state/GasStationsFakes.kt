package com.germandebustamante.fuelio.feature.list.state

import com.germandebustamante.fuelio.core.domain.error.DomainError
import com.germandebustamante.fuelio.core.domain.province.model.ProvinceBO
import com.germandebustamante.fuelio.core.fake.fakeGasStations

val fakeProvince = ProvinceBO("1", "Sevilla")
val fakeProvinces = listOf(
    fakeProvince,
    ProvinceBO("2", "Madrid"),
    ProvinceBO("3", "Barcelona"),
    ProvinceBO("4", "Valencia"),
    ProvinceBO("5", "Zaragoza"),
    ProvinceBO("6", "Málaga"),
)

val fakeGasStationItemVOs = fakeGasStations.map { GasStationItemVO(it) }

val fakeGasStationsUIState = GasStationsUIState(
    gasStations = fakeGasStationItemVOs,
    provinces = fakeProvinces,
    selectedProvince = fakeProvince,
    isLoading = false,
)

val fakeGasStationsUIStateError = fakeGasStationsUIState.copy(
    error = DomainError.ServerError(403),
)

val fakeGasStationsUIStateLoading = fakeGasStationsUIState.copy(
    isLoading = true,
)

val fakeGasStationsUIStateShowModalSheet = fakeGasStationsUIState.copy(
    showFilterProvince = true,
)

val fakeGasStationsUIStatePermissionSnackbar = fakeGasStationsUIState.copy(
    showPermissionDeniedPermanentlySnackbar = true,
)
