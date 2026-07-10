package com.darkrockstudios.apps.fasttrack.screens.preview

import com.darkrockstudios.apps.fasttrack.data.settings.LogViewMode
import com.darkrockstudios.apps.fasttrack.data.settings.PhaseVisibility
import com.darkrockstudios.apps.fasttrack.data.settings.SettingsDatasource
import com.darkrockstudios.apps.fasttrack.data.settings.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * Dummy implementation of SettingsDatasource for preview purposes
 */
class DummySettingsDatasource(
	private val alertsEnabled: Boolean = true
) : SettingsDatasource {
	override fun getFastingAlerts(): Boolean = alertsEnabled

	override fun setFastingAlerts(enabled: Boolean) {}

	override fun getIntroSeen(): Boolean = true

	override fun setIntroSeen(enabled: Boolean) {}

	override fun getShowFancyBackground(): Boolean = true

	override fun setShowFancyBackground(enabled: Boolean) {}

	override fun showFancyBackgroundFlow(): Flow<Boolean> = flowOf(getShowFancyBackground())

	override fun getShowFastingNotification(): Boolean = true

	override fun setShowFastingNotification(enabled: Boolean) {}

	override fun getUseMetricSystem(default: Boolean): Boolean = default

	override fun setUseMetricSystem(enabled: Boolean) {}

	override fun useMetricSystemFlow(default: Boolean): Flow<Boolean> = flowOf(default)

	override fun getThemeMode(): ThemeMode = ThemeMode.SYSTEM

	override fun setThemeMode(mode: ThemeMode) {}

	override fun getLogViewMode(): LogViewMode = LogViewMode.LIST

	override fun setLogViewMode(mode: LogViewMode) {}

	override fun getShowFatBurn(): Boolean = true
	override fun setShowFatBurn(enabled: Boolean) {}
	override fun getShowKetosis(): Boolean = true
	override fun setShowKetosis(enabled: Boolean) {}
	override fun getShowAutophagy(): Boolean = true
	override fun setShowAutophagy(enabled: Boolean) {}
	override fun getPhaseAutoMode(): Boolean = false
	override fun setPhaseAutoMode(enabled: Boolean) {}
	override fun phaseVisibilityFlow(): Flow<PhaseVisibility> =
		flowOf(PhaseVisibility(fatBurn = true, ketosis = true, autophagy = true, autoMode = false))
}