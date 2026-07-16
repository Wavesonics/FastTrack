package com.darkrockstudios.apps.fasttrack.data.settings

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * A fake implementation of SettingsDatasource for testing purposes.
 * Uses in-memory variables to simulate storage operations.
 */
class FakeSettingsDatasource : SettingsDatasource {
	private var fastingAlerts: Boolean = true
	private var introSeen: Boolean = false
	private var showFancyBackground: Boolean = false
	private var showFastingNotification: Boolean = true
	private var useMetricSystem: Boolean? = null
	private var themeMode: ThemeMode = ThemeMode.SYSTEM
	private var dateStyle: DateStyle = DateStyle.OPTIMIZED_COMPACT
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

	override fun getDateStyle(): DateStyle = dateStyle

	override fun setDateStyle(style: DateStyle) {
		dateStyle = style
	}

	override fun getLogViewMode(): LogViewMode = logViewMode

	override fun setLogViewMode(mode: LogViewMode) {
		logViewMode = mode
	}

	private var showFatBurn: Boolean = true
	private var showKetosis: Boolean = true
	private var showAutophagy: Boolean = true
	private var phaseAutoMode: Boolean = false

	override fun getShowFatBurn(): Boolean = showFatBurn
	override fun setShowFatBurn(enabled: Boolean) { showFatBurn = enabled }
	override fun getShowKetosis(): Boolean = showKetosis
	override fun setShowKetosis(enabled: Boolean) { showKetosis = enabled }
	override fun getShowAutophagy(): Boolean = showAutophagy
	override fun setShowAutophagy(enabled: Boolean) { showAutophagy = enabled }
	override fun getPhaseAutoMode(): Boolean = phaseAutoMode
	override fun setPhaseAutoMode(enabled: Boolean) { phaseAutoMode = enabled }

	override fun phaseVisibilityFlow(): Flow<PhaseVisibility> =
		flowOf(PhaseVisibility(showFatBurn, showKetosis, showAutophagy, phaseAutoMode))

	/**
	 * Clears all data - useful for test setup/teardown
	 */
	fun clear() {
		fastingAlerts = true
		introSeen = false
		showFancyBackground = false
		showFastingNotification = true
		useMetricSystem = null
		themeMode = ThemeMode.SYSTEM
		logViewMode = LogViewMode.LIST
	}
}