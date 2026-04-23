package com.darkrockstudios.apps.fasttrack.screens.log

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.darkrockstudios.apps.fasttrack.R
import com.darkrockstudios.apps.fasttrack.data.log.FastingLogEntry
import com.darkrockstudios.apps.fasttrack.data.settings.LogViewMode
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
		modifier = Modifier.fillMaxSize()
	) {
		val direction = LocalLayoutDirection.current
		val horizontalPadding = PaddingValues(
			start = contentPaddingValues.calculateStartPadding(direction),
			end = contentPaddingValues.calculateEndPadding(direction),
		)
		val bodyContentPadding = PaddingValues(
			bottom = contentPaddingValues.calculateBottomPadding() + 88.dp,
		)

		Column(
			modifier = Modifier
				.fillMaxHeight()
				.widthIn(max = MAX_COLUMN_WIDTH)
				.align(Alignment.Center)
				.padding(horizontalPadding)
				.padding(horizontal = 16.dp)
				.padding(top = contentPaddingValues.calculateTopPadding())
		) {
			LogStatsHeader(
				totalKetosisHours = uiState.totalKetosisHours,
				totalAutophagyHours = uiState.totalAutophagyHours,
				viewMode = uiState.viewMode,
				onViewModeChanged = viewModel::setViewMode,
			)

			when (uiState.viewMode) {
				LogViewMode.LIST -> LogListContent(
					entries = uiState.entries,
					onEdit = viewModel::showEditDialog,
					onDelete = { entryToDelete = it },
					contentPadding = bodyContentPadding,
				)

				LogViewMode.CALENDAR -> LogCalendarContent(
					entries = uiState.entries,
					selectedDate = uiState.selectedDate,
					onDateSelected = viewModel::selectDate,
					onEdit = viewModel::showEditDialog,
					onDelete = { entryToDelete = it },
					contentPadding = bodyContentPadding,
				)
			}
		}

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

		if (uiState.showManualAddDialog) {
			ManualAddDialog(
				onDismiss = { viewModel.hideManualAddDialog() },
				entryToEdit = uiState.entryToEdit
			)
		}
	}
}

@Composable
private fun LogStatsHeader(
	totalKetosisHours: Int,
	totalAutophagyHours: Int,
	viewMode: LogViewMode,
	onViewModeChanged: (LogViewMode) -> Unit,
) {
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.padding(vertical = 16.dp),
		verticalAlignment = Alignment.CenterVertically,
	) {
		Text(
			stringResource(R.string.log_stats_label),
			style = MaterialTheme.typography.labelLarge,
			color = MaterialTheme.colorScheme.onSurfaceVariant,
			modifier = Modifier.weight(1f),
		)
		LogViewModeIconToggle(
			viewMode = viewMode,
			onViewModeChanged = onViewModeChanged,
		)
	}
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.padding(bottom = 16.dp),
		horizontalArrangement = Arrangement.spacedBy(12.dp)
	) {
		StatCard(
			title = stringResource(id = R.string.log_total_ketosis),
			valueText = stringResource(id = R.string.log_total_hours, totalKetosisHours),
			modifier = Modifier.weight(1f)
		)
		StatCard(
			title = stringResource(id = R.string.log_total_autophagy),
			valueText = stringResource(id = R.string.log_total_hours, totalAutophagyHours),
			modifier = Modifier.weight(1f)
		)
	}
}

@Composable
private fun LogViewModeIconToggle(
	viewMode: LogViewMode,
	onViewModeChanged: (LogViewMode) -> Unit,
) {
	val isCalendar = viewMode == LogViewMode.CALENDAR
	val nextMode = if (isCalendar) LogViewMode.LIST else LogViewMode.CALENDAR
	val (icon, labelRes) = if (isCalendar) {
		Icons.AutoMirrored.Filled.ViewList to R.string.log_view_mode_list
	} else {
		Icons.Default.CalendarMonth to R.string.log_view_mode_calendar
	}
	IconButton(
		onClick = { onViewModeChanged(nextMode) },
		modifier = Modifier.size(28.dp),
	) {
		Icon(
			imageVector = icon,
			contentDescription = stringResource(id = labelRes),
			tint = MaterialTheme.colorScheme.onSurfaceVariant,
		)
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
