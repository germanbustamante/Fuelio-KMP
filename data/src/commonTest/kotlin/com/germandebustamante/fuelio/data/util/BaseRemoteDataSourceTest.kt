package com.germandebustamante.fuelio.data.util

import com.germandebustamante.fuelio.core.domain.error.DomainError
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

abstract class BaseRemoteDataSourceTest {

    protected val json = Json { ignoreUnknownKeys = true }

    protected fun createHttpClient(engine: MockEngine): HttpClient =
        HttpClient(engine) {
            install(ContentNegotiation) {
                json(json)
            }
            HttpResponseValidator {
                validateResponse { response ->
                    val statusCode = response.status.value
                    if (statusCode in 400..599) {
                        throw DomainError.ServerError(statusCode)
                    }
                }
            }
        }

    protected fun mockEngine(
        content: String,
        status: HttpStatusCode = HttpStatusCode.OK,
        contentType: ContentType = ContentType.Application.Json,
    ): MockEngine = MockEngine { _ ->
        respond(
            content = content,
            status = status,
            headers = headersOf(HttpHeaders.ContentType, contentType.toString())
        )
    }

    protected fun mockEngineWithJson(
        content: String,
        status: HttpStatusCode = HttpStatusCode.OK,
    ): MockEngine = mockEngine(content, status, ContentType.Application.Json)

    protected fun mockEngineWithError(status: HttpStatusCode): MockEngine =
        mockEngine(content = "", status = status)
}
