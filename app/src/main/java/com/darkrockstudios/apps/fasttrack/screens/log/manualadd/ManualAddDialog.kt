package com.darkrockstudios.apps.fasttrack.screens.log.manualadd

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.darkrockstudios.apps.fasttrack.R
import com.darkrockstudios.apps.fasttrack.screens.fasting.DateTimePickerDialog
import com.darkrockstudios.apps.fasttrack.screens.fasting.rememberDateTimePickerDialogState
import com.darkrockstudios.apps.fasttrack.utils.PastAndTodaySelectableDates
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@Composable
fun ManualAddDialog(
	onDismiss: () -> Unit,
	viewModel: IManualAddViewModel = koinViewModel<ManualAddViewModel>()
) {
	val uiState by viewModel.uiState.collectAsState()
	var showEndDateTimePicker by remember { mutableStateOf(false) }

	Dialog(
		onDismissRequest = {
			viewModel.onDismiss()
			onDismiss()
		},
		properties = DialogProperties(usePlatformDefaultWidth = false),
	) {
		Card {
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
						text = stringResource(id = R.string.manual_add_title),
						style = MaterialTheme.typography.headlineSmall
					)
					IconButton(onClick = {
						viewModel.onDismiss()
						onDismiss()
					}) {
						Icon(
							imageVector = Icons.Default.Close,
							contentDescription = stringResource(id = R.string.close_button_content_description)
						)
					}
				}

				Spacer(modifier = Modifier.height(16.dp))

				val datePickerState = rememberDatePickerState(
					selectableDates = PastAndTodaySelectableDates()
				)
				val timePickerState = rememberTimePickerState()

				when (uiState.currentStep) {
					ManualAddStep.StartDate -> {
						DatePicker(datePickerState)
					}

					ManualAddStep.StartTime -> {
						TimePicker(timePickerState)
					}

					ManualAddStep.SetDuration -> {
						// Length Input
						Column(modifier = Modifier.padding(16.dp)) {
							// Show summary of selections
							uiState.selectedDateTime?.let { dateTime ->
								Text(
									text = stringResource(
										id = R.string.manual_add_selected_start,
										"${dateTime.date} ${dateTime.hour}:${
											dateTime.minute.toString().padStart(2, '0')
										}"
									),
									style = MaterialTheme.typography.bodyMedium
								)
							}

							OutlinedTextField(
								value = uiState.lengthHours,
								onValueChange = { viewModel.onLengthChanged(it) },
								label = { Text(stringResource(id = R.string.manual_add_length_hint)) },
								keyboardOptions = KeyboardOptions(
									keyboardType = KeyboardType.Number
								),
								modifier = Modifier.fillMaxWidth()
							)

							Spacer(modifier = Modifier.height(8.dp))

							// Button to calculate length from end date/time
							Button(
								onClick = { showEndDateTimePicker = true },
							) {
								Text(stringResource(id = R.string.manual_add_calculate_from_end))
							}

							if (showEndDateTimePicker) {
								val dateTimePickerState = rememberDateTimePickerDialogState()
								DateTimePickerDialog(
									onDismiss = { showEndDateTimePicker = false },
									onDateTimeSelected = { instant ->
										viewModel.onEndDateTimeSelected(instant)
										showEndDateTimePicker = false
									},
									title = stringResource(R.string.manual_add_set_end_title),
									finishButton = stringResource(id = R.string.manual_add_set_end_complete),
									state = dateTimePickerState
								)
							}
						}
					}
				}

				Spacer(modifier = Modifier.height(16.dp))

				// Buttons
				Row(
					modifier = Modifier
						.padding(16.dp)
						.fillMaxWidth(),
					horizontalArrangement = Arrangement.End
				) {
					TextButton(onClick = {
						viewModel.onDismiss()
						onDismiss()
					}) {
						Text(stringResource(id = R.string.cancel_button))
					}

					Spacer(modifier = Modifier.width(8.dp))

					Button(
						onClick = {
							when (uiState.currentStep) {
								ManualAddStep.StartDate -> {
									datePickerState.selectedDateMillis?.let { ms ->
										viewModel.onDateSelected(ms)
									}
								}

								ManualAddStep.StartTime -> {
									viewModel.onTimeSelected(timePickerState.hour, timePickerState.minute)
								}

								ManualAddStep.SetDuration -> {
									viewModel.onAddEntry()
									viewModel.onDismiss()
									onDismiss()
								}
							}
						},
						enabled = if (uiState.currentStep.isFinalStep.not()) uiState.isNextButtonEnabled else uiState.isCompleteButtonEnabled
					) {
						Text(
							text = if (uiState.currentStep.isFinalStep.not()) {
								stringResource(id = R.string.next_button)
							} else {
								stringResource(id = R.string.manual_add_complete_button)
							}
						)
					}
				}
			}
		}
	}
}
