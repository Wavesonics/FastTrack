package com.darkrockstudios.apps.fasttrack.screens.main

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.core.bundle.bundleOf
import com.darkrockstudios.apps.fasttrack.R
import com.darkrockstudios.apps.fasttrack.data.Stages
import com.darkrockstudios.apps.fasttrack.data.activefast.ActiveFastRepositoryImpl
import com.darkrockstudios.apps.fasttrack.data.settings.SettingsDatasource
import com.darkrockstudios.apps.fasttrack.screens.info.InfoActivity
import com.darkrockstudios.apps.fasttrack.screens.intro.IntroActivity
import com.darkrockstudios.apps.fasttrack.ui.theme.FastTrackTheme
import com.vansuita.materialabout.builder.AboutBuilder
import org.koin.android.ext.android.inject
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalFoundationApi::class)
@ExperimentalTime
class MainActivity : AppCompatActivity() {
	private val settings by inject<SettingsDatasource>()
	private val repository by inject<ActiveFastRepositoryImpl>()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		enableEdgeToEdge()

		handleStartFastExtra(intent)

		if (!settings.getIntroSeen()) {
			startActivity(Intent(this, IntroActivity::class.java))
		}

		setContent {
			FastTrackTheme {
				MainScreen(
					repository = repository,
					onShareClick = { shareText() },
					onInfoClick = { startActivity(Intent(this, InfoActivity::class.java)) },
					onAboutClick = { showAbout() },
				)
			}
		}
	}

	override fun onNewIntent(intent: Intent) {
		super.onNewIntent(intent)
		handleStartFastExtra(intent)
	}

	private fun handleStartFastExtra(intent: Intent?) {
		if (intent?.getBooleanExtra(START_FAST_EXTRA, false) == true) {
			val startNow = intent.getBooleanExtra(START_FAST_NOW_EXTRA, false)
			supportFragmentManager.setFragmentResult(
				START_FAST_EXTRA,
				bundleOf(START_FAST_EXTRA to true, START_FAST_NOW_EXTRA to startNow)
			)
		} else if (intent?.getBooleanExtra(STOP_FAST_EXTRA, false) == true) {
			supportFragmentManager.setFragmentResult(STOP_FAST_EXTRA, bundleOf(STOP_FAST_EXTRA to true))
		}
	}

	private fun shareText() {
		val elapsedHours: Long
		val elapsedMinutes: Int

		val elapsedTime = repository.getElapsedFastTime()
		elapsedTime.toComponents { hours, minutes, _, _ ->
			elapsedHours = hours
			elapsedMinutes = minutes
		}

		val curPhase = Stages.getCurrentPhase(elapsedTime)
		val shareText = if (repository.isFasting()) {
			val energyModeStr =
				if (curPhase.fatBurning) getString(R.string.fasting_energy_mode_fat) else getString(R.string.fasting_energy_mode_glucose)
			getString(R.string.share_text, elapsedHours, elapsedMinutes, energyModeStr)
		} else {
			getString(R.string.share_text_past_tense, elapsedHours, elapsedMinutes)
		}

		val sendIntent: Intent = Intent().apply {
			action = Intent.ACTION_SEND
			putExtra(Intent.EXTRA_TEXT, shareText)
			type = "text/plain"
		}

		val shareIntent = Intent.createChooser(sendIntent, null)
		startActivity(shareIntent)
	}

	private fun showAbout() {
		val view = AboutBuilder.with(this)
			.setPhoto(R.mipmap.profile_picture)
			.setCover(R.mipmap.profile_cover)
			.setName(R.string.about_name)
			.setSubTitle(R.string.about_subtitle)
			.setBrief(R.string.about_brief)
			.setAppIcon(R.drawable.app_icon)
			.setAppName(R.string.app_name)
			.addGitHubLink("Wavesonics")
			.addWebsiteLink("https://darkrock.studio/")
			.addFiveStarsAction()
			.setVersionNameAsAppSubTitle()
			.addShareAction(R.string.app_name)
			.setWrapScrollView(true)
			.setLinksAnimated(true)
			.setShowAsCard(true)
			.build()

		AlertDialog.Builder(this).setView(view).create().show()
	}

	companion object {
		const val START_FAST_EXTRA = "START_FAST"
		const val START_FAST_NOW_EXTRA = "START_FAST_NOW"
		const val STOP_FAST_EXTRA = "STOP_FAST"
	}
}