package com.darkrockstudios.apps.fasttrack.screens.profile

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.darkrockstudios.apps.fasttrack.data.Gender
import com.darkrockstudios.apps.fasttrack.ui.theme.FastTrackTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * This file contains preview implementations for the ProfileScreen.
 * Since ProfileScreen requires a ViewModel, we create a simplified version
 * of the UI for preview purposes.
 */
@SuppressLint("ViewModelConstructorInComposable")
@Composable
fun ProfileScreenPreview(
	isMetric: Boolean = false,
	heightCm: String = "",
	heightFeet: String = "",
	heightInches: String = "",
	weightKg: String = "",
	weightLbs: String = "",
	age: String = "",
	gender: Gender = Gender.Male,
	heightCmError: String? = null,
	heightFeetError: String? = null,
	heightInchesError: String? = null,
	weightKgError: String? = null,
	weightLbsError: String? = null,
	ageError: String? = null,
	bmiValue: String = "",
	bmrValue: String = "",
	darkTheme: Boolean = false
) {
	val initialState = IProfileViewModel.ProfileUiState(
		isMetric = isMetric,
		heightCm = heightCm,
		heightFeet = heightFeet,
		heightInches = heightInches,
		weightKg = weightKg,
		weightLbs = weightLbs,
		age = age,
		gender = gender,
		heightCmError = heightCmError,
		heightFeetError = heightFeetError,
		heightInchesError = heightInchesError,
		weightKgError = weightKgError,
		weightLbsError = weightLbsError,
		ageError = ageError,
		bmiValue = bmiValue,
		bmrValue = bmrValue
	)

	val viewModel = FakeProfileViewModel(initialState)

	FastTrackTheme(darkTheme = darkTheme) {
		ProfileScreen(
			viewModel = viewModel,
			onShowInfoDialog = { _, _ -> /* No-op for preview */ }
		)
	}
}

@Preview(
	name = "Profile Screen - Metric",
	showBackground = true,
	widthDp = 360,
	heightDp = 640
)
@Composable
private fun ProfileScreenPreviewMetric() {
	ProfileScreenPreview(
		isMetric = true,
		heightCm = "175",
		weightKg = "70.0",
		age = "30",
		gender = Gender.Male,
		bmiValue = "BMI: 22.9 (Normal)",
		bmrValue = "BMR: 1,655 calories/day"
	)
}

@Preview(
	name = "Profile Screen - Metric (Dark)",
	showBackground = true,
	widthDp = 360,
	heightDp = 640,
	uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun ProfileScreenPreviewMetricDark() {
	ProfileScreenPreview(
		isMetric = true,
		heightCm = "175",
		weightKg = "70.0",
		age = "30",
		gender = Gender.Male,
		bmiValue = "BMI: 22.9 (Normal)",
		bmrValue = "BMR: 1,655 calories/day",
		darkTheme = true
	)
}

@Preview(
	name = "Profile Screen - Imperial",
	showBackground = true,
	widthDp = 360,
	heightDp = 640
)
@Composable
private fun ProfileScreenPreviewImperial() {
	ProfileScreenPreview(
		isMetric = false,
		heightFeet = "5",
		heightInches = "9",
		weightLbs = "154.0",
		age = "30",
		gender = Gender.Male,
		bmiValue = "BMI: 22.9 (Normal)",
		bmrValue = "BMR: 1,655 calories/day"
	)
}

@Preview(
	name = "Profile Screen - Imperial (Dark)",
	showBackground = true,
	widthDp = 360,
	heightDp = 640,
	uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun ProfileScreenPreviewImperialDark() {
	ProfileScreenPreview(
		isMetric = false,
		heightFeet = "5",
		heightInches = "9",
		weightLbs = "154.0",
		age = "30",
		gender = Gender.Male,
		bmiValue = "BMI: 22.9 (Normal)",
		bmrValue = "BMR: 1,655 calories/day",
		darkTheme = true
	)
}

@Preview(
	name = "Profile Screen - Female",
	showBackground = true,
	widthDp = 360,
	heightDp = 640
)
@Composable
private fun ProfileScreenPreviewFemale() {
	ProfileScreenPreview(
		isMetric = true,
		heightCm = "165",
		weightKg = "60.0",
		age = "28",
		gender = Gender.Female,
		bmiValue = "BMI: 22.0 (Normal)",
		bmrValue = "BMR: 1,385 calories/day"
	)
}

@Preview(
	name = "Profile Screen - Female (Dark)",
	showBackground = true,
	widthDp = 360,
	heightDp = 640,
	uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun ProfileScreenPreviewFemaleDark() {
	ProfileScreenPreview(
		isMetric = true,
		heightCm = "165",
		weightKg = "60.0",
		age = "28",
		gender = Gender.Female,
		bmiValue = "BMI: 22.0 (Normal)",
		bmrValue = "BMR: 1,385 calories/day",
		darkTheme = true
	)
}

@Preview(
	name = "Profile Screen - With Errors",
	showBackground = true,
	widthDp = 360,
	heightDp = 640
)
@Composable
private fun ProfileScreenPreviewWithErrors() {
	ProfileScreenPreview(
		isMetric = true,
		heightCm = "0",
		weightKg = "0",
		age = "0",
		gender = Gender.Male,
		heightCmError = "Height must be greater than 0",
		weightKgError = "Weight must be greater than 0",
		ageError = "Age must be greater than 0",
		bmiValue = "BMI: -- (--)",
		bmrValue = "BMR: -- calories/day"
	)
}

@Preview(
	name = "Profile Screen - With Errors (Dark)",
	showBackground = true,
	widthDp = 360,
	heightDp = 640,
	uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun ProfileScreenPreviewWithErrorsDark() {
	ProfileScreenPreview(
		isMetric = true,
		heightCm = "0",
		weightKg = "0",
		age = "0",
		gender = Gender.Male,
		heightCmError = "Height must be greater than 0",
		weightKgError = "Weight must be greater than 0",
		ageError = "Age must be greater than 0",
		bmiValue = "BMI: -- (--)",
		bmrValue = "BMR: -- calories/day",
		darkTheme = true
	)
}

@Preview(
	name = "Profile Screen - Tablet",
	showBackground = true,
	widthDp = 600,
	heightDp = 800
)
@Composable
private fun ProfileScreenPreviewTablet() {
	ProfileScreenPreview(
		isMetric = true,
		heightCm = "175",
		weightKg = "70.0",
		age = "30",
		gender = Gender.Male,
		bmiValue = "BMI: 22.9 (Normal)",
		bmrValue = "BMR: 1,655 calories/day"
	)
}

@Preview(
	name = "Profile Screen - Tablet (Dark)",
	showBackground = true,
	widthDp = 600,
	heightDp = 800,
	uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun ProfileScreenPreviewTabletDark() {
	ProfileScreenPreview(
		isMetric = true,
		heightCm = "175",
		weightKg = "70.0",
		age = "30",
		gender = Gender.Male,
		bmiValue = "BMI: 22.9 (Normal)",
		bmrValue = "BMR: 1,655 calories/day",
		darkTheme = true
	)
}

@Preview(
	name = "Profile Screen - Landscape Phone",
	showBackground = true,
	widthDp = 640,
	heightDp = 360
)
@Composable
private fun ProfileScreenPreviewLandscapePhone() {
	ProfileScreenPreview(
		isMetric = false,
		heightFeet = "5",
		heightInches = "9",
		weightLbs = "154.0",
		age = "30",
		gender = Gender.Male,
		bmiValue = "BMI: 22.9 (Normal)",
		bmrValue = "BMR: 1,655 calories/day"
	)
}

@Preview(
	name = "Profile Screen - Landscape Phone (Dark)",
	showBackground = true,
	widthDp = 640,
	heightDp = 360,
	uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun ProfileScreenPreviewLandscapePhoneDark() {
	ProfileScreenPreview(
		isMetric = false,
		heightFeet = "5",
		heightInches = "9",
		weightLbs = "154.0",
		age = "30",
		gender = Gender.Male,
		bmiValue = "BMI: 22.9 (Normal)",
		bmrValue = "BMR: 1,655 calories/day",
		darkTheme = true
	)
}

class FakeProfileViewModel(state: IProfileViewModel.ProfileUiState) : IProfileViewModel {
	override val uiState = MutableStateFlow(state).asStateFlow()
	override fun onCreate() {}
	override fun updateHeightCm(value: String) {}
	override fun updateHeightFeet(value: String) {}
	override fun updateHeightInches(value: String) {}
	override fun updateWeightKg(value: String) {}
	override fun updateWeightLbs(value: String) {}
	override fun updateAge(value: String) {}
	override fun updateGender(value: Gender) {}
	override fun updateMetricSystem(isMetric: Boolean) {}
}
