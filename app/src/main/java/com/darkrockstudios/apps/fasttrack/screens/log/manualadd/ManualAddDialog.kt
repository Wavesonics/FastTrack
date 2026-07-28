package com.darkrockstudios.apps.fasttrack.screens.log.manualadd

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.darkrockstudios.apps.fasttrack.R
import com.darkrockstudios.apps.fasttrack.data.log.FastingLogEntry
import com.darkrockstudios.apps.fasttrack.screens.fasting.DateTimePickerDialog
import com.darkrockstudios.apps.fasttrack.screens.fasting.rememberDateTimePickerDialogState
import com.darkrockstudios.apps.fasttrack.screens.preview.getContext
import com.darkrockstudios.apps.fasttrack.utils.AppDateTime
import com.darkrockstudios.apps.fasttrack.utils.LocalDateStyle
import com.darkrockstudios.apps.fasttrack.utils.PastAndTodaySelectableDates
import com.darkrockstudios.apps.fasttrack.utils.shouldUse24HourFormat
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toInstant
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.ExperimentalTime

// ---- Golden-ratio / Fibonacci proportion system ---------------------------
// A single Fibonacci ladder (5, 8, 13, 21, 34) governs every gap, inset, and
// corner radius, so spacings relate to their neighbours by ~phi at every scale
// — a self-similar (fractal) rhythm. Touch targets sit on the 48/55 rungs so the
// visual harmony never costs ergonomics.
private val GapXs = 5.dp
private val GapS = 8.dp
private val GapM = 13.dp
private val GapL = 21.dp
private val GapXl = 34.dp
private const val PHI = 1.618f
private val FieldHeight = 55.dp            // Fibonacci rung, comfortably above the 48dp a11y floor
private val RadiusField = 8.dp             // nested radii descend the same ladder: field 8 < group 13
private val RadiusGroup = 13.dp
private val SheetContentMaxWidth = 610.dp  // 377 * phi — caps line length on tablets

@OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class)
@Composable
fun ManualAddDialog(
	onDismiss: () -> Unit,
	entryToEdit: FastingLogEntry? = null,
	initialDate: LocalDate? = null,
	viewModel: IManualAddViewModel = koinViewModel<ManualAddViewModel>()
) {
	// Seed the flow: an existing entry to edit, or a preselected start date
	// (e.g. an empty calendar day the user chose to log).
	LaunchedEffect(entryToEdit, initialDate) {
		when {
			entryToEdit != null -> viewModel.initializeWithEntry(entryToEdit)
			initialDate != null -> viewModel.initializeWithDate(initialDate)
		}
	}

	val uiState by viewModel.uiState.collectAsState()
	val use24Hour = shouldUse24HourFormat(getContext())
	var showEndDateTimePicker by remember { mutableStateOf(false) }
	// Open fully: the date/time pickers are tall, so a half-height sheet would clip.
	val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

	fun dismiss() {
		viewModel.onDismiss()
		onDismiss()
	}

	ModalBottomSheet(
		onDismissRequest = { dismiss() },
		sheetState = sheetState,
	) {
		Column(
			modifier = Modifier
				.align(Alignment.CenterHorizontally)
				.widthIn(max = SheetContentMaxWidth)
				.fillMaxWidth()
				.imePadding()
				.verticalScroll(rememberScrollState())
				.padding(horizontal = GapL)
				.padding(bottom = GapXl),
		) {
			// Header: title + a golden-emphasis step meter (active segment phi x wider).
			Text(
				text = stringResource(
					id = if (entryToEdit != null) R.string.manual_edit_title else R.string.manual_add_title
				),
				style = MaterialTheme.typography.headlineSmall,
				fontWeight = FontWeight.Bold,
			)
			Spacer(modifier = Modifier.height(GapM))
			StepProgress(
				stepIndex = uiState.currentStep.ordinal,
				stepCount = ManualAddStep.entries.size,
				modifier = Modifier.fillMaxWidth(),
			)
			Spacer(modifier = Modifier.height(GapL))

			// Initialize picker states with existing values when editing.
			// Material's DatePicker expects UTC-midnight millis, so anchor the
			// preselected day in UTC (system zone would preselect the wrong day).
			val initialDateMillis =
				uiState.selectedDate?.atStartOfDayIn(TimeZone.UTC)?.toEpochMilliseconds()

			// Key on the initial date so a preselected day (arriving after first
			// composition) actually shows as selected in the picker.
			val datePickerState = key(initialDateMillis) {
				rememberDatePickerState(
					initialSelectedDateMillis = initialDateMillis,
					selectableDates = PastAndTodaySelectableDates()
				)
			}

			val timePickerState = rememberTimePickerState(
				initialHour = uiState.selectedDateTime?.hour ?: 0,
				initialMinute = uiState.selectedDateTime?.minute ?: 0,
				is24Hour = use24Hour,
			)

			when (uiState.currentStep) {
				ManualAddStep.StartDate -> {
					DatePicker(state = datePickerState, modifier = Modifier.fillMaxWidth())
				}

				ManualAddStep.StartTime -> {
					Row(
						modifier = Modifier.fillMaxWidth(),
						horizontalArrangement = Arrangement.Center,
					) {
						TimePicker(state = timePickerState)
					}
				}

				ManualAddStep.SetDuration -> {
					DurationStep(
						uiState = uiState,
						use24Hour = use24Hour,
						onEditDate = { viewModel.goToStep(ManualAddStep.StartDate) },
						onEditTime = { viewModel.goToStep(ManualAddStep.StartTime) },
						onHoursChanged = viewModel::onLengthChanged,
						onMinutesChanged = viewModel::onMinutesChanged,
						onNotesChanged = viewModel::onNotesChanged,
						onCalculateFromEnd = { showEndDateTimePicker = true },
					)
				}
			}

			Spacer(modifier = Modifier.height(GapL))

			ActionBar(
				currentStep = uiState.currentStep,
				isEditing = entryToEdit != null,
				nextEnabled = if (uiState.currentStep.isFinalStep.not()) {
					uiState.isNextButtonEnabled
				} else {
					uiState.isCompleteButtonEnabled
				},
				onPrevious = { viewModel.onPreviousStep() },
				onCancel = { dismiss() },
				onNextOrSave = {
					when (uiState.currentStep) {
						ManualAddStep.StartDate ->
							datePickerState.selectedDateMillis?.let { viewModel.onDateSelected(it) }

						ManualAddStep.StartTime ->
							viewModel.onTimeSelected(timePickerState.hour, timePickerState.minute)

						ManualAddStep.SetDuration ->
							if (viewModel.onAddEntry()) dismiss()
					}
				},
			)
		}
	}

	if (showEndDateTimePicker) {
		val dateTimePickerState = rememberDateTimePickerDialogState()
		val minStartInstant = uiState.selectedDateTime?.toInstant(TimeZone.currentSystemDefault())
		DateTimePickerDialog(
			onDismiss = { showEndDateTimePicker = false },
			onDateTimeSelected = { instant ->
				viewModel.onEndDateTimeSelected(instant)
				showEndDateTimePicker = false
			},
			title = stringResource(R.string.manual_add_set_end_title),
			finishButton = stringResource(id = R.string.manual_add_set_end_complete),
			state = dateTimePickerState,
			initialInstant = uiState.end(),
			minInstant = minStartInstant
		)
	}
}

/**
 * A slim, three-segment progress meter. The active step's segment is phi times
 * wider than the rest — a golden accent that reads as "you are here" at a glance,
 * shrinking the uncertainty that drives wizard drop-off.
 */
@Composable
private fun StepProgress(stepIndex: Int, stepCount: Int, modifier: Modifier = Modifier) {
	Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(GapXs)) {
		val primary = MaterialTheme.colorScheme.primary
		val track = MaterialTheme.colorScheme.surfaceVariant
		repeat(stepCount) { i ->
			Box(
				modifier = Modifier
					.weight(if (i == stepIndex) PHI else 1f)
					.height(GapXs)
					.clip(RoundedCornerShape(GapXs))
					.background(
						when {
							i == stepIndex -> primary
							i < stepIndex -> primary.copy(alpha = 0.45f)
							else -> track
						}
					)
			)
		}
	}
}

@OptIn(ExperimentalTime::class)
@Composable
private fun DurationStep(
	uiState: IManualAddViewModel.ManualAddUiState,
	use24Hour: Boolean,
	onEditDate: () -> Unit,
	onEditTime: () -> Unit,
	onHoursChanged: (String) -> Unit,
	onMinutesChanged: (String) -> Unit,
	onNotesChanged: (String) -> Unit,
	onCalculateFromEnd: () -> Unit,
) {
	val dateStyle = LocalDateStyle.current
	Column(modifier = Modifier.fillMaxWidth()) {
		// Start summary — tap either row to jump back and edit that step.
		uiState.selectedDateTime?.let { dateTime ->
			Text(
				text = stringResource(id = R.string.manual_add_start_date_time_label),
				style = MaterialTheme.typography.labelMedium,
				color = MaterialTheme.colorScheme.onSurfaceVariant
			)
			Spacer(modifier = Modifier.height(GapXs))
			EditableSummaryRow(
				text = AppDateTime.formatDate(dateTime, dateStyle),
				editContentDescription = R.string.edit_date,
				onClick = onEditDate,
			)
			HorizontalDivider()
			EditableSummaryRow(
				text = AppDateTime.formatTime(dateTime, dateStyle, use24Hour),
				editContentDescription = R.string.edit_time,
				onClick = onEditTime,
			)
			Spacer(modifier = Modifier.height(GapL))
		}

		// Duration group: hours and minutes get equal weight — two peers of the
		// same kind, so an even split reads faster than a golden one would.
		Text(
			text = stringResource(id = R.string.manual_add_duration_label),
			style = MaterialTheme.typography.labelMedium,
			color = MaterialTheme.colorScheme.onSurfaceVariant
		)
		Spacer(modifier = Modifier.height(GapXs))
		Row(
			modifier = Modifier.fillMaxWidth(),
			horizontalArrangement = Arrangement.spacedBy(GapM),
		) {
			OutlinedTextField(
				value = uiState.lengthHours,
				onValueChange = onHoursChanged,
				label = { Text(stringResource(id = R.string.manual_add_hours_hint)) },
				keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
				singleLine = true,
				shape = RoundedCornerShape(RadiusField),
				modifier = Modifier
					.weight(1f)
					.heightIn(min = FieldHeight),
			)
			OutlinedTextField(
				value = uiState.lengthMinutes,
				onValueChange = onMinutesChanged,
				label = { Text(stringResource(id = R.string.manual_add_minutes_hint)) },
				keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
				singleLine = true,
				shape = RoundedCornerShape(RadiusField),
				modifier = Modifier
					.weight(1f)
					.heightIn(min = FieldHeight),
			)
		}

		Spacer(modifier = Modifier.height(GapS))
		TextButton(onClick = onCalculateFromEnd, modifier = Modifier.fillMaxWidth()) {
			Text(stringResource(id = R.string.manual_add_calculate_from_end))
		}

		Spacer(modifier = Modifier.height(GapL))
		OutlinedTextField(
			value = uiState.notes,
			onValueChange = onNotesChanged,
			label = { Text(stringResource(id = R.string.fast_notes_label)) },
			placeholder = { Text(stringResource(id = R.string.fast_notes_placeholder)) },
			keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
			minLines = 2,
			maxLines = 5,
			shape = RoundedCornerShape(RadiusField),
			modifier = Modifier.fillMaxWidth(),
		)
	}
}

@Composable
private fun EditableSummaryRow(
	text: String,
	editContentDescription: Int,
	onClick: () -> Unit,
) {
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.clip(RoundedCornerShape(RadiusGroup))
			.clickable(onClick = onClick)
			.heightIn(min = 48.dp)
			.padding(vertical = GapS),
		horizontalArrangement = Arrangement.SpaceBetween,
		verticalAlignment = Alignment.CenterVertically
	) {
		Text(text = text, style = MaterialTheme.typography.bodyLarge)
		Icon(
			imageVector = Icons.Default.Edit,
			contentDescription = stringResource(id = editContentDescription),
			modifier = Modifier.size(GapL),
			tint = MaterialTheme.colorScheme.primary
		)
	}
}

@Composable
private fun ActionBar(
	currentStep: ManualAddStep,
	isEditing: Boolean,
	nextEnabled: Boolean,
	onPrevious: () -> Unit,
	onCancel: () -> Unit,
	onNextOrSave: () -> Unit,
) {
	Row(
		modifier = Modifier.fillMaxWidth(),
		horizontalArrangement = Arrangement.SpaceBetween,
		verticalAlignment = Alignment.CenterVertically
	) {
		if (currentStep != ManualAddStep.StartDate) {
			TextButton(onClick = onPrevious) {
				Text(stringResource(id = R.string.previous_button))
			}
		} else {
			Spacer(modifier = Modifier.width(1.dp))
		}

		Row(verticalAlignment = Alignment.CenterVertically) {
			TextButton(onClick = onCancel) {
				Text(stringResource(id = R.string.cancel_button))
			}
			Spacer(modifier = Modifier.width(GapS))
			Button(
				onClick = onNextOrSave,
				enabled = nextEnabled,
				shape = RoundedCornerShape(RadiusGroup),
				modifier = Modifier.heightIn(min = FieldHeight),
			) {
				Text(
					text = if (currentStep.isFinalStep.not()) {
						stringResource(id = R.string.next_button)
					} else if (isEditing) {
						stringResource(id = R.string.manual_add_save_button)
					} else {
						stringResource(id = R.string.manual_add_complete_button)
					}
				)
			}
		}
	}
}
