package com.darkrockstudios.apps.fasttrack.screens.log.manualadd

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlin.time.Instant

class FakeManualAddViewModel(initialState: IManualAddViewModel.ManualAddUiState = IManualAddViewModel.ManualAddUiState()) :
	IManualAddViewModel {
	private val _uiState = MutableStateFlow(initialState)
	override val uiState: StateFlow<IManualAddViewModel.ManualAddUiState> = _uiState

	override fun onDateSelected(dateTimestamp: Long) {}
	override fun onTimeSelected(hour: Int, minute: Int) {}
	override fun onLengthChanged(length: String) {}
	override fun onEndDateTimeSelected(instant: Instant) {}
	override fun onAddEntry() = true
	override fun onDismiss() {}
}

@Preview(showBackground = true, name = "Step 0 - Date Selection")
@Composable
fun ManualAddDialogPreviewStep0() {
	ManualAddDialog(
		onDismiss = {},
		viewModel = FakeManualAddViewModel(
			IManualAddViewModel.ManualAddUiState(
				currentStep = ManualAddStep.StartDate,
				isNextButtonEnabled = true
			)
		)
	)
}

@Preview(showBackground = true, name = "Step 1 - Length Input")
@Composable
fun ManualAddDialogPreviewStep1() {
	val selectedDate = LocalDate(2023, 5, 15)
	ManualAddDialog(
		onDismiss = {},
		viewModel = FakeManualAddViewModel(
			IManualAddViewModel.ManualAddUiState(
				currentStep = ManualAddStep.StartTime,
				selectedDate = selectedDate,
				lengthHours = "16",
				isNextButtonEnabled = true
			)
		)
	)
}

@Preview(showBackground = true, name = "Step 2 - Summary and Completion")
@Composable
fun ManualAddDialogPreviewStep2() {
	val selectedDateTime = LocalDateTime(2023, 5, 15, 8, 30)
	ManualAddDialog(
		onDismiss = {},
		viewModel = FakeManualAddViewModel(
			IManualAddViewModel.ManualAddUiState(
				currentStep = ManualAddStep.SetDuration,
				selectedDateTime = selectedDateTime,
				lengthHours = "16",
				isCompleteButtonEnabled = true
			)
		)
	)
}
