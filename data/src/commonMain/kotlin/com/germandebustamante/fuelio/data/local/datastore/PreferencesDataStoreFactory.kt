package com.germandebustamante.fuelio.data.local.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.germandebustamante.fuelio.data.di.ContextProvider
import okio.Path.Companion.toPath

/**
 * The `.preferences_pb` suffix is not decoration — `PreferenceDataStoreFactory` rejects any other
 * extension.
 */
internal const val PREFERENCES_FILE_NAME = "fuelio.preferences_pb"

fun createPreferencesDataStore(producePath: () -> String): DataStore<Preferences> =
    PreferenceDataStoreFactory.createWithPath { producePath().toPath() }

/**
 * Where the preferences file lives, per platform.
 *
 * Takes the existing [ContextProvider] rather than introducing another platform module, exactly like
 * `getDatabaseBuilder` does for Room — this is the seam CLAUDE.md reserves for "a class that needs
 * platform context".
 */
expect fun preferencesPath(contextProvider: ContextProvider): String
