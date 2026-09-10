package com.germandebustamante.fuelio.data.gasstation.repository

import app.cash.turbine.test
import com.germandebustamante.fuelio.data.gasstation.local.datasource.FavoriteStationLocalDataSource
import com.germandebustamante.fuelio.data.gasstation.local.model.GasStationEntity
import com.germandebustamante.fuelio.data.gasstation.local.model.GasStationEntityMother
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class FavoriteStationRepositoryImplTest {

    @Test
    fun `observeFavoriteIds - GIVEN stored favorites WHEN observed THEN they arrive as a set`() = runTest {
        val localDataSource = InMemoryFavoriteStationLocalDataSource(favoriteIds = listOf(STATION_ID, OTHER_STATION_ID))

        FavoriteStationRepositoryImpl(localDataSource).observeFavoriteIds().test {
            assertEquals(setOf(STATION_ID, OTHER_STATION_ID), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `toggleFavorite - GIVEN a station WHEN toggled twice THEN it is added and removed`() = runTest {
        val localDataSource = InMemoryFavoriteStationLocalDataSource()
        val sut = FavoriteStationRepositoryImpl(localDataSource)

        sut.observeFavoriteIds().test {
            assertEquals(emptySet(), awaitItem())

            sut.toggleFavorite(STATION_ID)
            assertEquals(setOf(STATION_ID), awaitItem())

            sut.toggleFavorite(STATION_ID)
            assertEquals(emptySet(), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `observeFavoriteStations - GIVEN every favorite is cached THEN none are reported unresolved`() = runTest {
        val station = GasStationEntityMother.gasStationEntity(id = STATION_ID)
        val localDataSource = InMemoryFavoriteStationLocalDataSource(
            favoriteIds = listOf(STATION_ID),
            cachedStations = listOf(station),
        )

        FavoriteStationRepositoryImpl(localDataSource).observeFavoriteStations().test {
            val result = awaitItem()
            assertEquals(listOf(STATION_ID), result.stations.map { it.id })
            assertEquals(0, result.unresolvedCount)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `observeFavoriteStations - GIVEN a favorite whose station is not cached THEN it is counted as unresolved`() = runTest {
        // The real case: replaceGasStationsByProvince drops stations the API stopped returning, and
        // only the province the user last loaded is cached at all.
        val localDataSource = InMemoryFavoriteStationLocalDataSource(
            favoriteIds = listOf(STATION_ID, OTHER_STATION_ID),
            cachedStations = listOf(GasStationEntityMother.gasStationEntity(id = STATION_ID)),
        )

        FavoriteStationRepositoryImpl(localDataSource).observeFavoriteStations().test {
            val result = awaitItem()
            assertEquals(1, result.stations.size)
            assertEquals(2, result.totalFavoriteCount)
            assertEquals(1, result.unresolvedCount)
            cancelAndIgnoreRemainingEvents()
        }
    }

    /** Models the DAO's `INNER JOIN`: only favourites whose station row exists come back. */
    private class InMemoryFavoriteStationLocalDataSource(
        favoriteIds: List<String> = emptyList(),
        private val cachedStations: List<GasStationEntity> = emptyList(),
    ) : FavoriteStationLocalDataSource {

        private val favorites = MutableStateFlow(favoriteIds)

        override fun observeFavoriteIds(): Flow<List<String>> = favorites

        override fun observeFavoriteStations(): Flow<List<GasStationEntity>> = favorites.map { ids -> cachedStations.filter { it.id in ids } }

        override fun observeFavoriteCount(): Flow<Int> = favorites.map { it.size }

        override suspend fun toggleFavorite(gasStationId: String, addedAt: Long) {
            favorites.update { ids -> if (gasStationId in ids) ids - gasStationId else ids + gasStationId }
        }
    }

    private companion object {
        const val STATION_ID = "1"
        const val OTHER_STATION_ID = "2"
    }
}
