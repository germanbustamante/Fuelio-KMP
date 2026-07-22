package com.germandebustamante.fuelio.core.domain.gasstation.repository

import com.germandebustamante.fuelio.core.domain.gasstation.model.GasStationBO
import com.germandebustamante.fuelio.core.domain.gasstation.model.GasStationsResult
import kotlinx.coroutines.flow.Flow

interface GasStationRepository {
    fun getGasStationsByLocation(provinceId: String): Flow<Result<GasStationsResult>>
    fun getGasStationById(id: String): Flow<GasStationBO?>
}
