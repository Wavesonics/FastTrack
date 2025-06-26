package com.darkrockstudios.apps.fasttrack.data.log

import com.darkrockstudios.apps.fasttrack.data.database.AppDatabase
import com.darkrockstudios.apps.fasttrack.data.database.FastEntry
import kotlinx.coroutines.flow.Flow

class FastingLogDatabaseDatasource(
	database: AppDatabase
) : FastingLogDatasource {
	private val dao = database.fastDao()

	override fun getAll(): List<FastEntry> = dao.getAll()
	override fun loadAll(): Flow<List<FastEntry>> = dao.loadAll()
	override fun insertAll(vararg newEntries: FastEntry) = dao.insertAll(*newEntries)
	override fun delete(entry: FastEntry): Boolean = dao.delete(entry) > 0
	override fun deleteByStartTime(startTime: Long): Boolean = dao.deleteByStartTime(startTime) > 0
	override fun deleteByUid(uid: Int): Boolean = dao.deleteByUid(uid) > 0
}
