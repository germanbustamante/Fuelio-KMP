package com.germandebustamante.fuelio.core.domain.province.testing

import com.germandebustamante.fuelio.core.domain.province.model.ProvinceBO

object ProvinceBOMother {
    fun provinceBO(
        id: String = "1",
        name: String = "Madrid"
    ) = ProvinceBO(
        id = id,
        name = name
    )

    fun provinceBOList() = listOf(
        provinceBO(),
        provinceBO(id = "2", name = "Barcelona")
    )
}
