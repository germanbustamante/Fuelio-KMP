package com.germandebustamante.fuelio.core.ui.theme.color

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.AndroidUiModes
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.germandebustamante.fuelio.core.ui.theme.FuelioTheme

@Preview(name = "Light Theme")
@Preview(name = "Dark Theme", uiMode = AndroidUiModes.UI_MODE_NIGHT_YES)
@Composable
fun ColorSchemePreview() {
    FuelioTheme {
        Surface {
            Column(Modifier.padding(16.dp)) {
                ColorSwatch("Primary", MaterialTheme.colorScheme.primary)
                ColorSwatch("Secondary", MaterialTheme.colorScheme.secondary)
                ColorSwatch("Tertiary", MaterialTheme.colorScheme.tertiary)
                ColorSwatch("Error", MaterialTheme.colorScheme.error)
                ColorSwatch("Background", MaterialTheme.colorScheme.background)
                ColorSwatch("Surface", MaterialTheme.colorScheme.surface)
            }
        }
    }
}

@Composable
fun ColorSwatch(name: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(color, RoundedCornerShape(8.dp)),
        )
        Spacer(Modifier.width(12.dp))
        Text(name, style = MaterialTheme.typography.bodyLarge)
    }
}
