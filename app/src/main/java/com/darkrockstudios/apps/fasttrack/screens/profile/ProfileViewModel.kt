package com.darkrockstudios.apps.fasttrack.screens.profile

import android.content.Context
import android.os.LocaleList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cafe.adriel.satchel.Satchel
import cafe.adriel.satchel.ktx.getOrSet
import com.darkrockstudios.apps.fasttrack.R
import com.darkrockstudios.apps.fasttrack.data.Data
import com.darkrockstudios.apps.fasttrack.data.Gender
import com.darkrockstudios.apps.fasttrack.data.Profile
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.*
import kotlin.math.floor
import kotlin.math.pow
import kotlin.math.roundToInt

class ProfileViewModel(
    private val appContext: Context,
) : ViewModel(), IProfileViewModel {

    private val _uiState = MutableStateFlow(IProfileViewModel.ProfileUiState())
    override val uiState: StateFlow<IProfileViewModel.ProfileUiState> = _uiState.asStateFlow()

    override fun onCreate() {
        viewModelScope.launch {
            val profile = Satchel.storage.getOrSet(Data.KEY_PROFILE, Profile(displayMetric = isMetricSystem()))
            populateUiState(profile)
        }
    }

    private fun populateUiState(profile: Profile) {
        val totalInches = Data.cmToInch(profile.heightCm)
        val feet = floor(totalInches / 12.0).toInt()
        val inches = (totalInches % 12).toInt()
        val totalCmStr = profile.heightCm.roundToInt().toString()
        val pounds = Data.kgToLbs(profile.weightKg)
        val weightPoundsStr = "%.01f".format(pounds)
        val weightKgStr = "%.01f".format(profile.weightKg)

        val bmi = calculateBmi(profile)
        val bmiCategory = when {
            bmi < 18.5 -> appContext.getString(R.string.profile_bmi_category_underweight)
            bmi < 25.0 -> appContext.getString(R.string.profile_bmi_category_normal)
            bmi < 30.0 -> appContext.getString(R.string.profile_bmi_category_overweight)
            bmi < 40.0 -> appContext.getString(R.string.profile_bmi_category_obese)
            bmi >= 40.0 -> appContext.getString(R.string.profile_bmi_category_morbidly_obese)
            else -> ""
        }

        val bmr = calculateBmr(profile)

        _uiState.update {
            it.copy(
                isMetric = profile.displayMetric,
                heightCm = totalCmStr,
                heightFeet = feet.toString(),
                heightInches = inches.toString(),
                weightKg = weightKgStr,
                weightLbs = weightPoundsStr,
                age = profile.ageYears.toString(),
                gender = profile.gender,
                bmiValue = appContext.getString(R.string.profile_bmi_value, bmi, bmiCategory),
                bmrValue = appContext.getString(R.string.profile_bmr_value, bmr)
            )
        }
    }

    override fun updateHeightCm(value: String) {
        _uiState.update { it.copy(heightCm = value) }
        validateAndSaveProfile()
    }

    override fun updateHeightFeet(value: String) {
        _uiState.update { it.copy(heightFeet = value) }
        validateAndSaveProfile()
    }

    override fun updateHeightInches(value: String) {
        _uiState.update { it.copy(heightInches = value) }
        validateAndSaveProfile()
    }

    override fun updateWeightKg(value: String) {
        _uiState.update { it.copy(weightKg = value) }
        validateAndSaveProfile()
    }

    override fun updateWeightLbs(value: String) {
        _uiState.update { it.copy(weightLbs = value) }
        validateAndSaveProfile()
    }

    override fun updateAge(value: String) {
        _uiState.update { it.copy(age = value) }
        validateAndSaveProfile()
    }

    override fun updateGender(value: Gender) {
        _uiState.update { it.copy(gender = value) }
        validateAndSaveProfile()
    }

    override fun updateMetricSystem(isMetric: Boolean) {
        val currentState = _uiState.value
        val profile = createProfileFromUiState()

        if (profile != null) {
            _uiState.update { it.copy(isMetric = isMetric) }
            populateUiState(profile.copy(displayMetric = isMetric))
            validateAndSaveProfile()
        } else {
            _uiState.update { it.copy(isMetric = isMetric) }
        }
    }

    private fun validateAndSaveProfile() {
        if (validateProfile()) {
            saveProfile()
        }
    }

    private fun validateProfile(): Boolean {
        val currentState = _uiState.value
        var isValid = true
        var heightCmError: String? = null
        var heightFeetError: String? = null
        var heightInchesError: String? = null
        var weightKgError: String? = null
        var weightLbsError: String? = null
        var ageError: String? = null

        if (currentState.isMetric) {
            // Validate height in cm
            if (currentState.heightCm.isEmpty()) {
                heightCmError = appContext.getString(R.string.profile_error)
                isValid = false
            } else if (parseInt(currentState.heightCm) <= 0) {
                heightCmError = appContext.getString(R.string.profile_error)
                isValid = false
            }

            // Validate weight in kg
            if (currentState.weightKg.isEmpty()) {
                weightKgError = appContext.getString(R.string.profile_error)
                isValid = false
            } else if (parseDouble(currentState.weightKg) <= 0.0) {
                weightKgError = appContext.getString(R.string.profile_error)
                isValid = false
            }
        } else {
            // Validate height in feet
            if (currentState.heightFeet.isEmpty()) {
                heightFeetError = appContext.getString(R.string.profile_error)
                isValid = false
            } else if (parseInt(currentState.heightFeet) <= 0) {
                heightFeetError = appContext.getString(R.string.profile_error)
                isValid = false
            }

            // Validate height in inches
            if (currentState.heightInches.isEmpty()) {
                heightInchesError = appContext.getString(R.string.profile_error)
                isValid = false
            } else if (parseInt(currentState.heightInches) < 0) {
                heightInchesError = appContext.getString(R.string.profile_error)
                isValid = false
            }

            // Validate weight in pounds
            if (currentState.weightLbs.isEmpty()) {
                weightLbsError = appContext.getString(R.string.profile_error)
                isValid = false
            } else if (parseDouble(currentState.weightLbs) <= 0.0) {
                weightLbsError = appContext.getString(R.string.profile_error)
                isValid = false
            }
        }

        // Validate age
        if (currentState.age.isEmpty()) {
            ageError = appContext.getString(R.string.profile_error)
            isValid = false
        } else if (parseInt(currentState.age) <= 0) {
            ageError = appContext.getString(R.string.profile_error)
            isValid = false
        }

        _uiState.update {
            it.copy(
                heightCmError = heightCmError,
                heightFeetError = heightFeetError,
                heightInchesError = heightInchesError,
                weightKgError = weightKgError,
                weightLbsError = weightLbsError,
                ageError = ageError
            )
        }

        return isValid
    }

    private fun saveProfile() {
        val profile = createProfileFromUiState()
        if (profile != null) {
            Satchel.storage.apply {
                set(Data.KEY_PROFILE, profile)
            }
            populateUiState(profile)
            Napier.i("Profile saved")
        }
    }

    private fun createProfileFromUiState(): Profile? {
        val currentState = _uiState.value

        val totalCm: Double
        val kg: Double

        if (currentState.isMetric) {
            totalCm = parseInt(currentState.heightCm).toDouble()
            kg = parseDouble(currentState.weightKg)
        } else {
            val feet = parseInt(currentState.heightFeet)
            val inches = parseInt(currentState.heightInches)
            val totalInches = (feet * 12) + inches
            totalCm = Data.inchToCm(totalInches)

            val pounds = parseDouble(currentState.weightLbs)
            kg = Data.lbsToKg(pounds)
        }

        val age = parseInt(currentState.age)

        return if (age > 0 && totalCm > 0 && kg > 0) {
            Profile(
                ageYears = age,
                heightCm = totalCm,
                weightKg = kg,
                gender = currentState.gender,
                displayMetric = currentState.isMetric
            )
        } else {
            null
        }
    }

    private fun calculateBmi(profile: Profile): Double {
        return if (profile.isValid()) {
            // weight (kg) / [height (m)]2
            val heightM = profile.heightCm / 100
            profile.weightKg / (heightM.pow(2.0))
        } else {
            0.0
        }
    }

    private fun calculateBmr(profile: Profile): Double {
        return when (profile.gender) {
            //BMR for Men = 66.47 + (13.75 * weight [kg]) + (5.003 * size [cm]) − (6.755 * age [years])
            Gender.Male -> {
                66.47 + (13.75 * profile.weightKg) + (5.003 * profile.heightCm) - (6.755 * profile.ageYears)
            }
            //BMR for Women = 655.1 + (9.563 * weight [kg]) + (1.85 * size [cm]) − (4.676 * age [years])
            Gender.Female -> {
                655.1 + (9.563 * profile.weightKg) + (1.85 * profile.heightCm) - (4.676 * profile.ageYears)
            }
        }
    }

    private fun parseInt(text: String?): Int {
        var number = 0
        text?.let {
            try {
                number = text.toInt()
            } catch (e: NumberFormatException) {
                Napier.w("Failed to parse int")
            }
        }
        return number
    }

    private fun parseDouble(text: String?): Double {
        var number = 0.0
        text?.let {
            try {
                number = text.toDouble()
            } catch (e: NumberFormatException) {
                Napier.w("Failed to parse double")
            }
        }
        return number
    }

    private fun isMetricSystem(): Boolean {
        val locale: Locale = LocaleList.getDefault()[0]
        val imperialCountries = listOf("US", "LR", "MM")
        return !imperialCountries.contains(locale.country)
    }
}
