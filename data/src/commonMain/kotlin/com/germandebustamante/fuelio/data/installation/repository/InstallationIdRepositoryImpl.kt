package com.germandebustamante.fuelio.data.installation.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.germandebustamante.fuelio.core.domain.installation.repository.InstallationIdRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Reuses the same preferences `DataStore` as [com.germandebustamante.fuelio.data.preferences.local.UserPreferencesLocalDataSourceImpl]
 * rather than a second file — one more `PreferenceDataStoreFactory.createWithPath` instance would
 * throw, since the factory enforces a single active instance per file.
 */
class InstallationIdRepositoryImpl(private val dataStore: DataStore<Preferences>) : InstallationIdRepository {

    // The read-and-maybe-write happens inside a single `edit` transform, which DataStore serializes,
    // so two concurrent callers can never generate and persist two different ids.
    @OptIn(ExperimentalUuidApi::class)
    override suspend fun getOrCreate(): String = withContext(Dispatchers.IO) {
        var installationId = ""
        dataStore.edit { preferences ->
            installationId = preferences[KEY_INSTALLATION_ID] ?: Uuid.random().toString().also {
                preferences[KEY_INSTALLATION_ID] = it
            }
        }
        installationId
    }

    private companion object {
        val KEY_INSTALLATION_ID = stringPreferencesKey("installation_id")
    }
}
