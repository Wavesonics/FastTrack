package com.darkrockstudios.apps.fasttrack.screens.log

import android.os.VibrationEffect
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.darkrockstudios.apps.fasttrack.R
import com.darkrockstudios.apps.fasttrack.data.Stages
import com.darkrockstudios.apps.fasttrack.data.log.FastingLogEntry
import com.darkrockstudios.apps.fasttrack.screens.fasting.gaugeColors
import com.darkrockstudios.apps.fasttrack.utils.formatAs
import com.darkrockstudios.apps.fasttrack.utils.formatDuration
import com.darkrockstudios.apps.fasttrack.utils.rememberVibrator
import com.darkrockstudios.apps.fasttrack.utils.shouldUse24HourFormat
import androidx.compose.ui.platform.LocalContext
import kotlin.math.roundToInt
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime

/**
 * A single logbook row.
 *
 * Interaction model (fluid + least-surprise + discoverable, and safe from the
 * pager's horizontal swipe):
 * - **Tap** opens the entry for editing — the universal "tap a record to open it"
 *   convention, so the row is never a dead target.
 * - **Long-press** opens a small Edit/Delete menu — a deliberate gesture (no
 *   accidental deletes) and the accessible, TalkBack-friendly path.
 *
 * Bulk deletion lives in the screen's overflow menu ("Clear logbook"), behind a
 * danger confirmation, rather than being reachable by a stray swipe.
 */
@ExperimentalTime
@Composable
fun FastEntryItem(
	entry: FastingLogEntry,
	onEdit: () -> Unit,
	onDelete: () -> Unit,
) {
	var showMenu by remember { mutableStateOf(false) }
	val vibrator = rememberVibrator()
	val context = LocalContext.current
	val use24Hour = shouldUse24HourFormat(context)
	val editLabel = stringResource(id = R.string.menu_edit)
	val deleteLabel = stringResource(id = R.string.menu_delete)

	Box(modifier = Modifier.padding(bottom = 8.dp)) {
		Card(
			modifier = Modifier
				.fillMaxWidth()
				.combinedClickable(
					onClick = onEdit,
					onLongClick = {
						vibrator?.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
						showMenu = true
					},
				)
				.semantics {
					// The long-press menu is invisible to assistive tech; surface both
					// actions explicitly in the TalkBack actions menu.
					customActions = listOf(
						CustomAccessibilityAction(editLabel) { onEdit(); true },
						CustomAccessibilityAction(deleteLabel) { onDelete(); true },
					)
				},
			colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
			elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
		) {
			Column {
				val dateStr = remember(entry.start, use24Hour) {
					val timePattern = if (use24Hour) "HH:mm" else "h:mm a"
					entry.start.formatAs("d MMM uuuu - $timePattern")
				}

				Text(
					text = stringResource(id = R.string.log_entry_started, dateStr),
					style = MaterialTheme.typography.titleMedium,
					fontStyle = FontStyle.Italic,
					color = MaterialTheme.colorScheme.onSurface,
					modifier = Modifier.padding(top = 13.dp, start = 16.dp, end = 16.dp)
				)

				Row(
					modifier = Modifier.Companion
						.fillMaxWidth()
						.padding(start = 16.dp, end = 16.dp, top = 13.dp),
					horizontalArrangement = Arrangement.spacedBy(13.dp),
					verticalAlignment = Alignment.CenterVertically
				) {
					val lenHours = entry.length.toDouble(DurationUnit.HOURS)

					// Determine highest stage reached
					val highestStage = remember(lenHours) {
						Stages.phases.lastOrNull { lenHours >= it.hours } ?: Stages.phases.first()
					}

					val stageIndex = Stages.phases.indexOf(highestStage).coerceAtLeast(0)
					val ovalColor = gaugeColors.getOrElse(stageIndex) { MaterialTheme.colorScheme.primary }

					Box(
						modifier = Modifier
							.width(12.dp)
							.height(36.dp)
							.background(ovalColor, shape = RoundedCornerShape(percent = 30))
							.border(
								width = 2.dp,
								color = MaterialTheme.colorScheme.onBackground,
								shape = RoundedCornerShape(percent = 30)
							),
					)

					Row(
						verticalAlignment = Alignment.CenterVertically
					) {
						val ketosisStart = Stages.PHASE_KETOSIS.hours.toDouble()
						val ketosisHours = if (lenHours > ketosisStart) {
							(lenHours - ketosisStart).roundToInt()
						} else 0

						val autophagyStart = Stages.PHASE_AUTOPHAGY.hours.toDouble()
						val autophagyHours = if (lenHours > autophagyStart) {
							(lenHours - autophagyStart).roundToInt()
						} else 0

						Text(
							text = "⏱️ " + formatDuration(context, entry.length),
							style = MaterialTheme.typography.headlineSmall.copy(fontSize = 18.sp),
							color = MaterialTheme.colorScheme.onSurface,
							fontWeight = FontWeight.Bold,
						)

						Spacer(modifier = Modifier.weight(1f))

						Column {
							Text(
								text = "🔥 " + stringResource(id = R.string.log_entry_ketosis, ketosisHours),
								style = MaterialTheme.typography.titleSmall,
								color = MaterialTheme.colorScheme.onSurface,
							)
							Text(
								text = "🧬 " + stringResource(id = R.string.log_entry_autophagy, autophagyHours),
								style = MaterialTheme.typography.titleSmall,
								color = MaterialTheme.colorScheme.onSurface,
							)
						}
					}
				}

				// Optional note, shown as a blockquote so long text stays readable
				if (entry.notes.isNotBlank()) {
					Row(
						modifier = Modifier
							.fillMaxWidth()
							.padding(start = 16.dp, end = 16.dp, top = 5.dp)
							.height(IntrinsicSize.Min),
						verticalAlignment = Alignment.CenterVertically,
					) {
						Box(
							modifier = Modifier
								.width(3.dp)
								.fillMaxHeight()
								.background(
									MaterialTheme.colorScheme.primary,
									RoundedCornerShape(2.dp)
								)
						)
						Text(
							text = entry.notes,
							style = MaterialTheme.typography.bodyMedium,
							fontStyle = FontStyle.Italic,
							color = MaterialTheme.colorScheme.onSurfaceVariant,
							maxLines = 6,
							overflow = TextOverflow.Ellipsis,
							modifier = Modifier
								.weight(1f)
								.padding(start = 8.dp)
						)
					}
				}

				Spacer(modifier = Modifier.height(13.dp))
			}
		}

		DropdownMenu(
			expanded = showMenu,
			onDismissRequest = { showMenu = false }
		) {
			DropdownMenuItem(
				text = { Text(stringResource(id = R.string.menu_edit)) },
				onClick = {
					showMenu = false
					onEdit()
				}
			)
			DropdownMenuItem(
				text = { Text(stringResource(id = R.string.menu_delete)) },
				onClick = {
					showMenu = false
					onDelete()
				}
			)
		}
	}
}
