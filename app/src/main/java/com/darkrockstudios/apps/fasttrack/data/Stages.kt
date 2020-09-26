package com.darkrockstudios.apps.fasttrack.data

object Stages
{
	val stage = arrayOf(
			Stage(hours = 12, title = "Fasting", description = "Your body is consuming it's stores of glucose, keep it up!"),
			Stage(hours = 16, title = "Fat Burning", description = "Your glucose stores are depleted! Your body is now burning fat for energy. A workout in the next couple hours would be optimal."),
			Stage(hours = 18, title = "Fasting Sweet Spot", description = "You're in the fasting sweet spot, keep it up!"),
			Stage(hours = 20, title = "Peak Fat Burning", description = "Your body is burning fat rapidly now, and ketosis has begun."),
			Stage(hours = 24, title = "Autophagy", description = "Your body has begun atophagy!."),
			Stage(hours = 48, title = "Optimal Autophagy", description = "Atophagy is in full swing! Keep it up, between now and 72 hours autophagy will be very effective.."),
					   )
}

data class Stage(val hours: Int, val title: String, val description: String)