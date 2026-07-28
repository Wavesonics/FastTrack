package com.darkrockstudios.apps.fasttrack.screens.log

import com.darkrockstudios.apps.fasttrack.data.log.FastingLogEntry
import com.darkrockstudios.apps.fasttrack.data.settings.LogViewMode
import kotlinx.coroutines.flow.StateFlow
import kotlinx.datetime.LocalDate
import kotlin.time.Duration

interface ILogViewModel {
	data class LogUiState(
		val entries: List<FastingLogEntry> = emptyList(),
		val totalKetosisHours: Int = 0,
		val totalAutophagyHours: Int = 0,
		val totalFasts: Int = 0,
		val totalFastedDuration: Duration = Duration.ZERO,
		val longestFastDuration: Duration = Duration.ZERO,
		val showManualAddDialog: Boolean = false,
		val entryToEdit: FastingLogEntry? = null,
		val viewMode: LogViewMode = LogViewMode.LIST,
		val selectedDate: LocalDate? = null,
		val showClearAllConfirmation: Boolean = false,
		// An empty (past/today) calendar day awaiting "add a fast here?" confirmation.
		val emptyDayToAdd: LocalDate? = null,
		// Date to preselect in the Manual Add picker (e.g. from an empty calendar day).
		val manualAddInitialDate: LocalDate? = null,
	)

	val uiState: StateFlow<LogUiState>

	fun deleteFast(item: FastingLogEntry)
	fun showManualAddDialog()
	fun showEditDialog(entry: FastingLogEntry)
	fun hideManualAddDialog()
	fun loadEntries()
	fun setViewMode(mode: LogViewMode)
	fun selectDate(date: LocalDate?)
	fun requestClearAll()
	fun dismissClearAll()
	fun clearAll()
	fun requestAddForDate(date: LocalDate)
	fun dismissAddForDate()
	fun confirmAddForDate()
}
