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
import kotlinx.datetime.UtcOffset
import kotlinx.datetime.number
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import java.util.Locale
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Instant

private val isoDurationRegex =
	Regex("""^([+-]?)P(?:(\d+)W)?(?:(\d+)D)?(?:T(?:(\d+)H)?(?:(\d+)M)?(?:(\d+)S)?)?$""")

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

	// region Backup import (shared)

	/** A fast to import: absolute epoch-millis [start, finish) plus notes. */
	private data class ImportedFast(val start: Long, val finish: Long, val notes: String)

	/**
	 * Insert [records] that don't overlap an existing entry (or an earlier
	 * record in this same batch). Overlaps are half-open [start, finish) and are
	 * skipped + counted, so repeated imports never duplicate data. Runs blocking
	 * DB I/O, so callers must invoke it on a background dispatcher.
	 */
	private fun importFasts(records: List<ImportedFast>): ImportResult {
		val intervals = datasource.getAll()
			.map { it.start to (it.start + it.length) }
			.toMutableList()

		var imported = 0
		var skippedOverlapping = 0
		for (rec in records) {
			// Ignore records without a valid finished range (never counted)
			if (rec.finish <= rec.start) continue

			val overlaps = intervals.any { (s, e) -> rec.start < e && s < rec.finish }
			if (overlaps) {
				skippedOverlapping++
				continue
			}

			datasource.insertAll(
				FastEntry(
					start = rec.start,
					length = rec.finish - rec.start,
					notes = rec.notes,
				)
			)
			intervals.add(rec.start to rec.finish)
			imported++
		}
		return ImportResult(imported, skippedOverlapping, ok = true)
	}

	// endregion

	// region EasyFast backup import

	override suspend fun importEasyFastBackup(zipBytes: ByteArray): ImportResult =
		withContext(Dispatchers.IO) {
			try {
				val json = extractFastsJson(zipBytes)
					?: return@withContext ImportResult(0, 0, ok = false)
				importFasts(parseEasyFastRecords(json))
			} catch (e: Exception) {
				Napier.e("Failed to import EasyFast backup", e)
				ImportResult(0, 0, ok = false)
			}
		}

	/** Read the `fasts.json` entry out of an EasyFast backup ZIP, or null. */
	private fun extractFastsJson(zipBytes: ByteArray): String? {
		java.util.zip.ZipInputStream(java.io.ByteArrayInputStream(zipBytes)).use { zis ->
			var entry = zis.nextEntry
			while (entry != null) {
				if (!entry.isDirectory && entry.name.substringAfterLast('/') == "fasts.json") {
					return zis.readBytes().toString(Charsets.UTF_8)
				}
				entry = zis.nextEntry
			}
		}
		return null
	}

	/** Parse the EasyFast fasts.json array; start/finish are epoch millis. */
	private fun parseEasyFastRecords(json: String): List<ImportedFast> {
		val array = org.json.JSONArray(json)
		val out = ArrayList<ImportedFast>(array.length())
		for (i in 0 until array.length()) {
			val obj = array.getJSONObject(i)
			out.add(
				ImportedFast(
					start = obj.optLong("start", 0L),
					finish = obj.optLong("finish", 0L),
					notes = obj.optString("notes", "").trim(),
				)
			)
		}
		return out
	}

	// endregion

	// region iCalendar (RFC 5545)

	override suspend fun exportIcs(): String = withContext(Dispatchers.IO) {
		val entries = datasource.getAll()
		val dtStamp = icsUtc(System.currentTimeMillis())

		val lines = ArrayList<String>()
		lines += "BEGIN:VCALENDAR"
		lines += "VERSION:2.0"
		lines += "PRODID:-//Dark Rock Studios//Fast Track//EN"
		lines += "CALSCALE:GREGORIAN"
		for (e in entries) {
			val start = e.start
			val finish = e.start + e.length
			lines += "BEGIN:VEVENT"
			lines += "UID:fasttrack-$start-$finish@darkrockstudios.com"
			lines += "DTSTAMP:$dtStamp"
			lines += "DTSTART:${icsUtc(start)}"
			lines += "DTEND:${icsUtc(finish)}"
			lines += "SUMMARY:${icsEscape("Fast — " + formatDurationFull(e.length.milliseconds))}"
			if (e.notes.isNotBlank()) lines += "DESCRIPTION:${icsEscape(e.notes)}"
			lines += "END:VEVENT"
		}
		lines += "END:VCALENDAR"

		// RFC 5545 uses CRLF line breaks and folds content lines over 75 octets.
		lines.joinToString("\r\n") { foldIcsLine(it) } + "\r\n"
	}

	override suspend fun importIcs(icsText: String): ImportResult = withContext(Dispatchers.IO) {
		try {
			importFasts(parseIcs(icsText))
		} catch (e: Exception) {
			Napier.e("Failed to import iCalendar", e)
			ImportResult(0, 0, ok = false)
		}
	}

	private fun parseIcs(icsText: String): List<ImportedFast> {
		val out = ArrayList<ImportedFast>()
		var inEvent = false
		var start: Long? = null
		var end: Long? = null
		var durationMs: Long? = null
		var desc = ""

		for (line in unfoldIcs(icsText)) {
			when (line.uppercase()) {
				"BEGIN:VEVENT" -> {
					inEvent = true; start = null; end = null; durationMs = null; desc = ""
				}

				"END:VEVENT" -> {
					if (inEvent && start != null) {
						val finish = end ?: durationMs?.let { start + it }
						if (finish != null) out += ImportedFast(start, finish, desc.trim())
					}
					inEvent = false
				}

				else -> if (inEvent) {
					val colon = line.indexOf(':')
					if (colon > 0) {
						val propPart = line.substring(0, colon)
						val value = line.substring(colon + 1)
						val segs = propPart.split(';')
						val tzid = segs.drop(1)
							.firstOrNull { it.uppercase().startsWith("TZID=") }
							?.substringAfter('=')
						when (segs[0].uppercase()) {
							"DTSTART" -> start = parseIcsDateTime(value, tzid)
							"DTEND" -> end = parseIcsDateTime(value, tzid)
							"DURATION" -> durationMs = parseIsoDurationMs(value)
							"DESCRIPTION" -> desc = icsUnescape(value)
						}
					}
				}
			}
		}
		return out
	}

	/** RFC 5545 line unfolding: a line starting with space/tab continues the prior line. */
	private fun unfoldIcs(text: String): List<String> {
		val physical = text.replace("\r\n", "\n").replace("\r", "\n").split("\n")
		val logical = ArrayList<String>()
		for (line in physical) {
			if ((line.startsWith(" ") || line.startsWith("\t")) && logical.isNotEmpty()) {
				logical[logical.size - 1] = logical[logical.size - 1] + line.substring(1)
			} else {
				logical += line
			}
		}
		return logical
	}

	/** Parse a basic-format iCalendar date-time ("YYYYMMDDThhmmss[Z]"), or null. */
	private fun parseIcsDateTime(value: String, tzid: String?): Long? {
		return try {
			val v = value.trim()
			val isUtc = v.endsWith("Z")
			val core = if (isUtc) v.dropLast(1) else v
			val datePart = core.substringBefore('T')
			val timePart = core.substringAfter('T', "")
			if (datePart.length < 8) return null
			val year = datePart.substring(0, 4).toInt()
			val month = datePart.substring(4, 6).toInt()
			val day = datePart.substring(6, 8).toInt()
			val hour = if (timePart.length >= 2) timePart.substring(0, 2).toInt() else 0
			val minute = if (timePart.length >= 4) timePart.substring(2, 4).toInt() else 0
			val second = if (timePart.length >= 6) timePart.substring(4, 6).toInt() else 0
			val ldt = LocalDateTime(year, month, day, hour, minute, second)
			val zone = when {
				isUtc -> TimeZone.UTC
				tzid != null -> runCatching { TimeZone.of(tzid) }.getOrDefault(TimeZone.currentSystemDefault())
				else -> TimeZone.currentSystemDefault()
			}
			ldt.toInstant(zone).toEpochMilliseconds()
		} catch (e: Exception) {
			null
		}
	}

	private fun icsUtc(epochMs: Long): String {
		val d = Instant.fromEpochMilliseconds(epochMs).toLocalDateTime(TimeZone.UTC)
		return String.format(
			Locale.ROOT, "%04d%02d%02dT%02d%02d%02dZ",
			d.year, d.month.number, d.day, d.hour, d.minute, d.second
		)
	}

	private fun icsEscape(text: String): String =
		text.replace("\\", "\\\\")
			.replace(";", "\\;")
			.replace(",", "\\,")
			.replace("\r\n", "\\n")
			.replace("\n", "\\n")
			.replace("\r", "\\n")

	private fun icsUnescape(text: String): String {
		val sb = StringBuilder(text.length)
		var i = 0
		while (i < text.length) {
			val c = text[i]
			if (c == '\\' && i + 1 < text.length) {
				when (val n = text[i + 1]) {
					'n', 'N' -> sb.append('\n')
					'\\' -> sb.append('\\')
					';' -> sb.append(';')
					',' -> sb.append(',')
					else -> sb.append(n)
				}
				i += 2
			} else {
				sb.append(c)
				i++
			}
		}
		return sb.toString()
	}

	/** Fold a content line to <=75 octets per physical line (continuation begins with a space). */
	private fun foldIcsLine(line: String): String {
		val out = StringBuilder()
		var octets = 0
		for (ch in line) {
			val chOctets = ch.toString().toByteArray(Charsets.UTF_8).size
			if (octets + chOctets > 75) {
				out.append("\r\n ")
				octets = 1
			}
			out.append(ch)
			octets += chOctets
		}
		return out.toString()
	}

	// endregion

	// region ActivityStreams 2.0 (JSON-LD)

	override suspend fun exportActivityStreams(): String = withContext(Dispatchers.IO) {
		val entries = datasource.getAll()
		val items = org.json.JSONArray()
		for (e in entries) {
			val start = e.start
			val finish = e.start + e.length
			val obj = org.json.JSONObject()
			obj.put("type", "Event")
			obj.put("name", "Fast")
			obj.put("startTime", isoUtc(start))
			obj.put("endTime", isoUtc(finish))
			obj.put("duration", isoDuration(e.length))
			if (e.notes.isNotBlank()) obj.put("content", e.notes)
			items.put(obj)
		}
		val root = org.json.JSONObject()
		root.put("@context", "https://www.w3.org/ns/activitystreams")
		root.put("type", "OrderedCollection")
		root.put("totalItems", entries.size)
		root.put("orderedItems", items)
		root.toString(2)
	}

	override suspend fun importActivityStreams(jsonText: String): ImportResult = withContext(Dispatchers.IO) {
		try {
			importFasts(parseActivityStreams(jsonText))
		} catch (e: Exception) {
			Napier.e("Failed to import ActivityStreams", e)
			ImportResult(0, 0, ok = false)
		}
	}

	private fun parseActivityStreams(jsonText: String): List<ImportedFast> {
		val trimmed = jsonText.trim()
		val items = org.json.JSONArray()
		if (trimmed.startsWith("[")) {
			val a = org.json.JSONArray(trimmed)
			for (i in 0 until a.length()) items.put(a.get(i))
		} else {
			val root = org.json.JSONObject(trimmed)
			val arr = root.optJSONArray("orderedItems") ?: root.optJSONArray("items")
			if (arr != null) {
				for (i in 0 until arr.length()) items.put(arr.get(i))
			} else if (root.has("startTime")) {
				items.put(root)
			}
		}

		val out = ArrayList<ImportedFast>(items.length())
		for (i in 0 until items.length()) {
			val o = items.optJSONObject(i) ?: continue
			val start = parseIso8601(o.optString("startTime", "")) ?: continue
			val end = parseIso8601(o.optString("endTime", ""))
				?: parseIsoDurationMs(o.optString("duration", ""))?.let { start + it }
				?: continue
			val notes = when {
				o.has("content") -> o.optString("content", "")
				o.has("summary") -> o.optString("summary", "")
				else -> ""
			}.trim()
			out += ImportedFast(start, end, notes)
		}
		return out
	}

	private fun isoUtc(epochMs: Long): String {
		val d = Instant.fromEpochMilliseconds(epochMs).toLocalDateTime(TimeZone.UTC)
		return String.format(
			Locale.ROOT, "%04d-%02d-%02dT%02d:%02d:%02dZ",
			d.year, d.month.number, d.day, d.hour, d.minute, d.second
		)
	}

	private fun isoDuration(ms: Long): String {
		val totalSeconds = (ms / 1000).coerceAtLeast(0)
		val h = totalSeconds / 3600
		val m = (totalSeconds % 3600) / 60
		val s = totalSeconds % 60
		return "PT${h}H${m}M${s}S"
	}

	/** Parse an extended-format ISO 8601 date-time (Z, ±offset, or floating/local), or null. */
	private fun parseIso8601(value: String): Long? {
		if (value.isBlank()) return null
		return try {
			val v = value.trim()
			val tIdx = v.indexOf('T')
			if (tIdx < 0) return null
			val datePart = v.substring(0, tIdx)
			var timePart = v.substring(tIdx + 1)

			var offsetMinutes: Int? = null // null => floating/local
			if (timePart.endsWith("Z") || timePart.endsWith("z")) {
				offsetMinutes = 0
				timePart = timePart.dropLast(1)
			} else {
				val off = maxOf(timePart.lastIndexOf('+'), timePart.lastIndexOf('-'))
				if (off > 0) {
					val sign = if (timePart[off] == '-') -1 else 1
					val offStr = timePart.substring(off + 1).replace(":", "")
					timePart = timePart.substring(0, off)
					offsetMinutes = when {
						offStr.length >= 4 -> sign * (offStr.substring(0, 2).toInt() * 60 + offStr.substring(2, 4).toInt())
						offStr.isNotEmpty() -> sign * offStr.take(2).toInt() * 60
						else -> 0
					}
				}
			}

			val dotIdx = timePart.indexOf('.')
			if (dotIdx >= 0) timePart = timePart.substring(0, dotIdx)

			val dp = datePart.split('-')
			val tp = timePart.split(':')
            val ldt = LocalDateTime(
				dp[0].toInt(), dp[1].toInt(), dp[2].toInt(),
                tp.getOrNull(0)?.toInt() ?: 0,
                tp.getOrNull(1)?.toInt() ?: 0,
                tp.getOrNull(2)?.toInt() ?: 0,
            )
			val instant = if (offsetMinutes != null) {
				ldt.toInstant(UtcOffset(hours = offsetMinutes / 60, minutes = offsetMinutes % 60))
			} else {
				ldt.toInstant(TimeZone.currentSystemDefault())
			}
			instant.toEpochMilliseconds()
		} catch (e: Exception) {
			null
		}
	}

	/** Parse an ISO 8601 duration ("PnW", "PnDTnHnMnS", …) into milliseconds, or null. */
	private fun parseIsoDurationMs(value: String): Long? {
		val m = isoDurationRegex.matchEntire(value.trim()) ?: return null
		val (sign, w, d, h, min, s) = m.destructured
		val weeks = w.toLongOrNull() ?: 0
		val days = d.toLongOrNull() ?: 0
		val hours = h.toLongOrNull() ?: 0
		val minutes = min.toLongOrNull() ?: 0
		val seconds = s.toLongOrNull() ?: 0
		val total = (((((weeks * 7 + days) * 24 + hours) * 60 + minutes) * 60 + seconds)) * 1000
		return if (sign == "-") -total else total
	}

	// endregion

	private fun formatDateTime(d: LocalDateTime): String =
		"%04d-%02d-%02d %02d:%02d:%02d".format(
			d.year, d.month.number, d.day, d.hour, d.minute, d.second
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
