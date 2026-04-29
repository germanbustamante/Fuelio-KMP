package com.germandebustamante.fuelio.data.province.remote.datasource

import com.germandebustamante.fuelio.data.province.remote.model.ProvinceDTO

interface ProvinceRemoteDataSource {
    suspend fun getProvinces(): List<ProvinceDTO>
}
