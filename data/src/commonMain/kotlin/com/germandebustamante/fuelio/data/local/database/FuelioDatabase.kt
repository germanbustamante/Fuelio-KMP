package com.germandebustamante.fuelio.data.local.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.TypeConverters
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.germandebustamante.fuelio.data.di.ContextProvider
import com.germandebustamante.fuelio.data.gasstation.local.datasource.GasStationDAO
import com.germandebustamante.fuelio.data.gasstation.local.model.GasStationEntity
import com.germandebustamante.fuelio.data.local.typeconverter.DayOfWeekConverter
import com.germandebustamante.fuelio.data.local.typeconverter.LocalTimeConverter
import com.germandebustamante.fuelio.data.local.typeconverter.ScheduleSegmentListConverter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

@Database(entities = [GasStationEntity::class], version = 1)
@TypeConverters(DayOfWeekConverter::class, LocalTimeConverter::class, ScheduleSegmentListConverter::class)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class FuelioDatabase : RoomDatabase() {
    abstract fun gasStationDao(): GasStationDAO
}

@Suppress("KotlinNoActualForExpect")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<FuelioDatabase> {
    override fun initialize(): FuelioDatabase
}

fun getRoomDatabase(
    builder: RoomDatabase.Builder<FuelioDatabase>
): FuelioDatabase = builder
    .setDriver(BundledSQLiteDriver())
    .setQueryCoroutineContext(Dispatchers.IO)
    .build()

expect fun getDatabaseBuilder(contextProvider: ContextProvider): RoomDatabase.Builder<FuelioDatabase>