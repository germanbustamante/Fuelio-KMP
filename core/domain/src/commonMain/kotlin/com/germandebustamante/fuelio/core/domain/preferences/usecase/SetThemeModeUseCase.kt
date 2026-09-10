package com.germandebustamante.fuelio.core.domain.preferences.usecase

import com.germandebustamante.fuelio.core.domain.preferences.model.ThemeMode
import com.germandebustamante.fuelio.core.domain.preferences.repository.UserPreferencesRepository

open class SetThemeModeUseCase(private val userPreferencesRepository: UserPreferencesRepository) {
    open suspend operator fun invoke(themeMode: ThemeMode) = userPreferencesRepository.setThemeMode(themeMode)
}
