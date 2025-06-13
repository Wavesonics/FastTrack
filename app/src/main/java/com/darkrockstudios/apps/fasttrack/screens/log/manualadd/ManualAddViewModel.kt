package com.darkrockstudios.apps.fasttrack.screens.log.manualadd

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

class ManualAddViewModel(
	private val repository: FastingLogRepository
) : ViewModel(), IManualAddViewModel {

	private val _uiState = MutableStateFlow(IManualAddViewModel.ManualAddUiState())
	override val uiState: StateFlow<IManualAddViewModel.ManualAddUiState> = _uiState.asStateFlow()

	override fun onDateSelected(dateTimestampMs: Long) {
		val instant = Instant.fromEpochMilliseconds(dateTimestampMs)
		val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())

		val selectedDate = LocalDate(
			year = localDateTime.year,
			month = localDateTime.month,
			dayOfMonth = localDateTime.dayOfMonth
		)

		_uiState.update { currentState ->
			currentState.copy(
				selectedDate = selectedDate,
				currentStep = 1
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
					currentStep = 2
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

	override fun onAddEntry(): Boolean {
		val currentState = _uiState.value
		val selectedDateTime = currentState.selectedDateTime
		val lengthHours = currentState.lengthHours.toLongOrNull() ?: 0

		return if (selectedDateTime != null && lengthHours > 0) {
			val length = lengthHours.hours
			viewModelScope.launch(Dispatchers.IO) {
				repository.addLogEntry(selectedDateTime, length)
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
}
