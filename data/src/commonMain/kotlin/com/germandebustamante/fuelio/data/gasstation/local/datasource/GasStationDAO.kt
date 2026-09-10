package com.germandebustamante.fuelio.data.gasstation.local.datasource

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.germandebustamante.fuelio.data.gasstation.local.model.GasStationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GasStationDAO {
    @Upsert
    suspend fun insertGasStations(gasStations: List<GasStationEntity>)

    @Query("DELETE FROM GasStationEntity WHERE provinceId = :provinceId")
    suspend fun deleteGasStationsByProvince(provinceId: String)

    /** Replaces the province's cached stations atomically, so entries no longer present remotely (closed/delisted) don't linger in cache. */
    @Transaction
    suspend fun replaceGasStationsByProvince(provinceId: String, gasStations: List<GasStationEntity>) {
        deleteGasStationsByProvince(provinceId)
        insertGasStations(gasStations)
    }

    @Query("SELECT * FROM GasStationEntity WHERE provinceId = :provinceId")
    suspend fun getGasStationsByProvince(provinceId: String): List<GasStationEntity>

    @Query("SELECT * FROM GasStationEntity WHERE id = :id")
    fun getGasStationById(id: String): Flow<GasStationEntity?>
}
