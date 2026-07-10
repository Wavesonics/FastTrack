package com.darkrockstudios.apps.fasttrack.data.log

import com.darkrockstudios.apps.fasttrack.data.database.FastEntry
import com.darkrockstudios.apps.fasttrack.utils.csvEscape
import com.darkrockstudios.apps.fasttrack.utils.formatDurationFull
import com.darkrockstudios.apps.fasttrack.utils.parseCsv
import io.github.aakira.napier.Napier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Instant

class FastingLogRepositoryImpl(
	private val datasource: FastingLogDatasource
) : FastingLogRepository {

	override fun logFast(startTime: Instant, endTime: Instant, notes: String) {
		val duration = endTime.minus(startTime)
		datasource.insertAll(
			FastEntry(
				start = startTime.toEpochMilliseconds(),
				length = duration.inWholeMilliseconds,
				notes = notes,
			)
		)
	}

	override fun loadAll(): Flow<List<FastingLogEntry>> = datasource.loadAll().map { entries ->
		entries.map { it.toFastingLogEntry() }
	}

	override fun delete(item: FastingLogEntry): Boolean = datasource.deleteByUid(item.id)

	override fun addLogEntry(start: LocalDateTime, length: Duration, notes: String) {
		val startInstant = start.toInstant(TimeZone.currentSystemDefault())
		datasource.insertAll(
			FastEntry(
				start = startInstant.toEpochMilliseconds(),
				length = length.inWholeMilliseconds,
				notes = notes,
			)
		)
	}

	override fun updateLogEntry(
		entry: FastingLogEntry,
		start: LocalDateTime,
		length: Duration,
		notes: String
	): Boolean {
		val startInstant = start.toInstant(TimeZone.currentSystemDefault())
		val updatedEntry = FastEntry(
			uid = entry.id,
			start = startInstant.toEpochMilliseconds(),
			length = length.inWholeMilliseconds,
			notes = notes,
		)
		return datasource.update(updatedEntry)
	}

	// region Export

	override suspend fun exportLog(): String = withContext(Dispatchers.IO) {
		val entries = datasource.getAll()
		val tz = TimeZone.currentSystemDefault()

		val header = "ID,Start,End,Duration (s),Duration,Notes"
		val rows = entries.map { entry ->
			val startInstant = Instant.fromEpochMilliseconds(entry.start)
			val length = entry.length.milliseconds
			val endInstant = startInstant.plus(length)

			val startStr = formatDateTime(startInstant.toLocalDateTime(tz))
			val endStr = formatDateTime(endInstant.toLocalDateTime(tz))
			val seconds = length.inWholeSeconds
			val humanized = formatDurationFull(length)

			listOf(
				entry.uid.toString(),
				startStr,
				endStr,
				seconds.toString(),
				humanized,
				csvEscape(entry.notes),
			).joinToString(",")
		}

		(listOf(header) + rows).joinToString("\n")
	}

	// endregion

	// region Import

	/**
	 * Import a logbook CSV. Supports both the current schema
	 * (ID, Start, End, Duration (s), Duration, Notes) and the legacy schema
	 * (ID, Start Date, Start Time, Duration (hours)). Timestamps are interpreted
	 * in the device's current time zone, matching how they were exported.
	 *
	 * Entries are de-duplicated by start-second, so re-importing the same file
	 * updates rather than duplicates.
	 */
	override suspend fun importLog(cvsExport: String): Boolean = withContext(Dispatchers.IO) {
		try {
			val allRows = parseCsv(cvsExport).filter { row -> row.any { it.isNotBlank() } }
			if (allRows.isEmpty()) return@withContext false

			val headerCells = allRows.first().map { it.trim().lowercase() }
			val hasHeader = headerCells.any { cell ->
				cell == "id" || cell == "start" || cell == "end" || cell == "notes" ||
					cell == "start date" || cell == "start time" || cell.startsWith("duration")
			}
			val isLegacy = headerCells.contains("start date") ||
				headerCells.any { it.startsWith("duration (h") }

			val dataRows = if (hasHeader) allRows.drop(1) else allRows
			val tz = TimeZone.currentSystemDefault()

			// Resolve column indices from the header when present; otherwise fall
			// back to the canonical positions of each schema.
			val cols = if (hasHeader) headerCells else emptyList()
			fun col(vararg names: String, default: Int): Int {
				for (n in names) {
					val idx = cols.indexOfFirst { it == n || it.startsWith(n) }
					if (idx >= 0) return idx
				}
				return default
			}

			var imported = false
			for (row in dataRows) {
				val parsed = if (isLegacy) {
					parseLegacyRow(
						row,
						dateIdx = col("start date", default = 1),
						timeIdx = col("start time", default = 2),
						hoursIdx = col("duration (h", default = 3),
						tz = tz,
					)
				} else {
					parseCurrentRow(
						row,
						startIdx = col("start", default = 1),
						endIdx = col("end", default = 2),
						secondsIdx = col("duration (s", default = 3),
						notesIdx = col("notes", default = 5),
						tz = tz,
					)
				} ?: continue

				val (startEpoch, lengthMs, notes) = parsed
				// De-dupe within the whole second the start falls in
				val secondFloor = startEpoch - (startEpoch % 1000)
				datasource.deleteByStartRange(secondFloor, secondFloor + 1000)
				datasource.insertAll(FastEntry(start = startEpoch, length = lengthMs, notes = notes))
				imported = true
			}

			imported
		} catch (e: Exception) {
			Napier.e("Failed to import logbook", e)
			false
		}
	}

	private data class ParsedRow(val startEpoch: Long, val lengthMs: Long, val notes: String)

	private fun parseCurrentRow(
		row: List<String>,
		startIdx: Int,
		endIdx: Int,
		secondsIdx: Int,
		notesIdx: Int,
		tz: TimeZone,
	): ParsedRow? {
		val start = parseLocalDateTime(row.getOrNull(startIdx)) ?: return null
		val startInstant = start.toInstant(tz)

		val end = parseLocalDateTime(row.getOrNull(endIdx))
		val lengthMs: Long = when {
			end != null -> {
				val d = end.toInstant(tz).minus(startInstant)
				if (d > Duration.ZERO) d.inWholeMilliseconds
				else row.getOrNull(secondsIdx)?.trim()?.toLongOrNull()?.times(1000) ?: return null
			}

			else -> row.getOrNull(secondsIdx)?.trim()?.toLongOrNull()?.times(1000) ?: return null
		}

		val notes = row.getOrNull(notesIdx)?.trim().orEmpty()
		return ParsedRow(startInstant.toEpochMilliseconds(), lengthMs, notes)
	}

	private fun parseLegacyRow(
		row: List<String>,
		dateIdx: Int,
		timeIdx: Int,
		hoursIdx: Int,
		tz: TimeZone,
	): ParsedRow? {
		val dateStr = row.getOrNull(dateIdx)?.trim() ?: return null
		val timeStr = row.getOrNull(timeIdx)?.trim() ?: return null
		val start = parseLocalDateTime("$dateStr $timeStr") ?: return null
		val startInstant = start.toInstant(tz)

		// Legacy duration was whole hours (but tolerate a decimal just in case)
		val hoursStr = row.getOrNull(hoursIdx)?.trim() ?: return null
		val hours = hoursStr.toLongOrNull()?.toDouble() ?: hoursStr.toDoubleOrNull() ?: return null
		val lengthMs = (hours * 60.0 * 60.0 * 1000.0).toLong()

		return ParsedRow(startInstant.toEpochMilliseconds(), lengthMs, "")
	}

	// endregion

	private fun formatDateTime(d: LocalDateTime): String =
		"%04d-%02d-%02d %02d:%02d:%02d".format(
			d.year, d.monthNumber, d.dayOfMonth, d.hour, d.minute, d.second
		)

	/**
	 * Parse "yyyy-MM-dd HH:mm[:ss]" (a space or 'T' separator, seconds optional).
	 * Locale-independent; returns null on any malformed input.
	 */
	private fun parseLocalDateTime(raw: String?): LocalDateTime? {
		val text = raw?.trim()?.replace('T', ' ') ?: return null
		return try {
			val parts = text.split(Regex("\\s+"))
			if (parts.size < 2) return null
			val (y, mo, d) = parts[0].split('-').map { it.toInt() }
			val timeParts = parts[1].split(':')
			val h = timeParts[0].toInt()
			val mi = timeParts.getOrNull(1)?.toInt() ?: 0
			val s = timeParts.getOrNull(2)?.toInt() ?: 0
			LocalDateTime(y, mo, d, h, mi, s)
		} catch (e: Exception) {
			null
		}
	}

	private fun FastEntry.toFastingLogEntry(): FastingLogEntry {
		val instant = Instant.fromEpochMilliseconds(start)
		val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
		return FastingLogEntry(
			id = uid,
			start = localDateTime,
			length = length.milliseconds,
			notes = notes,
		)
	}
}
