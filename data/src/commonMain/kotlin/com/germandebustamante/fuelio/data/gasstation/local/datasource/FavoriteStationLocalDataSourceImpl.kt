package com.germandebustamante.fuelio.data.gasstation.local.datasource

import com.germandebustamante.fuelio.data.gasstation.local.model.GasStationEntity
import kotlinx.coroutines.flow.Flow

class FavoriteStationLocalDataSourceImpl(private val dao: FavoriteStationDAO) : FavoriteStationLocalDataSource {

    override fun observeFavoriteIds(): Flow<List<String>> = dao.observeFavoriteIds()

    override fun observeFavoriteStations(): Flow<List<GasStationEntity>> = dao.observeFavoriteStations()

    override fun observeFavoriteCount(): Flow<Int> = dao.observeFavoriteCount()

    override suspend fun toggleFavorite(gasStationId: String, addedAt: Long) = dao.toggleFavorite(gasStationId, addedAt)
}
