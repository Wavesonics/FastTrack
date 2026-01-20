package com.darkrockstudios.apps.fasttrack.screens.fasting

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
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
    val spacing = 4.dp // Reduced spacing since rhombuses connect
    val barSize = 16.dp
    val needleSize = 3.dp
    val needleRadius = 4.dp
    val slantOffset = 8.dp // How much the rhombus leans to the right

    val outlineColor = MaterialTheme.colorScheme.onBackground

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(padding + barSize)
            .pointerInput(Stages.phases) {
                detectTapGestures { offset ->
                    val paddingPx = padding.toPx()
                    val spacingPx = spacing.toPx()
                    val barSizePx = barSize.toPx()
                    val availableWidth = size.width - paddingPx
                    val phaseWidth = (availableWidth / Stages.phases.size) - spacingPx
                    val startY = paddingPx
                    val yOk = abs(offset.y - startY) <= (barSizePx / 2f)
                    if (yOk) {
                        Stages.phases.forEachIndexed { index, phase ->
                            val startX = (index * phaseWidth) + (index * spacingPx) + paddingPx
                            val endX = startX + phaseWidth
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

        val availableWidth = size.width - padding.toPx()
        val phaseWidth = (availableWidth / Stages.phases.size) - spacing.toPx()

        val curPhase = Stages.getCurrentPhase(elapsedHours.hours)

        val slantOffsetPx = slantOffset.toPx()
        val barSizePx = barSize.toPx()

        // Draw the rhombuses (phases)
        Stages.phases.forEachIndexed { index, phase ->
            val startX = (index * phaseWidth) + (index * spacing.toPx()) + padding.toPx()
            val startY = padding.toPx()

            // Create rhombus path (parallelogram leaning right)
            val rhombusPath = Path().apply {
                // Top left
                moveTo(startX + slantOffsetPx, startY - barSizePx / 2)
                // Top right
                lineTo(startX + phaseWidth + slantOffsetPx, startY - barSizePx / 2)
                // Bottom right
                lineTo(startX + phaseWidth, startY + barSizePx / 2)
                // Bottom left
                lineTo(startX, startY + barSizePx / 2)
                close()
            }

            // Current phase, thicker orange outline
            if (curPhase == phase) {
                // Outline
                drawPath(
                    path = rhombusPath,
                    color = Color(0xFFE67E22),
                    style = Stroke(width = 3.dp.toPx())
                )

                // Current phase - filled
                drawPath(
                    path = rhombusPath,
                    color = gaugeColors[index],
                    style = Fill
                )
            } else {
                // Other phases - thinner "onBackground" outline

                // Outline
                drawPath(
                    path = rhombusPath,
                    color = outlineColor,
                    style = Stroke(width = 2.dp.toPx())
                )

                // Phase color
                drawPath(
                    path = rhombusPath,
                    color = gaugeColors[index],
                    style = Fill
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