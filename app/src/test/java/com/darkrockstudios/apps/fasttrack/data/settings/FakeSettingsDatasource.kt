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

	/**
	 * Clears all data - useful for test setup/teardown
	 */
	fun clear() {
		fastingAlerts = true
		introSeen = false
		showFancyBackground = false
	}
}