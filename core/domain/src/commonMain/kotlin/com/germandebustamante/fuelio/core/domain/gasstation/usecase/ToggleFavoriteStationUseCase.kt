package com.germandebustamante.fuelio.core.domain.gasstation.usecase

import com.germandebustamante.fuelio.core.domain.gasstation.repository.FavoriteStationRepository

open class ToggleFavoriteStationUseCase(private val favoriteStationRepository: FavoriteStationRepository) {
    open suspend operator fun invoke(gasStationId: String) = favoriteStationRepository.toggleFavorite(gasStationId)
}
