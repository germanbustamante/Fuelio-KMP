package com.germandebustamante.fuelio.core.domain.province.usecase

import com.germandebustamante.fuelio.core.domain.province.testing.ProvinceBOMother
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ResolveProvinceByLocationUseCaseTest {

    private val sut = ResolveProvinceByLocationUseCase()

    //region Null or empty location

    @Test
    fun `invoke - GIVEN null location WHEN provinces not empty THEN returns first province`() {
        val provinces = ProvinceBOMother.provinceBOList()

        val result = sut(provinces, null)

        assertEquals(provinces.first(), result)
    }

    @Test
    fun `invoke - GIVEN empty location WHEN provinces not empty THEN returns first province`() {
        val provinces = ProvinceBOMother.provinceBOList()

        val result = sut(provinces, "")

        assertEquals(provinces.first(), result)
    }

    @Test
    fun `invoke - GIVEN null location WHEN provinces empty THEN returns null`() {
        val result = sut(emptyList(), null)

        assertNull(result)
    }

    //endregion

    //region Exact match

    @Test
    fun `invoke - GIVEN location matches first province WHEN called THEN returns that province`() {
        val provinces = ProvinceBOMother.provinceBOList()
        val target = provinces.first()

        val result = sut(provinces, target.name)

        assertEquals(target, result)
    }

    @Test
    fun `invoke - GIVEN location matches non-first province WHEN called THEN returns matching province`() {
        val provinces = ProvinceBOMother.provinceBOList()
        val target = provinces[1]

        val result = sut(provinces, target.name)

        assertEquals(target, result)
    }

    //endregion

    //region Case-insensitive match

    @Test
    fun `invoke - GIVEN location matches province in uppercase WHEN called THEN returns matching province`() {
        val provinces = ProvinceBOMother.provinceBOList()
        val target = provinces[1]

        val result = sut(provinces, target.name.uppercase())

        assertEquals(target, result)
    }

    @Test
    fun `invoke - GIVEN location matches province in lowercase WHEN called THEN returns matching province`() {
        val provinces = ProvinceBOMother.provinceBOList()
        val target = provinces[1]

        val result = sut(provinces, target.name.lowercase())

        assertEquals(target, result)
    }

    @Test
    fun `invoke - GIVEN location matches province in mixed case WHEN called THEN returns matching province`() {
        val provinces = ProvinceBOMother.provinceBOList()
        val target = provinces[1]
        val mixedCase = target.name.mapIndexed { i, c -> if (i % 2 == 0) c.uppercaseChar() else c.lowercaseChar() }.joinToString("")

        val result = sut(provinces, mixedCase)

        assertEquals(target, result)
    }

    //endregion

    //region No match

    @Test
    fun `invoke - GIVEN location does not match any province WHEN provinces not empty THEN returns first province`() {
        val provinces = ProvinceBOMother.provinceBOList()

        val result = sut(provinces, "Tokio")

        assertEquals(provinces.first(), result)
    }

    @Test
    fun `invoke - GIVEN location does not match any province WHEN provinces empty THEN returns null`() {
        val result = sut(emptyList(), "Tokio")

        assertNull(result)
    }

    //endregion
}
