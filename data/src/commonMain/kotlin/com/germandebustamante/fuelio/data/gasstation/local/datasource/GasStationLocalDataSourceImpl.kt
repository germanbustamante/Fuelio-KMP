package com.germandebustamante.fuelio.data.gasstation.local.datasource

import com.germandebustamante.fuelio.data.gasstation.local.model.GasStationEntity
import kotlinx.coroutines.flow.Flow

class GasStationLocalDataSourceImpl(private val gasStationDAO: GasStationDAO) : GasStationLocalDataSource {
    override suspend fun insertGasStations(gasStations: List<GasStationEntity>) {
        gasStationDAO.insertGasStations(gasStations)
    }

    override suspend fun getGasStationsByProvince(provinceId: String): List<GasStationEntity> =
        gasStationDAO.getGasStationsByProvince(provinceId)

    override fun getGasStationById(id: String): Flow<GasStationEntity?> =
        gasStationDAO.getGasStationById(id)
}
