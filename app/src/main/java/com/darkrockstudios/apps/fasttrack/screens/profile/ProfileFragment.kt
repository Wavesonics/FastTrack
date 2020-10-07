package com.darkrockstudios.apps.fasttrack.screens.profile

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import cafe.adriel.satchel.Satchel
import cafe.adriel.satchel.ktx.getOrSet
import com.darkrockstudios.apps.fasttrack.R
import com.darkrockstudios.apps.fasttrack.Util
import com.darkrockstudios.apps.fasttrack.data.Data
import com.darkrockstudios.apps.fasttrack.data.Gender
import com.darkrockstudios.apps.fasttrack.data.Profile
import com.log4k.w
import kotlinx.android.synthetic.main.profile_fragment.*
import kotlin.math.floor
import kotlin.math.pow


class ProfileFragment: Fragment()
{
	private val viewModel by viewModels<ProfileViewModel>()
	private var dirty = false

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

		TextInputEditText_height_imper_feet.doOnTextChanged { _, _, _, _ -> updateDirty(true) }
		TextInputEditText_height_imper_inches.doOnTextChanged { _, _, _, _ -> updateDirty(true) }
		TextInputEditText_age.doOnTextChanged { _, _, _, _ -> updateDirty(true) }
		TextInputEditText_weight_pounds.doOnTextChanged { _, _, _, _ -> updateDirty(true) }
		gender_button_ground.setOnCheckedChangeListener { _, _ -> updateDirty(true) }

		profile_update_button.setOnClickListener { saveProfile() }

		textView_bmi_label.setOnClickListener {
			Util.showInfoDialog(R.string.info_dialog_bmi_title, R.string.info_dialog_bmi_content, requireContext())
		}

		textView_bmr_label.setOnClickListener {
			Util.showInfoDialog(R.string.info_dialog_bmr_title, R.string.info_dialog_bmr_content, requireContext())
		}

		Handler(Looper.getMainLooper()).post { updateDirty(false) }
	}

	private fun saveProfile()
	{
		val feet = parseInt(TextInputEditText_height_imper_feet.text)
		val inches = parseInt(TextInputEditText_height_imper_inches.text)
		val totalInches = (feet * 12) + inches
		val totalCm = Data.inchToCm(totalInches)

		val pounds = parseDouble(TextInputEditText_weight_pounds.text)
		val kg = Data.lbsToKg(pounds)

		val age = parseInt(TextInputEditText_age.text)

		val gender = when(gender_button_ground.checkedRadioButtonId)
		{
			R.id.gender_button_male -> Gender.Male
			R.id.gender_button_female -> Gender.Female
			else                      -> Gender.Male
		}

		val updatedProfile = Profile(ageYears = age, heightCm = totalCm, weightKg = kg, gender = gender)
		viewModel.profile.postValue(updatedProfile)

		Satchel.storage.apply {
			set(Data.KEY_PROFILE, updatedProfile)
		}

		// Close keyboard
		val imm = getSystemService(requireContext(), InputMethodManager::class.java)
		imm?.hideSoftInputFromWindow(TextInputEditText_height_imper_feet.windowToken, 0)

		updateDirty(false)
	}

	private fun updateDirty(isDirty: Boolean)
	{
		dirty = isDirty
		profile_update_button.isEnabled = dirty
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

	private fun updateUi(profile: Profile)
	{
		val totalInches = Data.cmToInch(profile.heightCm)

		val feet = floor(totalInches / 12.0).toInt()
		val inches = (totalInches % 12).toInt()

		val weightPounds = Data.kgToLbs(profile.weightKg)

		// Only update the text field if it's actually a different value, prevents update loops
		if(TextInputEditText_height_imper_feet.text?.toString() != "$feet")
			TextInputEditText_height_imper_feet.setText("$feet")
		if(TextInputEditText_height_imper_inches.text?.toString() != "$inches")
			TextInputEditText_height_imper_inches.setText("$inches")
		if(TextInputEditText_age.text?.toString() != "${profile.ageYears}")
			TextInputEditText_age.setText("${profile.ageYears}")
		if(TextInputEditText_weight_pounds.text?.toString() != "$weightPounds")
			TextInputEditText_weight_pounds.setText("$weightPounds")

		when(profile.gender)
		{
			Gender.Male -> gender_button_ground.check(R.id.gender_button_male)
			Gender.Female -> gender_button_ground.check(R.id.gender_button_female)
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

		textView_bmi_value.text = getString(R.string.profile_bmi_value, bmi, bmiCategory)

		val bmr = calculateBmr(profile)
		textView_bmr_value.text = getString(R.string.profile_bmr_value, bmr)

		updateDirty(dirty)
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