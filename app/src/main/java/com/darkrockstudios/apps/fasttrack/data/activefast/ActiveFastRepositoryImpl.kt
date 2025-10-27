package com.darkrockstudios.apps.fasttrack.data.activefast

import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Instant

class ActiveFastRepositoryImpl(
	private val datasource: ActiveFastDataSource,
	private val clock: Clock,
) : ActiveFastRepository {
	override fun isFasting(): Boolean {
		val fastStart = datasource.getFastStart()
		val fastEnd = datasource.getFastEnd()
		return fastStart != null && fastEnd == null
	}

	override fun getElapsedFastTime(): Duration {
		val start = datasource.getFastStart()
		val end = datasource.getFastEnd()
		return if (start != null) {
			end?.minus(start) ?: clock.now().minus(start)
		} else {
			Duration.ZERO
		}
	}

	override fun getFastStart(): Instant? = datasource.getFastStart()
	override fun getFastEnd(): Instant? = datasource.getFastEnd()

	override fun startFast(timeStarted: Instant?) {
		if (isFasting()) error("Cannot start a fast, one is already running")

		val startedMills = timeStarted ?: clock.now()

		datasource.setFastStart(startedMills)
		datasource.clearFastEnd()
	}

	override fun endFast(timeEnded: Instant?) {
		if (isFasting().not()) error("Cannot end a fast, one was not running")
		val endedMills = timeEnded ?: clock.now()
		datasource.setFastEnd(endedMills)
	}

	override fun debugOverrideFastStart(newStart: Instant) = datasource.setFastStart(newStart)
}
