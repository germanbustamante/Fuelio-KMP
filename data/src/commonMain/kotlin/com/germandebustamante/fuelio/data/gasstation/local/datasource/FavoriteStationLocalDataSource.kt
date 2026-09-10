package com.germandebustamante.fuelio.data.gasstation.local.datasource

import com.germandebustamante.fuelio.data.gasstation.local.model.GasStationEntity
import kotlinx.coroutines.flow.Flow

interface FavoriteStationLocalDataSource {
    fun observeFavoriteIds(): Flow<List<String>>
    fun observeFavoriteStations(): Flow<List<GasStationEntity>>
    fun observeFavoriteCount(): Flow<Int>
    suspend fun toggleFavorite(gasStationId: String, addedAt: Long)
}
