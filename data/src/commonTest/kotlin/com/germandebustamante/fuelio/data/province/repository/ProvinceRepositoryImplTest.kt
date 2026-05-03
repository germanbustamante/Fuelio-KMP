package com.germandebustamante.fuelio.data.province.repository

import app.cash.turbine.test
import com.germandebustamante.fuelio.data.province.model.ProvinceDTOMother
import com.germandebustamante.fuelio.data.province.remote.datasource.ProvinceRemoteDataSource
import dev.mokkery.answering.returns
import dev.mokkery.answering.throws
import dev.mokkery.everySuspend
import dev.mokkery.mock
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ProvinceRepositoryImplTest {

    private val remoteDataSource = mock<ProvinceRemoteDataSource>()

    private val sut = ProvinceRepositoryImpl(remoteDataSource)

    @Test
    fun `GIVEN remote data source succeeds WHEN getProvinces THEN emit success with mapped province`() = runTest {
        val expectedDto = ProvinceDTOMother.provinceDTO()
        everySuspend { remoteDataSource.getProvinces() } returns listOf(expectedDto)

        sut.getProvinces().test {
            val result = awaitItem()
            assertTrue(result.isSuccess)
            val provinces = result.getOrNull()
            assertNotNull(provinces)
            assertEquals(1, provinces.size)
            assertEquals(expectedDto.id, provinces.first().id)
            assertEquals(expectedDto.name, provinces.first().name)
            awaitComplete()
        }
    }

    @Test
    fun `GIVEN remote data source returns multiple provinces WHEN getProvinces THEN emit all mapped provinces`() = runTest {
        val dtos = ProvinceDTOMother.provinceDTOList()
        everySuspend { remoteDataSource.getProvinces() } returns dtos

        sut.getProvinces().test {
            val result = awaitItem()
            assertTrue(result.isSuccess)
            val provinces = result.getOrNull()
            assertNotNull(provinces)
            assertEquals(dtos.size, provinces.size)
            assertEquals(dtos[0].name, provinces[0].name)
            assertEquals(dtos[1].name, provinces[1].name)
            awaitComplete()
        }
    }

    @Test
    fun `GIVEN remote data source returns empty list WHEN getProvinces THEN emit success with empty list`() = runTest {
        everySuspend { remoteDataSource.getProvinces() } returns emptyList()

        sut.getProvinces().test {
            val result = awaitItem()
            assertTrue(result.isSuccess)
            assertEquals(emptyList(), result.getOrNull())
            awaitComplete()
        }
    }

    @Test
    fun `GIVEN remote data source throws exception WHEN getProvinces THEN emit failure`() = runTest {
        val expectedException = Exception(ERROR_MESSAGE)
        everySuspend { remoteDataSource.getProvinces() } throws expectedException

        sut.getProvinces().test {
            val result = awaitItem()
            assertTrue(result.isFailure)
            assertEquals(expectedException, result.exceptionOrNull())
            awaitComplete()
        }
    }

    companion object {
        private const val ERROR_MESSAGE = "Network error"
    }
}
