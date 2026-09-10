package com.germandebustamante.fuelio.core.domain.preferences.repository

import com.germandebustamante.fuelio.core.domain.preferences.model.FuelType
import com.germandebustamante.fuelio.core.domain.preferences.model.ThemeMode
import com.germandebustamante.fuelio.core.domain.preferences.model.UserPreferencesBO
import kotlinx.coroutines.flow.Flow

/**
 * Reads and writes the user's persisted choices.
 *
 * [observe] returns a `Flow` rather than a suspend read on purpose: a change made on the settings
 * screen has to reach the list screen and the app's theme without anyone re-querying, exactly the
 * way `GasStationRepository.getGasStationById` is backed by Room's own Flow.
 *
 * There is no `Result` here: reading a preference has no network path and a missing value is a
 * business default, not a failure.
 */
interface UserPreferencesRepository {

    fun observe(): Flow<UserPreferencesBO>

    suspend fun setDefaultFuelType(fuelType: FuelType)

    suspend fun setSavedProvinceId(provinceId: String)

    suspend fun setThemeMode(themeMode: ThemeMode)
}
