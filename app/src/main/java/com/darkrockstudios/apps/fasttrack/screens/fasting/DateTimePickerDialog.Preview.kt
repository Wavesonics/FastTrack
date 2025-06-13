package com.darkrockstudios.apps.fasttrack.screens.fasting

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import kotlin.time.ExperimentalTime

@ExperimentalTime
@Preview(showBackground = true)
@Composable
fun DateTimePickerDialogStep0Preview() {
	val state = rememberDateTimePickerDialogState()
	DateTimePickerDialog(
		onDismiss = {},
		onDateTimeSelected = {},
		state = state
	)
}


@ExperimentalTime
@Preview(showBackground = true)
@Composable
fun DateTimePickerDialogStep1Preview() {
	val state = rememberDateTimePickerDialogState()
	state.currentStep = 1
	DateTimePickerDialog(
		onDismiss = {},
		onDateTimeSelected = {},
		state = state
	)
}
