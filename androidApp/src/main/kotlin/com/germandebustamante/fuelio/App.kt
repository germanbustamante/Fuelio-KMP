package com.germandebustamante.fuelio

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.germandebustamante.fuelio.core.navigation.FuelioNavHost
import com.germandebustamante.fuelio.core.ui.theme.FuelioTheme
import com.germandebustamante.fuelio.core.ui.theme.isDark
import com.germandebustamante.fuelio.feature.app.state.AppViewModel
import com.germandebustamante.fuelio.feature.onboarding.ui.OnboardingScreen
import org.koin.compose.viewmodel.koinViewModel

/**
 * The theme is resolved here rather than inside a screen: it has to wrap the whole navigation graph,
 * which is above any screen ViewModel. `AppViewModel` is the shared holder for exactly that — iOS
 * applies the same state as `.preferredColorScheme` on `RootView`.
 *
 * `hasCompletedOnboarding` decides which root mounts, rather than onboarding being a pushed
 * `Destination`: it is never deep-linked to and never sits in a back stack — it *replaces* the root
 * until it is done, then never appears again, so it needs none of `Destination`'s
 * navigation/back-stack machinery. While it is `null` (the first preferences read hasn't resolved
 * yet), nothing renders but the themed background, to avoid a flash of the station list before
 * onboarding covers it.
 */
@Composable
fun App(viewModel: AppViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    FuelioTheme(darkTheme = state.themeMode.isDark()) {
        when (state.hasCompletedOnboarding) {
            null -> Box(modifier = Modifier.fillMaxSize())
            false -> OnboardingScreen()
            true -> FuelioNavHost()
        }
    }
}

@Preview
@Composable
private fun AppPreview() {
    FuelioTheme { FuelioNavHost() }
}
