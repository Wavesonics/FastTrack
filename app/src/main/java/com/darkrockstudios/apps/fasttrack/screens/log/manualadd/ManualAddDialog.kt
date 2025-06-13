package com.darkrockstudios.apps.fasttrack.screens.log.manualadd

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.darkrockstudios.apps.fasttrack.R
import com.darkrockstudios.apps.fasttrack.utils.PastAndTodaySelectableDates
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ManualAddDialog(
	onDismiss: () -> Unit,
	viewModel: IManualAddViewModel = koinViewModel<ManualAddViewModel>()
) {
	val uiState by viewModel.uiState.collectAsState()

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
					0 -> {
						DatePicker(datePickerState)
					}

					1 -> {
						TimePicker(timePickerState)
					}

					2 -> {
						// Length Input
						Column {
							OutlinedTextField(
								value = uiState.lengthHours,
								onValueChange = { viewModel.onLengthChanged(it) },
								label = { Text(stringResource(id = R.string.manual_add_length_hint)) },
								keyboardOptions = KeyboardOptions(
									keyboardType = KeyboardType.Number
								),
								modifier = Modifier.fillMaxWidth()
							)

							Spacer(modifier = Modifier.height(16.dp))

							// Show summary of selections
							uiState.selectedDateTime?.let { dateTime ->
								Text(
									text = stringResource(
										id = R.string.manual_add_selected_date,
										dateTime.date.toString()
									),
									style = MaterialTheme.typography.bodyMedium
								)
								Text(
									text = stringResource(
										id = R.string.manual_add_selected_time,
										dateTime.hour.toString(),
										dateTime.minute.toString().padStart(2, '0')
									),
									style = MaterialTheme.typography.bodyMedium
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
								0 -> {
									datePickerState.selectedDateMillis?.let { ms ->
										viewModel.onDateSelected(ms)
									}
								}

								1 -> {
									viewModel.onTimeSelected(timePickerState.hour, timePickerState.minute)
								}

								2 -> {
									viewModel.onAddEntry()
									viewModel.onDismiss()
									onDismiss()
								}
							}
						},
						enabled = if (uiState.currentStep < 2) uiState.isNextButtonEnabled else uiState.isCompleteButtonEnabled
					) {
						Text(
							text = if (uiState.currentStep < 2) {
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
