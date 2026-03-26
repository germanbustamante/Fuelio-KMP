package com.germandebustamante.fuelio.core.domain.gasstation.repository

import com.germandebustamante.fuelio.core.domain.gasstation.model.GasStationBO
import kotlinx.coroutines.flow.Flow

interface GasStationRepository {
    fun getGasStationsByLocation(): Flow<Result<List<GasStationBO>>>
}