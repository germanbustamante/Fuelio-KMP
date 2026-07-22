package com.germandebustamante.fuelio.data.gasstation.repository

import app.cash.turbine.test
import com.germandebustamante.fuelio.data.gasstation.local.datasource.GasStationLocalDataSource
import com.germandebustamante.fuelio.data.gasstation.local.mapper.toDomain
import com.germandebustamante.fuelio.data.gasstation.local.mapper.toEntity
import com.germandebustamante.fuelio.data.gasstation.local.model.GasStationEntity
import com.germandebustamante.fuelio.data.gasstation.local.model.GasStationEntityMother
import com.germandebustamante.fuelio.data.gasstation.model.GasStationDTOMother
import com.germandebustamante.fuelio.data.gasstation.remote.datasource.GasStationRemoteDataSource
import dev.mokkery.answering.returns
import dev.mokkery.answering.throws
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import com.germandebustamante.fuelio.data.gasstation.remote.model.toDomain as dtoToDomain

class GasStationRepositoryImplTest {

    private val remoteDataSource: GasStationRemoteDataSource = mock()

    private val localDataSource: GasStationLocalDataSource = mock()

    private val sut: GasStationRepositoryImpl = GasStationRepositoryImpl(remoteDataSource, localDataSource)

    //region getGasStationsByLocation
    @Test
    fun `getGasStationsByLocation - GIVEN local cache is empty AND remote succeeds WHEN called THEN emit success once with fresh stations`() = runTest {
        //GIVEN
        givenLocalGasStationsEmpty()
        givenRemoteGetGasStationsByLocationSuccess()

        //WHEN
        sut.getGasStationsByLocation(PROVINCE_ID).test {
            //THEN
            val result = awaitItem()
            assertTrue(result.isSuccess)
            val gasStations = result.getOrNull()
            assertNotNull(gasStations)
            assertEquals(1, gasStations.stations.size)
            assertFalse(gasStations.isFromCache)
            awaitComplete()
        }
    }

    @Test
    fun `getGasStationsByLocation - GIVEN local cache is empty AND remote fails WHEN called THEN emit failure once`() = runTest {
        //GIVEN
        givenLocalGasStationsEmpty()
        givenRemoteGetGasStationsByLocationFailure()

        //WHEN
        sut.getGasStationsByLocation(PROVINCE_ID).test {
            //THEN
            val result = awaitItem()
            assertTrue(result.isFailure)
            awaitComplete()
        }
    }

    @Test
    fun `getGasStationsByLocation - GIVEN local cache has stations AND remote succeeds WHEN called THEN emit cached stations first then fresh stations`() = runTest {
        //GIVEN
        val cachedEntity = GasStationEntityMother.gasStationEntity(id = CACHED_STATION_ID)
        givenLocalGasStations(listOf(cachedEntity))
        givenRemoteGetGasStationsByLocationSuccess()

        //WHEN
        sut.getGasStationsByLocation(PROVINCE_ID).test {
            //THEN
            val cachedResult = awaitItem()
            assertTrue(cachedResult.isSuccess)
            val cachedGasStations = cachedResult.getOrNull()
            assertNotNull(cachedGasStations)
            assertTrue(cachedGasStations.isFromCache)
            assertEquals(listOf(cachedEntity.toDomain()), cachedGasStations.stations)

            val freshResult = awaitItem()
            assertTrue(freshResult.isSuccess)
            val freshGasStations = freshResult.getOrNull()
            assertNotNull(freshGasStations)
            assertFalse(freshGasStations.isFromCache)
            assertEquals(1, freshGasStations.stations.size)

            awaitComplete()
        }
    }

    @Test
    fun `getGasStationsByLocation - GIVEN local cache has stations AND remote fails WHEN called THEN emit cached stations success then failure`() = runTest {
        //GIVEN
        val cachedEntity = GasStationEntityMother.gasStationEntity(id = CACHED_STATION_ID)
        givenLocalGasStations(listOf(cachedEntity))
        givenRemoteGetGasStationsByLocationFailure()

        //WHEN
        sut.getGasStationsByLocation(PROVINCE_ID).test {
            //THEN
            val cachedResult = awaitItem()
            assertTrue(cachedResult.isSuccess)
            assertEquals(true, cachedResult.getOrNull()?.isFromCache)

            val failureResult = awaitItem()
            assertTrue(failureResult.isFailure)

            awaitComplete()
        }
    }

    @Test
    fun `getGasStationsByLocation - GIVEN remote succeeds WHEN called THEN local cache is updated with stations mapped to the requested province`() = runTest {
        //GIVEN
        givenLocalGasStationsEmpty()
        givenRemoteGetGasStationsByLocationSuccess()
        val expectedEntities = listOf(GasStationDTOMother.gasStationDTO().dtoToDomain().toEntity(PROVINCE_ID))

        //WHEN
        sut.getGasStationsByLocation(PROVINCE_ID).test {
            awaitItem()
            awaitComplete()
        }

        //THEN
        verifySuspend { localDataSource.insertGasStations(expectedEntities) }
    }
    //region Stubs
    private fun givenLocalGasStationsEmpty() {
        everySuspend { localDataSource.getGasStationsByProvince(any()) } returns emptyList()
    }

    private fun givenLocalGasStations(entities: List<GasStationEntity>) {
        everySuspend { localDataSource.getGasStationsByProvince(any()) } returns entities
    }

    private fun givenRemoteGetGasStationsByLocationSuccess() {
        everySuspend { remoteDataSource.getGasStationsByLocation(any()) } returns listOf(GasStationDTOMother.gasStationDTO())
        everySuspend { localDataSource.insertGasStations(any()) } returns Unit
    }

    private fun givenRemoteGetGasStationsByLocationFailure() {
        everySuspend { remoteDataSource.getGasStationsByLocation(any()) } throws Exception()
    }
    //endregion
    //endregion

    //region getGasStationById
    @Test
    fun `getGasStationById - GIVEN local emits a matching entity WHEN called THEN emit mapped domain station`() = runTest {
        //GIVEN
        val entity = GasStationEntityMother.gasStationEntity(id = STATION_ID)
        every { localDataSource.getGasStationById(STATION_ID) } returns flowOf(entity)

        //WHEN
        sut.getGasStationById(STATION_ID).test {
            //THEN
            assertEquals(entity.toDomain(), awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `getGasStationById - GIVEN local has no matching entity WHEN called THEN emit null`() = runTest {
        //GIVEN
        every { localDataSource.getGasStationById(STATION_ID) } returns flowOf(null)

        //WHEN
        sut.getGasStationById(STATION_ID).test {
            //THEN
            assertNull(awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `getGasStationById - GIVEN local emits several updates over time WHEN called THEN emit each mapped value in order`() = runTest {
        //GIVEN
        val firstEntity = GasStationEntityMother.gasStationEntity(id = STATION_ID, gasolinePrice95 = 1.5)
        val updatedEntity = GasStationEntityMother.gasStationEntity(id = STATION_ID, gasolinePrice95 = 1.6)
        every { localDataSource.getGasStationById(STATION_ID) } returns flowOf(firstEntity, updatedEntity)

        //WHEN
        sut.getGasStationById(STATION_ID).test {
            //THEN
            assertEquals(firstEntity.toDomain(), awaitItem())
            assertEquals(updatedEntity.toDomain(), awaitItem())
            awaitComplete()
        }
    }
    //endregion

    companion object {
        private const val PROVINCE_ID = "1"
        private const val CACHED_STATION_ID = "cached-1"
        private const val STATION_ID = "station-1"
    }
}
