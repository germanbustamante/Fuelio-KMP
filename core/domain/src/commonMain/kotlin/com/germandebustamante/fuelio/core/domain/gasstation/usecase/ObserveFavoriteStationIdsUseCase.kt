package com.germandebustamante.fuelio.core.domain.gasstation.usecase

import com.germandebustamante.fuelio.core.domain.gasstation.repository.FavoriteStationRepository
import kotlinx.coroutines.flow.Flow

open class ObserveFavoriteStationIdsUseCase(private val favoriteStationRepository: FavoriteStationRepository) {
    open operator fun invoke(): Flow<Set<String>> = favoriteStationRepository.observeFavoriteIds()
}
