package com.darkrockstudios.apps.fasttrack.data.log

import com.darkrockstudios.apps.fasttrack.data.database.FastEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * A fake implementation of FastingLogDatasource for testing purposes.
 * Uses an in-memory data store to simulate database operations.
 */
class FakeFastingLogDatasource : FastingLogDatasource {
	private val entries = mutableListOf<FastEntry>()
	private val entriesFlow = MutableStateFlow<List<FastEntry>>(emptyList())

	private var nextId = 1

	override fun getAll(): List<FastEntry> = entries.toList()

	override fun loadAll(): Flow<List<FastEntry>> = entriesFlow.asStateFlow()

	override fun insertAll(vararg newEntries: FastEntry) {
		newEntries.forEach { entry ->
			if (entry.uid == 0) {
				val newId = entry.copy(uid = nextId++)
				entries.add(newId)
			} else {
				entries.add(entry)
			}
		}

		updateFlow()
	}

	override fun update(entry: FastEntry): Boolean {
		val index = entries.indexOfFirst { it.uid == entry.uid }
		return if (index != -1) {
			entries[index] = entry
			updateFlow()
			true
		} else {
			false
		}
	}

	override fun delete(entry: FastEntry): Boolean {
		val deleted = entries.remove(entry)
		updateFlow()
		return deleted
	}

	override fun deleteByStartTime(startTime: Long): Boolean {
		val initialSize = entries.size
		entries.removeAll { it.start == startTime }
		updateFlow()
		return entries.size < initialSize
	}

	override fun deleteByUid(uid: Int): Boolean {
		val initialSize = entries.size
		entries.removeAll { it.uid == uid }
		updateFlow()
		return entries.size < initialSize
	}

	private fun updateFlow() {
		entriesFlow.value = entries.toList()
	}

	fun clear() {
		entries.clear()
		updateFlow()
	}
}
