package com.darkrockstudios.apps.fasttrack.data.activefast

import kotlinx.datetime.Instant
import kotlin.time.Duration

interface ActiveFastRepository {
	fun isFasting(): Boolean
	fun getElapsedFastTime(): Duration
	fun getFastStart(): Instant?
	fun getFastEnd(): Instant?
	fun startFast(timeStarted: Instant?)
	fun endFast()
	fun debugOverrideFastStart(newStart: Instant)
}