package com.darkrockstudios.apps.fasttrack.data

import androidx.annotation.StringRes
import com.darkrockstudios.apps.fasttrack.R
import kotlin.time.Duration
import kotlin.time.DurationUnit


object Stages {
	val PHASE_GLUCOSE = GlucosePhase()
	val PHASE_FAT_BURN = FatBurningPhase()
	val PHASE_KETOSIS = KetosisPhase()
	val PHASE_AUTOPHAGY = AutophagyPhase()
	val PHASE_OPTIMAL_AUTOPHAGY = OptimalAutophagyPhase()
	val END_TIME = 72f

	val phases = arrayOf(PHASE_GLUCOSE, PHASE_FAT_BURN, PHASE_KETOSIS, PHASE_AUTOPHAGY, PHASE_OPTIMAL_AUTOPHAGY)

	fun getCurrentPhase(elapsedTime: Duration): Phase {
		return phases.findLast { elapsedTime.toDouble(DurationUnit.HOURS) >= it.hours } ?: PHASE_GLUCOSE
	}

	val stage = arrayOf(
		Stage(hours = 12, title = R.string.stage_fasting_title, description = R.string.stage_fasting_description),
		Stage(
			hours = 16,
			title = R.string.stage_fat_burning_title,
			description = R.string.stage_fat_burning_description
		),
		Stage(
			hours = 18,
			title = R.string.stage_fat_fasting_sweet_spot_title,
			description = R.string.stage_fat_fasting_sweet_spot_description
		),
		Stage(
			hours = 20,
			title = R.string.stage_fat_peak_fat_burning_title,
			description = R.string.stage_fat_peak_fat_burning_description
		),
		Stage(hours = 24, title = R.string.stage_autophagy_title, description = R.string.stage_autophagy_description),
		Stage(
			hours = 48,
			title = R.string.stage_optimal_autophagy_title,
			description = R.string.stage_optimal_autophagy_description
		),
	)
}

fun phaseForStage(stage: Stage): Phase {
	return when {
		stage.hours >= 48 -> Stages.PHASE_OPTIMAL_AUTOPHAGY
		stage.hours >= 24 -> Stages.PHASE_AUTOPHAGY
		stage.hours >= 20 -> Stages.PHASE_KETOSIS
		stage.hours >= 16 -> Stages.PHASE_FAT_BURN
		else -> Stages.PHASE_GLUCOSE
	}
}

sealed class Phase(
	val hours: Int,
	@StringRes val title: Int,
	val fatBurning: Boolean,
	val ketosis: Boolean,
	val autophagy: Boolean
)

class GlucosePhase : Phase(0, R.string.phase_glucose, false, false, false)
class FatBurningPhase : Phase(16, R.string.phase_fat_burning, true, false, false)
class KetosisPhase : Phase(20, R.string.phase_ketosis, true, true, false)
class AutophagyPhase : Phase(24, R.string.phase_autophagy, true, true, true)
class OptimalAutophagyPhase : Phase(48, R.string.phase_optimal_autophagy, true, true, true)

data class Stage(val hours: Int, @StringRes val title: Int, @StringRes val description: Int)