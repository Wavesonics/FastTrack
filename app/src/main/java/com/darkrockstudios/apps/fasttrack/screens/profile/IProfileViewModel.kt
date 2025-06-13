package com.darkrockstudios.apps.fasttrack.screens.profile

import com.darkrockstudios.apps.fasttrack.data.Gender
import kotlinx.coroutines.flow.StateFlow

interface IProfileViewModel {
	data class ProfileUiState(
		val isMetric: Boolean = false,
		val heightCm: String = "",
		val heightFeet: String = "",
		val heightInches: String = "",
		val weightKg: String = "",
		val weightLbs: String = "",
		val age: String = "",
		val gender: Gender = Gender.Male,
		val heightCmError: String? = null,
		val heightFeetError: String? = null,
		val heightInchesError: String? = null,
		val weightKgError: String? = null,
		val weightLbsError: String? = null,
		val ageError: String? = null,
		val bmiValue: String = "",
		val bmrValue: String = ""
	)

	val uiState: StateFlow<ProfileUiState>

	fun onCreate()
	fun updateHeightCm(value: String)
	fun updateHeightFeet(value: String)
	fun updateHeightInches(value: String)
	fun updateWeightKg(value: String)
	fun updateWeightLbs(value: String)
	fun updateAge(value: String)
	fun updateGender(value: Gender)
	fun updateMetricSystem(isMetric: Boolean)
}