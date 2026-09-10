package com.germandebustamante.fuelio.data.gasstation.repository

import com.germandebustamante.fuelio.core.domain.gasstation.model.FavoriteStationsResult
import com.germandebustamante.fuelio.core.domain.gasstation.repository.FavoriteStationRepository
import com.germandebustamante.fuelio.data.gasstation.local.datasource.FavoriteStationLocalDataSource
import com.germandebustamante.fuelio.data.gasstation.local.mapper.toDomain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlin.time.Clock

class FavoriteStationRepositoryImpl(private val localDataSource: FavoriteStationLocalDataSource, private val clock: Clock = Clock.System) :
    FavoriteStationRepository {

    override fun observeFavoriteIds(): Flow<Set<String>> = localDataSource.observeFavoriteIds().map { it.toSet() }.flowOn(Dispatchers.IO)

    /**
     * The count is read separately from the joined rows so the difference between them — favourites
     * whose station is no longer cached — is visible to the screen instead of silently vanishing.
     */
    override fun observeFavoriteStations(): Flow<FavoriteStationsResult> = combine(
        localDataSource.observeFavoriteStations(),
        localDataSource.observeFavoriteCount(),
    ) { entities, totalCount ->
        FavoriteStationsResult(stations = entities.map { it.toDomain() }, totalFavoriteCount = totalCount)
    }.flowOn(Dispatchers.IO)

    override suspend fun toggleFavorite(gasStationId: String) = withContext(Dispatchers.IO) {
        localDataSource.toggleFavorite(gasStationId, clock.now().toEpochMilliseconds())
    }
}
