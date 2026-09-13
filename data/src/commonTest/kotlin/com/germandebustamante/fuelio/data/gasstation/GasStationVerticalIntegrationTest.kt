package com.germandebustamante.fuelio.data.gasstation

import app.cash.turbine.test
import com.germandebustamante.fuelio.core.domain.gasstation.usecase.GetGasStationsByLocationUseCase
import com.germandebustamante.fuelio.data.gasstation.local.datasource.GasStationDAO
import com.germandebustamante.fuelio.data.gasstation.local.datasource.GasStationLocalDataSourceImpl
import com.germandebustamante.fuelio.data.gasstation.local.datasource.PriceHistoryLocalDataSource
import com.germandebustamante.fuelio.data.gasstation.local.mapper.toDomain
import com.germandebustamante.fuelio.data.gasstation.local.model.GasStationEntity
import com.germandebustamante.fuelio.data.gasstation.local.model.GasStationEntityMother
import com.germandebustamante.fuelio.data.gasstation.local.model.PriceSnapshotEntity
import com.germandebustamante.fuelio.data.gasstation.model.GasStationDTOMother
import com.germandebustamante.fuelio.data.gasstation.remote.datasource.GasStationRemoteDataSourceImpl
import com.germandebustamante.fuelio.data.gasstation.remote.model.GasStationResponseDTO
import com.germandebustamante.fuelio.data.gasstation.remote.model.toDomain
import com.germandebustamante.fuelio.data.gasstation.repository.GasStationRepositoryImpl
import com.germandebustamante.fuelio.data.util.BaseRemoteDataSourceTest
import io.ktor.client.engine.mock.MockEngine
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Exercises the full read path — `GetGasStationsByLocationUseCase → GasStationRepositoryImpl →
 * GasStationRemoteDataSourceImpl → MockEngine → parse` — the way the app actually calls it,
 * instead of mocking the local/remote data sources as `GasStationRepositoryImplTest` does.
 *
 * The local side uses [FakeGasStationDAO], an in-memory stand-in for the real Room DAO, rather than
 * a real `FuelioDatabase`. Room's KMP `databaseBuilder` needs a platform file path (Android's
 * `Context.getDatabasePath`, iOS's documents directory — see `FuelioDatabase.android.kt`/
 * `FuelioDatabase.ios.kt`), so a genuinely cross-platform `commonTest` can't stand up a real
 * database without per-platform plumbing this test doesn't need: `GasStationDAO`'s only
 * non-generated logic is `replaceGasStationsByProvince`'s delete-then-insert transaction, which the
 * fake reproduces exactly, and the DDL itself is already pinned by `Migration1To2Test`.
 */
class GasStationVerticalIntegrationTest : BaseRemoteDataSourceTest() {

    private val fakeDao = FakeGasStationDAO()

    private fun createSut(engine: MockEngine): GetGasStationsByLocationUseCase {
        val remoteDataSource = GasStationRemoteDataSourceImpl(createHttpClient(engine), BASE_URL)
        val localDataSource = GasStationLocalDataSourceImpl(fakeDao)
        val repository = GasStationRepositoryImpl(remoteDataSource, localDataSource, PriceHistoryRecorder(NoOpPriceHistoryLocalDataSource))
        return GetGasStationsByLocationUseCase(repository)
    }

    @Test
    fun `GIVEN empty cache AND remote succeeds WHEN invoked THEN emit one fresh success parsed from the network response`() = runTest {
        // GIVEN
        val expectedDto = GasStationDTOMother.gasStationDTO()
        val sut = createSut(mockEngineWithJson(json.encodeToString(GasStationResponseDTO(listOf(expectedDto)))))

        // WHEN
        sut(PROVINCE_ID).test {
            // THEN
            val result = awaitItem()
            assertTrue(result.isSuccess)
            val gasStations = requireNotNull(result.getOrNull())
            assertFalse(gasStations.isFromCache)
            assertEquals(listOf(expectedDto.toDomain()), gasStations.stations)
            awaitComplete()
        }
    }

    @Test
    fun `GIVEN cache has stations AND remote succeeds WHEN invoked THEN emit cached stations first THEN fresh stations from the network`() = runTest {
        // GIVEN
        val cachedEntity = GasStationEntityMother.gasStationEntity(id = CACHED_STATION_ID, provinceId = PROVINCE_ID)
        fakeDao.seed(cachedEntity)
        val freshDto = GasStationDTOMother.gasStationDTO(id = FRESH_STATION_ID)
        val sut = createSut(mockEngineWithJson(json.encodeToString(GasStationResponseDTO(listOf(freshDto)))))

        // WHEN
        sut(PROVINCE_ID).test {
            // THEN
            val cached = awaitItem()
            assertTrue(cached.isSuccess)
            val cachedResult = requireNotNull(cached.getOrNull())
            assertTrue(cachedResult.isFromCache)
            assertEquals(listOf(cachedEntity.toDomain()), cachedResult.stations)

            val fresh = awaitItem()
            assertTrue(fresh.isSuccess)
            val freshResult = requireNotNull(fresh.getOrNull())
            assertFalse(freshResult.isFromCache)
            assertEquals(listOf(freshDto.toDomain()), freshResult.stations)

            awaitComplete()
        }
    }

    @Test
    fun `GIVEN cache has stations AND remote fails WHEN invoked THEN emit cached stations THEN a failure without losing the cache`() = runTest {
        // GIVEN
        val cachedEntity = GasStationEntityMother.gasStationEntity(id = CACHED_STATION_ID, provinceId = PROVINCE_ID)
        fakeDao.seed(cachedEntity)
        val sut = createSut(mockEngineWithError(HttpStatusCode.InternalServerError))

        // WHEN
        sut(PROVINCE_ID).test {
            // THEN
            val cached = awaitItem()
            assertTrue(cached.isSuccess)
            assertTrue(requireNotNull(cached.getOrNull()).isFromCache)

            val failure = awaitItem()
            assertTrue(failure.isFailure)

            awaitComplete()
        }
        assertEquals(listOf(cachedEntity), fakeDao.getGasStationsByProvince(PROVINCE_ID))
    }

    private class FakeGasStationDAO : GasStationDAO {
        private val stationsByProvince = mutableMapOf<String, MutableList<GasStationEntity>>()
        private val stationById = mutableMapOf<String, MutableStateFlow<GasStationEntity?>>()

        fun seed(entity: GasStationEntity) {
            stationsByProvince.getOrPut(entity.provinceId) { mutableListOf() }.add(entity)
        }

        override suspend fun insertGasStations(gasStations: List<GasStationEntity>) {
            gasStations.forEach { entity ->
                stationsByProvince.getOrPut(entity.provinceId) { mutableListOf() }.add(entity)
                stationById.getOrPut(entity.id) { MutableStateFlow(null) }.value = entity
            }
        }

        override suspend fun deleteGasStationsByProvince(provinceId: String) {
            stationsByProvince[provinceId]?.clear()
        }

        override suspend fun getGasStationsByProvince(provinceId: String): List<GasStationEntity> = stationsByProvince[provinceId]?.toList().orEmpty()

        override fun getGasStationById(id: String): Flow<GasStationEntity?> = stationById.getOrPut(id) { MutableStateFlow(null) }.map { it }
    }

    /** Price history isn't this test's concern — a no-op keeps `createSut` from needing a real Room DAO for it. */
    private object NoOpPriceHistoryLocalDataSource : PriceHistoryLocalDataSource {
        override suspend fun getLatest(gasStationId: String): PriceSnapshotEntity? = null
        override suspend fun insert(snapshot: PriceSnapshotEntity) = Unit
        override fun observeHistory(gasStationId: String): Flow<List<PriceSnapshotEntity>> = MutableStateFlow(emptyList())
        override suspend fun deleteOlderThan(cutoffEpochDay: Long) = Unit
    }

    companion object {
        private const val BASE_URL = "https://example.com"
        private const val PROVINCE_ID = "28"
        private const val CACHED_STATION_ID = "cached-1"
        private const val FRESH_STATION_ID = "fresh-1"
    }
}
