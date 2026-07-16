package com.darkrockstudios.apps.fasttrack.screens.log.manualadd

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.darkrockstudios.apps.fasttrack.data.log.FastingLogEntry
import com.darkrockstudios.apps.fasttrack.data.log.FastingLogRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

class ManualAddViewModel(
	private val repository: FastingLogRepository
) : ViewModel(), IManualAddViewModel {

	private val _uiState = MutableStateFlow(IManualAddViewModel.ManualAddUiState())
	override val uiState: StateFlow<IManualAddViewModel.ManualAddUiState> = _uiState.asStateFlow()

	// A precise length that must be saved verbatim, minutes and all, because the
	// duration field only shows whole hours. Set when editing an existing entry
	// and when the length is computed from an end date/time; cleared only when
	// the user types directly into the hours field (an explicit whole-hour value).
	private var exactLength: Duration? = null

	override fun onDateSelected(dateTimestamp: Long) {
		// Material's DatePicker reports the picked day as UTC-midnight millis, so
		// read the calendar date back in UTC (using the system zone would shift the
		// day by one for users far enough east/west of UTC).
		val localDateTime = Instant.fromEpochMilliseconds(dateTimestamp).toLocalDateTime(TimeZone.UTC)

        val selectedDate = LocalDate(
            year = localDateTime.year,
            month = localDateTime.month,
            day = localDateTime.day
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
				day = selectedDate.day,
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
		// The user is typing an exact hours+minutes value, so the fields now carry
		// the full precision and any preserved length is superseded.
		exactLength = null
		_uiState.update { state ->
			state.copy(
				lengthHours = length,
				isCompleteButtonEnabled = durationEntered(length, state.lengthMinutes, state.selectedDateTime),
			)
		}
	}

	override fun onMinutesChanged(minutes: String) {
		exactLength = null
		_uiState.update { state ->
			state.copy(
				lengthMinutes = minutes,
				isCompleteButtonEnabled = durationEntered(state.lengthHours, minutes, state.selectedDateTime),
			)
		}
	}

	/** A savable duration is present when the start is set and hours+minutes > 0. */
	private fun durationEntered(hours: String, minutes: String, start: LocalDateTime?): Boolean {
		val total = (hours.toLongOrNull() ?: 0).hours + (minutes.toLongOrNull() ?: 0).minutes
		return start != null && total > Duration.ZERO
	}

	override fun onNotesChanged(notes: String) {
		_uiState.update { it.copy(notes = notes) }
	}

	override fun onEndDateTimeSelected(instant: Instant) {
		val startDateTime = _uiState.value.selectedDateTime ?: return
		val startInstant = startDateTime.toInstant(TimeZone.currentSystemDefault())
		val duration = instant.minus(startInstant)

		// Only accept an end after the start. Split the exact duration across the
		// hours and minutes fields so the display matches what will be saved.
		if (duration > Duration.ZERO) {
			exactLength = duration
			_uiState.update {
				it.copy(
					lengthHours = duration.inWholeHours.toString(),
					lengthMinutes = (duration.inWholeMinutes % 60).toString(),
					isCompleteButtonEnabled = true,
				)
			}
		}
	}

	override fun onAddEntry(): Boolean {
		val currentState = _uiState.value
		val selectedDateTime = currentState.selectedDateTime
		val entryToEdit = currentState.entryToEdit
		val notes = currentState.notes.trim()

		// Save the precise length when we have one (edited entry or an end-time
		// calculation); otherwise build it from the hours + minutes fields.
		val fieldLength = (currentState.lengthHours.toLongOrNull() ?: 0).hours +
				(currentState.lengthMinutes.toLongOrNull() ?: 0).minutes
		val length = exactLength ?: fieldLength

		return if (selectedDateTime != null && length > Duration.ZERO) {
			viewModelScope.launch(Dispatchers.IO) {
				if (entryToEdit != null) {
					// Update existing entry
					repository.updateLogEntry(entryToEdit, selectedDateTime, length, notes)
				} else {
					// Add new entry
					repository.addLogEntry(selectedDateTime, length, notes)
				}
			}
			true
		} else {
			false
		}
	}

	override fun onDismiss() {
		// Reset state when dialog is dismissed
		exactLength = null
		_uiState.update {
			IManualAddViewModel.ManualAddUiState()
		}
	}

	override fun initializeWithEntry(entry: FastingLogEntry) {
		exactLength = entry.length
        val selectedDate = LocalDate(
            year = entry.start.year,
            month = entry.start.month,
            day = entry.start.day
        )

		_uiState.update {
			it.copy(
				currentStep = ManualAddStep.SetDuration,
				selectedDate = selectedDate,
				selectedDateTime = entry.start,
				lengthHours = entry.length.inWholeHours.toString(),
				lengthMinutes = (entry.length.inWholeMinutes % 60).toString(),
				notes = entry.notes,
				isCompleteButtonEnabled = true,
				entryToEdit = entry
			)
		}
	}

	override fun initializeWithDate(date: LocalDate) {
		// Fresh entry, but with the day preselected (e.g. tapped on the calendar):
		// open on the date step with that day chosen so the user just confirms it.
		exactLength = null
		_uiState.update {
			IManualAddViewModel.ManualAddUiState(
				currentStep = ManualAddStep.StartDate,
				selectedDate = date,
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
