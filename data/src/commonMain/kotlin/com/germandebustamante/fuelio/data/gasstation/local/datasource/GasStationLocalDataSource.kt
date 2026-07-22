package com.germandebustamante.fuelio.data.gasstation.local.datasource

import com.germandebustamante.fuelio.data.gasstation.local.model.GasStationEntity
import kotlinx.coroutines.flow.Flow

interface GasStationLocalDataSource {
    suspend fun insertGasStations(gasStations: List<GasStationEntity>)
    suspend fun getGasStationsByProvince(provinceId: String): List<GasStationEntity>
    fun getGasStationById(id: String): Flow<GasStationEntity?>
}
