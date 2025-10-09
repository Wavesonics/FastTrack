package com.darkrockstudios.apps.fasttrack.screens.fasting

import androidx.compose.foundation.layout.*
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
import com.darkrockstudios.apps.fasttrack.utils.PastAndTodaySelectableDates
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
	initialInstant: Instant? = null
) {
	val initialDateTime = remember(initialInstant) {
		initialInstant?.let { instant ->
			val kotlinxInstant = Instant.fromEpochMilliseconds(instant.toEpochMilliseconds())
			kotlinxInstant.toLocalDateTime(TimeZone.currentSystemDefault())
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
		selectableDates = PastAndTodaySelectableDates()
	)

	// Use initial time if provided, otherwise use current time
	val initialHour = initialDateTime?.hour ?: Calendar.getInstance()[Calendar.HOUR_OF_DAY]
	val initialMinute = initialDateTime?.minute ?: Calendar.getInstance()[Calendar.MINUTE]

	val timePickerState = rememberTimePickerState(
		initialHour = initialHour,
		initialMinute = initialMinute,
		is24Hour = shouldUse24HourFormat(getContext()),
	)

	val isNextButtonEnabled = remember(datePickerState.getSelectedDate(), timePickerState) {
		when (state.currentStep) {
			0 -> datePickerState.selectedDateMillis != null
			1 -> true
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
		) {
			Column(
				verticalArrangement = Arrangement.Center,
				horizontalAlignment = Alignment.CenterHorizontally
			) {
				// Header with title and close button
				Row(
					modifier = Modifier
						.padding(16.dp)
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

				Spacer(modifier = Modifier.height(16.dp))

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

				Spacer(modifier = Modifier.height(16.dp))

				Row(
					modifier = Modifier
						.padding(16.dp)
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
