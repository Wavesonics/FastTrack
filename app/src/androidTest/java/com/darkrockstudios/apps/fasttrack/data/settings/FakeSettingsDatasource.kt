package com.darkrockstudios.apps.fasttrack.data.settings

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeSettingsDatasource : SettingsDatasource {
	private var fastingAlerts: Boolean = true
	private var introSeen: Boolean = false
	private var showFancyBackground: Boolean = false
	private var showFastingNotification: Boolean = true
	private var useMetricSystem: Boolean? = null
	private var themeMode: ThemeMode = ThemeMode.SYSTEM
	private var logViewMode: LogViewMode = LogViewMode.LIST

	override fun getFastingAlerts(): Boolean = fastingAlerts
	override fun setFastingAlerts(enabled: Boolean) {
		fastingAlerts = enabled
	}

	override fun getIntroSeen(): Boolean = introSeen
	override fun setIntroSeen(enabled: Boolean) {
		introSeen = enabled
	}

	override fun getShowFancyBackground(): Boolean = showFancyBackground
	override fun setShowFancyBackground(enabled: Boolean) {
		showFancyBackground = enabled
	}

	override fun showFancyBackgroundFlow(): Flow<Boolean> = flowOf(getShowFancyBackground())
	override fun getShowFastingNotification(): Boolean = showFastingNotification
	override fun setShowFastingNotification(enabled: Boolean) {
		showFastingNotification = enabled
	}

	override fun getUseMetricSystem(default: Boolean): Boolean = useMetricSystem ?: default
	override fun setUseMetricSystem(enabled: Boolean) {
		useMetricSystem = enabled
	}

	override fun useMetricSystemFlow(default: Boolean): Flow<Boolean> =
		flowOf(getUseMetricSystem(default))

	override fun getThemeMode(): ThemeMode = themeMode
	override fun setThemeMode(mode: ThemeMode) {
		themeMode = mode
	}

	override fun getLogViewMode(): LogViewMode = logViewMode
	override fun setLogViewMode(mode: LogViewMode) {
		logViewMode = mode
	}
}
