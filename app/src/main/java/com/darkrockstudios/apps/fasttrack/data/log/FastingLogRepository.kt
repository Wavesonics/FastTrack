package com.darkrockstudios.apps.fasttrack.data.log

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDateTime
import kotlin.time.Duration
import kotlin.time.Instant

interface FastingLogRepository {
	fun logFast(startTime: Instant, endTime: Instant, notes: String = "")
	fun loadAll(): Flow<List<FastingLogEntry>>
	fun delete(item: FastingLogEntry): Boolean
	fun addLogEntry(start: LocalDateTime, length: Duration, notes: String = "")
	// notes defaults to the entry's current notes so an edit that omits them preserves them
	fun updateLogEntry(
		entry: FastingLogEntry,
		start: LocalDateTime,
		length: Duration,
		notes: String = entry.notes
	): Boolean
	suspend fun exportLog(): String
	suspend fun importLog(cvsExport: String): Boolean
}
