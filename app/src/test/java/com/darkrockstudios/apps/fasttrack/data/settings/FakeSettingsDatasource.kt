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

	/**
	 * Clears all data - useful for test setup/teardown
	 */
	fun clear() {
		fastingAlerts = true
		introSeen = false
		showFancyBackground = false
		showFastingNotification = true
		useMetricSystem = null
	}
}