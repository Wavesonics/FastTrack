package com.darkrockstudios.apps.fasttrack.utils

import androidx.compose.ui.graphics.Color
import com.darkrockstudios.apps.fasttrack.data.*

val gaugeColors = listOf(
	Color.White,
	Color.Green,
	Color.Yellow,
	Color.Red,
	Color.Magenta
)

fun getColorFor(phase: Phase): Color {
	return when (phase) {
		is GlucosePhase -> gaugeColors[0]
		is FatBurningPhase -> gaugeColors[1]
		is KetosisPhase -> gaugeColors[2]
		is AutophagyPhase -> gaugeColors[3]
		is OptimalAutophagyPhase -> gaugeColors[4]
	}
}