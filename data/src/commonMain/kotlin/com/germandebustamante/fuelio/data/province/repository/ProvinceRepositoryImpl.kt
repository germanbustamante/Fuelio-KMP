package com.germandebustamante.fuelio.data.province.repository

import com.germandebustamante.fuelio.core.domain.province.model.ProvinceBO
import com.germandebustamante.fuelio.core.domain.province.repository.ProvinceRepository
import com.germandebustamante.fuelio.data.province.remote.datasource.ProvinceRemoteDataSource
import com.germandebustamante.fuelio.data.province.remote.model.toDomain
import com.germandebustamante.fuelio.data.util.resultFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn

class ProvinceRepositoryImpl(
    private val remoteDataSource: ProvinceRemoteDataSource,
) : ProvinceRepository {
    override fun getProvinces(): Flow<Result<List<ProvinceBO>>> =
        resultFlow { remoteDataSource.getProvinces().map { it.toDomain() } }.flowOn(Dispatchers.IO)
}
