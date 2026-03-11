package com.darkrockstudios.apps.fasttrack.screens.fasting

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.darkrockstudios.apps.fasttrack.data.Phase
import com.darkrockstudios.apps.fasttrack.data.Stages
import kotlin.math.abs
import kotlin.math.min
import kotlin.time.Duration.Companion.hours

val gaugeColors = listOf(
	Color.White,
	Color.Green,
	Color.Yellow,
	Color.Red,
	Color.Magenta
)

/**
 * Fasting Stages view
 */
@Composable
fun TimeLine(
	elapsedHours: Double,
	modifier: Modifier = Modifier,
	onPhaseClick: (Phase) -> Unit = {}
) {
	val padding = 16.dp
	val spacing = 4.dp
	val barSize = 16.dp
	val needleSize = 3.dp
	val needleRadius = 4.dp
	val slantOffset = 8.dp

	val outlineColor = MaterialTheme.colorScheme.onBackground
	
	val curPhase = Stages.getCurrentPhase(elapsedHours.hours)
	
	// Continuous blink animation for current phase
	val infiniteTransition = rememberInfiniteTransition(label = "phase_blink")
	val blinkProgress by infiniteTransition.animateFloat(
		initialValue = 0f,
		targetValue = 1f,
		animationSpec = infiniteRepeatable(
			animation = tween(durationMillis = 500),
			repeatMode = RepeatMode.Reverse
		),
		label = "blink_progress"
	)

	Canvas(
		modifier = modifier
			.fillMaxWidth()
			.height(padding + barSize)
			.pointerInput(curPhase) {
				detectTapGestures { offset ->
					val paddingPx = padding.toPx()
					val spacingPx = spacing.toPx()
					val barSizePx = barSize.toPx()
					val slantOffsetPx = slantOffset.toPx()
					
					val phaseWidth = (size.width - (2 * paddingPx) - (Stages.phases.size - 1) * spacingPx) / Stages.phases.size
					val totalWidth = (Stages.phases.size * phaseWidth) + ((Stages.phases.size - 1) * spacingPx) + slantOffsetPx
					val startOffset = (size.width - totalWidth) / 2f
					
					val startY = paddingPx
					val yOk = abs(offset.y - startY) <= (barSizePx / 2f)
					if (yOk) {
						Stages.phases.forEachIndexed { index, phase ->
							val startX = startOffset + (index * phaseWidth) + (index * spacingPx)
							val endX = startX + phaseWidth + slantOffsetPx
							if (offset.x in startX..endX) {
								onPhaseClick(phase)
								return@detectTapGestures
							}
						}
					}
				}
			}
	) {
		val lastPhase = Stages.phases.last()
		val lastPhaseHoursWeighted = lastPhase.hours * 1.5f

		val slantOffsetPx = slantOffset.toPx()
		val barSizePx = barSize.toPx()
		
		val phaseWidth = (size.width - (2 * padding.toPx()) - (Stages.phases.size - 1) * spacing.toPx()) / Stages.phases.size
		val totalWidth = (Stages.phases.size * phaseWidth) + ((Stages.phases.size - 1) * spacing.toPx()) + slantOffsetPx
		
		val startOffset = (size.width - totalWidth) / 2f

		Stages.phases.forEachIndexed { index, phase ->
			val startX = startOffset + (index * phaseWidth) + (index * spacing.toPx())
			val startY = padding.toPx()

			val rhombusPath = Path().apply {
				moveTo(startX + slantOffsetPx, startY - barSizePx / 2)
				lineTo(startX + phaseWidth + slantOffsetPx, startY - barSizePx / 2)
				lineTo(startX + phaseWidth, startY + barSizePx / 2)
				lineTo(startX, startY + barSizePx / 2)
				close()
			}

			if (curPhase == phase) {
				drawPath(
					path = rhombusPath,
					color = gaugeColors[index],
					style = Fill
				)
				
				// Continuously animate between orange and yellow
				val baseOutlineColor = Color(0xFFE67E22) // Orange
				val blinkColor = Color.Yellow
				val currentOutlineColor = lerp(baseOutlineColor, blinkColor, blinkProgress)
				
				drawPath(
					path = rhombusPath,
					color = currentOutlineColor,
					style = Stroke(width = 3.dp.toPx())
				)
			} else {
				drawPath(
					path = rhombusPath,
					color = gaugeColors[index],
					style = Fill
				)
				
				drawPath(
					path = rhombusPath,
					color = outlineColor,
					style = Stroke(width = 2.dp.toPx())
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

			val startX = startOffset + (curPhaseIndex * phaseWidth) + (curPhaseIndex * spacing.toPx())
			val x = (startX + (phaseWidth * percent)).toFloat()

			drawLine(
				color = Color.DarkGray,
				start = Offset(x, halfPadding),
				end = Offset(x, barSize.toPx() + halfPadding),
				strokeWidth = needleSize.toPx(),
				cap = StrokeCap.Square
			)

			drawCircle(
				color = Color.DarkGray,
				radius = needleRadius.toPx(),
				center = Offset(x, barSize.toPx() + halfPadding + needleRadius.toPx())
			)
		}
	}
}