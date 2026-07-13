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
    fun given_remote_data_source_succeeds_when_getProvinces_then_emit_success_with_mapped_province() = runTest {
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
    fun given_remote_data_source_returns_multiple_provinces_when_getProvinces_then_emit_all_mapped_provinces() = runTest {
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
    fun given_remote_data_source_returns_empty_list_when_getProvinces_then_emit_success_with_empty_list() = runTest {
        everySuspend { remoteDataSource.getProvinces() } returns emptyList()

        sut.getProvinces().test {
            val result = awaitItem()
            assertTrue(result.isSuccess)
            assertEquals(emptyList(), result.getOrNull())
            awaitComplete()
        }
    }

    @Test
    fun given_remote_data_source_throws_exception_when_getProvinces_then_emit_failure() = runTest {
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
