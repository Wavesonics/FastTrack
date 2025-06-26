package com.darkrockstudios.apps.fasttrack.screens.fasting

import kotlinx.coroutines.flow.StateFlow
import kotlinx.datetime.Instant
import kotlin.time.Duration

interface IFastingViewModel {
	enum class StageState {
		NotStarted, StartedInactive, StartedActive
	}

	data class FastingUiState(
		val isFasting: Boolean = false,
		val elapsedTime: Duration? = null,
		val stageTitle: String = "",
		val stageDescription: String = "",
		val energyMode: String = "",
		val fatBurnTime: String = "--:--:--",
		val ketosisTime: String = "--:--:--",
		val autophagyTime: String = "--:--:--",
		val fatBurnStageState: StageState = StageState.NotStarted,
		val ketosisStageState: StageState = StageState.NotStarted,
		val autophagyStageState: StageState = StageState.NotStarted,
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
