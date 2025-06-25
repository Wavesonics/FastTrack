package com.darkrockstudios.apps.fasttrack.screens.fasting

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.darkrockstudios.apps.fasttrack.R
import kotlin.time.ExperimentalTime

@ExperimentalTime
@Preview(showBackground = true)
@Composable
fun DateTimePickerDialogStep0Preview() {
	val state = rememberDateTimePickerDialogState()
	DateTimePickerDialog(
		onDismiss = {},
		onDateTimeSelected = {},
		title = stringResource(R.string.already_started_dialog_title),
		finishButton = stringResource(id = R.string.start_fast_button),
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
		title = stringResource(R.string.already_started_dialog_title),
		finishButton = stringResource(id = R.string.start_fast_button),
		state = state
	)
}
