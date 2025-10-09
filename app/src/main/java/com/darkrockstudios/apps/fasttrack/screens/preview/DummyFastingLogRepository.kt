package com.darkrockstudios.apps.fasttrack.screens.preview

import com.darkrockstudios.apps.fasttrack.data.log.FastingLogEntry
import com.darkrockstudios.apps.fasttrack.data.log.FastingLogRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.LocalDateTime
import kotlin.time.Duration
import kotlin.time.Instant

/**
 * Dummy implementation of FastingLogRepository for preview purposes
 */
class DummyFastingLogRepository(private val entries: List<FastingLogEntry> = emptyList()) : FastingLogRepository {
	override fun logFast(startTime: Instant, endTime: Instant) {}
	override fun loadAll(): Flow<List<FastingLogEntry>> = flow {}
	override fun delete(item: FastingLogEntry) = true
	override fun addLogEntry(start: LocalDateTime, length: Duration) {}
	override fun updateLogEntry(entry: FastingLogEntry, start: LocalDateTime, length: Duration) = true
	override suspend fun exportLog(): String = ""
	override suspend fun importLog(cvsExport: String) = true
}
