package com.germandebustamante.fuelio.core.testing

import com.germandebustamante.fuelio.core.domain.error.DomainError
import com.germandebustamante.fuelio.core.domain.gasstation.model.FavoriteStationsResult
import com.germandebustamante.fuelio.core.domain.gasstation.model.GasStationBO
import com.germandebustamante.fuelio.core.domain.gasstation.model.GasStationsResult
import com.germandebustamante.fuelio.core.domain.gasstation.repository.FavoriteStationRepository
import com.germandebustamante.fuelio.core.domain.gasstation.repository.GasStationRepository
import com.germandebustamante.fuelio.core.domain.preferences.model.FuelType
import com.germandebustamante.fuelio.core.domain.preferences.model.ThemeMode
import com.germandebustamante.fuelio.core.domain.preferences.model.UserPreferencesBO
import com.germandebustamante.fuelio.core.domain.preferences.repository.UserPreferencesRepository
import com.germandebustamante.fuelio.core.domain.province.model.ProvinceBO
import com.germandebustamante.fuelio.core.domain.province.repository.ProvinceRepository
import com.germandebustamante.fuelio.core.fake.fakeGasStations
import com.germandebustamante.fuelio.feature.common.permission.location.LocationPermissionController
import com.germandebustamante.fuelio.feature.common.permission.location.LocationPermissionState
import com.germandebustamante.fuelio.feature.list.state.fakeProvinces
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Overrides for the UI-test harness, shared by both platforms.
 *
 * Only the outermost boundary is replaced — repositories and the location permission prompt. Every
 * layer above (use cases, ViewModels, `Navigator`, analytics) is the production one, so an
 * instrumentation/XCUITest run exercises the real app rather than a mock of it, and does so without
 * the network, without Room and without a system permission dialog blocking the run.
 *
 * Moved here from `iosMain` (was iOS-only) since nothing in it touches a platform type — it is
 * plugged into `initKoin(overrides = ...)` from Android's `FuelioTestApplication` the same way iOS's
 * `initKoinIosForUiTests` already does, instead of duplicating a second copy of these fakes.
 */
fun uiTestModule(simulateStationFailure: Boolean): Module = module {
    single<GasStationRepository> {
        if (simulateStationFailure) FailingGasStationRepository() else InMemoryGasStationRepository()
    }
    single<ProvinceRepository> { InMemoryProvinceRepository() }
    single<UserPreferencesRepository> { InMemoryUserPreferencesRepository() }
    single<FavoriteStationRepository> { InMemoryFavoriteStationRepository() }
    single<LocationPermissionController> { DeniedLocationPermissionController() }
}

private class InMemoryGasStationRepository : GasStationRepository {

    override fun getGasStationsByLocation(provinceId: String): Flow<Result<GasStationsResult>> =
        flowOf(Result.success(GasStationsResult(fakeGasStations, isFromCache = false)))

    override fun getGasStationById(id: String): Flow<GasStationBO?> = flowOf(fakeGasStations.firstOrNull { it.id == id })
}

/**
 * Stations fail, provinces still load. That is the combination the blocking error state needs: the
 * ViewModel only shows a hard error when there is nothing on screen yet, and `contentState` stays
 * `Loading` until a province is selected.
 */
private class FailingGasStationRepository : GasStationRepository {

    override fun getGasStationsByLocation(provinceId: String): Flow<Result<GasStationsResult>> =
        flowOf(Result.failure(DomainError.ServerError(503)))

    override fun getGasStationById(id: String): Flow<GasStationBO?> = flowOf(null)
}

private class InMemoryProvinceRepository : ProvinceRepository {

    override fun getProvinces(): Flow<Result<List<ProvinceBO>>> = flowOf(Result.success(fakeProvinces))
}

/**
 * Same reasoning as the preferences fake: favourites are persisted in Room, so without this a
 * favourite starred by one test would still be starred in the next one, and in the next run.
 */
private class InMemoryFavoriteStationRepository : FavoriteStationRepository {
    private val favorites = MutableStateFlow(emptySet<String>())

    override fun observeFavoriteIds(): Flow<Set<String>> = favorites

    override fun observeFavoriteStations(): Flow<FavoriteStationsResult> = favorites.map { ids ->
        val stations = fakeGasStations.filter { it.id in ids }
        FavoriteStationsResult(stations = stations, totalFavoriteCount = ids.size)
    }

    override suspend fun toggleFavorite(gasStationId: String) {
        favorites.update { ids -> if (gasStationId in ids) ids - gasStationId else ids + gasStationId }
    }
}

/**
 * Keeps preferences in memory so a UI-test run never writes a real `.preferences_pb`.
 *
 * That file would otherwise survive between runs and across tests, so whichever test happened to
 * change the province or the theme would decide what the next one saw.
 */
private class InMemoryUserPreferencesRepository : UserPreferencesRepository {
    private val preferences = MutableStateFlow(UserPreferencesBO())

    override fun observe(): Flow<UserPreferencesBO> = preferences

    override suspend fun setDefaultFuelType(fuelType: FuelType) {
        preferences.update { it.copy(defaultFuelType = fuelType) }
    }

    override suspend fun setSavedProvinceId(provinceId: String) {
        preferences.update { it.copy(savedProvinceId = provinceId) }
    }

    override suspend fun setThemeMode(themeMode: ThemeMode) {
        preferences.update { it.copy(themeMode = themeMode) }
    }
}

/**
 * Reports a permanent denial without touching platform location APIs, so no system permission
 * dialog appears mid-test and the distance column stays absent (and therefore deterministic).
 */
private class DeniedLocationPermissionController : LocationPermissionController {

    override suspend fun requestPermission(): LocationPermissionState = LocationPermissionState.DeniedAlways

    override suspend fun checkCurrentStatus(): LocationPermissionState = LocationPermissionState.DeniedAlways

    override suspend fun getCurrentLocation(): LocationPermissionController.Location? = null

    override fun openAppSettings() = Unit
}
