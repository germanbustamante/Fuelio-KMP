package com.germandebustamante.fuelio.core.domain.preferences.usecase

import com.germandebustamante.fuelio.core.domain.preferences.repository.UserPreferencesRepository

open class SetSavedProvinceUseCase(private val userPreferencesRepository: UserPreferencesRepository) {
    open suspend operator fun invoke(provinceId: String) = userPreferencesRepository.setSavedProvinceId(provinceId)
}
