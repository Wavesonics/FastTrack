package com.darkrockstudios.apps.fasttrack.data.log

import com.darkrockstudios.apps.fasttrack.data.database.FastEntry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
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
}
