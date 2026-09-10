package com.germandebustamante.fuelio.core.domain.preferences.usecase

import com.germandebustamante.fuelio.core.domain.preferences.model.UserPreferencesBO
import com.germandebustamante.fuelio.core.domain.preferences.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow

open class ObserveUserPreferencesUseCase(private val userPreferencesRepository: UserPreferencesRepository) {
    open operator fun invoke(): Flow<UserPreferencesBO> = userPreferencesRepository.observe()
}
