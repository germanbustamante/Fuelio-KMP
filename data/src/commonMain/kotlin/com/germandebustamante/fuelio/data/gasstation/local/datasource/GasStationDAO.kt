package com.germandebustamante.fuelio.data.gasstation.local.datasource

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.germandebustamante.fuelio.data.gasstation.local.model.GasStationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GasStationDAO {
    @Upsert
    suspend fun insertGasStations(gasStations: List<GasStationEntity>)

    @Query("SELECT * FROM GasStationEntity WHERE provinceId = :provinceId")
    suspend fun getGasStationsByProvince(provinceId: String): List<GasStationEntity>

    @Query("SELECT * FROM GasStationEntity WHERE id = :id")
    fun getGasStationById(id: String): Flow<GasStationEntity?>
}