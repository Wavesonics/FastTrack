package com.darkrockstudios.apps.fasttrack.screens.main

import android.app.ComponentCaller
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.net.toUri
import androidx.core.view.WindowCompat
import com.darkrockstudios.apps.fasttrack.BuildConfig
import com.darkrockstudios.apps.fasttrack.FastingNotificationManager
import com.darkrockstudios.apps.fasttrack.R
import com.darkrockstudios.apps.fasttrack.data.activefast.ActiveFastRepository
import com.darkrockstudios.apps.fasttrack.data.settings.SettingsDatasource
import com.darkrockstudios.apps.fasttrack.data.settings.ThemeMode
import com.darkrockstudios.apps.fasttrack.screens.fasting.ExternalRequests
import com.darkrockstudios.apps.fasttrack.screens.fasting.StartFastRequest
import com.darkrockstudios.apps.fasttrack.screens.info.InfoActivity
import com.darkrockstudios.apps.fasttrack.screens.intro.IntroActivity
import com.darkrockstudios.apps.fasttrack.screens.settings.SettingsActivity
import com.darkrockstudios.apps.fasttrack.ui.theme.FastTrackTheme
import io.github.aakira.napier.Napier
import org.koin.android.ext.android.inject
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalFoundationApi::class)
@ExperimentalTime
class MainActivity : AppCompatActivity() {
	private var startFastRequestState by mutableStateOf<StartFastRequest?>(null)
	private var stopFastRequestState by mutableStateOf(false)
	private var shareRequestState by mutableStateOf(false)
	private var showAboutState by mutableStateOf(false)
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
				MainScreen(
					onShareClick = { shareRequestState = true },
					onInfoClick = { startActivity(Intent(this, InfoActivity::class.java)) },
					onAboutClick = { showAboutState = true },
					onSettingsClick = { startActivity(Intent(this, SettingsActivity::class.java)) },
					externalRequests = ExternalRequests(
						startFastRequest = startFastRequestState,
						stopFastRequested = stopFastRequestState,
						shareRequested = shareRequestState,
						consumeStartFastRequest = { startFastRequestState = null },
						consumeStopFastRequest = { stopFastRequestState = false },
						consumeShareRequest = { shareRequestState = false },
					),
				)

				if (showAboutState) {
					AboutDialog(
						versionName = BuildConfig.VERSION_NAME,
						onOpenUrl = { url -> openUrl(url) },
						onRateApp = { rateApp() },
						onShareApp = { shareApp() },
						onDismiss = { showAboutState = false },
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

	private fun openUrl(url: String) {
		try {
			startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
		} catch (e: ActivityNotFoundException) {
			Napier.w("No activity available to open url: $url", e)
		}
	}

	private fun rateApp() {
		try {
			startActivity(Intent(Intent.ACTION_VIEW, "market://details?id=$packageName".toUri()))
		} catch (e: ActivityNotFoundException) {
			// No Play Store on this device; fall back to the web listing
			openUrl("https://play.google.com/store/apps/details?id=$packageName")
		}
	}

	private fun shareApp() {
		val shareText =
			"${getString(R.string.app_name)} — https://play.google.com/store/apps/details?id=$packageName"
		val intent = Intent(Intent.ACTION_SEND).apply {
			type = "text/plain"
			putExtra(Intent.EXTRA_TEXT, shareText)
		}
		startActivity(Intent.createChooser(intent, null))
	}

	companion object {
		const val START_FAST_EXTRA = "START_FAST"
		const val START_FAST_NOW_EXTRA = "START_FAST_NOW"
		const val STOP_FAST_EXTRA = "STOP_FAST"
	}
}
