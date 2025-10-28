package com.darkrockstudios.apps.fasttrack.widget

import com.darkrockstudios.apps.fasttrack.data.activefast.ActiveFastRepository
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant

class FakeActiveFastRepository : ActiveFastRepository {

	var _isFasting: Boolean = true
	var _elapsedTime: Duration = 30.hours


	override fun isFasting(): Boolean = _isFasting

	override fun getElapsedFastTime(): Duration = _elapsedTime

	override fun getFastStart(): Instant? = null

	override fun getFastEnd(): Instant? = null

	override fun startFast(timeStarted: Instant?) {}

	override fun endFast(timeEnded: Instant?) {}

	override fun debugOverrideFastStart(newStart: Instant) {}
}