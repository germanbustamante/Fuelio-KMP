package com.germandebustamante.fuelio.core.interop

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
 * Overrides for the iOS UI-test harness.
 *
 * Only the outermost boundary is replaced — repositories and the location permission prompt. Every
 * layer above (use cases, ViewModels, `Navigator`, analytics) is the production one, so an XCUITest
 * exercises the real app rather than a mock of it, and does so without the network, without Room and
 * without a system permission dialog blocking the run.
 */
internal val uiTestModule: Module = module {
    single<GasStationRepository> { InMemoryGasStationRepository() }
    single<ProvinceRepository> { InMemoryProvinceRepository() }
    single<LocationPermissionController> { DeniedLocationPermissionController() }
}

private class InMemoryGasStationRepository : GasStationRepository {

    override fun getGasStationsByLocation(provinceId: String): Flow<Result<GasStationsResult>> =
        flowOf(Result.success(GasStationsResult(fakeGasStations, isFromCache = false)))

    override fun getGasStationById(id: String): Flow<GasStationBO?> =
        flowOf(fakeGasStations.firstOrNull { it.id == id })
}

private class InMemoryProvinceRepository : ProvinceRepository {

    override fun getProvinces(): Flow<Result<List<ProvinceBO>>> = flowOf(Result.success(fakeProvinces))
}

/**
 * Reports a permanent denial without touching CoreLocation, so no system alert appears mid-test and
 * the distance column stays absent (and therefore deterministic).
 */
private class DeniedLocationPermissionController : LocationPermissionController {

    override suspend fun requestPermission(): LocationPermissionState = LocationPermissionState.DeniedAlways

    override suspend fun checkCurrentStatus(): LocationPermissionState = LocationPermissionState.DeniedAlways

    override suspend fun getCurrentLocation(): LocationPermissionController.Location? = null

    override fun openAppSettings() = Unit
}
