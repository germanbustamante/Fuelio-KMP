package com.germandebustamante.fuelio.feature.settings.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.AndroidUiModes
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.germandebustamante.fuelio.R
import com.germandebustamante.fuelio.core.domain.preferences.model.FuelType
import com.germandebustamante.fuelio.core.domain.preferences.model.ThemeMode
import com.germandebustamante.fuelio.core.testing.A11yIdentifiers
import com.germandebustamante.fuelio.core.ui.theme.FuelioSpacing
import com.germandebustamante.fuelio.core.ui.theme.FuelioTheme
import com.germandebustamante.fuelio.designsystem.scaffold.FuelioScaffold
import com.germandebustamante.fuelio.designsystem.topbar.FuelioTopBar
import com.germandebustamante.fuelio.feature.settings.state.SettingsUIState
import com.germandebustamante.fuelio.feature.settings.state.SettingsViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SettingsScreen(modifier: Modifier = Modifier, viewModel: SettingsViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    SettingsScreen(
        state = state,
        onThemeModeSelected = viewModel::onThemeModeSelected,
        onDefaultFuelSelected = viewModel::onDefaultFuelSelected,
        onBackTapped = viewModel::onBackTapped,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsScreen(
    state: SettingsUIState,
    onThemeModeSelected: (ThemeMode) -> Unit,
    onDefaultFuelSelected: (FuelType) -> Unit,
    onBackTapped: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FuelioScaffold(
        modifier = modifier.testTag(A11yIdentifiers.SETTINGS_SCREEN),
        topBar = {
            FuelioTopBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(
                        onClick = onBackTapped,
                        modifier = Modifier.testTag(A11yIdentifiers.SETTINGS_BACK_BUTTON),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = stringResource(R.string.settings_back),
                        )
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(FuelioSpacing.md),
            verticalArrangement = Arrangement.spacedBy(FuelioSpacing.lg),
        ) {
            SettingsSection(title = stringResource(R.string.settings_appearance_title)) {
                ThemeModeSelector(
                    selectedThemeMode = state.themeMode,
                    onThemeModeSelected = onThemeModeSelected,
                )
            }

            SettingsSection(
                title = stringResource(R.string.settings_default_fuel_title),
                description = stringResource(R.string.settings_default_fuel_description),
            ) {
                DefaultFuelSelector(
                    selectedFuelType = state.defaultFuelType,
                    onFuelTypeSelected = onDefaultFuelSelected,
                )
            }
        }
    }
}

@Composable
private fun SettingsSection(title: String, modifier: Modifier = Modifier, description: String? = null, content: @Composable () -> Unit) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(FuelioSpacing.sm),
    ) {
        Text(text = title, style = MaterialTheme.typography.titleMedium)
        description?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        content()
    }
}

@Preview("LightMode", showBackground = true)
@Preview(name = "DarkMode", showBackground = true, uiMode = AndroidUiModes.UI_MODE_NIGHT_YES)
@Composable
private fun SettingsScreenPreview() {
    FuelioTheme {
        SettingsScreen(
            state = SettingsUIState(themeMode = ThemeMode.DARK, defaultFuelType = FuelType.DIESEL, isLoading = false),
            onThemeModeSelected = {},
            onDefaultFuelSelected = {},
            onBackTapped = {},
        )
    }
}
