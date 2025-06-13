package com.darkrockstudios.apps.fasttrack.screens.log

import com.darkrockstudios.apps.fasttrack.data.log.FastingLogEntry
import kotlinx.coroutines.flow.StateFlow
import kotlinx.datetime.LocalDateTime
import kotlin.time.Duration

interface ILogViewModel {
	data class LogUiState(
		val entries: List<FastingLogEntry> = emptyList(),
		val totalKetosisHours: Int = 0,
		val totalAutophagyHours: Int = 0,
		val showManualAddDialog: Boolean = false
	)

	val uiState: StateFlow<LogUiState>

	fun deleteFast(item: FastingLogEntry)
	fun showManualAddDialog()
	fun hideManualAddDialog()
	fun addEntry(startTime: LocalDateTime, length: Duration)
	fun loadEntries()
}
