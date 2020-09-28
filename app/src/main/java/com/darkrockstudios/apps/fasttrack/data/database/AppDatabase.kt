package com.darkrockstudios.apps.fasttrack.data.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [FastEntry::class], version = 1)
abstract class AppDatabase: RoomDatabase()
{
	abstract fun fastDao(): FastEntryDao
}
