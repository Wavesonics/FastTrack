package com.darkrockstudios.apps.fasttrack.data.log

import com.darkrockstudios.apps.fasttrack.data.database.FastEntry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.milliseconds

class FastingLogRepositoryImplTest {

	private lateinit var fakeDatasource: FakeFastingLogDatasource
	private lateinit var repository: FastingLogRepositoryImpl

	@Before
	fun setUp() {
		fakeDatasource = FakeFastingLogDatasource()
		repository = FastingLogRepositoryImpl(fakeDatasource)
	}

	@Test
	fun `test logFast creates and inserts entry with correct duration`() = runBlocking {
		// Given
		val startTime = Instant.fromEpochMilliseconds(1000)
		val endTime = Instant.fromEpochMilliseconds(5000)
		val expectedDuration = 4000L // 5000 - 1000 = 4000 milliseconds

		// When
		repository.logFast(startTime, endTime)

		// Then
		val entries = fakeDatasource.getAll()
		assertEquals(1, entries.size)
		assertEquals(startTime.toEpochMilliseconds(), entries[0].start)
		assertEquals(expectedDuration, entries[0].length)
	}

	@Test
	fun `test loadAll returns flow of entries`() = runBlocking {
		// Given
		val entry1 = FastEntry(uid = 1, start = 1000, length = 2000)
		val entry2 = FastEntry(uid = 2, start = 3000, length = 4000)
		fakeDatasource.insertAll(entry1, entry2)

		// When
		val flowEntries = repository.loadAll().first()

		// Then
		assertEquals(2, flowEntries.size)

		// Convert FastEntry to FastingLogEntry for comparison
		val expectedEntry1 = createFastingLogEntry(entry1)
		val expectedEntry2 = createFastingLogEntry(entry2)

		// Check if the list contains entries with matching properties
		assertTrue(flowEntries.any { it.start == expectedEntry1.start && it.length == expectedEntry1.length })
		assertTrue(flowEntries.any { it.start == expectedEntry2.start && it.length == expectedEntry2.length })
	}

	@Test
	fun `test delete removes entry from datasource`() = runBlocking {
		// Given
		val entry1 = FastEntry(uid = 1, start = 1000, length = 2000)
		val entry2 = FastEntry(uid = 2, start = 3000, length = 4000)
		fakeDatasource.insertAll(entry1, entry2)

		// Create FastingLogEntry from FastEntry for delete operation
		val fastingLogEntry = createFastingLogEntry(entry1)

		// When
		repository.delete(fastingLogEntry)

		// Then
		val entries = fakeDatasource.getAll()
		assertEquals(1, entries.size)
		assertEquals(entry2, entries[0])
	}

	@Test
	fun `test addLogEntry converts LocalDateTime to UTC and adds entry to datasource`() = runBlocking {
		// Given
		val startDateTime = LocalDateTime(2023, 1, 1, 12, 0, 0)
		val duration = 16.hours
		val expectedStartInstant = startDateTime.toInstant(TimeZone.currentSystemDefault())
		val expectedStartMillis = expectedStartInstant.toEpochMilliseconds()
		val expectedLengthMillis = duration.inWholeMilliseconds

		// When
		repository.addLogEntry(startDateTime, duration)

		// Then
		val entries = fakeDatasource.getAll()
		assertEquals(1, entries.size)
		assertEquals(expectedStartMillis, entries[0].start)
		assertEquals(expectedLengthMillis, entries[0].length)
	}

	// Helper function to create FastingLogEntry from FastEntry
	private fun createFastingLogEntry(entry: FastEntry): FastingLogEntry {
		val instant = Instant.fromEpochMilliseconds(entry.start)
		val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
		return FastingLogEntry(
			id = entry.uid,
			start = localDateTime,
			length = entry.length.milliseconds
		)
	}

	@Test
	fun `test exportLog returns CSV format with correct data`() = runBlocking {
		// Given
		fakeDatasource.clear() // Ensure we start with a clean state

		// Create test entries with known dates and times for predictable output
		val fixedTimeZone = TimeZone.of("UTC")
		val entry1Start = LocalDateTime(2023, 1, 1, 8, 0).toInstant(fixedTimeZone)
		val entry2Start = LocalDateTime(2023, 1, 2, 12, 30).toInstant(fixedTimeZone)

		val entry1 =
			FastEntry(uid = 1, start = entry1Start.toEpochMilliseconds(), length = 16.hours.inWholeMilliseconds)
		val entry2 =
			FastEntry(uid = 2, start = entry2Start.toEpochMilliseconds(), length = 24.hours.inWholeMilliseconds)

		fakeDatasource.insertAll(entry1, entry2)

		// When
		val csvOutput = repository.exportLog()

		// Then
		// Expected format: "ID,Start Date,Start Time,Duration (hours)"
		val lines = csvOutput.split("\n")

		// Check header
		assertEquals("ID,Start Date,Start Time,Duration (hours)", lines[0])

		// Check that we have the expected number of data rows
		assertEquals(3, lines.size) // Header + 2 entries

		// Convert entries to local time for comparison
		val entry1Local = entry1Start.toLocalDateTime(TimeZone.currentSystemDefault())
		val entry2Local = entry2Start.toLocalDateTime(TimeZone.currentSystemDefault())

		// Check that each entry is correctly formatted
		// Note: The exact format may vary depending on the local time zone, so we check for the presence of key data
		assertTrue(lines[1].startsWith("1,"))
		assertTrue(lines[1].contains(entry1Local.date.toString()))
		assertTrue(lines[1].endsWith("16"))

		assertTrue(lines[2].startsWith("2,"))
		assertTrue(lines[2].contains(entry2Local.date.toString()))
		assertTrue(lines[2].endsWith("24"))
	}

	@Test
	fun `test importLog parses CSV and adds entries to datasource`() = runBlocking<Unit> {
		// Given
		fakeDatasource.clear() // Ensure we start with a clean state

		// Create a CSV string in the same format as exportLog produces
		val csvInput = """
			ID,Start Date,Start Time,Duration (hours)
			1,2023-01-01,8:00,16
			2,2023-01-02,12:30,24
		""".trimIndent()

		// When
		val result = repository.importLog(csvInput)

		// Then
		assertTrue(result) // Import should succeed

		// Check that entries were added to the datasource
		val entries = fakeDatasource.getAll()
		assertEquals(2, entries.size)

		// Verify the entries have the correct properties
		// Note: We can't directly check the exact millisecond values as they depend on the timezone
		// So we'll check the IDs and relative properties

		// Find entries by ID
		val entry1 = entries.find { it.uid == 1 }
		val entry2 = entries.find { it.uid == 2 }

		// Verify entries exist
		assertTrue(entry1 != null)
		assertTrue(entry2 != null)

		// Verify durations
		assertEquals(16 * 60 * 60 * 1000, entry1!!.length) // 16 hours in milliseconds
		assertEquals(24 * 60 * 60 * 1000, entry2!!.length) // 24 hours in milliseconds
	}

	@Test
	fun `test importLog handles conflicts by replacing existing entries`() = runBlocking<Unit> {
		// Given
		fakeDatasource.clear() // Ensure we start with a clean state

		// Add an existing entry with ID 1
		val existingEntry = FastEntry(uid = 1, start = 1000, length = 2000)
		fakeDatasource.insertAll(existingEntry)

		// Create a CSV with an entry that has the same ID
		val csvInput = """
			ID,Start Date,Start Time,Duration (hours)
			1,2023-01-01,8:00,16
		""".trimIndent()

		// When
		val result = repository.importLog(csvInput)

		// Then
		assertTrue(result) // Import should succeed

		// Check that the entry was replaced
		val entries = fakeDatasource.getAll()
		assertEquals(1, entries.size)

		// Verify the entry has the new properties
		val entry = entries[0]
		assertEquals(1, entry.uid)
		assertEquals(16 * 60 * 60 * 1000, entry.length) // 16 hours in milliseconds

		// The original entry had length 2000, so if it's now 16 hours in milliseconds,
		// we know it was replaced
		assertNotEquals(2000, entry.length)
	}

	@Test
	fun `test importLog returns false for invalid CSV format`() = runBlocking<Unit> {
		// Given
		fakeDatasource.clear() // Ensure we start with a clean state

		// Create an invalid CSV string
		val invalidCsv = "This is not a valid CSV format"

		// When
		val result = repository.importLog(invalidCsv)

		// Then
		assertFalse(result) // Import should fail

		// Check that no entries were added
		val entries = fakeDatasource.getAll()
		assertEquals(0, entries.size)
	}
}
