package com.darkrockstudios.apps.fasttrack.data.log

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDateTime
import kotlin.time.Duration
import kotlin.time.Instant

interface FastingLogRepository {
	fun logFast(startTime: Instant, endTime: Instant)
	fun loadAll(): Flow<List<FastingLogEntry>>
	fun delete(item: FastingLogEntry): Boolean
	fun addLogEntry(start: LocalDateTime, length: Duration)
	suspend fun exportLog(): String
	suspend fun importLog(cvsExport: String): Boolean
}
