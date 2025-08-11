package com.darkrockstudios.apps.fasttrack.data.activefast

import kotlin.time.Instant

interface ActiveFastDataSource {
	fun getFastStart(): Instant?
	fun getFastEnd(): Instant?

	fun setFastStart(time: Instant)
	fun setFastEnd(time: Instant)

	fun clearFastStart()
	fun clearFastEnd()
}