package com.darkrockstudios.apps.fasttrack.screens.log.manualadd

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
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
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toInstant
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@Composable
fun ManualAddDialog(
	onDismiss: () -> Unit,
	entryToEdit: com.darkrockstudios.apps.fasttrack.data.log.FastingLogEntry? = null,
	viewModel: IManualAddViewModel = koinViewModel<ManualAddViewModel>()
) {
	// Initialize with entry if editing
	LaunchedEffect(entryToEdit) {
		entryToEdit?.let { viewModel.initializeWithEntry(it) }
	}

	val uiState by viewModel.uiState.collectAsState()
	var showEndDateTimePicker by remember { mutableStateOf(false) }

	Dialog(
		onDismissRequest = {
			viewModel.onDismiss()
			onDismiss()
		},
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
						.padding(horizontal = 16.dp)
						.fillMaxWidth(),
					horizontalArrangement = Arrangement.SpaceBetween,
					verticalAlignment = Alignment.CenterVertically
				) {
					Text(
						text = stringResource(
							id = if (entryToEdit != null) R.string.manual_edit_title else R.string.manual_add_title
						),
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

				// Initialize picker states with existing values when editing
				val initialDateMillis =
					uiState.selectedDate?.atStartOfDayIn(TimeZone.currentSystemDefault())?.toEpochMilliseconds()

				val datePickerState = rememberDatePickerState(
					initialSelectedDateMillis = initialDateMillis,
					selectableDates = PastAndTodaySelectableDates()
				)

				val initialHour = uiState.selectedDateTime?.hour ?: 0
				val initialMinute = uiState.selectedDateTime?.minute ?: 0
				val timePickerState = rememberTimePickerState(
					initialHour = initialHour,
					initialMinute = initialMinute
				)

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
							// Show summary of selections with clickable edit options
							uiState.selectedDateTime?.let { dateTime ->
								Text(
									text = stringResource(id = R.string.manual_add_start_date_time_label),
									style = MaterialTheme.typography.labelMedium,
									color = MaterialTheme.colorScheme.onSurfaceVariant
								)

								Spacer(modifier = Modifier.height(4.dp))

								// Date row - clickable
								Row(
									modifier = Modifier
										.fillMaxWidth()
										.clickable { viewModel.goToStep(ManualAddStep.StartDate) }
										.padding(vertical = 8.dp),
									horizontalArrangement = Arrangement.SpaceBetween,
									verticalAlignment = Alignment.CenterVertically
								) {
									Text(
										text = "${dateTime.date}",
										style = MaterialTheme.typography.bodyLarge
									)
									Icon(
										imageVector = Icons.Default.Edit,
										contentDescription = stringResource(id = R.string.edit_date),
										modifier = Modifier.size(20.dp),
										tint = MaterialTheme.colorScheme.primary
									)
								}

								HorizontalDivider()

								// Time row - clickable
								Row(
									modifier = Modifier
										.fillMaxWidth()
										.clickable { viewModel.goToStep(ManualAddStep.StartTime) }
										.padding(vertical = 8.dp),
									horizontalArrangement = Arrangement.SpaceBetween,
									verticalAlignment = Alignment.CenterVertically
								) {
									Text(
										text = "${dateTime.hour}:${dateTime.minute.toString().padStart(2, '0')}",
										style = MaterialTheme.typography.bodyLarge
									)
									Icon(
										imageVector = Icons.Default.Edit,
										contentDescription = stringResource(id = R.string.edit_time),
										modifier = Modifier.size(20.dp),
										tint = MaterialTheme.colorScheme.primary
									)
								}

								HorizontalDivider()

								Spacer(modifier = Modifier.height(16.dp))
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
								val initialEndInstant = uiState.end()
								val minStartInstant =
									uiState.selectedDateTime?.toInstant(TimeZone.currentSystemDefault())

								DateTimePickerDialog(
									onDismiss = { showEndDateTimePicker = false },
									onDateTimeSelected = { instant ->
										viewModel.onEndDateTimeSelected(instant)
										showEndDateTimePicker = false
									},
									title = stringResource(R.string.manual_add_set_end_title),
									finishButton = stringResource(id = R.string.manual_add_set_end_complete),
									state = dateTimePickerState,
									initialInstant = initialEndInstant,
									minInstant = minStartInstant
								)
							}
						}
					}
				}

				// Buttons
				Row(
					modifier = Modifier
						.padding(16.dp)
						.fillMaxWidth(),
					horizontalArrangement = Arrangement.SpaceBetween
				) {
					// Left side: Previous button (only show if not on first step)
					if (uiState.currentStep != ManualAddStep.StartDate) {
						TextButton(onClick = {
							viewModel.onPreviousStep()
						}) {
							Text(stringResource(id = R.string.previous_button))
						}
					} else {
						Spacer(modifier = Modifier.width(1.dp)) // Placeholder for alignment
					}

					// Right side: Cancel and Next/Complete buttons
					Row {
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
										if (viewModel.onAddEntry()) {
											viewModel.onDismiss()
											onDismiss()
										}
									}
								}
							},
							enabled = if (uiState.currentStep.isFinalStep.not()) uiState.isNextButtonEnabled else uiState.isCompleteButtonEnabled
						) {
							Text(
								text = if (uiState.currentStep.isFinalStep.not()) {
									stringResource(id = R.string.next_button)
								} else {
									// Show "Save" when editing, "Add" when creating new
									if (entryToEdit != null) {
										stringResource(id = R.string.manual_add_save_button)
									} else {
										stringResource(id = R.string.manual_add_complete_button)
									}
								}
							)
						}
					}
				}
			}
		}
	}
}
