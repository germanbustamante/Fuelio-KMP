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

    private fun createSut(engine: MockEngine) =
        ProvinceRemoteDataSourceImpl(createHttpClient(engine), BASE_URL)

    @Test
    fun `GIVEN remote server returns provinces WHEN getProvinces THEN return provinces`() = runTest {
        // GIVEN
        val expectedDtos = ProvinceDTOMother.provinceDTOList()
        val sut = createSut(mockEngineWithJson(json.encodeToString(expectedDtos)))

        // WHEN
        val result = sut.getProvinces()

        // THEN
        assertEquals(expectedDtos, result)
    }

    @Test
    fun `GIVEN remote server returns empty list WHEN getProvinces THEN return empty list`() = runTest {
        // GIVEN
        val sut = createSut(mockEngineWithJson(json.encodeToString(emptyList<Nothing>())))

        // WHEN
        val result = sut.getProvinces()

        // THEN
        assertEquals(emptyList(), result)
    }

    @Test
    fun `GIVEN remote server returns error WHEN getProvinces THEN throw DomainError ServerError`() = runTest {
        // GIVEN
        val sut = createSut(mockEngineWithError(HttpStatusCode.InternalServerError))

        // WHEN & THEN
        assertFailsWith<DomainError.ServerError> {
            sut.getProvinces()
        }
    }

    @Test
    fun `GIVEN remote server returns malformed JSON WHEN getProvinces THEN throw DomainError Unknown`() = runTest {
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
