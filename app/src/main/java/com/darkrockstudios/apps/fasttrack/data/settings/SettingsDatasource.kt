package com.darkrockstudios.apps.fasttrack.data.settings

import kotlinx.coroutines.flow.Flow

interface SettingsDatasource {
	fun getFastingAlerts(): Boolean
	fun setFastingAlerts(enabled: Boolean)

	fun getIntroSeen(): Boolean
	fun setIntroSeen(enabled: Boolean)

	fun getShowFancyBackground(): Boolean
	fun setShowFancyBackground(enabled: Boolean)

	fun showFancyBackgroundFlow(): Flow<Boolean>

	fun getShowFastingNotification(): Boolean
	fun setShowFastingNotification(enabled: Boolean)

	fun getUseMetricSystem(default: Boolean): Boolean
	fun setUseMetricSystem(enabled: Boolean)

	fun useMetricSystemFlow(default: Boolean): Flow<Boolean>

	fun getThemeMode(): ThemeMode
	fun setThemeMode(mode: ThemeMode)

	fun getDateStyle(): DateStyle
	fun setDateStyle(style: DateStyle)

	fun getLogViewMode(): LogViewMode
	fun setLogViewMode(mode: LogViewMode)

	fun getShowFatBurn(): Boolean
	fun setShowFatBurn(enabled: Boolean)
	fun getShowKetosis(): Boolean
	fun setShowKetosis(enabled: Boolean)
	fun getShowAutophagy(): Boolean
	fun setShowAutophagy(enabled: Boolean)

	/** Auto: reveal each phase row only once it has begun (positive countup). */
	fun getPhaseAutoMode(): Boolean
	fun setPhaseAutoMode(enabled: Boolean)

	fun phaseVisibilityFlow(): Flow<PhaseVisibility>
}

data class PhaseVisibility(
	val fatBurn: Boolean,
	val ketosis: Boolean,
	val autophagy: Boolean,
	val autoMode: Boolean,
)