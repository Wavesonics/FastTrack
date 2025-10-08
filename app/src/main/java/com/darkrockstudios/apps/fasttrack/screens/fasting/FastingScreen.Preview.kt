package com.darkrockstudios.apps.fasttrack.screens.fasting

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.darkrockstudios.apps.fasttrack.screens.fasting.IFastingViewModel.StageState
import com.darkrockstudios.apps.fasttrack.ui.theme.FastTrackTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.time.Instant

/**
 * This file contains preview implementations for the FastingScreen.
 * Since FastingScreen requires a ViewModel, we create a simplified version
 * of the UI for preview purposes.
 */
@SuppressLint("ViewModelConstructorInComposable")
@Composable
fun FastingScreenPreview(
	isFasting: Boolean = false,
	stageTitle: String = "",
	stageDescription: String = "",
	energyMode: String = "",
	elapsedHours: Double = 0.0,
	timerText: String = "0:00:00",
	milliseconds: String = "0",
	fatBurnTime: String = "--:--:--",
	ketosisTime: String = "--:--:--",
	autophagyTime: String = "--:--:--",
	fatBurnStageState: StageState = StageState.NotStarted,
	ketosisStageState: StageState = StageState.NotStarted,
	autophagyStageState: StageState = StageState.NotStarted,
	alertsEnabled: Boolean = true,
	darkTheme: Boolean = false,
	showGradientBackground: Boolean = true,
) {
	val initialState = IFastingViewModel.FastingUiState(
		isFasting = isFasting,
		stageTitle = stageTitle,
		stageDescription = stageDescription,
		energyMode = energyMode,
		elapsedHours = elapsedHours,
		timerText = timerText,
		milliseconds = milliseconds,
		fatBurnTime = fatBurnTime,
		ketosisTime = ketosisTime,
		autophagyTime = autophagyTime,
		fatBurnStageState = fatBurnStageState,
		ketosisStageState = ketosisStageState,
		autophagyStageState = autophagyStageState,
		alertsEnabled = alertsEnabled,
		showGradientBackground = showGradientBackground,
	)

	val viewModel = FakeFastingViewModel(initialState)

	FastTrackTheme(darkTheme = darkTheme) {
		FastingScreen(PaddingValues(0.dp), viewModel)
	}
}

@Preview(
	name = "Fasting Screen - Not Fasting",
	showBackground = true,
	widthDp = 360,
	heightDp = 640
)
@Composable
private fun FastingScreenPreviewNotFasting() {
	FastingScreenPreview(
		isFasting = false,
		stageTitle = "Not Fasting",
		stageDescription = "Start a fast to begin tracking your progress.",
		energyMode = "Energy Mode: Glucose",
		elapsedHours = 0.0,
		timerText = "0:00:00",
		milliseconds = "0",
		alertsEnabled = true,
	)
}

@Preview(
	name = "Fasting Screen - Not Fasting (Dark)",
	showBackground = true,
	widthDp = 360,
	heightDp = 640,
	uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun FastingScreenPreviewNotFastingDark() {
	FastingScreenPreview(
		isFasting = false,
		stageTitle = "Not Fasting",
		stageDescription = "Start a fast to begin tracking your progress.",
		energyMode = "Energy Mode: Glucose",
		elapsedHours = 0.0,
		timerText = "0:00:00",
		milliseconds = "0",
		alertsEnabled = true,
		darkTheme = true
	)
}

@Preview(
	name = "Fasting Screen - Fat Burning Phase",
	showBackground = true,
	widthDp = 360,
	heightDp = 640
)
@Composable
private fun FastingScreenPreviewFatBurning() {
	FastingScreenPreview(
		isFasting = true,
		stageTitle = "Fat Burning",
		stageDescription = "Your body has depleted its glucose reserves and is now burning fat for energy.",
		energyMode = "Energy Mode: Fat",
		elapsedHours = 14.0,
		timerText = "14:00:00",
		milliseconds = "0",
		fatBurnTime = "2:00:00",
		ketosisTime = "-4:00:00",
		autophagyTime = "-10:00:00",
		fatBurnStageState = StageState.StartedActive,
		ketosisStageState = StageState.StartedInactive,
		autophagyStageState = StageState.StartedInactive,
		alertsEnabled = true,
	)
}

@Preview(
	name = "Fasting Screen - Fat Burning Phase (Dark)",
	showBackground = true,
	widthDp = 360,
	heightDp = 640,
	uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun FastingScreenPreviewFatBurningDark() {
	FastingScreenPreview(
		isFasting = true,
		stageTitle = "Fat Burning",
		stageDescription = "Your body has depleted its glucose reserves and is now burning fat for energy.",
		energyMode = "Energy Mode: Fat",
		elapsedHours = 14.0,
		timerText = "14:00:00",
		milliseconds = "0",
		fatBurnTime = "2:00:00",
		ketosisTime = "-4:00:00",
		autophagyTime = "-10:00:00",
		fatBurnStageState = StageState.StartedActive,
		ketosisStageState = StageState.StartedInactive,
		autophagyStageState = StageState.StartedInactive,
		alertsEnabled = true,
		darkTheme = true
	)
}

@Preview(
	name = "Fasting Screen - No Background - Fat Burning Phase",
	showBackground = true,
	widthDp = 360,
	heightDp = 640
)
@Composable
private fun FastingScreenPreviewFatBurningNoBackground() {
	FastingScreenPreview(
		isFasting = true,
		stageTitle = "Fat Burning",
		stageDescription = "Your body has depleted its glucose reserves and is now burning fat for energy.",
		energyMode = "Energy Mode: Fat",
		elapsedHours = 14.0,
		timerText = "14:00:00",
		milliseconds = "0",
		fatBurnTime = "2:00:00",
		ketosisTime = "-4:00:00",
		autophagyTime = "-10:00:00",
		fatBurnStageState = StageState.StartedActive,
		ketosisStageState = StageState.StartedInactive,
		autophagyStageState = StageState.StartedInactive,
		alertsEnabled = true,
		showGradientBackground = false
	)
}

@Preview(
	name = "Fasting Screen - No Background - Fat Burning Phase (Dark)",
	showBackground = true,
	widthDp = 360,
	heightDp = 640,
	uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun FastingScreenPreviewFatBurningDarkNoBackground() {
	FastingScreenPreview(
		isFasting = true,
		stageTitle = "Fat Burning",
		stageDescription = "Your body has depleted its glucose reserves and is now burning fat for energy.",
		energyMode = "Energy Mode: Fat",
		elapsedHours = 14.0,
		timerText = "14:00:00",
		milliseconds = "0",
		fatBurnTime = "2:00:00",
		ketosisTime = "-4:00:00",
		autophagyTime = "-10:00:00",
		fatBurnStageState = StageState.StartedActive,
		ketosisStageState = StageState.StartedInactive,
		autophagyStageState = StageState.StartedInactive,
		alertsEnabled = true,
		darkTheme = true,
		showGradientBackground = false,
	)
}

@Preview(
	name = "Fasting Screen - Ketosis Phase",
	showBackground = true,
	widthDp = 360,
	heightDp = 640
)
@Composable
private fun FastingScreenPreviewKetosis() {
	FastingScreenPreview(
		isFasting = true,
		stageTitle = "Ketosis",
		stageDescription = "Your liver is producing ketones, which are being used for energy by your brain and body.",
		energyMode = "Energy Mode: Fat",
		elapsedHours = 24.0,
		timerText = "24:00:00",
		milliseconds = "0",
		fatBurnTime = "12:00:00",
		ketosisTime = "6:00:00",
		autophagyTime = "-12:00:00",
		fatBurnStageState = StageState.StartedActive,
		ketosisStageState = StageState.StartedActive,
		autophagyStageState = StageState.StartedInactive,
		alertsEnabled = true,
	)
}

@Preview(
	name = "Fasting Screen - Ketosis Phase (Dark)",
	showBackground = true,
	widthDp = 360,
	heightDp = 640,
	uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun FastingScreenPreviewKetosisDark() {
	FastingScreenPreview(
		isFasting = true,
		stageTitle = "Ketosis",
		stageDescription = "Your liver is producing ketones, which are being used for energy by your brain and body.",
		energyMode = "Energy Mode: Fat",
		elapsedHours = 24.0,
		timerText = "24:00:00",
		milliseconds = "0",
		fatBurnTime = "12:00:00",
		ketosisTime = "6:00:00",
		autophagyTime = "-12:00:00",
		fatBurnStageState = StageState.StartedActive,
		ketosisStageState = StageState.StartedActive,
		autophagyStageState = StageState.StartedInactive,
		alertsEnabled = true,
		darkTheme = true
	)
}

@Preview(
	name = "Fasting Screen - Autophagy Phase",
	showBackground = true,
	widthDp = 360,
	heightDp = 640
)
@Composable
private fun FastingScreenPreviewAutophagy() {
	FastingScreenPreview(
		isFasting = true,
		stageTitle = "Autophagy",
		stageDescription = "Your cells are recycling old components and damaged proteins.",
		energyMode = "Energy Mode: Fat",
		elapsedHours = 48.0,
		timerText = "48:00:00",
		milliseconds = "0",
		fatBurnTime = "36:00:00",
		ketosisTime = "30:00:00",
		autophagyTime = "12:00:00",
		fatBurnStageState = StageState.StartedActive,
		ketosisStageState = StageState.StartedActive,
		autophagyStageState = StageState.StartedActive,
		alertsEnabled = true,
	)
}

@Preview(
	name = "Fasting Screen - Autophagy Phase (Dark)",
	showBackground = true,
	widthDp = 360,
	heightDp = 640,
	uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun FastingScreenPreviewAutophagyDark() {
	FastingScreenPreview(
		isFasting = true,
		stageTitle = "Autophagy",
		stageDescription = "Your cells are recycling old components and damaged proteins.",
		energyMode = "Energy Mode: Fat",
		elapsedHours = 48.0,
		timerText = "48:00:00",
		milliseconds = "0",
		fatBurnTime = "36:00:00",
		ketosisTime = "30:00:00",
		autophagyTime = "12:00:00",
		fatBurnStageState = StageState.StartedActive,
		ketosisStageState = StageState.StartedActive,
		autophagyStageState = StageState.StartedActive,
		alertsEnabled = true,
		darkTheme = true
	)
}

@Preview(
	name = "Fasting Screen - Debug Mode",
	showBackground = true,
	widthDp = 360,
	heightDp = 640
)
@Composable
private fun FastingScreenPreviewDebugMode() {
	FastingScreenPreview(
		isFasting = true,
		stageTitle = "Ketosis",
		stageDescription = "Your liver is producing ketones, which are being used for energy by your brain and body.",
		energyMode = "Energy Mode: Fat",
		elapsedHours = 24.0,
		timerText = "24:00:00",
		milliseconds = "0",
		fatBurnTime = "12:00:00",
		ketosisTime = "6:00:00",
		autophagyTime = "-12:00:00",
		fatBurnStageState = StageState.StartedActive,
		ketosisStageState = StageState.StartedActive,
		autophagyStageState = StageState.StartedInactive,
		alertsEnabled = true,
	)
}

@Preview(
	name = "Fasting Screen - Debug Mode (Dark)",
	showBackground = true,
	widthDp = 360,
	heightDp = 640,
	uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun FastingScreenPreviewDebugModeDark() {
	FastingScreenPreview(
		isFasting = true,
		stageTitle = "Ketosis",
		stageDescription = "Your liver is producing ketones, which are being used for energy by your brain and body.",
		energyMode = "Energy Mode: Fat",
		elapsedHours = 24.0,
		timerText = "24:00:00",
		milliseconds = "0",
		fatBurnTime = "12:00:00",
		ketosisTime = "6:00:00",
		autophagyTime = "-12:00:00",
		fatBurnStageState = StageState.StartedActive,
		ketosisStageState = StageState.StartedActive,
		autophagyStageState = StageState.StartedInactive,
		alertsEnabled = true,
		darkTheme = true
	)
}

@Preview(
	name = "Fasting Screen - Tablet",
	showBackground = true,
	widthDp = 600,
	heightDp = 800
)
@Composable
private fun FastingScreenPreviewTablet() {
	FastingScreenPreview(
		isFasting = true,
		stageTitle = "Autophagy",
		stageDescription = "Your cells are recycling old components and damaged proteins.",
		energyMode = "Energy Mode: Fat",
		elapsedHours = 48.0,
		timerText = "48:00:00",
		milliseconds = "0",
		fatBurnTime = "36:00:00",
		ketosisTime = "30:00:00",
		autophagyTime = "12:00:00",
		fatBurnStageState = StageState.StartedActive,
		ketosisStageState = StageState.StartedActive,
		autophagyStageState = StageState.StartedActive,
		alertsEnabled = true,
	)
}

@Preview(
	name = "Fasting Screen - Tablet Landscape",
	showBackground = true,
	widthDp = 800,
	heightDp = 600
)
@Composable
private fun FastingScreenPreviewTabletLandscape() {
	FastingScreenPreview(
		isFasting = true,
		stageTitle = "Autophagy",
		stageDescription = "Your cells are recycling old components and damaged proteins.",
		energyMode = "Energy Mode: Fat",
		elapsedHours = 48.0,
		timerText = "48:00:00",
		milliseconds = "0",
		fatBurnTime = "36:00:00",
		ketosisTime = "30:00:00",
		autophagyTime = "12:00:00",
		fatBurnStageState = StageState.StartedActive,
		ketosisStageState = StageState.StartedActive,
		autophagyStageState = StageState.StartedActive,
		alertsEnabled = true,
	)
}

@Preview(
	name = "Fasting Screen - Tablet (Dark)",
	showBackground = true,
	widthDp = 600,
	heightDp = 800,
	uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun FastingScreenPreviewTabletDark() {
	FastingScreenPreview(
		isFasting = true,
		stageTitle = "Autophagy",
		stageDescription = "Your cells are recycling old components and damaged proteins.",
		energyMode = "Energy Mode: Fat",
		elapsedHours = 48.0,
		timerText = "48:00:00",
		milliseconds = "0",
		fatBurnTime = "36:00:00",
		ketosisTime = "30:00:00",
		autophagyTime = "12:00:00",
		fatBurnStageState = StageState.StartedActive,
		ketosisStageState = StageState.StartedActive,
		autophagyStageState = StageState.StartedActive,
		alertsEnabled = true,
		darkTheme = true
	)
}

@Preview(
	name = "Fasting Screen - Landscape Phone",
	showBackground = true,
	widthDp = 640,
	heightDp = 300
)
@Composable
private fun FastingScreenPreviewLandscapePhone() {
	FastingScreenPreview(
		isFasting = true,
		stageTitle = "Ketosis",
		stageDescription = "Your liver is producing ketones, which are being used for energy by your brain and body.",
		energyMode = "Energy Mode: Fat",
		elapsedHours = 24.0,
		timerText = "24:00:00",
		milliseconds = "0",
		fatBurnTime = "12:00:00",
		ketosisTime = "6:00:00",
		autophagyTime = "-12:00:00",
		fatBurnStageState = StageState.StartedActive,
		ketosisStageState = StageState.StartedActive,
		autophagyStageState = StageState.StartedInactive,
		alertsEnabled = true,
	)
}

@Preview(
	name = "Fasting Screen - Landscape Phone (Dark)",
	showBackground = true,
	widthDp = 640,
	heightDp = 300,
	uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun FastingScreenPreviewLandscapePhoneDark() {
	FastingScreenPreview(
		isFasting = true,
		stageTitle = "Ketosis",
		stageDescription = "Your liver is producing ketones, which are being used for energy by your brain and body.",
		energyMode = "Energy Mode: Fat",
		elapsedHours = 24.0,
		timerText = "24:00:00",
		milliseconds = "0",
		fatBurnTime = "12:00:00",
		ketosisTime = "6:00:00",
		autophagyTime = "-12:00:00",
		fatBurnStageState = StageState.StartedActive,
		ketosisStageState = StageState.StartedActive,
		autophagyStageState = StageState.StartedInactive,
		alertsEnabled = true,
		darkTheme = true
	)
}

class FakeFastingViewModel(state: IFastingViewModel.FastingUiState) : IFastingViewModel {
	override val uiState = MutableStateFlow(state).asStateFlow()
	override fun onCreate() {}
	override fun updateUi() {}
	override fun startFast(timeStartedMills: Instant?) {}
	override fun endFast() {}
	override fun setAlertsEnabled(enabled: Boolean) {}
	override fun setupAlerts() {}
	override fun debugIncreaseFastingTimeByOneHour() {}
}