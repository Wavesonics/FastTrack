package com.darkrockstudios.apps.fasttrack.screens.settings

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.LocaleList
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.WindowCompat
import androidx.lifecycle.coroutineScope
import com.darkrockstudios.apps.fasttrack.FastingNotificationManager
import com.darkrockstudios.apps.fasttrack.R
import com.darkrockstudios.apps.fasttrack.data.activefast.ActiveFastRepository
import com.darkrockstudios.apps.fasttrack.data.log.FastingLogRepository
import com.darkrockstudios.apps.fasttrack.data.log.ImportResult
import com.darkrockstudios.apps.fasttrack.data.log.LogExportFormat
import com.darkrockstudios.apps.fasttrack.data.settings.SettingsDatasource
import com.darkrockstudios.apps.fasttrack.data.settings.ThemeMode
import com.darkrockstudios.apps.fasttrack.ui.theme.FastTrackTheme
import io.github.aakira.napier.Napier
import kotlinx.coroutines.Dispatchers
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import java.io.File
import java.util.Locale

class SettingsActivity : AppCompatActivity() {
	private val settings by inject<SettingsDatasource>()
	private val activeFastRepository by inject<ActiveFastRepository>()
	private val logRepository by inject<FastingLogRepository>()
	private lateinit var requestNotificationPermission: ActivityResultLauncher<String>
	private lateinit var getContent: ActivityResultLauncher<String>
	private var pendingNotificationToggle = false
	private var notificationSettingState by mutableStateOf(false)
	private var stageAlertsSettingState by mutableStateOf(false)
	private var metricSystemSettingState by mutableStateOf(false)
	private var themeModeState by mutableStateOf(ThemeMode.SYSTEM)

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		enableEdgeToEdge()
		WindowCompat.getInsetsController(window, window.decorView)
			.isAppearanceLightStatusBars = false

		notificationSettingState = settings.getShowFastingNotification()
		stageAlertsSettingState = settings.getFastingAlerts()
		metricSystemSettingState = settings.getUseMetricSystem(default = isMetricSystemLocale())
		themeModeState = settings.getThemeMode()
		registerNotificationPermissionCallback()
		registerImportCallback()

		setContent {
			FastTrackTheme(themeMode = themeModeState) {
				SettingsScreen(
					onBack = { finish() },
					settings = settings,
					notificationSettingState = notificationSettingState,
					onNotificationSettingChanged = { enabled -> handleNotificationSettingChange(enabled) },
					stageAlertsSettingState = stageAlertsSettingState,
					onStageAlertsSettingChanged = { enabled -> handleStageAlertsSettingChange(enabled) },
					metricSystemSettingState = metricSystemSettingState,
					onMetricSystemSettingChanged = { enabled -> handleMetricSystemSettingChange(enabled) },
					themeModeState = themeModeState,
					onThemeModeChanged = { mode -> handleThemeModeChange(mode) },
					onExportClick = { format -> onExportLogBook(format) },
					onImportClick = { onImportLogBook() }
				)
			}
		}
	}

	private fun registerNotificationPermissionCallback() {
		requestNotificationPermission = registerForActivityResult(
			ActivityResultContracts.RequestPermission()
		) { isGranted: Boolean ->
			if (isGranted) {
				Napier.d("Notification permission granted")
				if (pendingNotificationToggle) {
					settings.setShowFastingNotification(true)
					notificationSettingState = true
					pendingNotificationToggle = false

					// Show the notification if there's an active fast
					if (activeFastRepository.isFasting()) {
						val elapsedTime = activeFastRepository.getElapsedFastTime()
						FastingNotificationManager.postFastingNotification(this, elapsedTime)
					}
				}
			} else {
				Napier.w("Notification permission denied")
				// Reset the toggle since permission was denied
				settings.setShowFastingNotification(false)
				notificationSettingState = false
				pendingNotificationToggle = false
			}
		}
	}

	private fun handleNotificationSettingChange(enabled: Boolean) {
		if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
			when {
				ContextCompat.checkSelfPermission(
					this,
					Manifest.permission.POST_NOTIFICATIONS
				) == PackageManager.PERMISSION_GRANTED -> {
					Napier.d("Notification permission already granted")
					settings.setShowFastingNotification(true)
					notificationSettingState = true

					// Show the notification if there's an active fast
					if (activeFastRepository.isFasting()) {
						val elapsedTime = activeFastRepository.getElapsedFastTime()
						FastingNotificationManager.postFastingNotification(this, elapsedTime)
					}
				}

				else -> {
					Napier.d("Requesting notification permission")
					pendingNotificationToggle = true
					requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
				}
			}
		} else {
			// Either disabled or Android < 13 (no permission needed)
			settings.setShowFastingNotification(enabled)
			notificationSettingState = enabled

			if (enabled) {
				// Show the notification if there's an active fast
				if (activeFastRepository.isFasting()) {
					val elapsedTime = activeFastRepository.getElapsedFastTime()
					FastingNotificationManager.postFastingNotification(this, elapsedTime)
				}
			} else {
				// Dismiss the notification if it's currently displayed
				FastingNotificationManager.cancelFastingNotification(this)
			}
		}
	}

	private fun handleStageAlertsSettingChange(enabled: Boolean) {
		settings.setFastingAlerts(enabled)
		stageAlertsSettingState = enabled
	}

	private fun handleMetricSystemSettingChange(enabled: Boolean) {
		settings.setUseMetricSystem(enabled)
		metricSystemSettingState = enabled
	}

	private fun handleThemeModeChange(mode: ThemeMode) {
		if (mode == themeModeState) return
		settings.setThemeMode(mode)
		themeModeState = mode
		recreate()
	}

	private fun isMetricSystemLocale(): Boolean {
		val locale: Locale = LocaleList.getDefault()[0]
		val imperialCountries = listOf("US", "LR", "MM")
		return !imperialCountries.contains(locale.country)
	}

	private fun registerImportCallback() {
		getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
			uri?.let {
				lifecycle.coroutineScope.launch(Dispatchers.Default) {
					// Auto-detect the file: EasyFast ZIP, iCalendar, ActivityStreams, or CSV
					val message = try {
						val bytes = contentResolver.openInputStream(it)?.use { s -> s.readBytes() }
						if (bytes == null) {
							getString(R.string.import_failed)
						} else if (isZip(bytes)) {
							importResultMessage(logRepository.importEasyFastBackup(bytes))
						} else {
							val text = bytes.toString(Charsets.UTF_8).removePrefix("\uFEFF")
							val head = text.trimStart()
							when {
								head.startsWith("BEGIN:VCALENDAR", ignoreCase = true) ->
									importResultMessage(logRepository.importIcs(text))

								(head.startsWith("{") || head.startsWith("[")) &&
									text.contains("activitystreams") ->
									importResultMessage(logRepository.importActivityStreams(text))

								else -> {
									val ok = logRepository.importLog(text)
									getString(if (ok) R.string.import_success else R.string.import_failed)
								}
							}
						}
					} catch (e: Exception) {
						Napier.w("Failed to import Log", e)
						getString(R.string.import_failed)
					}

					withContext(Dispatchers.Main) {
						Toast.makeText(this@SettingsActivity, message, Toast.LENGTH_LONG).show()
					}
				}
			}
		}
	}

	/** ZIP local-file-header magic bytes (PK). */
	private fun isZip(bytes: ByteArray): Boolean =
		bytes.size >= 4 &&
			bytes[0] == 0x50.toByte() && bytes[1] == 0x4B.toByte() &&
			bytes[2] == 0x03.toByte() && bytes[3] == 0x04.toByte()

	/** Turn an [ImportResult] into a user-facing toast message. */
	private fun importResultMessage(result: ImportResult): String =
		if (result.ok) {
			getString(R.string.import_easyfast_result, result.imported, result.skippedOverlapping)
		} else {
			getString(R.string.import_failed)
		}

	private fun onExportLogBook(format: LogExportFormat) {
		lifecycle.coroutineScope.launch {
			val content = when (format) {
				LogExportFormat.CSV -> logRepository.exportLog()
				LogExportFormat.ICS -> logRepository.exportIcs()
				LogExportFormat.ACTIVITY_STREAMS -> logRepository.exportActivityStreams()
			}

			// Locale-independent timestamp: fastingLogbook-YYYY-MM-DD-HHMM.<ext>
			val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
			val stamp = String.format(
				Locale.ROOT, "%04d-%02d-%02d-%02d%02d",
				now.year, now.monthNumber, now.dayOfMonth, now.hour, now.minute
			)
			val exportFile = File(cacheDir, "fastingLogbook-$stamp.${format.extension}")

			try {
				exportFile.writeText(content)

				val fileUri = FileProvider.getUriForFile(
					this@SettingsActivity,
					"${packageName}.fileprovider",
					exportFile
				)

				val sendIntent: Intent = Intent().apply {
					action = Intent.ACTION_SEND
					putExtra(Intent.EXTRA_STREAM, fileUri)
					type = format.mimeType
					addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
				}

				val shareIntent = Intent.createChooser(sendIntent, getString(R.string.app_name))
				startActivity(shareIntent)
			} catch (_: Exception) {
				Toast.makeText(
					this@SettingsActivity,
					getString(R.string.export_failed),
					Toast.LENGTH_SHORT
				).show()
			}
		}
	}

	private fun onImportLogBook() {
		// Allow both FastTrack CSV and EasyFast backup ZIP files
		getContent.launch("*/*")
	}
}
