package com.germandebustamante.fuelio.core.domain.gasstation.usecase

import com.germandebustamante.fuelio.core.domain.gasstation.model.FavoriteStationsResult
import com.germandebustamante.fuelio.core.domain.gasstation.repository.FavoriteStationRepository
import kotlinx.coroutines.flow.Flow

open class ObserveFavoriteStationsUseCase(private val favoriteStationRepository: FavoriteStationRepository) {
    open operator fun invoke(): Flow<FavoriteStationsResult> = favoriteStationRepository.observeFavoriteStations()
}
