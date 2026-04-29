package com.germandebustamante.fuelio.core.domain.province.usecase

import com.germandebustamante.fuelio.core.domain.province.model.ProvinceBO
import com.germandebustamante.fuelio.core.domain.province.repository.ProvinceRepository
import kotlinx.coroutines.flow.Flow

class GetProvincesUseCase(private val provinceRepository: ProvinceRepository) {
    operator fun invoke(): Flow<Result<List<ProvinceBO>>> = provinceRepository.getProvinces()
}
