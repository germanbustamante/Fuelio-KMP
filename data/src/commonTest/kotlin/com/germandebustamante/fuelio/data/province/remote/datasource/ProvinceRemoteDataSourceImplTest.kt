package com.germandebustamante.fuelio.data.province.remote.datasource

import com.germandebustamante.fuelio.core.domain.error.DomainError
import com.germandebustamante.fuelio.data.province.model.ProvinceDTOMother
import com.germandebustamante.fuelio.data.util.BaseRemoteDataSourceTest
import io.ktor.client.engine.mock.MockEngine
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ProvinceRemoteDataSourceImplTest : BaseRemoteDataSourceTest() {

    private fun createSut(engine: MockEngine) = ProvinceRemoteDataSourceImpl(createHttpClient(engine), BASE_URL)

    @Test
    fun given_remote_server_returns_provinces_when_getProvinces_then_return_provinces() = runTest {
        // GIVEN
        val expectedDtos = ProvinceDTOMother.provinceDTOList()
        val sut = createSut(mockEngineWithJson(json.encodeToString(expectedDtos)))

        // WHEN
        val result = sut.getProvinces()

        // THEN
        assertEquals(expectedDtos, result)
    }

    @Test
    fun given_remote_server_returns_empty_list_when_getProvinces_then_return_empty_list() = runTest {
        // GIVEN
        val sut = createSut(mockEngineWithJson(json.encodeToString(emptyList<Nothing>())))

        // WHEN
        val result = sut.getProvinces()

        // THEN
        assertEquals(emptyList(), result)
    }

    @Test
    fun given_remote_server_returns_error_when_getProvinces_then_throw_DomainError_ServerError() = runTest {
        // GIVEN
        val sut = createSut(mockEngineWithError(HttpStatusCode.InternalServerError))

        // WHEN & THEN
        assertFailsWith<DomainError.ServerError> {
            sut.getProvinces()
        }
    }

    @Test
    fun given_remote_server_returns_malformed_JSON_when_getProvinces_then_throw_DomainError_Unknown() = runTest {
        // GIVEN
        val sut = createSut(mockEngineWithJson("not valid json"))

        // WHEN & THEN
        assertFailsWith<DomainError.Unknown> {
            sut.getProvinces()
        }
    }

    companion object {
        private const val BASE_URL = "https://example.com"
    }
}
