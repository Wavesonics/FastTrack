package com.darkrockstudios.apps.fasttrack.data.settings

interface SettingsDatasource {
	fun getFastingAlerts(): Boolean
	fun setFastingAlerts(enabled: Boolean)

	fun getIntroSeen(): Boolean
	fun setIntroSeen(enabled: Boolean)
}