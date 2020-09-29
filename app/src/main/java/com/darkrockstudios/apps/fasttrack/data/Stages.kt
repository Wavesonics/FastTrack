package com.darkrockstudios.apps.fasttrack.data

import kotlin.time.Duration
import kotlin.time.ExperimentalTime


@ExperimentalTime
object Stages
{
	val PHASE_GLUCOSE = GlucosePhase()
	val PHASE_FAT_BURN = FatBurningPhase()
	val PHASE_KETOSIS = KetosisPhase()
	val PHASE_AUTOPHAGY = AutophagyPhase()

	val phases = arrayOf(PHASE_GLUCOSE, PHASE_FAT_BURN, PHASE_KETOSIS, PHASE_AUTOPHAGY)

	fun getCurrentPhase(elapsedTime: Duration): Phase
	{
		return phases.findLast { elapsedTime.inHours >= it.hours } ?: PHASE_GLUCOSE
	}

	val stage = arrayOf(
			Stage(hours = 12, title = "Fasting", description = "Your body is consuming it's stores of glucose, keep it up!"),
			Stage(hours = 16, title = "Fat Burning", description = "Your glucose stores are depleted! Your body is now burning fat for energy. A workout in the next couple hours would be optimal."),
			Stage(hours = 18, title = "Fasting Sweet Spot", description = "You're in the fasting sweet spot, keep it up!"),
			Stage(hours = 20, title = "Peak Fat Burning", description = "Your body is burning fat rapidly now, and ketosis has begun."),
			Stage(hours = 24, title = "Autophagy", description = "Your body has begun atophagy!."),
			Stage(hours = 48, title = "Optimal Autophagy", description = "Atophagy is in full swing! Keep it up, between now and 72 hours autophagy will be very effective.."),
					   )
}

sealed class Phase(val hours: Int, val title: String, val fatBurning: Boolean, val ketosis: Boolean, val autophagy: Boolean)
class GlucosePhase: Phase(0, "Glucose Burning", false, false, false)
class FatBurningPhase: Phase(16, "Fat Burning", true, false, false)
class KetosisPhase: Phase(20, "Ketosis", true, true, false)
class AutophagyPhase: Phase(24, "Ketosis", true, true, true)

data class Stage(val hours: Int, val title: String, val description: String)