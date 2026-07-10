package com.darkrockstudios.apps.fasttrack.screens.fasting

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.darkrockstudios.apps.fasttrack.AlertService
import com.darkrockstudios.apps.fasttrack.FastingNotificationManager
import com.darkrockstudios.apps.fasttrack.R
import com.darkrockstudios.apps.fasttrack.data.Phase
import com.darkrockstudios.apps.fasttrack.data.Stages
import com.darkrockstudios.apps.fasttrack.data.activefast.ActiveFastRepository
import com.darkrockstudios.apps.fasttrack.data.log.FastingLogRepository
import com.darkrockstudios.apps.fasttrack.data.settings.SettingsDatasource
import com.darkrockstudios.apps.fasttrack.utils.formatDuration
import com.darkrockstudios.apps.fasttrack.widget.WidgetUpdater
import io.github.aakira.napier.Napier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.DurationUnit
import kotlin.time.Instant

class FastingViewModel(
	private val appContext: Context,
	private val repository: ActiveFastRepository,
	private val logRepository: FastingLogRepository,
	private val settingsDatasource: SettingsDatasource,
	private val clock: Clock,
) : ViewModel(), IFastingViewModel {

	private val _uiState = MutableStateFlow(
		IFastingViewModel.FastingUiState(
			isFasting = repository.isFasting(),
			showGradientBackground = settingsDatasource.getShowFancyBackground(),
		)
	)
	override val uiState: StateFlow<IFastingViewModel.FastingUiState> = _uiState.asStateFlow()

	override fun onCreate() {
		viewModelScope.launch {
			settingsDatasource.showFancyBackgroundFlow().collect { enabled ->
				_uiState.update { state -> state.copy(showGradientBackground = enabled) }
			}
		}

		viewModelScope.launch {
			settingsDatasource.phaseVisibilityFlow().collect { v ->
				_uiState.update { state ->
					state.copy(
						showFatBurn = v.fatBurn,
						showKetosis = v.ketosis,
						showAutophagy = v.autophagy,
						phaseAutoMode = v.autoMode,
					)
				}
			}
		}

		updateUi()
		setupFastingNotification()
	}

	override fun updateUi() {
		updateFastingState()
		updateTimer()
		updateStage()
	}

	private fun updateFastingState() {
		val isFasting = repository.isFasting()
		_uiState.update { it.copy(isFasting = isFasting) }
	}

	private fun updateStage() {
		val fastStart = repository.getFastStart()
		var stageTitle = ""
		var stageDescription = ""
		var energyMode = ""

		if (repository.isFasting() && fastStart != null) {
			val elapsedTime = clock.now().minus(fastStart)
			val elapsedHours = elapsedTime.inWholeHours.toInt()

			var stageIndex = Stages.stage.indexOfLast { it.hours <= elapsedHours }
			if (stageIndex < 0) {
				stageIndex = 0
			}

			val stage = Stages.stage[stageIndex]

			val curPhase = Stages.getCurrentPhase(elapsedTime)
			energyMode = if (curPhase.fatBurning) {
				appContext.getString(
					R.string.fasting_energy_mode,
					appContext.getString(R.string.fasting_energy_mode_fat)
				)
			} else {
				appContext.getString(
					R.string.fasting_energy_mode,
					appContext.getString(R.string.fasting_energy_mode_glucose)
				)
			}

			stageTitle = appContext.getString(stage.title)
			stageDescription = appContext.getString(stage.description)
		}

		_uiState.update {
			it.copy(
				stageTitle = stageTitle,
				stageDescription = stageDescription,
				energyMode = energyMode
			)
		}
	}

	private fun updateTimer() {
		val fastStart = repository.getFastStart()
		val fastEnd = repository.getFastEnd()

		if (fastStart != null) {
			val elapsedTime = fastEnd?.minus(fastStart) ?: clock.now().minus(fastStart)

			updateTimerView(elapsedTime)
			updatePhases(elapsedTime)

			_uiState.update {
				it.copy(
					elapsedTime = elapsedTime,
					fastStartTime = fastStart,
					lastFastEndTime = fastEnd,
				)
			}
		} else {
			_uiState.update {
				it.copy(
					elapsedTime = null,
					fastStartTime = null,
					lastFastEndTime = repository.getFastEnd(),
					elapsedHours = 0.0
				)
			}
		}
	}

	private fun updateTimerView(elapsedTime: Duration) {
		_uiState.update {
			it.copy(
				timerText = formatDuration(appContext, elapsedTime),
				milliseconds = ""
			)
		}
	}

	private fun updatePhases(elapsedTime: Duration) {
		_uiState.update { it.copy(elapsedHours = elapsedTime.inWholeHours.toDouble()) }

		val fatBurnTimeAndState = getPhaseTimeAndStageState(Stages.PHASE_FAT_BURN, elapsedTime)
		val ketosisTimeAndState = getPhaseTimeAndStageState(Stages.PHASE_KETOSIS, elapsedTime)
		val autophagyTimeAndState = getPhaseTimeAndStageState(Stages.PHASE_AUTOPHAGY, elapsedTime)

		_uiState.update {
			it.copy(
				fatBurnTime = fatBurnTimeAndState.first,
				fatBurnStageState = fatBurnTimeAndState.second,
				ketosisTime = ketosisTimeAndState.first,
				ketosisStageState = ketosisTimeAndState.second,
				autophagyTime = autophagyTimeAndState.first,
				autophagyStageState = autophagyTimeAndState.second
			)
		}
	}

	private fun getPhaseTimeAndStageState(
		phase: Phase,
		elapsedTime: Duration
	): Pair<String, IFastingViewModel.StageState> {
		val phaseHours = phase.hours
		val timeText: String
		val stageState: IFastingViewModel.StageState

		if (elapsedTime.toDouble(DurationUnit.HOURS) > phaseHours) {
			// The phase is underway: how long you've been in it
			timeText = formatDuration(appContext, elapsedTime.minus(phaseHours.hours))
			stageState = IFastingViewModel.StageState.StartedActive
		} else {
			// The phase is ahead: frame it as anticipation, not deficit
			val timeUntil = phaseHours.hours.minus(elapsedTime)
			timeText = appContext.getString(R.string.phase_time_until, formatDuration(appContext, timeUntil))
			stageState = IFastingViewModel.StageState.StartedInactive
		}

		return Pair(timeText, stageState)
	}

	override fun startFast(timeStartedMills: Instant?) {
		if (!repository.isFasting()) {
			repository.startFast(timeStartedMills)

			updateUi()
			setupAlerts()
			setupFastingNotification()
			updateWidgets()

			Napier.i("Started fast!")
		} else {
			Napier.w("Cannot start fast with one in progress")
		}
	}

	override fun endFast(timeEnded: Instant?, notes: String) {
		if (repository.isFasting()) {
			repository.endFast(timeEnded)

			viewModelScope.launch(Dispatchers.IO) {
				saveFastToLog(repository.getFastStart(), repository.getFastEnd(), notes)
			}

			Napier.i("Fast ended!")

			updateUi()
			setupAlerts()
			setupFastingNotification()
			updateWidgets()
		} else {
			Napier.w("Cannot end fast, there is none started")
		}
	}

	override fun setupAlerts() {
		val shouldAlert = settingsDatasource.getFastingAlerts()

		if (repository.isFasting()) {
			if (shouldAlert) {
				val elapsedTime = repository.getElapsedFastTime()
				AlertService.scheduleAlerts(elapsedTime, appContext)
			}
			// User doesn't want notifications
			else {
				AlertService.cancelAlerts(appContext)
			}
		}
		// No notifications if we aren't fasting
		else {
			AlertService.cancelAlerts(appContext)
		}
	}

	private fun setupFastingNotification() {
		val shouldShowNotification = settingsDatasource.getShowFastingNotification()

		if (repository.isFasting() && shouldShowNotification) {
			val elapsedTime = repository.getElapsedFastTime()
			FastingNotificationManager.postFastingNotification(appContext, elapsedTime)
			AlertService.scheduleHourlyUpdate(appContext)
		} else {
			FastingNotificationManager.cancelFastingNotification(appContext)
			AlertService.cancelHourlyUpdates(appContext)
		}
	}

	override fun debugIncreaseFastingTimeByOneHour() {
		if (repository.isFasting()) {

			val currentStartTime = repository.getFastStart()
			if (currentStartTime != null) {
				val newStartTime = currentStartTime - 1.hours

				repository.debugOverrideFastStart(newStartTime)

				updateUi()
				updateWidgets()
				setupFastingNotification()

				Napier.d("Debug: Increased fasting time by 1 hour")
			}
		} else {
			Napier.d("Debug: Cannot increase fasting time when not fasting")
		}
	}

	private fun updateWidgets() {
		WidgetUpdater.updateWidgets(appContext)
	}

	private suspend fun saveFastToLog(startTime: Instant?, endTime: Instant?, notes: String) =
		withContext(Dispatchers.Default) {
			if (startTime != null && endTime != null) {
				logRepository.logFast(startTime, endTime, notes)
			} else {
				Napier.e("No start time when ending fast!")
			}
		}
}
