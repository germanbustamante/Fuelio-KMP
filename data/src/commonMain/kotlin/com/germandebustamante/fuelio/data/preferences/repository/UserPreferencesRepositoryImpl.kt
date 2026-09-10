package com.germandebustamante.fuelio.data.preferences.repository

import com.germandebustamante.fuelio.core.domain.preferences.model.FuelType
import com.germandebustamante.fuelio.core.domain.preferences.model.ThemeMode
import com.germandebustamante.fuelio.core.domain.preferences.model.UserPreferencesBO
import com.germandebustamante.fuelio.core.domain.preferences.repository.UserPreferencesRepository
import com.germandebustamante.fuelio.data.preferences.local.UserPreferencesLocalDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class UserPreferencesRepositoryImpl(private val localDataSource: UserPreferencesLocalDataSource) : UserPreferencesRepository {

    override fun observe(): Flow<UserPreferencesBO> = localDataSource.observe().flowOn(Dispatchers.IO)

    override suspend fun setDefaultFuelType(fuelType: FuelType) = withContext(Dispatchers.IO) {
        localDataSource.setDefaultFuelType(fuelType)
    }

    override suspend fun setSavedProvinceId(provinceId: String) = withContext(Dispatchers.IO) {
        localDataSource.setSavedProvinceId(provinceId)
    }

    override suspend fun setThemeMode(themeMode: ThemeMode) = withContext(Dispatchers.IO) {
        localDataSource.setThemeMode(themeMode)
    }
}
