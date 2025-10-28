package com.darkrockstudios.apps.fasttrack.screens.main

import android.app.ComponentCaller
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.net.toUri
import androidx.core.view.WindowCompat
import com.darkrockstudios.apps.fasttrack.FastingNotificationManager
import com.darkrockstudios.apps.fasttrack.R
import com.darkrockstudios.apps.fasttrack.data.Stages
import com.darkrockstudios.apps.fasttrack.data.activefast.ActiveFastRepository
import com.darkrockstudios.apps.fasttrack.data.settings.SettingsDatasource
import com.darkrockstudios.apps.fasttrack.screens.fasting.ExternalRequests
import com.darkrockstudios.apps.fasttrack.screens.fasting.StartFastRequest
import com.darkrockstudios.apps.fasttrack.screens.info.InfoActivity
import com.darkrockstudios.apps.fasttrack.screens.intro.IntroActivity
import com.darkrockstudios.apps.fasttrack.screens.settings.SettingsActivity
import com.darkrockstudios.apps.fasttrack.ui.theme.FastTrackTheme
import com.vansuita.materialabout.builder.AboutBuilder
import org.koin.android.ext.android.inject
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalFoundationApi::class)
@ExperimentalTime
class MainActivity : AppCompatActivity() {
	private var startFastRequestState by mutableStateOf<StartFastRequest?>(null)
	private var stopFastRequestState by mutableStateOf(false)
	private val settings by inject<SettingsDatasource>()
	private val fastingRepository by inject<ActiveFastRepository>()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		enableEdgeToEdge()
		WindowCompat.getInsetsController(window, window.decorView)
			.isAppearanceLightStatusBars = false

		handleStartFastExtra(intent)

		if (!settings.getIntroSeen()) {
			startActivity(Intent(this, IntroActivity::class.java))
		}

		setContent {
			FastTrackTheme {
				MainScreen(
					repository = fastingRepository,
					onShareClick = { shareText() },
					onInfoClick = { startActivity(Intent(this, InfoActivity::class.java)) },
					onAboutClick = { showAbout() },
					onSettingsClick = { startActivity(Intent(this, SettingsActivity::class.java)) },
					externalRequests = ExternalRequests(
						startFastRequest = startFastRequestState,
						stopFastRequested = stopFastRequestState,
						consumeStartFastRequest = { startFastRequestState = null },
						consumeStopFastRequest = { stopFastRequestState = false },
					),
				)
			}
		}
	}

	override fun onStart() {
		super.onStart()
		setupFastingNotification()
	}

	private fun setupFastingNotification() {
		val shouldShowNotification = settings.getShowFastingNotification()

		if (fastingRepository.isFasting() && shouldShowNotification) {
			val elapsedTime = fastingRepository.getElapsedFastTime()
			FastingNotificationManager.postFastingNotification(this, elapsedTime)
		} else {
			FastingNotificationManager.cancelFastingNotification(this)
		}
	}

	override fun onNewIntent(intent: Intent, caller: ComponentCaller) {
		super.onNewIntent(intent, caller)
		handleStartFastExtra(intent)
	}

	override fun onNewIntent(intent: Intent) {
		super.onNewIntent(intent)
		handleStartFastExtra(intent)
	}

	private fun handleStartFastExtra(intent: Intent?) {
		if (intent?.getBooleanExtra(START_FAST_EXTRA, false) == true) {
			val startNow = intent.getBooleanExtra(START_FAST_NOW_EXTRA, false)
			startFastRequestState = StartFastRequest(startNow)
		} else if (intent?.getBooleanExtra(STOP_FAST_EXTRA, false) == true) {
			stopFastRequestState = true
		}
	}

	private fun shareText() {
		val elapsedHours: Long
		val elapsedMinutes: Int

		val elapsedTime = fastingRepository.getElapsedFastTime()
		elapsedTime.toComponents { hours, minutes, _, _ ->
			elapsedHours = hours
			elapsedMinutes = minutes
		}

		val curPhase = Stages.getCurrentPhase(elapsedTime)
		val shareText = if (fastingRepository.isFasting()) {
			val energyModeStr =
				if (curPhase.fatBurning) {
					getString(R.string.fasting_energy_mode_fat)
				} else {
					getString(R.string.fasting_energy_mode_glucose)
				}
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
			.setPhoto(R.drawable.darkrockstudios_logo)
			.setCover(R.mipmap.profile_cover)
			.setName(R.string.about_name)
			.setSubTitle(R.string.about_subtitle)
			.setBrief(R.string.about_brief)
			.setAppIcon(R.drawable.app_icon)
			.setAppName(R.string.app_name)
			.addGitHubLink("Wavesonics")
			.addWebsiteLink("https://darkrock.studio/")
			.addLink(
				R.drawable.ic_discord,
				R.string.about_discord,
				"https://discord.gg/ju2RQa5x8W".toUri()
			)
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
