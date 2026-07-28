package com.darkrockstudios.apps.fasttrack.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [FastEntry::class], version = 2)
abstract class AppDatabase : RoomDatabase() {
	abstract fun fastDao(): FastEntryDao

	companion object {
		/** v2 adds the optional `notes` column to each fast entry. */
		val MIGRATION_1_2 = object : Migration(1, 2) {
			override fun migrate(db: SupportSQLiteDatabase) {
				db.execSQL("ALTER TABLE FastEntry ADD COLUMN notes TEXT NOT NULL DEFAULT ''")
			}
		}
	}
}
