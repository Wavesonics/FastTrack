package com.darkrockstudios.apps.fasttrack.screens.log

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.darkrockstudios.apps.fasttrack.data.Stages
import com.darkrockstudios.apps.fasttrack.data.log.FastingLogEntry
import com.darkrockstudios.apps.fasttrack.data.log.FastingLogRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime
import kotlin.math.roundToInt
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime

@ExperimentalTime
class LogViewModel(
	private val repository: FastingLogRepository,
) : ViewModel(), ILogViewModel {

	private val _uiState = MutableStateFlow(ILogViewModel.LogUiState())
	override val uiState: StateFlow<ILogViewModel.LogUiState> = _uiState.asStateFlow()

	override fun loadEntries() {
		viewModelScope.launch {
			repository.loadAll().collect { entries ->
				updateEntries(entries)
			}
		}
	}

	private fun updateEntries(entries: List<FastingLogEntry>) {
		val totalKetosisHours = entries.sumOf { calculateKetosis(it) }.roundToInt()
		val totalAutophagyHours = entries.sumOf { calculateAutophagy(it) }.roundToInt()

		_uiState.update { currentState ->
			currentState.copy(
				entries = entries.sortedByDescending { it.start },
				totalKetosisHours = totalKetosisHours,
				totalAutophagyHours = totalAutophagyHours
			)
		}
	}

	private fun calculateKetosis(entry: FastingLogEntry): Double {
		val ketosisStart = Stages.PHASE_KETOSIS.hours.toDouble()
		val lenHours = entry.length.toDouble(DurationUnit.HOURS)
		return if (lenHours > ketosisStart) {
			lenHours - ketosisStart
		} else {
			0.0
		}
	}

	private fun calculateAutophagy(entry: FastingLogEntry): Double {
		val autophagyStart = Stages.PHASE_AUTOPHAGY.hours.toDouble()
		val lenHours = entry.length.toDouble(DurationUnit.HOURS)
		return if (lenHours > autophagyStart) {
			lenHours - autophagyStart
		} else {
			0.0
		}
	}

	override fun deleteFast(item: FastingLogEntry) {
		viewModelScope.launch(Dispatchers.IO) {
			if (repository.delete(item)) {
				Log.w("LogViewModel", "Failed to delete Fast: $item")
			}
		}
	}

	override fun showManualAddDialog() {
		_uiState.update { it.copy(showManualAddDialog = true) }
	}

	override fun hideManualAddDialog() {
		_uiState.update { it.copy(showManualAddDialog = false) }
	}

	override fun addEntry(startTime: LocalDateTime, length: Duration) {
		viewModelScope.launch(Dispatchers.IO) {
			repository.addLogEntry(startTime, length)
			hideManualAddDialog()
		}
	}
}
