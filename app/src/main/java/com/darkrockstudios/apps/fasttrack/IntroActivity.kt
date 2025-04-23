package com.darkrockstudios.apps.fasttrack

import android.graphics.Color
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import com.darkrockstudios.apps.fasttrack.data.Data
import com.github.appintro.AppIntro
import com.github.appintro.AppIntroFragment

class IntroActivity : AppIntro() {
	private val storage by lazy { PreferenceManager.getDefaultSharedPreferences(this) }

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		addSlide(
			AppIntroFragment.newInstance(
				title = getString(R.string.intro_00_title),
				description = getString(R.string.intro_00_description),
				imageDrawable = R.drawable.intro_00,
				backgroundColor = Color.rgb(128, 91, 128) // Pastel Magenta
			)
		)

		addSlide(
			AppIntroFragment.newInstance(
				title = getString(R.string.intro_01_title),
				description = getString(R.string.intro_01_description),
				imageDrawable = R.drawable.intro_01,
				backgroundColor = Color.rgb(128, 91, 96) // Pastel Red
			)
		)

		addSlide(
			AppIntroFragment.newInstance(
				title = getString(R.string.intro_02_title),
				description = getString(R.string.intro_02_description),
				imageDrawable = R.drawable.intro_02,
				backgroundColor = Color.rgb(86, 108, 115) // Pastel Blue
			)
		)

		addSlide(
			AppIntroFragment.newInstance(
				title = getString(R.string.intro_03_title),
				description = getString(R.string.intro_03_description),
				backgroundColor = Color.rgb(110, 110, 110), // Pastel Gray
				imageDrawable = R.drawable.intro_03,
			)
		)

		addSlide(
			AppIntroFragment.newInstance(
				title = getString(R.string.intro_04_title),
				description = getString(R.string.intro_04_description),
				imageDrawable = R.drawable.intro_04,
				backgroundColor = Color.rgb(128, 91, 128) // Pastel Magenta
			)
		)
	}

	override fun onSkipPressed(currentFragment: Fragment?) {
		super.onSkipPressed(currentFragment)
		complete()
	}

	override fun onDonePressed(currentFragment: Fragment?) {
		super.onDonePressed(currentFragment)
		complete()
	}

	private fun complete() {
		storage.edit {
			putBoolean(Data.KEY_INTRO_SEEN, true)
		}
		finish()
	}
}
