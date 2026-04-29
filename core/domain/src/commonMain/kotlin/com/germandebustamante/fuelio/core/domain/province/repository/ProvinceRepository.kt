package com.germandebustamante.fuelio.core.domain.province.repository

import com.germandebustamante.fuelio.core.domain.province.model.ProvinceBO
import kotlinx.coroutines.flow.Flow

interface ProvinceRepository {
    fun getProvinces(): Flow<Result<List<ProvinceBO>>>
}
