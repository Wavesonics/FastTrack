package com.darkrockstudios.apps.fasttrack.data.settings

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import androidx.core.content.edit
import com.darkrockstudios.apps.fasttrack.data.Data
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class SettingsPreferencesDatasource(
	appContext: Context
) : SettingsDatasource {

	private val storage: SharedPreferences by lazy {
		PreferenceManager.getDefaultSharedPreferences(
			appContext
		)
	}

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

	override fun getShowFancyBackground() = storage.getBoolean(Data.KEY_FANCY_BACKGROUND, true)

	override fun setShowFancyBackground(enabled: Boolean) {
		storage.edit {
			putBoolean(Data.KEY_FANCY_BACKGROUND, enabled)
		}
	}

	override fun showFancyBackgroundFlow(): Flow<Boolean> = callbackFlow {
		trySend(storage.getBoolean(Data.KEY_FANCY_BACKGROUND, true))

		val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
			if (key == Data.KEY_FANCY_BACKGROUND) {
				trySend(storage.getBoolean(Data.KEY_FANCY_BACKGROUND, true))
			}
		}
		storage.registerOnSharedPreferenceChangeListener(listener)
		awaitClose { storage.unregisterOnSharedPreferenceChangeListener(listener) }
	}

	override fun getShowFastingNotification(): Boolean =
		storage.getBoolean(Data.KEY_FASTING_NOTIFICATION, true)

	override fun setShowFastingNotification(enabled: Boolean) {
		storage.edit { putBoolean(Data.KEY_FASTING_NOTIFICATION, enabled) }
	}

	override fun getUseMetricSystem(default: Boolean): Boolean =
		storage.getBoolean(Data.KEY_METRIC_SYSTEM, default)

	override fun setUseMetricSystem(enabled: Boolean) {
		storage.edit { putBoolean(Data.KEY_METRIC_SYSTEM, enabled) }
	}

	override fun useMetricSystemFlow(default: Boolean): Flow<Boolean> = callbackFlow {
		trySend(storage.getBoolean(Data.KEY_METRIC_SYSTEM, default))

		val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
			if (key == Data.KEY_METRIC_SYSTEM) {
				trySend(storage.getBoolean(Data.KEY_METRIC_SYSTEM, default))
			}
		}
		storage.registerOnSharedPreferenceChangeListener(listener)
		awaitClose { storage.unregisterOnSharedPreferenceChangeListener(listener) }
	}

	override fun getThemeMode(): ThemeMode =
		ThemeMode.fromName(storage.getString(Data.KEY_THEME_MODE, null))

	override fun setThemeMode(mode: ThemeMode) {
		storage.edit { putString(Data.KEY_THEME_MODE, mode.name) }
	}

	override fun getLogViewMode(): LogViewMode =
		LogViewMode.fromName(storage.getString(Data.KEY_LOG_VIEW_MODE, null))

	override fun setLogViewMode(mode: LogViewMode) {
		storage.edit { putString(Data.KEY_LOG_VIEW_MODE, mode.name) }
	}
}