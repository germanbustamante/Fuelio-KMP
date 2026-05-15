# Fuelio — Design System & UX Refactor

## Contexto

La app Fuelio es un proyecto KMP (Android + iOS) construido con Compose Multiplatform 1.10 + Material 3. Antes de este refactor, la UI contaba con un sistema de colores bien tokenizado pero carecía de tipografía personalizada, tokens de forma y espaciado, y el design system solo disponía de dos componentes de botón. Además, existían varios bugs visuales y carencias de UX relevantes.

### Problemas detectados antes del refactor

| Área | Problema |
|---|---|
| Tema | Sin `Typography.kt`, sin `Shape.kt`, sin `Spacing.kt` |
| Diálogos | `Color.White` hardcodeado → rompe dark mode |
| Animaciones | Cero: loading era hard-swap, sin `animateItem`, sin shimmer |
| Accesibilidad | Sin `semantics`, `contentDescription = null` en casi todos los iconos |
| Empty state | Solo emoji + texto, sin ilustración ni CTA |
| Strings ES | 7 traducciones faltantes + typo `cheapper` |
| Iconos | `gas_station_ic.xml` con color hardcodeado `#1C274C` (no tintable) |
| Estructura UI | Header artesanal con `Row` en vez de `Scaffold` + `TopAppBar` |
| Design system | Solo `FuelioTextButton` + `FuelioIconButton`; `IconButton` raw en 2 sitios |

---

## Fase 1 — Tokens del tema

### Fuente: Inter Variable (OFL)

Se bundlearon dos archivos TTF en `composeResources/font/` para que funcionen tanto en Android como en iOS sin depender de APIs Android-only:

```
composeApp/src/commonMain/composeResources/font/
├── inter_variable.ttf
└── inter_italic.ttf
```

Inter se eligió por su excelente legibilidad numérica en tamaños pequeños (fundamental para precios/distancias), soporte de variable weights en un único archivo, y carácter neutro que combina bien con la paleta naranja de la marca.

### Archivos nuevos

**`core/ui/theme/Typography.kt`**

Escala M3 completa (15 estilos) con `FuelioFontFamily` cargado vía `org.jetbrains.compose.resources.Font`. Display/Headline en `SemiBold(600)`, Title en `Medium(500)`, Body en `Regular(400)`, Label en `Medium(500)`.

**`core/ui/theme/Shape.kt`**

```kotlin
FuelioShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small      = RoundedCornerShape(8.dp),
    medium     = RoundedCornerShape(12.dp),
    large      = RoundedCornerShape(20.dp),    // +4dp vs M3 default → feel más moderno
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

Se eligió un `object` (no `CompositionLocal`) porque el espaciado nunca cambia en runtime — no hay variantes por tema ni por pantalla.

**`core/ui/theme/Elevation.kt`**

`FuelioElevation` con 5 niveles (`none` → `level5`) para uso consistente en tarjetas y superficies elevadas.

### Actualización de `Theme.kt`

```kotlin
MaterialTheme(
    colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
    typography  = FuelioTypography,
    shapes      = FuelioShapes,
    content     = content,
)
```

### Gradle

Se añadió `compose.materialIconsExtended` vía el shorthand del plugin de CMP (resolución automática de versión) para disponer de un catálogo completo de iconos.

---

## Fase 2 — Higiene de strings y drawables

### Traducciones

- Renombrado `cheapper_gas_station_tag` → `cheapest_station_tag` (typo corregido)
- Añadidas 7 traducciones faltantes en `values-es/strings.xml`:
  - `fuel_filter_gasoline_95` → "Gasolina 95"
  - `fuel_filter_gasoline_98` → "Gasolina 98"
  - `fuel_filter_diesel` → "Diésel"
  - `fuel_filter_diesel_premium` → "Diésel+"
  - `search_gas_station` → "Buscar gasolinera…"
  - `empty_state_title` → "No se encontraron gasolineras"
  - `empty_state_subtitle` → "Prueba con otra búsqueda o filtro"
- Nuevos strings para la UI refactorizada: `top_bar_subtitle_change_province`, `search_clear`, `scroll_to_top`, `empty_state_change_filters`, `error_state_title`, `error_state_retry`, `province_search_placeholder`, `favorite_add`, `favorite_remove`

### Drawable

`gas_station_ic.xml` tenía `android:strokeColor="#1C274C"` hardcodeado, lo que impedía que `Icon(tint = ...)` de Compose funcionase correctamente en dark mode. Se cambió a `#FF000000` (negro puro) para que el tinte del tema funcione en ambos modos de color.

---

## Fase 3 — Nuevos componentes del design system

Todos bajo `composeApp/src/commonMain/kotlin/com/germandebustamante/fuelio/designsystem/`. Cada componente incluye `@Preview` en Light y Dark.

| Componente | Archivo | Wrappea | Notas |
|---|---|---|---|
| `FuelioText` | `text/FuelioText.kt` | `Text` | Enum `FuelioTextStyle` que mapea a `MaterialTheme.typography.*`. Opt-in. |
| `FuelioCard` | `card/FuelioCard.kt` | `Card` / `ElevatedCard` / `OutlinedCard` | Variantes `Filled`, `Elevated`, `Outlined`. Acepta `onClick` para ripple. Shape `large` por defecto. |
| `FuelioDialog` | `dialog/FuelioDialog.kt` | `AlertDialog` (M3) | Usa `AlertDialog` nativo: dark mode, focus e insets gratis. **Elimina el patrón `Dialog + Card + Color.White`.** |
| `FuelioTextField` | `textfield/FuelioTextField.kt` | `OutlinedTextField` | `leadingIcon`, `trailingIcon`, `isError`, `helperText`. Future-proof. |
| `FuelioFilterChip` | `chip/FuelioChip.kt` | `FilterChip` | Shape `small`, etiqueta con `labelLarge`. |
| `FuelioAssistChip` | `chip/FuelioChip.kt` | `AssistChip` | Acepta `ImageVector` o `Painter` como leading icon. |
| `FuelioScaffold` | `scaffold/FuelioScaffold.kt` | `Scaffold` | `ScaffoldDefaults.contentWindowInsets`, `snackbarHostState` externalizable. |
| `FuelioTopBar` | `topbar/FuelioTopBar.kt` | `TopAppBar` / `MediumTopAppBar` / `CenterAlignedTopAppBar` | Variantes `Small`, `Medium`, `CenterAligned`. Acepta `scrollBehavior`. |
| `FuelioSearchBar` | `searchbar/FuelioSearchBar.kt` | `OutlinedTextField` | Botón clear con `AnimatedVisibility(fade + scale)`. Se eligió `OutlinedTextField` sobre `SearchBar` de M3 porque no hay suggestions/historial. |
| `FuelioLoadingIndicator` | `progress/FuelioLoadingIndicator.kt` | `CircularProgressIndicator` | Tamaños `Small(20)`, `Medium(32)`, `Large(48)`. |
| `FuelioSkeleton` | `progress/FuelioSkeleton.kt` | custom | `Modifier.fuelioSkeleton(shape)` con shimmer via `rememberInfiniteTransition`. Sin librería externa. Incluye `FuelioGasStationItemSkeleton()`. |
| `FuelioEmptyState` | `emptystate/FuelioEmptyState.kt` | composable | Slots `icon`, `title`, `subtitle`, `action`. |
| `FuelioDivider` | `divider/FuelioDivider.kt` | `HorizontalDivider` | Color `outlineVariant`. |
| `FuelioOutlinedButton` | `button/FuelioOutlinedButton.kt` | `OutlinedButton` | Misma API de config que `FuelioTextButton`. |

### Actualización de botones existentes

- `FuelioTextButton`: `Color.White` en preview → `MaterialTheme.colorScheme.surface`, `Arrangement.spacedBy(8.dp)` → `FuelioSpacing.sm`, typo "Alway disabled" → "Always disabled"
- `FuelioIconButton`: espaciado tokenizado

---

## Fase 4 — Refactor del modelo de estado

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

Permite usar `AnimatedContent(targetState = state.contentState)` con transiciones explícitas entre estados en lugar de `if/else` con hard-swaps.

### Cambios en `GasStationsUIState`

- `isRefreshing: Boolean` — distingue la carga inicial (skeleton) del pull-to-refresh (spinner sobre datos existentes)
- `favorites: Set<String>` — IDs de gasolineras marcadas como favoritas (UI-only; persistencia es TODO)
- `contentState: ContentState` — computed property derivada de `isLoading`, `error` y `gasStations`
- `withRefreshing()`, `withFavoriteToggled(id)` — nuevos helpers de copia

### Cambios en `GasStationItemVO`

- `isCheapest: Boolean` — marcado automáticamente por el ViewModel sobre la gasolinera con menor precio para el filtro de combustible activo

### Cambios en `GasStationsViewModel`

- `markCheapest()` — extensión privada que localiza el mínimo de `getCurrentFuelPrice()` y marca el primer VO coincidente. Se recalcula tras cada cambio de filtro de combustible.
- `onRefresh()` — re-fetch del proveedor actual sin resetear el contenido visible
- `onRetry()` — limpia el error y reactiva el flujo de provincias
- `onItemClick(id)` — no-op con `// TODO: Navigate to detail screen`
- `onToggleFavorite(id)` — alterna el ID en `favorites`

---

## Fase 5 — Migración de diálogos

**Eliminados:**
- `feature/common/dialog/error/ErrorDialog.kt`
- `feature/common/dialog/permission/LocationPermissionDialog.kt`

Ambos usaban el patrón `Dialog + Card(containerColor = Color.White)` que rompía el dark mode. Se reemplazaron in-situ en `GasStationsScreen` con llamadas directas a `FuelioDialog`, que usa `AlertDialog` de M3 y hereda el color del contenedor del tema automáticamente.

---

## Fase 6 — Refactor de `GasStationItem`

### Jerarquía visual nueva

| Elemento | Antes | Ahora |
|---|---|---|
| Precio | `titleSmall` | `headlineSmall` bold, color `primary` |
| Etiqueta combustible | ❌ | `labelSmall` `onSurfaceVariant` ("Gasolina 95") |
| Estado open/closed | Punto de 8dp + texto | `OpenClosedBadge` pill con fondo `tertiaryContainer`/`errorContainer` |
| Distancia | Punto separador + icono + texto | Icono + `labelMedium` en `onSurfaceVariant` |
| Badge "más barata" | ❌ (string existía pero no se usaba) | `CheapestBadge` overlay `top-end` con `primaryContainer` |
| Favorito | ❌ | `IconToggleButton` con `Star`/`StarBorder` |
| Click | Sin respuesta | `FuelioCard(onClick = onItemClick)` con ripple |

### Accesibilidad

```kotlin
Modifier.semantics(mergeDescendants = true) {
    contentDescription = "$name, $address, $openLabel, $distanceLabel, $fuelLabel a $priceLabel$cheapestLabel"
}
```

Cada tarjeta anuncia una frase completa a TalkBack/VoiceOver. Se añadió `contentDescription` a los iconos de favorito con strings localizados.

### Nuevos componentes auxiliares

- `feature/list/ui/components/OpenClosedBadge.kt` — badge pill con colores del tema
- `feature/list/ui/components/CheapestBadge.kt` — etiqueta "Más barata" / "Cheapest"

---

## Fase 7 — Search, filtros y bottom sheet

### `GasStationSearchBar`

Se reescribió delegando a `FuelioSearchBar`. El M3 `SearchBar` original estaba sobredimensionado para el caso de uso (no hay suggestions ni historial que mostrar).

### `FuelFilterSelector`

Migrado a `FuelioFilterChip`. Se añadió `LocalHapticFeedback.current.performHapticFeedback(HapticFeedbackType.TextHandleMove)` al seleccionar cada chip. El espaciado literal `8.dp` → `FuelioSpacing.sm`.

### Province bottom sheet

Extraído el estado de búsqueda interno (`rememberSaveable { mutableStateOf("") }`) para filtrar provincias client-side. Se añadió `OutlinedTextField` sticky en la cabecera del sheet. Lista filtrada como `remember(provinces, query)`.

### `ProvinceFilterButton`

**Eliminado.** La provincia se muestra ahora como subtítulo clickable en el TopAppBar, lo que libera espacio vertical y respeta el patrón M3 de navegación contextual.

---

## Fase 8 — Integración final de `GasStationsScreen`

### Layout con wireframe

```
┌──────────────────────────────────────────────┐
│ status bar                                   │
├──────────────────────────────────────────────┤
│ ⛽ Fuelio                           📍       │ ← SmallTopAppBar (enterAlways)
│    Sevilla  ▾                                │   subtítulo clickable → province sheet
├──────────────────────────────────────────────┤
│ [ 🔍  Buscar gasolinera…          ✕ ]       │ ← FuelioSearchBar
│  ( G95 )  ( G98 )  ( Diesel )  ( Diesel+ )  │ ← FuelioFilterChip row + haptics
│ ──────────────────────────────────────────── │ ← FuelioDivider
│  [AnimatedContent según ContentState]        │
│   • Loading → GasStationsLoadingSkeleton     │
│   • Success → LazyColumn + animateItem()     │
│   • Empty   → FuelioEmptyState + CTA         │
│   • Error   → GasStationsErrorState + Retry  │
│                                        (⬆)   │ ← SmallFAB (AnimatedVisibility)
├──────────────────────────────────────────────┤
│ navigation bar                               │
└──────────────────────────────────────────────┘
```

### Novedades técnicas

- **`FuelioScaffold` + `enterAlwaysScrollBehavior`** — el TopAppBar colapsa al hacer scroll hacia abajo y reaparece al subir, maximizando el espacio de contenido
- **`PullToRefreshBox`** — envuelve el contenido; usa `isRefreshing` (distinto de `isLoading` inicial) para no borrar la lista mientras recarga
- **`AnimatedContent(contentState)`** con `fadeIn() togetherWith fadeOut()` — transiciones suaves entre todos los estados
- **`Modifier.animateItem()`** en cada item del `LazyColumn` — reordenación fluida al cambiar filtro o búsqueda
- **FAB scroll-to-top** — `SmallFloatingActionButton` con `AnimatedVisibility(firstVisibleItemIndex > 3)` + `listState.animateScrollToItem(0)`
- **`imePadding()`** en el contenido del scaffold — el teclado no cubre la barra de búsqueda
- **`nestedScroll(scrollBehavior.nestedScrollConnection)`** en el scaffold — coordina el scroll de la lista con el colapso del TopAppBar

### Nuevos archivos de soporte

- `feature/list/ui/state/GasStationsLoadingSkeleton.kt` — `LazyColumn` con 6 `FuelioGasStationItemSkeleton` (shimmer)
- `feature/list/ui/state/GasStationsErrorState.kt` — icono `Warning` + título + botón Retry

---

## Resumen de archivos

### Nuevos
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

### Modificados
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

### Eliminados
```
feature/common/dialog/error/ErrorDialog.kt
feature/common/dialog/permission/LocationPermissionDialog.kt
feature/list/ui/ProvinceFilterButton.kt
```

---

## Checklist de verificación

### Build
```bash
./gradlew :composeApp:assembleDebug
./gradlew :composeApp:compileKotlinIosSimulatorArm64
./gradlew test
```

### Manual (Android device + iOS simulator)
- [ ] Cold launch → skeleton shimmer → transición a lista
- [ ] Subtítulo provincia clickable → bottom sheet con buscador interno
- [ ] Pull-to-refresh sobre la lista → spinner sin perder contenido
- [ ] Scroll ≥ 3 items → FAB aparece → tap → scroll suave al inicio
- [ ] Focus search bar → IME no tapa el campo (`imePadding`)
- [ ] Typing → clear button con animación fade+scale → tap clear
- [ ] Tap chip de combustible → haptic → lista reordena con `animateItem`
- [ ] Tap card → ripple visible (sin navegación por ahora)
- [ ] Tap estrella → toggle favorito (UI-only)
- [ ] Gasolinera más barata → badge "Más barata" visible
- [ ] Dark mode ON → sin artefactos blancos en diálogos ni tarjetas
- [ ] Locale español → todos los strings traducidos

### Accesibilidad
- [ ] TalkBack (Android) / VoiceOver (iOS) — cada card anuncia frase completa
- [ ] Subtítulo provincia anuncia "Sevilla, botón, Cambiar provincia"
- [ ] Todos los targets táctiles ≥ 48dp (favorito, clear, FAB)
