package com.germandebustamante.fuelio.data.preferences.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.germandebustamante.fuelio.core.domain.preferences.model.FuelType
import com.germandebustamante.fuelio.core.domain.preferences.model.ThemeMode
import com.germandebustamante.fuelio.core.domain.preferences.model.UserPreferencesBO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserPreferencesLocalDataSourceImpl(private val dataStore: DataStore<Preferences>) : UserPreferencesLocalDataSource {

    override fun observe(): Flow<UserPreferencesBO> = dataStore.data.map { preferences ->
        UserPreferencesBO(
            defaultFuelType = preferences[KEY_DEFAULT_FUEL_TYPE].toEnumOrDefault(FuelType.GASOLINE_95),
            savedProvinceId = preferences[KEY_SAVED_PROVINCE_ID],
            themeMode = preferences[KEY_THEME_MODE].toEnumOrDefault(ThemeMode.SYSTEM),
        )
    }

    override suspend fun setDefaultFuelType(fuelType: FuelType) {
        dataStore.edit { it[KEY_DEFAULT_FUEL_TYPE] = fuelType.name }
    }

    override suspend fun setSavedProvinceId(provinceId: String) {
        dataStore.edit { it[KEY_SAVED_PROVINCE_ID] = provinceId }
    }

    override suspend fun setThemeMode(themeMode: ThemeMode) {
        dataStore.edit { it[KEY_THEME_MODE] = themeMode.name }
    }

    /**
     * Enums are stored by name, so a constant renamed or removed in a later release would otherwise
     * blow up on read for anyone upgrading. Falling back to the default degrades one preference
     * instead of the whole flow.
     */
    private inline fun <reified T : Enum<T>> String?.toEnumOrDefault(default: T): T =
        enumValues<T>().firstOrNull { it.name == this } ?: default

    private companion object {
        val KEY_DEFAULT_FUEL_TYPE = stringPreferencesKey("default_fuel_type")
        val KEY_SAVED_PROVINCE_ID = stringPreferencesKey("saved_province_id")
        val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
    }
}
