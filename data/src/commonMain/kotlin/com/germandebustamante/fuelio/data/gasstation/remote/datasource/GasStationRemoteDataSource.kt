package com.germandebustamante.fuelio.data.gasstation.remote.datasource

import com.germandebustamante.fuelio.data.gasstation.remote.model.GasStationDTO

interface GasStationRemoteDataSource {
    suspend fun getGasStationsByLocation(provinceId: String): List<GasStationDTO>
}