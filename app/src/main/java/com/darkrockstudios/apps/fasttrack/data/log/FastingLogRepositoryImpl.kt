package com.darkrockstudios.apps.fasttrack.data.log

import com.darkrockstudios.apps.fasttrack.data.database.FastEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

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
