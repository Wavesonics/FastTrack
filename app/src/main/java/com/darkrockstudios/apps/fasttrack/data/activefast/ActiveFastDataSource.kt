package com.darkrockstudios.apps.fasttrack.data.activefast

import kotlinx.datetime.Instant

interface ActiveFastDataSource {
	fun getFastStart(): Instant?
	fun getFastEnd(): Instant?

	fun setFastStart(time: Instant)
	fun setFastEnd(time: Instant)

	fun clearFastStart()
	fun clearFastEnd()
}