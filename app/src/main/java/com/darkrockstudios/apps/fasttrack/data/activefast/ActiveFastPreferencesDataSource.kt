package com.darkrockstudios.apps.fasttrack.data.activefast

import android.content.Context
import android.preference.PreferenceManager
import androidx.core.content.edit
import com.darkrockstudios.apps.fasttrack.data.Data
import kotlin.time.Instant

class ActiveFastPreferencesDataSource(appContext: Context) : ActiveFastDataSource {

	private val storage = PreferenceManager.getDefaultSharedPreferences(appContext)

	override fun getFastStart(): Instant? {
		val mills = storage.getLong(Data.KEY_FAST_START, -1)
		return if (mills > -1) {
			Instant.fromEpochMilliseconds(mills)
		} else {
			null
		}
	}

	override fun getFastEnd(): Instant? {
		val mills = storage.getLong(Data.KEY_FAST_END, -1)
		return if (mills > -1) {
			Instant.fromEpochMilliseconds(mills)
		} else {
			null
		}
	}

	override fun setFastStart(time: Instant) {
		storage.edit { putLong(Data.KEY_FAST_START, time.toEpochMilliseconds()) }
	}

	override fun setFastEnd(time: Instant) {
		storage.edit { putLong(Data.KEY_FAST_END, time.toEpochMilliseconds()) }
	}

	override fun clearFastStart() {
		storage.edit { putLong(Data.KEY_FAST_START, -1) }
	}

	override fun clearFastEnd() {
		storage.edit { putLong(Data.KEY_FAST_END, -1) }
	}
}