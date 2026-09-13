package com.germandebustamante.fuelio.data.installation.repository

import com.germandebustamante.fuelio.data.local.datastore.createPreferencesDataStore
import kotlinx.coroutines.test.runTest
import okio.FileSystem
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

/**
 * Exercises the real DataStore, not a fake — same reasoning as `UserPreferencesLocalDataSourceImplTest`:
 * each test gets its own file, since `PreferenceDataStoreFactory` permits only one live instance per path.
 */
class InstallationIdRepositoryImplTest {

    @Test
    fun `getOrCreate - GIVEN an empty store WHEN called THEN a new id is generated and persisted`() = runTest {
        val dataStore = temporaryDataStore()
        val sut = InstallationIdRepositoryImpl(dataStore)

        val firstRead = sut.getOrCreate()
        val secondRead = sut.getOrCreate()

        assertEquals(firstRead, secondRead)
    }

    @Test
    fun `getOrCreate - GIVEN a persisted id WHEN a new repository instance reads it THEN the same id is returned`() = runTest {
        val dataStore = temporaryDataStore()
        val existingId = InstallationIdRepositoryImpl(dataStore).getOrCreate()

        val idFromNewInstance = InstallationIdRepositoryImpl(dataStore).getOrCreate()

        assertEquals(existingId, idFromNewInstance)
    }

    @Test
    fun `getOrCreate - GIVEN two independent stores THEN each generates its own id`() = runTest {
        val firstId = InstallationIdRepositoryImpl(temporaryDataStore()).getOrCreate()
        val secondId = InstallationIdRepositoryImpl(temporaryDataStore()).getOrCreate()

        assertNotEquals(firstId, secondId)
    }

    private fun temporaryDataStore() = createPreferencesDataStore {
        val directory = FileSystem.SYSTEM_TEMPORARY_DIRECTORY / "fuelio-installation-${Random.nextLong()}"
        FileSystem.SYSTEM.createDirectories(directory)
        (directory / "test.preferences_pb").toString()
    }
}
