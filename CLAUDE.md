# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

```bash
# Android
./gradlew :composeApp:assembleDebug        # Build debug APK
./gradlew :composeApp:installDebug         # Install on connected device

# Tests
./gradlew test                             # All unit tests
./gradlew :core:domain:test                # Domain module tests only
./gradlew :data:test                       # Data module tests only
./gradlew connectedAndroidTest             # Android instrumentation tests

# iOS
# Open iosApp/iosApp.xcodeproj in Xcode and run from there
```

## Module Architecture

Three Gradle modules with strict Clean Architecture layering:

```
:core:domain   →  Business logic only (no platform deps)
:data          →  Repository implementations, Ktor HTTP client
:composeApp    →  Compose Multiplatform UI, ViewModels, DI wiring
```

Dependencies flow one way: `composeApp → data → core:domain`. The domain module has zero platform or framework dependencies.

## Kotlin Multiplatform Targets

- **Android**: `composeApp/src/androidMain/` — OkHttp engine, `AndroidApplication` initializes Koin
- **iOS**: `composeApp/src/iosMain/` + `iosApp/` (Swift) — Darwin engine, `iOSApp.swift` calls `KoinInitIosKt.doInitKoinIos()`
- **Common**: `*/src/commonMain/` — platform-agnostic code shared across targets

Platform-specific HTTP engine selection uses `expect`/`actual` in `data/src/*/kotlin/.../engine/HttpClientEngineProvider.kt`.

## Dependency Injection (Koin)

Uses Koin with annotation processing (`@Module`, `@Singleton`, `@KoinViewModel`, `@ComponentScan`). Each feature/layer provides its own module:

- `DomainModule` — scanned via `@ComponentScan` in `core/domain`
- `DataModule` — in `data/src/commonMain/.../di/`
- `GasStationListModule` — in `composeApp`, pulls in Data + Domain modules
- `KoinInit.kt` — aggregates all modules for initialization

When adding a new use case or repository, annotate with `@Single` or `@Factory` and ensure the module has `@ComponentScan` pointing to the right package, or register it explicitly.

## State Management Pattern

ViewModels expose `StateFlow<UIState>` where `UIState` is a sealed class with `Loading`, `Success(data)`, and `Error` variants. UI collects via `collectAsStateWithLifecycle()`.

View objects (VO suffix) live in the feature's `state/` package and contain display-ready data with calculated fields. Business objects (BO suffix) live in `core/domain` and are the source of truth.

## Key Versions

- AGP: 9.2.1, Kotlin: 2.4.0, Compose Multiplatform: 1.11.1
- Koin BOM: 4.2.2, Ktor: 3.5.1
- Android minSdk: 26, compileSdk/targetSdk: 37

## R8 / Release Builds

`composeApp`'s `release` build type has `isMinifyEnabled` and `isShrinkResources` enabled, using `proguard-android-optimize.txt` plus `composeApp/proguard-rules.pro`. Verify `:composeApp:assembleRelease` after adding libraries that rely on reflection (Koin, kotlinx.serialization) — add narrow, specific keep rules to `proguard-rules.pro` only if R8 reports missing rules, rather than broad library-wide rules.

## Debug Tooling

- `AppLogger` (`expect`/`actual` in `core/logger`, per source set) wraps platform logging (`android.util.Log` on Android). Prefer it over direct platform log calls in shared code; avoid calling it from `Composable` getters or other code paths Compose may invoke multiple times per frame.
- ANR-WatchDog (`androidMain`, `AndroidApplication.onCreate`) is active only when `FLAG_DEBUGGABLE` is set, with `setIgnoreDebugger(true)` so it still fires while running under the Android Studio debugger. On detection it logs the full multi-thread stack trace via `AppLogger.e("ANRWatchDog", ...)` — filter Logcat by that tag to inspect.