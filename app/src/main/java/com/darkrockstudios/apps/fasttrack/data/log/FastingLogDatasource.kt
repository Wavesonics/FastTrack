package com.darkrockstudios.apps.fasttrack.data.log

import com.darkrockstudios.apps.fasttrack.data.database.FastEntry
import kotlinx.coroutines.flow.Flow

interface FastingLogDatasource {
	fun getAll(): List<FastEntry>
	fun loadAll(): Flow<List<FastEntry>>
	fun insertAll(vararg newEntries: FastEntry)
	fun delete(entry: FastEntry): Boolean
	fun deleteByStartTime(startTime: Long): Boolean
	fun deleteByUid(uid: Int): Boolean
}
