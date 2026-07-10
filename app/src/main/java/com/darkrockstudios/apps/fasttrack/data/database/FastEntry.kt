package com.darkrockstudios.apps.fasttrack.data.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.darkrockstudios.apps.fasttrack.data.Stages
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

	fun calculateKetosis(): Double {
		val ketosisStart = Stages.PHASE_KETOSIS.hours.toDouble()
		val lenHours = lengthHours()
		return if (lenHours > ketosisStart) {
			return lenHours - ketosisStart
		} else {
			0.0
		}
	}

	fun calculateAutophagy(): Double {
		val autophagyStart = Stages.PHASE_AUTOPHAGY.hours.toDouble()
		val lenHours = lengthHours()
		return if (lenHours > autophagyStart) {
			return lenHours - autophagyStart
		} else {
			0.0
		}
	}
}