package com.germandebustamante.fuelio.screenshot

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onRoot
import com.germandebustamante.fuelio.core.ui.theme.FuelioTheme
import com.github.takahirom.roborazzi.captureRoboImage

/**
 * Goldens live next to the tests rather than under `build/`, so they are committed and diffable —
 * that is the whole contract of a screenshot test.
 */
private const val SCREENSHOT_DIR = "src/test/screenshots"

/**
 * Captures [content] twice, light and dark.
 *
 * Both are always worth pinning: the two color schemes are hand-written token mappings
 * (`LightColorScheme`/`DarkColorScheme`), so a token wired into only one of them is exactly the kind
 * of regression that renders fine in whichever mode the developer happens to be running.
 *
 * Capture goes through the Compose rule rather than Roborazzi's standalone `captureRoboImage { }`:
 * that overload routes through Espresso, whose `InputManagerEventInjectionStrategy` calls
 * `InputManager.getInstance()`, a method that no longer exists on SDK 37.
 */
internal fun ComposeContentTestRule.captureThemed(name: String, content: @Composable () -> Unit) {
    var isDark by mutableStateOf(false)

    setContent {
        FuelioTheme(darkTheme = isDark) {
            Surface { content() }
        }
    }

    onRoot().captureRoboImage(filePath = "$SCREENSHOT_DIR/${name}_light.png")

    isDark = true
    waitForIdle()

    onRoot().captureRoboImage(filePath = "$SCREENSHOT_DIR/${name}_dark.png")
}
