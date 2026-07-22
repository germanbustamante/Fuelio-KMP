# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

```bash
# Android
./gradlew :composeApp:assembleDebug        # Build debug APK
./gradlew :composeApp:installDebug         # Install on connected device

# Tests
./gradlew test                             # composeApp unit tests (Android host tests aren't enabled for :data/:core:domain, so this only runs composeApp)
./gradlew :core:domain:iosSimulatorArm64Test  # Domain module tests (KMP-only module, no JVM/Android test task)
./gradlew :data:iosSimulatorArm64Test         # Data module tests (same — runs via the iOS simulator target)
./gradlew connectedAndroidTest             # Android instrumentation tests

# iOS
# Open iosApp/iosApp.xcodeproj in Xcode and run from there
```

## Module Architecture

Three Gradle modules with strict Clean Architecture layering:

```
:core:domain   →  Business logic only (no platform deps)
:data          →  Repository implementations, Ktor HTTP client, Room local persistence
:composeApp    →  Compose Multiplatform UI, ViewModels, Navigation3, DI wiring
```

Dependencies flow one way: `composeApp → data → core:domain`. The domain module has zero platform or framework dependencies.

## Kotlin Multiplatform Targets

- **Android**: `composeApp/src/androidMain/` — OkHttp engine, `AndroidApplication` initializes Koin
- **iOS**: `composeApp/src/iosMain/` + `iosApp/` (Swift) — Darwin engine, `iOSApp.swift` calls `KoinInitIosKt.doInitKoinIos()`
- **Common**: `*/src/commonMain/` — platform-agnostic code shared across targets

Platform-specific HTTP engine selection uses `expect`/`actual` in `data/src/*/kotlin/.../engine/HttpClientEngineProvider.kt`.

`:data` and `:core:domain` only target `iosArm64`/`iosSimulatorArm64` (no `iosX64`) — Google stopped publishing `iosX64` variants for recent AndroidX KMP libraries (Room/SQLite included) since Apple dropped Intel Mac support. `composeApp` already only had those two targets; keep all three modules aligned or KSP/Room builds break with an unresolved-dependency error for `iosX64`.

## Navigation (Navigation3)

Uses AndroidX Navigation3 (KMP), not classic Navigation Compose:

- `core/navigation/destination/Destination.kt` — `@Serializable sealed interface Destination : NavKey`; each screen is a nested `data object`/`data class` (e.g. `Destination.GasStationDetails(val gasStationId: String)`). Keep nav args as plain identifiers (IDs), never pass full domain objects — the back stack is serialized via `rememberNavBackStack`/`SavedStateConfiguration` on every navigation (`FuelioNavHost.kt`), so heavy or stale objects would bloat/duplicate state that a use case can already resolve.
- `core/navigation/action/Navigator.kt` — `Navigator`/`DefaultNavigator`, a `Channel`-based one-shot event bus so ViewModels can request navigation without depending on Compose (`navigate()`/`navigateUp()`, consumed via `ObserveAsEvent` in `FuelioNavHost`).
- `FuelioNavHost.kt` — owns the `backStack`, wires `entryProvider { entry<Destination.X> { ... } }`, and applies `rememberViewModelStoreNavEntryDecorator()` so each back-stack entry gets its own `ViewModelStore` (otherwise a ViewModel could be reused across unrelated navigations to the same route).

## Dependency Injection (Koin)

Plain Koin DSL (`module { ... }` with explicit `single {}` / `factory {}` / `viewModel {}` blocks) — **no annotation processing is currently wired up** despite `koin-annotations`/`koin_plugin` being present in the version catalog; don't add `@Single`/`@Factory`/`@ComponentScan` expecting them to be picked up automatically. Each feature/layer provides its own module:

- `DomainModule` — `core/domain/src/commonMain/.../di/`, one `factory {}` per use case
- `DataModule` — `data/src/commonMain/.../di/`, repositories/datasources/`FuelioDatabase`; includes `dataPlatformModule` (see below)
- `GasStationListModule` / `GasStationDetailModule` — in `composeApp`, one per feature, pull in Data + Domain modules
- `KoinInit.kt` — aggregates all modules for initialization

When adding a new use case or repository, register it explicitly with a `single {}`/`factory {}` line in the relevant module — there's no scanning step to rely on.

### ContextProvider pattern (Android `Context` in `:data`)

`:data` needs Android's `Context` (for `Room.databaseBuilder`) without leaking an Android type into `commonMain`. Rather than one `expect`/`actual` platform module per context-consuming class, there's a single seam:

- `data/di/ContextProvider.kt` (`expect class`) — Android `actual` wraps the real `Context` (`getAndroidContext()` does the one cast in the whole module); iOS `actual` is a no-op.
- `data/di/DataPlatformModule.kt` (`expect val dataPlatformModule: Module`) — Android provides `single { ContextProvider(androidContext()) }` (relies on `androidContext(this)` already registered in `AndroidApplication.onCreate`); iOS provides `single { ContextProvider(Unit) }`.

Any future class needing platform context takes `ContextProvider` as a constructor dependency — don't add another platform module for it.

## State Management Pattern

ViewModels expose a single flat `StateFlow<UIState>` — `UIState` is a `data class` (not a sealed class) with fields like `isLoading`/`isRefreshing`/`error`/data lists, plus `with*` copy-helper functions (`withStationsLoaded(...)`, `withError(...)`, etc.) for every transition. A computed `val contentState: ContentState` property derives a sealed `ContentState` (`Initial`/`Loading`/`Success`/`Empty`/`Error`) from those flat fields for the UI to `when`-branch on — see `GasStationsUIState.kt` for the reference implementation. UI collects via `collectAsStateWithLifecycle()`. Always mutate the backing `MutableStateFlow` with `.update { }`, never `.value = ...` (thread-safety).

`GasStationDetailUIState` still hasn't been migrated to this pattern (currently a bare `data class(val gasStation: GasStationBO)` with `MutableStateFlow<GasStationDetailUIState?>`, `null` standing in for "not loaded yet") — follow the `GasStationsUIState` pattern when fleshing out the detail screen instead of extending the current one.

View objects (VO suffix) live in the feature's `state/` package and contain display-ready data with calculated fields. Business objects (BO suffix) live in `core/domain` and are the source of truth.

## Local Persistence & Offline-First (Room)

`:data` persists gas stations in a Room database (`data/local/database/FuelioDatabase.kt`, KMP via `androidx.sqlite:sqlite-bundled`, entities/DAOs in `data/gasstation/local/`). Room's Gradle plugin manages the schema export to `data/schemas/` — commit that directory (don't gitignore it), it's the diffable history of schema changes.

`GasStationRepositoryImpl` implements a stale-while-revalidate strategy, not a simple network-then-cache:

- `getGasStationsByLocation(provinceId)` returns `Flow<Result<GasStationsResult>>` (`GasStationsResult(stations, isFromCache: Boolean)`, in `core/domain`) and can emit **twice**: cached rows first (`isFromCache = true`) if any exist, then the network result (`isFromCache = false`) after writing it back to Room. `Result` here models genuine network failure — `GasStationsResult`/`isFromCache` is business state, not part of the error channel. In the ViewModel, `notifyError` picks a "hard" error (`withError`, blocks the whole screen) only if `gasStations` is currently empty; if there's already content on screen (from cache or a prior load), a failed refresh goes to `withStaleDataError` instead, which keeps the list and just triggers a transient snackbar — never let a background refresh failure blank out data the user can already see.
- `getGasStationById(id)` returns a plain `Flow<GasStationBO?>` (no `Result`) sourced directly from a Room `Flow<GasStationEntity?>` query — it's a pure local read with no network path (the upstream MINETUR API has no fetch-by-station-id endpoint, only list-by-province/municipality/CCAA), so `null` is an expected business value ("not cached yet"), not a failure to wrap in `Result`. Being backed by Room's own `Flow` also makes it reactive: if the list screen refreshes that station's row in the background, an open detail screen updates without re-querying.

## Key Versions

- AGP: 9.2.1, Kotlin: 2.4.0, Compose Multiplatform: 1.11.1
- Koin BOM: 4.2.2, Ktor: 3.5.1
- Room: 2.8.4, KSP: 2.3.10, androidx.sqlite (bundled driver): 2.7.0
- Android minSdk: 26, compileSdk/targetSdk: 37

## R8 / Release Builds

`composeApp`'s `release` build type has `isMinifyEnabled` and `isShrinkResources` enabled, using `proguard-android-optimize.txt` plus `composeApp/proguard-rules.pro`. Verify `:composeApp:assembleRelease` after adding libraries that rely on reflection (Koin, kotlinx.serialization) — add narrow, specific keep rules to `proguard-rules.pro` only if R8 reports missing rules, rather than broad library-wide rules.

## Debug Tooling

- `AppLogger` (`expect`/`actual` in `core/logger`, per source set) wraps platform logging (`android.util.Log` on Android). Prefer it over direct platform log calls in shared code; avoid calling it from `Composable` getters or other code paths Compose may invoke multiple times per frame.
- ANR-WatchDog (`androidMain`, `AndroidApplication.onCreate`) is active only when `FLAG_DEBUGGABLE` is set, with `setIgnoreDebugger(true)` so it still fires while running under the Android Studio debugger. On detection it logs the full multi-thread stack trace via `AppLogger.e("ANRWatchDog", ...)` — filter Logcat by that tag to inspect.