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
		val lines = csvOutput.split("\n")

		// New schema header
		assertEquals("ID,Start,End,Duration (s),Duration,Notes", lines[0])
		assertEquals(3, lines.size) // Header + 2 entries

		// The datetime columns depend on the local time zone, so assert the
		// tz-independent columns: duration seconds and humanized duration.
		assertTrue(lines[1].startsWith("1,"))
		assertTrue(lines[1].contains(",57600,")) // 16h in seconds
		assertTrue(lines[1].endsWith("16h 0m,")) // humanized, then empty Notes

		assertTrue(lines[2].startsWith("2,"))
		assertTrue(lines[2].contains(",86400,")) // 24h in seconds
		assertTrue(lines[2].endsWith("1d 0h 0m,"))
	}

	@Test
	fun `test export then import round-trips through the new format`() = runBlocking<Unit> {
		fakeDatasource.clear()
		val start = LocalDateTime(2023, 3, 15, 9, 5, 30).toInstant(TimeZone.currentSystemDefault())
		fakeDatasource.insertAll(
			FastEntry(uid = 1, start = start.toEpochMilliseconds(), length = 40.hours.inWholeMilliseconds, notes = "Felt, \"great\"")
		)

		val csv = repository.exportLog()
		fakeDatasource.clear()
		val ok = repository.importLog(csv)

		assertTrue(ok)
		val entries = fakeDatasource.getAll()
		assertEquals(1, entries.size)
		assertEquals(start.toEpochMilliseconds(), entries[0].start)
		assertEquals(40.hours.inWholeMilliseconds, entries[0].length)
		assertEquals("Felt, \"great\"", entries[0].notes) // commas + quotes survive CSV escaping
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

		// When (legacy format must still import)
		val result = repository.importLog(csvInput)

		// Then
		assertTrue(result) // Import should succeed

		val entries = fakeDatasource.getAll()
		assertEquals(2, entries.size)

		// Import keys on start (not the CSV's ID), so verify by duration
		assertTrue(entries.any { it.length == 16L * 60 * 60 * 1000 })
		assertTrue(entries.any { it.length == 24L * 60 * 60 * 1000 })
	}

	@Test
	fun `test importLog de-dupes by start time, replacing the existing entry`() = runBlocking<Unit> {
		// Given an entry starting at a specific instant
		fakeDatasource.clear()
		val start = LocalDateTime(2023, 1, 1, 8, 0, 0).toInstant(TimeZone.currentSystemDefault())
		fakeDatasource.insertAll(FastEntry(uid = 1, start = start.toEpochMilliseconds(), length = 2000))

		// Importing a row with the SAME start (different duration) should replace it
		val csvInput = """
			ID,Start Date,Start Time,Duration (hours)
			99,2023-01-01,8:00,16
		""".trimIndent()

		// When
		val result = repository.importLog(csvInput)

		// Then
		assertTrue(result)
		val entries = fakeDatasource.getAll()
		assertEquals(1, entries.size) // replaced, not duplicated
		assertEquals(16L * 60 * 60 * 1000, entries[0].length)
		assertNotEquals(2000L, entries[0].length)
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
