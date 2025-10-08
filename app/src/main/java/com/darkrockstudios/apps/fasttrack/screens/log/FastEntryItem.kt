package com.darkrockstudios.apps.fasttrack.screens.log

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.darkrockstudios.apps.fasttrack.R
import com.darkrockstudios.apps.fasttrack.data.Stages
import com.darkrockstudios.apps.fasttrack.data.log.FastingLogEntry
import com.darkrockstudios.apps.fasttrack.screens.fasting.gaugeColors
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
		Row(
			modifier = Modifier.Companion
				.fillMaxWidth()
				.padding(16.dp),
			horizontalArrangement = Arrangement.spacedBy(16.dp),
			verticalAlignment = Alignment.CenterVertically
		) {
			val lenHours = entry.length.toDouble(DurationUnit.HOURS)
			val hours = lenHours.roundToInt()

			// Determine highest stage reached
			val highestStage = remember(lenHours) {
				Stages.phases.lastOrNull { lenHours >= it.hours } ?: Stages.phases.first()
			}

			val stageIndex = Stages.phases.indexOf(highestStage).coerceAtLeast(0)
			val ovalColor = gaugeColors.getOrElse(stageIndex) { MaterialTheme.colorScheme.primary }

			Box(
				modifier = Modifier
					.width(56.dp)
					.height(36.dp)
					.background(ovalColor, shape = RoundedCornerShape(percent = 30))
					.border(
						width = 2.dp,
						color = MaterialTheme.colorScheme.onBackground,
						shape = RoundedCornerShape(percent = 30)
					),
			)

			Column(modifier = Modifier.weight(1f)) {
				val ketosisStart = Stages.PHASE_KETOSIS.hours.toDouble()
				val ketosisHours = if (lenHours > ketosisStart) {
					(lenHours - ketosisStart).roundToInt()
				} else 0

				val autophagyStart = Stages.PHASE_AUTOPHAGY.hours.toDouble()
				val autophagyHours = if (lenHours > autophagyStart) {
					(lenHours - autophagyStart).roundToInt()
				} else 0

				val dateStr = remember(entry.start) {
					entry.start.formatAs("d MMM uuuu - HH:mm")
				}

				Text(
					text = stringResource(id = R.string.log_entry_started, dateStr),
					style = MaterialTheme.typography.titleMedium,
					color = MaterialTheme.colorScheme.onSurface,
				)
				Spacer(modifier = Modifier.height(2.dp))
				Text(
					text = "⏱️ " + stringResource(id = R.string.log_entry_length, hours),
					style = MaterialTheme.typography.headlineSmall,
					color = MaterialTheme.colorScheme.onSurface,
					fontWeight = FontWeight.Bold
				)
				Spacer(modifier = Modifier.height(2.dp))
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
}