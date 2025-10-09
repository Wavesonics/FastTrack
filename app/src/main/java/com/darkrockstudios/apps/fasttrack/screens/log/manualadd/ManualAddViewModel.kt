package com.darkrockstudios.apps.fasttrack.screens.log.manualadd

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.darkrockstudios.apps.fasttrack.data.log.FastingLogEntry
import com.darkrockstudios.apps.fasttrack.data.log.FastingLogRepository
import io.github.aakira.napier.Napier.w
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.*
import kotlin.time.Duration.Companion.hours
import kotlin.time.DurationUnit

class ManualAddViewModel(
	private val repository: FastingLogRepository
) : ViewModel(), IManualAddViewModel {

	private val _uiState = MutableStateFlow(IManualAddViewModel.ManualAddUiState())
	override val uiState: StateFlow<IManualAddViewModel.ManualAddUiState> = _uiState.asStateFlow()

	override fun onDateSelected(dateTimestamp: Long) {
		val instant = Instant.fromEpochMilliseconds(dateTimestamp)
		val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())

		val selectedDate = LocalDate(
			year = localDateTime.year,
			month = localDateTime.month,
			dayOfMonth = localDateTime.dayOfMonth
		)

		_uiState.update { currentState ->
			currentState.copy(
				selectedDate = selectedDate,
				currentStep = ManualAddStep.StartTime
			)
		}
	}

	override fun onTimeSelected(hour: Int, minute: Int) {
		val currentState = _uiState.value
		currentState.selectedDate?.let { selectedDate ->
			val selectedDateTime = LocalDateTime(
				year = selectedDate.year,
				month = selectedDate.month,
				dayOfMonth = selectedDate.dayOfMonth,
				hour = hour,
				minute = minute,
				second = 0,
				nanosecond = 0
			)
			_uiState.update { state ->
				state.copy(
					selectedDateTime = selectedDateTime,
					currentStep = ManualAddStep.SetDuration
				)
			}
		}
	}

	override fun onLengthChanged(length: String) {
		try {
			val lengthValue = if (length.isNotEmpty()) length.toLong() else null
			val isCompleteButtonEnabled = _uiState.value.selectedDateTime != null &&
					length.isNotEmpty() &&
					(length.toLongOrNull() ?: 0) > 0

			_uiState.update { currentState ->
				currentState.copy(
					lengthHours = length,
					isCompleteButtonEnabled = isCompleteButtonEnabled
				)
			}
		} catch (e: NumberFormatException) {
			w("Failed to parse length input")
		}
	}

	override fun onEndDateTimeSelected(instant: Instant) {
		val currentState = _uiState.value
		val startDateTime = currentState.selectedDateTime

		if (startDateTime != null) {
			val startInstant = startDateTime.toInstant(TimeZone.currentSystemDefault())
			val durationMillis = instant.toEpochMilliseconds() - startInstant.toEpochMilliseconds()

			// Convert milliseconds to hours, rounded to nearest whole number
			val hours = (durationMillis / (1000.0 * 60 * 60)).toLong()

			// Only update if the end time is after the start time
			if (hours > 0) {
				onLengthChanged(hours.toString())
			}
		}
	}

	override fun onAddEntry(): Boolean {
		val currentState = _uiState.value
		val selectedDateTime = currentState.selectedDateTime
		val lengthHours = currentState.lengthHours.toLongOrNull() ?: 0
		val entryToEdit = currentState.entryToEdit

		return if (selectedDateTime != null && lengthHours > 0) {
			val length = lengthHours.hours
			viewModelScope.launch(Dispatchers.IO) {
				if (entryToEdit != null) {
					// Update existing entry
					repository.updateLogEntry(entryToEdit, selectedDateTime, length)
				} else {
					// Add new entry
					repository.addLogEntry(selectedDateTime, length)
				}
			}
			true
		} else {
			false
		}
	}

	override fun onDismiss() {
		// Reset state when dialog is dismissed
		_uiState.update {
			IManualAddViewModel.ManualAddUiState()
		}
	}

	override fun initializeWithEntry(entry: FastingLogEntry) {
		val selectedDate = LocalDate(
			year = entry.start.year,
			month = entry.start.month,
			dayOfMonth = entry.start.dayOfMonth
		)

		val lengthHours = entry.length.toDouble(DurationUnit.HOURS).toLong().toString()

		_uiState.update {
			it.copy(
				currentStep = ManualAddStep.SetDuration,
				selectedDate = selectedDate,
				selectedDateTime = entry.start,
				lengthHours = lengthHours,
				isCompleteButtonEnabled = true,
				entryToEdit = entry
			)
		}
	}

	override fun onPreviousStep() {
		val currentState = _uiState.value
		val previousStep = when (currentState.currentStep) {
			ManualAddStep.StartTime -> ManualAddStep.StartDate
			ManualAddStep.SetDuration -> ManualAddStep.StartTime
			else -> return // Already at first step
		}
		_uiState.update { it.copy(currentStep = previousStep) }
	}

	override fun goToStep(step: ManualAddStep) {
		_uiState.update { it.copy(currentStep = step) }
	}
}
