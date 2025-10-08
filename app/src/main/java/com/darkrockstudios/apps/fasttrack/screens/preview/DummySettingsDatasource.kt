package com.darkrockstudios.apps.fasttrack.screens.preview

import com.darkrockstudios.apps.fasttrack.data.settings.SettingsDatasource
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
}