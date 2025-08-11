package com.darkrockstudios.apps.fasttrack.screens.main

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.core.content.FileProvider
import androidx.core.os.bundleOf
import androidx.core.view.WindowCompat
import androidx.lifecycle.coroutineScope
import com.darkrockstudios.apps.fasttrack.R
import com.darkrockstudios.apps.fasttrack.data.Stages
import com.darkrockstudios.apps.fasttrack.data.activefast.ActiveFastRepository
import com.darkrockstudios.apps.fasttrack.data.log.FastingLogRepository
import com.darkrockstudios.apps.fasttrack.data.settings.SettingsDatasource
import com.darkrockstudios.apps.fasttrack.screens.info.InfoActivity
import com.darkrockstudios.apps.fasttrack.screens.intro.IntroActivity
import com.darkrockstudios.apps.fasttrack.ui.theme.FastTrackTheme
import com.vansuita.materialabout.builder.AboutBuilder
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import java.io.File
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalFoundationApi::class)
@ExperimentalTime
class MainActivity : AppCompatActivity() {
	private val settings by inject<SettingsDatasource>()
	private val fastingRepository by inject<ActiveFastRepository>()
	private val logRepository by inject<FastingLogRepository>()

	private lateinit var getContent: ActivityResultLauncher<String>

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		enableEdgeToEdge()
		WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = false

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
					onExportClick = { onExportLogBook() },
					onImportClick = { onImportLogBook() },
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

		val elapsedTime = fastingRepository.getElapsedFastTime()
		elapsedTime.toComponents { hours, minutes, _, _ ->
			elapsedHours = hours
			elapsedMinutes = minutes
		}

		val curPhase = Stages.getCurrentPhase(elapsedTime)
		val shareText = if (fastingRepository.isFasting()) {
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

	private fun onExportLogBook() {
		lifecycle.coroutineScope.launch {
			val csvLog = logRepository.exportLog()

			val csvFile = File(cacheDir, "fastingLogbook.csv")

			try {
				csvFile.writeText(csvLog)

				val fileUri = FileProvider.getUriForFile(
					this@MainActivity,
					"${packageName}.fileprovider",
					csvFile
				)

				val sendIntent: Intent = Intent().apply {
					action = Intent.ACTION_SEND
					putExtra(Intent.EXTRA_STREAM, fileUri)
					type = "text/csv"
					addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
				}

				val shareIntent = Intent.createChooser(sendIntent, getString(R.string.app_name))
				startActivity(shareIntent)
			} catch (_: Exception) {
				Toast.makeText(this@MainActivity, getString(R.string.export_failed), Toast.LENGTH_SHORT).show()
			}
		}
	}

	private fun onImportLogBook() {
		getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
			uri?.let {
				lifecycle.coroutineScope.launch {
					try {
						val inputStream = contentResolver.openInputStream(it)
						val csvContent = inputStream?.bufferedReader()?.use { reader -> reader.readText() }

						if (csvContent != null) {
							val success = logRepository.importLog(csvContent)
							val messageResId = if (success) R.string.import_success else R.string.import_failed
							Toast.makeText(this@MainActivity, getString(messageResId), Toast.LENGTH_SHORT).show()
						} else {
							Toast.makeText(this@MainActivity, getString(R.string.import_failed), Toast.LENGTH_SHORT)
								.show()
						}
					} catch (e: Exception) {
						Toast.makeText(this@MainActivity, getString(R.string.import_failed), Toast.LENGTH_SHORT).show()
					}
				}
			}
		}

		getContent.launch("text/csv")
	}

	companion object {
		const val START_FAST_EXTRA = "START_FAST"
		const val START_FAST_NOW_EXTRA = "START_FAST_NOW"
		const val STOP_FAST_EXTRA = "STOP_FAST"
	}
}
