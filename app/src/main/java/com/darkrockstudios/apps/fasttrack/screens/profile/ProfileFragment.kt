package com.darkrockstudios.apps.fasttrack.screens.profile

import android.os.Bundle
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
import com.darkrockstudios.apps.fasttrack.utils.Utils
import com.log4k.i
import com.log4k.w
import kotlinx.android.synthetic.main.profile_fragment.*
import kotlin.math.floor
import kotlin.math.pow
import kotlin.math.roundToInt


class ProfileFragment: Fragment()
{
	companion object
	{
		fun newInstance() = ProfileFragment()
	}

	private val viewModel by viewModels<ProfileViewModel>()
	private var initialPopulationRequired = true

	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)

		Satchel.storage.apply {
			viewModel.profile.postValue(getOrSet(Data.KEY_PROFILE, Profile()))
		}
	}

	override fun onCreateView(
			inflater: LayoutInflater, container: ViewGroup?,
			savedInstanceState: Bundle?): View?
	{
		return inflater.inflate(R.layout.profile_fragment, container, false)
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?)
	{
		super.onViewCreated(view, savedInstanceState)

		viewModel.profile.observe(viewLifecycleOwner, ::updateUi)

		TextInputEditText_height_imper_feet.doOnTextChanged { _, _, _, _ -> updateProfile() }
		TextInputEditText_height_imper_inches.doOnTextChanged { _, _, _, _ -> updateProfile() }
		TextInputEditText_height_cm.doOnTextChanged { _, _, _, _ -> updateProfile() }
		TextInputEditText_age.doOnTextChanged { _, _, _, _ -> updateProfile() }
		TextInputEditText_weight_pounds.doOnTextChanged { _, _, _, _ -> updateProfile() }
		TextInputEditText_weight_kg.doOnTextChanged { _, _, _, _ -> updateProfile() }
		gender_button_ground.setOnCheckedChangeListener { _, _ -> updateProfile() }

		textView_bmi_label.setOnClickListener {
			Utils.showInfoDialog(R.string.info_dialog_bmi_title, R.string.info_dialog_bmi_content, requireContext())
		}

		textView_bmr_label.setOnClickListener {
			Utils.showInfoDialog(R.string.info_dialog_bmr_title, R.string.info_dialog_bmr_content, requireContext())
		}

		metric_switch.setOnCheckedChangeListener { _, isChecked ->
			viewModel.profile.value?.let { profile ->
				populateInput(profile.copy(displayMetric = isChecked))
				updateProfile()
			}
		}
	}

	private fun saveProfile()
	{
		val metric = metric_switch.isChecked

		val totalCm: Double
		val kg: Double

		if(metric)
		{
			totalCm = parseInt(TextInputEditText_height_cm.text).toDouble()
			kg = parseDouble(TextInputEditText_weight_kg.text)
		}
		else
		{
			val feet = parseInt(TextInputEditText_height_imper_feet.text)
			val inches = parseInt(TextInputEditText_height_imper_inches.text)
			val totalInches = (feet * 12) + inches
			totalCm = Data.inchToCm(totalInches)

			val pounds = parseDouble(TextInputEditText_weight_pounds.text)
			kg = Data.lbsToKg(pounds)
		}

		val age = parseInt(TextInputEditText_age.text)

		val gender = when(gender_button_ground.checkedRadioButtonId)
		{
			R.id.gender_button_male -> Gender.Male
			R.id.gender_button_female -> Gender.Female
			else                      -> Gender.Male
		}
		val updatedProfile = Profile(ageYears = age, heightCm = totalCm, weightKg = kg, gender = gender, displayMetric = metric)
		if(updatedProfile != viewModel.profile.value)
		{
			viewModel.profile.postValue(updatedProfile)

			Satchel.storage.apply {
				set(Data.KEY_PROFILE, updatedProfile)
			}

			i("Profile saved")
		}
	}

	private fun updateProfile()
	{
		if(validateProfile())
		{
			saveProfile()
		}
	}

	private fun validateProfile(): Boolean
	{
		var isValid = true

		val metric = metric_switch.isChecked
		if(metric)
		{
			val cmText = TextInputEditText_height_cm.text
			if(cmText?.isEmpty() == true)
			{
				TextInputEditText_height_cm.error = getString(R.string.profile_error)
				isValid = false
			}
			else
			{
				if(parseInt(cmText) > 0)
				{
					TextInputEditText_height_cm.error = null
				}
				else
				{
					TextInputEditText_height_cm.error = getString(R.string.profile_error)
					isValid = false
				}
			}

			val weightKgText = TextInputEditText_weight_kg.text
			if(weightKgText?.isEmpty() == true)
			{
				TextInputEditText_weight_kg.error = getString(R.string.profile_error)
				isValid = false
			}
			else
			{
				if(parseDouble(weightKgText) > 0.0)
				{
					TextInputEditText_weight_kg.error = null
				}
				else
				{
					TextInputEditText_weight_kg.error = getString(R.string.profile_error)
					isValid = false
				}
			}

			TextInputEditText_height_imper_feet.error = null
			TextInputEditText_height_imper_inches.error = null
			TextInputEditText_weight_pounds.error = null
		}
		else
		{
			val feetInches = TextInputEditText_height_imper_feet.text
			if(feetInches?.isEmpty() == true)
			{
				TextInputEditText_height_imper_feet.error = getString(R.string.profile_error)
				isValid = false
			}
			else
			{
				if(parseInt(feetInches) > 0)
				{
					TextInputEditText_height_imper_feet.error = null
				}
				else
				{
					TextInputEditText_height_imper_feet.error = getString(R.string.profile_error)
					isValid = false
				}
			}

			val inchesText = TextInputEditText_height_imper_inches.text
			if(inchesText?.isEmpty() == true)
			{
				TextInputEditText_height_imper_inches.error = getString(R.string.profile_error)
				isValid = false
			}
			else
			{
				if(parseInt(inchesText) >= 0)
				{
					TextInputEditText_height_imper_inches.error = null
				}
				else
				{
					TextInputEditText_height_imper_inches.error = getString(R.string.profile_error)
					isValid = false
				}
			}

			val weightPoundsText = TextInputEditText_weight_pounds.text
			if(weightPoundsText?.isEmpty() == true)
			{
				TextInputEditText_weight_pounds.error = getString(R.string.profile_error)
				isValid = false
			}
			else
			{
				if(parseDouble(weightPoundsText) > 0.0)
				{
					TextInputEditText_weight_pounds.error = null
				}
				else
				{
					TextInputEditText_weight_pounds.error = getString(R.string.profile_error)
					isValid = false
				}
			}

			TextInputEditText_weight_kg.error = null
			TextInputEditText_height_cm.error = null
		}

		val ageText = TextInputEditText_age.text
		if(ageText?.isEmpty() == true)
		{
			TextInputEditText_age.error = getString(R.string.profile_error)
			isValid = false
		}
		else
		{
			if(parseInt(ageText) > 0)
			{
				TextInputEditText_age.error = null
			}
			else
			{
				TextInputEditText_age.error = getString(R.string.profile_error)
				isValid = false
			}
		}

		return isValid
	}

	private fun parseDouble(text: CharSequence?): Double
	{
		var number = 0.0
		text?.let {
			try
			{
				number = text.toString().toDouble()
			}
			catch(e: NumberFormatException)
			{
				w("Failed to parse double")
			}
		}

		return number
	}

	private fun parseInt(text: CharSequence?): Int
	{
		var number = 0
		text?.let {
			try
			{
				number = text.toString().toInt()
			}
			catch(e: NumberFormatException)
			{
				w("Failed to parse int")
			}
		}

		return number
	}

	private fun populateInput(profile: Profile)
	{
		val totalInches = Data.cmToInch(profile.heightCm)

		val feet = floor(totalInches / 12.0).toInt()
		val inches = (totalInches % 12).toInt()

		val totalCmStr = profile.heightCm.roundToInt().toString()

		val pounds = Data.kgToLbs(profile.weightKg)
		val weightPoundsStr = "%.01f".format(pounds)
		val weightKgStr = "%.01f".format(profile.weightKg)

		// Only update the text field if it's actually a different value, prevents update loops
		if(TextInputEditText_height_imper_feet.text?.toString() != "$feet")
			TextInputEditText_height_imper_feet.setText("$feet")
		if(TextInputEditText_height_imper_inches.text?.toString() != "$inches")
			TextInputEditText_height_imper_inches.setText("$inches")
		if(TextInputEditText_height_cm.text?.toString() != totalCmStr)
			TextInputEditText_height_cm.setText(totalCmStr)
		if(TextInputEditText_age.text?.toString() != "${profile.ageYears}")
			TextInputEditText_age.setText("${profile.ageYears}")
		if(TextInputEditText_weight_pounds.text?.toString() != weightPoundsStr)
			TextInputEditText_weight_pounds.setText(weightPoundsStr)
		if(TextInputEditText_weight_kg.text?.toString() != weightKgStr)
			TextInputEditText_weight_kg.setText(weightKgStr)

		if(metric_switch.isChecked != profile.displayMetric)
			metric_switch.isChecked = profile.displayMetric

		textInputLayout_weight_kg.isVisible = profile.displayMetric
		textInputLayout_weight_pounds.isVisible = !profile.displayMetric

		textInputLayout_height_cm.visibility = if(profile.displayMetric) View.VISIBLE else View.INVISIBLE
		textInputLayout_height_imper_feet.visibility = if(!profile.displayMetric) View.VISIBLE else View.INVISIBLE
		textInputLayout_height_imper_inches.visibility = if(!profile.displayMetric) View.VISIBLE else View.INVISIBLE

		if(!isCheckedGender(profile.gender))
		{
			when(profile.gender)
			{
				Gender.Male -> gender_button_ground.check(R.id.gender_button_male)
				Gender.Female -> gender_button_ground.check(R.id.gender_button_female)
			}
		}

		updateUi(profile)
	}

	private fun updateUi(profile: Profile)
	{
		if(initialPopulationRequired)
		{
			initialPopulationRequired = false
			populateInput(profile)
		}

		val bmi = calculateBmi(profile)
		val bmiCategory = when
		{
			bmi < 18.5  -> getString(R.string.profile_bmi_category_underweight)
			bmi < 25.0  -> getString(R.string.profile_bmi_category_normal)
			bmi < 30.0  -> getString(R.string.profile_bmi_category_overweight)
			bmi < 40.0  -> getString(R.string.profile_bmi_category_obese)
			bmi >= 40.0 -> getString(R.string.profile_bmi_category_morbidly_obese)
			else        -> ""
		}

		metric_switch.isChecked = profile.displayMetric

		textView_bmi_value.text = getString(R.string.profile_bmi_value, bmi, bmiCategory)

		val bmr = calculateBmr(profile)
		textView_bmr_value.text = getString(R.string.profile_bmr_value, bmr)
	}

	private fun isCheckedGender(newGender: Gender): Boolean
	{
		return when(newGender)
		{
			Gender.Male -> gender_button_ground.checkedRadioButtonId == R.id.gender_button_male
			Gender.Female -> gender_button_ground.checkedRadioButtonId == R.id.gender_button_female
		}
	}

	private fun calculateBmi(profile: Profile): Double
	{
		return if(profile.isValid())
		{
			// weight (kg) / [height (m)]2
			val heightM = profile.heightCm / 100
			profile.weightKg / (heightM.pow(2.0))
		}
		else
		{
			0.0
		}
	}

	private fun calculateBmr(profile: Profile): Double
	{
		return when(profile.gender)
		{
			//BMR for Men = 66.47 + (13.75 * weight [kg]) + (5.003 * size [cm]) − (6.755 * age [years])
			Gender.Male ->
			{
				66.47 + (13.75 * profile.weightKg) + (5.003 * profile.heightCm) - (6.755 * profile.ageYears)
			}
			//BMR for Women = 655.1 + (9.563 * weight [kg]) + (1.85 * size [cm]) − (4.676 * age [years])
			Gender.Female ->
			{
				655.1 + (9.563 * profile.weightKg) + (1.85 * profile.heightCm) - (4.676 * profile.ageYears)
			}
		}
	}
}