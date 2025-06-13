package com.darkrockstudios.apps.fasttrack.screens.fasting

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import kotlin.time.ExperimentalTime

@ExperimentalTime
@Preview(
	name = "Timeline - Initial State",
	showBackground = true,
	widthDp = 320,
	backgroundColor = 0xFF9999FF
)
@Composable
fun TimeLinePreviewInitial() {
	TimeLine(elapsedHours = 0.0)
}

@ExperimentalTime
@Preview(
	name = "Timeline - Glucose Phase",
	showBackground = true,
	widthDp = 320,
	backgroundColor = 0xFF9999FF
)
@Composable
fun TimeLinePreviewGlucose() {
	TimeLine(elapsedHours = 8.0)
}

@ExperimentalTime
@Preview(
	name = "Timeline - Fat Burning Phase",
	showBackground = true,
	widthDp = 320,
	backgroundColor = 0xFF9999FF
)
@Composable
fun TimeLinePreviewFatBurning() {
	TimeLine(elapsedHours = 18.0)
}

@ExperimentalTime
@Preview(
	name = "Timeline - Ketosis Phase",
	showBackground = true,
	widthDp = 320,
	backgroundColor = 0xFF9999FF
)
@Composable
fun TimeLinePreviewKetosis() {
	TimeLine(elapsedHours = 22.0)
}

@ExperimentalTime
@Preview(
	name = "Timeline - Autophagy Phase",
	showBackground = true,
	widthDp = 320,
	backgroundColor = 0xFF9999FF
)
@Composable
fun TimeLinePreviewAutophagy() {
	TimeLine(elapsedHours = 36.0)
}

@ExperimentalTime
@Preview(
	name = "Timeline - Optimal Autophagy Phase",
	showBackground = true,
	widthDp = 320,
	backgroundColor = 0xFF9999FF
)
@Composable
fun TimeLinePreviewOptimalAutophagy() {
	TimeLine(elapsedHours = 60.0)
}

@ExperimentalTime
@Preview(
	name = "Timeline - Dark Mode",
	showBackground = true,
	widthDp = 320,
	backgroundColor = 0xFF121212
)
@Composable
fun TimeLinePreviewDarkMode() {
	TimeLine(elapsedHours = 22.0)
}

@ExperimentalTime
@Preview(
	name = "Timeline - Phone Width",
	showBackground = true,
	widthDp = 360,
	backgroundColor = 0xFF9999FF
)
@Composable
fun TimeLinePreviewPhoneWidth() {
	TimeLine(elapsedHours = 36.0)
}

@ExperimentalTime
@Preview(
	name = "Timeline - Tablet Width",
	showBackground = true,
	widthDp = 600,
	backgroundColor = 0xFF9999FF
)
@Composable
fun TimeLinePreviewTabletWidth() {
	TimeLine(elapsedHours = 18.0)
}
