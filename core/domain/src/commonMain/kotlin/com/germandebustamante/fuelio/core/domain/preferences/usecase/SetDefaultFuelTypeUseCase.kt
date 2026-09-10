package com.germandebustamante.fuelio.core.domain.preferences.usecase

import com.germandebustamante.fuelio.core.domain.preferences.model.FuelType
import com.germandebustamante.fuelio.core.domain.preferences.repository.UserPreferencesRepository

open class SetDefaultFuelTypeUseCase(private val userPreferencesRepository: UserPreferencesRepository) {
    open suspend operator fun invoke(fuelType: FuelType) = userPreferencesRepository.setDefaultFuelType(fuelType)
}
