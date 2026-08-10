package com.darkrockstudios.apps.fasttrack.screens.main

import android.app.ComponentCaller
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import com.darkrockstudios.apps.fasttrack.BuildConfig
import com.darkrockstudios.apps.fasttrack.FastingNotificationManager
import com.darkrockstudios.apps.fasttrack.R
import com.darkrockstudios.apps.fasttrack.data.Stages
import com.darkrockstudios.apps.fasttrack.data.activefast.ActiveFastRepository
import com.darkrockstudios.apps.fasttrack.data.settings.SettingsDatasource
import com.darkrockstudios.apps.fasttrack.data.settings.ThemeMode
import com.darkrockstudios.apps.fasttrack.screens.fasting.ExternalRequests
import com.darkrockstudios.apps.fasttrack.screens.fasting.StartFastRequest
import com.darkrockstudios.apps.fasttrack.screens.info.InfoActivity
import com.darkrockstudios.apps.fasttrack.screens.intro.IntroActivity
import com.darkrockstudios.apps.fasttrack.screens.settings.SettingsActivity
import com.darkrockstudios.apps.fasttrack.ui.theme.FastTrackTheme
import com.darkrockstudios.cairn.CairnAboutOverlay
import com.darkrockstudios.cairn.CairnAppId
import com.darkrockstudios.cairn.CairnConfig
import org.koin.android.ext.android.inject
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalFoundationApi::class)
@ExperimentalTime
class MainActivity : AppCompatActivity() {
	private var startFastRequestState by mutableStateOf<StartFastRequest?>(null)
	private var stopFastRequestState by mutableStateOf(false)
	private var themeModeState by mutableStateOf(ThemeMode.SYSTEM)
	private val settings by inject<SettingsDatasource>()
	private val fastingRepository by inject<ActiveFastRepository>()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		enableEdgeToEdge()
		WindowCompat.getInsetsController(window, window.decorView)
			.isAppearanceLightStatusBars = false

		themeModeState = settings.getThemeMode()
		handleStartFastExtra(intent)

		if (!settings.getIntroSeen()) {
			startActivity(Intent(this, IntroActivity::class.java))
		}

		setContent {
			FastTrackTheme(themeMode = themeModeState) {
				var aboutVisible by rememberSaveable { mutableStateOf(false) }
				Box(Modifier.fillMaxSize()) {
					MainScreen(
						repository = fastingRepository,
						onShareClick = { shareText() },
						onInfoClick = { startActivity(Intent(this@MainActivity, InfoActivity::class.java)) },
						onAboutClick = { aboutVisible = true },
						onSettingsClick = { startActivity(Intent(this@MainActivity, SettingsActivity::class.java)) },
						externalRequests = ExternalRequests(
							startFastRequest = startFastRequestState,
							stopFastRequested = stopFastRequestState,
							consumeStartFastRequest = { startFastRequestState = null },
							consumeStopFastRequest = { stopFastRequestState = false },
						),
					)
					CairnAboutOverlay(
						visible = aboutVisible,
						config = CairnConfig(
							currentAppId = CairnAppId.FastTrack,
							versionName = BuildConfig.VERSION_NAME,
						),
						onDismissed = { aboutVisible = false },
					)
				}
			}
		}
	}

	override fun onStart() {
		super.onStart()
		val currentMode = settings.getThemeMode()
		if (currentMode != themeModeState) {
			themeModeState = currentMode
		}
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

	companion object {
		const val START_FAST_EXTRA = "START_FAST"
		const val START_FAST_NOW_EXTRA = "START_FAST_NOW"
		const val STOP_FAST_EXTRA = "STOP_FAST"
	}
}
