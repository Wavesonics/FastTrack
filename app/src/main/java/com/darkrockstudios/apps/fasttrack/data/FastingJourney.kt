package com.darkrockstudios.apps.fasttrack.data

import androidx.annotation.StringRes
import com.darkrockstudios.apps.fasttrack.R

/**
 * One stage of the fasting journey as shown on the dial.
 * The canonical stage text lives in fasting-stages.md at the repo root;
 */
data class JourneyStage(
	val startHours: Int,
	/** null means the stage is open-ended (the final stage) */
	val endHours: Int?,
	val emoji: String,
	@StringRes val title: Int,
	@StringRes val body: Int,
)

object FastingJourney {
	val stages = listOf(
		JourneyStage(0, 2, "🌅", R.string.journey_stage_0_title, R.string.journey_stage_0_body),
		JourneyStage(2, 5, "🌊", R.string.journey_stage_1_title, R.string.journey_stage_1_body),
		JourneyStage(5, 8, "⚖️", R.string.journey_stage_2_title, R.string.journey_stage_2_body),
		JourneyStage(8, 12, "🧪", R.string.journey_stage_3_title, R.string.journey_stage_3_body),
		JourneyStage(12, 18, "🔥", R.string.journey_stage_4_title, R.string.journey_stage_4_body),
		JourneyStage(18, 24, "💎", R.string.journey_stage_5_title, R.string.journey_stage_5_body),
		JourneyStage(24, 48, "♻️", R.string.journey_stage_6_title, R.string.journey_stage_6_body),
		JourneyStage(48, 54, "💪", R.string.journey_stage_7_title, R.string.journey_stage_7_body),
		JourneyStage(54, 72, "🎯", R.string.journey_stage_8_title, R.string.journey_stage_8_body),
		JourneyStage(72, null, "🌱", R.string.journey_stage_9_title, R.string.journey_stage_9_body),
	)

	fun indexFor(elapsedHours: Double): Int =
		stages.indexOfLast { elapsedHours >= it.startHours }.coerceAtLeast(0)

	fun stageFor(elapsedHours: Double): JourneyStage = stages[indexFor(elapsedHours)]
}
