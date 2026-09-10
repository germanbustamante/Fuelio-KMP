package com.germandebustamante.fuelio.data.local.typeconverter

import androidx.room.TypeConverter
import com.germandebustamante.fuelio.data.gasstation.local.model.ScheduleSegmentEntity
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime

private const val SEGMENT_SEPARATOR = ";"
private const val FIELD_SEPARATOR = "|"

object ScheduleSegmentListConverter {
    @TypeConverter
    fun toSchedule(value: String): List<ScheduleSegmentEntity> {
        if (value.isEmpty()) return emptyList()
        return value.split(SEGMENT_SEPARATOR).map { segment ->
            val fields = segment.split(FIELD_SEPARATOR)
            ScheduleSegmentEntity(
                startDay = DayOfWeek.valueOf(fields[0]),
                endDay = DayOfWeek.valueOf(fields[1]),
                startTime = fields[2].ifEmpty { null }?.let { LocalTime.parse(it) },
                endTime = fields[3].ifEmpty { null }?.let { LocalTime.parse(it) },
            )
        }
    }

    @TypeConverter
    fun toScheduleString(value: List<ScheduleSegmentEntity>): String = value.joinToString(SEGMENT_SEPARATOR) { segment ->
        listOf(
            segment.startDay.name,
            segment.endDay.name,
            segment.startTime?.toString().orEmpty(),
            segment.endTime?.toString().orEmpty(),
        ).joinToString(FIELD_SEPARATOR)
    }
}
