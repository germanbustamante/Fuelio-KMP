# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Product Naming & Repo Scope

This repository (`Fuelio`) is a **public portfolio/showcase project** — it demonstrates KMP + native UI
architecture (Clean Architecture, SwiftUI/Jetpack Compose, offline-first persistence, analytics, testing)
for job-hunting purposes, per the roadmap in the "Fuelio · Senior Mobile Roadmap" doc. It keeps the
`Fuelio` name and is not intended to ship as a commercial product competing with the existing
`Fuelio` app by Sygic (5M+ downloads) — that naming collision is only a real problem for something
actually published to app stores at scale.

The **real product** — a separate, private repository with its own backend and resource management, not
an MVP — will ship under the name **Octana** instead, to avoid trademark/ASO collision with the existing
Fuelio app and to have a distinct, ownable brand once it's a commercial app in the stores. Don't rename
this repo's package/branding to Octana; that name belongs to the other, private codebase.

## Build Commands

```bash
# Android
./gradlew :androidApp:assembleDebug        # Build debug APK
./gradlew :androidApp:installDebug         # Install on connected device

# Tests
./gradlew :androidApp:testDebugUnitTest         # androidApp unit tests (Compose-dependent code: designsystem, screens)
# Every KMP module declares withHostTestBuilder {}, so each one's commonTest runs on the JVM too.
# Prefer the host tasks for a fast local loop and for CI on Linux; the simulator tasks are what
# additionally cover each module's iosTest source set (the Swift-facing bridges).
./gradlew :core:domain:testAndroidHostTest      # Domain module tests, JVM
./gradlew :core:analytics:testAndroidHostTest   # Analytics module tests, JVM
./gradlew :data:testAndroidHostTest             # Data module tests, JVM
./gradlew :core:presentation:testAndroidHostTest      # ViewModel/state/navigation tests on the JVM (Android host)

./gradlew :core:presentation:iosSimulatorArm64Test    # Same tests + the iOS DI factory, simulator target
./gradlew :core:domain:iosSimulatorArm64Test
./gradlew :data:iosSimulatorArm64Test
./gradlew :core:analytics:iosSimulatorArm64Test # Also covers the iosTest tracker-bridge tests
./gradlew connectedAndroidTest             # Android instrumentation tests (not run on PRs — see ADR 0006)

# Screenshots (Roborazzi; goldens committed under androidApp/src/test/screenshots/)
./gradlew :androidApp:verifyRoborazziDebug      # compare against the goldens; this is what CI runs
./gradlew :androidApp:recordRoborazziDebug      # re-record after an intentional visual change

# iOS
./gradlew :core:presentation:linkDebugFrameworkIosSimulatorArm64  # sanity-build the CorePresentation.framework outside Xcode
# Open iosApp/iosApp.xcodeproj in Xcode and run from there — the "Compile Kotlin Framework" build
# phase invokes `:core:presentation:embedAndSignAppleFrameworkForXcode` automatically.

xcodebuild build -project iosApp/iosApp.xcodeproj -scheme iosApp \
  -destination 'platform=iOS Simulator,name=iPhone 17,OS=latest'
xcodebuild test  -project iosApp/iosApp.xcodeproj -scheme iosApp \
  -destination 'platform=iOS Simulator,name=iPhone 17,OS=latest'
# Always pass OS=latest: `name=iPhone 17` alone is ambiguous when several iOS runtimes are installed,
# and the device contention surfaces as "Application failed preflight checks", not as a clear error.
```

## Module Architecture

Gradle modules with strict Clean Architecture layering:

```
:core:domain        →  Business logic only (no platform deps)
:core:analytics     →  Analytics tracking abstraction (Trace/Trackable/AnalyticsManager) + Firebase/PostHog implementations
:data               →  Repository implementations, Ktor HTTP client, Room local persistence
:core:presentation  →  ViewModels, UIState, Navigator/Destination, DI modules, design tokens — Kotlin only, zero Compose
:androidApp         →  Jetpack Compose UI (Android-only), consumes :core:presentation
iosApp (Xcode)      →  SwiftUI UI, consumes the CorePresentation.framework built from :core:presentation
```

Dependencies flow one way: `androidApp → core:presentation → data → core:domain`. The domain module has
zero platform or framework dependencies. `:core:analytics` is a separate branch consumed directly by
`:core:presentation` (`api(projects.core.analytics)`, not `implementation`, since it exposes
`Trace`/`AnalyticsTracking` types to feature code and iOS's Swift bridges) — it has no dependency on
`:core:domain` or `:data`.

**`:core:presentation` is the KMP + UI-nativa split point (roadmap phase P1.5, see `docs/adr/0001-*.md`
and `docs/adr/0002-*.md`).** It holds every presentation-layer type that doesn't touch Compose:
ViewModels (`GasStationsViewModel`, `GasStationDetailViewModel`), their `UIState`s, `Navigator`/
`NavigationAction`/`Destination`, the Koin DI modules (`di/KoinInit.kt`, `feature/*/di/*Module.kt`),
`LocationPermissionController` (interface + Android/iOS `actual`s), the `Trace` analytics subclasses,
and the shared design tokens (`core/designtokens/`). **Invariant:** nothing under
`core/presentation/src` may import `androidx.compose.*`, `org.jetbrains.compose.*`, or
`androidx.navigation3.*` — verify with
`grep -r "androidx.compose\|org.jetbrains.compose\|navigation3" core/presentation/src`. This is what
lets the exact same module compile into an Android library *and* export a Kotlin/Native framework
(`baseName = "CorePresentation"`, `export(projects.core.analytics)`) that Xcode links directly.

`:androidApp` is a **plain Android application module** (`com.android.application`, no Kotlin
Multiplatform plugin, sources under `src/main/kotlin` — not `src/commonMain`), holding only what's
Compose-dependent: the design system (`designsystem/`), `feature/*/ui/`, `core/ui/theme/` (the Compose
adapter over the shared design tokens), `FuelioNavHost`, and the two platform entry points
(`AndroidApplication`, `MainActivity`). It depends on `:core:presentation` for everything else.

## Kotlin Multiplatform Targets

- **Android**: `androidApp` (plain Android app, `src/main/`) consumes `core/presentation/src/androidMain/`
  — OkHttp engine (in `:data`), `AndroidApplication.onCreate` calls `initKoin { androidContext(...) }`.
- **iOS**: `core/presentation/src/iosMain/` + `iosApp/` (Swift) — Darwin engine (in `:data`),
  `iOSApp.swift` calls `KoinInitIosKt.doInitKoinIos()` after importing the `CorePresentation` framework.
  There is no `composeApp`/Compose UI on iOS anymore (see "Module Architecture" above) — `iosApp` is a
  native SwiftUI app.
- **Common**: `*/src/commonMain/` — platform-agnostic code shared across targets. `:androidApp` is the
  one exception: it's Android-only, so its source set is `src/main/`, not `src/commonMain/`.

Platform-specific HTTP engine selection uses `expect`/`actual` in `data/src/*/kotlin/.../engine/HttpClientEngineProvider.kt`.

`:data`, `:core:domain`, `:core:analytics`, and `:core:presentation` only target `iosArm64`/
`iosSimulatorArm64` (no `iosX64`) — Google stopped publishing `iosX64` variants for recent AndroidX KMP
libraries (Room/SQLite included) since Apple dropped Intel Mac support. Keep all modules aligned or
KSP/Room builds break with an unresolved-dependency error for `iosX64`.

## Navigation (Navigation3)

Uses AndroidX Navigation3 (KMP) on Android, not classic Navigation Compose. `Destination` itself lives
in `:core:presentation` and is **not** a `NavKey` — Navigation3 is a Compose-adjacent library, and
`:core:presentation` can't depend on it (see "Module Architecture"). The two are bridged in `:androidApp`:

- `core/presentation/.../core/navigation/destination/Destination.kt` (in `:core:presentation`) —
  `@Serializable sealed interface Destination` (no `NavKey`); each screen is a nested `data object`/
  `data class` (e.g. `Destination.GasStationDetails(val gasStationId: String)`). Keep nav args as plain
  identifiers (IDs), never pass full domain objects — a use case can already resolve them, and the back
  stack gets serialized on every navigation. There are four today: `GasStations` (root),
  `GasStationDetails(id)`, `Settings` and `Favorites`. **Adding a fifth is a cross-cutting change**,
  because SKIE turns every direct subtype into a case of the generated Swift enum — the compiler will
  point at each site, but expect to touch, in the same commit: `Destination.kt`,
  `SyntheticBackStack.kt` (the `parent` `when`), `DeepLinkParser.kt`, `FuelioNavHost.kt`,
  `Route.swift` (case + `init?`), `RootView.swift`, `KotlinSealed.swift` if it carries a sealed state,
  the `A11yIdentifiers`/`A11yID`/`UITestSupport` trio, `Localizable.xcstrings` + `LocalizationTests`,
  and the `SyntheticBackStackTest`/`DeepLinkParserTest`/`NavigationMappingTests` suites.
- `core/presentation/.../core/navigation/action/Navigator.kt` (in `:core:presentation`) —
  `Navigator`/`DefaultNavigator`, a `Channel`-based one-shot event bus so ViewModels can request
  navigation without depending on Compose (`navigate()`/`navigateUp()`, consumed via `ObserveAsEvent` in
  `FuelioNavHost`).
- `androidApp/.../core/navigation/destination/DestinationNavKey.kt` (in `:androidApp`) —
  `@Serializable data class DestinationNavKey(val destination: Destination) : NavKey`. This is the
  **only** place `Destination` touches `NavKey`; it's what actually goes on the Navigation3 back stack.
- `FuelioNavHost.kt` (in `:androidApp`) — owns the `backStack` of `DestinationNavKey`, wires a single
  `entry<DestinationNavKey> { key -> when (key.destination) { ... } }`, and applies
  `rememberViewModelStoreNavEntryDecorator()` so each back-stack entry gets its own `ViewModelStore`
  (otherwise a ViewModel could be reused across unrelated navigations to the same route).

iOS has no Navigation3 equivalent yet — SwiftUI screens (as they get ported) will drive navigation
directly off `Navigator.navigationActions`/`Destination` from Swift, with no `NavKey`-style wrapper
needed since `NavigationStack` doesn't require one.

## Dependency Injection (Koin)

Plain Koin DSL (`module { ... }` with explicit `single {}` / `factory {}` / `viewModel {}` blocks) — **no annotation processing is currently wired up** despite `koin-annotations`/`koin_plugin` being present in the version catalog; don't add `@Single`/`@Factory`/`@ComponentScan` expecting them to be picked up automatically. Each feature/layer provides its own module:

- `DomainModule` — `core/domain/src/commonMain/.../di/`, one `factory {}` per use case
- `DataModule` — `data/src/commonMain/.../di/`, repositories/datasources/`FuelioDatabase`; includes `dataPlatformModule` (see below)
- `GasStationListModule` / `GasStationDetailModule` — now in `:core:presentation` (moved from `composeApp` in P1.5), one per feature, pull in Data + Domain modules. `viewModel {}` here comes from `org.koin.core.module.dsl.viewModel` (koin-core), not `koin-compose-viewmodel` — that's what lets these modules live outside `:androidApp`.
- `KoinInit.kt` — also in `:core:presentation` now, aggregates all modules for initialization. Called from both `AndroidApplication.onCreate` (Android) and `doInitKoinIos()` (iOS) — same entry point, same module set, no platform-specific DI wiring needed beyond `dataPlatformModule`/`analyticsPlatformModule`'s `ContextProvider` seam.

When adding a new use case or repository, register it explicitly with a `single {}`/`factory {}` line in the relevant module — there's no scanning step to rely on.

### ContextProvider pattern (Android `Context` in `:data`)

`:data` needs Android's `Context` (for `Room.databaseBuilder`) without leaking an Android type into `commonMain`. Rather than one `expect`/`actual` platform module per context-consuming class, there's a single seam:

- `data/di/ContextProvider.kt` (`expect class`) — Android `actual` wraps the real `Context` (`getAndroidContext()` does the one cast in the whole module); iOS `actual` is a no-op.
- `data/di/DataPlatformModule.kt` (`expect val dataPlatformModule: Module`) — Android provides `single { ContextProvider(androidContext()) }` (relies on `androidContext(this)` already registered in `AndroidApplication.onCreate`); iOS provides `single { ContextProvider(Unit) }`.

Any future class needing platform context takes `ContextProvider` as a constructor dependency — don't add another platform module for it. The preferences DataStore is the second user of the seam: `expect fun preferencesPath(contextProvider: ContextProvider)` resolves `filesDir` on Android and `NSDocumentDirectory` on iOS, with no new platform module.

### App startup tasks (`StartupTask`)

`core/startup/StartupTask.kt` — `fun interface StartupTask { suspend operator fun invoke() }`, a contract for work that must run once at app launch and isn't owned by any single screen (e.g. a future background sync or remote-config fetch). `core/startup/di/StartupModule.kt` exposes `startupModule` with a `single<Set<StartupTask>> { setOf(...) }`, currently holding `CrashReporterStartupTask` (enabling/disabling crash collection and seeding platform keys) — everything else is still screen-scoped inside its own ViewModel's `init {}` (see "State Management Pattern" below). `KoinInit.kt`'s `initKoin()` resolves that `Set` and runs each task on a `MainScope()`, catching failures per-task via `AppLogger` so one broken task can't crash launch or cancel the others — this runs from shared `commonMain`, so it covers both the Android (`AndroidApplication.onCreate`) and iOS (`doInitKoinIos()`) entry points with no platform-specific plumbing.

Koin has no Hilt-style `@IntoSet` auto-multibinding, so the `Set<StartupTask>` in `StartupModule.kt` is assembled by hand — when a real cross-cutting startup task is needed, implement `StartupTask`, register it with `single<StartupTask> { ... }`, and add it to that `setOf(...)`. Don't invent a `StartupTask` for something a screen's ViewModel already owns (see `launchStartupTasks` below) — this contract is reserved for work with no natural screen owner.

## State Management Pattern

ViewModels expose a single flat `StateFlow<UIState>` — `UIState` is a `data class` (not a sealed class) with fields like `isLoading`/`isRefreshing`/`error`/data lists, plus `with*` copy-helper functions (`withStationsLoaded(...)`, `withError(...)`, etc.) for every transition. A computed `val contentState: ContentState` property derives a sealed `ContentState` (`Initial`/`Loading`/`Success`/`Empty`/`Error`) from those flat fields for the UI to `when`-branch on — see `GasStationsUIState.kt` for the reference implementation. UI collects via `collectAsStateWithLifecycle()`. Always mutate the backing `MutableStateFlow` with `.update { }`, never `.value = ...` (thread-safety).

`GasStationDetailUIState` now follows the same flat-state pattern as `GasStationsUIState` (non-nullable `MutableStateFlow<GasStationDetailUIState>`, `isLoading`/`gasStation`/`today` fields, computed `contentState`).

A ViewModel's `init {}` block launches its screen's startup work (analytics tracking, initial fetches, observers) via `launchStartupTasks(...)` (`feature/common/viewmodel/StartupTasks.kt`) — a small `vararg` helper that launches each lambda in its own `viewModelScope.launch`, same as writing them out by hand, but keeping the full list of "what this screen kicks off on creation" visible as one block instead of scattered `viewModelScope.launch { ... }` calls. See `GasStationsViewModel`/`GasStationDetailViewModel` for reference usage.

We deliberately did **not** adopt a full MVI/`MviConfig` startup-intent pattern (sealed `Intent`s + `isStateRestored` gating tied to `SavedStateHandle`) here: this project's flat-state + direct-method architecture is intentional, not a candidate for MVI migration, and `SavedStateHandle` isn't used anywhere in the project — there's no restored state for such gating to protect. Test-order flakiness (the other motivation for that pattern) is already handled by `StandardTestDispatcher` + `advanceUntilIdle()` (see "Testing suspend `track` with Mokkery" below).

Both `GasStationsViewModel` and `GasStationDetailViewModel` take an optional trailing `initialState` constructor param (defaulting to `GasStationsUIState()`/`GasStationDetailUIState()`) purely so tests can seed a starting state in one line (`createSut(initialState = ...)`) instead of driving the ViewModel through several actions to reach it — production/Koin call sites never pass it and keep relying on the default.

View objects (VO suffix) live in the feature's `state/` package and contain display-ready data with calculated fields. Business objects (BO suffix) live in `core/domain` and are the source of truth.

`GasStationsUIState`/`GasStationItemVO`/`ContentState.Success` (and `GasStationDetailUIState`) no longer
carry `@Immutable` — `:core:presentation`, where they now live, can't depend on Compose. Their stability
is instead declared in `androidApp/compose_stability.conf` (wired via
`composeCompiler { stabilityConfigurationFiles.add(...) }` in `androidApp/build.gradle.kts`), which has
the exact same effect on the Compose compiler's skippability analysis. If you add a new flat `UIState`
data class with the same "safe to treat as stable" property, add its fully-qualified name to that file
rather than reaching for an annotation that isn't available.

## Local Persistence & Offline-First (Room)

`:data` persists gas stations in a Room database (`data/local/database/FuelioDatabase.kt`, KMP via `androidx.sqlite:sqlite-bundled`, entities/DAOs in `data/gasstation/local/`). Room's Gradle plugin manages the schema export to `data/schemas/` — commit that directory (don't gitignore it), it's the diffable history of schema changes.

`GasStationRepositoryImpl` implements a stale-while-revalidate strategy, not a simple network-then-cache:

- `getGasStationsByLocation(provinceId)` returns `Flow<Result<GasStationsResult>>` (`GasStationsResult(stations, isFromCache: Boolean)`, in `core/domain`) and can emit **twice**: cached rows first (`isFromCache = true`) if any exist, then the network result (`isFromCache = false`) after writing it back to Room. `Result` here models genuine network failure — `GasStationsResult`/`isFromCache` is business state, not part of the error channel. In the ViewModel, `notifyError` picks a "hard" error (`withError`, blocks the whole screen) only if `gasStations` is currently empty; if there's already content on screen (from cache or a prior load), a failed refresh goes to `withStaleDataError` instead, which keeps the list and just triggers a transient snackbar — never let a background refresh failure blank out data the user can already see.
- `getGasStationById(id)` returns a plain `Flow<GasStationBO?>` (no `Result`) sourced directly from a Room `Flow<GasStationEntity?>` query — it's a pure local read with no network path (the upstream MINETUR API has no fetch-by-station-id endpoint, only list-by-province/municipality/CCAA), so `null` is an expected business value ("not cached yet"), not a failure to wrap in `Result`. Being backed by Room's own `Flow` also makes it reactive: if the list screen refreshes that station's row in the background, an open detail screen updates without re-querying.

### Preferences (DataStore) and favourites

Two more things are persisted, both below the presentation layer and both identical on the two
platforms — see `docs/adr/0005-preferences-and-favorites-persistence.md`:

- **`UserPreferencesRepository`** (`:core:domain`, implemented in `:data`) over
  `androidx.datastore:datastore-preferences-core`: default fuel, saved province and theme mode,
  exposed as `Flow<UserPreferencesBO>` with one suspend setter each. The `-core` artifact is the
  pure-Kotlin one, the only variant that resolves for the iOS targets, and the backing file **must**
  end in `.preferences_pb`. `GasStationsViewModel` restores the province and fuel from it on launch,
  guarded by `hasRestoredSavedProvince`/`hasUserSelectedFuel` so a slow disk read can never overwrite
  a choice the user has already made.
- **Favourites** live in their own `favorite_stations` table, never as a column on `GasStationEntity`:
  `replaceGasStationsByProvince` is a `DELETE`+`INSERT` transaction on every refresh and would wipe
  the column each time. `FavoriteStationDAO.observeFavoriteStations()` is an `INNER JOIN`, so
  favourites whose province isn't cached right now drop out — hence
  `FavoriteStationsResult(stations, totalFavoriteCount)`, whose `unresolvedCount` the favourites
  screen surfaces as a banner rather than hiding.

`FuelioDatabase` is at version 2; `Migration1To2` is the first migration in the project and
`data/schemas/.../2.json` is committed. Any new entity needs the same pair.

## Analytics Tracking (`:core:analytics`)

- `Trace` (`Event`/`Screen`/`Error`) is an `open class`, not `data class` — features define a typed subclass per event/screen instead of instantiating `Trace.Screen(...)` inline with raw string keys, e.g. `GasStationDetailScreenViewed` in `feature/detail/analytics/`. `equals`/`hashCode`/`toString` are implemented manually (structural, matching by field), since `open class` can't be a `data class`.
- Concrete `Trace` subclasses live in `:core:presentation`'s `feature/*/analytics/` packages (moved from `composeApp` in P1.5 — they were already Compose-free, since they only depend on `:core:analytics`) — one file per event, never in `:core:analytics` itself. `:core:analytics` only holds generic, reusable abstractions (`Trace`, `Trackable`, `AnalyticsManager`, `AnalyticsProviderType`); promote a concrete event type there only once a second, independent module actually needs the same one.
- Each event is a `data object` (no params, e.g. `GasStationsScreenViewed`) or a `data class` (has params, e.g. `GasStationSelected(gasStationId)`), never a plain `class`. `data object`s can't reference their own `EVENT_NAME`/`SCREEN_NAME` from inside the `super(...)` call (Kotlin: "Cannot access before initialized") and can't nest a `companion object` — inline the string literal in `super(...)` and duplicate it in the public `const val`, unlike `data class`es where the `companion object` constant can be referenced directly in `super(...)`.
- Event/screen name naming convention (`EVENT_NAME`/`SCREEN_NAME` constants, snake_case): `<origin_screen>_<category>_<action>` — prefixed by the screen/feature it originates from so events group together in analytics dashboards, middle segment names the category (`location_permission`, `province`, `station`), suffix is the action in past tense (`_selected`, `_changed`, `_requested`, `_granted`, `_denied`). Screen names (`Trace.Screen.screenName`) are just the `<origin_screen>` slug with no category/action suffix (e.g. `gas_stations_list`, `gas_station_detail`) since the `Trace.Screen` type already disambiguates it as a screen view. Examples: `gas_stations_list_station_selected`, `gas_stations_list_province_changed`, `gas_stations_list_location_permission_denied`. Param keys (`PARAM_*` constants) are plain snake_case with no prefix (`gas_station_id`, `province_id`) since they're already scoped by their event.
- `Trace.Error` is the one exception to "one bespoke class per event": errors are homogeneous (screen + operation + error type/message), so instead of one class per screen/operation there's a single reusable `ApiCallFailed(screenName, operation, errorType, errorMessage)` in `:core:presentation`'s `feature/common/analytics/` (moved from `composeApp` in P1.5; shared across features within `:core:presentation`, not promoted to `:core:analytics` since only presentation-layer code needs it). Its `eventName` is built as `"${screenName}_${operation}_failed"`, keeping the same naming convention without hardcoding a string per screen — this scales to many screens/call sites without new files, at the cost of losing compile-time type-per-error (tests filter on `eventName`/params instead of a distinct type). `operation` is a free-form string constant per call site (e.g. `"fetch_provinces"`, `"fetch_stations"`); `errorType` is typically `domainError::class.simpleName`.
- Tracking failed API calls is done from the ViewModel (where `AnalyticsTracking` is wired), not from the Repository or `:data`/network layer — `:core:analytics` is a dependency of `:core:presentation` (see above), and only the ViewModel knows the screen/operation context the naming convention needs, plus the business distinction between a hard error and a stale-data/background-refresh error. Each ViewModel centralizes this in a private `trackApiCallFailed(operation, domainError)` helper called from its single error-handling function (e.g. `GasStationsViewModel.notifyError`), rather than duplicating the tracking call at every `onFailure` site — see `GasStationsViewModel` for the reference implementation.
- Second exception to "one class per event": when several events are really just different outcomes of the *same* flow (e.g. requesting location permission ends in requested/granted/denied/denied-permanently), consolidate them into one `data class` parametrized by an enum discriminant instead of one class per outcome — see `LocationPermissionEvent(outcome: LocationPermissionOutcome)` in `feature/list/analytics/`. `eventName` is built as `"${screen}_location_permission_${outcome.name.lowercase()}"`, so the enum constant names (`REQUESTED`, `GRANTED`, `DENIED`, `DENIED_PERMANENTLY`) double as the naming convention's action suffix — pick enum constant names accordingly. Reserve one-class-per-event for events that are genuinely distinct actions (`GasStationSelected`, `ProvinceChanged`), not different results of the same action.
- `AnalyticsTracking` is the consumer-facing interface (`suspend fun track(trace: Trace)`) — ViewModels depend on this, not on the concrete `AnalyticsManager` class, so it can be mocked directly in tests (`AnalyticsManager` itself is a plain `class`, not `open`/an interface, so Mokkery can't mock it — mock `AnalyticsTracking` instead).
- `Trackable`/`Tracker` is the per-provider contract (`FirebaseTracker`, `PostHogTracker`); `track` is `suspend` all the way down (`AnalyticsTracking` → `AnalyticsManager` → `Trackable`), so a provider implementation can call a suspend API later without having to break the interface.
- `AnalyticsProviderType` enum (`FIREBASE`, `POSTHOG`) plus each `Trace`'s `targets` list decide which registered trackers receive it — `AnalyticsManager.track` matches by `Trackable.type`.
- `AnalyticsContextProvider` (`expect`/`actual`) + `AnalyticsPlatformModule` reuse the `ContextProvider` pattern from `:data` (below), so `PostHogTracker` gets Android's `Context` without leaking it into `commonMain`.
- `AnalyticsSecrets.POSTHOG_API_KEY` is generated by BuildKonfig from `thirdparties.properties` (gitignored, not `local.properties`) or the `POSTHOG_API_KEY` env var in CI; blank/missing is a valid state that just keeps PostHog off.
- iOS native bridge functions (`registerNativeFirebaseTracker`, `registerNativePostHogTracker`) are called directly from Swift in `iOSApp.swift`, so `:core:presentation`'s `CorePresentation.framework` re-exports `:core:analytics` (`export(projects.core.analytics)`) to make those symbols visible.

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

`androidApp`'s `release` build type has `isMinifyEnabled` and `isShrinkResources` enabled, using `proguard-android-optimize.txt` plus `androidApp/proguard-rules.pro`. Verify `:androidApp:assembleRelease` after adding libraries that rely on reflection (Koin, kotlinx.serialization) — add narrow, specific keep rules to `proguard-rules.pro` only if R8 reports missing rules, rather than broad library-wide rules.

## Design System — shared tokens, two native UIs

"One design system, two native implementations," not one shared UI. Components are **never** shared:
`androidApp/.../designsystem/` (`FuelioCard`, `FuelioChip`, `FuelioSearchBar`, `FuelioTopBar`, …) is
Jetpack Compose only; iOS screens use native SwiftUI components (`List`, `NavigationStack`,
`.searchable`, `Button`) directly. What *is* shared is the underlying **values** — colors, spacing,
radii, type scale — so both platforms render the same brand off one source of truth, verifiable at
compile time instead of by convention:

- `core/presentation/src/commonMain/.../core/designtokens/` (`FuelioColorTokens`, `FuelioSpacingTokens`,
  `FuelioRadiusTokens`, `FuelioTypeTokens`) — `object`s of `const val` primitives (ARGB `Long`s for
  color, `Double` points for spacing/radius/type size). `const val` is what makes Kotlin/Native export
  them as plain static constants Swift can read directly (`FuelioColorTokens.shared.ACCENT_LIGHT`).
- `androidApp/.../core/ui/theme/` — the Compose adapter. `Color.kt`/`Spacing.kt`/`Shape.kt`/
  `Typography.kt` keep their existing public API (`FuelioSpacing.md`, `LightColorScheme`,
  `FuelioTypography`, …) unchanged; only the literals inside now read from the tokens
  (`Color(FuelioColorTokens.DEEP_ORANGE_600)`). `Elevation.kt` is intentionally **not** token-backed —
  Material's tonal elevation has no iOS equivalent.
- `iosApp/iosApp/DesignSystem/` — the SwiftUI adapter (`FuelioColors`, `FuelioSpacing`, `FuelioRadius`,
  `Font.fuelio(...)`, `.fuelioTheme()`). Deliberately **not** a 1:1 port of Material: only brand +
  semantic color roles cross over (no `primaryContainer`/`onSurfaceVariant`/`surfaceTint`-style Material
  roles — use system colors/materials for those), and typography maps token sizes onto iOS's Dynamic
  Type text styles (`Font.custom(_:size:relativeTo:)`) instead of Material's 15 fixed sizes, so iOS text
  still respects the user's system text-size setting.

**Rule:** a new token value is added to `core/designtokens/` first, never hardcoded directly in either
platform adapter. See `docs/adr/0003-shared-design-tokens-in-kotlin.md` for the alternatives
considered (hand duplication, Style Dictionary codegen) and why this approach won.

## iOS app architecture (`iosApp/`)

Native SwiftUI over `CorePresentation.framework`, using three interop libraries that each cover a
different exporter limitation — see `docs/adr/0004-kotlin-swift-interop-libraries.md`:

| Library | Version | Covers |
|---|---|---|
| KMP-ObservableViewModel | 1.0.5 | ViewModel base class + lifetime (`@StateViewModel` on the Swift side) |
| KMP-NativeCoroutines | 1.0.4 | `StateFlow`/`Flow` and `suspend` across the bridge, with real cancellation |
| SKIE | 0.10.14 | `sealed` → exhaustive Swift enums (`onEnum(of:)`), Kotlin enums → Swift enums |

**SKIE and KMP-NativeCoroutines overlap on flows/suspend** — they are alternatives for that, not
complements. KMP-NativeCoroutines owns flows here; SKIE is kept only for the sealed/enum rows.
All three are pinned to the builds made for **Kotlin 2.4.0**; the next releases target 2.4.10 and will
not link, so a Kotlin bump must move all of them together.

Deployment target **iOS 18.2**, **Swift 5 language mode** (Swift 6 strict concurrency is not viable:
nothing the Kotlin/Native exporter emits is `Sendable`, so it would produce a wall of warnings that
cannot be fixed from Swift; `@MainActor` discipline is used instead).

### Layout

```
iosApp/iosApp/
  App/            iOSApp (entry point), RootView, AppRouter, LaunchArguments
  Navigation/     Route (Destination → SwiftUI route), RouterAction
  Features/
    GasStations/  GasStationsScreen, Components/
    Detail/       GasStationDetailScreen, Components/
  DesignSystem/   token adapters (FuelioColors/Spacing/Radius/Typography) + Components/
  Support/        KotlinSealed, KMPObservableViewModel, LazyStore, BrandLogo, formatting, A11yID
  Debug/          DesignTokensStorybook (preview-only, kept per ADR 0003)
  Resources/      Localizable.xcstrings, InfoPlist.xcstrings, Fonts/
iosApp/iosAppTests/     Swift Testing — mappings, design tokens, localization, interop integration
iosApp/iosAppUITests/   XCTest — XCUITest flows (Swift Testing does not host UI automation)
```

Screens hold the Kotlin ViewModel directly; there is no Swift `Store` or `Backend` layer.

### The Kotlin side (`core/presentation/src/iosMain/.../core/interop/`)

- `IosViewModelFactory` (`object`, reached as `IosViewModelFactory.shared` from Swift) — the **only**
  hand-written interop left. Both ViewModels are registered in Koin with resolution parameters
  (`LocationPermissionController` for the list, `Destination.GasStationDetails` for the detail) and no
  library resolves those, so the `parametersOf` work happens here and Swift passes plain values. Also
  exposes the shared `Navigator` plus an `isolatedNavigator()` for tests — `navigationActions` is
  `Channel`-backed and single-consumer, so a test subscribing to the shared one would steal the running
  app's events.
- `UiTestModule.kt` + `initKoinIosForUiTests(simulateStationFailure:)` — test-only Koin overrides,
  replacing only the outermost boundary (repositories, permission prompt).

ViewModel lifetime and state observation are **not** hand-written: `@StateViewModel` clears the
ViewModel (cancelling `viewModelScope`) when the view goes away, and `@NativeCoroutinesState` makes
the exporter emit a plain typed `state` property instead of an erased `StateFlow`.

### Rules for adding a screen

1. Add the ViewModel/UIState to `:core:presentation` (never to `iosApp`). The ViewModel must extend
   `com.rickclephas.kmp.observableviewmodel.ViewModel` and build its state with
   `MutableStateFlow(viewModelScope, …)` — the plain kotlinx builder repaints Android but never iOS.
2. Annotate the public state `@NativeCoroutinesState`. Keep private/internal flows as plain kotlinx
   flows: they are plumbing Swift never observes and must not pay the notification cost.
3. Add a factory function to `IosViewModelFactory` if the Koin definition takes parameters.
4. Hold it in the view with `@StateViewModel`, never plain `@State`: `@State` evaluates its initial
   value on every `View` struct init, which would resolve a ViewModel from Koin and fire a screen-view
   event on every re-render. Read `viewModel.state` directly and call the ViewModel's own methods —
   do **not** re-map the Kotlin `UIState` into a parallel Swift struct.
5. Map any new Kotlin sealed type in `Support/KotlinSealed.swift` — **only there** — with
   `onEnum(of:)`, and cover every variant in `KotlinSealedMappingTests`. Because SKIE makes that
   `switch` exhaustive, a variant added in Kotlin is a compile error rather than a silent `default`.
   **Never express a cross-cutting property (e.g. "is this destination linkable") as a sealed
   sub-interface over an exported sealed type** — SKIE turns every direct subtype into a Swift enum
   case, sub-interfaces included, and a concrete type implementing both the exported type and the
   marker sub-interface gets shadowed by the sub-interface's case in `onEnum(of:)`, silently making its
   own case dead code (see ADR 0004's Consequences for the incident this caused with
   `Destination`/`DeepLinkDestination`). Use an extension property/function with an exhaustive `when`
   instead — same compile-time guarantee, no effect on the exported enum.
6. Do **not** annotate `suspend` functions with `@NativeCoroutines` unless Swift actually calls them:
   the annotation replaces the exported `async` form with a closure that must be invoked via
   `asyncFunction(for:)`, and calling it as `try await` compiles with only a warning while doing
   nothing at all.
7. Add `A11yID` entries and mirror them in `iosAppUITests/UITestSupport.swift`
   (`AccessibilityIdentifierContractTests` fails if the two diverge).
8. Add every user-facing string to `Resources/Localizable.xcstrings` in EN **and** ES, and to the
   `uiKeys` table in `LocalizationTests`. Debug-only copy uses `Text(verbatim:)`.

### Invariants

- **Business logic stays in Kotlin.** No recomputing "cheapest", filtering by fuel, sorting by distance
  or deciding open/closed in Swift. The one exception is the detail screen's "cheapest of these four
  prices" highlight, which is presentation-only and which Android also does in its Composable.
- **Navigation goes through the ViewModel**, never by pushing/popping `NavigationStack` directly —
  otherwise the analytics events attached to those actions never fire.
- **Formatting comes from Kotlin** (`NumberFormatterKt.formatAsEuros/formatAsKilometers`). Both
  `format(digits:)` actuals are locale-aware on purpose, so tests assert shape, never a literal.
- **No hardcoded design values.** New token → `core/designtokens/` first (ADR 0003); `DesignSystemTests`
  compares the Swift adapter against the Kotlin tokens.
- **The generated header is the source of truth** for Kotlin symbol names. Regenerate with
  `:core:presentation:linkDebugFrameworkIosSimulatorArm64` and read
  `.../CorePresentation.framework/Headers/CorePresentation.h` rather than guessing. Note in particular
  that the two `ContentState` sealed interfaces collide once Objective-C flattens packages away and
  are exported as `ContentState` (detail) and `ContentState_` (list) — names that must not appear
  outside `Support/KotlinSealed.swift`.
- Kotlin **default arguments and `copy()` do not cross the bridge**. Build Kotlin objects from the
  exported fakes (`GasStationsFakesKt`, `FakeGasStationsKt`) and mutate state only through ViewModel
  actions.
- Optional Kotlin primitives arrive boxed (`KotlinDouble?`); unwrap them in
  `Support/GasStationFormatting.swift`, not in views.

### Brand assets — pending

`BrandLogo` derives an asset name from the Kotlin enum (`REPSOL` → `logo_repsol`), but **no brand
artwork exists yet**: Android's logos are XML vector drawables and cannot be reused on iOS. Every
brand currently renders the `fuelpump.fill` SF Symbol fallback. Dropping correctly named images into
`Assets.xcassets` is all that is needed — no code change.

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
- `CrashReporting` (`core/logger`) is the crash-reporting entry point, deliberately **separate** from `AppLogger`: `AppLogger` is a dependency-free `expect object`, so it can't hold a reporter. `CrashReporting.logError(tag, message, throwable)` logs *and* forwards a non-fatal to the `CrashReporter` installed at startup (`FirebaseCrashlytics` on Android; a Swift bridge registered via `registerNativeCrashReporter` on iOS, falling back to `NoOpCrashReporter` if nothing registers). `initKoin` installs it synchronously **before** launching the startup tasks, since those run concurrently and one failing first would otherwise report into a no-op.
- ANR-WatchDog (`androidMain`, `AndroidApplication.onCreate`) is active only when `FLAG_DEBUGGABLE` is set, with `setIgnoreDebugger(true)` so it still fires while running under the Android Studio debugger. On detection it logs the full multi-thread stack trace via `AppLogger.e("ANRWatchDog", ...)` — filter Logcat by that tag to inspect.