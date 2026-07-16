package com.darkrockstudios.apps.fasttrack.screens.log.manualadd

import com.darkrockstudios.apps.fasttrack.data.log.FastingLogEntry
import kotlinx.coroutines.flow.StateFlow
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

interface IManualAddViewModel {
	data class ManualAddUiState(
        val currentStep: ManualAddStep = ManualAddStep.StartDate,
        val selectedDate: LocalDate? = null,
		val selectedDateTime: LocalDateTime? = null,
        val lengthHours: String = "",
        val lengthMinutes: String = "",
        val notes: String = "",
        val isNextButtonEnabled: Boolean = true,
        val isCompleteButtonEnabled: Boolean = false,
        val entryToEdit: FastingLogEntry? = null
	) {
		fun end(): Instant? {
			return selectedDateTime?.let { start ->
				val h = lengthHours.toLongOrNull() ?: 0
				val m = lengthMinutes.toLongOrNull() ?: 0
				val length = h.hours + m.minutes
				if (length.inWholeMinutes > 0) {
					val startInstant = start.toInstant(TimeZone.currentSystemDefault())
					Instant.fromEpochMilliseconds(
						startInstant.toEpochMilliseconds() + length.inWholeMilliseconds
					)
				} else {
					null
				}
			}
		}
	}

	val uiState: StateFlow<ManualAddUiState>

	fun onDateSelected(dateTimestamp: Long)
	fun onTimeSelected(hour: Int, minute: Int)
	fun onLengthChanged(length: String)
	fun onMinutesChanged(minutes: String)
	fun onNotesChanged(notes: String)
	fun onEndDateTimeSelected(instant: Instant)
	fun onAddEntry(): Boolean
	fun onDismiss()
	fun initializeWithEntry(entry: FastingLogEntry)
	fun initializeWithDate(date: LocalDate)
	fun onPreviousStep()
	fun goToStep(step: ManualAddStep)
}

enum class ManualAddStep(val isFinalStep: Boolean = false) {
	StartDate, StartTime, SetDuration(true)
}
