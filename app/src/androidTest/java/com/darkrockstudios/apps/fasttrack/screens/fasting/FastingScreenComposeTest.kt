package com.darkrockstudios.apps.fasttrack.screens.fasting

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.darkrockstudios.apps.fasttrack.R
import com.darkrockstudios.apps.fasttrack.ui.theme.FastTrackTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Compose UI tests for the Fasting Screen.
 * Verifies core labels and start/stop FABs for both not-fasting and fasting states.
 */
@RunWith(AndroidJUnit4::class)
class FastingScreenComposeTest {

	@get:Rule
	val composeTestRule = createComposeRule()

	private val context = InstrumentationRegistry.getInstrumentation().targetContext

	@Test
	fun testFastingScreen_NotFasting_ShowsStartButtonAndLabels() {
		// Arrange a non-fasting state
		val state = IFastingViewModel.FastingUiState(
			isFasting = false,
			stageTitle = "Not Fasting",
			stageDescription = "Start a fast to begin tracking your progress.",
			energyMode = "Energy Mode: Glucose",
			elapsedHours = 0.0,
			timerText = "0:00:00",
			milliseconds = "0",
			fatBurnTime = "--:--:--",
			ketosisTime = "--:--:--",
			autophagyTime = "--:--:--",
			alertsEnabled = true
		)
		val vm = FakeFastingViewModel(state)

		composeTestRule.setContent {
			FastTrackTheme {
				FastingScreen(
					contentPaddingValues = PaddingValues(0.dp),
					viewModel = vm
				)
			}
		}

		// Phase labels exist
		composeTestRule.onAllNodesWithText(context.getString(R.string.fast_fat_burn_label))
			.assertCountEquals(1)
		composeTestRule.onAllNodesWithText(context.getString(R.string.fast_ketosis_label))
			.assertCountEquals(1)
		composeTestRule.onAllNodesWithText(context.getString(R.string.fast_autophagy_label))
			.assertCountEquals(1)

		// Alerts checkbox label exists
		composeTestRule.onNodeWithText(context.getString(R.string.stage_alerts_checkbox))
			.assertExists()

		// Start FAB exists; Stop FAB does not
		composeTestRule.onNodeWithContentDescription(context.getString(R.string.start_fast_button_description))
			.assertExists()
		composeTestRule.onNodeWithContentDescription(context.getString(R.string.stop_fast_button_description))
			.assertDoesNotExist()
	}

	@Test
	fun testFastingScreen_Fasting_ShowsStopButtonAndTimer() {
		// Arrange a fasting/active state
		val state = IFastingViewModel.FastingUiState(
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
			alertsEnabled = true
		)
		val vm = FakeFastingViewModel(state)

		composeTestRule.setContent {
			FastTrackTheme {
				FastingScreen(
					contentPaddingValues = PaddingValues(0.dp),
					viewModel = vm
				)
			}
		}

		// Phase labels exist
		composeTestRule.onAllNodesWithText(context.getString(R.string.fast_fat_burn_label))
			.assertCountEquals(1)
		composeTestRule.onAllNodesWithText(context.getString(R.string.fast_ketosis_label))
			.assertCountEquals(1)
		composeTestRule.onAllNodesWithText(context.getString(R.string.fast_autophagy_label))
			.assertCountEquals(1)

		// Alerts checkbox label exists
		composeTestRule.onNodeWithText(context.getString(R.string.stage_alerts_checkbox))
			.assertExists()

		// Stop FAB exists; Start FAB does not
		composeTestRule.onNodeWithContentDescription(context.getString(R.string.stop_fast_button_description))
			.assertExists()
		composeTestRule.onNodeWithContentDescription(context.getString(R.string.start_fast_button_description))
			.assertDoesNotExist()

		// Timer text is shown
		composeTestRule.onNodeWithText("24:00:00").assertExists()
	}
}
