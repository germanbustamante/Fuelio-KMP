package com.germandebustamante.fuelio.feature.list.ui

import com.germandebustamante.fuelio.core.domain.gasstation.model.GasStationBrand
import kotlin.test.Test
import kotlin.test.assertNotNull

class GasStationBrandLogoTest {

    @Test
    fun `GIVEN every known brand THEN a drawable resource is mapped`() {
        GasStationBrand.entries.forEach { brand ->
            assertNotNull(brand.toDrawableResource())
        }
    }
}
