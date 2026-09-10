package com.germandebustamante.fuelio.core.domain.gasstation.repository

import com.germandebustamante.fuelio.core.domain.gasstation.model.FavoriteStationsResult
import kotlinx.coroutines.flow.Flow

/**
 * No `Result` anywhere: favourites are a purely local read/write with no network path, so there is
 * no failure to model — the same reasoning as `GasStationRepository.getGasStationById`.
 */
interface FavoriteStationRepository {

    fun observeFavoriteIds(): Flow<Set<String>>

    fun observeFavoriteStations(): Flow<FavoriteStationsResult>

    suspend fun toggleFavorite(gasStationId: String)
}
