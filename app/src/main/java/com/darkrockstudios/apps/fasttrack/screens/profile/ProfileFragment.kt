package com.darkrockstudios.apps.fasttrack.screens.profile

import android.os.Bundle
import android.os.LocaleList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import cafe.adriel.satchel.Satchel
import cafe.adriel.satchel.ktx.getOrSet
import com.darkrockstudios.apps.fasttrack.R
import com.darkrockstudios.apps.fasttrack.data.Data
import com.darkrockstudios.apps.fasttrack.data.Gender
import com.darkrockstudios.apps.fasttrack.data.Profile
import com.darkrockstudios.apps.fasttrack.databinding.ProfileFragmentBinding
import com.darkrockstudios.apps.fasttrack.utils.Utils
import io.github.aakira.napier.Napier.i
import io.github.aakira.napier.Napier.w
import java.util.*
import kotlin.math.floor
import kotlin.math.pow
import kotlin.math.roundToInt


class ProfileFragment : Fragment() {
	companion object {
		fun newInstance() = ProfileFragment()
	}

	private val viewModel by viewModels<ProfileViewModel>()
	private var initialPopulationRequired = true

	private lateinit var binding: ProfileFragmentBinding

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		Satchel.storage.apply {
			viewModel.profile.postValue(getOrSet(Data.KEY_PROFILE, Profile(displayMetric = isMetricSystem())))
		}
	}

	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		binding = ProfileFragmentBinding.inflate(inflater, container, false)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		viewModel.profile.observe(viewLifecycleOwner, ::updateUi)

		binding.textInputEditTextHeightImperFeet.doOnTextChanged { _, _, _, _ -> updateProfile() }
		binding.textInputEditTextHeightImperInches.doOnTextChanged { _, _, _, _ -> updateProfile() }
		binding.textInputEditTextHeightCm.doOnTextChanged { _, _, _, _ -> updateProfile() }
		binding.TextInputEditTextAge.doOnTextChanged { _, _, _, _ -> updateProfile() }
		binding.textInputEditTextWeightPounds.doOnTextChanged { _, _, _, _ -> updateProfile() }
		binding.textInputEditTextWeightKg.doOnTextChanged { _, _, _, _ -> updateProfile() }
		binding.genderButtonGroup.setOnCheckedChangeListener { _, _ -> updateProfile() }


		binding.textViewBmiLabel.setOnClickListener {
			Utils.showInfoDialog(R.string.info_dialog_bmi_title, R.string.info_dialog_bmi_content, requireContext())
		}

		binding.textViewBmrLabel.setOnClickListener {
			Utils.showInfoDialog(R.string.info_dialog_bmr_title, R.string.info_dialog_bmr_content, requireContext())
		}

		binding.metricSwitch.setOnCheckedChangeListener { _, isChecked ->
			viewModel.profile.value?.let { profile ->
				populateInput(profile.copy(displayMetric = isChecked))
				updateProfile()
			}
		}
	}

	private fun saveProfile() {
		val metric = binding.metricSwitch.isChecked

		val totalCm: Double
		val kg: Double

		if (metric) {
			totalCm = parseInt(binding.textInputEditTextHeightCm.text).toDouble()
			kg = parseDouble(binding.textInputEditTextWeightKg.text)
		} else {
			val feet = parseInt(binding.textInputEditTextHeightImperFeet.text)
			val inches = parseInt(binding.textInputEditTextHeightImperInches.text)
			val totalInches = (feet * 12) + inches
			totalCm = Data.inchToCm(totalInches)

			val pounds = parseDouble(binding.textInputEditTextWeightPounds.text)
			kg = Data.lbsToKg(pounds)
		}

		val age = parseInt(binding.TextInputEditTextAge.text)

		val gender = when (binding.genderButtonGroup.checkedRadioButtonId) {
			R.id.gender_button_male -> Gender.Male
			R.id.gender_button_female -> Gender.Female
			else -> Gender.Male
		}
		val updatedProfile =
			Profile(ageYears = age, heightCm = totalCm, weightKg = kg, gender = gender, displayMetric = metric)
		if (updatedProfile != viewModel.profile.value) {
			viewModel.profile.postValue(updatedProfile)

			Satchel.storage.apply {
				set(Data.KEY_PROFILE, updatedProfile)
			}

			i("Profile saved")
		}
	}

	private fun updateProfile() {
		if (validateProfile()) {
			saveProfile()
		}
	}

	private fun validateProfile(): Boolean {
		var isValid = true

		val metric = binding.metricSwitch.isChecked
		if (metric) {
			val cmText = binding.textInputEditTextHeightCm.text
			if (cmText?.isEmpty() == true) {
				binding.textInputEditTextHeightCm.error = getString(R.string.profile_error)
				isValid = false
			} else {
				if (parseInt(cmText) > 0) {
					binding.textInputEditTextHeightCm.error = null
				} else {
					binding.textInputEditTextHeightCm.error = getString(R.string.profile_error)
					isValid = false
				}
			}

			val weightKgText = binding.textInputEditTextWeightKg.text
			if (weightKgText?.isEmpty() == true) {
				binding.textInputEditTextWeightKg.error = getString(R.string.profile_error)
				isValid = false
			} else {
				if (parseDouble(weightKgText) > 0.0) {
					binding.textInputEditTextWeightKg.error = null
				} else {
					binding.textInputEditTextWeightKg.error = getString(R.string.profile_error)
					isValid = false
				}
			}

			binding.textInputEditTextHeightImperFeet.error = null
			binding.textInputEditTextHeightImperInches.error = null
			binding.textInputEditTextWeightPounds.error = null
		} else {
			val feetInches = binding.textInputEditTextHeightImperFeet.text
			if (feetInches?.isEmpty() == true) {
				binding.textInputEditTextHeightImperFeet.error = getString(R.string.profile_error)
				isValid = false
			} else {
				if (parseInt(feetInches) > 0) {
					binding.textInputEditTextHeightImperFeet.error = null
				} else {
					binding.textInputEditTextHeightImperFeet.error = getString(R.string.profile_error)
					isValid = false
				}
			}

			val inchesText = binding.textInputEditTextHeightImperInches.text
			if (inchesText?.isEmpty() == true) {
				binding.textInputEditTextHeightImperInches.error = getString(R.string.profile_error)
				isValid = false
			} else {
				if (parseInt(inchesText) >= 0) {
					binding.textInputEditTextHeightImperInches.error = null
				} else {
					binding.textInputEditTextHeightImperInches.error = getString(R.string.profile_error)
					isValid = false
				}
			}

			val weightPoundsText = binding.textInputEditTextWeightPounds.text
			if (weightPoundsText?.isEmpty() == true) {
				binding.textInputEditTextWeightPounds.error = getString(R.string.profile_error)
				isValid = false
			} else {
				if (parseDouble(weightPoundsText) > 0.0) {
					binding.textInputEditTextWeightPounds.error = null
				} else {
					binding.textInputEditTextWeightPounds.error = getString(R.string.profile_error)
					isValid = false
				}
			}

			binding.textInputEditTextWeightKg.error = null
			binding.textInputEditTextHeightCm.error = null
		}

		val ageText = binding.TextInputEditTextAge.text
		if (ageText?.isEmpty() == true) {
			binding.TextInputEditTextAge.error = getString(R.string.profile_error)
			isValid = false
		} else {
			if (parseInt(ageText) > 0) {
				binding.TextInputEditTextAge.error = null
			} else {
				binding.TextInputEditTextAge.error = getString(R.string.profile_error)
				isValid = false
			}
		}

		return isValid
	}

	private fun parseDouble(text: CharSequence?): Double {
		var number = 0.0
		text?.let {
			try {
				number = text.toString().toDouble()
			} catch (e: NumberFormatException) {
				w("Failed to parse double")
			}
		}

		return number
	}

	private fun parseInt(text: CharSequence?): Int {
		var number = 0
		text?.let {
			try {
				number = text.toString().toInt()
			} catch (e: NumberFormatException) {
				w("Failed to parse int")
			}
		}

		return number
	}

	private fun populateInput(profile: Profile) {
		val totalInches = Data.cmToInch(profile.heightCm)

		val feet = floor(totalInches / 12.0).toInt()
		val inches = (totalInches % 12).toInt()

		val totalCmStr = profile.heightCm.roundToInt().toString()

		val pounds = Data.kgToLbs(profile.weightKg)
		val weightPoundsStr = "%.01f".format(pounds)
		val weightKgStr = "%.01f".format(profile.weightKg)

		// Only update the text field if it's actually a different value, prevents update loops
		if (binding.textInputEditTextHeightImperFeet.text?.toString() != "$feet")
			binding.textInputEditTextHeightImperFeet.setText("$feet")
		if (binding.textInputEditTextHeightImperInches.text?.toString() != "$inches")
			binding.textInputEditTextHeightImperInches.setText("$inches")
		if (binding.textInputEditTextHeightCm.text?.toString() != totalCmStr)
			binding.textInputEditTextHeightCm.setText(totalCmStr)
		if (binding.TextInputEditTextAge.text?.toString() != "${profile.ageYears}")
			binding.TextInputEditTextAge.setText("${profile.ageYears}")
		if (binding.textInputEditTextWeightPounds.text?.toString() != weightPoundsStr)
			binding.textInputEditTextWeightPounds.setText(weightPoundsStr)
		if (binding.textInputEditTextWeightKg.text?.toString() != weightKgStr)
			binding.textInputEditTextWeightKg.setText(weightKgStr)

		if (binding.metricSwitch.isChecked != profile.displayMetric)
			binding.metricSwitch.isChecked = profile.displayMetric

		binding.textInputLayoutWeightKg.isVisible = profile.displayMetric
		binding.textInputLayoutWeightPounds.isVisible = !profile.displayMetric

		binding.textInputLayoutHeightCm.visibility = if (profile.displayMetric) View.VISIBLE else View.INVISIBLE
		binding.textInputLayoutHeightImperFeet.visibility =
			if (!profile.displayMetric) View.VISIBLE else View.INVISIBLE
		binding.textInputLayoutHeightImperInches.visibility =
			if (!profile.displayMetric) View.VISIBLE else View.INVISIBLE

		if (!isCheckedGender(profile.gender)) {
			when (profile.gender) {
				Gender.Male -> binding.genderButtonGroup.check(R.id.gender_button_male)
				Gender.Female -> binding.genderButtonGroup.check(R.id.gender_button_female)
			}
		}

		updateUi(profile)
	}

	private fun updateUi(profile: Profile) {
		if (initialPopulationRequired) {
			initialPopulationRequired = false
			populateInput(profile)
		}

		val bmi = calculateBmi(profile)
		val bmiCategory = when {
			bmi < 18.5 -> getString(R.string.profile_bmi_category_underweight)
			bmi < 25.0 -> getString(R.string.profile_bmi_category_normal)
			bmi < 30.0 -> getString(R.string.profile_bmi_category_overweight)
			bmi < 40.0 -> getString(R.string.profile_bmi_category_obese)
			bmi >= 40.0 -> getString(R.string.profile_bmi_category_morbidly_obese)
			else -> ""
		}

		binding.metricSwitch.isChecked = profile.displayMetric

		binding.textViewBmiValue.text = getString(R.string.profile_bmi_value, bmi, bmiCategory)

		val bmr = calculateBmr(profile)
		binding.textViewBmrValue.text = getString(R.string.profile_bmr_value, bmr)
	}

	private fun isCheckedGender(newGender: Gender): Boolean {
		return when (newGender) {
			Gender.Male -> binding.genderButtonGroup.checkedRadioButtonId == R.id.gender_button_male
			Gender.Female -> binding.genderButtonGroup.checkedRadioButtonId == R.id.gender_button_female
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

	private fun isMetricSystem(): Boolean {
		val locale: Locale = LocaleList.getDefault()[0]
		val imperialCountries = listOf("US", "LR", "MM")
		return !imperialCountries.contains(locale.country)
	}
}