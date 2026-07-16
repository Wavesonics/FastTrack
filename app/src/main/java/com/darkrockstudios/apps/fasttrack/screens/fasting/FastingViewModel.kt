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
		// One read of the repository and one state emission per tick: separate
		// emissions here each trigger their own recomposition of the dial + rows.
		val isFasting = repository.isFasting()
		val fastStart = repository.getFastStart()
		val fastEnd = repository.getFastEnd()

		_uiState.update { state ->
			if (fastStart != null) {
				val elapsedTime = fastEnd?.minus(fastStart) ?: clock.now().minus(fastStart)

				// Stage copy is shown only while a fast is actually running.
				val stage = if (isFasting) computeStage(elapsedTime) else EMPTY_STAGE

				val fatBurn = getPhaseTimeAndStageState(Stages.PHASE_FAT_BURN, elapsedTime)
				val ketosis = getPhaseTimeAndStageState(Stages.PHASE_KETOSIS, elapsedTime)
				val autophagy = getPhaseTimeAndStageState(Stages.PHASE_AUTOPHAGY, elapsedTime)

				state.copy(
					isFasting = isFasting,
					elapsedTime = elapsedTime,
					elapsedHours = elapsedTime.inWholeHours.toDouble(),
					fastStartTime = fastStart,
					lastFastEndTime = fastEnd,
					timerText = formatDuration(appContext, elapsedTime),
					milliseconds = "",
					stageTitle = stage.title,
					stageDescription = stage.description,
					energyMode = stage.energyMode,
					fatBurnTime = fatBurn.first,
					fatBurnStageState = fatBurn.second,
					ketosisTime = ketosis.first,
					ketosisStageState = ketosis.second,
					autophagyTime = autophagy.first,
					autophagyStageState = autophagy.second,
				)
			} else {
				state.copy(
					isFasting = isFasting,
					elapsedTime = null,
					elapsedHours = 0.0,
					fastStartTime = null,
					lastFastEndTime = fastEnd,
					stageTitle = "",
					stageDescription = "",
					energyMode = "",
				)
			}
		}
	}

	private data class StageStrings(val title: String, val description: String, val energyMode: String)

	private val EMPTY_STAGE = StageStrings("", "", "")

	private fun computeStage(elapsedTime: Duration): StageStrings {
		val elapsedHours = elapsedTime.inWholeHours.toInt()

		var stageIndex = Stages.stage.indexOfLast { it.hours <= elapsedHours }
		if (stageIndex < 0) stageIndex = 0
		val stage = Stages.stage[stageIndex]

		val curPhase = Stages.getCurrentPhase(elapsedTime)
		val energyMode = appContext.getString(
			R.string.fasting_energy_mode,
			appContext.getString(
				if (curPhase.fatBurning) R.string.fasting_energy_mode_fat
				else R.string.fasting_energy_mode_glucose
			)
		)

		return StageStrings(
			title = appContext.getString(stage.title),
			description = appContext.getString(stage.description),
			energyMode = energyMode,
		)
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
