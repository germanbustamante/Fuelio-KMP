package com.germandebustamante.fuelio.core.domain.province.usecase

import com.germandebustamante.fuelio.core.domain.province.model.ProvinceBO

open class ResolveProvinceByLocationUseCase {
    open operator fun invoke(provinces: List<ProvinceBO>, locationName: String?): ProvinceBO? =
        provinces.firstOrNull { it.name.equals(locationName, ignoreCase = true) } ?: provinces.firstOrNull()
}
