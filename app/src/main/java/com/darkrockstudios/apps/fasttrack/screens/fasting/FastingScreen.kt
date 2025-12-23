package com.darkrockstudios.apps.fasttrack.screens.fasting

import android.content.res.Configuration
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.currentStateAsState
import com.darkrockstudios.apps.fasttrack.BuildConfig
import com.darkrockstudios.apps.fasttrack.R
import com.darkrockstudios.apps.fasttrack.screens.confetti.ConfettiState
import com.darkrockstudios.apps.fasttrack.screens.confetti.confettiEffect
import com.darkrockstudios.apps.fasttrack.screens.preview.getContext
import com.darkrockstudios.apps.fasttrack.ui.theme.fastBackgroundGradient
import com.darkrockstudios.apps.fasttrack.utils.Utils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Duration.Companion.seconds

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

	var showStartDateTimePicker by remember { mutableStateOf(false) }
	var showEndDateTimePicker by remember { mutableStateOf(false) }

	fun onShowStartFastSelector() {
		if (!uiState.isFasting) {
			AlertDialog.Builder(context)
				.setTitle(R.string.confirm_start_fast_title)
				.setPositiveButton(R.string.confirm_start_fast_positive) { _, _ -> viewModel.startFast() }
				.setNeutralButton(R.string.confirm_start_fast_neutral) { _, _ ->
					showStartDateTimePicker = true
				}
				.setNegativeButton(R.string.confirm_start_fast_negative, null)
				.show()
		}
	}

	if (showStartDateTimePicker) {
		val dateTimePickerState = rememberDateTimePickerDialogState()
		DateTimePickerDialog(
			onDismiss = { showStartDateTimePicker = false },
			onDateTimeSelected = { selectedDateTime ->
				viewModel.startFast(selectedDateTime)
				showStartDateTimePicker = false
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
			.setNeutralButton(R.string.confirm_end_fast_neutral) { _, _ ->
				showEndDateTimePicker = true
			}
			.setNegativeButton(R.string.confirm_end_fast_negative, null)
			.show()
	}

	if (showEndDateTimePicker) {
		val dateTimePickerState = rememberDateTimePickerDialogState()
		DateTimePickerDialog(
			onDismiss = { showEndDateTimePicker = false },
			onDateTimeSelected = { selectedDateTime ->
				viewModel.endFast(selectedDateTime)
				showEndDateTimePicker = false
				confetti.start(scope)
			},
			title = stringResource(R.string.already_stopped_dialog_title),
			finishButton = stringResource(id = R.string.end_fast_button),
			state = dateTimePickerState,
			minInstant = uiState.fastStartTime
		)
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

	val lifecycleState by LocalLifecycleOwner.current.lifecycle.currentStateAsState()
	LaunchedEffect(uiState.isFasting, lifecycleState) {
		while (uiState.isFasting && lifecycleState == Lifecycle.State.RESUMED) {
			viewModel.updateUi()
			delay(10)
		}
	}

	val configuration = LocalConfiguration.current
	val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

	BoxWithConstraints(
		modifier = Modifier
			.fillMaxSize()
			.fastBackgroundGradient(show = uiState.showGradientBackground)
			.confettiEffect(confetti)
			.padding(contentPaddingValues)
	) {
		val isCompact = remember(maxHeight) { maxHeight < 500.dp }

		val spacing = rememberFastingSpacing(isCompact)
		val typography = rememberFastingTypography(isCompact)

		CompositionLocalProvider(
			LocalFastingSpacing provides spacing,
			LocalFastingTypography provides typography
		) {
			if (isLandscape) {
				Row(
					modifier = Modifier
						.fillMaxSize()
						.padding(spacing.large),
					verticalAlignment = Alignment.Top
				) {
					FastHeadingContent(
						uiState = uiState,
						modifier = Modifier
							.weight(1f)
							.fillMaxHeight()
							.padding(end = spacing.medium)
					)

					Spacer(modifier = Modifier.size(height = spacing.large, width = 1.dp))

					FastDetailsContent(
						uiState = uiState,
						onShowInfoDialog = ::onShowInfoDialog,
						viewModel = viewModel,
						onShowEndFastConfirmation = ::onShowEndFastConfirmation,
						onShowStartFastSelector = ::onShowStartFastSelector,
						modifier = Modifier
							.weight(1f)
							.fillMaxHeight()
							.padding(start = spacing.medium)
					)
				}
			} else {
				Column(
					modifier = Modifier
						.fillMaxSize()
						.padding(spacing.large),
					horizontalAlignment = Alignment.CenterHorizontally
				) {
					FastHeadingContent(
						uiState = uiState,
						modifier = Modifier.fillMaxWidth()
					)

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
}

@Composable
private fun FastHeadingContent(
	uiState: IFastingViewModel.FastingUiState,
	modifier: Modifier = Modifier
) {
	val scope = rememberCoroutineScope()
	val tooltipState = rememberTooltipState()
	val spacing = fastingSpacing()
	val typography = fastingTypography()

	@StringRes
	var phaseTooltipResId by remember { mutableStateOf<Int?>(null) }

	LaunchedEffect(tooltipState.isVisible) {
		if (tooltipState.isVisible) {
			delay(4.seconds)
			tooltipState.dismiss()
			phaseTooltipResId = null
		}
	}

	Column(
		modifier = modifier,
		horizontalAlignment = Alignment.CenterHorizontally
	) {
		// Stage Title
		Text(
			text = uiState.stageTitle,
			style = typography.stageTitle(),
			color = MaterialTheme.colorScheme.onBackground,
			fontWeight = FontWeight.Bold,
			textAlign = TextAlign.Center,
			modifier = Modifier.padding(bottom = spacing.small)
		)

		TooltipBox(
			positionProvider = TooltipDefaults
				.rememberTooltipPositionProvider(TooltipAnchorPosition.Below),
			tooltip = {
				phaseTooltipResId?.let { stringRes ->
					PlainTooltip { Text(stringResource(stringRes)) }
				}
			},
			state = tooltipState,
		) {
			TimeLine(
				elapsedHours = uiState.elapsedHours,
				modifier = Modifier
					.fillMaxWidth()
					.padding(vertical = spacing.medium),
				onPhaseClick = { phase ->
					phaseTooltipResId = phase.title
					scope.launch {
						tooltipState.show()
					}
				}
			)
		}

		// Energy Mode
		Text(
			text = uiState.energyMode,
			style = typography.energyMode(),
			color = MaterialTheme.colorScheme.onBackground,
			textAlign = TextAlign.Center,
			modifier = Modifier.padding(bottom = spacing.medium)
		)

		Spacer(modifier = Modifier.size(height = spacing.large, width = 1.dp))

		// Timer
		Row(
			verticalAlignment = Alignment.Bottom,
			modifier = Modifier.padding(bottom = spacing.large)
		) {
			Text(
				text = uiState.timerText,
				style = typography.timerText(),
				color = MaterialTheme.colorScheme.onBackground,
				fontWeight = FontWeight.Bold,
			)
			Text(
				text = uiState.milliseconds,
				style = typography.timerMilliseconds(),
				color = MaterialTheme.colorScheme.onBackground,
				modifier = Modifier.padding(start = spacing.small, bottom = spacing.small)
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
	val spacing = fastingSpacing()
	val typography = fastingTypography()

	Column(
		modifier = modifier,
		horizontalAlignment = Alignment.CenterHorizontally,
	) {
		Spacer(modifier = Modifier.weight(1f))

		// Phase Information
		Column(
			modifier = Modifier
				.fillMaxWidth()
				.padding(bottom = spacing.medium)
		) {
			// Fat Burn Phase
			StageInfo(
				onShowInfoDialog = onShowInfoDialog,
				titleRes = R.string.info_dialog_fat_burn_title,
				contentRes = R.string.info_dialog_fat_burn_content,
				labelRes = R.string.fast_fat_burn_label,
				timeText = uiState.fatBurnTime,
				stageState = uiState.fatBurnStageState
			)

			// Ketosis Phase
			StageInfo(
				onShowInfoDialog = onShowInfoDialog,
				titleRes = R.string.info_dialog_ketosis_title,
				contentRes = R.string.info_dialog_ketosis_content,
				labelRes = R.string.fast_ketosis_label,
				timeText = uiState.ketosisTime,
				stageState = uiState.ketosisStageState
			)

			// Autophagy Phase
			StageInfo(
				onShowInfoDialog = onShowInfoDialog,
				titleRes = R.string.info_dialog_autophagy_title,
				contentRes = R.string.info_dialog_autophagy_content,
				labelRes = R.string.fast_autophagy_label,
				timeText = uiState.autophagyTime,
				stageState = uiState.autophagyStageState
			)
		}

		Row(modifier = Modifier
			.fillMaxWidth()
			.weight(2f)) {
			// Stage Description
			Box(
				modifier = Modifier
					.fillMaxWidth()
					.weight(1f)
					.verticalScroll(rememberScrollState())
			) {
				Text(
					text = uiState.stageDescription,
					style = typography.stageDescription(),
					color = MaterialTheme.colorScheme.onBackground,
					modifier = Modifier.padding(top = spacing.medium, end = spacing.medium)
				)
			}

			// Bottom Controls Row
			Row(
				modifier = Modifier
					.align(Alignment.Bottom)
					.wrapContentHeight()
					.padding(top = spacing.medium),
				horizontalArrangement = Arrangement.End,
				verticalAlignment = Alignment.CenterVertically
			) {
				// Debug Button (only in debug builds)
				if (BuildConfig.DEBUG) {
					FloatingActionButton(
						onClick = { viewModel.debugIncreaseFastingTimeByOneHour() },
						modifier = Modifier.padding(end = spacing.medium)
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
						onClick = onShowEndFastConfirmation,
					) {
						Icon(
							painter = painterResource(id = R.drawable.ic_fast_stop),
							contentDescription = stringResource(id = R.string.stop_fast_button_description)
						)
					}
				} else {
					FloatingActionButton(
						onClick = onShowStartFastSelector,
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
}

@Composable
private fun StageInfo(
	onShowInfoDialog: (Int, Int) -> Unit,
	titleRes: Int,
	contentRes: Int,
	labelRes: Int,
	timeText: String,
	stageState: IFastingViewModel.StageState
) {
	val spacing = fastingSpacing()
	val typography = fastingTypography()

	Row(
		modifier = Modifier
			.fillMaxWidth()
			.padding(vertical = spacing.small),
		horizontalArrangement = Arrangement.SpaceBetween,
		verticalAlignment = Alignment.CenterVertically
	) {
		TextButton(
			modifier = Modifier.weight(1f),
			onClick = { onShowInfoDialog(titleRes, contentRes) },
			contentPadding = PaddingValues(
				horizontal = spacing.buttonPaddingHorizontal,
				vertical = spacing.buttonPaddingVertical
			)
		) {
			Icon(
				painter = painterResource(id = R.drawable.ic_more_info),
				tint = phaseTextColor(),
				contentDescription = null,
				modifier = Modifier
					.padding(end = spacing.small)
					.size(spacing.iconSize)
			)
			Text(
				text = stringResource(id = labelRes),
				style = typography.phaseLabel(),
				color = phaseTextColor(),
				maxLines = 1,
				softWrap = false,
				overflow = TextOverflow.Ellipsis,
				modifier = Modifier.weight(1f)
			)
		}
		Text(
			text = timeText,
			style = typography.phaseTime(),
			color = stageColor(stageState),
			maxLines = 1,
			softWrap = false,
			overflow = TextOverflow.Visible,
		)
	}
}

@Composable
private fun stageColor(stageState: IFastingViewModel.StageState): Color = when (stageState) {
	IFastingViewModel.StageState.StartedActive -> Color(0xFF00DD00)
	IFastingViewModel.StageState.StartedInactive -> Color.Red
	IFastingViewModel.StageState.NotStarted -> MaterialTheme.colorScheme.onBackground
}
