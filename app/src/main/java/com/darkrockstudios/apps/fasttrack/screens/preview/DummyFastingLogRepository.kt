package com.darkrockstudios.apps.fasttrack.screens.preview

import com.darkrockstudios.apps.fasttrack.data.log.FastingLogEntry
import com.darkrockstudios.apps.fasttrack.data.log.FastingLogRepository
import com.darkrockstudios.apps.fasttrack.data.log.ImportResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.LocalDateTime
import kotlin.time.Duration
import kotlin.time.Instant

/**
 * Dummy implementation of FastingLogRepository for preview purposes
 */
class DummyFastingLogRepository(private val entries: List<FastingLogEntry> = emptyList()) : FastingLogRepository {
	override fun logFast(startTime: Instant, endTime: Instant, notes: String) {}
	override fun loadAll(): Flow<List<FastingLogEntry>> = flow {}
	override fun delete(item: FastingLogEntry) = true
	override fun deleteAllEntries() = entries.size
	override fun addLogEntry(start: LocalDateTime, length: Duration, notes: String) {}
	override fun updateLogEntry(entry: FastingLogEntry, start: LocalDateTime, length: Duration, notes: String) = true
	override suspend fun exportLog(): String = ""
	override suspend fun importLog(cvsExport: String) = true
	override suspend fun importEasyFastBackup(zipBytes: ByteArray) = ImportResult(0, 0, ok = true)
	override suspend fun exportIcs(): String = ""
	override suspend fun exportActivityStreams(): String = ""
	override suspend fun importIcs(icsText: String) = ImportResult(0, 0, ok = true)
	override suspend fun importActivityStreams(jsonText: String) = ImportResult(0, 0, ok = true)
}
