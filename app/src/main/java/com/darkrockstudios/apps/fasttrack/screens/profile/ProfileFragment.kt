package com.darkrockstudios.apps.fasttrack.screens.profile

import android.os.Bundle
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
import com.darkrockstudios.apps.fasttrack.data.Data
import com.darkrockstudios.apps.fasttrack.data.Profile
import com.log4k.w
import kotlinx.android.synthetic.main.profile_fragment.*
import kotlin.math.floor
import kotlin.math.pow
import kotlin.math.round


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

		profile_update_button.setOnClickListener { saveProfile() }
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

		val updatedProfile = Profile(ageYears = age, heightCm = totalCm, weightKg = kg)
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

		if(TextInputEditText_height_imper_feet.text?.toString() != "$feet")
			TextInputEditText_height_imper_feet.setText("$feet")
		if(TextInputEditText_height_imper_inches.text?.toString() != "$inches")
			TextInputEditText_height_imper_inches.setText("$inches")
		if(TextInputEditText_age.text?.toString() != "${profile.ageYears}")
			TextInputEditText_age.setText("${profile.ageYears}")
		if(TextInputEditText_weight_pounds.text?.toString() != "$weightPounds")
			TextInputEditText_weight_pounds.setText("$weightPounds")

		val bmi = calculateBmi(profile)
		textView_bmi_value.text = "$bmi"

		updateDirty(dirty)
	}

	private fun calculateBmi(profile: Profile): Int
	{
		return if(profile.isValid())
		{
			// weight (kg) / [height (m)]2
			val heightM = profile.heightCm / 100
			val rawBmi = profile.weightKg / (heightM.pow(2.0))
			round(rawBmi).toInt()
		}
		else
		{
			0
		}
	}
}