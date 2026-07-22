package com.germandebustamante.fuelio.data.local.database

import androidx.room.Room
import androidx.room.RoomDatabase
import com.germandebustamante.fuelio.data.di.ContextProvider

actual fun getDatabaseBuilder(contextProvider: ContextProvider): RoomDatabase.Builder<FuelioDatabase> {
    val appContext = contextProvider.getAndroidContext().applicationContext
    val dbFile = appContext.getDatabasePath("fuelio.db")
    return Room.databaseBuilder<FuelioDatabase>(
        context = appContext,
        name = dbFile.absolutePath
    )
}