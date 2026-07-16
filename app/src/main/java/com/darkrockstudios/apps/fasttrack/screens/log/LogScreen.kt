package com.darkrockstudios.apps.fasttrack.screens.log

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
import com.darkrockstudios.apps.fasttrack.utils.formatDuration
import kotlinx.datetime.toJavaLocalDate
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
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
				totalFasts = uiState.totalFasts,
				totalFastedDuration = uiState.totalFastedDuration,
				longestFastDuration = uiState.longestFastDuration,
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
					onAddForEmptyDay = viewModel::requestAddForDate,
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
				entryToEdit = uiState.entryToEdit,
				initialDate = uiState.manualAddInitialDate,
			)
		}

		uiState.emptyDayToAdd?.let { date ->
			AddFastForDayDialog(
				date = date,
				onConfirm = { viewModel.confirmAddForDate() },
				onDismiss = { viewModel.dismissAddForDate() },
			)
		}

		if (uiState.showClearAllConfirmation) {
			ClearLogbookSheet(
				count = uiState.totalFasts,
				onConfirm = { viewModel.clearAll() },
				onDismiss = { viewModel.dismissClearAll() },
			)
		}
	}
}

/**
 * Gentle confirmation shown when an empty (past/today) calendar day is tapped: it
 * offers to open Manual Add preseeded with that day, rather than silently doing
 * nothing.
 */
@Composable
private fun AddFastForDayDialog(
	date: kotlinx.datetime.LocalDate,
	onConfirm: () -> Unit,
	onDismiss: () -> Unit,
) {
	val label = remember(date) {
		val formatter = java.time.format.DateTimeFormatter
			.ofPattern("EEE, d MMM uuuu", java.util.Locale.getDefault())
		date.toJavaLocalDate().format(formatter)
	}
	AlertDialog(
		onDismissRequest = onDismiss,
		icon = { Icon(imageVector = Icons.Default.Add, contentDescription = null) },
		title = { Text(text = stringResource(id = R.string.add_fast_for_day_title, label)) },
		confirmButton = {
			TextButton(onClick = onConfirm) {
				Text(stringResource(id = R.string.add_fast_for_day_confirm))
			}
		},
		dismissButton = {
			TextButton(onClick = onDismiss) {
				Text(stringResource(id = R.string.cancel_button))
			}
		},
	)
}

/**
 * Danger confirmation for wiping the whole logbook. Deliberately heavy: an
 * error-toned warning glyph, an explicit "cannot be undone", the exact count in
 * both the message and the destructive button, and Cancel as the calm way out.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ClearLogbookSheet(
	count: Int,
	onConfirm: () -> Unit,
	onDismiss: () -> Unit,
) {
	ModalBottomSheet(
		onDismissRequest = onDismiss,
		sheetState = rememberModalBottomSheetState(),
	) {
		Column(
			modifier = Modifier
				.fillMaxWidth()
				.padding(horizontal = 21.dp)
				.padding(bottom = 34.dp),
			horizontalAlignment = Alignment.CenterHorizontally,
		) {
			Box(
				modifier = Modifier
					.size(56.dp)
					.clip(CircleShape)
					.background(MaterialTheme.colorScheme.errorContainer),
				contentAlignment = Alignment.Center,
			) {
				Icon(
					imageVector = Icons.Default.WarningAmber,
					contentDescription = null,
					tint = MaterialTheme.colorScheme.onErrorContainer,
					modifier = Modifier.size(30.dp),
				)
			}

			Spacer(modifier = Modifier.height(16.dp))
			Text(
				text = stringResource(id = R.string.clear_logbook_title),
				style = MaterialTheme.typography.headlineSmall,
				fontWeight = FontWeight.Bold,
				textAlign = TextAlign.Center,
				color = MaterialTheme.colorScheme.onSurface,
			)
			Spacer(modifier = Modifier.height(8.dp))
			Text(
				text = stringResource(id = R.string.clear_logbook_message, count),
				style = MaterialTheme.typography.bodyMedium,
				textAlign = TextAlign.Center,
				color = MaterialTheme.colorScheme.onSurfaceVariant,
			)

			Spacer(modifier = Modifier.height(24.dp))
			Button(
				onClick = onConfirm,
				colors = ButtonDefaults.buttonColors(
					containerColor = MaterialTheme.colorScheme.error,
					contentColor = MaterialTheme.colorScheme.onError,
				),
				modifier = Modifier
					.fillMaxWidth()
					.heightIn(min = 55.dp),
			) {
				Icon(
					imageVector = Icons.Default.Delete,
					contentDescription = null,
					modifier = Modifier.padding(end = 8.dp),
				)
				Text(
					text = stringResource(id = R.string.clear_logbook_confirm, count),
					style = MaterialTheme.typography.titleMedium,
				)
			}
			Spacer(modifier = Modifier.height(8.dp))
			TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
				Text(stringResource(id = R.string.cancel_button))
			}
		}
	}
}

@Composable
private fun LogStatsHeader(
	totalFasts: Int,
	totalFastedDuration: Duration,
	longestFastDuration: Duration,
	totalKetosisHours: Int,
	totalAutophagyHours: Int,
	viewMode: LogViewMode,
	onViewModeChanged: (LogViewMode) -> Unit,
) {
	val context = LocalContext.current

	// Local, non-persisted format toggle: durations default to "39d 22h";
	// tapping any stat card flips them to total hours ("432h 40m") for as long
	// as this screen stays composed, then returns to the default next time.
	var showTotalHours by remember { mutableStateOf(false) }
	val toggle = { showTotalHours = !showTotalHours }

	fun durationStat(d: Duration): String =
		if (d == Duration.ZERO) "0h"
		else formatDuration(context, d, showTotalHours = showTotalHours)

	// View mode switch, left-aligned (the "Lifetime stats" label was redundant)
	LogViewModeSwitch(
		viewMode = viewMode,
		onViewModeChanged = onViewModeChanged,
		modifier = Modifier.padding(vertical = 12.dp),
	)

	Row(
		modifier = Modifier
			.fillMaxWidth()
			.padding(bottom = 8.dp),
		horizontalArrangement = Arrangement.spacedBy(8.dp)
	) {
		StatCard(
			title = stringResource(id = R.string.stat_total_fasts),
			valueText = totalFasts.toString(),
			onClick = toggle,
			modifier = Modifier.weight(1f)
		)
		StatCard(
			title = stringResource(id = R.string.stat_total_fasted),
			valueText = durationStat(totalFastedDuration),
			onClick = toggle,
			modifier = Modifier.weight(1f)
		)
		StatCard(
			title = stringResource(id = R.string.stat_longest_fast),
			valueText = durationStat(longestFastDuration),
			onClick = toggle,
			modifier = Modifier.weight(1f)
		)
	}
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.padding(bottom = 16.dp),
		horizontalArrangement = Arrangement.spacedBy(8.dp)
	) {
		StatCard(
			title = stringResource(id = R.string.log_total_ketosis),
			valueText = formatDuration(context, totalKetosisHours.hours, showTotalHours = showTotalHours, withMinutes = false),
			onClick = toggle,
			modifier = Modifier.weight(1f)
		)
		StatCard(
			title = stringResource(id = R.string.log_total_autophagy),
			valueText = formatDuration(context, totalAutophagyHours.hours, showTotalHours = showTotalHours, withMinutes = false),
			onClick = toggle,
			modifier = Modifier.weight(1f)
		)
	}
}

/**
 * Two-option segmented control for the Log view mode. Both modes are always
 * visible with the active one highlighted, so switching back and forth is
 * obvious (the old single icon-only toggle was too easy to overlook).
 */
@Composable
private fun LogViewModeSwitch(
	viewMode: LogViewMode,
	onViewModeChanged: (LogViewMode) -> Unit,
	modifier: Modifier = Modifier,
) {
	SingleChoiceSegmentedButtonRow(modifier = modifier) {
		SegmentedButton(
			selected = viewMode == LogViewMode.LIST,
			onClick = { onViewModeChanged(LogViewMode.LIST) },
			shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
			icon = {
				Icon(
					imageVector = Icons.AutoMirrored.Filled.ViewList,
					contentDescription = null,
					modifier = Modifier.size(SegmentedButtonDefaults.IconSize),
				)
			},
			label = { Text(stringResource(id = R.string.log_view_mode_list)) },
		)
		SegmentedButton(
			selected = viewMode == LogViewMode.CALENDAR,
			onClick = { onViewModeChanged(LogViewMode.CALENDAR) },
			shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
			icon = {
				Icon(
					imageVector = Icons.Default.CalendarMonth,
					contentDescription = null,
					modifier = Modifier.size(SegmentedButtonDefaults.IconSize),
				)
			},
			label = { Text(stringResource(id = R.string.log_view_mode_calendar)) },
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
	onClick: (() -> Unit)? = null,
) {
	ElevatedCard(
		modifier = if (onClick != null) modifier.clickable(onClick = onClick) else modifier,
	) {
		Column(
			modifier = Modifier
				.fillMaxWidth()
				.padding(horizontal = 10.dp, vertical = 10.dp),
			verticalArrangement = Arrangement.spacedBy(2.dp)
		) {
			Text(
				text = title,
				style = MaterialTheme.typography.labelSmall,
				color = MaterialTheme.colorScheme.onSurfaceVariant,
				maxLines = 1,
				overflow = TextOverflow.Ellipsis,
			)
			Text(
				text = valueText,
				style = MaterialTheme.typography.titleMedium,
				color = MaterialTheme.colorScheme.onSurface,
				maxLines = 1,
				overflow = TextOverflow.Ellipsis,
			)
		}
	}
}
