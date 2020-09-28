package com.darkrockstudios.apps.fasttrack.data.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface FastEntryDao
{
	@Query("SELECT * FROM fastentry")
	fun getAll(): List<FastEntry>

	@Query("SELECT * FROM fastentry")
	fun loadAll(): LiveData<List<FastEntry>>

	@Insert
	fun insertAll(vararg entries: FastEntry)

	@Delete
	fun delete(entry: FastEntry)
}