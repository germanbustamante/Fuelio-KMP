# 0004 — Hand-rolled Kotlin↔Swift bridge instead of SKIE or KMP-NativeCoroutines

- **Status:** Accepted
- **Date:** 2026-08-06
- **Depends on:** [0001](0001-kmp-with-native-ui-instead-of-compose-multiplatform.md),
  [0002](0002-core-presentation-module-without-compose.md)

## Context

[0001](0001-kmp-with-native-ui-instead-of-compose-multiplatform.md) decided the iOS UI is native
SwiftUI consuming `CorePresentation.framework`. That framework is produced by the Kotlin/Native
Objective-C exporter, and the exporter loses exactly the things SwiftUI needs most:

- `StateFlow<GasStationsUIState>` is exported as the **raw, unparameterized**
  `id<Kotlinx_coroutines_coreStateFlow>` — the generic argument is erased, and there is no
  `AsyncSequence` conformance. Consuming it from Swift means calling `collect(collector:)` with a
  hand-written `FlowCollector`, in a coroutine Swift cannot cancel.
- `suspend fun` becomes a completion handler. Cancelling the Swift `Task` that awaits it does
  **not** cancel the Kotlin coroutine.
- `sealed interface ContentState` becomes an Objective-C protocol with no exhaustiveness. Worse,
  the project has two of them (`feature.list.state.ContentState` and
  `feature.detail.state.ContentState`) and Objective-C has no namespaces, so the exporter silently
  disambiguates them as `ContentState` (detail) and `ContentState_` (list) — a name that is not
  stable under refactoring and must never leak into view code.
- The `ViewModel`s are `androidx.lifecycle.ViewModel`. `ViewModel.clear()` is not public API, so
  from Swift there is no exported way to cancel `viewModelScope`; a screen that closes would leave
  its coroutines running.
- Both `ViewModel`s are registered in Koin **with parameters**
  (`viewModel { (controller: LocationPermissionController) -> ... }`), and
  `LocationPermissionController` is not registered at all — on Android `MainActivity` injects it.
  Resolving that from Swift would mean building `ParametersHolder` by hand through the exported
  Koin API.

Something has to close that gap. The question is what.

## Decision

Write the bridge by hand, in `core/presentation/src/iosMain/`, as three small, focused pieces:

1. **`FlowBridge`** (`core/interop/FlowBridge.kt`) — `Flow<T>.subscribe(onEach:)` returning a
   `Cancellable` handle. Collection runs on a `CoroutineScope(Dispatchers.Main.immediate + Job())`,
   so emissions reach Swift already on the main thread and `cancel()` really tears the coroutine
   down. Concrete, non-generic entry points (`IosGasStationsBinding.observeState`,
   `IosNavigationBinding.observeNavigation`) sit on top so Swift never sees an erased generic.
2. **`IosViewModelHandle`** (`core/interop/IosViewModelHandle.kt`) — owns an
   `androidx.lifecycle.ViewModelStore` per screen. `close()` calls `store.clear()`, which is the
   supported way to reach `ViewModel.onCleared()`/`viewModelScope.cancel()` from outside the
   lifecycle library. This is the same mechanism `ViewModelStoreOwner` uses on Android.
3. **`IosViewModelFactory`** (`core/interop/IosViewModelFactory.kt`) — flat, parameterless-looking
   factory functions (`createGasStationsBinding()`, `createGasStationDetailBinding(gasStationId:)`,
   `navigationBinding()`) that do the Koin `parametersOf` work on the Kotlin side.

Sealed-class exhaustiveness stays a **Swift-side** concern, converted in exactly one place per
sealed type (`Support/KotlinSealed.swift`), producing real Swift enums. If Kotlin gains a variant,
one file stops compiling instead of many views silently falling into `default`.

## Alternatives considered

**SKIE (`co.touchlab.skie`).** A Kotlin compiler plugin that regenerates a Swift layer on top of
the framework: `sealed` → Swift `enum` + `onEnum(of:)`, `Flow` → `AsyncSequence`, `suspend` →
`async` with two-way cancellation, default arguments restored. It supports Kotlin 2.4.0 (SKIE
tracks Kotlin closely, currently 2.0.0–2.4.10), so compatibility is *not* the blocker it would have
been a year ago. Rejected for this repository because:

- It is a **compiler plugin pinned to the exact Kotlin version**. Every Kotlin bump becomes a
  two-party upgrade (Kotlin + SKIE), and this project bumps Kotlin aggressively (2.4.0 today).
- The surface that actually needs bridging here is tiny: two `StateFlow`s, one navigation `Flow`,
  two factories. Introducing a compiler plugin for ~200 lines of Kotlin is a poor trade.
- It would **hide the interop model**, which is the opposite of this repository's stated purpose
  (see `CLAUDE.md`, "Product Naming & Repo Scope"): demonstrating a real Kotlin↔Swift bridge is the
  deliverable, not an implementation detail to be automated away.

This is a decision scoped to a portfolio project. **On a production app with a wide Kotlin surface
(dozens of sealed hierarchies, many `Flow`s, heavy use of default arguments), SKIE is the right
answer** and the hand-rolled bridge would not scale.

**KMP-NativeCoroutines (`com.rickclephas.kmp`).** Annotation (`@NativeCoroutines`) + compiler
plugin generating `…Native` properties that expose `Flow`/`suspend` in a Swift-friendly, cancellable
form, with a first-class `swift-async-algorithms`/Combine story. Also currently tracks Kotlin 2.4.0.
Rejected for the same "compiler plugin pinned to a Kotlin version" reason, plus a narrower benefit:
it solves only the coroutine half of the problem. Sealed exhaustiveness, `ViewModel` lifetime and
parametrized Koin resolution would still need the hand-written pieces above, so it would not
actually remove code — only add a dependency.

**Consume the raw exported API directly from SwiftUI (no bridge).** Rejected: it would put
`Kotlinx_coroutines_coreFlowCollector` conformances, `ContentState_` and `KotlinDouble` unwrapping
into view code, and leave `viewModelScope` uncancelled.

## Consequences

- No new Gradle plugins, no new SPM packages. A Kotlin upgrade is a one-party upgrade.
- The bridge is **the** place where Kotlin concurrency touches Swift; it is unit-tested from Kotlin
  (`core/presentation/src/iosTest/`) and integration-tested from Swift (`iosAppTests`). Views never
  see a `Flow`, a `Job` or a `KotlinDouble`.
- Every new screen must add its own `Ios*Binding` factory function. That is deliberate friction: it
  keeps the exported Swift surface flat and non-generic, which is what survives the Objective-C
  exporter well.
- Adding a variant to a Kotlin sealed type is a **breaking change for iOS** that must be reflected
  in `Support/KotlinSealed.swift`. The Swift unit tests cover the existing variants so the omission
  shows up as a failing test rather than a silent `default` branch.
- If the shared Kotlin surface grows substantially, revisit SKIE. This ADR is a decision about
  *scale*, not a rejection of the tool.
