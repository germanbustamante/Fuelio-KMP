package com.germandebustamante.fuelio.feature.common.dialog.error

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.AndroidUiModes
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.germandebustamante.fuelio.core.ui.theme.FuelioTheme
import com.germandebustamante.fuelio.designsystem.button.FuelioTextButton
import fuelio.composeapp.generated.resources.Res
import fuelio.composeapp.generated.resources.accept
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun ErrorDialog(
    title: String = "Oops!",
    description: String,
    acceptBtnText: StringResource = Res.string.accept,
    onDismissRequest: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismissRequest,
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.align(Alignment.Center)

            ) {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 28.dp, start = 32.dp, end = 32.dp),
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.labelLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 12.dp, start = 32.dp, end = 32.dp)
                    )

                    FuelioTextButton(
                        text = stringResource(acceptBtnText),
                        onClick = onDismissRequest,
                        modifier = Modifier.padding(vertical = 32.dp)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, uiMode = AndroidUiModes.UI_MODE_NIGHT_YES)
@Preview(showBackground = true)
@Composable
private fun ErrorDialogPreview() {
    FuelioTheme {
        var showDialog by rememberSaveable {
            mutableStateOf(false)
        }
        Box(modifier = Modifier.padding(40.dp)) {
            FuelioTextButton(text = "Show dialog", onClick = { showDialog = !showDialog })
        }

        if (showDialog) {
            ErrorDialog(
                "Oops!",
                "Server error occurred. Please try again later.",
                onDismissRequest = { showDialog = false })
        }
    }
}
