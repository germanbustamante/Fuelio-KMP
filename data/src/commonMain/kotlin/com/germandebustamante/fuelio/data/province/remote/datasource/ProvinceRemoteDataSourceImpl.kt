package com.germandebustamante.fuelio.data.province.remote.datasource

import com.germandebustamante.fuelio.data.province.remote.model.ProvinceDTO
import com.germandebustamante.fuelio.data.util.safeApiCall
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

class ProvinceRemoteDataSourceImpl(
    private val httpClient: HttpClient,
    private val baseUrl: String,
) : ProvinceRemoteDataSource {

    override suspend fun getProvinces(): List<ProvinceDTO> =
        safeApiCall { httpClient.get("$baseUrl/ServiciosRESTCarburantes/PreciosCarburantes/Listados/Provincias/").body() }
}
