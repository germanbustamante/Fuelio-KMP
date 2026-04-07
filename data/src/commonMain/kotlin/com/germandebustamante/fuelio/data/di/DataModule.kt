package com.germandebustamante.fuelio.data.di

import com.germandebustamante.fuelio.core.domain.error.DomainError
import com.germandebustamante.fuelio.core.domain.gasstation.repository.GasStationRepository
import com.germandebustamante.fuelio.data.engine.httpClientEngine
import com.germandebustamante.fuelio.data.gasstation.remote.datasource.GasStationRemoteDataSource
import com.germandebustamante.fuelio.data.gasstation.remote.datasource.GasStationRemoteDataSourceImpl
import com.germandebustamante.fuelio.data.gasstation.repository.GasStationRepositoryImpl
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.dsl.bind
import org.koin.dsl.module

private const val BASE_URL = "https://sedeaplicaciones.minetur.gob.es"

val dataModule = module {
    single { GasStationRepositoryImpl(get()) } bind GasStationRepository::class
    single { GasStationRemoteDataSourceImpl(get(), BASE_URL) } bind GasStationRemoteDataSource::class
    single {
        HttpClient(httpClientEngine()) {
            install(ContentNegotiation) {
                json(json = Json { ignoreUnknownKeys = true })
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
    } bind HttpClient::class
}
