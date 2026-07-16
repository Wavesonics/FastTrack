package com.darkrockstudios.apps.fasttrack.data.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.darkrockstudios.apps.fasttrack.data.autophagyHours
import com.darkrockstudios.apps.fasttrack.data.ketosisHours
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.DurationUnit

/**
 * start  - a linux UTC epoch timestamp in milliseconds
 * length - duration in milliseconds of the fast
 * notes  - optional user note captured when a fast ends (may be empty)
 */
@Entity
data class FastEntry(
	@PrimaryKey(autoGenerate = true) val uid: Int = 0,
	@ColumnInfo val start: Long,
	@ColumnInfo val length: Long,
	@ColumnInfo(defaultValue = "''") val notes: String = ""
) {
	fun lengthHours() = length.milliseconds.toDouble(DurationUnit.HOURS)

	fun calculateKetosis(): Double = ketosisHours(length.milliseconds)

	fun calculateAutophagy(): Double = autophagyHours(length.milliseconds)
}