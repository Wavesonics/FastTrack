package com.darkrockstudios.apps.fasttrack.screens.log

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.darkrockstudios.apps.fasttrack.R
import com.darkrockstudios.apps.fasttrack.data.Stages
import com.darkrockstudios.apps.fasttrack.data.log.FastingLogEntry
import com.darkrockstudios.apps.fasttrack.utils.formatAs
import kotlin.math.roundToInt
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime

@ExperimentalTime
@Composable
fun FastEntryItem(
	entry: FastingLogEntry,
	onLongClick: () -> Unit
) {
	Card(
		modifier = Modifier.Companion
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
			modifier = Modifier.Companion
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
				fontWeight = FontWeight.Companion.Bold
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