package com.darkrockstudios.apps.fasttrack.data.settings

import android.content.Context
import android.preference.PreferenceManager
import androidx.core.content.edit
import com.darkrockstudios.apps.fasttrack.data.Data

class SettingsPreferencesDatasource(
	appContext: Context
) : SettingsDatasource {

	private val storage by lazy { PreferenceManager.getDefaultSharedPreferences(appContext) }

	override fun getFastingAlerts(): Boolean = storage.getBoolean(Data.KEY_FAST_ALERTS, true)

	override fun setFastingAlerts(enabled: Boolean) {
		return storage.edit {
			putBoolean(Data.KEY_FAST_ALERTS, enabled)
		}
	}

	override fun getIntroSeen(): Boolean = storage.getBoolean(Data.KEY_INTRO_SEEN, false)

	override fun setIntroSeen(enabled: Boolean) {
		storage.edit {
			putBoolean(Data.KEY_INTRO_SEEN, enabled)
		}
	}
}