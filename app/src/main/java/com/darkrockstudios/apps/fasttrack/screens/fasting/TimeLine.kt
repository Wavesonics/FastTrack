package com.darkrockstudios.apps.fasttrack.screens.fasting

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.darkrockstudios.apps.fasttrack.data.Stages
import kotlin.math.min
import kotlin.time.Duration.Companion.hours
import kotlin.time.ExperimentalTime

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

			if (curPhase == phase) {
				// Current phase - filled
				drawLine(
					color = gaugeColors[index],
					start = Offset(startX, startY),
					end = Offset(startX + phaseWidth, startY),
					strokeWidth = barSize.toPx(),
					cap = StrokeCap.Round
				)
			} else {
				// Other phases - outlined
				// For outlined phases, we'll simulate a stroke by drawing two lines:
				// 1. A thicker line with the background color
				// 2. A thinner line with the phase color

				// First, draw a thicker line with the background color (or a very light version of the phase color)
				drawLine(
					color = Color.White, // Use background color or very light version of phase color
					start = Offset(startX, startY),
					end = Offset(startX + phaseWidth, startY),
					strokeWidth = barSize.toPx(),
					cap = StrokeCap.Round
				)

				// Then, draw a thinner line with the phase color around the edges
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
