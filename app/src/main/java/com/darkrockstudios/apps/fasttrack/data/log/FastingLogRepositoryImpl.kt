package com.darkrockstudios.apps.fasttrack.data.log

import com.darkrockstudios.apps.fasttrack.data.database.FastEntry
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.datetime.*
import java.time.format.DateTimeFormatter
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import java.time.LocalDate as JavaLocalDate
import java.time.LocalDateTime as JavaLocalDateTime
import java.time.LocalTime as JavaLocalTime

class FastingLogRepositoryImpl(
	private val datasource: FastingLogDatasource
) : FastingLogRepository {
	override fun logFast(startTime: Instant, endTime: Instant) {
		val duration = endTime.minus(startTime)
		val newEntry = FastEntry(
			start = startTime.toEpochMilliseconds(),
			length = duration.inWholeMilliseconds
		)
		datasource.insertAll(newEntry)
	}

	override fun loadAll(): Flow<List<FastingLogEntry>> = datasource.loadAll().map { entries ->
		entries.map { it.toFastingLogEntry() }
	}

	override fun delete(item: FastingLogEntry): Boolean {
		return datasource.deleteByUid(item.id)
	}

	override fun addLogEntry(start: LocalDateTime, length: Duration) {
		// Convert LocalDateTime to Instant (UTC time)
		val startInstant = start.toInstant(TimeZone.currentSystemDefault())

		// Create FastEntry with UTC epoch milliseconds and duration in milliseconds
		val newEntry = FastEntry(
			start = startInstant.toEpochMilliseconds(),
			length = length.inWholeMilliseconds
		)
		datasource.insertAll(newEntry)
	}

	override fun updateLogEntry(entry: FastingLogEntry, start: LocalDateTime, length: Duration): Boolean {
		// Convert LocalDateTime to Instant (UTC time)
		val startInstant = start.toInstant(TimeZone.currentSystemDefault())

		// Create updated FastEntry with the same ID but new values
		val updatedEntry = FastEntry(
			uid = entry.id,
			start = startInstant.toEpochMilliseconds(),
			length = length.inWholeMilliseconds
		)
		return datasource.update(updatedEntry)
	}

	override suspend fun exportLog(): String {
		val entries = loadAll().first()

		val header = "ID,Start Date,Start Time,Duration (hours)"

		val rows = entries.map { entry ->
			val startDate = entry.start.date.toString()
			val startTime = "${entry.start.hour}:${entry.start.minute.toString().padStart(2, '0')}"
			val durationHours = entry.length.inWholeHours

			"${entry.id},$startDate,$startTime,$durationHours"
		}

		return (listOf(header) + rows).joinToString("\n")
	}

	private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
	private val timeFormatter = DateTimeFormatter.ofPattern("H:mm")

	override suspend fun importLog(cvsExport: String): Boolean {
		try {
			val lines = cvsExport.split("\n")

			// Skip header line
			if (lines.size <= 1) {
				return false
			}

			// Process each data row
			for (i in 1 until lines.size) {
				val line = lines[i].trim()
				if (line.isEmpty()) continue

				val parts = line.split(",")
				if (parts.size < 4) continue // Skip invalid lines

				val id = parts[0].toIntOrNull() ?: continue
				val dateStr = parts[1]
				val timeStr = parts[2]
				val durationHours = parts[3].toLongOrNull() ?: continue

				val localDateTime: LocalDateTime
				try {
					val javaDate = JavaLocalDate.parse(dateStr, dateFormatter)
					val javaTime = JavaLocalTime.parse(timeStr, timeFormatter)
					val javaDateTime = JavaLocalDateTime.of(javaDate, javaTime)

					localDateTime = LocalDateTime(
						javaDateTime.year,
						javaDateTime.monthValue,
						javaDateTime.dayOfMonth,
						javaDateTime.hour,
						javaDateTime.minute
					)
				} catch (e: Exception) {
					Napier.w("Failed to import log entry", e)
					continue
				}

				val startInstant = localDateTime.toInstant(TimeZone.UTC)

				val newEntry = FastEntry(
					uid = id,
					start = startInstant.toEpochMilliseconds(),
					length = durationHours * 60 * 60 * 1000 // Convert hours to milliseconds
				)

				// Handle conflicts by deleting existing entry with the same ID
				datasource.deleteByUid(id)

				datasource.insertAll(newEntry)
			}

			return true
		} catch (e: Exception) {
			return false
		}
	}

	private fun FastEntry.toFastingLogEntry(): FastingLogEntry {
		val instant = Instant.fromEpochMilliseconds(start)
		val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
		return FastingLogEntry(
			id = uid,
			start = localDateTime,
			length = length.milliseconds
		)
	}
}
