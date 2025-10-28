package com.darkrockstudios.apps.fasttrack.screens.log.manualadd

import com.darkrockstudios.apps.fasttrack.data.log.FastingLogEntry
import kotlinx.coroutines.flow.StateFlow
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant

interface IManualAddViewModel {
	data class ManualAddUiState(
		val currentStep: ManualAddStep = ManualAddStep.StartDate,
		val selectedDate: LocalDate? = null,
		val selectedDateTime: LocalDateTime? = null,
		val lengthHours: String = "",
		val isNextButtonEnabled: Boolean = true,
		val isCompleteButtonEnabled: Boolean = false,
		val entryToEdit: FastingLogEntry? = null
	) {
		fun end(): Instant? {
			return selectedDateTime?.let { start ->
				val lengthHours = lengthHours.toLongOrNull() ?: 0
				if (lengthHours > 0) {
					val startInstant = start.toInstant(TimeZone.currentSystemDefault())
					val durationMillis = lengthHours.hours.inWholeMilliseconds
					Instant.fromEpochMilliseconds(
						startInstant.toEpochMilliseconds() + durationMillis
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
	fun onEndDateTimeSelected(instant: Instant)
	fun onAddEntry(): Boolean
	fun onDismiss()
	fun initializeWithEntry(entry: FastingLogEntry)
	fun onPreviousStep()
	fun goToStep(step: ManualAddStep)
}

enum class ManualAddStep(val isFinalStep: Boolean = false) {
	StartDate, StartTime, SetDuration(true)
}
