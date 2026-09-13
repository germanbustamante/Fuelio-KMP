package com.germandebustamante.fuelio.core.domain.preferences.usecase

import com.germandebustamante.fuelio.core.domain.preferences.repository.UserPreferencesRepository

open class SetHasCompletedOnboardingUseCase(private val userPreferencesRepository: UserPreferencesRepository) {
    open suspend operator fun invoke(completed: Boolean) = userPreferencesRepository.setHasCompletedOnboarding(completed)
}
