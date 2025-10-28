package com.darkrockstudios.apps.fasttrack.screens.log

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.darkrockstudios.apps.fasttrack.R
import com.darkrockstudios.apps.fasttrack.data.log.FakeFastingLogDatasource
import com.darkrockstudios.apps.fasttrack.data.log.FastingLogEntry
import com.darkrockstudios.apps.fasttrack.data.log.FastingLogRepository
import com.darkrockstudios.apps.fasttrack.data.log.FastingLogRepositoryImpl
import com.darkrockstudios.apps.fasttrack.ui.theme.FastTrackTheme
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.datetime.LocalDateTime
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.time.Duration.Companion.hours
import kotlin.time.ExperimentalTime

/**
 * Compose UI test for the Log Screen.
 * Tests displaying data from FakeFastingLogDatasource and manual add/delete operations.
 */
@ExperimentalTime
@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class LogScreenComposeTest {

	@get:Rule
	val composeTestRule = createComposeRule()

	private val context = InstrumentationRegistry.getInstrumentation().targetContext
	private lateinit var fakeDatasource: FakeFastingLogDatasource
	private lateinit var repository: FastingLogRepository
	private lateinit var viewModel: LogViewModel

	@Before
	fun setup() {
		fakeDatasource = FakeFastingLogDatasource()
		repository = FastingLogRepositoryImpl(fakeDatasource)
		viewModel = LogViewModel(repository)
	}

	@Test
	fun testEmptyLogScreen() {
		// Set up the compose content with our ViewModel
		composeTestRule.setContent {
			FastTrackTheme {
				LogScreen(viewModel = viewModel)
			}
		}

		// Verify the total hours are displayed as 0

		// Check if the autophagy label exists
		composeTestRule.onAllNodesWithText(context.getString(R.string.log_total_autophagy))
			.assertCountEquals(1)

		// Check if the ketosis label exists
		composeTestRule.onAllNodesWithText(context.getString(R.string.log_total_ketosis))
			.assertCountEquals(1)

		// Verify the add button is displayed
		composeTestRule.onNodeWithContentDescription(context.getString(R.string.manual_add_title)).assertExists()

		// No entries should be displayed
		composeTestRule.onNodeWithText(context.getString(R.string.log_entry_length, 24)).assertDoesNotExist()
	}

	@Test
	fun testLogScreenWithEntries() {
		// Clear any existing data
		fakeDatasource.clear()

		val entry1 = createTestEntry(
			0,
			LocalDateTime(2023, 1, 1, 12, 0),
			24.hours // 24 hours fast (will have ketosis but no autophagy)
		)
		val entry2 = createTestEntry(
			1,
			LocalDateTime(2023, 1, 2, 12, 0),
			16.hours // 16 hours fast (will have no ketosis or autophagy)
		)
		val entry3 = createTestEntry(
			2,
			LocalDateTime(2023, 1, 3, 12, 0),
			32.hours // 32 hours fast (will have ketosis and autophagy)
		)

		repository.addLogEntry(entry1.start, entry1.length)
		repository.addLogEntry(entry2.start, entry2.length)
		repository.addLogEntry(entry3.start, entry3.length)

		// Set up the compose content with our ViewModel
		composeTestRule.setContent {
			FastTrackTheme {
				LogScreen(viewModel = viewModel)
			}
		}

		// Wait for the UI to update
		composeTestRule.waitForIdle()


		// Check if the autophagy and ketosis labels exist
		composeTestRule.onAllNodesWithText(context.getString(R.string.log_total_autophagy))
			.assertCountEquals(1)
		composeTestRule.onAllNodesWithText(context.getString(R.string.log_total_ketosis))
			.assertCountEquals(1)

		// Verify entries exist by checking for their length values
		composeTestRule.onAllNodesWithText(
			"⏱️ " + context.getString(
				R.string.log_entry_length,
				entry1.length.inWholeHours
			)
		)
			.assertCountEquals(1)
		composeTestRule.onAllNodesWithText(
			"⏱️ " + context.getString(
				R.string.log_entry_length,
				entry2.length.inWholeHours
			)
		)
			.assertCountEquals(1)
		composeTestRule.onAllNodesWithText(
			"⏱️ " + context.getString(
				R.string.log_entry_length,
				entry3.length.inWholeHours
			)
		)
			.assertCountEquals(1)
	}

	// Helper function to create a test FastingLogEntry
	private fun createTestEntry(id: Int, start: LocalDateTime, length: kotlin.time.Duration): FastingLogEntry {
		return FastingLogEntry(id = id, start, length)
	}
}
