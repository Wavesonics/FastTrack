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
}