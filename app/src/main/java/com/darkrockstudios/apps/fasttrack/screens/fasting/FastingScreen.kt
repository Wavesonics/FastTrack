package com.darkrockstudios.apps.fasttrack.screens.fasting

import android.content.res.Configuration
import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.darkrockstudios.apps.fasttrack.BuildConfig
import com.darkrockstudios.apps.fasttrack.R
import com.darkrockstudios.apps.fasttrack.screens.confetti.ConfettiState
import com.darkrockstudios.apps.fasttrack.screens.confetti.confettiEffect
import com.darkrockstudios.apps.fasttrack.screens.preview.getContext
import com.darkrockstudios.apps.fasttrack.ui.theme.Purple100
import com.darkrockstudios.apps.fasttrack.ui.theme.Purple700
import com.darkrockstudios.apps.fasttrack.ui.theme.fastBackgroundGradient
import com.darkrockstudios.apps.fasttrack.utils.Utils
import kotlinx.coroutines.delay
import org.koin.compose.viewmodel.koinViewModel

private val phaseTextColor_Light = Purple700
private val phaseTextColor_Dark = Purple100

@Composable
fun phaseTextColor(): Color {
	val isDark: Boolean = isSystemInDarkTheme()
	return if (isDark) {
		phaseTextColor_Dark
	} else {
		phaseTextColor_Light
	}
}


@Composable
fun FastingScreen(
	contentPaddingValues: PaddingValues,
	viewModel: IFastingViewModel = koinViewModel<FastingViewModel>(),
	externalRequests: ExternalRequests = ExternalRequests(),
) {
	val context = getContext()
	val scope = rememberCoroutineScope()
	val confetti = remember { ConfettiState() }
	val uiState by viewModel.uiState.collectAsState()

	LaunchedEffect(Unit) {
		viewModel.onCreate()
	}

	var showDateTimePicker by remember { mutableStateOf(false) }

	fun onShowStartFastSelector() {
		if (!uiState.isFasting) {
			AlertDialog.Builder(context)
				.setTitle(R.string.confirm_start_fast_title)
				.setPositiveButton(R.string.confirm_start_fast_positive) { _, _ -> viewModel.startFast() }
				.setNeutralButton(R.string.confirm_start_fast_neutral) { _, _ ->
					showDateTimePicker = true
				}
				.setNegativeButton(R.string.confirm_start_fast_negative, null)
				.show()
		}
	}

	if (showDateTimePicker) {
		val dateTimePickerState = rememberDateTimePickerDialogState()
		DateTimePickerDialog(
			onDismiss = { showDateTimePicker = false },
			onDateTimeSelected = { selectedDateTime ->
				viewModel.startFast(selectedDateTime)
				showDateTimePicker = false
			},
			title = stringResource(R.string.already_started_dialog_title),
			finishButton = stringResource(id = R.string.start_fast_button),
			state = dateTimePickerState,
		)
	}

	fun onShowEndFastConfirmation() {
		AlertDialog.Builder(context)
			.setTitle(R.string.confirm_end_fast_title)
			.setPositiveButton(R.string.confirm_end_fast_positive) { _, _ ->
				viewModel.endFast()
				confetti.start(scope)
			}
			.setNegativeButton(R.string.confirm_end_fast_negative, null)
			.show()
	}

	fun onShowInfoDialog(titleRes: Int, contentRes: Int) {
		Utils.showInfoDialog(
			titleRes,
			contentRes,
			context
		)
	}

	// Handle deep link requests to show dialogs or start/stop directly
	LaunchedEffect(externalRequests.startFastRequest) {
		externalRequests.startFastRequest?.let { req ->
			if (!uiState.isFasting) {
				if (req.startNow) {
					viewModel.startFast()
				} else {
					onShowStartFastSelector()
				}
			}
			externalRequests.consumeStartFastRequest()
		}
	}
	LaunchedEffect(externalRequests.stopFastRequested) {
		if (externalRequests.stopFastRequested) {
			if (uiState.isFasting) {
				onShowEndFastConfirmation()
			}
			externalRequests.consumeStopFastRequest()
		}
	}

	DisposableEffect(Unit) {
		viewModel.setupAlerts()
		onDispose { }
	}

	LaunchedEffect(uiState.isFasting) {
		while (uiState.isFasting) {
			viewModel.updateUi()
			delay(10)
		}
	}

	// Get the current configuration to check orientation
	val configuration = LocalConfiguration.current
	val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

	Box(
		modifier = Modifier
			.fillMaxSize()
			.fastBackgroundGradient(show = uiState.showGradientBackground)
			.confettiEffect(confetti)
			.padding(contentPaddingValues)
	) {
		if (isLandscape) {
			Row(
				modifier = Modifier
					.fillMaxSize()
					.padding(16.dp),
				verticalAlignment = Alignment.Top
			) {
				FastHeadingContent(
					uiState = uiState,
					modifier = Modifier
						.weight(1f)
						.fillMaxHeight()
						.padding(end = 8.dp)
				)

				Spacer(modifier = Modifier.size(height = 16.dp, width = 1.dp))

				FastDetailsContent(
					uiState = uiState,
					onShowInfoDialog = ::onShowInfoDialog,
					viewModel = viewModel,
					onShowEndFastConfirmation = ::onShowEndFastConfirmation,
					onShowStartFastSelector = ::onShowStartFastSelector,
					modifier = Modifier
						.weight(1f)
						.fillMaxHeight()
						.padding(start = 8.dp)
				)
			}
		} else {
			Column(
				modifier = Modifier
					.fillMaxSize()
					.padding(16.dp),
				horizontalAlignment = Alignment.CenterHorizontally
			) {
				FastHeadingContent(
					uiState = uiState,
					modifier = Modifier.fillMaxWidth()
				)

				Spacer(modifier = Modifier.size(height = 64.dp, width = 1.dp))

				FastDetailsContent(
					uiState = uiState,
					onShowInfoDialog = ::onShowInfoDialog,
					viewModel = viewModel,
					onShowEndFastConfirmation = ::onShowEndFastConfirmation,
					onShowStartFastSelector = ::onShowStartFastSelector,
					modifier = Modifier.fillMaxWidth()
				)
			}
		}
	}
}

@Composable
private fun FastHeadingContent(
	uiState: IFastingViewModel.FastingUiState,
	modifier: Modifier = Modifier
) {
	Column(
		modifier = modifier,
		horizontalAlignment = Alignment.CenterHorizontally
	) {
		// Stage Title
		Text(
			text = uiState.stageTitle,
			style = MaterialTheme.typography.headlineMedium,
			color = MaterialTheme.colorScheme.onBackground,
			fontWeight = FontWeight.Bold,
			textAlign = TextAlign.Center,
			modifier = Modifier.padding(bottom = 8.dp)
		)

		TimeLine(
			elapsedHours = uiState.elapsedHours,
			modifier = Modifier
				.fillMaxWidth()
				.padding(vertical = 8.dp)
		)

		// Energy Mode
		Text(
			text = uiState.energyMode,
			style = MaterialTheme.typography.labelMedium,
			color = MaterialTheme.colorScheme.onBackground,
			textAlign = TextAlign.Center,
			modifier = Modifier.padding(bottom = 16.dp)
		)

		Spacer(modifier = Modifier.size(height = 16.dp, width = 1.dp))

		// Timer
		Row(
			verticalAlignment = Alignment.Bottom,
			modifier = Modifier.padding(bottom = 16.dp)
		) {
			Text(
				text = uiState.timerText,
				style = MaterialTheme.typography.displayLarge,
				fontWeight = FontWeight.Bold,
			)
			Text(
				text = uiState.milliseconds,
				style = MaterialTheme.typography.headlineMedium,
				modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
			)
		}
	}
}

@Composable
private fun FastDetailsContent(
	uiState: IFastingViewModel.FastingUiState,
	onShowInfoDialog: (Int, Int) -> Unit,
	viewModel: IFastingViewModel,
	onShowEndFastConfirmation: () -> Unit,
	onShowStartFastSelector: () -> Unit,
	modifier: Modifier = Modifier
) {
	Column(
		modifier = modifier,
		horizontalAlignment = Alignment.CenterHorizontally
	) {
		// Phase Information
		Column(
			modifier = Modifier
				.fillMaxWidth()
				.padding(bottom = 16.dp)
		) {
			// Fat Burn Phase
			Row(
				modifier = Modifier
					.fillMaxWidth()
					.padding(vertical = 4.dp),
				horizontalArrangement = Arrangement.SpaceBetween,
				verticalAlignment = Alignment.CenterVertically
			) {
				TextButton(
					onClick = {
						onShowInfoDialog(
							R.string.info_dialog_fat_burn_title,
							R.string.info_dialog_fat_burn_content
						)
					}
				) {
					Icon(
						painter = painterResource(id = R.drawable.ic_more_info),
						tint = phaseTextColor(),
						contentDescription = null,
						modifier = Modifier.padding(end = 8.dp)
					)
					Text(
						text = stringResource(id = R.string.fast_fat_burn_label),
						style = MaterialTheme.typography.headlineMedium,
						color = phaseTextColor(),
					)
				}
				Text(
					text = uiState.fatBurnTime,
					style = MaterialTheme.typography.headlineMedium,
					color = stageColor(uiState.fatBurnStageState)
				)
			}

			// Ketosis Phase
			Row(
				modifier = Modifier
					.fillMaxWidth()
					.padding(vertical = 4.dp),
				horizontalArrangement = Arrangement.SpaceBetween,
				verticalAlignment = Alignment.CenterVertically
			) {
				TextButton(
					onClick = {
						onShowInfoDialog(
							R.string.info_dialog_ketosis_title,
							R.string.info_dialog_ketosis_content
						)
					}
				) {
					Icon(
						painter = painterResource(id = R.drawable.ic_more_info),
						tint = phaseTextColor(),
						contentDescription = null,
						modifier = Modifier.padding(end = 8.dp)
					)
					Text(
						text = stringResource(id = R.string.fast_ketosis_label),
						style = MaterialTheme.typography.headlineMedium,
						color = phaseTextColor(),
					)
				}
				Text(
					text = uiState.ketosisTime,
					style = MaterialTheme.typography.headlineMedium,
					color = stageColor(uiState.ketosisStageState)
				)
			}

			// Autophagy Phase
			Row(
				modifier = Modifier
					.fillMaxWidth()
					.padding(vertical = 4.dp),
				horizontalArrangement = Arrangement.SpaceBetween,
				verticalAlignment = Alignment.CenterVertically
			) {
				TextButton(
					onClick = {
						onShowInfoDialog(
							R.string.info_dialog_autophagy_title,
							R.string.info_dialog_autophagy_content
						)
					}
				) {
					Icon(
						painter = painterResource(id = R.drawable.ic_more_info),
						tint = phaseTextColor(),
						contentDescription = null,
						modifier = Modifier.padding(end = 8.dp)
					)
					Text(
						text = stringResource(id = R.string.fast_autophagy_label),
						style = MaterialTheme.typography.headlineMedium,
						color = phaseTextColor(),
					)
				}
				Text(
					text = uiState.autophagyTime,
					style = MaterialTheme.typography.headlineMedium,
					color = stageColor(uiState.autophagyStageState)
				)
			}
		}

		// Stage Description
		Box(
			modifier = Modifier
				.weight(1f)
				.fillMaxWidth()
				.verticalScroll(rememberScrollState())
		) {
			Text(
				text = uiState.stageDescription,
				style = MaterialTheme.typography.bodyMedium,
				color = MaterialTheme.colorScheme.onBackground,
				modifier = Modifier.padding(top = 16.dp)
			)
		}

		// Bottom Controls
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.padding(top = 16.dp),
			horizontalArrangement = Arrangement.SpaceBetween,
			verticalAlignment = Alignment.CenterVertically
		) {
			// Notifications Checkbox
			Row(
				verticalAlignment = Alignment.CenterVertically
			) {
				Checkbox(
					checked = uiState.alertsEnabled,
					onCheckedChange = { viewModel.setAlertsEnabled(it) }
				)
				Text(
					text = stringResource(id = R.string.stage_alerts_checkbox),
					style = MaterialTheme.typography.labelLarge,
					color = MaterialTheme.colorScheme.onBackground,
					modifier = Modifier.padding(start = 8.dp)
				)
			}

			// Debug Button (only in debug builds)
			if (BuildConfig.DEBUG) {
				FloatingActionButton(
					onClick = { viewModel.debugIncreaseFastingTimeByOneHour() },
					modifier = Modifier.padding(end = 16.dp)
				) {
					Icon(
						imageVector = Icons.Default.Add,
						contentDescription = stringResource(id = R.string.debug_add_hour_button)
					)
				}
			}

			// Start/Stop Button
			if (uiState.isFasting) {
				FloatingActionButton(
					onClick = onShowEndFastConfirmation
				) {
					Icon(
						painter = painterResource(id = R.drawable.ic_fast_stop),
						contentDescription = stringResource(id = R.string.stop_fast_button_description)
					)
				}
			} else {
				FloatingActionButton(
					onClick = onShowStartFastSelector
				) {
					Icon(
						painter = painterResource(id = R.drawable.ic_start_fast),
						contentDescription = stringResource(id = R.string.start_fast_button_description)
					)
				}
			}
		}
	}
}

@Composable
private fun stageColor(stageState: IFastingViewModel.StageState): Color = when (stageState) {
	IFastingViewModel.StageState.StartedActive -> Color.Green
	IFastingViewModel.StageState.StartedInactive -> Color.Red
	IFastingViewModel.StageState.NotStarted -> Color.White
}

private val stageDropShadow = Shadow(
	color = Color.White.copy(alpha = 0.5f),
	offset = Offset(1f, 1f),
	blurRadius = 1f
)
