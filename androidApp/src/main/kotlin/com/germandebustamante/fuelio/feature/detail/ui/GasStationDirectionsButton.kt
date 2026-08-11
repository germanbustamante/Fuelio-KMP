package com.germandebustamante.fuelio.feature.detail.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.germandebustamante.fuelio.R
import com.germandebustamante.fuelio.core.domain.gasstation.model.GasStationBO
import com.germandebustamante.fuelio.core.fake.fakeGasStations
import com.germandebustamante.fuelio.core.ui.theme.FuelioTheme
import com.germandebustamante.fuelio.designsystem.button.FuelioTextButton
import com.germandebustamante.fuelio.designsystem.button.TextButtonDrawable
import com.germandebustamante.fuelio.designsystem.button.TextButtonDrawableAlignment
import com.germandebustamante.fuelio.designsystem.button.config.text.TextButtonConfig
import com.germandebustamante.fuelio.designsystem.button.config.text.TextButtonSize

// google.navigation: is Google-Maps-specific — unlike a plain https://maps.google.com URL, the
// system has no other registered handler to fall back to, so we set the package explicitly and must
// resolveActivity() ourselves before launching it (see launchNavigation below).
private const val GOOGLE_MAPS_PACKAGE = "com.google.android.apps.maps"

@Composable
fun GasStationDirectionsButton(gasStation: GasStationBO, modifier: Modifier = Modifier) {
    val context = LocalContext.current

    FuelioTextButton(
        text = stringResource(R.string.detail_directions_button),
        onClick = { launchNavigation(context, gasStation.latitude, gasStation.longitude) },
        drawable = TextButtonDrawable(R.drawable.directions_ic, TextButtonDrawableAlignment.START),
        config = TextButtonConfig(size = TextButtonSize.LARGE),
        modifier = modifier.fillMaxWidth(),
    )
}

// google.navigation: launches turn-by-turn driving directions directly, skipping the route-preview
// screen the web Maps URL shows first. Per the official guidance
// (https://developer.android.com/guide/components/google-maps-intents), "if the system can't
// identify an app that can respond to the intent, your app might crash" — so resolveActivity() is
// mandatory here, with a fall back to the universal web URL if Google Maps isn't installed.
private fun launchNavigation(context: Context, latitude: Double, longitude: Double) {
    val navigationIntent = Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:q=$latitude,$longitude"))
        .setPackage(GOOGLE_MAPS_PACKAGE)

    val intentToLaunch = if (navigationIntent.resolveActivity(context.packageManager) != null) {
        navigationIntent
    } else {
        Intent(
            Intent.ACTION_VIEW,
            Uri.parse(
                "https://www.google.com/maps/dir/?api=1" +
                    "&destination=$latitude,$longitude" +
                    "&travelmode=driving",
            ),
        )
    }

    context.startActivity(intentToLaunch)
}

@Composable
@Preview(showBackground = true)
private fun GasStationDirectionsButtonPreview() {
    FuelioTheme {
        GasStationDirectionsButton(gasStation = fakeGasStations.first())
    }
}
