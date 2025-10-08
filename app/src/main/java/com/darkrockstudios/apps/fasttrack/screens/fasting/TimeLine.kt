package com.darkrockstudios.apps.fasttrack.screens.fasting

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import com.darkrockstudios.apps.fasttrack.data.Stages
import kotlin.math.min
import kotlin.time.Duration.Companion.hours

/**
 * Fasting Stages view
 */
@Composable
fun TimeLine(
	elapsedHours: Double,
	modifier: Modifier = Modifier
) {
	val padding = 16.dp
	val spacing = 18.dp
	val barSize = 16.dp
	val needleSize = 3.dp
	val needleRadius = 4.dp

	val gaugeColors = listOf(
		Color.White,
		Color.Green,
		Color.Yellow,
		Color.Red,
		Color.Magenta
	)

	val outlineColor = MaterialTheme.colorScheme.onBackground

	Canvas(
		modifier = modifier
			.fillMaxWidth()
			.height(padding + barSize)
	) {
		val lastPhase = Stages.phases.last()
		val lastPhaseHoursWeighted = lastPhase.hours * 1.5f

		val availableWidth = size.width - padding.toPx()
		val phaseWidth = (availableWidth / Stages.phases.size) - spacing.toPx()

		val curPhase = Stages.getCurrentPhase(elapsedHours.hours)

		// Draw the bubbles (phases)
		Stages.phases.forEachIndexed { index, phase ->
			val startX = (index * phaseWidth) + (index * spacing.toPx()) + padding.toPx()
			val startY = padding.toPx()

			// Current phase, thicket orange outline
			if (curPhase == phase) {
				// Outline
				drawLine(
					color = Color(0xFFE67E22),
					start = Offset(startX, startY),
					end = Offset(startX + phaseWidth, startY),
					strokeWidth = barSize.toPx(),
					cap = StrokeCap.Round
				)

				// Current phase - filled
				drawLine(
					color = gaugeColors[index],
					start = Offset(startX, startY),
					end = Offset(startX + phaseWidth, startY),
					strokeWidth = barSize.toPx() * 0.7f,
					cap = StrokeCap.Round
				)
			} else {
				// Other phases - thinner "onBackground" outline

				// Thinner outline
				drawLine(
					color = outlineColor,
					start = Offset(startX, startY),
					end = Offset(startX + phaseWidth, startY),
					strokeWidth = barSize.toPx(),
					cap = StrokeCap.Round
				)

				// Phase color
				drawLine(
					color = gaugeColors[index],
					start = Offset(startX, startY),
					end = Offset(startX + phaseWidth, startY),
					strokeWidth = barSize.toPx() * 0.8f, // Slightly thinner
					cap = StrokeCap.Round
				)
			}
		}

		// Draw the needle
		if (elapsedHours > 0) {
			val curPhaseIndex = Stages.phases.indexOf(curPhase)
			val nextPhaseHours: Float = if (curPhaseIndex + 1 < Stages.phases.size) {
				val nextPhase = Stages.phases[curPhaseIndex + 1]
				nextPhase.hours.toFloat()
			} else {
				lastPhaseHoursWeighted
			}
			val phaseLength = (nextPhaseHours - curPhase.hours)
			val timeIntoPhase = elapsedHours - curPhase.hours

			val percent = min(timeIntoPhase / phaseLength, 1.0)

			val halfPadding = padding.toPx() / 2f

			val startX = (curPhaseIndex * phaseWidth) + (curPhaseIndex * spacing.toPx()) + padding.toPx()
			val x = (startX + (phaseWidth * percent)).toFloat()

			// Draw needle line
			drawLine(
				color = Color.DarkGray,
				start = Offset(x, halfPadding),
				end = Offset(x, barSize.toPx() + halfPadding),
				strokeWidth = needleSize.toPx(),
				cap = StrokeCap.Square
			)

			// Draw needle circle
			drawCircle(
				color = Color.DarkGray,
				radius = needleRadius.toPx(),
				center = Offset(x, barSize.toPx() + halfPadding + needleRadius.toPx())
			)
		}
	}
}
