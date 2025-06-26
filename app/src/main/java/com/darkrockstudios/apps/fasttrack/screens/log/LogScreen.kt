package com.darkrockstudios.apps.fasttrack.screens.log

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.darkrockstudios.apps.fasttrack.R
import com.darkrockstudios.apps.fasttrack.data.Stages
import com.darkrockstudios.apps.fasttrack.data.log.FastingLogEntry
import com.darkrockstudios.apps.fasttrack.screens.log.manualadd.ManualAddDialog
import com.darkrockstudios.apps.fasttrack.utils.formatAs
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.roundToInt
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime

@ExperimentalTime
@Composable
fun LogScreen(
	contentPaddingValues: PaddingValues = PaddingValues(0.dp),
	viewModel: ILogViewModel = koinViewModel<LogViewModel>(),
) {
	val uiState by viewModel.uiState.collectAsState()

	val lifecycleOwner = LocalLifecycleOwner.current
	LaunchedEffect(Unit) {
		lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
			viewModel.loadEntries()
		}
	}

	var entryToDelete by remember { mutableStateOf<FastingLogEntry?>(null) }
	if (entryToDelete != null) {
		ConfirmDelete(entryToDelete, viewModel) {
			entryToDelete = null
		}
	}

	Box(
		modifier = Modifier
			.fillMaxSize()
			.background(MaterialTheme.colorScheme.background)
	) {
		Column(
			modifier = Modifier
				.fillMaxSize()
				.padding(16.dp)
		) {
			// Total Ketosis and Autophagy Hours
			Column(
				modifier = Modifier
					.fillMaxWidth()
					.padding(top = contentPaddingValues.calculateTopPadding()),

			) {
				// Total Ketosis
				Row {
					Text(
						text = stringResource(id = R.string.log_total_ketosis),
						style = MaterialTheme.typography.bodyLarge,
						color = MaterialTheme.colorScheme.onBackground,
					)
					Spacer(modifier = Modifier.width(8.dp))
					Text(
						text = stringResource(id = R.string.log_total_hours, uiState.totalKetosisHours),
						style = MaterialTheme.typography.headlineSmall,
						color = MaterialTheme.colorScheme.onBackground,
					)
				}

				// Total Autophagy
				Row {
					Text(
						text = stringResource(id = R.string.log_total_autophagy),
						style = MaterialTheme.typography.bodyLarge,
						color = MaterialTheme.colorScheme.onBackground,
					)
					Spacer(modifier = Modifier.width(8.dp))
					Text(
						text = stringResource(id = R.string.log_total_hours, uiState.totalAutophagyHours),
						style = MaterialTheme.typography.headlineSmall,
						color = MaterialTheme.colorScheme.onBackground,
					)
				}
			}

			Spacer(modifier = Modifier.height(16.dp))
			val direction = LocalLayoutDirection.current
			LazyColumn(
				modifier = Modifier
					.fillMaxWidth()
					.weight(1f),
				contentPadding = PaddingValues(
					start = contentPaddingValues.calculateStartPadding(direction),
					end = contentPaddingValues.calculateEndPadding(direction),
					bottom = contentPaddingValues.calculateBottomPadding(),
				),
			) {
				items(uiState.entries, key = { it.id }) { entry ->
					FastEntryItem(
						entry = entry,
						onLongClick = {
							entryToDelete = entry
						}
					)
				}
			}
		}

		// FAB for Manual Add
		FloatingActionButton(
			onClick = { viewModel.showManualAddDialog() },
			modifier = Modifier
				.align(Alignment.BottomEnd)
				.padding(
					top = 16.dp,
					bottom = contentPaddingValues.calculateBottomPadding() + 16.dp,
					start = 16.dp,
					end = 16.dp,
				)
		) {
			Icon(
				imageVector = Icons.Default.Add,
				contentDescription = stringResource(id = R.string.manual_add_title)
			)
		}

		// Manual Add Dialog
		if (uiState.showManualAddDialog) {
			ManualAddDialog(
				onDismiss = { viewModel.hideManualAddDialog() },
			)
		}
	}
}

@Composable
private fun ConfirmDelete(
	entryToDelete: FastingLogEntry?,
	viewModel: ILogViewModel,
	onDismiss: () -> Unit,
) {
	if (entryToDelete != null) {
		AlertDialog(
			onDismissRequest = onDismiss,
			title = { Text(text = stringResource(id = R.string.confirm_delete_fast_title)) },
			confirmButton = {
				TextButton(
					onClick = {
						viewModel.deleteFast(entryToDelete)
						onDismiss()
					}
				) {
					Text(text = stringResource(id = R.string.confirm_delete_fast_positive))
				}
			},
			dismissButton = {
				TextButton(
					onClick = {
						onDismiss()
					}
				) {
					Text(text = stringResource(id = R.string.confirm_delete_fast_negative))
				}
			},
			properties = DialogProperties(
				dismissOnBackPress = true,
				dismissOnClickOutside = true
			)
		)
	}
}

@ExperimentalTime
@Composable
fun FastEntryItem(
	entry: FastingLogEntry,
	onLongClick: () -> Unit
) {
	Card(
		modifier = Modifier
			.fillMaxWidth()
			.padding(bottom = 8.dp)
			.pointerInput(Unit) {
				detectTapGestures(
					onLongPress = { onLongClick() }
				)
			},
		colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
		elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
	) {
		Column(
			modifier = Modifier
				.fillMaxWidth()
				.padding(16.dp)
		) {
			val hours = entry.length.toDouble(DurationUnit.HOURS).roundToInt()

			// Calculate ketosis hours
			val ketosisStart = Stages.PHASE_KETOSIS.hours.toDouble()
			val lenHours = entry.length.toDouble(DurationUnit.HOURS)
			val ketosisHours = if (lenHours > ketosisStart) {
				(lenHours - ketosisStart).roundToInt()
			} else {
				0
			}

			// Calculate autophagy hours
			val autophagyStart = Stages.PHASE_AUTOPHAGY.hours.toDouble()
			val autophagyHours = if (lenHours > autophagyStart) {
				(lenHours - autophagyStart).roundToInt()
			} else {
				0
			}

			val dateStr = remember(entry.start) {
				entry.start.formatAs("d MMM uuuu - HH:mm")
			}

			Text(
				text = stringResource(id = R.string.log_entry_started, dateStr),
				style = MaterialTheme.typography.titleLarge,
				color = MaterialTheme.colorScheme.onSurface,
			)
			Text(
				text = stringResource(id = R.string.log_entry_length, hours),
				style = MaterialTheme.typography.headlineSmall,
				color = MaterialTheme.colorScheme.onSurface,
				fontWeight = FontWeight.Bold
			)
			Text(
				text = stringResource(id = R.string.log_entry_ketosis, ketosisHours),
				style = MaterialTheme.typography.titleMedium,
				color = MaterialTheme.colorScheme.onSurface,
			)
			Text(
				text = stringResource(id = R.string.log_entry_autophagy, autophagyHours),
				style = MaterialTheme.typography.titleMedium,
				color = MaterialTheme.colorScheme.onSurface,
			)
		}
	}
}
