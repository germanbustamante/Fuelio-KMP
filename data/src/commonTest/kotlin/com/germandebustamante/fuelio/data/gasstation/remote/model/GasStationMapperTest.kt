package com.germandebustamante.fuelio.data.gasstation.remote.model

import com.germandebustamante.fuelio.data.gasstation.model.GasStationDTOMother
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class GasStationMapperTest {

    @Test
    fun `GIVEN dto with comma-decimal coordinates WHEN toDomain THEN parses latitude and longitude correctly`() {
        val dto = GasStationDTOMother.gasStationDTO(latitude = "40,4168", longitude = "-3,7038")

        val result = dto.toDomain()

        assertEquals(40.4168, result.latitude)
        assertEquals(-3.7038, result.longitude)
    }

    @Test
    fun `GIVEN dto with dot-decimal coordinates WHEN toDomain THEN parses latitude and longitude correctly`() {
        val dto = GasStationDTOMother.gasStationDTO(latitude = "40.4168", longitude = "-3.7038")

        val result = dto.toDomain()

        assertEquals(40.4168, result.latitude)
        assertEquals(-3.7038, result.longitude)
    }

    @Test
    fun `GIVEN dto with unparseable latitude WHEN toDomain THEN defaults latitude to 0,0`() {
        val dto = GasStationDTOMother.gasStationDTO(latitude = "N/D")

        val result = dto.toDomain()

        assertEquals(0.0, result.latitude)
    }

    @Test
    fun `GIVEN dto with unparseable longitude WHEN toDomain THEN defaults longitude to 0,0`() {
        val dto = GasStationDTOMother.gasStationDTO(longitude = "N/D")

        val result = dto.toDomain()

        assertEquals(0.0, result.longitude)
    }

    @Test
    fun `GIVEN dto with comma-decimal prices WHEN toDomain THEN parses all prices correctly`() {
        val dto = GasStationDTOMother.gasStationDTO(
            gasolinePrice95 = "1,759",
            gasolinePrice98 = "1,889",
            dieselPrice = "1,659",
            dieselPremiumPrice = "1,729",
        )

        val result = dto.toDomain()

        assertEquals(1.759, result.gasolinePrice95)
        assertEquals(1.889, result.gasolinePrice98)
        assertEquals(1.659, result.dieselPrice)
        assertEquals(1.729, result.dieselPremiumPrice)
    }

    @Test
    fun `GIVEN dto with null prices WHEN toDomain THEN maps all prices to null`() {
        val dto = GasStationDTOMother.gasStationDTO(
            gasolinePrice95 = null,
            gasolinePrice98 = null,
            dieselPrice = null,
            dieselPremiumPrice = null,
        )

        val result = dto.toDomain()

        assertNull(result.gasolinePrice95)
        assertNull(result.gasolinePrice98)
        assertNull(result.dieselPrice)
        assertNull(result.dieselPremiumPrice)
    }

    @Test
    fun `GIVEN dto with unparseable price WHEN toDomain THEN maps price to null instead of 0,0`() {
        val dto = GasStationDTOMother.gasStationDTO(
            gasolinePrice95 = "N/D",
            dieselPrice = "",
        )

        val result = dto.toDomain()

        assertNull(result.gasolinePrice95)
        assertNull(result.dieselPrice)
    }

    @Test
    fun `GIVEN dto with valid string fields WHEN toDomain THEN maps all string fields unchanged`() {
        val dto = GasStationDTOMother.gasStationDTO()

        val result = dto.toDomain()

        assertEquals(dto.id, result.id)
        assertEquals(dto.name, result.name)
        assertEquals(dto.address, result.address)
        assertEquals(dto.city, result.city)
        assertEquals(dto.municipality, result.municipality)
        assertEquals(dto.province, result.province)
        assertEquals(dto.zipCode, result.zipCode)
        assertEquals(dto.schedule, result.schedule)
    }
}
