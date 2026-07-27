# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

```bash
# Android
./gradlew :composeApp:assembleDebug        # Build debug APK
./gradlew :composeApp:installDebug         # Install on connected device

# Tests
./gradlew test                             # composeApp unit tests (Android host tests aren't enabled for :data/:core:domain/:core:analytics, so this only runs composeApp)
./gradlew :core:domain:iosSimulatorArm64Test    # Domain module tests (KMP-only module, no JVM/Android test task)
./gradlew :data:iosSimulatorArm64Test           # Data module tests (same — runs via the iOS simulator target)
./gradlew :core:analytics:iosSimulatorArm64Test # Analytics module tests (same — also covers the iosTest bridge tests)
./gradlew connectedAndroidTest             # Android instrumentation tests

# iOS
# Open iosApp/iosApp.xcodeproj in Xcode and run from there
```

## Module Architecture

Gradle modules with strict Clean Architecture layering:

```
:core:domain    →  Business logic only (no platform deps)
:core:analytics →  Analytics tracking abstraction (Trace/Trackable/AnalyticsManager) + Firebase/PostHog implementations
:data           →  Repository implementations, Ktor HTTP client, Room local persistence
:composeApp     →  Compose Multiplatform UI, ViewModels, Navigation3, DI wiring
```

Dependencies flow one way: `composeApp → data → core:domain`. The domain module has zero platform or framework dependencies. `:core:analytics` is a separate branch consumed directly by `composeApp` (`api(projects.core.analytics)`, not `implementation`, since it exposes `Trace`/`AnalyticsTracking` types to feature code) — it has no dependency on `:core:domain` or `:data`.

## Kotlin Multiplatform Targets

- **Android**: `composeApp/src/androidMain/` — OkHttp engine, `AndroidApplication` initializes Koin
- **iOS**: `composeApp/src/iosMain/` + `iosApp/` (Swift) — Darwin engine, `iOSApp.swift` calls `KoinInitIosKt.doInitKoinIos()`
- **Common**: `*/src/commonMain/` — platform-agnostic code shared across targets

Platform-specific HTTP engine selection uses `expect`/`actual` in `data/src/*/kotlin/.../engine/HttpClientEngineProvider.kt`.

`:data`, `:core:domain`, and `:core:analytics` only target `iosArm64`/`iosSimulatorArm64` (no `iosX64`) — Google stopped publishing `iosX64` variants for recent AndroidX KMP libraries (Room/SQLite included) since Apple dropped Intel Mac support. `composeApp` already only had those two targets; keep all modules aligned or KSP/Room builds break with an unresolved-dependency error for `iosX64`.

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

## Analytics Tracking (`:core:analytics`)

- `Trace` (`Event`/`Screen`/`Error`) is an `open class`, not `data class` — features define a typed subclass per event/screen instead of instantiating `Trace.Screen(...)` inline with raw string keys, e.g. `GasStationDetailScreenViewed` in `feature/detail/analytics/`. `equals`/`hashCode`/`toString` are implemented manually (structural, matching by field), since `open class` can't be a `data class`.
- Concrete `Trace` subclasses live in the consuming feature module's `analytics/` package (`feature/list/analytics/`, `feature/detail/analytics/`), one file per event — never in `:core:analytics` itself. `:core:analytics` only holds generic, reusable abstractions (`Trace`, `Trackable`, `AnalyticsManager`, `AnalyticsProviderType`); promote a concrete event type there only once a second, independent module actually needs the same one.
- Each event is a `data object` (no params, e.g. `GasStationsScreenViewed`, `LocationPermissionRequested`) or a `data class` (has params, e.g. `GasStationSelected(gasStationId)`), never a plain `class`. `data object`s can't reference their own `EVENT_NAME`/`SCREEN_NAME` from inside the `super(...)` call (Kotlin: "Cannot access before initialized") and can't nest a `companion object` — inline the string literal in `super(...)` and duplicate it in the public `const val`, unlike `data class`es where the `companion object` constant can be referenced directly in `super(...)`.
- Event/screen name naming convention (`EVENT_NAME`/`SCREEN_NAME` constants, snake_case): `<origin_screen>_<category>_<action>` — prefixed by the screen/feature it originates from so events group together in analytics dashboards, middle segment names the category (`location_permission`, `province`, `station`), suffix is the action in past tense (`_selected`, `_changed`, `_requested`, `_granted`, `_denied`). Screen names (`Trace.Screen.screenName`) are just the `<origin_screen>` slug with no category/action suffix (e.g. `gas_stations_list`, `gas_station_detail`) since the `Trace.Screen` type already disambiguates it as a screen view. Examples: `gas_stations_list_station_selected`, `gas_stations_list_province_changed`, `gas_stations_list_location_permission_denied`. Param keys (`PARAM_*` constants) are plain snake_case with no prefix (`gas_station_id`, `province_id`) since they're already scoped by their event.
- `Trace.Error` is the one exception to "one bespoke class per event": errors are homogeneous (screen + operation + error type/message), so instead of one class per screen/operation there's a single reusable `ApiCallFailed(screenName, operation, errorType, errorMessage)` in `composeApp/.../feature/common/analytics/` (shared across features within `composeApp`, not promoted to `:core:analytics` since only `composeApp` needs it). Its `eventName` is built as `"${screenName}_${operation}_failed"`, keeping the same naming convention without hardcoding a string per screen — this scales to many screens/call sites without new files, at the cost of losing compile-time type-per-error (tests filter on `eventName`/params instead of a distinct type). `operation` is a free-form string constant per call site (e.g. `"fetch_provinces"`, `"fetch_stations"`); `errorType` is typically `domainError::class.simpleName`.
- Tracking failed API calls is done from the ViewModel (where `AnalyticsTracking` is wired), not from the Repository or `:data`/network layer — `:core:analytics` is only a dependency of `composeApp` (see above), and only the ViewModel knows the screen/operation context the naming convention needs, plus the business distinction between a hard error and a stale-data/background-refresh error. Each ViewModel centralizes this in a private `trackApiCallFailed(operation, domainError)` helper called from its single error-handling function (e.g. `GasStationsViewModel.notifyError`), rather than duplicating the tracking call at every `onFailure` site — see `GasStationsViewModel` for the reference implementation.
- Second exception to "one class per event": when several events are really just different outcomes of the *same* flow (e.g. requesting location permission ends in requested/granted/denied/denied-permanently), consolidate them into one `data class` parametrized by an enum discriminant instead of one class per outcome — see `LocationPermissionEvent(outcome: LocationPermissionOutcome)` in `feature/list/analytics/`. `eventName` is built as `"${screen}_location_permission_${outcome.name.lowercase()}"`, so the enum constant names (`REQUESTED`, `GRANTED`, `DENIED`, `DENIED_PERMANENTLY`) double as the naming convention's action suffix — pick enum constant names accordingly. Reserve one-class-per-event for events that are genuinely distinct actions (`GasStationSelected`, `ProvinceChanged`), not different results of the same action.
- `AnalyticsTracking` is the consumer-facing interface (`suspend fun track(trace: Trace)`) — ViewModels depend on this, not on the concrete `AnalyticsManager` class, so it can be mocked directly in tests (`AnalyticsManager` itself is a plain `class`, not `open`/an interface, so Mokkery can't mock it — mock `AnalyticsTracking` instead).
- `Trackable`/`Tracker` is the per-provider contract (`FirebaseTracker`, `PostHogTracker`); `track` is `suspend` all the way down (`AnalyticsTracking` → `AnalyticsManager` → `Trackable`), so a provider implementation can call a suspend API later without having to break the interface.
- `AnalyticsProviderType` enum (`FIREBASE`, `POSTHOG`) plus each `Trace`'s `targets` list decide which registered trackers receive it — `AnalyticsManager.track` matches by `Trackable.type`.
- `AnalyticsContextProvider` (`expect`/`actual`) + `AnalyticsPlatformModule` reuse the `ContextProvider` pattern from `:data` (below), so `PostHogTracker` gets Android's `Context` without leaking it into `commonMain`.
- `AnalyticsSecrets.POSTHOG_API_KEY` is generated by BuildKonfig from `thirdparties.properties` (gitignored, not `local.properties`) or the `POSTHOG_API_KEY` env var in CI; blank/missing is a valid state that just keeps PostHog off.
- iOS native bridge functions (`registerNativeFirebaseTracker`, `registerNativePostHogTracker`) are called directly from Swift in `iOSApp.swift`, so `composeApp`'s iOS framework re-exports `:core:analytics` (`export(projects.core.analytics)`) to make those symbols visible.

### Testing suspend `track` with Mokkery

- Use `everySuspend`/`verifySuspend` (not `every`/`verify`) for anything touching `track`.
- `Capture.slot<T>()` + the `capture(slot)` matcher must go inside the `every`/`everySuspend` stub that actually intercepts the call — putting `capture(...)` inside a `verify`/`verifySuspend` block instead throws `AbsentValueInSlotException`, since Mokkery only fills the slot when a call is matched live, not on verify replay.
- If the code under test calls `track(...)` from inside `viewModelScope.launch { }` (as `GasStationDetailViewModel.init` does), the test needs `Dispatchers.setMain(StandardTestDispatcher())` in `@BeforeTest` / `Dispatchers.resetMain()` in `@AfterTest`, plus wrapping the test body in `runTest { ... advanceUntilIdle() }` — otherwise the launched coroutine never actually runs during the test (see `GasStationsViewModelTest` for the reference setup).

## Key Versions

- AGP: 9.2.1, Kotlin: 2.4.0, Compose Multiplatform: 1.11.1
- Koin BOM: 4.2.2, Ktor: 3.5.1
- Room: 2.8.4, KSP: 2.3.10, androidx.sqlite (bundled driver): 2.7.0
- Android minSdk: 26, compileSdk/targetSdk: 37

## R8 / Release Builds

`composeApp`'s `release` build type has `isMinifyEnabled` and `isShrinkResources` enabled, using `proguard-android-optimize.txt` plus `composeApp/proguard-rules.pro`. Verify `:composeApp:assembleRelease` after adding libraries that rely on reflection (Koin, kotlinx.serialization) — add narrow, specific keep rules to `proguard-rules.pro` only if R8 reports missing rules, rather than broad library-wide rules.

## Commit Message Convention

Every commit subject is prefixed with the ticket ID matching the branch (e.g. `FE-1.0.0:`), followed by a Conventional-Commits-style `Type(Scope):` (capitalized type: `Feat`, `Fix`, `Refactor`, `Perf`, `UI`, `Test`, `Build`, `Docs`, `Chore`) and an imperative, capitalized title with no trailing punctuation — e.g. `FE-1.0.0: Feat(UI): Redesign gas station items and implement detail screen`.

Pick the format based on the diff's scope:

- **Simple** (1-3 files, one logical change — a bug fix, a small tweak): subject line only, no body. e.g. `FE-1.0.0: Fix(UI): Correct scroll-to-top FAB arrow direction`.
- **Complex** (4+ files, multiple layers/components, a feature or refactor): subject line, blank line, then a short paragraph giving the "why" (business/architectural context), blank line, then a bullet list grouping changes logically — each bullet names the specific screen/component/class touched and explains what changed and why, not just what. Use Clean Architecture terms accurately (Repository, UseCase, BO/VO, ViewModel, Composable) and call out DI, navigation, state management, or persistence changes explicitly when present.

Content rules for both formats:
- Present tense, active voice, imperative mood ("Add", "Fix", "Extract" — not "Added"/"Fixes").
- Be specific — name the actual screen/class/function; avoid vague terms like "various fixes" or "misc updates".
- Never include personal info, secrets, internal URLs/hostnames, or bug-tracker IDs unless the ID is already part of the branch name.
- One logical change per commit message — don't bundle unrelated work into a single bullet list.

## Debug Tooling

- `AppLogger` (`expect`/`actual` in `core/logger`, per source set) wraps platform logging (`android.util.Log` on Android). Prefer it over direct platform log calls in shared code; avoid calling it from `Composable` getters or other code paths Compose may invoke multiple times per frame.
- ANR-WatchDog (`androidMain`, `AndroidApplication.onCreate`) is active only when `FLAG_DEBUGGABLE` is set, with `setIgnoreDebugger(true)` so it still fires while running under the Android Studio debugger. On detection it logs the full multi-thread stack trace via `AppLogger.e("ANRWatchDog", ...)` — filter Logcat by that tag to inspect.