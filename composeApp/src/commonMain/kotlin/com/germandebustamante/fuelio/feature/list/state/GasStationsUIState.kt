package com.germandebustamante.fuelio.feature.list.state

import com.germandebustamante.fuelio.core.domain.error.DomainError
import com.germandebustamante.fuelio.core.domain.province.model.ProvinceBO

data class GasStationsUIState(
    val gasStations: List<GasStationItemVO> = emptyList(),
    val provinces: List<ProvinceBO> = emptyList(),
    val selectedProvince: ProvinceBO? = null,
    val isLoading: Boolean = true,
    val showFilterProvince: Boolean = false,
    val error: DomainError? = null,
) {
    fun hasGasStationsLoaded() = gasStations.isNotEmpty() && selectedProvince != null
}