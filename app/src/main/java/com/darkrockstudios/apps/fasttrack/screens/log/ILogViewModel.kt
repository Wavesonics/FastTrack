package com.darkrockstudios.apps.fasttrack.screens.log

import com.darkrockstudios.apps.fasttrack.data.log.FastingLogEntry
import kotlinx.coroutines.flow.StateFlow

interface ILogViewModel {
	data class LogUiState(
		val entries: List<FastingLogEntry> = emptyList(),
		val totalKetosisHours: Int = 0,
		val totalAutophagyHours: Int = 0,
		val showManualAddDialog: Boolean = false,
		val entryToEdit: FastingLogEntry? = null
	)

	val uiState: StateFlow<LogUiState>

	fun deleteFast(item: FastingLogEntry)
	fun showManualAddDialog()
	fun showEditDialog(entry: FastingLogEntry)
	fun hideManualAddDialog()
	fun loadEntries()
}
