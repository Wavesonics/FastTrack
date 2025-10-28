package com.darkrockstudios.apps.fasttrack.screens.log

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.darkrockstudios.apps.fasttrack.R
import com.darkrockstudios.apps.fasttrack.data.log.FastingLogEntry
import com.darkrockstudios.apps.fasttrack.screens.log.manualadd.ManualAddDialog
import com.darkrockstudios.apps.fasttrack.utils.MAX_COLUMN_WIDTH
import org.koin.compose.viewmodel.koinViewModel
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
	) {
		val direction = LocalLayoutDirection.current
		LazyColumn(
			modifier = Modifier
				.fillMaxHeight()
				.widthIn(max = MAX_COLUMN_WIDTH)
				.align(Alignment.Center)
				.padding(16.dp),
			contentPadding = PaddingValues(
				top = contentPaddingValues.calculateTopPadding(),
				start = contentPaddingValues.calculateStartPadding(direction),
				end = contentPaddingValues.calculateEndPadding(direction),
				bottom = contentPaddingValues.calculateBottomPadding(),
			),
		) {
			item {
				Text(
					stringResource(R.string.log_stats_label),
					style = MaterialTheme.typography.labelLarge,
					color = MaterialTheme.colorScheme.onSurfaceVariant,
					modifier = Modifier.padding(bottom = 8.dp)
				)
			}
			item {
				// Total Ketosis and Autophagy Hours
				Row(
					modifier = Modifier
						.fillMaxWidth()
						.padding(bottom = 16.dp),
					horizontalArrangement = Arrangement.spacedBy(12.dp)
				) {
					StatCard(
						title = stringResource(id = R.string.log_total_ketosis),
						valueText = stringResource(
							id = R.string.log_total_hours,
							uiState.totalKetosisHours
						),
						modifier = Modifier.weight(1f)
					)
					StatCard(
						title = stringResource(id = R.string.log_total_autophagy),
						valueText = stringResource(
							id = R.string.log_total_hours,
							uiState.totalAutophagyHours
						),
						modifier = Modifier.weight(1f)
					)
				}
			}

			item {
				Text(
					stringResource(R.string.log_logbook_label),
					style = MaterialTheme.typography.labelLarge,
					color = MaterialTheme.colorScheme.onSurfaceVariant,
					modifier = Modifier.padding(bottom = 8.dp)
				)
			}

			if (uiState.entries.isNotEmpty()) {
				items(uiState.entries, key = { it.id }) { entry ->
					FastEntryItem(
						entry = entry,
						onEdit = {
							viewModel.showEditDialog(entry)
						},
						onDelete = {
							entryToDelete = entry
						}
					)
				}
			} else {
				item {
					Text(
						stringResource(R.string.log_no_entries),
						style = MaterialTheme.typography.bodyMedium,
						color = MaterialTheme.colorScheme.onSurfaceVariant,
						modifier = Modifier
							.fillMaxWidth()
							.padding(bottom = 8.dp),
						textAlign = TextAlign.Center
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

		// Manual Add/Edit Dialog
		if (uiState.showManualAddDialog) {
			ManualAddDialog(
				onDismiss = { viewModel.hideManualAddDialog() },
				entryToEdit = uiState.entryToEdit
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


@Composable
private fun StatCard(
	title: String,
	valueText: String,
	modifier: Modifier = Modifier,
) {
	ElevatedCard(
		modifier = modifier,
	) {
		Column(
			modifier = Modifier
				.fillMaxWidth()
				.padding(16.dp),
			verticalArrangement = Arrangement.spacedBy(6.dp)
		) {
			Text(
				text = title,
				style = MaterialTheme.typography.labelLarge,
				color = MaterialTheme.colorScheme.onSurfaceVariant
			)
			Text(
				text = valueText,
				style = MaterialTheme.typography.headlineMedium,
				color = MaterialTheme.colorScheme.onSurface
			)
		}
	}
}
