package com.germandebustamante.fuelio.data.gasstation.remote.datasource

import com.germandebustamante.fuelio.data.gasstation.remote.model.GasStationDTO
import com.germandebustamante.fuelio.data.gasstation.remote.model.GasStationResponseDTO
import com.germandebustamante.fuelio.data.util.safeApiCall
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

class GasStationRemoteDataSourceImpl(
    private val httpClient: HttpClient,
    private val baseUrl: String,
) : GasStationRemoteDataSource {

    override suspend fun getGasStationsByLocation(provinceId: String): List<GasStationDTO> =
        safeApiCall { fetchData<GasStationResponseDTO>("/ServiciosRESTCarburantes/PreciosCarburantes/EstacionesTerrestres/FiltroProvincia/$provinceId").stations }

    private suspend inline fun <reified T> fetchData(endpoint: String): T =
        httpClient.get("$baseUrl/$endpoint").body()

}
