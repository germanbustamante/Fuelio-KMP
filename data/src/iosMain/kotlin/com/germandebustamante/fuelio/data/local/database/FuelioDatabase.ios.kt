package com.germandebustamante.fuelio.data.local.database

import androidx.room.Room
import androidx.room.RoomDatabase
import com.germandebustamante.fuelio.data.di.ContextProvider
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

actual fun getDatabaseBuilder(contextProvider: ContextProvider): RoomDatabase.Builder<FuelioDatabase> {
    val dbFilePath = documentDirectory() + "/fuelio.db"
    return Room.databaseBuilder<FuelioDatabase>(
        name = dbFilePath,
    )
}

@OptIn(ExperimentalForeignApi::class)
private fun documentDirectory(): String {
    val documentDirectory = NSFileManager.defaultManager.URLForDirectory(
        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = false,
        error = null,
    )
    return requireNotNull(documentDirectory?.path)
}
