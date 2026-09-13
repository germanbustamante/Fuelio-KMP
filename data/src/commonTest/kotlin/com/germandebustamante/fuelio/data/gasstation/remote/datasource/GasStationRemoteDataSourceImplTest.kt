package com.germandebustamante.fuelio.data.gasstation.remote.datasource

import com.germandebustamante.fuelio.core.domain.error.DomainError
import com.germandebustamante.fuelio.data.gasstation.model.GasStationDTOMother
import com.germandebustamante.fuelio.data.gasstation.remote.model.GasStationResponseDTO
import com.germandebustamante.fuelio.data.util.BaseRemoteDataSourceTest
import io.ktor.client.engine.mock.MockEngine
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class GasStationRemoteDataSourceImplTest : BaseRemoteDataSourceTest() {

    private fun createSut(engine: MockEngine) = GasStationRemoteDataSourceImpl(createHttpClient(engine), BASE_URL)

    @Test
    fun given_remote_server_returns_stations_when_getGasStationsByLocation_then_return_stations() = runTest {
        // GIVEN
        val expectedDtos = listOf(GasStationDTOMother.gasStationDTO())
        val sut = createSut(mockEngineWithJson(json.encodeToString(GasStationResponseDTO(expectedDtos))))

        // WHEN
        val result = sut.getGasStationsByLocation(PROVINCE_ID)

        // THEN
        assertEquals(expectedDtos, result)
    }

    @Test
    fun given_remote_server_returns_empty_list_when_getGasStationsByLocation_then_return_empty_list() = runTest {
        // GIVEN
        val sut = createSut(mockEngineWithJson(json.encodeToString(GasStationResponseDTO(emptyList()))))

        // WHEN
        val result = sut.getGasStationsByLocation(PROVINCE_ID)

        // THEN
        assertEquals(emptyList(), result)
    }

    @Test
    fun given_remote_server_returns_error_when_getGasStationsByLocation_then_throw_DomainError_ServerError() = runTest {
        // GIVEN
        val sut = createSut(mockEngineWithError(HttpStatusCode.InternalServerError))

        // WHEN & THEN
        assertFailsWith<DomainError.ServerError> {
            sut.getGasStationsByLocation(PROVINCE_ID)
        }
    }

    @Test
    fun given_remote_server_returns_malformed_JSON_when_getGasStationsByLocation_then_throw_DomainError_Unknown() = runTest {
        // GIVEN
        val sut = createSut(mockEngineWithJson("not valid json"))

        // WHEN & THEN
        assertFailsWith<DomainError.Unknown> {
            sut.getGasStationsByLocation(PROVINCE_ID)
        }
    }

    companion object {
        private const val BASE_URL = "https://example.com"
        private const val PROVINCE_ID = "28"
    }
}
