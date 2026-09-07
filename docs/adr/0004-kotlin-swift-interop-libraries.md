# 0004 — Kotlin↔Swift interop with KMP-ObservableViewModel, KMP-NativeCoroutines and SKIE

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
- Kotlin `enum`s arrive as opaque classes, so a Swift `switch` over `GasStationBrand` cannot be
  exhaustive either.
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

Adopt the stack JetBrains documents for the iOS side of a Multiplatform ViewModel, plus SKIE for the
part that stack does not cover.

The three libraries solve **different** problems. This table is the reasoning that matters, because
the first two are routinely mistaken for alternatives to each other:

| Exporter limitation | SKIE | KMP-NativeCoroutines | KMP-ObservableViewModel |
|---|:--:|:--:|:--:|
| `StateFlow<T>` erased to a raw `StateFlow` | ✅ | ✅ | — |
| `suspend` → uncancellable completion handler | ✅ | ✅ | — |
| `sealed` → protocol with no exhaustiveness | ✅ | — | — |
| Kotlin `enum` → opaque class | ✅ | — | — |
| Default arguments stripped | ✅ | — | — |
| No `ViewModelStoreOwner` on iOS | — | — | ✅ |

**SKIE and KMP-NativeCoroutines overlap on flows and suspend functions — they are alternatives for
those two rows, not complements.** KMP-ObservableViewModel is orthogonal to both and pairs with
either. KMP-NativeCoroutines owns flows here (it is what the JetBrains guide and
KMP-ObservableViewModel's own documentation assume); SKIE is kept only for the rows nothing else
covers.

Concretely:

1. **KMP-ObservableViewModel 1.0.5** — the ViewModels extend
   `com.rickclephas.kmp.observableviewmodel.ViewModel` and build their state with
   `MutableStateFlow(viewModelScope, …)`, the overload that notifies SwiftUI. Swift holds them with
   `@StateViewModel`, which creates the ViewModel once per screen and clears it — cancelling
   `viewModelScope` — when the view goes away.
2. **KMP-NativeCoroutines 1.0.4** — `@NativeCoroutinesState` on each ViewModel's `state` and
   `@NativeCoroutines` on `Navigator.navigationActions`. The first makes the exporter emit a plain,
   typed `GasStationsUIState *state` property instead of an erased `StateFlow`; the second gives
   Swift an `AsyncSequence` whose cancellation propagates back into the coroutine.
3. **SKIE 0.10.14** — `onEnum(of:)` for the sealed hierarchies and real Swift enums for Kotlin enums.

What survives by hand is exactly one thing: **`IosViewModelFactory`**, ~50 lines, because both
ViewModels are registered in Koin with resolution parameters and no library resolves those.

Sealed conversion still happens in exactly one place per type (`Support/KotlinSealed.swift`,
`Navigation/Route.swift`) — but now the `switch` is exhaustive, so a variant added in Kotlin is a
**compile error** there rather than a `default` branch hit at runtime.

## Alternatives considered

**A hand-rolled bridge in `iosMain`.** This was the original decision recorded in this ADR, and it
was actually built: `FlowSubscription`, `IosViewModelHandle` (a per-screen `ViewModelStore` whose
`close()` reached `onCleared()`), three `Ios*Binding` types and `IosBindingFactory` — ~250 lines of
Kotlin — plus ~330 lines of Swift scaffolding (`Store`s, `Backend` protocols, `StateSubscription`,
spies). Its stated advantages were real: no new dependencies, a one-party Kotlin upgrade, and the
interop model stayed visible instead of being automated away.

It was replaced because the cost turned out to be **structural rather than one-off**. Every new
screen needed its own binding, its own `Backend` protocol (the exported ViewModels are
`objc_subclassing_restricted`, so a protocol was the only way to fake them), its own `Store` and its
own spy. That is a per-screen tax, and the original ADR's own exit condition — "if the shared Kotlin
surface grows substantially, revisit" — was met at the second screen. Following documented,
maintained tooling also demonstrates more than re-deriving it.

**Adopt only SKIE.** Tempting: one plugin, no `commonMain` changes at all, and it covers flows,
suspend, sealed and enums in one go. Rejected because it does **not** address ViewModel lifetime,
which is the part that actually leaks. `IosViewModelHandle` and its per-screen `ViewModelStore` would
have had to stay, and with them the `Store`/`Backend` layering that made up most of the deleted code.

**Adopt only the JetBrains stack, without SKIE.** This is the officially documented configuration and
was the fallback if SKIE conflicted with KMP-NativeCoroutines — a combination neither project
documents. It coexisted cleanly, so SKIE was kept; without it `Support/KotlinSealed.swift` keeps a
`default` branch per sealed type that silently swallows any variant added on the Kotlin side.

**Annotate the `Navigator` suspend functions with `@NativeCoroutines` too.** Rejected after it caused
a real, silent failure. The annotation *replaces* the exported `async` function with a closure that
must be invoked through `asyncFunction(for:)`; calling it as `try await` still compiles — with only a
warning — and does nothing at all. Swift never navigates directly (screens call a ViewModel action so
the analytics fire), so only `navigationActions` is annotated.

**Consume the raw exported API directly from SwiftUI (no bridge, no libraries).** Rejected: it would
put `Kotlinx_coroutines_coreFlowCollector` conformances, `ContentState_` and `KotlinDouble` unwrapping
into view code, and leave `viewModelScope` uncancelled.

## Consequences

- `core/interop` is one file instead of six. The Swift `Store`s, `Backend` protocols,
  `StateSubscription` and the spies are gone: a new screen needs a factory function, an annotation
  and `@StateViewModel`.
- **Android is unaffected.** The library's Android `actual` is
  `abstract class ViewModel: AndroidXViewModel`, so Koin's `viewModel {}` DSL and `koinViewModel`
  resolve unchanged. Not one file under `:androidApp` was touched and its tests pass as-is.
- Adding a variant to a Kotlin sealed type is now a compile error in `Support/KotlinSealed.swift` or
  `Navigation/Route.swift`, not a runtime `default`.
- Three Gradle dependencies and two SPM packages, each pinned to the build made for **Kotlin 2.4.0**
  (the next releases target 2.4.10 and do not link). A Kotlin bump is now a four-party upgrade — the
  cost the hand-rolled bridge was avoiding, accepted deliberately in exchange for deleting the
  plumbing. Move all of them together.
- **SKIE roughly triples the framework link step**, taking full Gradle verification from well under a
  minute to ~4m40s. This is the main ongoing price and the first thing to drop if it starts to hurt:
  it is isolated in its own commit and nothing else depends on it.
- The Swift-side unit-test seam is gone with the `Backend` protocols, since the exported ViewModels
  cannot be spied on. State-transition coverage lives in `commonTest`, which is where the "business
  logic stays in Kotlin" invariant says it belongs, and the flows are still covered end-to-end by the
  XCUITests.
- `LazyStore` is kept for `AppRouter`: it is a plain Swift observable rather than a ViewModel, so
  `@StateViewModel` does not apply and it still needs create-once semantics that `@State` does not
  give.
- **Caveat found while wiring deep linking:** the "adding a variant is a compile error" guarantee above
  holds only for direct subtypes of the exported sealed type itself — it does not extend to a *sealed
  sub-interface* layered over it. A `DeepLinkDestination: Destination` marker interface was tried first
  to type-check "is this destination linkable"; SKIE turns every direct subtype of a sealed interface
  into a `case` of the generated Swift enum, sub-interfaces included, so `Destination`'s enum grew a
  third case for `DeepLinkDestination` alongside its two real screens. Because `GasStationDetails`
  implemented both, the generated `onEnum(of:)` matched `case .deepLinkDestination` before the concrete
  `case .gasStationDetails`, making the concrete case unreachable dead code — silently, with no compile
  error, which is exactly the failure mode this ADR's library choice exists to prevent. The fix was to
  drop the sub-interface and express "is linkable" as data instead of type (an `internal val
  Destination.parent: Destination?` extension property matched with a non-`else` `when`), which keeps
  `Destination`'s own subtype list — and therefore its exported Swift enum — untouched. Lesson: never
  add a sealed sub-interface over a type exported through SKIE; extension properties/functions with an
  exhaustive `when` give the same compile-time guarantee without perturbing the enum.
