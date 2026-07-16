package com.darkrockstudios.apps.fasttrack.screens.fasting

import android.app.Activity
import android.content.res.Configuration
import android.graphics.Rect
import android.os.Handler
import android.os.Looper
import android.view.PixelCopy
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import androidx.core.graphics.createBitmap
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.currentStateAsState
import com.darkrockstudios.apps.fasttrack.BuildConfig
import com.darkrockstudios.apps.fasttrack.R
import com.darkrockstudios.apps.fasttrack.data.FastingJourney
import com.darkrockstudios.apps.fasttrack.data.JourneyStage
import com.darkrockstudios.apps.fasttrack.data.Stages
import com.darkrockstudios.apps.fasttrack.screens.confetti.ConfettiState
import com.darkrockstudios.apps.fasttrack.screens.confetti.confettiEffect
import com.darkrockstudios.apps.fasttrack.ui.theme.fastBackgroundGradient
import com.darkrockstudios.apps.fasttrack.utils.formatDuration
import com.darkrockstudios.apps.fasttrack.utils.shareFastImage
import io.github.aakira.napier.Napier
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import org.koin.compose.viewmodel.koinViewModel
import kotlin.coroutines.resume
import kotlin.math.roundToInt
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit

@Composable
fun FastingScreen(
	contentPaddingValues: PaddingValues,
	viewModel: IFastingViewModel = koinViewModel<FastingViewModel>(),
	externalRequests: ExternalRequests = ExternalRequests(),
) {
	val scope = rememberCoroutineScope()
	val confetti = remember { ConfettiState() }
	val uiState by viewModel.uiState.collectAsState()

	LaunchedEffect(Unit) {
		viewModel.onCreate()
	}

	var showStartDateTimePicker by remember { mutableStateOf(false) }
	var showEndDateTimePicker by remember { mutableStateOf(false) }
	var showStartSheet by remember { mutableStateOf(false) }
	var showEndSheet by remember { mutableStateOf(false) }
	// Note captured while ending a fast; carried across the "stopped earlier" picker
	var endNotes by rememberSaveable { mutableStateOf("") }

	fun onShowStartFastSelector() {
		if (!uiState.isFasting) showStartSheet = true
	}

	if (showStartSheet) {
		StartFastSheet(
			onStartNow = {
				showStartSheet = false
				viewModel.startFast()
			},
			onStartEarlier = {
				showStartSheet = false
				showStartDateTimePicker = true
			},
			onDismiss = { showStartSheet = false },
		)
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
		showEndSheet = true
	}

	if (showEndSheet) {
		EndFastSheet(
			notes = endNotes,
			onNotesChange = { endNotes = it },
			onEndNow = {
				showEndSheet = false
				viewModel.endFast(notes = endNotes.trim())
				endNotes = ""
				confetti.start(scope)
			},
			onEndEarlier = {
				// Keep the note; it is applied once the end time is chosen
				showEndSheet = false
				showEndDateTimePicker = true
			},
			onDismiss = {
				showEndSheet = false
				endNotes = ""
			},
		)
	}

	if (showEndDateTimePicker) {
		val dateTimePickerState = rememberDateTimePickerDialogState()
		DateTimePickerDialog(
			onDismiss = { showEndDateTimePicker = false },
			onDateTimeSelected = { selectedDateTime ->
				viewModel.endFast(selectedDateTime, endNotes.trim())
				endNotes = ""
				showEndDateTimePicker = false
				confetti.start(scope)
			},
			title = stringResource(R.string.already_stopped_dialog_title),
			finishButton = stringResource(id = R.string.end_fast_button),
			state = dateTimePickerState,
			minInstant = uiState.fastStartTime
		)
	}

	// Journey stage overlay, opened from the dial or the phase rows
	var selectedStage by remember { mutableStateOf<JourneyStage?>(null) }

	// One shared time format for the center timer and the phase rows:
	// days+hours ("2d 12h") vs total hours ("60h 30m"), toggled by tapping either
	var showTotalHours by rememberSaveable { mutableStateOf(false) }

	val context = LocalContext.current
	// Window-space bounds of the shareable hero (dial + rows), tracked for capture
	var heroBounds by remember { mutableStateOf<Rect?>(null) }

	// Share request: PixelCopy the hero region straight off the window surface
	// (so it's the screen as-is, minus the status bar and other windows), then
	// fire the chooser with a rich caption.
	LaunchedEffect(externalRequests.shareRequested) {
		if (externalRequests.shareRequested) {
			@Suppress("TooGenericExceptionCaught")
			try {
				val bounds = heroBounds
				val window = (context as? Activity)?.window
				if (bounds != null && window != null && bounds.width() > 0 && bounds.height() > 0) {
					val bitmap = createBitmap(bounds.width(), bounds.height())
					val ok = suspendCancellableCoroutine { cont ->
						PixelCopy.request(
							window, bounds, bitmap,
							{ result -> cont.resume(result == PixelCopy.SUCCESS) },
							Handler(Looper.getMainLooper())
						)
					}
					if (ok) {
						shareFastImage(context, bitmap, buildShareCaption(context, uiState))
					}
				}
			} catch (e: Throwable) {
				Napier.e("Failed to share fast", e)
			}
			externalRequests.consumeShareRequest()
		}
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
		// The timer displays minute granularity, so there is no reason to poll
		// faster than once a minute. Refresh immediately (e.g. on resume), then
		// wake exactly on each whole-minute boundary so the value flips on time.
		while (uiState.isFasting && lifecycleState == Lifecycle.State.RESUMED) {
			viewModel.updateUi()
			delay(60_000L - (System.currentTimeMillis() % 60_000L))
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

		// The dial must never starve the controls below it: cap it against the
		// viewport height, not just the width. In portrait the cap is 1/phi^2
		// (~0.382) of the height — the golden section between the dial zone
		// and everything below it.
		val dialMaxSize = if (isLandscape) {
			maxHeight * 0.8f
		} else {
			min(maxWidth, maxHeight * 0.382f)
		}

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
						dialMaxSize = dialMaxSize,
						showTotalHours = showTotalHours,
						onToggleTimeFormat = { showTotalHours = !showTotalHours },
						onStageSelected = { selectedStage = it },
						modifier = Modifier
							.weight(1f)
							.fillMaxHeight()
							.padding(end = spacing.medium)
							.onGloballyPositioned { heroBounds = it.boundsInWindow().toAndroidRect() }
					)

					Spacer(modifier = Modifier.size(height = spacing.large, width = 1.dp))

					FastDetailsContent(
						uiState = uiState,
						showTotalHours = showTotalHours,
						onToggleTimeFormat = { showTotalHours = !showTotalHours },
						onStageSelected = { selectedStage = it },
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
					// Golden-seat the whole hero cluster: a smaller minor share of
					// slack above, a larger major share below (before the pinned
					// action). Title + dial + rows + description read as one unit,
					// and this whole block is what gets captured for image sharing.
					Spacer(modifier = Modifier.weight(GOLDEN_MINOR))

					FastHero(
						uiState = uiState,
						dialMaxSize = dialMaxSize,
						showTotalHours = showTotalHours,
						onToggleTimeFormat = { showTotalHours = !showTotalHours },
						onStageSelected = { selectedStage = it },
						modifier = Modifier
							.fillMaxWidth()
							.onGloballyPositioned { heroBounds = it.boundsInWindow().toAndroidRect() }
					)

					Spacer(modifier = Modifier.weight(GOLDEN_MAJOR))

					FastActionRow(
						uiState = uiState,
						viewModel = viewModel,
						onShowEndFastConfirmation = ::onShowEndFastConfirmation,
						onShowStartFastSelector = ::onShowStartFastSelector,
					)
				}
			}
		}
	}

	selectedStage?.let { stage ->
		JourneyStageSheet(
			stage = stage,
			onDismiss = { selectedStage = null }
		)
	}
}

@Composable
private fun FastHeadingContent(
	uiState: IFastingViewModel.FastingUiState,
	dialMaxSize: Dp,
	showTotalHours: Boolean,
	onToggleTimeFormat: () -> Unit,
	onStageSelected: (JourneyStage) -> Unit,
	modifier: Modifier = Modifier
) {
	val spacing = fastingSpacing()
	val typography = fastingTypography()

	// Precise elapsed time; uiState.elapsedHours is truncated to whole hours.
	// After a fast has ended the dial rests at zero: muted milestones, no
	// heartbeat — the pulse is a reward that belongs to fasting alone.
	val elapsedHoursPrecise = if (uiState.isFasting) {
		uiState.elapsedTime?.toDouble(DurationUnit.HOURS) ?: uiState.elapsedHours
	} else {
		0.0
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

		TimeLine(
			elapsedHours = elapsedHoursPrecise,
			modifier = Modifier
				.widthIn(max = dialMaxSize)
				.fillMaxWidth()
				.padding(vertical = spacing.medium),
			onStageClick = onStageSelected
		) {
			// Timer + Energy Mode, centered inside the ring — only while a fast
			// is running. Tapping the timer toggles days+hours vs total hours.
			if (uiState.isFasting) {
				Column(
					horizontalAlignment = Alignment.CenterHorizontally,
					modifier = Modifier.clickable(
						interactionSource = remember { MutableInteractionSource() },
						indication = null
					) { onToggleTimeFormat() }
				) {
					val timerText = formatDuration(
						uiState.elapsedTime ?: uiState.elapsedHours.hours,
						showTotalHours
					)
					// Auto-size so the value always fits inside the ring — the
					// Compose-native answer (the View-system autoSizeTextType
					// APIs don't apply to Compose). One line, shrinks to fit.
					BasicText(
						text = timerText,
						maxLines = 1,
						softWrap = false,
						autoSize = TextAutoSize.StepBased(
							minFontSize = 20.sp,
							maxFontSize = 60.sp,
							stepSize = 1.sp,
						),
						style = typography.timerText().merge(
							TextStyle(
								color = MaterialTheme.colorScheme.onBackground,
								fontWeight = FontWeight.Bold,
								textAlign = TextAlign.Center,
							)
						),
						modifier = Modifier.fillMaxWidth(),
					)
					BasicText(
						text = uiState.energyMode,
						maxLines = 2,
						autoSize = TextAutoSize.StepBased(
							minFontSize = 9.sp,
							maxFontSize = 15.sp,
							stepSize = 1.sp,
						),
						style = typography.energyMode().merge(
							TextStyle(
								color = MaterialTheme.colorScheme.onBackground,
								textAlign = TextAlign.Center,
							)
						),
						modifier = Modifier
							.fillMaxWidth()
							.padding(top = spacing.small)
					)
				}
			}
		}
	}
}

// Golden section of the free vertical space: the hero cluster is seated with
// ~38.2% of the slack above it and ~61.8% below, so it rises toward the upper
// golden line and the primary action keeps a larger, deliberate breathing zone.
private const val GOLDEN_MINOR = 0.382f
private const val GOLDEN_MAJOR = 0.618f

/**
 * Portrait hero: stage title, dial (with center timer + energy), phase rows,
 * the status line, and a small brand footer — one cohesive cluster that also
 * doubles as the shareable image (the action button lives outside it).
 */
@Composable
private fun FastHero(
	uiState: IFastingViewModel.FastingUiState,
	dialMaxSize: Dp,
	showTotalHours: Boolean,
	onToggleTimeFormat: () -> Unit,
	onStageSelected: (JourneyStage) -> Unit,
	modifier: Modifier = Modifier,
) {
	val spacing = fastingSpacing()
	val typography = fastingTypography()
	val elapsed = uiState.elapsedTime ?: uiState.elapsedHours.takeIf { it > 0 }?.hours

	Column(
		modifier = modifier,
		horizontalAlignment = Alignment.CenterHorizontally,
	) {
		FastHeadingContent(
			uiState = uiState,
			dialMaxSize = dialMaxSize,
			showTotalHours = showTotalHours,
			onToggleTimeFormat = onToggleTimeFormat,
			onStageSelected = onStageSelected,
			modifier = Modifier.fillMaxWidth()
		)

		PhaseRows(uiState, showTotalHours, onToggleTimeFormat, onStageSelected, elapsed)

		Text(
			text = rememberFastStatusText(uiState, showTotalHours),
			style = typography.stageDescription(),
			color = MaterialTheme.colorScheme.onBackground,
			textAlign = TextAlign.Center,
			modifier = Modifier
				.fillMaxWidth()
				.padding(top = spacing.large, start = spacing.medium, end = spacing.medium)
		)
	}
}

/** Rounded window-space rectangle for PixelCopy capture. */
private fun androidx.compose.ui.geometry.Rect.toAndroidRect(): Rect =
	Rect(left.roundToInt(), top.roundToInt(), right.roundToInt(), bottom.roundToInt())

/**
 * A multi-line caption for a shared fast: the lead sentence plus a line for each
 * milestone the body has actually reached (positive count-up), reusing the
 * already-localized phase labels.
 */
private fun buildShareCaption(
	context: android.content.Context,
	uiState: IFastingViewModel.FastingUiState,
): String {
	val elapsed = uiState.elapsedTime ?: return context.getString(R.string.app_name)
	val durationText = formatDuration(context, elapsed)
	val curPhase = Stages.getCurrentPhase(elapsed)
	val energyStr = if (curPhase.fatBurning) {
		context.getString(R.string.fasting_energy_mode_fat)
	} else {
		context.getString(R.string.fasting_energy_mode_glucose)
	}
	val lead = if (uiState.isFasting) {
		context.getString(R.string.share_text_fasting, durationText, energyStr)
	} else {
		context.getString(R.string.share_text_finished, durationText)
	}

	val active = IFastingViewModel.StageState.StartedActive
	val lines = buildList {
		if (uiState.fatBurnStageState == active) {
			add("🔥 " + context.getString(R.string.fast_fat_burn_label) + " " + uiState.fatBurnTime)
		}
		if (uiState.ketosisStageState == active) {
			add("💎 " + context.getString(R.string.fast_ketosis_label) + " " + uiState.ketosisTime)
		}
		if (uiState.autophagyStageState == active) {
			add("♻️ " + context.getString(R.string.fast_autophagy_label) + " " + uiState.autophagyTime)
		}
	}
	return (listOf(lead) + lines).joinToString("\n")
}

/** Landscape body: rows + a scroll-safe description that fills the column, action pinned. */
@Composable
private fun FastDetailsContent(
	uiState: IFastingViewModel.FastingUiState,
	showTotalHours: Boolean,
	onToggleTimeFormat: () -> Unit,
	onStageSelected: (JourneyStage) -> Unit,
	viewModel: IFastingViewModel,
	onShowEndFastConfirmation: () -> Unit,
	onShowStartFastSelector: () -> Unit,
	modifier: Modifier = Modifier
) {
	val spacing = fastingSpacing()
	val typography = fastingTypography()
	val elapsed = uiState.elapsedTime ?: uiState.elapsedHours.takeIf { it > 0 }?.hours

	Column(
		modifier = modifier,
		horizontalAlignment = Alignment.CenterHorizontally,
	) {
		PhaseRows(uiState, showTotalHours, onToggleTimeFormat, onStageSelected, elapsed)

		Box(
			modifier = Modifier
				.fillMaxWidth()
				.weight(1f)
				.verticalScroll(rememberScrollState()),
			contentAlignment = Alignment.Center
		) {
			Text(
				text = rememberFastStatusText(uiState, showTotalHours),
				style = typography.stageDescription(),
				color = MaterialTheme.colorScheme.onBackground,
				textAlign = TextAlign.Center,
				modifier = Modifier
					.fillMaxWidth()
					.padding(vertical = spacing.medium)
			)
		}

		FastActionRow(
			uiState = uiState,
			viewModel = viewModel,
			onShowEndFastConfirmation = onShowEndFastConfirmation,
			onShowStartFastSelector = onShowStartFastSelector,
		)
	}
}

/**
 * Fat Burn / Ketosis / Autophagy rows. Only while fasting; tap the label to open
 * its journey stage, tap the time to switch formats everywhere. In auto mode a
 * row appears only once its phase has begun (positive count-up); otherwise it
 * follows the per-phase Settings toggle.
 */
@Composable
private fun PhaseRows(
	uiState: IFastingViewModel.FastingUiState,
	showTotalHours: Boolean,
	onToggleTimeFormat: () -> Unit,
	onStageSelected: (JourneyStage) -> Unit,
	elapsed: Duration?,
) {
	if (!uiState.isFasting) return

	fun visible(show: Boolean, startHours: Int): Boolean =
		if (uiState.phaseAutoMode) {
			elapsed != null && elapsed >= startHours.hours
		} else {
			show
		}

	Column(modifier = Modifier.fillMaxWidth()) {
		if (visible(uiState.showFatBurn, Stages.PHASE_FAT_BURN.hours)) {
			StageInfo(
				labelRes = R.string.fast_fat_burn_label,
				phaseStartHours = Stages.PHASE_FAT_BURN.hours,
				elapsed = elapsed,
				showTotalHours = showTotalHours,
				onToggleFormat = onToggleTimeFormat,
				onClick = { onStageSelected(FastingJourney.stages[4]) }
			)
		}
		if (visible(uiState.showKetosis, Stages.PHASE_KETOSIS.hours)) {
			StageInfo(
				labelRes = R.string.fast_ketosis_label,
				phaseStartHours = Stages.PHASE_KETOSIS.hours,
				elapsed = elapsed,
				showTotalHours = showTotalHours,
				onToggleFormat = onToggleTimeFormat,
				onClick = { onStageSelected(FastingJourney.stages[5]) }
			)
		}
		if (visible(uiState.showAutophagy, Stages.PHASE_AUTOPHAGY.hours)) {
			StageInfo(
				labelRes = R.string.fast_autophagy_label,
				phaseStartHours = Stages.PHASE_AUTOPHAGY.hours,
				elapsed = elapsed,
				showTotalHours = showTotalHours,
				onToggleFormat = onToggleTimeFormat,
				onClick = { onStageSelected(FastingJourney.stages[6]) }
			)
		}
	}
}

/**
 * While fasting: the stage description. In the first hour after a fast: a moment
 * of recognition. Beyond that: how long since the last one, pulled from storage
 * and refreshed once a minute.
 */
@Composable
private fun rememberFastStatusText(
	uiState: IFastingViewModel.FastingUiState,
	showTotalHours: Boolean,
): String {
	if (uiState.isFasting) return uiState.stageDescription

	var now by remember { mutableStateOf(Clock.System.now()) }
	LaunchedEffect(Unit) {
		while (true) {
			now = Clock.System.now()
			delay(60.seconds)
		}
	}

	return uiState.lastFastEndTime?.let { lastEnd ->
		val since = (now - lastEnd).coerceAtLeast(Duration.ZERO)
		if (since < 1.hours) {
			stringResource(R.string.just_finished_fast)
		} else {
			stringResource(
				R.string.time_since_last_fast,
				formatDuration(since, showTotalHours)
			)
		}
	} ?: ""
}

/** Debug +1h (debug builds) plus the full-width Start/End action, pinned. */
@Composable
private fun FastActionRow(
	uiState: IFastingViewModel.FastingUiState,
	viewModel: IFastingViewModel,
	onShowEndFastConfirmation: () -> Unit,
	onShowStartFastSelector: () -> Unit,
) {
	val spacing = fastingSpacing()
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.padding(top = spacing.medium),
		verticalAlignment = Alignment.CenterVertically
	) {
		if (BuildConfig.DEBUG) {
			FilledTonalIconButton(
				onClick = { viewModel.debugIncreaseFastingTimeByOneHour() },
				modifier = Modifier.padding(end = spacing.medium)
			) {
				Icon(
					imageVector = Icons.Default.Add,
					contentDescription = stringResource(id = R.string.debug_add_hour_button)
				)
			}
		}

		val (onClick, iconRes, labelRes) = if (uiState.isFasting) {
			Triple(onShowEndFastConfirmation, R.drawable.ic_fast_stop, R.string.end_fast_button)
		} else {
			Triple(onShowStartFastSelector, R.drawable.ic_start_fast, R.string.start_fast_button)
		}
		Button(
			onClick = onClick,
			modifier = Modifier
				.weight(1f)
				.heightIn(min = 55.dp)
		) {
			Icon(
				painter = painterResource(id = iconRes),
				contentDescription = null,
				modifier = Modifier.padding(end = spacing.medium)
			)
			Text(
				text = stringResource(id = labelRes),
				style = MaterialTheme.typography.titleMedium,
			)
		}
	}
}

@Composable
private fun StageInfo(
	labelRes: Int,
	phaseStartHours: Int,
	elapsed: Duration?,
	showTotalHours: Boolean,
	onToggleFormat: () -> Unit,
	onClick: () -> Unit,
) {
	val spacing = fastingSpacing()
	val typography = fastingTypography()

	val delta = elapsed?.minus(phaseStartHours.hours)
	val timeText: String
	val timeColor: Color
	when {
		delta == null -> {
			timeText = "—"
			timeColor = MaterialTheme.colorScheme.onBackground
		}

		delta >= Duration.ZERO -> {
			// Underway: alive, affirming green — the dial's own "burn begins" hue,
			// so it tracks the theme (light/dark) instead of a fixed color.
			timeText = formatDuration(delta, showTotalHours)
			timeColor = journeyStageColor(4)
		}

		else -> {
			// Ahead: calm anticipation, never red — an upcoming phase is not a failure
			timeText = stringResource(R.string.phase_time_until, formatDuration(-delta, showTotalHours))
			timeColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f)
		}
	}

	Row(
		modifier = Modifier
			.fillMaxWidth()
			.clip(RoundedCornerShape(12.dp))
			.clickable(onClick = onClick)
			.padding(horizontal = spacing.medium, vertical = spacing.small),
		horizontalArrangement = Arrangement.SpaceBetween,
		verticalAlignment = Alignment.CenterVertically
	) {
		Text(
			text = stringResource(id = labelRes),
			style = typography.phaseLabel(),
			color = phaseTextColor(),
			maxLines = 1,
			softWrap = false,
			overflow = TextOverflow.Ellipsis,
			modifier = Modifier.weight(1f)
		)
		Text(
			text = timeText,
			style = typography.phaseTime(),
			color = timeColor,
			maxLines = 1,
			softWrap = false,
			overflow = TextOverflow.Visible,
			modifier = Modifier.clickable(
				interactionSource = remember { MutableInteractionSource() },
				indication = null
			) { onToggleFormat() }
		)
	}
}

/**
 * Confirmation for starting a fast — a calm bottom sheet replacing the old
 * system alert. "Start now" is the primary action; "I started earlier" opens
 * the date/time picker. Dismiss by swipe or scrim.
 */
@Composable
private fun StartFastSheet(
	onStartNow: () -> Unit,
	onStartEarlier: () -> Unit,
	onDismiss: () -> Unit,
) {
	ModalBottomSheet(
		onDismissRequest = onDismiss,
		sheetState = rememberModalBottomSheetState(),
	) {
		Column(
			modifier = Modifier
				.fillMaxWidth()
				.padding(start = 21.dp, end = 21.dp, bottom = 34.dp),
			horizontalAlignment = Alignment.CenterHorizontally,
		) {
			Text(
				text = stringResource(R.string.confirm_start_fast_title),
				style = MaterialTheme.typography.headlineSmall,
				fontWeight = FontWeight.Bold,
			)
			Spacer(modifier = Modifier.size(21.dp))
			Button(
				onClick = onStartNow,
				modifier = Modifier
					.fillMaxWidth()
					.heightIn(min = 55.dp),
			) {
				Icon(
					painter = painterResource(id = R.drawable.ic_start_fast),
					contentDescription = null,
					modifier = Modifier.padding(end = 8.dp),
				)
				Text(
					text = stringResource(R.string.sheet_start_now),
					style = MaterialTheme.typography.titleMedium,
				)
			}
			Spacer(modifier = Modifier.size(8.dp))
			TextButton(onClick = onStartEarlier, modifier = Modifier.fillMaxWidth()) {
				Text(stringResource(R.string.sheet_start_earlier))
			}
		}
	}
}

/**
 * Confirmation for ending a fast. Carries an optional Notes field saved to the
 * logbook. "End now" ends at the current time; "I stopped earlier" opens the
 * picker (the typed note is preserved and applied afterward).
 */
@Composable
private fun EndFastSheet(
	notes: String,
	onNotesChange: (String) -> Unit,
	onEndNow: () -> Unit,
	onEndEarlier: () -> Unit,
	onDismiss: () -> Unit,
) {
	ModalBottomSheet(
		onDismissRequest = onDismiss,
		sheetState = rememberModalBottomSheetState(),
	) {
		Column(
			modifier = Modifier
				.fillMaxWidth()
				.imePadding()
				.padding(start = 21.dp, end = 21.dp, bottom = 34.dp),
			horizontalAlignment = Alignment.CenterHorizontally,
		) {
			Text(
				text = stringResource(R.string.confirm_end_fast_title),
				style = MaterialTheme.typography.headlineSmall,
				fontWeight = FontWeight.Bold,
			)
			Spacer(modifier = Modifier.size(13.dp))
			OutlinedTextField(
				value = notes,
				onValueChange = onNotesChange,
				modifier = Modifier.fillMaxWidth(),
				label = { Text(stringResource(R.string.fast_notes_label)) },
				placeholder = { Text(stringResource(R.string.fast_notes_placeholder)) },
				minLines = 2,
				maxLines = 5,
			)
			Spacer(modifier = Modifier.size(21.dp))
			Button(
				onClick = onEndNow,
				modifier = Modifier
					.fillMaxWidth()
					.heightIn(min = 55.dp),
			) {
				Icon(
					painter = painterResource(id = R.drawable.ic_fast_stop),
					contentDescription = null,
					modifier = Modifier.padding(end = 8.dp),
				)
				Text(
					text = stringResource(R.string.sheet_end_now),
					style = MaterialTheme.typography.titleMedium,
				)
			}
			Spacer(modifier = Modifier.size(8.dp))
			TextButton(onClick = onEndEarlier, modifier = Modifier.fillMaxWidth()) {
				Text(stringResource(R.string.sheet_end_earlier))
			}
		}
	}
}

/**
 * Overlay for one stage of the fasting journey, opened from the dial.
 * A bottom sheet: it slides in gently, never covers the dial fully,
 * and dismisses with a swipe or a tap outside.
 */
@Composable
private fun JourneyStageSheet(
	stage: JourneyStage,
	onDismiss: () -> Unit,
) {
	val stageIndex = FastingJourney.stages.indexOf(stage)
	val accent = journeyStageColor(stageIndex)

	ModalBottomSheet(
		onDismissRequest = onDismiss,
		sheetState = rememberModalBottomSheetState(),
	) {
		Column(
			modifier = Modifier
				.fillMaxWidth()
				.verticalScroll(rememberScrollState())
				.padding(start = 21.dp, end = 21.dp, bottom = 34.dp)
		) {
			Row(verticalAlignment = Alignment.CenterVertically) {
				Box(
					modifier = Modifier
						.size(55.dp)
						.clip(CircleShape)
						.background(accent.copy(alpha = 0.18f)),
					contentAlignment = Alignment.Center
				) {
					Text(text = stage.emoji, fontSize = 26.sp)
				}
				Spacer(modifier = Modifier.size(13.dp))
				Column {
					Text(
						text = stringResource(stage.title),
						style = MaterialTheme.typography.titleLarge,
						fontWeight = FontWeight.Bold,
					)
					val rangeText = stage.endHours?.let { end ->
						stringResource(R.string.journey_stage_hours_range, stage.startHours, end)
					} ?: stringResource(R.string.journey_stage_hours_open, stage.startHours)
					Text(
						text = rangeText,
						style = MaterialTheme.typography.labelLarge,
						color = accent,
					)
				}
			}

			Spacer(modifier = Modifier.size(21.dp))

			Text(
				text = stringResource(stage.body),
				style = MaterialTheme.typography.bodyLarge,
			)
		}
	}
}

/**
 * The app-wide duration format (see [com.darkrockstudios.apps.fasttrack.utils.formatDuration]),
 * bound to the composition's context.
 */
@Composable
private fun formatDuration(duration: Duration, showTotalHours: Boolean): String =
    formatDuration(
        LocalContext.current,
        duration,
        showTotalHours
    )
