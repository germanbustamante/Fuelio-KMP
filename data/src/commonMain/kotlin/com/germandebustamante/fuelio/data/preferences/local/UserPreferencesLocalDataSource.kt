package com.germandebustamante.fuelio.data.preferences.local

import com.germandebustamante.fuelio.core.domain.preferences.model.FuelType
import com.germandebustamante.fuelio.core.domain.preferences.model.ThemeMode
import com.germandebustamante.fuelio.core.domain.preferences.model.UserPreferencesBO
import kotlinx.coroutines.flow.Flow

interface UserPreferencesLocalDataSource {

    fun observe(): Flow<UserPreferencesBO>

    suspend fun setDefaultFuelType(fuelType: FuelType)

    suspend fun setSavedProvinceId(provinceId: String)

    suspend fun setThemeMode(themeMode: ThemeMode)
}
