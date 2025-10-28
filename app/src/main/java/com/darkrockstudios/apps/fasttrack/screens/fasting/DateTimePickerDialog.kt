package com.darkrockstudios.apps.fasttrack.screens.fasting

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.darkrockstudios.apps.fasttrack.R
import com.darkrockstudios.apps.fasttrack.screens.preview.getContext
import com.darkrockstudios.apps.fasttrack.utils.DateRangeSelectableDates
import com.darkrockstudios.apps.fasttrack.utils.shouldUse24HourFormat
import kotlinx.datetime.*
import kotlinx.datetime.TimeZone
import java.util.*
import kotlin.time.ExperimentalTime
import kotlin.time.Instant


/**
 * State for the DateTimePickerDialog
 */
class DateTimePickerDialogState {
	var currentStep by mutableIntStateOf(0)
}

/**
 * Create and remember a DateTimePickerDialogState
 */
@Composable
fun rememberDateTimePickerDialogState(): DateTimePickerDialogState {
	return remember { DateTimePickerDialogState() }
}

@ExperimentalTime
@Composable
fun DateTimePickerDialog(
	onDismiss: () -> Unit,
	onDateTimeSelected: (Instant) -> Unit,
	title: String,
	finishButton: String,
	state: DateTimePickerDialogState = rememberDateTimePickerDialogState(),
	initialInstant: Instant? = null,
	minInstant: Instant? = null
) {
	val initialDateTime = remember(initialInstant) {
		initialInstant?.let { instant ->
			val kotlinxInstant = Instant.fromEpochMilliseconds(instant.toEpochMilliseconds())
			kotlinxInstant.toLocalDateTime(TimeZone.currentSystemDefault())
		}
	}

	val minDateTime = remember(minInstant) {
		minInstant?.let { instant ->
			val kotlinxInstant = Instant.fromEpochMilliseconds(instant.toEpochMilliseconds())
			kotlinxInstant.toLocalDateTime(TimeZone.currentSystemDefault())
		}
	}

	val minDateMillis = remember(minDateTime) {
		minDateTime?.let { dateTime ->
			LocalDate(
				year = dateTime.year,
				month = dateTime.month,
				day = dateTime.day
			).atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
		}
	}

	val initialDateMillis = remember(initialDateTime) {
		initialDateTime?.let { dateTime ->
			LocalDate(
				year = dateTime.year,
				month = dateTime.month,
				day = dateTime.day
			).atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
		}
	}

	val datePickerState = rememberDatePickerState(
		initialSelectedDateMillis = initialDateMillis,
		selectableDates = DateRangeSelectableDates(minDateMillis)
	)

	// Use initial time if provided, otherwise use current time
	val initialHour = initialDateTime?.hour ?: Calendar.getInstance()[Calendar.HOUR_OF_DAY]
	val initialMinute = initialDateTime?.minute ?: Calendar.getInstance()[Calendar.MINUTE]

	val timePickerState = rememberTimePickerState(
		initialHour = initialHour,
		initialMinute = initialMinute,
		is24Hour = shouldUse24HourFormat(getContext()),
	)

	val isNextButtonEnabled = remember(
		datePickerState.selectedDateMillis,
		timePickerState.hour,
		timePickerState.minute,
		state.currentStep,
		minDateTime
	) {
		when (state.currentStep) {
			0 -> datePickerState.selectedDateMillis != null
			1 -> {
				// Check if selected datetime is valid (not before minDateTime)
				val selectedDate = datePickerState.getSelectedDate()
				if (selectedDate != null && minDateTime != null) {
					// Check if we're on the same day as minDateTime
					val isSameDay = selectedDate.year == minDateTime.year &&
							selectedDate.monthValue == minDateTime.monthNumber &&
							selectedDate.dayOfMonth == minDateTime.dayOfMonth

					if (isSameDay) {
						// Validate time is not before minDateTime's time
						val selectedMinutes = timePickerState.hour * 60 + timePickerState.minute
						val minMinutes = minDateTime.hour * 60 + minDateTime.minute
						selectedMinutes >= minMinutes
					} else {
						// Different day, already validated by date picker
						true
					}
				} else {
					true
				}
			}
			else -> false
		}
	}

	Dialog(
		onDismissRequest = onDismiss,
		properties = DialogProperties(usePlatformDefaultWidth = false),
	) {
		Card(
			modifier = Modifier
				.widthIn(max = 600.dp)
				.heightIn(max = 800.dp)
				.verticalScroll(rememberScrollState())
		) {
			Column(
				verticalArrangement = Arrangement.Center,
				horizontalAlignment = Alignment.CenterHorizontally
			) {
				// Header with title and close button
				Row(
					modifier = Modifier
						.padding(start = 16.dp)
						.fillMaxWidth(),
					horizontalArrangement = Arrangement.SpaceBetween,
					verticalAlignment = Alignment.CenterVertically
				) {
					Text(
						text = title,
						style = MaterialTheme.typography.headlineSmall
					)
					IconButton(onClick = onDismiss) {
						Icon(
							imageVector = Icons.Default.Close,
							contentDescription = stringResource(id = R.string.close_button_content_description)
						)
					}
				}

				Spacer(modifier = Modifier.height(8.dp))

				when (state.currentStep) {
					0 -> {
						DatePicker(
							state = datePickerState
						)
					}

					1 -> {
						TimePicker(
							state = timePickerState,
						)
					}
				}

				Row(
					modifier = Modifier
						.padding(8.dp)
						.fillMaxWidth(),
					horizontalArrangement = Arrangement.End
				) {
					TextButton(onClick = onDismiss) {
						Text(stringResource(id = R.string.cancel_button))
					}

					Spacer(modifier = Modifier.width(8.dp))

					Button(
						onClick = {
							if (state.currentStep < 1) {
								state.currentStep++
							} else {
								val selectedDate = datePickerState.getSelectedDate()
								selectedDate?.let { date ->
									val dateTime = LocalDateTime(
										year = date.year,
										month = date.month,
										dayOfMonth = date.dayOfMonth,
										hour = timePickerState.hour,
										minute = timePickerState.minute,
										second = 0,
										nanosecond = 0
									)
									val instant = dateTime.toInstant(TimeZone.currentSystemDefault())
									onDateTimeSelected(instant)
									onDismiss()
								}
							}
						},
						enabled = isNextButtonEnabled
					) {
						Text(
							text = if (state.currentStep < 1) {
								stringResource(id = R.string.next_button)
							} else {
								finishButton
							}
						)
					}
				}
			}
		}
	}
}
