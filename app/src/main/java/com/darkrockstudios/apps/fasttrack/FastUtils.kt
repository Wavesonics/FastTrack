package com.darkrockstudios.apps.fasttrack

import android.content.Context
import android.preference.PreferenceManager
import com.darkrockstudios.apps.fasttrack.data.Data
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

class FastUtils(appContext: Context)
{
	private val storage = PreferenceManager.getDefaultSharedPreferences(appContext)

	fun isFasting(): Boolean
	{
		val fastStart = storage.getLong(Data.KEY_FAST_START, -1)
		val fastEnd = storage.getLong(Data.KEY_FAST_END, -1)
		return fastStart != -1L && fastEnd == -1L
	}

	fun getFastStart(): Instant?
	{
		val mills = storage.getLong(Data.KEY_FAST_START, -1)
		return if(mills > -1)
		{
			Instant.fromEpochMilliseconds(mills)
		}
		else
		{
			null
		}
	}

	fun getFastEnd(): Instant?
	{
		val mills = storage.getLong(Data.KEY_FAST_END, -1)
		return if(mills > -1)
		{
			Instant.fromEpochMilliseconds(mills)
		}
		else
		{
			null
		}
	}

	@ExperimentalTime
	fun getElapsedFastTime(): Duration
	{
		val start = getFastStart()
		val end = getFastEnd()
		return if(start != null)
		{
			end?.minus(start) ?: Clock.System.now().minus(start)
		}
		else
		{
			Duration.ZERO
		}
	}
}