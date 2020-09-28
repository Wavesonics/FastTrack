package com.darkrockstudios.apps.fasttrack.data.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class FastEntry(
		@PrimaryKey(autoGenerate = true) val uid: Int = 0,
		@ColumnInfo val start: Long,
		@ColumnInfo val length: Long
					)