# 0007 — Koin over Hilt for dependency injection

- **Status:** Accepted
- **Date:** 2026-09-14

## Context

Fuelio needs one DI graph shared across `:core:domain`, `:core:analytics`, `:data`,
`:core:presentation` and the two platform entry points. Hilt is Google's recommended DI framework
for Android and generates its graph at compile time via KSP, catching a missing binding as a build
error instead of a runtime crash.

Hilt is Android-only: it is built on `dagger.android`, generates code against `Application`/
`Activity`/`Fragment` lifecycles, and has no Kotlin/Native target. `:core:domain`,
`:core:analytics`, `:data` and `:core:presentation` all compile to `iosArm64`/
`iosSimulatorArm64` in addition to the JVM (see "Kotlin Multiplatform Targets" in `CLAUDE.md`), so a
Hilt-based graph could only ever cover the Android half of the app; the iOS side (`iOSApp.swift`
calling `doInitKoinIos()`) would need an entirely separate DI mechanism, duplicating every module
declaration and drifting out of sync by construction.

## Decision

Use Koin, with plain DSL (`module { single {} / factory {} / viewModel {} }`) rather than
`koin-annotations`.

Koin's graph is plain Kotlin resolved at runtime by a `ClassLoader`-free service locator, so the
exact same module declarations (`DomainModule`, `DataModule`, `GasStationListModule`, …) compile
into both the Android library and the `CorePresentation.framework` Kotlin/Native export, and
`KoinInit.kt`'s `initKoin()` runs from `commonMain` — one call site for both
`AndroidApplication.onCreate` and `doInitKoinIos()`. `org.koin.core.module.dsl.viewModel` (koin-core)
provides the `viewModel {}` builder without depending on `koin-compose-viewmodel`, which is what
lets `GasStationListModule`/`GasStationDetailModule` live in `:core:presentation` instead of
`:androidApp`.

`koin-annotations` and `koin_plugin` are present in the version catalog but deliberately not wired
into any module's `build.gradle.kts`. They were evaluated and rejected for this project specifically
(not as a blanket rejection of annotation-based Koin): `@Single`/`@Factory`/`@ComponentScan`
processing runs through KSP, which is itself JVM/Android-first — Kotlin/Native KSP support exists
but is less mature, and mixing annotation-scanned bindings on Android with hand-written `module {}`
blocks on iOS would produce two different mental models of the same graph depending on platform,
which is worse than one uniform (if more verbose) DSL everywhere. The dependency stays in the
catalog as a reminder that it was considered, not an oversight.

## Alternatives considered

**Hilt for Android, hand-rolled service locator for iOS.** Rejected: two DI mechanisms means every
new binding is written twice, in two idioms, and nothing enforces that they stay equivalent — the
exact duplication risk KMP is supposed to eliminate.

**Kodein or another KMP-native DI framework.** Not seriously evaluated: Koin was already the
project's choice from the very first Android-only commits, and switching frameworks after
`:core:presentation` was extracted (ADR 0002) would touch every DI module in the repo for a lateral
move with no compile-time-safety gain over the current setup.

**Wire `koin-annotations` now that `:core:presentation` is stable.** Considered again while writing
this ADR, since the module graph is no longer expected to churn as much as during the P1.5
migration. Deferred, not rejected outright: it would remove the manual bookkeeping in
`StartupModule.kt`'s hand-assembled `Set<StartupTask>` (no Koin equivalent of Hilt's `@IntoSet`
either way, annotations or not) and in each feature module's `factory {}` list, but the win is
mostly ergonomic and the migration touches every DI file at once — better done as its own change
than folded into an unrelated feature.

## Consequences

- Zero compile-time verification of the Koin graph. A missing `single {}`/`factory {}` for a new use
  case surfaces as `NoDefinitionFoundException` at the first resolution attempt, not as a build
  error — the project's test suite (constructing each ViewModel through its Koin module in
  `IosViewModelFactoryTest`, plus the deterministic `uiTestModule`/`initKoinIosForUiTests` fakes used
  by both platforms' instrumentation) is what catches this in practice, not the compiler.
- Registering a new use case or repository is always an explicit, two-line diff (see "Dependency
  Injection (Koin)" in `CLAUDE.md`) — there is no scanning step, so nothing is registered by
  accident and nothing is silently picked up by a stray annotation.
- The same module set initializes both platforms from `commonMain`, so there is exactly one place
  (`KoinInit.kt`) that can drift from what either platform actually needs.
