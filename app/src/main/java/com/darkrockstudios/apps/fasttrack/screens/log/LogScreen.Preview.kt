package com.darkrockstudios.apps.fasttrack.screens.log

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.darkrockstudios.apps.fasttrack.data.log.FastingLogEntry
import com.darkrockstudios.apps.fasttrack.ui.theme.FastTrackTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * This file contains preview implementations for the LogScreen.
 * Since LogScreen requires a ViewModel, we create a simplified version
 * of the UI for preview purposes.
 */
@SuppressLint("ViewModelConstructorInComposable")
@Composable
fun LogScreenPreview(
	entries: List<FastingLogEntry> = emptyList(),
	totalKetosisHours: Int = 0,
	totalAutophagyHours: Int = 0,
	showManualAddDialog: Boolean = false,
	darkTheme: Boolean = false
) {
	val viewModel = FakeLogViewModel(
		ILogViewModel.LogUiState(
			entries = entries,
			totalKetosisHours = totalKetosisHours,
			totalAutophagyHours = totalAutophagyHours,
			showManualAddDialog = showManualAddDialog
		)
	)

	FastTrackTheme(darkTheme = darkTheme) {
		LogScreen(
			contentPaddingValues = PaddingValues(0.dp),
			viewModel = viewModel,
		)
	}
}

@ExperimentalTime
@Preview(
	name = "Log Screen - Empty",
	showBackground = true,
	widthDp = 360,
	heightDp = 640
)
@Composable
private fun LogScreenPreviewEmpty() {
	LogScreenPreview(
		entries = emptyList(),
		totalKetosisHours = 0,
		totalAutophagyHours = 0,
		showManualAddDialog = false
	)
}

@ExperimentalTime
@Preview(
	name = "Log Screen - Empty (Dark)",
	showBackground = true,
	widthDp = 360,
	heightDp = 640,
	uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun LogScreenPreviewEmptyDark() {
	LogScreenPreview(
		entries = emptyList(),
		totalKetosisHours = 0,
		totalAutophagyHours = 0,
		showManualAddDialog = false,
		darkTheme = true
	)
}

@ExperimentalTime
@Preview(
	name = "Log Screen - With Entries",
	showBackground = true,
	widthDp = 360,
	heightDp = 640
)
@Composable
private fun LogScreenPreviewWithEntries() {
	LogScreenPreview(
		entries = entries,
		totalKetosisHours = 52, // Sum of ketosis hours from entries
		totalAutophagyHours = 24, // Sum of autophagy hours from entries
		showManualAddDialog = false
	)
}

@ExperimentalTime
@Preview(
	name = "Log Screen - With Entries (Dark)",
	showBackground = true,
	widthDp = 360,
	heightDp = 640,
	uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun LogScreenPreviewWithEntriesDark() {
	LogScreenPreview(
		entries = entries,
		totalKetosisHours = 52, // Sum of ketosis hours from entries
		totalAutophagyHours = 24, // Sum of autophagy hours from entries
		showManualAddDialog = false,
		darkTheme = true
	)
}

@ExperimentalTime
@Preview(
	name = "Log Screen - Tablet",
	showBackground = true,
	widthDp = 600,
	heightDp = 800
)
@Composable
private fun LogScreenPreviewTablet() {
	LogScreenPreview(
		entries = entries,
		totalKetosisHours = 52,
		totalAutophagyHours = 24,
		showManualAddDialog = false
	)
}

@ExperimentalTime
@Preview(
	name = "Log Screen - Tablet (Dark)",
	showBackground = true,
	widthDp = 600,
	heightDp = 800,
	uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun LogScreenPreviewTabletDark() {
	LogScreenPreview(
		entries = entries,
		totalKetosisHours = 52,
		totalAutophagyHours = 24,
		showManualAddDialog = false,
		darkTheme = true
	)
}

@ExperimentalTime
@Preview(
	name = "Log Screen - Tablet Landscape (Dark)",
	showBackground = true,
	widthDp = 2000,
	heightDp = 1024,
	uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun LogScreenPreviewLargeTabletDark() {
	LogScreenPreview(
		entries = entries,
		totalKetosisHours = 52,
		totalAutophagyHours = 24,
		showManualAddDialog = false,
		darkTheme = true
	)
}

@ExperimentalTime
@Preview(
	name = "Log Screen - Landscape Phone",
	showBackground = true,
	widthDp = 640,
	heightDp = 360
)
@Composable
private fun LogScreenPreviewLandscapePhone() {
	val entries = listOf(
		createFastingLogEntry(
			System.currentTimeMillis() - (24 * 60 * 60 * 1000), // 24 hours ago
			16 * 60 * 60 * 1000 // 16 hours
		),
		createFastingLogEntry(
			System.currentTimeMillis() - (3 * 24 * 60 * 60 * 1000), // 3 days ago
			36 * 60 * 60 * 1000 // 36 hours
		)
	)

	LogScreenPreview(
		entries = entries,
		totalKetosisHours = 28, // Sum of ketosis hours from entries
		totalAutophagyHours = 12, // Sum of autophagy hours from entries
		showManualAddDialog = false
	)
}

@ExperimentalTime
@Preview(
	name = "Log Screen - Landscape Phone (Dark)",
	showBackground = true,
	widthDp = 640,
	heightDp = 360,
	uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun LogScreenPreviewLandscapePhoneDark() {
	val entries = listOf(
		createFastingLogEntry(
			System.currentTimeMillis() - (24 * 60 * 60 * 1000), // 24 hours ago
			16 * 60 * 60 * 1000 // 16 hours
		),
		createFastingLogEntry(
			System.currentTimeMillis() - (3 * 24 * 60 * 60 * 1000), // 3 days ago
			36 * 60 * 60 * 1000 // 36 hours
		)
	)

	LogScreenPreview(
		entries = entries,
		totalKetosisHours = 28, // Sum of ketosis hours from entries
		totalAutophagyHours = 12, // Sum of autophagy hours from entries
		showManualAddDialog = false,
		darkTheme = true
	)
}

private fun createFastingLogEntry(startMillis: Long, lengthMillis: Long): FastingLogEntry {
	val localDateTime = Instant.fromEpochMilliseconds(startMillis)
		.toLocalDateTime(TimeZone.currentSystemDefault())

	return FastingLogEntry(
		id = startMillis.toInt(),
		start = localDateTime,
		length = lengthMillis.milliseconds
	)
}

private val entries = listOf(
	createFastingLogEntry(
		System.currentTimeMillis() - (24 * 60 * 60 * 1000), // 24 hours ago
		16 * 60 * 60 * 1000 // 16 hours
	),
	createFastingLogEntry(
		System.currentTimeMillis() - (3 * 24 * 60 * 60 * 1000), // 3 days ago
		36 * 60 * 60 * 1000 // 36 hours
	),
	createFastingLogEntry(
		System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000), // 7 days ago
		48 * 60 * 60 * 1000 // 48 hours
	)
)

class FakeLogViewModel(state: ILogViewModel.LogUiState) : ILogViewModel {
	override val uiState = MutableStateFlow(state).asStateFlow()
	override fun deleteFast(item: FastingLogEntry) {}
	override fun showManualAddDialog() {}
	override fun hideManualAddDialog() {}
	override fun addEntry(startTime: LocalDateTime, length: Duration) {}
	override fun loadEntries() {}
}
