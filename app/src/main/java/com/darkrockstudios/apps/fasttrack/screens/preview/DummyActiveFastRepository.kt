package com.darkrockstudios.apps.fasttrack.screens.preview

import com.darkrockstudios.apps.fasttrack.data.activefast.ActiveFastRepository
import kotlinx.datetime.Instant
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours

/**
 * Dummy implementation of ActiveFastRepository for preview purposes
 */
class DummyActiveFastRepository(
	private val isFastingValue: Boolean = false,
	private val elapsedHoursValue: Double = 0.0
) : ActiveFastRepository {
	override fun isFasting(): Boolean = isFastingValue

	override fun getElapsedFastTime(): Duration = (elapsedHoursValue.hours)

	override fun getFastStart(): Instant? =
		if (isFastingValue) Instant.Companion.fromEpochMilliseconds(System.currentTimeMillis() - (elapsedHoursValue * 3600000).toLong())
		else null

	override fun getFastEnd(): Instant? = null

	override fun startFast(timeStarted: Instant?) {}

	override fun endFast() {}

	override fun debugOverrideFastStart(newStart: Instant) {}
}