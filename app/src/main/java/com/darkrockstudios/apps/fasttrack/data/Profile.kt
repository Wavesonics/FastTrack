package com.darkrockstudios.apps.fasttrack.data

import java.io.Serializable;

data class Profile(
		val ageYears: Int = 0,
		val heightCm: Double = 0.0,
		val weightKg: Double = 0.0,
		val gender: Gender = Gender.Male,
		val displayMetric: Boolean = false): Serializable
{
	companion object
	{
		private const val serialVersionUID = 8712361231924L
	}

	fun isValid() = ageYears > 0 && heightCm > 0.0 && weightKg > 0.0
}

enum class Gender
{ Male, Female }