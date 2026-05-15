# Fuelio — Design System & UX Refactor

## Context

Fuelio is a KMP project (Android + iOS) built with Compose Multiplatform 1.10 + Material 3. Before this refactor, the UI had a well-tokenized color system but lacked custom typography, shape and spacing tokens, and the design system only had two button components. Several visual bugs and UX gaps were also present.

### Issues found before the refactor

| Area | Problem |
|---|---|
| Theme | No `Typography.kt`, no `Shape.kt`, no `Spacing.kt` |
| Dialogs | Hardcoded `Color.White` → breaks dark mode |
| Animations | None: loading was a hard-swap, no `animateItem`, no shimmer |
| Accessibility | No `semantics`, `contentDescription = null` on almost all icons |
| Empty state | Emoji + text only, no illustration or CTA |
| ES strings | 7 missing translations + typo `cheapper` |
| Icons | `gas_station_ic.xml` with hardcoded `#1C274C` color (not themeable) |
| UI structure | Handcrafted header with `Row` instead of `Scaffold` + `TopAppBar` |
| Design system | Only `FuelioTextButton` + `FuelioIconButton`; raw `IconButton` used in 2 places |

---

## Phase 1 — Theme Tokens

### Font: Inter Variable (OFL)

Two TTF files were bundled in `composeResources/font/` to work on both Android and iOS without relying on Android-only APIs:

```
composeApp/src/commonMain/composeResources/font/
├── inter_variable.ttf
└── inter_italic.ttf
```

Inter was chosen for its excellent numeric legibility at small sizes (critical for prices/distances), support for variable weights in a single file, and its neutral character that pairs well with the brand's orange palette.

### New files

**`core/ui/theme/Typography.kt`**

Full M3 scale (15 styles) with `FuelioFontFamily` loaded via `org.jetbrains.compose.resources.Font`. Display/Headline at `SemiBold(600)`, Title at `Medium(500)`, Body at `Regular(400)`, Label at `Medium(500)`.

**`core/ui/theme/Shape.kt`**

```kotlin
FuelioShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small      = RoundedCornerShape(8.dp),
    medium     = RoundedCornerShape(12.dp),
    large      = RoundedCornerShape(20.dp),    // +4dp vs M3 default → more modern feel
    extraLarge = RoundedCornerShape(28.dp),
)
```

**`core/ui/theme/Spacing.kt`**

```kotlin
object FuelioSpacing {
    val xxs  = 2.dp
    val xs   = 4.dp
    val sm   = 8.dp
    val md   = 16.dp
    val lg   = 24.dp
    val xl   = 32.dp
    val xxl  = 48.dp
    val xxxl = 56.dp
}
```

An `object` (not `CompositionLocal`) was chosen because spacing never changes at runtime — there are no per-theme or per-screen variants.

**`core/ui/theme/Elevation.kt`**

`FuelioElevation` with 5 levels (`none` → `level5`) for consistent use on cards and elevated surfaces.

### `Theme.kt` update

```kotlin
MaterialTheme(
    colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
    typography  = FuelioTypography,
    shapes      = FuelioShapes,
    content     = content,
)
```

### Gradle

`compose.materialIconsExtended` was added via the CMP plugin shorthand (automatic version resolution) to provide access to the full icon catalog.

---

## Phase 2 — String and Drawable Hygiene

### Translations

- Renamed `cheapper_gas_station_tag` → `cheapest_station_tag` (typo fixed)
- Added 7 missing translations in `values-es/strings.xml`:
  - `fuel_filter_gasoline_95` → "Gasolina 95"
  - `fuel_filter_gasoline_98` → "Gasolina 98"
  - `fuel_filter_diesel` → "Diésel"
  - `fuel_filter_diesel_premium` → "Diésel+"
  - `search_gas_station` → "Buscar gasolinera…"
  - `empty_state_title` → "No se encontraron gasolineras"
  - `empty_state_subtitle` → "Prueba con otra búsqueda o filtro"
- New strings for the refactored UI: `top_bar_subtitle_change_province`, `search_clear`, `scroll_to_top`, `empty_state_change_filters`, `error_state_title`, `error_state_retry`, `province_search_placeholder`, `favorite_add`, `favorite_remove`

### Drawable

`gas_station_ic.xml` had a hardcoded `android:strokeColor="#1C274C"`, which prevented Compose's `Icon(tint = ...)` from working correctly in dark mode. Changed to `#FF000000` (pure black) so the theme tint works in both color modes.

---

## Phase 3 — New Design System Components

All located under `composeApp/src/commonMain/kotlin/com/germandebustamante/fuelio/designsystem/`. Each component includes `@Preview` for both Light and Dark.

| Component | File | Wraps | Notes |
|---|---|---|---|
| `FuelioText` | `text/FuelioText.kt` | `Text` | `FuelioTextStyle` enum maps to `MaterialTheme.typography.*`. Opt-in. |
| `FuelioCard` | `card/FuelioCard.kt` | `Card` / `ElevatedCard` / `OutlinedCard` | `Filled`, `Elevated`, `Outlined` variants. Accepts `onClick` for ripple. Default shape `large`. |
| `FuelioDialog` | `dialog/FuelioDialog.kt` | `AlertDialog` (M3) | Uses native `AlertDialog`: dark mode, focus, and insets for free. **Eliminates the `Dialog + Card + Color.White` pattern.** |
| `FuelioTextField` | `textfield/FuelioTextField.kt` | `OutlinedTextField` | `leadingIcon`, `trailingIcon`, `isError`, `helperText`. Future-proof. |
| `FuelioFilterChip` | `chip/FuelioChip.kt` | `FilterChip` | Shape `small`, label with `labelLarge`. |
| `FuelioAssistChip` | `chip/FuelioChip.kt` | `AssistChip` | Accepts `ImageVector` or `Painter` as leading icon. |
| `FuelioScaffold` | `scaffold/FuelioScaffold.kt` | `Scaffold` | `ScaffoldDefaults.contentWindowInsets`, externalizable `snackbarHostState`. |
| `FuelioTopBar` | `topbar/FuelioTopBar.kt` | `TopAppBar` / `MediumTopAppBar` / `CenterAlignedTopAppBar` | `Small`, `Medium`, `CenterAligned` variants. Accepts `scrollBehavior`. |
| `FuelioSearchBar` | `searchbar/FuelioSearchBar.kt` | `OutlinedTextField` | Clear button with `AnimatedVisibility(fade + scale)`. `OutlinedTextField` chosen over M3 `SearchBar` because there are no suggestions or history. |
| `FuelioLoadingIndicator` | `progress/FuelioLoadingIndicator.kt` | `CircularProgressIndicator` | Sizes `Small(20)`, `Medium(32)`, `Large(48)`. |
| `FuelioSkeleton` | `progress/FuelioSkeleton.kt` | custom | `Modifier.fuelioSkeleton(shape)` with shimmer via `rememberInfiniteTransition`. No external library. Includes `FuelioGasStationItemSkeleton()`. |
| `FuelioEmptyState` | `emptystate/FuelioEmptyState.kt` | composable | Slots: `icon`, `title`, `subtitle`, `action`. |
| `FuelioDivider` | `divider/FuelioDivider.kt` | `HorizontalDivider` | `outlineVariant` color. |
| `FuelioOutlinedButton` | `button/FuelioOutlinedButton.kt` | `OutlinedButton` | Same config API as `FuelioTextButton`. |

### Existing button updates

- `FuelioTextButton`: `Color.White` in preview → `MaterialTheme.colorScheme.surface`, `Arrangement.spacedBy(8.dp)` → `FuelioSpacing.sm`, typo "Alway disabled" → "Always disabled"
- `FuelioIconButton`: spacing tokenized

---

## Phase 4 — State Model Refactor

### `ContentState` sealed interface

```kotlin
sealed interface ContentState {
    data object Initial  : ContentState
    data object Loading  : ContentState
    data class  Success(val stations: List<GasStationItemVO>) : ContentState
    data object Empty    : ContentState
    data class  Error(val message: String) : ContentState
}
```

Enables `AnimatedContent(targetState = state.contentState)` with explicit transitions between states instead of `if/else` hard-swaps.

### `GasStationsUIState` changes

- `isRefreshing: Boolean` — distinguishes initial load (skeleton) from pull-to-refresh (spinner over existing data)
- `favorites: Set<String>` — IDs of stations marked as favorites (UI-only; persistence is TODO)
- `contentState: ContentState` — computed property derived from `isLoading`, `error`, and `gasStations`
- `withRefreshing()`, `withFavoriteToggled(id)` — new copy helpers

### `GasStationItemVO` changes

- `isCheapest: Boolean` — automatically set by the ViewModel on the station with the lowest price for the active fuel filter

### `GasStationsViewModel` changes

- `markCheapest()` — private extension that finds the minimum `getCurrentFuelPrice()` and marks the first matching VO. Recalculated after every fuel filter change.
- `onRefresh()` — re-fetches from the current provider without resetting visible content
- `onRetry()` — clears the error and re-triggers the province flow
- `onItemClick(id)` — no-op with `// TODO: Navigate to detail screen`
- `onToggleFavorite(id)` — toggles the ID in `favorites`

---

## Phase 5 — Dialog Migration

**Deleted:**
- `feature/common/dialog/error/ErrorDialog.kt`
- `feature/common/dialog/permission/LocationPermissionDialog.kt`

Both used the `Dialog + Card(containerColor = Color.White)` pattern that broke dark mode. Replaced in-place in `GasStationsScreen` with direct calls to `FuelioDialog`, which uses M3's `AlertDialog` and inherits the container color from the theme automatically.

---

## Phase 6 — `GasStationItem` Refactor

### New visual hierarchy

| Element | Before | After |
|---|---|---|
| Price | `titleSmall` | `headlineSmall` bold, `primary` color |
| Fuel label | ❌ | `labelSmall` `onSurfaceVariant` ("Gasoline 95") |
| Open/closed state | 8dp dot + text | `OpenClosedBadge` pill with `tertiaryContainer`/`errorContainer` background |
| Distance | Separator dot + icon + text | Icon + `labelMedium` in `onSurfaceVariant` |
| "Cheapest" badge | ❌ (string existed but unused) | `CheapestBadge` top-end overlay with `primaryContainer` |
| Favorite | ❌ | `IconToggleButton` with `Star`/`StarBorder` |
| Click | No response | `FuelioCard(onClick = onItemClick)` with ripple |

### Accessibility

```kotlin
Modifier.semantics(mergeDescendants = true) {
    contentDescription = "$name, $address, $openLabel, $distanceLabel, $fuelLabel at $priceLabel$cheapestLabel"
}
```

Each card announces a full sentence to TalkBack/VoiceOver. Added `contentDescription` to favorite icons with localized strings.

### New auxiliary components

- `feature/list/ui/components/OpenClosedBadge.kt` — pill badge with theme colors
- `feature/list/ui/components/CheapestBadge.kt` — "Cheapest" label badge

---

## Phase 7 — Search, Filters, and Bottom Sheet

### `GasStationSearchBar`

Rewritten to delegate to `FuelioSearchBar`. The original M3 `SearchBar` was oversized for this use case (no suggestions or history to display).

### `FuelFilterSelector`

Migrated to `FuelioFilterChip`. Added `LocalHapticFeedback.current.performHapticFeedback(HapticFeedbackType.TextHandleMove)` when selecting each chip. Literal spacing `8.dp` → `FuelioSpacing.sm`.

### Province bottom sheet

Extracted internal search state (`rememberSaveable { mutableStateOf("") }`) to filter provinces client-side. Added a sticky `OutlinedTextField` in the sheet header. Filtered list via `remember(provinces, query)`.

### `ProvinceFilterButton`

**Deleted.** The province is now shown as a clickable subtitle in the TopAppBar, freeing vertical space and following the M3 contextual navigation pattern.

---

## Phase 8 — Final `GasStationsScreen` Integration

### Layout wireframe

```
┌──────────────────────────────────────────────┐
│ status bar                                   │
├──────────────────────────────────────────────┤
│ ⛽ Fuelio                           📍       │ ← SmallTopAppBar (enterAlways)
│    Seville  ▾                                │   clickable subtitle → province sheet
├──────────────────────────────────────────────┤
│ [ 🔍  Search gas station…          ✕ ]      │ ← FuelioSearchBar
│  ( G95 )  ( G98 )  ( Diesel )  ( Diesel+ )  │ ← FuelioFilterChip row + haptics
│ ──────────────────────────────────────────── │ ← FuelioDivider
│  [AnimatedContent by ContentState]           │
│   • Loading → GasStationsLoadingSkeleton     │
│   • Success → LazyColumn + animateItem()     │
│   • Empty   → FuelioEmptyState + CTA         │
│   • Error   → GasStationsErrorState + Retry  │
│                                        (⬆)   │ ← SmallFAB (AnimatedVisibility)
├──────────────────────────────────────────────┤
│ navigation bar                               │
└──────────────────────────────────────────────┘
```

### Technical highlights

- **`FuelioScaffold` + `enterAlwaysScrollBehavior`** — the TopAppBar collapses on scroll down and reappears on scroll up, maximizing content space
- **`PullToRefreshBox`** — wraps the content; uses `isRefreshing` (distinct from initial `isLoading`) to avoid clearing the list while reloading
- **`AnimatedContent(contentState)`** with `fadeIn() togetherWith fadeOut()` — smooth transitions between all states
- **`Modifier.animateItem()`** on each `LazyColumn` item — fluid reordering when filter or search changes
- **Scroll-to-top FAB** — `SmallFloatingActionButton` with `AnimatedVisibility(firstVisibleItemIndex > 3)` + `listState.animateScrollToItem(0)`
- **`imePadding()`** on scaffold content — keyboard does not cover the search bar
- **`nestedScroll(scrollBehavior.nestedScrollConnection)`** on the scaffold — coordinates list scroll with TopAppBar collapse

### New support files

- `feature/list/ui/state/GasStationsLoadingSkeleton.kt` — `LazyColumn` with 6 `FuelioGasStationItemSkeleton` (shimmer)
- `feature/list/ui/state/GasStationsErrorState.kt` — `Warning` icon + title + Retry button

---

## File Summary

### New
```
composeResources/font/inter_variable.ttf
composeResources/font/inter_italic.ttf
core/ui/theme/Typography.kt
core/ui/theme/Shape.kt
core/ui/theme/Spacing.kt
core/ui/theme/Elevation.kt
designsystem/text/FuelioText.kt
designsystem/card/FuelioCard.kt
designsystem/dialog/FuelioDialog.kt
designsystem/textfield/FuelioTextField.kt
designsystem/chip/FuelioChip.kt
designsystem/scaffold/FuelioScaffold.kt
designsystem/topbar/FuelioTopBar.kt
designsystem/searchbar/FuelioSearchBar.kt
designsystem/progress/FuelioLoadingIndicator.kt
designsystem/progress/FuelioSkeleton.kt
designsystem/emptystate/FuelioEmptyState.kt
designsystem/divider/FuelioDivider.kt
designsystem/button/FuelioOutlinedButton.kt
feature/list/ui/components/OpenClosedBadge.kt
feature/list/ui/components/CheapestBadge.kt
feature/list/ui/state/GasStationsLoadingSkeleton.kt
feature/list/ui/state/GasStationsErrorState.kt
```

### Modified
```
core/ui/theme/Theme.kt
composeResources/drawable/gas_station_ic.xml
composeResources/values/strings.xml
composeResources/values-es/strings.xml
gradle/libs.versions.toml
composeApp/build.gradle.kts
feature/list/state/GasStationsUIState.kt
feature/list/state/GasStationItemVO.kt
feature/list/state/GasStationsViewModel.kt
feature/list/ui/GasStationsScreen.kt
feature/list/ui/GasStationItem.kt
feature/list/ui/GasStationSearchBar.kt
feature/list/ui/FuelFilterSelector.kt
feature/list/ui/GasStationsEmptyState.kt
designsystem/button/FuelioTextButton.kt
designsystem/button/FuelioIconButton.kt
```

### Deleted
```
feature/common/dialog/error/ErrorDialog.kt
feature/common/dialog/permission/LocationPermissionDialog.kt
feature/list/ui/ProvinceFilterButton.kt
```

---

## Verification Checklist

### Build
```bash
./gradlew :composeApp:assembleDebug
./gradlew :composeApp:compileKotlinIosSimulatorArm64
./gradlew test
```

### Manual (Android device + iOS simulator)
- [ ] Cold launch → shimmer skeleton → transition to list
- [ ] Clickable province subtitle → bottom sheet with internal search
- [ ] Pull-to-refresh on the list → spinner without clearing content
- [ ] Scroll ≥ 3 items → FAB appears → tap → smooth scroll to top
- [ ] Focus search bar → keyboard does not cover the field (`imePadding`)
- [ ] Typing → clear button with fade+scale animation → tap clear
- [ ] Tap fuel chip → haptic → list reorders with `animateItem`
- [ ] Tap card → ripple visible (no navigation yet)
- [ ] Tap star → toggle favorite (UI-only)
- [ ] Cheapest station → "Cheapest" badge visible
- [ ] Dark mode ON → no white artifacts in dialogs or cards
- [ ] Spanish locale → all strings translated

### Accessibility
- [ ] TalkBack (Android) / VoiceOver (iOS) — each card announces full sentence
- [ ] Province subtitle announces "Seville, button, Change province"
- [ ] All touch targets ≥ 48dp (favorite, clear, FAB)
