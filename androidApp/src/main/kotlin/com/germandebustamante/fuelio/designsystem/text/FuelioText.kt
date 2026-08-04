package com.germandebustamante.fuelio.designsystem.text

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.germandebustamante.fuelio.core.ui.theme.FuelioSpacing
import com.germandebustamante.fuelio.core.ui.theme.FuelioTheme

enum class FuelioTextStyle {
    DisplayLarge, DisplayMedium, DisplaySmall,
    HeadlineLarge, HeadlineMedium, HeadlineSmall,
    TitleLarge, TitleMedium, TitleSmall,
    BodyLarge, BodyMedium, BodySmall,
    LabelLarge, LabelMedium, LabelSmall,
}

@Composable
fun FuelioText(
    text: String,
    style: FuelioTextStyle,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
) {
    val typography = MaterialTheme.typography
    val textStyle = when (style) {
        FuelioTextStyle.DisplayLarge -> typography.displayLarge
        FuelioTextStyle.DisplayMedium -> typography.displayMedium
        FuelioTextStyle.DisplaySmall -> typography.displaySmall
        FuelioTextStyle.HeadlineLarge -> typography.headlineLarge
        FuelioTextStyle.HeadlineMedium -> typography.headlineMedium
        FuelioTextStyle.HeadlineSmall -> typography.headlineSmall
        FuelioTextStyle.TitleLarge -> typography.titleLarge
        FuelioTextStyle.TitleMedium -> typography.titleMedium
        FuelioTextStyle.TitleSmall -> typography.titleSmall
        FuelioTextStyle.BodyLarge -> typography.bodyLarge
        FuelioTextStyle.BodyMedium -> typography.bodyMedium
        FuelioTextStyle.BodySmall -> typography.bodySmall
        FuelioTextStyle.LabelLarge -> typography.labelLarge
        FuelioTextStyle.LabelMedium -> typography.labelMedium
        FuelioTextStyle.LabelSmall -> typography.labelSmall
    }
    Text(
        text = text,
        style = textStyle,
        color = color,
        textAlign = textAlign,
        maxLines = maxLines,
        overflow = overflow,
        modifier = modifier,
    )
}

@Preview(showBackground = true)
@Composable
private fun FuelioTextPreview() {
    FuelioTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(FuelioSpacing.xs),
            modifier = Modifier.padding(FuelioSpacing.md),
        ) {
            FuelioText("Display Large", FuelioTextStyle.DisplayLarge)
            FuelioText("Headline Large", FuelioTextStyle.HeadlineLarge)
            FuelioText("Title Large", FuelioTextStyle.TitleLarge)
            FuelioText("Title Medium", FuelioTextStyle.TitleMedium)
            FuelioText("Title Small", FuelioTextStyle.TitleSmall)
            FuelioText("Body Large", FuelioTextStyle.BodyLarge)
            FuelioText("Body Medium", FuelioTextStyle.BodyMedium)
            FuelioText("Body Small", FuelioTextStyle.BodySmall)
            FuelioText("Label Large", FuelioTextStyle.LabelLarge)
            FuelioText("Label Medium", FuelioTextStyle.LabelMedium)
            FuelioText("Label Small", FuelioTextStyle.LabelSmall)
        }
    }
}
