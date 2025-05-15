package com.darkrockstudios.apps.fasttrack.data.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.darkrockstudios.apps.fasttrack.data.Stages
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime

@Entity
data class FastEntry(
		@PrimaryKey(autoGenerate = true) val uid: Int = 0,
		@ColumnInfo val start: Long,
		@ColumnInfo val length: Long
					)
{
	@ExperimentalTime
	fun lengthHours() = length.milliseconds.toDouble(DurationUnit.HOURS)

	@ExperimentalTime
	fun calculateKetosis(): Double
	{
		val ketosisStart = Stages.PHASE_KETOSIS.hours.toDouble()
		val lenHours = lengthHours()
		return if(lenHours > ketosisStart)
		{
			return lenHours - ketosisStart
		}
		else
		{
			0.0
		}
	}

	@ExperimentalTime
	fun calculateAutophagy(): Double
	{
		val autophagyStart = Stages.PHASE_AUTOPHAGY.hours.toDouble()
		val lenHours = lengthHours()
		return if(lenHours > autophagyStart)
		{
			return lenHours - autophagyStart
		}
		else
		{
			0.0
		}
	}
}