package com.darkrockstudios.apps.fasttrack.screens.profile

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.darkrockstudios.apps.fasttrack.R
import com.darkrockstudios.apps.fasttrack.data.Gender
import com.darkrockstudios.apps.fasttrack.ui.theme.FastTrackTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Compose UI tests for the Profile Screen.
 * Verifies core labels, info buttons, and unit-specific fields for both metric and imperial modes.
 */
@RunWith(AndroidJUnit4::class)
class ProfileScreenComposeTest {

	@get:Rule
	val composeTestRule = createComposeRule()

	private val context = InstrumentationRegistry.getInstrumentation().targetContext

	@Test
	fun testProfileScreenMetricMode() {
		// Arrange a metric-state FakeProfileViewModel
		val state = IProfileViewModel.ProfileUiState(
			isMetric = true,
			heightCm = "175",
			weightKg = "70.0",
			age = "30",
			gender = Gender.Male,
			bmiValue = "BMI: 22.9 (Normal)",
			bmrValue = "BMR: 1,655 calories/day"
		)
		val vm = FakeProfileViewModel(state)

		composeTestRule.setContent {
			FastTrackTheme {
				ProfileScreen(
					viewModel = vm,
					onShowInfoDialog = { _, _ -> }
				)
			}
		}

		// BMI and BMR labels exist
		composeTestRule.onAllNodesWithText(context.getString(R.string.profile_bmi_label))
			.assertCountEquals(1)
		composeTestRule.onAllNodesWithText(context.getString(R.string.profile_bmr_label))
			.assertCountEquals(1)

		// Info buttons are present via content descriptions
		composeTestRule.onNodeWithContentDescription(context.getString(R.string.bmi_info_button_description))
			.assertExists()
		composeTestRule.onNodeWithContentDescription(context.getString(R.string.bmr_info_button_description))
			.assertExists()

		// Height section with metric switch label
		composeTestRule.onAllNodesWithText(context.getString(R.string.profile_height_label))
			.assertCountEquals(1)
		composeTestRule.onAllNodesWithText(context.getString(R.string.profile_metric_switch))
			.assertCountEquals(1)

		// Metric height input label
		composeTestRule.onNodeWithText(context.getString(R.string.profile_height_metric_label))
			.assertExists()
		// Imperial height labels should not be present in metric mode
		composeTestRule.onNodeWithText(context.getString(R.string.profile_height_imper_feet_label))
			.assertDoesNotExist()
		composeTestRule.onNodeWithText(context.getString(R.string.profile_height_imper_inches_label))
			.assertDoesNotExist()

		// Age section and field label
		composeTestRule.onNodeWithText(context.getString(R.string.profile_age_label)).assertExists()
		composeTestRule.onNodeWithText(context.getString(R.string.profile_age_years)).assertExists()

		// Weight section - metric label shown, imperial not shown
		composeTestRule.onNodeWithText(context.getString(R.string.profile_weight_label))
			.assertExists()
		composeTestRule.onNodeWithText(context.getString(R.string.profile_weight_kg_label))
			.assertExists()
		composeTestRule.onNodeWithText(context.getString(R.string.profile_weight_pounds_label))
			.assertDoesNotExist()

		// Gender section and options
		composeTestRule.onNodeWithText(context.getString(R.string.profile_gender_label))
			.assertExists()
		composeTestRule.onNodeWithText(context.getString(R.string.profile_gender_male))
			.assertExists()
		composeTestRule.onNodeWithText(context.getString(R.string.profile_gender_female))
			.assertExists()
	}

	@Test
	fun testProfileScreenImperialMode() {
		// Arrange an imperial-state FakeProfileViewModel
		val state = IProfileViewModel.ProfileUiState(
			isMetric = false,
			heightFeet = "5",
			heightInches = "9",
			weightLbs = "154.0",
			age = "30",
			gender = Gender.Male,
			bmiValue = "BMI: 22.9 (Normal)",
			bmrValue = "BMR: 1,655 calories/day"
		)
		val vm = FakeProfileViewModel(state)

		composeTestRule.setContent {
			FastTrackTheme {
				ProfileScreen(
					viewModel = vm,
					onShowInfoDialog = { _, _ -> }
				)
			}
		}

		// Height section labels
		composeTestRule.onNodeWithText(context.getString(R.string.profile_height_label))
			.assertExists()
		composeTestRule.onNodeWithText(context.getString(R.string.profile_metric_switch))
			.assertExists()

		// Imperial height input labels exist
		composeTestRule.onNodeWithText(context.getString(R.string.profile_height_imper_feet_label))
			.assertExists()
		composeTestRule.onNodeWithText(context.getString(R.string.profile_height_imper_inches_label))
			.assertExists()
		// Metric height label should not be present in imperial mode
		composeTestRule.onNodeWithText(context.getString(R.string.profile_height_metric_label))
			.assertDoesNotExist()

		// Weight section - imperial label shown, metric not shown
		composeTestRule.onNodeWithText(context.getString(R.string.profile_weight_label))
			.assertExists()
		composeTestRule.onNodeWithText(context.getString(R.string.profile_weight_pounds_label))
			.assertExists()
		composeTestRule.onNodeWithText(context.getString(R.string.profile_weight_kg_label))
			.assertDoesNotExist()
	}
}
