package com.germandebustamante.fuelio.feature.onboarding.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.germandebustamante.fuelio.R
import com.germandebustamante.fuelio.core.domain.preferences.model.FuelType
import com.germandebustamante.fuelio.core.testing.A11yIdentifiers
import com.germandebustamante.fuelio.core.ui.theme.FuelioSpacing
import com.germandebustamante.fuelio.core.ui.theme.FuelioTheme
import com.germandebustamante.fuelio.designsystem.button.FuelioTextButton
import com.germandebustamante.fuelio.designsystem.button.config.text.TextButtonConfig
import com.germandebustamante.fuelio.designsystem.button.config.text.TextButtonVariant
import com.germandebustamante.fuelio.designsystem.scaffold.FuelioScaffold
import com.germandebustamante.fuelio.feature.onboarding.state.OnboardingStep
import com.germandebustamante.fuelio.feature.onboarding.state.OnboardingUIState
import com.germandebustamante.fuelio.feature.onboarding.state.OnboardingViewModel
import com.germandebustamante.fuelio.feature.settings.ui.DefaultFuelSelector
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun OnboardingScreen(modifier: Modifier = Modifier, viewModel: OnboardingViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    OnboardingScreen(
        state = state,
        onNextTapped = viewModel::onWelcomeNextTapped,
        onSkipTapped = viewModel::onSkipAllTapped,
        onAllowLocationTapped = viewModel::onRequestLocationPermissionTapped,
        onSkipLocationTapped = viewModel::onSkipLocationPermissionTapped,
        onFuelTypeSelected = viewModel::onFuelTypeSelected,
        onFinishTapped = viewModel::onFinishTapped,
        modifier = modifier,
    )
}

@Composable
private fun OnboardingScreen(
    state: OnboardingUIState,
    onNextTapped: () -> Unit,
    onSkipTapped: () -> Unit,
    onAllowLocationTapped: () -> Unit,
    onSkipLocationTapped: () -> Unit,
    onFuelTypeSelected: (FuelType) -> Unit,
    onFinishTapped: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FuelioScaffold(modifier = modifier.testTag(A11yIdentifiers.ONBOARDING_SCREEN)) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(FuelioSpacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            when (state.step) {
                OnboardingStep.WELCOME -> WelcomeStep(onNextTapped = onNextTapped, onSkipTapped = onSkipTapped)
                OnboardingStep.LOCATION_PERMISSION -> LocationPermissionStep(
                    onAllowTapped = onAllowLocationTapped,
                    onSkipTapped = onSkipLocationTapped,
                )

                OnboardingStep.DEFAULT_FUEL -> DefaultFuelStep(
                    selectedFuelType = state.selectedFuelType,
                    onFuelTypeSelected = onFuelTypeSelected,
                    onFinishTapped = onFinishTapped,
                )
            }
        }
    }
}

@Composable
private fun WelcomeStep(onNextTapped: () -> Unit, onSkipTapped: () -> Unit) {
    Icon(
        imageVector = Icons.Filled.LocalGasStation,
        contentDescription = null,
        modifier = Modifier.height(64.dp),
        tint = MaterialTheme.colorScheme.primary,
    )
    Spacer(Modifier.height(FuelioSpacing.lg))
    Text(
        text = stringResource(R.string.onboarding_welcome_title),
        style = MaterialTheme.typography.headlineSmall,
        textAlign = TextAlign.Center,
    )
    Spacer(Modifier.height(FuelioSpacing.sm))
    Text(
        text = stringResource(R.string.onboarding_welcome_subtitle),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
    )
    Spacer(Modifier.height(FuelioSpacing.xl))
    FuelioTextButton(
        onClick = onNextTapped,
        text = stringResource(R.string.onboarding_next),
        modifier = Modifier.fillMaxWidth().testTag(A11yIdentifiers.ONBOARDING_NEXT_BUTTON),
    )
    Spacer(Modifier.height(FuelioSpacing.sm))
    FuelioTextButton(
        onClick = onSkipTapped,
        text = stringResource(R.string.onboarding_skip),
        config = TextButtonConfig(variant = TextButtonVariant.Text),
        modifier = Modifier.fillMaxWidth().testTag(A11yIdentifiers.ONBOARDING_SKIP_BUTTON),
    )
}

@Composable
private fun LocationPermissionStep(onAllowTapped: () -> Unit, onSkipTapped: () -> Unit) {
    Icon(
        imageVector = Icons.Filled.LocationOn,
        contentDescription = null,
        modifier = Modifier.height(64.dp),
        tint = MaterialTheme.colorScheme.primary,
    )
    Spacer(Modifier.height(FuelioSpacing.lg))
    Text(
        text = stringResource(R.string.onboarding_location_title),
        style = MaterialTheme.typography.headlineSmall,
        textAlign = TextAlign.Center,
    )
    Spacer(Modifier.height(FuelioSpacing.sm))
    Text(
        text = stringResource(R.string.onboarding_location_subtitle),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
    )
    Spacer(Modifier.height(FuelioSpacing.xl))
    FuelioTextButton(
        onClick = onAllowTapped,
        text = stringResource(R.string.onboarding_location_allow),
        modifier = Modifier.fillMaxWidth().testTag(A11yIdentifiers.ONBOARDING_ALLOW_LOCATION_BUTTON),
    )
    Spacer(Modifier.height(FuelioSpacing.sm))
    FuelioTextButton(
        onClick = onSkipTapped,
        text = stringResource(R.string.onboarding_skip),
        config = TextButtonConfig(variant = TextButtonVariant.Text),
        modifier = Modifier.fillMaxWidth().testTag(A11yIdentifiers.ONBOARDING_SKIP_BUTTON),
    )
}

@Composable
private fun DefaultFuelStep(selectedFuelType: FuelType, onFuelTypeSelected: (FuelType) -> Unit, onFinishTapped: () -> Unit) {
    Text(
        text = stringResource(R.string.onboarding_fuel_title),
        style = MaterialTheme.typography.headlineSmall,
        textAlign = TextAlign.Center,
    )
    Spacer(Modifier.height(FuelioSpacing.sm))
    Text(
        text = stringResource(R.string.onboarding_fuel_subtitle),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
    )
    Spacer(Modifier.height(FuelioSpacing.xl))
    DefaultFuelSelector(
        selectedFuelType = selectedFuelType,
        onFuelTypeSelected = onFuelTypeSelected,
        modifier = Modifier.testTag(A11yIdentifiers.ONBOARDING_FUEL_PICKER),
    )
    Spacer(Modifier.height(FuelioSpacing.xl))
    FuelioTextButton(
        onClick = onFinishTapped,
        text = stringResource(R.string.onboarding_finish),
        modifier = Modifier.fillMaxWidth().testTag(A11yIdentifiers.ONBOARDING_FINISH_BUTTON),
    )
}

@Composable
@Preview(showBackground = true)
private fun OnboardingWelcomeStepPreview() {
    FuelioTheme {
        OnboardingScreen(
            state = OnboardingUIState(step = OnboardingStep.WELCOME),
            onNextTapped = {},
            onSkipTapped = {},
            onAllowLocationTapped = {},
            onSkipLocationTapped = {},
            onFuelTypeSelected = {},
            onFinishTapped = {},
        )
    }
}

@Composable
@Preview(showBackground = true)
private fun OnboardingLocationStepPreview() {
    FuelioTheme {
        OnboardingScreen(
            state = OnboardingUIState(step = OnboardingStep.LOCATION_PERMISSION),
            onNextTapped = {},
            onSkipTapped = {},
            onAllowLocationTapped = {},
            onSkipLocationTapped = {},
            onFuelTypeSelected = {},
            onFinishTapped = {},
        )
    }
}

@Composable
@Preview(showBackground = true)
private fun OnboardingFuelStepPreview() {
    FuelioTheme {
        OnboardingScreen(
            state = OnboardingUIState(step = OnboardingStep.DEFAULT_FUEL),
            onNextTapped = {},
            onSkipTapped = {},
            onAllowLocationTapped = {},
            onSkipLocationTapped = {},
            onFuelTypeSelected = {},
            onFinishTapped = {},
        )
    }
}
