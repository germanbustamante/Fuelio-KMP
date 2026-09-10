package com.germandebustamante.fuelio.screenshot

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.germandebustamante.fuelio.core.ui.theme.FuelioSpacing
import com.germandebustamante.fuelio.designsystem.card.FuelioCard
import com.germandebustamante.fuelio.designsystem.card.FuelioCardVariant
import com.germandebustamante.fuelio.designsystem.chip.FuelioAssistChip
import com.germandebustamante.fuelio.designsystem.chip.FuelioFilterChip
import com.germandebustamante.fuelio.designsystem.emptystate.FuelioEmptyState
import com.germandebustamante.fuelio.designsystem.searchbar.FuelioSearchBar
import com.germandebustamante.fuelio.designsystem.topbar.FuelioTopBar
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Screenshot coverage for the design system, rendered off-device by Robolectric.
 *
 * These pin what the shared design tokens actually produce on Android. The token *values* are
 * already asserted numerically on both platforms (`DesignSystemTests` does it on iOS), but nothing
 * until now caught a component wiring the wrong token, or a Material default quietly changing under
 * a dependency bump.
 */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
// The SDK level and the stand-in Application come from src/test/resources/robolectric.properties.
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel7)
class DesignSystemScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun chips() {
        composeRule.captureThemed("chips") {
            Row(
                modifier = Modifier.padding(FuelioSpacing.md),
                horizontalArrangement = Arrangement.spacedBy(FuelioSpacing.sm),
            ) {
                FuelioFilterChip(selected = true, onClick = {}, label = "Gasolina 95")
                FuelioFilterChip(selected = false, onClick = {}, label = "Diésel")
                FuelioAssistChip(onClick = {}, label = "Cómo llegar")
            }
        }
    }

    @Test
    fun cards() {
        composeRule.captureThemed("cards") {
            Column(
                modifier = Modifier.padding(FuelioSpacing.md),
                verticalArrangement = Arrangement.spacedBy(FuelioSpacing.sm),
            ) {
                FuelioCardVariant.entries.forEach { variant ->
                    FuelioCard(variant = variant) {
                        Text(text = variant.name, modifier = Modifier.padding(FuelioSpacing.md))
                    }
                }
            }
        }
    }

    @Test
    fun searchBar() {
        composeRule.captureThemed("search_bar") {
            Column(
                modifier = Modifier.padding(FuelioSpacing.md),
                verticalArrangement = Arrangement.spacedBy(FuelioSpacing.sm),
            ) {
                // Empty and filled: the trailing clear affordance only exists in the second.
                FuelioSearchBar(query = "", onQueryChange = {}, placeholder = "Buscar gasolinera…")
                FuelioSearchBar(query = "Repsol", onQueryChange = {}, placeholder = "Buscar gasolinera…")
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Test
    fun topBar() {
        composeRule.captureThemed("top_bar") {
            FuelioTopBar(
                title = { Text("Favoritos") },
                navigationIcon = {
                    IconButton(onClick = {}) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Outlined.Star, contentDescription = null)
                    }
                },
            )
        }
    }

    @Test
    fun emptyState() {
        composeRule.captureThemed("empty_state") {
            FuelioEmptyState(
                title = "Todavía no hay favoritos",
                subtitle = "Toca la estrella de cualquier gasolinera para guardarla aquí.",
                icon = { Icon(Icons.Outlined.Star, contentDescription = null) },
                modifier = Modifier.padding(FuelioSpacing.md),
            )
        }
    }
}
