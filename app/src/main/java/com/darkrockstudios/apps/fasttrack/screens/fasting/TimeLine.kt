package com.darkrockstudios.apps.fasttrack.screens.fasting

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
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

private val Padding = 16.dp
private val Spacing = 4.dp
private val BarSize = 16.dp
private val NeedleSize = 3.dp
private val NeedleRadius = 4.dp
private val SlantOffset = 8.dp

private val CurrentPhaseStroke = 3.dp
private val PhaseStroke = 2.dp

private val BlinkFromColor = Color(0xFFE67E22)
private val BlinkToColor = Color.Yellow

/**
 * Pixel layout of the phase bar. Shared by hit testing and drawing so tap targets always line up
 * with what is on screen.
 */
private class TimeLineGeometry(density: Density, size: Size) {
	val spacingPx = with(density) { Spacing.toPx() }
	val slantOffsetPx = with(density) { SlantOffset.toPx() }
	val barSizePx = with(density) { BarSize.toPx() }
	val centerY = with(density) { Padding.toPx() }

	val phaseWidth: Float
	private val startOffset: Float

	init {
		val paddingPx = with(density) { Padding.toPx() }
		val count = Stages.phases.size
		phaseWidth = (size.width - (2 * paddingPx) - (count - 1) * spacingPx) / count
		val totalWidth = (count * phaseWidth) + ((count - 1) * spacingPx) + slantOffsetPx
		startOffset = (size.width - totalWidth) / 2f
	}

	fun startX(index: Int): Float = startOffset + index * (phaseWidth + spacingPx)

	fun endX(index: Int): Float = startX(index) + phaseWidth + slantOffsetPx

	fun rhombusPath(index: Int): Path {
		val startX = startX(index)
		val halfBar = barSizePx / 2
		return Path().apply {
			moveTo(startX + slantOffsetPx, centerY - halfBar)
			lineTo(startX + phaseWidth + slantOffsetPx, centerY - halfBar)
			lineTo(startX + phaseWidth, centerY + halfBar)
			lineTo(startX, centerY + halfBar)
			close()
		}
	}
}

/**
 * Fasting Stages view
 */
@Composable
fun TimeLine(
	elapsedHours: Double,
	modifier: Modifier = Modifier,
	onPhaseClick: (Phase) -> Unit = {}
) {
	val outlineColor = MaterialTheme.colorScheme.onBackground
	val curPhase = Stages.getCurrentPhase(elapsedHours.hours)

	// Continuous blink animation for current phase
	val infiniteTransition = rememberInfiniteTransition(label = "phase_blink")
	val blinkProgress = infiniteTransition.animateFloat(
		initialValue = 0f,
		targetValue = 1f,
		animationSpec = infiniteRepeatable(
			animation = tween(durationMillis = 500),
			repeatMode = RepeatMode.Reverse
		),
		label = "blink_progress"
	)

	// Read through State inside the draw block so the per-frame blink and the ~100Hz timer updates
	// redraw without rebuilding the cached paths.
	val elapsedState = rememberUpdatedState(elapsedHours)
	val curPhaseState = rememberUpdatedState(curPhase)
	val outlineColorState = rememberUpdatedState(outlineColor)
	val onPhaseClickState = rememberUpdatedState(onPhaseClick)

	Spacer(
		modifier = modifier
			.fillMaxWidth()
			.height(Padding + BarSize)
			.pointerInput(Unit) {
				detectTapGestures { offset ->
					val geometry = TimeLineGeometry(this, size.toSize())
					if (abs(offset.y - geometry.centerY) > geometry.barSizePx / 2f) {
						return@detectTapGestures
					}
					Stages.phases.forEachIndexed { index, phase ->
						if (offset.x in geometry.startX(index)..geometry.endX(index)) {
							onPhaseClickState.value(phase)
							return@detectTapGestures
						}
					}
				}
			}
			.drawTimeLine(elapsedState, curPhaseState, outlineColorState, blinkProgress)
	)
}

private fun Modifier.drawTimeLine(
	elapsedHours: State<Double>,
	curPhase: State<Phase>,
	outlineColor: State<Color>,
	blinkProgress: State<Float>,
): Modifier = drawWithCache {
	val geometry = TimeLineGeometry(this, size)
	val paths = Stages.phases.indices.map(geometry::rhombusPath)

	onDrawBehind {
		val current = curPhase.value
		Stages.phases.forEachIndexed { index, phase ->
			drawPath(path = paths[index], color = gaugeColors[index], style = Fill)

			val isCurrent = phase == current
			drawPath(
				path = paths[index],
				color = if (isCurrent) {
					lerp(BlinkFromColor, BlinkToColor, blinkProgress.value)
				} else {
					outlineColor.value
				},
				style = Stroke(
					width = (if (isCurrent) CurrentPhaseStroke else PhaseStroke).toPx()
				)
			)
		}

		val elapsed = elapsedHours.value
		if (elapsed > 0) {
			drawNeedle(geometry, current, elapsed)
		}
	}
}

private fun DrawScope.drawNeedle(
	geometry: TimeLineGeometry,
	curPhase: Phase,
	elapsedHours: Double,
) {
	val curPhaseIndex = Stages.phases.indexOf(curPhase)
	val nextPhaseHours: Float = if (curPhaseIndex + 1 < Stages.phases.size) {
		Stages.phases[curPhaseIndex + 1].hours.toFloat()
	} else {
		Stages.phases.last().hours * 1.5f
	}
	val phaseLength = nextPhaseHours - curPhase.hours
	val percent = min((elapsedHours - curPhase.hours) / phaseLength, 1.0)

	val halfPadding = Padding.toPx() / 2f
	val x = (geometry.startX(curPhaseIndex) + (geometry.phaseWidth * percent)).toFloat()

	drawLine(
		color = Color.DarkGray,
		start = Offset(x, halfPadding),
		end = Offset(x, BarSize.toPx() + halfPadding),
		strokeWidth = NeedleSize.toPx(),
		cap = StrokeCap.Square
	)

	drawCircle(
		color = Color.DarkGray,
		radius = NeedleRadius.toPx(),
		center = Offset(x, BarSize.toPx() + halfPadding + NeedleRadius.toPx())
	)
}
