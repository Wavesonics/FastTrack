package com.darkrockstudios.apps.fasttrack.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FastEntryDao
{
	@Query("SELECT * FROM fastentry")
	fun getAll(): List<FastEntry>

	@Query("SELECT * FROM fastentry")
	fun loadAll(): Flow<List<FastEntry>>

	@Insert
	fun insertAll(vararg entries: FastEntry)

	@Update
	fun update(entry: FastEntry): Int

	@Delete
	fun delete(entry: FastEntry): Int

	@Query("DELETE FROM fastentry WHERE start = :startTime")
	fun deleteByStartTime(startTime: Long): Int

	@Query("DELETE FROM fastentry WHERE uid = :uid")
	fun deleteByUid(uid: Int): Int
}
