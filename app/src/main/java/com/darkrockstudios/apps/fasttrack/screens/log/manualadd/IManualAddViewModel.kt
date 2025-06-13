package com.darkrockstudios.apps.fasttrack.screens.log.manualadd

import kotlinx.coroutines.flow.StateFlow
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

interface IManualAddViewModel {
	data class ManualAddUiState(
		val currentStep: Int = 0,
		val selectedDate: LocalDate? = null,
		val selectedDateTime: LocalDateTime? = null,
		val lengthHours: String = "",
		val isNextButtonEnabled: Boolean = true,
		val isCompleteButtonEnabled: Boolean = false
	)

	val uiState: StateFlow<ManualAddUiState>

	fun onDateSelected(dateTimestamp: Long)
	fun onTimeSelected(hour: Int, minute: Int)
	fun onLengthChanged(length: String)
	fun onAddEntry(): Boolean
	fun onDismiss()
}