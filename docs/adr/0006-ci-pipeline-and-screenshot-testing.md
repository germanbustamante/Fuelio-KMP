# 0006 — CI pipeline scope and screenshot testing

- **Status:** Accepted
- **Date:** 2026-09-10
- **Depends on:** [0001](0001-kmp-with-native-ui-instead-of-compose-multiplatform.md)

## Context

The repo had ~24 test suites across five Gradle modules and two Xcode test targets, and **nothing
ran them automatically** — `.github/workflows/` held only the Claude assistant workflows. There was
no linter either. With two native UIs, a regression on one platform stayed invisible until somebody
happened to build it.

## Decision

### One workflow, four jobs

`lint` and `android` run on `ubuntu-latest`; `ios` runs on `macos-latest` and covers both the four
`iosSimulatorArm64Test` tasks and `xcodebuild test`.

Getting the Kotlin suites onto Linux was a prerequisite: `:core:domain` and `:core:analytics` only
declared iOS test targets, so their `commonTest` could only run on a macOS runner. Adding
`withHostTestBuilder { }` to both moved eight test files onto the cheap runner, leaving macOS to
cover only what genuinely needs it — the `iosTest` source sets and Xcode.

### Instrumentation tests do not gate pull requests

`connectedAndroidTest` runs in its own job, skipped on `pull_request`. The eight instrumentation
suites need a booted emulator on a shared runner, which is by far the biggest source of flakiness
available to us. A pipeline that fails for reasons unrelated to the change is a pipeline people stop
reading, and the same flows are already covered on iOS by XCUITests and on Android by the
ViewModel-level suites.

### Screenshot tests via Roborazzi, pinned one SDK below the app

Goldens are committed under `androidApp/src/test/screenshots/` and verified by
`verifyRoborazziDebug`. Two constraints shaped the setup, both worth recording because neither is
obvious from a stack trace:

1. **Robolectric renders at SDK 36, not 37.** Compose's `RobolectricIdlingStrategy` calls into
   Espresso, whose `InputManagerEventInjectionStrategy` uses `InputManager.getInstance()` — a method
   SDK 37 removed. Robolectric 4.17 *does* provide an SDK 37 sandbox; Espresso is what cannot run in
   it. Nothing these tests assert is SDK-specific, so `robolectric.properties` pins `sdk=36`.
2. **The JUnit Platform hides Robolectric tests unless the vintage engine is present.** `androidApp`
   sets `useJUnitPlatform()` and Robolectric is JUnit 4 only, so without
   `junit-vintage-engine` on the test runtime the screenshot tests are simply never discovered and
   the build reports success having run nothing. This is the worst failure mode available — a green
   check for tests that do not exist — so the engine is a deliberate, commented dependency and the
   test counts were verified by hand after wiring it.

Robolectric also needs a Java 21 VM for a modern sandbox while the project compiles to JVM 11, so the
unit-test task alone gets a toolchain, provisioned by the foojay resolver rather than assumed to be
installed.

## Alternatives considered

- **Paparazzi instead of Roborazzi** — renders without Robolectric, which would have avoided the SDK
  and JUnit-engine problems entirely. Rejected because it has no support for the AGP 9 / Gradle 9
  combination this project is on, and it cannot render anything requiring a real Android context.
- **Google's `screenshotTest` source set (AGP built-in)** — still in preview and limited to
  Composable previews; it cannot capture the states these tests care about (favourited, cheapest)
  without adding previews purely for testing.
- **Running instrumentation tests on every PR** — see above.
- **Splitting lint into the android job** — kept separate so a formatting failure is visible
  immediately instead of after a full test run.

## Consequences

- A pull request gets lint, every Kotlin suite, both Swift targets, a debug APK and a screenshot
  diff. What it does not get is on-device verification; that runs on `development` and on demand.
- Screenshot goldens are binary files in review. They are small and cover six components, but any
  intentional visual change now requires re-recording and shows up as a binary diff.
- The screenshot tests found a real defect on their first run: the list row printed the price unit
  twice, because `formatAsEuros()` already appends it. Every string assertion passed, since each half
  was individually correct — which is precisely the class of bug this exists to catch.
- CI needs `MAPS_API_KEY` and `POSTHOG_API_KEY` only as optional secrets; both are supported as
  absent, and a clean checkout builds without either.
