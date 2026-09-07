package com.germandebustamante.fuelio.core.testing

import com.germandebustamante.fuelio.core.domain.error.DomainError
import com.germandebustamante.fuelio.core.domain.gasstation.model.GasStationBO
import com.germandebustamante.fuelio.core.domain.gasstation.model.GasStationsResult
import com.germandebustamante.fuelio.core.domain.gasstation.repository.GasStationRepository
import com.germandebustamante.fuelio.core.domain.province.model.ProvinceBO
import com.germandebustamante.fuelio.core.domain.province.repository.ProvinceRepository
import com.germandebustamante.fuelio.core.fake.fakeGasStations
import com.germandebustamante.fuelio.feature.common.permission.location.LocationPermissionController
import com.germandebustamante.fuelio.feature.common.permission.location.LocationPermissionState
import com.germandebustamante.fuelio.feature.list.state.fakeProvinces
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
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
    single<LocationPermissionController> { DeniedLocationPermissionController() }
}

private class InMemoryGasStationRepository : GasStationRepository {

    override fun getGasStationsByLocation(provinceId: String): Flow<Result<GasStationsResult>> =
        flowOf(Result.success(GasStationsResult(fakeGasStations, isFromCache = false)))

    override fun getGasStationById(id: String): Flow<GasStationBO?> =
        flowOf(fakeGasStations.firstOrNull { it.id == id })
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
 * Reports a permanent denial without touching platform location APIs, so no system permission
 * dialog appears mid-test and the distance column stays absent (and therefore deterministic).
 */
private class DeniedLocationPermissionController : LocationPermissionController {

    override suspend fun requestPermission(): LocationPermissionState = LocationPermissionState.DeniedAlways

    override suspend fun checkCurrentStatus(): LocationPermissionState = LocationPermissionState.DeniedAlways

    override suspend fun getCurrentLocation(): LocationPermissionController.Location? = null

    override fun openAppSettings() = Unit
}
