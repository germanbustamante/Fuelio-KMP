package com.germandebustamante.fuelio.core.domain.gasstation.model

/**
 * Favourites the app can actually show, plus how many exist in total.
 *
 * The two can differ: a favourite is only resolvable while its province is cached, and the cache is
 * replaced wholesale on every refresh. Modelling the gap explicitly — rather than silently returning
 * a shorter list — is what lets the screen tell the user some favourites aren't loaded instead of
 * looking like it lost them. Same idea as `GasStationsResult.isFromCache`: business state, not an
 * error channel.
 */
data class FavoriteStationsResult(val stations: List<GasStationBO>, val totalFavoriteCount: Int) {
    val unresolvedCount: Int get() = (totalFavoriteCount - stations.size).coerceAtLeast(0)
}
