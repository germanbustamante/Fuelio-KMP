# Fuelio

[![CI](https://github.com/germanbustamante/Fuelio-KMP/actions/workflows/ci.yml/badge.svg?branch=development)](https://github.com/germanbustamante/Fuelio-KMP/actions/workflows/ci.yml)

A gas-station finder built with Kotlin Multiplatform: shared domain/data/presentation logic in
Kotlin, **native UI on each platform** — Jetpack Compose on Android, SwiftUI on iOS. See
[`docs/adr/`](./docs/adr) for the architectural decisions behind that split, and `CLAUDE.md` for the
full module-by-module reference.

## Modules

```
:core:domain        →  Business logic only (no platform deps)
:core:analytics      →  Analytics tracking abstraction (Trace/Trackable/AnalyticsManager) + Firebase/PostHog
:data                →  Repository implementations, Ktor HTTP client, Room local persistence
:core:presentation   →  ViewModels, UIState, Navigator/Destination, DI modules, shared design tokens — no Compose
:androidApp          →  Jetpack Compose UI (Android-only application module)
iosApp (Xcode)       →  SwiftUI UI, consumes the CorePresentation.framework built from :core:presentation
```

`:core:presentation` is the module that gets exported as a Kotlin/Native framework
(`CorePresentation.framework`) for Xcode to link — it has zero Compose/Navigation3 dependencies by
design (see [ADR 0002](./docs/adr/0002-core-presentation-module-without-compose.md)).

## Build and run — Android

```shell
./gradlew :androidApp:assembleDebug
./gradlew :androidApp:installDebug
```

Or use the run configuration from the IDE's toolbar.

## Build and run — iOS

Open [`iosApp/iosApp.xcodeproj`](./iosApp/iosApp.xcodeproj) in Xcode and run from there. The
"Compile Kotlin Framework" build phase invokes
`./gradlew :core:presentation:embedAndSignAppleFrameworkForXcode` automatically, so no manual Gradle
step is needed first.

Swift dependencies come in via Swift Package Manager (no CocoaPods): Firebase, PostHog, and the Swift
halves of the two interop libraries — `KMPObservableViewModelSwiftUI` and `KMPNativeCoroutinesAsync`.
Those two are pinned to the exact versions of their Gradle counterparts; the halves are released in
lockstep and a mismatch fails at link time. See
[`docs/adr/0004-kotlin-swift-interop-libraries.md`](./docs/adr/0004-kotlin-swift-interop-libraries.md)
for what each library covers.

Note that SKIE roughly triples the framework link step, so a cold iOS build is noticeably slower than
the Kotlin-only tasks suggest.

From the command line:

```shell
xcodebuild build -project iosApp/iosApp.xcodeproj -scheme iosApp \
  -destination 'platform=iOS Simulator,name=iPhone 17,OS=latest'
```

To sanity-check the framework outside Xcode — and to regenerate the Objective-C header, which is the
source of truth for every Kotlin symbol Swift can see:

```shell
./gradlew :core:presentation:linkDebugFrameworkIosSimulatorArm64
open core/presentation/build/bin/iosSimulatorArm64/debugFramework/CorePresentation.framework/Headers/CorePresentation.h
```

## Tests

### Kotlin

```shell
./gradlew :androidApp:testDebugUnitTest              # Compose-dependent code (designsystem, screens)
# Every KMP module's commonTest runs on the JVM as well as on the simulator. The host tasks are the
# fast loop (and what CI runs on Linux); the simulator tasks additionally cover each module's
# iosTest source set.
./gradlew :core:domain:testAndroidHostTest
./gradlew :core:analytics:testAndroidHostTest
./gradlew :data:testAndroidHostTest
./gradlew :core:presentation:testAndroidHostTest      # ViewModel/state/navigation tests, JVM

./gradlew :core:presentation:iosSimulatorArm64Test    # Same tests + the iOS DI factory, simulator target
./gradlew :core:domain:iosSimulatorArm64Test
./gradlew :data:iosSimulatorArm64Test
./gradlew :core:analytics:iosSimulatorArm64Test
```

### Swift

```shell
xcodebuild test -project iosApp/iosApp.xcodeproj -scheme iosApp \
  -destination 'platform=iOS Simulator,name=iPhone 17,OS=latest'

# One target at a time
xcodebuild test … -only-testing:iosAppTests      # Swift Testing: mappings, tokens, interop integration
xcodebuild test … -only-testing:iosAppUITests    # XCUITest: list, detail, error and accessibility flows
```

**Always pass `OS=latest`.** A `name=iPhone 17` destination alone is ambiguous when more than one iOS
runtime is installed, and the resulting device contention shows up as
`Application failed preflight checks` rather than as a useful error.

Both test targets run against a deterministic Koin graph: `initKoinIosForUiTests(…)` swaps only the
repositories and the location permission prompt for in-memory fakes, so the real ViewModels,
`Navigator` and analytics are exercised without the network, Room or a system dialog. UI tests opt in
with the `-UITestMode` launch argument (`-UITestFailure` to reach the error state); unit tests are
detected automatically, since they are hosted by the app. None of it is compiled into release builds.

---

Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)…
