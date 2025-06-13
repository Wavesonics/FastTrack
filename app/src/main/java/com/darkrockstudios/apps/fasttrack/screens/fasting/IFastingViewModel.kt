package com.darkrockstudios.apps.fasttrack.screens.fasting

import android.graphics.Color
import androidx.annotation.ColorInt
import kotlinx.coroutines.flow.StateFlow
import kotlinx.datetime.Instant
import kotlin.time.Duration

interface IFastingViewModel {
	data class FastingUiState(
		val isFasting: Boolean = false,
		val elapsedTime: Duration? = null,
		val stageTitle: String = "",
		val stageDescription: String = "",
		val energyMode: String = "",
		val fatBurnTime: String = "--:--:--",
		val ketosisTime: String = "--:--:--",
		val autophagyTime: String = "--:--:--",
		@ColorInt val fatBurnTimeColor: Int = Color.WHITE,
		@ColorInt val ketosisTimeColor: Int = Color.WHITE,
		@ColorInt val autophagyTimeColor: Int = Color.WHITE,
		val elapsedHours: Double = 0.0,
		val milliseconds: String = "00",
		val timerText: String = "00:00:00",
		val alertsEnabled: Boolean = true
	)

	val uiState: StateFlow<FastingUiState>

	fun onCreate()
	fun updateUi()
	fun startFast(timeStartedMills: Instant? = null)
	fun endFast()
	fun setAlertsEnabled(enabled: Boolean)
	fun setupAlerts()
	fun debugIncreaseFastingTimeByOneHour()
}