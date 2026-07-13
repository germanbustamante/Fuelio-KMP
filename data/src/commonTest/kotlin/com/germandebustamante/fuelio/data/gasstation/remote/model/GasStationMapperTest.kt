package com.germandebustamante.fuelio.data.gasstation.remote.model

import com.germandebustamante.fuelio.data.gasstation.model.GasStationDTOMother
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GasStationMapperTest {

    @Test
    fun given_dto_with_comma_decimal_coordinates_when_toDomain_then_parses_latitude_and_longitude_correctly() {
        val dto = GasStationDTOMother.gasStationDTO(latitude = "40,4168", longitude = "-3,7038")

        val result = dto.toDomain()

        assertEquals(40.4168, result.latitude)
        assertEquals(-3.7038, result.longitude)
    }

    @Test
    fun given_dto_with_dot_decimal_coordinates_when_toDomain_then_parses_latitude_and_longitude_correctly() {
        val dto = GasStationDTOMother.gasStationDTO(latitude = "40.4168", longitude = "-3.7038")

        val result = dto.toDomain()

        assertEquals(40.4168, result.latitude)
        assertEquals(-3.7038, result.longitude)
    }

    @Test
    fun given_dto_with_unparseable_latitude_when_toDomain_then_defaults_latitude_to_zero() {
        val dto = GasStationDTOMother.gasStationDTO(latitude = "N/D")

        val result = dto.toDomain()

        assertEquals(0.0, result.latitude)
    }

    @Test
    fun given_dto_with_unparseable_longitude_when_toDomain_then_defaults_longitude_to_zero() {
        val dto = GasStationDTOMother.gasStationDTO(longitude = "N/D")

        val result = dto.toDomain()

        assertEquals(0.0, result.longitude)
    }

    @Test
    fun given_dto_with_comma_decimal_prices_when_toDomain_then_parses_all_prices_correctly() {
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
    fun given_dto_with_null_prices_when_toDomain_then_maps_all_prices_to_null() {
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
    fun given_dto_with_unparseable_price_when_toDomain_then_maps_price_to_null_instead_of_zero() {
        val dto = GasStationDTOMother.gasStationDTO(
            gasolinePrice95 = "N/D",
            dieselPrice = "",
        )

        val result = dto.toDomain()

        assertNull(result.gasolinePrice95)
        assertNull(result.dieselPrice)
    }

    @Test
    fun given_dto_with_valid_string_fields_when_toDomain_then_maps_all_string_fields_unchanged() {
        val dto = GasStationDTOMother.gasStationDTO()

        val result = dto.toDomain()

        assertEquals(dto.id, result.id)
        assertEquals(dto.name, result.name)
        assertEquals(dto.address, result.address)
        assertEquals(dto.city, result.city)
        assertEquals(dto.municipality, result.municipality)
        assertEquals(dto.province, result.province)
        assertEquals(dto.zipCode, result.zipCode)
    }

    //region schedule parsing

    @Test
    fun given_schedule_LD_24H_when_parseSchedule_then_single_always_open_segment_all_week() {
        val result = SCHEDULE_ALL_WEEK_24H.parseSchedule()

        assertEquals(1, result.size)
        val segment = result.first()
        assertEquals(DayOfWeek.MONDAY, segment.startDay)
        assertEquals(DayOfWeek.SUNDAY, segment.endDay)
        assertTrue(segment.isAlwaysOpen)
    }

    @Test
    fun given_schedule_LD_with_time_range_when_parseSchedule_then_correct_start_and_end_times() {
        val result = SCHEDULE_ALL_WEEK_07_22.parseSchedule()

        assertEquals(1, result.size)
        val segment = result.first()
        assertEquals(LocalTime(7, 0), segment.startTime)
        assertEquals(LocalTime(22, 0), segment.endTime)
    }

    @Test
    fun given_schedule_with_multiple_segments_when_parseSchedule_then_all_segments_parsed() {
        val result = SCHEDULE_WEEKDAYS_23_WEEKEND_24H.parseSchedule()

        assertEquals(2, result.size)
        assertEquals(DayOfWeek.MONDAY, result[0].startDay)
        assertEquals(DayOfWeek.THURSDAY, result[0].endDay)
        assertFalse(result[0].isAlwaysOpen)
        assertEquals(DayOfWeek.FRIDAY, result[1].startDay)
        assertEquals(DayOfWeek.SUNDAY, result[1].endDay)
        assertTrue(result[1].isAlwaysOpen)
    }

    @Test
    fun given_schedule_with_4_segments_when_parseSchedule_then_all_4_segments_parsed() {
        val result = SCHEDULE_4_SEGMENTS.parseSchedule()

        assertEquals(4, result.size)
        assertEquals(DayOfWeek.FRIDAY, result[1].startDay)
        assertEquals(DayOfWeek.FRIDAY, result[1].endDay)
        assertTrue(result[2].isAlwaysOpen)
    }

    @Test
    fun given_schedule_with_midnight_crossing_time_when_parseSchedule_then_end_time_before_start_time() {
        val result = SCHEDULE_ALL_WEEK_MIDNIGHT_CROSSING.parseSchedule()

        val segment = result.first()
        assertEquals(LocalTime(6, 0), segment.startTime)
        assertEquals(LocalTime(0, 0), segment.endTime)
    }

    @Test
    fun given_schedule_with_single_digit_hour_when_parseSchedule_then_padded_and_parsed_correctly() {
        val result = SCHEDULE_SINGLE_DIGIT_HOUR.parseSchedule()

        assertEquals(LocalTime(7, 0), result.first().startTime)
    }

    @Test
    fun given_malformed_schedule_when_parseSchedule_then_returns_empty_list_without_crashing() {
        val result = SCHEDULE_MALFORMED.parseSchedule()

        assertTrue(result.isEmpty())
    }

    //endregion

    private companion object {
        const val SCHEDULE_ALL_WEEK_24H = "L-D: 24H"
        const val SCHEDULE_ALL_WEEK_07_22 = "L-D: 07:00-22:00"
        const val SCHEDULE_WEEKDAYS_23_WEEKEND_24H = "L-J: 07:00-23:00; V-D: 24H"
        const val SCHEDULE_4_SEGMENTS = "L-J: 06:00-22:00; V: 06:00-00:00; S: 24H; D: 00:00-22:00"
        const val SCHEDULE_ALL_WEEK_MIDNIGHT_CROSSING = "L-D: 06:00-00:00"
        const val SCHEDULE_SINGLE_DIGIT_HOUR = "L-D: 7:00-22:00"
        const val SCHEDULE_MALFORMED = "INVALID_SCHEDULE"
    }
}
