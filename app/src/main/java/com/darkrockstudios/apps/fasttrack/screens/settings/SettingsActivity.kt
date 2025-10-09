package com.darkrockstudios.apps.fasttrack.screens.settings

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.darkrockstudios.apps.fasttrack.FastingNotificationManager
import com.darkrockstudios.apps.fasttrack.R
import com.darkrockstudios.apps.fasttrack.data.activefast.ActiveFastRepository
import com.darkrockstudios.apps.fasttrack.data.settings.SettingsDatasource
import com.darkrockstudios.apps.fasttrack.ui.theme.FastTrackTheme
import com.darkrockstudios.apps.fasttrack.utils.MAX_COLUMN_WIDTH
import io.github.aakira.napier.Napier
import org.koin.android.ext.android.inject

class SettingsActivity : AppCompatActivity() {
	private val settings by inject<SettingsDatasource>()
	private val activeFastRepository by inject<ActiveFastRepository>()
	private lateinit var requestNotificationPermission: ActivityResultLauncher<String>
	private var pendingNotificationToggle = false
	private var notificationSettingState by mutableStateOf(false)

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		enableEdgeToEdge()
		WindowCompat.getInsetsController(window, window.decorView)
			.isAppearanceLightStatusBars = false

		notificationSettingState = settings.getShowFastingNotification()
		registerNotificationPermissionCallback()

		setContent {
			FastTrackTheme {
				SettingsScreen(
					onBack = { finish() },
					settings = settings,
					notificationSettingState = notificationSettingState,
					onNotificationSettingChanged = { enabled -> handleNotificationSettingChange(enabled) }
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
}

@Composable
private fun SettingsScreen(
	onBack: () -> Unit,
	settings: SettingsDatasource,
	notificationSettingState: Boolean,
	onNotificationSettingChanged: (Boolean) -> Unit
) {
	Scaffold(
		topBar = {
			TopAppBar(
				colors = TopAppBarDefaults.topAppBarColors(
					containerColor = MaterialTheme.colorScheme.primaryContainer,
					titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
					navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
				),
				title = { Text(text = stringResource(id = R.string.title_activity_settings)) },
				navigationIcon = {
					IconButton(onClick = onBack) {
						Icon(
							Icons.Filled.ArrowBack,
							contentDescription = stringResource(id = R.string.back)
						)
					}
				},
			)
		}
	) { paddingValues ->
		SettingsList(
			paddingValues = paddingValues,
			settings = settings,
			notificationSettingState = notificationSettingState,
			onNotificationSettingChanged = onNotificationSettingChanged
		)
	}
}

@Composable
private fun SettingsList(
	paddingValues: PaddingValues,
	settings: SettingsDatasource,
	notificationSettingState: Boolean,
	onNotificationSettingChanged: (Boolean) -> Unit
) {
	var fancyBackground by remember { mutableStateOf(settings.getShowFancyBackground()) }

	Box(modifier = Modifier.fillMaxSize()) {
		LazyColumn(
			modifier = Modifier
				.widthIn(max = MAX_COLUMN_WIDTH)
				.fillMaxHeight()
				.align(Alignment.Center),
			contentPadding = paddingValues
		) {
			item(key = "fasting_notification") {
				SettingsItem(
					headline = R.string.settings_fasting_notification_title,
					details = R.string.settings_fasting_notification_subtitle,
					value = notificationSettingState,
					onChange = { checked ->
						onNotificationSettingChanged(checked)
					}
				)
			}
			item(key = "fancy_background") {
				SettingsItem(
					headline = R.string.settings_fancy_background_title,
					details = R.string.settings_fancy_background_subtitle,
					value = fancyBackground,
					onChange = { checked ->
						fancyBackground = checked
						settings.setShowFancyBackground(checked)
					}
				)
			}
		}
	}
}

@Composable
private fun SettingsItem(
	@StringRes headline: Int,
	@StringRes details: Int,
	value: Boolean,
	onChange: (enabled: Boolean) -> Unit
) {
	ListItem(
		headlineContent = {
			Text(
				text = stringResource(id = headline),
				style = MaterialTheme.typography.labelLarge,
				fontWeight = FontWeight.Bold
			)
		},
		supportingContent = {
			Text(
				text = stringResource(id = details),
				style = MaterialTheme.typography.bodySmall
			)
		},
		trailingContent = {
			Switch(
				checked = value,
				onCheckedChange = onChange
			)
		}
	)
}