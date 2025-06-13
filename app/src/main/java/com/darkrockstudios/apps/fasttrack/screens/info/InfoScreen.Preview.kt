package com.darkrockstudios.apps.fasttrack.screens.info

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.darkrockstudios.apps.fasttrack.ui.theme.FastTrackTheme

/**
 * This file contains preview implementations for the InfoScreen.
 * Since InfoScreen requires a ViewModel, we create a simplified version
 * of the UI for preview purposes.
 */
@SuppressLint("ViewModelConstructorInComposable")
@Composable
fun InfoScreenPreview(darkTheme: Boolean = false) {
	FastTrackTheme(darkTheme = darkTheme) {
		InfoScreen()
	}
}

@Preview(
	name = "Info Screen",
	showBackground = true,
	widthDp = 360,
	heightDp = 640
)
@Composable
private fun InfoScreenPreviewDefault() {
	InfoScreenPreview()
}

@Preview(
	name = "Info Screen (Dark)",
	showBackground = true,
	widthDp = 360,
	heightDp = 640,
	uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun InfoScreenPreviewDefaultDark() {
	InfoScreenPreview(darkTheme = true)
}

@Preview(
	name = "Info Screen - Tablet",
	showBackground = true,
	widthDp = 600,
	heightDp = 800
)
@Composable
private fun InfoScreenPreviewTablet() {
	InfoScreenPreview()
}

@Preview(
	name = "Info Screen - Tablet (Dark)",
	showBackground = true,
	widthDp = 600,
	heightDp = 800,
	uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun InfoScreenPreviewTabletDark() {
	InfoScreenPreview(darkTheme = true)
}

@Preview(
	name = "Info Screen - Landscape",
	showBackground = true,
	widthDp = 640,
	heightDp = 360
)
@Composable
private fun InfoScreenPreviewLandscape() {
	InfoScreenPreview()
}

@Preview(
	name = "Info Screen - Landscape (Dark)",
	showBackground = true,
	widthDp = 640,
	heightDp = 360,
	uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun InfoScreenPreviewLandscapeDark() {
	InfoScreenPreview(darkTheme = true)
}
